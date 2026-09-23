package com.spartan.launcer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spartan.launcer.data.model.BlockSchedule
import com.spartan.launcer.data.model.FontMode
import com.spartan.launcer.data.model.LauncherSettings
import com.spartan.launcer.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "launcher_settings"
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val FAVORITE_PACKAGES = stringSetPreferencesKey("favorite_packages")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BLOCKED_PACKAGES = stringPreferencesKey("blocked_packages")
        val BLOCK_SHORT_VIDEOS = booleanPreferencesKey("block_short_videos")
        val TIME_LIMITS = stringPreferencesKey("time_limits")
        val SCHEDULES = stringPreferencesKey("schedules")
        val MUTED_NOTIFICATIONS = stringPreferencesKey("muted_notifications")
        val CUSTOM_LABELS = stringPreferencesKey("custom_labels")
        val FONT_MODE = stringPreferencesKey("font_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val MONOCHROME = booleanPreferencesKey("monochrome")
        val FOCUS_DURATION_MINUTES = intPreferencesKey("focus_duration_minutes")
        val USAGE_MINUTES = stringPreferencesKey("usage_minutes")
    }

    val settings: Flow<LauncherSettings> = context.launcherDataStore.data.map { prefs ->
        LauncherSettings(
            favoritePackages = prefs[Keys.FAVORITE_PACKAGES].orEmpty().toList(),
            hiddenPackages = prefs[Keys.HIDDEN_PACKAGES].orEmpty().toList(),
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: ThemeMode.SYSTEM,
            blockedPackages = JsonCodec.decodeStringSet(prefs[Keys.BLOCKED_PACKAGES].orEmpty()),
            blockShortVideos = prefs[Keys.BLOCK_SHORT_VIDEOS] ?: false,
            timeLimits = JsonCodec.decodeMinutesMap(prefs[Keys.TIME_LIMITS].orEmpty()),
            schedules = JsonCodec.decodeSchedules(prefs[Keys.SCHEDULES].orEmpty()),
            mutedNotifications =
                JsonCodec.decodeStringSet(prefs[Keys.MUTED_NOTIFICATIONS].orEmpty()),
            customLabels = JsonCodec.decodeStringMap(prefs[Keys.CUSTOM_LABELS].orEmpty()),
            fontMode = prefs[Keys.FONT_MODE]
                ?.let { name -> FontMode.entries.firstOrNull { it.name == name } }
                ?: FontMode.SYSTEM,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            monochrome = prefs[Keys.MONOCHROME] ?: false,
            focusDurationMinutes = prefs[Keys.FOCUS_DURATION_MINUTES] ?: 25
        )
    }

    val usageMinutes: Flow<Map<String, Map<String, Int>>> =
        context.launcherDataStore.data.map { prefs ->
            JsonCodec.decodeUsage(prefs[Keys.USAGE_MINUTES].orEmpty())
        }

    suspend fun setFavorite(packageName: String, favorite: Boolean) {
        context.launcherDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (favorite) current.add(packageName) else current.remove(packageName)
            prefs[Keys.FAVORITE_PACKAGES] = current
        }
    }

    suspend fun setHidden(packageName: String, hidden: Boolean) {
        context.launcherDataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (hidden) current.add(packageName) else current.remove(packageName)
            prefs[Keys.HIDDEN_PACKAGES] = current
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setBlocked(packageName: String, blocked: Boolean) {
        context.launcherDataStore.edit { prefs ->
            val current = mutableSetOf<String>()
            current.addAll(
                JsonCodec.decodeStringSet(prefs[Keys.BLOCKED_PACKAGES].orEmpty())
            )
            if (blocked) current.add(packageName) else current.remove(packageName)
            prefs[Keys.BLOCKED_PACKAGES] = JsonCodec.encodeStringSet(current)
        }
    }

    suspend fun setBlockShortVideos(block: Boolean) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.BLOCK_SHORT_VIDEOS] = block
        }
    }

    suspend fun setTimeLimit(packageName: String, minutesPerDay: Int) {
        context.launcherDataStore.edit { prefs ->
            val current = JsonCodec.decodeMinutesMap(prefs[Keys.TIME_LIMITS].orEmpty())
                .toMutableMap()
            if (minutesPerDay <= 0) current.remove(packageName) else current[packageName] = minutesPerDay
            prefs[Keys.TIME_LIMITS] = JsonCodec.encodeMinutesMap(current)
        }
    }

    suspend fun upsertSchedule(schedule: BlockSchedule) {
        context.launcherDataStore.edit { prefs ->
            val current = JsonCodec.decodeSchedules(prefs[Keys.SCHEDULES].orEmpty()).toMutableList()
            val index = current.indexOfFirst { it.id == schedule.id }
            if (index >= 0) current[index] = schedule else current.add(schedule)
            prefs[Keys.SCHEDULES] = JsonCodec.encodeSchedules(current)
        }
    }

    suspend fun toggleScheduleEnabled(id: String, enabled: Boolean) {
        context.launcherDataStore.edit { prefs ->
            val current = JsonCodec.decodeSchedules(prefs[Keys.SCHEDULES].orEmpty())
                .map { if (it.id == id) it.copy(enabled = enabled) else it }
            prefs[Keys.SCHEDULES] = JsonCodec.encodeSchedules(current)
        }
    }

    suspend fun removeSchedule(id: String) {
        context.launcherDataStore.edit { prefs ->
            val current = JsonCodec.decodeSchedules(prefs[Keys.SCHEDULES].orEmpty())
                .filterNot { it.id == id }
            prefs[Keys.SCHEDULES] = JsonCodec.encodeSchedules(current)
        }
    }

    suspend fun setMutedNotifications(packageName: String, muted: Boolean) {
        context.launcherDataStore.edit { prefs ->
            val current = mutableSetOf<String>()
            current.addAll(
                JsonCodec.decodeStringSet(prefs[Keys.MUTED_NOTIFICATIONS].orEmpty())
            )
            if (muted) current.add(packageName) else current.remove(packageName)
            prefs[Keys.MUTED_NOTIFICATIONS] = JsonCodec.encodeStringSet(current)
        }
    }

    suspend fun setCustomLabel(packageName: String, label: String?) {
        context.launcherDataStore.edit { prefs ->
            val current = JsonCodec.decodeStringMap(prefs[Keys.CUSTOM_LABELS].orEmpty())
                .toMutableMap()
            if (label.isNullOrBlank()) current.remove(packageName) else current[packageName] = label.trim()
            prefs[Keys.CUSTOM_LABELS] = JsonCodec.encodeStringMap(current)
        }
    }

    suspend fun setFontMode(fontMode: FontMode) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.FONT_MODE] = fontMode.name
        }
    }

    suspend fun setFontScale(scale: Float) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE] = scale
        }
    }

    suspend fun setMonochrome(monochrome: Boolean) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.MONOCHROME] = monochrome
        }
    }

    suspend fun setFocusDurationMinutes(minutes: Int) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.FOCUS_DURATION_MINUTES] = minutes
        }
    }

    suspend fun setUsageMinutes(usage: Map<String, Map<String, Int>>) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.USAGE_MINUTES] = JsonCodec.encodeUsage(usage)
        }
    }
}