# Task 28: Offline Message Queue - Visual Guide

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        ChatRoomActivity                          │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Connection Status Banner (Orange/Green)               │    │
│  └────────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Messages RecyclerView                                  │    │
│  │  ┌──────────────────────────────────────────────┐     │    │
│  │  │ [Sent Message] ✓✓                            │     │    │
│  │  └──────────────────────────────────────────────┘     │    │
│  │  ┌──────────────────────────────────────────────┐     │    │
│  │  │ [Sending Message] 🕐                         │     │    │
│  │  └──────────────────────────────────────────────┘     │    │
│  │  ┌──────────────────────────────────────────────┐     │    │
│  │  │ [Failed Message] ⚠️ [Retry]                 │     │    │
│  │  └──────────────────────────────────────────────┘     │    │
│  └────────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Message Input + Send Button                           │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                      ChatRoomViewModel                           │
│  • Observes ConnectionMonitor.isConnected                       │
│  • Merges Firestore + Queued messages                           │
│  • Calls processQueuedMessages() when online                    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                       ChatRepository                             │
│  • sendMessage() → Queue → Firestore                            │
│  • retryMessage() → Retry failed messages                       │
│  • processQueuedMessages() → Send all queued                    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────┬──────────────────────────────────────────┐
│  OfflineMessageQueue │         ConnectionMonitor                │
│  • SharedPreferences │  • NetworkCallback                       │
│  • JSON storage      │  • Flow<Boolean>                         │
│  • Queue/Dequeue     │  • Real-time updates                     │
└──────────────────────┴──────────────────────────────────────────┘
```

## Message Flow Diagram

### Scenario 1: Send Message While Online

```
User Types Message
       ↓
Click Send Button
       ↓
ChatRoomViewModel.sendMessage()
       ↓
ChatRepository.sendMessage()
       ↓
Queue Message (SENDING) ──────────────┐
       ↓                               │
Send to Firestore ✓                   │
       ↓                               │
Remove from Queue ←───────────────────┘
       ↓
Update Status (SENT)
       ↓
Display with Checkmark ✓
```

### Scenario 2: Send Message While Offline

```
User Types Message
       ↓
Click Send Button
       ↓
ChatRoomViewModel.sendMessage()
       ↓
ChatRepository.sendMessage()
       ↓
Queue Message (SENDING) ──────────────┐
       ↓                               │
Attempt Send to Firestore ✗           │
       ↓                               │
Send Fails (No Connection)            │
       ↓                               │
Keep in Queue ←───────────────────────┘
       ↓
Display with Clock Icon 🕐
       ↓
Wait for Connection...
```

### Scenario 3: Connection Restored

```
Connection Restored
       ↓
ConnectionMonitor Detects
       ↓
Emit isConnected = true
       ↓
ChatRoomViewModel Receives
       ↓
Call processQueuedMessages()
       ↓
ChatRepository.processQueuedMessages()
       ↓
For Each Queued Message:
  ├─→ Attempt Send
  ├─→ If Success: Remove from Queue
  └─→ If Fail: Mark as FAILED
       ↓
Update UI with Results
       ↓
