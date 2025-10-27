package com.example.loginandregistration.utils

import android.util.Log

/**
 * Validator for Base64 encoded data to ensure compliance with Firestore limits.
 *
 * Firestore document size limit: 1MB (1,048,576 bytes) Since documents contain other fields, we
 * limit Base64 data to stay well under this limit.
 */
object Base64Validator {
    private const val TAG = "Base64Validator"

    // Size limits for different types of Base64 data
    const val MAX_CHAT_IMAGE_SIZE = 1 * 1024 * 1024 // 1MB for chat images
    const val MAX_PROFILE_IMAGE_SIZE = 200 * 1024 // 200KB for profile pictures

    /**
     * Validates Base64 data for chat images.
     *
     * @param base64Data Base64 encoded string
     * @return ValidationResult with success or error message
     */
    fun validateChatImage(base64Data: String): ValidationResult {
        if (base64Data.isEmpty()) {
            return ValidationResult.Error("Image data is empty")
        }

        val sizeBytes = base64Data.length
        val sizeKB = sizeBytes / 1024.0

        return if (sizeBytes <= MAX_CHAT_IMAGE_SIZE) {
            Log.d(TAG, "Chat image validation passed: ${"%.2f".format(sizeKB)}KB")
            ValidationResult.Success
        } else {
            val maxKB = MAX_CHAT_IMAGE_SIZE / 1024.0
            val message =
                    "Image is too large: ${"%.2f".format(sizeKB)}KB exceeds limit of ${"%.2f".format(maxKB)}KB"
            Log.w(TAG, "Chat image validation failed: $message")
            ValidationResult.Error(message)
        }
    }

    /**
     * Validates Base64 data for profile pictures.
     *
     * @param base64Data Base64 encoded string
     * @return ValidationResult with success or error message
     */
    fun validateProfileImage(base64Data: String): ValidationResult {
        if (base64Data.isEmpty()) {
            return ValidationResult.Error("Image data is empty")
        }

        val sizeBytes = base64Data.length
        val sizeKB = sizeBytes / 1024.0

        return if (sizeBytes <= MAX_PROFILE_IMAGE_SIZE) {
            Log.d(TAG, "Profile image validation passed: ${"%.2f".format(sizeKB)}KB")
            ValidationResult.Success
        } else {
            val maxKB = MAX_PROFILE_IMAGE_SIZE / 1024.0
            val message =
                    "Profile picture is too large: ${"%.2f".format(sizeKB)}KB exceeds limit of ${"%.2f".format(maxKB)}KB"
            Log.w(TAG, "Profile image validation failed: $message")
            ValidationResult.Error(message)
        }
    }

    /**
     * Validates Base64 data with custom size limit.
     *
     * @param base64Data Base64 encoded string
     * @param maxSizeBytes Maximum allowed size in bytes
     * @param dataType Description of the data type (for error messages)
     * @return ValidationResult with success or error message
     */
    fun validateCustomSize(
            base64Data: String,
            maxSizeBytes: Int,
            dataType: String = "Data"
    ): ValidationResult {
        if (base64Data.isEmpty()) {
            return ValidationResult.Error("$dataType is empty")
        }

        val sizeBytes = base64Data.length
        val sizeKB = sizeBytes / 1024.0

        return if (sizeBytes <= maxSizeBytes) {
            Log.d(TAG, "$dataType validation passed: ${"%.2f".format(sizeKB)}KB")
            ValidationResult.Success
        } else {
            val maxKB = maxSizeBytes / 1024.0
            val message =
                    "$dataType is too large: ${"%.2f".format(sizeKB)}KB exceeds limit of ${"%.2f".format(maxKB)}KB"
            Log.w(TAG, "$dataType validation failed: $message")
            ValidationResult.Error(message)
        }
    }

    /**
     * Gets human-readable size information for Base64 data.
     *
     * @param base64Data Base64 encoded string
     * @return Size information string
     */
    fun getSizeInfo(base64Data: String): String {
        val sizeBytes = base64Data.length
        return when {
            sizeBytes < 1024 -> "$sizeBytes bytes"
            sizeBytes < 1024 * 1024 -> "${"%.2f".format(sizeBytes / 1024.0)} KB"
            else -> "${"%.2f".format(sizeBytes / (1024.0 * 1024.0))} MB"
        }
    }

    /**
     * Checks if Base64 data will fit in a Firestore document. Firestore has a 1MB document size
     * limit, so we check if the data plus estimated overhead will fit.
     *
     * @param base64Data Base64 encoded string
     * @param estimatedOverheadBytes Estimated size of other fields in the document
     * @return true if data will fit, false otherwise
     */
    fun willFitInFirestoreDocument(
            base64Data: String,
            estimatedOverheadBytes: Int = 50 * 1024 // 50KB overhead estimate
    ): Boolean {
        val firestoreLimit = 1 * 1024 * 1024 // 1MB
        val totalSize = base64Data.length + estimatedOverheadBytes

        val willFit = totalSize <= firestoreLimit

        if (!willFit) {
            Log.w(
                    TAG,
                    "Data will not fit in Firestore document: ${totalSize / 1024}KB exceeds ${firestoreLimit / 1024}KB"
            )
        }

        return willFit
    }

    /** Result of validation. */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()

        fun isSuccess(): Boolean = this is Success
        fun isError(): Boolean = this is Error

        fun getErrorMessage(): String? = (this as? Error)?.message
    }
}
