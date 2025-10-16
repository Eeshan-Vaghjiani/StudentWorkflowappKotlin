# Task 43: Verification Checklist - Firestore Indexes

## Pre-Deployment Checklist

### File Creation
- [x] `firestore.indexes.json` created in project root
- [x] JSON syntax is valid
- [x] All 6 indexes defined:
  - [x] Messages index (chatId + timestamp)
  - [x] Tasks index (userId + dueDate)
  - [x] Group tasks index (groupId + dueDate)
  - [x] Tasks by category index (userId + category + dueDate)
  - [x] Tasks by status index (userId + status + dueDate)
  - [x] Chats index (participants + lastMessageTime)

## Deployment Checklist

### Firebase CLI Method
- [ ] Firebase CLI installed (`npm install -g firebase-tools`)
- [ ] Logged into Firebase (`firebase login`)
- [ ] Project initialized (`firebase init firestore`)
- [ ] Indexes deployed (`firebase deploy --only firestore:indexes`)
- [ ] Deployment successful (no errors in terminal)

### Firebase Console Method
- [ ] Opened Firebase Console
- [ ] Navigated to Firestore Database → Indexes
- [ ] Created all 6 indexes manually
- [ ] All indexes show "Building" or "Enabled" status

## Post-Deployment Verification

### Index Status Check
- [ ] All indexes show "Enabled" status in Firebase Console
- [ ] No indexes show "Error" status
- [ ] Index build completed (may take 5-30 minutes)

### Query Performance Testing

#### Test 1: Chat Messages Query
- [ ] Open a chat room in the app
- [ ] Messages load quickly (< 1 second)
- [ ] No index warnings in Logcat
- [ ] Pagination works smoothly
- [ ] Scroll to load older messages works

**Logcat Command**:
```bash
adb logcat | grep -E "(ChatRepository|index)"
```

**Expected**: No "index required" errors

#### Test 2: User Tasks Query
- [ ] Open Tasks screen
- [ ] Tasks load quickly (< 1 second)
- [ ] No index warnings in Logcat
- [ ] Tasks sorted by due date correctly

**Logcat Command**:
```bash
adb logcat | grep -E "(TaskRepository|index)"
```

**Expected**: No "index required" errors

#### Test 3: Chat List Query
- [ ] Open Chat screen
- [ ] Chat list loads quickly (< 1 second)
- [ ] No index warnings in Logcat
- [ ] Chats sorted by last message time

**Logcat Command**:
```bash
adb logcat | grep -E "(ChatRepository|index)"
```

**Expected**: No "index required" errors

#### Test 4: Group Tasks Query
- [ ] Open a group
- [ ] View group tasks
- [ ] Tasks load quickly
- [ ] No index warnings in Logcat

#### Test 5: Filtered Tasks Query
- [ ] Filter tasks by category
- [ ] Filter tasks by status
- [ ] Both filters work quickly
- [ ] No index warnings in Logcat

### Performance Benchmarks

| Query | Before Indexes | After Indexes | Status |
|-------|----------------|---------------|--------|
| Chat list | 2-5 seconds | < 500ms | [ ] |
| Messages | 1-3 seconds | < 300ms | [ ] |
| User tasks | 1-3 seconds | < 500ms | [ ] |
| Group tasks | 1-3 seconds | < 500ms | [ ] |
| Filtered tasks | 2-4 seconds | < 500ms | [ ] |

### Error Checking

#### Logcat Monitoring
- [ ] No "FAILED_PRECONDITION" errors
- [ ] No "index required" errors
- [ ] No "composite index" warnings
- [ ] No timeout errors on queries

#### Common Error Messages to Check For
```
❌ "The query requires an index"
❌ "FAILED_PRECONDITION: The query requires a composite index"
❌ "index not found"
✅ No index-related errors
```

## Troubleshooting Results

### If Indexes Not Working
- [ ] Verified index status is "Enabled" (not "Building")
- [ ] Waited sufficient time for index build (10-30 minutes)
- [ ] Cleared app data and cache
- [ ] Restarted app
- [ ] Checked Firebase project is correct
- [ ] Verified query fields match index definition

### If Deployment Failed
- [ ] Checked Firebase CLI version (`firebase --version`)
- [ ] Verified Firebase project ID
- [ ] Checked user permissions (Owner/Editor role)
- [ ] Tried manual creation in Console
- [ ] Checked firestore.indexes.json syntax

## Requirements Verification

### Requirement 10.5: Optimize Firestore queries with indexes
- [x] Created `firestore.indexes.json` file
- [x] Added composite index for messages (chatId + timestamp)
- [x] Added composite index for tasks (assignedTo/userId + dueDate)
- [x] Added composite index for chats (participants + lastMessageTime)
- [ ] Deployed indexes to Firebase Console
- [ ] Tested that queries run fast
- [ ] Verified no index warnings in production

## Additional Indexes Created
- [x] Group tasks index (groupId + dueDate)
- [x] Tasks by category index (userId + category + dueDate)
- [x] Tasks by status index (userId + status + dueDate)

## Sign-Off

### Developer Checklist
- [ ] All indexes created and deployed
- [ ] All tests passed
- [ ] Performance improved significantly
- [ ] No errors in Logcat
- [ ] Documentation complete

### Performance Improvement
- [ ] Chat queries: ___% faster
- [ ] Task queries: ___% faster
- [ ] Overall app responsiveness improved

### Notes
```
[Add any notes about deployment, issues encountered, or performance observations]
```

## Task Completion Criteria

✅ **Task is complete when**:
1. `firestore.indexes.json` file exists with all 6 indexes
2. Indexes deployed to Firebase (Console shows "Enabled")
3. All query tests pass without index warnings
4. Performance benchmarks met (< 1 second for all queries)
5. No index-related errors in Logcat during testing

## Next Steps
After completing this checklist:
1. Mark Task 43 as complete in tasks.md
2. Proceed to Task 44: Implement lifecycle-aware listeners
3. Continue with Phase 9: Performance Optimization
