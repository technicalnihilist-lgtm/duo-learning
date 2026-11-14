package com.duolearning.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.data.content.SeedContent
import com.duolearning.app.data.model.Course
import com.duolearning.app.data.model.Language
import com.duolearning.app.data.prefs.SelectionBus
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.prefs.UserPreferences
import com.duolearning.app.domain.Dates
import com.duolearning.app.domain.StreakCalculator
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LessonRow(
    val id: String,
    val title: String,
    val kind: String,
    val completed: Boolean,
    val unlocked: Boolean,
    val bestScore: Int
)

data class UnitRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val lessons: List<LessonRow>
) {
    val completed: Int get() = lessons.count { it.completed }
}

data class HomeUiState(
    val loading: Boolean = true,
    val courseCode: String = "hi",
    val courseLanguage: Language? = null,
    val nativeLanguage: Language? = null,
    val hasCourse: Boolean = false,
    val streak: Int = 0,
    val xpToday: Int = 0,
    val goal: Int = 50,
    val totalXp: Int = 0,
    val completedLessons: Int = 0,
    val totalLessons: Int = 0,
    val nextLessonId: String? = null,
    val units: List<UnitRow> = emptyList(),
    val weekDone: List<Boolean> = emptyList(),
    val weekLabels: List<String> = emptyList(),
    val todayIndex: Int = 6,
    val depthEffect: Boolean = true,
    val parallax: Boolean = true,
    val effectStrength: Float = 0.6f
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val learning: LearningRepository,
    private val prefs: UserPreferences,
    private val selectionBus: SelectionBus
) : ViewModel() {

    private val settings = learning.settings

    init {
        viewModelScope.launch {
            selectionBus.picks.collect { picks ->
                picks["learn"]?.let { code ->
                    prefs.setActiveCourse(code)
                    learning.ensureVocabSeeded(code)
                    selectionBus.consume("learn")
                }
            }
        }
    }

    private val lessonsFlow = settings.flatMapLatest { s ->
        learning.observeLessonProgress(s.activeCourse)
    }

    val state: StateFlow<HomeUiState> = combine(
        settings,
        learning.observeXpToday(),
        learning.observeTotalXp(),
        lessonsFlow,
        learning.observeDailyActivity()
    ) { s, xpToday, totalXp, lessons, activity ->
        buildState(
            s.activeCourse, s.nativeLanguage, s.dailyGoalXp,
            s.currentStreak, s.lastQualifiedDay, xpToday, totalXp, lessons,
            activity.filter { it.qualified }.map { it.epochDay }.toSet(),
            s.depthEffect, s.parallax, s.effectStrength
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun buildState(
        courseCode: String,
        nativeCode: String,
        goal: Int,
        currentStreak: Int,
        lastQualifiedDay: Long,
        xpToday: Int,
        totalXp: Int,
        lessons: List<com.duolearning.app.data.local.entity.LessonProgressEntity>,
        qualifiedDays: Set<Long> = emptySet(),
        depthEffect: Boolean = true,
        parallax: Boolean = true,
        effectStrength: Float = 0.6f
    ): HomeUiState {
        val course: Course? = SeedContent.courseFor(courseCode)
        val today = Dates.todayEpochDay()
        val completedIds = lessons.filter { it.completed }.associateBy { it.lessonId }
        val scoreById = lessons.associate { it.lessonId to it.bestScore }

        // Sequential unlock across the whole course: the first lesson is
        // always open, and every other lesson unlocks only once the lesson
        // before it (in the previous unit too) is completed.
        var previousCompleted = true
        val unitRows = course?.units?.map { unit ->
            val rows = mutableListOf<LessonRow>()
            unit.lessons.forEach { lesson ->
                rows += LessonRow(
                    id = lesson.id,
                    title = lesson.title,
                    kind = lesson.kind.name,
                    completed = completedIds.containsKey(lesson.id),
                    unlocked = previousCompleted,
                    bestScore = scoreById[lesson.id] ?: 0
                )
                previousCompleted = completedIds.containsKey(lesson.id)
            }
            UnitRow(unit.id, unit.title, unit.subtitle, rows)
        } ?: emptyList()

        val orderedIds = unitRows.flatMap { it.lessons.map { l -> l.id } }
        val nextId = orderedIds.firstOrNull { it !in completedIds.keys }

        return HomeUiState(
            loading = false,
            courseCode = courseCode,
            courseLanguage = LanguageCatalog.byCode(courseCode),
            nativeLanguage = LanguageCatalog.byCode(nativeCode),
            hasCourse = course != null,
            streak = StreakCalculator.displayStreak(currentStreak, lastQualifiedDay, today),
            xpToday = xpToday,
            goal = goal,
            totalXp = totalXp,
            completedLessons = completedIds.size,
            totalLessons = orderedIds.size,
            nextLessonId = nextId,
            units = unitRows,
            weekDone = (6 downTo 0).map { back -> (today - back) in qualifiedDays },
            weekLabels = (6 downTo 0).map { back -> Dates.shortWeekday(today - back) },
            todayIndex = 6,
            depthEffect = depthEffect,
            parallax = parallax,
            effectStrength = effectStrength
        )
    }

    /** Live effect settings, collected by the effects sheet. */
    val fx: StateFlow<com.duolearning.app.data.prefs.UserSettings> =
        learning.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            com.duolearning.app.data.prefs.UserSettings()
        )

    fun setGlass(on: Boolean) = viewModelScope.launch { prefs.setGlassEffect(on) }
    fun setAnimations(on: Boolean) = viewModelScope.launch { prefs.setAnimationsEnabled(on) }
    fun setBlur(v: Float) = viewModelScope.launch { prefs.setBlurPx(v) }
    fun setTransparency(v: Float) = viewModelScope.launch { prefs.setTransparencyPct(v) }
    fun setDepth(v: Float) = viewModelScope.launch { prefs.setDepthPct(v) }
    fun setBounce(v: Float) = viewModelScope.launch { prefs.setBouncePct(v) }
    fun setSpeed(v: Float) = viewModelScope.launch { prefs.setSpeedPct(v) }
    fun resetEffects() = viewModelScope.launch { prefs.resetEffects() }

    companion object {
        val Factory = duoFactory { c ->
            HomeViewModel(c.learningRepository, c.preferences, c.selectionBus)
        }
    }
}
