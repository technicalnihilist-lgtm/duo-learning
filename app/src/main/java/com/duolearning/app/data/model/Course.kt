package com.duolearning.app.data.model

/** A vocabulary item that can be practised, scheduled and translated. */
data class VocabItem(
    val id: String,
    val term: String,
    val translation: String,
    val exampleTarget: String,
    val exampleNative: String,
    val partOfSpeech: String,
    val note: String = ""
)

/** A single exercise within a lesson. */
sealed interface ExerciseSpec {
    val prompt: String
    val explanation: String

    /** Pick the correct translation from options. */
    data class MultipleChoice(
        override val prompt: String,
        val options: List<String>,
        val correctIndex: Int,
        override val explanation: String = ""
    ) : ExerciseSpec

    /** Translate by typing (accepts a set of valid answers). */
    data class TypedTranslation(
        override val prompt: String,
        val accepted: List<String>,
        override val explanation: String = ""
    ) : ExerciseSpec

    /** Match target words to their meanings. */
    data class MatchPairs(
        override val prompt: String,
        val pairs: List<Pair<String, String>>,
        override val explanation: String = ""
    ) : ExerciseSpec

    /** Build a sentence from scrambled tokens. */
    data class Scrambled(
        override val prompt: String,
        val tokens: List<String>,
        val correct: String,
        override val explanation: String = ""
    ) : ExerciseSpec

    /** Fill in the blank in a sentence. */
    data class FillBlank(
        override val prompt: String,
        val options: List<String>,
        val correctIndex: Int,
        override val explanation: String = ""
    ) : ExerciseSpec

    /** Listen to audio and choose what was said. */
    data class Listening(
        override val prompt: String,
        val spoken: String,
        val options: List<String>,
        val correctIndex: Int,
        override val explanation: String = ""
    ) : ExerciseSpec

    /** Correct an incorrect translation. */
    data class Correction(
        override val prompt: String,
        val wrong: String,
        val correct: String,
        override val explanation: String = ""
    ) : ExerciseSpec
}

data class Lesson(
    val id: String,
    val title: String,
    val kind: LessonKind,
    val exercises: List<ExerciseSpec>,
    val vocab: List<VocabItem>
) {
    enum class LessonKind { VOCAB, GRAMMAR, LISTENING, READING, TRANSLATION, REVISION, CHECKPOINT }
}

data class Unit(
    val id: String,
    val title: String,
    val subtitle: String,
    val lessons: List<Lesson>
)

data class Course(
    val languageCode: String,
    val units: List<Unit>
) {
    val lessonCount: Int get() = units.sumOf { it.lessons.size }
    val exerciseCount: Int get() = units.sumOf { u -> u.lessons.sumOf { it.exercises.size } }
}
