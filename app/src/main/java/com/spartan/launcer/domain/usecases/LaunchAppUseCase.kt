package com.spartan.launcer.domain.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import com.spartan.launcer.data.model.AppInfo

class LaunchAppUseCase(private val context: Context) {

    operator fun invoke(packageName: String): Boolean = launch(packageName, null, null)

    operator fun invoke(app: AppInfo): Boolean =
        launch(app.packageName, app.user, app.component)

    private fun launch(packageName: String, user: android.os.UserHandle?, component: android.content.ComponentName?): Boolean {
        if (user != null && component != null) {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
                ?: return false
            return runCatching {
                launcherApps.startMainActivity(component, user, null, null)
            }.isSuccess
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(launchIntent) }.isSuccess
    }
}