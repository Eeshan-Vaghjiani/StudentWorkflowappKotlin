# Task 6: Message Pagination - Testing Guide

## Prerequisites
- App must be built and running on a device/emulator
- Firebase Firestore must be configured
- At least one chat with more than 50 messages (for proper testing)

## Test Scenarios

### Scenario 1: Initial Message Load
**Steps:**
1. Open the app and navigate to a chat
2. Observe the messages that load

**Expected Result:**
- Initial 50 messages should load
- Messages should appear in chronological order (oldest at top, newest at bottom)
- Chat should scroll to the bottom automatically
- Loading indicator should appear briefly then disappear

### Scenario 2: Load More Messages by Scrolling
**Steps:**
1. Open a chat with more than 50 messages
2. Scroll up to the very top of the message list
3. Continue scrolling up (pull down gesture)

**Expected Result:**
- Small progress bar should appear at the top
- Next 50 older messages should load
- Your scroll position should be maintained (you should still see the same messages you were viewing)
- Progress bar should disappear after loading completes

### Scenario 3: Multiple Pagination Loads
**Steps:**
1. Open a chat with more than 100 messages
2. Scroll to top and wait for messages to load
3. Scroll to top again and wait for more messages to load
4. Repeat until all messages are loaded

**Expected Result:**
- Each scroll to top should load 50 more messages
- Progress bar should appear each time
- Scroll position should be maintained each time
- Eventually, no more messages will load (all historical messages loaded)

### Scenario 4: Prevent Duplicate Loading
**Steps:**
1. Open a chat with more than 50 messages
2. Scroll to top quickly multiple times before messages finish loading

**Expected Result:**
- Only one pagination request should be triggered
- Progress bar should remain visible until loading completes
- No duplicate messages should appear
- No errors should occur

### Scenario 5: No More Messages
**Steps:**
1. Open a chat with exactly 50 or fewer messages
2. Try to scroll to top to load more

**Expected Result:**
- No pagination should trigger (no more messages to load)
- No progress bar should appear
- No errors should occur

### Scenario 6: New Message While Viewing Old Messages
**Steps:**
1. Open a chat and scroll to top to load older messages
2. Stay scrolled up viewing old messages
3. Have another user send a new message to the chat

**Expected Result:**
- New message should appear at the bottom of the chat
- Your scroll position should NOT change (you should stay viewing old messages)
- You can manually scroll down to see the new message

### Scenario 7: New Message While at Bottom
**Steps:**
1. Open a chat and stay scrolled at the bottom
2. Have another user send a new message

**Expected Result:**
- New message should appear at the bottom
- Chat should auto-scroll to show the new message
- Smooth scrolling animation should occur

### Scenario 8: Send Message After Loading Old Messages
**Steps:**
1. Open a chat and scroll to top to load older messages
2. Scroll back to the bottom
3. Type and send a new message

**Expected Result:**
- Message should send successfully
- Message should appear at the bottom
- Chat should scroll to show your new message
- Typing indicator should work correctly

### Scenario 9: Offline Pagination
**Steps:**
1. Open a chat and load some messages
2. Enable airplane mode
3. Scroll to top to try loading more messages

**Expected Result:**
- If messages are cached, they should load from cache
- If not cached, appropriate error handling should occur
- No app crash should occur

### Scenario 10: Error Handling
**Steps:**
1. Simulate a network error (disconnect internet briefly)
2. Try to scroll to top to load more messages

**Expected Result:**
- Progress bar should appear
- After timeout, error message should appear (Toast)
- Progress bar should disappear
- App should remain functional

## Performance Checks

### Memory Usage
- Open Android Profiler
- Load multiple batches of messages (200+ messages)
- Check memory usage doesn't grow excessively
- No memory leaks should occur

### Scroll Performance
- Load 200+ messages
- Scroll up and down quickly
- Check for smooth scrolling (60 FPS)
- No lag or stuttering should occur

### Network Efficiency
- Monitor network requests in Firebase Console
- Verify only 50 messages are loaded per pagination request
- Verify no duplicate requests are made

## Edge Cases to Test

1. **Empty Chat**: Open a chat with no messages
2. **Exactly 50 Messages**: Open a chat with exactly 50 messages
3. **51 Messages**: Open a chat with 51 messages (should trigger pagination)
4. **Very Old Messages**: Load messages from weeks/months ago
5. **Rapid Scrolling**: Scroll up and down rapidly
6. **App Backgrounding**: Load messages, background app, return to app
7. **Rotation**: Load messages, rotate device, continue loading

## Success Criteria

✅ All test scenarios pass without errors
✅ Scroll position is maintained when loading older messages
✅ No duplicate messages appear
✅ No duplicate loading requests occur
✅ Performance remains smooth with 200+ messages
✅ Error handling works gracefully
✅ UI indicators (progress bars) work correctly
✅ New messages still appear correctly after pagination

## Common Issues to Watch For

❌ **Scroll jumps to bottom** when loading older messages
❌ **Duplicate messages** appear in the list
❌ **Multiple loading requests** triggered simultaneously
❌ **Progress bar doesn't disappear** after loading
❌ **App crashes** when no more messages exist
❌ **Memory leaks** with large message lists
❌ **Slow scrolling** with many messages loaded

## Debugging Tips

If issues occur:
1. Check Logcat for error messages (filter by "ChatRepository" or "ChatRoomViewModel")
2. Verify Firestore indexes are created (check Firebase Console)
3. Check network requests in Firebase Console
4. Use Android Profiler to check memory and CPU usage
5. Verify message timestamps are correct in Firestore
6. Check that messages have proper ordering in database

## Next Steps After Testing

Once all tests pass:
- Mark task as verified
- Document any issues found
- Proceed to next task in the implementation plan
- Consider adding unit tests for pagination logic
