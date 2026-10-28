# Design previews

**These are design mockups, not device captures.** They were rendered from the app's
own design tokens (the colours, spacing, typography and layout defined in
`ui/theme/` and the screen composables) to preview the intended interface. They were
**not** screenshotted from a running build, because the app had not been compiled when
they were created.

Why it matters: treat them as an accurate picture of the *design*, not as evidence that
the app has been run. Once you build and run the app, replace these files with real
screenshots taken from a device or emulator:

```bash
adb exec-out screencap -p > docs/screenshots/learn.png
```

| File | Screen |
| --- | --- |
| `mockup-learn.png` | Learn / home: course, streak, daily goal, lesson path |
| `mockup-translate.png` | Duo Translate: language pair, input, result, Learn this |
| `mockup-practice.png` | Practice: spaced-repetition modes and a flashcard |
| `mockup-progress.png` | Progress: streak, stats, learning calendar, achievements |

The Devanagari shown in the previews is real text shaped with a proper Devanagari font;
the strings are the actual Hindi course content.
