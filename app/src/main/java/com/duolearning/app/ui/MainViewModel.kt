package com.duolearning.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duolearning.app.data.repository.LearningRepository
import com.duolearning.app.data.prefs.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val learning: LearningRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = learning.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    val textScale: StateFlow<Float> = learning.settings
        .map { it.textScale }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1f)

    companion object {
        val Factory = duoFactory { container -> MainViewModel(container.learningRepository) }
    }
}
