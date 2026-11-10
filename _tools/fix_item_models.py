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
    (1001, 'dimensionwars:item/nether_icon'),
    (1002, 'dimensionwars:item/end_icon'),
    (1003, 'dimensionwars:item/overworld_icon'),
]
BOSSES = [
    (2001, 'dimensionwars:item/boss_nether'),
    (2011, 'dimensionwars:item/boss_nether_medium'),
    (2021, 'dimensionwars:item/boss_nether_hard'),
    (2002, 'dimensionwars:item/boss_end'),
    (2012, 'dimensionwars:item/boss_end_medium'),
    (2022, 'dimensionwars:item/boss_end_hard'),
    (2003, 'dimensionwars:item/boss_overworld'),
    (2013, 'dimensionwars:item/boss_overworld_medium'),
    (2023, 'dimensionwars:item/boss_overworld_hard'),
]
KEYS = [
    (3001, 'dimensionwars:item/key_nether'),
    (3002, 'dimensionwars:item/key_end'),
    (3003, 'dimensionwars:item/key_overworld'),
]
CATALYSTS = [
    (3001, 'dimensionwars:item/catalyst_nether'),
    (3002, 'dimensionwars:item/catalyst_end'),
    (3003, 'dimensionwars:item/catalyst_overworld'),
]

write(os.path.join(ITEMS, 'paper.json'), ranged('minecraft:item/paper', ICONS + BOSSES))
write(os.path.join(ITEMS, 'netherite_ingot.json'),
      ranged('minecraft:item/netherite_ingot', KEYS))
write(os.path.join(ITEMS, 'nether_star.json'),
      ranged('minecraft:item/nether_star', CATALYSTS))
write(os.path.join(ITEMS, 'netherite_sword.json'),
      ranged('minecraft:item/netherite_sword', [(4001, 'dimensionwars:item/sword_netherite')]))

# Strip the removed overrides field from the vanilla item models, which are now only
# used as the fallback model for their base item.
for name in ('paper', 'netherite_ingot', 'nether_star', 'netherite_sword'):
    path = os.path.join(PACK, 'assets/minecraft/models/item/%s.json' % name)
    with open(path, encoding='utf-8') as handle:
        data = json.load(handle)
    data.pop('overrides', None)
    write(path, data)

# The boss figure is drawn by an ItemDisplay using the item frame ("fixed") transform.
# The display pivot is the centre of the model, so lift the 64px tall model by half its
# scaled height to stand it on the ground, and shrink the GUI view to a single slot.
BOSS_DISPLAY = {
    'gui_light': 'front',
    'display': {
        'gui': {
            'rotation': [0, 0, 0],
            'translation': [0, 0, 0],
            'scale': [0.25, 0.25, 0.25],
        },
        'ground': {
            'rotation': [0, 0, 0],
            'translation': [0, 2, 0],
            'scale': [0.25, 0.25, 0.25],
        },
        'fixed': {
            'rotation': [0, 0, 0],
            'translation': [0, 22, 0],
            'scale': [0.7, 0.7, 0.7],
        },
    },
}

boss_dir = os.path.join(PACK, 'assets/dimensionwars/models/item')
for name in sorted(os.listdir(boss_dir)):
    if not (name.startswith('boss_') and name.endswith('.json')):
        continue
    path = os.path.join(boss_dir, name)
    with open(path, encoding='utf-8') as handle:
        data = json.load(handle)
    data.update(BOSS_DISPLAY)
    write(path, data)
    print('updated', path)
