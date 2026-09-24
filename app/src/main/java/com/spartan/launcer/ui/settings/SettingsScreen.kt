package com.spartan.launcer.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spartan.launcer.R
import com.spartan.launcer.data.model.FontMode
import com.spartan.launcer.data.model.LauncherSettings
import com.spartan.launcer.data.model.ThemeMode
import com.spartan.launcer.domain.focus.FocusPhase
import com.spartan.launcer.domain.focus.FocusSession
import java.util.Locale

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenScreenTime: () -> Unit,
    onOpenSchedules: () -> Unit,
    onOpenMuteNotifications: () -> Unit,
    onOpenAppManage: () -> Unit,
    onOpenShortVideoApps: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isDefaultHome by viewModel.isDefaultHome.collectAsStateWithLifecycle()
    val notificationShadeEnabled by viewModel.notificationShadeEnabled.collectAsStateWithLifecycle()
    val canDrawOverlays by viewModel.canDrawOverlays.collectAsStateWithLifecycle()
    val hasUsageAccess by viewModel.hasUsageAccess.collectAsStateWithLifecycle()
    val hasNotificationAccess by viewModel.hasNotificationAccess.collectAsStateWithLifecycle()
    val focusSession by viewModel.focusSession.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.recheckDefaultHome()
        viewModel.refreshNotificationShadeState()
        viewModel.refreshOverlayPermissionState()
        viewModel.refreshUsageAccessState()
        viewModel.refreshNotificationAccessState()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SettingsBlock(header = "Appearance") {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    GroupRow(
                        title = mode.label,
                        onClick = { viewModel.setThemeMode(mode) },
                        trailing = {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) }
                            )
                        },
                        showDivider = index != ThemeMode.entries.lastIndex
                    )
                }
                GroupRow(
                    title = "Monochrome (grayscale)",
                    subtitle = "Renders the launcher in gray tones",
                    trailing = {
                        Switch(
                            checked = settings.monochrome,
                            onCheckedChange = { viewModel.setMonochrome(it) }
                        )
                    },
                    showDivider = false
                )
            }
        }

        item {
            SettingsBlock(header = "Text") {
                GroupRow(
                    title = "Font family",
                    showDivider = true,
                    content = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FontMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = settings.fontMode == mode,
                                    onClick = { viewModel.setFontMode(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }
                    }
                )
                GroupRow(
                    title = "Text size",
                    showDivider = false,
                    content = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = settings.fontScale,
                                onValueChange = { viewModel.setFontScale(it) },
                                valueRange = 0.8f..1.3f,
                                steps = 4,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "${(settings.fontScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        item {
            SettingsBlock(header = "Launcher") {
                GroupRow(
                    title = "Default launcher",
                    subtitle = if (isDefaultHome) "Spartan is your home app" else "Not set — apps open with another launcher",
                    leading = {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = null,
                            tint = if (isDefaultHome) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailing = if (isDefaultHome) null else {
                        {
                            TextButton(onClick = viewModel::openHomeSettings) {
                                Text(text = "Set as default")
                            }
                        }
                    },
                    showDivider = true
                )
                GroupRow(
                    title = "Word of the day",
                    subtitle = if (settings.wordOfTheDayEnabled) {
                        "Shows a new word after every unlock"
                    } else {
                        "Off · tap a word to open its details"
                    },
                    trailing = {
                        Switch(
                            checked = settings.wordOfTheDayEnabled,
                            onCheckedChange = viewModel::setWordOfTheDayEnabled
                        )
                    },
                    showDivider = false
                )
            }
        }

        item {
            SettingsBlock(header = "Focus & blocking") {
                PermissionRow(
                    title = "Swipe-down gesture",
                    subtitle = "Opens the notification shade from the home screen",
                    enabled = notificationShadeEnabled,
                    onEnable = viewModel::openAccessibilitySettings,
                    description = "accessibility service",
                    showDivider = true
                )
                PermissionRow(
                    title = "Blocked app screen",
                    subtitle = "Covers blocked apps until the countdown ends",
                    enabled = canDrawOverlays,
                    onEnable = viewModel::openOverlayPermissionSettings,
                    description = "display-over-apps permission",
                    showDivider = true
                )
                NavRow(
                    title = "Screen time & daily limits",
                    subtitle = "Per-app usage and time limits",
                    enabled = hasUsageAccess,
                    onEnable = viewModel::openUsageAccessSettings,
                    description = "usage access",
                    onClick = onOpenScreenTime,
                    showDivider = true
                )
                NavRow(
                    title = "Blocking schedules",
                    subtitle = "Time windows that auto-block your apps",
                    enabled = null,
                    onClick = onOpenSchedules,
                    showDivider = true
                )
                NavRow(
                    title = "Mute app notifications",
                    subtitle = "Silence chosen apps' notifications",
                    enabled = hasNotificationAccess,
                    onEnable = viewModel::openNotificationAccessSettings,
                    description = "notification access",
                    onClick = onOpenMuteNotifications,
                    showDivider = true
                )
                NavRow(
                    title = "Block short videos",
                    subtitle = if (!canDrawOverlays) {
                        "Needs display-over-apps permission · TikTok, Shorts, Reels & more"
                    } else if (settings.blockShortVideos) {
                        "On · TikTok, Shorts, Reels, Snapchat & more"
                    } else {
                        "TikTok, Shorts, Reels, Snapchat & more"
                    },
                    enabled = canDrawOverlays,
                    onEnable = viewModel::openOverlayPermissionSettings,
                    description = "display-over-apps permission",
                    onClick = onOpenShortVideoApps,
                    showDivider = true
                )
                GroupRow(
                    title = "Double tap home to lock",
                    subtitle = when {
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.P -> "Requires Android 9+"
                        notificationShadeEnabled -> "Active · double tap the home screen to lock"
                        else -> "Needs accessibility service"
                    },
                    leading = {
                        RowStatusIcon(
                            enabled = notificationShadeEnabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                        )
                    },
                    showDivider = false
                )
            }
        }

        item {
            SettingsBlock(header = "Focus session") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    FocusSection(
                        durationMinutes = settings.focusDurationMinutes,
                        session = focusSession,
                        onDurationChange = viewModel::setFocusDuration,
                        onStart = viewModel::startFocus,
                        onPause = viewModel::pauseFocus,
                        onResume = viewModel::resumeFocus,
                        onStop = viewModel::stopFocus
                    )
                }
            }
        }

        item {
            SettingsBlock(header = "Apps") {
                GroupRow(
                    title = "Manage apps",
                    subtitle = "Favorites, hidden apps and custom names",
                    leading = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = onOpenAppManage,
                    trailing = { RowChevron() },
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnable: () -> Unit,
    description: String,
    showDivider: Boolean
) {
    GroupRow(
        title = title,
        subtitle = if (enabled) "Active · $subtitle" else "Needs $description · $subtitle",
        leading = { RowStatusIcon(enabled = enabled) },
        trailing = if (enabled) null else {
            {
                TextButton(onClick = onEnable) { Text(text = "Enable") }
            }
        },
        showDivider = showDivider
    )
}

@Composable
private fun NavRow(
    title: String,
    subtitle: String,
    enabled: Boolean?,
    onEnable: (() -> Unit)? = null,
    description: String = "",
    onClick: () -> Unit,
    showDivider: Boolean
) {
    GroupRow(
        title = title,
        subtitle = when {
            enabled == true -> "Active · $subtitle"
            enabled == false -> "Needs $description · $subtitle"
            else -> subtitle
        },
        leading = enabled?.let {
            { RowStatusIcon(enabled = enabled) }
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (enabled == false && onEnable != null) {
                    TextButton(onClick = onEnable) { Text(text = "Enable") }
                }
                RowChevron()
            }
        },
        onClick = onClick,
        showDivider = showDivider
    )
}

@Composable
private fun RowStatusIcon(enabled: Boolean) {
    Icon(
        imageVector = if (enabled) Icons.Filled.Check else Icons.Filled.Warning,
        contentDescription = if (enabled) "Active" else "Needs permission",
        tint = if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.error
    )
}

@Composable
private fun RowChevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsBlock(
    header: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
        Text(
            text = header.uppercase(Locale.US),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun GroupRow(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean,
    content: (@Composable () -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
        content?.invoke()
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = if (leading != null) 52.dp else 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun FocusSection(
    durationMinutes: Int,
    session: FocusSession,
    onDurationChange: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onStart() }
    val durations = listOf(15, 25, 45, 60)

    Column {
        if (session.isActive) {
            Text(
                text = when (session.phase) {
                    FocusPhase.FOCUS -> if (session.paused) "Focus paused" else "Focus session running"
                    FocusPhase.BREAK -> "Break"
                    FocusPhase.IDLE -> ""
                } + "  ·  ${formatRemaining(session.remainingMs)} left",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row {
                if (session.paused) {
                    TextButton(onClick = onResume) { Text(text = "Resume") }
                } else {
                    TextButton(onClick = onPause) { Text(text = "Pause") }
                }
                TextButton(onClick = onStop) { Text(text = "End") }
            }
        } else {
            Text(
                text = if (durationMinutes == 0) "Select a duration"
                else "Focus blocks everything except your favorites for $durationMinutes minutes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durations.forEach { minutes ->
                    FilterChip(
                        selected = durationMinutes == minutes,
                        onClick = { onDurationChange(minutes) },
                        label = { Text("$minutes") }
                    )
                }
            }
            TextButton(onClick = {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(permission)
                } else {
                    onStart()
                }
            }) { Text(text = "Start focus") }
        }
    }
}

private fun formatRemaining(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}