# Task 4: Typing Indicators and Read Receipts - Testing Guide

## Prerequisites
- Two devices or emulators with the app installed
- Two different user accounts logged in
- An existing chat between the two users

## Test Scenarios

### 1. Typing Indicator Tests

#### Test 1.1: Basic Typing Indicator
**Steps:**
1. Open the same chat on both devices (Device A and Device B)
2. On Device A, start typing in the message input field
3. On Device B, observe the area above the message input

**Expected Result:**
- Device B should show "typing..." indicator below the messages
- Indicator should appear within 1-2 seconds of typing

#### Test 1.2: Typing Timeout
**Steps:**
1. On Device A, type some text but don't send
2. Stop typing and wait for 2 seconds
3. Observe Device B

**Expected Result:**
- Typing indicator should disappear after 2 seconds of inactivity

#### Test 1.3: Typing Cleared on Send
**Steps:**
1. On Device A, start typing
2. Verify Device B shows typing indicator
3. On Device A, send the message
4. Observe Device B immediately

**Expected Result:**
- Typing indicator should disappear immediately when message is sent
- Message should appear in the chat

#### Test 1.4: Multiple Users Typing
**Steps:**
1. Use 3 devices in a group chat
2. Have Device A and Device B both start typing
3. Observe Device C

**Expected Result:**
- Device C should show "2 people are typing..." or similar

#### Test 1.5: Self-Typing Not Shown
**Steps:**
1. On Device A, start typing
2. Observe Device A (same device)

**Expected Result:**
- Device A should NOT show its own typing indicator

### 2. Read Receipt Tests

#### Test 2.1: Message Sending Status
**Steps:**
1. On Device A, send a message
2. Immediately observe the message bubble

**Expected Result:**
- Message should show a clock icon or single checkmark (white)
- Icon should be next to the timestamp

#### Test 2.2: Message Sent Status
**Steps:**
1. Send a message from Device A
2. Wait for it to reach Firestore (usually instant)
3. Observe the checkmark icon

**Expected Result:**
- Should show single checkmark (white) indicating "sent"

#### Test 2.3: Message Read Status
**Steps:**
1. Send a message from Device A
2. On Device B, open the chat and view the message
3. On Device A, observe the message status

**Expected Result:**
- Checkmark should change to double checkmark (blue) indicating "read"
- Change should happen in real-time (within 1-2 seconds)

#### Test 2.4: Multiple Messages Read
**Steps:**
1. From Device A, send 5 messages quickly
2. On Device B, open the chat
3. On Device A, observe all message statuses

**Expected Result:**
- All visible messages should update to double checkmark (blue)
- Updates should happen in real-time

#### Test 2.5: Offline Message Status
**Steps:**
1. Turn off internet on Device A
2. Try to send a message
3. Observe the message status

**Expected Result:**
- Message should show clock icon or "sending" status
- When internet is restored, should update to sent/delivered

### 3. Integration Tests

#### Test 3.1: Typing While Receiving Messages
**Steps:**
1. On Device A, start typing
2. On Device B, send a message
3. Observe both devices

**Expected Result:**
- Device A should receive the message while typing
- Device B should still show typing indicator
- No crashes or UI glitches

#### Test 3.2: Read Receipts in Group Chat
**Steps:**
1. Create a group chat with 3+ users
2. Send a message from Device A
3. Have Device B and Device C read the message
4. Observe Device A

**Expected Result:**
- Message should show double checkmark (blue) when at least one person reads it
- Status should update as more people read

#### Test 3.3: Rapid Typing and Sending
**Steps:**
1. On Device A, rapidly type and send multiple messages
2. Observe Device B

**Expected Result:**
- Typing indicator should appear and disappear correctly
- All messages should be received
- Read receipts should work for all messages

#### Test 3.4: App Backgrounding
**Steps:**
1. On Device A, start typing
2. Press home button to background the app
3. Observe Device B

**Expected Result:**
- Typing indicator should disappear when app is backgrounded
- No memory leaks or crashes

### 4. Edge Cases

#### Test 4.1: Empty Chat
**Steps:**
1. Create a new chat with no messages
2. Start typing
3. Observe the other device

**Expected Result:**
- Typing indicator should still work correctly

#### Test 4.2: Long Message
**Steps:**
1. Type a very long message (500+ characters)
2. Observe typing indicator on other device
3. Send the message

**Expected Result:**
- Typing indicator should work normally
- Message should send successfully
- Read receipts should work

#### Test 4.3: Network Interruption
**Steps:**
1. Start typing on Device A
2. Turn off WiFi/data mid-typing
3. Turn it back on
4. Observe Device B

**Expected Result:**
- Typing indicator should handle network interruption gracefully
- Should resume working when connection is restored

## Visual Verification

### Typing Indicator Appearance
- [ ] Text is italic and gray/secondary color
- [ ] Positioned above message input area
- [ ] Doesn't overlap with messages or input
- [ ] Smooth show/hide transitions

### Read Receipt Icons
- [ ] Icons are properly sized (16dp x 16dp)
- [ ] Icons are aligned with timestamp
- [ ] Colors are correct:
  - White for sending/sent
  - Blue for read
  - Red for failed
- [ ] Icons don't overlap with text
- [ ] Icons are visible on message background

## Performance Checks

### Typing Indicator Performance
- [ ] No lag when typing
- [ ] Indicator appears quickly (< 2 seconds)
- [ ] No excessive Firestore writes (check Firebase console)
- [ ] Timer cleanup works (no memory leaks)

### Read Receipt Performance
- [ ] Status updates happen in real-time
- [ ] No lag when scrolling through messages
- [ ] Batch read operations work efficiently
- [ ] No excessive Firestore reads

## Firestore Data Verification

### Check Firestore Console

#### Typing Status Collection
Navigate to: `chats/{chatId}/typing_status/{userId}`

Should contain:
```json
{
  "userId": "user123",
  "isTyping": true,
  "timestamp": 1234567890
}
```

#### Message Document
Navigate to: `chats/{chatId}/messages/{messageId}`

Should contain:
```json
{
  "id": "msg123",
  "text": "Hello",
  "senderId": "user123",
  "readBy": ["user123", "user456"],
  "status": "READ",
  "timestamp": "..."
}
```

## Common Issues and Solutions

### Issue: Typing indicator not showing
**Solution:**
- Check Firestore rules allow writing to typing_status subcollection
- Verify both users are in the same chat
- Check network connectivity

### Issue: Read receipts not updating
**Solution:**
- Verify messages are being marked as read in Firestore
- Check that readBy array is being updated
- Ensure real-time listeners are active

### Issue: Icons not displaying
**Solution:**
- Verify drawable resources exist (ic_check.xml, ic_check_double.xml)
- Check that ImageView is visible in layout
- Verify color filters are being applied

### Issue: Typing indicator stuck
**Solution:**
- Check that timer is properly cleaning up
- Verify onDestroy() is being called
- Check for any exceptions in logs

## Success Criteria

All tests should pass with:
- ✅ Typing indicators appear and disappear correctly
- ✅ Read receipts show correct status
- ✅ Real-time updates work smoothly
- ✅ No crashes or memory leaks
- ✅ UI is responsive and polished
- ✅ Firestore data is correct

## Next Steps

After successful testing:
1. Mark Task 4 as complete ✅
2. Proceed to Task 5: Add user search for direct messages
3. Document any issues found for future improvements
