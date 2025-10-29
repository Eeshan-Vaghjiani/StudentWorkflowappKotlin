# Task 7: Enhanced Offline Message Queue - Quick Reference

## For Developers Using This Feature

### Message Status Types

```kotlin
enum class MessageStatus {
    SENDING,              // Message is being sent
    SENT,                 // Message sent successfully
    DELIVERED,            // Message delivered to recipient
    READ,                 // Message read by recipient
    FAILED,               // Generic failure (legacy)
    FAILED_RETRYABLE,     // Failed but will retry automatically (network issues)
    FAILED_PERMANENT      // Failed permanently (permission errors, no retry)
}
```

### Setting Up Automatic Retry in Your ViewModel

```kotlin
class ChatRoomViewModel(
    application: Application,
    private val chatRepository: ChatRepository
) : AndroidViewModel(application) {
    
    private val retryManager = MessageRetryManager(
        context = application,
        chatRepository = chatRepository,
        scope = viewModelScope
    )
    
    init {
        // Start observing network connectivity
        retryManager.startObserving()
    }
    
    override fun onCleared() {
        // Stop observing when ViewModel is cleared
        retryManager.stopObserving()
        super.onCleared()
    }
    
    // Optional: Manual retry trigger
    fun retryFailedMessages() {
        viewModelScope.launch {
            val result = retryManager.manualRetry()
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                // Show success message: "$count messages retried"
            }
        }
    }
}
```

### Handling Retry in UI (Already Implemented)

The MessageAdapter automatically handles retry clicks:

```kotlin
// In your Fragment/Activity
val adapter = MessageAdapter(
    currentUserId = currentUserId,
    onRetryMessage = { message ->
        // Retry the message
        viewModel.retryMessage(message)
    }
)
```

### Checking Queue Status

```kotlin
// Get count of pending messages
val pendingCount = offlineQueue.getPendingMessageCount()

// Get count of retryable failures
val retryableCount = offlineQueue.getFailedRetryableCount()

// Get count of permanent failures
val permanentCount = offlineQueue.getFailedPermanentCount()

// Get all messages that need retry
val retryableMessages = offlineQueue.getAllRetryableMessages()
```

### Manual Message Retry

```kotlin
// In your ViewModel
fun retryMessage(message: Message) {
    viewModelScope.launch {
        val result = chatRepository.retryMessage(message)
        if (result.isSuccess) {
            // Message sent successfully
        } else {
            // Handle failure
        }
    }
}
```

### Clearing Failed Messages

```kotlin
// Clear all permanently failed messages
offlineQueue.clearPermanentlyFailedMessages()

// Clear all failed messages (retryable and permanent)
offlineQueue.clearFailedMessages()

// Clear entire queue
offlineQueue.clearQueue()
```

## Visual Indicators

### Message Status Icons

| Status | Icon | Color | User Action |
|--------|------|-------|-------------|
| SENDING | 🕐 Clock | White (dim) | Wait |
| SENT | ✓ Check | White (dim) | None |
| DELIVERED | ✓✓ Double Check | White (dim) | None |
| READ | ✓✓ Double Check | Blue | None |
| FAILED_RETRYABLE | ⚠️ Error | Orange | Tap to retry |
| FAILED_PERMANENT | ❌ Error | Dark Red | Cannot retry |

### Color Meanings
- **White (dim)**: Normal status, no action needed
- **Blue**: Message has been read
- **Orange**: Temporary failure, will retry automatically or tap to retry now
- **Dark Red**: Permanent failure (permission error), cannot retry

## Automatic Retry Behavior

### When Retry Happens
1. **Network Restored**: Automatically retries when internet connection is restored
2. **App Foreground**: Retries pending messages when app comes to foreground
3. **Manual Trigger**: User taps retry button on failed message

### Retry Limits
- **Max Attempts**: 5 attempts per message
- **Retry Interval**: Minimum 30 seconds between automatic retry attempts
- **Delay Between Messages**: 500ms delay between individual message retries

### What Gets Retried
- Messages with `SENDING` status (stuck in queue)
- Messages with `FAILED_RETRYABLE` status (network errors)
- Messages that haven't exceeded max retry attempts

### What Doesn't Get Retried
- Messages with `FAILED_PERMANENT` status (permission errors)
- Messages that exceeded 5 retry attempts
- Messages successfully sent (`SENT`, `DELIVERED`, `READ`)

## Error Categorization

### Retryable Errors (FAILED_RETRYABLE)
- `UNAVAILABLE`: Firestore service temporarily unavailable
- `DEADLINE_EXCEEDED`: Request timeout
- `ABORTED`: Transaction aborted
- Network errors (no internet connection)
- Unknown Firestore errors (safe default)

### Permanent Errors (FAILED_PERMANENT)
- `PERMISSION_DENIED`: User doesn't have permission to send message
- Messages that exceeded max retry attempts (5)

## Testing Scenarios

### Test Automatic Retry
1. Turn off WiFi/mobile data
2. Send a message → should show SENDING status
3. Turn on WiFi/mobile data
4. Message should automatically retry and change to SENT

### Test Manual Retry
1. Send message while offline → shows orange error icon
2. Tap the error icon
3. If online, message should send immediately

