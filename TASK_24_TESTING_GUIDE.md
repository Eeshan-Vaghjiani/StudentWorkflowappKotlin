# Task 24: Message Status Indicators - Testing Guide

## Prerequisites
- Android device or emulator running API 23+
- Two test accounts for testing read receipts
- Internet connection (and ability to toggle offline mode)

## Build Instructions

### Step 1: Sync and Build
```bash
# Close Android Studio first if you encounter file locks
./gradlew clean assembleDebug
```

### Step 2: Install on Device
```bash
./gradlew installDebug
```

Or use Android Studio:
1. Click "Sync Project with Gradle Files"
2. Click "Build" → "Rebuild Project"
3. Click "Run" to install on device

## Test Scenarios

### Test 1: Basic Message Sending
**Objective**: Verify status changes from SENDING to SENT

**Steps:**
1. Open the app and log in
2. Navigate to a chat (or create a new one)
3. Type a message and send it
4. Observe the status indicator

**Expected Result:**
- ⏰ Clock icon appears briefly (SENDING)
- ✓ Single checkmark appears (SENT)
- Icon is white with 70% opacity
- Icon appears to the right of the timestamp

**Pass/Fail:** ___

---

### Test 2: Read Receipts
**Objective**: Verify status changes to READ when recipient opens chat

**Steps:**
1. Send a message from Account A to Account B
2. Verify single checkmark appears on Account A
3. Open the chat on Account B's device
4. Switch back to Account A's device
5. Observe the status indicator

**Expected Result:**
- ✓✓ Double checkmark appears (READ)
- Checkmarks are blue (holo_blue_light)
- Icon has 100% opacity
- Change happens in real-time (within 1-2 seconds)

**Pass/Fail:** ___

---

### Test 3: Failed Message with Retry
**Objective**: Verify FAILED status and retry functionality

**Steps:**
1. Turn on Airplane Mode on your device
2. Try to send a message
3. Observe the status indicator
4. Tap on the error icon
5. Turn off Airplane Mode
6. Observe the message status

**Expected Result:**
- ⚠️ Red error icon appears (FAILED)
- Icon is clickable
- Tapping icon attempts to resend
- After reconnecting, message sends successfully
- Status changes to SENT then READ

**Pass/Fail:** ___

---

### Test 4: Received Messages
**Objective**: Verify status indicators don't show for received messages

**Steps:**
1. Send a message from Account B to Account A
2. Open chat on Account A
3. Observe the received message

**Expected Result:**
- No status indicator appears on received messages
- Only sent messages show status indicators
- Received messages show sender's profile picture instead

**Pass/Fail:** ___

---

### Test 5: Multiple Messages
**Objective**: Verify each message has independent status

**Steps:**
1. Send 3 messages quickly in succession
2. Observe status indicators for all messages

**Expected Result:**
- Each message has its own status indicator
- All show SENDING briefly, then SENT
- When recipient reads, all change to READ
- Status updates happen independently

**Pass/Fail:** ___

---

### Test 6: Image and Document Messages
**Objective**: Verify status indicators work with attachments

**Steps:**
1. Send an image message
2. Send a document message
3. Send a message with text + image
4. Observe status indicators

**Expected Result:**
- Status indicators appear for all message types
- Positioned consistently below content
- Status updates work the same as text messages

**Pass/Fail:** ___

---

### Test 7: Real-Time Updates
**Objective**: Verify status updates without manual refresh

