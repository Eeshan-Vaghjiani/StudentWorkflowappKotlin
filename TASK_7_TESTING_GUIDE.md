# Task 7: Offline Message Queue - Testing Guide

## Prerequisites
- App installed on Android device or emulator
- At least two user accounts for testing
- Ability to control network connectivity (airplane mode or network settings)

## Test Scenarios

### Test 1: Queue Messages When Offline

**Steps:**
1. Open the app and navigate to a chat
2. Turn on airplane mode on your device
3. Type a message and tap send
4. Observe the message in the chat

**Expected Results:**
- ✅ Message appears in chat immediately
- ✅ Message shows clock icon (SENDING status)
- ✅ Toast notification appears: "No internet connection. Messages will be sent when connection is restored."
- ✅ Message remains in chat with SENDING status

**What to Check:**
- Message text is correct
- Timestamp is shown
- Clock icon is visible on the right side
- Message is aligned to the right (sent message)

---

### Test 2: Auto-Send When Connection Restored

**Steps:**
1. With messages queued from Test 1, turn off airplane mode
2. Wait a few seconds
3. Observe the queued messages

**Expected Results:**
- ✅ Messages automatically change from clock icon to checkmark
- ✅ Messages are sent to Firestore
- ✅ Other user receives the messages
- ✅ Messages show SENT status (single checkmark)

**What to Check:**
- All queued messages are sent
- Messages appear in correct order
- Timestamps are preserved
- No duplicate messages

---

### Test 3: Failed Message After Retry Attempts

**Steps:**
1. Turn on airplane mode
2. Send a message (it will be queued)
3. Turn off airplane mode briefly (1-2 seconds)
4. Turn airplane mode back on before message sends
5. Repeat step 3-4 several times to exhaust retry attempts

**Expected Results:**
- ✅ After 3 failed attempts, message shows error icon (red)
- ✅ Message status changes to FAILED
- ✅ Message remains in chat

**What to Check:**
- Error icon is red and visible
- Message is still readable
- Message can be tapped

---

### Test 4: Manual Retry of Failed Message

**Steps:**
1. With a failed message from Test 3, ensure you have internet connection
2. Tap on the failed message
3. Observe the dialog that appears
4. Tap "Retry" button

**Expected Results:**
- ✅ Confirmation dialog appears with title "Retry Message"
- ✅ Dialog asks "Do you want to retry sending this message?"
- ✅ Dialog has "Retry" and "Cancel" buttons
- ✅ After tapping Retry, message status changes to SENDING
- ✅ Message is successfully sent
- ✅ Message shows checkmark (SENT status)

**What to Check:**
- Dialog is clear and user-friendly
- Cancel button dismisses dialog without action
- Retry button attempts to send message
- Message updates correctly after retry

---

### Test 5: Multiple Messages Queued

**Steps:**
1. Turn on airplane mode
2. Send 5 different messages
3. Observe all messages in chat
4. Turn off airplane mode
5. Wait for all messages to send

**Expected Results:**
- ✅ All 5 messages appear with clock icons
- ✅ All messages show SENDING status
- ✅ When connection restored, all messages are sent
- ✅ Messages are sent in correct order
- ✅ All messages show checkmarks

**What to Check:**
- Message order is preserved
- No messages are lost
- No duplicate messages
- All messages update to SENT status

---

### Test 6: Queue Persistence Across App Restarts

**Steps:**
1. Turn on airplane mode
2. Send 2-3 messages
3. Close the app completely (swipe away from recent apps)
4. Reopen the app
5. Navigate to the same chat
6. Observe the queued messages
7. Turn off airplane mode

**Expected Results:**
- ✅ Queued messages still appear in chat after restart
- ✅ Messages still show SENDING status
- ✅ When connection restored, messages are sent
- ✅ Messages update to SENT status

**What to Check:**
- Messages persist in SharedPreferences
- Message content is intact
- Timestamps are correct
- Messages are sent after connection restored

---

### Test 7: Connection Status Indicator

**Steps:**
1. Open a chat with internet connection
2. Turn on airplane mode
3. Observe any notifications or indicators
4. Turn off airplane mode
5. Observe any changes

**Expected Results:**
- ✅ When offline, toast shows "No internet connection" message
- ✅ When online, no special indicator (normal operation)

**What to Check:**
- Toast message is clear and helpful
- Toast doesn't block UI
- Connection status is detected quickly

---

