# Task 43: Testing Guide - Firestore Indexes

## Overview
This guide provides step-by-step instructions for testing Firestore indexes after deployment.

## Prerequisites
- [ ] Indexes deployed to Firebase
- [ ] App installed on device/emulator
- [ ] ADB tools available
- [ ] Firebase Console access

## Test Environment Setup

### 1. Enable Verbose Logging
Add to your Application class or MainActivity:
```kotlin
FirebaseFirestore.setLoggingEnabled(true)
```

### 2. Monitor Logcat
Open terminal and run:
```bash
# Monitor all Firestore activity
adb logcat | grep -E "(Firestore|index|ChatRepository|TaskRepository)"

# Or filter for errors only
adb logcat *:E | grep -i index
```

### 3. Clear App Data (Optional)
For clean test:
```bash
adb shell pm clear com.example.loginandregistration
```

## Test Scenarios

### Test 1: Chat Messages Index

**Index Being Tested**: `messages` (chatId + timestamp)

**Steps**:
1. Launch the app and login
2. Navigate to Chat screen
3. Open any chat room
4. Observe message loading time
5. Scroll up to load older messages (pagination)
6. Check Logcat for index warnings

**Expected Results**:
- ✅ Messages load in < 500ms
- ✅ No "index required" errors in Logcat
- ✅ Smooth scrolling and pagination
- ✅ Timestamp ordering is correct (newest at bottom)

**Logcat Check**:
```bash
adb logcat | grep "getChatMessages"
```

**Success Indicators**:
```
✅ "getChatMessages: Received X messages"
✅ No FAILED_PRECONDITION errors
✅ No "composite index required" warnings
```

**Failure Indicators**:
```
❌ "The query requires an index"
❌ "FAILED_PRECONDITION"
❌ Slow loading (> 2 seconds)
```

---

### Test 2: User Tasks Index

**Index Being Tested**: `tasks` (userId + dueDate)

**Steps**:
1. Navigate to Tasks screen
2. Observe task loading time
3. Verify tasks are sorted by due date
4. Pull to refresh
5. Check Logcat for index warnings

**Expected Results**:
- ✅ Tasks load in < 500ms
- ✅ No "index required" errors in Logcat
- ✅ Tasks sorted correctly by due date
- ✅ Refresh works smoothly

**Logcat Check**:
```bash
adb logcat | grep "getUserTasks"
```

**Success Indicators**:
```
✅ "Loaded X tasks"
✅ No index errors
✅ Query completes quickly
```

---

### Test 3: Chat List Index

**Index Being Tested**: `chats` (participants + lastMessageTime)

**Steps**:
1. Navigate to Chat screen
2. Observe chat list loading time
3. Verify chats are sorted by last message time
4. Send a message in one chat
5. Verify that chat moves to top of list
6. Check Logcat for index warnings

**Expected Results**:
- ✅ Chat list loads in < 500ms
- ✅ No "index required" errors in Logcat
- ✅ Chats sorted by most recent message
- ✅ Real-time updates work correctly

**Logcat Check**:
```bash
adb logcat | grep "getUserChats"
```

**Success Indicators**:
```
✅ "getUserChats: Received X chat documents"
✅ "getUserChats: Sending X chats to UI"
✅ No "FIRESTORE INDEX REQUIRED" errors
```

**Note**: The code has a fallback that sorts manually if index is missing, but with index it should be faster.

---

### Test 4: Group Tasks Index

**Index Being Tested**: `tasks` (groupId + dueDate)

**Steps**:
1. Navigate to Groups screen
2. Open a group
3. View group tasks
4. Observe loading time
5. Check Logcat for index warnings

**Expected Results**:
- ✅ Group tasks load in < 500ms
- ✅ No "index required" errors in Logcat
- ✅ Tasks sorted by due date

**Logcat Check**:
```bash
adb logcat | grep "getGroupTasks"
```

---

### Test 5: Filtered Tasks Indexes

**Indexes Being Tested**: 
- `tasks` (userId + category + dueDate)
- `tasks` (userId + status + dueDate)

**Steps**:
1. Navigate to Tasks screen
2. Apply category filter (if implemented)
3. Apply status filter (if implemented)
4. Observe loading time for each filter
5. Check Logcat for index warnings

**Expected Results**:
- ✅ Filtered tasks load in < 500ms
- ✅ No "index required" errors in Logcat
- ✅ Correct tasks shown for each filter

**Logcat Check**:
```bash
adb logcat | grep -E "(getTasksByCategory|getTasksByStatus)"
```

---

## Performance Benchmarking

### Measure Query Time

Add timing logs to repositories (temporary for testing):

```kotlin
// In ChatRepository.getChatMessages()
val startTime = System.currentTimeMillis()
// ... query execution ...
val endTime = System.currentTimeMillis()
Log.d(TAG, "Query took ${endTime - startTime}ms")
```

### Performance Targets

| Query Type | Target Time | Acceptable | Needs Optimization |
|------------|-------------|------------|-------------------|
| Chat messages | < 300ms | < 500ms | > 1000ms |
| User tasks | < 300ms | < 500ms | > 1000ms |
| Chat list | < 300ms | < 500ms | > 1000ms |
| Group tasks | < 300ms | < 500ms | > 1000ms |
| Filtered tasks | < 500ms | < 1000ms | > 2000ms |

