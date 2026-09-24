package com.spartan.launcer.ui.navigation

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spartan.launcer.ui.blocking.BlockingSchedulesScreen
import com.spartan.launcer.ui.drawer.AppDrawerScreen
import com.spartan.launcer.ui.home.HomeScreen
import com.spartan.launcer.ui.notifications.NotificationMuteScreen
import com.spartan.launcer.ui.settings.AppManageScreen
import com.spartan.launcer.ui.settings.SettingsScreen
import com.spartan.launcer.ui.settings.ShortVideoAppsScreen
import com.spartan.launcer.ui.usage.ScreenTimeScreen
import com.spartan.launcer.ui.word.WordDetailScreen

object Routes {
    const val HOME = "home"
    const val DRAWER = "drawer"
    const val SETTINGS = "settings"
    const val APP_MANAGE = "app_manage"
    const val SCREEN_TIME = "screen_time"
    const val BLOCKING_SCHEDULES = "blocking_schedules"
    const val NOTIFICATION_MUTE = "notification_mute"
    const val SHORT_VIDEO_APPS = "short_video_apps"
    const val WORD_DETAIL = "word/{word}"

    fun wordDetail(word: String): String = "word/${Uri.encode(word)}"
}

@Composable
fun LauncherNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenDrawer = { navController.navigate(Routes.DRAWER) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenWord = { word -> navController.navigate(Routes.wordDetail(word)) }
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
                onOpenSchedules = { navController.navigate(Routes.BLOCKING_SCHEDULES) },
                onOpenMuteNotifications = { navController.navigate(Routes.NOTIFICATION_MUTE) },
                onOpenAppManage = { navController.navigate(Routes.APP_MANAGE) },
                onOpenShortVideoApps = { navController.navigate(Routes.SHORT_VIDEO_APPS) }
            )
        }
        composable(Routes.APP_MANAGE) {
            AppManageScreen(
                onBack = { navController.popBackStack() }
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
        composable(Routes.NOTIFICATION_MUTE) {
            NotificationMuteScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SHORT_VIDEO_APPS) {
            ShortVideoAppsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.WORD_DETAIL,
            arguments = listOf(navArgument("word") { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(tween(280)) { it } },
            popExitTransition = { slideOutHorizontally(tween(280)) { it } }
        ) { backStackEntry ->
            val word = backStackEntry.arguments?.getString("word").orEmpty()
            WordDetailScreen(
                word = word,
                onBack = { navController.popBackStack() }
            )
        }
    }
}