package com.example.loginandregistration.models

import java.util.UUID

/**
 * Represents a message in the AI chat conversation
 */
data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: AIAction? = null
)

/**
 * Role of the message sender
 */
enum class MessageRole {
    USER,      // Message from the user
    ASSISTANT, // Message from the AI
    SYSTEM     // System message
}

/**
 * Represents an action that the AI can perform
 */
data class AIAction(
    val type: ActionType,
    val data: Map<String, Any>
)

/**
 * Types of actions the AI can perform
 */
enum class ActionType {
    CREATE_ASSIGNMENT,  // Create a new task/assignment
    UPDATE_TASK,        // Update an existing task
    SUGGEST_SCHEDULE,   // Suggest a schedule
    PROVIDE_INFO        // Provide information only
}

/**
 * Response from the AI API
 */
data class AIResponse(
    val message: String,
    val action: AIAction? = null,
    val success: Boolean = true,
    val error: String? = null
)

/**
 * Parsed task data from AI response
 */
data class AITaskData(
    val title: String,
    val description: String,
    val subject: String,
    val dueDate: String, // Format: YYYY-MM-DD
    val priority: String // low, medium, high
)
