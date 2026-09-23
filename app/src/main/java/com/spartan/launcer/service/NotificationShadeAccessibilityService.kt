package com.spartan.launcer.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

/**
 * Lets the swipe-down gesture open the notification shade. The status bar
 * expansion APIs are not available to third-party apps without this service,
 * which is enabled once from the system accessibility settings.
 */
class NotificationShadeAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    companion object {

        @Volatile
        private var instance: NotificationShadeAccessibilityService? = null

        fun openNotificationShadeIfAvailable(): Boolean {
            val service = instance ?: return false
            service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            return true
        }

        fun isServiceEnabled(context: Context): Boolean {
            val expected = ComponentName(
                context,
                NotificationShadeAccessibilityService::class.java
            )
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