package com.spartan.launcer.data

import com.spartan.launcer.data.model.BlockSchedule
import com.spartan.launcer.data.model.WordEntry
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonCodecTest {

    @Test
    fun stringSetRoundTrip() {
        val values = setOf("a.pkg", "b.pkg", "c.pkg")
        assertEquals(values, JsonCodec.decodeStringSet(JsonCodec.encodeStringSet(values)))
    }

    @Test
    fun stringSetHandlesEmptyAndInvalid() {
        assertTrue(JsonCodec.decodeStringSet("").isEmpty())
        assertTrue(JsonCodec.decodeStringSet("null").isEmpty())
        assertTrue(JsonCodec.decodeStringSet("   ").isEmpty())
        assertTrue(JsonCodec.decodeStringSet("[\"x\"]").contains("x"))
    }

    @Test
    fun minutesMapRoundTrip() {
        val values = mapOf("a.pkg" to 30, "b.pkg" to 0, "c.pkg" to 120)
        assertEquals(values, JsonCodec.decodeMinutesMap(JsonCodec.encodeMinutesMap(values)))
    }

    @Test
    fun stringMapRoundTrip() {
        val values = mapOf("a.pkg" to "Focus", "b.pkg" to "Work")
        assertEquals(values, JsonCodec.decodeStringMap(JsonCodec.encodeStringMap(values)))
    }

    @Test
    fun schedulesRoundTripIncludingMidnightSpan() {
        val schedules = listOf(
            BlockSchedule(
                id = "s1",
                label = "Work focus",
                startMinute = 8 * 60,
                endMinute = 12 * 60,
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                appliesToPackages = setOf("com.instagram.android", "com.google.android.youtube"),
                enabled = true
            ),
            BlockSchedule(
                id = "s2",
                label = "Sleep",
                startMinute = 22 * 60,
                endMinute = 7 * 60,
                days = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                appliesToPackages = emptySet(),
                enabled = false
            )
        )
        assertEquals(schedules, JsonCodec.decodeSchedules(JsonCodec.encodeSchedules(schedules)))
    }

    @Test
    fun schedulesRoundTripEmpty() {
        assertEquals(emptyList<BlockSchedule>(), JsonCodec.decodeSchedules(""))
        assertEquals(emptyList<BlockSchedule>(), JsonCodec.decodeSchedules("null"))
    }

    @Test
    fun usageNestedMapRoundTrip() {
        val usage = mapOf(
            "2026-09-23" to mapOf("a.pkg" to 45, "b.pkg" to 12),
            "2026-09-22" to mapOf("c.pkg" to 5)
        )
        assertEquals(usage, JsonCodec.decodeUsage(JsonCodec.encodeUsage(usage)))
    }

    @Test
    fun wordRoundTrip() {
        val entry = WordEntry(
            word = "ephemeral",
            meaning = "lasting for a very short time",
            examples = listOf("The moment was ephemeral.", "Fame proved ephemeral."),
            synonyms = listOf("fleeting", "transient"),
            antonyms = listOf("permanent", "eternal")
        )
        assertEquals(entry, JsonCodec.decodeWord(JsonCodec.encodeWord(entry)))
    }

    @Test
    fun wordDecodeHandlesEmptyAndMissingFields() {
        assertNull(JsonCodec.decodeWord(""))
        assertNull(JsonCodec.decodeWord("null"))
        assertNull(JsonCodec.decodeWord("   "))
        val partial = JsonCodec.decodeWord("""{"word":"x","meaning":"y"}""")
        assertTrue(partial != null)
        assertTrue(partial!!.examples.isEmpty())
        assertTrue(partial.synonyms.isEmpty())
        assertTrue(partial.antonyms.isEmpty())
    }
}