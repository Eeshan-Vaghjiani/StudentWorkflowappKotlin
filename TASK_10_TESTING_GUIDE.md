# Task 10: Chat Functionality - Testing Guide

## Pre-Testing Checklist

Before testing, ensure:
- [ ] Firestore security rules are deployed
- [ ] App is built with latest changes
- [ ] At least 2 test accounts are available
- [ ] Test group exists with multiple members
- [ ] Logcat is visible for monitoring errors

## Test Suite 1: Group Chat Creation and Access

### Test 1.1: Create Group Chat
**Objective:** Verify group chats are created with all members in participants array

**Steps:**
1. Login as User A
2. Navigate to Groups tab
3. Create a new group "Test Group Chat"
4. Add User B and User C as members
5. Navigate to Chat tab
6. Verify "Test Group Chat" appears in chat list

**Expected Results:**
- ✅ Group chat appears in chat list
- ✅ No permission errors in logcat
- ✅ Chat shows group name correctly

**Logcat Check:**
```
Look for: "getOrCreateGroupChat: Successfully created chat [chatId] for group [groupId] with [X] participants"
Should show: Number of participants matches number of group members
```

### Test 1.2: Verify Participants Array in Firestore
**Objective:** Confirm all members are in participants array

**Steps:**
1. Open Firebase Console
2. Navigate to Firestore Database
3. Open `chats` collection
4. Find the chat document for "Test Group Chat"
5. Check the `participants` field

**Expected Results:**
- ✅ `participants` array contains all member IDs
- ✅ `participants` array length matches group member count
- ✅ `groupId` field matches the group document ID

**Example Document:**
```json
{
  "chatId": "abc123",
  "type": "GROUP",
  "participants": ["userId1", "userId2", "userId3"],
  "groupId": "groupId123",
  "groupName": "Test Group Chat",
  "lastMessage": "",
  "lastMessageTime": "2025-01-18T10:00:00Z"
}
```

### Test 1.3: All Members Can Access Group Chat
**Objective:** Verify all group members can open and view the chat

**Steps:**
1. Login as User A
2. Open "Test Group Chat"
3. Verify chat opens without errors
4. Logout and login as User B
5. Navigate to Chat tab
6. Open "Test Group Chat"
7. Verify chat opens without errors
8. Repeat for User C

**Expected Results:**
- ✅ All users can see the group chat in their chat list
- ✅ All users can open the chat
- ✅ No PERMISSION_DENIED errors in logcat

**Logcat Check:**
```
Should NOT see: "PERMISSION_DENIED: User does not have permission to read messages"
Should see: "getChatMessages: Received [X] messages"
```

## Test Suite 2: Sending Messages

### Test 2.1: Send Message in Group Chat
**Objective:** Verify messages can be sent and received in group chats

**Steps:**
1. Login as User A
2. Open "Test Group Chat"
3. Type message: "Hello from User A"
4. Click send button
5. Verify message appears in chat
6. Logout and login as User B
7. Open "Test Group Chat"
8. Verify User A's message is visible

**Expected Results:**
- ✅ Message sends successfully
- ✅ Message appears immediately for sender
- ✅ Message appears for other participants
- ✅ No errors in logcat

**Logcat Check:**
```
Should see: "sendMessage: Message sent successfully"
Should NOT see: "Permission denied: You don't have access to send messages"
```

### Test 2.2: Send Message in Direct Chat
**Objective:** Verify direct chat messaging works

**Steps:**
1. Login as User A
2. Navigate to Chat tab
3. Click "New Chat" button
4. Search for User B
5. Start chat with User B
6. Send message: "Hello User B"
7. Verify message appears
8. Login as User B
9. Open chat with User A
10. Verify message is visible

**Expected Results:**
- ✅ Direct chat created successfully
- ✅ Message sends without errors
- ✅ Both users can see the message
- ✅ participants array contains both user IDs

### Test 2.3: Message Retry on Permission Error
**Objective:** Verify permission errors are handled correctly

**Steps:**
1. Login as User A
2. Try to send message in a chat where User A is not a participant (if possible)
3. Observe error handling

**Expected Results:**
- ✅ Error message displayed to user
- ✅ Message marked as failed
- ✅ No retry attempts for permission errors
- ✅ Proper error logged in logcat

**Logcat Check:**
```
Should see: "PERMISSION_DENIED: User does not have permission to send messages"
Should see: "Permission denied: You don't have access to send messages in this chat"
```

## Test Suite 3: Loading Messages

### Test 3.1: Load Existing Messages
**Objective:** Verify messages load correctly when opening a chat

**Steps:**
1. Open a chat with existing messages
2. Observe message loading
3. Check logcat for errors

**Expected Results:**
- ✅ Messages load without errors
- ✅ Messages display in chronological order
- ✅ Loading indicator shows while loading
- ✅ No permission errors

