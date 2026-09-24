package com.spartan.launcer.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.di.AppContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * One combined accessibility service used by the launcher:
 *  - feeds foreground-package events to the usage tracker / app blocker;
 *  - opens the notification shade for the swipe-down gesture;
 *  - presses HOME as the fallback when the block overlay permission is absent;
 *  - detects phone unlocks from the keyguard window for the word-of-the-day.
 */
class SpartanAccessibilityService : AccessibilityService() {

    private val foregroundEvents =
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED

    @Volatile
    private var shortsDetector: ShortsDetector? = null

    private val unlockDetector = UnlockDetector()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        tracker?.onForegroundPackage(null)
        val appBlocker = (application as? SpartanLauncherApp)?.container?.appBlocker
        appBlocker?.connectAccessibility(this)
        shortsDetector = appBlocker?.let { ShortsDetector(it) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        if (e.eventType and foregroundEvents == 0) return
        tracker?.onForegroundPackage(e.packageName?.toString())
        shortsDetector?.onAccessibilityEvent(e, rootInActiveWindow)
        handleUnlockSignal(e.packageName?.toString())
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        (application as? SpartanLauncherApp)?.container?.appBlocker?.disconnectAccessibility()
        instance = null
        return super.onUnbind(intent)
    }

    private val container: AppContainer?
        get() = (application as? SpartanLauncherApp)?.container

    private val tracker
        get() = container?.foregroundUsageTracker

    private fun handleUnlockSignal(pkg: String?) {
        val c = container ?: return
        when (unlockDetector.onWindowPackage(pkg)) {
            UnlockDetector.Signal.LOCKED -> c.screenOffDetected = true
            UnlockDetector.Signal.UNLOCKED -> {
                c.screenOffDetected = false
                c.appScope.launch {
                    c.appBlocker.clearGracePeriods()
                    val enabled = c.settingsDataStore.settings.first().wordOfTheDayEnabled
                    if (enabled) {
                        val word = c.wordOfTheDayRepository
                            .advanceWord(skipIfAdvancedWithinMs = RECENT_ADVANCE_MS)
                        Log.d(TAG, "Unlock via accessibility: ${word?.word}")
                    }
                }
            }
            UnlockDetector.Signal.NONE -> Unit
        }
    }

    fun goHome() {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    fun lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            container?.screenOffDetected = true
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        }
    }

    companion object {

        private const val TAG = "WordOfTheDay"
        // Prevents a double advance when more than one unlock signal fires for
        // the same unlock (broadcast receiver, launcher resume, accessibility).
        private const val RECENT_ADVANCE_MS = 10_000L

        @Volatile
        private var instance: SpartanAccessibilityService? = null

        fun openNotificationShadeIfAvailable(): Boolean {
            val service = instance ?: return false
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            return true
        }

        fun lockScreenIfAvailable(): Boolean {
            val service = instance ?: return false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
            runCatching { service.lockScreen() }
            return true
        }

        fun isServiceEnabled(context: Context): Boolean {
            val expected = ComponentName(context, SpartanAccessibilityService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val fullName = expected.flattenToString()
            val shortName = expected.flattenToShortString()
            return enabledServices.split(':').any {
                it.equals(fullName, ignoreCase = true) ||
                    it.equals(shortName, ignoreCase = true)
            }
        }
    }
}