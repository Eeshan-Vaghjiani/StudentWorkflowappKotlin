# Task 28: Offline Message Queue - Testing Guide

## Prerequisites

Before testing, ensure:
- ✅ App is installed on a physical device or emulator
- ✅ You have at least one chat conversation
- ✅ You can toggle airplane mode or disable WiFi/mobile data
- ✅ Firebase is properly configured

## Test Scenarios

### Test 1: Send Message While Online

**Objective:** Verify normal message sending works correctly

**Steps:**
1. Open the app and navigate to a chat
2. Ensure you have internet connection (WiFi or mobile data)
3. Type a message: "Test message online"
4. Click send button
5. Observe the message

**Expected Results:**
- ✅ Message appears immediately in chat
- ✅ Message shows clock icon briefly (SENDING)
- ✅ Message shows single checkmark (SENT)
- ✅ No connection banner visible
- ✅ Message delivered to Firestore

**Pass/Fail:** ___________

---

### Test 2: Send Message While Offline

**Objective:** Verify messages are queued when offline

**Steps:**
1. Open the app and navigate to a chat
2. Enable airplane mode on your device
3. Wait 2-3 seconds for connection to drop
4. Type a message: "Test message offline"
5. Click send button
6. Observe the message and UI

**Expected Results:**
- ✅ Orange connection banner appears: "No internet connection"
- ✅ Message appears in chat with clock icon (SENDING status)
- ✅ Message stays in SENDING state
- ✅ No error message shown
- ✅ Message input clears after send

**Pass/Fail:** ___________

---

### Test 3: Auto-Send When Connection Restored

**Objective:** Verify queued messages send automatically when online

**Steps:**
1. Complete Test 2 (have a queued message)
2. Disable airplane mode
3. Wait for connection to restore (2-5 seconds)
4. Observe the message and connection banner

**Expected Results:**
- ✅ Connection banner changes to green: "Connected"
- ✅ Green banner disappears after 2 seconds
- ✅ Queued message(s) automatically sent
- ✅ Message status changes from clock to checkmark
- ✅ Message appears on other devices/accounts

**Pass/Fail:** ___________

---

### Test 4: Multiple Messages Queued

**Objective:** Verify multiple messages can be queued and sent

**Steps:**
1. Enable airplane mode
2. Send 5 different messages:
   - "Message 1"
   - "Message 2"
   - "Message 3"
   - "Message 4"
   - "Message 5"
3. Verify all show SENDING status
4. Disable airplane mode
5. Wait for connection to restore

**Expected Results:**
- ✅ All 5 messages appear with clock icon while offline
- ✅ All messages remain visible in chat
- ✅ When online, all messages sent in order
- ✅ All messages change to checkmark status
- ✅ Message order preserved

**Pass/Fail:** ___________

---

### Test 5: Queue Persists Across App Restart

**Objective:** Verify queued messages survive app restart

**Steps:**
1. Enable airplane mode
2. Send a message: "Persistent message"
3. Verify message shows SENDING status
4. Force close the app (swipe away from recent apps)
5. Reopen the app
6. Navigate to the same chat
7. Observe the message

**Expected Results:**
- ✅ Message still visible in chat
- ✅ Message still shows SENDING status (clock icon)
- ✅ Connection banner still shows offline
- ✅ Message not lost

**Pass/Fail:** ___________

---

### Test 6: Send After App Restart

**Objective:** Verify queued messages send after app restart

**Steps:**
1. Complete Test 5 (have a persistent queued message)
2. Disable airplane mode (while app is open)
3. Wait for connection to restore
4. Observe the message

**Expected Results:**
- ✅ Connection banner shows "Connected"
- ✅ Queued message automatically sent
- ✅ Message status changes to checkmark
- ✅ Message delivered to Firestore

**Pass/Fail:** ___________

---

### Test 7: Failed Message Retry

**Objective:** Verify manual retry works for failed messages

**Steps:**
1. Enable airplane mode
2. Send a message: "Test retry"
3. Keep airplane mode on for 30 seconds
4. Long-press the message with clock icon
5. Select "Retry" from context menu
6. Confirm retry in dialog
7. Disable airplane mode
8. Observe the message

**Expected Results:**
- ✅ Long-press shows context menu
- ✅ "Retry" option available
- ✅ Confirmation dialog appears
- ✅ After confirming, message attempts to send
- ✅ When online, message sends successfully
- ✅ Status changes to checkmark

**Pass/Fail:** ___________

---

### Test 8: Connection Banner Behavior

**Objective:** Verify connection status banner displays correctly

**Steps:**
1. Open a chat while online
2. Verify no banner visible
3. Enable airplane mode
4. Observe banner appearance
5. Wait 5 seconds
6. Disable airplane mode
7. Observe banner changes

**Expected Results:**
- ✅ No banner when online initially
- ✅ Orange banner appears when offline
- ✅ Banner text: "No internet connection"
- ✅ Banner persists while offline
- ✅ Green banner appears when connected
- ✅ Banner text changes to "Connected"
- ✅ Green banner disappears after 2 seconds

**Pass/Fail:** ___________

---

### Test 9: Send Image While Offline

**Objective:** Verify image messages are queued when offline

**Steps:**
1. Enable airplane mode
2. Open a chat
3. Click attachment button
4. Select "Gallery"
5. Choose an image
6. Observe the upload process

