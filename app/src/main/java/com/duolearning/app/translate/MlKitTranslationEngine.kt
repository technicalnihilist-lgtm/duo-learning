package com.duolearning.app.translate

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device translation via Google ML Kit.
 *
 * Why this engine:
 *  - free and unlimited, with no API key and no server to run;
 *  - runs fully on-device once a language model is downloaded, so it works in
 *    airplane mode (satisfying the offline requirement);
 *  - no user text ever leaves the device, which satisfies the privacy rules.
 *
 * The trade-off is a fixed language list and per-language model downloads
 * (roughly 15-40 MB each). [supportedLanguageCodes] mirrors that list exactly,
 * and the UI reports unsupported languages honestly.
 */
class MlKitTranslationEngine : TranslationEngine {

    override val supportedLanguageCodes: Set<String> = setOf(
        "af", "ar", "be", "bg", "bn", "ca", "cs", "cy", "da", "de", "el", "en",
        "eo", "es", "et", "fa", "fi", "fr", "ga", "gl", "gu", "he", "hi", "hr",
        "ht", "hu", "id", "is", "it", "ja", "ka", "kn", "ko", "lt", "lv", "mk",
        "mr", "ms", "mt", "nl", "no", "pl", "pt", "ro", "ru", "sk", "sl", "sq",
        "sr", "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "ur", "vi", "zh"
    )

    /** Small LRU-ish cache of open translators. Kept tiny to bound RAM. */
    private val clients = ConcurrentHashMap<String, Translator>()
    private val clientLock = Mutex()
    private val maxClients = 3

    private val modelManager = RemoteModelManager.getInstance()

    override suspend fun translate(
        text: String,
        source: String,
        target: String
    ): TranslationResult = withContext(Dispatchers.Default) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult.Failure(
                TranslationError.EMPTY_INPUT, "There is nothing to translate."
            )
        }
        if (source != "auto" && !isSupported(source)) {
            return@withContext TranslationResult.Failure(
                TranslationError.UNSUPPORTED_LANGUAGE_PAIR,
                "The source language is not supported by the on-device engine."
            )
        }
        if (!isSupported(target)) {
            return@withContext TranslationResult.Failure(
                TranslationError.UNSUPPORTED_LANGUAGE_PAIR,
                "The target language is not supported by the on-device engine."
            )
        }

        val translator = try {
            getOrCreateTranslator(source, target)
        } catch (t: Throwable) {
            return@withContext TranslationResult.Failure(
                TranslationError.ENGINE_ERROR, t.message ?: "Could not start the engine."
            )
        }

        try {
            translator.translate(trimmed).await()
                .let { TranslationResult.Success(it) }
        } catch (t: Throwable) {
            // ML Kit raises a FirebaseMLException subclass when the model is
            // missing or the download failed.
            val msg = t.message ?: "Translation failed."
            val error = if (msg.contains("download", ignoreCase = true) ||
                msg.contains("model", ignoreCase = true)
            ) TranslationError.MODEL_DOWNLOAD_FAILED else TranslationError.ENGINE_ERROR
            TranslationResult.Failure(error, msg)
        }
    }

    override suspend fun ensureModel(code: String): ModelDownloadState {
        if (!isSupported(code)) {
            return ModelDownloadState.Failed("Language not supported by this engine.")
        }
        return try {
            val already = isModelDownloaded(code)
            // Download over Wi-Fi + mobile data is allowed; users explicitly
            // asked to prepare a language, so requiring Wi-Fi would be a
            // surprise. Progress is coarse because ML Kit does not report
            // byte-level progress.
            val conditions = DownloadConditions.Builder().build()
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage("en")
                    .setTargetLanguage(code)
                    .build()
            ).use { client ->
                client.downloadModelIfNeeded(conditions).await()
            }
            if (already) ModelDownloadState.AlreadyDownloaded else ModelDownloadState.Done
        } catch (t: Throwable) {
            ModelDownloadState.Failed(t.message ?: "Model download failed.")
        }
    }

    override suspend fun isModelDownloaded(code: String): Boolean = try {
        val models = modelManager.getDownloadedModels(TranslateRemoteModel::class.java).await()
        models.any { it.language == code }
    } catch (t: Throwable) {
        false
    }

    override suspend fun deleteModel(code: String) {
        try {
            modelManager.deleteDownloadedModel(TranslateRemoteModel.Builder(code).build()).await()
        } catch (_: Throwable) {
            // Nothing to delete; ignore.
        }
    }

    private suspend fun getOrCreateTranslator(source: String, target: String): Translator =
        clientLock.withLock {
            val key = "$source-$target"
            clients[key]?.let { return@withLock it }
            if (clients.size >= maxClients) {
                // Evict the oldest entry to keep resident memory bounded.
                val first = clients.keys.firstOrNull()
                if (first != null) {
                    runCatching { clients.remove(first)?.close() }
                }
            }
            val created = Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build()
            )
            clients[key] = created
            created
        }

    /** Release all cached translators; call when the app goes to background. */
    suspend fun release() = clientLock.withLock {
        clients.values.forEach { runCatching { it.close() } }
        clients.clear()
    }
}

/** Await a Play Services [Task] without pulling in an extra dependency. */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
    addOnSuccessListener { if (cont.isActive) cont.resume(it) }
    addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
    addOnCanceledListener { if (cont.isActive) cont.cancel() }
}
