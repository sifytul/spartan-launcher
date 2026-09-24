package com.spartan.launcer.domain

import com.spartan.launcer.data.DictionaryApi
import com.spartan.launcer.data.WordDictionary
import com.spartan.launcer.data.WordOfTheDayStore
import com.spartan.launcer.data.model.WordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Orchestrates the word-of-the-day feature: on each unlock a candidate is
 * picked from the bundled dictionary, enriched from the network API when
 * available, and persisted so it can be shown and looked up later.
 */
class WordOfTheDayRepository(
    private val dictionary: WordDictionary,
    private val api: DictionaryApi,
    private val store: WordOfTheDayStore
) {

    fun currentWord(): Flow<WordEntry?> = store.current.map { it.entry }

    suspend fun currentStoredEntry(): WordEntry? = currentWord().first()

    suspend fun entryByWord(word: String): WordEntry? = dictionary.entryByWord(word)

    /**
     * Picks a fresh word (never the previous one) and saves it. When the API
     * succeeds, its richer data is stored; otherwise the bundled definition is
     * used so the feature always works offline.
     */
    suspend fun advanceWord() {
        val previous = currentStoredEntry()
        val candidate = dictionary.randomEntry(excluding = previous?.word) ?: return
        val enriched = runCatching { api.fetch(candidate.word) }
            .getOrNull()
            ?.takeIf { it.meaning.isNotBlank() }
        store.save(enriched ?: candidate)
    }
}