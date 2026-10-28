package com.duolearning.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionTest {

    @Test fun `first correct review schedules one day out`() {
        val r = SpacedRepetition.schedule(2.5, 0, 0, true, 0L)
        assertEquals(1, r.intervalDays)
        assertEquals(1, r.repetitions)
        assertEquals(SpacedRepetition.DAY_MS, r.dueAt)
    }

    @Test fun `second correct review schedules six days out`() {
        val r = SpacedRepetition.schedule(2.5, 1, 1, true, 0L)
        assertEquals(6, r.intervalDays)
    }

    @Test fun `an incorrect review resets the interval`() {
        val r = SpacedRepetition.schedule(2.5, 10, 4, false, 0L)
        assertEquals(1, r.intervalDays)
        assertEquals(0, r.repetitions)
    }

    @Test fun `ease factor never drops below 1_3`() {
        var ease = 2.5
        repeat(20) { ease = SpacedRepetition.schedule(ease, 1, 0, false, 0L).easeFactor }
        assertTrue(ease >= 1.3)
    }
}
