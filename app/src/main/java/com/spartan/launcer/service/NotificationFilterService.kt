package com.spartan.launcer.service

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.spartan.launcer.SpartanLauncherApp
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Suppresses notifications from apps the user has muted via "Mute app
 * notifications", and during a focus session suppresses all third-party
 * notifications except the launcher's own reminders.
 */
class NotificationFilterService : NotificationListenerService() {

    @Volatile
    private var mutedPackages: Set<String> = emptySet()

    @Volatile
    private var filterAll: Boolean = false

    private var collectorJob: Job? = null
    private lateinit var ownPackage: String

    override fun onListenerConnected() {
        super.onListenerConnected()
        ownPackage = packageName
        val container = (application as? SpartanLauncherApp)?.container ?: return
        collectorJob = container.appScope.launch {
            combine(
                container.settingsDataStore.settings.map { it.mutedNotifications },
                container.focusController.session.map { it.blocking }
            ) { muted, focusBlocking ->
                muted to focusBlocking
            }.collect { (muted, focusBlocking) ->
                mutedPackages = muted
                filterAll = focusBlocking
            }
        }
    }

    override fun onListenerDisconnected() {
        collectorJob?.cancel()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        val pkg = notification.packageName
        if (pkg == ownPackage) return
        if (filterAll || pkg in mutedPackages) {
            cancelNotification(notification.key)
        }
    }

    companion object {

        fun isEnabled(context: Context): Boolean {
            val expected = ComponentName(context, NotificationFilterService::class.java)
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val fullName = expected.flattenToString()
            val shortName = expected.flattenToShortString()
            return enabled.split(':').any {
                it.equals(fullName, ignoreCase = true) ||
                    it.equals(shortName, ignoreCase = true)
            }
        }
    }
}