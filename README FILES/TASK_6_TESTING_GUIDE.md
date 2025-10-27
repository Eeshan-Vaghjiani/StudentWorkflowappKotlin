# Task 6: Chat Functionality - Testing Guide

## Overview

This guide provides step-by-step instructions for testing the chat functionality fixes implemented in Task 6. Follow these tests to ensure chat creation and message sending work correctly without permission errors.

## Prerequisites

### Required Setup
1. **Two Test Accounts**: Create two Firebase Auth accounts for testing
   - Test User A: `testa@example.com`
   - Test User B: `testb@example.com`

2. **Devices**: Use either:
   - Two physical Android devices, OR
   - One physical device + one emulator, OR
   - Two emulators (may be slower)

3. **Firebase Configuration**:
   - Firestore rules deployed
   - Firebase project properly configured
   - `google-services.json` up to date

4. **App Installation**:
   - Latest build installed on both devices
   - Both users logged in

### Environment Check
```bash
# Verify Firebase CLI is installed
firebase --version

# Verify you're logged in
firebase login

# Verify project is selected
firebase use --add

# Deploy latest rules
firebase deploy --only firestore:rules
```

## Test Suite 1: Basic Chat Creation

### Test 1.1: Create Direct Chat from Search

**Objective**: Verify users can create new direct chats without permission errors

**Steps**:
1. Open app on Device A (logged in as User A)
2. Navigate to Chat tab (bottom navigation)
3. Tap the FAB (floating action button with + icon)
4. In the search dialog, type User B's name or email
5. Tap on User B in the search results

**Expected Results**:
- ✅ UserSearchDialog opens without errors
- ✅ User B appears in search results
- ✅ Tapping User B creates a new chat
- ✅ ChatRoomActivity opens immediately
- ✅ No "Permission denied" error appears
- ✅ Chat appears in chat list on Device A

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 1.2: Verify Chat Appears on Both Devices

**Objective**: Ensure chat is visible to both participants

**Steps**:
1. On Device B (logged in as User B)
2. Navigate to Chat tab
3. Look for the new chat with User A

**Expected Results**:
- ✅ Chat appears in User B's chat list
- ✅ Chat shows User A's name
- ✅ Chat shows "No messages yet" or similar empty state

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 2: Message Sending

### Test 2.1: Send First Message

**Objective**: Verify message sending works without permission errors

**Steps**:
1. On Device A, in the chat with User B
2. Type "Hello, this is a test message" in the message input
3. Tap the send button

**Expected Results**:
- ✅ Message appears immediately in chat
- ✅ Message shows "sending" status briefly
- ✅ Message status changes to "sent"
- ✅ No "Permission denied" error
- ✅ Message input clears after sending

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 2.2: Receive Message in Real-time

**Objective**: Verify real-time message delivery

**Steps**:
1. On Device B, open the chat with User A
2. Observe as User A sends the message

**Expected Results**:
- ✅ Message appears automatically (no refresh needed)
- ✅ Message displays within 1-2 seconds
- ✅ Sender name shows "User A"
- ✅ Timestamp is accurate
- ✅ Message text is correct

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 2.3: Last Message Update

**Objective**: Verify chat list updates with last message

**Steps**:
1. On Device B, go back to chat list
2. Look at the chat with User A

**Expected Results**:
- ✅ Last message shows "Hello, this is a test message"
- ✅ Timestamp shows current time
- ✅ Unread count shows "1"
- ✅ Chat moves to top of list

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 2.4: Bidirectional Messaging

**Objective**: Verify both users can send messages

**Steps**:
1. On Device B, send a reply: "Hi! I received your message"
2. On Device A, observe the reply

**Expected Results**:
- ✅ User B's message sends successfully
- ✅ User A receives message in real-time
- ✅ Both messages display in correct order
- ✅ No permission errors for either user

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 3: Image and Document Messages

### Test 3.1: Send Image Message

**Objective**: Verify image upload and sending

**Steps**:
1. On Device A, in the chat
2. Tap the attachment button (paperclip icon)
3. Select "Image"
4. Choose an image from gallery
5. Wait for upload to complete

**Expected Results**:
- ✅ Attachment picker opens
- ✅ Image selection works
- ✅ Upload progress shown (0-100%)
- ✅ Image appears in chat after upload
- ✅ Image can be tapped to view full-screen
- ✅ Last message shows "📷 Photo"
- ✅ No upload errors

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 3.2: Receive Image Message

**Objective**: Verify image messages display correctly

**Steps**:
1. On Device B, observe the image message

