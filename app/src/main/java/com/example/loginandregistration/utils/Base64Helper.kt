package com.example.loginandregistration.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Utility class for encoding and decoding images to/from Base64 strings. Used for storing images
 * directly in Firestore documents instead of Firebase Storage.
 *
 * IMPORTANT: Base64 encoded images should be kept under 1MB to comply with Firestore document size
 * limits and performance best practices.
 */
object Base64Helper {
    private const val TAG = "Base64Helper"

    // Maximum size for base64 encoded image (1MB)
    const val MAX_BASE64_SIZE_BYTES = 1 * 1024 * 1024 // 1MB

    // For profile pictures, use smaller limit (200KB encoded)
    const val MAX_PROFILE_BASE64_SIZE_BYTES = 200 * 1024 // 200KB

    /**
     * Encodes an image from URI to Base64 string. Automatically compresses the image to stay under
     * the size limit.
     *
     * @param context Android context
     * @param uri URI of the image to encode
     * @param maxSizeBytes Maximum size for the encoded string (default 1MB)
     * @return Result with Base64 string on success, or error on failure
     */
    fun encodeImageToBase64(
            context: Context,
            uri: Uri,
            maxSizeBytes: Int = MAX_BASE64_SIZE_BYTES
    ): Result<String> {
        return try {
            Log.d(TAG, "Starting image encoding from URI: $uri")

            // First, compress the image using ImageCompressor
            // For base64, we need smaller images to fit in Firestore
            val maxDimension =
                    when {
                        maxSizeBytes <= MAX_PROFILE_BASE64_SIZE_BYTES -> 400 // Profile pictures
                        else -> 800 // Chat images
                    }

            val compressedFile =
                    ImageCompressor.compressImage(
                            context = context,
                            uri = uri,
                            maxWidth = maxDimension,
                            maxHeight = maxDimension,
                            quality = 70 // Lower quality for smaller base64 size
                    )

            // Load the compressed image as bitmap
            val bitmap =
                    BitmapFactory.decodeFile(compressedFile.absolutePath)
                            ?: throw IOException("Failed to decode compressed image")

            // Try encoding with decreasing quality until it fits
            var quality = 70
            var base64String: String
            var encodedSize: Int

            do {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                // Encode to Base64
                base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                encodedSize = base64String.length

                Log.d(TAG, "Encoded size at quality $quality: ${encodedSize / 1024}KB")

                // If it fits, we're done
                if (encodedSize <= maxSizeBytes) {
                    break
                }

                // Reduce quality and try again
                quality -= 10
            } while (quality >= 30) // Don't go below 30% quality

            // Clean up
            bitmap.recycle()
            compressedFile.delete()

            // Final check
            if (encodedSize > maxSizeBytes) {
                val sizeMB = encodedSize / (1024.0 * 1024.0)
                val maxMB = maxSizeBytes / (1024.0 * 1024.0)
                return Result.failure(
                        IOException(
                                "Image is too large even after compression. " +
                                        "Size: ${"%.2f".format(sizeMB)}MB, Max: ${"%.2f".format(maxMB)}MB. " +
                                        "Please choose a smaller image."
                        )
                )
            }

            Log.d(TAG, "Successfully encoded image to Base64 (${encodedSize / 1024}KB)")
            Result.success(base64String)
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding image to Base64", e)
            Result.failure(e)
        }
    }

    /**
     * Decodes a Base64 string to a Bitmap.
     *
     * @param base64String Base64 encoded image string
     * @return Result with Bitmap on success, or error on failure
     */
    fun decodeBase64ToImage(base64String: String): Result<Bitmap> {
        return try {
            if (base64String.isEmpty()) {
                return Result.failure(IllegalArgumentException("Base64 string is empty"))
            }

            Log.d(TAG, "Decoding Base64 string (${base64String.length / 1024}KB)")

            // Decode Base64 string to byte array
            val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)

            // Decode byte array to Bitmap
            val bitmap =
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            ?: throw IOException("Failed to decode Base64 string to Bitmap")

            Log.d(TAG, "Successfully decoded Base64 to Bitmap (${bitmap.width}x${bitmap.height})")
            Result.success(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Base64 to image", e)
            Result.failure(e)
        }
    }

    /**
     * Validates that a Base64 string is within the size limit.
     *
     * @param base64String Base64 encoded string to validate
     * @param maxSizeBytes Maximum allowed size in bytes
     * @return true if valid, false if exceeds limit
     */
    fun validateBase64Size(
            base64String: String,
            maxSizeBytes: Int = MAX_BASE64_SIZE_BYTES
    ): Boolean {
        val sizeBytes = base64String.length
        val isValid = sizeBytes <= maxSizeBytes

        if (!isValid) {
            Log.w(
                    TAG,
                    "Base64 size validation failed: ${sizeBytes / 1024}KB exceeds ${maxSizeBytes / 1024}KB"
            )
        }

        return isValid
    }

    /**
     * Gets the size of a Base64 string in bytes.
     *
     * @param base64String Base64 encoded string
     * @return Size in bytes
     */
    fun getBase64Size(base64String: String): Int {
        return base64String.length
    }

    /**
     * Gets the size of a Base64 string in kilobytes.
     *
     * @param base64String Base64 encoded string
     * @return Size in KB
     */
    fun getBase64SizeKB(base64String: String): Double {
        return base64String.length / 1024.0
    }

    /**
     * Gets the size of a Base64 string in megabytes.
     *
     * @param base64String Base64 encoded string
     * @return Size in MB
     */
    fun getBase64SizeMB(base64String: String): Double {
        return base64String.length / (1024.0 * 1024.0)
    }

    /**
     * Compresses a Base64 string by re-encoding the image at lower quality. Useful when an existing
     * Base64 string is too large.
     *
     * @param base64String Original Base64 string
     * @param targetSizeBytes Target size in bytes
     * @return Result with compressed Base64 string on success
     */
    fun compressBase64(
            base64String: String,
            targetSizeBytes: Int = MAX_BASE64_SIZE_BYTES
    ): Result<String> {
        return try {
            Log.d(
                    TAG,
                    "Compressing Base64 string from ${base64String.length / 1024}KB to ${targetSizeBytes / 1024}KB"
            )

            // Decode to bitmap
            val bitmap = decodeBase64ToImage(base64String).getOrThrow()

            // Try encoding with decreasing quality
            var quality = 70
            var compressedBase64: String
            var encodedSize: Int

            do {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                compressedBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                encodedSize = compressedBase64.length

                if (encodedSize <= targetSizeBytes) {
                    break
                }

                quality -= 10
            } while (quality >= 30)

            bitmap.recycle()

            if (encodedSize > targetSizeBytes) {
                return Result.failure(
                        IOException("Unable to compress Base64 string to target size")
                )
            }

            Log.d(
                    TAG,
                    "Successfully compressed Base64 to ${encodedSize / 1024}KB at quality $quality"
            )
            Result.success(compressedBase64)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing Base64 string", e)
            Result.failure(e)
        }
    }
}
