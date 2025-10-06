// app/src/main/java/com/example/loginandregistration/ui/groups/GroupsViewModel.kt
package com.example.loginandregistration.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.repository.GroupsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Groups screen following MVVM architecture. Manages UI state and coordinates
 * data loading from repository.
 */
class GroupsViewModel(private val repository: GroupsRepository = GroupsRepository()) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(GroupsUiState())

    // Public read-only state
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        // Load data automatically when ViewModel is created
        loadDashboardData()
    }

    /**
     * Loads all dashboard data in parallel to minimize loading time. Uses async/await pattern for
     * concurrent execution.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // Set loading state
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Launch all data fetching operations concurrently
                val myGroupsDeferred = async { repository.getMyGroupsCount() }
                val activeAssignmentsDeferred = async { repository.getActiveAssignmentsCount() }
                val newMessagesDeferred = async { repository.getNewMessagesCount() }
                val totalTasksDeferred = async { repository.getTotalTasksCount() }
                val completedTasksDeferred = async { repository.getCompletedTasksCount() }
                val overdueTasksDeferred = async { repository.getOverdueTasksCount() }

                // Wait for all operations to complete
                val myGroupsCount = myGroupsDeferred.await()
                val activeAssignmentsCount = activeAssignmentsDeferred.await()
                val newMessagesCount = newMessagesDeferred.await()
                val totalTasksCount = totalTasksDeferred.await()
                val completedTasksCount = completedTasksDeferred.await()
                val overdueTasksCount = overdueTasksDeferred.await()

                // Update state with all data at once
                _uiState.update { currentState ->
                    currentState.copy(
                            isLoading = false,
                            errorMessage = null,
                            myGroupsCount = myGroupsCount,
                            activeAssignmentsCount = activeAssignmentsCount,
                            newMessagesCount = newMessagesCount,
                            totalTasksCount = totalTasksCount,
                            completedTasksCount = completedTasksCount,
                            overdueTasksCount = overdueTasksCount
                    )
                }
            } catch (e: Exception) {
                // Handle any unexpected errors
                android.util.Log.e("GroupsViewModel", "Error loading dashboard data", e)
                _uiState.update { currentState ->
                    currentState.copy(
                            isLoading = false,
                            errorMessage = "Failed to load dashboard data. Please try again."
                    )
                }
            }
        }
    }

    /** Clears any error state and retries loading data. */
    fun retry() {
        loadDashboardData()
    }

    /** Clears the current error message. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
