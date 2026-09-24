package com.spartan.launcer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spartan.launcer.SpartanLauncherApp

/**
 * Arms the unlock fallback when the screen actually turns OFF. On the next
 * launcher resume the word-of-the-day advances — covering devices that do not
 * deliver ACTION_USER_PRESENT. KEYGUARD: this is only a screen-off signal; the
 * resume handler decides whether an unlock happened.
 */
class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_SCREEN_OFF) return
        val app = context.applicationContext as? SpartanLauncherApp ?: return
        app.container.screenOffDetected = true
    }
}