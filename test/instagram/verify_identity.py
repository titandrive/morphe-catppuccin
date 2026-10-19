#!/usr/bin/env python3
"""Check a decoded isolated test manifest before it may be installed."""
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

ANDROID = '{http://schemas.android.com/apk/res/android}'
PACKAGE = 'com.instagram.android.catppuccin.test'

def verify(path):
    root = ET.parse(path).getroot()
    assert root.get('package') == PACKAGE, 'Test APK must use the isolated package'
    assert not root.get(ANDROID + 'sharedUserId'), 'Test APK must not share an app UID'
    authorities = [authority for node in root.iter('provider')
                   for authority in node.get(ANDROID + 'authorities', '').split(';') if authority]
    duplicate = [a for a, count in Counter(authorities).items() if count > 1]
    assert not duplicate, f'Duplicate provider authorities: {duplicate}'
    assert all('catppuccin.test' in a for a in authorities), 'Unisolated provider'
    declared = [node.get(ANDROID + 'name', '') for node in root.iter('permission')]
    assert all(p.startswith(PACKAGE + '.permission.') for p in declared), 'Unisolated private permission'
    assert len(declared) == len(set(declared)), 'Duplicate private permission'
    app = root.find('application')
    assert app is not None and app.get(ANDROID + 'label') == 'Instagram Theme Test'
    print(f'Isolated package, {len(authorities)} unique provider authorities, and {len(declared)} private permissions verified')

if __name__ == '__main__':
    verify(Path(sys.argv[1]))
