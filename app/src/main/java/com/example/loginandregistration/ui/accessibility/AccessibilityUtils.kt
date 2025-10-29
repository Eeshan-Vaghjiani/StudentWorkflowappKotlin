package com.example.loginandregistration.ui.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Accessibility utilities for ensuring WCAG AA compliance */
object AccessibilityUtils {

    /**
     * Calculates the contrast ratio between two colors WCAG AA requires:
     * - 4.5:1 for normal text
     * - 3:1 for large text (18pt+ or 14pt+ bold)
     *
     * @param foreground The foreground color (text)
     * @param background The background color
     * @return The contrast ratio
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val luminance1 = foreground.luminance()
        val luminance2 = background.luminance()

        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)

        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Checks if the contrast ratio meets WCAG AA standards for normal text
     *
     * @param foreground The foreground color
     * @param background The background color
     * @return True if contrast ratio is at least 4.5:1
     */
    fun meetsWCAGAANormalText(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 4.5
    }

    /**
     * Checks if the contrast ratio meets WCAG AA standards for large text
     *
     * @param foreground The foreground color
     * @param background The background color
     * @return True if contrast ratio is at least 3:1
     */
    fun meetsWCAGAALargeText(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 3.0
    }

    /**
     * Checks if the contrast ratio meets WCAG AAA standards for normal text
     *
     * @param foreground The foreground color
     * @param background The background color
     * @return True if contrast ratio is at least 7:1
     */
    fun meetsWCAGAAANormalText(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 7.0
    }

    /**
     * Adjusts a color to meet minimum contrast requirements
     *
     * @param foreground The foreground color to adjust
     * @param background The background color
     * @param targetRatio The target contrast ratio (default 4.5 for WCAG AA)
     * @return An adjusted color that meets the contrast requirement
     */
    fun adjustColorForContrast(
            foreground: Color,
            background: Color,
            targetRatio: Double = 4.5
    ): Color {
        var adjustedColor = foreground
        var currentRatio = calculateContrastRatio(adjustedColor, background)

        if (currentRatio >= targetRatio) {
            return adjustedColor
        }

        // Determine if we need to lighten or darken
        val shouldLighten = background.luminance() < 0.5

        var factor = if (shouldLighten) 1.1f else 0.9f
        var iterations = 0
        val maxIterations = 20

        while (currentRatio < targetRatio && iterations < maxIterations) {
            adjustedColor =
                    if (shouldLighten) {
                        adjustedColor.copy(
                                red = (adjustedColor.red * factor).coerceIn(0f, 1f),
                                green = (adjustedColor.green * factor).coerceIn(0f, 1f),
                                blue = (adjustedColor.blue * factor).coerceIn(0f, 1f)
                        )
                    } else {
                        adjustedColor.copy(
                                red = (adjustedColor.red * factor).coerceIn(0f, 1f),
                                green = (adjustedColor.green * factor).coerceIn(0f, 1f),
                                blue = (adjustedColor.blue * factor).coerceIn(0f, 1f)
                        )
                    }

            currentRatio = calculateContrastRatio(adjustedColor, background)
            iterations++
        }

        return adjustedColor
    }

    /**
     * Validates if a touch target meets minimum size requirements WCAG recommends at least 44x44 dp
     * for touch targets
     *
     * @param sizeDp The size of the touch target in dp
     * @return True if size meets minimum requirement
     */
    fun meetsTouchTargetSize(sizeDp: Float): Boolean {
        return sizeDp >= 44f
    }

    /** Recommended minimum touch target size in dp */
    const val MIN_TOUCH_TARGET_SIZE_DP = 44f

    /** Recommended minimum spacing between touch targets in dp */
    const val MIN_TOUCH_TARGET_SPACING_DP = 8f
}

/** Semantic labels for common UI elements to ensure consistent accessibility */
object AccessibilityLabels {
    const val BUTTON_BACK = "Navigate back"
    const val BUTTON_CLOSE = "Close"
    const val BUTTON_MENU = "Open menu"
    const val BUTTON_SEARCH = "Search"
    const val BUTTON_FILTER = "Filter"
    const val BUTTON_SORT = "Sort"
    const val BUTTON_REFRESH = "Refresh"
    const val BUTTON_ADD = "Add"
    const val BUTTON_EDIT = "Edit"
    const val BUTTON_DELETE = "Delete"
    const val BUTTON_SAVE = "Save"
    const val BUTTON_CANCEL = "Cancel"
    const val BUTTON_SUBMIT = "Submit"
    const val BUTTON_SEND = "Send"

    const val LOADING = "Loading"
    const val ERROR = "Error"
    const val SUCCESS = "Success"
    const val WARNING = "Warning"

    const val CHECKBOX_CHECKED = "Checked"
    const val CHECKBOX_UNCHECKED = "Unchecked"
    const val SWITCH_ON = "On"
    const val SWITCH_OFF = "Off"

    fun taskStatus(status: String): String = "Task status: $status"
    fun priority(priority: String): String = "Priority: $priority"
    fun dueDate(date: String): String = "Due date: $date"
    fun assigneeCount(count: Int): String = "$count assignee${if (count != 1) "s" else ""}"
}

/** Extension function to validate color contrast */
fun Color.hasGoodContrastWith(other: Color): Boolean {
    return AccessibilityUtils.meetsWCAGAANormalText(this, other)
}

/** Extension function to get an accessible version of a color */
fun Color.makeAccessibleOn(background: Color): Color {
    return AccessibilityUtils.adjustColorForContrast(this, background)
}
