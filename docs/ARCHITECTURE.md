# Architecture

Single-module Android app, MVVM with unidirectional data flow, manual DI.

```
UI (Compose)  ──collects StateFlow──▶  ViewModel  ──calls──▶  Repository
     ▲                                     │                      │
     └──────────── user intents ───────────┘                      ▼
                                                    Room DB · DataStore · ML Kit · TTS
```

## Layers

**`ui/`** — Compose screens and their view models. Each screen observes one
`StateFlow<UiState>`; every user action is a view-model function. Navigation is
Navigation-Compose with four bottom-bar destinations (Learn, Translate, Practice,
Progress) plus detail routes for lessons, language picking, settings, offline packs and
saved translations.

**`data/`**
- `local/` — Room entities, DAOs and the database. Six tables: `lesson_progress`,
  `vocab_progress`, `saved_translation`, `translation_history`, `daily_activity`,
  `achievement`. Indices on the columns actually filtered by.
- `prefs/` — DataStore-backed `UserPreferences` (settings + streak counters) and a small
  `SelectionBus` that returns a language choice from the picker to the opener.
- `repository/` — `LearningRepository` (lessons, XP, streaks, vocabulary scheduling,
  achievements) and `TranslationRepository` (translate + cache, history, saved items).
- `content/` — `LanguageCatalog` (the single source of truth for availability) and
  `SeedContent` (the shipped courses).

**`domain/`** — pure Kotlin with no Android dependencies, therefore unit-testable:
`StreakCalculator`, `Xp`, `SpacedRepetition`, `AnswerValidator`, `Achievements`, `Dates`.

**`translate/`, `audio/`** — engine and TTS adapters behind small interfaces.

**`di/AppContainer.kt`** — lazily constructs and shares the singletons.

## Data flow examples

**Completing a lesson.** `LessonViewModel.finish()` → `LearningRepository
.recordLessonResult()` writes `lesson_progress`, folds XP into today's `daily_activity`,
recomputes the streak via `StreakCalculator`, persists it through `UserPreferences`,
evaluates achievements, and returns an `ActivityResult` the results screen renders.
Lesson vocabulary is pushed into the SM-2 schedule.

**Translating.** `TranslateViewModel.translate()` cancels any in-flight job, optionally
detects the language, asks `TranslationRepository` (which checks the exact-match cache
first), then calls the engine. Failures surface as explicit messages — never as
invented output.

## Why these choices

- **Manual DI** over Hilt/Koin: no annotation processor, smaller APK, faster builds.
- **Room** over raw SQLite: compile-time-checked queries and Flow support.
- **DataStore** over SharedPreferences: atomic, coroutine-friendly, survives force-stop.
- **epoch-day integers** for streak math: immune to the midnight double-count and
  time-zone-change bugs that plague naive date arithmetic.
- **Per-ABI splits**: ML Kit native libs dominate APK size; splits keep downloads small.

## Performance posture

- Long lists use `LazyColumn`; there is no eager loading of whole tables.
- All Room access is `suspend`/`Flow`; nothing runs on the main thread.
- Only three translation clients are kept resident; models load on demand.
- No background services, no polling, no wake locks, no continuous animations.
- `collectAsStateWithLifecycle` stops collection when a screen is not visible.
