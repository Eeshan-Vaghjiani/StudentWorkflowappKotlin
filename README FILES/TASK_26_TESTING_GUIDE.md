# Task 26 Testing Guide: Long-Press Context Menu for Messages

## Prerequisites
- App installed on device/emulator
- At least two user accounts for testing
- Active chat with messages from both users

## Test Setup

### Create Test Environment:
1. Log in with User A
2. Create or open a chat with User B
3. Send several messages from User A
4. Log out and log in with User B
5. Send several messages from User B
6. Log back in with User A

## Test Cases

### Test Case 1: Long-Press on Own Text Message
**Objective:** Verify context menu appears for user's own messages

**Steps:**
1. Open a chat where you have sent messages
2. Long-press on one of your text messages
3. Observe the context menu

**Expected Results:**
✅ Context menu appears anchored to the message
✅ Menu shows three options: "Copy", "Delete", "Forward"
✅ Menu is positioned near the long-pressed message

**Pass/Fail:** ___________

---

### Test Case 2: Long-Press on Other User's Message
**Objective:** Verify delete option is hidden for other users' messages

**Steps:**
1. Open a chat with messages from another user
2. Long-press on a message from the other user
3. Observe the context menu options

**Expected Results:**
✅ Context menu appears
✅ Menu shows only "Copy" and "Forward" options
✅ "Delete" option is NOT visible

**Pass/Fail:** ___________

---

### Test Case 3: Copy Text Message
**Objective:** Verify text copying functionality

**Steps:**
1. Long-press on a text message
2. Tap "Copy" from the context menu
3. Open any text input field (e.g., message input)
4. Long-press and select "Paste"

**Expected Results:**
✅ Toast appears: "Copied to clipboard"
✅ Context menu closes
✅ Pasted text matches the original message

**Pass/Fail:** ___________

---

### Test Case 4: Copy Image Message
**Objective:** Verify copying works for image messages

**Steps:**
1. Long-press on an image message
2. Tap "Copy" from the context menu
3. Open any text input field
4. Long-press and select "Paste"

**Expected Results:**
✅ Toast appears: "Copied to clipboard"
✅ Image URL is copied (not the image itself)

**Pass/Fail:** ___________

---

### Test Case 5: Copy Document Message
**Objective:** Verify copying works for document messages

**Steps:**
1. Long-press on a document message
2. Tap "Copy" from the context menu
3. Open any text input field
4. Long-press and select "Paste"

**Expected Results:**
✅ Toast appears: "Copied to clipboard"
✅ Document name is copied

**Pass/Fail:** ___________

---

### Test Case 6: Delete Message - Confirmation Dialog
**Objective:** Verify confirmation dialog appears before deletion

**Steps:**
1. Long-press on your own message
2. Tap "Delete" from the context menu
3. Observe the confirmation dialog

**Expected Results:**
✅ Dialog appears with title "Delete Message"
✅ Dialog shows warning: "Are you sure you want to delete this message? This action cannot be undone."
✅ Dialog has "Delete" and "Cancel" buttons

**Pass/Fail:** ___________

---

### Test Case 7: Delete Message - Cancel
**Objective:** Verify canceling deletion keeps the message

**Steps:**
1. Long-press on your own message
2. Tap "Delete" from the context menu
3. Tap "Cancel" in the confirmation dialog
4. Observe the chat

**Expected Results:**
✅ Dialog closes
✅ Message remains in the chat
✅ No changes to the chat

**Pass/Fail:** ___________

---

### Test Case 8: Delete Message - Confirm
**Objective:** Verify successful message deletion

**Steps:**
1. Long-press on your own message
2. Tap "Delete" from the context menu
3. Tap "Delete" in the confirmation dialog
4. Observe the chat

**Expected Results:**
✅ Dialog closes
✅ Message disappears from chat immediately
✅ Toast appears: "Message deleted"
✅ Message is removed from Firestore (verify by refreshing)

**Pass/Fail:** ___________

---

### Test Case 9: Delete Most Recent Message
**Objective:** Verify chat preview updates when last message is deleted

**Steps:**
1. Note the last message in a chat
2. Open the chat
3. Delete the most recent message
4. Go back to chat list
5. Observe the chat preview

**Expected Results:**
✅ Message is deleted
✅ Chat preview shows the new last message
✅ Timestamp updates to the new last message

**Pass/Fail:** ___________

---

### Test Case 10: Delete Only Message in Chat
**Objective:** Verify behavior when deleting the only message

**Steps:**
1. Create a new chat with only one message
2. Delete that message
3. Observe the chat

**Expected Results:**
✅ Message is deleted
✅ Chat shows empty state or no messages
✅ Chat still exists in chat list
✅ Last message field is cleared

**Pass/Fail:** ___________

---

### Test Case 11: Forward Message (Placeholder)
**Objective:** Verify forward option shows placeholder message

**Steps:**
1. Long-press on any message
2. Tap "Forward" from the context menu

**Expected Results:**
✅ Toast appears: "Forward feature coming soon"
✅ Context menu closes
✅ No other action occurs

**Pass/Fail:** ___________

---

### Test Case 12: Long-Press on Sent Message (Sending Status)
**Objective:** Verify context menu works on messages being sent

**Steps:**
1. Turn on airplane mode
2. Send a message (it will show "Sending..." status)
3. Long-press on the sending message

**Expected Results:**
✅ Context menu appears
✅ All options are available
✅ Delete option works even for sending messages

**Pass/Fail:** ___________

---

