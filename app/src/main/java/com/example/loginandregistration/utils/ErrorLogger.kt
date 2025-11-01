package com.example.loginandregistration.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Comprehensive error logging utility for tracking all application errors with detailed context.
 * Integrates with Firebase Crashlytics for production monitoring.
 *
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7
 */
object ErrorLogger {
    private const val TAG = "ErrorLogger"
    private val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private var appVersion: String = "unknown"
    private var appContext: Context? = null

    /**
     * Initializes the ErrorLogger with application context. Should be called from
     * Application.onCreate() or MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersion = "${packageInfo.versionName} (${packageInfo.versionCode})"
            Log.d(TAG, "ErrorLogger initialized - App version: $appVersion")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Failed to get app version", e)
        }
    }

    /** Gets the current user ID from Firebase Auth. */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "anonymous"
    }

    /** Gets the current timestamp in a readable format. */
    private fun getTimestamp(): String {
        return dateFormat.format(Date())
    }

    /** Creates a standardized error context string with timestamp, user ID, and app version. */
    private fun getErrorContext(): String {
        return "[${getTimestamp()}] [User: ${getCurrentUserId()}] [Version: $appVersion]"
    }

    // ========== PERMISSION_DENIED ERRORS ==========

    /**
     * Logs Firestore PERMISSION_DENIED errors with detailed context.
     *
     * Requirement 7.1: Add detailed logging for all PERMISSION_DENIED errors with collection path
     * and user ID
     *
     * @param collectionPath The Firestore collection path where permission was denied
     * @param operationType The type of operation (READ, WRITE, CREATE, UPDATE, DELETE)
     * @param documentId Optional document ID if applicable
     * @param exception The FirebaseFirestoreException
     */
    fun logPermissionDenied(
            collectionPath: String,
            operationType: String,
            documentId: String? = null,
            exception: FirebaseFirestoreException
    ) {
        val userId = getCurrentUserId()
        val context = getErrorContext()
        val docPath = if (documentId != null) "$collectionPath/$documentId" else collectionPath

        val errorMessage =
                """
            $context
            PERMISSION_DENIED ERROR:
            - Collection Path: $docPath
            - Operation Type: $operationType
            - User ID: $userId
            - Error Code: ${exception.code}
            - Error Message: ${exception.message}
            - Stack Trace: ${exception.stackTraceToString()}
        """.trimIndent()

        Log.e(TAG, errorMessage)

        // Record to Crashlytics with custom keys
        crashlytics.apply {
            setCustomKey("error_type", "PERMISSION_DENIED")
            setCustomKey("collection_path", docPath)
            setCustomKey("operation_type", operationType)
            setCustomKey("user_id", userId)
            setCustomKey("firestore_error_code", exception.code.toString())
            setCustomKey("timestamp", getTimestamp())
            setCustomKey("app_version", appVersion)
            recordException(PermissionDeniedException(docPath, operationType, userId, exception))
        }
    }

    // ========== FRAME SKIP ERRORS ==========

    /**
     * Logs frame skip events with activity/fragment context.
     *
     * Requirement 7.2: Add logging for frame skip events with activity/fragment name
     *
     * @param framesSkipped Number of frames skipped
     * @param activityOrFragment Name of the current activity or fragment
     * @param operation Optional description of the operation causing the skip
     */
    fun logFrameSkip(framesSkipped: Int, activityOrFragment: String, operation: String? = null) {
        val context = getErrorContext()
        val durationMs = framesSkipped * 16 // Each frame is ~16ms at 60 FPS

        val errorMessage =
                """
            $context
            FRAME SKIP DETECTED:
            - Frames Skipped: $framesSkipped
            - Duration: ${durationMs}ms
            - Location: $activityOrFragment
            - Operation: ${operation ?: "unknown"}
            - Device: ${Build.MANUFACTURER} ${Build.MODEL}
            - Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
        """.trimIndent()

        Log.w(TAG, errorMessage)

        // Record to Crashlytics if significant frame skip (>30 frames = 500ms)
        if (framesSkipped > 30) {
            crashlytics.apply {
                setCustomKey("error_type", "FRAME_SKIP")
                setCustomKey("frames_skipped", framesSkipped)
                setCustomKey("duration_ms", durationMs)
                setCustomKey("location", activityOrFragment)
                setCustomKey("operation", operation ?: "unknown")
                setCustomKey("timestamp", getTimestamp())
                setCustomKey("app_version", appVersion)
                recordException(FrameSkipException(framesSkipped, activityOrFragment, operation))
            }
        }
    }

    // ========== GSON DESERIALIZATION ERRORS ==========

    /**
     * Logs GSON deserialization failures with JSON string and exception details.
     *
     * Requirement 7.3: Add logging for GSON deserialization failures with JSON string and exception
     *
     * @param jsonString The JSON string that failed to deserialize
     * @param targetClass The target class for deserialization
     * @param exception The exception thrown during deserialization
     */
    fun logGsonDeserializationError(
            jsonString: String,
            targetClass: Class<*>,
            exception: Exception
    ) {
        val context = getErrorContext()
        val truncatedJson =
                if (jsonString.length > 500) {
                    "${jsonString.take(500)}... (truncated)"
                } else {
                    jsonString
                }

        val errorMessage =
                """
            $context
            GSON DESERIALIZATION ERROR:
            - Target Class: ${targetClass.simpleName}
            - JSON String: $truncatedJson
            - Exception Type: ${exception.javaClass.simpleName}
            - Exception Message: ${exception.message}
            - Stack Trace: ${exception.stackTraceToString()}
        """.trimIndent()

        Log.e(TAG, errorMessage)

        // Record to Crashlytics
        crashlytics.apply {
            setCustomKey("error_type", "GSON_DESERIALIZATION")
            setCustomKey("target_class", targetClass.simpleName)
            setCustomKey("json_length", jsonString.length)
            setCustomKey("exception_type", exception.javaClass.simpleName)
            setCustomKey("timestamp", getTimestamp())
            setCustomKey("app_version", appVersion)
            recordException(
                    GsonDeserializationException(targetClass.simpleName, truncatedJson, exception)
            )
        }
    }

    // ========== CUSTOM CLASS MAPPER WARNINGS ==========

    /**
     * Logs CustomClassMapper warnings with field names and document path.
     *
     * Requirement 7.4: Add logging for CustomClassMapper warnings with field names and document
     * path
     *
     * @param documentPath The Firestore document path
     * @param className The class name being mapped
     * @param missingFields List of field names that are missing or have issues
     * @param warningMessage The warning message from CustomClassMapper
     */
    fun logCustomClassMapperWarning(
            documentPath: String,
            className: String,
            missingFields: List<String>,
            warningMessage: String
    ) {
        val context = getErrorContext()

        val errorMessage =
                """
            $context
            CUSTOM CLASS MAPPER WARNING:
            - Document Path: $documentPath
            - Class Name: $className
            - Missing Fields: ${missingFields.joinToString(", ")}
            - Warning Message: $warningMessage
        """.trimIndent()

        Log.w(TAG, errorMessage)

        // Record to Crashlytics
        crashlytics.apply {
            setCustomKey("error_type", "CUSTOM_CLASS_MAPPER_WARNING")
            setCustomKey("document_path", documentPath)
            setCustomKey("class_name", className)
            setCustomKey("missing_fields", missingFields.joinToString(", "))
            setCustomKey("timestamp", getTimestamp())
            setCustomKey("app_version", appVersion)
            recordException(CustomClassMapperException(documentPath, className, missingFields))
        }
    }

    // ========== FIRESTORE OPERATION ERRORS ==========

    /**
     * Logs all Firestore operation failures with operation type and error code.
     *
     * Requirement 7.5: Add logging for all Firestore operation failures with operation type and
     * error code
     *
     * @param operationType The type of operation (GET, SET, UPDATE, DELETE, QUERY)
     * @param collectionPath The collection path
     * @param documentId Optional document ID
     * @param exception The FirebaseFirestoreException
     */
    fun logFirestoreOperationError(
            operationType: String,
            collectionPath: String,
            documentId: String? = null,
            exception: FirebaseFirestoreException
    ) {
        val userId = getCurrentUserId()
        val context = getErrorContext()
        val docPath = if (documentId != null) "$collectionPath/$documentId" else collectionPath

        val errorMessage =
                """
            $context
            FIRESTORE OPERATION ERROR:
            - Operation Type: $operationType
            - Collection Path: $docPath
            - User ID: $userId
            - Error Code: ${exception.code}
            - Error Name: ${exception.code.name}
            - Error Message: ${exception.message}
            - Stack Trace: ${exception.stackTraceToString()}
        """.trimIndent()

        Log.e(TAG, errorMessage)

        // Record to Crashlytics
        crashlytics.apply {
            setCustomKey("error_type", "FIRESTORE_OPERATION")
            setCustomKey("operation_type", operationType)
            setCustomKey("collection_path", docPath)
            setCustomKey("user_id", userId)
            setCustomKey("firestore_error_code", exception.code.toString())
            setCustomKey("firestore_error_name", exception.code.name)
            setCustomKey("timestamp", getTimestamp())
            setCustomKey("app_version", appVersion)
            recordException(FirestoreOperationException(operationType, docPath, userId, exception))
        }
    }

    // ========== GENERAL ERROR LOGGING ==========

    /**
     * Logs a general error with full context.
     *
     * Requirement 7.6: Include timestamps, user ID, and app version in all error logs
     *
     * @param errorType Type of error (e.g., "NETWORK", "DATABASE", "UI")
     * @param errorMessage Description of the error
     * @param exception Optional exception
     * @param additionalContext Optional additional context map
     */
    fun logError(
            errorType: String,
            errorMessage: String,
            exception: Exception? = null,
            additionalContext: Map<String, String> = emptyMap()
    ) {
        val context = getErrorContext()

        val contextString =
                if (additionalContext.isNotEmpty()) {
                    additionalContext.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }
                } else {
                    "- No additional context"
                }

        val fullMessage =
                """
            $context
            ERROR TYPE: $errorType
            MESSAGE: $errorMessage
            ADDITIONAL CONTEXT:
            $contextString
            ${if (exception != null) "EXCEPTION: ${exception.message}\nSTACK TRACE: ${exception.stackTraceToString()}" else ""}
        """.trimIndent()

        Log.e(TAG, fullMessage)

        // Record to Crashlytics
        crashlytics.apply {
            setCustomKey("error_type", errorType)
            setCustomKey("error_message", errorMessage)
            setCustomKey("timestamp", getTimestamp())
            setCustomKey("app_version", appVersion)
            setCustomKey("user_id", getCurrentUserId())

            additionalContext.forEach { (key, value) -> setCustomKey(key, value) }

            if (exception != null) {
                recordException(exception)
            } else {
                recordException(GeneralException(errorType, errorMessage))
            }
        }
    }

    /**
     * Logs a critical error that requires immediate attention.
     *
     * Requirement 7.7: Configure Firebase Crashlytics to capture critical errors
     *
     * @param errorType Type of critical error
     * @param errorMessage Description of the error
     * @param exception The exception
     */
    fun logCriticalError(errorType: String, errorMessage: String, exception: Exception) {
        val context = getErrorContext()

        val fullMessage =
                """
            $context
            ⚠️ CRITICAL ERROR ⚠️
            ERROR TYPE: $errorType
            MESSAGE: $errorMessage
            EXCEPTION: ${exception.message}
            STACK TRACE: ${exception.stackTraceToString()}
        """.trimIndent()

        Log.e(TAG, fullMessage)

        // Record to Crashlytics with high priority
        crashlytics.apply {
            setCustomKey("error_type", "CRITICAL_$errorType")
            setCustomKey("error_message", errorMessage)
            setCustomKey("timestamp", getTimestamp())
            setCustomKey("app_version", appVersion)
            setCustomKey("user_id", getCurrentUserId())
            setCustomKey("is_critical", true)
            recordException(CriticalException(errorType, errorMessage, exception))
        }
    }

    // ========== CUSTOM EXCEPTION CLASSES ==========

    /** Custom exception for PERMISSION_DENIED errors. */
    class PermissionDeniedException(
            val collectionPath: String,
            val operationType: String,
            val userId: String,
            cause: Throwable
    ) : Exception("Permission denied for $operationType on $collectionPath by user $userId", cause)

    /** Custom exception for frame skip events. */
    class FrameSkipException(val framesSkipped: Int, val location: String, val operation: String?) :
            Exception(
                    "Skipped $framesSkipped frames in $location${if (operation != null) " during $operation" else ""}"
            )

    /** Custom exception for GSON deserialization errors. */
    class GsonDeserializationException(
            val targetClass: String,
            val jsonString: String,
            cause: Throwable
    ) : Exception("Failed to deserialize JSON to $targetClass: $jsonString", cause)

    /** Custom exception for CustomClassMapper warnings. */
    class CustomClassMapperException(
            val documentPath: String,
            val className: String,
            val missingFields: List<String>
    ) :
            Exception(
                    "CustomClassMapper warning for $className at $documentPath: missing fields ${missingFields.joinToString(", ")}"
            )

    /** Custom exception for Firestore operation errors. */
    class FirestoreOperationException(
            val operationType: String,
            val collectionPath: String,
            val userId: String,
            cause: Throwable
    ) :
            Exception(
                    "Firestore $operationType operation failed on $collectionPath by user $userId",
                    cause
            )

    /** Custom exception for general errors. */
    class GeneralException(val errorType: String, message: String) :
            Exception("$errorType: $message")

    /** Custom exception for critical errors. */
    class CriticalException(val errorType: String, message: String, cause: Throwable) :
            Exception("CRITICAL $errorType: $message", cause)
}
