package com.duolearning.app.domain

import kotlin.math.max

/** SM-2 style scheduling for vocabulary revision. */
object SpacedRepetition {

    const val DAY_MS = 24L * 60 * 60 * 1000

    data class Result(
        val easeFactor: Double,
        val intervalDays: Int,
        val repetitions: Int,
        val dueAt: Long
    )

    /**
     * @param correct whether the recall was correct (a 5/4 quality) or not (2).
     */
    fun schedule(
        easeFactor: Double,
        intervalDays: Int,
        repetitions: Int,
        correct: Boolean,
        now: Long
    ): Result {
        val q = if (correct) 5 else 2
        val newEase = max(
            1.3,
            easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        )
        val (newInterval, newReps) = if (!correct) {
            1 to 0
        } else {
            val reps = repetitions + 1
            val interval = when (reps) {
                1 -> 1
                2 -> 6
                else -> (intervalDays * newEase).toInt().coerceAtLeast(1)
            }
            interval to reps
        }
        return Result(
            easeFactor = newEase,
            intervalDays = newInterval,
            repetitions = newReps,
            dueAt = now + newInterval * DAY_MS
        )
    }
}
