package com.spartan.launcer.data

import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Tracks which package is currently on screen (fed by the accessibility
 * service's WINDOW_STATE_CHANGED events) and accrues per-package foreground
 * minutes at one-minute granularity. Persists the whole usage map back to
 * [SettingsDataStore] every minute so live daily limits can be enforced.
 *
 * Note on accuracy: this is a foreground-session approximation and loses the
 * final partial minute on process death; the UsageStats-backed history page
 * remains the authoritative record of past screen time.
 */
class ForegroundUsageTracker(
    private val ownPackageName: String,
    private val settingsDataStore: SettingsDataStore,
    private val scope: CoroutineScope
) {

    private val ignorePackages = setOf(ownPackageName, "android", "com.android.systemui")

    private val _usage = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val usage: StateFlow<Map<String, Map<String, Int>>> = _usage.asStateFlow()

    private val _currentForeground = MutableStateFlow<String?>(null)
    val currentForeground: StateFlow<String?> = _currentForeground.asStateFlow()

    private var sessionPackage: String? = null
    private var sessionStartMs: Long? = null

    init {
        scope.launch {
            _usage.value = settingsDataStore.usageMinutes.first()
        }
        scope.launch {
            while (scope.isActive) {
                flush()
                delay(60_000)
            }
        }
    }

    fun onForegroundPackage(packageName: String?) {
        closeOutSession(System.currentTimeMillis())
        if (packageName != null && packageName !in ignorePackages) {
            sessionPackage = packageName
            sessionStartMs = System.currentTimeMillis()
        }
        _currentForeground.value = packageName
    }

    fun minutesUsedToday(packageName: String): Int {
        val today = LocalDate.now().toString()
        return _usage.value[today]?.get(packageName) ?: 0
    }

    private fun closeOutSession(nowMs: Long) {
        val pkg = sessionPackage ?: return
        val start = sessionStartMs ?: return
        val elapsedMinutes = ((nowMs - start) / 60_000).toInt()
        if (elapsedMinutes > 0) {
            val today = LocalDate.now().toString()
            val updated = _usage.value.toMutableMap()
            val day = (updated[today] ?: emptyMap()).toMutableMap()
            day[pkg] = (day[pkg] ?: 0) + elapsedMinutes
            updated[today] = day
            _usage.value = updated
        }
        sessionPackage = null
        sessionStartMs = null
    }

    private suspend fun flush() {
        closeOutSession(System.currentTimeMillis())
        settingsDataStore.setUsageMinutes(_usage.value)
    }
}