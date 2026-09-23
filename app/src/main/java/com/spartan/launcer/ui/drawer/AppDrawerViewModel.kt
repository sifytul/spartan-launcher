package com.spartan.launcer.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.filterAndSortApps
import com.spartan.launcer.data.model.AppInfo
import com.spartan.launcer.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppDrawerViewModel(private val container: AppContainer) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val allApps = container.getAppListUseCase.allApps()

    val apps: StateFlow<List<AppInfo>> = combine(allApps, _query) { apps, q ->
        filterAndSortApps(apps, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            container.toggleFavoriteUseCase(packageName)
        }
    }

    fun toggleHidden(packageName: String) {
        viewModelScope.launch {
            container.hideAppUseCase(packageName)
        }
    }

    fun setCustomLabel(packageName: String, label: String?) {
        viewModelScope.launch {
            container.settingsDataStore.setCustomLabel(packageName, label)
        }
    }

    fun launchApp(packageName: String) {
        container.launchAppUseCase(packageName)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpartanLauncherApp
                AppDrawerViewModel(app.container)
            }
        }
    }
}