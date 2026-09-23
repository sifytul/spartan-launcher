package com.spartan.launcer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spartan.launcer.data.model.LauncherSettings
import com.spartan.launcer.ui.navigation.LauncherNavGraph
import com.spartan.launcer.ui.theme.SpartanLauncherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SpartanLauncherApp).container
        setContent {
            val settings by container.settingsDataStore.settings
                .collectAsStateWithLifecycle(initialValue = LauncherSettings())
            SpartanLauncherTheme(
                themeMode = settings.themeMode,
                monochrome = settings.monochrome,
                fontMode = settings.fontMode,
                fontScale = settings.fontScale
            ) {
                LauncherNavGraph()
            }
        }
    }
}