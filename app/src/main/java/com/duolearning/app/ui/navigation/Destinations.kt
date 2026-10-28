package com.duolearning.app.ui.navigation

/** Type-safe-ish route table for the Compose navigation graph. */
object Routes {
    const val HOME = "home"
    const val TRANSLATE = "translate"
    const val PRACTICE = "practice"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val SAVED = "saved"
    const val OFFLINE_PACKS = "offline_packs"

    const val LESSON = "lesson/{course}/{lessonId}"
    fun lesson(course: String, lessonId: String) = "lesson/$course/$lessonId"

    /** mode is one of: learn, source, target */
    const val LANGUAGE_PICKER = "languages?mode={mode}"
    fun languagePicker(mode: String) = "languages?mode=$mode"

    const val ARG_COURSE = "course"
    const val ARG_LESSON_ID = "lessonId"
    const val ARG_MODE = "mode"
}
