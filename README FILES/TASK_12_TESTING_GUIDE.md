# Task 12: Chat Message Sending and Reading - Testing Guide

## Overview
This guide provides comprehensive testing procedures for the chat message sending and reading functionality with error handling, offline support, and retry logic.

## Prerequisites

### Required Setup
1. ✅ Firebase project configured
2. ✅ Firestore security rules deployed
3. ✅ Two test accounts for chat testing
4. ✅ Android device or emulator
5. ✅ Network connectivity controls (airplane mode)

### Test Data
- Test User 1: Primary tester account
- Test User 2: Secondary account for chat partner
- Test Chat: Direct chat between User 1 and User 2
- Test Group: Group chat with multiple members

## Test Scenarios

### 1. Basic Message Sending ✅

#### Test 1.1: Send Text Message
**Steps**:
1. Open chat with another user
2. Type a message: "Hello, this is a test message"
3. Click send button
4. Observe message status

**Expected Results**:
- ✅ Message appears immediately with SENDING status
- ✅ Message status changes to SENT after server confirmation
- ✅ Message appears in recipient's chat
- ✅ Last message updated in chat list
- ✅ Timestamp displayed correctly

**Verification**:
```kotlin
// Check message in Firestore
- Message document exists in messages subcollection
- Message has correct senderId, text, timestamp
- Message status is SENT
- Chat lastMessage updated
```

#### Test 1.2: Send Empty Message
**Steps**:
1. Open chat
2. Leave message input empty
3. Click send button

**Expected Results**:
- ✅ Send button disabled or message rejected
- ✅ Error message: "Message cannot be empty"
- ✅ No message created in Firestore
- ✅ No entry in offline queue

#### Test 1.3: Send Long Message
**Steps**:
1. Open chat
2. Type a very long message (>1000 characters)
3. Click send button

**Expected Results**:
- ✅ Message sent successfully
- ✅ Message displays correctly in chat
- ✅ No truncation or data loss
- ✅ Proper text wrapping in UI

### 2. Permission Error Handling ✅

#### Test 2.1: Send Message Without Permission
**Steps**:
1. Modify Firestore rules to deny message sending
2. Try to send a message
3. Observe error handling

**Expected Results**:
- ✅ Error caught by safeFirestoreCall
- ✅ Message marked as FAILED immediately
- ✅ Error message: "Permission denied: You don't have access to send messages in this chat"
- ✅ Message NOT retried automatically
- ✅ Error logged to Crashlytics

**Verification**:
```kotlin
// Check offline queue
val failedMessages = offlineQueue.getQueuedMessages()
    .filter { it.status == MessageStatus.FAILED }
// Should contain the failed message
```

#### Test 2.2: Read Messages Without Permission
**Steps**:
1. Modify Firestore rules to deny read access
2. Try to mark messages as read
3. Observe error handling

**Expected Results**:
- ✅ Error caught gracefully
- ✅ Warning logged (non-critical operation)
- ✅ UI continues to function
- ✅ No crash or freeze
- ✅ Error message logged but not shown to user

### 3. Offline Message Queue ✅

#### Test 3.1: Send Message While Offline
**Steps**:
1. Enable airplane mode
2. Send a message: "Offline test message"
3. Observe message status
4. Disable airplane mode
5. Wait for automatic retry

**Expected Results**:
- ✅ Message queued with SENDING status
- ✅ Message visible in chat with sending indicator
- ✅ Message persisted in SharedPreferences
- ✅ When online, message automatically sent
- ✅ Status changes to SENT
- ✅ Message removed from queue

**Verification**:
```kotlin
// While offline
val queuedMessages = chatRepository.getAllQueuedMessages()
// Should contain the message

// After online
val queuedMessages = chatRepository.getAllQueuedMessages()
// Should be empty (message sent)
```

#### Test 3.2: Multiple Offline Messages
**Steps**:
1. Enable airplane mode
2. Send 5 messages in sequence
3. Observe queue status
4. Disable airplane mode
5. Observe batch processing

**Expected Results**:
- ✅ All 5 messages queued
- ✅ All visible in chat with sending status
- ✅ Queue persisted across app restart
- ✅ When online, messages sent in order
- ✅ 500ms delay between sends
- ✅ All messages eventually sent
- ✅ Queue cleared after success

