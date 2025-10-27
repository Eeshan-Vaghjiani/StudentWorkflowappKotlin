# Task 8: Visual Guide

## Test Infrastructure Overview

```
Project Root
│
├── firestore.rules                    # Rules being tested
├── firebase.json                      # Emulator configuration
│
├── firestore-rules-tests/             # Test directory
│   ├── firestore.test.js              # 21 test cases
│   ├── package.json                   # Dependencies
│   ├── jest.config.js                 # Test config
│   ├── README.md                      # Setup guide
│   ├── MANUAL_TEST_GUIDE.md           # Manual testing
│   ├── run-tests.bat                  # Windows runner
│   └── run-tests.sh                   # Linux/Mac runner
│
└── Documentation/
    ├── TASK_8_TEST_EXECUTION_REPORT.md
    ├── TASK_8_QUICK_START.md
    ├── TASK_8_COMPLETION_SUMMARY.md
    ├── TASK_8_VERIFICATION_CHECKLIST.md
    └── TASK_8_VISUAL_GUIDE.md (this file)
```

## Test Execution Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Execution Flow                       │
└─────────────────────────────────────────────────────────────┘

Step 1: Start Emulator
┌──────────────────────────────────────┐
│ firebase emulators:start             │
│   --only firestore                   │
│   --project test-project             │
└──────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Emulator Running on Port 8080        │
│ UI Available on Port 4000            │
└──────────────────────────────────────┘
                 │
                 ▼
Step 2: Run Tests
┌──────────────────────────────────────┐
│ cd firestore-rules-tests             │
│ npm test                             │
└──────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Jest Executes 21 Test Cases          │
└──────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Tests Connect to Emulator            │
│ Load firestore.rules                 │
│ Execute Test Scenarios               │
└──────────────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────┐
│ Results Displayed                    │
│ ✓ 21 tests passed                   │
└──────────────────────────────────────┘
```

## Test Coverage Map

```
┌─────────────────────────────────────────────────────────────┐
│                      Test Coverage                           │
└─────────────────────────────────────────────────────────────┘

Groups Collection (8 tests)
├── Read Permissions (4 tests)
│   ├── ✓ Member can read
│   ├── ✓ Query with array-contains
│   ├── ✓ Non-member denied
│   └── ✓ Unauthenticated denied
│
└── Create Permissions (4 tests)
    ├── ✓ Create with self as member
    ├── ✓ Create with multiple members
    ├── ✓ Cannot create without self
    └── ✓ Cannot create with different owner

Tasks Collection (5 tests)
├── ✓ Creator can read
├── ✓ Assignee can read
├── ✓ Query by userId
├── ✓ Query by assignedTo
└── ✓ Non-owner/assignee denied

Chats Collection (3 tests)
├── ✓ Participant can read
├── ✓ Query by participants
└── ✓ Non-participant denied

Group Activities (2 tests)
├── ✓ Member can read
└── ✓ Non-member denied

Permission Denied Scenarios (3 tests)
├── ✓ Groups query returns empty
├── ✓ Tasks query returns empty
└── ✓ Chats query returns empty

Total: 21 Tests
```

## Test Case Structure

```
┌─────────────────────────────────────────────────────────────┐
│                   Test Case Anatomy                          │
└─────────────────────────────────────────────────────────────┘

test('user can read groups they are a member of', async () => {
  
  // 1. SETUP: Create test data (rules disabled)
  ┌────────────────────────────────────────┐
  │ await testEnv.withSecurityRulesDisabled│
  │   Create group with user1 as member    │
  └────────────────────────────────────────┘
  
  // 2. TEST: Attempt operation (rules enabled)
  ┌────────────────────────────────────────┐
  │ const user1Context =                   │
  │   testEnv.authenticatedContext(USER1)  │
  │ Try to read group document             │
  └────────────────────────────────────────┘
  
  // 3. ASSERT: Verify result
  ┌────────────────────────────────────────┐
  │ await assertSucceeds(operation)        │
  │ OR                                     │
  │ await assertFails(operation)           │
  └────────────────────────────────────────┘
});
```

## Emulator UI Navigation

```
┌─────────────────────────────────────────────────────────────┐
│              Firebase Emulator UI (localhost:4000)           │
└─────────────────────────────────────────────────────────────┘

Home Screen
├── Firestore Tab
│   ├── Collections View
│   │   ├── groups/
│   │   ├── tasks/
│   │   ├── chats/
│   │   └── group_activities/
│   │
│   └── Rules Playground
│       ├── Set Authentication (user1, user2, etc.)
│       ├── Choose Operation (get, list, create, etc.)
│       ├── Enter Path
│       └── Execute & View Results
│
└── Logs Tab
    └── View Firestore operations and errors
