package com.duolearning.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Thin wrapper over Android's [TextToSpeech] engine.
 *
 * Behaviour we care about for a "no dead buttons" experience:
 *  - reports whether the engine finished initialising;
 *  - reports whether a given language actually has a voice installed, so the
 *    UI can tell the user to install speech data instead of silently failing;
 *  - never crashes if the platform engine is missing.
 */
class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val _speaking = MutableStateFlow(false)
    val speaking: StateFlow<Boolean> = _speaking.asStateFlow()

    private var speechRate = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            _ready.value = status == TextToSpeech.SUCCESS
        }.also { engine ->
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { _speaking.value = true }
                override fun onDone(utteranceId: String?) { _speaking.value = false }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { _speaking.value = false }
            })
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(speechRate)
    }

    /** True when the device has a usable voice for [languageCode]. */
    fun hasVoice(languageCode: String): Boolean {
        val engine = tts ?: return false
        val locale = Locale.forLanguageTag(languageCode)
        val availability = engine.isLanguageAvailable(locale)
        return availability == TextToSpeech.LANG_AVAILABLE ||
            availability == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            availability == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }

    /**
     * Speak [text]. Returns false when no engine or no voice is available, so
     * the caller can surface a helpful message rather than doing nothing.
     */
    fun speak(text: String, languageCode: String): Boolean {
        val engine = tts ?: return false
        if (text.isBlank()) return false
        val locale = Locale.forLanguageTag(languageCode)
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            return false
        }
        engine.setSpeechRate(speechRate)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "duo-${System.nanoTime()}")
        return true
    }

    fun stop() {
        tts?.stop()
        _speaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _ready.value = false
    }
}
