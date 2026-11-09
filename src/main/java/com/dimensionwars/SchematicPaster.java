package com.dimensionwars;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Minimal, dependency-free reader and paster for Sponge schematics (.schem v2/v3)
 * and for MCEdit-free Sponge v1. Only block data is pasted; block entities and
 * entities are intentionally skipped, which is all a kingdom build needs.
 *
 * <p>Pasting is sliced across ticks so a large build never blocks the server.</p>
 */
public final class SchematicPaster {

    private static final BlockData AIR = Material.AIR.createBlockData();

    private final String name;
    private final int width;
    private final int height;
    private final int length;
    private final BlockData[] palette;
    private final byte[] blockData;
    private final int expectedBlocks;

    private SchematicPaster(String name, int width, int height, int length,
                            BlockData[] palette, byte[] blockData, int expectedBlocks) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.length = length;
        this.palette = palette;
        this.blockData = blockData;
        this.expectedBlocks = expectedBlocks;
    }

    public String name() {
        return this.name;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int length() {
        return this.length;
    }

    public int blockCount() {
        return this.expectedBlocks;
    }

    // ------------------------------------------------------------------ load

    public static SchematicPaster load(InputStream raw, String fallbackName) throws IOException {
        byte[] bytes;
        try (InputStream in = raw) {
            bytes = readAll(in);
        }
        Object parsed = new Nbt(bytes).readRoot();
        if (!(parsed instanceof Map<?, ?> root)) {
            throw new IOException("schematic root is not a compound tag");
        }
        Map<?, ?> schematic = root;
        Object nested = root.get("Schematic");
        if (nested instanceof Map<?, ?> map) {
            schematic = map;
        }
        Map<?, ?> blocks = schematic.get("Blocks") instanceof Map<?, ?> map ? map : schematic;

        int width = intValue(schematic.get("Width"), 0);
        int height = intValue(schematic.get("Height"), 0);
        int length = intValue(schematic.get("Length"), 0);
        if (width <= 0 || height <= 0 || length <= 0) {
            throw new IOException("schematic has no usable dimensions");
        }

        Map<?, ?> paletteTag = blocks.get("Palette") instanceof Map<?, ?> map
                ? map : (schematic.get("Palette") instanceof Map<?, ?> map ? map : Map.of());
        byte[] data = blocks.get("Data") instanceof byte[] array
                ? array : (blocks.get("BlockData") instanceof byte[] array2 ? array2 : null);
        if (paletteTag.isEmpty() || data == null) {
            throw new IOException("schematic has no palette or block data");
        }

        int paletteMax = paletteTag.size();
        int highest = 0;
        for (Map.Entry<?, ?> entry : paletteTag.entrySet()) {
            highest = Math.max(highest, intValue(entry.getValue(), 0));
        }
        paletteMax = Math.max(paletteMax, highest + 1);

        BlockData[] palette = new BlockData[paletteMax];
        Map<Integer, String> rawPalette = new HashMap<>();
        for (Map.Entry<?, ?> entry : paletteTag.entrySet()) {
            int index = intValue(entry.getValue(), -1);
            if (index < 0 || index >= paletteMax) {
                continue;
            }
            String state = String.valueOf(entry.getKey());
            rawPalette.put(index, state);
            palette[index] = parseState(state);
        }
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == null) {
                palette[i] = AIR;
            }
        }

        long expected = (long) width * height * length;
        if (expected > Integer.MAX_VALUE) {
            throw new IOException("schematic is too large to paste: " + expected + " blocks");
        }

        String name = fallbackName;
        if (schematic.get("Metadata") instanceof Map<?, ?> metadata
                && metadata.get("Name") instanceof String metaName && !metaName.isBlank()) {
            name = metaName;
        }
        return new SchematicPaster(name, width, height, length, palette, data, (int) expected);
    }

    private static BlockData parseState(String state) {
        try {
            return Bukkit.createBlockData(state);
        } catch (RuntimeException ex) {
            String base = state;
            int bracket = base.indexOf('[');
            if (bracket > 0) {
                base = base.substring(0, bracket);
            }
            Material material = Material.matchMaterial(base.toUpperCase(Locale.ROOT));
            if (material == null) {
                material = Material.matchMaterial(base);
            }
            return material == null || !material.isBlock() ? AIR : material.createBlockData();
        }
    }

    // ----------------------------------------------------------------- paste

    /**
     * Starts a sliced paste. The callback runs on the main server thread once every
     * block has been placed (or the world unloaded).
     */
    public PasteJob paste(Plugin plugin, World world, Location corner, int blocksPerTick,
                          boolean placeAir, Runnable whenDone) {
        PasteJob job = new PasteJob(plugin, world, corner, Math.max(500, blocksPerTick), placeAir, whenDone, this);
        job.start();
        return job;
    }

    public static final class PasteJob {
        private final Plugin plugin;
        private final World world;
        private final Location corner;
        private final int blocksPerTick;
        private final boolean placeAir;
        private final Runnable whenDone;
        private final SchematicPaster schematic;
        private final int total;
        private BukkitTask task;
        private int cursor;
        private int dataPos;
        private int placed;
        private int skipped;

        private PasteJob(Plugin plugin, World world, Location corner, int blocksPerTick, boolean placeAir,
                         Runnable whenDone, SchematicPaster schematic) {
            this.plugin = plugin;
            this.world = world;
            this.corner = corner;
            this.blocksPerTick = blocksPerTick;
            this.placeAir = placeAir;
            this.whenDone = whenDone;
            this.schematic = schematic;
            this.total = schematic.expectedBlocks;
        }

        public int placed() {
            return this.placed;
        }

        public int skipped() {
            return this.skipped;
        }

        public int total() {
            return this.total;
        }

        public void start() {
            this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::slice, 1L, 1L);
        }

        public void cancel() {
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }
        }

        private void slice() {
            if (this.world == null || !this.world.equals(this.corner.getWorld())) {
                this.finish();
                return;
            }
            int budget = this.blocksPerTick;
            byte[] data = this.schematic.blockData;
            int width = this.schematic.width;
            int length = this.schematic.length;
            int baseX = this.corner.getBlockX();
            int baseY = this.corner.getBlockY();
            int baseZ = this.corner.getBlockZ();
            while (budget-- > 0 && this.cursor < this.total) {
                int value = 0;
                int shift = 0;
                while (this.dataPos < data.length) {
                    int b = data[this.dataPos++] & 0xFF;
                    value |= (b & 0x7F) << shift;
                    if ((b & 0x80) == 0) {
                        break;
                    }
                    shift += 7;
                }
                int index = this.cursor++;
                BlockData state = value < this.schematic.palette.length
                        ? this.schematic.palette[value] : AIR;
                if (state == AIR && !this.placeAir) {
                    this.skipped++;
                    continue;
                }
                int x = index % width;
                int z = (index / width) % length;
                int y = index / (width * length);
                this.world.getBlockAt(baseX + x, baseY + y, baseZ + z).setBlockData(state, false);
                this.placed++;
            }
            if (this.cursor >= this.total) {
                this.finish();
            }
        }

        private void finish() {
            this.cancel();
            if (this.whenDone != null) {
                this.whenDone.run();
            }
        }
    }

    // ------------------------------------------------------------------- NBT

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static int intValue(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    /** Tiny big-endian NBT reader, enough for schematic files. */
    private static final class Nbt {
        private final byte[] data;
        private int pos;

        Nbt(byte[] data) throws IOException {
            if (data.length >= 2 && (data[0] & 0xFF) == 0x1F && (data[1] & 0xFF) == 0x8B) {
                this.data = readAll(new GZIPInputStream(new java.io.ByteArrayInputStream(data), 1 << 16));
            } else {
                this.data = data;
            }
        }

        Object readRoot() throws IOException {
            int type = this.u8();
            if (type != 10) {
                throw new IOException("root tag is not a compound (type " + type + ")");
            }
            this.skipString();
            return this.readPayload(type);
        }

        private Object readPayload(int type) throws IOException {
            switch (type) {
                case 1: return this.i8();
                case 2: return this.i16();
                case 3: return this.i32();
                case 4: return this.i64();
                case 5: return Float.intBitsToFloat(this.i32());
                case 6: return Double.longBitsToDouble(this.i64());
                case 7: {
                    int n = this.i32();
                    byte[] out = new byte[n];
                    System.arraycopy(this.data, this.pos, out, 0, n);
                    this.pos += n;
                    return out;
                }
                case 8: return this.readString();
                case 9: {
                    int elementType = this.u8();
                    int n = this.i32();
                    List<Object> list = new ArrayList<>(Math.min(n, 1 << 20));
                    for (int i = 0; i < n; i++) {
                        list.add(this.readPayload(elementType));
                    }
                    return list;
                }
                case 10: {
                    Map<String, Object> map = new HashMap<>();
                    while (true) {
                        int childType = this.u8();
                        if (childType == 0) {
                            return map;
                        }
                        String key = this.readString();
                        map.put(key, this.readPayload(childType));
                    }
                }
                case 11: {
                    int n = this.i32();
                    int[] out = new int[n];
                    for (int i = 0; i < n; i++) {
                        out[i] = this.i32();
                    }
                    return out;
                }
                case 12: {
                    int n = this.i32();
                    long[] out = new long[n];
                    for (int i = 0; i < n; i++) {
                        out[i] = this.i64();
                    }
                    return out;
                }
                default:
                    throw new IOException("unsupported NBT tag type " + type);
            }
        }

        private int u8() throws IOException {
            if (this.pos >= this.data.length) {
                throw new IOException("unexpected end of NBT data");
            }
            return this.data[this.pos++] & 0xFF;
        }

        private byte i8() throws IOException {
            return (byte) this.u8();
        }

        private short i16() throws IOException {
            return (short) ((this.u8() << 8) | this.u8());
        }

        private int i32() throws IOException {
            return (this.u8() << 24) | (this.u8() << 16) | (this.u8() << 8) | this.u8();
        }

        private long i64() throws IOException {
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | this.u8();
            }
            return value;
        }

        private String readString() throws IOException {
            int n = ((this.u8() << 8) | this.u8());
            String value = new String(this.data, this.pos, n, java.nio.charset.StandardCharsets.UTF_8);
            this.pos += n;
            return value;
        }

        private void skipString() throws IOException {
            int n = ((this.u8() << 8) | this.u8());
            this.pos += n;
        }
    }
}
