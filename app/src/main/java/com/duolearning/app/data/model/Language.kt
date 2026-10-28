package com.duolearning.app.data.model

/**
 * Whether the bundled on-device engine can translate this language.
 *
 * The app ships Google ML Kit's on-device translation engine. Its language
 * list is fixed and verifiable; anything not in it is reported as
 * [TranslationSupport.UNSUPPORTED] rather than silently failing. The engine
 * downloads a small model the first time a language is used and then works
 * fully offline, so "online" and "offline" availability coincide for the
 * supported set.
 */
enum class TranslationSupport {
    /** Engine has a model for this language; works offline after first download. */
    ON_DEVICE,
    /** No engine model exists. Translation is not offered. */
    UNSUPPORTED
}

data class Language(
    /** BCP-47 tag, e.g. "hi", "zh", "pt". */
    val code: String,
    val englishName: String,
    val nativeName: String,
    val script: String,
    val isRtl: Boolean = false,
    val translationSupport: TranslationSupport = TranslationSupport.UNSUPPORTED,
    /** True only when the app ships a real, playable course for this language. */
    val courseAvailable: Boolean = false,
    /** Region grouping used purely for catalogue browsing. */
    val region: Region = Region.INTERNATIONAL
) {
    enum class Region { INDIAN, INTERNATIONAL }

    val translateSupported: Boolean get() = translationSupport == TranslationSupport.ON_DEVICE
}
