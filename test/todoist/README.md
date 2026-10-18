# Todoist patch checks

Initial supported APK: `com.todoist`, `v12314` (version code 12314).

Both patches are opt-in and independent. The theme adds Catppuccin Latte and Macchiato to the native picker. Original
Todoist palettes remain unchanged. Custom selections use local IDs 1001/1002
and disable cross-device theme sync through Todoist's existing local-only flag. The optional custom font patch takes a `.ttf`
through Morphe's file option and embeds it into the APK.

Before publishing:

- Build the MPP and apply each patch individually and both together to an original APK.
- Check reconstructed color resources: main background, text, dividers, mauve
  primary controls, and distinct red/orange/blue priorities in both modes.
- Run `ThemeHooks.java` against the patched APK and `TodoistSchema.java`: check
  catalog, resolver guards, conditional navigation hooks, and 413 semantic fields.
- Compare embedded font bytes with the selected TTF. Check the Application
  initialization hook and font resolver calls; extension calls must remain unhooked.
- Run a separate test installation. Check launch, task lists, editor, navigation,
  search, settings, date picker and dialogs in both modes. Confirm fonts visually
  and preserve task data in the original app.

Device runtime verification is required before calling this ready for release.

Verify switching from stock Todoist to both Catppuccin choices and back,
including a force-stop/relaunch. Check that stock palettes are unchanged and
that custom selections remain local.
