"""Migrate the Eclipse SMP pack from pre-1.21.4 item overrides to item model definitions.

1.21.4 removed the `overrides` field from models; custom_model_data routing now lives in
assets/<namespace>/items/<item>.json as a minecraft:range_dispatch definition.
"""
import json
import os

PACK = 'pack'
ITEMS = os.path.join(PACK, 'assets/minecraft/items')
os.makedirs(ITEMS, exist_ok=True)


def write(path, data):
    with open(path, 'w', encoding='utf-8') as handle:
        json.dump(data, handle, indent=2)
        handle.write('\n')


def model(name):
    return {'type': 'minecraft:model', 'model': name}


def ranged(fallback, entries):
    entries = sorted(entries, key=lambda entry: entry[0])
    return {
        'model': {
            'type': 'minecraft:range_dispatch',
            'property': 'minecraft:custom_model_data',
            'fallback': model(fallback),
            'entries': [{'threshold': threshold, 'model': model(target)}
                        for threshold, target in entries],
        }
    }


ICONS = [
    (1001, 'eclipsesmp:item/nether_icon'),
    (1002, 'eclipsesmp:item/end_icon'),
    (1003, 'eclipsesmp:item/overworld_icon'),
]
BOSSES = [
    (2001, 'eclipsesmp:item/boss_nether'),
    (2011, 'eclipsesmp:item/boss_nether_medium'),
    (2021, 'eclipsesmp:item/boss_nether_hard'),
    (2002, 'eclipsesmp:item/boss_end'),
    (2012, 'eclipsesmp:item/boss_end_medium'),
    (2022, 'eclipsesmp:item/boss_end_hard'),
    (2003, 'eclipsesmp:item/boss_overworld'),
    (2013, 'eclipsesmp:item/boss_overworld_medium'),
    (2023, 'eclipsesmp:item/boss_overworld_hard'),
]
KEYS = [
    (3001, 'eclipsesmp:item/key_nether'),
    (3002, 'eclipsesmp:item/key_end'),
    (3003, 'eclipsesmp:item/key_overworld'),
]
CATALYSTS = [
    (3001, 'eclipsesmp:item/catalyst_nether'),
    (3002, 'eclipsesmp:item/catalyst_end'),
    (3003, 'eclipsesmp:item/catalyst_overworld'),
]

write(os.path.join(ITEMS, 'paper.json'), ranged('minecraft:item/paper', ICONS + BOSSES))
write(os.path.join(ITEMS, 'netherite_ingot.json'),
      ranged('minecraft:item/netherite_ingot', KEYS))
write(os.path.join(ITEMS, 'nether_star.json'),
      ranged('minecraft:item/nether_star', CATALYSTS))
write(os.path.join(ITEMS, 'netherite_sword.json'),
      ranged('minecraft:item/netherite_sword', [(4001, 'eclipsesmp:item/sword_netherite')]))

# Strip the removed overrides field from the vanilla item models, which are now only
# used as the fallback model for their base item.
for name in ('paper', 'netherite_ingot', 'nether_star', 'netherite_sword'):
    path = os.path.join(PACK, 'assets/minecraft/models/item/%s.json' % name)
    with open(path, encoding='utf-8') as handle:
        data = json.load(handle)
    data.pop('overrides', None)
    write(path, data)

# Minecraft's model loader rejects any element coordinate outside -16..32, so the
# 64px-tall boss figures have to be normalised into that box before the client will
# even parse them. Scaling the geometry by k and the display scale by 1/k keeps the
# on-screen size: the figure stays 2.8 blocks tall and its feet stay on the ground.
#
# The client applies an item transform as translate(-0.5 blocks), rotate, scale,
# translate (ItemTransform#apply), and the item display renderer spins the model 180
# degrees around Y before drawing it, so `fixed` uses rotation 180 to cancel the flip.
# Item rendering also multiplies every display transform by a base 0.5 scale, which is
# why a scale of 1.4 renders a 32px model 1.4 blocks tall instead of 2.8. Sizes below
# are therefore twice what the geometry alone would suggest.
#
# Model point p (element coords, 16 units per block) maps to
#   y = ((p - 8) * scale + translation) / 16 * 0.5
# so standing the feet (which the normalisation leaves at y=0) on the ground needs
# translation = 8 * scale, and a height of 2 blocks * scale * 0.5 = 2.8 blocks needs
# a scale of 2.8.
MAX_COORD = 32.0
BOSS_HEIGHT_BLOCKS = 2.8
# Measured against a live 1.21.11 client with a 32px model: the fixed/ground
# contexts render at half size, the GUI slot camera at about five sixteenths.
BASE_SCALE = 0.5
BASE_GUI = 0.3125
BASE_HAND = 0.5


def boss_display(height_px, min_y_px):
    centre_px = min_y_px + height_px / 2.0

    def transform(wanted_blocks, target_centre, base=BASE_SCALE, spin=False):
        scale = wanted_blocks / (height_px * base / 16.0)
        # Solve ((centre_px - 8) * scale + translation) / 16 * base == target_centre.
        translation = (target_centre * 16.0 - (centre_px - 8.0) * scale * base) / base
        return {
            'rotation': [0, 180 if spin else 0, 0],
            'translation': [0, round(translation, 3), 0],
            'scale': [round(scale, 3)] * 3,
        }

    return {
        'gui_light': 'front',
        'display': {
            # Scale and translation stay in the same 8:1 ratio that centres the icon in
            # the slot; the GUI camera magnifies by about 1/3, so 1.4 fills the slot.
            'gui': {'rotation': [0, 180, 0], 'translation': [0, 11.2, 0],
                    'scale': [1.4, 1.4, 1.4]},
            'ground': transform(0.5, 0.25),                 # dropped, feet on the ground
            'fixed': transform(BOSS_HEIGHT_BLOCKS, BOSS_HEIGHT_BLOCKS / 2.0, spin=True),
            'firstperson_righthand': transform(0.7, 0.35, base=BASE_HAND, spin=True),
            'thirdperson_righthand': transform(0.7, 0.35, base=BASE_HAND, spin=True),
        },
    }


boss_dir = os.path.join(PACK, 'assets/eclipsesmp/models/item')
for name in sorted(os.listdir(boss_dir)):
    if not (name.startswith('boss_') and name.endswith('.json')):
        continue
    path = os.path.join(boss_dir, name)
    with open(path, encoding='utf-8') as handle:
        data = json.load(handle)
    elements = data.get('elements', [])
    extent = max((abs(value) for element in elements
                  for key in ('from', 'to') for value in element[key]), default=0.0)
    factor = min(1.0, MAX_COORD / extent) if extent else 1.0
    if factor < 1.0:
        for element in elements:
            element['from'] = [round(value * factor, 3) for value in element['from']]
            element['to'] = [round(value * factor, 3) for value in element['to']]
            if 'rotation' in element:
                element['rotation']['origin'] = [round(value * factor, 3)
                                                for value in element['rotation']['origin']]
    ys = [value for element in elements for value in (element['from'][1], element['to'][1])]
    height = (max(ys) - min(ys)) if ys else 16.0
    data.update(boss_display(height, min(ys)))
    write(path, data)
    print('updated %s (geometry scale %.3f, height %.1fpx)' % (path, factor, height))
