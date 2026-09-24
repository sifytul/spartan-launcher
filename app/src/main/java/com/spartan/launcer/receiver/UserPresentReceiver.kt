package com.spartan.launcer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spartan.launcer.SpartanLauncherApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Listens for phone unlocks (ACTION_USER_PRESENT) and advances the
 * word-of-the-day. Also clears every pending "Use anyway" grace window so
 * relocking re-asserts app blocks. Registered in the manifest so it still
 * runs when the launcher process was alive or killed while the screen was off.
 */
class UserPresentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_USER_PRESENT) return
        Log.d(TAG, "USER_PRESENT received")
        val app = context.applicationContext as? SpartanLauncherApp ?: return
        val container = app.container
        container.appScope.launch {
            container.appBlocker.clearGracePeriods()
            val enabled = container.settingsDataStore.settings.first().wordOfTheDayEnabled
            if (enabled) {
                val word = container.wordOfTheDayRepository
                    .advanceWord(skipIfAdvancedWithinMs = RECENT_ADVANCE_MS)
                Log.d(TAG, "Advanced word to: ${word?.word}")
            }
        }
    }

    companion object {
        private const val TAG = "WordOfTheDay"
        // Prevents a double advance when both this broadcast and the launcher
        // resume detect the same unlock.
        private const val RECENT_ADVANCE_MS = 10_000L
    }
}