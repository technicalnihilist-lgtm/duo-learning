package com.duolearning.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.duolearning.app.data.local.dao.AchievementDao
import com.duolearning.app.data.local.dao.ActivityDao
import com.duolearning.app.data.local.dao.LessonProgressDao
import com.duolearning.app.data.local.dao.TranslationDao
import com.duolearning.app.data.local.dao.VocabProgressDao
import com.duolearning.app.data.local.entity.AchievementEntity
import com.duolearning.app.data.local.entity.DailyActivityEntity
import com.duolearning.app.data.local.entity.LessonProgressEntity
import com.duolearning.app.data.local.entity.SavedTranslationEntity
import com.duolearning.app.data.local.entity.TranslationHistoryEntity
import com.duolearning.app.data.local.entity.VocabProgressEntity

@Database(
    entities = [
        LessonProgressEntity::class,
        VocabProgressEntity::class,
        SavedTranslationEntity::class,
        TranslationHistoryEntity::class,
        DailyActivityEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class DuoDatabase : RoomDatabase() {
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun vocabProgressDao(): VocabProgressDao
    abstract fun translationDao(): TranslationDao
    abstract fun activityDao(): ActivityDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile private var INSTANCE: DuoDatabase? = null

        fun get(context: Context): DuoDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                DuoDatabase::class.java,
                "duo_learning.db"
            )
                // Never destructive-migrate: losing progress on upgrade is
                // unacceptable. Add real migrations as the schema evolves.
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                .also { INSTANCE = it }
        }
    }
}