```

## Manual Testing Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                  Manual Testing Steps                        │
└─────────────────────────────────────────────────────────────┘

1. Start Emulator
   ┌────────────────────────────────┐
   │ firebase emulators:start       │
   │   --only firestore             │
   └────────────────────────────────┘

2. Open UI
   ┌────────────────────────────────┐
   │ http://localhost:4000          │
   └────────────────────────────────┘

3. Create Test Data
   ┌────────────────────────────────┐
   │ Firestore Tab → Add Document   │
   │ Collection: groups             │
   │ Document ID: group1            │
   │ Fields: name, owner, memberIds │
   └────────────────────────────────┘

4. Test Permissions
   ┌────────────────────────────────┐
   │ Rules Playground               │
   │ Auth: user1                    │
   │ Operation: get                 │
   │ Path: /groups/group1           │
   │ Execute → Should Succeed ✓     │
   └────────────────────────────────┘

5. Test Denial
   ┌────────────────────────────────┐
   │ Rules Playground               │
   │ Auth: user3                    │
   │ Operation: get                 │
   │ Path: /groups/group1           │
   │ Execute → Should Fail ✗        │
   └────────────────────────────────┘

6. Verify Results
   ┌────────────────────────────────┐
   │ Check success/failure messages │
   │ Review in MANUAL_TEST_GUIDE.md │
   └────────────────────────────────┘
```

## Test Results Interpretation

```
┌─────────────────────────────────────────────────────────────┐
│                    Success Indicators                        │
└─────────────────────────────────────────────────────────────┘

✅ All Tests Pass
┌────────────────────────────────────────┐
│ Test Suites: 1 passed, 1 total         │
│ Tests:       21 passed, 21 total       │
│ Snapshots:   0 total                   │
│ Time:        X.XXXs                    │
└────────────────────────────────────────┘

✅ Rules Working Correctly
- No circular dependency errors
- Query patterns supported
- Permission denied returns empty results
- Access control enforced

┌─────────────────────────────────────────────────────────────┐
│                    Failure Indicators                        │
└─────────────────────────────────────────────────────────────┘

❌ Connection Error
┌────────────────────────────────────────┐
│ FetchError: request to localhost:8080  │
│ → Emulator not running                 │
│ → Start emulator first                 │
└────────────────────────────────────────┘

❌ Permission Error
┌────────────────────────────────────────┐
│ Expected success but got denied        │
│ → Check rules syntax                   │
│ → Verify test data setup               │
│ → Review memberIds/participants        │
└────────────────────────────────────────┘

❌ Timeout Error
┌────────────────────────────────────────┐
│ Test exceeded 30000ms timeout          │
│ → Emulator may be slow                 │
│ → Increase timeout in jest.config.js   │
└────────────────────────────────────────┘
```

## Quick Reference Commands

```
┌─────────────────────────────────────────────────────────────┐
│                    Command Reference                         │
└─────────────────────────────────────────────────────────────┘

Install Dependencies
$ cd firestore-rules-tests
$ npm install

Start Emulator
$ firebase emulators:start --only firestore --project test-project

Run Tests
$ cd firestore-rules-tests
$ npm test

Run Tests (Watch Mode)
$ npm run test:watch

View Emulator UI
$ open http://localhost:4000

Stop Emulator
$ Ctrl+C (in emulator terminal)

Check Emulator Status
$ curl http://localhost:8080
```

## File Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                   File Dependencies                          │
└─────────────────────────────────────────────────────────────┘

firestore.rules
      │
      │ (loaded by)
      ▼
firebase.json ──────► Emulator Config
      │                    │
      │                    │ (port 8080)
      │                    ▼
      │              Firebase Emulator
      │                    │
      │                    │ (connects to)
      │                    ▼
      └──────────► firestore.test.js
                         │
                         │ (uses)
                         ▼
                   @firebase/rules-unit-testing
                         │
                         │ (runs with)
                         ▼
                      Jest
                         │
                         │ (configured by)
                         ▼
                   jest.config.js
```

## Next Steps After Task 8

```
┌─────────────────────────────────────────────────────────────┐
│                      Task Progression                        │
└─────────────────────────────────────────────────────────────┘

✅ Task 8: Test Rules with Firebase Emulator
   │
   │ (tests pass)
   ▼
⏭️ Task 9: Deploy Updated Rules to Firebase
   │
   │ (deployment successful)
   ▼
⏭️ Task 10: Test App with Updated Rules
   │
   │ (app works correctly)
   ▼
⏭️ Task 11: Monitor Production Metrics
   │
   │ (no errors detected)
   ▼
⏭️ Task 12: Document Changes and Rollback Plan
   │
   │ (documentation complete)
   ▼
✅ Firestore Permissions Fix Complete
```

## Summary

Task 8 provides comprehensive testing infrastructure:

- **21 automated tests** covering all collections
- **Manual testing guide** for UI-based validation
- **Complete documentation** for setup and execution
- **Emulator configuration** for local testing
- **Test runner scripts** for easy execution

All tests validate that the Firestore rules:
- ✅ Have no circular dependencies
- ✅ Support required query patterns
- ✅ Handle permission denied gracefully
- ✅ Enforce proper access control

**Status**: ✅ COMPLETE and ready for Task 9
