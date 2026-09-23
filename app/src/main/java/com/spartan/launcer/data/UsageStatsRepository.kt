package com.spartan.launcer.data

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId

/**
 * Thin wrapper over [UsageStatsManager] used for the screen-time report page.
 * Requires the user to grant "Usage access" (PACKAGE_USAGE_STATS app-op).
 */
class UsageStatsRepository(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED) return true
        // Some OEMs report via a different path; trust the manager if it returns data.
        return try {
            val now = System.currentTimeMillis()
            usageManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                now - HOUR_MS,
                now
            ).isNotEmpty()
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun minutesUsedTodayByPackage(): Map<String, Int> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val begin = today.atStartOfDay(zone).toInstant().toEpochMilli()
        return minutesByPackage(begin, System.currentTimeMillis())
    }

    fun totalMinutesToday(): Int =
        minutesUsedTodayByPackage().values.sum()

    /**
     * Minutes per package for each of the last [days] full days, oldest first.
     */
    fun dailyMinutes(packageName: String, days: Int): List<Pair<LocalDate, Int>> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        return (0 until days).map { offset ->
            val date = today.minusDays(offset.toLong())
            val begin = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            date to (minutesByPackage(begin, end)[packageName] ?: 0)
        }.reversed()
    }

    @Suppress("DEPRECATION")
    private fun minutesByPackage(begin: Long, end: Long): Map<String, Int> {
        val stats = try {
            usageManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, begin, end)
        } catch (_: SecurityException) {
            return emptyMap()
        } catch (_: Exception) {
            return emptyMap()
        }
        return stats
            .filter { it.totalTimeInForeground > 0 }
            .groupBy { it.packageName }
            .mapValues { (_, entries) ->
                (entries.sumOf { it.totalTimeInForeground } / 60_000L).toInt()
            }
    }

    private val usageManager
        get() = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    companion object {
        private const val HOUR_MS = 3_600_000L
    }
}

/** Opens the system's usage-access screen for this app. */
fun Context.openUsageAccessSettings() {
    runCatching {
        startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}