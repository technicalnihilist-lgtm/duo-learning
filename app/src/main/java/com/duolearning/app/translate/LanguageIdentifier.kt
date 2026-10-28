package com.duolearning.app.translate

import com.google.android.gms.tasks.Task
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device language detection via ML Kit's language-id model.
 * Returns a BCP-47 code or null when the model is unsure.
 */
class LanguageIdentifier {

    private val identifier = LanguageIdentification.getClient()

    suspend fun detect(text: String): String? = withContext(Dispatchers.Default) {
        if (text.trim().length < 2) return@withContext null
        try {
            identifier.identifyLanguage(text).await().takeIf { it != "und" }
        } catch (t: Throwable) {
            null
        }
    }

    fun close() {
        runCatching { identifier.close() }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
    addOnSuccessListener { if (cont.isActive) cont.resume(it) }
    addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
    addOnCanceledListener { if (cont.isActive) cont.cancel() }
}
