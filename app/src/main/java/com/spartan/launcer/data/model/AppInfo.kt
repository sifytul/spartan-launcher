package com.spartan.launcer.data.model

data class AppInfo(
    val packageName: String,
    val label: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val launchCount: Int = 0
)