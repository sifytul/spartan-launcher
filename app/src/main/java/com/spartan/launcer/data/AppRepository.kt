package com.spartan.launcer.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
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
                isHidden = app.packageName in settings.hiddenPackages,
                customLabel = settings.customLabels[app.packageName]
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
        val manager = context.packageManager
        @Suppress("DEPRECATION")
        val resolveInfos = manager.queryIntentActivities(launcherIntent(), 0)
        val primaryApps = resolveInfos
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

        val workApps = queryWorkProfileApps()
        (primaryApps + workApps).distinctBy { it.packageName }
    }

    /**
     * Apps only reachable through a work (managed) profile. Duplicate
     * package names already present in the primary profile are skipped so the
     * drawer does not show the same app twice. Launching these uses
     * [LauncherApps] with the owning user handle.
     */
    private fun queryWorkProfileApps(): List<AppInfo> {
        val launcherApps = context.getSystemService(LauncherApps::class.java)
            ?: return emptyList()
        val result = mutableListOf<AppInfo>()
        launcherApps.profiles.forEach { user ->
            runCatching { launcherApps.getActivityList(null, user) }
                .getOrDefault(emptyList())
                .forEach { activity ->
                    val packageName = activity.componentName.packageName
                    if (packageName == context.packageName) return@forEach
                    if (result.any { it.packageName == packageName }) return@forEach
                    result.add(
                        AppInfo(
                            packageName = packageName,
                            label = activity.label?.toString() ?: packageName,
                            user = user,
                            component = activity.componentName
                        )
                    )
                }
        }
        return result
    }

    private fun launcherIntent(): Intent =
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
}