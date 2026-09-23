package com.spartan.launcer.data

import com.spartan.launcer.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class AppListOpsTest {

    private fun app(pkg: String, label: String, hidden: Boolean = false) =
        AppInfo(packageName = pkg, label = label, isHidden = hidden)

    @Test
    fun emptyQueryReturnsAllVisibleApps() {
        val apps = listOf(app("a", "Alpha"), app("b", "Beta"))
        assertEquals(apps, filterAndSortApps(apps, ""))
    }

    @Test
    fun hiddenAppsAreExcluded() {
        val apps = listOf(app("a", "Alpha"), app("b", "Beta", hidden = true))
        assertEquals(listOf(app("a", "Alpha")), filterAndSortApps(apps, ""))
    }

    @Test
    fun queryMatchesLabelCaseInsensitively() {
        val apps = listOf(app("a.pkg", "Alpha"), app("b.pkg", "Beta"))
        assertEquals(
            listOf(app("a.pkg", "Alpha")),
            filterAndSortApps(apps, "alph")
        )
    }

    @Test
    fun queryMatchesPackageName() {
        val apps = listOf(app("com.example.zero", "Zero"), app("com.other.one", "One"))
        assertEquals(
            listOf(app("com.example.zero", "Zero")),
            filterAndSortApps(apps, "example")
        )
    }

    @Test
    fun resultIsSortedAlphabeticallyIgnoringCase() {
        val apps = listOf(
            app("c", "Camera"),
            app("a", "alpha"),
            app("b", "Bravo")
        )
        assertEquals(
            listOf(app("a", "alpha"), app("b", "Bravo"), app("c", "Camera")),
            filterAndSortApps(apps, "")
        )
    }

    @Test
    fun favoriteAppsAreOrderedByAddition() {
        val apps = listOf(
            AppInfo("phone", "Phone", isFavorite = true),
            AppInfo("maps", "Maps", isFavorite = true),
            AppInfo("camera", "Camera", isFavorite = true, isHidden = true)
        )
        val favorites = listOf("maps", "phone")
        assertEquals(
            listOf(
                AppInfo("maps", "Maps", isFavorite = true),
                AppInfo("phone", "Phone", isFavorite = true)
            ),
            orderedFavorites(apps, favorites)
        )
    }

    @Test
    fun favoritesExcludeNonFavoriteApps() {
        val apps = listOf(
            AppInfo("phone", "Phone"),
            AppInfo("maps", "Maps", isFavorite = true)
        )
        assertEquals(
            listOf(AppInfo("maps", "Maps", isFavorite = true)),
            orderedFavorites(apps, listOf("maps"))
        )
    }
}