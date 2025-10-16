# Task 35: ErrorHandler Quick Reference

## 🚀 Quick Start

```kotlin
import com.example.loginandregistration.utils.ErrorHandler
```

## 📖 Common Usage Patterns

### 1. Automatic Error Handling (Recommended)

```kotlin
try {
    // Your risky operation
    val result = repository.doSomething()
} catch (e: Exception) {
    ErrorHandler.handleError(
        context = this,
        exception = e,
        view = binding.root,
        onRetry = { /* retry logic */ }
    )
}
```

### 2. Network Error

```kotlin
ErrorHandler.handleNetworkError(
    context = this,
    view = binding.root,
    onRetry = { loadData() }
)
```

### 3. Permission Error

```kotlin
ErrorHandler.handlePermissionDenied(
    context = this,
    permissionName = "Camera",
    rationale = "Camera permission is required to take photos."
)
```

### 4. Validation Error

```kotlin
ErrorHandler.handleValidationError(
    context = this,
    message = "Please enter a valid email address."
)
```

### 5. Upload Error

```kotlin
ErrorHandler.handleUploadError(
    context = this,
    view = binding.root,
    onRetry = { uploadFile() }
)
```

### 6. Download Error

```kotlin
ErrorHandler.handleDownloadError(
    context = this,
    view = binding.root,
    onRetry = { downloadFile() }
)
```

### 7. Auth Error

```kotlin
ErrorHandler.handleAuthError(
    context = this,
    message = "Please sign in to continue."
)
```

### 8. Generic Error

```kotlin
ErrorHandler.handleGenericError(
    context = this,
    view = binding.root,
    message = "Something went wrong.",
    onRetry = { retryOperation() }
)
```

### 9. Success Message

```kotlin
ErrorHandler.showSuccessMessage(
    context = this,
    view = binding.root,
    message = "File uploaded successfully!"
)
```

### 10. Info Message

```kotlin
ErrorHandler.showInfoMessage(
    context = this,
    view = binding.root,
    message = "Processing your request..."
)
```

## 🎯 UI Feedback Types

| Error Type | UI Feedback | Duration | Action |
|-----------|-------------|----------|--------|
| Network | Snackbar | 5 seconds | Retry button |
| Permission | Dialog | Until dismissed | Settings button |
| Validation | Toast | 2-3 seconds | None |
| Auth | Toast/Snackbar | 3-5 seconds | Optional retry |
| Storage | Snackbar | 5 seconds | Retry button |
| Generic | Snackbar/Toast | 3-5 seconds | Optional retry |
| Success | Snackbar | 2 seconds | None |

## 📝 Error Messages

### Network Errors
- "No internet connection. Please check your network and try again."
- "Connection timed out. Please try again."
- "Service temporarily unavailable. Please try again."

### Auth Errors
- "Please sign in to continue."
- "Incorrect password. Please try again."
- "No account found with this email."
- "This email is already registered."
- "Password is too weak. Use at least 6 characters."

### Permission Errors
- "Permission required to perform this action."
- Dialog with custom rationale message

### Storage Errors
- "Upload failed. Please try again."
- "Download failed. Please try again."
- "File not found."
- "Storage quota exceeded."

### Validation Errors
- "Message cannot be empty."
- "Please enter a valid email address."
- "This field is required."

## 🔧 Advanced Usage

### Custom Snackbar

```kotlin
ErrorHandler.showErrorSnackbar(
    context = this,
    view = binding.root,
    message = "Custom error message",
    onRetry = { /* custom retry */ }
)
```

### Custom Dialog

```kotlin
ErrorHandler.showPermissionErrorDialog(
    context = this,
    message = "Custom permission message",
    permission = "CUSTOM_PERMISSION"
)
```

### Custom Toast

```kotlin
ErrorHandler.showErrorToast(
    context = this,
    message = "Custom error message"
)
```

## 🎨 Customization

### Change Snackbar Colors

Modify in ErrorHandler.kt:
```kotlin
snackbar.setActionTextColor(context.getColor(R.color.your_color))
```

