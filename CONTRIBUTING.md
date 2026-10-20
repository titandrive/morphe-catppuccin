# Contributing

Report bugs and suggest changes in [GitHub issues](https://github.com/titandrive/morphe-catppuccin/issues). Include the app version, bundle version, and steps to reproduce.

## Build

Install Python 3, JDK 21 or newer, and the Android SDK with a platform and build tools. Set `JAVA_HOME` and `ANDROID_HOME`, then run:

```sh
python3 tools/build_catppuccin.py
```

The builder downloads public Morphe and Kotlin artifacts into `.cache/catppuccin/` and writes the bundle to `build/`. No Gradle or GitHub Packages credentials are needed.

Work from `feat/catppuccin` and submit changes against that branch. `main` and `dev` also serve Morphe update metadata; follow [the release checklist](test/RELEASING.md) when publishing.

The default build contains the released apps. Maps, NYT Games, and Instagram remain opt-in drafts using `CATPPUCCIN_INCLUDE_MAPS=1`, `CATPPUCCIN_INCLUDE_NYT_GAMES=1`, or `CATPPUCCIN_INCLUDE_INSTAGRAM=1`. Use a development version such as `CATPPUCCIN_VERSION=0.6.1-dev` for local experiments. App-specific checks and test-copy instructions are under `test/`.

Keep LICENSE, NOTICE, COPYRIGHT, and bundled asset licenses intact. This repository originated from Patcheddit; its API/login patches are maintained and distributed separately.
