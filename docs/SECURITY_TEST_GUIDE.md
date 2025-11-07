# Security Testing Guide

## Overview

This guide provides comprehensive instructions for performing security testing on the Play Store compliance pages (Privacy Policy and Account Deletion pages). These tests verify compliance with requirements 6.1-6.8 and 3.9.

## Test Environment

- **Test Page**: `docs/test-security.html`
- **Target Pages**: 
  - `docs/privacy-policy.html`
  - `docs/delete-account.html`
- **Requirements Tested**: 6.1, 6.2, 6.3, 6.4, 6.5, 6.7, 6.8, 3.9

## Quick Start

1. Open `docs/test-security.html` in a web browser
2. Click "Run All Tests" to execute all security tests
3. Review the results for each test section
4. Address any failures or warnings

## Test Sections

### 1. HTTPS Enforcement (Requirement 6.1)

**Purpose**: Verify that HTTPS is enforced and HTTP connections are not allowed.

**Test Method**:
- Checks the current page protocol
- Validates that the connection is secure

**Expected Results**:
- ✅ **PASS**: Protocol is `https://`
- ⚠️ **WARNING**: Protocol is `http://` on localhost (acceptable for development)
- ❌ **FAIL**: Protocol is `http://` on production domain

**Manual Verification**:
1. Open the delete account page in a browser
2. Check the address bar for the lock icon (🔒)
3. Click the lock icon to view certificate details
4. Verify the certificate is valid and issued by a trusted authority

**GitHub Pages Note**: GitHub Pages automatically enforces HTTPS for all pages.


### 2. Credential Logging Prevention (Requirement 6.2)

**Purpose**: Confirm no credentials are logged to browser console.

**Test Method**:
- Intercepts console.log, console.error, and console.warn
- Checks for sensitive patterns (password, credential, token, email, etc.)
- Monitors console output during page operations

**Expected Results**:
- ✅ **PASS**: No sensitive data found in console logs
- ❌ **FAIL**: Credentials or sensitive data detected in console

**Manual Verification**:
1. Open browser Developer Tools (F12)
2. Go to the Console tab
3. Clear the console
4. Navigate to the delete account page
5. Fill in the form with test credentials
6. Submit the form (use invalid credentials to avoid actual deletion)
7. Review all console messages
8. Verify that:
   - No passwords are logged
   - No email addresses are logged
   - No authentication tokens are logged
   - Only generic security events are logged (e.g., "Authentication Failed")

**Security Logger Behavior**:
The SecurityLogger module filters out sensitive data:
- Passwords are never logged
- Email addresses are removed from logs
- User IDs are removed from logs
- Only event types and non-sensitive metadata are logged

### 3. Storage Security (Requirement 6.3)

**Purpose**: Verify no sensitive data stored in localStorage or sessionStorage (except rate limit).

**Test Method**:
- Scans localStorage for sensitive keys/values
- Scans sessionStorage for sensitive keys/values
- Allows `account_deletion_attempts` in sessionStorage (rate limiting)

**Expected Results**:
- ✅ **PASS**: No sensitive data in storage (except allowed rate limit data)
- ❌ **FAIL**: Sensitive data found in localStorage or sessionStorage

**Manual Verification**:
1. Open browser Developer Tools (F12)
2. Go to the Application tab (Chrome) or Storage tab (Firefox)
3. Expand "Local Storage" and select your domain
4. Verify no sensitive data is stored
5. Expand "Session Storage" and select your domain
6. Verify only `account_deletion_attempts` is present (if any)
7. Check the value of `account_deletion_attempts`:
   - Should be an array of timestamps
   - Should not contain any personal information

**Allowed Storage**:
- `sessionStorage.account_deletion_attempts`: Array of attempt timestamps (cleared when browser closes)

**Prohibited Storage**:
- Passwords
- Email addresses
- Authentication tokens
- User IDs
- Any personal information

### 4. XSS Prevention (Requirement 6.7)

**Purpose**: Test XSS prevention by entering script tags in form fields.

**Test Method**:
- Tests multiple XSS payloads
- Verifies that dangerous patterns are sanitized
- Checks HTML encoding and script blocking

**XSS Payloads Tested**:
```javascript
'<script>alert("XSS")</script>'
'javascript:alert("XSS")'
'<img src=x onerror=alert("XSS")>'
'<iframe src="javascript:alert(\'XSS\')">'
'onclick=alert("XSS")'
'<svg onload=alert("XSS")>'
```

**Expected Results**:
- ✅ **PASS**: All XSS payloads are properly sanitized
- ❌ **FAIL**: Some XSS payloads were not sanitized