Show "Connected" Banner (2s)
```

## Connection Status Banner States

### State 1: Online (Default)
```
┌─────────────────────────────────────────┐
│  [Toolbar]                              │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  [Messages]                             │
│                                         │
```
**Banner:** Hidden
**Color:** N/A

### State 2: Offline
```
┌─────────────────────────────────────────┐
│  [Toolbar]                              │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  🔴 No internet connection              │  ← Orange Banner
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  [Messages]                             │
│                                         │
```
**Banner:** Visible
**Color:** Orange (#FFA726)
**Text:** "No internet connection"

### State 3: Connecting/Connected
```
┌─────────────────────────────────────────┐
│  [Toolbar]                              │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  ✓ Connected                            │  ← Green Banner
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  [Messages]                             │
│                                         │
```
**Banner:** Visible (2 seconds)
**Color:** Green (#689F38)
**Text:** "Connected"
**Auto-hide:** After 2 seconds

## Message Status Indicators

### Status: SENDING
```
┌────────────────────────────────────┐
│  Hello, how are you?          🕐   │  ← Clock Icon
└────────────────────────────────────┘
```
**Icon:** Clock (🕐)
**Color:** Gray
**Meaning:** Message queued, waiting to send

### Status: SENT
```
┌────────────────────────────────────┐
│  Hello, how are you?          ✓    │  ← Single Checkmark
└────────────────────────────────────┘
```
**Icon:** Single Checkmark (✓)
**Color:** Gray
**Meaning:** Message sent to server

### Status: DELIVERED
```
┌────────────────────────────────────┐
│  Hello, how are you?          ✓✓   │  ← Double Checkmark
└────────────────────────────────────┘
```
**Icon:** Double Checkmark (✓✓)
**Color:** Gray
**Meaning:** Message delivered to recipient

### Status: READ
```
┌────────────────────────────────────┐
│  Hello, how are you?          ✓✓   │  ← Blue Double Checkmark
└────────────────────────────────────┘
```
**Icon:** Double Checkmark (✓✓)
**Color:** Blue
**Meaning:** Message read by recipient

### Status: FAILED
```
┌────────────────────────────────────┐
│  Hello, how are you?     ⚠️ [Retry]│  ← Error Icon + Button
└────────────────────────────────────┘
```
**Icon:** Warning (⚠️)
**Color:** Red
**Action:** Retry button available
**Meaning:** Message failed to send after 3 attempts

## Queue Storage Structure

### SharedPreferences File
```
File: offline_message_queue.xml
Location: /data/data/com.example.app/shared_prefs/

Content:
{
  "queued_messages": "[
    {
      \"id\": \"msg_123\",
      \"chatId\": \"chat_456\",
      \"senderId\": \"user_789\",
      \"senderName\": \"John Doe\",
      \"senderImageUrl\": \"https://...\",
      \"text\": \"Hello offline\",
      \"timestamp\": 1696800000000,
      \"status\": \"SENDING\"
    },
    {
      \"id\": \"msg_124\",
      \"chatId\": \"chat_456\",
      \"senderId\": \"user_789\",
      \"senderName\": \"John Doe\",
      \"senderImageUrl\": \"https://...\",
      \"text\": \"Another message\",
      \"timestamp\": 1696800001000,
      \"status\": \"SENDING\"
    }
  ]"
}
```

## User Interaction Flow

### Flow 1: Normal Send (Online)
```
1. User opens chat
   └─→ Connection banner: Hidden
   
2. User types message
   └─→ Typing indicator sent to other users
   
3. User clicks send
   └─→ Message appears with clock icon (brief)
   └─→ Message sent to Firestore
   └─→ Clock changes to checkmark
   └─→ Message input clears
   
4. Other user receives
   └─→ Message appears on their device
   └─→ Sender sees double checkmark
```

### Flow 2: Send While Offline
```
1. User opens chat
   └─→ Connection banner: "No internet connection" (Orange)
   
2. User types message
   └─→ Typing indicator NOT sent (offline)
   
3. User clicks send
   └─→ Message appears with clock icon
   └─→ Message queued locally
   └─→ Message stays with clock icon
   └─→ Message input clears
   
4. User can send more messages
   └─→ All messages queued
   └─→ All show clock icon
   
5. Connection restored
   └─→ Banner changes to "Connected" (Green)
   └─→ All queued messages sent automatically
   └─→ Clock icons change to checkmarks
   └─→ Banner disappears after 2 seconds
```

### Flow 3: Retry Failed Message
```
1. Message failed after 3 attempts
   └─→ Shows warning icon ⚠️
   └─→ Shows [Retry] button
   
2. User long-presses message
   └─→ Context menu appears
   └─→ Options: Copy, Delete, Retry
   
