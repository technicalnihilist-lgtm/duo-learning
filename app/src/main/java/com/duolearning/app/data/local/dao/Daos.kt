package com.duolearning.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.duolearning.app.data.local.entity.AchievementEntity
import com.duolearning.app.data.local.entity.DailyActivityEntity
import com.duolearning.app.data.local.entity.LessonProgressEntity
import com.duolearning.app.data.local.entity.SavedTranslationEntity
import com.duolearning.app.data.local.entity.TranslationHistoryEntity
import com.duolearning.app.data.local.entity.VocabProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress WHERE courseCode = :course")
    fun forCourse(course: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :id LIMIT 1")
    suspend fun byId(id: String): LessonProgressEntity?

    @Upsert
    suspend fun upsert(entity: LessonProgressEntity)

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE courseCode = :course AND completed = 1")
    fun completedCount(course: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1")
    fun completedCountAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE bestScore >= 100")
    fun perfectCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1")
    suspend fun completedCountNow(): Int

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE bestScore >= 100")
    suspend fun perfectCountNow(): Int

    @Query("DELETE FROM lesson_progress")
    suspend fun clear()
}

@Dao
interface VocabProgressDao {
    @Query("SELECT * FROM vocab_progress WHERE courseCode = :course ORDER BY dueAt ASC")
    fun forCourse(course: String): Flow<List<VocabProgressEntity>>

    @Query("SELECT * FROM vocab_progress WHERE courseCode = :course AND dueAt <= :now ORDER BY dueAt ASC LIMIT :limit")
    suspend fun due(course: String, now: Long, limit: Int): List<VocabProgressEntity>

    @Query("SELECT * FROM vocab_progress WHERE vocabId = :id LIMIT 1")
    suspend fun byId(id: String): VocabProgressEntity?

    @Upsert
    suspend fun upsert(entity: VocabProgressEntity)

    @Upsert
    suspend fun upsertAll(entities: List<VocabProgressEntity>)

    @Query("SELECT COUNT(*) FROM vocab_progress WHERE courseCode = :course")
    fun learnedCount(course: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocab_progress")
    suspend fun learnedCountNow(): Int

    @Query("UPDATE vocab_progress SET favourite = :fav WHERE vocabId = :id")
    suspend fun setFavourite(id: String, fav: Boolean)

    @Query("DELETE FROM vocab_progress")
    suspend fun clear()
}

@Dao
interface TranslationDao {
    // --- saved ---
    @Query("SELECT * FROM saved_translation ORDER BY createdAt DESC")
    fun saved(): Flow<List<SavedTranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaved(entity: SavedTranslationEntity): Long

    @Query("DELETE FROM saved_translation WHERE id = :id")
    suspend fun deleteSaved(id: Long)

    @Query("UPDATE saved_translation SET favourite = :fav WHERE id = :id")
    suspend fun setSavedFavourite(id: Long, fav: Boolean)

    @Query("DELETE FROM saved_translation")
    suspend fun clearSaved()

    @Query("SELECT COUNT(*) FROM saved_translation")
    suspend fun savedCountNow(): Int

    // --- history ---
    @Query("SELECT * FROM translation_history ORDER BY createdAt DESC LIMIT :limit")
    fun history(limit: Int): Flow<List<TranslationHistoryEntity>>

    @Query("SELECT * FROM translation_history WHERE sourceLang = :src AND targetLang = :tgt AND sourceText = :text LIMIT 1")
    suspend fun cached(src: String, tgt: String, text: String): TranslationHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: TranslationHistoryEntity)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM translation_history")
    suspend fun clearHistory()

    /** Trim history to the newest [keep] rows. */
    @Query("DELETE FROM translation_history WHERE id NOT IN (SELECT id FROM translation_history ORDER BY createdAt DESC LIMIT :keep)")
    suspend fun trimHistory(keep: Int)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM daily_activity WHERE epochDay = :day LIMIT 1")
    suspend fun byDay(day: Long): DailyActivityEntity?

    @Upsert
    suspend fun upsert(entity: DailyActivityEntity)

    @Query("SELECT * FROM daily_activity ORDER BY epochDay DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<DailyActivityEntity>>

    @Query("SELECT COALESCE(SUM(xpEarned), 0) FROM daily_activity")
    fun totalXp(): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpEarned), 0) FROM daily_activity")
    suspend fun totalXpNow(): Int

    @Query("SELECT * FROM daily_activity WHERE qualified = 1 ORDER BY epochDay DESC")
    suspend fun allQualified(): List<DailyActivityEntity>

    @Query("SELECT COALESCE(SUM(xpEarned),0) FROM daily_activity WHERE epochDay = :day")
    suspend fun xpOnDay(day: Long): Int

    @Query("DELETE FROM daily_activity")
    suspend fun clear()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement")
    fun all(): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievement")
    suspend fun ids(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(entity: AchievementEntity)

    @Query("DELETE FROM achievement")
    suspend fun clear()
}
