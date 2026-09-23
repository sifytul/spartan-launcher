package com.spartan.launcer.domain.usecases

import com.spartan.launcer.data.SettingsDataStore
import kotlinx.coroutines.flow.first

class ToggleFavoriteUseCase(private val settingsDataStore: SettingsDataStore) {

    suspend operator fun invoke(packageName: String) {
        val current = settingsDataStore.settings.first()
        settingsDataStore.setFavorite(packageName, packageName !in current.favoritePackages)
    }
}