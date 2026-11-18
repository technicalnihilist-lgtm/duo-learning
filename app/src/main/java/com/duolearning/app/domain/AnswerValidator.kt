package com.duolearning.app.domain

/**
 * Answer checking that is forgiving of harmless differences (case, trailing
 * punctuation, extra spaces) but never accepts a genuinely wrong translation.
 * Accents are significant, because they change meaning in French/Spanish/etc.
 */
object AnswerValidator {

    /**
     * Everything that is not a letter, a digit, whitespace or a combining
     * mark.
     *
     * Combining marks (`\p{M}`) have to survive normalisation: Indic scripts
     * write their vowels as separate combining characters (की, का, हूँ), and
     * dropping every non-letter deleted them. "काल" and "कल" both collapsed
     * to "कल", so a wrong answer with the right consonants was graded correct.
     */
    private val punctuation = Regex("[^\\p{L}\\p{N}\\p{M}\\s]")
    private val whitespace = Regex("\\s+")

    fun normalize(input: String): String = input
        .lowercase()
        .replace(punctuation, " ")
        .replace(whitespace, " ")
        .trim()

    /** True when [input] equals any accepted answer after normalisation. */
    fun matchesTyped(input: String, accepted: List<String>): Boolean {
        val n = normalize(input)
        if (n.isEmpty()) return false
        return accepted.any { normalize(it) == n }
    }

    fun matchesSentence(built: String, correct: String): Boolean =
        normalize(built) == normalize(correct)
}
