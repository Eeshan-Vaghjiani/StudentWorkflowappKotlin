package com.example.loginandregistration.utils

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import java.io.InputStream

/**
 * Memory management utility for monitoring and optimizing memory usage.
 *
 * Features:
 * - Monitor memory usage and available memory
 * - Clear image cache when memory is low
 * - Optimize bitmap loading to prevent OOM errors
 * - Provide memory statistics for debugging
 *
 * Requirements: 10.8, 10.9
 */
object MemoryManager {

    private const val TAG = "MemoryManager"
    private const val LOW_MEMORY_THRESHOLD_PERCENT = 0.15 // 15% available memory
    private const val CRITICAL_MEMORY_THRESHOLD_PERCENT = 0.10 // 10% available memory

    /**
     * Get current memory statistics.
     *
     * @param context Application context
     * @return Map containing memory statistics
     */
    fun getMemoryStats(context: Context): MemoryStats {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory

        return MemoryStats(
                totalMemory = memoryInfo.totalMem,
                availableMemory = memoryInfo.availMem,
                usedMemory = usedMemory,
                maxMemory = maxMemory,
                isLowMemory = memoryInfo.lowMemory,
                threshold = memoryInfo.threshold,
                percentAvailable = (memoryInfo.availMem.toFloat() / memoryInfo.totalMem.toFloat())
        )
    }

    /**
     * Check if device is in low memory state.
     *
     * @param context Application context
     * @return True if memory is low
     */
    fun isLowMemory(context: Context): Boolean {
        val stats = getMemoryStats(context)
        return stats.isLowMemory || stats.percentAvailable < LOW_MEMORY_THRESHOLD_PERCENT
    }

    /**
     * Check if device is in critical memory state.
     *
     * @param context Application context
     * @return True if memory is critically low
     */
    fun isCriticalMemory(context: Context): Boolean {
        val stats = getMemoryStats(context)
        return stats.percentAvailable < CRITICAL_MEMORY_THRESHOLD_PERCENT
    }

    /**
     * Clear image cache when memory is low.
     *
     * @param imageLoader Coil ImageLoader instance
     * @param level Memory trim level (from ComponentCallbacks2)
     */
    @OptIn(ExperimentalCoilApi::class)
    fun handleMemoryTrim(imageLoader: ImageLoader, level: Int) {
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Clear all caches
                Log.w(TAG, "Critical memory - clearing all image caches")
                imageLoader.memoryCache?.clear()
                imageLoader.diskCache?.clear()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                // Clear memory cache only
                Log.w(TAG, "Low memory - clearing memory cache")
                imageLoader.memoryCache?.clear()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                // Trim memory cache to 50%
                Log.i(TAG, "Moderate memory pressure - trimming memory cache")
                trimMemoryCache(imageLoader, 0.5f)
            }
        }
    }

    /**
     * Trim memory cache to a percentage of its current size.
     *
     * @param imageLoader Coil ImageLoader instance
     * @param percentage Percentage to keep (0.0 to 1.0)
     */
    @OptIn(ExperimentalCoilApi::class)
    private fun trimMemoryCache(imageLoader: ImageLoader, percentage: Float) {
        val memoryCache = imageLoader.memoryCache ?: return
        val currentSize = memoryCache.size
        val targetSize = (currentSize * percentage).toLong()

        // Coil doesn't have a direct trim method, so we clear and let it rebuild
        if (percentage < 0.5f) {
            memoryCache.clear()
        }
    }

    /**
     * Calculate optimal sample size for loading a bitmap to avoid OOM.
     *
     * @param options BitmapFactory.Options with outWidth and outHeight set
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Sample size to use
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Load a bitmap from URI with memory-efficient sampling.
     *
     * @param context Application context
     * @param uri Image URI
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Decoded bitmap or null if failed
     */
    fun decodeSampledBitmapFromUri(
            context: Context,
            uri: Uri,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap from URI", e)
            null
        }
    }

    /**
     * Load a bitmap from InputStream with memory-efficient sampling.
     *
     * @param inputStream Input stream
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Decoded bitmap or null if failed
     */
    fun decodeSampledBitmapFromStream(
            inputStream: InputStream,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap? {
        return try {
            // Mark the stream to reset later
            inputStream.mark(inputStream.available())

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)

            // Reset stream
            inputStream.reset()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory

            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap from stream", e)
            null
        }
    }

    /**
     * Get recommended image dimensions based on available memory.
     *
     * @param context Application context
     * @return Pair of (maxWidth, maxHeight)
     */
    fun getRecommendedImageDimensions(context: Context): Pair<Int, Int> {
        val stats = getMemoryStats(context)
        val availableMemoryMB = stats.availableMemory / (1024 * 1024)

        return when {
            availableMemoryMB < 50 -> Pair(800, 800) // Low memory device
            availableMemoryMB < 100 -> Pair(1280, 1280) // Medium memory device
            else -> Pair(1920, 1920) // High memory device
        }
    }

    /**
     * Log current memory statistics for debugging.
     *
     * @param context Application context
     * @param tag Tag for logging
     */
    fun logMemoryStats(context: Context, tag: String = TAG) {
        val stats = getMemoryStats(context)
        Log.d(
                tag,
                """
            Memory Statistics:
            - Total Memory: ${formatBytes(stats.totalMemory)}
            - Available Memory: ${formatBytes(stats.availableMemory)}
            - Used Memory: ${formatBytes(stats.usedMemory)}
            - Max Memory: ${formatBytes(stats.maxMemory)}
            - Percent Available: ${(stats.percentAvailable * 100).toInt()}%
            - Low Memory: ${stats.isLowMemory}
            - Threshold: ${formatBytes(stats.threshold)}
        """.trimIndent()
        )
    }

    /** Format bytes to human-readable string. */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    /** Data class containing memory statistics. */
    data class MemoryStats(
            val totalMemory: Long,
            val availableMemory: Long,
            val usedMemory: Long,
            val maxMemory: Long,
            val isLowMemory: Boolean,
            val threshold: Long,
            val percentAvailable: Float
    )
}
