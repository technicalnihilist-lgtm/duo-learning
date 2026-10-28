package com.duolearning.app.domain

/** XP economy. Tuned so a short session still moves the daily goal. */
object Xp {
    const val PER_CORRECT_ANSWER = 2
    const val LESSON_COMPLETE_BONUS = 10
    const val CHECKPOINT_COMPLETE_BONUS = 20
    const val PRACTICE_SESSION_BONUS = 8
    const val PERFECT_LESSON_BONUS = 5

    fun forLesson(
        correct: Int,
        total: Int,
        isCheckpoint: Boolean,
        bonus: Int = LESSON_COMPLETE_BONUS
    ): Int {
        val base = correct * PER_CORRECT_ANSWER
        val completion = if (isCheckpoint) CHECKPOINT_COMPLETE_BONUS else bonus
        val perfect = if (total > 0 && correct == total) PERFECT_LESSON_BONUS else 0
        return base + completion + perfect
    }

    /** A lesson "passes" at 70% accuracy. */
    fun passed(correct: Int, total: Int): Boolean =
        total > 0 && correct.toFloat() / total >= 0.7f
}
