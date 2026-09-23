package com.spartan.launcer.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import com.spartan.launcer.data.openUsageAccessSettings
import com.spartan.launcer.data.model.FontMode
import com.spartan.launcer.domain.focus.FocusSession
import com.spartan.launcer.service.NotificationFilterService
import com.spartan.launcer.service.SpartanAccessibilityService
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

    val focusSession: StateFlow<FocusSession> = container.focusController.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusSession())

    private val _isDefaultHome = MutableStateFlow(false)
    val isDefaultHome: StateFlow<Boolean> = _isDefaultHome.asStateFlow()

    private val _notificationShadeEnabled = MutableStateFlow(false)
    val notificationShadeEnabled: StateFlow<Boolean> = _notificationShadeEnabled.asStateFlow()

    private val _canDrawOverlays = MutableStateFlow(false)
    val canDrawOverlays: StateFlow<Boolean> = _canDrawOverlays.asStateFlow()

    private val _hasUsageAccess = MutableStateFlow(false)
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(false)
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    init {
        recheckDefaultHome()
        refreshNotificationShadeState()
        refreshOverlayPermissionState()
        refreshUsageAccessState()
        refreshNotificationAccessState()
    }

    fun recheckDefaultHome() {
        _isDefaultHome.value = isDefaultLauncher(app)
    }

    fun refreshNotificationShadeState() {
        _notificationShadeEnabled.value =
            SpartanAccessibilityService.isServiceEnabled(app)
    }

    fun refreshOverlayPermissionState() {
        _canDrawOverlays.value = Settings.canDrawOverlays(app)
    }

    fun refreshUsageAccessState() {
        _hasUsageAccess.value = container.usageStatsRepository.hasUsageAccess()
    }

    fun refreshNotificationAccessState() {
        _hasNotificationAccess.value = NotificationFilterService.isEnabled(app)
    }

    fun openNotificationAccessSettings() {
        startActivityFromApp(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    fun openUsageAccessSettings() {
        app.openUsageAccessSettings()
    }

    fun openAccessibilitySettings() {
        startActivityFromApp(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openOverlayPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${app.packageName}")
        )
        startActivityFromApp(intent)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            container.settingsDataStore.setThemeMode(themeMode)
        }
    }

    fun setMonochrome(monochrome: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setMonochrome(monochrome)
        }
    }

    fun setFontMode(fontMode: FontMode) {
        viewModelScope.launch {
            container.settingsDataStore.setFontMode(fontMode)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            container.settingsDataStore.setFontScale(scale)
        }
    }

    fun setCustomLabel(packageName: String, label: String?) {
        viewModelScope.launch {
            container.settingsDataStore.setCustomLabel(packageName, label)
        }
    }

    fun setFavorite(packageName: String, favorite: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setFavorite(packageName, favorite)
        }
    }

    fun setBlocked(packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setBlocked(packageName, blocked)
        }
    }

    fun setBlockShortVideos(block: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setBlockShortVideos(block)
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

    fun setFocusDuration(minutes: Int) {
        viewModelScope.launch {
            container.settingsDataStore.setFocusDurationMinutes(minutes)
        }
    }

    fun startFocus() {
        container.focusController.startFocus(settings.value.focusDurationMinutes)
    }

    fun pauseFocus() = container.focusController.pause()

    fun resumeFocus() = container.focusController.resume()

    fun stopFocus() = container.focusController.stop()

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