### Change Dialog Theme

Modify in ErrorHandler.kt:
```kotlin
AlertDialog.Builder(context, R.style.YourDialogTheme)
```

### Change Toast Duration

```kotlin
Toast.makeText(context, message, Toast.LENGTH_LONG).show()
```

## 📊 Error Flow Diagram

```
User Action
    ↓
Try Operation
    ↓
Exception? ──No──> Success
    ↓ Yes
ErrorHandler.handleError()
    ↓
Categorize Error
    ↓
Log to Console
    ↓
Show UI Feedback
    ├─ Snackbar (Network/Storage)
    ├─ Dialog (Permission)
    └─ Toast (Validation/Auth)
    ↓
User Action
    ├─ Retry (if available)
    ├─ Open Settings (if permission)
    └─ Dismiss
```

## 🔍 Debugging

### Enable Verbose Logging

In ErrorHandler.kt, change:
```kotlin
private const val TAG = "ErrorHandler"
```

Then filter Logcat by "ErrorHandler"

### Check Error Type

```kotlin
when (error) {
    is AppError.NetworkError -> Log.d(TAG, "Network error occurred")
    is AppError.AuthError -> Log.d(TAG, "Auth error occurred")
    // etc.
}
```

## ⚠️ Common Pitfalls

### 1. Null View
```kotlin
// ❌ Wrong - view might be null
ErrorHandler.handleError(this, e, null, onRetry)

// ✅ Correct - provide view or handle null
ErrorHandler.handleError(this, e, binding.root, onRetry)
```

### 2. Context Lifecycle
```kotlin
// ❌ Wrong - context might be destroyed
lifecycleScope.launch {
    delay(5000)
    ErrorHandler.showErrorToast(this@Activity, "Error")
}

// ✅ Correct - check if context is valid
lifecycleScope.launch {
    delay(5000)
    if (!isFinishing) {
        ErrorHandler.showErrorToast(this@Activity, "Error")
    }
}
```

### 3. Multiple Errors
```kotlin
// ❌ Wrong - might show multiple snackbars
repeat(5) {
    ErrorHandler.handleError(this, exception, view, null)
}

// ✅ Correct - show once or queue
var errorShown = false
if (!errorShown) {
    ErrorHandler.handleError(this, exception, view, null)
    errorShown = true
}
```

## 📚 Related Classes

- `ConnectionMonitor.kt` - Check network status
- `NotificationPermissionHelper.kt` - Handle notification permissions
- `strings.xml` - Error message resources

## 🎯 Best Practices

1. **Always provide context** - Use Activity or Fragment context
2. **Provide view for Snackbar** - Better UX than Toast
3. **Include retry logic** - When operation can be retried
4. **Use specific handlers** - More semantic than generic
5. **Log all errors** - Helps with debugging
6. **User-friendly messages** - No technical jargon
7. **Test error scenarios** - Don't just test happy path

## 📱 Platform Considerations

### Android 13+ (API 33)
- Notification permission required
- Use `ErrorHandler.handlePermissionDenied()` for notifications

### Android 11+ (API 30)
- Scoped storage
- Handle storage permission errors appropriately

### Android 6+ (API 23)
- Runtime permissions
- Always check and request permissions

## ✅ Checklist

Before using ErrorHandler:
- [ ] Import ErrorHandler
- [ ] Wrap risky operations in try-catch
- [ ] Provide valid context
- [ ] Provide view for Snackbar (if possible)
- [ ] Implement retry logic (if applicable)
- [ ] Test error scenarios
- [ ] Check Logcat for errors

## 🎉 Quick Tips

- Use `handleError()` for automatic categorization
- Use specific handlers for known error types
- Always provide retry logic for network operations
- Use dialogs for permission errors
- Use toasts for quick validation errors
- Check Logcat for debugging
- Test on different Android versions

## 📞 Support

For issues or questions:
1. Check Logcat for error logs
2. Review error categorization logic
3. Test with different error types
4. Verify context and view are valid
5. Check string resources exist
