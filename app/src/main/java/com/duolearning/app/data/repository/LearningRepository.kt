package com.duolearning.app.data.repository

import com.duolearning.app.data.content.SeedContent
import com.duolearning.app.data.local.DuoDatabase
import com.duolearning.app.data.local.entity.AchievementEntity
import com.duolearning.app.data.local.entity.DailyActivityEntity
import com.duolearning.app.data.local.entity.LessonProgressEntity
import com.duolearning.app.data.local.entity.VocabProgressEntity
import com.duolearning.app.data.prefs.UserPreferences
import com.duolearning.app.data.prefs.UserSettings
import com.duolearning.app.domain.AchievementStats
import com.duolearning.app.domain.Achievements
import com.duolearning.app.domain.Dates
import com.duolearning.app.domain.SpacedRepetition
import com.duolearning.app.domain.StreakCalculator
import com.duolearning.app.domain.StreakState
import com.duolearning.app.domain.Xp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class ProgressStats(
    val totalXp: Int = 0,
    val lessonsCompleted: Int = 0,
    val wordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val dailyGoal: Int = 50,
    val achievementsEarned: Int = 0,
    val achievementsTotal: Int = Achievements.all.size
)

data class ActivityResult(
    val xpAwarded: Int,
    val passed: Boolean,
    val newStreak: Int,
    val streakExtended: Boolean,
    val unlockedAchievements: List<String>
)

