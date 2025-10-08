// app/src/main/java/com/example/loginandregistration/ui/groups/GroupsUiState.kt
package com.example.loginandregistration.ui.groups

/**
 * Immutable UI state for the Groups screen. Represents all possible states: loading, error, and
 * success with data.
 */
data class GroupsUiState(
        val isLoading: Boolean = true,
        val errorMessage: String? = null,

        // Statistics
        val myGroupsCount: Int = 0,
        val activeAssignmentsCount: Int = 0,
        val newMessagesCount: Int = 0,

        // Additional stats for future use
        val totalTasksCount: Int = 0,
        val completedTasksCount: Int = 0,
        val overdueTasksCount: Int = 0
) {
    /** Convenience property to check if we're in an error state */
    val hasError: Boolean
        get() = errorMessage != null

    /** Convenience property to check if we have valid data to display */
    val hasData: Boolean
        get() = !isLoading && !hasError

    /** Convenience property for completion rate calculation */
    val completionRate: Int
        get() =
                if (totalTasksCount > 0) {
                    (completedTasksCount * 100) / totalTasksCount
                } else 0
}
