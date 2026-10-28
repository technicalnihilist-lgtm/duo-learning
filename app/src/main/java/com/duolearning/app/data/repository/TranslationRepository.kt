package com.duolearning.app.data.repository

import com.duolearning.app.data.local.DuoDatabase
import com.duolearning.app.data.local.entity.SavedTranslationEntity
import com.duolearning.app.data.local.entity.TranslationHistoryEntity
import com.duolearning.app.translate.LanguageIdentifier
import com.duolearning.app.translate.TranslationEngine
import com.duolearning.app.translate.TranslationError
import com.duolearning.app.translate.TranslationResult

class TranslationRepository(
    private val db: DuoDatabase,
    private val engine: TranslationEngine,
    private val identifier: LanguageIdentifier
) {
    companion object {
        private const val HISTORY_CAP = 300
    }

    fun observeHistory() = db.translationDao().history(HISTORY_CAP)
    fun observeSaved() = db.translationDao().saved()

    suspend fun detect(text: String): String? = identifier.detect(text)

    /**
     * Translate, using an exact (source, target, text) cache hit when present.
     * Never writes a fabricated result on failure.
     */
    suspend fun translate(text: String, source: String, target: String): TranslationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return TranslationResult.Failure(TranslationError.EMPTY_INPUT, "Nothing to translate.")
        }
        val effectiveSource = if (source == "auto") (identifier.detect(trimmed) ?: "auto") else source

        if (effectiveSource != "auto" && effectiveSource == target) {
            // Same language: echoing input would look like a fake success.
            return TranslationResult.Failure(
                TranslationError.UNSUPPORTED_LANGUAGE_PAIR,
                "Source and target languages are the same."
            )
        }

        // Exact-match cache (same text and same pair only).
        val cached = db.translationDao().cached(effectiveSource, target, trimmed)
        if (cached != null) {
            return TranslationResult.Success(cached.targetText, fromCache = true)
        }

        val result = engine.translate(trimmed, effectiveSource, target)
        if (result is TranslationResult.Success) {
            val now = System.currentTimeMillis()
            db.translationDao().insertHistory(
                TranslationHistoryEntity(
                    sourceLang = effectiveSource,
                    targetLang = target,
                    sourceText = trimmed,
                    targetText = result.text,
                    createdAt = now
                )
            )
            db.translationDao().trimHistory(HISTORY_CAP)
        }
        return result
    }

    suspend fun saveTranslation(
        source: String, target: String, sourceText: String, targetText: String
    ): Long = db.translationDao().insertSaved(
        SavedTranslationEntity(
            sourceLang = source, targetLang = target,
            sourceText = sourceText, targetText = targetText,
            createdAt = System.currentTimeMillis()
        )
    )

    suspend fun deleteSaved(id: Long) = db.translationDao().deleteSaved(id)
    suspend fun setSavedFavourite(id: Long, fav: Boolean) = db.translationDao().setSavedFavourite(id, fav)
    suspend fun deleteHistory(id: Long) = db.translationDao().deleteHistory(id)
    suspend fun clearHistory() = db.translationDao().clearHistory()
    suspend fun clearSaved() = db.translationDao().clearSaved()

    suspend fun isModelDownloaded(code: String) = engine.isModelDownloaded(code)
    suspend fun ensureModel(code: String) = engine.ensureModel(code)
    suspend fun deleteModel(code: String) = engine.deleteModel(code)
    fun isSupported(code: String) = engine.isSupported(code)
}
