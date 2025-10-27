# Task 9: Google Sign-In Flow - Testing Guide

## Overview
This guide provides step-by-step instructions for testing the enhanced Google Sign-In flow implementation.

## Prerequisites

Before testing:
1. Ensure Firebase project is properly configured
2. Google Sign-In is enabled in Firebase Console
3. SHA-1 fingerprint is added to Firebase project
4. `google-services.json` is up to date
5. App is built and installed on device/emulator

## Test Scenarios

### Scenario 1: Successful Google Sign-In (New User)

**Objective:** Verify new user can sign in with Google and all fields are initialized

**Steps:**
1. Launch the app
2. Tap "Sign in with Google" button
3. Select a Google account that hasn't signed in before
4. Grant permissions if requested
5. Wait for authentication to complete

**Expected Results:**
- ✅ Loading indicator appears during sign-in
- ✅ Success message "Welcome back!" is displayed
- ✅ User is navigated to dashboard/main screen
- ✅ No errors are shown

**Firestore Verification:**
1. Open Firebase Console → Firestore Database
2. Navigate to `users` collection
3. Find document with your user ID
4. Verify all fields are present:
   ```
   uid: [user-id]
   email: [email]
   displayName: [name]
   photoUrl: [url]
   profileImageUrl: [url]
   authProvider: "google"
   createdAt: [timestamp]
   lastActive: [timestamp]
   isOnline: true
   fcmToken: [token-string]
   aiPromptsUsed: 0
   aiPromptsLimit: 10
   bio: ""
   phoneNumber: ""
   notificationsEnabled: true
   emailNotifications: true
   tasksCompleted: 0
   groupsJoined: 0
   ```

**Logcat Verification:**
```
D/LoginActivity: Google sign-in successful, authenticating with Firebase
D/GoogleSignInHelper: signInWithCredential:success - userId: [user-id]
D/GoogleSignInHelper: User document created successfully with all required fields for user: [user-id]
D/LoginActivity: Firebase authentication successful for user: [user-id]
D/NotificationRepository: FCM token saved successfully: [token]
D/LoginActivity: FCM token saved successfully after Google sign-in
```

---

### Scenario 2: Successful Google Sign-In (Existing User)

**Objective:** Verify existing user can sign in and document is updated

**Steps:**
1. Sign out from the app
2. Launch the app again
3. Tap "Sign in with Google" button
4. Select the same Google account used before
5. Wait for authentication to complete

**Expected Results:**
- ✅ Loading indicator appears
- ✅ Success message displayed
- ✅ User navigated to dashboard
- ✅ No errors shown

**Firestore Verification:**
1. Check the same user document in Firestore
2. Verify updated fields:
   - `lastActive`: Updated to current timestamp
   - `isOnline`: true
   - `fcmToken`: Updated with current token
3. Verify original fields remain unchanged:
   - `createdAt`: Original timestamp
   - `aiPromptsUsed`: Previous value
   - Other user data preserved

---

### Scenario 3: Google Sign-In Cancellation

**Objective:** Verify graceful handling when user cancels sign-in

**Steps:**
1. Launch the app
2. Tap "Sign in with Google" button
3. When Google account picker appears, press back button or tap outside
4. Observe the app behavior

**Expected Results:**
- ✅ Loading indicator disappears
- ✅ No error message is shown
- ✅ User remains on login screen
- ✅ Can attempt sign-in again

**Logcat Verification:**
```
D/LoginActivity: Google sign-in cancelled by user
```

**Should NOT see:**
- ❌ Error toasts or snackbars
- ❌ "Sign-in failed" messages
- ❌ Crash or freeze

---

### Scenario 4: Network Error During Google Sign-In

**Objective:** Verify proper error handling for network issues

**Steps:**
1. Enable airplane mode or disable WiFi/data
2. Launch the app
3. Tap "Sign in with Google" button
4. Observe the error handling

**Expected Results:**
- ✅ Loading indicator disappears
- ✅ Error message displayed: "Network error. Please check your connection and try again."
- ✅ User remains on login screen
- ✅ Can retry after enabling network

**Logcat Verification:**
```
W/LoginActivity: Google sign in failed
E/LoginActivity: Sign-in failed (Error 7). Please try again.
```

---

### Scenario 5: Successful Email/Password Sign-In

**Objective:** Verify email sign-in also saves FCM token and initializes fields

**Steps:**
1. Enter valid email and password
2. Tap "Login" button
3. Wait for authentication

**Expected Results:**
- ✅ Loading indicator appears
- ✅ Success message "Welcome back!" displayed
- ✅ User navigated to dashboard
- ✅ No errors shown

**Firestore Verification:**
1. Check user document in Firestore
2. Verify `authProvider`: "email"
3. Verify `fcmToken` is populated
4. Verify all required fields present

**Logcat Verification:**
```
D/LoginActivity: User document created/updated successfully for user: [user-id]
D/NotificationRepository: FCM token saved successfully: [token]
D/LoginActivity: FCM token saved successfully after email login
```

---

### Scenario 6: Email Sign-In with Wrong Password

**Objective:** Verify proper error handling for authentication failures

