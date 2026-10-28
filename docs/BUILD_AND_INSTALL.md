# Build and install

## Prerequisites

- Android Studio **Ladybug (2024.2)** or newer, **or** a command-line Android SDK with
  JDK 17 and Gradle 8.7+.
- Android SDK Platform 34 and Build-Tools 34.
- A device or emulator running Android 7.0 (API 24) or newer.

## Building in Android Studio (recommended)

1. **File → Open** and select this project folder.
2. Let Gradle sync. Android Studio downloads Gradle 8.7 (per
   `gradle/wrapper/gradle-wrapper.properties`) and any missing SDK components.
3. Run on a device, or **Build → Generate App Bundles or APKs → Generate APKs**.

Android Studio regenerates the Gradle wrapper jar on first sync if it is absent, which
is why this repo ships `gradle-wrapper.properties` but not the binary jar.

## Building from the command line

If you have Gradle installed system-wide:

```bash
# one-time: create the wrapper jar and scripts
gradle wrapper --gradle-version 8.7

# debug build
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk

# release build
./gradlew assembleRelease        # app/build/outputs/apk/release/app-release.apk
```

The build produces **per-ABI APKs plus a universal APK** (`app-universal-release.apk`)
because of the ABI splits configured in `app/build.gradle.kts`. Install the one that
matches your device, or the universal one if unsure.

## Signing

The release build type currently points at the **debug** keystore so that
`assembleRelease` yields an installable APK for testing. Before you publish, create your
own keystore and reference it:

```bash
keytool -genkey -v -keystore duo-release.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias duo
```

Then in `app/build.gradle.kts` add a `signingConfigs { create("release") { ... } }` block
and point `buildTypes.release.signingConfig` at it. Keep the keystore and passwords out
of version control (`local.properties` or environment variables).

## Installing on a device

```bash
adb install -r app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

Or copy the APK to the phone and tap it (you may need to allow installs from unknown
sources for the file manager you use).

## Troubleshooting

- **"SDK location not found"** — open the project in Android Studio once, or create
  `local.properties` with `sdk.dir=/path/to/Android/sdk`.
- **First translation is slow** — the language model is downloading. Watch
  Settings → Offline language packs; it will say "Ready offline" when done.
- **No pronunciation** — the device has no speech data for that language. Install
  Google Text-to-Speech voices in Android Settings → System → Languages & input.
- **Gradle wrapper jar missing** — run `gradle wrapper --gradle-version 8.7` once, or
  let Android Studio regenerate it.

## Reproducing the release build

```
Gradle     8.7
AGP        8.5.2
Kotlin     2.0.20   (Compose compiler plugin 2.0.20)
KSP        2.0.20-1.0.25
Compose    BOM 2024.09.03
Room       2.6.1
ML Kit     translate 17.0.2, language-id 17.0.5
JDK        17
```
