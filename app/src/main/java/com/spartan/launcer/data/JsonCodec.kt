package com.spartan.launcer.data

import com.spartan.launcer.data.model.BlockSchedule
import com.spartan.launcer.data.model.WordEntry
import java.time.DayOfWeek
import org.json.JSONArray
import org.json.JSONObject

/**
 * Pure JSON helpers for the collections stored in DataStore Preferences,
 * kept here so the encoding/decoding logic is unit-testable without Android.
 */
object JsonCodec {

    fun encodeStringSet(values: Set<String>): String =
        JSONArray(values.toList()).toString()

    fun decodeStringSet(raw: String): Set<String> {
        if (raw.isBlank() || raw == "null") return emptySet()
        val array = JSONArray(raw)
        return buildSet {
            for (i in 0 until array.length()) add(array.getString(i))
        }
    }

    fun encodeStringMap(values: Map<String, String>): String =
        JSONObject(values).toString()

    fun decodeStringMap(raw: String): Map<String, String> {
        if (raw.isBlank() || raw == "null") return emptyMap()
        val obj = JSONObject(raw)
        val result = mutableMapOf<String, String>()
        for (key in obj.keys()) result[key] = obj.getString(key)
        return result
    }

    fun encodeMinutesMap(values: Map<String, Int>): String =
        JSONObject(values).toString()

    fun decodeMinutesMap(raw: String): Map<String, Int> {
        if (raw.isBlank() || raw == "null") return emptyMap()
        val obj = JSONObject(raw)
        val result = mutableMapOf<String, Int>()
        for (key in obj.keys()) result[key] = obj.getInt(key)
        return result
    }

    fun encodeWord(entry: WordEntry): String =
        JSONObject()
            .put("word", entry.word)
            .put("meaning", entry.meaning)
            .put("examples", JSONArray(entry.examples))
            .put("synonyms", JSONArray(entry.synonyms))
            .put("antonyms", JSONArray(entry.antonyms))
            .toString()

    fun decodeWord(raw: String): WordEntry? {
        if (raw.isBlank() || raw == "null") return null
        val obj = JSONObject(raw)
        return WordEntry(
            word = obj.getString("word"),
            meaning = obj.getString("meaning"),
            examples = obj.optJSONArray("examples")?.toList() ?: emptyList(),
            synonyms = obj.optJSONArray("synonyms")?.toList() ?: emptyList(),
            antonyms = obj.optJSONArray("antonyms")?.toList() ?: emptyList()
        )
    }

    private fun org.json.JSONArray.toList(): List<String> =
        (0 until length()).map { getString(it) }

    fun encodeUsage(values: Map<String, Map<String, Int>>): String {
        val root = JSONObject()
        values.forEach { (dateKey, byPkg) ->
            root.put(dateKey, JSONObject(byPkg))
        }
        return root.toString()
    }

    fun decodeUsage(raw: String): Map<String, Map<String, Int>> {
        if (raw.isBlank() || raw == "null") return emptyMap()
        val root = JSONObject(raw)
        val result = mutableMapOf<String, Map<String, Int>>()
        for (dateKey in root.keys()) {
            val day = root.getJSONObject(dateKey)
            val byPkg = mutableMapOf<String, Int>()
            for (pkg in day.keys()) byPkg[pkg] = day.getInt(pkg)
            result[dateKey] = byPkg
        }
        return result
    }

    fun encodeSchedules(values: List<BlockSchedule>): String {
        val array = JSONArray()
        values.forEach { schedule ->
            val days = JSONArray()
            schedule.days.forEach { days.put(it.value) }
            val apps = JSONArray(schedule.appliesToPackages.toList())
            array.put(
                JSONObject()
                    .put("id", schedule.id)
                    .put("label", schedule.label)
                    .put("start", schedule.startMinute)
                    .put("end", schedule.endMinute)
                    .put("enabled", schedule.enabled)
                    .put("days", days)
                    .put("apps", apps)
            )
        }
        return array.toString()
    }

    fun decodeSchedules(raw: String): List<BlockSchedule> {
        if (raw.isBlank() || raw == "null") return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            val days = obj.getJSONArray("days")
            val daySet = buildSet {
                for (i in 0 until days.length()) add(DayOfWeek.of(days.getInt(i)))
            }
            val appsArray = obj.optJSONArray("apps")
            val apps = buildSet {
                if (appsArray != null) {
                    for (i in 0 until appsArray.length()) add(appsArray.getString(i))
                }
            }
            BlockSchedule(
                id = obj.getString("id"),
                label = obj.getString("label"),
                startMinute = obj.getInt("start"),
                endMinute = obj.getInt("end"),
                enabled = obj.optBoolean("enabled", true),
                days = daySet,
                appliesToPackages = apps
            )
        }
    }
}