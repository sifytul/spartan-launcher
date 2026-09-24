package com.spartan.launcer.data

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryApiTest {

    @Test
    fun parsesMeaningExamplesAndSynAnt() {
        val body = """
            [
              {
                "word": "abate",
                "meanings": [
                  {
                    "partOfSpeech": "verb",
                    "definitions": [
                      {
                        "definition": "to become less intense or widespread",
                        "example": "The storm finally began to abate.",
                        "synonyms": ["subside", "diminish"],
                        "antonyms": ["intensify"]
                      },
                      {
                        "definition": "(of a person) to grow less in strength",
                        "example": "His fever did not abate.",
                        "synonyms": ["lessen"],
                        "antonyms": ["rise"]
                      }
                    ]
                  }
                ]
              }
            ]
        """.trimIndent()

        val entry = DictionaryApi.parseEntry(JSONArray(body))

        assertEquals("abate", entry.word)
        assertEquals("to become less intense or widespread", entry.meaning)
        assertEquals(
            listOf("The storm finally began to abate.", "His fever did not abate."),
            entry.examples
        )
        assertEquals(listOf("subside", "diminish", "lessen"), entry.synonyms)
        assertEquals(listOf("intensify", "rise"), entry.antonyms)
    }

    @Test
    fun picksFirstMeaningAndSkipsBlankExamples() {
        val body = """
            [
              {
                "word": "block",
                "meanings": [
                  {
                    "partOfSpeech": "noun",
                    "definitions": [
                      { "definition": "a solid piece of material", "example": "" }
                    ]
                  },
                  {
                    "partOfSpeech": "verb",
                    "definitions": [
                      { "definition": "to prevent movement", "example": "The barrier blocks the road." }
                    ]
                  }
                ]
              }
            ]
        """.trimIndent()

        val entry = DictionaryApi.parseEntry(JSONArray(body))
        assertEquals("block", entry.word)
        assertEquals("a solid piece of material", entry.meaning)
        assertEquals(listOf("The barrier blocks the road."), entry.examples)
    }

    @Test
    fun resultsInEmptyEntryWhenResponseHasNoDefinitions() {
        val entry = DictionaryApi.parseEntry(JSONArray("""[{"word":"xyz"}]"""))
        assertEquals("xyz", entry.word)
        assertTrue(entry.meaning.isEmpty())
        assertTrue(entry.examples.isEmpty())
    }
}