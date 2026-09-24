package com.spartan.launcer.data

import android.content.Context
import com.spartan.launcer.data.model.WordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Bundled word dictionary (assets/dictionary.json). Always available offline;
 * serves both as the source of fallback entries and as the candidate pool the
 * word-of-the-day feature picks from before trying the network API.
 */
class WordDictionary(
    private val context: Context,
    private val assetName: String = "dictionary.json"
) {

    private var cached: List<WordEntry>? = null
    private val mutex = Mutex()

    suspend fun entries(): List<WordEntry> = mutex.withLock {
        cached ?: loadFromAssets().also { cached = it }
    }

    suspend fun randomEntry(excluding: String? = null): WordEntry? {
        val all = entries()
        if (all.isEmpty()) return null
        val pool = if (excluding == null) all else all.filter { it.word != excluding }
        return pool.randomOrNull() ?: all.random()
    }

    suspend fun entryByWord(word: String): WordEntry? {
        if (word.isBlank()) return null
        val target = word.lowercase()
        return entries().firstOrNull { it.word.lowercase() == target }
    }

    private suspend fun loadFromAssets(): List<WordEntry> = withContext(Dispatchers.IO) {
        val bytes = context.assets.open(assetName).use { it.readBytes() }
        parseEntries(String(bytes, Charsets.UTF_8))
    }

    companion object {
        fun parseEntries(raw: String): List<WordEntry> {
            val array = JSONArray(raw)
            return buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        WordEntry(
                            word = obj.getString("word"),
                            meaning = obj.getString("meaning"),
                            examples = obj.optJSONArray("examples").toStringList(),
                            synonyms = obj.optJSONArray("synonyms").toStringList(),
                            antonyms = obj.optJSONArray("antonyms").toStringList()
                        )
                    )
                }
            }
        }

        private fun org.json.JSONArray?.toStringList(): List<String> {
            if (this == null) return emptyList()
            return (0 until length()).map { getString(it) }
        }
    }
}