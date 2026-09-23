package com.spartan.launcer.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import com.spartan.launcer.SpartanLauncherApp

/**
 * One combined accessibility service used by the launcher:
 *  - feeds foreground-package events to the usage tracker / app blocker;
 *  - opens the notification shade for the swipe-down gesture;
 *  - presses HOME as the fallback when the block overlay permission is absent.
 */
class SpartanAccessibilityService : AccessibilityService() {

    private val foregroundEvents =
        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        tracker?.onForegroundPackage(null)
        (application as? SpartanLauncherApp)?.container?.appBlocker?.connectAccessibility(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        if (e.eventType and foregroundEvents == 0) return
        tracker?.onForegroundPackage(e.packageName?.toString())
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        (application as? SpartanLauncherApp)?.container?.appBlocker?.disconnectAccessibility()
        instance = null
        return super.onUnbind(intent)
    }

    private val tracker
        get() = (application as? SpartanLauncherApp)?.container?.foregroundUsageTracker

    fun goHome() {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    fun lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        }
    }

    companion object {

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