package com.spartan.launcer.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.text.format.DateFormat as SystemDateFormat
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.domain.focus.FocusPhase
import com.spartan.launcer.domain.focus.FocusSession
import java.text.DateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val isDefaultHome by viewModel.isDefaultHome.collectAsStateWithLifecycle()
    val focusSession by viewModel.focusSession.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val timeFormatter = remember { SystemDateFormat.getTimeFormat(context) }
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.FULL) }
    val swipeThreshold = with(LocalDensity.current) { 120.dp.toPx() }

    BackHandler { }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.recheckDefaultHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var accumulatedDrag = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount -> accumulatedDrag += dragAmount },
                    onDragEnd = {
                        when {
                            accumulatedDrag > swipeThreshold ->
                                viewModel.openNotificationShade()
                            accumulatedDrag < -swipeThreshold ->
                                onOpenDrawer()
                        }
                        accumulatedDrag = 0f
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { viewModel.lockScreen() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.4f))
            Text(
                text = timeFormatter.format(currentTime),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { viewModel.launchClock() }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = dateFormatter.format(currentTime),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            if (favorites.isEmpty()) {
                Text(
                    text = "Swipe up to open the app drawer.\nLong-press apps there to add favorites.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    favorites.forEach { app ->
                        FavoriteItem(app = app, onClick = {
                            viewModel.launchApp(app.packageName)
                        })
                    }
                }
            }
            if (focusSession.isActive) {
                Spacer(Modifier.height(24.dp))
                FocusProgressCard(
                    session = focusSession,
                    onPause = viewModel::pauseFocus,
                    onResume = viewModel::resumeFocus,
                    onStop = viewModel::stopFocus
                )
            }
            Spacer(Modifier.weight(1.6f))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable(onClick = onOpenSettings)
            )
        }

        if (!isDefaultHome) {
            DefaultHomeBanner(
                onSetAsHome = { viewModel.openHomeSettings() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun FavoriteItem(app: AppInfo, onClick: () -> Unit) {
    Text(
        text = app.displayLabel,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun FocusProgressCard(
    session: FocusSession,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val progress = if (session.totalMs > 0) {
        (session.remainingMs.toFloat() / session.totalMs.toFloat()).coerceIn(0f, 1f)
    } else 0f
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = when (session.phase) {
                    FocusPhase.FOCUS -> if (session.paused) "Focus paused" else "Focus session"
                    FocusPhase.BREAK -> "Break"
                    FocusPhase.IDLE -> "Focus"
                } + " · ${formatRemaining(session.remainingMs)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Row {
                if (session.paused) {
                    TextButton(onClick = onResume) { Text(text = "Resume") }
                } else {
                    TextButton(onClick = onPause) { Text(text = "Pause") }
                }
                TextButton(onClick = onStop) { Text(text = "End") }
            }
        }
    }
}

private fun formatRemaining(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

@Composable
private fun DefaultHomeBanner(onSetAsHome: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Not your default launcher",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onSetAsHome) {
                Text(text = "Set as home")
            }
        }
    }
}