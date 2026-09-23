package com.spartan.launcer.ui.notifications

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.service.NotificationFilterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationMuteViewModel(private val app: SpartanLauncherApp) : ViewModel() {

    private val container = app.container

    private val _hasNotificationAccess = MutableStateFlow(NotificationFilterService.isEnabled(app))
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private val muted = container.settingsDataStore.settings
        .map { it.mutedNotifications }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val apps: StateFlow<List<AppInfo>> = container.getAppListUseCase.allApps()
        .map { apps ->
            apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val mutedPackages: StateFlow<Set<String>> = muted

    fun refreshAccess() {
        _hasNotificationAccess.value = NotificationFilterService.isEnabled(app)
    }

    fun setMuted(packageName: String, muted: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.setMutedNotifications(packageName, muted)
        }
    }

    fun openNotificationAccessSettings() {
        val intent = Intent(
            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        )
        runCatching {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            app.startActivity(intent)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpartanLauncherApp
                NotificationMuteViewModel(app)
            }
        }
    }
}