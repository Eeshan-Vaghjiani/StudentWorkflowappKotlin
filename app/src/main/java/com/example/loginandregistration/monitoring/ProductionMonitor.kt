package com.example.loginandregistration.monitoring

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

/**
 * Production monitoring utility for tracking user profile initialization, chat functionality, and
 * sign-in success rates.
 *
 * Requirements: 1.5, 4.3, 4.5
 */
object ProductionMonitor {
    private const val TAG = "ProductionMonitor"

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }
    private val crashlytics: FirebaseCrashlytics by lazy { Firebase.crashlytics }

    // Event names
    private const val EVENT_PROFILE_CREATION_ATTEMPT = "profile_creation_attempt"
    private const val EVENT_PROFILE_CREATION_SUCCESS = "profile_creation_success"
    private const val EVENT_PROFILE_CREATION_FAILURE = "profile_creation_failure"
    private const val EVENT_PROFILE_UPDATE_SUCCESS = "profile_update_success"
    private const val EVENT_PROFILE_UPDATE_FAILURE = "profile_update_failure"

    private const val EVENT_SIGN_IN_ATTEMPT = "sign_in_attempt"
    private const val EVENT_SIGN_IN_SUCCESS = "sign_in_success"
    private const val EVENT_SIGN_IN_FAILURE = "sign_in_failure"

    private const val EVENT_CHAT_CREATION_ATTEMPT = "chat_creation_attempt"
    private const val EVENT_CHAT_CREATION_SUCCESS = "chat_creation_success"
    private const val EVENT_CHAT_CREATION_FAILURE = "chat_creation_failure"
    private const val EVENT_GROUP_CHAT_VALIDATION_FAILURE = "group_chat_validation_failure"

    private const val EVENT_MESSAGE_SEND_ATTEMPT = "message_send_attempt"
    private const val EVENT_MESSAGE_SEND_SUCCESS = "message_send_success"
    private const val EVENT_MESSAGE_SEND_FAILURE = "message_send_failure"

    private const val EVENT_USER_NOT_FOUND_ERROR = "user_not_found_error"

    // Parameter names
    private const val PARAM_ERROR_TYPE = "error_type"
    private const val PARAM_ERROR_MESSAGE = "error_message"
    private const val PARAM_USER_ID = "user_id"
    private const val PARAM_AUTH_METHOD = "auth_method"
    private const val PARAM_CHAT_TYPE = "chat_type"
    private const val PARAM_IS_NEW_USER = "is_new_user"

    /** Tracks profile creation attempt */
    fun logProfileCreationAttempt(userId: String, isNewUser: Boolean) {
        Log.d(TAG, "Profile creation attempt - userId: $userId, isNewUser: $isNewUser")

        analytics.logEvent(EVENT_PROFILE_CREATION_ATTEMPT) {
            param(PARAM_USER_ID, userId)
            param(PARAM_IS_NEW_USER, if (isNewUser) "true" else "false")
        }

        crashlytics.setCustomKey("last_profile_creation_attempt", System.currentTimeMillis())
        crashlytics.setUserId(userId)
    }

    /** Tracks successful profile creation */
    fun logProfileCreationSuccess(userId: String, isNewUser: Boolean) {
        Log.d(TAG, "Profile creation success - userId: $userId, isNewUser: $isNewUser")

        analytics.logEvent(EVENT_PROFILE_CREATION_SUCCESS) {
            param(PARAM_USER_ID, userId)
            param(PARAM_IS_NEW_USER, if (isNewUser) "true" else "false")
        }

        crashlytics.setCustomKey("last_profile_creation_success", System.currentTimeMillis())
    }

    /** Tracks profile creation failure with error details */
    fun logProfileCreationFailure(userId: String, errorType: String, errorMessage: String) {
        Log.e(TAG, "Profile creation failure - userId: $userId, error: $errorType - $errorMessage")

        analytics.logEvent(EVENT_PROFILE_CREATION_FAILURE) {
            param(PARAM_USER_ID, userId)
            param(PARAM_ERROR_TYPE, errorType)
            param(PARAM_ERROR_MESSAGE, errorMessage.take(100)) // Limit to 100 chars
        }

        // Record non-fatal exception for Crashlytics
        val exception = ProfileCreationException(errorType, errorMessage)
        crashlytics.recordException(exception)
        crashlytics.setCustomKey("last_profile_creation_failure", System.currentTimeMillis())
    }

    /** Tracks profile update success */
    fun logProfileUpdateSuccess(userId: String) {
        Log.d(TAG, "Profile update success - userId: $userId")

        analytics.logEvent(EVENT_PROFILE_UPDATE_SUCCESS) { param(PARAM_USER_ID, userId) }
    }

    /** Tracks profile update failure */
    fun logProfileUpdateFailure(userId: String, errorType: String, errorMessage: String) {
        Log.e(TAG, "Profile update failure - userId: $userId, error: $errorType - $errorMessage")

        analytics.logEvent(EVENT_PROFILE_UPDATE_FAILURE) {
            param(PARAM_USER_ID, userId)
            param(PARAM_ERROR_TYPE, errorType)
            param(PARAM_ERROR_MESSAGE, errorMessage.take(100))
        }

        val exception = ProfileUpdateException(errorType, errorMessage)
        crashlytics.recordException(exception)
    }

    /** Tracks sign-in attempt */
    fun logSignInAttempt(authMethod: String) {
        Log.d(TAG, "Sign-in attempt - method: $authMethod")

        analytics.logEvent(EVENT_SIGN_IN_ATTEMPT) { param(PARAM_AUTH_METHOD, authMethod) }
    }

    /** Tracks successful sign-in */
    fun logSignInSuccess(userId: String, authMethod: String) {
        Log.d(TAG, "Sign-in success - userId: $userId, method: $authMethod")

        analytics.logEvent(EVENT_SIGN_IN_SUCCESS) {
            param(PARAM_USER_ID, userId)
            param(PARAM_AUTH_METHOD, authMethod)
        }

        crashlytics.setUserId(userId)
        crashlytics.setCustomKey("last_sign_in_method", authMethod)
        crashlytics.setCustomKey("last_sign_in_success", System.currentTimeMillis())
    }

    /** Tracks sign-in failure */
    fun logSignInFailure(authMethod: String, errorType: String, errorMessage: String) {
        Log.e(TAG, "Sign-in failure - method: $authMethod, error: $errorType - $errorMessage")

        analytics.logEvent(EVENT_SIGN_IN_FAILURE) {
            param(PARAM_AUTH_METHOD, authMethod)
            param(PARAM_ERROR_TYPE, errorType)
            param(PARAM_ERROR_MESSAGE, errorMessage.take(100))
        }

        val exception = SignInException(authMethod, errorType, errorMessage)
        crashlytics.recordException(exception)
    }

    /** Tracks chat creation attempt */
    fun logChatCreationAttempt(chatType: String, participantCount: Int) {
        Log.d(TAG, "Chat creation attempt - type: $chatType, participants: $participantCount")

        analytics.logEvent(EVENT_CHAT_CREATION_ATTEMPT) {
            param(PARAM_CHAT_TYPE, chatType)
            param("participant_count", participantCount.toLong())
        }
    }

    /** Tracks successful chat creation */
    fun logChatCreationSuccess(chatType: String, chatId: String) {
        Log.d(TAG, "Chat creation success - type: $chatType, chatId: $chatId")

        analytics.logEvent(EVENT_CHAT_CREATION_SUCCESS) {
            param(PARAM_CHAT_TYPE, chatType)
            param("chat_id", chatId)
        }
    }

    /** Tracks chat creation failure */
    fun logChatCreationFailure(chatType: String, errorType: String, errorMessage: String) {
        Log.e(TAG, "Chat creation failure - type: $chatType, error: $errorType - $errorMessage")

        analytics.logEvent(EVENT_CHAT_CREATION_FAILURE) {
            param(PARAM_CHAT_TYPE, chatType)
            param(PARAM_ERROR_TYPE, errorType)
            param(PARAM_ERROR_MESSAGE, errorMessage.take(100))
        }

        val exception = ChatCreationException(chatType, errorType, errorMessage)
        crashlytics.recordException(exception)
    }

    /** Tracks group chat validation failure */
    fun logGroupChatValidationFailure(
            groupId: String,
            validationType: String,
            errorMessage: String
    ) {
        Log.e(
                TAG,
                "Group chat validation failure - groupId: $groupId, type: $validationType, error: $errorMessage"
        )

        analytics.logEvent(EVENT_GROUP_CHAT_VALIDATION_FAILURE) {
            param("group_id", groupId)
            param("validation_type", validationType)
            param(PARAM_ERROR_MESSAGE, errorMessage.take(100))
        }

        val exception = GroupChatValidationException(groupId, validationType, errorMessage)
        crashlytics.recordException(exception)
    }

    /** Tracks message send attempt */
    fun logMessageSendAttempt(chatId: String, messageType: String) {
        Log.d(TAG, "Message send attempt - chatId: $chatId, type: $messageType")

        analytics.logEvent(EVENT_MESSAGE_SEND_ATTEMPT) {
            param("chat_id", chatId)
            param("message_type", messageType)
        }
    }

    /** Tracks successful message send */
    fun logMessageSendSuccess(chatId: String, messageType: String) {
        Log.d(TAG, "Message send success - chatId: $chatId, type: $messageType")

        analytics.logEvent(EVENT_MESSAGE_SEND_SUCCESS) {
            param("chat_id", chatId)
            param("message_type", messageType)
        }
    }

    /** Tracks message send failure */
    fun logMessageSendFailure(
            chatId: String,
            messageType: String,
            errorType: String,
            errorMessage: String
    ) {
        Log.e(TAG, "Message send failure - chatId: $chatId, error: $errorType - $errorMessage")

        analytics.logEvent(EVENT_MESSAGE_SEND_FAILURE) {
            param("chat_id", chatId)
            param("message_type", messageType)
            param(PARAM_ERROR_TYPE, errorType)
            param(PARAM_ERROR_MESSAGE, errorMessage.take(100))
        }

        val exception = MessageSendException(chatId, messageType, errorType, errorMessage)
        crashlytics.recordException(exception)
    }

    /**
     * Tracks "User not found" errors - this should be ZERO in production after profile
     * initialization fix
     */
    fun logUserNotFoundError(userId: String, context: String) {
        Log.e(TAG, "USER NOT FOUND ERROR - userId: $userId, context: $context")

        analytics.logEvent(EVENT_USER_NOT_FOUND_ERROR) {
            param(PARAM_USER_ID, userId)
            param("context", context)
        }

        // This is a critical error that should not happen
        val exception = UserNotFoundException(userId, context)
        crashlytics.recordException(exception)
        crashlytics.setCustomKey("last_user_not_found_error", System.currentTimeMillis())
    }

    /** Sets user properties for analytics segmentation */
    fun setUserProperties(userId: String, hasProfile: Boolean, profileCreatedAt: Long?) {
        crashlytics.setUserId(userId)

        analytics.setUserProperty("has_profile", if (hasProfile) "true" else "false")

        profileCreatedAt?.let {
            val daysSinceCreation = (System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)
            analytics.setUserProperty("days_since_profile_creation", daysSinceCreation.toString())
        }
    }

    // Custom exception classes for better error tracking
    class ProfileCreationException(val errorType: String, message: String) :
            Exception("Profile creation failed: $errorType - $message")
    class ProfileUpdateException(val errorType: String, message: String) :
            Exception("Profile update failed: $errorType - $message")
    class SignInException(val authMethod: String, val errorType: String, message: String) :
            Exception("Sign-in failed ($authMethod): $errorType - $message")
    class ChatCreationException(val chatType: String, val errorType: String, message: String) :
            Exception("Chat creation failed ($chatType): $errorType - $message")
    class MessageSendException(
            val chatId: String,
            val messageType: String,
            val errorType: String,
            message: String
    ) : Exception("Message send failed ($chatId, $messageType): $errorType - $message")
    class UserNotFoundException(val userId: String, val context: String) :
            Exception("User not found: $userId in context: $context")
    class GroupChatValidationException(
            val groupId: String,
            val validationType: String,
            message: String
    ) : Exception("Group chat validation failed ($groupId, $validationType): $message")
}
