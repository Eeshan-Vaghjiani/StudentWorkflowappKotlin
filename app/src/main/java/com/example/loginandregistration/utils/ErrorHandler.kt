package com.example.loginandregistration.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Centralized error handling utility for the app. Provides user-friendly error messages and
 * appropriate UI feedback.
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    /** Error types for categorization */
    sealed class AppError {
        data class NetworkError(val message: String, val exception: Throwable? = null) : AppError()
        data class AuthError(val message: String, val exception: Throwable? = null) : AppError()
        data class PermissionError(val message: String, val permission: String? = null) :
                AppError()
        data class StorageError(val message: String, val exception: Throwable? = null) : AppError()
        data class ValidationError(val message: String) : AppError()
        data class FirestoreError(val message: String, val exception: Throwable? = null) :
                AppError()
        data class UnknownError(val message: String, val exception: Throwable? = null) : AppError()
    }

    /** Handle any exception and show appropriate UI feedback */
    fun handleError(
            context: Context,
            exception: Throwable,
            view: View? = null,
            onRetry: (() -> Unit)? = null
    ) {
        val error = categorizeError(exception)
        logError(error)
        showErrorFeedback(context, error, view, onRetry)
    }

    /** Categorize exception into AppError types */
    private fun categorizeError(exception: Throwable): AppError {
        return when (exception) {
            // Network errors
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException,
            is FirebaseNetworkException -> {
                AppError.NetworkError(
                        "No internet connection. Please check your network and try again.",
                        exception
                )
            }

            // Firestore errors
            is FirebaseFirestoreException -> {
                when (exception.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        AppError.FirestoreError(
                                "You don't have permission to access this data.",
                                exception
                        )
                    }
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        AppError.NetworkError(
                                "Service temporarily unavailable. Please try again.",
                                exception
                        )
                    }
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                        AppError.AuthError("Please sign in to continue.", exception)
                    }
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                        AppError.FirestoreError(
                                "Database is being configured. This may take a few minutes. Please try again shortly.",
                                exception
                        )
                    }
                    else -> {
                        AppError.FirestoreError(
                                "Something went wrong. Please try again.",
                                exception
                        )
                    }
                }
            }

            // Auth errors
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> {
                        AppError.ValidationError("Please enter a valid email address.")
                    }
                    "ERROR_WRONG_PASSWORD" -> {
                        AppError.AuthError("Incorrect password. Please try again.", exception)
                    }
                    "ERROR_USER_NOT_FOUND" -> {
                        AppError.AuthError("No account found with this email.", exception)
                    }
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        AppError.AuthError("This email is already registered.", exception)
                    }
                    "ERROR_WEAK_PASSWORD" -> {
                        AppError.ValidationError("Password is too weak. Use at least 6 characters.")
                    }
                    "ERROR_USER_DISABLED" -> {
                        AppError.AuthError("This account has been disabled.", exception)
                    }
                    "ERROR_TOO_MANY_REQUESTS" -> {
                        AppError.AuthError("Too many attempts. Please try again later.", exception)
                    }
                    else -> {
                        AppError.AuthError("Authentication failed. Please try again.", exception)
                    }
                }
            }

            // Storage errors
            is StorageException -> {
                when (exception.errorCode) {
                    StorageException.ERROR_OBJECT_NOT_FOUND -> {
                        AppError.StorageError("File not found.", exception)
                    }
                    StorageException.ERROR_QUOTA_EXCEEDED -> {
                        AppError.StorageError("Storage quota exceeded.", exception)
                    }
                    StorageException.ERROR_NOT_AUTHENTICATED -> {
                        AppError.AuthError("Please sign in to upload files.", exception)
                    }
                    StorageException.ERROR_NOT_AUTHORIZED -> {
                        AppError.StorageError(
                                "You don't have permission to access this file.",
                                exception
                        )
                    }
                    StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> {
                        AppError.NetworkError(
                                "Upload failed. Please check your connection.",
                                exception
                        )
                    }
                    else -> {
                        AppError.StorageError("File operation failed. Please try again.", exception)
                    }
                }
            }

            // Security exceptions (permissions)
            is SecurityException -> {
                AppError.PermissionError("Permission required to perform this action.", null)
            }

            // Unknown errors
            else -> {
                AppError.UnknownError("Something went wrong. Please try again.", exception)
            }
        }
    }

    /** Show appropriate UI feedback based on error type */
    private fun showErrorFeedback(
            context: Context,
            error: AppError,
            view: View?,
            onRetry: (() -> Unit)?
    ) {
        when (error) {
            is AppError.NetworkError -> {
                showNetworkErrorSnackbar(context, view, error.message, onRetry)
            }
            is AppError.PermissionError -> {
                showPermissionErrorDialog(context, error.message, error.permission)
            }
            is AppError.ValidationError -> {
                showValidationErrorToast(context, error.message)
            }
            is AppError.AuthError,
            is AppError.FirestoreError,
            is AppError.StorageError,
            is AppError.UnknownError -> {
                if (view != null && onRetry != null) {
                    showErrorSnackbar(context, view, error.getMessage(), onRetry)
                } else {
                    showErrorToast(context, error.getMessage())
                }
            }
        }
    }

    /** Show Snackbar for network errors with retry button */
    fun showNetworkErrorSnackbar(
            context: Context,
            view: View?,
            message: String = "No internet connection",
            onRetry: (() -> Unit)? = null
    ) {
        if (view == null) {
            showErrorToast(context, message)
            return
        }

        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)

        if (onRetry != null) {
            snackbar.setAction("RETRY") { onRetry() }
        }

        snackbar.setActionTextColor(context.getColor(android.R.color.holo_blue_light))
        snackbar.show()
    }

    /** Show Snackbar for general errors with retry button */
    fun showErrorSnackbar(
            context: Context,
            view: View,
            message: String,
            onRetry: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)

        if (onRetry != null) {
            snackbar.setAction("RETRY") { onRetry() }
            snackbar.setActionTextColor(context.getColor(android.R.color.holo_blue_light))
        }

        snackbar.show()
    }

    /** Show dialog for permission errors with settings button */
    fun showPermissionErrorDialog(context: Context, message: String, permission: String? = null) {
        AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Open Settings") { dialog, _ ->
                    openAppSettings(context)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .show()
    }

    /** Show toast for validation errors */
    fun showValidationErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /** Show toast for general errors */
    fun showErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /** Show success message */
    fun showSuccessMessage(context: Context, view: View?, message: String) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /** Show info message */
    fun showInfoMessage(context: Context, view: View?, message: String) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    /** Log error to console and Crashlytics for debugging */
    private fun logError(error: AppError) {
        val crashlytics = FirebaseCrashlytics.getInstance()

        when (error) {
            is AppError.NetworkError -> {
                Log.e(TAG, "Network Error: ${error.message}", error.exception)
                error.exception?.let {
                    crashlytics.recordException(it)
                    crashlytics.setCustomKey("error_type", "network")
                    crashlytics.setCustomKey("error_message", error.message)
                }
            }
            is AppError.AuthError -> {
                Log.e(TAG, "Auth Error: ${error.message}", error.exception)
                error.exception?.let {
                    crashlytics.recordException(it)
                    crashlytics.setCustomKey("error_type", "auth")
                    crashlytics.setCustomKey("error_message", error.message)
                }
            }
            is AppError.PermissionError -> {
                Log.e(TAG, "Permission Error: ${error.message} (${error.permission})")
                crashlytics.log("Permission Error: ${error.message} (${error.permission})")
                crashlytics.setCustomKey("error_type", "permission")
                crashlytics.setCustomKey("permission", error.permission ?: "unknown")
            }
            is AppError.StorageError -> {
                Log.e(TAG, "Storage Error: ${error.message}", error.exception)
                error.exception?.let {
                    crashlytics.recordException(it)
                    crashlytics.setCustomKey("error_type", "storage")
                    crashlytics.setCustomKey("error_message", error.message)
                }
            }
            is AppError.ValidationError -> {
                Log.w(TAG, "Validation Error: ${error.message}")
                // Don't log validation errors to Crashlytics (user input errors)
            }
            is AppError.FirestoreError -> {
                Log.e(TAG, "Firestore Error: ${error.message}", error.exception)
                error.exception?.let {
                    crashlytics.recordException(it)
                    crashlytics.setCustomKey("error_type", "firestore")
                    crashlytics.setCustomKey("error_message", error.message)
                }
            }
            is AppError.UnknownError -> {
                Log.e(TAG, "Unknown Error: ${error.message}", error.exception)
                error.exception?.let {
                    crashlytics.recordException(it)
                    crashlytics.setCustomKey("error_type", "unknown")
                    crashlytics.setCustomKey("error_message", error.message)
                }
            }
        }
    }

    /** Open app settings */
    private fun openAppSettings(context: Context) {
        val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
        context.startActivity(intent)
    }

    /** Get user-friendly message from AppError */
    private fun AppError.getMessage(): String {
        return when (this) {
            is AppError.NetworkError -> message
            is AppError.AuthError -> message
            is AppError.PermissionError -> message
            is AppError.StorageError -> message
            is AppError.ValidationError -> message
            is AppError.FirestoreError -> message
            is AppError.UnknownError -> message
        }
    }

    /** Handle specific error scenarios */

    // Network connectivity error
    fun handleNetworkError(context: Context, view: View?, onRetry: (() -> Unit)? = null) {
        showNetworkErrorSnackbar(
                context,
                view,
                "No internet connection. Please check your network and try again.",
                onRetry
        )
    }

    // File upload error
    fun handleUploadError(context: Context, view: View?, onRetry: (() -> Unit)? = null) {
        showErrorSnackbar(context, view ?: return, "Upload failed. Please try again.", onRetry)
    }

    // File download error
    fun handleDownloadError(context: Context, view: View?, onRetry: (() -> Unit)? = null) {
        showErrorSnackbar(context, view ?: return, "Download failed. Please try again.", onRetry)
    }

    // Authentication error
    fun handleAuthError(context: Context, message: String = "Please sign in to continue.") {
        showErrorToast(context, message)
    }

    // Permission denied error
    fun handlePermissionDenied(context: Context, permissionName: String, rationale: String) {
        showPermissionErrorDialog(
                context,
                "$rationale\n\nPlease grant $permissionName permission in settings.",
                permissionName
        )
    }

    // Validation error
    fun handleValidationError(context: Context, message: String) {
        showValidationErrorToast(context, message)
    }

    // Generic error with custom message
    fun handleGenericError(
            context: Context,
            view: View?,
            message: String,
            onRetry: (() -> Unit)? = null
    ) {
        if (view != null && onRetry != null) {
            showErrorSnackbar(context, view, message, onRetry)
        } else {
            showErrorToast(context, message)
        }
    }
}

