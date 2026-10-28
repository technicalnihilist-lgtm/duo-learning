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

    fun setSource(code: String) = _state.update { it.copy(source = code, result = null) }
    fun setTarget(code: String) = _state.update { it.copy(target = code, result = null) }
    fun setText(value: String) = _state.update { it.copy(text = value, savedJustNow = false) }

    fun swap() = _state.update {
        val newSource = it.target
        val newTarget = if (it.source == "auto") (it.detected ?: "en") else it.source
        it.copy(source = newSource, target = newTarget, text = it.resultText ?: it.text, result = null)
    }

    fun clear() = _state.update { it.copy(text = "", result = null, savedJustNow = false) }

    fun append(text: String) = _state.update { it.copy(text = it.text + text) }

    /**
     * Kick off a translation. Any in-flight request for a previous input or
     * language pair is cancelled first, so results can never arrive out of
     * order or reflect stale settings.
     */
    fun translate() {
        val s = _state.value
        if (s.text.isBlank()) return
        translateJob?.cancel()
        _state.update { it.copy(loading = true, result = null, savedJustNow = false) }
        translateJob = viewModelScope.launch {
            if (s.source == "auto") {
                val detected = translation.detect(s.text)
                _state.update { it.copy(detected = detected) }
            } else {
                _state.update { it.copy(detected = s.source) }
            }
            val result = translation.translate(s.text, s.source, s.target)
            _state.update { it.copy(loading = false, result = result) }
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
