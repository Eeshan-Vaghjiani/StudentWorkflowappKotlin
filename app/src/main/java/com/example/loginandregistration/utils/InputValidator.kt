package com.example.loginandregistration.utils

import android.util.Patterns

/**
 * Utility class for input validation and sanitization. Provides validation for messages, files,
 * emails, and group names.
 */
object InputValidator {

    // Constants for validation limits
    const val MAX_MESSAGE_LENGTH = 5000
    const val MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
    const val MAX_DOCUMENT_SIZE_BYTES = 10 * 1024 * 1024 // 10MB
    const val MAX_GROUP_NAME_LENGTH = 50
    const val MIN_GROUP_NAME_LENGTH = 3
    const val MAX_GROUP_DESCRIPTION_LENGTH = 500

    /** Validation result containing success status and error message if validation fails. */
    data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null) {
        companion object {
            fun success() = ValidationResult(true, null)
            fun failure(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Validates message text length.
     * @param text The message text to validate
     * @return ValidationResult indicating if the message is valid
     */
    fun validateMessageText(text: String?): ValidationResult {
        return when {
            text.isNullOrBlank() -> ValidationResult.failure("Message cannot be empty")
            text.length > MAX_MESSAGE_LENGTH ->
                    ValidationResult.failure(
                            "Message is too long (max $MAX_MESSAGE_LENGTH characters)"
                    )
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates image file size.
     * @param sizeInBytes The size of the image file in bytes
     * @return ValidationResult indicating if the image size is valid
     */
    fun validateImageSize(sizeInBytes: Long): ValidationResult {
        return when {
            sizeInBytes <= 0 -> ValidationResult.failure("Invalid file size")
            sizeInBytes > MAX_IMAGE_SIZE_BYTES -> {
                val sizeMB = MAX_IMAGE_SIZE_BYTES / (1024 * 1024)
                ValidationResult.failure("Image is too large (max ${sizeMB}MB)")
            }
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates document file size.
     * @param sizeInBytes The size of the document file in bytes
     * @return ValidationResult indicating if the document size is valid
     */
    fun validateDocumentSize(sizeInBytes: Long): ValidationResult {
        return when {
            sizeInBytes <= 0 -> ValidationResult.failure("Invalid file size")
            sizeInBytes > MAX_DOCUMENT_SIZE_BYTES -> {
                val sizeMB = MAX_DOCUMENT_SIZE_BYTES / (1024 * 1024)
                ValidationResult.failure("Document is too large (max ${sizeMB}MB)")
            }
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates email format.
     * @param email The email address to validate
     * @return ValidationResult indicating if the email is valid
     */
    fun validateEmail(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.failure("Email cannot be empty")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    ValidationResult.failure("Invalid email format")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates group name.
     * @param name The group name to validate
     * @return ValidationResult indicating if the group name is valid
     */
    fun validateGroupName(name: String?): ValidationResult {
        return when {
            name.isNullOrBlank() -> ValidationResult.failure("Group name cannot be empty")
            name.length < MIN_GROUP_NAME_LENGTH ->
                    ValidationResult.failure(
                            "Group name must be at least $MIN_GROUP_NAME_LENGTH characters"
                    )
            name.length > MAX_GROUP_NAME_LENGTH ->
                    ValidationResult.failure(
                            "Group name is too long (max $MAX_GROUP_NAME_LENGTH characters)"
                    )
            !name.matches(Regex("^[a-zA-Z0-9\\s\\-_]+$")) ->
                    ValidationResult.failure(
                            "Group name can only contain letters, numbers, spaces, hyphens, and underscores"
                    )
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates group description.
     * @param description The group description to validate
     * @return ValidationResult indicating if the description is valid
     */
    fun validateGroupDescription(description: String?): ValidationResult {
        return when {
            description == null -> ValidationResult.success() // Description is optional
            description.length > MAX_GROUP_DESCRIPTION_LENGTH ->
                    ValidationResult.failure(
                            "Description is too long (max $MAX_GROUP_DESCRIPTION_LENGTH characters)"
                    )
            else -> ValidationResult.success()
        }
    }

    /**
     * Sanitizes user input by removing potentially harmful characters. Prevents basic injection
     * attacks by escaping special characters.
     * @param input The input string to sanitize
     * @return Sanitized string
     */
    fun sanitizeInput(input: String): String {
        return input.trim()
                .replace(Regex("[<>\"']"), "") // Remove HTML/script injection characters
                .replace(Regex("\\s+"), " ") // Normalize whitespace
    }

    /**
     * Validates and sanitizes message text.
     * @param text The message text to validate and sanitize
     * @return Pair of ValidationResult and sanitized text
     */
    fun validateAndSanitizeMessage(text: String?): Pair<ValidationResult, String?> {
        val validation = validateMessageText(text)
        if (!validation.isValid) {
            return Pair(validation, null)
        }
        val sanitized = sanitizeInput(text!!)
        return Pair(ValidationResult.success(), sanitized)
    }

    /**
     * Validates and sanitizes group name.
     * @param name The group name to validate and sanitize
     * @return Pair of ValidationResult and sanitized name
     */
    fun validateAndSanitizeGroupName(name: String?): Pair<ValidationResult, String?> {
        val validation = validateGroupName(name)
        if (!validation.isValid) {
            return Pair(validation, null)
        }
        val sanitized = sanitizeInput(name!!)
        return Pair(ValidationResult.success(), sanitized)
    }

    /**
     * Formats file size in human-readable format.
     * @param bytes The size in bytes
     * @return Formatted string (e.g., "2.5 MB")
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
