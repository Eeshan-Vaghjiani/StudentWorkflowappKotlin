# Task 7: Enhanced Offline Message Queue - Testing Guide

## Manual Testing Checklist

### Test 1: Basic Message Sending (Baseline)
**Objective**: Verify normal message sending still works

**Steps**:
1. Ensure device has internet connection
2. Open a chat
3. Send a text message
4. Verify message shows clock icon briefly
5. Verify message changes to checkmark icon
6. Verify message appears in chat

**Expected Result**: ✅ Message sends successfully with SENT status

---

### Test 2: Offline Message Queuing
**Objective**: Verify messages are queued when offline

**Steps**:
1. Turn off WiFi and mobile data
2. Open a chat
3. Send a text message
4. Observe the message status icon

**Expected Result**: 
- ✅ Message shows clock icon (SENDING status)
- ✅ Message stays in chat with clock icon
- ✅ No error message shown immediately

---

### Test 3: Automatic Retry on Network Restore
**Objective**: Verify automatic retry when network is restored

**Steps**:
1. Turn off WiFi and mobile data
2. Send a message (should show clock icon)
3. Wait 2 seconds
4. Turn on WiFi or mobile data
5. Wait up to 5 seconds

**Expected Result**:
- ✅ Message automatically retries
- ✅ Clock icon changes to checkmark
- ✅ Message status changes to SENT
- ✅ No user interaction required

---

### Test 4: Failed Retryable Status
**Objective**: Verify retryable failure status and icon

**Steps**:
1. Turn off WiFi and mobile data
2. Send a message
3. Wait 30 seconds (for retry threshold)
4. Message should attempt retry and fail

**Expected Result**:
- ✅ Message shows orange error icon
- ✅ Error icon is clickable
- ✅ Message status is FAILED_RETRYABLE

---

### Test 5: Manual Retry
**Objective**: Verify manual retry by tapping error icon

**Steps**:
1. Have a message with orange error icon (FAILED_RETRYABLE)
2. Turn on internet connection
3. Tap the orange error icon
4. Observe the message status

**Expected Result**:
- ✅ Message immediately attempts to send
- ✅ Icon changes from orange error to clock
- ✅ Then changes to checkmark on success
- ✅ Message status changes to SENT

---

### Test 6: Permanent Failure (Permission Denied)
**Objective**: Verify permanent failure handling

**Setup**: This requires simulating a permission error. You can:
- Remove user from chat participants in Firestore
- Or modify Firestore rules temporarily to deny write access

**Steps**:
1. Trigger a permission denied error when sending
2. Observe the message status icon

**Expected Result**:
- ✅ Message shows dark red error icon
- ✅ Error icon is NOT clickable
- ✅ Message status is FAILED_PERMANENT
- ✅ Message will not retry automatically

---

### Test 7: Max Retry Attempts
**Objective**: Verify messages convert to permanent failure after max attempts

**Steps**:
1. Turn off internet
2. Send a message
3. Manually retry 5 times (tap orange icon each time)
4. Observe status after 5th attempt

**Expected Result**:
- ✅ After 5 attempts, icon changes from orange to dark red
- ✅ Status changes to FAILED_PERMANENT
- ✅ Icon becomes non-clickable
- ✅ No more automatic retries

---

### Test 8: Multiple Messages Retry
**Objective**: Verify multiple queued messages retry correctly

**Steps**:
1. Turn off internet
2. Send 3 different messages
3. All should show clock icon
4. Turn on internet
5. Wait for automatic retry

**Expected Result**:
- ✅ All 3 messages retry automatically
- ✅ 500ms delay between each retry
- ✅ All messages change to checkmark
- ✅ All messages appear in correct order

---

### Test 9: Image Message Retry
**Objective**: Verify image messages can be retried

**Steps**:
1. Turn off internet
2. Send an image message
3. Turn on internet
4. Observe retry behavior

**Expected Result**:
- ✅ Image upload fails initially
- ✅ Shows orange error icon
- ✅ Automatically retries when online
- ✅ Image uploads and message sends

---

### Test 10: Network State Indicator
**Objective**: Verify network state changes are detected

**Steps**:
1. Turn off WiFi
2. Observe app behavior
3. Turn on WiFi
4. Observe app behavior

**Expected Result**:
- ✅ Network observer detects offline state
- ✅ Network observer detects online state
- ✅ Automatic retry triggers on online state
- ✅ No crashes or errors

---

### Test 11: App Restart with Queued Messages
**Objective**: Verify queued messages persist across app restarts

**Steps**:
1. Turn off internet
2. Send 2 messages
3. Force close the app
4. Reopen the app
5. Navigate to the chat

**Expected Result**:
- ✅ Messages still show in chat
- ✅ Messages still have clock or orange error icon
- ✅ Messages are in offline queue
- ✅ Turn on internet → messages retry automatically

---

### Test 12: Retry Throttling
**Objective**: Verify retry attempts are throttled

**Steps**:
1. Turn off internet
2. Send a message
3. Turn on internet briefly (2 seconds)
4. Turn off internet again
5. Turn on internet again within 30 seconds

