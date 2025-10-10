package com.example.loginandregistration.utils

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

/**
 * Configuration for Coil ImageLoader with offline caching support.
 *
 * This configuration enables:
 * - Memory cache (25% of app memory)
 * - Disk cache (50MB)
 * - Automatic caching of profile pictures and chat images
 * - Offline image display from cache
 */
object ImageLoaderConfig {

    /**
     * Creates and configures an ImageLoader with memory and disk caching.
     *
     * @param context Application context
     * @return Configured ImageLoader instance
     */
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
                // Memory cache configuration (25% of app memory)
                .memoryCache {
                    MemoryCache.Builder(context)
                            .maxSizePercent(0.25) // Use 25% of available app memory
                            .build()
                }
                // Disk cache configuration (50MB)
                .diskCache {
                    DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache"))
                            .maxSizeBytes(50 * 1024 * 1024) // 50MB disk cache
                            .build()
                }
                // Cache policies for offline support
                .respectCacheHeaders(false) // Don't respect server cache headers
                // Enable debug logging in debug builds
                .apply {
                    if (android.util.Log.isLoggable("ImageLoader", android.util.Log.DEBUG)) {
                        logger(DebugLogger())
                    }
                }
                .build()
    }

    /**
     * Gets cache statistics for monitoring.
     *
     * @param imageLoader The ImageLoader instance
     * @return Map containing cache statistics
     */
    fun getCacheStats(imageLoader: ImageLoader): Map<String, Any> {
        val memoryCache = imageLoader.memoryCache
        val diskCache = imageLoader.diskCache

        return mapOf(
                "memoryCacheSize" to (memoryCache?.size ?: 0),
                "memoryCacheMaxSize" to (memoryCache?.maxSize ?: 0),
                "diskCacheSize" to (diskCache?.size ?: 0),
                "diskCacheMaxSize" to (diskCache?.maxSize ?: 0)
        )
    }

    /**
     * Clears all image caches (memory and disk). Useful when memory is low or user wants to clear
     * cached data.
     *
     * @param imageLoader The ImageLoader instance
     */
    fun clearCache(imageLoader: ImageLoader) {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}
