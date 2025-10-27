package com.example.loginandregistration.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Example usage of Base64Helper and Base64Validator for storing images in Firestore.
 *
 * This demonstrates the recommended approach for handling images without Firebase Storage:
 * 1. Compress and encode image to Base64
 * 2. Validate size before saving
 * 3. Store in Firestore document
 * 4. Decode and display when needed
 */
object Base64ImageExample {
    private const val TAG = "Base64ImageExample"

    /**
     * Example: Upload a profile picture as Base64 to Firestore.
     *
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @param userId User ID
     * @param firestore Firestore instance
     * @return Result with success or error
     */
    suspend fun uploadProfilePictureAsBase64(
            context: Context,
            imageUri: Uri,
            userId: String,
            firestore: FirebaseFirestore
    ): Result<Unit> {
        return try {
            Log.d(TAG, "Starting profile picture upload as Base64")

            // Step 1: Encode image to Base64 with size limit for profile pictures
            val encodeResult =
                    Base64Helper.encodeImageToBase64(
                            context = context,
                            uri = imageUri,
                            maxSizeBytes = Base64Helper.MAX_PROFILE_BASE64_SIZE_BYTES
                    )

            if (encodeResult.isFailure) {
                return Result.failure(
                        encodeResult.exceptionOrNull() ?: Exception("Failed to encode image")
                )
            }

            val base64Data = encodeResult.getOrThrow()

            // Step 2: Validate the Base64 data
            val validationResult = Base64Validator.validateProfileImage(base64Data)
            if (validationResult.isError()) {
                return Result.failure(
                        Exception(validationResult.getErrorMessage() ?: "Validation failed")
                )
            }

            Log.d(
                    TAG,
                    "Profile picture encoded and validated: ${Base64Validator.getSizeInfo(base64Data)}"
            )

            // Step 3: Save to Firestore
            firestore
                    .collection("users")
                    .document(userId)
                    .update("profileImageBase64", base64Data)
                    .await()

            Log.d(TAG, "Profile picture saved to Firestore successfully")
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile picture as Base64", e)
            return Result.failure(e)
        }
    }

    /**
     * Example: Send a chat image as Base64 in a message.
     *
     * @param context Android context
     * @param imageUri URI of the image to send
     * @param chatId Chat ID
     * @param messageId Message ID
     * @param firestore Firestore instance
     * @return Result with success or error
     */
    suspend fun sendChatImageAsBase64(
            context: Context,
            imageUri: Uri,
            chatId: String,
            messageId: String,
            firestore: FirebaseFirestore
    ): Result<Unit> {
        return try {
            Log.d(TAG, "Starting chat image upload as Base64")

            // Step 1: Encode image to Base64 with size limit for chat images
            val encodeResult =
                    Base64Helper.encodeImageToBase64(
                            context = context,
                            uri = imageUri,
                            maxSizeBytes = Base64Helper.MAX_BASE64_SIZE_BYTES
                    )

            if (encodeResult.isFailure) {
                return Result.failure(
                        encodeResult.exceptionOrNull() ?: Exception("Failed to encode image")
                )
            }

            val base64Data = encodeResult.getOrThrow()

            // Step 2: Validate the Base64 data
            val validationResult = Base64Validator.validateChatImage(base64Data)
            if (validationResult.isError()) {
                return Result.failure(
                        Exception(validationResult.getErrorMessage() ?: "Validation failed")
                )
            }

            // Step 3: Check if it will fit in Firestore document
            if (!Base64Validator.willFitInFirestoreDocument(base64Data)) {
                return Result.failure(Exception("Image is too large to fit in Firestore document"))
            }

            Log.d(
                    TAG,
                    "Chat image encoded and validated: ${Base64Validator.getSizeInfo(base64Data)}"
            )

            // Step 4: Save to Firestore message document
            val messageData =
                    mapOf(
                            "base64ImageData" to base64Data,
                            "hasImage" to true,
                            "timestamp" to System.currentTimeMillis()
                    )

            firestore
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(messageId)
                    .update(messageData)
                    .await()

            Log.d(TAG, "Chat image saved to Firestore successfully")
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending chat image as Base64", e)
            return Result.failure(e)
        }
    }

    /**
     * Example: Load and decode a Base64 image from Firestore.
     *
     * @param base64Data Base64 encoded image string from Firestore
     * @return Result with Bitmap on success
     */
    fun loadBase64Image(base64Data: String): Result<android.graphics.Bitmap> {
        return try {
            if (base64Data.isEmpty()) {
                return Result.failure(Exception("No image data"))
            }

            Log.d(TAG, "Loading Base64 image: ${Base64Validator.getSizeInfo(base64Data)}")

            // Decode Base64 to Bitmap
            val decodeResult = Base64Helper.decodeBase64ToImage(base64Data)

            if (decodeResult.isFailure) {
                return Result.failure(
                        decodeResult.exceptionOrNull() ?: Exception("Failed to decode image")
                )
            }

            val bitmap = decodeResult.getOrThrow()
            Log.d(TAG, "Image decoded successfully: ${bitmap.width}x${bitmap.height}")

            return Result.success(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Base64 image", e)
            return Result.failure(e)
        }
    }

    /**
     * Example: Compress an existing Base64 string if it's too large.
     *
     * @param base64Data Original Base64 string
     * @param targetSize Target size in bytes
     * @return Result with compressed Base64 string
     */
    fun compressBase64IfNeeded(base64Data: String, targetSize: Int): Result<String> {
        return try {
            val currentSize = base64Data.length

            if (currentSize <= targetSize) {
                Log.d(TAG, "Base64 data is already within target size")
                return Result.success(base64Data)
            }

            Log.d(TAG, "Compressing Base64 from ${currentSize / 1024}KB to ${targetSize / 1024}KB")

            val compressResult = Base64Helper.compressBase64(base64Data, targetSize)

            if (compressResult.isFailure) {
                return Result.failure(
                        compressResult.exceptionOrNull() ?: Exception("Failed to compress")
                )
            }

            val compressed = compressResult.getOrThrow()
            Log.d(TAG, "Compressed successfully to ${compressed.length / 1024}KB")

            return Result.success(compressed)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing Base64", e)
            return Result.failure(e)
        }
    }
}
