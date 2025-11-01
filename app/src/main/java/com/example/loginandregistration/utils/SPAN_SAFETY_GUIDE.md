# Span Safety Guide

## Overview

This guide explains how to safely apply text spans in the TeamSync application to prevent the common "SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length" error.

## The Problem

Android's `Spannable.setSpan()` method throws an `IllegalArgumentException` when:
1. The text is empty (length = 0)
2. The start index equals or exceeds the end index (zero-length span)
3. The indices are out of bounds (start < 0 or end > text.length)

This error commonly occurs when:
- Applying formatting to empty EditText fields
- Processing text that gets deleted while spans are being applied
- Using incorrect index calculations for dynamic text

## The Solution

### Use Safe Span Extension Functions

Instead of calling `setSpan()` directly, use the safe extension functions provided in `SpanExtensions.kt`:

```kotlin
import com.example.loginandregistration.utils.safeSetSpan

// Instead of this:
spannable.setSpan(styleSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

// Use this:
spannable.safeSetSpan(styleSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
```

### Available Extension Functions

The following extension functions are available:

1. **Spannable.safeSetSpan()** - For Spannable objects
2. **SpannableString.safeSetSpan()** - For SpannableString objects
3. **SpannableStringBuilder.safeSetSpan()** - For SpannableStringBuilder objects

All functions:
- Return `Boolean` (true if span was applied, false if validation failed)
- Validate text is not empty
- Validate start < end (no zero-length spans)
- Validate indices are within bounds

### Validation Helper

You can also check if span boundaries are valid before attempting to apply:

```kotlin
import com.example.loginandregistration.utils.isValidSpanRange

if (isValidSpanRange(text, start, end)) {
    // Safe to apply span
    spannable.setSpan(styleSpan, start, end, flags)
}
```

## Examples

### Example 1: Making URLs Clickable

```kotlin
fun makeLinksClickable(textView: TextView, text: String) {
    // Check if text is empty before processing
    if (text.isEmpty()) {
        textView.text = text
        return
    }

    val spannableString = SpannableString(text)
    val links = detectLinks(text)

    links.forEach { link ->
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl(widget, link.url)
            }
        }

        // Use safe span application
        spannableString.safeSetSpan(
            clickableSpan,
            link.start,
            link.end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    textView.text = spannableString
}
```

### Example 2: Applying Text Styles

```kotlin
fun applyBoldStyle(editText: EditText, start: Int, end: Int) {
    val text = editText.text
    
    // Validate before applying
    if (isValidSpanRange(text, start, end)) {
        val boldSpan = StyleSpan(Typeface.BOLD)
        text.safeSetSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
```

### Example 3: Handling Dynamic Text

```kotlin
fun highlightSearchTerm(textView: TextView, fullText: String, searchTerm: String) {
    if (fullText.isEmpty() || searchTerm.isEmpty()) {
        textView.text = fullText
        return
    }

    val spannableString = SpannableString(fullText)
    val startIndex = fullText.indexOf(searchTerm, ignoreCase = true)

    if (startIndex >= 0) {
        val endIndex = startIndex + searchTerm.length
        val highlightSpan = BackgroundColorSpan(Color.YELLOW)
        
        // Safe application handles all edge cases
        spannableString.safeSetSpan(
            highlightSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    textView.text = spannableString
}
```

## Best Practices

1. **Always check for empty text** before processing spans
2. **Use safe extension functions** instead of direct `setSpan()` calls
3. **Validate indices** when calculating span boundaries dynamically
4. **Handle edge cases** like text deletion during span application
5. **Test with empty inputs** to ensure robustness

## Where Spans Are Used in TeamSync

The following components use text spans:

1. **LinkifyHelper** - Makes URLs clickable in chat messages
2. **MessageAdapter** - Displays formatted chat messages
3. **ChatAdapter** - Shows chat previews with formatting

All of these now use the safe span extension functions.

## Migration Guide

If you're adding new code that uses spans:

1. Import the extension functions:
   ```kotlin
   import com.example.loginandregistration.utils.safeSetSpan
   ```

2. Replace all `setSpan()` calls with `safeSetSpan()`:
   ```kotlin
   // Before
   spannable.setSpan(span, start, end, flags)
   
   // After
   spannable.safeSetSpan(span, start, end, flags)
   ```

3. Add empty text checks at the beginning of your function:
   ```kotlin
   if (text.isEmpty()) {
       return // or handle appropriately
   }
   ```

## Testing

To test span safety:

1. Test with empty text
2. Test with single character text
3. Test with text that gets deleted while processing
4. Test with invalid indices (negative, out of bounds)
5. Test with zero-length ranges (start == end)

## References

- `SpanExtensions.kt` - Safe span extension functions
- `LinkifyHelper.kt` - Example usage in URL detection
- Android Documentation: [Spans](https://developer.android.com/guide/topics/text/spans)
