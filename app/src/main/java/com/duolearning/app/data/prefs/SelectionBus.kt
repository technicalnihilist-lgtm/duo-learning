package com.duolearning.app.data.prefs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Tiny in-process channel used to return a language choice from the picker
 * screen to whichever screen opened it. Each mode ("learn", "source",
 * "target", "native") holds at most one pending pick that the consumer
 * clears once applied, so a selection is never applied twice.
 */
class SelectionBus {
    private val _picks = MutableStateFlow<Map<String, String>>(emptyMap())
    val picks: StateFlow<Map<String, String>> = _picks.asStateFlow()

    fun publish(mode: String, code: String) {
        _picks.update { it + (mode to code) }
    }

    fun consume(mode: String) {
        _picks.update { it - mode }
    }
}
