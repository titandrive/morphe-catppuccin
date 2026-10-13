#!/usr/bin/env python3
"""Check built APK assets and isolated-test boundaries after real Morphe patching."""
# SPDX-License-Identifier: GPL-3.0-or-later
import sys
import zipfile
from pathlib import Path
root = Path(__file__).resolve().parents[2]
with zipfile.ZipFile(sys.argv[1]) as apk:
    assets = root / 'patches/src/main/resources/catppuccin/anki'
    for name in ('JetBrainsMono-Regular.ttf', 'font.js', 'pages.css', 'latte.css', 'macchiato.css', 'OFL-1.1.txt'):
        assert apk.read('assets/catppuccin/anki/' + name) == (assets / name).read_bytes(), name
    dex = [apk.read(name) for name in apk.namelist() if name.endswith('.dex')]
    for hook in (b'Lapp/morphe/extension/anki/AnkiTheme;', b'Lapp/morphe/extension/anki/FontSettingsActivity;', b'catppuccin_anki_font', b'catppuccin_anki_size'):
        assert any(hook in data for data in dex), hook
    print('APK contains current theme palettes, bundled font and license, font override script, and runtime hooks.')
