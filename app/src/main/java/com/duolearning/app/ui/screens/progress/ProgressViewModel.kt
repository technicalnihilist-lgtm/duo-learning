package com.duolearning.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.local.entity.DailyActivityEntity
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.repository.ProgressStats
import com.duolearning.app.domain.Achievements
import com.duolearning.app.domain.Dates
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AchievementRow(
    val id: String,
    val title: String,
    val description: String,
    val earned: Boolean
)

data class ProgressUiState(
    val stats: ProgressStats = ProgressStats(),
    val xpToday: Int = 0,
    val activity: List<DailyActivityEntity> = emptyList(),
    val achievements: List<AchievementRow> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(
    private val learning: LearningRepository
) : ViewModel() {

    private val statsFlow = learning.settings.flatMapLatest { s ->
        learning.observeProgressStats(s.activeCourse)
    }

    val state: StateFlow<ProgressUiState> = combine(
        statsFlow,
        learning.observeXpToday(),
        learning.observeDailyActivity(),
        learning.observeAchievements()
    ) { stats, xpToday, activity, earned ->
        val earnedIds = earned.map { it.id }.toSet()
        ProgressUiState(
            stats = stats,
            xpToday = xpToday,
            activity = activity,
            achievements = Achievements.all.map { def ->
                AchievementRow(def.id, def.title, def.description, def.id in earnedIds)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    /** Last [days] calendar days as a set of epoch-days that qualified. */
    fun calendarWindow(days: Int = 35): List<Long> {
        val today = Dates.todayEpochDay()
        return (0 until days).map { today - (days - 1 - it) }
    }

    companion object {
        val Factory = duoFactory { c -> ProgressViewModel(c.learningRepository) }
    }
}
