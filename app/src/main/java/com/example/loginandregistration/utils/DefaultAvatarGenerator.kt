package com.example.loginandregistration.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.collection.LruCache

/**
 * Utility class for generating default avatar images with initials
 * Implements caching to avoid regenerating the same avatars
 */
object DefaultAvatarGenerator {

    // Cache for generated avatars (key: "initials_size_color")
    private val avatarCache = LruCache<String, Bitmap>(50)

    // Predefined color palette for consistent avatar colors
    private val colorPalette = listOf(
        Color.parseColor("#FF6B6B"), // Red
        Color.parseColor("#4ECDC4"), // Teal
        Color.parseColor("#45B7D1"), // Blue
        Color.parseColor("#FFA07A"), // Orange
        Color.parseColor("#98D8C8"), // Mint
        Color.parseColor("#F7DC6F"), // Yellow
        Color.parseColor("#BB8FCE"), // Purple
        Color.parseColor("#85C1E2"), // Sky Blue
        Color.parseColor("#F8B88B"), // Peach
        Color.parseColor("#A8E6CF")  // Light Green
    )

    /**
     * Generate an avatar bitmap with initials
     * @param name The full name to extract initials from
     * @param size The size of the avatar in pixels (width and height)
     * @param userId Optional user ID for consistent color generation
     * @return Bitmap with colored background and white initials
     */
    fun generateAvatar(name: String, size: Int, userId: String? = null): Bitmap {
        val initials = getInitials(name)
        val color = generateColorFromString(userId ?: name)
        val cacheKey = "${initials}_${size}_${color}"

        // Check cache first
        avatarCache.get(cacheKey)?.let {
            return it
        }

        // Create new bitmap
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val backgroundPaint = Paint().apply {
            this.color = color
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint)

        // Draw initials
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            textSize = size * 0.4f // 40% of avatar size
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Calculate text position (centered)
        val textBounds = Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        val textX = size / 2f
        val textY = size / 2f - textBounds.exactCenterY()

        canvas.drawText(initials, textX, textY, textPaint)

        // Cache the bitmap
        avatarCache.put(cacheKey, bitmap)

        return bitmap
    }

    /**
     * Extract initials from a name
     * @param name The full name
     * @return Initials (1-2 characters)
     */
    fun getInitials(name: String): String {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return "?"

        val names = trimmedName.split(" ").filter { it.isNotEmpty() }
        return when {
            names.size >= 2 -> {
                // First letter of first name + first letter of last name
                "${names[0].first()}${names[names.size - 1].first()}".uppercase()
            }
            names.size == 1 -> {
                // First two letters of single name
                names[0].take(2).uppercase()
            }
            else -> "?"
        }
    }

    /**
     * Generate a consistent color based on a string
     * @param str The string to generate color from (usually userId or name)
     * @return Color integer
     */
    fun generateColorFromString(str: String): Int {
        val hash = str.hashCode()
        return colorPalette[Math.abs(hash) % colorPalette.size]
    }

    /**
     * Clear the avatar cache
     * Call this if memory is low
     */
    fun clearCache() {
        avatarCache.evictAll()
    }

    /**
     * Get cache statistics
     * @return Pair of (current size, max size)
     */
    fun getCacheStats(): Pair<Int, Int> {
        return Pair(avatarCache.size(), avatarCache.maxSize())
    }
}
