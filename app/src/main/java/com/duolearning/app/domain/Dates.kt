package com.duolearning.app.domain

import java.time.LocalDate
import java.time.ZoneId

/**
 * All streak math runs on the device's *local* calendar day, expressed as an
 * epoch-day number. Using epoch-day avoids the classic bug where a streak
 * increments twice around midnight or resets on a time-zone change.
 */
object Dates {
    fun todayEpochDay(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zone).toEpochDay()

    fun epochDayToLocalDate(day: Long): LocalDate = LocalDate.ofEpochDay(day)

    fun dayOfMonth(day: Long): Int = epochDayToLocalDate(day).dayOfMonth

    fun monthOf(day: Long): Int = epochDayToLocalDate(day).monthValue

    /**
     * Epoch-day of the Monday that starts the week containing [day].
     * Used to line a day grid up on real calendar weeks.
     */
    fun startOfWeek(day: Long): Long {
        val date = epochDayToLocalDate(day)
        return date.minusDays((date.dayOfWeek.value - 1).toLong()).toEpochDay()
    }

    /**
     * Two-letter weekday label (Mo, Tu, We, Th, Fr, Sa, Su) for [day].
     *
     * Single initials are ambiguous - Tuesday and Thursday both start with
     * "T", Saturday and Sunday with "S" - so the activity strip needs two
     * letters to be readable.
     */
    fun shortWeekday(day: Long): String =
        SHORT_WEEKDAYS[epochDayToLocalDate(day).dayOfWeek.value - 1]

    private val SHORT_WEEKDAYS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

    /** The two-letter weekday labels, Monday first. */
    val shortWeekdays: List<String> = SHORT_WEEKDAYS
}
