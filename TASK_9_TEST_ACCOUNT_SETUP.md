# Task 9: Test Account Setup Guide

## Overview
This guide explains how to set up test accounts in Firebase Authentication for testing the chat functionality with two users.

## Option 1: Create Test Accounts in Firebase Console (Recommended)

### Step 1: Access Firebase Console
1. Go to https://console.firebase.google.com
2. Select your project
3. Click "Authentication" in the left sidebar
4. Click the "Users" tab

### Step 2: Create Test User 1
1. Click "Add user" button
2. Enter email: `testuser1@example.com`
3. Enter password: `TestPassword123!`
4. Click "Add user"
5. **Note the User UID** - you'll need this for verification

### Step 3: Create Test User 2
1. Click "Add user" button again
2. Enter email: `testuser2@example.com`
3. Enter password: `TestPassword123!`
4. Click "Add user"
5. **Note the User UID** - you'll need this for verification

### Step 4: Verify Accounts
- [ ] Both accounts appear in the Users list
- [ ] Both accounts show "Email/Password" as the provider
- [ ] Both accounts are enabled (not disabled)

---

## Option 2: Use Real Google Accounts

If you prefer to use real Google accounts for more realistic testing:

### Requirements
- Two different Google accounts you have access to
- Both accounts must be able to sign in to the app

### Setup
1. Use your primary Google account as User 1
2. Use a secondary Google account as User 2
   - This could be a work account, alternate personal account, or test account
3. Make sure both accounts can access the app

### Advantages
- More realistic testing scenario
- Tests actual Google Sign-In flow
- Profile photos and names are real

### Disadvantages
- Requires two separate Google accounts
- May clutter your personal accounts with test data

---

## Option 3: Use Firebase Emulator (Advanced)

For local testing without affecting production data:

### Step 1: Install Firebase Emulator
```bash
npm install -g firebase-tools
firebase login
```

### Step 2: Initialize Emulator
```bash
firebase init emulators
# Select Authentication and Firestore
```

### Step 3: Start Emulator
```bash
firebase emulators:start
```

### Step 4: Configure App
Update your app to connect to the emulator:

```kotlin
// In your Application class or test setup
if (BuildConfig.DEBUG) {
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
    FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
}
```

### Step 5: Create Test Users
Use the Emulator UI (http://localhost:4000) to create test users

---

## Automated Test Setup

If you want to run the automated integration test (`ChatFunctionalityIntegrationTest`), you MUST create the test accounts with these exact credentials:

### Required Test Accounts
- **User 1:**
  - Email: `testuser1@example.com`
  - Password: `TestPassword123!`

- **User 2:**
  - Email: `testuser2@example.com`
  - Password: `TestPassword123!`

### Create via Firebase Console
Follow Option 1 above with these exact credentials.

### Create via Firebase CLI
```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Create test users (requires Firebase Admin SDK)
# You'll need to create a Node.js script or use the console
```

---

## Verification Steps

After creating test accounts:

### 1. Verify in Firebase Console
- [ ] Go to Firebase Console > Authentication > Users
- [ ] Both test accounts are listed
- [ ] UIDs are visible and noted

### 2. Test Sign-In
- [ ] Open the app
- [ ] Sign in with testuser1@example.com
- [ ] Sign-in succeeds
- [ ] Sign out
- [ ] Sign in with testuser2@example.com
- [ ] Sign-in succeeds

### 3. Verify Profile Creation
- [ ] Go to Firebase Console > Firestore Database
- [ ] Open `users` collection
- [ ] Profile document exists for User 1
- [ ] Profile document exists for User 2

---

## Security Considerations

### For Production Testing
- ⚠️ **Do NOT use these test accounts in production**
- ⚠️ **Use a separate Firebase project for testing**
- ⚠️ **Delete test accounts after testing is complete**

### For Development Testing
- ✅ Use Firebase Emulator for local testing
- ✅ Use a separate Firebase project for development
- ✅ Keep test credentials secure (don't commit to git)

### Best Practices
1. Use environment-specific Firebase projects:
   - `myapp-dev` for development
   - `myapp-staging` for testing
   - `myapp-prod` for production

2. Use Firebase App Distribution for beta testing with real users

3. Clean up test data regularly:
   ```bash
   # Delete test users after testing
   # Use Firebase Console or Admin SDK
   ```

---

## Troubleshooting

### Issue: "Email already in use"
**Solution:** The test account already exists. Either:
- Use the existing account
- Delete the existing account and recreate it
- Use different email addresses

### Issue: "Weak password"
**Solution:** Firebase requires passwords to be at least 6 characters. Use `TestPassword123!` or similar.

### Issue: "Cannot sign in with test account"
**Solution:** 
1. Verify Email/Password authentication is enabled in Firebase Console
2. Check that the account is not disabled
3. Verify you're using the correct password

### Issue: "Test accounts not appearing in Firestore"
**Solution:**
1. Sign in with each account in the app first
2. The profile is created during sign-in, not when the account is created
3. Check that `ensureUserProfileExists()` is being called in Login.kt

---

## Test Account Information Template

Use this template to track your test accounts:

```
Test Account 1:
- Email: testuser1@example.com
- Password: TestPassword123!
- UID: _______________________
- Display Name: Test User 1
- Created: ___________

Test Account 2:
- Email: testuser2@example.com
- Password: TestPassword123!
- UID: _______________________
- Display Name: Test User 2
- Created: ___________

Firebase Project: _______________________
Environment: [ ] Development [ ] Staging [ ] Production
```

---

## Next Steps

After setting up test accounts:

1. ✅ Verify both accounts can sign in to the app
2. ✅ Verify profiles are created in Firestore
3. ✅ Proceed with Task 9 manual testing
4. ✅ Or run automated test: `./gradlew connectedAndroidTest --tests ChatFunctionalityIntegrationTest`

---

## Cleanup After Testing

### Delete Test Accounts
1. Go to Firebase Console > Authentication > Users
2. Find testuser1@example.com
3. Click the three dots menu > Delete user
4. Repeat for testuser2@example.com

### Delete Test Data
1. Go to Firebase Console > Firestore Database
2. Delete user profile documents
3. Delete test chat documents
4. Delete test message documents

### Or Use Firebase CLI
```bash
# Delete all data in Firestore (use with caution!)
firebase firestore:delete --all-collections --project your-project-id
```

**⚠️ WARNING: Only use cleanup commands on test/development projects, never on production!**
