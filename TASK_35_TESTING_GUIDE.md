# Task 35: Error Handling Testing Guide

## 🧪 Testing the ErrorHandler Implementation

This guide provides comprehensive testing scenarios for the ErrorHandler utility.

## 📋 Testing Checklist

### ✅ 1. Network Error Testing

#### Test Scenario 1.1: No Internet Connection
**Steps:**
1. Turn on airplane mode on your device
2. Try to load chats, tasks, or any data from Firestore
3. **Expected Result:**
   - Snackbar appears at bottom: "No internet connection. Please check your network and try again."
   - Snackbar has a blue "RETRY" button
   - Tapping retry attempts to reload data

#### Test Scenario 1.2: Connection Timeout
**Steps:**
1. Use a very slow network connection
2. Try to upload a large file
3. **Expected Result:**
   - After timeout, Snackbar appears with appropriate message
   - Retry button available

#### Test Scenario 1.3: Service Unavailable
**Steps:**
1. Simulate Firebase service being down (if possible)
2. Try to access Firestore
3. **Expected Result:**
   - Snackbar: "Service temporarily unavailable. Please try again."
   - Retry button available

### ✅ 2. Permission Error Testing

#### Test Scenario 2.1: Camera Permission Denied
**Steps:**
1. Go to Profile or Chat
2. Try to take a photo
3. Deny camera permission when prompted
4. **Expected Result:**
   - Dialog appears with title "Permission Required"
   - Message explains why permission is needed
   - "Open Settings" button opens app settings
   - "Cancel" button dismisses dialog

#### Test Scenario 2.2: Storage Permission Denied
**Steps:**
1. Try to select an image from gallery
2. Deny storage/media permission
3. **Expected Result:**
   - Permission error dialog appears
   - Can open settings to grant permission

#### Test Scenario 2.3: Notification Permission Denied (Android 13+)
**Steps:**
1. On Android 13+ device
2. Deny notification permission
3. Try to enable notifications
4. **Expected Result:**
   - Dialog explains notification permission is needed
   - Can navigate to settings

### ✅ 3. Validation Error Testing

#### Test Scenario 3.1: Empty Message
**Steps:**
1. Open a chat room
2. Try to send an empty message
3. **Expected Result:**
   - Toast appears: "Message cannot be empty."
   - Toast disappears after 2-3 seconds
   - No error dialog or snackbar

#### Test Scenario 3.2: Invalid Email
**Steps:**
1. Go to login/register screen
2. Enter invalid email format
3. Try to submit
4. **Expected Result:**
   - Toast: "Please enter a valid email address."

#### Test Scenario 3.3: Weak Password
**Steps:**
1. Try to register with password "123"
2. **Expected Result:**
   - Toast: "Password is too weak. Use at least 6 characters."

#### Test Scenario 3.4: File Too Large
**Steps:**
1. Try to upload a file larger than 10MB
2. **Expected Result:**
   - Toast with file size limit message

### ✅ 4. Authentication Error Testing

#### Test Scenario 4.1: Wrong Password
**Steps:**
1. Go to login screen
2. Enter correct email but wrong password
3. Try to login
4. **Expected Result:**
   - Toast or Snackbar: "Incorrect password. Please try again."

#### Test Scenario 4.2: User Not Found
**Steps:**
1. Try to login with non-existent email
2. **Expected Result:**
   - Toast: "No account found with this email."

#### Test Scenario 4.3: Email Already in Use
**Steps:**
1. Try to register with existing email
2. **Expected Result:**
   - Toast: "This email is already registered."

#### Test Scenario 4.4: Too Many Requests
**Steps:**
1. Try to login multiple times with wrong password
2. **Expected Result:**
   - Toast: "Too many attempts. Please try again later."

### ✅ 5. Storage Error Testing

#### Test Scenario 5.1: Upload Failed
**Steps:**
1. Turn on airplane mode
2. Try to upload an image
3. **Expected Result:**
   - Snackbar: "Upload failed. Please try again."
   - Retry button available

