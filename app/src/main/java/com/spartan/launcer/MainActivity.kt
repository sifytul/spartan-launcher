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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val container get() = (application as SpartanLauncherApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    override fun onStart() {
        super.onStart()
        // After the screen was off, coming back to the launcher means the phone
        // was unlocked: advance the word here in case the unlock broadcast was
        // not delivered (some OEMs). Dedupe prevents a double advance when
        // ACTION_USER_PRESENT also fired for the same unlock.
        if (container.screenOffDetected) {
            container.screenOffDetected = false
            container.appScope.launch {
                val enabled = container.settingsDataStore.settings.first().wordOfTheDayEnabled
                if (enabled) {
                    container.wordOfTheDayRepository.advanceWord(skipIfAdvancedWithinMs = RECENT_ADVANCE_MS)
                }
            }
        }
    }

    companion object {
        private const val RECENT_ADVANCE_MS = 10_000L
    }
}