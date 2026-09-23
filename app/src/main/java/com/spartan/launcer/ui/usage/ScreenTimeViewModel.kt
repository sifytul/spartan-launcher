package com.spartan.launcer.ui.usage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.openUsageAccessSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UsageEntry(
    val packageName: String,
    val label: String,
    val minutesToday: Int,
    val dailyLimitMinutes: Int?
)

class ScreenTimeViewModel(private val app: SpartanLauncherApp) : ViewModel() {

    private val container = app.container
    private val usageRepository = container.usageStatsRepository

    private val _hasUsageAccess = MutableStateFlow(usageRepository.hasUsageAccess())
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0L)

    private val appLabels = container.getAppListUseCase.allApps()
        .map { apps -> apps.associate { it.packageName to it.displayLabel } }

    val usage: StateFlow<List<UsageEntry>> = combine(
        appLabels,
        container.settingsDataStore.settings,
        _refreshTrigger
    ) { labels, settings, _ ->
        val usedToday = usageRepository.minutesUsedTodayByPackage()
        val packages = (usedToday.keys + settings.timeLimits.keys)
            .sortedByDescending { usedToday[it] ?: 0 }
        packages.mapNotNull { pkg ->
            val minutes = usedToday[pkg] ?: 0
            if (minutes == 0 && settings.timeLimits[pkg] == null) return@mapNotNull null
            UsageEntry(
                packageName = pkg,
                label = labels[pkg] ?: pkg,
                minutesToday = minutes,
                dailyLimitMinutes = settings.timeLimits[pkg]
            )
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalMinutesToday: StateFlow<Int> = usage
        .map { entries -> entries.sumOf { it.minutesToday } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun refresh() {
        _hasUsageAccess.value = usageRepository.hasUsageAccess()
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setDailyLimit(packageName: String, minutesPerDay: Int) {
        viewModelScope.launch {
            container.settingsDataStore.setTimeLimit(packageName, minutesPerDay)
        }
    }

    fun openUsageAccessSettings() {
        app.openUsageAccessSettings()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpartanLauncherApp
                ScreenTimeViewModel(app)
            }
        }
    }
}