**Steps:**
1. Enter valid email
2. Enter incorrect password
3. Tap "Login" button

**Expected Results:**
- ✅ Loading indicator disappears
- ✅ Error message displayed via ErrorHandler
- ✅ Error is user-friendly (not technical)
- ✅ User can retry

**Possible Error Messages:**
- "Incorrect password. Please try again."
- "Authentication failed. Please try again."

---

### Scenario 7: Email Sign-In with Invalid Email

**Objective:** Verify validation error handling

**Steps:**
1. Enter invalid email format (e.g., "notanemail")
2. Enter any password
3. Tap "Login" button

**Expected Results:**
- ✅ Validation error shown before API call
- ✅ Error message: "Please enter a valid email address"
- ✅ Email field highlighted with error

---

### Scenario 8: FCM Token Save Failure (Simulated)

**Objective:** Verify login continues even if FCM token save fails

**Note:** This is difficult to test without code modification, but the implementation ensures:
- Login flow completes successfully
- User is navigated to dashboard
- Error is logged but not shown to user
- App remains functional

**Logcat Verification:**
```
W/LoginActivity: Failed to save FCM token, but continuing with login
```

---

### Scenario 9: Multiple Rapid Sign-In Attempts

**Objective:** Verify no memory leaks or state issues

**Steps:**
1. Tap "Sign in with Google"
2. Immediately cancel
3. Repeat 5-10 times rapidly
4. Then complete a successful sign-in

**Expected Results:**
- ✅ No crashes or freezes
- ✅ Each cancellation handled properly
- ✅ Final sign-in succeeds normally
- ✅ No duplicate user documents created

---

### Scenario 10: Sign-In with Poor Network

**Objective:** Verify handling of slow/unstable network

**Steps:**
1. Enable network throttling (if using emulator)
2. Or use slow WiFi connection
3. Attempt Google sign-in
4. Observe behavior during slow authentication

**Expected Results:**
- ✅ Loading indicator remains visible during slow operation
- ✅ Eventually succeeds or shows timeout error
- ✅ No crashes or UI freezes
- ✅ Proper error message if timeout occurs

---

## Automated Testing Checklist

### Unit Tests (Recommended)

```kotlin
// Test FCM token saving
@Test
fun `saveFcmTokenAfterLogin should invoke callback with true on success`()

@Test
fun `saveFcmTokenAfterLogin should invoke callback with false on failure`()

// Test error handling
@Test
fun `handleGoogleSignInError should not show error for cancellation`()

@Test
fun `handleGoogleSignInError should show appropriate message for network error`()

// Test user document creation
@Test
fun `createOrUpdateUserInFirestore should initialize all required fields for new user`()

@Test
fun `createOrUpdateUserInFirestore should update only necessary fields for existing user`()
```

### Integration Tests (Recommended)

```kotlin
@Test
fun `Google sign-in flow should complete successfully`()

@Test
fun `Email sign-in should save FCM token`()

@Test
fun `Sign-in cancellation should not show error`()
```

---

## Common Issues and Solutions

### Issue 1: "Sign-in configuration error"
**Cause:** SHA-1 fingerprint not added to Firebase project
**Solution:**
1. Get SHA-1: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
2. Add to Firebase Console → Project Settings → Your apps
3. Download new `google-services.json`
4. Rebuild app

### Issue 2: FCM Token Not Saved
**Cause:** Firestore permissions or network issue
**Solution:**
1. Check Firestore security rules allow user document updates
2. Verify network connection
3. Check logcat for specific error

### Issue 3: User Document Missing Fields
**Cause:** Old code version or partial update
**Solution:**
1. Delete test user document from Firestore
2. Sign in again with updated code
3. Verify all fields present

### Issue 4: "No internet connection" Error
**Cause:** Network connectivity issue
**Solution:**
1. Check device/emulator network settings
2. Verify Firebase services are reachable
3. Try on different network

---

## Performance Metrics

Monitor these metrics during testing:

1. **Sign-In Duration:**
   - Google Sign-In: 2-5 seconds (typical)
   - Email Sign-In: 1-3 seconds (typical)

2. **FCM Token Save:**
   - Should complete within 1-2 seconds
   - Should not block UI

3. **User Document Creation:**
   - Should complete within 1 second
   - Should not block navigation

---

## Regression Testing

After implementing this task, verify these still work:

- [ ] Email registration flow
- [ ] Password reset flow (if implemented)
- [ ] Auto-login on app restart
- [ ] Sign-out functionality
- [ ] Profile updates
- [ ] Other features requiring authentication

---

## Sign-Off Checklist

Before marking this task complete:

- [ ] All test scenarios pass
- [ ] No crashes or errors in logcat
- [ ] User documents have all required fields
- [ ] FCM tokens are saved successfully
- [ ] Error messages are user-friendly
- [ ] Cancellation handled gracefully
- [ ] Code reviewed for quality
- [ ] Documentation updated
- [ ] No regressions in other features

---

## Next Steps

After successful testing:
1. Mark Task 9 as complete
2. Proceed to Task 10: Fix group creation and display
3. Continue with remaining tasks in the spec
