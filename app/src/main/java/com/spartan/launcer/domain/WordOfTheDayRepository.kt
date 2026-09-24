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
     * Picks a fresh word (never the previous one) and saves it. The bundled
     * definition is saved first so the change is visible immediately on every
     * unlock even offline, then upgraded with richer API data when the network
     * answers (only if the word was not replaced meanwhile).
     *
     * [skipIfAdvancedWithinMs] ignores the call when a word was already picked
     * very recently (e.g. when both the unlock receiver and the home screen
     * detect the same unlock), so a single unlock advances the word exactly
     * once.
     */
    suspend fun advanceWord(skipIfAdvancedWithinMs: Long = 0) {
        if (skipIfAdvancedWithinMs > 0) {
            val current = store.current.first()
            if (System.currentTimeMillis() - current.pickedAt < skipIfAdvancedWithinMs) return
        }
        val previous = currentStoredEntry()
        val candidate = dictionary.randomEntry(excluding = previous?.word) ?: return
        store.save(candidate)
        val enriched = runCatching { api.fetch(candidate.word) }
            .getOrNull()
            ?.takeIf { it.meaning.isNotBlank() }
        if (enriched != null && currentStoredEntry()?.word == candidate.word) {
            store.save(enriched)
        }
    }
}