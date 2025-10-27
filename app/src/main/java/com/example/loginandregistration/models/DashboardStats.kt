package com.example.loginandregistration.models

/**
 * Data class representing dashboard statistics with loading and error states Used to display
 * real-time data on the home dashboard
 */
data class DashboardStats(
        // Task statistics
        val totalTasks: Int = 0,
        val completedTasks: Int = 0,
        val pendingTasks: Int = 0,
        val overdueTasks: Int = 0,
        val tasksDue: Int = 0, // Overdue + due today

        // Group statistics
        val activeGroups: Int = 0,

        // AI usage statistics
        val aiUsageCount: Int = 0,
        val aiUsageLimit: Int = 10,

        // Session statistics (for study sessions)
        val totalSessions: Int = 0,

        // State management
        val isLoading: Boolean = true,
        val error: String? = null
) {
    /** Calculate AI usage progress percentage */
    fun getAIUsageProgress(): Int {
        return if (aiUsageLimit > 0) {
            (aiUsageCount * 100) / aiUsageLimit
        } else {
            0
        }
    }

    /** Calculate remaining AI prompts */
    fun getRemainingAIPrompts(): Int {
        return (aiUsageLimit - aiUsageCount).coerceAtLeast(0)
    }

    /** Check if there's an error state */
    fun hasError(): Boolean = error != null

    /** Check if data is successfully loaded */
    fun isSuccess(): Boolean = !isLoading && error == null
}
