package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for wrapping Firestore operations with comprehensive error logging. Automatically logs
 * all Firestore errors to ErrorLogger with full context.
 *
 * Requirements: 7.1, 7.5
 */
object FirestoreErrorHandler {
    private const val TAG = "FirestoreErrorHandler"

    /**
     * Wraps a Firestore operation with error logging.
     *
     * @param operationType The type of operation (GET, SET, UPDATE, DELETE, QUERY)
     * @param collectionPath The collection path
     * @param documentId Optional document ID
     * @param block The Firestore operation to execute
     * @return Result.success with the operation result, or Result.failure with the exception
     */
    suspend fun <T> safeFirestoreOperation(
            operationType: String,
            collectionPath: String,
            documentId: String? = null,
            block: suspend () -> T
    ): Result<T> =
            withContext(Dispatchers.IO) {
                try {
                    val result = block()
                    Result.success(result)
                } catch (e: FirebaseFirestoreException) {
                    // Log specific Firestore errors
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            ErrorLogger.logPermissionDenied(
                                    collectionPath = collectionPath,
                                    operationType = operationType,
                                    documentId = documentId,
                                    exception = e
                            )
                        }
                        else -> {
                            ErrorLogger.logFirestoreOperationError(
                                    operationType = operationType,
                                    collectionPath = collectionPath,
                                    documentId = documentId,
                                    exception = e
                            )
                        }
                    }
                    Result.failure(e)
                } catch (e: Exception) {
                    // Log general errors
                    Log.e(
                            TAG,
                            "Unexpected error in Firestore $operationType on $collectionPath: ${e.message}",
                            e
                    )
                    ErrorLogger.logError(
                            errorType = "FIRESTORE_UNEXPECTED",
                            errorMessage = "Unexpected error in $operationType on $collectionPath",
                            exception = e,
                            additionalContext =
                                    mapOf(
                                            "operation_type" to operationType,
                                            "collection_path" to collectionPath,
                                            "document_id" to (documentId ?: "none")
                                    )
                    )
                    Result.failure(e)
                }
            }

    /**
     * Logs a CustomClassMapper warning.
     *
     * @param documentPath The Firestore document path
     * @param className The class name being mapped
     * @param missingFields List of field names that are missing
     * @param warningMessage The warning message
     */
    fun logCustomClassMapperWarning(
            documentPath: String,
            className: String,
            missingFields: List<String>,
            warningMessage: String
    ) {
        ErrorLogger.logCustomClassMapperWarning(
                documentPath = documentPath,
                className = className,
                missingFields = missingFields,
                warningMessage = warningMessage
        )
    }
}
