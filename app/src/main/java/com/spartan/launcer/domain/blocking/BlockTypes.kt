package com.spartan.launcer.domain.blocking

enum class BlockReason { MANUAL, LIMIT, SCHEDULE, FOCUS, SHORTS }

data class BlockDecision(
    val shouldBlock: Boolean,
    val reason: BlockReason? = null
) {
    companion object {
        val ALLOW = BlockDecision(shouldBlock = false)
        fun block(reason: BlockReason) = BlockDecision(shouldBlock = true, reason = reason)
    }
}