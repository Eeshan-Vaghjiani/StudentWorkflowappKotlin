# Task 12: Chat Message Sending and Reading - Quick Reference

## Quick Overview

Task 12 implements comprehensive error handling, offline support, and retry logic for chat message sending and reading.

## Key Features

### 1. Offline Message Queue
- Messages queued when offline
- Persistent across app restarts
- Automatic retry when online
- Manual retry option

### 2. Error Handling
- Permission errors: No retry, marked as FAILED
- Network errors: Retry with exponential backoff
- Validation errors: Immediate rejection
- User-friendly error messages

### 3. Retry Logic
- Automatic retry on network restore
- Exponential backoff (1s → 2s → 4s → 8s → 16s → 30s max)
- Max 3 retry attempts
- Manual retry for failed messages

### 4. Read Status
- Batch updates for efficiency
- Graceful error handling
- Partial failure support
- Non-blocking operations

### 5. Typing Status
- Real-time updates
- Non-critical (fails silently)
- Doesn't block message sending
- Permission error handling

## Quick Usage

### Send Message
```kotlin
val result = chatRepository.sendMessage(chatId, messageText)
if (result.isSuccess) {
    // Message sent
} else {
    // Handle error
    ErrorHandler.handleError(context, result.exceptionOrNull(), view)
}
```

### Mark Messages as Read
```kotlin
val result = chatRepository.markMessagesAsRead(chatId, messageIds)
// Can ignore result (non-critical)
```

### Update Typing Status
```kotlin
chatRepository.updateTypingStatus(chatId, isTyping = true)
// Can ignore result (non-critical)
```

### Retry Failed Message
```kotlin
val result = chatRepository.retryMessage(failedMessage)
if (result.isSuccess) {
    showToast("Message sent")
}
```

### Process Queued Messages
```kotlin
lifecycleScope.launch {
    val result = chatRepository.processQueuedMessages()
    val sentCount = result.getOrNull() ?: 0
    Log.d(TAG, "Sent $sentCount queued messages")
}
```

### Retry with Backoff
```kotlin
lifecycleScope.launch {
    chatRepository.retryPendingMessagesWithBackoff()
}
```

## Queue Management

### Check Queue Status
```kotlin
val pendingCount = offlineQueue?.getPendingMessageCount()
val failedCount = offlineQueue?.getFailedMessageCount()
```

### Get Queued Messages
```kotlin
val allQueued = chatRepository.getAllQueuedMessages()
val chatQueued = chatRepository.getQueuedMessagesForChat(chatId)
```

### Clear Queue
```kotlin
chatRepository.clearOfflineQueue() // Clear all
offlineQueue?.clearFailedMessages() // Clear only failed
```

## Message Status Flow

```
User sends message
       ↓
   SENDING (queued)
       ↓
   [Network check]
       ↓
   ┌───────────┬───────────┐
   ↓           ↓           ↓
Success    Network     Permission
   ↓        Error        Error
 SENT         ↓           ↓
   ↓      Retry       FAILED
   ↓         ↓        (no retry)
   ↓      SENT
   ↓         ↓
   └─────────┘
       ↓
   DELIVERED
       ↓
     READ
```

## Error Handling Flow

```
Operation attempted
       ↓
safeFirestoreCall
       ↓
   [Error?]
       ↓
   ┌───────────┬───────────┬───────────┐
   ↓           ↓           ↓           ↓
Permission  Network   Validation  Unknown
  Error      Error      Error      Error
   ↓           ↓           ↓           ↓
No retry    Retry      Return     Retry
Mark FAILED  Queue    Error Msg   Queue
   ↓           ↓           ↓           ↓
User msg    Backoff   User msg   User msg
```

## Configuration

### Constants
```kotlin
MAX_RETRY_ATTEMPTS = 3
RETRY_THRESHOLD_MS = 30000L // 30 seconds
RETRY_DELAY_MS = 500L // Between retries
BASE_BACKOFF_DELAY = 1000L // 1 second
MAX_BACKOFF_DELAY = 30000L // 30 seconds
```

### Backoff Delays
- 0-30s old: 1s delay
- 30-60s old: 2s delay
- 60-90s old: 4s delay
- 90-120s old: 8s delay
- 120-150s old: 16s delay
- >150s old: 30s delay (max)

## Common Patterns

### Send with Error Handling
```kotlin
lifecycleScope.launch {
    val result = chatRepository.sendMessage(chatId, text)
    when {
        result.isSuccess -> {
            // Success
            clearInput()
        }
        result.exceptionOrNull()?.message?.contains("Permission") == true -> {
            // Permission error
            showError("You don't have permission to send messages")
        }
        else -> {
            // Other error
            showError("Failed to send message. Will retry automatically.")
        }
    }
}
```

