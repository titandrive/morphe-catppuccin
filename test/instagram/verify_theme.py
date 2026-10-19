#!/usr/bin/env python3
"""Validate color resources in an APKEditor-decoded Instagram test build."""
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]) / 'resources/package_1/res'
base = {node.get('name'): node.text for node in ET.parse(root/'values/colors.xml').getroot()}
assert base['igds_primary_background'] == '#eff1f5'
night = {node.get('name'): node.text for node in ET.parse(root/'values-night/colors.xml').getroot()}
assert night['igds_primary_background'] == '#24273a'
for table, accent, foreground, raw_black in [
    (base, '#8839ef', 'ffeff1f5', 'ff4c4f69'),
    (night, '#c6a0f6', 'ffcad3f5', 'ff24273a'),
]:
    assert table['checkbox_checked_enabled'] == accent
    assert table['igds_prism_indigo_1000'] == accent
    assert table['bds_white'] == '#' + foreground
    assert table['igds_prism_black'] == '#' + raw_black
    assert table['bds_white_95_transparent'].startswith('#f2'), 'Alpha must be preserved'
checked = 0
for style in ET.parse(root/'values/styles.xml').getroot():
    name = style.get('name', '')
    semantic = name.startswith('Igds') and name.endswith(('Dark', 'Light'))
    native = name in {'BaseNightTheme', 'Theme.Instagram', 'DirectDarkMode', 'DirectLightMode'}
    if not semantic and not native:
        continue
    for item in style:
        value = item.text or ''
        if 'catppuccin_instagram_' not in value:
            continue
        assert value.startswith('@color/'), f'{name}: Instagram requires a resource ID, not a literal color'
        assert value.removeprefix('@color/') in base, f'Unresolved theme reference: {value}'
        checked += 1
assert checked >= 20, f'Expected legacy and Prism theme coverage, found {checked}'
print(f'Latte/Macchiato backgrounds and {checked} named theme references verified')
