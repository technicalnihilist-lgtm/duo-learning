package com.duolearning.app.data.content

import com.duolearning.app.data.model.Course
import com.duolearning.app.data.model.ExerciseSpec
import com.duolearning.app.data.model.Lesson
import com.duolearning.app.data.model.Unit
import com.duolearning.app.data.model.VocabItem

/**
 * Real, hand-curated course content that ships inside the APK. It is small
 * enough to be free (a few hundred kB of Kotlin string data) yet large enough
 * that every shipped course is genuinely playable offline with correct
 * answers and validation. Additional courses are translation-only until real
 * content is authored for them — see [LanguageCatalog].
 */
object SeedContent {

    private fun vocab(
        id: String, term: String, translation: String,
        exT: String, exN: String, pos: String, note: String = ""
    ) = VocabItem(id, term, translation, exT, exN, pos, note)

    private fun mc(prompt: String, correct: String, vararg wrong: String, explanation: String = "") =
        ExerciseSpec.MultipleChoice(
            prompt = prompt,
            options = (listOf(correct) + wrong).shuffled(kotlin.random.Random(prompt.hashCode().toLong())),
            correctIndex = (listOf(correct) + wrong).shuffled(kotlin.random.Random(prompt.hashCode().toLong()))
                .indexOf(correct),
            explanation = explanation
        )

    private fun typed(prompt: String, correct: String, vararg alt: String, explanation: String = "") =
        ExerciseSpec.TypedTranslation(prompt, (listOf(correct) + alt), explanation)

    private fun fill(prompt: String, correct: String, vararg wrong: String, explanation: String = "") =
        ExerciseSpec.FillBlank(
            prompt = prompt,
            options = (listOf(correct) + wrong).shuffled(kotlin.random.Random(prompt.hashCode().toLong())),
            correctIndex = (listOf(correct) + wrong).shuffled(kotlin.random.Random(prompt.hashCode().toLong()))
                .indexOf(correct),
            explanation = explanation
        )

    private fun listen(spoken: String, correct: String, vararg wrong: String) =
        ExerciseSpec.Listening(
            prompt = "Tap what you hear",
            spoken = spoken,
            options = (listOf(correct) + wrong).shuffled(kotlin.random.Random(spoken.hashCode().toLong())),
            correctIndex = (listOf(correct) + wrong).shuffled(kotlin.random.Random(spoken.hashCode().toLong()))
                .indexOf(correct)
        )

    // ------------------------------------------------------------------
    // Hindi course (learner is an English speaker)
    // ------------------------------------------------------------------
    private val hindiVocabU1 = listOf(
        vocab("hi.namaste", "नमस्ते", "Hello", "नमस्ते, आप कैसे हैं?", "Hello, how are you?", "interjection"),
        vocab("hi.dhanyavad", "धन्यवाद", "Thank you", "आपकी मदद के लिए धन्यवाद।", "Thank you for your help.", "interjection"),
        vocab("hi.haan", "हाँ", "Yes", "हाँ, मुझे चाय चाहिए।", "Yes, I want tea.", "adverb"),
        vocab("hi.nahin", "नहीं", "No", "नहीं, मैं नहीं जानता।", "No, I do not know.", "adverb"),
        vocab("hi.main", "मैं", "I", "मैं एक विद्यार्थी हूँ।", "I am a student.", "pronoun"),
        vocab("hi.aap", "आप", "You (formal)", "आप कहाँ रहते हैं?", "Where do you live?", "pronoun")
    )
    private val hindiVocabU2 = listOf(
        vocab("hi.pani", "पानी", "Water", "मुझे ठंडा पानी चाहिए।", "I want cold water.", "noun"),
        vocab("hi.khana", "खाना", "Food / to eat", "खाना बहुत स्वादिष्ट है।", "The food is very tasty.", "noun"),
        vocab("hi.ghar", "घर", "House", "मेरा घर पास में है।", "My house is nearby.", "noun"),
        vocab("hi.kitab", "किताब", "Book", "यह किताब बहुत अच्छी है।", "This book is very good.", "noun"),
        vocab("hi.ladka", "लड़का", "Boy", "वह लड़का स्कूल जाता है।", "That boy goes to school.", "noun"),
        vocab("hi.ladki", "लड़की", "Girl", "लड़की पानी पीती है।", "The girl drinks water.", "noun")
    )
    private val hindiVocabU3 = listOf(
        vocab("hi.achha", "अच्छा", "Good", "यह बहुत अच्छा है।", "This is very good.", "adjective"),
        vocab("hi.sikhna", "सीखना", "To learn", "मैं हिन्दी सीखता हूँ।", "I learn Hindi.", "verb"),
        vocab("hi.school", "स्कूल", "School", "स्कूल कल बंद है।", "School is closed tomorrow.", "noun"),
        vocab("hi.kaise", "कैसे", "How", "आप कैसे हैं?", "How are you?", "adverb")
    )

