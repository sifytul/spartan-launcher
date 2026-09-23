package com.spartan.launcer.domain.focus

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.spartan.launcer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Runs a focus/pomodoro session in memory: FOCUS for the configured minutes,
 * then an automatic short BREAK, then idle. While the session is in its FOCUS
 * phase the app blocker only allows favorite apps (the allowlist).
 */
class FocusController(
    private val context: Context,
    private val appScope: CoroutineScope,
    private val defaultBreakMinutes: Int = 5
) {

    private val _session = MutableStateFlow(FocusSession())
    val session: StateFlow<FocusSession> = _session.asStateFlow()

    private var runJob: Job? = null

    fun startFocus(durationMinutes: Int) {
        stopInternal()
        ensureChannel()
        val totalMs = durationMinutes * 60_000L
        _session.value = FocusSession(
            phase = FocusPhase.FOCUS,
            startedAtMs = System.currentTimeMillis(),
            totalMs = totalMs,
            remainingMs = totalMs
        )
        runJob = appScope.launch {
            var target = _session.value.totalMs
            while (isActive) {
                delay(1_000)
                val current = _session.value
                if (current.phase == FocusPhase.IDLE) break
                if (current.paused) continue
                val remaining = current.remainingMs - 1_000
                if (remaining > 0) {
                    _session.value = current.copy(remainingMs = remaining)
                    continue
                }
                when (current.phase) {
                    FocusPhase.FOCUS -> {
                        notifySessionEnded()
                        val breakMs = defaultBreakMinutes * 60_000L
                        _session.value = FocusSession(
                            phase = FocusPhase.BREAK,
                            startedAtMs = System.currentTimeMillis(),
                            totalMs = breakMs,
                            remainingMs = breakMs
                        )
                    }
                    FocusPhase.BREAK -> {
                        _session.value = FocusSession()
                        break
                    }
                    FocusPhase.IDLE -> break
                }
            }
        }
    }

    fun pause() {
        if (_session.value.isActive && !_session.value.paused) {
            _session.value = _session.value.copy(paused = true)
        }
    }

    fun resume() {
        if (_session.value.paused) {
            _session.value = _session.value.copy(paused = false)
        }
    }

    fun stop() = stopInternal()

    private fun stopInternal() {
        runJob?.cancel()
        runJob = null
        _session.value = FocusSession()
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Focus sessions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Reminders when a focus session finishes" }
        )
    }

    private fun notifySessionEnded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            SESSION_END_NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_focus)
                .setContentTitle("Focus session complete")
                .setContentText("Nice work. Take a short break.")
                .setAutoCancel(true)
                .build()
        )
    }

    companion object {
        private const val CHANNEL_ID = "focus_sessions"
        private const val SESSION_END_NOTIFICATION_ID = 1
    }
}