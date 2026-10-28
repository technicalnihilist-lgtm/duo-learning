package com.duolearning.app.translate

/** Outcome of a translation request. Failures are explicit, never faked. */
sealed interface TranslationResult {
    data class Success(val text: String, val fromCache: Boolean = false) : TranslationResult
    data class Failure(val reason: TranslationError, val message: String) : TranslationResult
}

enum class TranslationError {
    EMPTY_INPUT,
    UNSUPPORTED_LANGUAGE_PAIR,
    MODEL_DOWNLOAD_FAILED,
    NETWORK_REQUIRED,
    ENGINE_ERROR,
    CANCELLED
}

/** Progress of downloading an on-device language model. */
sealed interface ModelDownloadState {
    data object AlreadyDownloaded : ModelDownloadState
    data object Downloading : ModelDownloadState
    data object Done : ModelDownloadState
    data class Failed(val message: String) : ModelDownloadState
}

/**
 * A translation backend. The app ships one implementation backed by Google
 * ML Kit's on-device models; the interface keeps the UI decoupled from the
 * engine and makes it easy to add another provider later.
 */
interface TranslationEngine {

    /** Language codes this engine can translate (source and target). */
    val supportedLanguageCodes: Set<String>

    fun isSupported(code: String): Boolean = code in supportedLanguageCodes

    /**
     * Translate [text] from [source] to [target]. [source] may be "auto" when
     * the engine supports detection; otherwise it must be a concrete code.
     * Implementations must return a [TranslationResult.Failure] rather than
     * inventing output.
     */
    suspend fun translate(text: String, source: String, target: String): TranslationResult

    /** Ensure a model for [code] is present locally; downloads if needed. */
    suspend fun ensureModel(code: String): ModelDownloadState

    /** Whether the model for [code] is already on the device. */
    suspend fun isModelDownloaded(code: String): Boolean

    /** Delete a downloaded model to reclaim storage. */
    suspend fun deleteModel(code: String)
}
