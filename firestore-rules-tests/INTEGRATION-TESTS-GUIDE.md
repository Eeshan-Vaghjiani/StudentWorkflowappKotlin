# Integration Tests Quick Start Guide

## Overview

The integration tests (`integration.test.js`) validate complete end-to-end workflows across Firebase Security Rules for both Firestore and Storage.

## What Do Integration Tests Cover?

### 1. Group Workflows (3 tests)
- ✅ Create group → Create group task → Verify member access
- ✅ Create public group → Verify non-member can discover it
- ✅ Add user to group → Verify they gain access to group data

### 2. Chat Workflows (3 tests)
- ✅ Create chat → Send message → Verify participant access
- ✅ Test typing indicators work correctly
- ✅ Test message validation prevents invalid content

### 3. File Upload Workflows (3 tests)
- ✅ Upload profile picture → Verify URL validation
- ✅ Upload chat attachment → Verify type and size validation
- ✅ Test invalid uploads are rejected with clear errors

## Running Integration Tests

### Prerequisites

1. Firebase emulators must be running
2. Node.js and npm installed
3. Dependencies installed (`npm install` in firestore-rules-tests directory)

### Option 1: Manual Emulator Start (Recommended for Development)

**Terminal 1** - Start emulators:
```bash
firebase emulators:start
```

Keep this terminal running. You should see:
```
✔  firestore: Firestore Emulator running on port 8080
✔  storage: Storage Emulator running on port 9199
✔  ui: Emulator UI running on port 4000
```

**Terminal 2** - Run integration tests:
```bash
cd firestore-rules-tests
npm test -- integration.test.js --testTimeout=30000
```

### Option 2: Automatic Emulator Management (CI/CD)

This command automatically starts emulators, runs tests, and stops emulators:

```bash
firebase emulators:exec --only firestore,storage "cd firestore-rules-tests && npm test -- integration.test.js --testTimeout=30000"
```

### Option 3: Run All Tests (Unit + Integration)

```bash
# Terminal 1: Start emulators
firebase emulators:start

# Terminal 2: Run all tests
cd firestore-rules-tests
npm test
```

## Expected Results

When all integration tests pass:

```
Test Suites: 1 passed, 1 total
Tests:       9 passed, 9 total
Snapshots:   0 total
Time:        ~15-20s
```

### Test Breakdown
- 3 Group workflow tests
- 3 Chat workflow tests
- 3 File upload workflow tests
- **Total: 9 integration tests**

## What Each Test Validates

### Group Workflow Test 1: Create → Task → Access
```javascript
✓ User1 creates group with user2
✓ User1 creates group task
✓ User2 (member) can access group and task
✓ User3 (non-member) cannot access group or task
```

### Group Workflow Test 2: Public Discovery
```javascript
✓ User1 creates public group
✓ User3 (non-member) can read public group
✓ User3 can query for public groups
✓ User3 cannot access private groups
```

### Group Workflow Test 3: Dynamic Membership
```javascript
✓ User3 initially cannot access group/tasks
✓ User1 adds user3 to group
✓ User3 can now access group and tasks
✓ User3 can create tasks in the group
✓ Other members can see user3's tasks
```

### Chat Workflow Test 1: Create → Message → Access
```javascript
✓ User1 creates chat with user2
✓ User1 sends message
✓ User2 can read chat and message
✓ User2 sends reply
✓ User3 (non-participant) cannot access chat or messages
```

### Chat Workflow Test 2: Typing Indicators
```javascript
✓ User1 sets typing status
✓ User2 can read user1's typing status
✓ User1 cannot set user2's typing status
✓ User3 (non-participant) cannot read or set typing status
```

### Chat Workflow Test 3: Message Validation
```javascript
✓ Empty messages rejected
✓ Messages > 10,000 chars rejected
✓ Valid text messages accepted
✓ External URLs rejected
✓ Firebase Storage URLs accepted
✓ Spoofed senderId rejected
```

### File Upload Test 1: Profile Picture
```javascript
✓ User1 uploads valid profile picture
✓ User1 creates profile with Firebase Storage URL
✓ External URL updates rejected
✓ Valid Firebase Storage URL updates accepted
✓ Empty string (clear picture) accepted
```

### File Upload Test 2: Chat Attachments
```javascript
✓ User1 uploads image attachment
✓ User1 sends message with image URL
✓ User1 uploads PDF document
✓ User1 sends message with document URL
✓ User2 can read messages with attachments
```

### File Upload Test 3: Invalid Uploads
```javascript
✓ Non-image to profile folder rejected
✓ File > 5MB to profile folder rejected
✓ File > 10MB to chat attachments rejected
✓ Executable files rejected
✓ Upload to another user's folder rejected
✓ Upload to invalid path rejected
✓ Valid uploads still work
```

## Troubleshooting

### Error: "FetchError: request to http://localhost:8080/... failed"

**Problem**: Emulators are not running

**Solution**: Start emulators in a separate terminal:
```bash
firebase emulators:start
```

### Error: "Timeout - Async callback was not invoked"

**Problem**: Tests are taking too long (default timeout is 5 seconds)

**Solution**: Use the `--testTimeout` flag:
```bash
npm test -- integration.test.js --testTimeout=30000
```

### Error: "Port already in use"

**Problem**: Another process is using emulator ports

**Solution**: 
1. Stop any running Firebase emulators
2. Or kill the process using the port:
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   ```

### Tests Fail After Rule Changes

**Problem**: Emulators are using cached rules

**Solution**: Restart the emulators to reload rules:
```bash
# Stop emulators (Ctrl+C)
# Start again
firebase emulators:start
```

## Integration vs Unit Tests

### Unit Tests (firestore.test.js, storage.test.js)
- Test individual rules in isolation
- Focus on specific permissions and validations
- Fast execution (~5-10 seconds)
- 117 tests total

### Integration Tests (integration.test.js)
- Test complete user workflows
- Validate cross-collection interactions
- Test realistic multi-step scenarios
- Slower execution (~15-20 seconds)
- 9 tests total

### When to Run Each

**During Development**:
- Run unit tests frequently for quick feedback
- Run integration tests before committing changes

**Before Deployment**:
- Run all tests (unit + integration)
- Verify all 126 tests pass

**In CI/CD**:
- Run all tests automatically on every commit
- Use `firebase emulators:exec` for automatic emulator management

## Next Steps

After running integration tests successfully:

1. ✅ All workflows validated end-to-end
2. ✅ Ready to proceed with client-side error handling (Task 15)
3. ✅ Ready to create documentation (Task 16)

## Additional Resources

- [Firebase Rules Unit Testing](https://firebase.google.com/docs/rules/unit-tests)
- [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite)
- [Jest Testing Framework](https://jestjs.io/)
