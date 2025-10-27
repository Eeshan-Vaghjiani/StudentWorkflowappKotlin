# Firestore Rules Testing

This directory contains tests for Firebase Firestore security rules using the Firebase Emulator.

## Setup

1. Install dependencies:
```bash
cd firestore-rules-tests
npm install
```

2. Make sure Firebase CLI is installed globally:
```bash
npm install -g firebase-tools
```

## Running Tests

### Option 1: Automated Tests (Requires Emulator)

**Step 1:** Start the Firebase Emulator in a separate terminal:
```bash
firebase emulators:start --only firestore --project test-project
```

Wait for the message: `✔  firestore: Firestore Emulator logging to firestore-debug.log`

**Step 2:** In another terminal, run the tests:
```bash
cd firestore-rules-tests
npm test
```

**Step 3:** Stop the emulator (Ctrl+C in the emulator terminal)

### Option 2: Use the Test Runner Script (Windows)

```bash
cd firestore-rules-tests
run-tests.bat
```

This script will:
1. Start the emulator in the background
2. Wait for it to initialize
3. Run the tests
4. Stop the emulator

### Option 3: Manual Testing (No Automation Required)

If you prefer to test manually or the automated tests have issues:

1. Start the emulator with UI:
```bash
firebase emulators:start --only firestore
```

2. Open http://localhost:4000 in your browser

3. Follow the step-by-step guide in `MANUAL_TEST_GUIDE.md`

## Test Coverage

The test suite covers:

### Groups Collection
- ✅ User can read groups they are a member of
- ✅ User can query groups using array-contains filter
- ✅ User cannot read groups they are not a member of
- ✅ Unauthenticated users cannot read groups
- ✅ User can create group with themselves as member and owner
- ✅ User can create group with multiple members
- ✅ User cannot create group without themselves as member
- ✅ User cannot create group with different owner

### Tasks Collection
- ✅ User can read tasks they created
- ✅ User can read tasks they are assigned to
- ✅ User can query tasks using userId filter
- ✅ User can query tasks using assignedTo filter
- ✅ User cannot read tasks they don't own or aren't assigned to

### Chats Collection
- ✅ User can read chats they are a participant in
- ✅ User can query chats using participants filter
- ✅ User cannot read chats they are not a participant in

### Group Activities
- ✅ User can read activities from groups they are members of
- ✅ User cannot read activities from groups they are not members of

### Permission Denied Scenarios
- ✅ Querying with proper filters returns empty results (not errors) when no data matches
- ✅ Verifies that permission denied scenarios are handled gracefully

## Test Structure

Each test follows this pattern:

1. **Setup**: Create test data with security rules disabled
2. **Test**: Attempt operation with authenticated user context
3. **Assert**: Verify operation succeeds or fails as expected

## Requirements Covered

- **Requirement 4.1**: Rules tested against Firebase Emulator before deployment
- **Requirement 4.2**: All critical user flows validated (groups, tasks, chats)
- **Requirement 4.3**: Rule changes validated to not break existing functionality

## Troubleshooting

### Emulator Connection Issues

If tests fail to connect to the emulator:
1. Ensure the emulator is running on port 8080
2. Check that no other process is using port 8080
3. Try restarting the emulator

### Test Timeouts

If tests timeout:
1. Increase the timeout in jest.config.js
2. Check emulator logs for errors
3. Ensure your machine has sufficient resources

### Permission Errors in Tests

If tests fail with permission errors:
1. Verify the firestore.rules file is in the parent directory
2. Check that the rules syntax is correct
3. Review the test setup to ensure data is created correctly
