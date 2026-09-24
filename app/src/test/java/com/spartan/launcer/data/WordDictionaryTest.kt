package com.spartan.launcer.data

import com.spartan.launcer.data.model.WordEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordDictionaryTest {

    private val raw = """
        [
          {
            "word": "ephemeral",
            "meaning": "lasting for a very short time",
            "examples": ["The moment was ephemeral.", "Fame proved ephemeral."],
            "synonyms": ["fleeting", "transient"],
            "antonyms": ["permanent", "eternal"]
          },
          {
            "word": "abate",
            "meaning": "to become less intense",
            "examples": ["The storm abated."],
            "synonyms": ["subside"],
            "antonyms": ["intensify"]
          }
        ]
    """.trimIndent()

    @Test
    fun parsesFullEntriesFromJson() {
        val entries = WordDictionary.parseEntries(raw)
        assertEquals(2, entries.size)
        assertEquals(
            WordEntry(
                word = "ephemeral",
                meaning = "lasting for a very short time",
                examples = listOf("The moment was ephemeral.", "Fame proved ephemeral."),
                synonyms = listOf("fleeting", "transient"),
                antonyms = listOf("permanent", "eternal")
            ),
            entries[0]
        )
    }

    @Test
    fun toleratesMissingOptionalArrays() {
        val parsed = WordDictionary.parseEntries(
            """[{"word":"brief","meaning":"short in duration"}]"""
        )
        val entry = parsed.single()
        assertTrue(entry.examples.isEmpty())
        assertTrue(entry.synonyms.isEmpty())
        assertTrue(entry.antonyms.isEmpty())
    }

    @Test
    fun handlesEmptyDocument() {
        assertTrue(WordDictionary.parseEntries("[]").isEmpty())
    }
}