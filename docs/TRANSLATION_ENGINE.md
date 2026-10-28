# Translation engine

## What is integrated

The app uses **Google ML Kit On-Device Translation** (`com.google.mlkit:translate`)
for translation and **ML Kit Language Identification** (`com.google.mlkit:language-id`)
for automatic source detection. Both run entirely on the device.

## Why this engine

| Requirement | How ML Kit satisfies it |
| --- | --- |
| Real translations, not hardcoded demos | It is a real statistical/NMT engine, not string lookup. |
| Free, no app-level charge to users | ML Kit is free and unlimited; there is no billing. |
| No private server API keys in the APK | There is no key at all. Nothing to embed or leak. |
| Offline translation | Models run locally after a one-time download. |
| Privacy | User text never leaves the device. |
| No backend to run | There is no server component. |

## Cost, limits and requirements — stated plainly

- **Monetary cost:** none. There is no usage cap and no API key.
- **Storage cost:** each downloaded language model is roughly 15–40 MB. This is why the
  app downloads models on demand and lets users remove them (Settings → Offline
  language packs).
- **Download cost:** the first use of a language needs a data connection to fetch its
  model. After that it works in airplane mode.
- **Language set:** fixed. See `docs/LANGUAGE_COVERAGE.md` for the exact list.
- **APK size:** ML Kit's native libraries add a few MB per ABI; the build uses per-ABI
  splits to keep each download small.

The app does **not** claim unlimited free translation of every language — only the
languages the engine actually supports, which the UI reports honestly.

## How it is wired

- `translate/TranslationEngine.kt` — the interface the UI depends on. Adding a second
  provider (for example a self-hosted LibreTranslate server) means implementing this
  interface and swapping it in `di/AppContainer.kt`; no screen changes required.
- `translate/MlKitTranslationEngine.kt` — the ML Kit implementation. It:
  - keeps at most **3** open translators (an LRU eviction) to bound RAM;
  - downloads models via `downloadModelIfNeeded` with default conditions;
  - reports downloaded models through `RemoteModelManager` so the UI can show offline
    availability;
  - wraps Play Services `Task`s in a small `await()` helper instead of pulling in an
    extra coroutines-play-services dependency.
- `translate/LanguageIdentifier.kt` — on-device language detection for the "Detect
  language" source option.
- `data/repository/TranslationRepository.kt` — adds an exact-match cache (same text AND
  same language pair only), trims history to 300 rows, and never writes a fabricated
  result on failure.

## If you want a different or broader engine

The interface makes this a contained change. Two realistic options:

1. **LibreTranslate self-hosted** — free and open source, broader language list, but it
   needs a server you run. Point the engine at your server's URL; do not ship keys in
   the APK.
2. **A cloud provider** — broader quality/coverage, but it costs money and sends text
   off-device, which would break the offline and privacy promises. It would need a
   backend proxy (keys must never live in the APK) and honest in-app disclosure.

Because the current engine needs no server, there is no backend source in this repo.
