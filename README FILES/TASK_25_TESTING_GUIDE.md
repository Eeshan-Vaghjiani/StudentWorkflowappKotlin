# Task 25 Testing Guide: Message Grouping and Layout Improvements

## Overview
This guide provides comprehensive testing procedures for the message grouping and layout improvements implemented in Task 25.

## Pre-Testing Setup

### Requirements:
- Android device or emulator (API 23+)
- Two test accounts for sending/receiving messages
- Existing chat with some message history

### Test Data Preparation:
1. Create or use an existing chat
2. Have access to two devices/accounts for real-time testing
3. Prepare test messages of various types (text, images, documents)

## Test Cases

### Test Case 1: Message Grouping - Same Sender

**Objective**: Verify consecutive messages from the same sender are grouped correctly

**Steps:**
1. Open a chat room
2. Send 3-5 messages quickly (within 1 minute)
3. Observe the message display

**Expected Results:**
- ✅ First message shows sender info (name and picture/avatar)
- ✅ Subsequent messages hide sender info
- ✅ All messages maintain proper alignment
- ✅ Messages appear as a cohesive group

**Visual Check:**
```
[Avatar] John Doe
         Message 1
         
         Message 2
         
         Message 3
```

---

### Test Case 2: Message Grouping - Time Threshold

**Objective**: Verify sender info reappears after 5+ minutes

**Steps:**
1. Send a message
2. Wait 6 minutes (or modify device time)
3. Send another message from the same sender

**Expected Results:**
- ✅ Second message shows sender info again
- ✅ Messages are treated as separate groups
- ✅ Timestamp reflects the time gap

**Note**: For faster testing, you can temporarily modify `MESSAGE_GROUP_TIME_THRESHOLD` in `MessageGrouper.kt` to 30 seconds.

---

### Test Case 3: Message Grouping - Different Senders

**Objective**: Verify messages from different senders are not grouped

**Steps:**
1. User A sends a message
2. User B sends a message immediately after
3. User A sends another message

**Expected Results:**
- ✅ Each sender's messages show their sender info
- ✅ Messages alternate between left and right alignment
- ✅ Profile pictures/avatars display correctly

**Visual Check:**
```
[Avatar A] User A
           Message from A

                    Message from B [Avatar B]
                    User B

[Avatar A] User A
           Another from A
```

---

### Test Case 4: Sent Message Alignment

**Objective**: Verify sent messages align to the right with proper styling

**Steps:**
1. Send several messages from your account
2. Observe the layout and styling