### Monitor Queue
```kotlin
// In ViewModel or Activity
fun observeQueue() {
    lifecycleScope.launch {
        while (isActive) {
            val pending = chatRepository.getAllQueuedMessages()
                .count { it.status == MessageStatus.SENDING }
            val failed = chatRepository.getAllQueuedMessages()
                .count { it.status == MessageStatus.FAILED }
            
            updateQueueUI(pending, failed)
            delay(5000) // Check every 5 seconds
        }
    }
}
```

### Retry Failed Messages
```kotlin
// Show retry button for failed messages
if (message.status == MessageStatus.FAILED) {
    retryButton.setOnClickListener {
        lifecycleScope.launch {
            val result = chatRepository.retryMessage(message)
            if (result.isSuccess) {
                showToast("Message sent")
            } else {
                showToast("Retry failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
```

### Handle Network Changes
```kotlin
// In Activity or Service
private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        lifecycleScope.launch {
            // Network restored, process queued messages
            chatRepository.processQueuedMessages()
        }
    }
}
```

## Debugging

### Enable Verbose Logging
```kotlin
// In ChatRepository
private const val TAG = "ChatRepository"
Log.d(TAG, "Message sent: ${message.id}")
Log.w(TAG, "Retry attempt ${retryCount + 1}")
Log.e(TAG, "Error sending message", exception)
```

### Check Firestore
```javascript
// In Firebase Console
db.collection('chats')
  .doc(chatId)
  .collection('messages')
  .where('status', '==', 'FAILED')
  .get()
```

### Monitor Queue
```kotlin
// Check SharedPreferences
val prefs = context.getSharedPreferences("offline_message_queue", Context.MODE_PRIVATE)
val json = prefs.getString("queued_messages", null)
Log.d(TAG, "Queue: $json")
```

## Troubleshooting

### Messages Not Sending
1. Check network connectivity
2. Verify Firestore rules
3. Check error logs
4. Try manual retry
5. Clear and resend

### Queue Not Working
1. Check storage permissions
2. Verify SharedPreferences
3. Check Gson serialization
4. Review error logs

### Excessive Retries
1. Verify MAX_RETRY_ATTEMPTS
2. Check permission error detection
3. Review backoff calculation
4. Monitor retry logs

### Read Status Not Updating
1. Check Firestore rules
2. Verify batch update logic
3. Check error logs
4. Test with single message

## Best Practices

### Do's ✅
- Always handle Result types
- Check message status before retry
- Use batch operations for efficiency
- Log errors for debugging
- Clear queue on success
- Validate input before sending
- Handle partial failures gracefully

### Don'ts ❌
- Don't retry permission errors
- Don't block UI thread
- Don't ignore error results
- Don't retry indefinitely
- Don't expose sensitive errors
- Don't skip validation
- Don't forget to clear queue

## Performance Tips

1. **Batch Operations**: Use batch updates for multiple messages
2. **Delays**: Add delays between retries to avoid flooding
3. **Backoff**: Use exponential backoff for retries
4. **Non-Critical**: Let non-critical operations fail silently
5. **Cleanup**: Clear queue regularly to prevent bloat

## Security Tips

1. **Validation**: Always validate and sanitize input
2. **Authentication**: Check user authentication before operations
3. **Permissions**: Verify permissions before operations
4. **Error Messages**: Don't expose sensitive information
5. **Logging**: Log errors but protect user data

## Files Reference

### Implementation
- `utils/OfflineMessageQueue.kt` - Queue management
- `repository/ChatRepository.kt` - Chat operations
- `utils/ErrorHandler.kt` - Error handling
- `models/Message.kt` - Message model

### Documentation
- `TASK_12_IMPLEMENTATION_SUMMARY.md` - Full details
- `TASK_12_TESTING_GUIDE.md` - Testing procedures
- `TASK_12_VERIFICATION_CHECKLIST.md` - Verification steps
- `TASK_12_QUICK_REFERENCE.md` - This file

### Tests
- `ChatMessageSendingAndReadingTest.kt` - Unit tests

## Support

### Logs to Check
- ChatRepository logs (TAG: "ChatRepository")
- ErrorHandler logs (TAG: "ErrorHandler")
- OfflineMessageQueue logs (TAG: "OfflineMessageQueue")
- Firebase Crashlytics reports

### Metrics to Monitor
- Message send success rate
- Queue size over time
- Failed message count
- Retry success rate
- Average send time

## Quick Commands

### Run Tests
```bash
./gradlew :app:testDebugUnitTest --tests "ChatMessageSendingAndReadingTest"
```

### Check Diagnostics
```bash
./gradlew :app:lintDebug
```

### Build App
```bash
./gradlew :app:assembleDebug
```

## Summary

Task 12 provides:
- ✅ Robust error handling
- ✅ Offline message queue
- ✅ Smart retry logic
- ✅ Exponential backoff
- ✅ Reliable message delivery
- ✅ User-friendly errors
- ✅ Production-ready code

For detailed information, see the full implementation summary and testing guide.
