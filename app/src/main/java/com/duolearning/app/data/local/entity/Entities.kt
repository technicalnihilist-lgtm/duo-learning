package com.duolearning.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Per-lesson completion state. Keyed by the stable seed lesson id. */
@Entity(tableName = "lesson_progress", indices = [Index("courseCode")])
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val courseCode: String,
    val completed: Boolean = false,
    val bestScore: Int = 0,
    val attempts: Int = 0,
    val lastAttemptAt: Long = 0L
)

/** Spaced-repetition state for one vocabulary item (SM-2 style fields). */
@Entity(tableName = "vocab_progress", indices = [Index("courseCode"), Index("dueAt")])
data class VocabProgressEntity(
    @PrimaryKey val vocabId: String,
    val courseCode: String,
    val term: String,
    val translation: String,
    val exampleTarget: String,
    val exampleNative: String,
    val partOfSpeech: String,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val dueAt: Long = 0L,
    val lastReviewedAt: Long = 0L,
    val correct: Int = 0,
    val wrong: Int = 0,
    val favourite: Boolean = false
)

/** A translation the user explicitly saved for later study. */
@Entity(tableName = "saved_translation", indices = [Index("createdAt")])
data class SavedTranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceLang: String,
    val targetLang: String,
    val sourceText: String,
    val targetText: String,
    val createdAt: Long,
    val favourite: Boolean = false
)

/** Rolling recent-translation history (capped; user-clearable). */
@Entity(tableName = "translation_history", indices = [Index("createdAt")])
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceLang: String,
    val targetLang: String,
    val sourceText: String,
    val targetText: String,
    val createdAt: Long
)

/** One row per local calendar day on which the user did anything. */
@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    /** Local date as epoch-day (days since 1970-01-01 in the device zone). */
    @PrimaryKey val epochDay: Long,
    val xpEarned: Int = 0,
    val lessonsCompleted: Int = 0,
    /** True when the day counts toward the streak (a qualifying activity). */
    val qualified: Boolean = false
)

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Long
)
