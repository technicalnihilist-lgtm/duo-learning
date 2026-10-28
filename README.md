# Duo Learning

A native Android app that combines a Duolingo-style progressive learning path with a
real, free, on-device universal translator. No account, no ads, no subscriptions, no
analytics, dark by default, and fully usable offline once content and models are on
the device.

- **Platform:** native Android (Kotlin + Jetpack Compose, Material 3)
- **Min SDK:** 24 (Android 7.0) · **Target/Compile SDK:** 34
- **Translation engine:** Google ML Kit on-device translation (free, no API key)
- **Storage:** Room (SQLite) + DataStore, all local
- **Package:** `com.duolearning.app`

> **Important:** this repository contains complete, buildable source. It does **not**
> contain a prebuilt APK, because the build was authored in an environment without a
> JDK/Android SDK. See `docs/BUILD_AND_INSTALL.md` — building takes one Android Studio
> sync. `docs/TEST_REPORT.md` states plainly what was and was not verified.

## Features

**Learn** — Units → lessons → checkpoints with 7 exercise types (multiple choice, typed
translation, word matching, scrambled sentences, fill-in-the-blank, listening, and
translation correction). Answers accept valid alternatives; accents stay significant.
Four real courses ship in the APK (Hindi, English, Spanish, French); every other
language is honestly labelled translation-only.

**Translate** — Source/target pickers, automatic language detection, swap, paste/clear,
copy/share, pronunciation for both sides, recent history, saved favourites, a result
cache keyed on the exact text + pair, and request cancellation when input changes.

**Learn from every translation** — a *Learn this* panel saves a pair to vocabulary and
feeds it into practice. Word-by-word glosses and grammar notes are **not** fabricated
when the engine cannot supply them.

**Practice** — spaced-repetition flashcards (SM-2), a difficult-words drill, and
revision of saved translations.

**Progress** — streak with flame, daily XP goal, learning calendar, achievements,
totals for XP / lessons / words.

**Settings** — daily goal, speech rate, sound, text scale, offline language-pack
management, history controls, and a confirmed full-progress reset.

## Quick start

1. Open the project folder in Android Studio (Ladybug or newer).
2. Let it sync (it downloads Gradle 8.7 and the Android SDK components).
3. Press **Run**, or build a release APK with:
   ```
   ./gradlew assembleRelease
   ```
   See `docs/BUILD_AND_INSTALL.md` for the wrapper note and signing details.

## Project layout

```
app/src/main/java/com/duolearning/app/
  data/            Room entities/DAOs/database, DataStore prefs, repositories, seed content
  domain/          Pure logic: streaks, XP, spaced repetition, answer validation, achievements
  translate/       TranslationEngine interface + ML Kit implementation + language detection
  audio/           TextToSpeech wrapper
  ui/              Compose theme, navigation, shared components, screens + view models
  di/              Manual dependency container
app/src/test/      JUnit tests for the pure logic and the language-coverage honesty rule
docs/              Coverage, build, engine, test report, limitations, privacy, GitHub notes
```

## Documentation

| Document | Contents |
| --- | --- |
| `docs/LANGUAGE_COVERAGE.md` | Exactly which languages are learnable and translatable, and why |
| `docs/TRANSLATION_ENGINE.md` | ML Kit integration, cost, limits, offline behaviour |
| `docs/BUILD_AND_INSTALL.md` | Build, sign, install, and troubleshooting |
| `docs/ARCHITECTURE.md` | Module-by-module design and data flow |
| `docs/TEST_REPORT.md` | Automated tests + an honest verification status |
| `docs/LIMITATIONS.md` | Known gaps and deliberate scope decisions |
| `docs/PRIVACY.md` | Data handling, in plain language |
| `docs/GITHUB.md` | How to publish this repository yourself |

## Design decisions worth knowing

- **ML Kit over a cloud API.** It is free, needs no key, runs on-device (so offline and
  private), and has a fixed, verifiable language list. The cost is a bounded language
  set and per-language model downloads. This is documented honestly in the UI.
- **Manual dependency injection.** Hilt/Koin would add a build-time processor and size
  for an app this small; the container in `di/AppContainer.kt` is clearer and lighter.
- **No bundled fonts.** The system font family already covers every script the app
  renders, which keeps the APK small.
- **Per-ABI APK splits.** ML Kit's native libraries are the largest size contributor;
  splits keep each download small while a universal APK is still produced.
