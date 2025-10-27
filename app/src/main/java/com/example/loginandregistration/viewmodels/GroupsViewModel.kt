package com.example.loginandregistration.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.repository.GroupRepository
import com.example.loginandregistration.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing groups with proper error handling.
 * Demonstrates how to propagate errors from repositories to the UI.
 */
class GroupsViewModel : ViewModel() {

    private val repository = GroupRepository()

    // UI State
    private val _groups = MutableStateFlow<List<FirebaseGroup>>(emptyList())
    val groups: StateFlow<List<FirebaseGroup>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state - exposed as AppError for better error handling in UI
    private val _error = MutableStateFlow<ErrorHandler.AppError?>(null)
    val error: StateFlow<ErrorHandler.AppError?> = _error.asStateFlow()

    // Success messages
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Load user's groups with error handling
     */
    fun loadUserGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.getUserGroups()
            
            result.fold(
                onSuccess = { groupList ->
                    _groups.value = groupList
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    // Convert exception to AppError if it's already one, otherwise create UnknownError
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.UnknownError(
                            exception.message ?: "Failed to load groups",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new group with error handling
     */
    fun createGroup(name: String, description: String, subject: String, privacy: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.createGroup(name, description, subject, privacy)
            
            result.fold(
                onSuccess = { groupId ->
                    _isLoading.value = false
                    _successMessage.value = "Group created successfully!"
                    // Reload groups to show the new one
                    loadUserGroups()
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.FirestoreError(
                            "Failed to create group: ${exception.message}",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Load public groups with error handling
     */
    fun loadPublicGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.getPublicGroups()
            
            result.fold(
                onSuccess = { groupList ->
                    _groups.value = groupList
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.UnknownError(
                            exception.message ?: "Failed to load public groups",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Clear error state (call this after showing error to user)
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear success message (call this after showing message to user)
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