### Test Case 13: Long-Press on Failed Message
**Objective:** Verify context menu works on failed messages

**Steps:**
1. Send a message that fails (simulate by turning off network)
2. Wait for message to show "Failed" status
3. Long-press on the failed message

**Expected Results:**
✅ Context menu appears
✅ Delete option is available
✅ Can delete failed messages

**Pass/Fail:** ___________

---

### Test Case 14: Multiple Users - Delete Verification
**Objective:** Verify other users see the deletion

**Steps:**
1. User A sends a message
2. User B opens the chat and sees the message
3. User A deletes the message
4. User B observes the chat

**Expected Results:**
✅ Message disappears from User B's chat in real-time
✅ Both users see the updated chat state

**Pass/Fail:** ___________

---

### Test Case 15: Context Menu Dismissal
**Objective:** Verify context menu can be dismissed

**Steps:**
1. Long-press on a message to show context menu
2. Tap outside the menu or press back button

**Expected Results:**
✅ Context menu closes
✅ No action is performed
✅ Message remains unchanged

**Pass/Fail:** ___________

---

### Test Case 16: Rapid Long-Press
**Objective:** Verify system handles rapid long-presses

**Steps:**
1. Rapidly long-press on multiple messages
2. Observe behavior

**Expected Results:**
✅ Only one context menu appears at a time
✅ Previous menu closes when new one opens
✅ No crashes or UI glitches

**Pass/Fail:** ___________

---

### Test Case 17: Long-Press During Scroll
**Objective:** Verify long-press works while scrolling

**Steps:**
1. Open a chat with many messages
2. Start scrolling
3. Long-press on a message while scrolling

**Expected Results:**
✅ Scroll stops
✅ Context menu appears
✅ Correct message is selected

**Pass/Fail:** ___________

---

### Test Case 18: Delete Message with Image
**Objective:** Verify deleting image messages works correctly

**Steps:**
1. Send an image message
2. Long-press on the image message
3. Delete the message

**Expected Results:**
✅ Context menu appears
✅ Message is deleted
✅ Image is removed from UI
✅ Storage reference is handled (image may remain in storage)

**Pass/Fail:** ___________

---

### Test Case 19: Delete Message with Document
**Objective:** Verify deleting document messages works correctly

**Steps:**
1. Send a document message
2. Long-press on the document message
3. Delete the message

**Expected Results:**
✅ Context menu appears
✅ Message is deleted
✅ Document reference is removed from UI
✅ Storage reference is handled

**Pass/Fail:** ___________

---

### Test Case 20: Authorization Check
**Objective:** Verify security - cannot delete other users' messages

**Steps:**
1. User A sends a message
2. User B tries to delete User A's message (if possible through any means)

**Expected Results:**
✅ Delete option is not shown in UI for User B
✅ If attempted through other means, server rejects the request
✅ Error message: "You can only delete your own messages"

**Pass/Fail:** ___________

---

## Edge Cases to Test

### Edge Case 1: Network Interruption During Delete
**Steps:**
1. Long-press and delete a message
2. Turn off network immediately after confirming
3. Turn network back on

**Expected Results:**
✅ Message is removed from UI immediately
✅ Deletion syncs to Firestore when network returns
✅ No duplicate or ghost messages

**Pass/Fail:** ___________

---

### Edge Case 2: Delete Message in Group Chat
**Steps:**
1. Open a group chat
2. Delete your own message
3. Verify all group members see the deletion

**Expected Results:**
✅ Message is deleted for all participants
✅ Group chat preview updates correctly

**Pass/Fail:** ___________

---

### Edge Case 3: Long-Press on Timestamp Header
**Steps:**
1. Long-press on a timestamp header (e.g., "Today", "Yesterday")

**Expected Results:**
✅ No context menu appears (timestamp headers are not messages)
✅ No crash or error

**Pass/Fail:** ___________

---

## Performance Testing

### Performance Test 1: Large Chat
**Steps:**
1. Open a chat with 100+ messages
2. Long-press on various messages
3. Delete several messages

**Expected Results:**
✅ Context menu appears quickly (<100ms)
✅ Deletion is fast
✅ UI remains responsive
✅ No lag or stuttering

**Pass/Fail:** ___________

---

### Performance Test 2: Multiple Deletions
**Steps:**
1. Delete 10 messages in quick succession
2. Observe app performance

**Expected Results:**
✅ Each deletion completes successfully
✅ No memory leaks
✅ App remains stable

**Pass/Fail:** ___________

---

## Accessibility Testing

### Accessibility Test 1: TalkBack
**Steps:**
1. Enable TalkBack
2. Long-press on a message
3. Navigate the context menu

**Expected Results:**
✅ TalkBack announces the context menu
✅ Menu items are readable
✅ Actions can be performed with TalkBack

**Pass/Fail:** ___________

---

## Summary

**Total Test Cases:** 23
**Passed:** ___________
**Failed:** ___________
**Blocked:** ___________

## Issues Found

| Issue # | Description | Severity | Status |
|---------|-------------|----------|--------|
| 1 | | | |
| 2 | | | |
| 3 | | | |

## Notes

_Add any additional observations or notes here_

---

## Sign-Off

**Tester Name:** ___________________
**Date:** ___________________
**Build Version:** ___________________
**Device/Emulator:** ___________________
**Android Version:** ___________________

**Overall Result:** ☐ Pass ☐ Fail ☐ Pass with Issues

**Comments:**
_______________________________________________________
_______________________________________________________
_______________________________________________________
