package com.example.loginandregistration.utils

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages error states with categorization, user-friendly messages, and retry mechanisms. Provides
 * a consistent error handling experience across fragments.
 */
class ErrorStateManager(private val context: Context) {

    companion object {
        private const val TAG = "ErrorStateManager"
        private const val MAX_AUTO_RETRY_ATTEMPTS = 2
        private const val RETRY_DELAY_MS = 2000L
    }

    /** Error categories for better error handling */
    enum class ErrorCategory {
        PERMISSION,
        NETWORK,
        CONFIGURATION,
        NOT_FOUND,
        TIMEOUT,
        UNKNOWN
    }

    /** Error state with retry information */
    data class ErrorState(
            val category: ErrorCategory,
            val message: String,
            val userMessage: String,
            val isRetryable: Boolean,
            val isTransient: Boolean,
            val exception: Throwable? = null
    )

    /** Categorize an exception into an ErrorState */
    fun categorizeError(exception: Throwable): ErrorState {
        Log.e(TAG, "Categorizing error: ${exception.message}", exception)

        return when (exception) {
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        ErrorState(
                                category = ErrorCategory.PERMISSION,
                                message = "Permission denied: ${exception.message}",
                                userMessage = ErrorMessages.PERMISSION_DENIED,
                                isRetryable = false,
                                isTransient = false,
                                exception = exception
                        )
                    }
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        ErrorState(
                                category = ErrorCategory.NETWORK,
                                message = "Service unavailable: ${exception.message}",
                                userMessage = "Service temporarily unavailable. Please try again.",
                                isRetryable = true,
                                isTransient = true,
                                exception = exception
                        )
                    }
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                        ErrorState(
                                category = ErrorCategory.TIMEOUT,
                                message = "Request timeout: ${exception.message}",
                                userMessage = ErrorMessages.TIMEOUT,
                                isRetryable = true,
                                isTransient = true,
                                exception = exception
                        )
                    }
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                        ErrorState(
                                category = ErrorCategory.CONFIGURATION,
                                message = "Database configuration issue: ${exception.message}",
                                userMessage = ErrorMessages.INDEX_MISSING,
                                isRetryable = false,
                                isTransient = false,
                                exception = exception
                        )
                    }
                    FirebaseFirestoreException.Code.NOT_FOUND -> {
                        ErrorState(
                                category = ErrorCategory.NOT_FOUND,
                                message = "Data not found: ${exception.message}",
                                userMessage = ErrorMessages.NOT_FOUND,
                                isRetryable = false,
                                isTransient = false,
                                exception = exception
                        )
                    }
                    else -> {
                        ErrorState(
                                category = ErrorCategory.UNKNOWN,
                                message =
                                        "Firestore error: ${exception.code} - ${exception.message}",
                                userMessage = ErrorMessages.GENERIC_ERROR,
                                isRetryable = true,
                                isTransient = false,
                                exception = exception
                        )
                    }
                }
            }
            is FirebaseNetworkException -> {
                ErrorState(
                        category = ErrorCategory.NETWORK,
                        message = "Network error: ${exception.message}",
                        userMessage = ErrorMessages.NETWORK_ERROR,
                        isRetryable = true,
                        isTransient = true,
                        exception = exception
                )
            }
            else -> {
                // Check message for common error patterns
                val message = exception.message?.lowercase() ?: ""
                when {
                    message.contains("permission") -> {
                        ErrorState(
                                category = ErrorCategory.PERMISSION,
                                message = "Permission error: ${exception.message}",
                                userMessage = ErrorMessages.PERMISSION_DENIED,
                                isRetryable = false,
                                isTransient = false,
                                exception = exception
                        )
                    }
                    message.contains("network") || message.contains("connection") -> {
                        ErrorState(
                                category = ErrorCategory.NETWORK,
                                message = "Network error: ${exception.message}",
                                userMessage = ErrorMessages.NETWORK_ERROR,
                                isRetryable = true,
                                isTransient = true,
                                exception = exception
                        )
                    }
                    message.contains("timeout") -> {
                        ErrorState(
                                category = ErrorCategory.TIMEOUT,
                                message = "Timeout: ${exception.message}",
                                userMessage = ErrorMessages.TIMEOUT,
                                isRetryable = true,
                                isTransient = true,
                                exception = exception
                        )
                    }
                    else -> {
                        ErrorState(
                                category = ErrorCategory.UNKNOWN,
                                message = "Unknown error: ${exception.message}",
                                userMessage = ErrorMessages.GENERIC_ERROR,
                                isRetryable = true,
                                isTransient = false,
                                exception = exception
                        )
                    }
                }
            }
        }
    }

    /** Show error with appropriate UI feedback and retry option */
    fun showError(errorState: ErrorState, view: View?, onRetry: (() -> Unit)? = null) {
        Log.d(TAG, "Showing error: ${errorState.category} - ${errorState.userMessage}")

        if (view == null) {
            Log.w(TAG, "View is null, cannot show error UI")
            return
        }

        val snackbar =
                Snackbar.make(
                        view,
                        errorState.userMessage,
                        if (errorState.isRetryable) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
                )

        // Add retry action for retryable errors
        if (errorState.isRetryable && onRetry != null) {
            snackbar.setAction("Retry") {
                Log.d(TAG, "User initiated retry for ${errorState.category}")
                onRetry()
            }
            snackbar.setActionTextColor(context.getColor(android.R.color.holo_blue_light))
        }

        snackbar.show()
    }

    /** Handle error with automatic retry for transient errors */
    suspend fun handleErrorWithAutoRetry(
            exception: Throwable,
            view: View?,
            retryAttempt: Int = 0,
            onRetry: suspend () -> Unit
    ): Boolean {
        val errorState = categorizeError(exception)

        // Auto-retry transient errors
        if (errorState.isTransient && retryAttempt < MAX_AUTO_RETRY_ATTEMPTS) {
            Log.d(
                    TAG,
                    "Auto-retrying transient error (attempt ${retryAttempt + 1}/$MAX_AUTO_RETRY_ATTEMPTS)"
            )
            delay(RETRY_DELAY_MS)

            try {
                onRetry()
                return true // Retry successful
            } catch (e: Exception) {
                Log.e(TAG, "Auto-retry failed", e)
                return handleErrorWithAutoRetry(e, view, retryAttempt + 1, onRetry)
            }
        }

        // Show error to user if auto-retry exhausted or not transient
        showError(errorState, view) {
            // Manual retry callback
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    onRetry()
                } catch (e: Exception) {
                    Log.e(TAG, "Manual retry failed", e)
                    handleErrorWithAutoRetry(e, view, 0, onRetry)
                }
            }
        }

        return false // Retry not successful
    }

    /** Get user-friendly error message for an exception */
    fun getUserMessage(exception: Throwable): String {
        return categorizeError(exception).userMessage
    }

    /** Check if an error is retryable */
    fun isRetryable(exception: Throwable): Boolean {
        return categorizeError(exception).isRetryable
    }

    /** Check if an error is transient (should auto-retry) */
    fun isTransient(exception: Throwable): Boolean {
        return categorizeError(exception).isTransient
    }
}
