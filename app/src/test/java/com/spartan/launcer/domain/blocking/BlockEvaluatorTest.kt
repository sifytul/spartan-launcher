package com.spartan.launcer.domain.blocking

import com.spartan.launcer.data.model.BlockSchedule
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class BlockEvaluatorTest {

    private val own = "com.spartan.launcer"
    private val app = "com.example.app"
    private val now = 1_000_000L

    private fun activeSchedule(appliesTo: Set<String> = emptySet()) = BlockSchedule(
        id = "s",
        label = "s",
        startMinute = 0,
        endMinute = 1440,
        days = DayOfWeek.entries.toSet(),
        appliesToPackages = appliesTo
    )

    @Test
    fun ownPackageAlwaysAllowed() {
        assertEquals(
            BlockDecision.ALLOW,
            BlockEvaluator.decide(
                packageName = own,
                ownPackageName = own,
                blockedPackages = setOf(own),
                usageMinutes = 999,
                timeLimits = mapOf(own to 1),
                activeSchedule = activeSchedule(setOf(own)),
                focusActive = true,
                focusAllowlist = emptySet(),
                tempAllowedUntil = 0,
                nowMs = now
            )
        )
    }

    @Test
    fun manualBlockBlocksApp() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = setOf(app),
            usageMinutes = 0, timeLimits = emptyMap(), activeSchedule = null,
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockReason.MANUAL, decision.reason)
    }

    @Test
    fun temporaryGraceOverridesBlock() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = setOf(app),
            usageMinutes = 0, timeLimits = emptyMap(), activeSchedule = null,
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = now + 60_000, nowMs = now
        )
        assertEquals(BlockDecision.ALLOW, decision)
    }

    @Test
    fun scheduleBlocksRegardlessOfManualList() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = setOf("other.app"),
            usageMinutes = 0, timeLimits = emptyMap(),
            activeSchedule = activeSchedule(setOf(app)),
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockReason.SCHEDULE, decision.reason)
    }

    @Test
    fun blockAllScheduleOnlyAppliesToManuallyBlocked() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = setOf("other.app"),
            usageMinutes = 0, timeLimits = emptyMap(),
            activeSchedule = activeSchedule(emptySet()),
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockDecision.ALLOW, decision)
        val blocked = BlockEvaluator.decide(
            packageName = "other.app", ownPackageName = own,
            blockedPackages = setOf("other.app"),
            usageMinutes = 0, timeLimits = emptyMap(),
            activeSchedule = activeSchedule(emptySet()),
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockReason.SCHEDULE, blocked.reason)
    }

    @Test
    fun limitReachedBlocksAppNotManuallyBlocked() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = emptySet(),
            usageMinutes = 32, timeLimits = mapOf(app to 30), activeSchedule = null,
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockReason.LIMIT, decision.reason)
    }

    @Test
    fun limitNotReachedAllowsApp() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = emptySet(),
            usageMinutes = 15, timeLimits = mapOf(app to 30), activeSchedule = null,
            focusActive = false, focusAllowlist = emptySet(),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockDecision.ALLOW, decision)
    }

    @Test
    fun focusBlocksNonAllowlistedButAllowsAllowlisted() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = emptySet(),
            usageMinutes = 0, timeLimits = emptyMap(), activeSchedule = null,
            focusActive = true, focusAllowlist = setOf("safe.app"),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockReason.FOCUS, decision.reason)

        val allowed = BlockEvaluator.decide(
            packageName = "safe.app", ownPackageName = own, blockedPackages = emptySet(),
            usageMinutes = 0, timeLimits = emptyMap(), activeSchedule = null,
            focusActive = true, focusAllowlist = setOf("safe.app"),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockDecision.ALLOW, allowed)
    }

    @Test
    fun scheduleTakesPriorityOverFocus() {
        val decision = BlockEvaluator.decide(
            packageName = app, ownPackageName = own, blockedPackages = emptySet(),
            usageMinutes = 0, timeLimits = emptyMap(),
            activeSchedule = activeSchedule(setOf(app)),
            focusActive = true, focusAllowlist = setOf(app),
            tempAllowedUntil = 0, nowMs = now
        )
        assertEquals(BlockReason.SCHEDULE, decision.reason)
    }
}