#### Test 3.3: Queue Persistence Across Restart
**Steps**:
1. Enable airplane mode
2. Send 3 messages
3. Force close app
4. Reopen app
5. Check queue status
6. Disable airplane mode

**Expected Results**:
- ✅ Messages still in queue after restart
- ✅ Messages visible in chat
- ✅ Status still SENDING
- ✅ When online, messages sent automatically
- ✅ No data loss

### 4. Retry Logic ✅

#### Test 4.1: Automatic Retry on Network Error
**Steps**:
1. Simulate network error (airplane mode)
2. Send message
3. Enable network after 10 seconds
4. Observe retry behavior

**Expected Results**:
- ✅ Message queued with SENDING status
- ✅ Automatic retry when network restored
- ✅ Message sent successfully
- ✅ Status updated to SENT
- ✅ Removed from queue

#### Test 4.2: Manual Retry Failed Message
**Steps**:
1. Create a failed message (permission error)
2. Fix permission issue
3. Call retryMessage() manually
4. Observe result

**Expected Results**:
- ✅ Message status reset to SENDING
- ✅ Retry attempt made
- ✅ If successful, status changes to SENT
- ✅ If failed, status remains FAILED
- ✅ Appropriate error message shown

**Code**:
```kotlin
val failedMessage = // get failed message
lifecycleScope.launch {
    val result = chatRepository.retryMessage(failedMessage)
    if (result.isSuccess) {
        showToast("Message sent successfully")
    } else {
        showToast("Retry failed: ${result.exceptionOrNull()?.message}")
    }
}
```

#### Test 4.3: Max Retry Attempts
**Steps**:
1. Simulate persistent network error
2. Send message
3. Observe retry attempts
4. Verify max attempts reached

**Expected Results**:
- ✅ Message retried up to 3 times (MAX_RETRY_ATTEMPTS)
- ✅ After 3 failures, marked as FAILED
- ✅ Error message: "Failed to send message after 3 attempts"
- ✅ Message remains in queue as FAILED
- ✅ No further automatic retries

### 5. Exponential Backoff ✅

#### Test 5.1: Backoff Delay Calculation
**Steps**:
1. Create messages with different ages
2. Call retryPendingMessagesWithBackoff()
3. Observe retry delays

**Expected Results**:
- ✅ Fresh messages (0-30s old): 1s delay
- ✅ 30-60s old messages: 2s delay
- ✅ 60-90s old messages: 4s delay
- ✅ 90-120s old messages: 8s delay
- ✅ 120-150s old messages: 16s delay
- ✅ >150s old messages: 30s delay (max)

**Verification**:
```kotlin
// Test backoff calculation
val messageAge = 45000L // 45 seconds
val delay = calculateBackoffDelay(messageAge)
// Should be 2000L (2 seconds)
```

#### Test 5.2: Batch Retry with Backoff
**Steps**:
1. Queue 10 messages at different times
2. Wait for various durations
3. Call retryPendingMessagesWithBackoff()
4. Observe retry pattern

**Expected Results**:
- ✅ Older messages retried first
- ✅ Increasing delays for older messages
- ✅ Maximum 30s delay enforced
- ✅ All messages eventually retried
- ✅ Success count returned

### 6. Read Status Updates ✅

#### Test 6.1: Mark Messages as Read
**Steps**:
1. Receive 5 unread messages
2. Open chat
3. Observe read status update

**Expected Results**:
- ✅ All messages marked as read
- ✅ Batch update performed
- ✅ readBy array includes current user
- ✅ Message status changed to READ
- ✅ Unread count updated to 0
- ✅ Chat list shows 0 unread

**Verification**:
```kotlin
// Check Firestore
messages.forEach { message ->
    assert(message.readBy.contains(currentUserId))
    assert(message.status == MessageStatus.READ)
}
```

#### Test 6.2: Read Status with Permission Error
**Steps**:
1. Modify rules to deny read status updates
2. Open chat with unread messages
3. Observe error handling

**Expected Results**:
- ✅ Error caught gracefully
- ✅ Warning logged (non-critical)
- ✅ UI continues to function
- ✅ No crash or freeze
- ✅ User not shown error message

