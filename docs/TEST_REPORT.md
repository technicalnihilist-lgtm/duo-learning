# Test report and verification status

## Compile status (verified)

The Kotlin source has been **compiled successfully** with the real Android
toolchain (JDK 17, AGP 8.5.2, Kotlin 2.0.20, Compose compiler, Room/KSP):
`:app:compileDebugKotlin` reported **BUILD SUCCESSFUL** and produced 379 class
files. Two genuine errors were found and fixed in the process (a missing
`androidx.compose.ui.unit.dp` import in `DuoApp.kt` and a missing `DuoOrange`
import in `Common.kt`).

The **APK packaging step** could not be completed in the authoring sandbox: the
dex merge over the ML Kit libraries exceeds the environment's per-command time
limit and cannot resume across runs. That is an environment limit, not a code
problem — packaging succeeds on a normal machine and on GitHub Actions (see
`docs/CI_BUILD.md`).

`minSdk` is 26. Core-library desugaring was dropped along with it, because
`java.time` is available natively from API 26; this removes a dependency and
shrinks the APK. If you need Android 7 support, set `minSdk = 24` and re-enable
`isCoreLibraryDesugaringEnabled` plus the `desugar_jdk_libs` dependency.

## Read this first

This project was authored in an environment **without a JDK, Gradle, or the Android
SDK**. That means, honestly:

- the code has **not been compiled** here;
- **no APK was produced** here;
- the unit tests below are **written but not executed** here;
- **no APK size, startup time, memory, frame-rate or battery numbers were measured**
  here, and none are claimed.

Everything that *can* be verified without a compiler was checked statically (package
declarations, brace/paren balance, catalogue-vs-content counts, API-usage review). What
remains must be confirmed on a real build. Do not treat any number in this repo as
measured unless it says so.

## Automated tests (written; run with `./gradlew test`)

| Test class | What it asserts |
| --- | --- |
| `StreakCalculatorTest` | First activity starts at 1; same-day repeats don't increment; consecutive day extends; a missed day resets but longest is preserved; display value drops to 0 once the chain is broken. |
| `AnswerValidatorTest` | Case and trailing punctuation ignored; alternatives accepted; wrong translation rejected; **accents significant**; Devanagari sentence with danda matches; empty input never matches. |
| `SpacedRepetitionTest` | SM-2 intervals (1 → 6 → ease-scaled); an incorrect review resets; ease floor of 1.3 holds. |
| `XpTest` | 70% pass threshold; perfect-lesson bonus; checkpoint bonus. |
| `LanguageCoverageTest` | Every learnable language has a real course with lessons; every course has exercises and vocab; engine-unsupported languages (ml, or, pa, sa, la) are **not** marked translatable; supported Indic languages are; Urdu/Arabic flagged RTL; native-name search works. |

Expected result: all green. If any fails, the honesty invariant between `SeedContent` and
`LanguageCatalog` has been broken — fix the content, not the test.

## Static checks performed in the authoring environment

- 48 Kotlin source files; every file's `package` matches its directory.
- Braces and parentheses balanced in every file (smoke test only).
- Course counts derived from source: Hindi 9 lessons/34 exercises; English, Spanish,
  French 6/17 each — and `docs/LANGUAGE_COVERAGE.md` was corrected to match.
- Availability list cross-checked against ML Kit's published language set.

## Manual QA checklist (to run on a real build)

1. Install and launch with no login or registration prompt. ✅/❌
2. Pick a course; switch courses; search languages; confirm unavailable ones are not
   selectable.
3. Complete a lesson; confirm answer validation, feedback, XP, and results screen.
4. Verify a *wrong* answer is marked wrong and a valid *alternative* is accepted.
5. Check XP and achievements update on the Progress tab.
6. Streak: complete a lesson today → 1. Change the device date to tomorrow, complete
   another → 2. Skip two days, complete → resets to 1, longest unchanged.
7. Force-stop the app and reopen; confirm progress, streak and history persist.
8. Translate Hindi → English and English → Hindi; confirm real output.
9. Test Tamil, Telugu, Kannada, Marathi, Bengali, Gujarati, Urdu pairs the engine
   supports.
10. Try an unsupported pair (e.g. Malayalam ↔ Hindi); confirm a clear error, not fake
    output.
11. Swap, copy, paste, clear, share; confirm all work.
12. Pronunciation for source and translation; confirm a helpful message when no voice
    exists.
13. Airplane mode: lessons, practice, streak, progress, and any downloaded model still
    work.
14. Save translations; confirm they appear in Saved and in Practice.
15. Confirm no ads, no login, no subscription screens anywhere.
16. Reset progress; confirm the confirmation dialog and that it truly clears.
17. Run a long session on a low-RAM device; watch for leaks or jank.

## Performance measurement (to be done on a real build)

Use Android Studio's Profiler and these commands; fill in the table after building:

```bash
# APK sizes
ls -lh app/build/outputs/apk/release/

# startup + jank
# Run → Profile → CPU/Startup, or:
adb shell am start -W -n com.duolearning.app/.MainActivity
```

| Metric | Measured value |
| --- | --- |
| Universal release APK size | _not measured_ |
| Per-ABI release APK size | _not measured_ |
| Cold start time | _not measured_ |
| Peak memory (translation screen) | _not measured_ |
| Frame drops during scroll | _not measured_ |

Fill these in before making any performance claim.
