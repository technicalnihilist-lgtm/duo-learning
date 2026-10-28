package com.duolearning.app.data.content

import com.duolearning.app.data.model.Language
import com.duolearning.app.data.model.Language.Region
import com.duolearning.app.data.model.TranslationSupport

/**
 * The single source of truth for language availability.
 *
 * IMPORTANT: this list is deliberately conservative. [TranslationSupport]
 * reflects the languages Google ML Kit's on-device translation engine
 * actually ships models for (verified against the ML Kit language list).
 * [Language.courseAvailable] is true only for languages the app ships real,
 * playable lessons for. A language can therefore be translatable but not
 * learnable, and vice versa is never claimed.
 */
object LanguageCatalog {

    private val SUPPORTED = TranslationSupport.ON_DEVICE
    private val NO = TranslationSupport.UNSUPPORTED

    val languages: List<Language> = listOf(
        // ---------------- Indian languages ----------------
        Language("hi", "Hindi", "हिन्दी", "Devanagari", false, SUPPORTED, true, Region.INDIAN),
        Language("en", "English", "English", "Latin", false, SUPPORTED, true, Region.INDIAN),
        Language("bn", "Bengali", "বাংলা", "Bengali", false, SUPPORTED, false, Region.INDIAN),
        Language("ta", "Tamil", "தமிழ்", "Tamil", false, SUPPORTED, false, Region.INDIAN),
        Language("te", "Telugu", "తెలుగు", "Telugu", false, SUPPORTED, false, Region.INDIAN),
        Language("kn", "Kannada", "ಕನ್ನಡ", "Kannada", false, SUPPORTED, false, Region.INDIAN),
        Language("mr", "Marathi", "मराठी", "Devanagari", false, SUPPORTED, false, Region.INDIAN),
        Language("gu", "Gujarati", "ગુજરાતી", "Gujarati", false, SUPPORTED, false, Region.INDIAN),
        Language("ur", "Urdu", "اردو", "Arabic", true, SUPPORTED, false, Region.INDIAN),
        Language("ml", "Malayalam", "മലയാളം", "Malayalam", false, NO, false, Region.INDIAN),
        Language("or", "Odia", "ଓଡ଼ିଆ", "Odia", false, NO, false, Region.INDIAN),
        Language("pa", "Punjabi", "ਪੰਜਾਬੀ", "Gurmukhi", false, NO, false, Region.INDIAN),
        Language("as", "Assamese", "অসমীয়া", "Bengali", false, NO, false, Region.INDIAN),
        Language("ne", "Nepali", "नेपाली", "Devanagari", false, NO, false, Region.INDIAN),
        Language("sa", "Sanskrit", "संस्कृतम्", "Devanagari", false, NO, false, Region.INDIAN),
        Language("kok", "Konkani", "कोंकणी", "Devanagari", false, NO, false, Region.INDIAN),
        Language("sd", "Sindhi", "سنڌي", "Arabic", true, NO, false, Region.INDIAN),
        Language("ks", "Kashmiri", "کٲشُر", "Arabic", true, NO, false, Region.INDIAN),
        Language("mni", "Manipuri", "মৈতৈলোন্", "Bengali", false, NO, false, Region.INDIAN),
        Language("brx", "Bodo", "बड़ो", "Devanagari", false, NO, false, Region.INDIAN),
        Language("doi", "Dogri", "डोगरी", "Devanagari", false, NO, false, Region.INDIAN),
        Language("mai", "Maithili", "मैथिली", "Devanagari", false, NO, false, Region.INDIAN),
        Language("sat", "Santali", "ᱥᱟᱱᱛᱟᱲᱤ", "Ol Chiki", false, NO, false, Region.INDIAN),

        // ---------------- International languages ----------------
        Language("es", "Spanish", "Español", "Latin", false, SUPPORTED, true, Region.INTERNATIONAL),
        Language("fr", "French", "Français", "Latin", false, SUPPORTED, true, Region.INTERNATIONAL),
        Language("de", "German", "Deutsch", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("it", "Italian", "Italiano", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("pt", "Portuguese", "Português", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("ja", "Japanese", "日本語", "Japanese", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("ko", "Korean", "한국어", "Hangul", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("zh", "Chinese (Mandarin)", "中文", "Han", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("ar", "Arabic", "العربية", "Arabic", true, SUPPORTED, false, Region.INTERNATIONAL),
        Language("ru", "Russian", "Русский", "Cyrillic", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("tr", "Turkish", "Türkçe", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("nl", "Dutch", "Nederlands", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("sv", "Swedish", "Svenska", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("no", "Norwegian", "Norsk", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("da", "Danish", "Dansk", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("fi", "Finnish", "Suomi", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("pl", "Polish", "Polski", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("uk", "Ukrainian", "Українська", "Cyrillic", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("el", "Greek", "Ελληνικά", "Greek", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("he", "Hebrew", "עברית", "Hebrew", true, SUPPORTED, false, Region.INTERNATIONAL),
        Language("th", "Thai", "ไทย", "Thai", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("vi", "Vietnamese", "Tiếng Việt", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("id", "Indonesian", "Bahasa Indonesia", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("ms", "Malay", "Bahasa Melayu", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("ga", "Irish", "Gaeilge", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("cy", "Welsh", "Cymraeg", "Latin", false, SUPPORTED, false, Region.INTERNATIONAL),
        Language("la", "Latin", "Latina", "Latin", false, NO, false, Region.INTERNATIONAL)
    )

    private val byCode = languages.associateBy { it.code }

    fun byCode(code: String): Language? = byCode[code]

    fun requireByCode(code: String): Language =
        byCode[code] ?: error("Unknown language code: $code")

    fun search(query: String): List<Language> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return languages
        return languages.filter {
            it.englishName.lowercase().contains(q) ||
                it.nativeName.lowercase().contains(q) ||
                it.code.contains(q)
        }
    }

    /** Languages that are both translatable and have a shipped course. */
    val learnable: List<Language> get() = languages.filter { it.courseAvailable }

    val translatable: List<Language> get() = languages.filter { it.translateSupported }

    val indian: List<Language> get() = languages.filter { it.region == Region.INDIAN }

    val international: List<Language> get() = languages.filter { it.region == Region.INTERNATIONAL }
}
