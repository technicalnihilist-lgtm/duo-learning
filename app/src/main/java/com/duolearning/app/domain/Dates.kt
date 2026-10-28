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
}
