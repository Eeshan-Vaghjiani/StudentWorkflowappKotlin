# Firebase Rules Testing Guide

This directory contains comprehensive unit tests for Firebase Security Rules (Firestore and Storage).

## Prerequisites

1. **Firebase CLI** - Already installed (version 14.20.0)
2. **Node.js and npm** - Required for running tests
3. **Firebase Emulators** - Configured in `firebase.json`

## Test Structure

### Test Files

- `firestore.test.js` - Unit tests for Firestore Security Rules
  - Authentication rules
  - Groups collection (public discovery, member limits)
  - Tasks collection (group membership, assignment limits)
  - Chats collection (participant limits)
  - Messages subcollection (content validation, access control)
  - Typing status subcollection
  - Notifications collection
  - Group activities collection
  - Users collection (profile picture URL validation)

- `storage.test.js` - Unit tests for Storage Security Rules
  - Profile pictures (file type, size validation)
  - Chat attachments (file type, size validation)
  - Invalid path protection

- `integration.test.js` - Integration tests for complete workflows
  - Group workflow: Create group → Create task → Verify member access
  - Public group discovery workflow
  - Add user to group workflow
  - Chat workflow: Create chat → Send message → Verify access
  - Typing indicators workflow
  - Message validation workflow
  - Profile picture upload workflow
  - Chat attachment upload workflow
  - Invalid upload rejection workflow

### Test Coverage

The tests cover all requirements from the Firebase Rules Audit spec:

1. **Requirement 1**: Chat message participants denormalization
2. **Requirement 2**: Public group discovery
3. **Requirement 3**: Profile picture URL validation
4. **Requirement 4**: Task group membership validation
5. **Requirement 5**: Typing status participants check
6. **Requirement 6**: File size and type validation
7. **Requirement 7**: Chat participant limit validation
8. **Requirement 8**: Group member limit validation
9. **Requirement 9**: Notification write restrictions
10. **Requirement 10**: Task assignment validation
11. **Requirement 11**: Message content validation
12. **Requirement 12**: Group activity access control

## Running the Tests

### Step 1: Start Firebase Emulators

Open a **separate terminal** and start the emulators:

```bash
firebase emulators:start
```

This will start:
- Firestore Emulator on port 8080
- Storage Emulator on port 9199
- Emulator UI on port 4000

**Important**: Keep this terminal running while you execute the tests.

### Step 2: Run Tests

In a **new terminal**, navigate to the test directory and run the tests:

```bash
cd firestore-rules-tests
npm test
```

### Running Specific Test Files

To run only Firestore unit tests:
```bash
npm test firestore.test.js
```

To run only Storage unit tests:
```bash
npm test storage.test.js
```

To run only Integration tests:
```bash
npm test integration.test.js --testTimeout=30000
```

### Watch Mode

To run tests in watch mode (re-runs on file changes):
```bash
npm run test:watch
```

## Test Results

All tests should pass if:
1. Firebase emulators are running
2. Firestore rules are correctly deployed (from `../firestore.rules`)
3. Storage rules are correctly deployed (from `../storage.rules`)

### Expected Output

When all tests pass, you should see:
```
Test Suites: 3 passed, 3 total
Tests:       126 passed, 126 total
```

(117 unit tests + 9 integration tests = 126 total tests)

## Troubleshooting

### Error: "FetchError: request to http://localhost:8080/... failed"

**Cause**: Firebase emulators are not running.

**Solution**: Start the emulators in a separate terminal:
```bash
firebase emulators:start
```

### Error: "Port already in use"

**Cause**: Another process is using the emulator ports.

**Solution**: 
1. Stop any running Firebase emulators
2. Or change the ports in `firebase.json`

### Tests Failing Due to Rule Changes

**Cause**: The deployed rules don't match the expected behavior.

**Solution**: 
1. Verify `firestore.rules` and `storage.rules` are up to date
2. Restart the emulators to reload the rules
3. Check the Firebase Emulator UI at http://localhost:4000

## Test Categories

### Unit Tests (firestore.test.js, storage.test.js)

#### Authentication Tests
- Unauthenticated access denial
- Authenticated user access to own data
- Prevention of unauthorized access to other users' data

#### Groups Tests
- Public group discovery
- Private group access control
- Member limit enforcement (1-100 members)
- Owner must remain in memberIds

#### Tasks Tests
- Group membership validation for group tasks
- Assignment limit enforcement (1-50 assignees)
- Personal vs group task access control

#### Chats Tests
- Participant-based access control
- DIRECT chat: exactly 2 participants
- GROUP chat: 2-100 participants
- Participant array immutability

#### Messages Tests
- Participant-based access control (via parent chat)
- Content validation (text, imageUrl, documentUrl)
- Text length limit (10,000 characters)
- Firebase Storage URL validation
- Sender ID verification

#### Storage Tests
- Profile pictures: image types only, 5MB limit
- Chat attachments: images and documents, 10MB limit
- File type validation
- Invalid path protection

### Integration Tests (integration.test.js)

#### Group Workflow Tests
- Complete workflow: Create group → Create task → Verify member access
- Public group discovery and querying
- Dynamic membership: Add user → Verify access to group data

#### Chat Workflow Tests
- Complete workflow: Create chat → Send message → Verify participant access
- Typing indicators: Set status → Read status → Verify access control
- Message validation: Test content rules and URL validation

#### File Upload Workflow Tests
- Profile picture upload with URL validation in Firestore
- Chat attachment upload with type and size validation
- Invalid upload rejection with comprehensive error scenarios

## Continuous Integration

To integrate these tests into CI/CD:

1. Install Firebase CLI in CI environment
2. Start emulators before running tests:
   ```bash
   firebase emulators:exec --only firestore,storage "npm test"
   ```
3. This command automatically starts emulators, runs tests, and stops emulators

## Additional Resources

- [Firebase Rules Unit Testing](https://firebase.google.com/docs/rules/unit-tests)
- [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite)
- [Jest Documentation](https://jestjs.io/docs/getting-started)
