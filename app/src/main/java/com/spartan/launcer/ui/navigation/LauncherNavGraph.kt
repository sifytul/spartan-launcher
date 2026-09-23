package com.spartan.launcer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spartan.launcer.ui.drawer.AppDrawerScreen
import com.spartan.launcer.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val DRAWER = "drawer"
    const val SETTINGS = "settings"
}

@Composable
fun LauncherNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenDrawer = { navController.navigate(Routes.DRAWER) }
            )
        }
        composable(Routes.DRAWER) {
            AppDrawerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}