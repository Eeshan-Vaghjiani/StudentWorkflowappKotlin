package com.example.loginandregistration.utils

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.example.loginandregistration.R

/** Utility class for theme-related operations */
object ThemeUtils {

    /** Check if the app is currently in dark mode */
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /** Get color based on current theme */
    fun getThemedColor(context: Context, colorResId: Int): Int {
        return ContextCompat.getColor(context, colorResId)
    }

    /** Get status color based on task status */
    fun getStatusColor(context: Context, status: String): Int {
        return when (status.lowercase()) {
            "completed" -> ContextCompat.getColor(context, R.color.completed_color)
            "overdue" -> ContextCompat.getColor(context, R.color.overdue_color)
            "pending" -> ContextCompat.getColor(context, R.color.due_today_color)
            else -> ContextCompat.getColor(context, R.color.primary_color)
        }
    }

    /** Get priority color based on task priority */
    fun getPriorityColor(context: Context, priority: String): Int {
        return when (priority.lowercase()) {
            "high" -> ContextCompat.getColor(context, R.color.high_priority_color)
            "medium" -> ContextCompat.getColor(context, R.color.medium_priority_color)
            "low" -> ContextCompat.getColor(context, R.color.low_priority_color)
            else -> ContextCompat.getColor(context, R.color.text_secondary)
        }
    }

    /** Get hex color string for status (for backward compatibility) */
    fun getStatusColorHex(context: Context, status: String): String {
        val isDark = isDarkMode(context)
        return when (status.lowercase()) {
            "completed" -> if (isDark) "#32D74B" else "#34C759"
            "overdue" -> if (isDark) "#FF453A" else "#FF3B30"
            "pending" -> if (isDark) "#FF9F0A" else "#FF9500"
            else -> if (isDark) "#0A84FF" else "#007AFF"
        }
    }

    /** Get hex color string for priority (for backward compatibility) */
    fun getPriorityColorHex(context: Context, priority: String): String {
        val isDark = isDarkMode(context)
        return when (priority.lowercase()) {
            "high" -> if (isDark) "#FF453A" else "#F44336"
            "medium" -> if (isDark) "#FF9F0A" else "#FF9800"
            "low" -> if (isDark) "#32D74B" else "#4CAF50"
            else -> if (isDark) "#636366" else "#9E9E9E"
        }
    }
}
