package com.spartan.launcer.data.model

data class LauncherSettings(
    val favoritePackages: List<String> = emptyList(),
    val hiddenPackages: List<String> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)