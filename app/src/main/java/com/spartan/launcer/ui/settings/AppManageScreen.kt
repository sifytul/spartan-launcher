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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spartan.launcer.R
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.data.model.LauncherSettings

@Composable
fun AppManageScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<AppInfo?>(null) }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Apps",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${allApps.size} installed",
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

        Text(
            text = "Blocked apps are covered by the block screen whenever you open them. Favorites always stay usable during Focus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { "${it.packageName}_${it.user?.hashCode() ?: 0}" }) { app ->
                AppManageRow(
                    app = app,
                    settings = settings,
                    onToggleFavorite = {
                        viewModel.setFavorite(app.packageName, !app.isFavorite)
                    },
                    onToggleBlocked = {
                        viewModel.setBlocked(app.packageName, !settings.blockedPackages.contains(app.packageName))
                    },
                    onToggleHidden = {
                        viewModel.setHidden(app.packageName, !app.isHidden)
                    },
                    onRename = { renameTarget = app }
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

    renameTarget?.let { app ->
        var label by remember(app.packageName) { mutableStateOf(app.customLabel ?: "") }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename ${app.label}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Custom name") },
                        singleLine = true
                    )
                    Text(
                        text = "Leave empty to use the original name.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCustomLabel(app.packageName, label)
                    renameTarget = null
                }) { Text(text = "Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text(text = "Cancel") }
            }
        )
    }
}

@Composable
private fun AppManageRow(
    app: AppInfo,
    settings: LauncherSettings,
    onToggleFavorite: () -> Unit,
    onToggleBlocked: () -> Unit,
    onToggleHidden: () -> Unit,
    onRename: () -> Unit
) {
    val isFavorite = settings.favoritePackages.contains(app.packageName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.displayLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (app.customLabel != null) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onRename) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Rename",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            ToggleSwitch(
                label = "Block",
                checked = settings.blockedPackages.contains(app.packageName),
                onToggle = onToggleBlocked
            )
            ToggleSwitch(
                label = "Hide",
                checked = settings.hiddenPackages.contains(app.packageName),
                onToggle = onToggleHidden
            )
        }
    }
}

@Composable
private fun ToggleSwitch(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (checked) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() }
        )
    }
}