**Manual Verification**:
1. Navigate to the delete account page
2. Enter the following in the email field:
   ```
   <script>alert('XSS')</script>
   ```
3. Verify that:
   - No alert popup appears
   - The script tags are escaped or removed
   - The form validation shows an error for invalid email
4. Try other XSS payloads in both email and password fields
5. Verify that none of them execute

**Input Sanitization**:
The InputSanitizer module provides:
- HTML entity encoding
- Script tag removal
- JavaScript protocol removal
- Event handler attribute removal
- Null byte removal

### 5. Firebase Config Exposure (Requirement 6.8)

**Purpose**: Verify Firebase config is exposed (acceptable for client-side apps).

**Test Method**:
- Attempts to import Firebase configuration module
- Checks if auth and db instances are available
- Confirms configuration is accessible

**Expected Results**:
- ✅ **PASS**: Firebase config is accessible (this is expected and acceptable)
- ⚠️ **WARNING**: Could not load Firebase module (may be expected on test page)
- ❌ **FAIL**: Firebase not properly initialized

**Manual Verification**:
1. Open browser Developer Tools (F12)
2. Go to the Sources tab
3. Navigate to `assets/js/firebase-config.js`
4. Verify that the Firebase configuration is visible:
   ```javascript
   const firebaseConfig = {
     apiKey: "...",
     authDomain: "...",
     projectId: "...",
     // etc.
   };
   ```
5. This is **acceptable and expected** for client-side Firebase apps
6. Verify in Firebase Console that:
   - API key restrictions are configured
   - Security rules protect data access
   - Only authorized domains are allowed

**Why This Is Acceptable**:
- Firebase API keys are not secret keys
- They identify your Firebase project
- Security is enforced through:
  - Firebase Security Rules
  - Domain restrictions in Firebase Console
  - Authentication requirements

### 6. CSRF Protection (Requirement 6.4)

**Purpose**: Test CSRF protection by attempting requests from different origins.

**Test Method**:
- Validates current page origin
- Checks if origin is in allowed list
- Verifies referrer validation

**Expected Results**:
- ✅ **PASS**: Valid origin detected (localhost or GitHub Pages)
- ⚠️ **WARNING**: Origin may not be in allowed list (custom domain)
- ❌ **FAIL**: CSRF protection not active

**Manual Verification**:
1. Open the delete account page normally
2. Verify the form works as expected
3. Try to create a form on a different domain that submits to your delete account page:
   ```html
   <form action="https://yourdomain.github.io/yourrepo/delete-account.html" method="POST">
     <input name="email" value="test@example.com">
     <input name="password" value="password123">
     <button type="submit">Submit</button>
   </form>
   ```
4. Verify that the CSRF protection blocks the request
5. Check the console for CSRF validation errors

**CSRF Protection Mechanisms**:
- Origin validation (checks window.location.origin)
- Referrer validation (checks document.referrer)
- Same-origin policy enforcement
- Allowed origins:
  - localhost (development)
  - *.github.io (GitHub Pages)
  - Custom domains (if configured)

### 7. Error Message Security (Requirement 6.5)

**Purpose**: Verify error messages don't reveal whether accounts exist.

**Test Method**:
- Tests error messages for different authentication failures
- Verifies that messages are ambiguous
- Checks for account enumeration prevention

**Test Cases**:
- `auth/user-not-found` → "Invalid email or password"
- `auth/wrong-password` → "Invalid email or password"
- `auth/invalid-credential` → "Invalid email or password"

**Expected Results**:
- ✅ **PASS**: All error messages are ambiguous
- ❌ **FAIL**: Some error messages reveal account existence

**Manual Verification**:
1. Navigate to the delete account page
2. Enter a non-existent email address and any password
3. Submit the form
4. Verify the error message is: "Invalid email or password"
5. Enter a valid email address (if you have a test account) with wrong password
6. Submit the form
7. Verify the error message is the same: "Invalid email or password"
8. Confirm that you cannot determine whether an account exists based on the error message

**Security Benefit**:
- Prevents account enumeration attacks
- Attackers cannot determine which email addresses have accounts
- Protects user privacy

### 8. Rate Limiting (Requirement 3.9)

**Purpose**: Test rate limiting functionality.

**Test Method**:
- Checks sessionStorage for rate limit data
- Validates attempt tracking
- Verifies time window enforcement

**Rate Limit Configuration**:
- Maximum attempts: 3 per hour
- Time window: 3600000 ms (1 hour)
- Storage: sessionStorage (cleared when browser closes)

**Expected Results**:
- ✅ **PASS**: Rate limiting is active and working
- ❌ **FAIL**: Rate limiting not configured or not working