#### Test Scenario 5.2: Download Failed
**Steps:**
1. Turn on airplane mode
2. Try to download a document from chat
3. **Expected Result:**
   - Snackbar: "Download failed. Please try again."
   - Retry button available

#### Test Scenario 5.3: File Not Found
**Steps:**
1. Try to access a deleted file
2. **Expected Result:**
   - Toast or Snackbar: "File not found."

### ✅ 6. Firestore Error Testing

#### Test Scenario 6.1: Permission Denied
**Steps:**
1. Try to access data you don't have permission for
2. **Expected Result:**
   - Snackbar: "You don't have permission to access this data."

#### Test Scenario 6.2: Unauthenticated
**Steps:**
1. Logout
2. Try to access protected data
3. **Expected Result:**
   - Toast: "Please sign in to continue."

### ✅ 7. Success Message Testing

#### Test Scenario 7.1: Message Sent
**Steps:**
1. Send a message in chat
2. **Expected Result:**
   - Brief Snackbar: "Message sent."
   - Auto-dismisses quickly

#### Test Scenario 7.2: File Uploaded
**Steps:**
1. Successfully upload an image
2. **Expected Result:**
   - Snackbar: "File uploaded successfully!"

#### Test Scenario 7.3: Profile Updated
**Steps:**
1. Update profile picture
2. **Expected Result:**
   - Success message appears

### ✅ 8. Console Logging Testing

#### Test Scenario 8.1: Check Logcat
**Steps:**
1. Trigger various errors
2. Open Android Studio Logcat
3. Filter by "ErrorHandler"
4. **Expected Result:**
   - All errors logged with appropriate tags
   - Network errors: `Log.e()`
   - Auth errors: `Log.e()`
   - Validation errors: `Log.w()`
   - Stack traces included for debugging

### ✅ 9. Retry Functionality Testing

#### Test Scenario 9.1: Network Retry
**Steps:**
1. Turn on airplane mode
2. Try to load data
3. See error Snackbar with retry button
4. Turn off airplane mode
5. Tap "RETRY" button
6. **Expected Result:**
   - Data loads successfully
   - Success message or data appears

#### Test Scenario 9.2: Upload Retry
**Steps:**
1. Start uploading a file
2. Turn on airplane mode mid-upload
3. Upload fails with retry option
4. Turn off airplane mode
5. Tap retry
6. **Expected Result:**
   - Upload resumes/restarts
   - Success message when complete

### ✅ 10. UI Feedback Testing

#### Test Scenario 10.1: Snackbar Appearance
**Steps:**
1. Trigger a network error
2. **Expected Result:**
   - Snackbar slides up from bottom
   - Has dark background
   - White text
   - Blue "RETRY" button
   - Auto-dismisses after 5 seconds
   - Can swipe to dismiss

#### Test Scenario 10.2: Dialog Appearance
**Steps:**
1. Trigger a permission error
2. **Expected Result:**
   - Modal dialog appears
   - Dims background
   - Has title "Permission Required"
   - Has message text
   - Has "Open Settings" button
   - Has "Cancel" button
   - Tapping outside dismisses (if cancelable)

#### Test Scenario 10.3: Toast Appearance
**Steps:**
1. Trigger a validation error
2. **Expected Result:**
   - Toast appears centered at bottom
   - Semi-transparent background
   - White text
   - Auto-dismisses after 2-3 seconds
   - Cannot be dismissed manually

## 🔧 Manual Testing Code Snippets

### Test Network Error
```kotlin
// Add to any Activity for testing
binding.testNetworkErrorButton.setOnClickListener {
    ErrorHandler.handleNetworkError(
        context = this,
        view = binding.root,
        onRetry = {
            Toast.makeText(this, "Retry clicked!", Toast.LENGTH_SHORT).show()
        }
    )
}
```

