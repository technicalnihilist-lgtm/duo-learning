# Language coverage

Availability is reported in two independent axes, exactly as the product spec requires:

- **Learnable** — the app ships a real, playable course with correct content.
- **Translatable** — the on-device engine ships a model. Because ML Kit runs on-device,
  a language that is translatable is also translatable **offline** after its model is
  downloaded once. There is no separate "online-only" set with this engine.

Nothing is marked supported unless it actually is. The `LanguageCoverageTest` unit tests
fail if content and catalogue ever drift apart.

## Learnable (real courses in the APK)

| Language | Code | Units | Lessons | Exercises |
| --- | --- | --- | --- | --- |
| Hindi (for English speakers) | `hi` | 3 | 9 | 34 |
| English (for Hindi speakers) | `en` | 2 | 6 | 17 |
| Spanish (for English speakers) | `es` | 2 | 6 | 17 |
| French (for English speakers) | `fr` | 2 | 6 | 17 |

Counts are derived from `SeedContent` and asserted by tests. Adding a course is a matter
of adding real content to `SeedContent.kt` and flipping `courseAvailable` in
`LanguageCatalog.kt`.

## Translatable (on-device engine models)

The list below mirrors Google ML Kit's published translation language set. Codes are
BCP-47.

**Indian languages — supported:** `hi` Hindi, `en` English, `bn` Bengali, `ta` Tamil,
`te` Telugu, `kn` Kannada, `mr` Marathi, `gu` Gujarati, `ur` Urdu.

**Indian languages — not supported by the engine:** `ml` Malayalam, `or` Odia, `pa`
Punjabi, `as` Assamese, `ne` Nepali, `sa` Sanskrit, `kok` Konkani, `sd` Sindhi, `ks`
Kashmiri, `mni` Manipuri, `brx` Bodo, `doi` Dogri, `mai` Maithili, `sat` Santali.
These appear in the catalogue with a "No model" tag and cannot be selected for
translation — the app never pretends otherwise.

**International — supported:** `es` Spanish, `fr` French, `de` German, `it` Italian,
`pt` Portuguese, `ja` Japanese, `ko` Korean, `zh` Chinese (Mandarin), `ar` Arabic, `ru`
Russian, `tr` Turkish, `nl` Dutch, `sv` Swedish, `no` Norwegian, `da` Danish, `fi`
Finnish, `pl` Polish, `uk` Ukrainian, `el` Greek, `he` Hebrew, `th` Thai, `vi`
Vietnamese, `id` Indonesian, `ms` Malay, `ga` Irish, `cy` Welsh.

**International — not supported:** `la` Latin.

The full engine list (including `af, be, bg, ca, cs, eo, et, fa, gl, hr, ht, hu, is, ka,
lt, lv, mk, mt, ro, sk, sl, sq, sr, sw, tl`) is present in
`MlKitTranslationEngine.supportedLanguageCodes` and is used at runtime to gate the UI.

## Language pairs

ML Kit translates directly between any two of its supported languages; it does **not**
force English as an intermediate step. So `ta ↔ hi`, `gu ↔ ta`, `ja ↔ hi`, `ar ↔ en`
and similar all work directly.

## Interface / native language

The interface text ships in English. A separate "native language" setting selects the
language used for translation defaults and for the meaning side of exercises; it does
not translate the UI chrome itself.
