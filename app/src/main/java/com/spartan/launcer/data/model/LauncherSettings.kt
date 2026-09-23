package com.spartan.launcer.data.model

data class LauncherSettings(
    val favoritePackages: List<String> = emptyList(),
    val hiddenPackages: List<String> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val blockedPackages: Set<String> = emptySet(),
    val timeLimits: Map<String, Int> = emptyMap(),
    val schedules: List<BlockSchedule> = emptyList(),
    val mutedNotifications: Set<String> = emptySet(),
    val customLabels: Map<String, String> = emptyMap(),
    val fontMode: FontMode = FontMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val focusDurationMinutes: Int = 25
)