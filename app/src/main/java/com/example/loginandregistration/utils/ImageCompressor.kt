package com.example.loginandregistration.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * Utility class for compressing images before upload. Compresses images to max 1920x1080 resolution
 * at 80% quality while maintaining aspect ratio.
 */
object ImageCompressor {
    private const val TAG = "ImageCompressor"
    private const val MAX_WIDTH = 1920
    private const val MAX_HEIGHT = 1080
    private const val COMPRESSION_QUALITY = 80

    /**
     * Compresses an image from URI to a cache file.
     *
     * @param context Android context
     * @param uri URI of the image to compress
     * @param maxWidth Maximum width (default 1920)
     * @param maxHeight Maximum height (default 1080)
     * @param quality Compression quality 0-100 (default 80)
     * @return Compressed image file in cache directory
     * @throws IOException if compression fails
     */
    fun compressImage(
            context: Context,
            uri: Uri,
            maxWidth: Int = MAX_WIDTH,
            maxHeight: Int = MAX_HEIGHT,
            quality: Int = COMPRESSION_QUALITY
    ): File {
        try {
            Log.d(TAG, "Starting image compression for URI: $uri")

            // Check memory before loading bitmap
            if (MemoryManager.isLowMemory(context)) {
                Log.w(TAG, "Low memory detected - using reduced dimensions")
                val (reducedWidth, reducedHeight) =
                        MemoryManager.getRecommendedImageDimensions(context)
                return compressImage(context, uri, reducedWidth, reducedHeight, quality)
            }

            // Use MemoryManager for efficient bitmap decoding
            var bitmap =
                    MemoryManager.decodeSampledBitmapFromUri(context, uri, maxWidth, maxHeight)
                            ?: throw IOException("Failed to decode bitmap from URI: $uri")

            Log.d(TAG, "Decoded bitmap dimensions: ${bitmap.width}x${bitmap.height}")

            // Handle image rotation based on EXIF data
            bitmap = rotateImageIfRequired(context, uri, bitmap)

            // Resize if still larger than max dimensions
            bitmap = resizeIfNeeded(bitmap, maxWidth, maxHeight)
            Log.d(TAG, "Final bitmap dimensions: ${bitmap.width}x${bitmap.height}")

            // Save compressed image to cache directory
            val cacheDir = File(context.cacheDir, "compressed_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val outputFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            // Recycle bitmap to free memory immediately
            bitmap.recycle()

            val fileSizeKB = outputFile.length() / 1024
            Log.d(TAG, "Compressed image saved: ${outputFile.absolutePath} (${fileSizeKB}KB)")

            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            throw IOException("Failed to compress image: ${e.message}", e)
        }
    }

    /**
     * Calculates the sample size for efficient bitmap decoding. Uses power of 2 for better
     * performance.
     */
    private fun calculateSampleSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
        var sampleSize = 1

        if (width > maxWidth || height > maxHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2

            // Calculate the largest sample size that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while ((halfWidth / sampleSize) >= maxWidth && (halfHeight / sampleSize) >= maxHeight) {
                sampleSize *= 2
            }
        }

        return sampleSize
    }

    /** Resizes bitmap if it exceeds max dimensions while maintaining aspect ratio. */
    private fun resizeIfNeeded(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Check if resizing is needed
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        // Calculate scale factor to fit within max dimensions
        val scale =
                min(maxWidth.toFloat() / width.toFloat(), maxHeight.toFloat() / height.toFloat())

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        Log.d(TAG, "Resizing from ${width}x${height} to ${newWidth}x${newHeight}")

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Recycle original if it's different from resized
        if (resizedBitmap != bitmap) {
            bitmap.recycle()
        }

        return resizedBitmap
    }

    /**
     * Rotates the image based on EXIF orientation data. This is important for images taken with
     * camera.
     */
    private fun rotateImageIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val orientation =
                    exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                    )

            val rotation =
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> return bitmap
                    }

            Log.d(TAG, "Rotating image by $rotation degrees")

            val matrix = Matrix()
            matrix.postRotate(rotation)

            val rotatedBitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Recycle original if it's different from rotated
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }

            return rotatedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating image", e)
            return bitmap
        }
    }

    /**
     * Compresses an image specifically for Base64 encoding. Uses smaller dimensions and lower
     * quality to ensure the Base64 string stays under 1MB.
     *
     * @param context Android context
     * @param uri URI of the image to compress
     * @param maxWidth Maximum width (default 800 for chat images)
     * @param maxHeight Maximum height (default 800 for chat images)
     * @param quality Compression quality 0-100 (default 70 for smaller size)
     * @return Compressed image file in cache directory
     * @throws IOException if compression fails
     */
    fun compressImageForBase64(
            context: Context,
            uri: Uri,
            maxWidth: Int = 800,
            maxHeight: Int = 800,
            quality: Int = 70
    ): File {
        Log.d(
                TAG,
                "Compressing image for Base64 encoding with max dimensions ${maxWidth}x${maxHeight}"
        )
        return compressImage(context, uri, maxWidth, maxHeight, quality)
    }

    /** Clears all compressed images from cache directory. */
    fun clearCache(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "compressed_images")
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file -> file.delete() }
                Log.d(TAG, "Cleared compressed images cache")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
}