**Expected Result**:
- ✅ First retry attempt happens
- ✅ Second retry is throttled (skipped if within 30s)
- ✅ No server overload
- ✅ Retry happens after 30s threshold

---

### Test 13: Chat List with Failed Messages
**Objective**: Verify failed messages don't break chat list

**Steps**:
1. Send failed messages in multiple chats
2. Navigate to chat list
3. Observe chat list display

**Expected Result**:
- ✅ Chat list loads correctly
- ✅ Last message shows for each chat
- ✅ Failed messages don't cause crashes
- ✅ Can navigate to any chat

---

### Test 14: Clear Failed Messages
**Objective**: Verify clearing failed messages works

**Steps**:
1. Have several permanently failed messages
2. Call `offlineQueue.clearPermanentlyFailedMessages()`
3. Check queue status

**Expected Result**:
- ✅ Permanently failed messages removed from queue
- ✅ Retryable messages remain in queue
- ✅ Pending messages remain in queue
- ✅ No crashes

---

### Test 15: Status Icon Colors
**Objective**: Verify all status icons show correct colors

**Test Matrix**:

| Status | Icon | Color | Clickable |
|--------|------|-------|-----------|
| SENDING | Clock | White (70% opacity) | No |
| SENT | Single Check | White (70% opacity) | No |
| DELIVERED | Double Check | White (70% opacity) | No |
| READ | Double Check | Blue | No |
| FAILED | Error | Red | Yes |
| FAILED_RETRYABLE | Error | Orange | Yes |
| FAILED_PERMANENT | Error | Dark Red | No |

**Expected Result**: ✅ All icons match the table above

---

## Automated Testing Scenarios

### Unit Test: Failure Categorization
```kotlin
@Test
fun `PERMISSION_DENIED should mark as FAILED_PERMANENT`() {
    // Given: A message and permission denied error
    val message = createTestMessage()
    val exception = FirebaseFirestoreException(
        "Permission denied",
        FirebaseFirestoreException.Code.PERMISSION_DENIED
    )
    
    // When: Send message fails with permission error
    val result = chatRepository.sendMessage(chatId, text)
    
    // Then: Message should be marked as FAILED_PERMANENT
    val queuedMessage = offlineQueue.getQueuedMessages().first()
    assertEquals(MessageStatus.FAILED_PERMANENT, queuedMessage.status)
}

@Test
fun `UNAVAILABLE should mark as FAILED_RETRYABLE`() {
    // Given: A message and unavailable error
    val message = createTestMessage()
    val exception = FirebaseFirestoreException(
        "Service unavailable",
        FirebaseFirestoreException.Code.UNAVAILABLE
    )
    
    // When: Send message fails with unavailable error
    val result = chatRepository.sendMessage(chatId, text)
    
    // Then: Message should be marked as FAILED_RETRYABLE
    val queuedMessage = offlineQueue.getQueuedMessages().first()
    assertEquals(MessageStatus.FAILED_RETRYABLE, queuedMessage.status)
}
```

### Unit Test: Retry Counter
```kotlin
@Test
fun `retry counter should increment on each attempt`() {
    // Given: A queued message
    val message = createTestMessage()
    offlineQueue.queueMessage(message)
    
    // When: Increment attempts 3 times
    offlineQueue.incrementAttempts(message.id)
    offlineQueue.incrementAttempts(message.id)
    offlineQueue.incrementAttempts(message.id)
    
    // Then: Attempt count should be 3
    val queued = offlineQueue.getQueuedMessagesInternal().first()
    assertEquals(3, queued.attempts)
}

@Test
fun `max retry attempts should convert to FAILED_PERMANENT`() {
    // Given: A message with 4 attempts
    val message = createTestMessage()
    offlineQueue.queueMessage(message)
    repeat(4) { offlineQueue.incrementAttempts(message.id) }
    
    // When: Mark as failed retryable (5th attempt)
    offlineQueue.markMessageAsFailedRetryable(message.id)
    
    // Then: Should convert to FAILED_PERMANENT
    val queuedMessage = offlineQueue.getQueuedMessages().first()
    assertEquals(MessageStatus.FAILED_PERMANENT, queuedMessage.status)
}
```

### Unit Test: Network Observer
```kotlin
@Test
fun `network observer should emit true when connected`() = runTest {
    // Given: Network observer
    val observer = NetworkConnectivityObserver(context)
    
    // When: Collect network state
    val states = mutableListOf<Boolean>()
    val job = launch {
        observer.observe().take(2).collect { states.add(it) }
    }
    
    // Simulate network connection
    // (This requires mocking ConnectivityManager)
    
    // Then: Should emit true
    job.join()
    assertTrue(states.contains(true))
}
```

