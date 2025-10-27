package com.example.loginandregistration.utils

import android.content.Context
import android.webkit.MimeTypeMap
import com.example.loginandregistration.R
import java.io.File

/**
 * Validator for Firebase Security Rules constraints Provides client-side validation to prevent rule
 * violations before attempting operations
 */
object FirebaseRulesValidator {

    // Constants matching Firebase rules
    const val MAX_MESSAGE_LENGTH = 10000
    const val MAX_DIRECT_CHAT_PARTICIPANTS = 2
    const val MIN_GROUP_CHAT_PARTICIPANTS = 2
    const val MAX_GROUP_CHAT_PARTICIPANTS = 100
    const val MIN_GROUP_MEMBERS = 1
    const val MAX_GROUP_MEMBERS = 100
    const val MIN_TASK_ASSIGNMENTS = 1
    const val MAX_TASK_ASSIGNMENTS = 50
    const val MAX_PROFILE_PICTURE_SIZE_MB = 5
    const val MAX_DOCUMENT_SIZE_MB = 10

    private const val FIREBASE_STORAGE_DOMAIN = "https://firebasestorage.googleapis.com/"

    // Supported file types
    private val SUPPORTED_IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")
    private val SUPPORTED_DOCUMENT_TYPES =
            setOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain"
            )

    /** Validation result with error message */
    data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null) {
        companion object {
            fun success() = ValidationResult(true)
            fun error(message: String) = ValidationResult(false, message)
        }
    }

    /** Validate chat participant count based on chat type */
    fun validateChatParticipants(
            context: Context,
            participantCount: Int,
            chatType: String
    ): ValidationResult {
        return when (chatType.uppercase()) {
            "DIRECT" -> {
                if (participantCount != MAX_DIRECT_CHAT_PARTICIPANTS) {
                    ValidationResult.error(
                            context.getString(R.string.error_invalid_participant_count_direct)
                    )
                } else {
                    ValidationResult.success()
                }
            }
            "GROUP" -> {
                when {
                    participantCount < MIN_GROUP_CHAT_PARTICIPANTS -> {
                        ValidationResult.error(
                                context.getString(R.string.error_invalid_participant_count_group)
                        )
                    }
                    participantCount > MAX_GROUP_CHAT_PARTICIPANTS -> {
                        ValidationResult.error(
                                context.getString(R.string.error_max_participants_reached)
                        )
                    }
                    else -> ValidationResult.success()
                }
            }
            else -> ValidationResult.error("Invalid chat type")
        }
    }

    /** Validate message length */
    fun validateMessageLength(context: Context, message: String): ValidationResult {
        return if (message.length > MAX_MESSAGE_LENGTH) {
            ValidationResult.error(context.getString(R.string.error_message_too_long))
        } else {
            ValidationResult.success()
        }
    }

    /** Validate message content (must have text, image, or document) */
    fun validateMessageContent(
            context: Context,
            text: String?,
            imageUrl: String?,
            documentUrl: String?
    ): ValidationResult {
        val hasContent =
                !text.isNullOrBlank() || !imageUrl.isNullOrBlank() || !documentUrl.isNullOrBlank()

        return if (hasContent) {
            ValidationResult.success()
        } else {
            ValidationResult.error(context.getString(R.string.error_message_empty))
        }
    }

    /** Validate Firebase Storage URL */
    fun validateStorageUrl(context: Context, url: String?): ValidationResult {
        if (url.isNullOrBlank()) {
            return ValidationResult.success() // Empty URLs are allowed
        }

        return if (url.startsWith(FIREBASE_STORAGE_DOMAIN)) {
            ValidationResult.success()
        } else {
            ValidationResult.error(context.getString(R.string.error_invalid_image_url))
        }
    }

    /** Validate group member count */
    fun validateGroupMembers(context: Context, memberCount: Int): ValidationResult {
        return when {
            memberCount < MIN_GROUP_MEMBERS -> {
                ValidationResult.error("Group must have at least one member")
            }
            memberCount > MAX_GROUP_MEMBERS -> {
                ValidationResult.error(context.getString(R.string.error_max_group_members_reached))
            }
            else -> ValidationResult.success()
        }
    }

    /** Validate task assignments */
    fun validateTaskAssignments(context: Context, assignmentCount: Int): ValidationResult {
        return when {
            assignmentCount < MIN_TASK_ASSIGNMENTS -> {
                ValidationResult.error("Task must be assigned to at least one user")
            }
            assignmentCount > MAX_TASK_ASSIGNMENTS -> {
                ValidationResult.error(
                        context.getString(R.string.error_max_task_assignments_reached)
                )
            }
            else -> ValidationResult.success()
        }
    }

    /** Validate file type for profile pictures */
    fun validateProfilePictureType(context: Context, mimeType: String?): ValidationResult {
        if (mimeType == null) {
            return ValidationResult.error(
                    context.getString(R.string.error_invalid_profile_picture_type)
            )
        }

        return if (SUPPORTED_IMAGE_TYPES.contains(mimeType)) {
            ValidationResult.success()
        } else {
            ValidationResult.error(context.getString(R.string.error_invalid_profile_picture_type))
        }
    }

    /** Validate file type for documents/attachments */
    fun validateDocumentType(context: Context, mimeType: String?): ValidationResult {
        if (mimeType == null) {
            return ValidationResult.error(context.getString(R.string.error_invalid_document_type))
        }

        val isSupported =
                SUPPORTED_IMAGE_TYPES.contains(mimeType) ||
                        SUPPORTED_DOCUMENT_TYPES.contains(mimeType)

        return if (isSupported) {
            ValidationResult.success()
        } else {
            ValidationResult.error(context.getString(R.string.error_invalid_document_type))
        }
    }

    /** Validate file size for profile pictures */
    fun validateProfilePictureSize(context: Context, fileSizeBytes: Long): ValidationResult {
        val maxSizeBytes = MAX_PROFILE_PICTURE_SIZE_MB * 1024 * 1024

        return if (fileSizeBytes <= maxSizeBytes) {
            ValidationResult.success()
        } else {
            ValidationResult.error(context.getString(R.string.error_profile_picture_too_large))
        }
    }

    /** Validate file size for documents */
    fun validateDocumentSize(context: Context, fileSizeBytes: Long): ValidationResult {
        val maxSizeBytes = MAX_DOCUMENT_SIZE_MB * 1024 * 1024

        return if (fileSizeBytes <= maxSizeBytes) {
            ValidationResult.success()
        } else {
            ValidationResult.error(context.getString(R.string.error_document_too_large))
        }
    }

    /** Get MIME type from file */
    fun getMimeType(file: File): String? {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /** Get MIME type from URI path */
    fun getMimeTypeFromPath(path: String): String? {
        val extension = path.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } else {
            null
        }
    }

    /** Validate complete file upload for profile picture */
    fun validateProfilePictureUpload(context: Context, file: File): ValidationResult {
        // Check file size
        val sizeValidation = validateProfilePictureSize(context, file.length())
        if (!sizeValidation.isValid) {
            return sizeValidation
        }

        // Check file type
        val mimeType = getMimeType(file)
        return validateProfilePictureType(context, mimeType)
    }

    /** Validate complete file upload for documents */
    fun validateDocumentUpload(context: Context, file: File): ValidationResult {
        // Check file size
        val sizeValidation = validateDocumentSize(context, file.length())
        if (!sizeValidation.isValid) {
            return sizeValidation
        }

        // Check file type
        val mimeType = getMimeType(file)
        return validateDocumentType(context, mimeType)
    }

    /** Check if user is attempting to add too many participants */
    fun canAddMoreParticipants(
            currentCount: Int,
            chatType: String,
            additionalCount: Int = 1
    ): Boolean {
        val newCount = currentCount + additionalCount
        return when (chatType.uppercase()) {
            "DIRECT" -> newCount <= MAX_DIRECT_CHAT_PARTICIPANTS
            "GROUP" -> newCount <= MAX_GROUP_CHAT_PARTICIPANTS
            else -> false
        }
    }

    /** Check if group can accept more members */
    fun canAddMoreGroupMembers(currentCount: Int, additionalCount: Int = 1): Boolean {
        return (currentCount + additionalCount) <= MAX_GROUP_MEMBERS
    }

    /** Check if task can have more assignments */
    fun canAddMoreTaskAssignments(currentCount: Int, additionalCount: Int = 1): Boolean {
        return (currentCount + additionalCount) <= MAX_TASK_ASSIGNMENTS
    }
}