class LearningRepository(
    private val db: DuoDatabase,
    private val prefs: UserPreferences
) {
    val settings: Flow<UserSettings> = prefs.settings

    fun observeLessonProgress(course: String) = db.lessonProgressDao().forCourse(course)
    fun observeVocab(course: String) = db.vocabProgressDao().forCourse(course)
    fun observeDailyActivity() = db.activityDao().recent(120)
    fun observeAchievements() = db.achievementDao().all()
    fun observeTotalXp() = db.activityDao().totalXp()
    fun observePerfectCount() = db.lessonProgressDao().perfectCount()

    fun observeProgressStats(course: String): Flow<ProgressStats> = combine(
        db.activityDao().totalXp(),
        db.lessonProgressDao().completedCountAll(),
        db.vocabProgressDao().learnedCount(course),
        db.achievementDao().all(),
        prefs.settings
    ) { totalXp, lessons, words, achievements, settings ->
        val today = Dates.todayEpochDay()
        ProgressStats(
            totalXp = totalXp,
            lessonsCompleted = lessons,
            wordsLearned = words,
            currentStreak = StreakCalculator.displayStreak(
                settings.currentStreak, settings.lastQualifiedDay, today
            ),
            longestStreak = settings.longestStreak,
            dailyGoal = settings.dailyGoalXp,
            achievementsEarned = achievements.size
        )
    }

    fun observeXpToday(): Flow<Int> {
        val today = Dates.todayEpochDay()
        return db.activityDao().recent(7).map { rows ->
            rows.firstOrNull { it.epochDay == today }?.xpEarned ?: 0
        }
    }

    fun observeStreakExtendedToday(): Flow<Boolean> {
        val today = Dates.todayEpochDay()
        return db.activityDao().recent(1).map { rows ->
            rows.firstOrNull()?.let { it.epochDay == today && it.qualified } ?: false
        }
    }

    suspend fun ensureVocabSeeded(course: String) {
        val items = SeedContent.allVocab(course)
        if (items.isEmpty()) return
        val existing = db.vocabProgressDao().due(course, Long.MAX_VALUE, Int.MAX_VALUE)
        val existingIds = existing.map { it.vocabId }.toSet()
        val now = System.currentTimeMillis()
        val fresh = items.filter { it.id !in existingIds }.map { v ->
            VocabProgressEntity(
                vocabId = v.id, courseCode = course, term = v.term,
                translation = v.translation, exampleTarget = v.exampleTarget,
                exampleNative = v.exampleNative, partOfSpeech = v.partOfSpeech,
                dueAt = now
            )
        }
        if (fresh.isNotEmpty()) db.vocabProgressDao().upsertAll(fresh)
    }

    suspend fun dueVocabNow(course: String, limit: Int = 20): List<VocabProgressEntity> =
        db.vocabProgressDao().due(course, System.currentTimeMillis(), limit)

    suspend fun allVocabNow(course: String, limit: Int = 50): List<VocabProgressEntity> =
        db.vocabProgressDao().due(course, Long.MAX_VALUE, limit)

    suspend fun reviewVocab(vocabId: String, correct: Boolean) {
        val existing = db.vocabProgressDao().byId(vocabId) ?: return
        val now = System.currentTimeMillis()
        val result = SpacedRepetition.schedule(
            existing.easeFactor, existing.intervalDays, existing.repetitions, correct, now
        )
        db.vocabProgressDao().upsert(
            existing.copy(
                easeFactor = result.easeFactor,
                intervalDays = result.intervalDays,
                repetitions = result.repetitions,
                dueAt = result.dueAt,
                lastReviewedAt = now,
                correct = existing.correct + if (correct) 1 else 0,
                wrong = existing.wrong + if (!correct) 1 else 0
            )
        )
    }

    suspend fun toggleFavourite(vocabId: String, fav: Boolean) =
        db.vocabProgressDao().setFavourite(vocabId, fav)

    suspend fun recordLessonResult(
        courseCode: String,
        lessonId: String?,
        correct: Int,
        total: Int,
        isCheckpoint: Boolean,
        qualifying: Boolean = true
    ): ActivityResult {
        val xp = if (lessonId != null) Xp.forLesson(correct, total, isCheckpoint)
        else correct * Xp.PER_CORRECT_ANSWER + Xp.PRACTICE_SESSION_BONUS
        val passed = Xp.passed(correct, total)

        if (lessonId != null) {
            val existing = db.lessonProgressDao().byId(lessonId)
            val score = if (total > 0) (correct * 100) / total else 0
            db.lessonProgressDao().upsert(
                LessonProgressEntity(
                    lessonId = lessonId,
                    courseCode = courseCode,
                    completed = existing?.completed == true || passed,
                    bestScore = maxOf(existing?.bestScore ?: 0, score),
                    attempts = (existing?.attempts ?: 0) + 1,
                    lastAttemptAt = System.currentTimeMillis()
                )
            )
        }
        return awardActivity(xp, lessonsDelta = if (lessonId != null && passed) 1 else 0, qualifying = qualifying)
    }

    private suspend fun awardActivity(xp: Int, lessonsDelta: Int, qualifying: Boolean): ActivityResult {
        val today = Dates.todayEpochDay()
        val activity = db.activityDao().byDay(today) ?: DailyActivityEntity(epochDay = today)
        db.activityDao().upsert(
            activity.copy(
                xpEarned = activity.xpEarned + xp,
                lessonsCompleted = activity.lessonsCompleted + lessonsDelta,
                qualified = activity.qualified || qualifying
            )
        )

        val s = prefs.settings.first()
        val before = StreakCalculator.displayStreak(s.currentStreak, s.lastQualifiedDay, today)
        val state: StreakState = if (qualifying) {
            StreakCalculator.record(s.currentStreak, s.longestStreak, s.lastQualifiedDay, today)
        } else {
            StreakState(s.currentStreak, s.longestStreak, s.lastQualifiedDay)
        }
        prefs.setStreak(state.current, state.longest, state.lastQualifiedDay)
        val newStreak = StreakCalculator.displayStreak(state.current, state.lastQualifiedDay, today)

        val unlocked = evaluateAchievements()
        return ActivityResult(
            xpAwarded = xp,
            passed = true,
            newStreak = newStreak,
            streakExtended = qualifying && newStreak > before,
            unlockedAchievements = unlocked
        )
    }

    private suspend fun evaluateAchievements(): List<String> {
        val owned = db.achievementDao().ids().toSet()
        val newly = Achievements.newlyEarned(snapshotStats(), owned)
        val now = System.currentTimeMillis()
        newly.forEach { db.achievementDao().unlock(AchievementEntity(it.id, now)) }
        return newly.map { it.id }
    }

    suspend fun snapshotStats(): AchievementStats {
        val s = prefs.settings.first()
        val today = Dates.todayEpochDay()
        return AchievementStats(
            totalXp = db.activityDao().totalXpNow(),
            lessonsCompleted = db.lessonProgressDao().completedCountNow(),
            wordsLearned = db.vocabProgressDao().learnedCountNow(),
            currentStreak = StreakCalculator.displayStreak(s.currentStreak, s.lastQualifiedDay, today),
            longestStreak = s.longestStreak,
            translationsSaved = db.translationDao().savedCountNow(),
            perfectLessons = db.lessonProgressDao().perfectCountNow()
        )
    }

    suspend fun currentSettings(): UserSettings = prefs.settings.first()

    suspend fun resetProgress() {
        db.lessonProgressDao().clear()
        db.vocabProgressDao().clear()
        db.activityDao().clear()
        db.achievementDao().clear()
        prefs.setStreak(0, 0, -1L)
    }
}
