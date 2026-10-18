#!/usr/bin/env python3
"""Check reconstructed Todoist resource semantics and embedded font bytes."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
root = Path(sys.argv[1])
resources = root / 'resources/package_1' if (root/'resources/package_1').is_dir() else root
colors = {e.attrib['name']: e.text for p in (resources/'res').glob('values/*.xml') for e in ET.parse(p).getroot() if e.tag == 'color'}
expected = {
 'catppuccin_latte_background_base_primary':'#eff1f5',
 'catppuccin_macchiato_background_base_primary':'#24273a',
 'catppuccin_latte_background_grouped_tertiary':'#eff1f5',
 'catppuccin_macchiato_background_grouped_tertiary':'#24273a',
 'catppuccin_latte_display_primary_idle_tint':'#4c4f69',
 'catppuccin_macchiato_display_primary_idle_tint':'#cad3f5',
 'catppuccin_latte_actionable_primary_idle_fill':'#8839ef',
 'catppuccin_macchiato_actionable_primary_idle_fill':'#c6a0f6',
 'catppuccin_macchiato_actionable_primary_on_idle_tint':'#24273a',
 'catppuccin_latte_actionable_primary_on_idle_tint':'#eff1f5',
 'catppuccin_macchiato_actionable_destructive_on_idle_tint':'#24273a',
 'catppuccin_macchiato_priorities_p1_primary_idle_fill':'#ed8796',
 'catppuccin_macchiato_priorities_p2_primary_idle_fill':'#f5a97f',
 'catppuccin_macchiato_priorities_p3_primary_idle_fill':'#8aadf4',
 'catppuccin_macchiato_selectable_primary_unselected_tint':'#b8c0e0',
 'catppuccin_macchiato_selectable_secondary_selected_fill':'#363a4f',
 'catppuccin_macchiato_selectable_secondary_on_selected_tint':'#c6a0f6',
 'catppuccin_latte_selectable_primary_unselected_tint':'#5c5f77',
 'catppuccin_latte_selectable_secondary_selected_fill':'#ccd0da',
 'catppuccin_latte_selectable_secondary_on_selected_tint':'#8839ef',
 'catppuccin_macchiato_divider_primary':'#5b6078',
 'catppuccin_latte_divider_primary':'#9ca0b0',
}
for name, value in expected.items():
 assert colors[name].lower() == value, (name, colors[name], value)
font = Path(sys.argv[2]).read_bytes()
assert Path(sys.argv[3]).read_bytes() == font
assert (resources/'res/font/catppuccin_todoist_custom.ttf').read_bytes() == font
print(f'Passed {len(expected)} color assertions and both embedded font checks')
styles = {e.get('name'): {i.get('name'): i.text for i in e.findall('item')} for p in (resources/'res').glob('values/styles.xml') for e in ET.parse(p).getroot() if e.tag == 'style'}
assert styles['Widget.Catppuccin.Snackbar']['backgroundTint'] == '?attr/backgroundRaisedPrimary'
assert styles['Widget.Catppuccin.Snackbar.TextView']['android:textColor'] == '?android:attr/textColorPrimary'
print('Snackbar surface and text colors verified')

assert styles['Base.Widget.Material3.Snackbar']['backgroundTint'] == '?attr/colorSurfaceInverse'
assert 'snackbarStyle' not in styles['ThemeOverlay.Light.Todoist.Default']
assert styles['ThemeOverlay.Catppuccin.Latte']['backgroundBasePrimary'] == '@color/catppuccin_latte_background_base_primary'
assert styles['ThemeOverlay.Catppuccin.Macchiato']['backgroundBasePrimary'] == '@color/catppuccin_macchiato_background_base_primary'
if len(sys.argv) > 4:
 original = {e.attrib['name']: e.text for e in ET.parse(sys.argv[4]).getroot() if e.tag == 'color'}
 def canonical(value):
  value = value.lower()
  return '#' + value[3:] if value.startswith('#ff') and len(value) == 9 else value
 for name, value in original.items():
  assert canonical(colors[name]) == canonical(value), (name, colors[name], value)
 print(f'All {len(original)} original colors preserved')

for variant, prefix in [('Latte', 'catppuccin_latte_'), ('Macchiato', 'catppuccin_macchiato_')]:
 for attribute in ('colorAccent', 'android:colorAccent'):
  assert styles['ThemeOverlay.Catppuccin.' + variant][attribute] == '@color/' + prefix + 'actionable_primary_idle_fill'
print('Both refresh-spinner accent sources use mauve in custom themes')