### Record Results

| Test | Time (ms) | Status | Notes |
|------|-----------|--------|-------|
| Chat messages | ___ | ⬜ Pass / ⬜ Fail | |
| User tasks | ___ | ⬜ Pass / ⬜ Fail | |
| Chat list | ___ | ⬜ Pass / ⬜ Fail | |
| Group tasks | ___ | ⬜ Pass / ⬜ Fail | |
| Filter by category | ___ | ⬜ Pass / ⬜ Fail | |
| Filter by status | ___ | ⬜ Pass / ⬜ Fail | |

---

## Stress Testing

### Test with Large Datasets

**Scenario 1: Many Messages**
1. Create a chat with 500+ messages
2. Open the chat
3. Measure loading time
4. Test pagination by scrolling up

**Expected**: Should still load quickly with index

**Scenario 2: Many Tasks**
1. Create 100+ tasks
2. Open Tasks screen
3. Measure loading time
4. Apply filters

**Expected**: Should still load quickly with index

**Scenario 3: Many Chats**
1. Create 50+ chats (groups + direct)
2. Open Chat screen
3. Measure loading time

**Expected**: Should still load quickly with index

---

## Troubleshooting Tests

### Test: Index Not Found Error

**Trigger**: Run query before index is deployed

**Expected Error**:
```
FAILED_PRECONDITION: The query requires an index. 
You can create it here: https://console.firebase.google.com/...
```

**Resolution**:
1. Click the link in error message
2. Or deploy indexes manually
3. Wait for index to build
4. Retry query

### Test: Index Building

**Check Status**:
1. Go to Firebase Console
2. Navigate to Firestore → Indexes
3. Check status column

**Statuses**:
- 🟡 Building: Wait 5-30 minutes
- 🟢 Enabled: Ready to use
- 🔴 Error: Check configuration

### Test: Query Still Slow with Index

**Possible Causes**:
1. Index still building
2. Large document sizes
3. Slow network connection
4. Offline persistence not enabled

**Debug Steps**:
```bash
# Check network speed
adb shell ping -c 5 8.8.8.8

# Check Firestore cache
adb logcat | grep "Firestore cache"

# Check document sizes
# In Firebase Console → Firestore → Documents
```

---

## Automated Testing (Optional)

### Unit Test for Index Verification

```kotlin
@Test
fun testChatMessagesQueryPerformance() = runBlocking {
    val startTime = System.currentTimeMillis()
    
    val messages = chatRepository.getChatMessages(testChatId, 50)
        .first()
    
    val duration = System.currentTimeMillis() - startTime
    
    assertTrue("Query took too long: ${duration}ms", duration < 1000)
    assertFalse("No messages loaded", messages.isEmpty())
}
```

### Integration Test

```kotlin
@Test
fun testAllIndexesWork() = runBlocking {
    // Test chat messages
    val messages = chatRepository.getChatMessages(testChatId).first()
    assertNotNull(messages)
    
    // Test user tasks
    val tasks = taskRepository.getUserTasks()
    assertNotNull(tasks)
    
    // Test chat list
    val chats = chatRepository.getUserChats().first()
    assertNotNull(chats)
    
    // All queries should complete without errors
}
```

---

## Verification Checklist

### Before Testing
- [ ] Indexes deployed to Firebase
- [ ] Index status is "Enabled" in Console
- [ ] App has latest code
- [ ] Test data available (chats, tasks, messages)

### During Testing
- [ ] Logcat monitoring active
- [ ] Performance times recorded
- [ ] Screenshots of results taken
- [ ] Errors documented

### After Testing
- [ ] All tests passed
- [ ] Performance targets met
- [ ] No index warnings in Logcat
- [ ] Results documented

---

## Test Report Template

```
## Firestore Indexes Test Report

**Date**: ___________
**Tester**: ___________
**App Version**: ___________
**Device**: ___________

### Index Deployment
- Deployment Method: [ ] CLI [ ] Console
- Deployment Time: ___________
- Index Build Time: ___________

### Test Results

#### Chat Messages Index
- Status: [ ] Pass [ ] Fail
- Load Time: _____ms
- Notes: ___________

#### User Tasks Index
- Status: [ ] Pass [ ] Fail
- Load Time: _____ms
- Notes: ___________

#### Chat List Index
- Status: [ ] Pass [ ] Fail
- Load Time: _____ms
- Notes: ___________

#### Group Tasks Index
- Status: [ ] Pass [ ] Fail
- Load Time: _____ms
- Notes: ___________

#### Filtered Tasks Indexes
- Status: [ ] Pass [ ] Fail
- Load Time: _____ms
- Notes: ___________

### Performance Summary
- Overall Improvement: ____%
- Issues Found: ___________
- Recommendations: ___________

### Sign-Off
- [ ] All tests passed
- [ ] Performance acceptable
- [ ] Ready for production
```

---

## Next Steps After Testing

1. ✅ If all tests pass:
   - Mark Task 43 as complete
   - Document performance improvements
   - Proceed to Task 44

2. ❌ If tests fail:
   - Review index configuration
   - Check Firebase Console for errors
   - Verify query syntax matches indexes
   - Re-deploy if necessary
   - Re-test

3. 📊 Performance Monitoring:
   - Set up Firebase Performance Monitoring
   - Track query times in production
   - Monitor index usage
   - Optimize as needed