**Logcat Check:**
```
Should see: "getChatMessages: Setting up listener for chat [chatId]"
Should see: "getChatMessages: Received [X] messages"
Should see: "getChatMessages: Sending [X] messages to UI"
```

### Test 3.2: Load More Messages (Pagination)
**Objective:** Verify pagination works correctly

**Steps:**
1. Open a chat with 50+ messages
2. Scroll to top of message list
3. Observe older messages loading
4. Check for errors

**Expected Results:**
- ✅ Older messages load when scrolling to top
- ✅ Loading indicator shows
- ✅ Messages maintain correct order
- ✅ No duplicate messages

### Test 3.3: Real-time Message Updates
**Objective:** Verify new messages appear in real-time

**Steps:**
1. Login as User A on Device 1
2. Login as User B on Device 2
3. Both open the same chat
4. User A sends a message
5. Observe on Device 2

**Expected Results:**
- ✅ Message appears on Device 2 immediately
- ✅ No refresh needed
- ✅ Message appears in correct position

## Test Suite 4: Error Handling

### Test 4.1: Offline Message Sending
**Objective:** Verify offline message queuing works

**Steps:**
1. Login and open a chat
2. Turn off internet connection
3. Send a message
4. Observe message status
5. Turn internet back on
6. Observe message sending

**Expected Results:**
- ✅ Message shows "Sending" status
- ✅ Message queued for sending
- ✅ Message sends automatically when online
- ✅ Status updates to "Sent"

### Test 4.2: Service Unavailable Error
**Objective:** Verify handling of Firestore unavailable errors

**Steps:**
1. Simulate Firestore unavailable (if possible)
2. Try to load messages
3. Observe error handling

**Expected Results:**
- ✅ User-friendly error message shown
- ✅ Empty state displayed
- ✅ Retry option available
- ✅ Proper error logged

**Logcat Check:**
```
Should see: "UNAVAILABLE: Firestore service is temporarily unavailable"
```

### Test 4.3: Empty Chat State
**Objective:** Verify empty state displays correctly

**Steps:**
1. Create a new chat
2. Don't send any messages
3. Observe UI

**Expected Results:**
- ✅ Empty state message displayed
- ✅ No errors in logcat
- ✅ Input field is functional

## Test Suite 5: Typing Indicators

### Test 5.1: Typing Indicator Display
**Objective:** Verify typing indicators work (non-critical)

**Steps:**
1. Login as User A on Device 1
2. Login as User B on Device 2
3. Both open the same chat
4. User A starts typing
5. Observe on Device 2

**Expected Results:**
- ✅ "typing..." indicator appears on Device 2
- ✅ Indicator disappears when User A stops typing
- ⚠️ If this fails, it's non-critical

**Note:** Typing indicators are non-critical. Permission errors for typing status should not break the UI.

### Test 5.2: Typing Indicator Error Handling
**Objective:** Verify typing errors don't break UI

**Steps:**
1. Open a chat
2. Start typing
3. Check logcat for typing status errors
4. Verify UI still works

**Expected Results:**
- ✅ UI remains functional even if typing status fails
- ✅ Messages can still be sent
- ✅ Errors are logged but don't crash app

**Logcat Check:**
```
If typing fails, should see: "Permission denied for typing status - user may not have access"
But UI should continue working normally
```

## Test Suite 6: Group Chat Specific Tests

### Test 6.1: Add Member to Existing Group
**Objective:** Verify new members get access to group chat

**Steps:**
1. Login as group owner
2. Open group details
3. Add User D to the group
4. Login as User D
5. Navigate to Chat tab
6. Verify group chat appears

**Expected Results:**
- ✅ Group chat appears for new member
- ✅ New member can open chat
- ✅ New member can send messages
- ⚠️ May need to trigger chat creation manually

**Note:** May need to call `ensureGroupChatsExist()` or restart app for new member.

### Test 6.2: Remove Member from Group
**Objective:** Verify removed members lose access

**Steps:**
1. Login as group owner
2. Remove User D from group
3. Login as User D
4. Try to access group chat

**Expected Results:**
- ✅ User D cannot access chat
- ✅ Permission denied error if trying to send message
- ✅ Chat may still appear in list but cannot be accessed

### Test 6.3: Multiple Group Chats
**Objective:** Verify user can be in multiple group chats

**Steps:**
1. Create Group A with User A, B, C
2. Create Group B with User A, D, E
3. Login as User A
4. Verify both group chats appear
5. Send messages in both chats
6. Verify no cross-contamination

**Expected Results:**
- ✅ Both chats appear in chat list
- ✅ Messages go to correct chat
- ✅ No permission errors
- ✅ Each chat has correct participants

## Logcat Monitoring Guide

### Success Patterns to Look For

