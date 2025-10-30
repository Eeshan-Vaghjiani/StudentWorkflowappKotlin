package com.example.loginandregistration.utils

import com.example.loginandregistration.models.Message

/**
 * Utility class for validating Message objects to prevent null pointer exceptions and ensure data
 * integrity throughout the chat system.
 */
object MessageValidator {

    /** Sealed class representing the result of message validation */
    sealed class ValidationResult {
        /** Message is valid and can be processed */
        data class Valid(val message: Message) : ValidationResult()

        /** Message is invalid with specific error messages */
        data class Invalid(val errors: List<String>) : ValidationResult()
    }

    /**
     * Validates a Message object for null safety and required field presence.
     *
     * Checks:
     * - Message is not null
     * - Required fields (id, chatId, senderId) are not blank
     * - At least one content type is present (text or attachment)
     *
     * @param message The message to validate (can be null)
     * @return ValidationResult.Valid if message is valid, ValidationResult.Invalid with errors
     * otherwise
     */
    fun validate(message: Message?): ValidationResult {
        if (message == null) {
            android.util.Log.w("MessageValidator", "Validation failed: Message is null")
            return ValidationResult.Invalid(listOf("Message is null"))
        }

        val errors = mutableListOf<String>()

        // Validate required fields are not blank
        if (message.id.isBlank()) {
            errors.add("Message ID is blank")
        }

        if (message.chatId.isBlank()) {
            errors.add("Chat ID is blank")
        }

        if (message.senderId.isBlank()) {
            errors.add("Sender ID is blank")
        }

        // Validate that at least one content type is present
        val hasTextContent = message.text.isNotBlank()
        val hasImageContent =
                !message.imageUrl.isNullOrBlank() ||
                        (message.attachmentType == "image" &&
                                !message.attachmentUrl.isNullOrBlank())
        val hasDocumentContent =
                !message.documentUrl.isNullOrBlank() ||
                        (message.attachmentType == "document" &&
                                !message.attachmentUrl.isNullOrBlank())
        val hasAudioContent =
                message.attachmentType == "audio" && !message.attachmentUrl.isNullOrBlank()
        val hasVideoContent =
                message.attachmentType == "video" && !message.attachmentUrl.isNullOrBlank()

        val hasAnyContent =
                hasTextContent ||
                        hasImageContent ||
                        hasDocumentContent ||
                        hasAudioContent ||
                        hasVideoContent

        if (!hasAnyContent) {
            errors.add("Message has no content (text or attachment)")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid(message)
        } else {
            // Log specific validation errors with sanitized message data for debugging
            val sanitizedData = buildString {
                append("messageId: ${message.id.take(8)}..., ")
                append("chatId: ${message.chatId.take(8)}..., ")
                append("senderId: ${message.senderId.take(8)}..., ")
                append("hasText: $hasTextContent, ")
                append("hasImage: $hasImageContent, ")
                append("hasDocument: $hasDocumentContent, ")
                append("hasAudio: $hasAudioContent, ")
                append("hasVideo: $hasVideoContent, ")
                append("status: ${message.status}")
            }
            android.util.Log.w(
                    "MessageValidator",
                    "Validation failed - errors: ${errors.joinToString(", ")} | data: $sanitizedData"
            )
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Convenience function to check if a message is valid without getting detailed errors.
     *
     * @param message The message to validate (can be null)
     * @return true if message is valid, false otherwise
     */
    fun isValid(message: Message?): Boolean {
        return validate(message) is ValidationResult.Valid
    }
}
