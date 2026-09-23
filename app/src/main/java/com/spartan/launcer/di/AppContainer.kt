package com.spartan.launcer.di

import android.content.Context
import com.spartan.launcer.data.AppRepository
import com.spartan.launcer.data.ForegroundUsageTracker
import com.spartan.launcer.data.SettingsDataStore
import com.spartan.launcer.data.UsageStatsRepository
import com.spartan.launcer.domain.blocking.AppBlocker
import com.spartan.launcer.domain.focus.FocusController
import com.spartan.launcer.domain.usecases.GetAppListUseCase
import com.spartan.launcer.domain.usecases.HideAppUseCase
import com.spartan.launcer.domain.usecases.LaunchAppUseCase
import com.spartan.launcer.domain.usecases.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settingsDataStore: SettingsDataStore = SettingsDataStore(appContext)

    val appRepository: AppRepository = AppRepository(appContext, settingsDataStore)

    val usageStatsRepository: UsageStatsRepository = UsageStatsRepository(appContext)

    val focusController: FocusController = FocusController(appContext, appScope)

    val foregroundUsageTracker: ForegroundUsageTracker =
        ForegroundUsageTracker(appContext.packageName, settingsDataStore, appScope)

    val appBlocker: AppBlocker = AppBlocker(
        appContext,
        appScope,
        foregroundUsageTracker,
        settingsDataStore,
        focusController
    )

    val getAppListUseCase: GetAppListUseCase = GetAppListUseCase(appRepository)

    val toggleFavoriteUseCase: ToggleFavoriteUseCase = ToggleFavoriteUseCase(settingsDataStore)

    val hideAppUseCase: HideAppUseCase = HideAppUseCase(settingsDataStore)

    val launchAppUseCase: LaunchAppUseCase = LaunchAppUseCase(appContext)
}