package com.example.loginandregistration.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.BuildConfig
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.services.GeminiAssistantService
import com.example.loginandregistration.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing tasks with proper error handling.
 * Demonstrates how to propagate errors from repositories to the UI.
 */
class TasksViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TasksViewModel"
    }

    private val repository = TaskRepository(application.applicationContext)
    
    // AI Assistant Service (lazy initialization to handle missing API key gracefully)
    private val aiService: GeminiAssistantService? by lazy {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotEmpty()) {
                GeminiAssistantService(apiKey, repository)
            } else {
                Log.w(TAG, "Gemini API key not configured")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize AI service: ${e.message}")
            null
        }
    }

    // UI State
    private val _tasks = MutableStateFlow<List<FirebaseTask>>(emptyList())
    val tasks: StateFlow<List<FirebaseTask>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state - exposed as AppError for better error handling in UI
    private val _error = MutableStateFlow<ErrorHandler.AppError?>(null)
    val error: StateFlow<ErrorHandler.AppError?> = _error.asStateFlow()

    // Success messages
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Setup real-time listener for tasks
        setupRealTimeListener()
    }

    /**
     * Setup real-time listener for user's tasks
     */
    private fun setupRealTimeListener() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.getUserTasksFlow().collect { taskList ->
                    _tasks.value = taskList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = ErrorHandler.AppError.UnknownError(
                    e.message ?: "Failed to load tasks",
                    e as? Exception
                )
            }
        }
    }

    /**
     * Load user's tasks with error handling (for manual refresh)
     */
    fun loadUserTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.getUserTasks()
            
            result.fold(
                onSuccess = { taskList ->
                    _tasks.value = taskList
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.UnknownError(
                            exception.message ?: "Failed to load tasks",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Create a new task with error handling
     */
    fun createTask(task: FirebaseTask) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.createTask(task)
            
            result.fold(
                onSuccess = { taskId ->
                    _isLoading.value = false
                    _successMessage.value = "Task created successfully!"
                    // Real-time listener will automatically update the tasks list
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.FirestoreError(
                            "Failed to create task: ${exception.message}",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Update a task with error handling
     */
    fun updateTask(taskId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _error.value = null

            val result = repository.updateTask(taskId, updates)
            
            result.fold(
                onSuccess = {
                    _successMessage.value = "Task updated successfully!"
                    // Real-time listener will automatically update the tasks list
                },
                onFailure = { exception ->
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.FirestoreError(
                            "Failed to update task: ${exception.message}",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    /**
     * Delete a task with error handling
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _error.value = null

            val result = repository.deleteTask(taskId)
            
            result.fold(
                onSuccess = {
                    _successMessage.value = "Task deleted successfully!"
                    // Real-time listener will automatically update the tasks list
                },
                onFailure = { exception ->
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.FirestoreError(
                            "Failed to delete task: ${exception.message}",
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

    /**
     * Create a task from AI-generated suggestion
     * @param aiPrompt The user's prompt to the AI (e.g., "Create a math homework assignment due next week")
     */
    fun createTaskFromAI(aiPrompt: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val service = aiService
            if (service == null) {
                _isLoading.value = false
                _error.value = ErrorHandler.AppError.UnknownError(
                    "AI Assistant is not configured. Please add GEMINI_API_KEY to local.properties",
                    null
                )
                return@launch
            }

            try {
                Log.d(TAG, "Sending prompt to AI: $aiPrompt")
                
                // Send message to AI
                val aiResponse = service.sendMessage(aiPrompt)
                
                aiResponse.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "AI Response: ${response.message}")
                        
                        // Check if AI wants to create an assignment
                        if (response.action != null && response.action.type == com.example.loginandregistration.models.ActionType.CREATE_ASSIGNMENT) {
                            Log.d(TAG, "AI suggested creating an assignment")
                            
                            // Create assignment from AI suggestion
                            val createResult = service.createAssignmentFromAI(response.message)
                            
                            createResult.fold(
                                onSuccess = { task ->
                                    _isLoading.value = false
                                    _successMessage.value = "Task created successfully with AI assistance!"
                                    Log.d(TAG, "Task created from AI: ${task.title}")
                                },
                                onFailure = { exception ->
                                    _isLoading.value = false
                                    _error.value = ErrorHandler.AppError.UnknownError(
                                        "Failed to create task from AI: ${exception.message}",
                                        exception as? Exception
                                    )
                                    Log.e(TAG, "Failed to create task from AI", exception)
                                }
                            )
                        } else {
                            // AI didn't suggest creating a task, just show the response
                            _isLoading.value = false
                            _error.value = ErrorHandler.AppError.UnknownError(
                                "AI Response: ${response.message}\n\nPlease ask me to create a specific task or assignment.",
                                null
                            )
                        }
                    },
                    onFailure = { exception ->
                        _isLoading.value = false
                        _error.value = ErrorHandler.AppError.NetworkError(
                            "Failed to communicate with AI: ${exception.message}",
                            exception as? Exception
                        )
                        Log.e(TAG, "AI communication failed", exception)
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = ErrorHandler.AppError.UnknownError(
                    "Error creating task with AI: ${e.message}",
                    e
                )
                Log.e(TAG, "Error in createTaskFromAI", e)
            }
        }
    }

    /**
     * Check if AI service is available
     */
    fun isAIServiceAvailable(): Boolean {
        return aiService != null
    }
}
