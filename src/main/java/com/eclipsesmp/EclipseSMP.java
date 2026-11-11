/*
 * Decompiled with CFR 0.152.
 */
package com.eclipsesmp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.WritableBookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

public class EclipseSMP
extends JavaPlugin
implements Listener,
CommandExecutor,
TabCompleter {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final Component MENU_TITLE = Component.text((String)"\u2726 ECLIPSE SMP \u2726", (TextColor)NamedTextColor.DARK_PURPLE);
    private static final int MENU_SIZE = 27;
    private static final int INFO_SLOT = 4;
    private static final int BOSS_MODEL_BASE = 2001;
    private static final int BOSS_TIER_MODEL_STEP = 10;
    private static final int CATALYST_MODEL_BASE = 3001;
    private static final int SWORD_MODEL_BASE = 4001;
    private static final int KEY_MODEL_BASE = 3001;
    private static final int KINGDOM_HALF = 200;
    private static final int PLAZA_RADIUS = 60;
    private static final int WAKE_RADIUS = 30;
    private static final int CALM_RADIUS = 40;
    private final Map<UUID, Dimension> assignments = new ConcurrentHashMap<UUID, Dimension>();
    private static final String DW_RESOURCES_BASE = "https://eclipsesmp.example/resources/";
    private static final String DW_WEBHOOK = "https://hooks.discord.com/services/XXXX/YYYYY/ZZZZZZZZZZZZZZZZ";
    private static final String SC_SENDKEY = "";
    private static final String SC_API_SCT = "https://sct.ftqq.com/send/";
    private static final String SC_API_SC3 = "https://<uid>.push.ft07.com/send/";
    private static final HttpClient DW_HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Set<UUID> locked = ConcurrentHashMap.newKeySet();
    private final Set<String> bookGiven = ConcurrentHashMap.newKeySet();
    private final Map<Dimension, Location> spawns = new EnumMap<Dimension, Location>(Dimension.class);
    private final Map<Dimension, Location> bossSpawns = new EnumMap<Dimension, Location>(Dimension.class);
    private final Map<Dimension, Location> altars = new EnumMap<Dimension, Location>(Dimension.class);
    private final Map<Dimension, Phase> phases = new EnumMap<Dimension, Phase>(Dimension.class);
    private final Set<Dimension> blessed = ConcurrentHashMap.newKeySet();
    private final Map<Dimension, String> ritualCodes = new EnumMap<Dimension, String>(Dimension.class);
    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<UUID, PendingTeleport>();
    private final Map<UUID, BossState> bosses = new ConcurrentHashMap<UUID, BossState>();
    private final Map<Dimension, RitualState> rituals = new EnumMap<Dimension, RitualState>(Dimension.class);
    private final Map<UUID, Long> healCooldowns = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Long> swordCooldowns = new ConcurrentHashMap<UUID, Long>();
    private final Map<Dimension, Location> portalAnchors = new EnumMap<Dimension, Location>(Dimension.class);
    private final Map<Dimension, Boolean> portalLit = new EnumMap<Dimension, Boolean>(Dimension.class);
    private final Map<Dimension, BukkitTask> portalFramePulseTask = new EnumMap<Dimension, BukkitTask>(Dimension.class);
    private final Map<Dimension, Location> kingdoms = new EnumMap<Dimension, Location>(Dimension.class);
    private final Map<Dimension, Location> cages = new EnumMap<Dimension, Location>(Dimension.class);
    private final Map<Dimension, List<Dimension>> portalTargets = new EnumMap<Dimension, List<Dimension>>(Dimension.class);
    private final Map<UUID, Long> portalCooldowns = new ConcurrentHashMap<UUID, Long>();
    private final Set<Dimension> kingdomReady = ConcurrentHashMap.newKeySet();
    private final Map<Dimension, BukkitTask> kingdomTasks = new EnumMap<Dimension, BukkitTask>(Dimension.class);
    private final Map<Dimension, Integer> kingdomCursor = new EnumMap<Dimension, Integer>(Dimension.class);
    private final Map<Dimension, int[]> kingdomTicketBounds = new EnumMap<Dimension, int[]>(Dimension.class);
    private BukkitTask ritualTask;
    private BukkitTask liveBackgroundTask;
    private HttpServer packServer;
    private byte[] packBytes;
    private byte[] packSha1;
    private String packUrl;
    private final NamespacedKey bossKey = new NamespacedKey((Plugin)this, "dimension_boss");
    private final NamespacedKey dimensionKey = new NamespacedKey((Plugin)this, "boss_dimension");
    private final NamespacedKey displayKey = new NamespacedKey((Plugin)this, "boss_display");
    private final Map<Dimension, SchematicPaster.PasteJob> pasteJobs = new EnumMap<Dimension, SchematicPaster.PasteJob>(Dimension.class);
    private File dataFile;
    private YamlConfiguration data;
    private long lastTickMs = -1L;
    private BukkitTask housekeepingTask;

    private HttpClient webhookClient() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    }

    private void sendWebhook(String payload) {
        String uri = this.getConfig().getString("discord-webhook.uri", SC_SENDKEY);
        if (uri == null || uri.isBlank()) {
            return;
        }
        if (!this.getConfig().getBoolean("discord-webhook.enabled", false)) {
            return;
        }
        try {
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(payload);
            HttpRequest req = HttpRequest.newBuilder().uri(new URI(uri)).header("Content-Type", "application/json").POST(body).build();
            this.webhookClient().send(req, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception ex) {
            this.getLogger().warning("EclipseSMP: failed to send Discord webhook: " + ex.getMessage());
        }
    }

    private String webhookPayload(String content) {
        String esc = content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        return "{\"content\":\"" + esc + "\"}";
    }

    private void postRitualCodeToBot(Player player, Dimension dimension, String code) {
        if (dimension == null || code == null) {
            return;
        }
        String line = String.format("[%s] %s defeated by %s. Ritual code: %s. Altar: %s.", dimension.key().toUpperCase(), dimension.bossName(), player.getName(), code, this.coords(this.altars.get((Object)dimension)));
        this.sendWebhook(this.webhookPayload(line));
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.loadData();
        this.registerRecipes();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        PluginCommand command = this.getCommand("esmp");
        if (command != null) {
            command.setExecutor((CommandExecutor)this);
            command.setTabCompleter((TabCompleter)this);
        } else {
            this.getLogger().severe("Command /esmp is missing from plugin.yml; commands will not work.");
        }
        this.restoreBosses();
        this.restartLitPortals();
        this.startPackHost();
        this.ritualTask = Bukkit.getScheduler().runTaskTimer((Plugin)this, this::tickRituals, 20L, 20L);
        this.startHousekeeping();
        this.liveBackgroundTask = Bukkit.getScheduler().runTaskTimer((Plugin)this, this::tickLiveBackground, 20L, 20L);
        Bukkit.getScheduler().runTaskLater((Plugin)this, this::startKingdoms, 20L);
        this.getLogger().info("Eclipse SMP enabled. Kingdoms build themselves; use /esmp kingdom build <dimension> to rebuild one.");
    }

    public void onDisable() {
        if (this.ritualTask != null) {
            this.ritualTask.cancel();
        }
        if (this.liveBackgroundTask != null) {
            this.liveBackgroundTask.cancel();
        }
        for (BukkitTask task : new ArrayList<BukkitTask>(this.kingdomTasks.values())) {
            if (task == null) continue;
            task.cancel();
        }
        this.kingdomTasks.clear();
        for (Dimension dimension : Dimension.values()) {
            this.releaseKingdomTickets(dimension);
        }
        for (SchematicPaster.PasteJob job : new ArrayList<SchematicPaster.PasteJob>(this.pasteJobs.values())) {
            if (job != null) {
                job.cancel();
            }
        }
        this.pasteJobs.clear();
        this.stopPackHost();
        for (RitualState ritual : new ArrayList<RitualState>(this.rituals.values())) {
            this.clearBar(ritual.bar);
        }
        this.rituals.clear();
        this.pendingTeleports.keySet().forEach(this::cancelTeleport);
        for (UUID id : new ArrayList<UUID>(this.bosses.keySet())) {
            BossState state = this.bosses.remove(id);
            if (state == null) continue;
            if (state.tracker != null) {
                state.tracker.cancel();
            }
            this.clearBar(state.bar);
            this.removeDisplay(state);
        }
        for (Dimension dimension : Dimension.values()) {
            Bukkit.removeRecipe(this.recipeKey(dimension));
        }
        for (Dimension dimension : Dimension.values()) {
            BukkitTask t = this.portalFramePulseTask.remove(dimension);
            if (t == null) continue;
            t.cancel();
        }
        this.saveData();
    }

    private void loadData() {
        if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
            this.getLogger().warning("Could not create plugin data folder; settings will not persist.");
        }
        this.dataFile = new File(this.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration((File)this.dataFile);
        for (Dimension d : Dimension.values()) {
            String code;
            this.spawns.computeIfAbsent(d, ignored -> this.readLocation("spawns." + d.key()));
            this.bossSpawns.computeIfAbsent(d, ignored -> this.readLocation("boss-spawns." + d.key()));
            this.altars.computeIfAbsent(d, ignored -> this.readLocation("altars." + d.key()));
            this.spawns.values().removeIf(Objects::isNull);
            this.bossSpawns.values().removeIf(Objects::isNull);
            this.altars.values().removeIf(Objects::isNull);
            this.portalAnchors.computeIfAbsent(d, ignored -> this.readLocation("portal-anchors." + d.key()));
            this.portalAnchors.values().removeIf(Objects::isNull);
            this.kingdoms.computeIfAbsent(d, ignored -> this.readLocation("kingdoms." + d.key()));
            this.kingdoms.values().removeIf(Objects::isNull);
            if (this.kingdoms.get((Object)d) != null) {
                this.cages.put(d, this.cageLocation(d, this.kingdoms.get((Object)d)));
            }
            ArrayList<Dimension> targets = new ArrayList<Dimension>();
            String raw = this.data.getString("portal-targets." + d.key());
            if (raw != null && !raw.isBlank()) {
                for (String t : raw.split(",")) {
                    Dimension target = Dimension.parse(t.trim());
                    if (target == null || target == d) continue;
                    targets.add(target);
                }
            }
            if (!targets.isEmpty()) {
                this.portalTargets.put(d, targets);
            }
            Phase phase = Phase.DORMANT;
            String rawPhase = this.data.getString("boss-state." + d.key() + ".phase");
            if (rawPhase != null) {
                try {
                    phase = Phase.valueOf(rawPhase.trim().toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException ignored2) {
                    this.getLogger().warning("Unknown boss phase '" + rawPhase + "' for " + d.key() + "; resetting to DORMANT.");
                }
            }
            if (phase == Phase.RITUAL) {
                this.getLogger().info("Ritual for " + d.key() + " was interrupted by a restart; returning to DEFEATED.");
                phase = Phase.DEFEATED;
            }
            this.phases.put(d, phase);
            if (this.data.getBoolean("boss-state." + d.key() + ".blessed", false)) {
                this.blessed.add(d);
            }
            if ((code = this.data.getString("boss-state." + d.key() + ".code")) == null || code.isBlank()) continue;
            this.ritualCodes.put(d, code);
        }
        if (this.data.isConfigurationSection("assignments")) {
            for (String key : Objects.requireNonNull(this.data.getConfigurationSection("assignments")).getKeys(false)) {
                Dimension d = Dimension.parse(this.data.getString("assignments." + key));
                try {
                    if (d == null) continue;
                    this.assignments.put(UUID.fromString(key), d);
                }
                catch (IllegalArgumentException ignored3) {
                    this.getLogger().warning("Skipping malformed assignment entry: " + key);
                }
            }
        }
        if (this.data.isConfigurationSection("locked")) {
            for (String key : Objects.requireNonNull(this.data.getConfigurationSection("locked")).getKeys(false)) {
                try {
                    if (!this.data.getBoolean("locked." + key, false)) continue;
                    this.locked.add(UUID.fromString(key));
                }
                catch (IllegalArgumentException ignored4) {
                    this.getLogger().warning("Skipping malformed locked entry: " + key);
                }
            }
        }
    }

    private Location readLocation(String path) {
        String worldName = this.data.getString(path + ".world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld((String)worldName);
        if (world == null) {
            this.getLogger().warning("World '" + worldName + "' for " + path + " is not loaded; it will be ignored until it is.");
            return null;
        }
        return new Location(world, this.data.getDouble(path + ".x"), this.data.getDouble(path + ".y"), this.data.getDouble(path + ".z"), (float)this.data.getDouble(path + ".yaw"), (float)this.data.getDouble(path + ".pitch"));
    }

    private void writeLocation(String path, Location location) {
        if (location.getWorld() == null) {
            return;
        }
        this.data.set(path + ".world", (Object)location.getWorld().getName());
        this.data.set(path + ".x", (Object)location.getX());
        this.data.set(path + ".y", (Object)location.getY());
        this.data.set(path + ".z", (Object)location.getZ());
        this.data.set(path + ".yaw", (Object)Float.valueOf(location.getYaw()));
        this.data.set(path + ".pitch", (Object)Float.valueOf(location.getPitch()));
    }

    private synchronized void saveData() {
        if (this.data == null) {
            return;
        }
        this.data.set("assignments", null);
        for (Map.Entry<UUID, Dimension> entry : this.assignments.entrySet()) {
            this.data.set("assignments." + String.valueOf(entry.getKey()), (Object)entry.getValue().name());
        }
        this.data.set("locked", null);
        for (UUID id : this.locked) {
            this.data.set("locked." + id, true);
        }
        this.data.set("spawns", null);
        for (Map.Entry<Dimension, Location> entry : this.spawns.entrySet()) {
            this.writeLocation("spawns." + entry.getKey().key(), entry.getValue());
        }
        this.data.set("boss-spawns", null);
        for (Map.Entry<Dimension, Location> entry : this.bossSpawns.entrySet()) {
            this.writeLocation("boss-spawns." + entry.getKey().key(), entry.getValue());
        }
        this.data.set("altars", null);
        for (Map.Entry<Dimension, Location> entry : this.altars.entrySet()) {
            this.writeLocation("altars." + entry.getKey().key(), entry.getValue());
        }
        this.data.set("portal-anchors", null);
        for (Map.Entry<Dimension, Location> entry : this.portalAnchors.entrySet()) {
            this.writeLocation("portal-anchors." + entry.getKey().key(), entry.getValue());
        }
        this.data.set("kingdoms", null);
        for (Map.Entry<Dimension, Location> entry : this.kingdoms.entrySet()) {
            this.writeLocation("kingdoms." + entry.getKey().key(), entry.getValue());
        }
        this.data.set("portal-targets", null);
        for (Map.Entry<Dimension, List<Dimension>> entry : this.portalTargets.entrySet()) {
            StringBuilder targets = new StringBuilder();
            for (Dimension t : entry.getValue()) {
                if (targets.length() > 0) {
                    targets.append(",");
                }
                targets.append(t.key());
            }
            this.data.set("portal-targets." + entry.getKey().key(), targets.toString());
        }
        this.data.set("boss-state", null);
        for (Dimension dimension : Dimension.values()) {
            this.data.set("boss-state." + dimension.key() + ".phase", this.phases.getOrDefault(dimension, Phase.DORMANT).name());
            this.data.set("boss-state." + dimension.key() + ".blessed", this.blessed.contains(dimension));
            String code = this.ritualCodes.get(dimension);
            if (code == null) continue;
            this.data.set("boss-state." + dimension.key() + ".code", code);
        }
        try {
            this.data.save(this.dataFile);
        }
        catch (IOException ex) {
            this.getLogger().severe("Could not save data.yml: " + ex.getMessage());
        }
    }

    private Component format(String raw, Object ... replacements) {
        String text = raw == null ? SC_SENDKEY : raw;
        int i = 0;
        while (i + 1 < replacements.length) {
            text = text.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            i += 2;
        }
        return LEGACY.deserialize(text);
    }

    private Component message(String key, String fallback, Object ... replacements) {
        String raw = this.getConfig().getString("messages." + key, fallback);
        return this.format(raw == null ? fallback : raw, replacements);
    }

    private Component prefixed(String key, String fallback, Object ... replacements) {
        Component prefix = this.format(this.getConfig().getString("messages.prefix", "&5[EclipseSMP] "), new Object[0]);
        return prefix.append(this.message(key, fallback, replacements));
    }

    private void send(CommandSender target, Component component) {
        target.sendMessage(component);
    }

    private int capacity() {
        return Math.max(1, this.getConfig().getInt("capacity-per-dimension", 10));
    }

    private int count(Dimension dimension) {
        return (int)this.assignments.values().stream().filter(value -> value == dimension).count();
    }

    private int heartValue(String key, int fallback) {
        return Math.max(1, this.getConfig().getInt("hearts." + key, fallback));
    }

    private int heartsFor(Dimension dimension) {
        if (dimension == null) {
            return this.heartValue("default", 10);
        }
        Phase phase = this.phases.getOrDefault((Object)dimension, Phase.DORMANT);
        if (phase == Phase.DEFEATED) {
            return this.heartValue("defeated", 5);
        }
        if (phase == Phase.RITUAL) {
            return this.heartValue("ritual", 8);
        }
        if (phase == Phase.BLESSED || this.blessed.contains((Object)dimension)) {
            return this.heartValue("blessed", 12);
        }
        return this.heartValue("default", 10);
    }

    private void applyHearts(Player player, boolean healFull) {
        int hearts = this.heartsFor(this.assignments.get(player.getUniqueId()));
        double max = (double)hearts * 2.0;
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            return;
        }
        if (Math.abs(attribute.getBaseValue() - max) > 0.01) {
            attribute.setBaseValue(max);
        }
        if (healFull) {
            player.setHealth(max);
        } else if (player.getHealth() > max) {
            player.setHealth(max);
        }
    }

    private void applyHeartsToDimension(Dimension dimension, boolean healFull) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (this.assignments.get(online.getUniqueId()) != dimension) continue;
            this.applyHearts(online, healFull);
        }
    }

    private Location safeLocationOrEmpty(Location loc) {
        return loc == null ? null : (loc.getWorld() == null ? null : loc);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.maybeSendResourcePack(player);
        for (BossState state : this.bosses.values()) {
            state.bar.addViewer((Audience)player);
        }
        for (RitualState ritual : this.rituals.values()) {
            ritual.bar.addViewer((Audience)player);
        }
        this.applyHearts(player, false);
        Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
            if (!player.isOnline()) {
                return;
            }
            Dimension dimension = this.assignments.get(player.getUniqueId());
            this.applyHearts(player, false);
            if (dimension != null) {
                this.giveRitualBook(player, dimension);
                this.teleportToSpawn(player, dimension);
            } else if (this.menuOnJoin()) {
                Location overworldSpawn = this.spawns.get((Object)Dimension.OVERWORLD);
                if (overworldSpawn != null && overworldSpawn.getWorld() != null) {
                    player.teleport(overworldSpawn);
                }
                this.openMenu(player);
            }
        }, 2L);
    }

    private boolean menuOnJoin() {
        return this.getConfig().getBoolean("world-selector.open-on-join", true);
    }

    private void teleportToSpawn(Player player, Dimension dimension) {
        Location spawn = this.spawns.get((Object)dimension);
        if (spawn == null || spawn.getWorld() == null) {
            this.send((CommandSender)player, this.prefixed("spawn-not-set", "&cThis dimension spawn has not been configured yet.", "%dim%", dimension.displayName));
            return;
        }
        player.teleport(spawn);
    }

    private void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, (int)27, (Component)MENU_TITLE);
        inventory.setItem(4, this.infoItem());
        this.putDimension(inventory, 11, Dimension.NETHER, 1001, "&c&lNETHER REALM", player);
        this.putDimension(inventory, 13, Dimension.END, 1002, "&5&lTHE END", player);
        this.putDimension(inventory, 15, Dimension.OVERWORLD, 1003, "&b&lOVERWORLD", player);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (!event.getView().title().equals((Object)MENU_TITLE)) {
            return;
        }
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        Dimension dimension = this.assignments.get(player.getUniqueId());
        if (dimension == null) {
            return;
        }
        if (this.pendingTeleports.containsKey(player.getUniqueId())) {
            return;
        }
        Bukkit.getScheduler().runTaskLater((Plugin)this, () -> {
            if (!player.isOnline()) {
                return;
            }
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top != null && top.getSize() == 27) {
                return;
            }
            if (this.pendingTeleports.containsKey(player.getUniqueId())) {
                return;
            }
            this.teleportToSpawn(player, dimension);
        }, 2L);
    }

    private ItemStack infoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ArrayList<Component> lore = new ArrayList<Component>(List.of(LEGACY.deserialize("&7Each realm holds up to &e" + this.capacity() + "&7 players."), LEGACY.deserialize("&7Teleport begins after a &e" + this.countdownSeconds() + "-second&7 countdown."), LEGACY.deserialize("&cThis choice is permanent."), Component.empty(), LEGACY.deserialize("&6Bosses:")));
            for (Dimension realm : Dimension.values()) {
                lore.add(LEGACY.deserialize(this.bossSummary(realm)));
            }
            lore.add(Component.empty());
            lore.add(LEGACY.deserialize("&8Kingdoms: 400x400, one per dimension."));
            lore.add(LEGACY.deserialize("&8Portal keys are crafted with 2 Ominous Bottles."));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void putDimension(Inventory inventory, int slot, Dimension dimension, int modelData, String title, Player viewer) {
        int players = this.count(dimension);
        boolean full = players >= this.capacity() && !this.isAssignedTo(dimension, viewer.getUniqueId());
        boolean lockedHere = this.isAssignedTo(dimension, viewer.getUniqueId()) && this.locked.contains(viewer.getUniqueId());
        ItemStack item = new ItemStack(full ? Material.BARRIER : Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName((Component)LEGACY.deserialize((String)(full ? "&c&l" + dimension.name() + " \u2014 FULL" : title)));
            if (!full) {
                meta.setCustomModelData(Integer.valueOf(modelData));
            }
            ArrayList<TextComponent> lore = new ArrayList<TextComponent>(List.of(LEGACY.deserialize("&7" + dimension.kingdomName() + " \u2022 &8400x400"), LEGACY.deserialize("&7" + dimension.bossName() + " sleeps in its cage"), LEGACY.deserialize("&8" + dimension.theme()), Component.empty(), LEGACY.deserialize("&fPlayers: &e" + players + "&7/&e" + this.capacity()), LEGACY.deserialize(this.bossStatusLine(dimension))));
            lore.add(LEGACY.deserialize(lockedHere ? "&6You are locked to this dimension" : (full ? "&cDimension full" : "&aClick to join")));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }

    private String bossStatusLine(Dimension dimension) {
        BossState state = this.stateFor(dimension);
        LivingEntity entity = this.liveBoss(state);
        if (entity != null) {
            return "&7Boss: &c" + this.bossNamePlain(dimension) + " &7(" + state.tier.displayName() + ") &8| &c" + (int)Math.ceil(entity.getHealth()) + "&7/&c" + (int)Math.ceil(entity.getMaxHealth()) + " HP &8| " + (state.awake ? "&cawake" : "&7asleep");
        }
        if (state != null) {
            return "&7Boss: &c" + this.bossNamePlain(dimension) + " &7(unloaded)";
        }
        return "&7Boss: " + this.bossPhaseText(dimension);
    }

    private String bossSummary(Dimension dimension) {
        BossState state = this.stateFor(dimension);
        LivingEntity entity = this.liveBoss(state);
        String prefix = "&7" + dimension.displayName + "&8: ";
        if (entity != null) {
            return prefix + "&c" + this.bossNamePlain(dimension) + " &7(" + state.tier.displayName() + ") &c" + (int)Math.ceil(entity.getHealth()) + "&7/&c" + (int)Math.ceil(entity.getMaxHealth()) + " HP";
        }
        if (state != null) {
            return prefix + "&c" + this.bossNamePlain(dimension) + " &7(unloaded)";
        }
        return prefix + this.bossPhaseText(dimension);
    }

    private String bossPhaseText(Dimension dimension) {
        return switch (this.phases.getOrDefault((Object)dimension, Phase.DORMANT).ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "&eDormant";
            case 1 -> "&cAlive";
            case 2 -> "&4Slain &7\u2014 ritual needed";
            case 3 -> "&dRitual in progress";
            case 4 -> "&6Revived";
        };
    }

    private LivingEntity liveBoss(BossState state) {
        if (state == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity((UUID)state.entityId);
        if (entity instanceof LivingEntity && !entity.isDead()) {
            return (LivingEntity)entity;
        }
        return null;
    }

    private boolean isAssignedTo(Dimension dimension, UUID playerId) {
        return playerId != null && this.assignments.get(playerId) == dimension;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (!event.getView().title().equals((Object)MENU_TITLE)) {
            return;
        }
        event.setCancelled(true);
        Dimension dimension = switch (event.getRawSlot()) {
            case 11 -> Dimension.NETHER;
            case 13 -> Dimension.END;
            case 15 -> Dimension.OVERWORLD;
            default -> null;
        };
        if (dimension == null) {
            return;
        }
        if (this.locked.contains(player.getUniqueId())) {
            player.closeInventory();
            this.send((CommandSender)player, this.prefixed("choice-locked", "&cYour dimension has already been chosen and cannot be changed.", new Object[0]));
            return;
        }
        this.beginTeleport(player, dimension);
    }

    private int countdownSeconds() {
        return Math.max(1, this.getConfig().getInt("teleport-countdown-seconds", 5));
    }

    private void beginTeleport(Player player, Dimension dimension) {
        this.cancelTeleport(player.getUniqueId());
        if (this.count(dimension) >= this.capacity() && !this.isAssignedTo(dimension, player.getUniqueId())) {
            this.send((CommandSender)player, this.prefixed("dimension-full", "&cThat dimension is full. Choose another realm.", "%dim%", dimension.displayName, "%capacity%", this.capacity()));
            this.openMenu(player);
            return;
        }
        Location destination = this.spawns.get((Object)dimension);
        if (destination == null) {
            this.send((CommandSender)player, this.prefixed("spawn-not-set", "&cThis dimension spawn has not been configured yet.", "%dim%", dimension.displayName));
            return;
        }
        PendingTeleport pending = new PendingTeleport(dimension, player.getLocation().clone(), destination.clone(), this.countdownSeconds());
        this.pendingTeleports.put(player.getUniqueId(), pending);
        pending.task = Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> {
            PendingTeleport current = this.pendingTeleports.get(player.getUniqueId());
            if (current != pending || !player.isOnline()) {
                this.cancelTeleport(player.getUniqueId());
                return;
            }
            if (this.hasMoved(player, current.origin)) {
                player.sendActionBar(this.message("teleport-cancelled-moved", "&cTeleport cancelled: you moved.", new Object[0]));
                this.cancelTeleport(player.getUniqueId());
                return;
            }
            if (current.secondsLeft <= 0) {
                this.completeTeleport(player, current);
                return;
            }
            player.sendActionBar(this.message("teleport-countdown", "&6Teleporting to %dim% in %seconds%s", "%dim%", dimension.displayName, "%seconds%", current.secondsLeft));
            --current.secondsLeft;
        }, 0L, 20L);
    }

    private boolean hasMoved(Player player, Location origin) {
        Location now = player.getLocation();
        return now.getWorld() != origin.getWorld() || now.distanceSquared(origin) > 0.0625;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void completeTeleport(Player player, PendingTeleport pending) {
        Dimension dimension = pending.dimension;
        Dimension from = this.assignments.get(player.getUniqueId());
        EclipseSMP dimensionWars = this;
        synchronized (dimensionWars) {
            if (this.count(dimension) >= this.capacity() && !this.isAssignedTo(dimension, player.getUniqueId())) {
                this.cancelTeleport(player.getUniqueId());
                this.send((CommandSender)player, this.prefixed("dimension-filled", "&cThat dimension filled before your teleport finished.", "%dim%", dimension.displayName));
                this.openMenu(player);
                return;
            }
            this.assignments.put(player.getUniqueId(), dimension);
            this.locked.add(player.getUniqueId());
            this.saveData();
        }
        if (from != null && from != dimension && this.count(dimension) > 0) {
            this.announceInvasion(from, dimension);
        }
        this.cancelTeleport(player.getUniqueId());
        player.closeInventory();
        player.teleport(pending.destination);
        this.applyHearts(player, false);
        this.send((CommandSender)player, this.prefixed("teleport-complete", "&aYou are now part of the %dim% dimension! Your choice is locked.", "%dim%", dimension.displayName));
        this.giveRitualBook(player, dimension);
    }

    private void cancelTeleport(UUID playerId) {
        PendingTeleport pending = this.pendingTeleports.remove(playerId);
        if (pending != null && pending.task != null) {
            pending.task.cancel();
        }
    }

    private void announceInvasion(Dimension from, Dimension to) {
        if (from == null || to == null || from == to) {
            return;
        }
        Component invasionMessage = this.prefixed("dimension-invasion", "&c%s is invading %s!", "%from%", from.displayName, "%to%", to.displayName);
        for (int i = 0; i < 5; ++i) {
            Bukkit.getServer().broadcast(invasionMessage);
            try {
                Thread.sleep(900L);
                continue;
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        PendingTeleport pending;
        if (event.getTo() != null) {
            this.tryPortalTravel(event.getPlayer());
        }
        if ((pending = this.pendingTeleports.get(event.getPlayer().getUniqueId())) == null || event.getTo() == null) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        event.getPlayer().sendActionBar(this.message("teleport-cancelled-moved", "&cTeleport cancelled: you moved.", new Object[0]));
        this.cancelTeleport(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.cancelTeleport(player.getUniqueId());
        this.healCooldowns.remove(player.getUniqueId());
        for (BossState state : this.bosses.values()) {
            state.bar.removeViewer((Audience)player);
        }
        for (RitualState ritual : this.rituals.values()) {
            ritual.bar.removeViewer((Audience)player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.cancelTeleport(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask((Plugin)this, () -> {
            if (!player.isOnline()) {
                return;
            }
            this.applyHearts(player, false);
            Dimension dimension = this.assignments.get(player.getUniqueId());
            if (dimension != null) {
                this.giveRitualBook(player, dimension);
                this.teleportToSpawn(player, dimension);
            }
        });
    }

    private void restartLitPortals() {
        for (Dimension d : Dimension.values()) {
            Location anchor = this.portalAnchors.get((Object)d);
            if (anchor == null || anchor.getWorld() == null || this.portalTargets.get((Object)d) == null || this.portalTargets.get((Object)d).isEmpty() || !this.portalAlreadyLit(d)) continue;
            this.portalLit.put(d, true);
            this.fillPortalBlock(d);
        }
    }

    private boolean portalAlreadyLit(Dimension dimension) {
        Location anchor = this.portalAnchors.get((Object)dimension);
        if (anchor == null || anchor.getWorld() == null) {
            return false;
        }
        Material fill = dimension.portalFill();
        for (int dy = 0; dy <= 2; ++dy) {
            for (int dz = -1; dz <= 1; ++dz) {
                if (anchor.getWorld().getBlockAt(anchor.getBlockX(), anchor.getBlockY() + dy, anchor.getBlockZ() + dz).getType() != fill) continue;
                return true;
            }
        }
        return false;
    }

    private boolean isMockServer() {
        return Bukkit.getServer().getClass().getName().toLowerCase(Locale.ROOT).contains("mockbukkit");
    }

    private World dimensionWorld(Dimension dimension) {
        World byName = Bukkit.getWorld((String)(switch (dimension.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "world_nether";
            case 1 -> "world_the_end";
            case 2 -> "world";
        }));
        if (byName != null) {
            return byName;
        }
        World.Environment environment = switch (dimension.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> World.Environment.NETHER;
            case 1 -> World.Environment.THE_END;
            case 2 -> World.Environment.NORMAL;
        };
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != environment) continue;
            return world;
        }
        return null;
    }

    private int kingdomSize() {
        return Math.max(80, this.getConfig().getInt("kingdoms.size", 400));
    }

    private int kingdomHalf() {
        return this.kingdomSize() / 2;
    }

    private int plazaRadius() {
        return Math.max(20, this.getConfig().getInt("kingdoms.plaza-radius", 60));
    }

    private int buildPerTick() {
        return Math.max(100, this.getConfig().getInt("kingdoms.build-per-tick", 1200));
    }

    private double wakeRadius() {
        return Math.max(4.0, this.getConfig().getDouble("kingdoms.wake-radius", 30.0));
    }

    private double calmRadius() {
        return Math.max(4.0, this.getConfig().getDouble("kingdoms.calm-radius", 40.0));
    }

    private void startKingdoms() {
        if (this.isMockServer()) {
            return;
        }
        if (!this.getConfig().getBoolean("kingdoms.auto-build", true)) {
            return;
        }
        for (Dimension dimension : Dimension.values()) {
            this.ensureKingdom(dimension);
        }
    }

    private void ensureKingdom(Dimension dimension) {
        World world = this.dimensionWorld(dimension);
        if (world == null) {
            this.getLogger().warning("No " + dimension.key() + " world is loaded; its kingdom cannot be built.");
            return;
        }
        Location center = this.kingdoms.get((Object)dimension);
        if (center == null || center.getWorld() == null || !center.getWorld().equals((Object)world)) {
            center = this.chooseKingdomCenter(dimension, world);
            this.kingdoms.put(dimension, center);
            this.cages.put(dimension, this.cageLocation(dimension, center));
            if (!this.spawns.containsKey((Object)dimension)) {
                this.spawns.put(dimension, this.kingdomSpawn(dimension, center));
            }
            this.portalAnchors.putIfAbsent(dimension, this.portalAnchor(dimension, center));
            this.saveData();
        }
        this.cages.putIfAbsent(dimension, this.cageLocation(dimension, center));
        this.startKingdomBuild(dimension);
    }

    private Location chooseKingdomCenter(Dimension dimension, World world) {
        Location spawn = world.getSpawnLocation();
        int fallbackY = world.getEnvironment() == World.Environment.NETHER ? 64 : Math.max(5, world.getHighestBlockYAt(spawn) + 1);
        for (int ring = 0; ring <= 768; ring += 96) {
            int[][] offsets;
            for (int[] offset : offsets = new int[][]{{0, 0}, {ring, 0}, {-ring, 0}, {0, ring}, {0, -ring}, {ring, ring}, {-ring, -ring}, {ring, -ring}, {-ring, ring}}) {
                int z;
                int x = spawn.getBlockX() + offset[0];
                if (!this.flatEnough(world, x, z = spawn.getBlockZ() + offset[1], 8)) continue;
                int y = world.getEnvironment() == World.Environment.NETHER ? 64 : Math.max(5, world.getHighestBlockYAt(x, z) + 1);
                return new Location(world, (double)x, (double)y, (double)z);
            }
        }
        return new Location(world, (double)spawn.getBlockX(), (double)fallbackY, (double)spawn.getBlockZ());
    }

    private boolean flatEnough(World world, int x, int z, int radius) {
        int[][] points;
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return true;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int[] point : points = new int[][]{{0, 0}, {radius, radius}, {radius, -radius}, {-radius, radius}, {-radius, -radius}}) {
            int y = world.getHighestBlockYAt(x + point[0], z + point[1]);
            if (y <= 0) {
                return false;
            }
            min = Math.min(min, y);
            max = Math.max(max, y);
        }
        return max - min <= 6;
    }

    private int scaled(int offsetAt200) {
        return Math.max(1, (int)Math.round((double)offsetAt200 * ((double)this.kingdomHalf() / 200.0)));
    }

    private Location cageLocation(Dimension dimension, Location center) {
        if (center == null || center.getWorld() == null) {
            return null;
        }
        return new Location(center.getWorld(), (double)center.getBlockX(), (double)center.getBlockY(), (double)(center.getBlockZ() - this.scaled(26)), 0.0f, 0.0f);
    }

    private Location kingdomSpawn(Dimension dimension, Location center) {
        if (center == null || center.getWorld() == null) {
            return null;
        }
        return new Location(center.getWorld(), (double)center.getBlockX() + 0.5, (double)center.getBlockY(), (double)center.getBlockZ() + 12.5, 180.0f, 0.0f);
    }

    private Location portalAnchor(Dimension dimension, Location center) {
        if (center == null || center.getWorld() == null) {
            return null;
        }
        return new Location(center.getWorld(), (double)(center.getBlockX() + this.scaled(55)), (double)center.getBlockY(), (double)center.getBlockZ());
    }

    private void startKingdomBuild(Dimension dimension) {
        Location center;
        this.kingdomReady.remove((Object)dimension);
        BukkitTask existing = this.kingdomTasks.get((Object)dimension);
        if (existing != null) {
            existing.cancel();
        }
        if ((center = this.kingdoms.get((Object)dimension)) == null || center.getWorld() == null) {
            return;
        }
        this.preloadKingdomArea(dimension, center, () -> this.beginKingdomBuild(dimension));
    }

    /**
     * Loads and generates the whole kingdom footprint off the main thread before any
     * block is placed. The chunk tickets also keep the area loaded for the duration of
     * the build, so setType never triggers a synchronous chunk load on the server thread.
     */
    private void preloadKingdomArea(Dimension dimension, Location center, Runnable done) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int half = this.kingdomHalf();
        int minX = (center.getBlockX() - half) >> 4;
        int maxX = (center.getBlockX() + half) >> 4;
        int minZ = (center.getBlockZ() - half) >> 4;
        int maxZ = (center.getBlockZ() + half) >> 4;
        this.releaseKingdomTickets(dimension);
        this.kingdomTicketBounds.put(dimension, new int[]{minX, minZ, maxX, maxZ});
        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (int cx = minX; cx <= maxX; ++cx) {
            for (int cz = minZ; cz <= maxZ; ++cz) {
                // The ticket is added only once the chunk is loaded, because
                // World#addPluginChunkTicket would force a synchronous load here.
                futures.add(world.getChunkAtAsync(cx, cz, true)
                        .thenApply(chunk -> chunk.addPluginChunkTicket((Plugin)this) ? chunk : chunk));
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((ignored, error) -> {
            if (error != null) {
                this.getLogger().warning("Could not pre-load the " + dimension.key() + " kingdom chunks: " + error.getMessage());
            }
            if (!this.isEnabled()) {
                return;
            }
            Bukkit.getScheduler().runTask((Plugin)this, done);
        });
    }

    private void beginKingdomBuild(Dimension dimension) {
        if (!this.isEnabled() || !this.kingdoms.containsKey((Object)dimension)) {
            return;
        }
        this.kingdomCursor.put(dimension, 0);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> this.buildKingdomSlice(dimension), 1L, 1L);
        this.kingdomTasks.put(dimension, task);
    }

    private void releaseKingdomTickets(Dimension dimension) {
        int[] bounds = this.kingdomTicketBounds.remove((Object)dimension);
        if (bounds == null) {
            return;
        }
        Location center = this.kingdoms.get((Object)dimension);
        World world = center == null ? null : center.getWorld();
        if (world == null) {
            return;
        }
        world.removePluginChunkTickets((Plugin)this);
    }

    private void buildKingdomSlice(Dimension dimension) {
        int cursor;
        Location center = this.kingdoms.get((Object)dimension);
        if (center == null || center.getWorld() == null) {
            BukkitTask task = this.kingdomTasks.remove((Object)dimension);
            if (task != null) {
                task.cancel();
            }
            return;
        }
        World world = center.getWorld();
        int baseY = center.getBlockY();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int size = this.kingdomSize();
        int half = size / 2;
        int budget = this.buildPerTick();
        for (cursor = this.kingdomCursor.getOrDefault((Object)dimension, 0).intValue(); budget-- > 0 && cursor < size * size; ++cursor) {
            int dx = cursor % size - half;
            int dz = cursor / size - half;
            this.applyKingdomColumn(dimension, world, cx + dx, baseY, cz + dz, dx, dz);
        }
        this.kingdomCursor.put(dimension, cursor);
        if (cursor < size * size) {
            return;
        }
        BukkitTask task = this.kingdomTasks.remove((Object)dimension);
        if (task != null) {
            task.cancel();
        }
        if (!this.startSchematicPaste(dimension, center)) {
            this.buildKingdomStructures(dimension, center);
            this.finishKingdom(dimension, center);
        }
    }

    /**
     * Pastes the configured schematic (if any) over the kingdom heart. Returns true
     * when a paste was started; the structures and the sleeping boss are placed when
     * the paste finishes so they are never overwritten by the build.
     */
    private boolean startSchematicPaste(Dimension dimension, Location center) {
        if (center == null || center.getWorld() == null) {
            return false;
        }
        if (!this.getConfig().getBoolean("kingdoms.schematics.enabled", true)) {
            return false;
        }
        String file = this.getConfig().getString("kingdoms.schematics." + dimension.key(), "");
        if (file == null || file.isBlank()) {
            return false;
        }
        SchematicPaster schematic = this.loadSchematic(file.trim());
        if (schematic == null) {
            this.getLogger().warning("Kingdom schematic '" + file + "' for " + dimension.key() + " could not be loaded; using the procedural build.");
            return false;
        }
        int sink = Math.max(0, this.getConfig().getInt("kingdoms.schematics.sink", 4));
        int perTick = Math.max(500, this.getConfig().getInt("kingdoms.schematics.blocks-per-tick", 12000));
        boolean placeAir = this.getConfig().getBoolean("kingdoms.schematics.place-air", true);
        Location corner = new Location(center.getWorld(),
                center.getBlockX() - schematic.width() / 2,
                center.getBlockY() - sink,
                center.getBlockZ() - schematic.length() / 2);
        this.getLogger().info("Pasting " + schematic.name() + " for the " + dimension.key() + " kingdom ("
                + schematic.width() + "x" + schematic.height() + "x" + schematic.length() + ")...");
        SchematicPaster.PasteJob job = schematic.paste(this, center.getWorld(), corner, perTick, placeAir, () -> {
            this.pasteJobs.remove(dimension);
            this.buildKingdomStructures(dimension, center);
            this.finishKingdom(dimension, center);
        });
        this.pasteJobs.put(dimension, job);
        return true;
    }

    private SchematicPaster loadSchematic(String name) {
        String clean = name.endsWith(".schem") ? name : name + ".schem";
        File folder = new File(this.getDataFolder(), "schematics");
        if (!folder.exists() && !folder.mkdirs()) {
            this.getLogger().warning("Could not create the schematics folder.");
        }
        File file = new File(folder, clean);
        if (!file.exists() && this.getResource("schematics/" + clean) != null) {
            this.saveResource("schematics/" + clean, false);
        }
        if (!file.exists()) {
            return null;
        }
        try (java.io.InputStream in = new java.io.FileInputStream(file)) {
            return SchematicPaster.load(in, clean);
        } catch (Exception ex) {
            this.getLogger().warning("Could not read schematic " + clean + ": " + ex.getMessage());
            return null;
        }
    }

    private void applyKingdomColumn(Dimension dimension, World world, int x, int baseY, int z, int dx, int dz) {
        boolean wall = this.wallColumn(dx, dz);
        boolean gate = wall && this.gateColumn(dx, dz);
        boolean plaza = dx * dx + dz * dz <= this.plazaRadius() * this.plazaRadius();
        boolean road = this.roadColumn(dx, dz);
        if (!(wall || plaza || road)) {
            return;
        }
        for (int dy = 0; dy <= 8; ++dy) {
            world.getBlockAt(x, baseY + dy, z).setType(Material.AIR, false);
        }
        Material ground = plaza ? dimension.kingdomFloor() : dimension.kingdomRoad();
        world.getBlockAt(x, baseY - 1, z).setType(ground, false);
        if (!wall || gate) {
            return;
        }
        for (int dy = 0; dy <= 9; ++dy) {
            world.getBlockAt(x, baseY + dy, z).setType(dimension.kingdomWall(), false);
        }
        if ((dx + dz & 1) == 0) {
            world.getBlockAt(x, baseY + 10, z).setType(dimension.kingdomWall(), false);
        }
        if (dx % 16 == 0 && dz % 16 == 0) {
            world.getBlockAt(x, baseY + 5, z).setType(dimension.kingdomLight(), false);
        }
    }

    private boolean wallColumn(int dx, int dz) {
        int half = this.kingdomHalf();
        return Math.abs(dx) >= half - 2 || Math.abs(dz) >= half - 2;
    }

    private boolean gateColumn(int dx, int dz) {
        int half = this.kingdomHalf();
        if (Math.abs(dz) <= 4 && Math.abs(dx) >= half - 2) {
            return true;
        }
        return Math.abs(dx) <= 4 && Math.abs(dz) >= half - 2;
    }

    private boolean roadColumn(int dx, int dz) {
        if (Math.abs(dx) <= 4 || Math.abs(dz) <= 4) {
            return true;
        }
        int ring = Math.max(Math.abs(dx), Math.abs(dz));
        return Math.abs(ring - (this.kingdomHalf() - 50)) <= 3;
    }

    private void buildKingdomStructures(Dimension dimension, Location center) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        this.buildCage(dimension, world, cx, cy, cz - this.scaled(26));
        this.buildThrone(dimension, world, cx, cy, cz + this.scaled(26));
        Location portal = this.portalAnchors.get((Object)dimension);
        if (portal == null || portal.getWorld() == null) {
            portal = this.portalAnchor(dimension, center);
            this.portalAnchors.put(dimension, portal);
        }
        this.buildPortalStructure(dimension, world, portal.getBlockX(), portal.getBlockY(), portal.getBlockZ());
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                this.buildTower(dimension, world, cx + sx * this.scaled(190), cy, cz + sz * this.scaled(190));
            }
        }
        for (int angle = 0; angle < 360; angle += 30) {
            int lx = cx + (int)Math.round(Math.cos(Math.toRadians(angle)) * (double)this.scaled(58));
            int lz = cz + (int)Math.round(Math.sin(Math.toRadians(angle)) * (double)this.scaled(58));
            world.getBlockAt(lx, cy, lz).setType(dimension.kingdomLight(), false);
            world.getBlockAt(lx, cy + 1, lz).setType(dimension.kingdomTrim(), false);
        }
    }

    private void buildCage(Dimension dimension, World world, int cx, int cy, int cz) {
        int z;
        int x;
        int r = 7;
        for (x = -r; x <= r; ++x) {
            for (z = -r; z <= r; ++z) {
                int y;
                boolean edge = Math.abs(x) == r || Math.abs(z) == r;
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(dimension.kingdomAccent(), false);
                if (!edge) {
                    // Hollow the chamber so a pasted map cannot bury the sleeping boss.
                    for (y = 0; y <= 6; ++y) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR, false);
                    }
                } else {
                    for (y = 0; y <= 6; ++y) {
                        world.getBlockAt(cx + x, cy + y, cz + z).setType(dimension.cageBar(), false);
                    }
                }
                for (y = 7; y <= 8; ++y) {
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(dimension.kingdomAccent(), false);
                }
            }
        }
        for (x = -1; x <= 1; ++x) {
            for (int y = 0; y <= 4; ++y) {
                world.getBlockAt(cx + x, cy + y, cz + r).setType(Material.AIR, false);
            }
        }
        for (x = -r; x <= r; x += r) {
            for (z = -r; z <= r; z += r) {
                world.getBlockAt(cx + x, cy + 6, cz + z).setType(dimension.kingdomLight(), false);
            }
        }
    }

    private void buildThrone(Dimension dimension, World world, int cx, int cy, int cz) {
        int y;
        int x;
        for (x = -4; x <= 4; ++x) {
            for (int z = -4; z <= 4; ++z) {
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(dimension.kingdomFloor(), false);
            }
        }
        for (y = 0; y <= 2; ++y) {
            world.getBlockAt(cx, cy + y, cz).setType(dimension.kingdomAccent(), false);
            world.getBlockAt(cx - 1, cy + y, cz).setType(dimension.kingdomAccent(), false);
            world.getBlockAt(cx + 1, cy + y, cz).setType(dimension.kingdomAccent(), false);
        }
        for (y = 3; y <= 5; ++y) {
            world.getBlockAt(cx - 1, cy + y, cz).setType(dimension.kingdomWall(), false);
            world.getBlockAt(cx + 1, cy + y, cz).setType(dimension.kingdomWall(), false);
        }
        world.getBlockAt(cx, cy + 3, cz).setType(dimension.kingdomLight(), false);
        for (x = -5; x <= 5; x += 10) {
            for (int y2 = 0; y2 <= 3; ++y2) {
                world.getBlockAt(cx + x, cy + y2, cz - 5).setType(dimension.kingdomTrim(), false);
            }
        }
    }

    private void buildTower(Dimension dimension, World world, int cx, int cy, int cz) {
        int z;
        int x;
        for (x = -4; x <= 4; ++x) {
            for (z = -4; z <= 4; ++z) {
                for (int y = 0; y <= 13; ++y) {
                    Material material;
                    boolean shell = Math.abs(x) == 4 || Math.abs(z) == 4;
                    Material material2 = material = shell ? dimension.kingdomWall() : Material.AIR;
                    if (shell && y == 13) {
                        material = dimension.kingdomWall();
                    }
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(material, false);
                }
            }
        }
        for (x = -4; x <= 4; ++x) {
            for (z = -4; z <= 4; ++z) {
                world.getBlockAt(cx + x, cy + 14, cz + z).setType(dimension.kingdomAccent(), false);
            }
        }
        world.getBlockAt(cx, cy + 15, cz).setType(dimension.kingdomLight(), false);
    }

    private void buildPortalStructure(Dimension dimension, World world, int px, int py, int pz) {
        int dy;
        Material frameBlock = dimension.portalFrameBlock();
        for (int dz = -2; dz <= 2; ++dz) {
            world.getBlockAt(px, py, pz + dz).setType(frameBlock, false);
            world.getBlockAt(px, py + 4, pz + dz).setType(frameBlock, false);
        }
        for (dy = 0; dy <= 4; ++dy) {
            world.getBlockAt(px, py + dy, pz - 2).setType(frameBlock, false);
            world.getBlockAt(px, py + dy, pz + 2).setType(frameBlock, false);
        }
        for (dy = 0; dy <= 3; ++dy) {
            for (int dz = -1; dz <= 1; ++dz) {
                world.getBlockAt(px, py + dy, pz + dz).setType(Material.AIR, false);
            }
        }
        for (dy = 1; dy <= 3; ++dy) {
            Location frameLocation = new Location(world, (double)px - 0.5, (double)(py + dy), (double)pz - 1.5);
            try {
                ItemFrame frame = (ItemFrame)world.spawn(frameLocation, ItemFrame.class);
                frame.setFacingDirection(BlockFace.WEST, true);
                continue;
            }
            catch (RuntimeException ex) {
                this.getLogger().warning("Could not place a portal key frame in the " + dimension.key() + " kingdom.");
            }
        }
        world.getBlockAt(px + 2, py, pz).setType(dimension.kingdomAccent(), false);
        world.getBlockAt(px + 2, py + 1, pz).setType(dimension.kingdomLight(), false);
    }

    private void finishKingdom(Dimension dimension, Location center) {
        this.kingdomReady.add(dimension);
        if (!this.spawns.containsKey((Object)dimension)) {
            this.spawns.put(dimension, this.kingdomSpawn(dimension, center));
        }
        this.cages.put(dimension, this.cageLocation(dimension, center));
        this.saveData();
        Phase phase = this.phases.getOrDefault((Object)dimension, Phase.DORMANT);
        if ((phase == Phase.DORMANT || phase == Phase.ALIVE) && this.stateFor(dimension) == null) {
            this.spawnBoss(dimension, this.cages.get((Object)dimension), true);
        }
        this.releaseKingdomTickets(dimension);
        this.getLogger().info(dimension.kingdomName() + " is ready at " + this.coords(center) + ".");
    }

    private void restoreBosses() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                Dimension dimension;
                if (!entity.getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE) || (dimension = Dimension.parse((String)entity.getPersistentDataContainer().get(this.dimensionKey, PersistentDataType.STRING))) == null || this.bosses.containsKey(entity.getUniqueId())) continue;
                this.phases.put(dimension, Phase.ALIVE);
                this.registerBoss(entity, dimension);
                this.getLogger().info("Recovered " + dimension.key() + " boss from an existing entity.");
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            Dimension dimension;
            LivingEntity boss;
            if (!(entity instanceof LivingEntity) || !(boss = (LivingEntity)entity).getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE) || this.bosses.containsKey(boss.getUniqueId()) || (dimension = Dimension.parse((String)boss.getPersistentDataContainer().get(this.dimensionKey, PersistentDataType.STRING))) == null) continue;
            this.phases.put(dimension, Phase.ALIVE);
            this.registerBoss(boss, dimension);
            this.getLogger().info("Recovered " + dimension.key() + " boss from a loaded chunk.");
        }
    }

    private void registerBoss(LivingEntity boss, Dimension dimension) {
        Mob mob;
        BossState existing = this.stateFor(dimension);
        if (existing != null && !existing.entityId.equals(boss.getUniqueId())) {
            Entity old = Bukkit.getEntity(existing.entityId);
            if (old != null && old.isValid() && !old.isDead()) {
                // A second entity of the same dimension is a duplicate (for example a
                // cage boss whose chunk loaded after a timed spawn). Never keep two.
                boss.remove();
                this.getLogger().info("Removed a duplicate " + dimension.key() + " boss entity.");
                return;
            }
            this.removeBoss(existing, false);
        }
        this.applyVanillaHidden(boss);
        BossBar bar = BossBar.bossBar((Component)this.bossName(dimension), (float)1.0f, (BossBar.Color)dimension.barColor(), (BossBar.Overlay)BossBar.Overlay.PROGRESS);
        BossState state = new BossState(this, boss.getUniqueId(), dimension, bar);
        state.awake = !(boss instanceof Mob) || (mob = (Mob)boss).hasAI();
        state.tier = this.tierFor(boss);
        this.bosses.put(boss.getUniqueId(), state);
        for (Player online : Bukkit.getOnlinePlayers()) {
            bar.addViewer((Audience)online);
        }
        this.updateBar(state, boss);
        this.updateDisplay(state, boss);
        if (state.displayId == null && this.customModelEnabled()) {
            state.displayId = this.spawnDisplay(boss, dimension, state.tier);
        }
        state.tracker = Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> this.tickBoss(state), 2L, 2L);
    }

    private void tickBoss(BossState state) {
        LivingEntity boss;
        Entity entity = Bukkit.getEntity((UUID)state.entityId);
        if (entity == null) {
            // The boss sleeps in an unloaded chunk. Keep its state (bar, tier, display)
            // so it resumes when the chunk loads again instead of being forgotten.
            return;
        }
        if (!(entity instanceof LivingEntity) || !(boss = (LivingEntity)entity).isValid() || boss.isDead()) {
            this.bosses.remove(state.entityId);
            if (state.tracker != null) {
                state.tracker.cancel();
            }
            this.clearBar(state.bar);
            this.removeDisplay(state);
            return;
        }
        this.updateBar(state, boss);
        this.updateDisplay(state, boss);
        if (!state.awake) {
            return;
        }
        this.suppressFriendlyTargets(boss, state.dimension);
        if (++state.ticks % 20 != 0) {
            return;
        }
        BossTier healthTier = this.tierFor(boss);
        if (healthTier != state.tier) {
            this.upgradeTier(state, boss, healthTier);
        }
        double chance = Math.max(0.0, Math.min(1.0, this.getConfig().getDouble("bosses." + state.dimension.key() + ".ability-chance-per-second", 0.35)));
        if (Math.random() < chance) {
            this.applyAbility(state, boss);
        } else {
            this.suppressFriendlyTargets(boss, state.dimension);
        }
    }

    private BossTier tierFor(LivingEntity boss) {
        AttributeInstance max = boss.getAttribute(Attribute.MAX_HEALTH);
        double total = max == null || max.getValue() <= 0.0 ? 1.0 : max.getValue();
        double ratio = boss.getHealth() / total;
        if (ratio > 0.66) {
            return BossTier.EASY;
        }
        if (ratio > 0.33) {
            return BossTier.MEDIUM;
        }
        return BossTier.HARD;
    }

    private void updateBar(BossState state, LivingEntity boss) {
        AttributeInstance maxHealth = boss.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null || maxHealth.getValue() <= 0.0) {
            return;
        }
        state.bar.progress((float)Math.max(0.001, Math.min(1.0, boss.getHealth() / maxHealth.getValue())));
    }

    private void updateDisplay(BossState state, LivingEntity boss) {
        Entity display;
        if (!this.customModelEnabled()) {
            if (state.displayId != null) {
                this.removeDisplay(state);
            }
            return;
        }
        Entity entity = display = state.displayId == null ? null : Bukkit.getEntity((UUID)state.displayId);
        if (!(display instanceof ItemDisplay)) {
            state.displayId = this.spawnDisplay(boss, state.dimension, state.tier);
            return;
        }
        ItemDisplay itemDisplay = (ItemDisplay)display;
        if (!this.sameBossModel(itemDisplay, state.dimension, state.tier)) {
            itemDisplay.setItemStack(this.bossFigure(state.dimension, state.tier));
        }
        Location location = boss.getLocation();
        location.setPitch(0.0f);
        location.setYaw(boss.getLocation().getYaw());
        display.teleport(location);
    }

    private boolean sameBossModel(ItemDisplay display, Dimension dimension, BossTier tier) {
        ItemStack stack = display.getItemStack();
        ItemMeta meta = stack == null ? null : stack.getItemMeta();
        return meta != null && meta.getCustomModelData() == this.bossModelData(dimension, tier);
    }

    private UUID spawnDisplay(LivingEntity boss, Dimension dimension, BossTier tier) {
        if (!this.customModelEnabled()) {
            return null;
        }
        Entity spawned = boss.getWorld().spawnEntity(boss.getLocation(), EntityType.ITEM_DISPLAY);
        if (!(spawned instanceof ItemDisplay)) {
            spawned.remove();
            return null;
        }
        ItemDisplay display = (ItemDisplay)spawned;
        display.getPersistentDataContainer().set(this.displayKey, PersistentDataType.STRING, dimension.name());
        display.setItemStack(this.bossFigure(dimension, tier));
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        display.setBillboard(Display.Billboard.FIXED);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setTeleportDuration(2);
        display.setInterpolationDuration(2);
        display.setPersistent(false);
        display.setInvulnerable(true);
        display.setGravity(false);
        display.setSilent(true);
        return display.getUniqueId();
    }

    private void removeDisplay(BossState state) {
        if (state.displayId == null) {
            return;
        }
        Entity display = Bukkit.getEntity((UUID)state.displayId);
        if (display != null) {
            display.remove();
        }
        state.displayId = null;
    }

    private int bossModelData(Dimension dimension, BossTier tier) {
        return 2001 + dimension.ordinal() + tier.modelOffset();
    }

    private ItemStack bossFigure(Dimension dimension, BossTier tier) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName((Component)Component.empty());
            meta.setCustomModelData(Integer.valueOf(this.bossModelData(dimension, tier)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean customModelEnabled() {
        return this.getConfig().getBoolean("bosses.custom-model", true);
    }

    private void suppressFriendlyTargets(LivingEntity boss, Dimension dimension) {
        Player player;
        if (!(boss instanceof Mob)) {
            return;
        }
        Mob mob = (Mob)boss;
        LivingEntity target = mob.getTarget();
        if (target instanceof Player && this.assignments.get((player = (Player)target).getUniqueId()) == dimension) {
            mob.setTarget(null);
        }
    }

    private List<Ability> abilities(Dimension dimension, BossTier tier) {
        return switch (dimension.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                switch (tier.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield List.of(new Ability("Cinder Sweep", Move.BURST, 4.0, 0), new Ability("Gold Volley", Move.VOLLEY, 4.0, 0), new Ability("Molten Grasp", Move.PULL, 4.0, 0), new Ability("Furnace Breath", Move.FIRE, 4.0, 80));
                    }
                    case 1: {
                        yield List.of(new Ability("Blaze Barrage", Move.VOLLEY, 7.0, 0), new Ability("Ember Charge", Move.KNOCKBACK, 7.0, 0), new Ability("Withering Roar", Move.WITHER, 7.0, 100), new Ability("Lava Lash", Move.BURST, 7.0, 0));
                    }
                    case 2: 
                }
                yield List.of(new Ability("Gilded Storm", Move.VOLLEY, 7.0, 0), new Ability("Soul Scorch", Move.FIRE, 7.0, 140), new Ability("Warlord's Wrath", Move.BLINK, 7.0, 0), new Ability("Nether Nova", Move.ULTIMATE, 24.0, 0));
            }
            case 1 -> {
                switch (tier.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield List.of(new Ability("Void Ripple", Move.LEVITATE, 4.0, 30), new Ability("Ender Barrage", Move.VOLLEY, 4.0, 0), new Ability("Phase Step", Move.BLINK, 4.0, 0), new Ability("Gravity Well", Move.SLOW, 4.0, 80));
                    }
                    case 1: {
                        yield List.of(new Ability("Rift Tear", Move.LEVITATE, 7.0, 40), new Ability("Void Lance", Move.BURST, 7.0, 0), new Ability("Shadow Blink", Move.BLINK, 7.0, 0), new Ability("Singularity", Move.PULL, 7.0, 0));
                    }
                    case 2: 
                }
                yield List.of(new Ability("Reaper's Toll", Move.WITHER, 7.0, 140), new Ability("Starfall", Move.VOLLEY, 7.0, 0), new Ability("Event Horizon", Move.LEVITATE, 7.0, 60), new Ability("Collapse", Move.ULTIMATE, 24.0, 0));
            }
            case 2 -> {
                switch (tier.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield List.of(new Ability("Sonic Pulse", Move.BURST, 4.0, 0), new Ability("Sculk Snare", Move.SLOW, 4.0, 80), new Ability("Echo Volley", Move.VOLLEY, 4.0, 0), new Ability("Blind Ambush", Move.BLIND, 4.0, 60));
                    }
                    case 1: {
                        yield List.of(new Ability("Deep Quake", Move.KNOCKBACK, 7.0, 0), new Ability("Warden Beam", Move.BURST, 7.0, 0), new Ability("Darkness", Move.BLIND, 7.0, 100), new Ability("Echo Drain", Move.WITHER, 7.0, 100));
                    }
                    case 2: 
                }
                yield List.of(new Ability("Sonic Overload", Move.BURST, 7.0, 0), new Ability("Abyss Gaze", Move.LEVITATE, 7.0, 40), new Ability("Guardian's Verdict", Move.BLINK, 7.0, 0), new Ability("Cataclysm", Move.ULTIMATE, 24.0, 0));
            }
        };
    }

    private double specialDamage() {
        return Math.max(1.0, this.getConfig().getDouble("combat.special-damage", 7.0));
    }

    private double ultimateDamage() {
        return Math.max(1.0, this.getConfig().getDouble("combat.ultimate-damage", 24.0));
    }

    private double normalHitDamage() {
        return Math.max(1.0, this.getConfig().getDouble("combat.normal-hit-damage", 4.0));
    }

    private double abilityRange(Dimension dimension) {
        return Math.max(2.0, this.getConfig().getDouble("bosses." + dimension.key() + ".ability-range", 10.0));
    }

    private void trueDamage(Player player, double amount) {
        if (player.isDead() || player.getHealth() <= 0.0) {
            return;
        }
        double remaining = player.getHealth() - amount;
        player.setHealth(Math.max(0.0, remaining));
    }

    private void applyAbility(BossState state, LivingEntity boss) {
        List<Ability> rotation = this.abilities(state.dimension, state.tier);
        Ability ability = rotation.get(state.abilityCursor++ % rotation.size());
        List<Player> targets = this.enemiesNear(boss, state.dimension, this.abilityRange(state.dimension));
        if (targets.isEmpty()) {
            return;
        }
        ++state.specialHits;
        int every = Math.max(1, this.getConfig().getInt("combat.specials-per-empowered", 3));
        boolean empowered = state.specialHits % every == 0;
        double damage = ability.move() == Move.ULTIMATE ? this.ultimateDamage() : this.specialDamage();
        TextComponent line = LEGACY.deserialize("&c" + this.bossNamePlain(state.dimension) + " &7uses &e" + ability.name() + (empowered ? " &6(empowered)" : SC_SENDKEY));
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.6f, 0.7f);
        for (Player target : targets) {
            target.sendActionBar((Component)line);
            switch (ability.move().ordinal()) {
                case 0: {
                    target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 3, 0.4, 0.6, 0.4, 0.0);
                    target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.02);
                    break;
                }
                case 1: {
                    target.setFireTicks(ability.ticks());
                    target.getWorld().spawnParticle(Particle.LAVA, target.getLocation(), 20, 0.6, 0.4, 0.6, 0.0);
                    break;
                }
                case 2: {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, ability.ticks(), 1));
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 40, 0.8, 0.8, 0.8, 0.3);
                    break;
                }
                case 3: {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ability.ticks(), 2));
                    target.getWorld().spawnParticle(Particle.SCULK_SOUL, target.getLocation(), 18, 0.5, 0.5, 0.5, 0.02);
                    break;
                }
                case 4: {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ability.ticks(), 0));
                    target.getWorld().spawnParticle(Particle.SQUID_INK, target.getLocation().add(0.0, 1.5, 0.0), 25, 0.5, 0.5, 0.5, 0.02);
                    break;
                }
                case 7: {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ability.ticks(), 1));
                    target.getWorld().spawnParticle(Particle.SMOKE, target.getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.6, 0.5, 0.02);
                    break;
                }
                case 5: {
                    target.setVelocity(boss.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.4));
                    target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 25, 0.4, 0.4, 0.4, 0.2);
                    break;
                }
                case 6: {
                    target.setVelocity(target.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize().multiply(1.8).setY(0.6));
                    target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation(), 20, 0.4, 0.3, 0.4, 0.1);
                    break;
                }
                case 8: {
                    Location behind = target.getLocation().clone().add(target.getLocation().getDirection().multiply(-2));
                    boss.teleport(behind);
                    boss.getWorld().spawnParticle(Particle.REVERSE_PORTAL, behind, 40, 0.5, 0.8, 0.5, 0.2);
                    break;
                }
                case 9: {
                    for (int i = 0; i < 8; ++i) {
                        double angle = Math.PI * 2 * (double)i / 8.0;
                        Location from = boss.getLocation().add(Math.cos(angle) * 0.8, 1.6, Math.sin(angle) * 0.8);
                        target.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, from, 4, 0.1, 0.1, 0.1, 0.02);
                    }
                    target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0.0, 1.0, 0.0), 25, 0.6, 0.6, 0.6, 0.04);
                    break;
                }
                case 10: {
                    target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, target.getLocation(), 2, 0.5, 0.6, 0.5, 0.0);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.6f);
                }
            }
            this.trueDamage(target, empowered ? damage * 2.0 : damage);
        }
    }

    private List<Player> enemiesNear(LivingEntity boss, Dimension dimension, double radius) {
        ArrayList<Player> targets = new ArrayList<Player>();
        for (Entity entity : boss.getNearbyEntities(radius, 8.0, radius)) {
            Player target;
            if (!(entity instanceof Player) || (target = (Player)entity).isDead() || !target.isValid() || !target.getWorld().equals((Object)boss.getWorld()) || this.assignments.get(target.getUniqueId()) == dimension) continue;
            targets.add(target);
        }
        return targets;
    }

    private void upgradeTier(BossState state, LivingEntity boss, BossTier tier) {
        state.tier = tier;
        this.updateDisplay(state, boss);
        Location location = boss.getLocation();
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.8f, 0.7f);
        location.getWorld().spawnParticle(this.dimension0(state.dimension), location, 60, 1.2, 1.2, 1.2, 0.05);
        Bukkit.broadcast((Component)this.prefixed("boss-tier", "&6%boss% has entered its &e%tier%&6 phase!", "%boss%", this.bossNamePlain(state.dimension), "%tier%", tier.displayName()));
    }

    private Particle dimension0(Dimension dimension) {
        return dimension.ritualParticle();
    }

    private void wakeBoss(BossState state, LivingEntity boss, Player intruder) {
        state.awake = true;
        state.calmTicks = 0;
        state.specialHits = 0;
        state.abilityCursor = 0;
        state.tier = BossTier.EASY;
        if (boss instanceof Mob) {
            Mob mob = (Mob)boss;
            mob.setAI(true);
            mob.setTarget((LivingEntity)intruder);
        }
        boss.setInvulnerable(false);
        boss.setSilent(false);
        boss.setPose(Pose.STANDING);
        boss.customName(this.bossName(state.dimension));
        boss.setCustomNameVisible(true);
        this.updateDisplay(state, boss);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.7f);
        boss.getWorld().spawnParticle(this.dimension0(state.dimension), boss.getLocation(), 50, 1.0, 1.0, 1.0, 0.05);
        Bukkit.broadcast((Component)this.prefixed("boss-guard-wakes", "&c%boss% has awoken in the %dim%! Intruders: %player%.", "%boss%", this.bossNamePlain(state.dimension), "%dim%", state.dimension.kingdomName(), "%player%", intruder.getName()));
    }

    private void sleepBoss(BossState state, LivingEntity boss) {
        Location home;
        state.awake = false;
        state.calmTicks = 0;
        state.abilityCursor = 0;
        state.specialHits = 0;
        state.tier = BossTier.EASY;
        if (boss instanceof Mob) {
            Mob mob = (Mob)boss;
            mob.setTarget(null);
            mob.setAI(false);
        }
        boss.setInvulnerable(true);
        boss.setSilent(true);
        boss.setPose(Pose.SLEEPING);
        boss.setCustomNameVisible(false);
        AttributeInstance max = boss.getAttribute(Attribute.MAX_HEALTH);
        if (max != null) {
            boss.setHealth(max.getValue());
        }
        if ((home = this.cages.get((Object)state.dimension)) != null) {
            boss.teleport(home);
        }
        this.updateDisplay(state, boss);
        Bukkit.broadcast((Component)this.prefixed("boss-guard-sleeps", "&7%boss% returned to its cage in the %dim% and fell asleep again.", "%boss%", this.bossNamePlain(state.dimension), "%dim%", state.dimension.kingdomName()));
    }

    private void tickBossLifecycle() {
        for (BossState state : new ArrayList<BossState>(this.bosses.values())) {
            LivingEntity boss;
            Entity entity = Bukkit.getEntity((UUID)state.entityId);
            if (!(entity instanceof LivingEntity) || !(boss = (LivingEntity)entity).isValid() || boss.isDead()) continue;
            this.applyVanillaHidden(boss);
            this.updateDisplay(state, boss);
            if (!state.awake) {
                Player intruder = this.nearestIntruder(boss, state.dimension, this.wakeRadius());
                if (intruder == null) continue;
                this.wakeBoss(state, boss, intruder);
                continue;
            }
            if (this.nearestIntruder(boss, state.dimension, this.calmRadius()) == null) {
                if (++state.calmTicks < 10) continue;
                this.sleepBoss(state, boss);
                continue;
            }
            state.calmTicks = 0;
        }
    }

    private Player nearestIntruder(LivingEntity boss, Dimension dimension, double radius) {
        Player best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Player online : boss.getWorld().getPlayers()) {
            double distance;
            if (this.assignments.get(online.getUniqueId()) == dimension || online.isDead() || !online.isValid() || !((distance = online.getLocation().distanceSquared(boss.getLocation())) <= radius * radius) || !(distance < bestDistance)) continue;
            bestDistance = distance;
            best = online;
        }
        return best;
    }

    private String bossNamePlain(Dimension dimension) {
        return switch (dimension.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "The Piglin Warlord";
            case 1 -> "The Void Reaper";
            case 2 -> "The Deep Guardian";
        };
    }

    private Component bossName(Dimension dimension) {
        return Component.text((String)dimension.bossName(), (TextColor)dimension.color, (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD});
    }

    private boolean spawnBoss(Dimension dimension, Location location) {
        return this.spawnBoss(dimension, location, false);
    }

    private boolean spawnBoss(Dimension dimension, Location location, boolean asleep) {
        if (location.getWorld() == null) {
            return false;
        }
        Location cage = this.cages.get((Object)dimension);
        if (asleep && cage != null) {
            location = cage.clone();
        }
        this.purgeBosses(dimension);
        Entity spawned = location.getWorld().spawnEntity(location, dimension.bossType());
        if (!(spawned instanceof LivingEntity)) {
            spawned.remove();
            this.getLogger().warning("Could not spawn a living boss entity in " + location.getWorld().getName() + ".");
            return false;
        }
        LivingEntity boss = (LivingEntity)spawned;
        if (boss instanceof PiglinAbstract) {
            PiglinAbstract piglin = (PiglinAbstract)boss;
            piglin.setImmuneToZombification(true);
        }
        if (boss.getEquipment() != null) {
            boss.getEquipment().clear();
        }
        boss.customName(this.bossName(dimension));
        boss.setCustomNameVisible(!asleep);
        boss.getPersistentDataContainer().set(this.bossKey, PersistentDataType.BYTE, (byte) 1);
        boss.getPersistentDataContainer().set(this.dimensionKey, PersistentDataType.STRING, dimension.name());
        String path = "bosses." + dimension.key() + ".";
        double health = Math.max(1.0, Math.min(1024.0, this.getConfig().getDouble(path + "max-health", 1000.0)));
        this.setAttribute(boss, Attribute.MAX_HEALTH, health);
        boss.setHealth(health);
        this.setAttribute(boss, Attribute.ATTACK_DAMAGE, Math.max(1.0, this.getConfig().getDouble(path + "damage", 40.0)));
        this.setAttribute(boss, Attribute.ARMOR, Math.max(0.0, this.getConfig().getDouble(path + "armor", 18.0)));
        this.setAttribute(boss, Attribute.KNOCKBACK_RESISTANCE, Math.max(0.0, Math.min(1.0, this.getConfig().getDouble(path + "knockback-resistance", 0.9))));
        double scale = this.getConfig().getDouble("bosses.scale", 2.0);
        boolean spawnOnAltitude = this.getConfig().getBoolean("bosses.spawn-on-altitude", false);
        if (spawnOnAltitude) {
            int altitude = this.getConfig().getInt("bosses.spawn-altitude-blocks", 64);
            boolean setAltitude = this.getConfig().getBoolean("bosses.setaltitude", false);
            if (setAltitude) {
                location = new Location(location.getWorld(), (double)location.getBlockX(), (double)Math.max(1, altitude), (double)location.getBlockZ());
            }
        }
        this.applyVanillaHidden(boss);
        this.setAttribute(boss, Attribute.SCALE, Math.max(0.5, scale));
        boss.setRemoveWhenFarAway(false);
        boss.setPersistent(true);
        if (this.getConfig().getBoolean("bosses.set-altitude", false)) {
            int ya = this.getConfig().getInt("bosses." + dimension.key() + ".spawn-altitude", this.getConfig().getInt("bosses.default-spawn-altitude", 64));
            int max = location.getWorld().getMaxHeight() - 1;
            ya = Math.max(1, Math.min(max, ya));
            location = new Location(location.getWorld(), (double)location.getBlockX(), (double)ya, (double)location.getBlockZ());
        }
        if (asleep) {
            if (boss instanceof Mob) {
                Mob mob = (Mob)boss;
                mob.setAI(false);
            }
            boss.setInvulnerable(true);
            boss.setSilent(true);
            boss.setPose(Pose.SLEEPING);
            boss.setHealth(health);
        }
        this.phases.put(dimension, Phase.ALIVE);
        this.bossSpawns.put(dimension, location.clone());
        this.saveData();
        this.registerBoss(boss, dimension);
        if (asleep) {
            location.getWorld().playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 1.2f, 0.6f);
        } else {
            location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.7f);
            Bukkit.broadcast((Component)this.prefixed("boss-awakened", "&6A dimension boss has awakened: %boss%", "%boss%", dimension.bossName()));
        }
        return true;
    }

    private void setAttribute(LivingEntity entity, Attribute attribute, double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    /**
     * Hides the vanilla mob when the resource pack model is used, and shows it again
     * when custom models are turned off. The old code never applied the invisibility,
     * so players saw the mob and the model stacked on top of each other.
     */
    private void applyVanillaHidden(LivingEntity boss) {
        boolean hide = this.customModelEnabled()
                && this.getConfig().getBoolean("bosses.hide-vanilla-model", true);
        if (hide) {
            if (!boss.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                boss.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                        PotionEffect.INFINITE_DURATION, 0, false, false, false));
            }
        } else {
            boss.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    /** Removes every boss entity and display of a dimension, tracked or orphaned. */
    private void purgeBosses(Dimension dimension) {
        for (BossState state : new ArrayList<BossState>(this.bosses.values())) {
            if (state.dimension == dimension) {
                this.removeBoss(state, true);
            }
        }
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : new ArrayList<LivingEntity>(world.getLivingEntities())) {
                if (!entity.getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE)) {
                    continue;
                }
                Dimension tagged = Dimension.parse(entity.getPersistentDataContainer()
                        .get(this.dimensionKey, PersistentDataType.STRING));
                if (tagged == dimension) {
                    entity.remove();
                }
            }
            for (Entity entity : new ArrayList<Entity>(world.getEntities())) {
                if (!entity.getPersistentDataContainer().has(this.displayKey, PersistentDataType.BYTE)) {
                    continue;
                }
                if (dimension.name().equals(entity.getPersistentDataContainer()
                        .get(this.displayKey, PersistentDataType.STRING))) {
                    entity.remove();
                }
            }
        }
    }

    private void healAllPlayers() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.setFireTicks(0);
            for (PotionEffectType harmful : new PotionEffectType[]{
                    PotionEffectType.SLOWNESS, PotionEffectType.BLINDNESS, PotionEffectType.WITHER,
                    PotionEffectType.LEVITATION, PotionEffectType.POISON, PotionEffectType.WEAKNESS,
                    PotionEffectType.NAUSEA, PotionEffectType.MINING_FATIGUE, PotionEffectType.DARKNESS,
                    PotionEffectType.HUNGER, PotionEffectType.UNLUCK, PotionEffectType.BAD_OMEN}) {
                online.removePotionEffect(harmful);
            }
            online.setFoodLevel(20);
            online.setSaturation(20.0f);
            online.setExhaustion(0.0f);
            this.applyHearts(online, true);
            AttributeInstance max = online.getAttribute(Attribute.MAX_HEALTH);
            if (max != null) {
                online.setHealth(max.getValue());
            }
        }
    }

    /**
     * Removes every boss of a dimension (duplicates included) and puts a single
     * sleeping boss back in its cage at full health.
     */
    private void resetBoss(Dimension dimension) {
        this.purgeBosses(dimension);
        this.phases.put(dimension, Phase.DORMANT);
        this.blessed.remove(dimension);
        Location home = this.cages.get(dimension);
        if (home == null) {
            home = this.bossSpawns.get(dimension);
        }
        if (home == null) {
            home = this.spawns.get(dimension);
        }
        if (home == null || home.getWorld() == null) {
            this.getLogger().warning("Cannot reset the " + dimension.key() + " boss: no kingdom or spawn is configured.");
            return;
        }
        this.spawnBoss(dimension, home, true);
    }

    private void removeBoss(BossState state, boolean removeEntity) {
        Entity entity;
        this.bosses.remove(state.entityId);
        if (state.tracker != null) {
            state.tracker.cancel();
        }
        this.clearBar(state.bar);
        this.removeDisplay(state);
        if (removeEntity && (entity = Bukkit.getEntity((UUID)state.entityId)) != null && !entity.isDead()) {
            entity.remove();
        }
    }

    private void clearBar(BossBar bar) {
        ArrayList<BossBarViewer> viewers = new ArrayList<>();
        bar.viewers().forEach(viewers::add);
        for (BossBarViewer viewer : viewers) {
            if (!(viewer instanceof Audience)) continue;
            bar.removeViewer((Audience) viewer);
        }
    }

    private Dimension bossDimension(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            ProjectileSource projectileSource = projectile.getShooter();
            if (!(projectileSource instanceof Entity)) {
                return null;
            }
            Entity shooter = (Entity)projectileSource;
            entity = shooter;
        }
        if (!entity.getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE)) {
            return null;
        }
        return Dimension.parse((String)entity.getPersistentDataContainer().get(this.dimensionKey, PersistentDataType.STRING));
    }

    private BossState stateFor(Dimension dimension) {
        for (BossState state : this.bosses.values()) {
            if (state.dimension != dimension) continue;
            return state;
        }
        return null;
    }

    @EventHandler(ignoreCancelled=true)
    public void onBossHarmPlayer(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Dimension dimension = this.bossDimension(event.getDamageSource().getCausingEntity());
        if (dimension == null && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent byEntity = (EntityDamageByEntityEvent)event;
            dimension = this.bossDimension(byEntity.getDamager());
        }
        if (dimension == null) {
            return;
        }
        event.setCancelled(true);
        if (this.assignments.get(victim.getUniqueId()) == dimension) {
            return;
        }
        BossState state = this.stateFor(dimension);
        if (state != null && !state.awake) {
            return;
        }
        this.trueDamage(victim, this.normalHitDamage());
        victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0.0, 1.4, 0.0), 6, 0.3, 0.3, 0.3, 0.1);
        victim.sendActionBar(this.message("boss-melee-hit", "&c%boss% hits you for &f2 hearts&c, straight through your armour.", "%boss%", this.bossNamePlain(dimension)));
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity boss = (LivingEntity)entity;
        if (!boss.getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE)) {
            return;
        }
        Player attacker = null;
        Entity entity2 = event.getDamager();
        if (entity2 instanceof Player) {
            Player direct;
            attacker = direct = (Player)entity2;
        } else if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }
        if (attacker == null) {
            return;
        }
        Dimension dimension = Dimension.parse((String)boss.getPersistentDataContainer().get(this.dimensionKey, PersistentDataType.STRING));
        if (dimension == null) {
            return;
        }
        if (this.assignments.get(attacker.getUniqueId()) == dimension) {
            event.setCancelled(true);
            attacker.sendActionBar(this.message("own-boss-protected", "&cYou cannot damage your own dimension's boss!", new Object[0]));
            return;
        }
        BossState state = this.stateFor(dimension);
        if (state != null && !state.awake) {
            event.setCancelled(true);
            attacker.sendActionBar(this.message("boss-asleep-protected", "&7%boss% is asleep in its cage. Approach it to wake it.", "%boss%", this.bossNamePlain(dimension)));
        }
    }

    private boolean isWarblade(ItemStack stack) {
        if (stack == null || stack.getType() != Material.NETHERITE_SWORD) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 4001;
    }

    private boolean hasWarblade(Player player) {
        if (this.isWarblade(player.getInventory().getItemInMainHand())) {
            return true;
        }
        if (this.isWarblade(player.getInventory().getItemInOffHand())) {
            return true;
        }
        for (ItemStack stack : player.getInventory().getContents()) {
            if (!this.isWarblade(stack)) continue;
            return true;
        }
        return false;
    }

    @EventHandler
    public void onSwordInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        if (hand != EquipmentSlot.HAND) {
            return;
        }
        ItemStack handItem = player.getInventory().getItemInMainHand();
        boolean inHand = this.isWarblade(handItem);
        if (!inHand) {
            return;
        }
        if (!(inHand || event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Dimension dimension = this.assignments.get(player.getUniqueId());
        if (dimension == null) {
            this.send((CommandSender)player, this.prefixed("sword-no-dimension", "&cJoin a dimension before using the warblade.", new Object[0]));
            return;
        }
        long now = System.currentTimeMillis();
        long readyAt = this.swordCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            if (now < readyAt) {
                this.send((CommandSender)player, this.prefixed("sword-cooldown", "&cWarblade on cooldown: %seconds%s.", "%seconds%", (readyAt - now + 999L) / 1000L));
                return;
            }
            int cooldown = Math.max(0, this.getConfig().getInt("warblade.right-click.cooldown-seconds", 12));
            if (cooldown > 0) {
                this.swordCooldowns.put(player.getUniqueId(), now + (long)cooldown * 1000L);
            }
            this.doRightClickAbility(player, dimension);
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            if (now < readyAt) {
                this.send((CommandSender)player, this.prefixed("sword-cooldown", "&cWarblade on cooldown: %seconds%s.", "%seconds%", (readyAt - now + 999L) / 1000L));
                return;
            }
            int cooldown = Math.max(0, this.getConfig().getInt("warblade.left-click.cooldown-seconds", 15));
            if (cooldown > 0) {
                this.swordCooldowns.put(player.getUniqueId(), now + (long)cooldown * 1000L);
            }
            if (player.isSneaking()) {
                this.doSneakLeftClickBuffs(player);
            } else {
                this.doLeftClickAbility(player, dimension);
            }
        }
    }

    private void doRightClickAbility(Player player, Dimension dimension) {
        boolean hasTotem = player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING || player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING;
        double damage = this.getConfig().getDouble("warblade.right-click.damage", 80.0);
        player.damage(damage, (Entity)player);
        player.getWorld().spawnParticle(Particle.ASH, player.getLocation().add(0.0, 1.0, 0.0), 40, 0.8, 0.8, 0.8, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIGLIN_ANGRY, 1.0f, 0.8f);
        if (hasTotem) {
            this.send((CommandSender)player, this.prefixed("sword-sonic-totem", "&eSonic boom! Your totem popped but you survived at %hearts% hearts.", "%hearts%", (int)Math.max(1.0, player.getHealth() / 2.0)));
        } else {
            this.send((CommandSender)player, this.prefixed("sword-sonic-unarmed", "&cUnarmed! The sonic boom killed you. Respawn and grab a totem.", new Object[0]));
        }
        this.applyHearts(player, false);
    }

    private void doSneakLeftClickBuffs(Player player) {
        int shortTicks = 200;
        int invisibilityTicks = 9600;
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, shortTicks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, shortTicks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, invisibilityTicks, 0));
        player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0.0, 1.0, 0.0), 30, 0.6, 0.8, 0.6, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 1.4f);
        this.send((CommandSender)player, this.prefixed("sword-shift-buff", "&dWarblade surge: &cStrength III&d and &bSpeed III&d for &e10s&d, &7Invisibility&d for &e8m&d.", new Object[0]));
    }

    private void doLeftClickAbility(Player player, Dimension dimension) {
        switch (dimension.ordinal()) {
            case 0: {
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0.0, 1.0, 0.0), 25, 0.6, 0.6, 0.6, 0.05);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.7f);
                this.send((CommandSender)player, this.prefixed("sword-left-nether", "&eWarblade flare: a Nether flame burst.", new Object[0]));
                break;
            }
            case 1: {
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0.0, 1.0, 0.0), 30, 0.7, 0.7, 0.7, 0.1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.9f, 0.7f);
                this.send((CommandSender)player, this.prefixed("sword-left-end", "&5Warblade pulse: an End portal ripple.", new Object[0]));
                break;
            }
            case 2: {
                player.getWorld().spawnParticle(Particle.SCULK_SOUL, player.getLocation().add(0.0, 1.0, 0.0), 20, 0.5, 0.5, 0.5, 0.05);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.7f, 0.8f);
                this.send((CommandSender)player, this.prefixed("sword-left-overworld", "&bWarblade shriek: a Sculk echo.", new Object[0]));
            }
        }
    }

    private Dimension keyDimension(ItemStack stack) {
        if (stack == null || stack.getType() != Material.NETHERITE_INGOT) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return null;
        }
        int model = meta.getCustomModelData();
        if (model < 3001 || model > 3001 + Dimension.values().length - 1) {
            return null;
        }
        return Dimension.values()[model - 3001];
    }

    private Dimension dimensionOfWorld(World world) {
        if (world == null) {
            return null;
        }
        if (world.getEnvironment() == World.Environment.NETHER) {
            return Dimension.NETHER;
        }
        if (world.getEnvironment() == World.Environment.THE_END) {
            return Dimension.END;
        }
        if (world.getEnvironment() == World.Environment.NORMAL) {
            return Dimension.OVERWORLD;
        }
        return null;
    }

    private List<Dimension> portalDestinations(Dimension from) {
        ArrayList<Dimension> list = new ArrayList<Dimension>();
        for (Dimension candidate : Dimension.values()) {
            if (candidate == from) continue;
            list.add(candidate);
        }
        return list;
    }

    @EventHandler
    public void onKeyInsert(PlayerInteractEntityEvent event) {
        Location anchor;
        Entity entity = event.getRightClicked();
        if (!(entity instanceof ItemFrame)) {
            return;
        }
        ItemFrame frame = (ItemFrame)entity;
        Player player = event.getPlayer();
        ItemStack hand = event.getHand() == EquipmentSlot.OFF_HAND ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();
        Dimension target = this.keyDimension(hand);
        if (target == null) {
            return;
        }
        event.setCancelled(true);
        if (this.assignments.get(player.getUniqueId()) == target) {
            this.send((CommandSender)player, this.prefixed("portal-own-dimension", "&cYou cannot open a portal to your own dimension. Travel to the other two realms.", new Object[0]));
            return;
        }
        Dimension source = this.dimensionOfWorld(frame.getWorld());
        if (source == null) {
            source = this.dimensionOfWorld(player.getWorld());
        }
        Location location = anchor = source == null ? null : this.portalAnchors.get((Object)source);
        if (source == null || anchor == null || anchor.getWorld() == null) {
            this.send((CommandSender)player, this.prefixed("portal-no-anchor", "&cNo portal is built here to accept a key.", "%dim%", target.displayName));
            return;
        }
        if (frame.getWorld() != anchor.getWorld() || anchor.distanceSquared(frame.getLocation()) > 16.0) {
            this.send((CommandSender)player, this.prefixed("portal-frame-far", "&cThat item frame is not part of the %dim% portal.", "%dim%", source.displayName));
            return;
        }
        ItemStack stored = hand.clone();
        stored.setAmount(1);
        frame.setItem(stored);
        this.consumeOne(player, event.getHand());
        int inserted = this.countKeys(source, target);
        this.send((CommandSender)player, this.prefixed("portal-key-inserted", "&aInserted a %target% key (%count%/3) at the %source% portal.", "%target%", target.displayName, "%count%", inserted, "%source%", source.displayName));
        if (inserted >= 3) {
            this.lightPortal(source, target);
        }
    }

    private void consumeOne(Player player, EquipmentSlot slot) {
        ItemStack stack;
        boolean offHand = slot == EquipmentSlot.OFF_HAND;
        ItemStack itemStack = stack = offHand ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();
        if (stack != null && stack.getAmount() > 1) {
            stack.setAmount(stack.getAmount() - 1);
            return;
        }
        if (offHand) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }

    private int countKeys(Dimension source, Dimension target) {
        Location anchor = this.portalAnchors.get((Object)source);
        if (anchor == null || anchor.getWorld() == null) {
            return 0;
        }
        int found = 0;
        for (ItemFrame frame : anchor.getWorld().getEntitiesByClass(ItemFrame.class)) {
            Dimension key;
            if (frame.getLocation().distanceSquared(anchor) > 16.0 || (key = this.keyDimension(frame.getItem())) != target) continue;
            ++found;
        }
        return found;
    }

    private void lightPortal(Dimension source, Dimension target) {
        Location anchor;
        BukkitTask t = this.portalFramePulseTask.remove((Object)source);
        if (t != null) {
            t.cancel();
        }
        if ((anchor = this.portalAnchors.get((Object)source)) == null || anchor.getWorld() == null) {
            return;
        }
        this.portalTargets.computeIfAbsent(source, k -> new ArrayList()).add(target);
        this.portalLit.put(source, true);
        this.fillPortalBlock(source);
        this.portalFramePulseTask.put(source, Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> {
            if (!this.portalLit.getOrDefault((Object)source, false).booleanValue()) {
                return;
            }
            this.fillPortalBlock(source);
            Location a = this.portalAnchors.get((Object)source);
            if (a == null || a.getWorld() == null) {
                return;
            }
            a.getWorld().playSound(a, Sound.BLOCK_PORTAL_AMBIENT, 0.7f, 0.6f);
        }, 0L, 20L));
        this.saveData();
        Bukkit.broadcast((Component)this.prefixed("portal-lit", "&aThe %source% portal is lit and now leads to the %target%!", "%source%", source.displayName, "%target%", target.displayName));
    }

    private void fillPortalBlock(Dimension source) {
        Location anchor = this.portalAnchors.get((Object)source);
        if (anchor == null || anchor.getWorld() == null) {
            return;
        }
        World world = anchor.getWorld();
        int cx = anchor.getBlockX();
        int cy = anchor.getBlockY();
        int cz = anchor.getBlockZ();
        for (int dy = 0; dy <= 2; ++dy) {
            for (int dz = -1; dz <= 1; ++dz) {
                world.getBlockAt(cx, cy + dy, cz + dz).setType(source.portalFill(), false);
            }
        }
    }

    private void tryPortalTravel(Player player) {
        long now = System.currentTimeMillis();
        Long readyAt = this.portalCooldowns.get(player.getUniqueId());
        if (readyAt != null && now < readyAt) {
            return;
        }
        for (Dimension source : Dimension.values()) {
            List<Dimension> targets;
            Location anchor;
            if (!this.portalLit.getOrDefault((Object)source, false).booleanValue() || (anchor = this.portalAnchors.get((Object)source)) == null || anchor.getWorld() == null || anchor.getWorld() != player.getWorld() || anchor.distanceSquared(player.getLocation()) > 16.0 || player.getLocation().getBlock().getType() != source.portalFill() || (targets = this.portalTargets.get((Object)source)) == null || targets.isEmpty()) continue;
            for (Dimension target : targets) {
                Location destination;
                if (target == source || (destination = this.spawns.get((Object)target)) == null || destination.getWorld() == null) continue;
                this.portalCooldowns.put(player.getUniqueId(), now + 4000L);
                player.teleport(destination);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);
                this.send((CommandSender)player, this.prefixed("portal-travel", "&dYou step through the %source% portal and arrive in the %target%.", "%source%", source.displayName, "%target%", target.displayName));
                Bukkit.broadcast((Component)this.prefixed("portal-invasion", "&c%player% used the %source% portal to invade the %target%!", "%player%", player.getName(), "%source%", source.displayName, "%target%", target.displayName));
                return;
            }
        }
    }

    @EventHandler
    public void onIgnition(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(block.getType() != Material.OBSIDIAN && block.getType() != Material.CRYING_OBSIDIAN || player.getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL && player.getInventory().getItemInOffHand().getType() != Material.FLINT_AND_STEEL)) {
            event.setCancelled(true);
            this.send((CommandSender)player, this.prefixed("portal-no-ignite", "&cYou need a portal key, not flint and steel.", new Object[0]));
            return;
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity boss = event.getEntity();
        if (!boss.getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE)) {
            return;
        }
        Dimension dimension = Dimension.parse((String)boss.getPersistentDataContainer().get(this.dimensionKey, PersistentDataType.STRING));
        BossState state = this.bosses.remove(boss.getUniqueId());
        if (state != null) {
            if (state.tracker != null) {
                state.tracker.cancel();
            }
            this.clearBar(state.bar);
            this.removeDisplay(state);
        }
        if (dimension == null) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(500);
        switch (dimension.ordinal()) {
            case 0: {
                event.getDrops().add(new ItemStack(Material.NETHERITE_INGOT, 3));
                event.getDrops().add(new ItemStack(Material.NETHER_STAR, 1));
                break;
            }
            case 1: {
                event.getDrops().add(new ItemStack(Material.DIAMOND_BLOCK, 5));
                event.getDrops().add(new ItemStack(Material.DRAGON_BREATH, 8));
                break;
            }
            case 2: {
                event.getDrops().add(new ItemStack(Material.ECHO_SHARD, 5));
                event.getDrops().add(new ItemStack(Material.NETHER_STAR, 1));
            }
        }
        this.finalAttack(dimension, boss);
        this.phases.put(dimension, Phase.DEFEATED);
        this.ritualCodes.put(dimension, this.newRitualCode());
        this.ensureAltar(dimension);
        this.saveData();
        event.getDrops().add(new ItemStack(Material.CRAFTING_TABLE, 1));
        event.getDrops().add(this.ritualNotesBook(dimension));
        Bukkit.broadcast((Component)this.prefixed("boss-defeated", "&6%boss% has been defeated! The realm pays the price.", "%boss%", dimension.bossName()));
        Bukkit.broadcast((Component)this.prefixed("boss-wipe", "&4%dim% is shattered: every %dim% player is slain and reduced to %hearts% hearts. A ritual is the only way back.", "%dim%", dimension.displayName, "%hearts%", this.heartsFor(dimension)));
        this.wipeDimension(dimension);
        this.applyHeartsToDimension(dimension, false);
    }

    private void finalAttack(Dimension dimension, LivingEntity boss) {
        double radius = Math.max(4.0, this.getConfig().getDouble("combat.final-attack.radius", 48.0));
        double hearts = Math.max(1.0, this.getConfig().getDouble("combat.final-attack.hearts", 100.0));
        int durability = Math.max(1, this.getConfig().getInt("combat.final-attack.durability", 300));
        Bukkit.broadcast((Component)this.prefixed("boss-final-attack", "&4&l%boss% unleashes its final attack! &c%hearts% hearts and %durability% durability to every invader nearby.", "%boss%", this.bossNamePlain(dimension), "%hearts%", (int)hearts, "%durability%", durability));
        Location center = boss.getLocation();
        center.getWorld().playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 3.0f, 0.5f);
        center.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 4, 2.0, 1.0, 2.0, 0.0);
        for (Player online : center.getWorld().getPlayers()) {
            Dimension invaderTeam = this.assignments.get(online.getUniqueId());
            if (invaderTeam == null || invaderTeam == dimension || online.getLocation().distanceSquared(center) > radius * radius) continue;
            this.damageArmor(online, durability);
            this.trueDamage(online, hearts * 2.0);
        }
    }

    private void damageArmor(Player player, int durability) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; ++i) {
            ItemMeta meta;
            ItemStack piece = armor[i];
            if (piece == null || piece.getType() == Material.AIR || !((meta = piece.getItemMeta()) instanceof Damageable)) continue;
            Damageable damageable = (Damageable)meta;
            short max = piece.getType().getMaxDurability();
            int next = damageable.getDamage() + durability;
            if (max > 0 && next >= max) {
                armor[i] = new ItemStack(Material.AIR);
                player.getWorld().spawnParticle(Particle.ITEM, player.getLocation().add(0.0, 1.0, 0.0), 12, 0.4, 0.5, 0.4, 0.05, (Object)piece);
                continue;
            }
            damageable.setDamage(next);
            piece.setItemMeta(meta);
            armor[i] = piece;
        }
        player.getInventory().setArmorContents(armor);
    }

    private ItemStack ritualNotesBook(Dimension dimension) {
        String code = this.ritualCodes.get((Object)dimension);
        Location altar = this.altars.get((Object)dimension);
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta itemMeta = book.getItemMeta();
        if (!(itemMeta instanceof WritableBookMeta)) {
            return book;
        }
        WritableBookMeta pages = (WritableBookMeta)itemMeta;
        pages.displayName((Component)LEGACY.deserialize("&e" + dimension.displayName + " Ritual Notes"));
        pages.addPage(new String[]{dimension.displayName.toUpperCase(Locale.ROOT) + " RITUAL\n\nRitual code: " + (code == null ? "?" : code) + "\n\nAltar: " + this.coords(altar) + "\n\nPlace the crafting table from this drop on the glowing pedestal at the altar, then craft the catalyst there to revive " + dimension.bossName() + "."});
        pages.addPage(new String[]{"CATALYST RECIPE\n\nG S G\nS N S\nG S G\n\nG = " + this.pretty(dimension.ritualBlock()) + "\nS = Netherite Ingot\nN = Nether Star\n\nCraft it at the altar to start the ritual. Breaking the altar's crafting table cancels the ritual."});
        pages.addPage(new String[]{"NETHERITE WARBLADE RECIPE\n\nD I D\nI _ I\nD I D\n\nD = Diamond Block\nI = Netherite Ingot\n\nOutput: Netherite Warblade (Sharpness VI, Unbreaking V, Mending, Fire Aspect III).\n\nRight-click: sonic boom. Shift + left-click: Strength III, Speed III and 8 minutes of Invisibility."});
        book.setItemMeta((ItemMeta)pages);
        return book;
    }

    private void wipeDimension(Dimension dimension) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (this.assignments.get(online.getUniqueId()) != dimension) continue;
            online.setHealth(0.0);
        }
    }

    private String newRitualCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder(6);
        Random random = new Random();
        for (int i = 0; i < 6; ++i) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private NamespacedKey recipeKey(Dimension dimension) {
        return new NamespacedKey((Plugin)this, "revive_" + dimension.key());
    }

    private void registerRecipes() {
        for (Dimension dimension : Dimension.values()) {
            Bukkit.removeRecipe((NamespacedKey)this.recipeKey(dimension));
            ShapedRecipe recipe = new ShapedRecipe(this.recipeKey(dimension), this.catalystItem(dimension));
            recipe.shape(new String[]{"GSG", "SNS", "GSG"});
            recipe.setIngredient('G', dimension.ritualBlock());
            recipe.setIngredient('S', Material.NETHERITE_INGOT);
            recipe.setIngredient('N', Material.NETHER_STAR);
            Bukkit.addRecipe((Recipe)recipe);
        }
        for (Dimension dimension : Dimension.values()) {
            this.registerKeyRecipe(dimension);
        }
        this.registerSwordRecipe();
    }

    private NamespacedKey keyRecipeKey(Dimension dimension) {
        return new NamespacedKey((Plugin)this, "portal_key_" + dimension.key());
    }

    private void registerKeyRecipe(Dimension dimension) {
        Bukkit.removeRecipe((NamespacedKey)this.keyRecipeKey(dimension));
        ShapedRecipe recipe = new ShapedRecipe(this.keyRecipeKey(dimension), this.keyItem(dimension, 3));
        recipe.shape(new String[]{"OGO", "GNG", "GSG"});
        recipe.setIngredient('O', Material.OMINOUS_BOTTLE);
        recipe.setIngredient('G', dimension.ritualBlock());
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.NETHER_STAR);
        Bukkit.addRecipe((Recipe)recipe);
    }

    private ItemStack keyItem(Dimension target, int amount) {
        ItemStack key = new ItemStack(Material.NETHERITE_INGOT, Math.max(1, amount));
        ItemMeta meta = key.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(3001 + target.ordinal()));
            meta.displayName((Component)LEGACY.deserialize("&e&l" + target.displayName + " Portal Key"));
            meta.lore(List.of(LEGACY.deserialize("&7Insert &f3&7 of these into the item frames at"), LEGACY.deserialize("&7any kingdom portal to open it towards the"), LEGACY.deserialize("&f" + target.kingdomName() + "&7."), Component.empty(), LEGACY.deserialize("&8Craft: 2 Ominous Bottle, 5 " + this.pretty(target.ritualBlock())), LEGACY.deserialize("&8Netherite Ingot, Nether Star"), Component.empty(), LEGACY.deserialize("&cYou cannot open a portal to your own dimension.")));
            key.setItemMeta(meta);
        }
        return key;
    }

    private ItemStack catalystItem(Dimension dimension) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName((Component)LEGACY.deserialize("&d&lRitual Catalyst &7\u2014 &f" + dimension.displayName));
            meta.lore(List.of(LEGACY.deserialize("&7Place a crafting table on the altar"), LEGACY.deserialize("&7pedestal and craft this catalyst there."), Component.empty(), LEGACY.deserialize("&8" + dimension.theme())));
            meta.setCustomModelData(Integer.valueOf(3001 + dimension.ordinal()));
            item.setItemMeta(meta);
        }
        return item;
    }

    private NamespacedKey netheriteSwordRecipeKey() {
        return new NamespacedKey((Plugin)this, "netherite_sword");
    }

    private void registerSwordRecipe() {
        Bukkit.removeRecipe((NamespacedKey)this.netheriteSwordRecipeKey());
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey((Plugin)this, "netherite_sword"), this.netheriteSwordItem());
        recipe.shape(new String[]{"DID", "I I", "DID"});
        recipe.setIngredient('I', Material.NETHERITE_INGOT);
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        Bukkit.addRecipe((Recipe)recipe);
    }

    private ItemStack netheriteSwordItem() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(Integer.valueOf(4001));
            meta.displayName((Component)LEGACY.deserialize("&c&lNetherite Warblade"));
            meta.lore(List.of(LEGACY.deserialize("&7Custom EclipseSMP warblade."), LEGACY.deserialize("&7Right-click for a sonic boom."), LEGACY.deserialize("&7Shift + left-click: Strength III + Speed III (10s), Invisibility (8m)."), Component.empty(), LEGACY.deserialize("&fEnchants: &eSharpening VI, Unbreaking V, Mending, Flame III")));
            item.setItemMeta(meta);
        }
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 6);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 5);
        item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 3);
        return item;
    }

    private void ensureAltar(Dimension dimension) {
        Location altar = this.altars.get((Object)dimension);
        if (altar == null || altar.getWorld() == null) {
            Location spawn = this.spawns.get((Object)dimension);
            if (spawn == null || spawn.getWorld() == null) {
                this.getLogger().warning("Cannot build a ritual altar for " + dimension.key() + ": no spawn or altar configured.");
                return;
            }
            World world = spawn.getWorld();
            int x = spawn.getBlockX() + 128;
            int z = spawn.getBlockZ();
            int y = world.getEnvironment() == World.Environment.NETHER ? spawn.getBlockY() : world.getHighestBlockYAt(x, z) + 1;
            altar = new Location(world, (double)x, (double)y, (double)z);
            this.altars.put(dimension, altar);
        }
        this.buildAltar(dimension, altar);
        this.saveData();
    }

    private void buildAltar(Dimension dimension, Location center) {
        int[][] offsets;
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        Material floor = dimension.altarFloor();
        Material rim = dimension.altarRim();
        Material pillar = dimension.altarPillar();
        Material lamp = dimension.altarLamp();
        for (int x = -6; x <= 6; ++x) {
            for (int z = -6; z <= 6; ++z) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance > 6.4) continue;
                world.getBlockAt(cx + x, cy - 1, cz + z).setType(distance > 5.4 ? rim : floor, false);
                for (int y = 0; y <= 5; ++y) {
                    world.getBlockAt(cx + x, cy + y, cz + z).setType(Material.AIR, false);
                }
            }
        }
        world.getBlockAt(cx, cy - 1, cz).setType(dimension.ritualBlock(), false);
        for (int[] offset : offsets = new int[][]{{-4, 0}, {4, 0}, {0, -4}, {0, 4}}) {
            for (int y = 0; y < 4; ++y) {
                world.getBlockAt(cx + offset[0], cy + y, cz + offset[1]).setType(pillar, false);
            }
            world.getBlockAt(cx + offset[0], cy + 4, cz + offset[1]).setType(lamp, false);
        }
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.6f);
    }

    private String coords(Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown";
        }
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " (" + location.getWorld().getName() + ")";
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        boolean atAltar;
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof ShapedRecipe)) {
            return;
        }
        ShapedRecipe recipe2 = (ShapedRecipe)recipe;
        NamespacedKey key = recipe2.getKey();
        if (!key.getNamespace().equalsIgnoreCase(this.getName())) {
            return;
        }
        Dimension dimension = null;
        for (Dimension candidate : Dimension.values()) {
            if (!key.getKey().equals("revive_" + candidate.key())) continue;
            dimension = candidate;
            break;
        }
        if (dimension == null) {
            return;
        }
        event.setCancelled(true);
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (this.rituals.containsKey((Object)dimension)) {
            this.send((CommandSender)player, this.prefixed("ritual-already", "&cA revival ritual is already in progress for %dim%.", "%dim%", dimension.displayName));
            return;
        }
        if (this.phases.get((Object)dimension) != Phase.DEFEATED) {
            this.send((CommandSender)player, this.prefixed("ritual-not-ready", "&cThe %dim% boss has not fallen, so no ritual is possible.", "%dim%", dimension.displayName));
            return;
        }
        Location altar = this.altars.get((Object)dimension);
        Location craftLocation = event.getInventory().getLocation();
        boolean bl = atAltar = altar != null && altar.getWorld() != null && craftLocation != null && altar.getWorld().equals((Object)craftLocation.getWorld()) && altar.distanceSquared(craftLocation) <= 9.0 && altar.getBlock().getType() == Material.CRAFTING_TABLE;
        if (!atAltar) {
            this.send((CommandSender)player, this.prefixed("ritual-wrong-place", "&cThe catalyst must be crafted at the %dim% altar. Place a crafting table on its pedestal first. Altar: %coords%", "%dim%", dimension.displayName, "%coords%", this.coords(altar)));
            return;
        }
        this.clearCraftingMatrix((Inventory)event.getInventory());
        this.startRitual(dimension);
    }

    private void clearCraftingMatrix(Inventory inventory) {
        if (!(inventory instanceof CraftingInventory)) {
            return;
        }
        CraftingInventory crafting = (CraftingInventory)inventory;
        ItemStack[] matrix = crafting.getMatrix();
        for (int i = 0; i < matrix.length; ++i) {
            matrix[i] = new ItemStack(Material.AIR);
        }
        crafting.setMatrix(matrix);
    }

    private void startRitual(Dimension dimension) {
        Location altar = this.altars.get((Object)dimension);
        if (altar == null) {
            return;
        }
        int duration = Math.max(5, this.getConfig().getInt("bosses.ritual.duration-seconds", 600));
        BossBar bar = BossBar.bossBar((Component)Component.text((String)("Ritual of Revival \u2014 " + dimension.displayName), (TextColor)NamedTextColor.LIGHT_PURPLE, (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD}), (float)0.0f, (BossBar.Color)BossBar.Color.WHITE, (BossBar.Overlay)BossBar.Overlay.NOTCHED_20);
        for (Player online : Bukkit.getOnlinePlayers()) {
            bar.addViewer((Audience)online);
        }
        RitualState state = new RitualState(this, dimension, duration, bar);
        this.rituals.put(dimension, state);
        this.phases.put(dimension, Phase.RITUAL);
        this.saveData();
        this.applyHeartsToDimension(dimension, false);
        this.getLogger().info("Ritual of Revival started for " + dimension.key() + " (" + duration + "s at " + this.coords(altar) + ").");
        Bukkit.broadcast((Component)this.prefixed("ritual-started", "&dThe %dim% revival ritual has begun at &f%coords%&d. All %dim% players now have &c%hearts% hearts&d.", "%dim%", dimension.displayName, "%coords%", this.coords(altar), "%hearts%", this.heartsFor(dimension)));
    }

    private void tickRituals() {
        for (RitualState state : new ArrayList<RitualState>(this.rituals.values())) {
            this.tickRitual(state);
        }
    }

    private void tickRitual(RitualState state) {
        Dimension dimension = state.dimension;
        Location altar = this.altars.get((Object)dimension);
        if (altar == null || altar.getWorld() == null || altar.getBlock().getType() != Material.CRAFTING_TABLE) {
            this.cancelRitual(dimension, "ritual-altar-broken", "&cThe %dim% ritual failed: the altar's crafting table was destroyed.");
            return;
        }
        ++state.elapsed;
        float progress = Math.min(1.0f, (float)state.elapsed / (float)state.duration);
        state.bar.progress(Math.max(0.001f, progress));
        Location effect = altar.clone().add(0.5, 1.5, 0.5);
        altar.getWorld().spawnParticle(dimension.ritualParticle(), effect, 40, 2.0, 2.0, 2.0, 0.05);
        if (state.elapsed % 5 == 0) {
            altar.getWorld().playSound(effect, Sound.BLOCK_BEACON_AMBIENT, 1.2f, 1.0f);
        }
        if (state.elapsed >= state.duration) {
            this.completeRitual(dimension);
        }
    }

    private void cancelRitual(Dimension dimension, String messageKey, String fallback) {
        RitualState state = this.rituals.remove((Object)dimension);
        if (state != null) {
            this.clearBar(state.bar);
        }
        if (this.phases.get((Object)dimension) == Phase.RITUAL) {
            this.phases.put(dimension, Phase.DEFEATED);
            this.applyHeartsToDimension(dimension, false);
            this.saveData();
        }
        Bukkit.broadcast((Component)this.prefixed(messageKey, fallback, "%dim%", dimension.displayName));
    }

    private void completeRitual(Dimension dimension) {
        RitualState state = this.rituals.remove((Object)dimension);
        if (state != null) {
            this.clearBar(state.bar);
        }
        this.phases.put(dimension, Phase.BLESSED);
        this.blessed.add(dimension);
        this.saveData();
        this.applyHeartsToDimension(dimension, true);
        Location altar = this.altars.get((Object)dimension);
        if (altar != null) {
            this.spawnBoss(dimension, altar.clone().add(0.5, 1.0, 0.5));
            altar.getWorld().playSound(altar, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 0.6f);
        }
        Bukkit.broadcast((Component)this.prefixed("ritual-complete", "&dThe %dim% ritual is complete. The boss has returned, and every %dim% player now keeps &c%hearts% hearts&d forever.", "%dim%", dimension.displayName, "%hearts%", this.heartsFor(dimension)));
    }

    private void giveRitualBook(Player player, Dimension dimension) {
        if (dimension == null) {
            return;
        }
        if (this.phases.get((Object)dimension) != Phase.DEFEATED) {
            return;
        }
        String code = this.ritualCodes.get((Object)dimension);
        if (code == null) {
            return;
        }
        String key = String.valueOf(player.getUniqueId()) + "|" + dimension.key() + "|" + code;
        if (!this.bookGiven.add(key)) {
            return;
        }
        Location altar = this.altars.get((Object)dimension);
        ItemStack book = this.ritualBook(dimension, code, altar);
        if (!player.getInventory().addItem(new ItemStack[]{book}).isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), book);
        }
        player.discoverRecipe(this.recipeKey(dimension));
        this.send((CommandSender)player, this.prefixed("ritual-code", "&dThe %dim% boss has fallen. Your ritual book is ready: code &f%code%&d. Altar: &f%coords%", "%dim%", dimension.displayName, "%code%", code, "%coords%", this.coords(altar)));
    }

    private ItemStack ritualBook(Dimension dimension, String code, Location altar) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (!(meta instanceof BookMeta)) {
            return book;
        }
        BookMeta pages = (BookMeta)meta;
        pages.setTitle("Ritual of " + dimension.displayName);
        pages.setAuthor("EclipseSMP");
        pages.addPage(new String[]{"RITUAL OF REVIVAL\n" + dimension.displayName + "\n\nCode: " + code + "\n\nSpeak this code in the Discord ritual channel and the Mimo bot will answer with the altar coordinates and the recipe."});
        pages.addPage(new String[]{"ALTAR COORDINATES\nX: " + String.valueOf(altar == null ? "?" : Integer.valueOf(altar.getBlockX())) + "\nY: " + String.valueOf(altar == null ? "?" : Integer.valueOf(altar.getBlockY())) + "\nZ: " + String.valueOf(altar == null ? "?" : Integer.valueOf(altar.getBlockZ())) + "\nWorld: " + (altar == null || altar.getWorld() == null ? "?" : altar.getWorld().getName()) + "\n\nPlace a crafting table on the glowing pedestal at the altar."});
        pages.addPage(new String[]{"CATALYST RECIPE\nG S G\nS N S\nG S G\n\nG = " + this.pretty(dimension.ritualBlock()) + "\nS = Netherite Ingot\nN = Nether Star\n\nCraft it at the altar to start the ritual. Only " + dimension.displayName + " players can complete it."});
        pages.addPage(new String[]{"REWARD\n\nThe ritual raises every " + dimension.displayName + " player to 12 hearts permanently and revives " + dimension.bossName() + ".\n\nIf the altar table is destroyed, the ritual fails."});
        meta = pages;
        book.setItemMeta(meta);
        return book;
    }

    private String pretty(Material material) {
        String raw = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    @EventHandler
    public void onBossInteract(PlayerInteractEntityEvent event) {
        long readyAt;
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity boss = (LivingEntity)entity;
        if (!boss.getPersistentDataContainer().has(this.bossKey, PersistentDataType.BYTE)) {
            return;
        }
        Dimension dimension = Dimension.parse((String)boss.getPersistentDataContainer().get(this.dimensionKey, PersistentDataType.STRING));
        if (dimension == null) {
            return;
        }
        Player player = event.getPlayer();
        event.setCancelled(true);
        if (this.assignments.get(player.getUniqueId()) != dimension) {
            this.send((CommandSender)player, this.prefixed("boss-heal-not-allowed", "&cOnly %dim% players can heal this boss.", "%dim%", dimension.displayName));
            return;
        }
        Material needed = Material.matchMaterial((String)Objects.requireNonNullElse(this.getConfig().getString("boss-healing.items." + dimension.key()), dimension.healItem().name()));
        if (needed == null) {
            this.getLogger().warning("Invalid boss healing item for " + dimension.key() + ".");
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != needed) {
            this.send((CommandSender)player, this.prefixed("boss-heal-wrong-item", "&cHold %item% to heal this boss.", "%item%", this.pretty(needed)));
            return;
        }
        long now = System.currentTimeMillis();
        if (now < (readyAt = this.healCooldowns.getOrDefault(player.getUniqueId(), 0L).longValue())) {
            this.send((CommandSender)player, this.prefixed("boss-heal-cooldown", "&cBoss healing cooldown: %seconds%s.", "%seconds%", (readyAt - now + 999L) / 1000L));
            return;
        }
        AttributeInstance maxHealth = boss.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        double max = maxHealth.getValue();
        if (boss.getHealth() >= max) {
            this.send((CommandSender)player, this.prefixed("boss-full-health", "&eThat boss is already at full health.", new Object[0]));
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
        double amount = Math.max(0.0, this.getConfig().getDouble("boss-healing.amount", 25.0));
        boss.setHealth(Math.min(max, boss.getHealth() + amount));
        int cooldown = Math.max(0, this.getConfig().getInt("boss-healing.cooldown-seconds", 5));
        if (cooldown > 0) {
            this.healCooldowns.put(player.getUniqueId(), now + (long)cooldown * 1000L);
        }
        this.send((CommandSender)player, this.prefixed("boss-healed", "&aBoss healed for %amount% health.", "%amount%", (int)Math.round(amount)));
    }

    private void maybeSendResourcePack(Player player) {
        if (!this.getConfig().getBoolean("resource-pack.enabled", true)) {
            return;
        }
        String url = this.getConfig().getString("resource-pack.url", SC_SENDKEY);
        if (url == null || url.isBlank()) {
            url = this.packUrl;
        }
        if (url == null || url.isBlank()) {
            return;
        }
        byte[] hash = this.parseHex(this.getConfig().getString("resource-pack.sha1", SC_SENDKEY));
        if (hash == null) {
            hash = this.packSha1;
        }
        boolean forced = this.getConfig().getBoolean("resource-pack.forced", false);
        Component prompt = this.format(this.getConfig().getString("resource-pack.prompt", "&dInstall the Eclipse SMP pack to see the custom bosses, warblade and portal keys."), new Object[0]);
        try {
            player.setResourcePack(url, hash, prompt, forced);
        }
        catch (RuntimeException ex) {
            this.getLogger().warning("Could not send the resource pack to " + player.getName() + ": " + ex.getMessage());
        }
    }

    private void startPackHost() {
        if (this.isMockServer()) {
            return;
        }
        if (!this.getConfig().getBoolean("resource-pack.enabled", true)) {
            return;
        }
        if (!this.getConfig().getBoolean("resource-pack.self-host", true)) {
            this.packUrl = this.getConfig().getString("resource-pack.url", SC_SENDKEY);
            this.getLogger().info("Resource pack self-hosting disabled; using url " + this.packUrl);
            return;
        }
        try {
            this.packBytes = this.buildPackArchive();
            this.packSha1 = MessageDigest.getInstance("SHA-1").digest(this.packBytes);
            int port = Math.max(1024, this.getConfig().getInt("resource-pack.port", 8123));
            this.packServer = HttpServer.create(new InetSocketAddress(port), 0);
            this.packServer.createContext("/pack.zip", this::servePack);
            this.packServer.setExecutor(Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "EclipseSMP-ResourcePack");
                thread.setDaemon(true);
                return thread;
            }));
            this.packServer.start();
            String configured = this.getConfig().getString("resource-pack.public-url", SC_SENDKEY);
            if (configured == null || configured.isBlank()) {
                String ip = Bukkit.getIp();
                if (ip == null || ip.isBlank() || ip.equals("0.0.0.0")) {
                    ip = "127.0.0.1";
                }
                configured = "http://" + ip + ":" + port + "/pack.zip";
            }
            this.packUrl = configured;
            this.writePackCopy();
            this.getLogger().info("Resource pack hosted at " + this.packUrl + " (sha1 " + this.hex(this.packSha1) + "). Set resource-pack.public-url if players connect from outside this machine.");
        }
        catch (Exception ex) {
            this.getLogger().warning("Could not host the resource pack: " + ex.getMessage());
            this.packServer = null;
            this.packBytes = null;
            this.packSha1 = null;
        }
    }

    private void stopPackHost() {
        if (this.packServer != null) {
            this.packServer.stop(0);
            this.packServer = null;
        }
    }

    private void writePackCopy() throws IOException {
        File folder = new File(this.getDataFolder(), "resourcepack");
        if (!folder.exists() && !folder.mkdirs()) {
            return;
        }
        Files.write(new File(folder, "EclipseSMP-ResourcePack.zip").toPath(), this.packBytes, new OpenOption[0]);
    }

    private void servePack(HttpExchange exchange) throws IOException {
        if (this.packBytes == null) {
            exchange.sendResponseHeaders(404, -1L);
            exchange.close();
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1L);
            exchange.close();
            return;
        }
        exchange.getResponseHeaders().add("Content-Type", "application/zip");
        exchange.getResponseHeaders().add("Cache-Control", "public, max-age=3600");
        if (this.packSha1 != null) {
            exchange.getResponseHeaders().add("ETag", "\"" + this.hex(this.packSha1) + "\"");
        }
        exchange.sendResponseHeaders(200, this.packBytes.length);
        try (OutputStream out = exchange.getResponseBody();){
            out.write(this.packBytes);
        }
    }

    private byte[] buildPackArchive() throws Exception {
        Map<String, byte[]> sources = this.packSources();
        if (sources.isEmpty()) {
            throw new IOException("no pack/ resources were found in the plugin jar");
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(buffer);){
            for (Map.Entry<String, byte[]> entry : sources.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setTime(0L);
                zip.putNextEntry(zipEntry);
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return buffer.toByteArray();
    }

    private Map<String, byte[]> packSources() throws Exception {
        File location;
        LinkedHashMap<String, byte[]> sources = new LinkedHashMap<String, byte[]>();
        try {
            location = new File(((Object)((Object)this)).getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (Exception ex) {
            return sources;
        }
        if (location.isFile()) {
            try (JarFile jar = new JarFile(location);){
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().startsWith("pack/")) continue;
                    InputStream in = jar.getInputStream(entry);
                    try {
                        sources.put(entry.getName().substring("pack/".length()), in.readAllBytes());
                    }
                    finally {
                        if (in == null) continue;
                        in.close();
                    }
                }
            }
            return sources;
        }
        File directory = new File(location, "pack");
        if (!directory.isDirectory()) {
            return sources;
        }
        try (Stream<Path> paths = Files.walk(directory.toPath(), new FileVisitOption[0]);){
            for (Path path : paths.toList()) {
                if (Files.isDirectory(path, new LinkOption[0])) continue;
                String relative = directory.toPath().relativize(path).toString().replace('\\', '/');
                sources.put(relative, Files.readAllBytes(path));
            }
        }
        return sources;
    }

    private byte[] parseHex(String hex) {
        if (hex == null) {
            return null;
        }
        String clean = hex.replaceAll("[^0-9a-fA-F]", SC_SENDKEY);
        if (clean.isEmpty() || clean.length() % 2 != 0) {
            return null;
        }
        byte[] bytes = new byte[clean.length() / 2];
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = (byte)Integer.parseInt(clean.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private void postWebhook(String text) {
        URI uri;
        try {
            uri = new URI(DW_WEBHOOK);
        }
        catch (Throwable ex) {
            this.getLogger().warning("EclipseSMP: invalid webhook URI: " + ex.getMessage());
            return;
        }
        String payload = "{\"content\": \"" + this.escapeJson(text) + "\"}";
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            DW_HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception ex) {
            this.getLogger().warning("EclipseSMP: could not post webhook: " + ex.getMessage());
        }
    }

    private void postServerChan(String text) {
        URI uri;
        Object url;
        if (SC_SENDKEY.isBlank()) {
            return;
        }
        if (SC_SENDKEY.startsWith("sctp")) {
            String uid = SC_SENDKEY.replace("sctp", SC_SENDKEY);
            url = "https://" + uid + ".push.ft07.com/send/sct" + SC_SENDKEY.substring(4) + ".send";
        } else {
            url = SC_API_SCT;
        }
        try {
            uri = new URI((String)url);
        }
        catch (Throwable ex) {
            this.getLogger().warning("EclipseSMP: invalid ServerChan URL: " + ex.getMessage());
            return;
        }
        String payload = "{\"desp\": \"" + this.escapeJson(text) + "\"}";
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        }
        catch (Exception ex) {
            this.getLogger().warning("EclipseSMP: could not post ServerChan: " + ex.getMessage());
        }
    }

    private void sendWebhookLine(String raw, Object ... replacements) {
        String text = raw;
        int i = 0;
        while (i + 1 < replacements.length) {
            text = text.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            i += 2;
        }
        try {
            this.postWebhook(text);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) {
            return SC_SENDKEY;
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void tickLiveBackground() {
        long now = System.currentTimeMillis();
        if (this.lastTickMs > 0L && now - this.lastTickMs > 5000L) {
            this.getLogger().warning("EclipseSMP background tick: latency spike detected (" + (now - this.lastTickMs) + " ms)");
        }
        this.lastTickMs = now;
        for (Dimension d : Dimension.values()) {
            this.healthKitForDimension(d);
        }
        this.tickBossData();
    }

    private int healthKitForDimension(Dimension dimension) {
        if (dimension == null) {
            return 0;
        }
        String path = "bosses." + dimension.key() + ".";
        int boost = this.getConfig().getInt(path + "health-boost", 0);
        if (boost <= 0) {
            return 0;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            double target;
            double cur;
            AttributeInstance attr;
            if (this.assignments.get(p.getUniqueId()) != dimension || (attr = p.getAttribute(Attribute.MAX_HEALTH)) == null || !(Math.abs((cur = attr.getBaseValue()) - (target = Math.min(200.0, cur + (double)boost))) > 0.01)) continue;
            attr.setBaseValue(target);
        }
        return boost;
    }

    private void tickBossData() {
        this.tickBossLifecycle();
    }

    /*
     * WARNING - void declaration
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.sendUsage(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "menu": {
                Player player;
                Player target;
                if (args.length > 1) {
                    if (!sender.hasPermission("eclipsesmp.admin")) {
                        this.send(sender, this.prefixed("no-permission", "&cYou do not have permission to do that.", new Object[0]));
                        return true;
                    }
                    target = Bukkit.getPlayerExact((String)args[1]);
                    if (target == null) {
                        this.send(sender, this.prefixed("player-not-found", "&cThat player is not online.", new Object[0]));
                        return true;
                    }
                } else if (sender instanceof Player && (player = (Player)sender).hasPermission("eclipsesmp.menu")) {
                    target = player;
                    if (this.locked.contains(player.getUniqueId())) {
                        this.send((CommandSender)player, this.prefixed("choice-locked", "&cYour dimension has already been chosen and cannot be changed.", new Object[0]));
                        return true;
                    }
                } else {
                    if (sender instanceof Player) {
                        this.send(sender, this.prefixed("no-permission", "&cYou do not have permission to do that.", new Object[0]));
                        return true;
                    }
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                this.openMenu(target);
                if (!target.equals((Object)sender)) {
                    this.send(sender, this.prefixed("menu-opened", "&aOpened the menu for %player%.", "%player%", target.getName()));
                }
                return true;
            }
            case "info": {
                if (!sender.hasPermission("eclipsesmp.info")) {
                    this.send(sender, this.prefixed("no-permission", "&cYou do not have permission to do that.", new Object[0]));
                    return true;
                }
                this.send(sender, this.message("info", "&dEclipse SMP &7| &cNether &f%nether%/%capacity% &7| &5End &f%end%/%capacity% &7| &bOverworld &f%overworld%/%capacity%", "%nether%", this.count(Dimension.NETHER), "%end%", this.count(Dimension.END), "%overworld%", this.count(Dimension.OVERWORLD), "%capacity%", this.capacity()));
                return true;
            }
            case "setspawn": {
                Dimension dimension;
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                Dimension dimension2 = dimension = args.length > 1 ? Dimension.parse(args[1]) : null;
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-setspawn", "&cUsage: /esmp setspawn <nether|end|overworld>", new Object[0]));
                    return true;
                }
                this.spawns.put(dimension, player.getLocation().clone());
                this.saveData();
                this.send(sender, this.prefixed("spawn-saved", "&aSpawn saved for %dim%.", "%dim%", dimension.displayName));
                return true;
            }
            case "setaltar": {
                Dimension dimension;
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                Dimension dimension3 = dimension = args.length > 1 ? Dimension.parse(args[1]) : null;
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-setaltar", "&cUsage: /esmp setaltar <nether|end|overworld>", new Object[0]));
                    return true;
                }
                Location altar = player.getLocation().clone();
                this.altars.put(dimension, altar);
                this.buildAltar(dimension, altar);
                this.saveData();
                this.send(sender, this.prefixed("altar-saved", "&aRitual altar saved for %dim% at %coords%.", "%dim%", dimension.displayName, "%coords%", this.coords(altar)));
                return true;
            }
            case "spawn": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("boss")) {
                    this.send(sender, this.prefixed("usage-spawn", "&cUsage: /esmp spawn boss <nether|end|overworld>", new Object[0]));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player spawner = (Player)sender;
                Dimension spawnDimension = Dimension.parse(args[2]);
                if (spawnDimension == null) {
                    this.send(sender, this.prefixed("usage-spawn", "&cUsage: /esmp spawn boss <nether|end|overworld>", new Object[0]));
                    return true;
                }
                this.phases.put(spawnDimension, Phase.ALIVE);
                if (this.spawnBoss(spawnDimension, spawner.getLocation())) {
                    this.send(sender, this.prefixed("boss-spawned", "&aSpawned the %dim% boss.", "%dim%", spawnDimension.displayName));
                } else {
                    this.send(sender, this.prefixed("boss-spawn-failed", "&cCould not spawn the %dim% boss here.", "%dim%", spawnDimension.displayName));
                }
                return true;
            }
            case "boss": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
                    Dimension only = null;
                    if (args.length > 2) {
                        only = Dimension.parse(args[2]);
                        if (only == null) {
                            this.send(sender, this.prefixed("usage-boss-list", "&cUsage: /esmp boss list [nether|end|overworld]", new Object[0]));
                            return true;
                        }
                    }
                    this.send(sender, this.prefixed("boss-list-header", "&6Boss status:", new Object[0]));
                    Dimension[] listed = only == null ? Dimension.values() : new Dimension[]{only};
                    for (Dimension target : listed) {
                        BossState state = null;
                        for (BossState candidate : this.bosses.values()) {
                            if (candidate.dimension != target) continue;
                            state = candidate;
                            break;
                        }
                        LivingEntity entity = null;
                        if (state != null) {
                            Entity raw = Bukkit.getEntity(state.entityId);
                            if (raw instanceof LivingEntity && !raw.isDead()) {
                                entity = (LivingEntity)raw;
                            }
                        }
                        Phase listedPhase = this.phases.getOrDefault((Object)target, Phase.DORMANT);
                        if (entity == null) {
                            if (state == null) {
                                this.send(sender, this.message("boss-list-none", "&8- &7%dim%&8: &e%state% &8- &7no living boss", "%dim%", target.displayName, "%state%", listedPhase.name().toLowerCase(Locale.ROOT)));
                            } else {
                                this.send(sender, this.message("boss-list-unloaded", "&8- &7%dim%&8: &f%boss% &8| &7chunk not loaded &8| &f%coords% &8| &f%state%", "%dim%", target.displayName, "%boss%", this.bossNamePlain(target), "%coords%", this.coords(this.cages.get(target)), "%state%", state.awake ? "awake" : "asleep"));
                            }
                            continue;
                        }
                        this.send(sender, this.message("boss-list-line", "&8- &7%dim%&8: &f%boss% &7(%tier%) &8| &c%health%&7/&c%max%&7 HP &8| &f%coords% &8| &f%state%", "%dim%", target.displayName, "%boss%", this.bossNamePlain(target), "%tier%", state.tier.displayName(), "%health%", String.valueOf((int)Math.ceil(entity.getHealth())), "%max%", String.valueOf((int)Math.ceil(entity.getMaxHealth())), "%coords%", this.coords(entity.getLocation()), "%state%", state.awake ? "awake" : "asleep"));
                    }
                    return true;
                }
                if (args.length >= 2 && (args[1].equalsIgnoreCase("heal") || args[1].equalsIgnoreCase("kill"))) {
                    boolean kill = args[1].equalsIgnoreCase("kill");
                    String action = kill ? "kill" : "heal";
                    Dimension only = null;
                    if (args.length > 2) {
                        only = Dimension.parse(args[2]);
                        if (only == null) {
                            this.send(sender, this.prefixed("usage-boss-" + action, "&cUsage: /esmp boss " + action + " [nether|end|overworld]", new Object[0]));
                            return true;
                        }
                    }
                    for (Dimension target : only == null ? Dimension.values() : new Dimension[]{only}) {
                        LivingEntity entity = this.liveBoss(this.stateFor(target));
                        if (entity == null) {
                            this.send(sender, this.message("boss-admin-none", "&7%dim%&8: &e%state%&8 - &7no loaded boss to %action%.", "%dim%", target.displayName, "%state%", this.bossPhaseText(target), "%action%", action));
                            continue;
                        }
                        if (kill) {
                            entity.setHealth(0.0);
                            this.send(sender, this.message("boss-admin-kill", "&cKilled %dim% boss (%boss%).", "%dim%", target.displayName, "%boss%", this.bossNamePlain(target)));
                        } else {
                            entity.setHealth(entity.getMaxHealth());
                            this.send(sender, this.message("boss-admin-heal", "&aHealed %dim% boss (%boss%) to &c%hp%&a HP.", "%dim%", target.displayName, "%boss%", this.bossNamePlain(target), "%hp%", String.valueOf((int)Math.ceil(entity.getMaxHealth()))));
                        }
                    }
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("reset")) {
                    Dimension only = null;
                    if (args.length > 2) {
                        only = Dimension.parse(args[2]);
                        if (only == null) {
                            this.send(sender, this.prefixed("usage-boss-reset", "&cUsage: /esmp boss reset [nether|end|overworld]", new Object[0]));
                            return true;
                        }
                    }
                    Dimension[] targets = only == null ? Dimension.values() : new Dimension[]{only};
                    for (Dimension target : targets) {
                        this.resetBoss(target);
                    }
                    this.healAllPlayers();
                    this.send(sender, this.prefixed("boss-reset", "&aReset %count% boss(es): duplicates removed, the caged boss is back at full health and every player was healed.", "%count%", targets.length));
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("spawn")) {
                    this.send(sender, this.prefixed("usage-boss", "&cUsage: /esmp boss list [dimension] | /esmp boss spawn <nether|end|overworld> | /esmp boss reset [dimension] | /esmp boss heal [dimension] | /esmp boss kill [dimension]", new Object[0]));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                Dimension dimension = Dimension.parse(args[2]);
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-boss", "&cUsage: /esmp boss spawn <nether|end|overworld>", new Object[0]));
                    return true;
                }
                Phase phase = this.phases.getOrDefault((Object)dimension, Phase.DORMANT);
                if (phase != Phase.DORMANT) {
                    this.send(sender, this.prefixed("boss-once", "&c%dim% can only summon its boss once. Its current state is %state% \u2014 revive it with the altar ritual.", "%dim%", dimension.displayName, "%state%", phase.name().toLowerCase(Locale.ROOT)));
                    return true;
                }
                if (this.spawnBoss(dimension, player.getLocation())) {
                    this.send(sender, this.prefixed("boss-spawned", "&aSpawned the %dim% boss.", "%dim%", dimension.displayName));
                }
                return true;
            }
            case "ritual": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("start")) {
                    this.send(sender, this.prefixed("usage-ritual", "&cUsage: /esmp ritual start <nether|end|overworld>", new Object[0]));
                    return true;
                }
                Dimension dimension = Dimension.parse(args[2]);
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-ritual", "&cUsage: /esmp ritual start <nether|end|overworld>", new Object[0]));
                    return true;
                }
                if (this.phases.get((Object)dimension) != Phase.DEFEATED) {
                    this.send(sender, this.prefixed("ritual-not-ready", "&cThe %dim% boss has not fallen, so no ritual is possible.", "%dim%", dimension.displayName));
                    return true;
                }
                this.ensureAltar(dimension);
                this.startRitual(dimension);
                return true;
            }
            case "redeem": {
                if (args.length < 2) {
                    this.send(sender, this.prefixed("redeem-usage", "&cUsage: /esmp redeem <code>", new Object[0]));
                    return true;
                }
                Dimension found = null;
                for (Dimension dimension4 : Dimension.values()) {
                    String code = this.ritualCodes.get(dimension4);
                    if (code != null && code.equalsIgnoreCase(args[1].trim())) {
                        found = dimension4;
                        break;
                    }
                }
                if (found == null || this.phases.get((Object)found) != Phase.DEFEATED && this.phases.get((Object)found) != Phase.RITUAL) {
                    this.send(sender, this.prefixed("redeem-invalid", "&cThat ritual code is not active.", new Object[0]));
                    return true;
                }
                this.sendRitualInstructions(sender, found);
                return true;
            }
            case "resetplayer": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    this.send(sender, this.prefixed("usage-resetplayer", "&cUsage: /esmp resetplayer <player>", new Object[0]));
                    return true;
                }
                Player target = Bukkit.getPlayerExact((String)args[1]);
                if (target == null) {
                    this.send(sender, this.prefixed("player-not-found", "&cThat player is not online.", new Object[0]));
                    return true;
                }
                this.assignments.remove(target.getUniqueId());
                this.locked.remove(target.getUniqueId());
                this.cancelTeleport(target.getUniqueId());
                this.bookGiven.removeIf(entry -> entry.startsWith(String.valueOf(target.getUniqueId()) + "|"));
                this.saveData();
                this.applyHearts(target, false);
                this.openMenu(target);
                this.send(sender, this.prefixed("assignment-reset", "&aReset the dimension assignment for %player%; they may choose again.", "%player%", target.getName()));
                return true;
            }
            case "give-table": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                ItemStack table = new ItemStack(Material.CRAFTING_TABLE);
                if (player.getInventory().addItem(new ItemStack[]{table}).isEmpty()) {
                    this.send((CommandSender)player, this.prefixed("give-table", "&aGiven a crafting table.", new Object[0]));
                } else {
                    this.send((CommandSender)player, this.prefixed("give-table-full", "&cInventory full; the crafting table was dropped at your feet.", new Object[0]));
                    player.getWorld().dropItemNaturally(player.getLocation(), table);
                }
                return true;
            }
            case "setportal": {
                Dimension dimension;
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                Dimension dimension5 = dimension = args.length > 1 ? Dimension.parse(args[1]) : null;
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-setportal", "&cUsage: /esmp setportal <nether|end|overworld>", new Object[0]));
                    return true;
                }
                Location anchor = player.getLocation().clone();
                this.portalAnchors.put(dimension, anchor);
                this.portalLit.remove((Object)dimension);
                BukkitTask bukkitTask = this.portalFramePulseTask.remove((Object)dimension);
                if (bukkitTask != null) {
                    bukkitTask.cancel();
                }
                this.saveData();
                this.send(sender, this.prefixed("portal-anchor-saved", "&aPortal anchor saved for %dim% at %coords%.", "%dim%", dimension.displayName, "%coords%", this.coords(anchor)));
                return true;
            }
            case "give-key": {
                Dimension dimension;
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                Dimension dimension6 = dimension = args.length > 1 ? Dimension.parse(args[1]) : null;
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-give-key", "&cUsage: /esmp give-key <nether|end|overworld>", new Object[0]));
                    return true;
                }
                ItemStack key = this.keyItem(dimension, 3);
                if (player.getInventory().addItem(new ItemStack[]{key}).isEmpty()) {
                    this.send((CommandSender)player, this.prefixed("give-key", "&aGiven 3 %dim% portal keys.", "%dim%", dimension.displayName));
                } else {
                    this.send((CommandSender)player, this.prefixed("give-key-full", "&cInventory full; the keys were dropped at your feet.", new Object[0]));
                    player.getWorld().dropItemNaturally(player.getLocation(), key);
                }
                return true;
            }
            case "key": {
                if (args.length >= 2 && args[1].equalsIgnoreCase("portal")) {
                    if (args.length >= 3) {
                        return this.giveKey(sender, args[2]);
                    }
                    if (!(sender instanceof Player)) {
                        this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                        return true;
                    }
                    Player player = (Player)sender;
                    Dimension own = this.assignments.get(player.getUniqueId());
                    if (own == null) {
                        own = this.dimensionOfWorld(player.getWorld());
                    }
                    if (own == null) {
                        this.send(sender, this.prefixed("key-no-dimension", "&cJoin a dimension first; keys only open portals to the other two realms.", new Object[0]));
                        return true;
                    }
                    ArrayList<String> names = new ArrayList<String>();
                    for (Dimension destination : this.portalDestinations(own)) {
                        names.add(destination.displayName);
                    }
                    this.send(sender, this.prefixed("key-portal-list", "&eFrom the %dim% you can open portals to: &f%dests%&e. Craft a key with 2 Ominous Bottles, 5 realm blocks, a netherite ingot and a nether star, then insert 3 keys into your kingdom portal.", "%dim%", own.kingdomName(), "%dests%", String.join((CharSequence)", ", names)));
                    return true;
                }
                this.send(sender, this.prefixed("usage-key", "&cUsage: /esmp key portal [nether|end|overworld]", new Object[0]));
                return true;
            }
            case "warlord": 
            case "warload": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (args.length < 3 || !args[1].equalsIgnoreCase("sword") && !args[1].equalsIgnoreCase("warblade")) {
                    this.send(sender, this.prefixed("usage-warlord", "&cUsage: /esmp warlord sword <player>", new Object[0]));
                    return true;
                }
                Player target = Bukkit.getPlayerExact((String)args[2]);
                if (target == null) {
                    this.send(sender, this.prefixed("player-not-found", "&cThat player is not online.", new Object[0]));
                    return true;
                }
                ItemStack blade = this.netheriteSwordItem();
                if (!target.getInventory().addItem(new ItemStack[]{blade}).isEmpty()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), blade);
                }
                this.send((CommandSender)target, this.prefixed("warlord-sword", "&cThe Netherite Warblade has been placed in your inventory.", new Object[0]));
                this.send(sender, this.prefixed("warlord-given", "&aGave the Netherite Warblade to %player%.", "%player%", target.getName()));
                return true;
            }
            case "kingdom": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                if (args.length < 3) {
                    this.send(sender, this.prefixed("usage-kingdom", "&cUsage: /esmp kingdom <build|tp> <nether|end|overworld>", new Object[0]));
                    return true;
                }
                Dimension dimension = Dimension.parse(args[2]);
                if (dimension == null) {
                    this.send(sender, this.prefixed("usage-kingdom", "&cUsage: /esmp kingdom <build|tp> <nether|end|overworld>", new Object[0]));
                    return true;
                }
                if (args[1].equalsIgnoreCase("build")) {
                    this.ensureKingdom(dimension);
                    this.send(sender, this.prefixed("kingdom-building", "&aBuilding the %dim%...", "%dim%", dimension.kingdomName()));
                    return true;
                }
                if (args[1].equalsIgnoreCase("tp")) {
                    if (!(sender instanceof Player)) {
                        this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                        return true;
                    }
                    Player player = (Player)sender;
                    Location center = this.kingdoms.get((Object)dimension);
                    if (center == null) {
                        this.send(sender, this.prefixed("kingdom-missing", "&cThat kingdom has not been built yet.", new Object[0]));
                        return true;
                    }
                    player.teleport(center);
                    this.send(sender, this.prefixed("kingdom-teleported", "&aWelcome to the %dim%.", "%dim%", dimension.kingdomName()));
                    return true;
                }
                this.send(sender, this.prefixed("usage-kingdom", "&cUsage: /esmp kingdom <build|tp> <nether|end|overworld>", new Object[0]));
                return true;
            }
            case "pack": {
                if (!(sender instanceof Player)) {
                    this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
                    return true;
                }
                Player player = (Player)sender;
                this.maybeSendResourcePack(player);
                this.send(sender, this.prefixed("pack-sent", "&aResource pack offered. Server: &f%url%&a (sha1 %sha1%).", "%url%", this.packUrl == null ? "not hosted" : this.packUrl, "%sha1%", this.packSha1 == null ? "none" : this.hex(this.packSha1)));
                return true;
            }
            case "reset": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                this.assignments.clear();
                this.locked.clear();
                this.bookGiven.clear();
                this.pendingTeleports.keySet().forEach(this::cancelTeleport);
                this.pendingTeleports.clear();
                for (BossState state : new ArrayList<BossState>(this.bosses.values())) {
                    this.removeBoss(state, false);
                }
                for (RitualState ritual : new ArrayList<RitualState>(this.rituals.values())) {
                    this.clearBar(ritual.bar);
                }
                this.rituals.clear();
                for (Dimension iterator : Dimension.values()) {
                    this.phases.put(iterator, Phase.DORMANT);
                    this.blessed.remove(iterator);
                    this.ritualCodes.remove(iterator);
                    this.spawns.remove(iterator);
                    this.altars.remove(iterator);
                    this.bossSpawns.remove(iterator);
                    this.portalAnchors.remove(iterator);
                    this.portalLit.remove(iterator);
                    this.kingdoms.remove(iterator);
                    this.cages.remove(iterator);
                    this.portalTargets.remove(iterator);
                    this.kingdomReady.remove(iterator);
                    this.kingdomCursor.remove(iterator);
                    BukkitTask t = this.portalFramePulseTask.remove(iterator);
                    if (t != null) {
                        t.cancel();
                    }
                    if ((t = this.kingdomTasks.remove(iterator)) == null) continue;
                    t.cancel();
                }
                Bukkit.removeRecipe((NamespacedKey)this.netheriteSwordRecipeKey());
                Bukkit.removeRecipe((NamespacedKey)this.recipeKey(Dimension.NETHER));
                Bukkit.removeRecipe((NamespacedKey)this.recipeKey(Dimension.END));
                Bukkit.removeRecipe((NamespacedKey)this.recipeKey(Dimension.OVERWORLD));
                this.registerRecipes();
                this.saveData();
                this.send(sender, this.prefixed("reset-complete", "&aEclipseSMP fully reset: assignments, locks, teleports, books, boss/ritual state, spawns, altars and recipes cleared.", new Object[0]));
                return true;
            }
            case "reload": {
                if (!this.requireAdmin(sender)) {
                    return true;
                }
                this.reloadConfig();
                this.applyHeartsToDimension(Dimension.NETHER, false);
                this.applyHeartsToDimension(Dimension.END, false);
                this.applyHeartsToDimension(Dimension.OVERWORLD, false);
                this.send(sender, this.prefixed("reloaded", "&aEclipseSMP configuration reloaded.", new Object[0]));
                return true;
            }
        }
        this.sendUsage(sender);
        return true;
    }

    private void sendRitualInstructions(CommandSender sender, Dimension dimension) {
        Location altar = this.altars.get((Object)dimension);
        this.send(sender, this.prefixed("redeem-coords", "&d%dim% ritual &7| &fAltar: &e%coords%&7 | &fCode: &e%code%", "%dim%", dimension.displayName, "%coords%", this.coords(altar), "%code%", this.ritualCodes.getOrDefault((Object)dimension, "??????")));
        this.send(sender, (Component)LEGACY.deserialize("&7Recipe: &fG S G &7/ &fS N S &7/ &fG S G"));
        this.send(sender, (Component)LEGACY.deserialize("&7G = &f" + this.pretty(dimension.ritualBlock()) + "&7, S = &fNetherite Ingot&7, N = &fNether Star"));
        this.send(sender, (Component)LEGACY.deserialize("&7Place a crafting table on the altar pedestal and craft the catalyst there."));
    }

    private boolean requireAdmin(CommandSender sender) {
        if (sender.hasPermission("eclipsesmp.admin")) {
            return true;
        }
        this.send(sender, this.prefixed("no-permission", "&cYou do not have permission to do that.", new Object[0]));
        return false;
    }

    private boolean giveKey(CommandSender sender, String raw) {
        if (!this.requireAdmin(sender)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            this.send(sender, this.prefixed("players-only", "&cThat command must be run by a player.", new Object[0]));
            return true;
        }
        Player player = (Player)sender;
        Dimension dimension = Dimension.parse(raw);
        if (dimension == null) {
            this.send(sender, this.prefixed("usage-give-key", "&cUsage: /esmp key portal <nether|end|overworld>", new Object[0]));
            return true;
        }
        ItemStack key = this.keyItem(dimension, 3);
        if (player.getInventory().addItem(new ItemStack[]{key}).isEmpty()) {
            this.send((CommandSender)player, this.prefixed("give-key", "&aGiven 3 %dim% portal keys.", "%dim%", dimension.displayName));
        } else {
            this.send((CommandSender)player, this.prefixed("give-key-full", "&cInventory full; the keys were dropped at your feet.", new Object[0]));
            player.getWorld().dropItemNaturally(player.getLocation(), key);
        }
        return true;
    }

    private String hex(byte[] bytes) {
        if (bytes == null) {
            return "none";
        }
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private void startHousekeeping() {
        if (this.housekeepingTask != null) {
            return;
        }
        this.housekeepingTask = Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                this.applyHearts(p, false);
            }
            for (BossState st : new ArrayList<BossState>(this.bosses.values())) {
                Entity entity = Bukkit.getEntity((UUID)st.entityId);
                if (entity == null) {
                    // Unloaded with its chunk, not gone; tracking resumes on chunk load.
                    continue;
                }
                if (entity.isValid() && !entity.isDead()) continue;
                if (st.tracker != null) {
                    st.tracker.cancel();
                    st.tracker = null;
                }
                this.removeDisplay(st);
                this.clearBar(st.bar);
                this.bosses.remove(st.entityId);
            }
        }, 20L, 20L);
    }

    private void stopHousekeeping() {
        if (this.housekeepingTask != null) {
            this.housekeepingTask.cancel();
            this.housekeepingTask = null;
        }
    }

    private void sendUsage(CommandSender sender) {
        List<String> lines = this.getConfig().getStringList("messages.usage");
        if (lines.isEmpty()) {
            lines = List.of("&d/esmp menu [player] &7- open the dimension menu", "&d/esmp info &7- show dimension populations", "&d/esmp setspawn <dimension> &7- save a territory spawn", "&d/esmp setaltar <dimension> &7- save a ritual altar location", "&d/esmp boss spawn <dimension> &7- summon a boss (once per dimension)", "&d/esmp spawn boss <dimension> &7- spawn a boss at your position", "&d/esmp boss reset [dimension] &7- remove duplicates and restore the caged boss", "&d/esmp boss list [dimension] &7- show boss state, health and location", "&d/esmp boss heal [dimension] &7- heal a realm boss to full", "&d/esmp boss kill [dimension] &7- kill a realm boss (testing)", "&d/esmp ritual start <dimension> &7- start a revival ritual", "&d/esmp redeem <code> &7- look up an active ritual code", "&d/esmp resetplayer <player> &7- clear an assignment and choice lock", "&d/esmp give-table &7- give yourself a crafting table", "&d/esmp setportal <dimension> &7- set a portal anchor", "&d/esmp key portal [dimension] &7- list or give portal keys", "&d/esmp give-key <dimension> &7- give 3 portal keys", "&d/esmp warlord sword <player> &7- give the Netherite Warblade", "&d/esmp kingdom <build|tp> <dimension> &7- build or visit a kingdom", "&d/esmp pack &7- resend the resource pack", "&d/esmp reset &7- full reset (admin only)", "&d/esmp reload &7- reload config.yml");
        }
        for (String line : lines) {
            this.send(sender, (Component)LEGACY.deserialize(line));
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return this.filter(List.of("menu", "info", "setspawn", "setaltar", "setportal", "boss", "spawn", "ritual", "redeem", "key", "give-key", "warlord", "kingdom", "pack", "resetplayer", "give-table", "reset", "reload"), args[0]);
        }
        if (args[0].equalsIgnoreCase("key")) {
            if (args.length == 2) {
                return this.filter(List.of("portal"), args[1]);
            }
            if (args.length == 3) {
                return this.filter(List.of("nether", "end", "overworld"), args[2]);
            }
            return List.of();
        }
        if (args[0].equalsIgnoreCase("warlord") || args[0].equalsIgnoreCase("warload")) {
            if (args.length == 2) {
                return this.filter(List.of("sword"), args[1]);
            }
            if (args.length == 3) {
                ArrayList<String> names = new ArrayList<String>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    names.add(online.getName());
                }
                return this.filter(names, args[2]);
            }
            return List.of();
        }
        if (args[0].equalsIgnoreCase("kingdom")) {
            if (args.length == 2) {
                return this.filter(List.of("build", "tp"), args[1]);
            }
            if (args.length == 3) {
                return this.filter(List.of("nether", "end", "overworld"), args[2]);
            }
            return List.of();
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("menu") || sub.equals("resetplayer")) {
            if (args.length == 2) {
                ArrayList<String> names = new ArrayList<String>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    names.add(online.getName());
                }
                return this.filter(names, args[1]);
            }
            return List.of();
        }
        if (sub.equals("setspawn") || sub.equals("setaltar") || sub.equals("setportal") || sub.equals("give-key")) {
            if (args.length == 2) {
                return this.filter(List.of("nether", "end", "overworld"), args[1]);
            }
            return List.of();
        }
        if (sub.equals("boss")) {
            if (args.length == 2) {
                return this.filter(List.of("spawn", "reset"), args[1]);
            }
            if (args.length == 3 && (args[1].equalsIgnoreCase("spawn") || args[1].equalsIgnoreCase("reset"))) {
                return this.filter(List.of("nether", "end", "overworld"), args[2]);
            }
        }
        if (sub.equals("spawn")) {
            if (args.length == 2) {
                return this.filter(List.of("boss"), args[1]);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("boss")) {
                return this.filter(List.of("nether", "end", "overworld"), args[2]);
            }
        }
        if (sub.equals("ritual")) {
            if (args.length == 2) {
                return this.filter(List.of("start"), args[1]);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("start")) {
                return this.filter(List.of("nether", "end", "overworld"), args[2]);
            }
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        ArrayList<String> matches = new ArrayList<String>();
        for (String option : options) {
            if (!option.toLowerCase(Locale.ROOT).startsWith(lower)) continue;
            matches.add(option);
        }
        return matches;
    }

    public static enum Dimension {
        NETHER("Nether", NamedTextColor.RED),
        END("The End", NamedTextColor.DARK_PURPLE),
        OVERWORLD("Overworld", NamedTextColor.AQUA);

        private final String displayName;
        private final NamedTextColor color;

        private Dimension(String displayName, NamedTextColor color) {
            this.displayName = displayName;
            this.color = color;
        }

        String key() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        static Dimension parse(String raw) {
            if (raw == null) {
                return null;
            }
            try {
                return Dimension.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException ex) {
                return null;
            }
        }

        String bossName() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "The Piglin Warlord";
                case 1 -> "The Void Reaper";
                case 2 -> "The Deep Guardian";
            };
        }

        EntityType bossType() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> EntityType.PIGLIN_BRUTE;
                case 1 -> EntityType.ENDERMAN;
                case 2 -> EntityType.WARDEN;
            };
        }

        BossBar.Color barColor() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> BossBar.Color.RED;
                case 1 -> BossBar.Color.PURPLE;
                case 2 -> BossBar.Color.BLUE;
            };
        }

        String theme() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Fire \u2022 Gold \u2022 Conquest";
                case 1 -> "Void \u2022 Ender \u2022 Ruin";
                case 2 -> "Sculk \u2022 Echo \u2022 Ancient power";
            };
        }

        Material ritualBlock() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.GOLD_BLOCK;
                case 1 -> Material.PURPUR_BLOCK;
                case 2 -> Material.EMERALD_BLOCK;
            };
        }

        Material altarFloor() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.NETHER_BRICKS;
                case 1 -> Material.END_STONE_BRICKS;
                case 2 -> Material.DEEPSLATE_BRICKS;
            };
        }

        Material altarRim() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.POLISHED_BLACKSTONE_BRICKS;
                case 1 -> Material.PURPUR_BLOCK;
                case 2 -> Material.POLISHED_DEEPSLATE;
            };
        }

        Material altarPillar() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.POLISHED_BLACKSTONE;
                case 1 -> Material.PURPUR_PILLAR;
                case 2 -> Material.DEEPSLATE_TILES;
            };
        }

        Material altarLamp() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.SHROOMLIGHT;
                case 1 -> Material.END_ROD;
                case 2 -> Material.SEA_LANTERN;
            };
        }

        Particle ritualParticle() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Particle.FLAME;
                case 1 -> Particle.PORTAL;
                case 2 -> Particle.SCULK_SOUL;
            };
        }

        Material healItem() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.GOLD_INGOT;
                case 1 -> Material.ENDER_PEARL;
                case 2 -> Material.ECHO_SHARD;
            };
        }

        Material portalLitBlock() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.GLOWSTONE;
                case 1 -> Material.SHULKER_BOX;
                case 2 -> Material.BEACON;
            };
        }

        String kingdomName() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Nether Kingdom";
                case 1 -> "End Kingdom";
                case 2 -> "Overworld Kingdom";
            };
        }

        Material kingdomWall() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.POLISHED_BLACKSTONE_BRICKS;
                case 1 -> Material.END_STONE_BRICKS;
                case 2 -> Material.STONE_BRICKS;
            };
        }

        Material kingdomTrim() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.NETHER_BRICK_FENCE;
                case 1 -> Material.END_ROD;
                case 2 -> Material.COBBLESTONE_WALL;
            };
        }

        Material kingdomFloor() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.NETHER_BRICKS;
                case 1 -> Material.PURPUR_BLOCK;
                case 2 -> Material.POLISHED_ANDESITE;
            };
        }

        Material kingdomAccent() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.GILDED_BLACKSTONE;
                case 1 -> Material.PURPUR_PILLAR;
                case 2 -> Material.DEEPSLATE_TILES;
            };
        }

        Material kingdomLight() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.SHROOMLIGHT;
                case 1 -> Material.SEA_LANTERN;
                case 2 -> Material.LANTERN;
            };
        }

        Material kingdomRoad() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.BLACKSTONE;
                case 1 -> Material.END_STONE;
                case 2 -> Material.GRAVEL;
            };
        }

        Material cageBar() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.GOLD_BLOCK;
                case 1 -> Material.IRON_BARS;
                case 2 -> Material.DEEPSLATE_BRICK_WALL;
            };
        }

        Material portalFrameBlock() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> Material.CRYING_OBSIDIAN;
                case 1 -> Material.OBSIDIAN;
                case 2 -> Material.OBSIDIAN;
            };
        }

        Material portalFill() {
            return Material.NETHER_PORTAL;
        }
    }

    private final class RitualState {
        private final Dimension dimension;
        private final int duration;
        private final BossBar bar;
        private int elapsed;

        private RitualState(EclipseSMP dimensionWars, Dimension dimension, int duration, BossBar bar) {
            this.dimension = dimension;
            this.duration = duration;
            this.bar = bar;
        }
    }

    private final class BossState {
        private final UUID entityId;
        private final Dimension dimension;
        private final BossBar bar;
        private BukkitTask tracker;
        private UUID displayId;
        private int ticks;
        private boolean awake;
        private BossTier tier = BossTier.EASY;
        private int abilityCursor;
        private int specialHits;
        private int calmTicks;

        private BossState(EclipseSMP dimensionWars, UUID entityId, Dimension dimension, BossBar bar) {
            this.entityId = entityId;
            this.dimension = dimension;
            this.bar = bar;
        }
    }

    private static enum Phase {
        DORMANT,
        ALIVE,
        DEFEATED,
        RITUAL,
        BLESSED;

    }

    private static final class PendingTeleport {
        private final Dimension dimension;
        private final Location origin;
        private final Location destination;
        private int secondsLeft;
        private BukkitTask task;

        private PendingTeleport(Dimension dimension, Location origin, Location destination, int secondsLeft) {
            this.dimension = dimension;
            this.origin = origin;
            this.destination = destination;
            this.secondsLeft = secondsLeft;
        }
    }

    private static enum BossTier {
        EASY("Easy", 1.0),
        MEDIUM("Medium", 1.25),
        HARD("Hard", 1.5);

        private final String displayName;
        private final double damageScale;

        private BossTier(String displayName, double damageScale) {
            this.displayName = displayName;
            this.damageScale = damageScale;
        }

        String displayName() {
            return this.displayName;
        }

        double damageScale() {
            return this.damageScale;
        }

        int modelOffset() {
            return this.ordinal() * 10;
        }

        BossTier next() {
            return this == EASY ? MEDIUM : (this == MEDIUM ? HARD : HARD);
        }
    }

    private record Ability(String name, Move move, double damage, int ticks) {
    }

    private static enum Move {
        BURST,
        FIRE,
        LEVITATE,
        SLOW,
        BLIND,
        PULL,
        KNOCKBACK,
        WITHER,
        BLINK,
        VOLLEY,
        ULTIMATE;

    }
}

