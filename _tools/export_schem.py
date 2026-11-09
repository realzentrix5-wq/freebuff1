"""Convert the uploaded 'End Castle' world save into a Sponge v3 .schem.

The world is a flat overworld save; the castle is built around the origin.
We crop to the non-air bounding box, skip air, and write gzipped NBT v3 so the
plugin can paste it into the End kingdom.
"""
import gzip, struct, sys, glob, os

sys.path.insert(0, '/tmp')
from anvil import chunks, decode_section

SRC = sys.argv[1] if len(sys.argv) > 1 else '_incoming/endcastle/End Castle/region'
OUT = sys.argv[2] if len(sys.argv) > 2 else 'src/main/resources/schematics/end.schem'

AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}

# ---- collect blocks -------------------------------------------------------
blocks = {}          # (x,y,z) -> state string
minx = miny = minz = 10 ** 9
maxx = maxy = maxz = -10 ** 9
for f in sorted(glob.glob(os.path.join(SRC, '*.mca'))):
    for cx, cz, ch in chunks(f):
        x0 = ch['xPos'] * 16
        z0 = ch['zPos'] * 16
        for sec in ch.get('sections', []):
            dec = decode_section(sec)
            if not dec:
                continue
            pal, data = dec
            sy = sec.get('Y', 0) * 16
            for idx, pid in enumerate(data):
                state = pal[pid]
                if state in AIR:
                    continue
                x = x0 + (idx & 15)
                z = z0 + ((idx >> 4) & 15)
                y = sy + (idx >> 8)
                blocks[(x, y, z)] = state
                if x < minx: minx = x
                if x > maxx: maxx = x
                if y < miny: miny = y
                if y > maxy: maxy = y
                if z < minz: minz = z
                if z > maxz: maxz = z

if not blocks:
    raise SystemExit('no blocks found')

W = maxx - minx + 1
H = maxy - miny + 1
L = maxz - minz + 1
print('blocks', len(blocks), 'size', W, H, L, 'min', (minx, miny, minz), 'max', (maxx, maxy, maxz))

palette = {'minecraft:air': 0}
order = ['minecraft:air']
for state in sorted(set(blocks.values())):
    palette[state] = len(order)
    order.append(state)
print('palette', len(order))

# ---- encode BlockData: index = (y * L + z) * W + x, varints ---------------
out = bytearray()
for y in range(H):
    for z in range(L):
        for x in range(W):
            v = palette[blocks.get((minx + x, miny + y, minz + z), 'minecraft:air')]
            while True:
                b = v & 0x7F
                v >>= 7
                if v:
                    out.append(b | 0x80)
                else:
                    out.append(b)
                    break
print('blockdata bytes', len(out))

# ---- minimal NBT writer ---------------------------------------------------
def tag(t, name, payload):
    n = name.encode()
    return bytes([t]) + struct.pack('>H', len(n)) + n + payload

def int_tag(n, v): return tag(3, n, struct.pack('>i', v))
def short_tag(n, v): return tag(2, n, struct.pack('>h', v))
def str_tag(n, v):
    b = v.encode()
    return tag(8, n, struct.pack('>H', len(b)) + b)
def byte_array_tag(n, data): return tag(7, n, struct.pack('>i', len(data)) + bytes(data))
def int_array_tag(n, values): return tag(11, n, struct.pack('>i', len(values)) + b''.join(struct.pack('>i', v) for v in values))
def compound_tag(n, entries): return tag(10, n, b''.join(entries) + b'\x00')
def list_tag(n, elem_type, payloads): return tag(9, n, bytes([elem_type]) + struct.pack('>i', len(payloads)) + b''.join(payloads))

pal_entries = [int_tag(state, idx) for idx, state in enumerate(order)]
blocks_container = compound_tag('Blocks', [
    compound_tag('Palette', pal_entries),
    byte_array_tag('Data', out),
    list_tag('BlockEntities', 10, []),
])
root = compound_tag('Schematic', [
    int_tag('Version', 3),
    int_tag('DataVersion', 3578),
    short_tag('Width', W),
    short_tag('Height', H),
    short_tag('Length', L),
    int_array_tag('Offset', [0, 0, 0]),
    int_tag('PaletteMax', len(order)),
    compound_tag('Palette', pal_entries),
    blocks_container,
    compound_tag('Metadata', [str_tag('Name', 'Eclipse SMP End Castle'), str_tag('Author', 'Eclipse SMP')]),
    list_tag('Entities', 10, []),
])

raw = tag(10, 'Schematic', root[len(b'\x0a\x00\x04Schematic'):]) if False else root
# root compound already carries the name; prepend the root tag type only
blob = b'\x0a' + root[1:]
os.makedirs(os.path.dirname(OUT), exist_ok=True)
with gzip.open(OUT, 'wb', compresslevel=9) as fh:
    fh.write(blob)
print('wrote', OUT, os.path.getsize(OUT), 'bytes')