```
✅ getUserChats: Sending [X] chats to UI
✅ getChatMessages: Received [X] messages
✅ getOrCreateGroupChat: Successfully created chat [chatId] for group [groupId] with [X] participants
✅ sendMessage: Message sent successfully
✅ markMessagesAsRead: Successfully marked messages as read
```

### Error Patterns to Watch For

```
❌ PERMISSION_DENIED: User does not have permission to access chats
❌ PERMISSION_DENIED: User does not have permission to read messages
❌ Permission denied: You don't have access to send messages in this chat
❌ FAILED_PRECONDITION: The query requires an index
❌ Group has no members
```

### Non-Critical Warnings (OK to Ignore)

```
⚠️ Permission denied for typing status - user may not have access
⚠️ Failed to update typing status (non-critical)
⚠️ Error updating typing status
```

## Firestore Console Verification

### Check Chat Documents

1. Open Firebase Console
2. Navigate to Firestore Database
3. Open `chats` collection
4. For each chat, verify:
   - ✅ `participants` array is populated
   - ✅ `participants` contains correct user IDs
   - ✅ `type` is either "DIRECT" or "GROUP"
   - ✅ `groupId` is set for GROUP chats
   - ✅ `lastMessage` updates when messages sent

### Check Message Documents

1. Open a chat document
2. Open `messages` subcollection
3. For each message, verify:
   - ✅ `senderId` is set
   - ✅ `text` contains message content
   - ✅ `timestamp` is set
   - ✅ `readBy` array is populated
   - ✅ `status` is "SENT" or "READ"

## Performance Testing

### Test 7.1: Large Group Chat
**Objective:** Verify performance with many participants

**Steps:**
1. Create group with 10+ members
2. Send messages
3. Observe loading time
4. Check for performance issues

**Expected Results:**
- ✅ Chat loads within 2 seconds
- ✅ Messages send quickly
- ✅ No UI lag

### Test 7.2: Many Messages
**Objective:** Verify performance with message history

**Steps:**
1. Open chat with 100+ messages
2. Scroll through messages
3. Load older messages
4. Observe performance

**Expected Results:**
- ✅ Smooth scrolling
- ✅ Pagination works efficiently
- ✅ No memory issues

## Regression Testing

After fixes, verify these still work:
- [ ] Direct chats still work
- [ ] Image messages still send
- [ ] Document messages still send
- [ ] Message deletion still works
- [ ] Message copying still works
- [ ] Notifications still work
- [ ] Unread count still updates

## Test Results Template

```
Test Date: ___________
Tester: ___________
App Version: ___________

Test Suite 1: Group Chat Creation
- Test 1.1: [ ] Pass [ ] Fail - Notes: ___________
- Test 1.2: [ ] Pass [ ] Fail - Notes: ___________
- Test 1.3: [ ] Pass [ ] Fail - Notes: ___________

Test Suite 2: Sending Messages
- Test 2.1: [ ] Pass [ ] Fail - Notes: ___________
- Test 2.2: [ ] Pass [ ] Fail - Notes: ___________
- Test 2.3: [ ] Pass [ ] Fail - Notes: ___________

Test Suite 3: Loading Messages
- Test 3.1: [ ] Pass [ ] Fail - Notes: ___________
- Test 3.2: [ ] Pass [ ] Fail - Notes: ___________
- Test 3.3: [ ] Pass [ ] Fail - Notes: ___________

Test Suite 4: Error Handling
- Test 4.1: [ ] Pass [ ] Fail - Notes: ___________
- Test 4.2: [ ] Pass [ ] Fail - Notes: ___________
- Test 4.3: [ ] Pass [ ] Fail - Notes: ___________

Test Suite 5: Typing Indicators
- Test 5.1: [ ] Pass [ ] Fail [ ] N/A - Notes: ___________
- Test 5.2: [ ] Pass [ ] Fail - Notes: ___________

Test Suite 6: Group Chat Specific
- Test 6.1: [ ] Pass [ ] Fail - Notes: ___________
- Test 6.2: [ ] Pass [ ] Fail - Notes: ___________
- Test 6.3: [ ] Pass [ ] Fail - Notes: ___________

Overall Result: [ ] All Pass [ ] Some Failures
Critical Issues Found: ___________
```

## Troubleshooting Common Issues

### Issue: Group chat doesn't appear
**Solution:** 
- Check if `ensureGroupChatsExist()` was called
- Verify user is in group's memberIds array
- Check Firestore rules are deployed

### Issue: Permission denied errors
**Solution:**
- Verify Firestore rules are deployed
- Check participants array includes user ID
- Verify user is authenticated

### Issue: Messages don't send
**Solution:**
- Check internet connection
- Verify user is in participants array
- Check message validation passes
- Look for errors in logcat

### Issue: Typing indicators don't work
**Solution:**
- This is non-critical, app should still work
- Check typing_status subcollection permissions
- Verify error is logged but doesn't break UI
