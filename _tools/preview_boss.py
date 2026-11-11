"""Software preview of the boss item models as an ItemDisplay would show them.

This is not the Minecraft client; it is a quick isometric rasteriser so the model
silhouette, facing, texture coverage and item-display placement (feet on the ground,
boss-sized height) can be eyeballed without launching the game. It applies the item
transform exactly as the client does (translate -8px, rotate, scale, translate in
blocks) and the item display renderer's own 180 degree flip.

Usage: /tmp/pv/bin/python _tools/preview_boss.py [outdir]
"""
import json
import math
import os
import sys

from PIL import Image, ImageDraw

PACK = 'pack'
MODELS = os.path.join(PACK, 'assets/eclipsesmp/models/item')
TEXTURES = os.path.join(PACK, 'assets/eclipsesmp/textures/item')
OUT = sys.argv[1] if len(sys.argv) > 1 else '/tmp/boss_previews'
os.makedirs(OUT, exist_ok=True)

SCALE = 34          # pixels per block
WIDTH = HEIGHT = 420
CAM_YAW = math.radians(35)
CAM_PITCH = math.radians(18)
LIGHT = (-0.45, 0.8, 0.4)
LABELS = ['boss_nether', 'boss_nether_medium', 'boss_nether_hard',
          'boss_end', 'boss_end_medium', 'boss_end_hard',
          'boss_overworld', 'boss_overworld_medium', 'boss_overworld_hard']


def load_model(name):
    with open(os.path.join(MODELS, name + '.json'), encoding='utf-8') as handle:
        return json.load(handle)


def texture_for(model):
    path = model['textures']['0'].split(':', 1)[1]
    image = Image.open(os.path.join(PACK, 'assets/eclipsesmp/textures', path + '.png'))
    return image.convert('RGBA')


def face_color(texture, uv):
    x0, y0, x1, y1 = [int(round(v)) for v in uv]
    x0, x1 = sorted((max(0, x0), min(texture.width, max(x0 + 1, x1))))
    y0, y1 = sorted((max(0, y0), min(texture.height, max(y0 + 1, y1))))
    total = [0, 0, 0, 0]
    for y in range(y0, y1):
        for x in range(x0, x1):
            r, g, b, a = texture.getpixel((x, y))
            total[0] += r
            total[1] += g
            total[2] += b
            total[3] += a
    count = max(1, (x1 - x0) * (y1 - y0))
    alpha = total[3] / count
    if alpha < 8:
        return None
    return (total[0] / count, total[1] / count, total[2] / count)


def transform_matrix(model):
    fixed = model.get('display', {}).get('fixed', {})
    rotation = [math.radians(v) for v in fixed.get('rotation', [0, 0, 0])]
    translation = fixed.get('translation', [0, 0, 0])
    scale = fixed.get('scale', [1, 1, 1])
    return rotation, translation, scale


def apply_transform(point, rotation, translation, scale):
    # ItemTransform#apply: translate(-0.5 blocks) first, then rotate, scale and
    # translate in blocks. The item display renderer also spins the model 180 degrees.
    x, y, z = point[0] - 8.0, point[1] - 8.0, point[2] - 8.0
    rx, ry, rz = rotation

    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)

    x, y, z = x * scale[0], y * scale[1], z * scale[2]
    x += translation[0]
    y += translation[1]
    z += translation[2]

    # Renderer-level 180 degree spin around the entity origin.
    x, z = -x, -z
    return (x / 16.0, y / 16.0, z / 16.0)


def project(point):
    x, y, z = point
    xr = x * math.cos(CAM_YAW) - z * math.sin(CAM_YAW)
    zr = x * math.sin(CAM_YAW) + z * math.cos(CAM_YAW)
    sy = y * math.cos(CAM_PITCH) - zr * math.sin(CAM_PITCH)
    depth = y * math.sin(CAM_PITCH) + zr * math.cos(CAM_PITCH)
    return xr, sy, depth


def render(name):
    model = load_model(name)
    texture = texture_for(model)
    rotation, translation, scale = transform_matrix(model)

    canvas = Image.new('RGBA', (WIDTH, HEIGHT), (24, 26, 32, 255))
    draw = ImageDraw.Draw(canvas)
    origin_x, origin_y = WIDTH / 2, HEIGHT * 0.72

    def screen(point):
        xr, sy, depth = project(point)
        return (origin_x + xr * SCALE, origin_y - sy * SCALE), depth

    # Ground grid at y = 0 (one block cells) so feet placement is visible.
    for i in range(-4, 5):
        for a, b in (((i, 0, -4), (i, 0, 4)), ((-4, 0, i), (4, 0, i))):
            (x0, y0), _ = screen(a)
            (x1, y1), _ = screen(b)
            draw.line([(x0, y0), (x1, y1)], fill=(46, 50, 60, 255), width=1)

    faces = []
    for element in model['elements']:
        x0, y0, z0 = element['from']
        x1, y1, z1 = element['to']
        corners = {
            'west': [(x0, y0, z0), (x0, y0, z1), (x0, y1, z1), (x0, y1, z0)],
            'east': [(x1, y0, z1), (x1, y0, z0), (x1, y1, z0), (x1, y1, z1)],
            'down': [(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)],
            'up': [(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
            'north': [(x1, y0, z0), (x0, y0, z0), (x0, y1, z0), (x1, y1, z0)],
            'south': [(x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)],
        }
        for side, quad in corners.items():
            face = element['faces'].get(side)
            if not face:
                continue
            color = face_color(texture, face.get('uv', [0, 0, 1, 1]))
            if color is None:
                continue
            points = [apply_transform(point, rotation, translation, scale)
                      for point in quad]
            projected = [screen(p) for p in points]
            depth = sum(d for _, d in projected) / 4.0
            normal = {'west': (-1, 0, 0), 'east': (1, 0, 0), 'down': (0, -1, 0),
                      'up': (0, 1, 0), 'north': (0, 0, -1), 'south': (0, 0, 1)}[side]
            light = max(0.0, sum(n * l for n, l in zip(normal, LIGHT)))
            shade = 0.55 + 0.45 * light
            faces.append((depth, [p for p, _ in projected],
                          tuple(min(255, int(c * shade)) for c in color)))

    for _, polygon, color in sorted(faces, key=lambda item: -item[0]):
        draw.polygon(polygon, fill=color + (255,), outline=(0, 0, 0, 90))

    # 2-block reference bar in the corner.
    draw.text((10, 10), name, fill=(220, 220, 220, 255))
    draw.text((10, 26), 'feet y=0 line, 1-block grid', fill=(140, 145, 160, 255))
    draw.line([(10, HEIGHT - 20), (10 + SCALE * 2, HEIGHT - 20)], fill=(200, 80, 80, 255), width=3)
    draw.text((12 + SCALE * 2, HEIGHT - 28), '2 blocks', fill=(200, 120, 120, 255))

    path = os.path.join(OUT, name + '.png')
    canvas.convert('RGB').save(path)
    return path


if __name__ == '__main__':
    for index, name in enumerate(LABELS):
        print(render(name))
