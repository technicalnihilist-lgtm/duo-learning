# Building the APK on GitHub (no local SDK needed)

`.github/workflows/build-apk.yml` builds an installable APK on GitHub's runners.

## What it does

- Runs on every push to `main`, on any tag starting with `v`, and on demand.
- Sets up JDK 17 and Gradle 8.7, then runs `:app:assembleDebug`.
- Uploads the APK as a workflow **artifact** (`duo-learning-apk`).
- On a **tag**, also attaches the APK to that GitHub Release.

## Getting the APK

1. Push this repository to GitHub (see `docs/GITHUB.md`).
2. Open the **Actions** tab → **Build APK** → the latest run.
3. Download the `duo-learning-apk` artifact — that zip contains the `.apk`.

To publish a versioned release with the APK attached, push a tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The release then appears under **Releases** with the APK as a downloadable asset.

## Notes

- The workflow builds the **debug** APK, which is signed with the debug key and
  installs directly. For a store-ready build, add a keystore as repository
  secrets and switch the task to `:app:assembleRelease`.
- GitHub's Ubuntu runners ship with the Android SDK preinstalled, so no SDK
  setup step is required.
- If the first run fails on a compile error, the log points at the exact file
  and line — fix and push again; the workflow re-runs automatically.
