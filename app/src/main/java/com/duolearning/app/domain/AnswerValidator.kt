package com.duolearning.app.domain

/**
 * Answer checking that is forgiving of harmless differences (case, trailing
 * punctuation, extra spaces) but never accepts a genuinely wrong translation.
 * Accents are significant, because they change meaning in French/Spanish/etc.
 */
object AnswerValidator {

    private val punctuation = Regex("[^\\p{L}\\p{N}\\s]")
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