### Integration Test: Automatic Retry Flow
```kotlin
@Test
fun `automatic retry should send queued messages when network restored`() = runTest {
    // Given: Offline messages in queue
    turnOffNetwork()
    chatRepository.sendMessage(chatId, "Test message 1")
    chatRepository.sendMessage(chatId, "Test message 2")
    
    // When: Network is restored
    turnOnNetwork()
    delay(3000) // Wait for automatic retry
    
    // Then: Messages should be sent
    val queuedMessages = offlineQueue.getQueuedMessages()
    assertTrue(queuedMessages.isEmpty())
    
    // And: Messages should appear in Firestore
    val messages = getChatMessages(chatId)
    assertEquals(2, messages.size)
}
```

---

## Performance Testing

### Test 1: Large Queue Performance
**Objective**: Verify app handles large message queue

**Steps**:
1. Queue 100 messages while offline
2. Turn on internet
3. Measure retry time and memory usage

**Expected Result**:
- ✅ All messages retry successfully
- ✅ No memory leaks
- ✅ No ANR (Application Not Responding)
- ✅ Retry completes within reasonable time

### Test 2: Rapid Network Changes
**Objective**: Verify app handles rapid network state changes

**Steps**:
1. Toggle WiFi on/off rapidly 10 times
2. Observe app behavior
3. Check for crashes or memory leaks

**Expected Result**:
- ✅ No crashes
- ✅ Network observer handles rapid changes
- ✅ Retry throttling prevents server overload
- ✅ No memory leaks

### Test 3: Battery Impact
**Objective**: Measure battery impact of network observer

**Steps**:
1. Enable battery profiling
2. Run app for 1 hour with network observer active
3. Measure battery drain

**Expected Result**:
- ✅ Minimal battery impact
- ✅ Network callback only active when needed
- ✅ No background polling

---

## Edge Cases

### Edge Case 1: Message Sent While Retry in Progress
**Scenario**: User manually retries while automatic retry is in progress

**Expected Behavior**: 
- First retry completes
- Second retry is skipped (message already sent)
- No duplicate messages

### Edge Case 2: App Killed During Retry
**Scenario**: App is force-closed while retrying messages

**Expected Behavior**:
- Messages remain in queue
- Retry resumes when app reopens
- No lost messages

### Edge Case 3: Firestore Rules Change
**Scenario**: Firestore rules change while messages are queued

**Expected Behavior**:
- Permission errors detected on retry
- Messages marked as FAILED_PERMANENT
- User notified of permission issue

### Edge Case 4: Network Flapping
**Scenario**: Network connection unstable (on/off repeatedly)

**Expected Behavior**:
- Retry throttling prevents excessive attempts
- Messages eventually send when stable
- No server overload

---

## Regression Testing

After implementing this feature, verify these existing features still work:

- ✅ Normal message sending (online)
- ✅ Image message sending
- ✅ Document message sending
- ✅ Chat list loading
- ✅ Message list scrolling
- ✅ Typing indicators
- ✅ Read receipts
- ✅ Message deletion
- ✅ Chat creation
- ✅ Group chat functionality

---

## Known Issues & Limitations

### Issue 1: SharedPreferences Size Limit
**Description**: Large queues may exceed SharedPreferences size limit

**Workaround**: Periodically clear old permanent failures

**Future Fix**: Migrate to Room database for queue storage

### Issue 2: Image Upload Retry
**Description**: Image uploads retry from scratch (no resume)

**Impact**: Large images may take multiple attempts

**Future Fix**: Implement resumable uploads

### Issue 3: Retry Interval Fixed
**Description**: 30-second retry interval is not configurable

**Impact**: May be too long or too short for some use cases

**Future Fix**: Make retry interval configurable

---

## Success Criteria

All tests pass if:

1. ✅ Messages queue correctly when offline
2. ✅ Automatic retry works when network restored
3. ✅ Manual retry works by tapping error icon
4. ✅ Failure types categorized correctly (retryable vs permanent)
5. ✅ Status icons show correct colors
6. ✅ Max retry attempts enforced (5 attempts)
7. ✅ Retry throttling prevents server overload
8. ✅ Queue persists across app restarts
9. ✅ No crashes or memory leaks
10. ✅ Existing features not broken

---

## Reporting Issues

When reporting issues, include:

1. **Device Info**: Model, Android version
2. **Network State**: WiFi, mobile data, or offline
3. **Queue Status**: Number of pending/failed messages
4. **Steps to Reproduce**: Detailed steps
5. **Expected vs Actual**: What should happen vs what happened
6. **Logs**: Relevant logcat output (filter by "ChatRepository", "OfflineMessageQueue", "MessageRetryManager")
7. **Screenshots**: Status icons, error messages

Example log filter:
```bash
adb logcat | grep -E "ChatRepository|OfflineMessageQueue|MessageRetryManager|NetworkConnectivity"
```

---

## Test Completion Checklist

- [ ] All manual tests completed
- [ ] All automated tests passing
- [ ] Performance tests acceptable
- [ ] Edge cases handled
- [ ] Regression tests passing
- [ ] No memory leaks detected
- [ ] Battery impact acceptable
- [ ] Documentation reviewed
- [ ] Known issues documented
- [ ] Ready for production
