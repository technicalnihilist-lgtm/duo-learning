# Publishing this project to GitHub

The brief asked to publish the APK to GitHub under **technicalnihilist-lgtm**. A few
honest notes first:

- No APK exists yet, because it must be compiled with the Android SDK, which was not
  available where this source was written. Build it first
  (`docs/BUILD_AND_INSTALL.md`), then publish the resulting APK.
- I have no access to your GitHub account, and I will not ask for or handle your
  credentials. Publishing is something you run yourself with your own login.

## Option A — create the repo and push (command line)

```bash
cd duo-learning
git init
git add .
git commit -m "Duo Learning: offline-first language learning + on-device translator"

# create the repo on GitHub first (website or gh CLI), then:
git remote add origin https://github.com/technicalnihilist-lgtm/duo-learning.git
git branch -M main
git push -u origin main
```

With the GitHub CLI:

```bash
gh auth login
gh repo create technicalnihilist-lgtm/duo-learning --public --source=. --push
```

## Option B — publish a built APK as a release

After you have built the APK:

```bash
gh release create v1.0.0 \
  app/build/outputs/apk/release/app-universal-release.apk \
  --title "Duo Learning 1.0.0" \
  --notes "First release. Offline-first learning + on-device translation."
```

Or via the website: **Releases → Draft a new release → attach the APK**.

## Before you push — check these

- `.gitignore` already excludes `build/`, `.gradle/`, `local.properties` and IDE files.
- Do **not** commit a signing keystore or its passwords.
- Consider adding a `LICENSE` file (e.g. MIT) if you want others to reuse the code.
- If you attach an APK built with the debug keystore, label it clearly as a test build.
