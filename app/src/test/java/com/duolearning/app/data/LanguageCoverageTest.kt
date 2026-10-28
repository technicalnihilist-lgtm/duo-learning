package com.duolearning.app.data

import com.duolearning.app.data.content.LanguageCatalog
import com.duolearning.app.data.content.SeedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the honesty rule: a language is only flagged learnable when a real
 * course exists for it, and only flagged translatable when the engine ships a
 * model. These tests fail loudly if content and catalogue drift apart.
 */
class LanguageCoverageTest {

    @Test fun `every learnable language has a real course`() {
        LanguageCatalog.learnable.forEach { lang ->
            val course = SeedContent.courseFor(lang.code)
            assertTrue("Missing course for ${lang.code}", course != null)
            assertTrue("Course ${lang.code} has no lessons", (course?.lessonCount ?: 0) > 0)
        }
    }

    @Test fun `every shipped course has exercises and vocabulary`() {
        SeedContent.courses.forEach { course ->
            assertTrue(course.exerciseCount > 0)
            assertTrue(SeedContent.allVocab(course.languageCode).isNotEmpty())
        }
    }

    @Test fun `unsupported languages are not marked translatable`() {
        listOf("ml", "or", "pa", "sa", "la").forEach { code ->
            val lang = LanguageCatalog.requireByCode(code)
            assertFalse("$code should not be translatable", lang.translateSupported)
        }
    }

    @Test fun `supported indic languages are marked translatable`() {
        listOf("hi", "bn", "ta", "te", "kn", "mr", "gu", "ur", "en").forEach { code ->
            assertTrue("$code should be translatable", LanguageCatalog.requireByCode(code).translateSupported)
        }
    }

    @Test fun `urdu is flagged right to left`() {
        assertTrue(LanguageCatalog.requireByCode("ur").isRtl)
        assertTrue(LanguageCatalog.requireByCode("ar").isRtl)
        assertFalse(LanguageCatalog.requireByCode("hi").isRtl)
    }

    @Test fun `search finds a language by native name`() {
        assertEquals("hi", LanguageCatalog.search("हिन्दी").first().code)
    }
}
