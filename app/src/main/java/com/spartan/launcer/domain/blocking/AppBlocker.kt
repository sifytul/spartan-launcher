package com.spartan.launcer.domain.blocking

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.spartan.launcer.data.ForegroundUsageTracker
import com.spartan.launcer.data.SettingsDataStore
import com.spartan.launcer.data.model.LauncherSettings
import com.spartan.launcer.domain.focus.FocusController
import com.spartan.launcer.service.SpartanAccessibilityService
import com.spartan.launcer.ui.block.BlockOverlayActivity
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Decides when to intervene. Consumes the foreground package stream from
 * [ForegroundUsageTracker] together with the latest settings and, when a
 * decision says block, launches the [BlockOverlayActivity] (if "Display over
 * other apps" is granted) or bounces the user home via the accessibility
 * service as a fallback.
 */
class AppBlocker(
    private val context: Context,
    private val appScope: CoroutineScope,
    private val tracker: ForegroundUsageTracker,
    private val settingsDataStore: SettingsDataStore,
    private val focusController: FocusController
) {

    private val ownPackageName = context.packageName
    private val tempAllow = mutableMapOf<String, Long>()
    private val overlayVisible = AtomicBoolean(false)

    @Volatile
    private var accessibilityService: SpartanAccessibilityService? = null

    private val evaluationTick = MutableStateFlow(0L)

    init {
        appScope.launch {
            while (appScope.isActive) {
                delay(30_000)
                evaluationTick.value = System.currentTimeMillis()
            }
        }
        appScope.launch {
            combine(
                settingsDataStore.settings,
                tracker.currentForeground,
                evaluationTick
            ) { settings, foreground, tick ->
                Triple(settings, foreground, tick)
            }.collect { (settings, foreground, _) ->
                evaluate(settings, foreground)
            }
        }
    }

    fun connectAccessibility(service: SpartanAccessibilityService) {
        accessibilityService = service
    }

    fun disconnectAccessibility() {
        accessibilityService = null
    }

    fun onOverlayShown() {
        overlayVisible.set(true)
    }

    fun onOverlayClosed() {
        overlayVisible.set(false)
    }

    fun grantTemporaryAccess(packageName: String, minutes: Int) {
        if (minutes > 0) {
            tempAllow[packageName] = System.currentTimeMillis() + minutes * 60_000L
        }
        onOverlayClosed()
    }

    private fun evaluate(settings: LauncherSettings, foreground: String?) {
        if (foreground == null || foreground == ownPackageName) return
        val nowMs = System.currentTimeMillis()
        val focusSession = focusController.session.value
        val decision = BlockEvaluator.decide(
            packageName = foreground,
            ownPackageName = ownPackageName,
            blockedPackages = settings.blockedPackages,
            usageMinutes = tracker.minutesUsedToday(foreground),
            timeLimits = settings.timeLimits,
            activeSchedule = ScheduleEvaluator.findActive(LocalDateTime.now(), settings.schedules),
            focusActive = focusSession.blocking,
            focusAllowlist = settings.favoritePackages.toSet(),
            tempAllowedUntil = tempAllow[foreground] ?: 0L,
            nowMs = nowMs
        )
        if (!decision.shouldBlock) return
        if (overlayVisible.get()) return

        if (Settings.canDrawOverlays(context)) {
            val label = appLabel(foreground)
            overlayVisible.set(true)
            Log.d(TAG, "Blocking $foreground (${decision.reason})")
            val intent = BlockOverlayActivity.newIntent(context, foreground, label, decision.reason!!)
            runCatching { context.startActivity(intent) }
                .onFailure { overlayVisible.set(false) }
        } else {
            // Fallback: no overlay permission, bounce to home and grant a short
            // grace so we do not re-trigger while the app is still foreground.
            accessibilityService?.goHome()
            tempAllow[foreground] = nowMs + FALLBACK_GRACE_MS
        }
    }

    private fun appLabel(packageName: String): String = runCatching {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info)?.toString() ?: packageName
    }.getOrDefault(packageName)

    companion object {
        private const val TAG = "AppBlocker"
        private const val FALLBACK_GRACE_MS = 30_000L
    }
}