### Test Permission Error
```kotlin
binding.testPermissionErrorButton.setOnClickListener {
    ErrorHandler.handlePermissionDenied(
        context = this,
        permissionName = "Camera",
        rationale = "Camera permission is required to take photos for your profile."
    )
}
```

### Test Validation Error
```kotlin
binding.testValidationErrorButton.setOnClickListener {
    ErrorHandler.handleValidationError(
        context = this,
        message = "Please enter a valid email address."
    )
}
```

### Test Generic Error
```kotlin
binding.testGenericErrorButton.setOnClickListener {
    ErrorHandler.handleGenericError(
        context = this,
        view = binding.root,
        message = "Something went wrong. Please try again.",
        onRetry = {
            Toast.makeText(this, "Retry clicked!", Toast.LENGTH_SHORT).show()
        }
    )
}
```

### Test Success Message
```kotlin
binding.testSuccessButton.setOnClickListener {
    ErrorHandler.showSuccessMessage(
        context = this,
        view = binding.root,
        message = "Operation completed successfully!"
    )
}
```

### Test Exception Handling
```kotlin
binding.testExceptionButton.setOnClickListener {
    try {
        // Simulate network exception
        throw UnknownHostException("Unable to resolve host")
    } catch (e: Exception) {
        ErrorHandler.handleError(
            context = this,
            exception = e,
            view = binding.root,
            onRetry = {
                Toast.makeText(this, "Retry clicked!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
```

## 📊 Test Results Template

| Test Scenario | Status | Notes |
|--------------|--------|-------|
| Network Error - No Internet | ⬜ | |
| Network Error - Timeout | ⬜ | |
| Permission Error - Camera | ⬜ | |
| Permission Error - Storage | ⬜ | |
| Validation Error - Empty Field | ⬜ | |
| Validation Error - Invalid Email | ⬜ | |
| Auth Error - Wrong Password | ⬜ | |
| Auth Error - User Not Found | ⬜ | |
| Storage Error - Upload Failed | ⬜ | |
| Storage Error - Download Failed | ⬜ | |
| Firestore Error - Permission Denied | ⬜ | |
| Success Message Display | ⬜ | |
| Console Logging | ⬜ | |
| Retry Functionality | ⬜ | |
| Snackbar UI | ⬜ | |
| Dialog UI | ⬜ | |
| Toast UI | ⬜ | |

## 🎯 Integration Testing

### Test in ChatRoomActivity
1. Send message with no internet → Network error
2. Send empty message → Validation error
3. Upload image with no internet → Upload error
4. Upload image successfully → Success message

### Test in ProfileFragment
1. Update profile picture without permission → Permission error
2. Update profile picture successfully → Success message
3. Update with no internet → Network error

### Test in TasksFragment
1. Load tasks with no internet → Network error
2. Create task with empty fields → Validation error
3. Create task successfully → Success message

### Test in GroupsFragment
1. Join group with invalid code → Validation error
2. Join group with no internet → Network error
3. Join group successfully → Success message

## 🐛 Known Issues to Test

1. **Multiple Snackbars** - Ensure only one Snackbar shows at a time
2. **Dialog Over Dialog** - Ensure dialogs don't stack
3. **Toast Spam** - Ensure toasts don't queue up
4. **Memory Leaks** - Ensure dialogs are dismissed on activity destroy
5. **Context Issues** - Ensure context is valid when showing UI

## ✅ Acceptance Criteria

- [ ] All error types show appropriate UI feedback
- [ ] Network errors show Snackbar with retry button
- [ ] Permission errors show dialog with settings button
- [ ] Validation errors show toast
- [ ] All errors logged to console
- [ ] Messages are user-friendly (no technical jargon)
- [ ] Retry functionality works correctly
- [ ] Success messages display properly
- [ ] No crashes or memory leaks
- [ ] Works on different Android versions (API 23+)

## 🎉 Testing Complete

Once all test scenarios pass, the ErrorHandler implementation is ready for production use!
