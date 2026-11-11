"""Generate crisp 16x16 pixel-art item textures for the Eclipse SMP pack."""
import os, struct, zlib

OUT = 'pack/assets/eclipsesmp/textures/item'
os.makedirs(OUT, exist_ok=True)


def write_png(path, width, height, pixels):
    """pixels: list of rows, each row a list of (r,g,b,a)."""
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        return (struct.pack('>I', len(data)) + tag + data
                + struct.pack('>I', zlib.crc32(tag + data) & 0xFFFFFFFF))

    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
    png += chunk(b'IEND', b'')
    open(path, 'wb').write(png)


def hexc(value):
    value = value.lstrip('#')
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), 255)


def canvas(palette):
    return [[' ' for _ in range(16)] for _ in range(16)]


def draw(path, rows, palette):
    grid = canvas(palette)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if x < 16 and y < 16:
                grid[y][x] = ch
    pixels = [[palette.get(ch, (0, 0, 0, 0)) for ch in row] for row in grid]
    write_png(path, 16, 16, pixels)


# --------------------------------------------------------------------- keys
KEY = [
    "                ",
    "    ooooo       ",
    "   oaaaaao      ",
    "  oaaggg ao     ",
    "  oaaggg ao     ",
    "   oaaaaao      ",
    "    oaaao       ",
    "     oaao       ",
    "     oaao       ",
    "     oaao       ",
    "     oaao       ",
    "     oaao       ",
    "     oaaoo      ",
    "     oaaoao     ",
    "     oaaooao    ",
    "      oo  oo    ",
]

PALETTES = {
    'nether': {
        'o': hexc('#3A1B00'), 'a': hexc('#E8B03A'), 'g': hexc('#FF3B30'),
        'h': hexc('#FFE08A'),
    },
    'end': {
        'o': hexc('#2B1240'), 'a': hexc('#B87BE8'), 'g': hexc('#FF4FD8'),
        'h': hexc('#E3C8FF'),
    },
    'overworld': {
        'o': hexc('#0F3524'), 'a': hexc('#43C46F'), 'g': hexc('#35D6FF'),
        'h': hexc('#A8F5BE'),
    },
}

for name, palette in PALETTES.items():
    draw(os.path.join(OUT, 'key_%s.png' % name), KEY, palette)

# highlight a few pixels on the left edge of the shaft / ring
def add_highlights(path, coords, colour):
    data = open(path, 'rb').read()
    # decode our own simple PNG (filter 0, RGBA8)
    pos = 8
    idat = b''
    w = h = 0
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos + 4])[0]
        tag = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        if tag == b'IHDR':
            w, h = struct.unpack('>II', chunk[:8])
        elif tag == b'IDAT':
            idat += chunk
        pos += 12 + length
    raw = zlib.decompress(idat)
    rows = []
    stride = w * 4
    p = 0
    for _ in range(h):
        p += 1
        rows.append(bytearray(raw[p:p + stride]))
        p += stride
    for (x, y) in coords:
        idx = x * 4
        rows[y][idx:idx + 4] = bytes(colour)
    out = bytearray()
    for row in rows:
        out.append(0)
        out += row
    def chunk(tag, payload):
        return (struct.pack('>I', len(payload)) + tag + payload
                + struct.pack('>I', zlib.crc32(tag + payload) & 0xFFFFFFFF))
    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(bytes(out), 9))
    png += chunk(b'IEND', b'')
    open(path, 'wb').write(png)

for name, palette in PALETTES.items():
    add_highlights(os.path.join(OUT, 'key_%s.png' % name),
                   [(4, 3), (5, 3), (6, 4), (5, 5), (4, 6), (4, 7)],
                   palette['h'])

# -------------------------------------------------------------------- sword
SWORD = [
    "              oo",
    "             obo",
    "            obbo",
    "           obbo ",
    "          obbo  ",
    "         obbo   ",
    "        obbo    ",
    "       obbo     ",
    "      obbo      ",
    "  oo obbo       ",
    " oggobbo        ",
    " ogggbo         ",
    "  oggo          ",
    "  ohho          ",
    " ohoo           ",
    " oo             ",
]
SWORD_PAL = {
    'o': hexc('#160B14'),
    'b': hexc('#6E7480'),
    'c': hexc('#C8D2E0'),
    'g': hexc('#E8B03A'),
    'h': hexc('#5A3A22'),
    'r': hexc('#D22B2B'),
}
draw(os.path.join(OUT, 'sword_netherite.png'), SWORD, SWORD_PAL)
add_highlights(os.path.join(OUT, 'sword_netherite.png'),
               [(12, 2), (11, 3), (10, 4), (9, 5), (8, 6), (7, 7), (6, 8)],
               SWORD_PAL['c'])

# ----------------------------------------------------------------- catalysts
STAR = [
    "                ",
    "       oo       ",
    "      oggo      ",
    "      oggo      ",
    "     oaggao     ",
    "     oaggao     ",
    " ooooaaggaoooo  ",
    "oggggaaggaagggo ",
    "oggggaaggaagggo ",
    " ooooaaggaoooo  ",
    "     oaggao     ",
    "     oaggao     ",
    "      oggo      ",
    "      oggo      ",
    "       oo       ",
    "                ",
]
for name, palette in PALETTES.items():
    pal = {'o': palette['o'], 'g': palette['g'], 'a': palette['a']}
    draw(os.path.join(OUT, 'catalyst_%s.png' % name), STAR, pal)

print('wrote textures to', OUT)
for f in sorted(os.listdir(OUT)):
    print(' ', f, os.path.getsize(os.path.join(OUT, f)))
