package com.duolearning.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    @Test fun `first activity starts a one day streak`() {
        val r = StreakCalculator.record(0, 0, -1L, 100L)
        assertEquals(1, r.current)
        assertEquals(1, r.longest)
        assertEquals(100L, r.lastQualifiedDay)
    }

    @Test fun `second activity on the same day does not increment`() {
        val r = StreakCalculator.record(1, 1, 100L, 100L)
        assertEquals(1, r.current)
    }

    @Test fun `consecutive day extends the streak`() {
        val r = StreakCalculator.record(1, 1, 100L, 101L)
        assertEquals(2, r.current)
    }

    @Test fun `missing a full day resets the streak`() {
        val r = StreakCalculator.record(9, 9, 100L, 102L)
        assertEquals(1, r.current)
        assertEquals(9, r.longest) // longest is preserved
    }

    @Test fun `longest streak is remembered`() {
        val r = StreakCalculator.record(5, 12, 100L, 101L)
        assertEquals(6, r.current)
        assertEquals(12, r.longest)
    }

    @Test fun `display streak is zero once the chain is broken`() {
        assertEquals(0, StreakCalculator.displayStreak(7, 100L, 103L))
        assertEquals(7, StreakCalculator.displayStreak(7, 100L, 101L))
        assertEquals(7, StreakCalculator.displayStreak(7, 101L, 101L))
    }
}
