package com.spartan.launcer.ui.blocking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spartan.launcer.SpartanLauncherApp
import com.spartan.launcer.data.model.BlockSchedule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BlockingSchedulesViewModel(private val app: SpartanLauncherApp) : ViewModel() {

    private val container = app.container

    val schedules: StateFlow<List<BlockSchedule>> = container.settingsDataStore.settings
        .map { it.schedules }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsert(schedule: BlockSchedule) {
        viewModelScope.launch {
            container.settingsDataStore.upsertSchedule(schedule)
        }
    }

    fun toggleEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch {
            container.settingsDataStore.toggleScheduleEnabled(id, enabled)
        }
    }

    fun remove(id: String) {
        viewModelScope.launch {
            container.settingsDataStore.removeSchedule(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SpartanLauncherApp
                BlockingSchedulesViewModel(app)
            }
        }
    }
}