package com.example.loginandregistration.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error messages for user-facing error displays. These messages provide clear,
 * actionable guidance to users when errors occur.
 */
object ErrorMessages {
        // Core error messages
        const val PERMISSION_DENIED = "Unable to access data. Try logging out and back in."
        const val NETWORK_ERROR = "Connection lost. Check your internet and try again."
        const val NETWORK_UNREACHABLE =
                "Network unreachable. Please check your internet connection."
        const val INDEX_MISSING = "Database configuration issue. Please contact support."
        const val NOT_FOUND = "The requested data could not be found."
        const val ALREADY_EXISTS = "This item already exists."
        const val QUOTA_EXCEEDED = "Service limit reached. Please try again later."
        const val OPERATION_CANCELLED = "Operation was cancelled."
        const val TIMEOUT = "Operation timed out. Please try again."
        const val UNAUTHENTICATED = "You need to be logged in to perform this action."
        const val GENERIC_ERROR = "Something went wrong. Please try again."

        // Authentication error messages
        const val AUTH_INVALID_EMAIL = "Please enter a valid email address."
        const val AUTH_WRONG_PASSWORD = "Incorrect password. Please try again."
        const val AUTH_USER_NOT_FOUND = "No account found with this email."
        const val AUTH_EMAIL_IN_USE = "This email is already registered. Try logging in instead."
        const val AUTH_WEAK_PASSWORD = "Password is too weak. Use at least 6 characters."
        const val AUTH_USER_DISABLED = "This account has been disabled."
        const val AUTH_TOO_MANY_REQUESTS = "Too many attempts. Please try again later."
        const val AUTH_FAILED = "Authentication failed. Please try again."

        // Validation error messages
        const val VALIDATION_TITLE_REQUIRED = "Title is required"
        const val VALIDATION_TITLE_TOO_LONG = "Title must be 200 characters or less"
        const val VALIDATION_DESCRIPTION_TOO_LONG = "Description must be 1000 characters or less"
        const val VALIDATION_ASSIGNEE_REQUIRED =
                "Please select at least one team member for this task"
        const val VALIDATION_ASSIGNEE_LIMIT = "Maximum 50 assignees allowed"
        const val VALIDATION_INVALID_PRIORITY = "Invalid priority value"
        const val VALIDATION_INVALID_CATEGORY = "Invalid category value"
        const val VALIDATION_CREATOR_REQUIRED = "Task creator must be in assignedTo array"

        // Feature-specific error messages
        const val TASK_NOT_FOUND = "Task not found."
        const val CALENDAR_LOAD_FAILED = "Failed to load calendar. Please try again."
        const val RETRY_PROMPT = "Tap to retry"
        const val GROUP_NOT_FOUND = "Group not found."
        const val GROUP_LOAD_FAILED = "Failed to load groups. Please try again."
        const val PULL_TO_REFRESH = "Pull to refresh"
        const val TASK_LOAD_FAILED = "Failed to load tasks. Please try again."
        const val PROFILE_LOAD_FAILED = "Failed to load profile. Please try again."
        const val UNEXPECTED_ERROR = "An unexpected error occurred."
        const val CHAT_SEND_FAILED = "Failed to send message. Please try again."
        const val CHAT_NOT_PARTICIPANT = "You are not a participant in this chat."
        const val DASHBOARD_LOAD_FAILED = "Failed to load dashboard. Please try again."

        /**
         * Get a user-friendly error message from an exception. Handles all error types including
         * PERMISSION_DENIED, network errors (ENETUNREACH), validation errors, and authentication
         * errors.
         *
         * @param exception The exception to convert to a user-friendly message
         * @return A clear, actionable error message for the user
         */
        fun getErrorMessage(exception: Throwable): String {
                return when (exception) {
                        // Network errors - including ENETUNREACH
                        is UnknownHostException,
                        is SocketTimeoutException -> NETWORK_UNREACHABLE
                        is IOException -> {
                                if (exception.message?.contains("ENETUNREACH", ignoreCase = true) ==
                                                true
                                ) {
                                        NETWORK_UNREACHABLE
                                } else {
                                        NETWORK_ERROR
                                }
                        }
                        is FirebaseNetworkException -> NETWORK_ERROR

                        // Firestore errors
                        is FirebaseFirestoreException -> {
                                when (exception.code) {
                                        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                                PERMISSION_DENIED
                                        FirebaseFirestoreException.Code.UNAVAILABLE,
                                        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                                                NETWORK_ERROR
                                        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                                                UNAUTHENTICATED
                                        FirebaseFirestoreException.Code.NOT_FOUND -> NOT_FOUND
                                        FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                                                ALREADY_EXISTS
                                        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                                                QUOTA_EXCEEDED
                                        FirebaseFirestoreException.Code.CANCELLED ->
                                                OPERATION_CANCELLED
                                        FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                                                INDEX_MISSING
                                        else -> GENERIC_ERROR
                                }
                        }

                        // Authentication errors
                        is FirebaseAuthUserCollisionException -> AUTH_EMAIL_IN_USE
                        is FirebaseAuthException -> {
                                when (exception.errorCode) {
                                        "ERROR_INVALID_EMAIL" -> AUTH_INVALID_EMAIL
                                        "ERROR_WRONG_PASSWORD" -> AUTH_WRONG_PASSWORD
                                        "ERROR_USER_NOT_FOUND" -> AUTH_USER_NOT_FOUND
                                        "ERROR_EMAIL_ALREADY_IN_USE" -> AUTH_EMAIL_IN_USE
                                        "ERROR_WEAK_PASSWORD" -> AUTH_WEAK_PASSWORD
                                        "ERROR_USER_DISABLED" -> AUTH_USER_DISABLED
                                        "ERROR_TOO_MANY_REQUESTS" -> AUTH_TOO_MANY_REQUESTS
                                        else -> AUTH_FAILED
                                }
                        }

                        // Validation errors
                        is com.example.loginandregistration.validation.ValidationException -> {
                                // Return the first validation error message
                                exception.errors.firstOrNull() ?: GENERIC_ERROR
                        }

                        // Check message content for specific error patterns
                        else -> {
                                val message = exception.message ?: ""
                                when {
                                        message.contains("PERMISSION_DENIED", ignoreCase = true) ->
                                                PERMISSION_DENIED
                                        message.contains("ENETUNREACH", ignoreCase = true) ->
                                                NETWORK_UNREACHABLE
                                        message.contains("network", ignoreCase = true) ->
                                                NETWORK_ERROR
                                        message.contains("assignee", ignoreCase = true) ->
                                                VALIDATION_ASSIGNEE_REQUIRED
                                        message.contains("participant", ignoreCase = true) ->
                                                CHAT_NOT_PARTICIPANT
                                        message.contains("timeout", ignoreCase = true) -> TIMEOUT
                                        else -> GENERIC_ERROR
                                }
                        }
                }
        }

