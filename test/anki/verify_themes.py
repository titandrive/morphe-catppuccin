#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-or-later
"""Verify every compiled-APK resource qualifier retains Catppuccin theme overrides."""
import sys
import xml.etree.ElementTree as E
from pathlib import Path
root = Path(sys.argv[1])
seen = 0
for file in sorted(root.glob('values*/styles.xml')):
    for style in E.parse(file).getroot().findall('style'):
        palette = {'Theme_Light':'latte','Theme_Dark':'macchiato'}.get(style.attrib['name'])
        if palette is None: continue
        values={item.attrib['name']:item.text for item in style.findall('item')}
        for attr,role in [('android:colorBackground','base'),('android:textColorPrimary','text'),('fab_normal','mauve'),('navDrawerImage',None)]:
            expected = '@drawable/nav_drawer_catppuccin_'+palette if role is None else '@color/catppuccin_'+palette+'_'+role
            assert values.get(attr)==expected,(str(file),style.attrib['name'],attr,values.get(attr),expected)
        seen+=1
assert seen>=4,seen
print('All',seen,'light/dark theme variants contain background, text, mauve FAB, and drawer overrides.')
