package com.spartan.launcer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spartan.launcer.SpartanLauncherApp
import kotlinx.coroutines.launch

class AppChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? SpartanLauncherApp ?: return
        app.container.appScope.launch {
            app.container.appRepository.refresh()
        }
    }
}