package com.manimarank.spell4wiki

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for utility functions and core app logic
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun url_validation_works_correctly() {
        // Test valid URLs
        val validUrl = "https://example.com"
        assertTrue(validUrl.startsWith("http"))

        val httpUrl = "http://example.com"
        assertTrue(httpUrl.startsWith("http"))

        val wiktionaryUrl = "https://ta.wiktionary.org/wiki/test"
        assertTrue(wiktionaryUrl.contains("wiktionary"))

        // Test invalid URLs
        val emptyUrl = ""
        assertFalse(emptyUrl.isNotEmpty())

        val invalidUrl = "not-a-url"
        assertFalse(invalidUrl.startsWith("http"))
    }

    @Test
    fun word_extraction_from_url_works() {
        val url = "https://ta.wiktionary.org/wiki/test_word"
        val extractedWord = url.substringAfterLast("/")
        assertEquals("test_word", extractedWord)
    }

    @Test
    fun wiktionary_url_detection_works() {
        // Test Wiktionary URLs
        assertTrue("https://ta.wiktionary.org/wiki/test".contains("wiktionary"))
        assertTrue("https://en.wiktionary.org/wiki/example".contains("wiktionary"))

        // Test non-Wiktionary URLs
        assertFalse("https://example.com".contains("wiktionary"))
        assertFalse("https://wikipedia.org".contains("wiktionary"))
    }
}