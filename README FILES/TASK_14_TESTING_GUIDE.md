# Task 14: AI Assistant UI - Testing Guide

## Pre-Testing Setup

### 1. Configure API Key
Before testing, ensure the Gemini API key is configured:

```properties
# In local.properties
GEMINI_API_KEY=your_actual_gemini_api_key_here
```

### 2. Rebuild Project
After adding the API key:
```bash
./gradlew clean build
```

### 3. Verify Dependencies
Ensure these dependencies are in `app/build.gradle.kts`:
- OkHttp (for API calls)
- Gson (for JSON parsing)
- Material Components (for UI)
- Coroutines (for async operations)

## Manual Testing Checklist

### Test 1: Launch AI Assistant
**Steps:**
1. Open the app
2. Navigate to Tasks tab
3. Scroll to Quick Actions section
4. Tap "AI Assistant" button

**Expected Result:**
- ✅ AI Assistant activity opens
- ✅ Empty state is displayed
- ✅ AI brain icon is visible
- ✅ Welcome message is shown
- ✅ Message input is ready

**Pass/Fail:** ___

---

### Test 2: Send Simple Message
**Steps:**
1. Type "Hello" in the message input
2. Tap the send button

**Expected Result:**
- ✅ User message appears (right-aligned, blue)
- ✅ Loading indicator shows "AI is thinking..."
- ✅ AI response appears (left-aligned, gray)
- ✅ Messages have timestamps
- ✅ Input field clears after sending

**Pass/Fail:** ___

---

### Test 3: Create Assignment Request
**Steps:**
1. Type "Create a Math homework assignment due next Friday"
2. Tap send button
3. Wait for AI response

**Expected Result:**
- ✅ AI responds with assignment details
- ✅ "Create Assignment" button appears
- ✅ Button is clickable
- ✅ Assignment details are visible in message

**Pass/Fail:** ___

---

### Test 4: Create Assignment Action
**Steps:**
1. After Test 3, tap "Create Assignment" button
2. Wait for confirmation

**Expected Result:**
- ✅ Loading indicator appears
- ✅ Success toast shows "Assignment created: [title]"
- ✅ Confirmation message appears in chat
- ✅ Task appears in Tasks list

**Pass/Fail:** ___

---

### Test 5: Multiple Messages
**Steps:**
1. Send 5 different messages in sequence
2. Wait for each response

**Expected Result:**
- ✅ All messages display correctly
- ✅ Conversation flows naturally
- ✅ RecyclerView scrolls to bottom
- ✅ No UI glitches or crashes

**Pass/Fail:** ___

---

### Test 6: Loading States
**Steps:**
1. Send a message
2. Observe loading indicator
3. Wait for response

**Expected Result:**
- ✅ Loading layout appears immediately
- ✅ "AI is thinking..." text is visible
- ✅ Send button is disabled
- ✅ Input field is disabled
- ✅ Loading disappears when response arrives

**Pass/Fail:** ___

---

### Test 7: Error Handling - No API Key
**Steps:**
1. Remove API key from local.properties
2. Rebuild app
3. Open AI Assistant
4. Send a message

**Expected Result:**
- ✅ Toast shows "Please configure Gemini API key"
- ✅ App doesn't crash
- ✅ Error message in chat (optional)

**Pass/Fail:** ___

---

### Test 8: Error Handling - Network Error
**Steps:**
1. Turn off internet connection
2. Send a message
3. Observe behavior

**Expected Result:**
- ✅ Error toast appears
- ✅ Error message in chat
- ✅ App doesn't crash
- ✅ Can retry after reconnecting

**Pass/Fail:** ___

---

### Test 9: Empty State
**Steps:**
1. Open AI Assistant (fresh)
2. Observe initial state

**Expected Result:**
- ✅ Empty state layout is visible
- ✅ AI brain icon is displayed
- ✅ Welcome text is shown
- ✅ Instructions are clear
- ✅ RecyclerView is hidden

**Pass/Fail:** ___

---

### Test 10: Dark Mode
**Steps:**
1. Enable dark mode in device settings
2. Open AI Assistant
3. Send messages

**Expected Result:**
- ✅ Dark theme is applied
- ✅ Text is readable
- ✅ Colors are appropriate
- ✅ No contrast issues

**Pass/Fail:** ___

---

### Test 11: Keyboard Behavior
**Steps:**
1. Tap message input
2. Type a long message
3. Observe layout

**Expected Result:**
- ✅ Keyboard appears
- ✅ Layout adjusts (adjustResize)
- ✅ Input field is visible
- ✅ Send button is accessible
- ✅ Messages scroll up

**Pass/Fail:** ___

---

### Test 12: Enter Key to Send
**Steps:**
1. Type a message
2. Press Enter key on keyboard

**Expected Result:**
- ✅ Message is sent
- ✅ Same behavior as tapping send button

**Pass/Fail:** ___

---

### Test 13: Back Navigation
**Steps:**
1. Open AI Assistant
2. Send a message
3. Tap back button in toolbar

**Expected Result:**
- ✅ Returns to Tasks screen
- ✅ No crash
- ✅ Conversation is lost (expected)

**Pass/Fail:** ___

---

### Test 14: Screen Rotation
**Steps:**
1. Open AI Assistant
2. Send messages
3. Rotate device
4. Send more messages

**Expected Result:**
- ✅ Messages persist after rotation
- ✅ ViewModel survives rotation
- ✅ No crash
- ✅ UI rebuilds correctly

**Pass/Fail:** ___

---

### Test 15: Rapid Message Sending
**Steps:**
1. Type and send 3 messages quickly
2. Don't wait for responses

