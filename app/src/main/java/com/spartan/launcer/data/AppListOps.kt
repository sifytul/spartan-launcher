package com.spartan.launcer.data

import com.spartan.launcer.data.model.AppInfo
import java.text.Collator

/**
 * Pure helpers for filtering/sorting app lists so they can be unit tested
 * without an Android runtime.
 */
fun filterAndSortApps(
    apps: List<AppInfo>,
    query: String
): List<AppInfo> {
    val collator = Collator.getInstance()
    val normalizedQuery = query.trim()
    return apps
        .asSequence()
        .filter { !it.isHidden }
        .filter { app ->
            normalizedQuery.isEmpty() ||
                app.label.contains(normalizedQuery, ignoreCase = true) ||
                app.packageName.contains(normalizedQuery, ignoreCase = true)
        }
        .sortedWith(compareBy(collator) { it.label })
        .toList()
}

/**
 * Returns favorite, non-hidden apps ordered by the order they were added.
 */
fun orderedFavorites(
    apps: List<AppInfo>,
    favoritePackages: List<String>
): List<AppInfo> {
    val order = favoritePackages.withIndex().associate { (index, pkg) -> pkg to index }
    return apps
        .asSequence()
        .filter { it.isFavorite && !it.isHidden }
        .sortedBy { app -> order[app.packageName] ?: Int.MAX_VALUE }
        .toList()
}