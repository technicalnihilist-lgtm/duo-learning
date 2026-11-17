package com.duolearning.app.ui.screens.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.local.entity.SavedTranslationEntity
import com.duolearning.app.data.local.entity.TranslationHistoryEntity
import com.duolearning.app.data.prefs.SelectionBus
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.repository.TranslationRepository
import com.duolearning.app.translate.TranslationError
import com.duolearning.app.translate.TranslationResult
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TranslateUiState(
    val source: String = "auto",
    val target: String = "en",
    val text: String = "",
    val result: TranslationResult? = null,
    val loading: Boolean = false,
    val detected: String? = null,
    val savedJustNow: Boolean = false
) {
    val resultText: String? get() = (result as? TranslationResult.Success)?.text
    val errorText: String? get() = (result as? TranslationResult.Failure)?.message
    val isCacheHit: Boolean get() = (result as? TranslationResult.Success)?.fromCache == true
}

class TranslateViewModel(
    private val translation: TranslationRepository,
    private val learning: LearningRepository,
    private val selectionBus: SelectionBus
) : ViewModel() {

    private val _state = MutableStateFlow(TranslateUiState())
    val state: StateFlow<TranslateUiState> = _state.asStateFlow()

    val history: StateFlow<List<TranslationHistoryEntity>> =
        translation.observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val saved: StateFlow<List<SavedTranslationEntity>> =
        translation.observeSaved().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var translateJob: Job? = null

    /**
     * Monotonic id of the newest request. Every result is checked against it
     * before it is written to the UI, so a slow request for old input can
     * never overwrite the answer for what is on screen now.
     */
    private var requestId = 0

    init {
        viewModelScope.launch {
            val s = learning.currentSettings()
            _state.update {
                it.copy(
                    source = s.nativeLanguage,
                    target = if (s.activeCourse != s.nativeLanguage) s.activeCourse else "en"
                )
            }
        }
        // Apply language choices made in the picker screen.
        viewModelScope.launch {
            selectionBus.picks.collect { picks ->
                picks["source"]?.let { code ->
                    setSource(code); selectionBus.consume("source")
                }
                picks["target"]?.let { code ->
                    setTarget(code); selectionBus.consume("target")
                }
            }
        }
    }

    /**
     * Drop the request that is in flight (if any) together with the result it
     * would have produced. Called by every edit that changes what a pending
     * translation was asked for.
     */
    private fun invalidatePending() {
        translateJob?.cancel()
        translateJob = null
        requestId++
    }

    fun setSource(code: String) {
        invalidatePending()
        _state.update { it.copy(source = code, result = null, detected = null, loading = false) }
    }

    fun setTarget(code: String) {
        invalidatePending()
        _state.update { it.copy(target = code, result = null, detected = null, loading = false) }
    }

    /**
     * Editing the input invalidates whatever translation is on screen: keeping
     * it would show a result for text the user has already changed, and would
     * let them save or copy the wrong pairing.
     */
    fun setText(value: String) {
        invalidatePending()
        _state.update {
            it.copy(text = value, result = null, detected = null, savedJustNow = false, loading = false)
        }
    }

    fun swap() {
        invalidatePending()
        _state.update { s ->
            val newSource = s.target
            val detected = s.detected
            // Swapping with the source on "auto" and nothing detected yet left
            // the target as a blind "en", which for an English target produced
            // source == target and made every following Translate fail with
            // "source and target languages are the same".
            val newTarget = when {
                s.source != "auto" -> s.source
                detected != null && detected != newSource -> detected
                newSource == "en" -> "hi"
                else -> "en"
            }
            s.copy(
                source = newSource, target = newTarget,
                text = s.resultText ?: s.text,
                result = null, detected = null, savedJustNow = false, loading = false
            )
        }
    }

    fun clear() {
        invalidatePending()
        _state.update {
            it.copy(text = "", result = null, detected = null, savedJustNow = false, loading = false)
        }
    }

    /**
     * Append pasted text. A separator is added when neither side already has
     * one, so pasting after typed text no longer glues the words together
     * ("hola" + "amigo" used to become "holaamigo").
     */
    fun append(text: String) {
        invalidatePending()
        _state.update { s ->
            val needsSpace = s.text.isNotEmpty() &&
                !s.text.last().isWhitespace() &&
                text.firstOrNull()?.isWhitespace() != true
            s.copy(
                text = s.text + (if (needsSpace) " " else "") + text,
                result = null, detected = null, savedJustNow = false, loading = false
            )
        }
    }

    /**
     * Kick off a translation. Any in-flight request for a previous input or
     * language pair is cancelled first, so results can never arrive out of
     * order or reflect stale settings.
     */
    fun translate() {
        val s = _state.value
        if (s.text.isBlank()) return
        invalidatePending()
        val id = requestId
        _state.update { it.copy(loading = true, result = null, savedJustNow = false) }
        translateJob = viewModelScope.launch {
            try {
                if (s.source == "auto") {
                    val detected = translation.detect(s.text)
                    if (id != requestId) return@launch
                    _state.update { it.copy(detected = detected) }
                } else {
                    _state.update { it.copy(detected = s.source) }
                }
                val result = translation.translate(s.text, s.source, s.target)
                if (id != requestId) return@launch
                _state.update { it.copy(loading = false, result = result) }
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                if (id != requestId) return@launch
                // Never leave the spinner running on an unexpected failure.
                _state.update {
                    it.copy(
                        loading = false,
                        result = TranslationResult.Failure(
                            TranslationError.ENGINE_ERROR,
                            t.message ?: "Translation failed unexpectedly."
                        )
                    )
                }
            }
        }
    }

    /** Save the current successful translation to the vocabulary list. */
    fun saveCurrent() = viewModelScope.launch {
        val s = _state.value
        val text = s.resultText ?: return@launch
        translation.saveTranslation(
            source = if (s.source == "auto") (s.detected ?: "auto") else s.source,
            target = s.target,
            sourceText = s.text,
            targetText = text
        )
        _state.update { it.copy(savedJustNow = true) }
    }

    fun deleteSaved(id: Long) = viewModelScope.launch { translation.deleteSaved(id) }
    fun setSavedFavourite(id: Long, fav: Boolean) = viewModelScope.launch {
        translation.setSavedFavourite(id, fav)
    }
    fun deleteHistory(id: Long) = viewModelScope.launch { translation.deleteHistory(id) }
    fun clearHistory() = viewModelScope.launch { translation.clearHistory() }

    fun supports(code: String): Boolean =
        code == "auto" || translation.isSupported(code)

    override fun onCleared() {
        translateJob?.cancel()
        super.onCleared()
    }

    companion object {
        val Factory = duoFactory { c ->
            TranslateViewModel(c.translationRepository, c.learningRepository, c.selectionBus)
        }
    }
}
