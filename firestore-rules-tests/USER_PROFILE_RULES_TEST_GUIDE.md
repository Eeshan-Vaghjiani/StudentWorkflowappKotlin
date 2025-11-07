# User Profile Security Rules Testing Guide

This guide explains how to test the updated Firestore security rules for user profiles.

## What Was Updated

The Firestore security rules for the `users` collection have been updated to:

1. ✅ **Allow users to create their own profiles** (Requirement 3.1)
2. ✅ **Prevent users from creating profiles for other users** (Requirement 3.2)
3. ✅ **Allow all authenticated users to read any profile** (Requirement 3.3, 3.4)
4. ✅ **Prevent users from modifying other users' profiles** (Requirement 3.5)
5. ✅ **Prevent profile deletion** (Requirement 3.5)

## Test File

A comprehensive test suite has been created: `user-profile.test.js`

This test file includes 22 test cases covering:
- Profile creation permissions
- Profile read permissions
- Profile update permissions
- Profile deletion prevention

## How to Run the Tests

### Prerequisites

1. Firebase CLI must be installed:
```bash
npm install -g firebase-tools
```

2. Install test dependencies:
```bash
cd firestore-rules-tests
npm install
```

### Option 1: Run Tests with Emulator (Recommended)

**Step 1:** Start the Firebase Emulator in a separate terminal window:
```bash
firebase emulators:start --only firestore --project test-project
```

Wait for the message: `✔  firestore: Firestore Emulator logging to firestore-debug.log`

**Step 2:** In another terminal, run the user profile tests:
```bash
cd firestore-rules-tests
npm test -- user-profile.test.js
```

**Step 3:** Stop the emulator (Ctrl+C in the emulator terminal)

### Option 2: Run All Tests

To run all Firestore tests including the new user profile tests:

**Step 1:** Start the emulator (same as above)

**Step 2:** Run all tests:
```bash
cd firestore-rules-tests
npm test
```

## Expected Test Results

All 22 tests should pass:

```
PASS  ./user-profile.test.js
  User Profile Security Rules - Create Permissions
    ✓ user can create their own profile
    ✓ user can create profile with uid field (backward compatibility)
    ✓ user cannot create profile for another user
    ✓ user cannot create profile with mismatched userId
    ✓ user cannot create profile without displayName
    ✓ user cannot create profile without email
    ✓ user cannot create profile with displayName exceeding 100 characters
    ✓ user can create profile with optional fields
    ✓ unauthenticated user cannot create profile

  User Profile Security Rules - Read Permissions
    ✓ authenticated user can read any user profile
    ✓ authenticated user can read their own profile
    ✓ authenticated user can query all user profiles
    ✓ unauthenticated user cannot read user profiles

  User Profile Security Rules - Update Permissions
    ✓ user can update their own profile
    ✓ user cannot update another user profile
    ✓ user cannot change their userId when updating
    ✓ user cannot change their email when updating
    ✓ user can update optional fields
    ✓ unauthenticated user cannot update profile

  User Profile Security Rules - Delete Permissions
    ✓ user cannot delete their own profile
    ✓ user cannot delete another user profile
    ✓ unauthenticated user cannot delete profile

Test Suites: 1 passed, 1 total
Tests:       22 passed, 22 total
```

## Manual Testing (Alternative)

If you prefer to test manually without running automated tests:

1. Start the emulator with UI:
```bash
firebase emulators:start --only firestore
```

2. Open http://localhost:4000 in your browser

3. Navigate to the Firestore tab

4. Try creating user profile documents manually to verify the rules

## Troubleshooting

### Emulator Not Running

**Error:** `FetchError: request to http://localhost:8080/emulator/v1/projects/...`

**Solution:** Make sure the Firebase Emulator is running before executing tests.

### Port Already in Use

**Error:** `Port 8080 is already in use`

**Solution:** 
- Stop any other Firebase Emulator instances
- Or change the port in firebase.json

### Tests Timeout

**Solution:**
- Ensure your machine has sufficient resources
- Check emulator logs for errors
- Increase timeout in jest.config.js if needed

## Next Steps

After tests pass:

1. ✅ Rules are validated in the emulator
2. 🔄 Deploy rules to Firebase project (Task 7)
3. 🔄 Implement UserProfileRepository (Task 2-3)
4. 🔄 Integrate with authentication flow (Task 4)

## Requirements Validated

This test suite validates the following requirements:

- **Requirement 3.1**: Users can create their own profiles
- **Requirement 3.2**: Users cannot create profiles for other users
- **Requirement 3.3**: Authenticated users can read their own profile
- **Requirement 3.4**: Authenticated users can read other users' profiles
- **Requirement 3.5**: Users cannot update other users' profiles
- **Requirement 3.5**: Profile deletion is prevented

## Files Modified

- `firestore.rules` - Updated user profile security rules
- `firestore-rules-tests/user-profile.test.js` - New comprehensive test suite
