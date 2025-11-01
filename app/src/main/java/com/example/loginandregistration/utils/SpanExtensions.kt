package com.example.loginandregistration.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder

/**
 * Extension functions for safely applying spans to text. These functions prevent
 * "SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length" errors by validating span boundaries
 * before application.
 */

/**
 * Safely apply a span to a Spannable object. Validates that:
 * - Text is not empty
 * - Start index is less than end index (no zero-length spans)
 * - Indices are within bounds
 *
 * @param what The span object to apply
 * @param start The start index of the span
 * @param end The end index of the span
 * @param flags The span flags (e.g., SPAN_EXCLUSIVE_EXCLUSIVE)
 * @return true if span was applied, false if validation failed
 */
fun Spannable.safeSetSpan(what: Any, start: Int, end: Int, flags: Int): Boolean {
    // Validate that text is not empty
    if (this.isEmpty()) {
        return false
    }

    // Validate that start is less than end (no zero-length spans)
    if (start >= end) {
        return false
    }

    // Validate that indices are within bounds
    if (start < 0 || end > this.length) {
        return false
    }

    // All validations passed, safe to apply span
    this.setSpan(what, start, end, flags)
    return true
}

/**
 * Safely apply a span to a SpannableString object. Validates that:
 * - Text is not empty
 * - Start index is less than end index (no zero-length spans)
 * - Indices are within bounds
 *
 * @param what The span object to apply
 * @param start The start index of the span
 * @param end The end index of the span
 * @param flags The span flags (e.g., SPAN_EXCLUSIVE_EXCLUSIVE)
 * @return true if span was applied, false if validation failed
 */
fun SpannableString.safeSetSpan(what: Any, start: Int, end: Int, flags: Int): Boolean {
    // Validate that text is not empty
    if (this.isEmpty()) {
        return false
    }

    // Validate that start is less than end (no zero-length spans)
    if (start >= end) {
        return false
    }

    // Validate that indices are within bounds
    if (start < 0 || end > this.length) {
        return false
    }

    // All validations passed, safe to apply span
    this.setSpan(what, start, end, flags)
    return true
}

/**
 * Safely apply a span to a SpannableStringBuilder object. Validates that:
 * - Text is not empty
 * - Start index is less than end index (no zero-length spans)
 * - Indices are within bounds
 *
 * @param what The span object to apply
 * @param start The start index of the span
 * @param end The end index of the span
 * @param flags The span flags (e.g., SPAN_EXCLUSIVE_EXCLUSIVE)
 * @return true if span was applied, false if validation failed
 */
fun SpannableStringBuilder.safeSetSpan(what: Any, start: Int, end: Int, flags: Int): Boolean {
    // Validate that text is not empty
    if (this.isEmpty()) {
        return false
    }

    // Validate that start is less than end (no zero-length spans)
    if (start >= end) {
        return false
    }

    // Validate that indices are within bounds
    if (start < 0 || end > this.length) {
        return false
    }

    // All validations passed, safe to apply span
    this.setSpan(what, start, end, flags)
    return true
}

/**
 * Helper function to validate span boundaries without applying the span. Useful for checking if a
 * span can be safely applied before attempting to do so.
 *
 * @param text The text to validate against
 * @param start The start index of the span
 * @param end The end index of the span
 * @return true if the span boundaries are valid, false otherwise
 */
fun isValidSpanRange(text: CharSequence, start: Int, end: Int): Boolean {
    // Validate that text is not empty
    if (text.isEmpty()) {
        return false
    }

    // Validate that start is less than end (no zero-length spans)
    if (start >= end) {
        return false
    }

    // Validate that indices are within bounds
    if (start < 0 || end > text.length) {
        return false
    }

    return true
}
