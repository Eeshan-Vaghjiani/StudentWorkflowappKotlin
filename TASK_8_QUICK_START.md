# Task 8: Quick Start Guide

## Run Tests in 3 Steps

### Option A: Automated Tests

**Terminal 1:**
```bash
firebase emulators:start --only firestore --project test-project
```

**Terminal 2:**
```bash
cd firestore-rules-tests
npm test
```

### Option B: Manual Testing

```bash
firebase emulators:start --only firestore
```

Then open http://localhost:4000 and follow `firestore-rules-tests/MANUAL_TEST_GUIDE.md`

## What Gets Tested

✅ Groups: Read, create, query permissions (8 tests)
✅ Tasks: Read, query by owner/assignee (5 tests)
✅ Chats: Read, query by participants (3 tests)
✅ Group Activities: Read with membership (2 tests)
✅ Permission denied scenarios (3 tests)

**Total: 21 test cases**

## Expected Result

All 21 tests should pass, confirming:
- No circular dependencies in rules
- Query patterns work correctly
- Permission denied returns empty results (not errors)
- Access control works as designed

## Files Created

- `firestore-rules-tests/firestore.test.js` - Test suite
- `firestore-rules-tests/MANUAL_TEST_GUIDE.md` - Manual testing steps
- `firestore-rules-tests/README.md` - Full documentation
- `firebase.json` - Updated with emulator config

## Next Task

Once tests pass → **Task 9: Deploy Updated Rules to Firebase**
