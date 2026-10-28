package com.duolearning.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "duo_prefs")

data class UserSettings(
    val nativeLanguage: String = "en",
    val activeCourse: String = "hi",
    val dailyGoalXp: Int = 50,
    val speechRate: Float = 1.0f,
    val soundEnabled: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    /** Local epoch-day of the last qualifying learning activity, or -1. */
    val lastQualifiedDay: Long = -1L,
    val onboarded: Boolean = false,
    val textScale: Float = 1.0f
)

/**
 * Local-only settings and streak counters. No account, no network, no
 * analytics. Backed by DataStore so writes are atomic and survive
 * force-stops.
 */
class UserPreferences(private val context: Context) {

    private object Keys {
        val native = stringPreferencesKey("native_language")
        val course = stringPreferencesKey("active_course")
        val goal = intPreferencesKey("daily_goal_xp")
        val rate = floatPreferencesKey("speech_rate")
        val sound = booleanPreferencesKey("sound_enabled")
        val streak = intPreferencesKey("current_streak")
        val longest = intPreferencesKey("longest_streak")
        val lastDay = longPreferencesKey("last_qualified_day")
        val onboarded = booleanPreferencesKey("onboarded")
        val textScale = floatPreferencesKey("text_scale")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            nativeLanguage = p[Keys.native] ?: "en",
            activeCourse = p[Keys.course] ?: "hi",
            dailyGoalXp = p[Keys.goal] ?: 50,
            speechRate = p[Keys.rate] ?: 1.0f,
            soundEnabled = p[Keys.sound] ?: true,
            currentStreak = p[Keys.streak] ?: 0,
            longestStreak = p[Keys.longest] ?: 0,
            lastQualifiedDay = p[Keys.lastDay] ?: -1L,
            onboarded = p[Keys.onboarded] ?: false,
            textScale = p[Keys.textScale] ?: 1.0f
        )
    }

    suspend fun setNativeLanguage(code: String) = edit { it[Keys.native] = code }
    suspend fun setActiveCourse(code: String) = edit { it[Keys.course] = code }
    suspend fun setDailyGoal(xp: Int) = edit { it[Keys.goal] = xp.coerceIn(10, 500) }
    suspend fun setSpeechRate(rate: Float) = edit { it[Keys.rate] = rate.coerceIn(0.5f, 2.0f) }
    suspend fun setSoundEnabled(on: Boolean) = edit { it[Keys.sound] = on }
    suspend fun setTextScale(scale: Float) = edit { it[Keys.textScale] = scale.coerceIn(0.85f, 1.5f) }
    suspend fun setOnboarded(done: Boolean) = edit { it[Keys.onboarded] = done }

    suspend fun setStreak(current: Int, longest: Int, lastQualifiedDay: Long) = edit {
        it[Keys.streak] = current
        it[Keys.longest] = longest
        it[Keys.lastDay] = lastQualifiedDay
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
