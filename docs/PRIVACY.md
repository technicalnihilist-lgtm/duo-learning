# Privacy

Plain language, because it matters.

## What is stored, and where

Everything stays on your device:

- selected languages and course positions
- completed lessons, XP, current and longest streak, daily goal
- vocabulary progress and spaced-repetition schedule
- saved translations, favourites, recent translation history
- achievements and settings

Storage is a local SQLite database (Room) and a DataStore preferences file. Android's
standard device backup may include them; nothing else does.

## What is sent off the device

**Nothing about your text — ever.** Translation and language detection run on-device.
Your text is not uploaded, logged, or shared.

The only network traffic is downloading translation language models the first time you
use a language, and that download contains no user data. You can also delete models.

## What is NOT collected

- No account, email, phone number or identity of any kind.
- No advertising SDK, no analytics SDK, no crash-reporting SDK.
- No tracking, no profiling, no hidden telemetry.
- No automatic upload of history or text.

## Your controls

- Delete individual translations or clear all history.
- Remove downloaded language packs to reclaim storage.
- Reset all learning progress (with a confirmation dialog).

## Permissions

- `INTERNET` — only to download optional on-device translation models.
- `ACCESS_NETWORK_STATE` — only to tell whether a model download is possible.

There is no location, camera, microphone, contacts or storage permission.