**Steps:**
1. Send a message from Account A
2. Keep chat open on Account A
3. Open chat on Account B (don't close Account A)
4. Watch Account A's screen

**Expected Result:**
- Status changes from SENT to READ automatically
- No need to scroll or refresh
- Update happens within 1-2 seconds
- Smooth transition between states

**Pass/Fail:** ___

---

### Test 8: Offline Queue
**Objective**: Verify status for queued offline messages

**Steps:**
1. Turn on Airplane Mode
2. Send 3 messages
3. Observe status indicators
4. Turn off Airplane Mode
5. Watch messages send

**Expected Result:**
- All messages show SENDING status while offline
- Messages remain in queue with SENDING status
- When online, messages send one by one
- Each changes to SENT as it completes
- If any fail, they show FAILED with retry option

**Pass/Fail:** ___

---

### Test 9: Visual Consistency
**Objective**: Verify icons are properly sized and aligned

**Steps:**
1. Send various types of messages
2. Check status indicator appearance
3. Compare with timestamp alignment

**Expected Result:**
- Icons are 16dp x 16dp
- Properly aligned with timestamp
- 4dp margin from timestamp
- Icons are clear and recognizable
- No clipping or overlap

**Pass/Fail:** ___

---

### Test 10: Accessibility
**Objective**: Verify accessibility features

**Steps:**
1. Enable TalkBack on device
2. Navigate to a sent message
3. Focus on the status indicator
4. Listen to the content description

**Expected Result:**
- Status indicator has proper content description
- TalkBack announces "Message status indicator"
- Icon is focusable
- Failed messages announce they're clickable

**Pass/Fail:** ___

---

## Visual Reference

### Status Icons

```
SENDING:  ⏰ (Clock - White 70%)
SENT:     ✓  (Single Check - White 70%)
DELIVERED: ✓✓ (Double Check - White 70%)
READ:     ✓✓ (Double Check - Blue 100%)
FAILED:   ⚠️  (Error - Red)
```

### Layout Position

```
┌─────────────────────────────┐
│ [Message Text]              │
│                             │
│ 2:30 PM ✓✓                 │
│         ↑                   │
│         Status Indicator    │
└─────────────────────────────┘
```

## Common Issues and Solutions

### Issue 1: Icons Not Showing
**Symptoms:** Status indicator area is blank
**Solution:** 
- Rebuild project: `./gradlew clean assembleDebug`
- Sync Gradle files in Android Studio
- Check that drawable files exist in `res/drawable/`

### Issue 2: Status Not Updating
**Symptoms:** Status stays on SENDING or SENT
**Solution:**
- Check internet connection
- Verify Firestore listeners are active
- Check ChatRepository is updating status field
- Restart the app

### Issue 3: Wrong Icon Displayed
**Symptoms:** Blue checkmark when should be white, etc.
**Solution:**
- Check MessageStatus enum value in Firestore
- Verify MessageStatusView.updateStatus() logic
- Check color filter application

### Issue 4: Status Shows on Received Messages
**Symptoms:** Status indicator appears on left-side messages
**Solution:**
- Verify `setMessage()` checks `senderId != currentUserId`
- Check that received message layout doesn't include MessageStatusView

### Issue 5: Retry Not Working
**Symptoms:** Tapping error icon does nothing
**Solution:**
- Verify `setOnRetryClickListener()` is called in MessageAdapter
- Check that `onRetryMessage` callback is passed to adapter
- Verify ChatRoomActivity handles retry logic

## Performance Checks

### Memory Usage
- Open Android Profiler
- Monitor memory while sending 50+ messages
- Verify no memory leaks from status updates

### Battery Impact
- Monitor battery usage during active chat session
- Real-time updates should not drain battery excessively

### Network Usage
- Status updates should use existing Firestore listeners
- No additional network calls for status checks

## Regression Testing

Verify these existing features still work:
- ✅ Sending text messages
- ✅ Sending image messages
- ✅ Sending document messages
- ✅ Typing indicators
- ✅ Message grouping
- ✅ Timestamp headers
- ✅ Profile pictures
- ✅ Link detection
- ✅ Message pagination

## Sign-Off

**Tester Name:** _______________
**Date:** _______________
**Device/Emulator:** _______________
**Android Version:** _______________
**App Version:** _______________

**Overall Result:** PASS / FAIL

**Notes:**
_______________________________________
_______________________________________
_______________________________________

## Next Steps After Testing

If all tests pass:
- ✅ Mark task as complete
- ✅ Commit changes to version control
- ✅ Move to next task (Task 25: Message grouping)

If tests fail:
- Document failures in detail
- Create bug reports with reproduction steps
- Fix issues and retest
- Update implementation as needed
