package com.duolearning.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

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
    val textScale: Float = 1.0f,
    val depthEffect: Boolean = true,
    val parallax: Boolean = true,
    val effectStrength: Float = 0.6f,
    val glassEffect: Boolean = true,
    val animationsEnabled: Boolean = true,
    val blurPx: Float = 16f,
    val transparencyPct: Float = 26f,
    val depthPct: Float = 100f,
    val bouncePct: Float = 100f,
    val speedPct: Float = 100f
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
        val depth = booleanPreferencesKey("depth_effect")
        val parallax = booleanPreferencesKey("parallax")
        val strength = floatPreferencesKey("effect_strength")
        val glass = booleanPreferencesKey("fx_glass")
        val anims = booleanPreferencesKey("fx_animations")
        val blur = floatPreferencesKey("fx_blur")
        val transparency = floatPreferencesKey("fx_transparency")
        val depthPct = floatPreferencesKey("fx_depth")
        val bounce = floatPreferencesKey("fx_bounce")
        val speed = floatPreferencesKey("fx_speed")
    }

    val settings: Flow<UserSettings> = context.dataStore.data
        // A corrupt or unreadable preferences file would otherwise throw out of
        // this flow and take every screen that observes settings down with it.
        // Fall back to defaults instead, so the app stays usable.
        .catch { t -> if (t is IOException) emit(emptyPreferences()) else throw t }
        .map { p ->
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
                textScale = p[Keys.textScale] ?: 1.0f,
                depthEffect = p[Keys.depth] ?: true,
                parallax = p[Keys.parallax] ?: true,
                effectStrength = p[Keys.strength] ?: 0.6f,
                glassEffect = p[Keys.glass] ?: true,
                animationsEnabled = p[Keys.anims] ?: true,
                blurPx = p[Keys.blur] ?: 16f,
                transparencyPct = p[Keys.transparency] ?: 26f,
                depthPct = p[Keys.depthPct] ?: 100f,
                bouncePct = p[Keys.bounce] ?: 100f,
                speedPct = p[Keys.speed] ?: 100f
            )
        }

    suspend fun setNativeLanguage(code: String) = edit { it[Keys.native] = code }
    suspend fun setActiveCourse(code: String) = edit { it[Keys.course] = code }
    suspend fun setDailyGoal(xp: Int) = edit { it[Keys.goal] = xp.coerceIn(10, 500) }
    suspend fun setSpeechRate(rate: Float) = edit { it[Keys.rate] = rate.coerceIn(0.5f, 2.0f) }
    suspend fun setSoundEnabled(on: Boolean) = edit { it[Keys.sound] = on }
    suspend fun setTextScale(scale: Float) = edit { it[Keys.textScale] = scale.coerceIn(0.85f, 1.5f) }
    suspend fun setDepthEffect(on: Boolean) = edit { it[Keys.depth] = on }
    suspend fun setParallax(on: Boolean) = edit { it[Keys.parallax] = on }
    suspend fun setEffectStrength(v: Float) = edit { it[Keys.strength] = v.coerceIn(0f, 1f) }
    suspend fun setGlassEffect(on: Boolean) = edit { it[Keys.glass] = on }
    suspend fun setAnimationsEnabled(on: Boolean) = edit { it[Keys.anims] = on }
    suspend fun setBlurPx(v: Float) = edit { it[Keys.blur] = v.coerceIn(0f, 40f) }
    suspend fun setTransparencyPct(v: Float) = edit { it[Keys.transparency] = v.coerceIn(4f, 30f) }
    suspend fun setDepthPct(v: Float) = edit { it[Keys.depthPct] = v.coerceIn(0f, 200f) }
    suspend fun setBouncePct(v: Float) = edit { it[Keys.bounce] = v.coerceIn(0f, 200f) }
    suspend fun setSpeedPct(v: Float) = edit { it[Keys.speed] = v.coerceIn(50f, 200f) }

    /** Restore every visual effect to its default. */
    suspend fun resetEffects() = edit {
        it[Keys.glass] = true
        it[Keys.anims] = true
        it[Keys.blur] = 16f
        it[Keys.transparency] = 26f
        it[Keys.depthPct] = 100f
        it[Keys.bounce] = 100f
        it[Keys.speed] = 100f
    }
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
