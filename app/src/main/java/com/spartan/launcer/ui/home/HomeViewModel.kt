package com.spartan.launcer.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.domain.focus.FocusSession
import com.spartan.launcer.service.SpartanAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(private val app: SpartanLauncherApp) : ViewModel() {

    private val container = app.container

    val favorites: StateFlow<List<AppInfo>> = container.getAppListUseCase.favorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _isDefaultHome = MutableStateFlow(false)
    val isDefaultHome: StateFlow<Boolean> = _isDefaultHome.asStateFlow()

    val focusSession: StateFlow<FocusSession> = container.focusController.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusSession())

    init {
        recheckDefaultHome()
        viewModelScope.launch {
            while (isActive) {
                _currentTime.value = System.currentTimeMillis()
                delay(1_000)
            }
        }
    }

    fun recheckDefaultHome() {
        _isDefaultHome.value = isDefaultLauncher(app)
    }

    fun openHomeSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_HOME_SETTINGS)
        } else {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        }
        startActivityFromApp(intent)
    }

    fun launchClock() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        if (intent.resolveActivity(app.packageManager) != null) {
            startActivityFromApp(intent)
        }
    }

    fun launchApp(packageName: String) {
        container.launchAppUseCase(packageName)
    }

    fun openNotificationShade() {
        SpartanAccessibilityService.openNotificationShadeIfAvailable()
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
                HomeViewModel(app)
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