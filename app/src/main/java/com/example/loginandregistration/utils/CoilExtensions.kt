package com.example.loginandregistration.utils

import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.transform.Transformation

/** Extension functions for Coil image loading with offline caching support. */

/**
 * Loads an image with offline caching enabled. This ensures images are cached to disk and can be
 * displayed when offline.
 *
 * @param url The image URL to load
 * @param placeholder Placeholder drawable resource
 * @param error Error drawable resource
 * @param transformations List of transformations to apply
 * @param crossfade Enable crossfade animation
 */
fun ImageView.loadWithCache(
        url: String?,
        placeholder: Int? = null,
        error: Int? = null,
        transformations: List<Transformation> = emptyList(),
        crossfade: Boolean = true
) {
    this.load(url) {
        // Enable disk caching for offline support
        diskCachePolicy(CachePolicy.ENABLED)
        memoryCachePolicy(CachePolicy.ENABLED)
        networkCachePolicy(CachePolicy.ENABLED)

        // Apply transformations
        if (transformations.isNotEmpty()) {
            transformations(transformations)
        }

        // Set placeholder and error
        placeholder?.let { placeholder(it) }
        error?.let { error(it) }

        // Enable crossfade
        if (crossfade) {
            crossfade(true)
        }
    }
}

/**
 * Loads a profile image with circular crop and offline caching.
 *
 * @param url The profile image URL
 * @param placeholder Placeholder drawable resource
 */
fun ImageView.loadProfileImage(
        url: String?,
        placeholder: Int = android.R.drawable.ic_menu_gallery
) {
    this.load(url) {
        diskCachePolicy(CachePolicy.ENABLED)
        memoryCachePolicy(CachePolicy.ENABLED)
        networkCachePolicy(CachePolicy.ENABLED)
        transformations(coil.transform.CircleCropTransformation())
        crossfade(true)
        placeholder(placeholder)
        error(placeholder)
    }
}

/**
 * Loads a chat image with offline caching.
 *
 * @param url The image URL
 * @param placeholder Placeholder drawable resource
 */
fun ImageView.loadChatImage(url: String?, placeholder: Int = android.R.drawable.ic_menu_gallery) {
    this.load(url) {
        diskCachePolicy(CachePolicy.ENABLED)
        memoryCachePolicy(CachePolicy.ENABLED)
        networkCachePolicy(CachePolicy.ENABLED)
        crossfade(true)
        placeholder(placeholder)
        error(android.R.drawable.ic_menu_report_image)
    }
}