    private val hindi = Course(
        languageCode = "hi",
        units = listOf(
            Unit(
                "hi.u1", "Basics", "Greetings and pronouns",
                listOf(
                    Lesson("hi.u1.l1", "Greetings", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"Hello\"?", "नमस्ते", "धन्यवाद", "नहीं"),
                        mc("What does धन्यवाद mean?", "Thank you", "Hello", "Yes"),
                        typed("Translate to Hindi: Yes", "हाँ"),
                        listen("नमस्ते", "नमस्ते", "धन्यवाद", "हाँ"),
                        ExerciseSpec.MatchPairs("Match the pairs",
                            listOf("नमस्ते" to "Hello", "हाँ" to "Yes", "नहीं" to "No"))
                    ), hindiVocabU1),
                    Lesson("hi.u1.l2", "Pronouns", Lesson.LessonKind.GRAMMAR, listOf(
                        mc("Which pronoun is the formal \"you\"?", "आप", "मैं", "हाँ"),
                        fill("___ एक विद्यार्थी हूँ। (I am a student.)", "मैं", "आप", "नहीं"),
                        typed("Translate to English: मैं", "I"),
                        ExerciseSpec.Scrambled("Build: \"I am a student.\"",
                            listOf("मैं", "विद्यार्थी", "हूँ"), "मैं विद्यार्थी हूँ"),
                        ExerciseSpec.Correction("Fix the translation",
                            "नमस्ते = Thank you", "नमस्ते = Hello")
                    ), hindiVocabU1),
                    Lesson("hi.u1.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        mc("What does नमस्ते mean?", "Hello", "Water", "Book"),
                        typed("Translate to Hindi: Thank you", "धन्यवाद"),
                        mc("Choose \"Yes\":", "हाँ", "नहीं", "आप")
                    ), hindiVocabU1)
                )
            ),
            Unit(
                "hi.u2", "Everyday things", "Objects around you",
                listOf(
                    Lesson("hi.u2.l1", "Food and water", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"Water\"?", "पानी", "घर", "किताब"),
                        typed("Translate to English: खाना", "food", "to eat", "eat"),
                        listen("पानी", "पानी", "खाना", "घर"),
                        ExerciseSpec.MatchPairs("Match the pairs",
                            listOf("पानी" to "Water", "खाना" to "Food", "घर" to "House")),
                        fill("मुझे ___ चाहिए। (I want water.)", "पानी", "घर", "किताब")
                    ), hindiVocabU2),
                    Lesson("hi.u2.l2", "People", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"Girl\"?", "लड़की", "लड़का", "किताब"),
                        typed("Translate to English: लड़का", "boy"),
                        ExerciseSpec.Scrambled("Build: \"The boy goes to school.\"",
                            listOf("लड़का", "स्कूल", "जाता", "है"), "लड़का स्कूल जाता है"),
                        listen("लड़की", "लड़की", "लड़का", "किताब")
                    ), hindiVocabU2),
                    Lesson("hi.u2.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        mc("What does किताब mean?", "Book", "Water", "House"),
                        typed("Translate to Hindi: House", "घर"),
                        ExerciseSpec.Correction("Fix the translation",
                            "लड़का = Girl", "लड़का = Boy")
                    ), hindiVocabU2)
                )
            ),
            Unit(
                "hi.u3", "Making sentences", "Describe and ask",
                listOf(
                    Lesson("hi.u3.l1", "Describing", Lesson.LessonKind.GRAMMAR, listOf(
                        mc("Which word means \"Good\"?", "अच्छा", "कैसे", "सीखना"),
                        fill("यह बहुत ___ है। (This is very good.)", "अच्छा", "पानी", "किताब"),
                        typed("Translate to English: स्कूल", "school")
                    ), hindiVocabU3),
                    Lesson("hi.u3.l2", "Questions", Lesson.LessonKind.TRANSLATION, listOf(
                        typed("Translate to Hindi: How are you?", "आप कैसे हैं?", "आप कैसे हैं", "तुम कैसे हो?"),
                        mc("What does कैसे mean?", "How", "Good", "School"),
                        ExerciseSpec.Scrambled("Build: \"How are you?\"",
                            listOf("आप", "कैसे", "हैं"), "आप कैसे हैं?")
                    ), hindiVocabU3),
                    Lesson("hi.u3.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        typed("Translate to Hindi: I learn Hindi.", "मैं हिन्दी सीखता हूँ।", "मैं हिंदी सीखता हूँ।", "मैं हिन्दी सीखती हूँ।"),
                        mc("Choose \"To learn\":", "सीखना", "अच्छा", "कैसे"),
                        listen("अच्छा", "अच्छा", "सीखना", "स्कूल")
                    ), hindiVocabU3)
                )
            )
        )
    )

    // ------------------------------------------------------------------
    // English course (learner is a Hindi speaker)
    // ------------------------------------------------------------------
    private val enVocab1 = listOf(
        vocab("en.book", "book", "किताब", "This is a book.", "यह एक किताब है।", "noun"),
        vocab("en.water", "water", "पानी", "I want water.", "मुझे पानी चाहिए।", "noun"),
        vocab("en.house", "house", "घर", "My house is nearby.", "मेरा घर पास में है।", "noun"),
        vocab("en.good", "good", "अच्छा", "This is good.", "यह अच्छा है।", "adjective")
    )
    private val enVocab2 = listOf(
        vocab("en.boy", "boy", "लड़का", "The boy runs.", "लड़का दौड़ता है।", "noun"),
        vocab("en.girl", "girl", "लड़की", "The girl reads.", "लड़की पढ़ती है।", "noun"),
        vocab("en.fine", "fine", "ठीक", "I am fine.", "मैं ठीक हूँ।", "adjective")
    )

    private val english = Course(
        languageCode = "en",
        units = listOf(
            Unit("en.u1", "First words", "Nouns and simple sentences",
                listOf(
                    Lesson("en.u1.l1", "Things", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"किताब\"?", "book", "water", "house"),
                        typed("Translate to Hindi: water", "पानी"),
                        listen("book", "book", "water", "house"),
                        ExerciseSpec.MatchPairs("Match the pairs",
                            listOf("book" to "किताब", "water" to "पानी", "house" to "घर"))
                    ), enVocab1),
                    Lesson("en.u1.l2", "Sentences", Lesson.LessonKind.GRAMMAR, listOf(
                        ExerciseSpec.Scrambled("Build: \"This is a book.\"",
                            listOf("This", "is", "a", "book"), "This is a book."),
                        fill("I want ___. (मुझे पानी चाहिए।)", "water", "book", "house"),
                        typed("Translate to English: यह एक किताब है।", "This is a book.", "This is a book")
                    ), enVocab1),
                    Lesson("en.u1.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        mc("Choose \"good\":", "good", "bad", "book"),
                        typed("Translate to Hindi: house", "घर")
                    ), enVocab1)
                )),
            Unit("en.u2", "People", "Talking about people",
                listOf(
                    Lesson("en.u2.l1", "Who is it?", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"लड़का\"?", "boy", "girl", "fine"),
                        typed("Translate to Hindi: girl", "लड़की"),
                        listen("boy", "boy", "girl", "fine")
                    ), enVocab2),
                    Lesson("en.u2.l2", "How are you", Lesson.LessonKind.TRANSLATION, listOf(
                        typed("Translate to Hindi: I am fine.", "मैं ठीक हूँ।", "मैं ठीक हूँ"),
                        mc("What does \"fine\" mean?", "ठीक", "अच्छा", "पानी"),
                        ExerciseSpec.Correction("Fix the translation", "boy = लड़की", "boy = लड़का")
                    ), enVocab2),
                    Lesson("en.u2.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        typed("Translate to English: मैं ठीक हूँ।", "I am fine.", "I am fine"),
                        mc("Choose \"girl\":", "girl", "boy", "book")
                    ), enVocab2)
                ))
        )
    )

    // ------------------------------------------------------------------
    // Spanish course (learner is an English speaker)
    // ------------------------------------------------------------------
    private val esVocab1 = listOf(
        vocab("es.hola", "hola", "hello", "¡Hola! ¿Cómo estás?", "Hello! How are you?", "interjection"),
        vocab("es.gracias", "gracias", "thank you", "Gracias por tu ayuda.", "Thank you for your help.", "interjection"),
        vocab("es.agua", "agua", "water", "Bebo agua.", "I drink water.", "noun"),
        vocab("es.pan", "pan", "bread", "El niño come pan.", "The boy eats bread.", "noun")
    )
    private val esVocab2 = listOf(
        vocab("es.casa", "casa", "house", "Mi casa es pequeña.", "My house is small.", "noun"),
        vocab("es.libro", "libro", "book", "Este libro es bueno.", "This book is good.", "noun"),
        vocab("es.nino", "niño", "boy", "El niño corre.", "The boy runs.", "noun")
    )

    private val spanish = Course(
        languageCode = "es",
        units = listOf(
            Unit("es.u1", "Primeros pasos", "Greetings and basics",
                listOf(
                    Lesson("es.u1.l1", "Greetings", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"hello\"?", "hola", "gracias", "agua"),
                        typed("Translate to English: gracias", "thank you", "thanks"),
                        listen("hola", "hola", "gracias", "pan"),
                        ExerciseSpec.MatchPairs("Match the pairs",
                            listOf("hola" to "hello", "gracias" to "thank you", "agua" to "water"))
                    ), esVocab1),
                    Lesson("es.u1.l2", "Food", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"bread\"?", "pan", "agua", "hola"),
                        typed("Translate to Spanish: water", "agua"),
                        fill("El niño come ___. (The boy eats bread.)", "pan", "agua", "hola")
                    ), esVocab1),
                    Lesson("es.u1.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        mc("Choose \"thank you\":", "gracias", "hola", "pan"),
                        typed("Translate to Spanish: hello", "hola")
                    ), esVocab1)
                )),
            Unit("es.u2", "En casa", "Everyday objects",
                listOf(
                    Lesson("es.u2.l1", "Things", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"house\"?", "casa", "libro", "niño"),
                        typed("Translate to English: libro", "book"),
                        listen("casa", "casa", "libro", "niño")
                    ), esVocab2),
                    Lesson("es.u2.l2", "People and things", Lesson.LessonKind.TRANSLATION, listOf(
                        typed("Translate to Spanish: The boy eats bread.", "El niño come pan.", "El niño come pan"),
                        mc("What does \"niño\" mean?", "boy", "girl", "house"),
                        ExerciseSpec.Scrambled("Build: \"This book is good.\"",
                            listOf("Este", "libro", "es", "bueno"), "Este libro es bueno.")
                    ), esVocab2),
                    Lesson("es.u2.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        typed("Translate to English: Mi casa es pequeña.", "My house is small.", "My house is small"),
                        mc("Choose \"book\":", "libro", "casa", "niño")
                    ), esVocab2)
                ))
        )
    )

    // ------------------------------------------------------------------
    // French course (learner is an English speaker)
    // ------------------------------------------------------------------
    private val frVocab1 = listOf(
        vocab("fr.bonjour", "bonjour", "hello", "Bonjour, ça va ?", "Hello, how are you?", "interjection"),
        vocab("fr.merci", "merci", "thank you", "Merci beaucoup.", "Thank you very much.", "interjection"),
        vocab("fr.eau", "eau", "water", "Elle boit de l'eau.", "She drinks water.", "noun"),
        vocab("fr.pain", "pain", "bread", "Le garçon mange du pain.", "The boy eats bread.", "noun")
    )
    private val frVocab2 = listOf(
        vocab("fr.maison", "maison", "house", "Ma maison est petite.", "My house is small.", "noun"),
        vocab("fr.livre", "livre", "book", "Ce livre est bon.", "This book is good.", "noun"),
        vocab("fr.garcon", "garçon", "boy", "Le garçon court.", "The boy runs.", "noun")
    )

    private val french = Course(
        languageCode = "fr",
        units = listOf(
            Unit("fr.u1", "Premiers pas", "Greetings and basics",
                listOf(
                    Lesson("fr.u1.l1", "Greetings", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"hello\"?", "bonjour", "merci", "eau"),
                        typed("Translate to English: merci", "thank you", "thanks"),
                        listen("bonjour", "bonjour", "merci", "pain"),
                        ExerciseSpec.MatchPairs("Match the pairs",
                            listOf("bonjour" to "hello", "merci" to "thank you", "eau" to "water"))
                    ), frVocab1),
                    Lesson("fr.u1.l2", "Food", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"bread\"?", "pain", "eau", "bonjour"),
                        typed("Translate to French: water", "eau", "de l'eau"),
                        fill("Le garçon mange du ___. (The boy eats bread.)", "pain", "eau", "merci")
                    ), frVocab1),
                    Lesson("fr.u1.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        mc("Choose \"thank you\":", "merci", "bonjour", "pain"),
                        typed("Translate to French: hello", "bonjour")
                    ), frVocab1)
                )),
            Unit("fr.u2", "À la maison", "Everyday objects",
                listOf(
                    Lesson("fr.u2.l1", "Things", Lesson.LessonKind.VOCAB, listOf(
                        mc("Which word means \"house\"?", "maison", "livre", "garçon"),
                        typed("Translate to English: livre", "book"),
                        listen("maison", "maison", "livre", "garçon")
                    ), frVocab2),
                    Lesson("fr.u2.l2", "Sentences", Lesson.LessonKind.TRANSLATION, listOf(
                        typed("Translate to French: The boy eats bread.", "Le garçon mange du pain.", "Le garçon mange du pain"),
                        mc("What does \"garçon\" mean?", "boy", "girl", "house"),
                        ExerciseSpec.Scrambled("Build: \"This book is good.\"",
                            listOf("Ce", "livre", "est", "bon"), "Ce livre est bon.")
                    ), frVocab2),
                    Lesson("fr.u2.l3", "Checkpoint", Lesson.LessonKind.CHECKPOINT, listOf(
                        typed("Translate to English: Ma maison est petite.", "My house is small.", "My house is small"),
                        mc("Choose \"book\":", "livre", "maison", "garçon")
                    ), frVocab2)
                ))
        )
    )

    val courses: List<Course> = listOf(hindi, english, spanish, french)

    private val byCode = courses.associateBy { it.languageCode }

    fun courseFor(code: String): Course? = byCode[code]

    /** Flat list of every vocabulary item in a course, for the practice pool. */
    fun allVocab(code: String): List<VocabItem> =
        courseFor(code)?.units?.flatMap { u -> u.lessons.flatMap { it.vocab } }?.distinctBy { it.id } ?: emptyList()
}
