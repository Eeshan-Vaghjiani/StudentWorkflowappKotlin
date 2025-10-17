# Task 6: Chat Functionality - Quick Reference

## What Was Fixed

### The Problem
Users couldn't create new chats or send messages due to Firestore permission errors. The security rules were checking if the user was a participant in a chat document that didn't exist yet.

### The Solution
Updated Firestore rules to allow chat creation when the user is in the `participants` array of the document being created.

## Key Changes

### Firestore Rules (firestore.rules)

**Before**:
```javascript
match /chats/{chatId} {
  allow read: if isParticipant(chatId);
  allow write: if isParticipant(chatId);  // ❌ Fails for new chats
}
```

**After**:
```javascript
match /chats/{chatId} {
  allow read: if isAuthenticated() && 
    request.auth.uid in resource.data.participants;
  
  allow create: if isAuthenticated() && 
    request.auth.uid in request.resource.data.participants;  // ✅ Works for new chats
  
  allow update: if isAuthenticated() && 
    request.auth.uid in resource.data.participants;
}
```

## How to Use

### Creating a New Chat

1. Open the app and go to the Chat tab
2. Tap the FAB (floating action button) with the "+" icon
3. Search for a user by name or email
4. Tap on a user to create a chat
5. Start sending messages!

### Sending Messages

1. Open a chat from the chat list
2. Type your message in the input field
3. Tap the send button
4. Message appears immediately with "sending" status
5. Status updates to "sent" when delivered

### Sending Images/Documents

1. Open a chat
2. Tap the attachment button (paperclip icon)
3. Choose "Image" or "Document"
4. Select the file
5. Wait for upload progress
6. File appears in chat when complete

## API Reference

### ChatRepository Methods

```kotlin
// Create or get existing direct chat
suspend fun getOrCreateDirectChat(
    otherUserId: String,
    otherUserInfo: UserInfo? = null
): Result<Chat>

// Send a text message
suspend fun sendMessage(
    chatId: String,
    text: String,
    retryCount: Int = 0
): Result<Message>

// Send an image message
suspend fun sendImageMessage(
    chatId: String,
    imageUri: Uri,
    onProgress: (Int) -> Unit = {}
): Result<Message>

// Send a document message
suspend fun sendDocumentMessage(
    chatId: String,
    documentUri: Uri,
    onProgress: (Int) -> Unit = {}
): Result<Message>

// Get messages for a chat (real-time)
fun getChatMessages(
    chatId: String,
    limit: Int = 50
): Flow<List<Message>>

// Retry a failed message
suspend fun retryMessage(message: Message): Result<Message>

// Delete a message (sender only)
suspend fun deleteMessage(
    chatId: String,
    message: Message
): Result<Unit>
```

## Firestore Structure

### Chat Document
```
chats/{chatId}
  - chatId: String
  - type: "DIRECT" | "GROUP"
  - participants: [userId1, userId2]
  - participantDetails: {
      userId1: { displayName, email, profileImageUrl, ... },
      userId2: { ... }
    }
  - lastMessage: String
  - lastMessageTime: Timestamp
  - lastMessageSenderId: String
  - unreadCount: { userId1: 0, userId2: 3 }
  - groupId: String? (for group chats)
  - groupName: String? (for group chats)
  - createdAt: Timestamp
```

### Message Document
```
chats/{chatId}/messages/{messageId}
  - id: String
  - chatId: String
  - senderId: String
  - senderName: String
  - senderImageUrl: String
  - text: String
  - imageUrl: String? (for image messages)
  - documentUrl: String? (for document messages)
  - documentName: String? (for document messages)
  - documentSize: Long? (for document messages)
  - timestamp: Timestamp
  - readBy: [userId1, userId2]
  - status: "SENDING" | "SENT" | "READ" | "FAILED"
```

## Common Issues & Solutions

### Issue: "Permission denied" when creating chat
**Solution**: Make sure Firestore rules are deployed:
```bash
firebase deploy --only firestore:rules
```

### Issue: Messages not appearing in real-time
**Solution**: Check that the app has internet connection and Firestore listeners are active

### Issue: "User not found" when creating chat
**Solution**: The other user needs to log into the app at least once to create their profile

### Issue: Failed messages stuck in queue
**Solution**: Use the retry button or check internet connection

## Testing Commands

### Deploy Firestore Rules
```bash
firebase deploy --only firestore:rules
```

### View Firestore Rules
```bash
firebase firestore:rules:get
```

### Test Rules Locally (with emulator)
```bash
firebase emulators:start --only firestore
```

## Security Notes

- Only authenticated users can access chats
- Users can only read/write chats they are participants in
- Only message senders can delete their own messages
- All operations are logged for security auditing

## Performance Tips

- Messages are paginated (50 per load)
- Images are compressed before upload
- Offline messages are queued and sent when online
- Real-time listeners are lifecycle-aware (stop when app is in background)

## Related Files

- `firestore.rules` - Security rules
- `ChatRepository.kt` - Data layer
- `ChatFragment.kt` - Chat list UI
- `ChatRoomActivity.kt` - Chat room UI
- `ChatViewModel.kt` - Chat list logic
- `ChatRoomViewModel.kt` - Chat room logic
- `UserSearchDialog.kt` - User search UI

## Next Task

After completing this task, proceed to:
- **Task 7**: Update Firestore Security Rules (comprehensive review)
- **Task 8**: Remove All Demo Data Dependencies
