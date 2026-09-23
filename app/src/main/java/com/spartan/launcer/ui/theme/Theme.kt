package com.spartan.launcer.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.spartan.launcer.data.model.FontMode
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

private val MonoLightColors = lightColorScheme(
    primary = MonoLightPrimary,
    onPrimary = MonoLightOnPrimary,
    primaryContainer = MonoLightSurfaceVariant,
    onPrimaryContainer = MonoLightOnSurfaceVariant,
    surface = MonoLightSurface,
    onSurface = MonoLightOnSurface,
    background = MonoLightSurface,
    onBackground = MonoLightOnSurface,
    surfaceVariant = MonoLightSurfaceVariant,
    onSurfaceVariant = MonoLightOnSurfaceVariant,
    secondary = MonoLightOnSurfaceVariant,
    onSecondary = MonoLightSurface,
    error = MonoLightOnSurfaceVariant,
    onError = MonoLightSurface
)

private val MonoDarkColors = darkColorScheme(
    primary = MonoDarkPrimary,
    onPrimary = MonoDarkOnPrimary,
    primaryContainer = MonoDarkSurfaceVariant,
    onPrimaryContainer = MonoDarkOnSurfaceVariant,
    surface = MonoDarkSurface,
    onSurface = MonoDarkOnSurface,
    background = MonoDarkSurface,
    onBackground = MonoDarkOnSurface,
    surfaceVariant = MonoDarkSurfaceVariant,
    onSurfaceVariant = MonoDarkOnSurfaceVariant,
    secondary = MonoDarkOnSurfaceVariant,
    onSecondary = MonoDarkSurface,
    error = MonoDarkOnSurfaceVariant,
    onError = MonoDarkSurface
)

@Composable
fun SpartanLauncherTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    monochrome: Boolean = false,
    fontMode: FontMode = FontMode.SYSTEM,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val colors = when {
        monochrome && dark -> MonoDarkColors
        monochrome -> MonoLightColors
        else -> when (themeMode) {
            ThemeMode.SYSTEM -> if (systemInDark) DarkColors else LightColors
            ThemeMode.LIGHT -> LightColors
            ThemeMode.DARK -> DarkColors
            ThemeMode.AMOLED -> AmoledColors
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? ComponentActivity
        SideEffect {
            if (activity != null) {
                val barStyle = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                activity.enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography.withFont(fontFamilyFor(fontMode), fontScale),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colors.background
            ) {
                content()
            }
        }
    )
}