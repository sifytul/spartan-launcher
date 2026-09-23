package com.spartan.launcer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    }

    val settings: Flow<LauncherSettings> = context.launcherDataStore.data.map { prefs ->
        LauncherSettings(
            favoritePackages = prefs[Keys.FAVORITE_PACKAGES].orEmpty().toList(),
            hiddenPackages = prefs[Keys.HIDDEN_PACKAGES].orEmpty().toList(),
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: ThemeMode.SYSTEM
        )
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
}