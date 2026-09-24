package com.spartan.launcer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spartan.launcer.SpartanLauncherApp

/**
 * Tracks when the screen turns on/off so the launcher can detect that the
 * phone was woken up. Combined with the launcher's own resume, this advances
 * the word-of-the-day per unlock even on devices that drop
 * ACTION_USER_PRESENT.
 */
class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_SCREEN_OFF && intent?.action != Intent.ACTION_SCREEN_ON) return
        val app = context.applicationContext as? SpartanLauncherApp ?: return
        app.container.screenOffDetected = true
    }
}