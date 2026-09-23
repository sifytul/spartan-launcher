package com.spartan.launcer.data.model

import java.time.DayOfWeek

/**
 * A time window during which app blocking is active.
 *
 * [startMinute]/[endMinute] are minute-of-day in the range 0..1439.
 * When [endMinute] <= [startMinute] the schedule spans midnight.
 */
data class BlockSchedule(
    val id: String,
    val label: String,
    val startMinute: Int,
    val endMinute: Int,
    val days: Set<DayOfWeek>,
    val appliesToPackages: Set<String> = emptySet(),
    val enabled: Boolean = true
)