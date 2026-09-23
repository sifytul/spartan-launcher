package com.spartan.launcer.data.model

import android.content.ComponentName
import android.os.Process
import android.os.UserHandle

data class AppInfo(
    val packageName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val launchCount: Int = 0,
    val customLabel: String? = null,
    val user: UserHandle? = null,
    val component: ComponentName? = null
) {
    val displayLabel: String
        get() = customLabel ?: label

    val isWorkProfile: Boolean
        get() = user != null && user != Process.myUserHandle()
}