**Expected Results**:
- ✅ Image appears in chat
- ✅ Image thumbnail loads
- ✅ Tapping image opens full-screen viewer
- ✅ Image quality is acceptable

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 3.3: Send Document Message

**Objective**: Verify document upload and sending

**Steps**:
1. On Device A, tap attachment button
2. Select "Document"
3. Choose a PDF or document file
4. Wait for upload

**Expected Results**:
- ✅ Document picker opens
- ✅ Document selection works
- ✅ Upload progress shown
- ✅ Document appears in chat
- ✅ Document name and size displayed
- ✅ Last message shows "📄 [filename]"

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 3.4: Download Document

**Objective**: Verify document download works

**Steps**:
1. On Device B, tap on the document message
2. Wait for download
3. Try to open the document

**Expected Results**:
- ✅ Download progress shown
- ✅ Document downloads successfully
- ✅ Document saved to Downloads folder
- ✅ Document can be opened with appropriate app

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 4: Group Chats

### Test 4.1: Auto-create Group Chat

**Objective**: Verify group chats are created automatically

**Steps**:
1. Create a new group with User A and User B as members
2. Go to Chat tab
3. Look for the group chat

**Expected Results**:
- ✅ Group chat appears in chat list
- ✅ Group name displayed correctly
- ✅ Group icon/avatar shown
- ✅ Member count visible

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 4.2: Send Message in Group Chat

**Objective**: Verify group messaging works

**Steps**:
1. Open the group chat
2. Send a message: "Hello group!"
3. Check on other devices

**Expected Results**:
- ✅ Message sends successfully
- ✅ All group members receive message
- ✅ Sender name shown for each message
- ✅ No permission errors

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 5: Real-time Features

### Test 5.1: Typing Indicator

**Objective**: Verify typing indicators work