### Test Permanent Failure
1. Remove user from chat participants (simulate permission error)
2. Try to send message
3. Should show dark red error icon (not clickable)

### Test Max Retry Attempts
1. Keep device offline
2. Send message (attempt 1)
3. Manually retry 4 more times
4. After 5th attempt, should convert to FAILED_PERMANENT

## Troubleshooting

### Messages Not Retrying Automatically
- Check if MessageRetryManager is started in ViewModel
- Verify network connectivity observer is working
- Check if messages are marked as FAILED_RETRYABLE (not PERMANENT)
- Ensure retry interval (30s) has passed since last attempt

### Retry Button Not Working
- Verify onRetryMessage callback is set in MessageAdapter
- Check if message status is FAILED_RETRYABLE (not PERMANENT)
- Ensure ChatRepository.retryMessage() is called in ViewModel

### Messages Stuck in SENDING
- Check if offline queue is properly initialized
- Verify ChatRepository has context for OfflineMessageQueue
- Check for exceptions in sendMessage() method

## Performance Tips

### Optimize Queue Size
```kotlin
// Periodically clean up old permanent failures
viewModelScope.launch {
    delay(3600000) // Every hour
    offlineQueue.clearPermanentlyFailedMessages()
}
```

### Batch Operations
```kotlin
// Process all queued messages at once
chatRepository.processQueuedMessages()

// Or retry only failed messages
chatRepository.retryFailedMessages()
```

### Monitor Queue Health
```kotlin
// Check queue status periodically
val queueHealth = QueueHealth(
    pending = offlineQueue.getPendingMessageCount(),
    retryable = offlineQueue.getFailedRetryableCount(),
    permanent = offlineQueue.getFailedPermanentCount()
)

// Alert if too many permanent failures
if (queueHealth.permanent > 10) {
    // Show user notification or clear old failures
}
```

## API Reference

### OfflineMessageQueue
```kotlin
// Queue management
fun queueMessage(message: Message)
fun removeMessage(messageId: String)

// Status updates
fun markMessageAsFailedRetryable(messageId: String)
fun markMessageAsFailedPermanent(messageId: String)
fun updateMessageStatus(messageId: String, status: MessageStatus)
fun incrementAttempts(messageId: String)

// Queries
fun getQueuedMessages(): List<Message>
fun getQueuedMessagesForChat(chatId: String): List<Message>
fun getAllRetryableMessages(): List<Message>
fun getPendingMessageCount(): Int
fun getFailedRetryableCount(): Int
fun getFailedPermanentCount(): Int

// Cleanup
fun clearQueue()
fun clearFailedMessages()
fun clearPermanentlyFailedMessages()
```

### ChatRepository
```kotlin
// Retry operations
suspend fun retryMessage(message: Message): Result<Message>
suspend fun retryFailedMessages(): Result<Int>
suspend fun processQueuedMessages(): Result<Int>

// Queue access
fun getQueuedMessagesForChat(chatId: String): List<Message>
fun getAllQueuedMessages(): List<Message>
fun clearOfflineQueue()
```

### MessageRetryManager
```kotlin
// Lifecycle
fun startObserving()
fun stopObserving()

// Operations
suspend fun manualRetry(): Result<Int>
fun isNetworkAvailable(): Boolean
```

### NetworkConnectivityObserver
```kotlin
// Observe network state
fun observe(): Flow<Boolean>

// Check current state
fun isNetworkAvailable(): Boolean
fun isNetworkConnected(): Boolean
```

## Best Practices

1. **Always start MessageRetryManager in ViewModel init**
2. **Always stop MessageRetryManager in onCleared()**
3. **Use viewModelScope for retry operations**
4. **Periodically clean up permanent failures**
5. **Show user feedback for manual retry results**
6. **Don't retry on FAILED_PERMANENT status**
7. **Respect retry limits (max 5 attempts)**
8. **Add delay between retries to avoid server overload**

## Common Patterns

### Show Retry Status to User
```kotlin
// In your ViewModel
val retryStatus = MutableLiveData<String>()

fun retryFailedMessages() {
    viewModelScope.launch {
        retryStatus.value = "Retrying messages..."
        val result = retryManager.manualRetry()
        retryStatus.value = if (result.isSuccess) {
            val count = result.getOrNull() ?: 0
            "Successfully retried $count messages"
        } else {
            "Failed to retry messages"
        }
    }
}
```

### Monitor Network State
```kotlin
// In your ViewModel
val isOnline = MutableLiveData<Boolean>()

init {
    viewModelScope.launch {
        networkObserver.observe().collect { connected ->
            isOnline.value = connected
            if (connected) {
                // Optionally trigger manual retry
                retryFailedMessages()
            }
        }
    }
}
```

### Show Queue Status
```kotlin
// In your ViewModel
val queueStatus = MutableLiveData<QueueStatus>()

fun updateQueueStatus() {
    queueStatus.value = QueueStatus(
        pending = offlineQueue.getPendingMessageCount(),
        retryable = offlineQueue.getFailedRetryableCount(),
        permanent = offlineQueue.getFailedPermanentCount()
    )
}

data class QueueStatus(
    val pending: Int,
    val retryable: Int,
    val permanent: Int
) {
    val total = pending + retryable + permanent
    val hasFailures = retryable > 0 || permanent > 0
}
```