        /**
         * Determine if a retry button should be shown for the given exception. Network errors and
         * temporary service issues should show retry buttons. Validation errors and permission
         * errors should not.
         *
         * @param exception The exception to check
         * @return true if a retry button should be shown, false otherwise
         */
        fun shouldShowRetry(exception: Throwable): Boolean {
                return when (exception) {
                        // Network errors - always retryable
                        is UnknownHostException,
                        is SocketTimeoutException,
                        is IOException,
                        is FirebaseNetworkException -> true

                        // Firestore errors - some are retryable
                        is FirebaseFirestoreException -> {
                                when (exception.code) {
                                        FirebaseFirestoreException.Code.UNAVAILABLE,
                                        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                                        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED,
                                        FirebaseFirestoreException.Code.ABORTED,
                                        FirebaseFirestoreException.Code.INTERNAL -> true
                                        // Permission and authentication errors are not retryable
                                        FirebaseFirestoreException.Code.PERMISSION_DENIED,
                                        FirebaseFirestoreException.Code.UNAUTHENTICATED,
                                        FirebaseFirestoreException.Code.NOT_FOUND,
                                        FirebaseFirestoreException.Code.ALREADY_EXISTS,
                                        FirebaseFirestoreException.Code.INVALID_ARGUMENT,
                                        FirebaseFirestoreException.Code.FAILED_PRECONDITION -> false
                                        else -> true // Default to retryable for unknown errors
                                }
                        }

                        // Authentication errors - not retryable (user needs to fix input)
                        is FirebaseAuthException -> false

                        // Validation errors - not retryable (user needs to fix input)
                        is com.example.loginandregistration.validation.ValidationException -> false

                        // Check message content for specific patterns
                        else -> {
                                val message = exception.message ?: ""
                                when {
                                        message.contains("PERMISSION_DENIED", ignoreCase = true) ->
                                                false
                                        message.contains("ENETUNREACH", ignoreCase = true) -> true
                                        message.contains("network", ignoreCase = true) -> true
                                        message.contains("UNAVAILABLE", ignoreCase = true) -> true
                                        message.contains("timeout", ignoreCase = true) -> true
                                        message.contains("validation", ignoreCase = true) -> false
                                        message.contains("invalid", ignoreCase = true) -> false
                                        else -> true // Default to retryable for unknown errors
                                }
                        }
                }
        }

        /**
         * Get a specific error message for a validation field.
         *
         * @param field The field name that failed validation
         * @param constraint The constraint that was violated (optional)
         * @return A user-friendly validation error message
         */
        fun getValidationMessage(field: String, constraint: String? = null): String {
                return when (field.lowercase()) {
                        "title" -> {
                                when (constraint) {
                                        "required" -> VALIDATION_TITLE_REQUIRED
                                        "length" -> VALIDATION_TITLE_TOO_LONG
                                        else -> "Invalid title"
                                }
                        }
                        "description" -> {
                                when (constraint) {
                                        "length" -> VALIDATION_DESCRIPTION_TOO_LONG
                                        else -> "Invalid description"
                                }
                        }
                        "assignedto", "assignee", "assignees" -> {
                                when (constraint) {
                                        "required" -> VALIDATION_ASSIGNEE_REQUIRED
                                        "limit" -> VALIDATION_ASSIGNEE_LIMIT
                                        "creator" -> VALIDATION_CREATOR_REQUIRED
                                        else -> VALIDATION_ASSIGNEE_REQUIRED
                                }
                        }
                        "priority" -> VALIDATION_INVALID_PRIORITY
                        "category" -> VALIDATION_INVALID_CATEGORY
                        else -> "Invalid $field"
                }
        }
}
