package com.spartan.launcer.domain.blocking

import com.spartan.launcer.data.model.BlockSchedule
import java.time.LocalDateTime

object ScheduleEvaluator {

    fun findActive(now: LocalDateTime, schedules: List<BlockSchedule>): BlockSchedule? =
        schedules.firstOrNull { isActiveAt(now, it) }

    fun isActiveAt(now: LocalDateTime, schedule: BlockSchedule): Boolean {
        if (!schedule.enabled) return false
        val minuteOfDay = now.hour * 60 + now.minute
        val today = now.dayOfWeek
        return if (schedule.endMinute > schedule.startMinute) {
            today in schedule.days && minuteOfDay in schedule.startMinute until schedule.endMinute
        } else {
            (today in schedule.days && minuteOfDay >= schedule.startMinute) ||
                (today.minus(1) in schedule.days && minuteOfDay < schedule.endMinute)
        }
    }
}