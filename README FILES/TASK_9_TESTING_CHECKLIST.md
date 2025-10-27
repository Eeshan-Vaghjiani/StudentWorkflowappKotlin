# Task 9: Google Sign-In Flow - Testing Checklist

## Pre-Testing Setup

### Firebase Configuration
- [ ] Verify `google-services.json` is in `app/` directory
- [ ] Confirm Google Sign-In is enabled in Firebase Console
- [ ] Verify SHA-1 fingerprint is added to Firebase project
- [ ] Check that `default_web_client_id` is in `strings.xml`

### Device/Emulator Setup
- [ ] Google Play Services installed
- [ ] Google account signed in on device
- [ ] Internet connection available
- [ ] App has necessary permissions

## Functional Testing

### 1. Successful Google Sign-In (New User)
**Steps:**
1. Launch app
2. Tap "Sign in with Google" button
3. Select Google account
4. Grant permissions if requested

**Expected Results:**
- [ ] Loading indicator appears
- [ ] Google account picker shows
- [ ] User is authenticated successfully
- [ ] User document created in Firestore with all fields:
  - uid, email, displayName, photoUrl, profileImageUrl
  - authProvider = "google"
  - createdAt, lastActive timestamps
  - isOnline = true
  - fcmToken (populated after login)
  - aiPromptsUsed = 0, aiPromptsLimit = 10
  - bio = "", phoneNumber = ""
  - notificationsEnabled = true, emailNotifications = true
  - tasksCompleted = 0, groupsJoined = 0
- [ ] FCM token saved to user document
- [ ] Success message: "Welcome back!" displayed
- [ ] User redirected to dashboard
- [ ] Loading indicator disappears

### 2. Successful Google Sign-In (Existing User)
**Steps:**
1. Sign out if already signed in
2. Launch app
3. Tap "Sign in with Google" button
4. Select same Google account used before

**Expected Results:**
- [ ] User authenticated successfully
- [ ] User document updated (not recreated):
  - displayName updated
  - photoUrl updated
  - lastActive updated
  - isOnline set to true
- [ ] FCM token updated
- [ ] Success message displayed
- [ ] User redirected to dashboard

### 3. Sign-In Cancellation
**Steps:**
1. Launch app
2. Tap "Sign in with Google" button
3. Press back button or tap outside account picker
4. Or select account then press back

**Expected Results:**
- [ ] Loading indicator disappears
- [ ] **NO error message shown**
- [ ] User remains on login screen
- [ ] Can attempt sign-in again
- [ ] Log shows: "Google sign-in cancelled by user"

### 4. Network Error During Sign-In
**Steps:**
1. Disable internet connection
2. Launch app
3. Tap "Sign in with Google" button

**Expected Results:**
- [ ] Error message: "Network error. Please check your connection and try again."
- [ ] Loading indicator disappears
- [ ] User can retry after enabling internet

### 5. Configuration Error
**Steps:**
1. Remove or corrupt `google-services.json`
2. Rebuild app
3. Attempt Google Sign-In

**Expected Results:**
- [ ] Error message: "Sign-in configuration error. Please contact support."
- [ ] Detailed error logged for debugging

### 6. FCM Token Saving
**Steps:**
1. Sign in successfully
2. Check Firestore console
3. Navigate to users collection
4. Find user document

**Expected Results:**
- [ ] User document exists
- [ ] `fcmToken` field is populated
- [ ] Token is a valid FCM token string
- [ ] Log shows: "FCM token saved successfully after Google sign-in"

### 7. Multiple Sign-In Attempts
**Steps:**
1. Sign in successfully
2. Sign out
3. Sign in again with same account
4. Repeat 2-3 times

**Expected Results:**
- [ ] Each sign-in succeeds
- [ ] No duplicate user documents created
- [ ] lastActive timestamp updates each time
- [ ] FCM token updates if changed
- [ ] No memory leaks or crashes

### 8. Sign-In with Different Accounts
**Steps:**
1. Sign in with Account A
2. Sign out
3. Sign in with Account B
4. Sign out
5. Sign in with Account A again

**Expected Results:**
- [ ] Each account has separate user document
- [ ] Correct user data loaded for each account
- [ ] No data mixing between accounts
- [ ] Dashboard shows correct user info

## Error Handling Testing

### 1. Firestore Permission Denied
**Steps:**
1. Modify Firestore rules to deny writes
2. Attempt Google Sign-In

**Expected Results:**
- [ ] Sign-in still completes (non-blocking)
- [ ] Error logged: "Error creating user document"
- [ ] User can access app
- [ ] Warning logged but login not blocked

### 2. FCM Token Save Failure
**Steps:**
1. Disable FCM in Firebase Console temporarily
2. Attempt Google Sign-In

**Expected Results:**
- [ ] Sign-in completes successfully
- [ ] Warning logged: "Failed to save FCM token, but continuing with login"
- [ ] User can access app
- [ ] Login flow not blocked

