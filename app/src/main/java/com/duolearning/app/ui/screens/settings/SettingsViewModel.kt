package com.duolearning.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.prefs.SelectionBus
import com.duolearning.app.data.prefs.UserPreferences
import com.duolearning.app.data.prefs.UserSettings
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.repository.TranslationRepository
import com.duolearning.app.ui.duoFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: UserPreferences,
    private val learning: LearningRepository,
    private val translation: TranslationRepository,
    private val selectionBus: SelectionBus
) : ViewModel() {

    val settings: StateFlow<UserSettings> = prefs.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    init {
        viewModelScope.launch {
            selectionBus.picks.collect { picks ->
                picks["native"]?.let { code ->
                    prefs.setNativeLanguage(code)
                    selectionBus.consume("native")
                }
                picks["learn"]?.let { code ->
                    prefs.setActiveCourse(code)
                    learning.ensureVocabSeeded(code)
                    selectionBus.consume("learn")
                }
            }
        }
    }

    fun setGoal(xp: Int) = viewModelScope.launch { prefs.setDailyGoal(xp) }
    fun setSpeechRate(rate: Float) = viewModelScope.launch { prefs.setSpeechRate(rate) }
    fun setSound(on: Boolean) = viewModelScope.launch { prefs.setSoundEnabled(on) }
    fun setTextScale(scale: Float) = viewModelScope.launch { prefs.setTextScale(scale) }

    fun resetProgress(onDone: () -> Unit) = viewModelScope.launch {
        learning.resetProgress()
        translation.clearHistory()
        translation.clearSaved()
        onDone()
    }

    fun clearHistory() = viewModelScope.launch { translation.clearHistory() }

    companion object {
        val Factory = duoFactory { c ->
            SettingsViewModel(c.preferences, c.learningRepository, c.translationRepository, c.selectionBus)
        }
    }
}
