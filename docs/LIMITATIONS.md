# Known limitations and scope decisions

These are stated openly rather than hidden, because several are inherent to the chosen
free, offline, on-device engine.

## Translation

- **Fixed language set.** The engine supports the languages listed in
  `docs/LANGUAGE_COVERAGE.md` and no others. Malayalam, Odia, Punjabi, Sanskrit and
  several other Indian languages are **not** translatable by this engine. The app says
  so instead of failing silently.
- **First use needs a download.** A language model (15–40 MB) must be fetched once;
  until then that language is not available offline.
- **No word-by-word alignment.** The engine returns a sentence, not a token-aligned
  gloss. The *Learn this* panel therefore does **not** fabricate word meanings, parts of
  speech or grammar explanations — it offers the pair as study material instead. This is
  a deliberate correctness choice, not an oversight.
- **No formal/informal register control.** Output is whatever the model produces.

## Learning content

- **Four courses ship.** Hindi, English, Spanish, French. Other languages are
  translation-only until real content is authored. There are **no empty placeholder
  courses** — a language either has genuine lessons or is not offered for learning.
- **Course depth is modest** (2–3 units each). The path, exercise types, scoring and
  scheduling are complete; the content library is intentionally small so the APK stays
  light. Adding units is pure content work in `SeedContent.kt`.
- **Interface language is English only.** A native-language setting exists but does not
  translate the UI chrome.

## Audio

- Pronunciation uses the device's TextToSpeech engine. Quality and availability depend
  on the device's installed voices. When a voice is missing the app reports it rather
  than showing a dead button — but it cannot supply voices itself.

## Platform

- **minSdk 24.** `java.time` is used via core-library desugaring, which is enabled in the
  build.
- **No cloud sync.** Progress lives on the device (with Android's standard backup). There
  is no account, so there is nothing to sync across devices.
- **No iOS/desktop.** Android only, as specified.

## Not included (and why)

- Ads, analytics, subscriptions, in-app purchases, login, email, phone — excluded by the
  brief and by the privacy posture.
- Bundled fonts and bundled language models — excluded to keep the APK small.
- A backend — unnecessary, because the engine runs on-device.
