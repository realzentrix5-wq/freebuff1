"""Validate the Eclipse SMP resource pack against the 1.21.11 (pack format 75) schema.

Checks item model definitions, every model/texture reference, legacy fields that were
removed in 1.21.4, display transforms and the custom_model_data routing used by the
plugin. Exits non-zero on the first failed check set.
"""
import json
import os
import sys
import zipfile

PACK = 'pack'
FORMAT = 75
# Pass the official 1.21.11 client jar to also check vanilla fallbacks and assets.
CLIENT_JAR = sys.argv[1] if len(sys.argv) > 1 else None
VANILLA_MODELS = {'minecraft:item/generated', 'minecraft:item/handheld'}
# Vanilla sprite/model references the pack intentionally inherits.
VANILLA_TEXTURES = {
    'minecraft:item/paper',
    'minecraft:item/netherite_ingot',
    'minecraft:item/netherite_sword',
    'minecraft:item/nether_star',
}

errors = []


def fail(message):
    errors.append(message)


def load(path):
    with open(path, encoding='utf-8') as handle:
        return json.load(handle)


def asset_files(*parts):
    root = os.path.join(PACK, *parts)
    for base, _, files in os.walk(root):
        for name in files:
            if name.endswith('.json') or name.endswith('.png'):
                yield os.path.join(base, name)


def json_files(*parts):
    for path in asset_files(*parts):
        if path.endswith('.json'):
            yield path


# ---------------------------------------------------------------- pack.mcmeta
meta = load(os.path.join(PACK, 'pack.mcmeta'))
pack = meta.get('pack', {})
if pack.get('pack_format') != FORMAT:
    fail('pack_format is %r, expected %d' % (pack.get('pack_format'), FORMAT))
if pack.get('min_format') != [FORMAT, 0] or pack.get('max_format') != [FORMAT, 0]:
    fail('min_format/max_format must be [%d, 0]' % FORMAT)

# ------------------------------------------------------------------- indexes
models = {}
textures = set()
for path in asset_files('assets'):
    rel = os.path.relpath(path, os.path.join(PACK, 'assets')).replace(os.sep, '/')
    if '/models/' in '/' + rel:
        namespace, _, rest = rel.partition('/')
        name = rest[len('models/'):-len('.json')]
        models[namespace + ':' + name] = (path, load(path))
    elif '/textures/' in '/' + rel:
        namespace, _, rest = rel.partition('/')
        name = rest[len('textures/'):-len('.png')]
        textures.add(namespace + ':' + name)

items = {}
for path in json_files('assets'):
    rel = os.path.relpath(path, os.path.join(PACK, 'assets')).replace(os.sep, '/')
    if '/items/' in '/' + rel:
        namespace, _, rest = rel.partition('/')
        items[namespace + ':' + rest[len('items/'):-len('.json')]] = load(path)


def resolve_model(name, where):
    if name in models:
        return
    if name in VANILLA_MODELS:
        return
    if name.startswith('minecraft:block/'):
        return
    fail('%s references missing model %s' % (where, name))


def walk_model(name, path, seen):
    if name in seen or name not in models:
        return
    seen.add(name)
    _, data = models[name]
    if 'overrides' in data:
        fail('%s still uses the removed overrides field' % path)
    parent = data.get('parent')
    if parent:
        resolve_model(parent, path)
        walk_model(parent, path, seen)
    for key, texture in data.get('textures', {}).items():
        if texture.startswith('#'):
            continue
        if texture in textures or texture in VANILLA_TEXTURES:
            # Only the atlas directories are stitched for item models. A texture in
            # textures/entity (or anywhere else) resolves on disk but renders as the
            # missing-texture checker in game.
            namespace, _, sprite = texture.partition(':')
            namespace = namespace if _ else 'minecraft'
            if not sprite.startswith(('item/', 'block/')):
                fail('%s texture %s is outside textures/item and textures/block, so '
                     'the item atlas will not stitch it' % (path, texture))
            continue
        fail('%s references missing texture %s' % (path, texture))
    elements = data.get('elements', [])
    for index, element in enumerate(elements):
        for face in element.get('faces', {}).values():
            texture = face.get('texture')
            if texture and texture.startswith('#') and texture[1:] not in data.get('textures', {}):
                fail('%s element %d references undefined texture variable %s'
                     % (path, index, texture))


for name, (path, _) in models.items():
    walk_model(name, path, set())

