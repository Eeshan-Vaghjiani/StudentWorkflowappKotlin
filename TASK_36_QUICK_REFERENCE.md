# Task 36: Dark Mode Support - Quick Reference

## Quick Start

### Enable Dark Mode
1. Open device Settings
2. Go to Display
3. Enable "Dark theme"
4. App automatically switches

### Disable Dark Mode
1. Open device Settings
2. Go to Display
3. Disable "Dark theme"
4. App automatically switches back

## Color Reference

### Quick Color Lookup
```kotlin
// Get theme-aware colors
val statusColor = ThemeUtils.getStatusColor(context, "overdue")
val priorityColor = ThemeUtils.getPriorityColor(context, "high")

// Check if dark mode
val isDark = ThemeUtils.isDarkMode(context)

// Get hex colors (for backward compatibility)
val hexColor = ThemeUtils.getStatusColorHex(context, "completed")
```

### Color Values

#### Dark Mode Colors
```
Background:     #1C1C1E
Cards:          #2C2C2E
Surface:        #3A3A3C
Divider:        #48484A
Text Primary:   #FFFFFF
Text Secondary: #8E8E93
Primary Blue:   #0A84FF
Error Red:      #FF453A
Warning Orange: #FF9F0A
Success Green:  #32D74B
```

#### Light Mode Colors
```
Background:     #FFFFFF
Cards:          #FFFFFF
Surface:        #F2F2F7
Divider:        #E5E5EA
Text Primary:   #000000
Text Secondary: #8E8E93
Primary Blue:   #007AFF
Error Red:      #FF3B30
Warning Orange: #FF9500
Success Green:  #34C759
```

## Usage Examples

### In XML Layouts
```xml
<!-- Use color resources -->
<TextView
    android:textColor="@color/text_primary"
    android:background="@color/card_background" />

<!-- Status colors -->
<View
    android:background="@color/overdue_color" />
```

### In Kotlin Code
```kotlin
// Using ThemeUtils
val color = ThemeUtils.getStatusColor(context, "overdue")
textView.setTextColor(color)

// Using ContextCompat
val color = ContextCompat.getColor(context, R.color.text_primary)
view.setBackgroundColor(color)

// Check dark mode
if (ThemeUtils.isDarkMode(context)) {
    // Dark mode specific logic
}
```

### Status Colors
```kotlin
// Get status color
when (status) {
    "completed" -> ThemeUtils.getStatusColor(context, "completed")
    "overdue" -> ThemeUtils.getStatusColor(context, "overdue")
    "pending" -> ThemeUtils.getStatusColor(context, "pending")
}
```

### Priority Colors
```kotlin
// Get priority color
when (priority) {
    "high" -> ThemeUtils.getPriorityColor(context, "high")
    "medium" -> ThemeUtils.getPriorityColor(context, "medium")
    "low" -> ThemeUtils.getPriorityColor(context, "low")
}
```

## File Locations

### Theme Files
```
app/src/main/res/values/themes.xml          (Light theme)
app/src/main/res/values-night/themes.xml    (Dark theme)
```

### Color Files
```
app/src/main/res/values/colors.xml          (Light colors)
app/src/main/res/values-night/colors.xml    (Dark colors)
```

### Utility Class
```
app/src/main/java/.../utils/ThemeUtils.kt
```

## Common Tasks

### Add New Color
1. Add to `values/colors.xml`
2. Add dark variant to `values-night/colors.xml`
3. Use in layouts or code

### Update Existing Color
1. Update in `values/colors.xml`
2. Update in `values-night/colors.xml`
3. Test in both themes

### Use Theme Color in Code
```kotlin
// Preferred method
val color = ContextCompat.getColor(context, R.color.your_color)

// For status/priority
val color = ThemeUtils.getStatusColor(context, status)
```

## Troubleshooting

### Text Not Readable
```kotlin
// Use theme-aware text colors
textView.setTextColor(
    ContextCompat.getColor(context, R.color.text_primary)
)
```

### Icon Not Visible
```kotlin
// Use theme-aware tint
imageView.setColorFilter(
    ContextCompat.getColor(context, R.color.text_secondary)
)
```

### Card Not Visible
```xml
<!-- Use card background color -->
<androidx.cardview.widget.CardView
    app:cardBackgroundColor="@color/card_background" />
```

### Hardcoded Color
```kotlin
// ❌ Don't do this
view.setBackgroundColor(Color.parseColor("#FFFFFF"))

// ✅ Do this instead
view.setBackgroundColor(
    ContextCompat.getColor(context, R.color.background_color)
)
```

## Testing Quick Checks

### Visual Check
- [ ] Text is readable
- [ ] Icons are visible
- [ ] Cards are distinguishable
- [ ] Colors are appropriate

### Functional Check
- [ ] Theme switches automatically
- [ ] No crashes
- [ ] No visual glitches
- [ ] Performance is good

### Accessibility Check
- [ ] Contrast ratios are good
- [ ] Text is readable
- [ ] Elements are distinguishable

## Key Points

### Do's
✅ Use color resources
✅ Use ThemeUtils for status/priority
✅ Test in both themes
✅ Check contrast ratios
✅ Use ContextCompat

### Don'ts
❌ Hardcode colors
❌ Use Color.parseColor() for theme colors
❌ Forget dark mode variants
❌ Ignore contrast
❌ Skip testing

## Resources

### Documentation
- Material Design 3 Dark Theme
- Android Dark Theme Guide
- WCAG Accessibility Guidelines

### Tools
- Android Studio Layout Inspector
- Accessibility Scanner
- Color Contrast Checker

## Support

### Common Questions

**Q: How do I add a new color?**
A: Add to both `values/colors.xml` and `values-night/colors.xml`

**Q: How do I test dark mode?**
A: Enable dark theme in device settings

**Q: Why is my text not readable?**
A: Use `@color/text_primary` or `@color/text_secondary`

**Q: How do I check if dark mode is active?**
A: Use `ThemeUtils.isDarkMode(context)`

**Q: Can users manually toggle dark mode?**
A: Currently respects system setting only

## Status
✅ Dark mode fully implemented
✅ All colors defined
✅ Theme utilities available
✅ Ready for use
