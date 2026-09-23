package com.spartan.launcer.domain.blocking

import com.spartan.launcer.data.model.BlockSchedule
import java.time.DayOfWeek
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleEvaluatorTest {

    private fun schedule(
        startMinute: Int,
        endMinute: Int,
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        enabled: Boolean = true
    ) = BlockSchedule(
        id = "t",
        label = "test",
        startMinute = startMinute,
        endMinute = endMinute,
        enabled = enabled,
        days = days,
        appliesToPackages = emptySet()
    )

    @Test
    fun sameDayWindowActiveInsideAndInactiveOutside() {
        val s = schedule(startMinute = 8 * 60, endMinute = 12 * 60)
        assertTrue(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 23, 9, 0), s))
        assertTrue(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 23, 8, 0), s))
        assertFalse(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 23, 7, 59), s))
        assertFalse(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 23, 12, 0), s))
    }

    @Test
    fun sameDayWindowRespectsDays() {
        val s = schedule(
            startMinute = 0,
            endMinute = 1440,
            days = setOf(DayOfWeek.MONDAY)
        )
        assertTrue(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 21, 10, 0), s)) // Monday
        assertFalse(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 22, 10, 0), s)) // Tuesday
    }

    @Test
    fun midnightSpanActiveInBothSegmentsAndUsesPreviousDay() {
        val s = schedule(
            startMinute = 22 * 60,
            endMinute = 7 * 60,
            days = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )
        // Saturday night segment (22:00 on Saturday)
        assertTrue(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 26, 23, 30), s))
        // Sunday early morning is still Saturday's segment (carried past midnight)
        assertTrue(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 27, 6, 0), s))
        // Middle of Sunday is outside the overnight window
        assertFalse(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 27, 12, 0), s))
        // Sunday night 23:00 is Sunday's segment, active
        assertTrue(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 27, 23, 0), s))
    }

    @Test
    fun disabledScheduleNeverActive() {
        val s = schedule(startMinute = 0, endMinute = 1440, enabled = false)
        assertFalse(ScheduleEvaluator.isActiveAt(LocalDateTime.of(2026, 9, 21, 10, 0), s))
    }

    @Test
    fun findActiveReturnsFirstMatchingSchedule() {
        val first = schedule(startMinute = 0, endMinute = 1440)
        val second = schedule(startMinute = 0, endMinute = 1440)
        val now = LocalDateTime.of(2026, 9, 21, 10, 0)
        assertEquals(first, ScheduleEvaluator.findActive(now, listOf(first, second)))
        assertNull(ScheduleEvaluator.findActive(now, emptyList()))
    }
}