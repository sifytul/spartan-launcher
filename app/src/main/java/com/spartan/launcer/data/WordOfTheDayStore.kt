package com.spartan.launcer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spartan.launcer.data.model.WordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wordDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "word_of_the_day"
)

/**
 * Persists the current word-of-the-day so it survives process death and is
 * available to both the home screen and the unlock receiver.
 */
class WordOfTheDayStore(private val context: Context) {

    data class Current(val entry: WordEntry?, val pickedAt: Long)

    private object Keys {
        val CURRENT_WORD = stringPreferencesKey("current_word")
        val PICKED_AT = longPreferencesKey("picked_at")
    }

    val current: Flow<Current> = context.wordDataStore.data.map { prefs ->
        Current(
            entry = JsonCodec.decodeWord(prefs[Keys.CURRENT_WORD].orEmpty()),
            pickedAt = prefs[Keys.PICKED_AT] ?: 0L
        )
    }

    suspend fun save(entry: WordEntry, now: Long = System.currentTimeMillis()) {
        context.wordDataStore.edit { prefs ->
            prefs[Keys.CURRENT_WORD] = JsonCodec.encodeWord(entry)
            prefs[Keys.PICKED_AT] = now
        }
    }
}