# Task 7: Offline Message Queue - Implementation Summary

## Overview
Successfully implemented offline message queue functionality that allows messages to be queued when offline and automatically sent when connection is restored.

## Files Created

### 1. OfflineMessageQueue.kt
**Location:** `app/src/main/java/com/example/loginandregistration/utils/OfflineMessageQueue.kt`

**Purpose:** Helper class to manage offline message queue using SharedPreferences

**Key Features:**
- Queue messages when offline
- Store messages with JSON serialization
- Mark messages as SENDING, SENT, or FAILED
- Remove messages from queue when successfully sent
- Get queued messages for specific chats
- Clear entire queue

**Methods:**
- `queueMessage(message: Message)` - Add message to queue
- `getQueuedMessages()` - Get all queued messages
- `removeMessage(messageId: String)` - Remove message from queue
- `markMessageAsFailed(messageId: String)` - Mark message as failed
- `updateMessageStatus(messageId: String, status: MessageStatus)` - Update message status
- `clearQueue()` - Clear all queued messages
- `getQueuedMessagesForChat(chatId: String)` - Get queued messages for specific chat

### 2. ConnectionMonitor.kt
**Location:** `app/src/main/java/com/example/loginandregistration/utils/ConnectionMonitor.kt`

**Purpose:** Monitor network connectivity changes using ConnectivityManager

**Key Features:**
- Real-time connection status monitoring
- Flow-based API for reactive updates
- Detects when internet connection is available/lost
- Checks network capabilities

**Methods:**
- `isConnected: Flow<Boolean>` - Flow that emits connection status
- `isCurrentlyConnected(): Boolean` - Check current connection status

## Files Modified

### 1. ChatRepository.kt
**Changes:**
- Added `context` parameter to constructor for OfflineMessageQueue initialization
- Added `offlineQueue` instance
- Modified `sendMessage()` to queue messages and handle retries
- Added retry count parameter with max retry attempts (3)
- Added methods:
  - `getQueuedMessagesForChat(chatId: String)` - Get queued messages for chat
  - `getAllQueuedMessages()` - Get all queued messages
  - `retryMessage(message: Message)` - Retry sending failed message
  - `processQueuedMessages()` - Process all queued messages when online
  - `clearOfflineQueue()` - Clear the queue

### 2. ChatRoomViewModel.kt
**Changes:**
- Changed from `ViewModel` to `AndroidViewModel` to access Application context
- Added `ConnectionMonitor` instance
- Added `_isConnected` StateFlow to track connection status
- Modified `loadChat()` to merge Firestore messages with queued messages
- Added connection monitoring in `init` block
- Added methods:
  - `retryMessage(message: Message)` - Retry failed message
  - `processQueuedMessages()` - Process queued messages when connection restored
  - `getQueuedMessages()` - Get queued messages for current chat

### 3. ChatRoomViewModelFactory.kt
**Changes:**
- Updated to accept `Application` parameter
- Changed to create `ChatRoomViewModel` with Application context

### 4. ChatRoomActivity.kt
**Changes:**
- Updated ViewModel initialization to pass `application` context
- Added retry callback to `MessageAdapter` initialization
- Shows confirmation dialog before retrying failed messages
- Added connection status observer
- Shows toast when offline

### 5. MessageAdapter.kt
**Changes:**
- Added `onRetryMessage` callback parameter
- Updated `SentMessageViewHolder.bind()` to accept retry callback
- Added click listener for failed messages to trigger retry
- Already had status indicator logic for SENDING and FAILED states

### 6. build.gradle.kts
**Changes:**
- Added Gson dependency: `implementation("com.google.code.gson:gson:2.10.1")`
- Note: Ended up using JSONArray/JSONObject instead for simpler implementation

## Message Status Flow

1. **SENDING** - Message is being sent or queued
   - Shows clock icon
   - Message is in offline queue

2. **SENT** - Message successfully sent to Firestore
   - Shows single checkmark
   - Message removed from queue

3. **DELIVERED** - Message delivered to recipient
   - Shows double checkmark (gray)

4. **READ** - Message read by recipient
   - Shows double checkmark (blue)

5. **FAILED** - Message failed to send after retries
   - Shows error icon (red)
   - User can tap to retry
   - Message remains in queue

## Offline Queue Behavior

### When Offline:
1. User sends message
2. Message is queued with SENDING status
3. Message appears in chat with clock icon
4. Toast shows "No internet connection" message

### When Connection Restored:
1. ConnectionMonitor detects connection
2. ViewModel automatically calls `processQueuedMessages()`
3. All queued messages are sent
4. Successfully sent messages are removed from queue
5. Failed messages are marked as FAILED

### Manual Retry:
1. User taps on failed message
2. Confirmation dialog appears
3. User confirms retry
4. Message status changes to SENDING
5. Attempt to send message
6. On success: remove from queue
7. On failure: mark as FAILED again

## Data Persistence

Messages are stored in SharedPreferences as JSON:
- **Key:** `queued_messages`
- **Format:** JSONArray of message objects
- **Fields:** id, chatId, senderId, senderName, senderImageUrl, text, timestamp, status

## Testing Checklist

✅ **Offline Message Queue:**
- [x] Create `OfflineMessageQueue.kt` helper class
- [x] Queue messages when offline
- [x] Show "Sending..." status for queued messages
- [x] Auto-send when connection restored
- [x] Show "Failed" status if send fails after retry
- [x] Allow manual retry for failed messages

## How to Test

1. **Test Offline Queueing:**
   - Turn on airplane mode
   - Send a message
   - Verify message shows with clock icon (SENDING status)
   - Verify message is in offline queue

2. **Test Auto-Send:**
   - With queued messages, turn off airplane mode
   - Verify messages are automatically sent
   - Verify messages show checkmark (SENT status)
   - Verify messages are removed from queue

3. **Test Failed Messages:**
   - Simulate network error (disconnect during send)
   - Verify message shows error icon (FAILED status)
   - Tap on failed message
   - Verify retry dialog appears
   - Confirm retry
   - Verify message is resent

4. **Test Connection Status:**
   - Turn on airplane mode
   - Verify toast shows "No internet connection"
   - Turn off airplane mode
   - Verify queued messages are sent

5. **Test Message Persistence:**
   - Queue messages while offline
   - Close and reopen app
   - Verify queued messages still appear
   - Turn on connection
   - Verify messages are sent

## Requirements Covered

✅ **Requirement 1.9:** WHEN a user is offline THEN the system SHALL queue messages for sending when connection is restored

## Next Steps

The offline message queue is now fully implemented. The next task in the spec is:

**Task 8: Set up Firebase Cloud Messaging service** (Phase 2: Push Notifications)

## Notes

- The implementation uses JSONArray/JSONObject for serialization instead of Gson to avoid additional dependencies
- Messages are stored in SharedPreferences for simplicity and persistence
- Connection monitoring uses ConnectivityManager with NetworkCallback for real-time updates
- Maximum retry attempts is set to 3 before marking message as FAILED
- The ViewModel automatically processes queued messages when connection is restored
- Users can manually retry failed messages by tapping on them
