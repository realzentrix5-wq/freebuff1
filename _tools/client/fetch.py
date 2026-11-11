"""Download a Minecraft client (jar, libraries, natives, assets) for headless testing.

Usage: python3 _tools/client/fetch.py [version] [--dir DIR]
Defaults to 1.21.11 and /tmp/mc so the downloads stay out of the repository.
"""
import argparse
import json
import os
import urllib.request
import zipfile

UA = {'User-Agent': 'Mozilla/5.0'}
MANIFEST = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'


def get(url, dest=None):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req) as response:
        data = response.read()
    if dest:
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        with open(dest, 'wb') as f:
            f.write(data)
    return data


def allowed(lib):
    rules = lib.get('rules')
    if not rules:
        return True
    ok = False
    for rule in rules:
        name = rule.get('os', {}).get('name')
        if rule['action'] == 'allow' and (name is None or name == 'linux'):
            ok = True
        if rule['action'] == 'disallow' and name == 'linux':
            ok = False
    return ok


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('version', nargs='?', default='1.21.11')
    parser.add_argument('--dir', default='/tmp/mc')
    args = parser.parse_args()
    base = args.dir
    os.makedirs(base, exist_ok=True)

    manifest = json.loads(get(MANIFEST))
    entry = next(v for v in manifest['versions'] if v['id'] == args.version)
    meta = json.loads(get(entry['url']))
    with open(os.path.join(base, 'version.json'), 'w') as f:
        json.dump(meta, f)

    get(meta['downloads']['client']['url'], os.path.join(base, 'client.jar'))
    print('client.jar %d bytes' % os.path.getsize(os.path.join(base, 'client.jar')))

    count = 0
    for lib in meta['libraries']:
        if not allowed(lib):
            continue
        artifact = lib.get('downloads', {}).get('artifact')
        if artifact:
            path = os.path.join(base, 'libraries', artifact['path'])
            if not os.path.exists(path):
                get(artifact['url'], path)
                count += 1
        for key, extra in lib.get('downloads', {}).get('classifiers', {}).items():
            if 'linux' not in key:
                continue
            jar = os.path.join(base, 'natives', os.path.basename(extra['path']))
            if os.path.exists(jar):
                continue
            get(extra['url'], jar)
            with zipfile.ZipFile(jar) as z:
                for name in z.namelist():
                    if name.endswith(('.so', '.so.1')):
                        with open(os.path.join(base, 'natives', os.path.basename(name)), 'wb') as f:
                            f.write(z.read(name))
    print('libraries downloaded %d' % count)

    index = meta['assetIndex']
    index_path = os.path.join(base, 'assets', 'indexes', index['id'] + '.json')
    objects = json.loads(get(index['url'], index_path))['objects']
    missing = 0
    for obj in objects.values():
        path = os.path.join(base, 'assets', 'objects', obj['hash'][:2], obj['hash'])
        if os.path.exists(path):
            continue
        get('https://resources.download.minecraft.net/%s/%s' % (obj['hash'][:2], obj['hash']), path)
        missing += 1
    print('assets %d total, %d downloaded' % (len(objects), missing))


if __name__ == '__main__':
    main()
