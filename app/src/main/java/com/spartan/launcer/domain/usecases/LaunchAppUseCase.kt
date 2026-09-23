package com.spartan.launcer.domain.usecases

import android.content.Context
import android.content.Intent

class LaunchAppUseCase(private val context: Context) {

    operator fun invoke(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(launchIntent) }.isSuccess
    }
}