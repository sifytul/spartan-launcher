package com.spartan.launcer.ui.blocking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spartan.launcer.R
import com.spartan.launcer.data.model.BlockSchedule
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingSchedulesScreen(
    onBack: () -> Unit,
    viewModel: BlockingSchedulesViewModel = viewModel(factory = BlockingSchedulesViewModel.Factory)
) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<BlockSchedule?>(null) }

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
                    text = "Blocking schedules",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        editing = newSchedule()
                    }
                ) {
                    Text(text = "Add")
                }
            }
        }

        item {
            Text(
                text = "During a schedule, the apps on your blocked list are blocked automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }

        if (schedules.isEmpty()) {
            item {
                Text(
                    text = "No schedules yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        items(schedules, key = { it.id }) { schedule ->
            ScheduleRow(
                schedule = schedule,
                onToggle = { enabled -> viewModel.toggleEnabled(schedule.id, enabled) },
                onEdit = { editing = schedule },
                onDelete = { viewModel.remove(schedule.id) }
            )
        }
    }

    editing?.let { current ->
        ScheduleEditorDialog(
            schedule = current,
            onSave = { updated ->
                viewModel.upsert(updated)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun ScheduleRow(
    schedule: BlockSchedule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = schedule.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = schedule.enabled, onCheckedChange = onToggle)
        }
        Text(
            text = "${formatMinute(schedule.startMinute)} – ${formatMinute(schedule.endMinute)}  ·  ${daysLabel(schedule.days)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (schedule.appliesToPackages.isEmpty()) "Applies to all blocked apps"
            else "Applies to specific apps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row {
            TextButton(onClick = onEdit) { Text(text = "Edit") }
            TextButton(onClick = onDelete) { Text(text = "Delete") }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun ScheduleEditorDialog(
    schedule: BlockSchedule,
    onSave: (BlockSchedule) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(schedule.label) }
    var days by remember { mutableStateOf(schedule.days) }
    var enabled by remember { mutableStateOf(schedule.enabled) }

    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    var startMinute by remember { mutableStateOf(schedule.startMinute) }
    var endMinute by remember { mutableStateOf(schedule.endMinute) }

    if (pickStart) {
        TimePickerDialog(
            initialMinute = startMinute,
            onConfirm = { startMinute = it; pickStart = false },
            onDismiss = { pickStart = false }
        )
    }
    if (pickEnd) {
        TimePickerDialog(
            initialMinute = endMinute,
            onConfirm = { endMinute = it; pickEnd = false },
            onDismiss = { pickEnd = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule.id.isEmpty()) "New schedule" else "Edit schedule") },
        text = {
            Column {
                TextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true
                )
                Spacer(Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    DayOfWeek.entries.forEach { day ->
                        FilterChip(
                            selected = day in days,
                            onClick = {
                                days = if (day in days) days - day else days + day
                            },
                            label = { Text(dayNarrowLabel(day)) }
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
                Spacer(Modifier.padding(vertical = 8.dp))
                OutlinedButton(onClick = { pickStart = true }) {
                    Text("Start ${formatMinute(startMinute)}")
                }
                OutlinedButton(onClick = { pickEnd = true }) {
                    Text("End ${formatMinute(endMinute)}")
                }
                Spacer(Modifier.padding(vertical = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Enabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    schedule.copy(
                        label = label.ifBlank { "Schedule" },
                        days = days,
                        enabled = enabled,
                        startMinute = startMinute,
                        endMinute = endMinute
                    )
                )
            }) { Text(text = "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun TimePickerDialog(
    initialMinute: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialMinute / 60,
        initialMinute = initialMinute % 60
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}

@Composable
private fun dayNarrowLabel(day: DayOfWeek): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
        ?.let { java.util.Locale.forLanguageTag(it.toLanguageTag()) }
        ?: java.util.Locale.US
    return day.getDisplayName(TextStyle.NARROW, locale)
}

private fun newSchedule() = BlockSchedule(
    id = UUID.randomUUID().toString(),
    label = "",
    startMinute = 8 * 60,
    endMinute = 17 * 60,
    days = DayOfWeek.entries.toSet(),
    appliesToPackages = emptySet(),
    enabled = true
)

private fun formatMinute(minuteOfDay: Int): String {
    val h = minuteOfDay / 60
    val m = minuteOfDay % 60
    return String.format(Locale.US, "%02d:%02d", h, m)
}

@Composable
private fun daysLabel(days: Set<DayOfWeek>): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
        ?.let { java.util.Locale.forLanguageTag(it.toLanguageTag()) }
        ?: java.util.Locale.US
    return DayOfWeek.entries
        .filter { it in days }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
}