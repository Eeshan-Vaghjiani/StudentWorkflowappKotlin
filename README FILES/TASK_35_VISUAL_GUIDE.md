# Task 35: ErrorHandler Visual Guide

## 🎨 UI Components Overview

This guide shows what each error feedback type looks like and when to use it.

## 1. Snackbar (Network & Retriable Errors)

### Appearance
```
┌─────────────────────────────────────────────────┐
│                                                 │
│  [App Content]                                  │
│                                                 │
│                                                 │
└─────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────┐
│ ⚠️ No internet connection. Please check...  RETRY│
└─────────────────────────────────────────────────┘
```

### Characteristics
- **Position:** Bottom of screen
- **Background:** Dark gray (#323232)
- **Text Color:** White
- **Action Button:** Blue
- **Duration:** 5 seconds (auto-dismiss)
- **Dismissible:** Yes (swipe or tap outside)
- **Animation:** Slides up from bottom

### When to Use
- Network errors
- Upload/download failures
- Service unavailable
- Any error that can be retried

### Code Example
```kotlin
ErrorHandler.showNetworkErrorSnackbar(
    context = this,
    view = binding.root,
    onRetry = { loadData() }
)
```

### Visual States

#### With Retry Button
```
┌──────────────────────────────────────────────────────┐
│ ⚠️ Upload failed. Please try again.          [RETRY] │
└──────────────────────────────────────────────────────┘
```

#### Without Retry Button
```
┌──────────────────────────────────────────────────────┐
│ ⚠️ Service temporarily unavailable.                  │
└──────────────────────────────────────────────────────┘
```

#### Success Snackbar
```
┌──────────────────────────────────────────────────────┐
│ ✅ File uploaded successfully!                       │
└──────────────────────────────────────────────────────┘
```

---

## 2. Dialog (Permission Errors)

### Appearance
```
┌─────────────────────────────────────────────────┐
│                                                 │
│  [Dimmed Background]                            │
│                                                 │
│  ┌───────────────────────────────────────────┐ │
│  │  Permission Required                      │ │
│  │                                           │ │
│  │  Camera permission is required to take    │ │
│  │  photos for your profile.                 │ │
│  │                                           │ │
│  │  Please grant Camera permission in        │ │
│  │  settings.                                │ │
│  │                                           │ │
│  │              [CANCEL]  [OPEN SETTINGS]    │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Characteristics
- **Position:** Center of screen
- **Background:** White (light theme) / Dark (dark theme)
- **Overlay:** Semi-transparent black (dims background)
- **Title:** Bold, larger text
- **Message:** Regular text, multi-line
- **Buttons:** Two buttons (Cancel, Open Settings)
- **Duration:** Until user dismisses
- **Dismissible:** Yes (tap outside or cancel)
- **Animation:** Fade in with scale

### When to Use
- Camera permission denied
- Storage permission denied
- Location permission denied
- Notification permission denied
- Any permission that requires user action

### Code Example
```kotlin
ErrorHandler.handlePermissionDenied(
    context = this,
    permissionName = "Camera",
    rationale = "Camera permission is required to take photos."
)
```

### Visual States

#### Camera Permission
```
┌─────────────────────────────────────────────────┐
│  Permission Required                            │
│                                                 │
│  Camera permission is required to take photos   │
│  for your profile.                              │
│                                                 │
│  Please grant Camera permission in settings.    │
│                                                 │
│              [CANCEL]  [OPEN SETTINGS]          │
└─────────────────────────────────────────────────┘
```

#### Storage Permission
```
┌─────────────────────────────────────────────────┐
│  Permission Required                            │
│                                                 │
│  Storage permission is required to save files.  │
│                                                 │
│  Please grant Storage permission in settings.   │
│                                                 │
│              [CANCEL]  [OPEN SETTINGS]          │
└─────────────────────────────────────────────────┘
```

#### Notification Permission
```
┌─────────────────────────────────────────────────┐
│  Permission Required                            │
│                                                 │
│  Notification permission is required to         │
│  receive message alerts.                        │
│                                                 │
│  Please grant Notification permission in        │
│  settings.                                      │
│                                                 │
│              [CANCEL]  [OPEN SETTINGS]          │
└─────────────────────────────────────────────────┘
```

---

## 3. Toast (Validation & Quick Errors)

### Appearance
```
┌─────────────────────────────────────────────────┐
│                                                 │
│  [App Content]                                  │
│                                                 │
│                                                 │
│         ┌─────────────────────────────┐         │
│         │ Message cannot be empty.    │         │
│         └─────────────────────────────┘         │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Characteristics
- **Position:** Bottom-center (above navigation bar)
- **Background:** Semi-transparent dark gray
- **Text Color:** White
- **Duration:** 2-3 seconds (auto-dismiss)
- **Dismissible:** No (auto-dismiss only)
- **Animation:** Fade in/out
- **No Actions:** Cannot add buttons

### When to Use
- Empty field validation
- Invalid email format
- Weak password
- Invalid input
- Quick informational messages
- Non-critical errors

### Code Example
```kotlin
ErrorHandler.handleValidationError(
    context = this,
    message = "Please enter a valid email address."
)
```

### Visual States

#### Validation Error
```
┌─────────────────────────────────────────────────┐
│         ┌─────────────────────────────┐         │
│         │ Message cannot be empty.    │         │
│         └─────────────────────────────┘         │
└─────────────────────────────────────────────────┘
```

#### Email Validation
```
┌─────────────────────────────────────────────────┐
│         ┌─────────────────────────────────────┐ │
│         │ Please enter a valid email address. │ │
│         └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

#### Password Validation
```
┌─────────────────────────────────────────────────┐
│         ┌─────────────────────────────────────┐ │
│         │ Password must be at least 6         │ │
│         │ characters.                         │ │
│         └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

#### Success Toast
```
┌─────────────────────────────────────────────────┐
│         ┌─────────────────────────────┐         │
│         │ ✅ Message sent.            │         │
│         └─────────────────────────────┘         │
└─────────────────────────────────────────────────┘
```

---

## 4. Comparison Chart

| Feature | Snackbar | Dialog | Toast |
|---------|----------|--------|-------|
| **Position** | Bottom | Center | Bottom-center |
| **Duration** | 5 seconds | Until dismissed | 2-3 seconds |
| **Dismissible** | Yes (swipe/tap) | Yes (button/outside) | No |
| **Actions** | Yes (1 button) | Yes (2 buttons) | No |
| **Blocking** | No | Yes (modal) | No |
| **Animation** | Slide up | Fade + scale | Fade |
| **Use Case** | Retriable errors | Permissions | Quick validation |

---

## 5. Error Severity Levels

### 🔴 Critical (Dialog)
- Permission denied
- Account disabled
- Access denied
- **UI:** Modal dialog with action required

### 🟡 Warning (Snackbar)
- Network error
- Upload failed
- Service unavailable
- **UI:** Snackbar with retry option

### 🟢 Info (Toast)
- Validation error
- Empty field
- Invalid format
- **UI:** Quick toast message

### ✅ Success (Snackbar/Toast)
- Operation complete
- File uploaded
- Message sent
- **UI:** Brief confirmation

---

## 6. User Flow Examples

### Example 1: Network Error Flow
```
User taps "Load Data"
    ↓
No internet connection
    ↓
Snackbar appears at bottom
"No internet connection. Please check your network and try again."
[RETRY] button visible
    ↓
User connects to WiFi
    ↓
User taps [RETRY]
    ↓
Data loads successfully
    ↓
Snackbar: "Data loaded successfully!"
```

### Example 2: Permission Error Flow
```
User taps "Take Photo"
    ↓
Camera permission not granted
    ↓
Dialog appears (center, modal)
"Permission Required"
"Camera permission is required to take photos."
[CANCEL] [OPEN SETTINGS]
    ↓
User taps [OPEN SETTINGS]
    ↓
App settings open
    ↓
User grants permission
    ↓
User returns to app
    ↓
User taps "Take Photo" again
    ↓
Camera opens successfully
```

### Example 3: Validation Error Flow
```
User types message
    ↓
User taps "Send" (message is empty)
    ↓
Toast appears at bottom
"Message cannot be empty."
    ↓
Toast auto-dismisses after 2 seconds
    ↓
User types message
    ↓
User taps "Send"
    ↓
Message sent successfully
    ↓
Toast: "Message sent."
```

---

## 7. Color Scheme

### Snackbar
- **Background:** `#323232` (dark gray)
- **Text:** `#FFFFFF` (white)
- **Action Button:** `#33B5E5` (light blue)

### Dialog
- **Background:** `#FFFFFF` (white) or theme-based
- **Title:** `#000000` (black) or theme-based
- **Message:** `#666666` (gray) or theme-based
- **Buttons:** `#33B5E5` (blue) or theme primary color

### Toast
- **Background:** `#323232` with 90% opacity
- **Text:** `#FFFFFF` (white)

---

## 8. Accessibility

### Snackbar
- ✅ Screen reader announces message
- ✅ Action button is focusable
- ✅ Sufficient contrast ratio
- ✅ Touch target size: 48dp minimum

### Dialog
- ✅ Screen reader announces title and message
- ✅ Buttons are focusable
- ✅ Can be dismissed with back button
- ✅ Focus trapped within dialog

### Toast
- ✅ Screen reader announces message
- ⚠️ No interactive elements (by design)
- ✅ Sufficient contrast ratio
- ⚠️ Auto-dismisses (may be missed)

---

## 9. Animation Timing

### Snackbar
- **Enter:** 150ms (slide up)
- **Exit:** 75ms (slide down)
- **Auto-dismiss:** 5000ms

### Dialog
- **Enter:** 200ms (fade + scale)
- **Exit:** 150ms (fade)
- **Auto-dismiss:** Never (user action required)

### Toast
- **Enter:** 100ms (fade in)
- **Exit:** 100ms (fade out)
- **Auto-dismiss:** 2000-3000ms

---

## 10. Best Practices

### ✅ Do
- Use Snackbar for errors that can be retried
- Use Dialog for actions that require user decision
- Use Toast for quick, non-critical messages
- Provide clear, actionable messages
- Include retry buttons when appropriate
- Use consistent colors and styling

### ❌ Don't
- Don't show multiple Snackbars at once
- Don't use Dialog for non-critical errors
- Don't use Toast for important errors
- Don't use technical jargon in messages
- Don't block user with unnecessary dialogs
- Don't show errors without context

---

## 11. Testing Checklist

- [ ] Snackbar appears at bottom
- [ ] Snackbar has retry button (when applicable)
- [ ] Snackbar auto-dismisses after 5 seconds
- [ ] Snackbar can be swiped away
- [ ] Dialog appears centered
- [ ] Dialog dims background
- [ ] Dialog has two buttons
- [ ] Dialog opens settings when clicked
- [ ] Toast appears at bottom-center
- [ ] Toast auto-dismisses after 2-3 seconds
- [ ] All messages are user-friendly
- [ ] Colors match design system
- [ ] Animations are smooth
- [ ] Accessible to screen readers

---

## 🎉 Summary

The ErrorHandler provides three distinct UI feedback types:

1. **Snackbar** - For retriable errors with action buttons
2. **Dialog** - For critical errors requiring user decision
3. **Toast** - For quick, informational messages

Each type is designed for specific use cases and provides appropriate user feedback with consistent styling and behavior.
