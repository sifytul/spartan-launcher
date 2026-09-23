package com.spartan.launcer.domain.usecases

import com.spartan.launcer.data.SettingsDataStore
import kotlinx.coroutines.flow.first

class HideAppUseCase(private val settingsDataStore: SettingsDataStore) {

    suspend operator fun invoke(packageName: String) {
        val current = settingsDataStore.settings.first()
        settingsDataStore.setHidden(packageName, packageName !in current.hiddenPackages)
    }
}