package com.duolearning.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerValidatorTest {

    @Test fun `case and trailing punctuation are ignored`() {
        assertTrue(AnswerValidator.matchesTyped("This is a book.", listOf("this is a book")))
        assertTrue(AnswerValidator.matchesTyped("  HELLO ", listOf("hello")))
    }

    @Test fun `alternative answers are accepted`() {
        assertTrue(AnswerValidator.matchesTyped("thanks", listOf("thank you", "thanks")))
    }

    @Test fun `a different translation is rejected`() {
        assertFalse(AnswerValidator.matchesTyped("house", listOf("book")))
    }

    @Test fun `accents remain significant`() {
        assertFalse(AnswerValidator.matchesTyped("el nino come pan", listOf("el niño come pan")))
    }

    @Test fun `devanagari sentence with danda matches`() {
        assertTrue(AnswerValidator.matchesSentence("मैं हिन्दी सीखता हूँ", "मैं हिन्दी सीखता हूँ।"))
    }

    @Test fun `indic vowel signs are not stripped`() {
        // Both answers used to normalise to "कल" because every combining mark
        // was treated as punctuation, so the wrong one was accepted.
        assertFalse(AnswerValidator.matchesTyped("कल", listOf("काल")))
        assertFalse(AnswerValidator.matchesTyped("पानि", listOf("पानी")))
    }

    @Test fun `devanagari answers still match after normalisation`() {
        assertTrue(AnswerValidator.matchesTyped("  पानी  ", listOf("पानी")))
        assertTrue(AnswerValidator.matchesSentence("आप कैसे हैं", "आप कैसे हैं?"))
        assertTrue(AnswerValidator.matchesTyped("मैं ठीक हूँ", listOf("मैं ठीक हूँ।")))
    }

    @Test fun `empty input never matches`() {
        assertFalse(AnswerValidator.matchesTyped("   ", listOf("book")))
    }
}
