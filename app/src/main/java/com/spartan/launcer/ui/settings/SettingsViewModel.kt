package com.spartan.launcer.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.data.model.LauncherSettings
import com.spartan.launcer.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val app: SpartanLauncherApp) : ViewModel() {

    private val container = app.container

    val settings: StateFlow<LauncherSettings> = container.settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings())

    val allApps: StateFlow<List<AppInfo>> = container.getAppListUseCase.allApps()
        .map { apps ->
            apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isDefaultHome = MutableStateFlow(false)
    val isDefaultHome: StateFlow<Boolean> = _isDefaultHome.asStateFlow()

    init {
        recheckDefaultHome()
    }

    fun recheckDefaultHome() {
        _isDefaultHome.value = isDefaultLauncher(app)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            container.settingsDataStore.setThemeMode(themeMode)
        }
    }

    fun setFavorite(packageName: String, favorite: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setFavorite(packageName, favorite)
        }
    }

    fun setHidden(packageName: String, hidden: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setHidden(packageName, hidden)
        }
    }

    fun openHomeSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_HOME_SETTINGS)
        } else {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        }
        startActivityFromApp(intent)
    }

    private fun startActivityFromApp(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { app.startActivity(intent) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpartanLauncherApp
                SettingsViewModel(app)
            }
        }
    }
}

private fun isDefaultLauncher(context: Context): Boolean {
    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolveInfo = context.packageManager.resolveActivity(
        homeIntent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return resolveInfo?.activityInfo?.packageName == context.packageName
}