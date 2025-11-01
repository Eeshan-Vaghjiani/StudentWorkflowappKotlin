package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Safe wrapper for Firestore operations that handles permission errors gracefully.
 *
 * This function wraps Firestore calls and catches FirebaseFirestoreException errors, specifically
 * handling PERMISSION_DENIED errors by returning empty results instead of crashing the app.
 *
 * Uses ErrorHandler for categorization and user-friendly error messages.
 *
 * @param operationName Name of the operation for logging/monitoring
 * @param operation The Firestore operation to execute
 * @return Result containing the operation result or an error
 */
suspend fun <T> safeFirestoreCall(
        operationName: String = "unknown",
        operation: suspend () -> T
): Result<T> {
    // Record query attempt
    ProductionMetricsMonitor.recordQueryAttempt(operationName)

    return try {
        val result = operation()

        // Record success
        ProductionMetricsMonitor.recordQuerySuccess(operationName)

        Result.success(result)
    } catch (e: Exception) {
        // Record failure
        ProductionMetricsMonitor.recordQueryFailure(operationName, e)

        // Categorize error using ErrorHandler
        val appError = ErrorHandler.categorizeError(e)
        val userFriendlyError = ErrorHandler.getUserFriendlyError(appError)

        // Log detailed error for debugging
        Log.e("SafeFirestoreCall", "Error in $operationName: ${userFriendlyError.message}", e)

        // Record specific error types
        if (e is FirebaseFirestoreException &&
                        e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        ) {
            ProductionMetricsMonitor.recordPermissionError(operationName, e)
        }

        // Return failure with user-friendly message wrapped in exception
        Result.failure(Exception(userFriendlyError.message, e))
    }
}
