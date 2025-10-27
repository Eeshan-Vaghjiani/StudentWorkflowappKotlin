package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Safe wrapper for Firestore operations that handles permission errors gracefully.
 *
 * This function wraps Firestore calls and catches FirebaseFirestoreException errors, specifically
 * handling PERMISSION_DENIED errors by returning empty results instead of crashing the app.
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
    } catch (e: FirebaseFirestoreException) {
        // Record failure
        ProductionMetricsMonitor.recordQueryFailure(operationName, e)

        when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                Log.e("SafeFirestoreCall", "Permission denied in $operationName: ${e.message}", e)
                ProductionMetricsMonitor.recordPermissionError(operationName, e)
                Result.failure(e)
            }
            else -> {
                Log.e("SafeFirestoreCall", "Firestore error in $operationName: ${e.message}", e)
                Result.failure(e)
            }
        }
    } catch (e: Exception) {
        Log.e("SafeFirestoreCall", "Unexpected error in $operationName: ${e.message}", e)
        ProductionMetricsMonitor.recordQueryFailure(operationName, e)
        Result.failure(e)
    }
}
