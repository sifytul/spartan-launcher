package com.spartan.launcer

import android.os.Bundle
import android.util.Log
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

    override fun onResume() {
        super.onResume()
        // When the phone is locked the launcher is paused (often just paused,
        // not stopped), so the next "unlock back to home" shows up as onResume
        // rather than onStart. If the screen went off since the last time we
        // handled this, the phone was locked & re-entered: advance the word —
        // this covers devices that drop ACTION_USER_PRESENT. The 10s dedupe
        // prevents a double advance when the broadcast also fired.
        if (container.screenOffDetected) {
            container.screenOffDetected = false
            container.appScope.launch {
                val enabled = container.settingsDataStore.settings.first().wordOfTheDayEnabled
                if (enabled) {
                    val word = container.wordOfTheDayRepository
                        .advanceWord(skipIfAdvancedWithinMs = RECENT_ADVANCE_MS)
                    Log.d(TAG, "Unlock fallback advanced word to: ${word?.word}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "WordOfTheDay"
        private const val RECENT_ADVANCE_MS = 10_000L
    }
}