package com.example.loginandregistration.utils

/**
 * Centralized error messages for user-facing error displays. These messages provide clear,
 * actionable guidance to users when errors occur.
 */
object ErrorMessages {
        const val PERMISSION_DENIED =
                "You don't have permission to access this data. Try logging out and back in."
        const val NETWORK_ERROR =
                "Network connection issue. Please check your internet connection and try again."
        const val INDEX_MISSING = "Database configuration issue. Please contact support."
        const val NOT_FOUND = "The requested data could not be found."
        const val ALREADY_EXISTS = "This item already exists."
        const val QUOTA_EXCEEDED = "Service limit reached. Please try again later."
        const val OPERATION_CANCELLED = "Operation was cancelled."
        const val TIMEOUT = "Operation timed out. Please try again."
        const val UNAUTHENTICATED = "You need to be logged in to perform this action."
        const val GENERIC_ERROR = "An unexpected error occurred. Please try again."

        // Additional error messages for specific features
        const val TASK_NOT_FOUND = "Task not found."
        const val CALENDAR_LOAD_FAILED = "Failed to load calendar. Please try again."
        const val RETRY_PROMPT = "Tap to retry"
        const val GROUP_NOT_FOUND = "Group not found."
        const val GROUP_LOAD_FAILED = "Failed to load groups. Please try again."
        const val PULL_TO_REFRESH = "Pull to refresh"
        const val TASK_LOAD_FAILED = "Failed to load tasks. Please try again."
        const val PROFILE_LOAD_FAILED = "Failed to load profile. Please try again."
        const val UNEXPECTED_ERROR = "An unexpected error occurred."
}
