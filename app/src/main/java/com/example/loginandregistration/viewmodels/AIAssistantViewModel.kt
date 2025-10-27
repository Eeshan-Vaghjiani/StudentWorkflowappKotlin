package com.example.loginandregistration.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.models.AIChatMessage
import com.example.loginandregistration.models.AIResponse
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.models.MessageRole
import com.example.loginandregistration.services.GeminiAssistantService
import kotlinx.coroutines.launch

/**
 * ViewModel for AI Assistant functionality
 */
class AIAssistantViewModel(
    private val geminiService: GeminiAssistantService
) : ViewModel() {

    companion object {
        private const val TAG = "AIAssistantViewModel"
    }

    // Messages in the conversation
    private val _messages = MutableLiveData<List<AIChatMessage>>(emptyList())
    val messages: LiveData<List<AIChatMessage>> = _messages

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error state
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Task creation success
    private val _taskCreated = MutableLiveData<FirebaseTask?>()
    val taskCreated: LiveData<FirebaseTask?> = _taskCreated

    /**
     * Send a message to the AI assistant
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) {
            return
        }

        viewModelScope.launch {
            try {
                // Add user message to the conversation
                val userMessage = AIChatMessage(
                    role = MessageRole.USER,
                    content = messageText
                )
                addMessage(userMessage)

                // Show loading state
                _isLoading.value = true
                _error.value = null

                // Get conversation history
                val history = _messages.value ?: emptyList()

                // Send message to AI
                val result = geminiService.sendMessage(messageText, history)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        handleAIResponse(response)
                    } else {
                        _error.value = "Received empty response from AI"
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "Error sending message", exception)
                    _error.value = exception?.message ?: "Failed to send message"
                    
                    // Add error message to conversation
                    val errorMessage = AIChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = "Sorry, I encountered an error. Please try again."
                    )
                    addMessage(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendMessage", e)
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handle the AI response
     */
    private fun handleAIResponse(response: AIResponse) {
        // Add AI message to conversation
        val aiMessage = AIChatMessage(
            role = MessageRole.ASSISTANT,
            content = response.message,
            action = response.action
        )
        addMessage(aiMessage)

        // Check if there was an error in the response
        if (!response.success && response.error != null) {
            _error.value = response.error
        }
    }

    /**
     * Create an assignment from AI suggestion
     */
    fun createAssignmentFromAI(message: AIChatMessage) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = geminiService.createAssignmentFromAI(message.content)

                if (result.isSuccess) {
                    val task = result.getOrNull()
                    _taskCreated.value = task
                    
                    // Add confirmation message
                    val confirmationMessage = AIChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = "Great! I've created the assignment \"${task?.title}\" for you. You can find it in your tasks list."
                    )
                    addMessage(confirmationMessage)
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "Error creating assignment", exception)
                    _error.value = exception?.message ?: "Failed to create assignment"
                    
                    // Add error message
                    val errorMessage = AIChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = "Sorry, I couldn't create the assignment. Please try again or create it manually."
                    )
                    addMessage(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in createAssignmentFromAI", e)
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a message to the conversation
     */
    private fun addMessage(message: AIChatMessage) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _messages.value = currentMessages
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear task created state
     */
    fun clearTaskCreated() {
        _taskCreated.value = null
    }

    /**
     * Clear conversation
     */
    fun clearConversation() {
        _messages.value = emptyList()
        _error.value = null
        _taskCreated.value = null
    }
}