**Steps**:
1. On Device A, start typing (don't send)
2. On Device B, observe the typing indicator

**Expected Results**:
- ✅ "typing..." appears on Device B
- ✅ Indicator appears within 1 second
- ✅ Indicator disappears after 2 seconds of inactivity
- ✅ Indicator disappears when message is sent

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 5.2: Read Receipts

**Objective**: Verify read status updates

**Steps**:
1. User A sends a message
2. User B opens the chat and reads the message
3. User A observes the message status

**Expected Results**:
- ✅ Message status changes to "read"
- ✅ Unread count decreases to 0
- ✅ Status updates in real-time

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 6: Offline Functionality

### Test 6.1: Send Message Offline

**Objective**: Verify offline message queuing

**Steps**:
1. On Device A, turn off WiFi and mobile data
2. Send a message
3. Observe the message status

**Expected Results**:
- ✅ Message appears with "sending" status
- ✅ Message stays in queue
- ✅ No error message (yet)

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 6.2: Auto-send When Online

**Objective**: Verify messages send when connection restored

**Steps**:
1. Turn WiFi/data back on
2. Wait a few seconds
3. Observe the message status

**Expected Results**:
- ✅ Message sends automatically
- ✅ Status changes to "sent"
- ✅ Message appears on other device
- ✅ No manual retry needed

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 6.3: Manual Retry

**Objective**: Verify manual retry works for failed messages

**Steps**:
1. Send a message while offline
2. Wait for it to fail (after timeout)
3. Tap the retry button

**Expected Results**:
- ✅ Retry button appears on failed message
- ✅ Tapping retry attempts to send again
- ✅ Message sends successfully when online
- ✅ Failed message removed from queue

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 7: Error Handling

### Test 7.1: Invalid User Search

**Objective**: Verify error handling for invalid searches

**Steps**:
1. Open user search dialog
2. Search for a non-existent user
3. Try to create a chat

**Expected Results**:
- ✅ "No users found" message shown
- ✅ No crash
- ✅ Can search again

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 7.2: Network Error Handling

**Objective**: Verify network error messages

**Steps**:
1. Turn off internet
2. Try to create a new chat
3. Observe error message

**Expected Results**:
- ✅ "No internet connection" message shown
- ✅ Connection status indicator appears
- ✅ No app crash
- ✅ Can retry when online

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 7.3: Large File Upload Error

**Objective**: Verify file size validation

**Steps**:
1. Try to upload a very large image (>10MB)
2. Observe the result

**Expected Results**:
- ✅ File size validation works
- ✅ Error message if file too large
- ✅ Suggestion to compress or choose smaller file

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 8: Message Management

### Test 8.1: Delete Own Message

**Objective**: Verify message deletion works

**Steps**:
1. User A sends a message
2. Long press on the message
3. Select "Delete"
4. Confirm deletion

**Expected Results**:
- ✅ Context menu appears
- ✅ Delete option available
- ✅ Confirmation dialog shown
- ✅ Message deleted from chat
- ✅ Message removed on both devices
- ✅ Last message updates if needed

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 8.2: Cannot Delete Other's Message

**Objective**: Verify users can't delete others' messages

**Steps**:
1. User B tries to delete User A's message
2. Long press on User A's message

**Expected Results**:
- ✅ Delete option NOT available
- ✅ Only "Copy" and "Forward" options shown
- ✅ Security enforced

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 8.3: Copy Message Text

**Objective**: Verify copy functionality

**Steps**:
1. Long press on a message
2. Select "Copy"
3. Paste in another app

**Expected Results**:
- ✅ Copy option available
- ✅ Text copied to clipboard
- ✅ Can paste in other apps

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 9: Performance

### Test 9.1: Load Time with Many Chats

**Objective**: Verify performance with large dataset

**Steps**:
1. Create 20+ chats
2. Close and reopen app
3. Navigate to Chat tab
4. Measure load time

**Expected Results**:
- ✅ Chat list loads in < 2 seconds
- ✅ Smooth scrolling
- ✅ No lag or stuttering
- ✅ Images load progressively

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 9.2: Load Time with Many Messages

**Objective**: Verify performance with long chat history

**Steps**:
1. Open a chat with 100+ messages
2. Measure load time
3. Scroll through messages

**Expected Results**:
- ✅ Messages load in < 1 second
- ✅ Smooth scrolling
- ✅ No memory issues
- ✅ Pagination works correctly

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Test Suite 10: Security

### Test 10.1: Firestore Rules Enforcement

**Objective**: Verify security rules are enforced

**Steps**:
1. Try to access a chat you're not a participant in
2. Check Firebase Console logs

**Expected Results**:
- ✅ Access denied
- ✅ No data leakage
- ✅ Appropriate error message
- ✅ Security rule violation logged

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

### Test 10.2: Message Validation

**Objective**: Verify input validation works

**Steps**:
1. Try to send empty message
2. Try to send very long message (>5000 chars)
3. Try to send message with special characters

**Expected Results**:
- ✅ Empty messages rejected
- ✅ Long messages handled appropriately
- ✅ Special characters sanitized
- ✅ No XSS vulnerabilities

**Pass/Fail**: ☐ PASS  ☐ FAIL

**Notes**: _______________

## Firebase Console Verification

### Firestore Data Check

**Steps**:
1. Open Firebase Console
2. Navigate to Firestore Database
3. Check the `chats` collection

**Verify**:
- ✅ Chat documents created correctly
- ✅ Participant arrays populated
- ✅ Timestamps accurate
- ✅ Last message fields updated
- ✅ Unread counts correct

**Pass/Fail**: ☐ PASS  ☐ FAIL

### Messages Subcollection Check

**Steps**:
1. Open a chat document
2. Check the `messages` subcollection

**Verify**:
- ✅ Message documents in correct location
- ✅ All fields populated correctly
- ✅ Timestamps in order
- ✅ Read status accurate

**Pass/Fail**: ☐ PASS  ☐ FAIL

### Security Rules Check

**Steps**:
1. Navigate to Firestore > Rules
2. Check published rules

**Verify**:
- ✅ Rules published successfully
- ✅ Rules match updated version
- ✅ No syntax errors
- ✅ Recent publish timestamp

**Pass/Fail**: ☐ PASS  ☐ FAIL

## Summary

### Test Results

| Test Suite | Tests Passed | Tests Failed | Pass Rate |
|------------|--------------|--------------|-----------|
| Suite 1: Basic Chat Creation | __ / 2 | __ | __% |
| Suite 2: Message Sending | __ / 4 | __ | __% |
| Suite 3: Image/Document | __ / 4 | __ | __% |
| Suite 4: Group Chats | __ / 2 | __ | __% |
| Suite 5: Real-time Features | __ / 2 | __ | __% |
| Suite 6: Offline Functionality | __ / 3 | __ | __% |
| Suite 7: Error Handling | __ / 3 | __ | __% |
| Suite 8: Message Management | __ / 3 | __ | __% |
| Suite 9: Performance | __ / 2 | __ | __% |
| Suite 10: Security | __ / 2 | __ | __% |
| **TOTAL** | **__ / 27** | **__** | **__%** |

### Critical Issues Found

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Minor Issues Found

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Recommendations

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Sign-off

**Tester Name**: _______________
**Date**: _______________
**Overall Status**: ☐ PASS  ☐ FAIL  ☐ NEEDS REVIEW

**Notes**: _______________________________________________