# ---------------------------------------------------------- display transforms
for name, (path, data) in models.items():
    display = data.get('display')
    if display is None:
        continue
    for context, transform in display.items():
        if context not in {'gui', 'ground', 'fixed', 'head', 'thirdperson_lefthand',
                           'thirdperson_righthand', 'firstperson_lefthand',
                           'firstperson_righthand'}:
            fail('%s has unknown display context %s' % (path, context))
        for key in ('rotation', 'translation', 'scale'):
            value = transform.get(key)
            if not (isinstance(value, list) and len(value) == 3
                    and all(isinstance(v, (int, float)) for v in value)):
                fail('%s display.%s.%s must be three numbers' % (path, context, key))
        rotation = transform.get('rotation', [0, 0, 0])
        translation = transform.get('translation', [0, 0, 0])
        scale = transform.get('scale', [1, 1, 1])
        if any(abs(v) > 360 for v in rotation):
            fail('%s display.%s rotation out of range' % (path, context))
        if any(abs(v) > 80 for v in translation):
            fail('%s display.%s translation out of range' % (path, context))
        if any(not 0 <= v <= 4 for v in scale):
            fail('%s display.%s scale out of range' % (path, context))


# ------------------------------------------------------- item model definitions
def walk_definition(name, node, where):
    if not isinstance(node, dict):
        fail('%s: item model node is not an object' % where)
        return
    kind = node.get('type')
    if kind == 'minecraft:model':
        target = node.get('model')
        if not isinstance(target, str):
            fail('%s: model definition without a model id' % where)
        else:
            resolve_model(target, where)
    elif kind == 'minecraft:range_dispatch':
        if node.get('property') != 'minecraft:custom_model_data':
            fail('%s: unexpected dispatch property %r' % (where, node.get('property')))
        entries = node.get('entries')
        if not isinstance(entries, list) or not entries:
            fail('%s: range_dispatch needs entries' % where)
            return
        thresholds = []
        for entry in entries:
            threshold = entry.get('threshold')
            if not isinstance(threshold, (int, float)):
                fail('%s: entry without numeric threshold' % where)
                continue
            thresholds.append(threshold)
            walk_definition(name, entry.get('model'), where)
        if thresholds != sorted(set(thresholds)):
            fail('%s: thresholds are not strictly ascending (%r)' % (where, thresholds))
        fallback = node.get('fallback')
        if fallback is None:
            fail('%s: missing fallback model' % where)
        else:
            walk_definition(name, fallback, where)
    else:
        fail('%s: unsupported item model type %r' % (where, kind))


for name, data in items.items():
    walk_definition(name, data.get('model'), 'assets/%s/items/%s.json' % tuple(name.split(':', 1)))

# ------------------------------------------------- routing expected by the plugin
EXPECTED = {
    'minecraft:paper': {
        1001: 'dimensionwars:item/nether_icon',
        1002: 'dimensionwars:item/end_icon',
        1003: 'dimensionwars:item/overworld_icon',
        2001: 'dimensionwars:item/boss_nether',
        2011: 'dimensionwars:item/boss_nether_medium',
        2021: 'dimensionwars:item/boss_nether_hard',
        2002: 'dimensionwars:item/boss_end',
        2012: 'dimensionwars:item/boss_end_medium',
        2022: 'dimensionwars:item/boss_end_hard',
        2003: 'dimensionwars:item/boss_overworld',
        2013: 'dimensionwars:item/boss_overworld_medium',
        2023: 'dimensionwars:item/boss_overworld_hard',
    },
    'minecraft:netherite_ingot': {
        3001: 'dimensionwars:item/key_nether',
        3002: 'dimensionwars:item/key_end',
        3003: 'dimensionwars:item/key_overworld',
    },
    'minecraft:nether_star': {
        3001: 'dimensionwars:item/catalyst_nether',
        3002: 'dimensionwars:item/catalyst_end',
        3003: 'dimensionwars:item/catalyst_overworld',
    },
    'minecraft:netherite_sword': {
        4001: 'dimensionwars:item/sword_netherite',
    },
}

