# Fastmail patch

Target inspected: Android Fastmail 4.24.0 (26100803).

The patch replaces the web app's dark-mode semantic CSS variables with
Catppuccin Macchiato. Fastmail's normal Appearance setting still controls
light/dark mode; light mode is unchanged in this first pass.

The Java hook runs only for the main HTTPS app.fastmail.com page. It injects
one idempotent stylesheet, does not modify account preferences, does not read
message text, and does not inject into email frames. Native dialogs, system
bars, and loading screens still need visual verification.

Build an isolated test bundle locally:

```
CATPPUCCIN_FASTMAIL_TEST=1 \
CATPPUCCIN_VERSION=0.6.0-fastmail-test \
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=/home/jake/Android/Sdk \
python3 tools/build_catppuccin.py
```

The test identity patch gives the APK a separate package and provider
permissions. Never install a test-signed APK over the user's main app.
