# Pocket Casts draft

Target: `au.com.shiftyjelly.pocketcasts` **8.23-rc-1** (9464), copied as original base and ARM64/English/xxhdpi splits from the phone. Local merged input: `/tmp/pocketcasts-original.apk`.

Opt in with `CATPPUCCIN_INCLUDE_POCKETCASTS=1` and a development `CATPPUCCIN_VERSION` when running `tools/build_catppuccin.py`. `CATPPUCCIN_POCKETCASTS_TEST=1` also includes the isolated test-identity patch. Neither is enabled in releases.

- **Pocket Casts themes** adds separate Latte, Macchiato, and Radioactivity choices. Stock XML styles and Compose palettes are preserved.
- **Pocket Casts custom font** uses the TTF file option, replaces bundled text fonts, and hooks system font creation for Compose.

Radioactivity's 107 semantic colors come from Automattic's Pocket Casts Android 8.3 `Colors.kt` (GPL-3.0). The legacy artwork tint attributes are restored too; Compose artwork and special effects still need visual verification before calling this a complete restoration.

Additional theme enum identities retain unique ordinals and preference strings. Theme dispatches normalize their behavior to light or dark. Kotlin EnumEntries keeps the true identities and ordinals. Validate theme persistence, stock switching, playback screens, widgets, and custom font rendering on-device before publishing.

The separate `Pocket Casts Theme Test` copy is installed with JetBrains Mono; the regular app remains installed. The first test build crashed after selecting an added theme because Material bottom navigation resolves `android:textColorSecondary` as a resource ID. All added theme colors now use named resources; the regression check requires that reference.

The corrected build opens Appearance with all three added choices. Radioactivity rendering and green-tinted artwork were observed on the Playlists page, and the user accepted its appearance. JetBrains Mono is visible. Both Catppuccin matrices pass host checks; full on-device coverage of Latte/Macchiato, playback and widgets remains outstanding. No release has been published.

## Local checks

```sh
/opt/android-studio/jbr/bin/javac --release 8 -d /tmp/pocket-fixtures \
  -cp "$ANDROID_HOME/platforms/android-37.0/android.jar" \
  tools/pocketcasts/src/app/morphe/extension/pocketcasts/PocketThemes.java \
  tools/pocketcasts/src/app/morphe/extension/pocketcasts/PocketSurfaces.java \
  test/pocketcasts/fixtures/*.java
/opt/android-studio/jbr/bin/java -cp /tmp/pocket-fixtures PaletteChecks
python3 test/pocketcasts/verify_apk.py /path/to/decoded-test-apk
```

The host checks exercise unique theme identities, preference conversion, stock palette isolation, cached palettes, and Catppuccin color values. They do not prove rendering on Android.

Featured and now-playing follow-up: artwork-derived native colors are remapped through a theme marker on added styles. Stock contexts have no marker and retain their original colors. The native mini-player gets a themed surface and mauve icon/play-button tints; the Compose player palette receives separate background colors. `PlayerChecks` verifies that the player override leaves stock themes alone.

Details/Up Next follow-up: the playing-row chapter progress view uses custom Canvas paints, now scoped to the selected added theme. Player Details and episode show notes load through WebViews; their generated HTML receives themed background/text/link styles and the optional embedded TTF as a data font. The HTML filter only touches the app's Roboto-based show-notes document. With a custom font but no theme patch, notes retain stock colors.

The Featured/mini-player and Details/Up Next follow-up builds apply successfully to the original merged 8.23-rc-1 APK. Decoded APK checks confirm all hooks and named color references, provider isolation, embedded font, and preservation of stock style colors/parents. The latest test copy is installed. Radioactivity mini-player colors were observed; final on-device verification of Details/Up Next and both Catppuccin variants remains outstanding. Keep these patches opt-in and unpublished.
