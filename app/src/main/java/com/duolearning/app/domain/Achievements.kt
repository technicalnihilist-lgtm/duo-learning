package com.duolearning.app.domain

/** Snapshot of everything achievements are evaluated against. */
data class AchievementStats(
    val totalXp: Int,
    val lessonsCompleted: Int,
    val wordsLearned: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val translationsSaved: Int,
    val perfectLessons: Int
)

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val earned: (AchievementStats) -> Boolean
)

object Achievements {
    val all: List<AchievementDef> = listOf(
        AchievementDef("first_lesson", "First Steps", "Complete your first lesson") { it.lessonsCompleted >= 1 },
        AchievementDef("ten_lessons", "Getting Serious", "Complete 10 lessons") { it.lessonsCompleted >= 10 },
        AchievementDef("fifty_lessons", "Dedicated", "Complete 50 lessons") { it.lessonsCompleted >= 50 },
        AchievementDef("xp_100", "Century", "Earn 100 XP") { it.totalXp >= 100 },
        AchievementDef("xp_500", "High Scorer", "Earn 500 XP") { it.totalXp >= 500 },
        AchievementDef("xp_2000", "XP Machine", "Earn 2000 XP") { it.totalXp >= 2000 },
        AchievementDef("streak_3", "Warming Up", "Reach a 3-day streak") { it.currentStreak >= 3 },
        AchievementDef("streak_7", "Week Warrior", "Reach a 7-day streak") { it.currentStreak >= 7 },
        AchievementDef("streak_30", "Unstoppable", "Reach a 30-day streak") { it.currentStreak >= 30 },
        AchievementDef("words_25", "Word Collector", "Learn 25 words") { it.wordsLearned >= 25 },
        AchievementDef("words_100", "Lexicon", "Learn 100 words") { it.wordsLearned >= 100 },
        AchievementDef("saver", "Well Saved", "Save 10 translations") { it.translationsSaved >= 10 },
        AchievementDef("perfect", "Flawless", "Finish a lesson with no mistakes") { it.perfectLessons >= 1 }
    )

    /** Ids newly earned given [stats] that are not already in [owned]. */
    fun newlyEarned(stats: AchievementStats, owned: Set<String>): List<AchievementDef> =
        all.filter { it.id !in owned && it.earned(stats) }
}
