package com.spartan.launcer.domain.blocking

import com.spartan.launcer.data.model.BlockSchedule

object BlockEvaluator {

    /**
     * Pure decision function for whether [packageName] must be blocked right now.
     * Deterministic and unit-testable; all runtime inputs are passed in.
     */
    fun decide(
        packageName: String,
        ownPackageName: String,
        blockedPackages: Set<String>,
        usageMinutes: Int,
        timeLimits: Map<String, Int>,
        activeSchedule: BlockSchedule?,
        focusActive: Boolean,
        focusAllowlist: Set<String>,
        tempAllowedUntil: Long,
        nowMs: Long
    ): BlockDecision {
        // Own package is the launcher / overlay / settings: never block yourself.
        if (packageName == ownPackageName) return BlockDecision.ALLOW
        // Temporary grace ("Use anyway") window.
        if (nowMs < tempAllowedUntil) return BlockDecision.ALLOW

        val active = activeSchedule
        if (active != null && appliesTo(active, packageName, blockedPackages)) {
            return BlockDecision.block(BlockReason.SCHEDULE)
        }
        if (focusActive && packageName !in focusAllowlist) {
            return BlockDecision.block(BlockReason.FOCUS)
        }
        val limit = timeLimits[packageName]
        if (limit != null && usageMinutes >= limit) {
            return BlockDecision.block(BlockReason.LIMIT)
        }
        if (packageName in blockedPackages) {
            return BlockDecision.block(BlockReason.MANUAL)
        }
        return BlockDecision.ALLOW
    }

    private fun appliesTo(schedule: BlockSchedule, packageName: String, blockedPackages: Set<String>): Boolean =
        if (schedule.appliesToPackages.isEmpty()) {
            packageName in blockedPackages
        } else {
            packageName in schedule.appliesToPackages
        }
}