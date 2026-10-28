package com.duolearning.app.ui.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.repository.ActivityResult
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.repository.TranslationRepository
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PracticeMode { FLASHCARDS, DIFFICULT, SAVED_TRANSLATIONS }

data class PracticeCard(
    val id: String,
    val front: String,
    val back: String,
    val frontLang: String,
    val backLang: String,
    val hint: String = "",
    val isVocab: Boolean
)

data class PracticeUiState(
    val active: Boolean = false,
    val mode: PracticeMode = PracticeMode.FLASHCARDS,
    val courseCode: String = "hi",
    val cards: List<PracticeCard> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val correct: Int = 0,
    val finished: Boolean = false,
    val result: ActivityResult? = null,
    val emptyMessage: String? = null
) {
    val current: PracticeCard? get() = cards.getOrNull(index)
    val total: Int get() = cards.size
}

class PracticeViewModel(
    private val learning: LearningRepository,
    private val translation: TranslationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    val savedCount: StateFlow<Int> = translation.observeSaved()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val settings = learning.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            com.duolearning.app.data.prefs.UserSettings())

    fun start(mode: PracticeMode) = viewModelScope.launch {
        val s = learning.currentSettings()
        learning.ensureVocabSeeded(s.activeCourse)
        val cards: List<PracticeCard> = when (mode) {
            PracticeMode.FLASHCARDS -> {
                val due = learning.dueVocabNow(s.activeCourse, 20)
                    .ifEmpty { learning.allVocabNow(s.activeCourse, 15) }
                due.map { v ->
                    PracticeCard(
                        id = v.vocabId, front = v.term, back = v.translation,
                        frontLang = s.activeCourse, backLang = s.nativeLanguage,
                        hint = v.exampleTarget, isVocab = true
                    )
                }
            }
            PracticeMode.DIFFICULT -> {
                val all = learning.allVocabNow(s.activeCourse, 100)
                    .filter { it.wrong > it.correct }
                    .sortedByDescending { it.wrong - it.correct }
                    .take(20)
                all.map { v ->
                    PracticeCard(
                        id = v.vocabId, front = v.term, back = v.translation,
                        frontLang = s.activeCourse, backLang = s.nativeLanguage,
                        hint = v.exampleTarget, isVocab = true
                    )
                }
            }
            PracticeMode.SAVED_TRANSLATIONS -> {
                val saved = translation.observeSaved().first()
                saved.map { t ->
                    PracticeCard(
                        id = "saved-${t.id}", front = t.sourceText, back = t.targetText,
                        frontLang = t.sourceLang, backLang = t.targetLang,
                        isVocab = false
                    )
                }
            }
        }
        _state.value = PracticeUiState(
            active = cards.isNotEmpty(),
            mode = mode,
            courseCode = s.activeCourse,
            cards = cards,
            emptyMessage = if (cards.isEmpty()) emptyMessageFor(mode) else null
        )
    }

    private fun emptyMessageFor(mode: PracticeMode) = when (mode) {
        PracticeMode.FLASHCARDS -> "No vocabulary to review yet. Finish a lesson first."
        PracticeMode.DIFFICULT -> "No tricky words recorded yet — nice work."
        PracticeMode.SAVED_TRANSLATIONS -> "You haven't saved any translations yet."
    }

    fun reveal() = _state.update { it.copy(revealed = true) }

    /** Grade the current card; advances to the next or finishes the session. */
    fun grade(correct: Boolean) = viewModelScope.launch {
        val s = _state.value
        val card = s.current ?: return@launch
        if (card.isVocab) learning.reviewVocab(card.id, correct)

        val nextIndex = s.index + 1
        val newCorrect = s.correct + if (correct) 1 else 0
        if (nextIndex >= s.cards.size) {
            val result = learning.recordLessonResult(
                courseCode = s.courseCode,
                lessonId = null,
                correct = newCorrect,
                total = s.cards.size,
                isCheckpoint = false,
                qualifying = true
            )
            _state.update {
                it.copy(correct = newCorrect, finished = true, result = result)
            }
        } else {
            _state.update { it.copy(index = nextIndex, correct = newCorrect, revealed = false) }
        }
    }

    fun exit() { _state.value = PracticeUiState() }

    companion object {
        val Factory = duoFactory { c ->
            PracticeViewModel(c.learningRepository, c.translationRepository)
        }
    }
}