# ------------------------------------------------------- model geometry sanity
# Large boss models are drawn by an ItemDisplay in FIXED mode. Their faces must sit
# inside the texture, and the display transform must stand them on the ground at a
# sensible height instead of burying or floating them.
for name, (path, data) in models.items():
    if not name.startswith('dimensionwars:item/boss_'):
        continue
    size = data.get('texture_size')
    if not size and data.get('parent') in models:
        size = models[data['parent']][1].get('texture_size')
    if size:
        for element in data.get('elements', []):
            for face_name, face in element.get('faces', {}).items():
                uv = face.get('uv')
                if not uv:
                    continue
                if uv[0] < 0 or uv[1] < 0 or uv[2] > size[0] or uv[3] > size[1]:
                    fail('%s face %s UV %r outside texture %r' % (path, face_name, uv, size))
    fixed = data.get('display', {}).get('fixed')
    if not fixed:
        fail('%s has no fixed display transform' % path)
        continue
    # ItemTransform#apply: point -> (point - 8) * scale + translation (translation in
    # 1/16 blocks), and the display renderer adds its own 180 degree Y spin, which the
    # model's fixed rotation must cancel so the figure faces where the boss faces.
    if fixed.get('rotation', [0, 0, 0])[1] % 360 != 180:
        fail('%s fixed rotation Y is %r; the display renderer spins 180 so this must be 180'
             % (path, fixed.get('rotation', [0, 0, 0])[1]))
    # The vanilla model loader refuses any coordinate outside -16..32 and then drops
    # the whole model, so the geometry has to be normalised before it is written.
    for element in data.get('elements', []):
        for key in ('from', 'to'):
            for axis, value in enumerate(element.get(key, [])):
                if not -16.0 <= value <= 32.0:
                    fail('%s %s.%s=%s outside the client limit -16..32'
                         % (path, key, 'xyz'[axis], value))
    ys = [v for element in data.get('elements', [])
          for v in (element.get('from', [0, 0, 0])[1], element.get('to', [0, 0, 0])[1])]
    if not ys:
        fail('%s has no geometry' % path)
        continue
    scale = fixed.get('scale', [1, 1, 1])[1]
    lift = fixed.get('translation', [0, 0, 0])[1]
    bottom = (min(ys) - 8.0) * scale / 16.0 + lift / 16.0
    top = (max(ys) - 8.0) * scale / 16.0 + lift / 16.0
    if abs(bottom) > 0.12:
        fail('%s feet at %+.2f blocks, expected the ground (fixed translation/scale)'
             % (path, bottom))
    if not 2.0 <= top <= 3.4:
        fail('%s is %.2f blocks tall when displayed, expected a boss-sized 2-3.4'
             % (path, top))

# ------------------------------------------------------- official client check
if CLIENT_JAR:
    try:
        jar = zipfile.ZipFile(CLIENT_JAR)
    except OSError as error:
        fail('cannot open client jar: %s' % error)
        jar = None
    if jar:
        names = set(jar.namelist())
        version = json.loads(jar.read('version.json'))
        pack_version = version.get('pack_version', {})
        major = pack_version.get('resource_major', pack_version.get('resource'))
        minor = pack_version.get('resource_minor', 0)
        client_format = '%s.%s' % (major, minor)
        if major is not None and int(major) != FORMAT:
            fail('client jar is pack format %s, validator expects %d'
                 % (client_format, FORMAT))
        for item, routes in EXPECTED.items():
            vanilla_path = 'assets/minecraft/items/%s.json' % item.split(':', 1)[1]
            if vanilla_path not in names:
                fail('client jar has no %s, cannot verify the override path' % vanilla_path)
        for fallback in {route.get('model') for item in EXPECTED
                         for route in [items[item]['model'].get('fallback', {})]}:
            if not fallback or not fallback.startswith('minecraft:'):
                continue
            vanilla_model = 'assets/minecraft/models/%s.json' % fallback.split(':', 1)[1]
            if vanilla_model not in names:
                fail('vanilla fallback model %s does not exist in the client jar'
                     % fallback)
        print('checked against client jar (resource pack format %s)' % client_format)

for item, routes in EXPECTED.items():
    definition = items.get(item)
    if definition is None:
        fail('missing item definition for %s' % item)
        continue
    node = definition['model']
    table = {}
    for entry in node.get('entries', []):
        table[entry['threshold']] = entry['model'].get('model')
    for value, expected in routes.items():
        chosen = None
        for threshold in sorted(table):
            if value >= threshold:
                chosen = table[threshold]
            else:
                break
        if chosen != expected:
            fail('%s custom_model_data %d routes to %r, expected %r'
                 % (item, value, chosen, expected))

# -------------------------------------------------------------------- summary
print('%d models, %d textures, %d item definitions' % (len(models), len(textures), len(items)))
if errors:
    print('\n%d problem(s):' % len(errors))
    for problem in errors:
        print(' -', problem)
    sys.exit(1)
print('pack OK for 1.21.11 (format %d)' % FORMAT)
