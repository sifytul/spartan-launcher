package com.spartan.launcer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spartan.launcer.ui.blocking.BlockingSchedulesScreen
import com.spartan.launcer.ui.drawer.AppDrawerScreen
import com.spartan.launcer.ui.home.HomeScreen
import com.spartan.launcer.ui.settings.SettingsScreen
import com.spartan.launcer.ui.usage.ScreenTimeScreen

object Routes {
    const val HOME = "home"
    const val DRAWER = "drawer"
    const val SETTINGS = "settings"
    const val SCREEN_TIME = "screen_time"
    const val BLOCKING_SCHEDULES = "blocking_schedules"
}

@Composable
fun LauncherNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenDrawer = { navController.navigate(Routes.DRAWER) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.DRAWER) {
            AppDrawerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenScreenTime = { navController.navigate(Routes.SCREEN_TIME) },
                onOpenSchedules = { navController.navigate(Routes.BLOCKING_SCHEDULES) }
            )
        }
        composable(Routes.SCREEN_TIME) {
            ScreenTimeScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.BLOCKING_SCHEDULES) {
            BlockingSchedulesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}