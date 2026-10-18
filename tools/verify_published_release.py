#!/usr/bin/env python3
"""Check the public source channels after publishing, before announcing a release."""
import argparse
import json
import re
import urllib.request


def verify(version, app):
    expected_url = (
        'https://github.com/titandrive/morphe-catppuccin/releases/download/'
        f'catppuccin-v{version}/morphe-catppuccin-{version}.mpp'
    )
    for branch in ('main', 'dev'):
        base = f'https://raw.githubusercontent.com/titandrive/morphe-catppuccin/{branch}/'

        def read(name):
            # Use the same public URLs as Morphe, including the CDN cache.
            with urllib.request.urlopen(base + name, timeout=30) as response:
                return response.read().decode('utf-8')

        for name in ('patches-bundle.json', 'morphe-patches-bundle.json'):
            manifest = json.loads(read(name))
            assert manifest['version'] == version, (branch, name, manifest['version'])
            assert manifest['download_url'] == expected_url, (branch, name, 'download URL')
        listing = json.loads(read('patches-list.json'))
        assert listing['version'] == version, (branch, 'patch listing version')
        assert any(p['name'].startswith(app) for p in listing['patches']), (branch, app, 'patch missing')
        changelog = read('CHANGELOG.md')
        entries = re.split(r'(?m)^#{1,2} ', changelog)
        latest = next(entry for entry in entries if entry.strip())
        assert latest.startswith(f'[{version}]'), (branch, 'latest changelog version')
        scope = rf'(?m)^[-*+] \*\*{re.escape(app)}:\*\*\s+\S'
        assert re.search(scope, latest), (branch, app, 'scoped update bullets missing')
        print(f'{branch}: {version} manifests, patch listing, and {app} changelog verified')
    with urllib.request.urlopen(expected_url, timeout=30) as response:
        assert response.read(4) == b'PK\x03\x04', 'Release asset is not a ZIP/MPP'
    print('Published MPP download verified')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('version')
    parser.add_argument('app', help='Changelog app scope, e.g. Todoist or Gboard')
    args = parser.parse_args()
    verify(args.version, args.app)