**Expected Results:**
- ✅ Messages aligned to right side of screen
- ✅ Blue background color (`colorPrimaryBlue`)
- ✅ Rounded corners with tail on bottom-right
- ✅ Message status indicators visible
- ✅ No sender info shown (it's always you)

**Visual Check:**
```
                    My message here
                    2:30 PM ✓✓

                    Another message
                    2:31 PM ✓✓
```

---

### Test Case 5: Received Message Alignment

**Objective**: Verify received messages align to the left with sender info

**Steps:**
1. Have another user send you messages
2. Observe the layout and styling

**Expected Results:**
- ✅ Messages aligned to left side of screen
- ✅ Gray background color (`light_gray`)
- ✅ Rounded corners with tail on bottom-left
- ✅ Sender profile picture or avatar visible
- ✅ Sender name shown for first message in group

**Visual Check:**
```
[Avatar] Sender Name
         Received message
         2:30 PM

         Another message
         2:31 PM
```

---

### Test Case 6: Timestamp Headers

**Objective**: Verify timestamp headers appear correctly

**Steps:**
1. View messages from today
2. View messages from yesterday (if available)
3. View messages from previous dates

**Expected Results:**
- ✅ "Today" header for today's messages
- ✅ "Yesterday" header for yesterday's messages
- ✅ Formatted date (e.g., "January 15, 2024") for older messages
- ✅ Headers centered and styled distinctly
- ✅ Headers appear only when date changes

**Visual Check:**
```
        [Today]

[Avatar] User
         Message from today

        [Yesterday]

[Avatar] User
         Message from yesterday

        [January 14, 2024]

[Avatar] User
         Older message
```

---

### Test Case 7: Individual Timestamps

**Objective**: Verify individual message timestamps display correctly

**Steps:**
1. Send messages with varying time gaps
2. Check timestamp visibility

**Expected Results:**
- ✅ Timestamps shown every 5 minutes
- ✅ Format: "h:mm a" (e.g., "2:30 PM")
- ✅ Timestamps subtle but readable
- ✅ Timestamps aligned with message content

---

### Test Case 8: Message Bubbles with Tail

**Objective**: Verify message bubbles have proper rounded corners and tail indicators

**Steps:**
1. Send and receive various messages
2. Examine the bubble shapes closely

**Expected Results:**
- ✅ **Sent messages**: Tail on bottom-right (4dp radius)
- ✅ **Received messages**: Tail on bottom-left (4dp radius)
- ✅ Other corners: 16dp radius (fully rounded)
- ✅ Tail points toward the sender's side

**Visual Inspection:**
```
Sent:     ╭─────────╮
          │ Message │
          ╰─────────╯

Received: ╭─────────╮
          │ Message │
          ╰─────────╯
```

---

### Test Case 9: Profile Pictures and Avatars

**Objective**: Verify profile pictures and generated avatars display correctly

**Steps:**
1. Receive messages from users with profile pictures
2. Receive messages from users without profile pictures
3. Check avatar generation

**Expected Results:**
- ✅ Profile pictures load and display (circular)
- ✅ Avatars generated with initials for users without pictures
- ✅ Avatar colors consistent for each user
- ✅ Avatars only shown for first message in group

---

### Test Case 10: Mixed Message Types

**Objective**: Verify grouping works with different message types

**Steps:**
1. Send a text message
2. Send an image message
3. Send a document message
4. Send another text message

**Expected Results:**
- ✅ All message types group correctly
- ✅ Sender info shown only for first message
- ✅ Each message type displays properly
- ✅ Consistent bubble styling across types

---

### Test Case 11: Long Messages

**Objective**: Verify message bubbles handle long text correctly

**Steps:**
1. Send a very long message (500+ characters)
2. Observe the bubble wrapping

**Expected Results:**
- ✅ Message wraps within bubble
- ✅ Bubble expands vertically as needed
- ✅ Bubble doesn't exceed screen width
- ✅ Tail indicator remains at bottom corner
- ✅ Text remains readable

---

### Test Case 12: Empty Chat

**Objective**: Verify behavior with no messages

**Steps:**
1. Open a new chat with no messages
2. Send the first message

**Expected Results:**
- ✅ No errors or crashes
- ✅ First message displays correctly
- ✅ Sender info shown appropriately
- ✅ Timestamp header appears

---

### Test Case 13: Rapid Message Sending

**Objective**: Verify grouping with rapid message sending

**Steps:**
1. Send 10 messages as quickly as possible
2. Observe the grouping behavior

**Expected Results:**
- ✅ All messages group together
- ✅ Only first message shows sender info
- ✅ No UI lag or stuttering
- ✅ Messages appear in correct order

---

### Test Case 14: Message Status Integration

**Objective**: Verify message grouping works with status indicators

**Steps:**
1. Send messages and observe status changes
2. Check status indicators on grouped messages

**Expected Results:**
- ✅ Status indicators visible on all sent messages
- ✅ Status updates in real-time
- ✅ Grouping doesn't interfere with status display
- ✅ Checkmarks visible and properly colored

---

### Test Case 15: Scroll and Pagination

**Objective**: Verify grouping works with message pagination

**Steps:**
1. Open a chat with many messages
2. Scroll to top to load older messages
3. Observe grouping of newly loaded messages

**Expected Results:**
- ✅ Newly loaded messages group correctly
- ✅ Timestamp headers appear appropriately
- ✅ No duplicate headers or sender info
- ✅ Smooth scrolling experience

---

## Edge Cases

### Edge Case 1: Midnight Boundary
**Test**: Send messages before and after midnight
**Expected**: Proper timestamp headers for each day

### Edge Case 2: Null Timestamps
**Test**: Messages with missing timestamp data
**Expected**: Graceful handling, default to current time

### Edge Case 3: Same Timestamp
**Test**: Multiple messages with identical timestamps
**Expected**: Proper ordering and grouping

### Edge Case 4: Very Old Messages
**Test**: Messages from years ago
**Expected**: Proper date formatting (e.g., "January 15, 2020")

### Edge Case 5: Single Message
**Test**: Chat with only one message
**Expected**: Sender info shown, proper styling

---

## Performance Testing

### Test 1: Large Message List
**Steps:**
1. Load a chat with 500+ messages
2. Scroll through the entire list
3. Monitor performance

**Expected:**
- ✅ Smooth scrolling (60 FPS)
- ✅ No memory leaks
- ✅ Efficient view recycling

### Test 2: Rapid Updates
**Steps:**
1. Receive many messages quickly
2. Observe UI updates

**Expected:**
- ✅ Messages appear without delay
- ✅ No UI freezing
- ✅ Proper grouping maintained

---

## Visual Regression Testing

### Checklist:
- [ ] Message bubble colors correct
- [ ] Corner radii consistent
- [ ] Tail indicators visible
- [ ] Padding and margins appropriate
- [ ] Text alignment correct
- [ ] Profile pictures circular
- [ ] Avatar colors vibrant
- [ ] Timestamp headers centered
- [ ] Status indicators visible
- [ ] Overall layout balanced

---

## Accessibility Testing

### Checklist:
- [ ] Text readable at various font sizes
- [ ] Sufficient color contrast
- [ ] Touch targets adequate size
- [ ] Screen reader compatible
- [ ] Works in landscape orientation

---

## Device Testing Matrix

Test on multiple devices/configurations:

| Device Type | Screen Size | Android Version | Status |
|-------------|-------------|-----------------|--------|
| Phone       | Small       | API 23          | [ ]    |
| Phone       | Medium      | API 28          | [ ]    |
| Phone       | Large       | API 33          | [ ]    |
| Tablet      | Large       | API 30          | [ ]    |

---

## Regression Testing

Verify existing features still work:

- [ ] Sending text messages
- [ ] Sending image messages
- [ ] Sending document messages
- [ ] Message status updates
- [ ] Clickable URLs
- [ ] Image viewer
- [ ] Document download
- [ ] Typing indicators
- [ ] Read receipts

---

## Bug Reporting Template

If issues are found, report using this template:

```
**Bug Title**: [Brief description]

**Steps to Reproduce**:
1. 
2. 
3. 

**Expected Behavior**:
[What should happen]

**Actual Behavior**:
[What actually happens]

**Screenshots**:
[Attach screenshots if applicable]

**Device Info**:
- Device: 
- Android Version: 
- App Version: 

**Severity**: [Critical/High/Medium/Low]
```

---

## Sign-Off Checklist

Before marking task as complete:

- [ ] All test cases passed
- [ ] Edge cases handled
- [ ] Performance acceptable
- [ ] Visual design matches requirements
- [ ] No regressions introduced
- [ ] Code reviewed
- [ ] Documentation updated
- [ ] Ready for next task

---

## Notes

- Some tests may require modifying the time threshold temporarily for faster testing
- Use Android Studio's Layout Inspector for detailed visual debugging
- Consider using Espresso for automated UI testing in the future
- Document any issues found and their resolutions

---

**Testing Status**: ⏳ Pending
**Last Updated**: January 2025
**Tester**: [Your Name]