**Manual Verification**:
1. Navigate to the delete account page
2. Submit the form with invalid credentials 3 times
3. On the 4th attempt, verify that:
   - The form submission is blocked
   - An error message appears: "Too many deletion attempts. Please try again in X minutes."
   - The rate limit is enforced
4. Check sessionStorage for `account_deletion_attempts`
5. Verify it contains an array of 3 timestamps
6. Wait for the time window to expire (or clear sessionStorage)
7. Verify that you can attempt again

**Rate Limit Reset**:
- Automatic: After 1 hour from the oldest attempt
- Manual: Clear sessionStorage or close browser

**Testing Rate Limit**:
1. Click "Run Rate Limiting Test" on the test page
2. Click it 3 times to reach the limit
3. Verify the rate limit is triggered
4. Click "Clear Rate Limit" to reset for further testing

## Test Results Interpretation

### Pass (✅)
- The security control is working as expected
- No action required
- Document the passing test in your compliance checklist

### Warning (⚠️)
- The test detected a non-critical issue
- Review the details to determine if action is needed
- May be expected behavior in certain environments (e.g., localhost)

### Fail (❌)
- The security control is not working properly
- **Action required**: Fix the issue before deployment
- Review the code and implementation
- Re-run the test after fixing

## Automated Testing

Run all tests automatically:
```bash
# Open test page in browser
open docs/test-security.html

# Or use a specific browser
chrome docs/test-security.html
firefox docs/test-security.html
```

## Manual Testing Checklist

Use this checklist to verify all security requirements:

- [ ] **HTTPS Enforcement**
  - [ ] Page loads with https:// protocol
  - [ ] Lock icon visible in address bar
  - [ ] Valid SSL certificate

- [ ] **Credential Logging**
  - [ ] No passwords in console logs
  - [ ] No email addresses in console logs
  - [ ] No tokens in console logs
  - [ ] Only generic security events logged

- [ ] **Storage Security**
  - [ ] No sensitive data in localStorage
  - [ ] Only rate limit data in sessionStorage
  - [ ] Rate limit data contains only timestamps

- [ ] **XSS Prevention**
  - [ ] Script tags are sanitized
  - [ ] Event handlers are removed
  - [ ] JavaScript protocols are blocked
  - [ ] No XSS payloads execute

- [ ] **Firebase Config**
  - [ ] Firebase config is accessible (expected)
  - [ ] API key restrictions configured in Firebase Console
  - [ ] Security rules protect data access

- [ ] **CSRF Protection**
  - [ ] Origin validation is active
  - [ ] Referrer validation is active
  - [ ] Cross-origin requests are blocked

- [ ] **Error Messages**
  - [ ] Authentication errors are ambiguous
  - [ ] Cannot determine account existence from errors
  - [ ] All auth errors use same message

- [ ] **Rate Limiting**
  - [ ] Rate limit enforces 3 attempts per hour
  - [ ] Rate limit blocks after max attempts
  - [ ] Rate limit resets after time window
  - [ ] Rate limit data stored in sessionStorage only

## Troubleshooting

### Test Page Not Loading
- Verify the file path is correct
- Check browser console for errors
- Ensure all CSS and JS files are present

### Firebase Tests Failing
- Verify Firebase is initialized on the page
- Check Firebase configuration in `firebase-config.js`
- Ensure Firebase SDK is loaded from CDN

### Rate Limit Not Working
- Clear sessionStorage and try again
- Check browser console for errors
- Verify sessionStorage is enabled in browser

### CSRF Tests Failing
- Check the current origin
- Verify origin is in allowed list
- Add custom domain to CSRFProtection if needed

## Reporting

After completing all tests, document the results:

1. **Test Summary**:
   - Total tests run
   - Tests passed
   - Tests failed
   - Warnings

2. **Failed Tests**:
   - List each failed test
   - Describe the issue
   - Document the fix applied
   - Re-test and verify

3. **Compliance Status**:
   - Confirm all requirements are met
   - Document any exceptions or warnings
   - Include screenshots of passing tests

## Next Steps

After all security tests pass:

1. ✅ Mark task 21 as complete in tasks.md
2. ✅ Proceed to task 22: Performance testing
3. ✅ Document test results for Play Store submission
4. ✅ Include security test results in compliance documentation

## References

- **Requirements**: See `.kiro/specs/play-store-compliance-pages/requirements.md`
- **Design**: See `.kiro/specs/play-store-compliance-pages/design.md`
- **Implementation**: See `docs/assets/js/delete-account.js`
- **Firebase Security**: See Firebase Console → Security Rules

## Contact

For questions or issues with security testing:
- Review the design document for security architecture
- Check the implementation code for security controls
- Consult Firebase documentation for authentication security