/**
 * Extension function for safe Firestore operations with automatic error handling. Wraps Firestore
 * calls and converts exceptions to Result types.
 *
 * Usage:
 * ```
 * val result = safeFirestoreCall {
 *     firestore.collection("users").document(userId).get().await()
 * }
 * ```
 */
suspend fun <T> safeFirestoreCall(block: suspend () -> T): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: FirebaseFirestoreException) {
            // Enhanced logging for permission denied errors
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.e(
                            "SafeFirestoreCall",
                            "PERMISSION_DENIED: Missing or insufficient permissions. " +
                                    "User may not have access to the requested data. " +
                                    "Error: ${e.message}",
                            e
                    )
                    // Log additional context for debugging
                    Log.e(
                            "SafeFirestoreCall",
                            "Permission denied details - Code: ${e.code}, " +
                                    "Message: ${e.message}"
                    )
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    Log.e("SafeFirestoreCall", "Firestore service unavailable: ${e.message}", e)
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Log.e("SafeFirestoreCall", "User not authenticated: ${e.message}", e)
                }
                else -> {
                    Log.e("SafeFirestoreCall", "Firestore error: ${e.code} - ${e.message}", e)
                }
            }
            Result.failure(e)
        } catch (e: FirebaseNetworkException) {
            Log.e("SafeFirestoreCall", "Network error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("SafeFirestoreCall", "Unknown error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
