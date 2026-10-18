# Morphe - Catppuccin

Catppuccin themes for Boost, Gboard, and AnkiDroid, patched with [Morphe Manager](https://morphe.software/).

Add `titandrive/morphe-catppuccin` as a remote patch source in Morphe.

## Boost

For **Boost 1.12.12**, apply **Catppuccin theme** alongside the original [Patcheddit patches](https://github.com/wchill/patcheddit). This add-on does not include their login/API fixes.

Open Boost’s theme selector and choose **Catppuccin Macchiato** (dark) or **Catppuccin Latte** (light).

For a custom font, also select **Boost custom font**, open its options, and choose a **TTF file**. The font applies throughout Boost with no in-app selector. Repatch to change it; omit the font patch to restore the original fonts.

## Gboard

For **Gboard 18.4.1.985164140-release-arm64-v8a**, apply **Gboard Catppuccin theme**. No Patcheddit patches are needed.

Open Gboard **Settings → Theme → Colors → Show more → Catppuccin Macchiato**. Choose **Key shape: None** for the borderless look, then **Apply**.

For a custom font, also select **Gboard custom font**, open its options, and choose a **TTF file**. Switch fonts in **Gboard Settings → Preferences → Font**.

## AnkiDroid

For **AnkiDroid 2.25.1 or 2.25.2**, apply **AnkiDroid Catppuccin theme and fonts**. No other patch source is needed.

In **Settings → Appearance**, choose **Catppuccin Latte** as the day theme and **Catppuccin Macchiato** as the night theme. **Font and text size** offers App default, bundled JetBrains Mono, a custom TTF picker, and 70–150% text scaling. Selected fonts override card template fonts in study and preview screens; App default restores the original fonts.

Start with an **original, unpatched APK** when updating. Morphe signs the result with its own key, so it cannot install directly over a Google Play/GitHub-signed AnkiDroid installation. Back up/sync your collection before switching installations, and keep using the same Morphe signing key for subsequent updates. AnkiCat remains a separate app.

## Vibe-code disclosure

This project is vibe-coded with AI assistance from OpenAI Codex.

This app uses code from Patcheddit. To learn more, visit https://reddit.com/r/patcheddit

### Todoist (in development)

Two optional patches target Todoist `v12314`: **Todoist Catppuccin theme** and
**Todoist custom font**. Select **Catppuccin Latte** or **Catppuccin Macchiato** in Todoist's theme picker.
Original themes stay available; Catppuccin selections are saved locally. The font patch embeds a `.ttf` chosen in Morphe; repatch
with an original APK to change or remove it. This work has not been published yet.
