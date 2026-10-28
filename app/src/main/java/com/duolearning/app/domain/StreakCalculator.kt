package com.duolearning.app.domain

/** Result of folding a qualifying activity into the streak state. */
data class StreakState(
    val current: Int,
    val longest: Int,
    val lastQualifiedDay: Long
)

object StreakCalculator {

    /**
     * Fold a qualifying learning activity performed on [todayDay].
     *
     * Rules (mirroring the product spec):
     *  - first ever activity starts the streak at 1;
     *  - a second activity on the *same* local day changes nothing;
     *  - activity on the day after the last one extends the streak by 1;
     *  - a gap of two or more days resets the streak to 1;
     *  - [StreakState.longest] never decreases.
     */
    fun record(
        current: Int,
        longest: Int,
        lastQualifiedDay: Long,
        todayDay: Long
    ): StreakState {
        val newCurrent = when {
            lastQualifiedDay < 0L -> 1
            lastQualifiedDay == todayDay -> current.coerceAtLeast(1)
            lastQualifiedDay == todayDay - 1L -> current + 1
            else -> 1 // missed at least one full day
        }
        return StreakState(
            current = newCurrent,
            longest = maxOf(longest, newCurrent),
            lastQualifiedDay = maxOf(lastQualifiedDay, todayDay)
        )
    }

    /**
     * The streak value to *display* right now. If the user has already broken
     * the chain (last qualified day is older than yesterday) we show 0 until
     * they earn a new day, so the UI never claims a streak that is dead.
     */
    fun displayStreak(current: Int, lastQualifiedDay: Long, todayDay: Long): Int = when {
        lastQualifiedDay < 0L -> 0
        lastQualifiedDay == todayDay || lastQualifiedDay == todayDay - 1L -> current
        else -> 0
    }

    fun isActiveToday(lastQualifiedDay: Long, todayDay: Long): Boolean =
        lastQualifiedDay == todayDay
}