#### Test 6.3: Partial Read Status Failure
**Steps**:
1. Mark 10 messages as read
2. Simulate failure on unread count update
3. Observe behavior

**Expected Results**:
- ✅ Messages marked as read successfully
- ✅ Unread count update failure logged
- ✅ Warning message in logs
- ✅ Operation considered successful
- ✅ UI shows correct state

### 7. Typing Status ✅

#### Test 7.1: Update Typing Status
**Steps**:
1. Open chat
2. Start typing
3. Observe typing indicator
4. Stop typing

**Expected Results**:
- ✅ Typing status updated to true
- ✅ Recipient sees typing indicator
- ✅ Status updated in real-time
- ✅ Typing status cleared after stop
- ✅ No UI blocking

#### Test 7.2: Typing Status with Permission Error
**Steps**:
1. Modify rules to deny typing status updates
2. Start typing in chat
3. Observe error handling

**Expected Results**:
- ✅ Error caught silently
- ✅ Warning logged
- ✅ No error shown to user
- ✅ Chat continues to function
- ✅ Message sending not affected

#### Test 7.3: Typing Status Non-Critical
**Steps**:
1. Simulate typing status failure
2. Send a message
3. Verify message sent successfully

**Expected Results**:
- ✅ Typing status failure doesn't block sending
- ✅ Message sent successfully
- ✅ No user-visible error
- ✅ Only logged for debugging

### 8. Concurrent Operations ✅

#### Test 8.1: Send Multiple Messages Quickly
**Steps**:
1. Send 10 messages in rapid succession
2. Observe queue and sending behavior

**Expected Results**:
- ✅ All messages queued
- ✅ Messages sent in order
- ✅ 500ms delay between sends
- ✅ No race conditions
- ✅ All messages eventually sent
- ✅ Correct order maintained

#### Test 8.2: Send While Processing Queue
**Steps**:
1. Queue 5 offline messages
2. Go online (start processing)
3. Send 3 new messages during processing
4. Observe behavior

**Expected Results**:
- ✅ Queued messages processed first
- ✅ New messages queued and sent
- ✅ No conflicts or race conditions
- ✅ All messages sent successfully
- ✅ Correct order maintained

### 9. Error Recovery ✅

#### Test 9.1: Recover from Network Error
**Steps**:
1. Send message while offline
2. Go online
3. Verify automatic recovery

**Expected Results**:
- ✅ Message automatically retried
- ✅ Sent successfully
- ✅ Status updated
- ✅ Queue cleared
- ✅ No user intervention needed

#### Test 9.2: Recover from Permission Error
**Steps**:
1. Send message with permission denied
2. Fix permissions
3. Manually retry message

**Expected Results**:
- ✅ Message marked as FAILED
- ✅ Manual retry option available
- ✅ Retry successful after fix
- ✅ Message sent
- ✅ Queue cleared

#### Test 9.3: Clear Failed Messages
**Steps**:
1. Create 5 failed messages
2. Call clearFailedMessages()
3. Verify cleanup

**Expected Results**:
- ✅ Failed messages removed from queue
- ✅ SENDING messages remain
- ✅ Queue size reduced
- ✅ UI updated

### 10. Performance Testing ✅

#### Test 10.1: Large Message Volume
**Steps**:
1. Queue 100 messages offline
2. Go online
3. Observe processing

**Expected Results**:
- ✅ All messages processed
- ✅ No memory issues
- ✅ No UI freezing
- ✅ Reasonable processing time
- ✅ All messages eventually sent

#### Test 10.2: Long-Running Queue
**Steps**:
1. Queue messages over 24 hours
2. Check queue persistence
3. Process old messages

**Expected Results**:
- ✅ Messages persist across time
- ✅ No data corruption
- ✅ Old messages still sendable
- ✅ Backoff delays appropriate
- ✅ No memory leaks

## Automated Testing

### Unit Tests
Run the test suite:
```bash
./gradlew :app:testDebugUnitTest --tests "ChatMessageSendingAndReadingTest"
```

### Test Coverage
- ✅ Permission error handling
- ✅ Network error handling
- ✅ Message validation
- ✅ Queue operations
- ✅ Retry logic
- ✅ Status tracking
- ✅ Error categorization

