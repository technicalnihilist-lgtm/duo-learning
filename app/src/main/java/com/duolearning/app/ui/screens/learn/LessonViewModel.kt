package com.duolearning.app.ui.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.data.content.SeedContent
import com.duolearning.app.data.model.ExerciseSpec
import com.duolearning.app.data.model.Language
import com.duolearning.app.data.model.Lesson
import com.duolearning.app.data.repository.ActivityResult
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.domain.AnswerValidator
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LessonUiState(
    val loading: Boolean = true,
    val courseCode: String = "",
    val lessonTitle: String = "",
    val isCheckpoint: Boolean = false,
    val courseLanguage: Language? = null,
    val nativeLanguage: Language? = null,
    val index: Int = 0,
    val total: Int = 0,
    val exercise: ExerciseSpec? = null,
    val options: List<String> = emptyList(),
    val bank: List<String> = emptyList(),
    val rightColumn: List<String> = emptyList(),
    // user answer
    val textAnswer: String = "",
    val selectedIndex: Int = -1,
    val selectedTokens: List<String> = emptyList(),
    val matches: Map<String, String> = emptyMap(),
    val selectedLeft: String? = null,
    // progress
    val checked: Boolean = false,
    val isCorrect: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val result: ActivityResult? = null,
    val nextLessonId: String? = null,
    val error: String? = null
) {
    val accuracy: Int get() = if (total == 0) 0 else (correctCount * 100) / total
}

class LessonViewModel(
    private val learning: LearningRepository,
    private val courseCode: String,
    private val lessonId: String
) : ViewModel() {

    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    private var lesson: Lesson? = null
    private var orderedLessonIds: List<String> = emptyList()

    init { load() }

    private fun load() = viewModelScope.launch {
        val course = SeedContent.courseFor(courseCode)
        val found = course?.units?.flatMap { it.lessons }?.firstOrNull { it.id == lessonId }
        if (course == null || found == null) {
            _state.update { it.copy(loading = false, error = "Lesson not found.") }
            return@launch
        }
        lesson = found
        orderedLessonIds = course.units.flatMap { u -> u.lessons.map { it.id } }
        learning.ensureVocabSeeded(courseCode)
        val settings = learning.currentSettings()
        _state.update {
            it.copy(
                loading = false,
                courseCode = courseCode,
                lessonTitle = found.title,
                isCheckpoint = found.kind == Lesson.LessonKind.CHECKPOINT,
                courseLanguage = LanguageCatalog.byCode(courseCode),
                nativeLanguage = LanguageCatalog.byCode(settings.nativeLanguage),
                total = found.exercises.size,
                index = 0
            )
        }
        prepareExercise(0)
    }

    private fun prepareExercise(index: Int) {
        val found = lesson ?: return
        val ex = found.exercises.getOrNull(index) ?: return
        val seed = ("$lessonId-$index").hashCode().toLong()
        val rnd = Random(seed)
        val options = when (ex) {
            is ExerciseSpec.MultipleChoice -> ex.options.shuffled(rnd)
            is ExerciseSpec.FillBlank -> ex.options.shuffled(rnd)
            is ExerciseSpec.Listening -> ex.options.shuffled(rnd)
            is ExerciseSpec.Correction -> listOf(ex.wrong, ex.correct).shuffled(rnd)
            else -> emptyList()
        }
        val bank = (ex as? ExerciseSpec.Scrambled)?.tokens?.shuffled(rnd) ?: emptyList()
        val right = (ex as? ExerciseSpec.MatchPairs)?.pairs?.map { it.second }?.shuffled(rnd) ?: emptyList()
        _state.update {
            it.copy(
                index = index,
                exercise = ex,
                options = options,
                bank = bank,
                rightColumn = right,
                textAnswer = "",
                selectedIndex = -1,
                selectedTokens = emptyList(),
                matches = emptyMap(),
                selectedLeft = null,
                checked = false,
                isCorrect = false
            )
        }
    }

    fun setTextAnswer(value: String) = _state.update { it.copy(textAnswer = value) }

    fun selectOption(index: Int) = _state.update {
        if (it.checked) it else it.copy(selectedIndex = index)
    }

    fun toggleToken(token: String) = _state.update { s ->
        if (s.checked) return@update s
        val selected = s.selectedTokens.toMutableList()
        if (selected.contains(token)) selected.remove(token) else selected.add(token)
        s.copy(selectedTokens = selected)
    }

    fun selectLeft(token: String) = _state.update { s ->
        if (s.checked) return@update s
        s.copy(selectedLeft = if (s.selectedLeft == token) null else token)
    }

    fun matchRight(token: String) = _state.update { s ->
        if (s.checked) return@update s
        val left = s.selectedLeft ?: return@update s
        // A right item can only be used once.
        val cleaned = s.matches.filterValues { it != token }
        s.copy(matches = cleaned + (left to token), selectedLeft = null)
    }

    /** Evaluate the current answer and lock the exercise. */
    fun check() {
        val s = _state.value
        val ex = s.exercise ?: return
        if (s.checked) return
        val correct = evaluate(ex, s)
        _state.update {
            it.copy(
                checked = true,
                isCorrect = correct,
                correctCount = it.correctCount + if (correct) 1 else 0
            )
        }
    }

    private fun evaluate(ex: ExerciseSpec, s: LessonUiState): Boolean = when (ex) {
        is ExerciseSpec.MultipleChoice ->
            s.options.getOrNull(s.selectedIndex) == ex.options[ex.correctIndex]
        is ExerciseSpec.FillBlank ->
            s.options.getOrNull(s.selectedIndex) == ex.options[ex.correctIndex]
        is ExerciseSpec.Listening ->
            s.options.getOrNull(s.selectedIndex) == ex.options[ex.correctIndex]
        is ExerciseSpec.Correction ->
            s.options.getOrNull(s.selectedIndex) == ex.correct
        is ExerciseSpec.TypedTranslation ->
            AnswerValidator.matchesTyped(s.textAnswer, ex.accepted)
        is ExerciseSpec.Scrambled ->
            AnswerValidator.matchesSentence(s.selectedTokens.joinToString(" "), ex.correct)
        is ExerciseSpec.MatchPairs -> {
            val expected = ex.pairs.toMap()
            s.matches.size == ex.pairs.size && s.matches.all { (k, v) -> expected[k] == v }
        }
    }

    fun next() {
        val s = _state.value
        if (!s.checked) return
        val nextIndex = s.index + 1
        if (nextIndex < s.total) {
            prepareExercise(nextIndex)
        } else {
            finish()
        }
    }

    private fun finish() = viewModelScope.launch {
        val s = _state.value
        val found = lesson ?: return@launch
        val result = learning.recordLessonResult(
            courseCode = courseCode,
            lessonId = lessonId,
            correct = s.correctCount,
            total = s.total,
            isCheckpoint = s.isCheckpoint,
            qualifying = true
        )
        // Feed lesson vocabulary into the spaced-repetition schedule.
        val passed = result.passed
        found.vocab.forEach { learning.reviewVocab(it.id, passed) }

        val nextId = orderedLessonIds.getOrNull(orderedLessonIds.indexOf(lessonId) + 1)
        _state.update { it.copy(finished = true, result = result, nextLessonId = nextId) }
    }

    companion object {
        fun factory(course: String, lessonId: String) = duoFactory { c ->
            LessonViewModel(c.learningRepository, course, lessonId)
        }
    }
}
