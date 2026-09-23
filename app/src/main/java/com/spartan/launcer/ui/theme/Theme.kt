package com.spartan.launcer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.spartan.launcer.data.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightOnSurfaceVariant,
    surface = LightSurface,
    onSurface = LightOnSurface,
    background = LightSurface,
    onBackground = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    secondary = LightOnSurfaceVariant,
    onSecondary = LightSurface
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurfaceVariant,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkSurface
)

private val AmoledColors = darkColorScheme(
    primary = AmoledOnSurface,
    onPrimary = AmoledSurface,
    primaryContainer = AmoledSurfaceVariant,
    onPrimaryContainer = AmoledOnSurfaceVariant,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    background = AmoledSurface,
    onBackground = AmoledOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledOnSurfaceVariant,
    secondary = AmoledOnSurfaceVariant,
    onSecondary = AmoledSurface
)

@Composable
fun SpartanLauncherTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val colors = when (themeMode) {
        ThemeMode.SYSTEM -> if (systemInDark) DarkColors else LightColors
        ThemeMode.LIGHT -> LightColors
        ThemeMode.DARK -> DarkColors
        ThemeMode.AMOLED -> AmoledColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}