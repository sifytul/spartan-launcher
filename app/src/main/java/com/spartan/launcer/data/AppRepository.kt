package com.spartan.launcer.data

import android.content.Context
import android.content.Intent
import com.spartan.launcer.data.model.AppInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val cachedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    /**
     * All launchable apps annotated with their favorite/hidden state.
     */
    val allApps: Flow<List<AppInfo>> = combine(cachedApps, settingsDataStore.settings) {
        installed,
        settings ->
        installed.map { app ->
            app.copy(
                isFavorite = app.packageName in settings.favoritePackages,
                isHidden = app.packageName in settings.hiddenPackages
            )
        }
    }

    /**
     * Favorite apps, hidden ones excluded, ordered by addition order.
     */
    val favorites: Flow<List<AppInfo>> = combine(allApps, settingsDataStore.settings) {
        apps,
        settings ->
        orderedFavorites(apps, settings.favoritePackages)
    }

    suspend fun refresh() {
        cachedApps.value = queryInstalledApps()
    }

    private suspend fun queryInstalledApps(): List<AppInfo> = withContext(ioDispatcher) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val manager = context.packageManager
        @Suppress("DEPRECATION")
        val resolveInfos = manager.queryIntentActivities(intent, 0)
        resolveInfos
            .asSequence()
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(manager)?.toString()
                    ?: resolveInfo.activityInfo.packageName
                AppInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    label = label
                )
            }
            .filterNot { it.packageName == context.packageName }
            .toList()
    }
}