package com.duolearning.app.ui.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.duolearning.app.audio.SpeakOutcome
import com.duolearning.app.ui.LocalContainer

/**
 * A "speak" action that explains itself.
 *
 * Every speak button in the app used to ignore the result of
 * [com.duolearning.app.audio.TtsManager.speak], so with sound switched off, a
 * voice still initialising, or no speech data for the language, the button
 * looked broken. This helper surfaces the reason as a short toast.
 */
@Composable
fun rememberSpeaker(): (String, String) -> Unit {
    val context = LocalContext.current
    val tts = LocalContainer.current.tts
    return remember(context, tts) {
        { text: String, languageCode: String ->
            val message: String? = when (tts.speak(text, languageCode)) {
                SpeakOutcome.SPOKEN, SpeakOutcome.EMPTY -> null
                SpeakOutcome.MUTED ->
                    "Pronunciation audio is off. Turn it on in Settings."
                SpeakOutcome.NOT_READY ->
                    "The voice is still starting up. Try again in a moment."
                SpeakOutcome.NO_VOICE ->
                    "No voice is installed for this language. Add speech data in Android's text-to-speech settings."
            }
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
