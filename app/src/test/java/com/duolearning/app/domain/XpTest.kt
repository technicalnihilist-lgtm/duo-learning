package com.duolearning.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XpTest {

    @Test fun `passing threshold is seventy percent`() {
        assertTrue(Xp.passed(7, 10))
        assertFalse(Xp.passed(6, 10))
        assertFalse(Xp.passed(0, 0))
    }

    @Test fun `perfect lesson adds a bonus`() {
        val normal = Xp.forLesson(5, 10, false)
        val perfect = Xp.forLesson(10, 10, false)
        assertTrue(perfect > normal)
    }

    @Test fun `checkpoint awards the checkpoint bonus`() {
        assertEquals(Xp.CHECKPOINT_COMPLETE_BONUS, Xp.forLesson(0, 5, isCheckpoint = true))
    }
}
