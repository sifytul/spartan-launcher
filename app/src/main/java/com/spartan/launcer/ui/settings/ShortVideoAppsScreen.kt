package com.spartan.launcer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spartan.launcer.R
import com.spartan.launcer.data.ShortVideoPackages
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.data.model.GraceMode

private val GRACE_MINUTE_OPTIONS = listOf(1, 2, 5, 10)

@Composable
fun ShortVideoAppsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val accessibilityEnabled by viewModel.notificationShadeEnabled.collectAsStateWithLifecycle()
    val canDrawOverlays by viewModel.canDrawOverlays.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refreshNotificationShadeState()
        viewModel.refreshOverlayPermissionState()
    }

    val filtered = remember(allApps, query) {
        if (query.isBlank()) allApps
        else allApps.filter {
            it.displayLabel.contains(query.trim(), ignoreCase = true) ||
                it.packageName.contains(query.trim(), ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
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
            Column {
                Text(
                    text = "Short video apps",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Blocked while the toggle is on",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Search apps") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Block short videos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (canDrawOverlays) {
                        if (settings.blockShortVideos) "On"
                        else "Off"
                    } else {
                        "Needs display-over-apps permission"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!canDrawOverlays) {
                TextButton(onClick = viewModel::openOverlayPermissionSettings) {
                    Text(text = "Enable")
                }
            } else {
                Switch(
                    checked = settings.blockShortVideos,
                    onCheckedChange = viewModel::setBlockShortVideos
                )
            }
        }

        Text(
            text = "Short-form video tabs have no separate API, so most of these block the whole app. The screen remembers your choices even if an app is not installed yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        GraceSection(
            mode = settings.shortVideoGraceMode,
            minutes = settings.shortVideoGraceMinutes,
            onMode = viewModel::setShortVideoGraceMode,
            onMinutes = viewModel::setShortVideoGraceMinutes
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { "${it.packageName}_${it.user?.hashCode() ?: 0}" }) { app ->
                ShortVideoAppRow(
                    app = app,
                    included = settings.shortVideoPackages.contains(app.packageName),
                    shortsOnly = settings.youtubeShortsOnly,
                    accessibilityEnabled = accessibilityEnabled,
                    onToggle = {
                        viewModel.setShortVideoPackage(app.packageName, !settings.shortVideoPackages.contains(app.packageName))
                    },
                    onShortsOnly = { viewModel.setYouTubeShortsOnly(it) }
                )
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "No apps match \"$query\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GraceSection(
    mode: GraceMode,
    minutes: Int,
    onMode: (GraceMode) -> Unit,
    onMinutes: (Int) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Use anyway grace",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMode(GraceMode.UNLOCK) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = mode == GraceMode.UNLOCK,
                onClick = { onMode(GraceMode.UNLOCK) }
            )
            Text(
                text = "Until you lock the phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMode(GraceMode.MINUTES) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = mode == GraceMode.MINUTES,
                onClick = { onMode(GraceMode.MINUTES) }
            )
            Text(
                text = "Fixed minutes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (mode == GraceMode.MINUTES) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 44.dp, top = 4.dp)
            ) {
                GRACE_MINUTE_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = minutes == option,
                        onClick = { onMinutes(option) },
                        label = { Text("$option m") }
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun ShortVideoAppRow(
    app: AppInfo,
    included: Boolean,
    shortsOnly: Boolean,
    accessibilityEnabled: Boolean,
    onToggle: () -> Unit,
    onShortsOnly: (Boolean) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.displayLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = included, onCheckedChange = { onToggle() })
        }
        if (app.packageName == ShortVideoPackages.YOUTUBE && included) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "YouTube Shorts only",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Leave regular YouTube videos alone. Experimental — detected via the accessibility tree.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = shortsOnly,
                        onCheckedChange = onShortsOnly,
                        enabled = accessibilityEnabled
                    )
                }
                if (!accessibilityEnabled) {
                    Text(
                        text = "Enable accessibility (in System settings) to detect Shorts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}