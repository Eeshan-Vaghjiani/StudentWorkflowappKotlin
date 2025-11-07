# Account Deletion Functional Testing Guide

## Overview

This document provides a comprehensive guide for performing functional testing of the account deletion feature. It covers all test scenarios required by task 20 of the Play Store compliance implementation.

**Test Date:** _To be filled during testing_  
**Tester:** _To be filled during testing_  
**Environment:** Production (GitHub Pages)  
**URLs:**
- Privacy Policy: https://[username].github.io/[repo]/privacy-policy.html
- Account Deletion: https://[username].github.io/[repo]/delete-account.html

---

## Prerequisites

Before starting the tests, ensure you have:

1. ✅ Access to Firebase Console (https://console.firebase.google.com)
2. ✅ Test email accounts for creating test users
3. ✅ Browser developer tools open (F12) for monitoring console logs
4. ✅ Network tab open to monitor Firebase API calls
5. ✅ Notepad for recording test results

---

## Test Scenario 1: Create Test Firebase Account with Sample Data

### Objective
Create a test account with sample data in all collections that will be deleted.

### Steps

1. **Create Test User Account**
   - Open the Team Collaboration App on Android
   - Register a new account with test credentials:
     - Email: `test-deletion-[timestamp]@example.com`
     - Password: `TestPass123!`
   - Note: Replace `[timestamp]` with current timestamp (e.g., `test-deletion-20250107@example.com`)

2. **Create Sample Messages**
   - Send at least 3 messages in different chats
   - Note the message IDs or content for verification

3. **Create Sample Tasks**
   - Create at least 2 tasks
   - Note the task titles for verification

4. **Join or Create Groups**
   - Join or create at least 1 group
   - Note the group name for verification

5. **Verify Data in Firebase Console**
   - Open Firebase Console → Firestore Database
   - Navigate to each collection and verify:
     - `users` collection: User profile document exists
     - `messages` collection: Messages with your `senderId` exist
     - `tasks` collection: Tasks with your `createdBy` field exist
     - `groupMembers` collection: Group membership documents exist

### Expected Results
- ✅ Test account created successfully
- ✅ Sample data exists in all relevant Firestore collections
- ✅ User can log in with test credentials

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 2: Successful Deletion Flow

### Objective
Test the complete successful deletion flow from start to finish.

### Steps

1. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html
   - Verify page loads correctly

2. **Review Warning Section**
   - Verify warning message is prominently displayed
   - Verify warning icon (⚠️) is visible
   - Verify text clearly states action is irreversible

3. **Review Data Explanation Section**
   - Verify "What Will Be Deleted" section lists all data types
   - Verify "What Will Be Retained" section explains retention policy

4. **Fill Out Deletion Form**
   - Enter test email: `test-deletion-[timestamp]@example.com`
   - Enter test password: `TestPass123!`
   - Observe: Submit button should remain disabled

5. **Check Confirmation Checkbox**
   - Check the "I understand this action cannot be undone" checkbox
   - Observe: Submit button should become enabled

6. **Submit Deletion Request**
   - Click "Delete My Account Permanently" button
   - Start timer to measure deletion time

7. **Observe Loading Spinner**
   - Verify loading spinner appears immediately
   - Verify spinner shows "Deleting your account... Please wait."
   - Verify form inputs are disabled during deletion

8. **Wait for Completion**
   - Wait for deletion process to complete
   - Note the time taken (should be < 10 seconds for typical data)

9. **Verify Success Message**
   - Verify success message displays with checkmark (✓)
   - Verify message states: "Your account has been successfully deleted"
   - Verify options to "View Privacy Policy" or "Close Page" are shown

10. **Verify Account Deletion in Firebase Console**
    - Open Firebase Console → Authentication
    - Search for test email
    - Verify: User account no longer exists

11. **Verify Data Deletion in Firestore**
    - Open Firebase Console → Firestore Database
    - Check each collection:
      - `users`: User profile document deleted
      - `messages`: Messages with test user's `senderId` deleted
      - `tasks`: Tasks with test user's `createdBy` deleted
      - `groupMembers`: Group memberships deleted

### Expected Results
- ✅ Loading spinner appears during deletion
- ✅ Success message displays after completion
- ✅ Account deleted from Firebase Authentication
- ✅ All user data deleted from Firestore collections
- ✅ Deletion completes within reasonable time (< 10 seconds)

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

**Deletion Time:** _______ seconds

---

## Test Scenario 3: Wrong Password Error

### Objective
Verify that entering wrong password shows appropriate error message without revealing account existence.

### Steps

1. **Create New Test Account** (if previous one was deleted)
   - Email: `test-wrong-password@example.com`
   - Password: `CorrectPass123!`

2. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html

3. **Enter Credentials with Wrong Password**
   - Email: `test-wrong-password@example.com`
   - Password: `WrongPassword123!` (intentionally wrong)
   - Check confirmation checkbox

4. **Submit Form**
   - Click "Delete My Account Permanently"
   - Observe loading spinner appears

5. **Verify Error Message**
   - Verify error message displays
   - Verify message states: "Invalid email or password. Please check your credentials and try again."
   - Verify message does NOT reveal whether account exists
   - Verify password field is cleared
   - Verify email field retains value (for user convenience)

6. **Verify No Deletion Occurred**
   - Check Firebase Console → Authentication
   - Verify account still exists

### Expected Results
- ✅ Error message displays without revealing account existence
- ✅ Message is user-friendly and actionable
- ✅ Password field cleared for security
- ✅ No data deleted from Firebase

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 4: Non-Existent Email Error

### Objective
Verify that entering non-existent email shows appropriate error message without revealing account existence.

### Steps

1. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html

2. **Enter Non-Existent Email**
   - Email: `nonexistent-user-12345@example.com`
   - Password: `AnyPassword123!`
   - Check confirmation checkbox

3. **Submit Form**
   - Click "Delete My Account Permanently"
   - Observe loading spinner appears

4. **Verify Error Message**
   - Verify error message displays
   - Verify message states: "Invalid email or password. Please check your credentials and try again."
   - Verify message is IDENTICAL to wrong password error
   - Verify message does NOT reveal that account doesn't exist

### Expected Results
- ✅ Error message identical to wrong password error
- ✅ No indication that account doesn't exist
- ✅ Security best practice: prevents account enumeration

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 5: Network Error Handling

### Objective
Verify that network errors are handled gracefully with appropriate user feedback.

### Steps

1. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html

2. **Open Browser Developer Tools**
   - Press F12
   - Go to Network tab
   - Enable "Offline" mode (or throttle to "Offline")

3. **Enter Valid Credentials**
   - Email: (any test account)
   - Password: (correct password)
   - Check confirmation checkbox

4. **Submit Form**
   - Click "Delete My Account Permanently"
   - Observe loading spinner appears

5. **Verify Network Error Message**
   - Verify error message displays
   - Verify message mentions network error
   - Verify message suggests checking internet connection
   - Verify retry suggestion is shown

6. **Re-enable Network**
   - Disable "Offline" mode in developer tools

7. **Retry Deletion**
   - Refresh page
   - Enter credentials again
   - Submit form
   - Verify deletion succeeds this time

### Expected Results
- ✅ Network error detected and handled gracefully
- ✅ User-friendly error message displayed
- ✅ Retry suggestion provided
- ✅ No crash or unhandled exception
- ✅ Deletion succeeds after network restored

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 6: Rate Limiting

### Objective
Verify that rate limiting prevents abuse by limiting deletion attempts to 3 per hour.

### Steps

1. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html

2. **Make First Attempt (Wrong Password)**
   - Email: `test-rate-limit@example.com`
   - Password: `WrongPass1`
   - Check confirmation checkbox
   - Submit form
   - Verify error message displays

3. **Make Second Attempt (Wrong Password)**
   - Change password to: `WrongPass2`
   - Submit form
   - Verify error message displays

4. **Make Third Attempt (Wrong Password)**
   - Change password to: `WrongPass3`
   - Submit form
   - Verify error message displays

5. **Make Fourth Attempt (Should Be Blocked)**
   - Change password to: `WrongPass4`
   - Submit form
   - Verify rate limit error displays

6. **Verify Rate Limit Message**
   - Verify message states: "Too many deletion attempts"
   - Verify message includes time until reset (e.g., "try again in 60 minutes")
   - Verify message explains this protects account security

7. **Check Browser Console**
   - Open developer tools console
   - Verify rate limit logs are present
   - Verify no sensitive data (passwords, tokens) logged

8. **Verify Rate Limit Storage**
   - Open Application tab → Session Storage
   - Verify `account_deletion_attempts` key exists
   - Verify it contains array of timestamps

9. **Test Rate Limit Reset**
   - Option A: Wait 1 hour and verify attempts reset
   - Option B: Clear session storage and verify attempts reset
   - Clear session storage: `sessionStorage.clear()`
   - Refresh page
   - Verify you can attempt deletion again

### Expected Results
- ✅ First 3 attempts allowed (even with wrong password)
- ✅ Fourth attempt blocked by rate limiter
- ✅ Rate limit message is clear and informative
- ✅ Rate limit data stored in sessionStorage (not localStorage)
- ✅ Rate limit resets after 1 hour or session clear

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

**Attempts Before Block:** _______  
**Rate Limit Message:** _______________________

---

## Test Scenario 7: Form Validation

### Objective
Verify that form validation works correctly and submit button is properly enabled/disabled.

### Steps

1. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html

2. **Test Empty Form**
   - Verify submit button is disabled
   - Verify button has `disabled` attribute

3. **Test Email Only**
   - Enter valid email: `test@example.com`
   - Verify submit button remains disabled

4. **Test Email + Password**
   - Enter password: `TestPass123`
   - Verify submit button remains disabled

5. **Test Invalid Email Format**
   - Enter invalid email: `notanemail`
   - Enter password: `TestPass123`
   - Check confirmation checkbox
   - Verify submit button remains disabled OR shows validation error

6. **Test Short Password**
   - Enter valid email: `test@example.com`
   - Enter short password: `12345` (less than 6 characters)
   - Check confirmation checkbox
   - Verify submit button remains disabled OR shows validation error

7. **Test All Fields Valid**
   - Enter valid email: `test@example.com`
   - Enter valid password: `TestPass123`
   - Check confirmation checkbox
   - Verify submit button becomes enabled

8. **Test Unchecking Confirmation**
   - Uncheck confirmation checkbox
   - Verify submit button becomes disabled again

9. **Test Real-Time Validation**
   - Type in email field
   - Observe button state updates in real-time
   - Type in password field
   - Observe button state updates in real-time

### Expected Results
- ✅ Submit button disabled when form incomplete
- ✅ Submit button enabled only when all validations pass
- ✅ Real-time validation updates button state
- ✅ Invalid email format detected
- ✅ Short password detected
- ✅ Confirmation checkbox required

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 8: XSS Prevention

### Objective
Verify that input sanitization prevents XSS attacks.

### Steps

1. **Navigate to Account Deletion Page**
   - Open: https://[username].github.io/[repo]/delete-account.html
   - Open browser console (F12)

2. **Test Script Tag in Email**
   - Enter email: `<script>alert('XSS')</script>@example.com`
   - Enter password: `TestPass123`
   - Check confirmation checkbox
   - Submit form
   - Verify: No alert popup appears
   - Verify: Script is sanitized or rejected

3. **Test JavaScript Protocol**
   - Enter email: `javascript:alert('XSS')@example.com`
   - Submit form
   - Verify: No alert popup appears
   - Verify: Input is sanitized

4. **Test Event Handler**
   - Enter email: `test@example.com" onerror="alert('XSS')`
   - Submit form
   - Verify: No alert popup appears
   - Verify: Input is sanitized

5. **Check Console for Errors**
   - Verify no XSS-related errors in console
   - Verify sanitization logs (if any) show input was cleaned

### Expected Results
- ✅ All XSS attempts blocked
- ✅ No script execution occurs
- ✅ Input sanitization working correctly
- ✅ No security vulnerabilities detected

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 9: CSRF Protection

### Objective
Verify that CSRF protection prevents requests from unauthorized origins.

### Steps

1. **Test from Correct Origin**
   - Navigate to: https://[username].github.io/[repo]/delete-account.html
   - Enter valid credentials
   - Submit form
   - Verify: Request is allowed

2. **Test from Different Origin** (Advanced)
   - Create a simple HTML file on local machine:
   ```html
   <!DOCTYPE html>
   <html>
   <body>
     <form action="https://[username].github.io/[repo]/delete-account.html" method="POST">
       <input name="email" value="test@example.com">
       <input name="password" value="TestPass123">
       <button type="submit">Submit</button>
     </form>
   </body>
   </html>
   ```
   - Open this file in browser (file:// protocol)
   - Submit form
   - Verify: CSRF protection blocks the request

3. **Check Console Logs**
   - Verify CSRF validation logs in console
   - Verify origin validation is performed

### Expected Results
- ✅ Requests from correct origin allowed
- ✅ Requests from different origins blocked
- ✅ CSRF protection working correctly

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 10: Accessibility Testing

### Objective
Verify that the account deletion page is accessible to users with disabilities.

### Steps

1. **Keyboard Navigation**
   - Navigate to page
   - Press Tab key repeatedly
   - Verify: All interactive elements are focusable
   - Verify: Focus order is logical (email → password → checkbox → button)
   - Verify: Focus indicators are visible

2. **Screen Reader Testing** (if available)
   - Use NVDA or JAWS screen reader
   - Navigate through page
   - Verify: All labels are announced correctly
   - Verify: Error messages are announced
   - Verify: Loading state is announced

3. **ARIA Attributes**
   - Open developer tools
   - Inspect form elements
   - Verify: `aria-describedby` attributes present
   - Verify: `aria-live` regions for status messages
   - Verify: `role="alert"` for error messages

4. **Color Contrast**
   - Use browser extension (e.g., WAVE)
   - Check color contrast ratios
   - Verify: Text meets WCAG AA standards (4.5:1)

### Expected Results
- ✅ All elements keyboard accessible
- ✅ Screen reader announces content correctly
- ✅ ARIA attributes properly implemented
- ✅ Color contrast meets WCAG AA standards

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 11: Mobile Responsiveness

### Objective
Verify that the account deletion page works correctly on mobile devices.

### Steps

1. **Test on Mobile Device** (or use browser dev tools)
   - Open page on mobile device or use responsive mode (F12 → Toggle device toolbar)
   - Test at 320px width (smallest mobile)
   - Test at 375px width (iPhone)
   - Test at 768px width (tablet)

2. **Verify Layout**
   - Verify: Page content fits within viewport
   - Verify: No horizontal scrolling required
   - Verify: Text is readable (minimum 16px)
   - Verify: Buttons are touch-friendly (minimum 44x44px)

3. **Test Form Interaction**
   - Tap on email field
   - Verify: Keyboard appears
   - Verify: Email keyboard type shown (with @ key)
   - Tap on password field
   - Verify: Password keyboard shown
   - Tap checkbox
   - Verify: Checkbox is easy to tap (large enough)

4. **Test in Portrait and Landscape**
   - Rotate device (or change orientation in dev tools)
   - Verify: Layout adapts correctly
   - Verify: All content remains accessible

### Expected Results
- ✅ Page responsive at all screen sizes
- ✅ Touch targets large enough (44x44px minimum)
- ✅ Text readable without zooming
- ✅ Form usable on mobile devices

### Test Results
- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 12: Browser Compatibility

### Objective
Verify that the account deletion page works across different browsers.

### Browsers to Test
- [ ] Google Chrome (latest)
- [ ] Mozilla Firefox (latest)
- [ ] Microsoft Edge (latest)
- [ ] Safari (latest) - if available
- [ ] Mobile Safari (iOS) - if available
- [ ] Chrome Mobile (Android) - if available

### Steps for Each Browser

1. **Navigate to Page**
   - Open: https://[username].github.io/[repo]/delete-account.html

2. **Test Basic Functionality**
   - Fill out form
   - Submit deletion request
   - Verify success/error messages display correctly

3. **Check Console for Errors**
   - Open developer console
   - Verify no JavaScript errors
   - Verify Firebase SDK loads correctly

4. **Test Styling**
   - Verify page renders correctly
   - Verify CSS styles applied
   - Verify responsive design works

### Expected Results
- ✅ Page works in all major browsers
- ✅ No browser-specific errors
- ✅ Consistent user experience across browsers

### Test Results

| Browser | Version | Result | Notes |
|---------|---------|--------|-------|
| Chrome  |         | [ ] PASS / [ ] FAIL | |
| Firefox |         | [ ] PASS / [ ] FAIL | |
| Edge    |         | [ ] PASS / [ ] FAIL | |
| Safari  |         | [ ] PASS / [ ] FAIL | |

---

## Test Scenario 13: Performance Testing

### Objective
Verify that the account deletion page loads quickly and performs well.

### Steps

1. **Run Lighthouse Audit**
   - Open page in Chrome
   - Open DevTools (F12)
   - Go to Lighthouse tab
   - Select "Performance", "Accessibility", "Best Practices", "SEO"
   - Click "Generate report"

2. **Check Performance Metrics**
   - First Contentful Paint (FCP): Target < 1.5s
   - Time to Interactive (TTI): Target < 3.0s
   - Total Blocking Time (TBT): Target < 200ms
   - Cumulative Layout Shift (CLS): Target < 0.1

3. **Check Page Size**
   - Open Network tab
   - Reload page
   - Check total page size: Target < 500KB

4. **Test on Slow Network**
   - Open Network tab
   - Throttle to "Slow 3G"
   - Reload page
   - Verify page loads within reasonable time

### Expected Results
- ✅ Lighthouse Performance score > 90
- ✅ Lighthouse Accessibility score > 90
- ✅ FCP < 1.5s
- ✅ TTI < 3.0s
- ✅ Total page size < 500KB

### Test Results

**Lighthouse Scores:**
- Performance: _____ / 100
- Accessibility: _____ / 100
- Best Practices: _____ / 100
- SEO: _____ / 100

**Performance Metrics:**
- FCP: _____ s
- TTI: _____ s
- Total Page Size: _____ KB

- [ ] PASS
- [ ] FAIL (Reason: _________________)

---

## Test Scenario 14: Security Audit

### Objective
Verify that security measures are properly implemented.

### Security Checklist

1. **HTTPS Enforcement**
   - [ ] Page served over HTTPS
   - [ ] No mixed content warnings
   - [ ] Valid SSL certificate

2. **Credential Protection**
   - [ ] No credentials logged to console
   - [ ] No credentials in localStorage
   - [ ] No credentials in sessionStorage (except rate limit data)
   - [ ] Password field uses type="password"

3. **Input Sanitization**
   - [ ] XSS prevention working
   - [ ] Input validation working
   - [ ] Malicious patterns detected and blocked

4. **Rate Limiting**
   - [ ] Rate limiting functional
   - [ ] Stored in sessionStorage (not localStorage)
   - [ ] Resets after 1 hour

5. **CSRF Protection**
   - [ ] Origin validation working
   - [ ] Referrer validation working

6. **Error Messages**
   - [ ] Don't reveal account existence
   - [ ] User-friendly and actionable
   - [ ] No sensitive data exposed

7. **Firebase Security**
   - [ ] Firebase config exposed (acceptable for client-side)
   - [ ] Security rules properly configured
   - [ ] Users can only delete own data

### Test Results
- [ ] PASS (All security checks passed)
- [ ] FAIL (Reason: _________________)

---

## Summary and Sign-Off

### Test Summary

**Total Test Scenarios:** 14  
**Passed:** _____  
**Failed:** _____  
**Pass Rate:** _____%

### Critical Issues Found

1. _______________________________________
2. _______________________________________
3. _______________________________________

### Recommendations

1. _______________________________________
2. _______________________________________
3. _______________________________________

### Sign-Off

**Tester Name:** _______________________  
**Date:** _______________________  
**Signature:** _______________________

**Approved for Production:**
- [ ] YES
- [ ] NO (Reason: _________________)

---

## Appendix A: Test Data

### Test Accounts Created

| Email | Password | Purpose | Status |
|-------|----------|---------|--------|
| test-deletion-[timestamp]@example.com | TestPass123! | Successful deletion | Deleted |
| test-wrong-password@example.com | CorrectPass123! | Wrong password test | Active |
| test-rate-limit@example.com | TestPass123! | Rate limiting test | Active |

### Firebase Collections Verified

- [x] users
- [x] messages
- [x] tasks
- [x] groupMembers

---

## Appendix B: Troubleshooting

### Common Issues and Solutions

**Issue:** Loading spinner doesn't appear  
**Solution:** Check that `loadingSpinner` element exists in HTML and CSS is loaded

**Issue:** Success message doesn't display  
**Solution:** Check that `statusMessage` element exists and JavaScript is loaded

**Issue:** Rate limiting not working  
**Solution:** Check sessionStorage is enabled in browser settings

**Issue:** Firebase errors  
**Solution:** Verify Firebase config is correct and project is active

**Issue:** CSRF protection blocking legitimate requests  
**Solution:** Verify page is accessed via correct GitHub Pages URL

---

## Appendix C: Quick Test Commands

### Clear Rate Limit (Browser Console)
```javascript
sessionStorage.clear();
location.reload();
```

### Check Rate Limit Status (Browser Console)
```javascript
console.log(sessionStorage.getItem('account_deletion_attempts'));
```

### Simulate Network Error (Browser Console)
```javascript
// Enable offline mode in Network tab
// Or use: navigator.onLine = false (may not work in all browsers)
```

---

**End of Functional Test Guide**
