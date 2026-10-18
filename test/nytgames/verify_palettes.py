#!/usr/bin/env python3
"""Check palette cloning/gating with minimal fixtures matching NYT 6.46.1's API."""
import os
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
java = Path(os.environ['JAVA_HOME']) / 'bin'
with tempfile.TemporaryDirectory(prefix='nyt-palette-check-') as output:
    sources = sorted((Path(__file__).parent / 'fixtures').rglob('*.java'))
    sources.append(root / 'tools/nytgames/src/app/morphe/extension/nytgames/NytTheme.java')
    subprocess.run([str(java / 'javac'), '-d', output, *map(str, sources)], check=True)
    subprocess.run([str(java / 'java'), '-cp', output, 'PaletteChecks'], check=True)