**Expected Result:**
- ✅ All messages are queued
- ✅ Responses arrive in order
- ✅ No crashes
- ✅ UI remains responsive

**Pass/Fail:** ___

---

## Integration Testing

### Test 16: Task Creation Integration
**Steps:**
1. Create assignment via AI Assistant
2. Navigate to Tasks tab
3. Check for new task

**Expected Result:**
- ✅ Task appears in Tasks list
- ✅ Task has correct title
- ✅ Task has correct due date
- ✅ Task has correct subject
- ✅ Task has correct priority

**Pass/Fail:** ___

---

### Test 17: Multiple Task Creation
**Steps:**
1. Create 3 different assignments via AI
2. Check Tasks list

**Expected Result:**
- ✅ All 3 tasks are created
- ✅ Each task has unique details
- ✅ No duplicates
- ✅ All tasks are visible

**Pass/Fail:** ___

---

## Performance Testing

### Test 18: Message List Performance
**Steps:**
1. Send 20+ messages
2. Scroll through conversation
3. Observe performance

**Expected Result:**
- ✅ Smooth scrolling
- ✅ No lag
- ✅ DiffUtil updates efficiently
- ✅ No memory issues

**Pass/Fail:** ___

---

### Test 19: Memory Usage
**Steps:**
1. Open AI Assistant
2. Send 50+ messages
3. Check memory usage

**Expected Result:**
- ✅ Memory usage is reasonable
- ✅ No memory leaks
- ✅ App doesn't slow down
- ✅ No OutOfMemoryError

**Pass/Fail:** ___

---

## Edge Cases

### Test 20: Empty Message
**Steps:**
1. Try to send empty message
2. Try to send whitespace only

**Expected Result:**
- ✅ Message is not sent
- ✅ No error
- ✅ Input remains empty

**Pass/Fail:** ___

---

### Test 21: Very Long Message
**Steps:**
1. Type a very long message (500+ characters)
2. Send it

**Expected Result:**
- ✅ Message is sent
- ✅ Displays correctly
- ✅ No truncation issues
- ✅ AI responds appropriately

**Pass/Fail:** ___

---

### Test 22: Special Characters
**Steps:**
1. Send message with emojis: "Create task 📚 for Math 🔢"
2. Send message with symbols: "Assignment #1 @ 5pm"

**Expected Result:**
- ✅ Special characters display correctly
- ✅ AI processes message correctly
- ✅ No encoding issues

**Pass/Fail:** ___

---

### Test 23: Rapid Button Taps
**Steps:**
1. Tap send button multiple times rapidly
2. Tap action button multiple times

**Expected Result:**
- ✅ Only one message is sent
- ✅ Only one task is created
- ✅ No duplicate operations
- ✅ Button is disabled during operation

**Pass/Fail:** ___

---

## Accessibility Testing

### Test 24: TalkBack Support
**Steps:**
1. Enable TalkBack
2. Navigate AI Assistant
3. Send messages

**Expected Result:**
- ✅ All elements are announced
- ✅ Content descriptions are present
- ✅ Navigation is logical
- ✅ Actions are accessible

**Pass/Fail:** ___

---

### Test 25: Large Text
**Steps:**
1. Enable large text in accessibility settings
2. Open AI Assistant
3. Send messages

**Expected Result:**
- ✅ Text scales appropriately
- ✅ Layout doesn't break
- ✅ All text is readable
- ✅ Buttons are still accessible

**Pass/Fail:** ___

---

## Regression Testing

### Test 26: Tasks Screen Still Works
**Steps:**
1. Navigate to Tasks screen
2. Verify all features work
3. Create task manually

**Expected Result:**
- ✅ Tasks screen functions normally
- ✅ No regressions introduced
- ✅ AI Assistant button is visible

**Pass/Fail:** ___

---

### Test 27: Other Features Unaffected
**Steps:**
1. Test Groups, Calendar, Chat features
2. Verify no issues

**Expected Result:**
- ✅ All features work as before
- ✅ No crashes
- ✅ No performance issues

**Pass/Fail:** ___

---

## API Testing

### Test 28: Valid API Response
**Steps:**
1. Send message
2. Check logs for API response
3. Verify parsing

**Expected Result:**
- ✅ API returns valid JSON
- ✅ Response is parsed correctly
- ✅ Message is extracted
- ✅ Action is detected (if present)

**Pass/Fail:** ___

---

### Test 29: Invalid API Response
**Steps:**
1. Mock invalid API response (if possible)
2. Observe error handling

**Expected Result:**
- ✅ Error is caught
- ✅ User-friendly message shown
- ✅ App doesn't crash

**Pass/Fail:** ___

---

### Test 30: API Timeout
**Steps:**
1. Simulate slow network
2. Send message
3. Wait for timeout

**Expected Result:**
- ✅ Timeout is handled
- ✅ Error message shown
- ✅ Can retry

**Pass/Fail:** ___

---

## Test Summary

### Overall Results
- Total Tests: 30
- Passed: ___
- Failed: ___
- Skipped: ___

### Critical Issues Found
1. ___
2. ___
3. ___

### Minor Issues Found
1. ___
2. ___
3. ___

### Recommendations
1. ___
2. ___
3. ___

## Sign-Off

**Tester Name:** _______________
**Date:** _______________
**Build Version:** _______________
**Device:** _______________
**Android Version:** _______________

**Overall Assessment:** _______________

**Ready for Production:** Yes / No

**Notes:**
_______________________________________________
_______________________________________________
_______________________________________________
