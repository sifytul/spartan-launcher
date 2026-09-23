package com.spartan.launcer.domain.focus

enum class FocusPhase { IDLE, FOCUS, BREAK }

data class FocusSession(
    val phase: FocusPhase = FocusPhase.IDLE,
    val startedAtMs: Long = 0L,
    val totalMs: Long = 0L,
    val remainingMs: Long = 0L,
    val paused: Boolean = false
) {
    val isActive: Boolean get() = phase != FocusPhase.IDLE
    val blocking: Boolean get() = phase == FocusPhase.FOCUS && !paused
}