**Expected Results:**
- ✅ Connection banner shows offline
- ✅ Image upload fails gracefully
- ✅ Error message shown (upload requires connection)
- ✅ No app crash

**Note:** Image uploads require connection for Firebase Storage. Text messages are queued, but media uploads need to be attempted when online.

**Pass/Fail:** ___________

---

### Test 10: Multiple Chats with Queued Messages

**Objective:** Verify queue works across multiple chats

**Steps:**
1. Enable airplane mode
2. Open Chat A, send message: "Chat A message"
3. Go back to chat list
4. Open Chat B, send message: "Chat B message"
5. Go back to chat list
6. Open Chat C, send message: "Chat C message"
7. Disable airplane mode
8. Wait for connection
9. Check all three chats

**Expected Results:**
- ✅ All messages queued in respective chats
- ✅ All messages show SENDING status
- ✅ When online, all messages sent to correct chats
- ✅ No messages mixed between chats
- ✅ All messages delivered successfully

**Pass/Fail:** ___________

---

### Test 11: Connection Toggle Rapidly

**Objective:** Verify system handles rapid connection changes

**Steps:**
1. Open a chat
2. Enable airplane mode
3. Send a message
4. Wait 2 seconds
5. Disable airplane mode
6. Wait 2 seconds
7. Enable airplane mode again
8. Send another message
9. Disable airplane mode
10. Observe both messages

**Expected Results:**
- ✅ First message queued when offline
- ✅ First message sent when online
- ✅ Second message queued when offline again
- ✅ Second message sent when online again
- ✅ No duplicate messages
- ✅ No app crashes or freezes

**Pass/Fail:** ___________

---

### Test 12: Queue Limit (Stress Test)

**Objective:** Verify system handles many queued messages

**Steps:**
1. Enable airplane mode
2. Send 20 messages rapidly (use copy-paste)
3. Verify all messages appear
4. Disable airplane mode
5. Wait for all messages to send
6. Verify on another device

**Expected Results:**
- ✅ All 20 messages queued successfully
- ✅ All messages visible in chat
- ✅ All messages show SENDING status
- ✅ When online, all messages sent
- ✅ All messages delivered in order
- ✅ No messages lost
- ✅ No performance issues

**Pass/Fail:** ___________

---

## Edge Cases

### Edge Case 1: Send While Connection Dropping

**Steps:**
1. Start with good connection
2. Click send button
3. Immediately enable airplane mode (within 1 second)
4. Observe behavior

**Expected Results:**
- ✅ Message either sends successfully or queues
- ✅ No duplicate messages
- ✅ No app crash

**Pass/Fail:** ___________

---

### Edge Case 2: Clear App Data

**Steps:**
1. Queue messages while offline
2. Go to Settings > Apps > Your App
3. Clear app data
4. Reopen app and login
5. Check chat

**Expected Results:**
- ✅ Queued messages lost (expected - data cleared)
- ✅ App works normally after data clear
- ✅ No crashes

**Pass/Fail:** ___________

---

### Edge Case 3: Low Storage

**Steps:**
1. Fill device storage to near capacity
2. Enable airplane mode
3. Try to send messages
4. Observe behavior

**Expected Results:**
- ✅ Messages queue if storage available
- ✅ Graceful error if storage full
- ✅ No app crash

**Pass/Fail:** ___________

---

## Performance Tests

### Performance Test 1: Queue Load Time

**Objective:** Verify queued messages load quickly

**Steps:**
1. Queue 10 messages while offline
2. Close and reopen app
3. Navigate to chat
4. Measure time to display messages

**Expected Results:**
- ✅ Messages appear within 1 second
- ✅ No noticeable lag
- ✅ Smooth scrolling

**Pass/Fail:** ___________

---

### Performance Test 2: Send Speed

**Objective:** Verify queued messages send quickly when online

**Steps:**
1. Queue 10 messages while offline
2. Disable airplane mode
3. Measure time for all messages to send

**Expected Results:**
- ✅ All messages sent within 5 seconds
- ✅ No UI freezing
- ✅ Status updates visible

**Pass/Fail:** ___________

---

## Regression Tests

### Regression Test 1: Normal Sending Still Works

**Objective:** Verify online sending not affected

**Steps:**
1. Ensure online connection
2. Send 5 messages normally
3. Verify all send immediately

**Expected Results:**
- ✅ Messages send instantly
- ✅ No queueing when online
- ✅ Checkmarks appear quickly

**Pass/Fail:** ___________

---

### Regression Test 2: Other Features Work

**Objective:** Verify other chat features unaffected

**Steps:**
1. Test typing indicator
2. Test read receipts
3. Test message deletion
4. Test image sending (while online)

**Expected Results:**
- ✅ All features work normally
- ✅ No regressions introduced

**Pass/Fail:** ___________

---

## Summary

**Total Tests:** 17
**Tests Passed:** ___________
**Tests Failed:** ___________
**Pass Rate:** ___________%

## Issues Found

| Test # | Issue Description | Severity | Status |
|--------|------------------|----------|--------|
|        |                  |          |        |
|        |                  |          |        |
|        |                  |          |        |

## Notes

- Test on both WiFi and mobile data
- Test on different Android versions (API 23+)
- Test with different network speeds
- Test with Firebase emulator if available

## Sign-off

**Tester Name:** ___________________
**Date:** ___________________
**Signature:** ___________________