### 3. Invalid Google Account
**Steps:**
1. Use account not configured in Firebase Console
2. Attempt sign-in

**Expected Results:**
- [ ] Appropriate error message shown
- [ ] User can retry with different account
- [ ] No crash or undefined behavior

## UI/UX Testing

### 1. Loading States
**Test Points:**
- [ ] Loading overlay appears when sign-in starts
- [ ] Loading overlay disappears on success
- [ ] Loading overlay disappears on error
- [ ] Loading overlay disappears on cancellation
- [ ] Buttons disabled during loading
- [ ] Buttons re-enabled after loading

### 2. Error Messages
**Test Points:**
- [ ] Error messages are user-friendly
- [ ] Error messages are clearly visible
- [ ] Error messages don't contain technical jargon
- [ ] Success messages are encouraging
- [ ] Messages disappear after appropriate time

### 3. Navigation
**Test Points:**
- [ ] Successful sign-in navigates to dashboard
- [ ] Back button doesn't return to login after successful sign-in
- [ ] App state preserved during sign-in flow
- [ ] No navigation glitches or flickers

## Performance Testing

### 1. Sign-In Speed
**Metrics:**
- [ ] Sign-in completes in < 3 seconds (good network)
- [ ] Loading indicator shows immediately
- [ ] No UI freezing during sign-in
- [ ] Smooth transition to dashboard

### 2. Memory Usage
**Metrics:**
- [ ] No memory leaks after multiple sign-ins
- [ ] Coroutines properly cancelled on activity destroy
- [ ] No retained activity references

### 3. Battery Impact
**Metrics:**
- [ ] No excessive battery drain
- [ ] Background tasks properly managed
- [ ] No wake locks held unnecessarily

## Security Testing

### 1. Token Security
**Test Points:**
- [ ] FCM tokens not logged in production
- [ ] Auth tokens not exposed in logs
- [ ] User data encrypted in transit
- [ ] Secure connection (HTTPS) used

### 2. Data Privacy
**Test Points:**
- [ ] Only necessary user data collected
- [ ] User data not shared without permission
- [ ] Proper data retention policies
- [ ] GDPR compliance (if applicable)

## Regression Testing

### 1. Email Sign-In Still Works
**Test Points:**
- [ ] Email/password sign-in unaffected
- [ ] Email sign-in also saves FCM token
- [ ] Email sign-in creates proper user document
- [ ] Both auth methods work independently

### 2. Existing Features Unaffected
**Test Points:**
- [ ] Registration still works
- [ ] Password reset still works
- [ ] Dashboard loads correctly
- [ ] All other features functional

## Edge Cases

### 1. Rapid Button Taps
**Steps:**
1. Tap "Sign in with Google" button rapidly multiple times

**Expected Results:**
- [ ] Only one sign-in flow initiated
- [ ] Button disabled during loading
- [ ] No duplicate requests
- [ ] No crashes

### 2. App Backgrounding During Sign-In
**Steps:**
1. Start Google Sign-In
2. Press home button
3. Return to app

**Expected Results:**
- [ ] Sign-in flow resumes or restarts gracefully
- [ ] No crashes
- [ ] Proper state management

### 3. Account Picker Timeout
**Steps:**
1. Start Google Sign-In
2. Leave account picker open for extended time
3. Select account after delay

**Expected Results:**
- [ ] Sign-in completes or shows timeout error
- [ ] User can retry
- [ ] No crashes

## Logging Verification

### Check Logs For:
- [ ] "Google sign-in successful, authenticating with Firebase"
- [ ] "Firebase authentication successful for user: [userId]"
- [ ] "FCM token saved successfully after Google sign-in"
- [ ] "User document created successfully with all required fields"
- [ ] "Google sign-in cancelled by user" (on cancellation)
- [ ] Appropriate error logs for failures

## Firestore Verification

### Check Firestore Console:
- [ ] User document exists in `users` collection
- [ ] Document ID matches Firebase Auth UID
- [ ] All required fields present
- [ ] Field values are correct types
- [ ] Timestamps are valid
- [ ] FCM token is populated

## Final Verification

### Sign-In Flow Complete When:
- [x] All functional tests pass
- [x] All error handling tests pass
- [x] All UI/UX tests pass
- [x] No crashes or errors
- [x] User experience is smooth
- [x] All requirements met

## Test Results Summary

**Date Tested:** _________________

**Tester:** _________________

**Device/Emulator:** _________________

**Android Version:** _________________

**Test Results:**
- Total Tests: _____
- Passed: _____
- Failed: _____
- Blocked: _____

**Issues Found:**
1. _________________
2. _________________
3. _________________

**Overall Status:** ☐ PASS ☐ FAIL ☐ NEEDS REVIEW

**Notes:**
_________________________________________________________________________________
_________________________________________________________________________________
_________________________________________________________________________________