3. User selects "Retry"
   └─→ Confirmation dialog appears
   └─→ "Do you want to retry sending this message?"
   
4. User confirms
   └─→ Message status changes to SENDING (clock)
   └─→ Send attempted again
   
5. If online:
   └─→ Message sends successfully
   └─→ Clock changes to checkmark
   └─→ Message removed from queue
   
6. If offline:
   └─→ Message stays in queue
   └─→ Will auto-send when online
```

## Timeline Example

### Example: User Goes Offline and Back Online

```
Time    Event                           UI State
────────────────────────────────────────────────────────────────
10:00   User online, chatting          Banner: Hidden
                                       Messages: All sent ✓✓

10:05   User enters tunnel             Banner: Hidden
        (Connection dropping)          Messages: All sent ✓✓

10:06   Connection lost                Banner: "No internet" 🔴
        User sends "Hello"             Message: "Hello" 🕐

10:07   User sends "Are you there?"    Message: "Are you there?" 🕐
                                       Queue: 2 messages

10:08   User exits app                 Queue: Persisted to disk

10:10   User reopens app               Banner: "No internet" 🔴
        (Still in tunnel)              Messages: Both with 🕐

10:12   User exits tunnel              Banner: "Connected" ✅
        Connection restored            Messages: Sending...

10:13   Messages sent                  Messages: Both with ✓
                                       Queue: Cleared

10:15   Banner auto-hides              Banner: Hidden
                                       Messages: All sent ✓✓
```

## Code Flow Visualization

### sendMessage() Flow
```kotlin
fun sendMessage(chatId: String, text: String) {
    // 1. Queue message
    offlineQueue?.queueMessage(message)
    
    try {
        // 2. Attempt to send
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .await()
        
        // 3. Success: Remove from queue
        offlineQueue?.removeMessage(messageId)
        
    } catch (e: Exception) {
        // 4. Failure: Keep in queue
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            offlineQueue?.markMessageAsFailed(messageId)
        }
    }
}
```

### processQueuedMessages() Flow
```kotlin
suspend fun processQueuedMessages() {
    // 1. Get all queued messages
    val queuedMessages = offlineQueue?.getQueuedMessages()
    
    // 2. For each message
    for (message in queuedMessages) {
        // Skip failed messages
        if (message.status == FAILED) continue
        
        // 3. Retry sending
        val result = retryMessage(message)
        
        // 4. Track success count
        if (result.isSuccess) successCount++
    }
    
    // 5. Return count
    return Result.success(successCount)
}
```

## Testing Scenarios Visualization

### Scenario: Multiple Messages Queued
```
Before (Offline):
┌─────────────────────────────────────┐
│ 🔴 No internet connection           │
├─────────────────────────────────────┤
│ Message 1                      🕐   │
│ Message 2                      🕐   │
│ Message 3                      🕐   │
│ Message 4                      🕐   │
│ Message 5                      🕐   │
└─────────────────────────────────────┘

After (Online):
┌─────────────────────────────────────┐
│ ✅ Connected                         │
├─────────────────────────────────────┤
│ Message 1                      ✓    │
│ Message 2                      ✓    │
│ Message 3                      ✓    │
│ Message 4                      ✓    │
│ Message 5                      ✓    │
└─────────────────────────────────────┘

2 seconds later:
┌─────────────────────────────────────┐
│ Message 1                      ✓✓   │
│ Message 2                      ✓✓   │
│ Message 3                      ✓✓   │
│ Message 4                      ✓✓   │
│ Message 5                      ✓✓   │
└─────────────────────────────────────┘
```

## Summary

This visual guide illustrates:
- ✅ Architecture and component relationships
- ✅ Message flow through the system
- ✅ Connection status banner states
- ✅ Message status indicators
- ✅ Queue storage structure
- ✅ User interaction flows
- ✅ Timeline examples
- ✅ Code flow visualization
- ✅ Testing scenarios

The offline message queue provides a seamless experience for users, automatically handling network interruptions without data loss or user frustration.
