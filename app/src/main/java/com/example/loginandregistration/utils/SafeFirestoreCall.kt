package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility object for safely executing Firestore operations with comprehensive error handling. Maps
 * FirebaseFirestoreException codes to user-friendly messages and provides logging.
 */
object SafeFirestoreCall {
    private const val TAG = "SafeFirestoreCall"

    /**
     * Executes a Firestore operation with error handling and logging.
     *
     * @param operation The suspend function to execute
     * @param onError Optional callback for handling errors
     * @return Result<T> containing either success data or failure exception
     */
    suspend fun <T> execute(
            operation: suspend () -> T,
            onError: (Exception) -> Unit = {}
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(operation())
            } catch (e: FirebaseFirestoreException) {
                Log.e(TAG, "Firestore error: ${e.code} - ${e.message}", e)
                onError(e)

                val userMessage =
                        when (e.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                ErrorMessages.PERMISSION_DENIED
                            }
                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                ErrorMessages.NETWORK_ERROR
                            }
                            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                                ErrorMessages.INDEX_MISSING
                            }
                            FirebaseFirestoreException.Code.NOT_FOUND -> {
                                ErrorMessages.NOT_FOUND
                            }
                            FirebaseFirestoreException.Code.ALREADY_EXISTS -> {
                                ErrorMessages.ALREADY_EXISTS
                            }
                            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                                ErrorMessages.QUOTA_EXCEEDED
                            }
                            FirebaseFirestoreException.Code.CANCELLED -> {
                                ErrorMessages.OPERATION_CANCELLED
                            }
                            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                                ErrorMessages.TIMEOUT
                            }
                            FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                                ErrorMessages.UNAUTHENTICATED
                            }
                            else -> {
                                "${ErrorMessages.GENERIC_ERROR} (${e.code})"
                            }
                        }

                Result.failure(Exception(userMessage, e))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during Firestore operation", e)
                onError(e)
                Result.failure(Exception(ErrorMessages.GENERIC_ERROR, e))
            }
        }
    }

    /**
     * Executes a Firestore operation synchronously (for non-suspend contexts). Note: This should be
     * called from a background thread.
     *
     * @param operation The function to execute
     * @param onError Optional callback for handling errors
     * @return Result<T> containing either success data or failure exception
     */
    fun <T> executeSync(operation: () -> T, onError: (Exception) -> Unit = {}): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error (sync): ${e.code} - ${e.message}", e)
            onError(e)

            val userMessage =
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            ErrorMessages.PERMISSION_DENIED
                        }
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            ErrorMessages.NETWORK_ERROR
                        }
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                            ErrorMessages.INDEX_MISSING
                        }
                        else -> {
                            "${ErrorMessages.GENERIC_ERROR} (${e.code})"
                        }
                    }

            Result.failure(Exception(userMessage, e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Firestore operation (sync)", e)
            onError(e)
            Result.failure(Exception(ErrorMessages.GENERIC_ERROR, e))
        }
    }

    /**
     * Logs detailed information about a Firestore exception for debugging.
     *
     * @param tag The log tag to use
     * @param operation Description of the operation that failed
     * @param exception The exception that occurred
     */
    fun logFirestoreError(tag: String, operation: String, exception: Exception) {
        when (exception) {
            is FirebaseFirestoreException -> {
                Log.e(
                        tag,
                        """
                    Firestore Error Details:
                    Operation: $operation
                    Code: ${exception.code}
                    Message: ${exception.message}
                    Cause: ${exception.cause?.message}
                """.trimIndent(),
                        exception
                )
            }
            else -> {
                Log.e(tag, "Error during $operation: ${exception.message}", exception)
            }
        }
    }
}
