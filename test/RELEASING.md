# Publishing a Catppuccin update

1. Add the release to `CHANGELOG.md` before building. Use app scopes such as
   `* **Todoist:** Fix the P4 color.` Morphe uses these for per-app update badges.
2. Bump the bundle version, both JSON manifests and `patches-list.json` together.
3. Build and check the MPP, push the source, and upload the GitHub release asset.
4. Publish the same manifests, patch list and changelog on **both `main` and `dev`**.
   Morphe can select the stable bundle while reading the dev changelog when
   prerelease sources are enabled. Use `[skip ci]` for metadata-only commits.
5. Run `python3 tools/verify_published_release.py VERSION APP`, for example
   `python3 tools/verify_published_release.py 0.5.1 Todoist`. This checks the live
   raw URLs, not just local files, and confirms the release asset downloads.
6. Announce the release only after these checks pass. Do not claim a phone badge
   was verified unless it was actually observed.