## Manual Testing Checklist

### Basic Functionality
- [ ] Send text message
- [ ] Receive text message
- [ ] Mark messages as read
- [ ] Update typing status
- [ ] View message history

### Error Handling
- [ ] Permission denied error
- [ ] Network error
- [ ] Validation error
- [ ] Unknown error
- [ ] Partial failure

### Offline Support
- [ ] Queue message offline
- [ ] Persist queue across restart
- [ ] Auto-retry when online
- [ ] Manual retry
- [ ] Clear failed messages

### Retry Logic
- [ ] Automatic retry
- [ ] Exponential backoff
- [ ] Max retry attempts
- [ ] Permission error no retry
- [ ] Network error retry

### Performance
- [ ] Multiple messages
- [ ] Large message volume
- [ ] Long-running queue
- [ ] Concurrent operations
- [ ] Memory usage

## Debugging Tools

### Check Queue Status
```kotlin
// Get queue statistics
val pendingCount = offlineQueue?.getPendingMessageCount()
val failedCount = offlineQueue?.getFailedMessageCount()
val allMessages = offlineQueue?.getQueuedMessages()

Log.d(TAG, "Queue status: $pendingCount pending, $failedCount failed")
```

### Monitor Message Status
```kotlin
// Observe message status changes
chatRepository.getChatMessages(chatId).collect { messages ->
    messages.forEach { message ->
        Log.d(TAG, "Message ${message.id}: ${message.status}")
    }
}
```

### Test Retry Logic
```kotlin
// Manually trigger retry
lifecycleScope.launch {
    val result = chatRepository.retryPendingMessagesWithBackoff()
    Log.d(TAG, "Retry result: ${result.getOrNull()} messages sent")
}
```

### Clear Queue
```kotlin
// Clear all queued messages (for testing)
chatRepository.clearOfflineQueue()
```

## Common Issues and Solutions

### Issue 1: Messages Not Sending
**Symptoms**: Messages stuck in SENDING status
**Causes**:
- Network connectivity issues
- Permission errors
- Firestore rules misconfigured

**Solutions**:
1. Check network connectivity
2. Verify Firestore rules
3. Check error logs
4. Try manual retry
5. Clear and resend

### Issue 2: Queue Not Persisting
**Symptoms**: Messages lost after app restart
**Causes**:
- SharedPreferences not working
- Serialization errors
- Storage permissions

**Solutions**:
1. Check storage permissions
2. Verify SharedPreferences access
3. Check Gson serialization
4. Review error logs

### Issue 3: Excessive Retries
**Symptoms**: Too many retry attempts
**Causes**:
- Backoff logic not working
- Max retries not enforced
- Permission errors retrying

**Solutions**:
1. Verify MAX_RETRY_ATTEMPTS constant
2. Check permission error detection
3. Review backoff calculation
4. Monitor retry logs

### Issue 4: Read Status Not Updating
**Symptoms**: Messages remain unread
**Causes**:
- Permission errors
- Batch update failures
- Network issues

**Solutions**:
1. Check Firestore rules
2. Verify batch update logic
3. Check error logs
4. Test with single message

## Success Criteria

### All Tests Pass ✅
- [ ] Basic message sending works
- [ ] Permission errors handled gracefully
- [ ] Offline queue functions correctly
- [ ] Retry logic works as expected
- [ ] Read status updates properly
- [ ] Typing status non-critical
- [ ] Performance acceptable
- [ ] No memory leaks
- [ ] No crashes

### User Experience ✅
- [ ] Messages send reliably
- [ ] Clear error messages
- [ ] Automatic recovery
- [ ] No UI blocking
- [ ] Smooth performance

### Code Quality ✅
- [ ] Comprehensive error handling
- [ ] Proper logging
- [ ] Clean code structure
- [ ] Good test coverage
- [ ] Documentation complete

## Conclusion

This testing guide provides comprehensive coverage of all chat message sending and reading functionality. Follow these tests to verify that:

1. ✅ Messages send reliably
2. ✅ Errors handled gracefully
3. ✅ Offline support works
4. ✅ Retry logic functions correctly
5. ✅ Performance is acceptable
6. ✅ User experience is smooth

All tests should pass before considering Task 12 complete.
