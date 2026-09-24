package com.spartan.launcer.data

import com.spartan.launcer.data.model.WordEntry
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Thin client for the free DictionaryAPI.dev endpoint. Returns a [WordEntry]
 * enriched with the best definition, up to two example sentences and any
 * synonyms/antonyms the API provides. Fails fast so callers can fall back to
 * the bundled dictionary.
 */
class DictionaryApi(private val timeoutMs: Int = 5_000) {

    suspend fun fetch(word: String): WordEntry = withContext(Dispatchers.IO) {
        fetchBlocking(word)
    }

    private fun fetchBlocking(word: String): WordEntry {
        val url = URL(
            "https://api.dictionaryapi.dev/api/v2/entries/en/${word.lowercase()}"
        )
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = timeoutMs
        connection.readTimeout = timeoutMs
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("Dictionary API returned HTTP $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return parseEntry(JSONArray(body))
        } finally {
            connection.disconnect()
        }
    }

    companion object {

        fun parseEntry(root: JSONArray): WordEntry {
            val head = root.optJSONObject(0) ?: throw IOException("Empty API response")
            val word = head.getString("word")

            val meanings = head.optJSONArray("meanings") ?: JSONArray()
            val definitions = buildList {
                for (m in 0 until meanings.length()) {
                    val meaning = meanings.getJSONObject(m)
                    val defs = meaning.optJSONArray("definitions") ?: continue
                    for (d in 0 until defs.length()) {
                        add(defs.getJSONObject(d))
                    }
                }
            }

            val meaning = definitions
                .mapNotNull { it.stringOrNull("definition") }
                .firstOrNull() ?: ""

            val examples = linkedSetOf<String>()
            definitions.forEach { def ->
                def.stringOrNull("example")?.let { examples.add(it) }
                def.optJSONArray("examples").toStringList().forEach { examples.add(it) }
            }

            val synonyms = linkedSetOf<String>()
            val antonyms = linkedSetOf<String>()
            head.optJSONArray("synonyms").toStringList().forEach { synonyms.add(it) }
            head.optJSONArray("antonyms").toStringList().forEach { antonyms.add(it) }
            definitions.forEach { def ->
                def.optJSONArray("synonyms").toStringList().forEach { synonyms.add(it) }
                def.optJSONArray("antonyms").toStringList().forEach { antonyms.add(it) }
            }

            return WordEntry(
                word = word,
                meaning = meaning,
                examples = examples.take(2).toList(),
                synonyms = synonyms.take(8).toList(),
                antonyms = antonyms.take(8).toList()
            )
        }

        private fun org.json.JSONObject.stringOrNull(name: String): String? =
            (opt(name) as? String)?.takeIf { it.isNotBlank() }

        private fun org.json.JSONArray?.toStringList(): List<String> {
            if (this == null) return emptyList()
            return buildList {
                for (i in 0 until length()) {
                    val raw = opt(i)
                    val text = when (raw) {
                        is String -> raw
                        is JSONObject -> (raw.get("text") as? String).orEmpty()
                        else -> ""
                    }
                    if (text.isNotBlank()) add(text)
                }
            }
        }
    }
}