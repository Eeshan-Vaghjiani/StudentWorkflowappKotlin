package com.example.loginandregistration.utils

/**
 * Centralized object containing all user-friendly error messages used throughout the app. This
 * ensures consistent messaging and makes it easy to update messages in one place.
 */
object ErrorMessages {

    // Firestore Permission Errors
    const val PERMISSION_DENIED =
            "You don't have permission to access this data. Please try logging out and back in."

    // Network Errors
    const val NETWORK_ERROR =
            "Unable to connect. Please check your internet connection and try again."
    const val TIMEOUT = "The request took too long. Please check your connection and try again."

    // Firestore Index Errors
    const val INDEX_MISSING = "Database is being configured. Please try again in a few moments."

    // Generic Errors
    const val GENERIC_ERROR = "Something went wrong. Please try again."
    const val UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later."

    // Authentication Errors
    const val UNAUTHENTICATED =
            "You need to be logged in to perform this action. Please log in and try again."
    const val AUTH_FAILED = "Authentication failed. Please check your credentials and try again."

    // Data Errors
    const val NOT_FOUND = "The requested data could not be found."
    const val ALREADY_EXISTS = "This item already exists."
    const val DATA_LOAD_FAILED = "Failed to load data. Please try again."

    // Operation Errors
    const val OPERATION_CANCELLED = "The operation was cancelled."
    const val OPERATION_FAILED = "The operation failed. Please try again."
    const val SAVE_FAILED = "Failed to save changes. Please try again."
    const val DELETE_FAILED = "Failed to delete item. Please try again."
    const val UPDATE_FAILED = "Failed to update item. Please try again."

    // Resource Errors
    const val QUOTA_EXCEEDED = "Service limit reached. Please try again later."
    const val STORAGE_FULL = "Storage is full. Please free up some space and try again."

    // AI Assistant Errors
    const val AI_UNAVAILABLE = "AI assistant is temporarily unavailable. Please try again later."
    const val AI_REQUEST_FAILED = "Failed to get AI response. Please try again."
    const val AI_INVALID_RESPONSE = "Received an invalid response from AI. Please try again."

    // Group Errors
    const val GROUP_LOAD_FAILED = "Failed to load groups. Please try again."
    const val GROUP_CREATE_FAILED = "Failed to create group. Please try again."
    const val GROUP_JOIN_FAILED = "Failed to join group. Please try again."
    const val GROUP_LEAVE_FAILED = "Failed to leave group. Please try again."
    const val GROUP_NOT_FOUND = "Group not found."

    // Task Errors
    const val TASK_LOAD_FAILED = "Failed to load tasks. Please try again."
    const val TASK_CREATE_FAILED = "Failed to create task. Please try again."
    const val TASK_UPDATE_FAILED = "Failed to update task. Please try again."
    const val TASK_DELETE_FAILED = "Failed to delete task. Please try again."
    const val TASK_NOT_FOUND = "Task not found."

    // Chat Errors
    const val CHAT_LOAD_FAILED = "Failed to load messages. Please try again."
    const val MESSAGE_SEND_FAILED = "Failed to send message. Please try again."
    const val CHAT_CREATE_FAILED = "Failed to create chat. Please try again."
    const val CHAT_NOT_FOUND = "Chat not found."

    // Calendar Errors
    const val CALENDAR_LOAD_FAILED = "Failed to load calendar events. Please try again."
    const val EVENT_CREATE_FAILED = "Failed to create event. Please try again."

    // Profile Errors
    const val PROFILE_LOAD_FAILED = "Failed to load profile. Please try again."
    const val PROFILE_UPDATE_FAILED = "Failed to update profile. Please try again."
    const val PROFILE_PICTURE_UPLOAD_FAILED = "Failed to upload profile picture. Please try again."

    // Validation Errors
    const val INVALID_INPUT = "Please check your input and try again."
    const val REQUIRED_FIELD = "This field is required."
    const val INVALID_EMAIL = "Please enter a valid email address."
    const val INVALID_DATE = "Please enter a valid date."

    // Empty State Messages
    const val NO_GROUPS =
            "You haven't joined any groups yet. Create or join a group to get started!"
    const val NO_TASKS = "You don't have any tasks yet. Create a task to get started!"
    const val NO_MESSAGES = "No messages yet. Start a conversation!"
    const val NO_EVENTS = "No events scheduled for this date."
    const val NO_SEARCH_RESULTS = "No results found. Try a different search term."

    // Success Messages
    const val SAVE_SUCCESS = "Changes saved successfully."
    const val DELETE_SUCCESS = "Item deleted successfully."
    const val UPDATE_SUCCESS = "Item updated successfully."
    const val CREATE_SUCCESS = "Item created successfully."

    // Retry Messages
    const val RETRY_PROMPT = "Tap to retry"
    const val PULL_TO_REFRESH = "Pull down to refresh"

    /** Helper function to create a custom error message with context. */
    fun withContext(baseMessage: String, context: String): String {
        return "$baseMessage ($context)"
    }

    /** Helper function to create a detailed error message. */
    fun detailed(message: String, details: String?): String {
        return if (details.isNullOrBlank()) {
            message
        } else {
            "$message\nDetails: $details"
        }
    }
}