### Test 8: Mixed Online and Offline Messages

**Steps:**
1. With internet connection, send 2 messages
2. Turn on airplane mode
3. Send 2 more messages
4. Turn off airplane mode
5. Send 2 more messages

**Expected Results:**
- ✅ First 2 messages send immediately (checkmarks)
- ✅ Next 2 messages show clock icons (queued)
- ✅ When connection restored, queued messages send
- ✅ Last 2 messages send immediately
- ✅ All messages appear in correct order

**What to Check:**
- Message order is correct
- Status indicators are appropriate for each message
- No messages are lost or duplicated

---

### Test 9: Queued Messages in Different Chats

**Steps:**
1. Turn on airplane mode
2. Open Chat A and send a message
3. Navigate to Chat B and send a message
4. Navigate to Chat C and send a message
5. Turn off airplane mode
6. Check all three chats

**Expected Results:**
- ✅ Each chat shows its queued message
- ✅ All messages are sent when connection restored
- ✅ Messages appear in correct chats
- ✅ No cross-chat message mixing

**What to Check:**
- Messages are in correct chats
- Each chat's queue is independent
- All messages are sent successfully

---

### Test 10: Cancel Retry Dialog

**Steps:**
1. Create a failed message (see Test 3)
2. Tap on the failed message
3. Tap "Cancel" button in dialog

**Expected Results:**
- ✅ Dialog dismisses
- ✅ Message remains in FAILED status
- ✅ No retry attempt is made
- ✅ Message can be tapped again later

**What to Check:**
- Cancel button works correctly
- Message state doesn't change
- User can retry later if desired

---

## Edge Cases to Test

### Edge Case 1: Very Long Message
- Send a very long message (1000+ characters) while offline
- Verify it queues and sends correctly

### Edge Case 2: Special Characters
- Send messages with emojis, special characters while offline
- Verify they are preserved correctly

### Edge Case 3: Rapid Connection Changes
- Toggle airplane mode on/off rapidly while messages are queued
- Verify messages eventually send without duplicates

### Edge Case 4: Low Memory
- Queue many messages (20+) while offline
- Verify all are stored and sent correctly

### Edge Case 5: App Killed While Sending
- Queue messages, turn on connection
- Kill app immediately while messages are sending
- Reopen app and verify messages are handled correctly

---

## Common Issues and Solutions

### Issue: Messages not sending after connection restored
**Solution:** 
- Check if ConnectionMonitor is working correctly
- Verify processQueuedMessages() is called
- Check Firestore permissions

### Issue: Duplicate messages
**Solution:**
- Verify messages are removed from queue after successful send
- Check message ID uniqueness

### Issue: Messages lost after app restart
**Solution:**
- Verify SharedPreferences is saving correctly
- Check JSON serialization/deserialization

### Issue: Failed status not showing
**Solution:**
- Verify retry count is being tracked
- Check MessageAdapter status icon logic

---

## Performance Checks

1. **Queue Size:** Test with 50+ queued messages
2. **Send Speed:** Verify messages send quickly when online
3. **UI Responsiveness:** Ensure UI doesn't freeze during queue processing
4. **Memory Usage:** Monitor memory with many queued messages
5. **Battery Impact:** Check battery usage with connection monitoring

---

## Accessibility Testing

1. **Screen Reader:** Verify message status is announced
2. **High Contrast:** Check status icons are visible
3. **Large Text:** Verify layout works with large text sizes
4. **Touch Targets:** Ensure failed messages are easy to tap

---

## Success Criteria

All tests pass with:
- ✅ No crashes or errors
- ✅ Messages queue correctly when offline
- ✅ Messages send automatically when online
- ✅ Failed messages can be retried manually
- ✅ Message order is preserved
- ✅ No duplicate or lost messages
- ✅ Queue persists across app restarts
- ✅ UI is responsive and clear
- ✅ Connection status is accurately detected

---

## Reporting Issues

When reporting issues, include:
1. Device model and Android version
2. Steps to reproduce
3. Expected vs actual behavior
4. Screenshots or screen recording
5. Logcat output (filter by "ChatRepository", "OfflineMessageQueue", "ConnectionMonitor")

---

## Next Steps After Testing

Once all tests pass:
1. Mark Task 7 as complete ✅
2. Proceed to Task 8: Set up Firebase Cloud Messaging service
3. Document any issues found for future improvement
4. Consider additional features (e.g., retry count display, queue size indicator)
