# TeamSync API Documentation

## Overview

This document provides comprehensive API documentation for the TeamSync Collaboration app, including data models, validation requirements, and code examples for common operations with the enhanced Firebase Security Rules.

---

## Table of Contents

1. [Data Models](#data-models)
2. [Validation Requirements](#validation-requirements)
3. [Common Operations](#common-operations)
4. [Error Handling](#error-handling)
5. [Best Practices](#best-practices)

---

## Data Models

### User

**Collection:** `users`

**Document ID:** User's Firebase Auth UID

**Fields:**
```typescript
{
  id: string                    // Firebase Auth UID
  email: string                 // User's email address
  name: string                  // Display name
  profileImageUrl?: string      // Firebase Storage URL or empty (max 5MB image)
  createdAt: number            // Timestamp (milliseconds)
  lastActive?: number          // Timestamp (milliseconds)
}
```

**Validation Rules:**
- `profileImageUrl` must be empty or start with `https://firebasestorage.googleapis.com/`
- Only the user can create/update their own document
- All authenticated users can read user documents


**Example:**
```kotlin
// Create user profile
val user = hashMapOf(
    "id" to currentUserId,
    "email" to email,
    "name" to displayName,
    "profileImageUrl" to "", // Empty initially
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("users")
    .document(currentUserId)
    .set(user)
```

---

### Group

**Collection:** `groups`

**Document ID:** Auto-generated

**Fields:**
```typescript
{
  id: string                    // Document ID
  name: string                  // Group name
  description: string           // Group description
  subject: string              // Subject/category
  joinCode: string             // 6-character join code
  memberIds: string[]          // Array of user IDs (1-100 members)
  owner: string                // Owner's user ID (must be in memberIds)
  isPublic: boolean            // Public groups are discoverable
  createdAt: number            // Timestamp (milliseconds)
}
```


**Validation Rules:**
- `memberIds` must contain 1-100 members
- `owner` must be in `memberIds` array
- Members can read the group
- Anyone can read if `isPublic` is `true`
- Owner or members can update
- Only owner can delete

**Example:**
```kotlin
// Create a public group
val group = hashMapOf(
    "name" to "Computer Science Study Group",
    "description" to "Weekly study sessions",
    "subject" to "Computer Science",
    "joinCode" to generateJoinCode(),
    "memberIds" to listOf(currentUserId),
    "owner" to currentUserId,
    "isPublic" to true,
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("groups")
    .add(group)
    .addOnSuccessListener { documentReference ->
        val groupId = documentReference.id
        // Update document with its ID
        documentReference.update("id", groupId)
    }
```


```kotlin
// Query public groups
firestore.collection("groups")
    .whereEqualTo("isPublic", true)
    .get()
    .addOnSuccessListener { documents ->
        val publicGroups = documents.map { it.toObject(Group::class.java) }
    }

// Add member to group
firestore.collection("groups")
    .document(groupId)
    .update("memberIds", FieldValue.arrayUnion(newUserId))
```

---

### Task

**Collection:** `tasks`

**Document ID:** Auto-generated

**Fields:**
```typescript
{
  id: string                    // Document ID
  title: string                 // Task title
  description: string           // Task description
  category: string             // Category (e.g., "Study", "Project")
  priority: string             // Priority level
  status: string               // Status (e.g., "TODO", "IN_PROGRESS", "DONE")
  dueDate: number              // Timestamp (milliseconds)
  groupId?: string             // Optional group ID
  assignedTo: string[]         // Array of user IDs (1-50 users)
  userId: string               // Creator's user ID
  createdAt: number            // Timestamp (milliseconds)
}
```


**Validation Rules:**
- `assignedTo` must contain 1-50 users
- Creator must be in `assignedTo` on creation
- If `groupId` is set, creator must be a group member
- Creator or assigned users can read the task
- Group members can read group tasks
- Only creator can delete

**Example:**
```kotlin
// Create personal task
val task = hashMapOf(
    "title" to "Complete homework",
    "description" to "Math assignment chapter 5",
    "category" to "Study",
    "priority" to "HIGH",
    "status" to "TODO",
    "dueDate" to dueDate.time,
    "assignedTo" to listOf(currentUserId),
    "userId" to currentUserId,
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("tasks")
    .add(task)
```


```kotlin
// Create group task
val groupTask = hashMapOf(
    "title" to "Prepare presentation",
    "description" to "Create slides for group project",
    "category" to "Project",
    "priority" to "HIGH",
    "status" to "TODO",
    "dueDate" to dueDate.time,
    "groupId" to groupId,
    "assignedTo" to listOf(currentUserId, userId2),
    "userId" to currentUserId,
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("tasks")
    .add(groupTask)

// Query user's tasks
firestore.collection("tasks")
    .whereEqualTo("userId", currentUserId)
    .get()

// Query tasks assigned to user
firestore.collection("tasks")
    .whereArrayContains("assignedTo", currentUserId)
    .get()
```

---

### Chat

**Collection:** `chats`

**Document ID:** Auto-generated

**Fields:**
```typescript
{
  id: string                              // Document ID
  type: 'DIRECT' | 'GROUP'               // Chat type
  participants: string[]                  // Array of user IDs (2 for DIRECT, 2-100 for GROUP)
  participantDetails: Map<string, {      // User info for each participant
    name: string
    imageUrl: string
  }>
  lastMessage: string                     // Last message text
  lastMessageTime: number                 // Timestamp (milliseconds)
  lastMessageSenderId: string            // Sender's user ID
  unreadCount: Map<string, number>       // Unread count per user
  groupId?: string                       // Optional group ID for group chats
  groupName?: string                     // Optional group name
  createdAt: number                      // Timestamp (milliseconds)
}
```


**Validation Rules:**
- DIRECT chats must have exactly 2 participants
- GROUP chats must have 2-100 participants
- Only participants can read/write
- Cannot modify `participants` array via update

**Example:**
```kotlin
// Create direct chat
val directChat = hashMapOf(
    "type" to "DIRECT",
    "participants" to listOf(currentUserId, otherUserId),
    "participantDetails" to mapOf(
        currentUserId to mapOf("name" to currentUserName, "imageUrl" to currentUserImage),
        otherUserId to mapOf("name" to otherUserName, "imageUrl" to otherUserImage)
    ),
    "lastMessage" to "",
    "lastMessageTime" to System.currentTimeMillis(),
    "lastMessageSenderId" to "",
    "unreadCount" to mapOf(currentUserId to 0, otherUserId to 0),
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("chats")
    .add(directChat)
```


```kotlin
// Create group chat
val groupChat = hashMapOf(
    "type" to "GROUP",
    "participants" to participantIds, // 2-100 users
    "participantDetails" to participantDetailsMap,
    "groupId" to groupId,
    "groupName" to groupName,
    "lastMessage" to "",
    "lastMessageTime" to System.currentTimeMillis(),
    "lastMessageSenderId" to "",
    "unreadCount" to unreadCountMap,
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("chats")
    .add(groupChat)

// Query user's chats
firestore.collection("chats")
    .whereArrayContains("participants", currentUserId)
    .orderBy("lastMessageTime", Query.Direction.DESCENDING)
    .get()
```

---

### Message

**Collection:** `chats/{chatId}/messages` (subcollection)

**Document ID:** Auto-generated

**Fields:**
```typescript
{
  id: string                    // Document ID
  chatId: string               // Parent chat ID
  senderId: string             // Sender's user ID
  senderName: string           // Sender's display name
  senderImageUrl: string       // Sender's profile image
  text?: string                // Message text (max 10,000 characters)
  imageUrl?: string            // Firebase Storage URL for image
  documentUrl?: string         // Firebase Storage URL for document
  documentName?: string        // Document filename
  documentSize?: number        // Document size in bytes
  timestamp: number            // Timestamp (must be within 5 minutes of server time)
  readBy: string[]            // Array of user IDs who read the message
  status: string              // Message status
}
```


**Validation Rules:**
- Must have at least one of: `text`, `imageUrl`, or `documentUrl`
- `text` max length: 10,000 characters
- URLs must be Firebase Storage URLs or empty
- `senderId` must match authenticated user
- `timestamp` must be within 5 minutes of server time
- Only chat participants can read/write messages
- Only sender can update/delete their own messages

**Example:**
```kotlin
// Send text message
val message = hashMapOf(
    "chatId" to chatId,
    "senderId" to currentUserId,
    "senderName" to currentUserName,
    "senderImageUrl" to currentUserImage,
    "text" to messageText,
    "timestamp" to System.currentTimeMillis(),
    "readBy" to listOf(currentUserId),
    "status" to "SENT"
)

firestore.collection("chats")
    .document(chatId)
    .collection("messages")
    .add(message)
```


```kotlin
// Send image message
val imageMessage = hashMapOf(
    "chatId" to chatId,
    "senderId" to currentUserId,
    "senderName" to currentUserName,
    "senderImageUrl" to currentUserImage,
    "imageUrl" to firebaseStorageUrl, // From Firebase Storage upload
    "timestamp" to System.currentTimeMillis(),
    "readBy" to listOf(currentUserId),
    "status" to "SENT"
)

firestore.collection("chats")
    .document(chatId)
    .collection("messages")
    .add(imageMessage)

// Query messages
firestore.collection("chats")
    .document(chatId)
    .collection("messages")
    .orderBy("timestamp", Query.Direction.ASCENDING)
    .addSnapshotListener { snapshot, error ->
        // Handle messages
    }
```

---

### Notification

**Collection:** `notifications`

**Document ID:** Auto-generated

**Fields:**
```typescript
{
  id: string                    // Document ID
  userId: string               // Recipient's user ID
  type: string                 // Notification type
  title: string                // Notification title
  message: string              // Notification message
  chatId?: string             // Optional chat ID
  groupId?: string            // Optional group ID
  taskId?: string             // Optional task ID
  read: boolean               // Read status
  createdAt: number           // Timestamp (milliseconds)
}
```


**Validation Rules:**
- **Client-side creation is BLOCKED** (use Cloud Functions)
- Users can only read their own notifications
- Users can only update the `read` field
- Users can delete their own notifications

**Example:**
```kotlin
// Mark notification as read (client-side)
firestore.collection("notifications")
    .document(notificationId)
    .update("read", true)

// Delete notification (client-side)
firestore.collection("notifications")
    .document(notificationId)
    .delete()

// Query user's notifications
firestore.collection("notifications")
    .whereEqualTo("userId", currentUserId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
```

**Cloud Function Example (for creating notifications):**
```typescript
// functions/src/index.ts
export const onNewMessage = functions.firestore
  .document('chats/{chatId}/messages/{messageId}')
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const chatDoc = await admin.firestore()
      .collection('chats')
      .doc(context.params.chatId)
      .get();
    
    const participants = chatDoc.data()?.participants || [];
    
    // Create notifications for all participants except sender
    const batch = admin.firestore().batch();
    participants
      .filter(uid => uid !== message.senderId)
      .forEach(uid => {
        const notificationRef = admin.firestore().collection('notifications').doc();
        batch.set(notificationRef, {
          userId: uid,
          type: 'NEW_MESSAGE',
          title: 'New message',
          message: `${message.senderName}: ${message.text || 'Sent an attachment'}`,
          chatId: context.params.chatId,
          read: false,
          createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
      });
    
    await batch.commit();
  });
```

---


## Validation Requirements

### Summary Table

| Collection | Field | Requirement | Error Message |
|------------|-------|-------------|---------------|
| users | profileImageUrl | Firebase Storage URL or empty | "Invalid image URL" |
| groups | memberIds | 1-100 members | "Group member limit exceeded" |
| groups | owner | Must be in memberIds | "Group owner must be a member" |
| tasks | assignedTo | 1-50 users | "Too many users assigned to task" |
| tasks | groupId | Creator must be group member | "You must be a group member" |
| chats (DIRECT) | participants | Exactly 2 | "Direct chats must have 2 participants" |
| chats (GROUP) | participants | 2-100 | "Invalid number of participants" |
| messages | text | Max 10,000 chars | "Message too long" |
| messages | content | At least one field | "Message must have content" |
| messages | URLs | Firebase Storage only | "Invalid file URL" |
| messages | timestamp | Within 5 minutes | "Invalid timestamp" |
| Storage | profile pictures | Images only, 5MB | "File type not supported" / "File too large" |
| Storage | chat attachments | Approved types, 10MB | "File type not supported" / "File too large" |

### Client-Side Validation Helper

Use the `FirebaseRulesValidator` utility class for client-side validation:

```kotlin
// app/src/main/java/com/example/loginandregistration/utils/FirebaseRulesValidator.kt

object FirebaseRulesValidator {
    
    fun validateChatParticipants(type: ChatType, participants: List<String>): ValidationResult {
        return when (type) {
            ChatType.DIRECT -> {
                if (participants.size != 2) {
                    ValidationResult.Error("Direct chats must have exactly 2 participants")
                } else {
                    ValidationResult.Success
                }
            }
            ChatType.GROUP -> {
                when {
                    participants.size < 2 -> 
                        ValidationResult.Error("Group chats must have at least 2 participants")
                    participants.size > 100 -> 
                        ValidationResult.Error("Maximum 100 participants allowed")
                    else -> ValidationResult.Success
                }
            }
        }
    }
    
    fun validateGroupMembers(memberIds: List<String>, ownerId: String): ValidationResult {
        return when {
            memberIds.isEmpty() -> 
                ValidationResult.Error("Group must have at least one member")
            memberIds.size > 100 -> 
                ValidationResult.Error("Maximum 100 members allowed")
            ownerId !in memberIds -> 
                ValidationResult.Error("Group owner must be a member")
            else -> ValidationResult.Success
        }
    }
    
    fun validateTaskAssignments(assignedTo: List<String>): ValidationResult {
        return when {
            assignedTo.isEmpty() -> 
                ValidationResult.Error("Task must be assigned to at least one user")
            assignedTo.size > 50 -> 
                ValidationResult.Error("Maximum 50 users can be assigned")
            else -> ValidationResult.Success
        }
    }
    
    fun validateMessageText(text: String?): ValidationResult {
        if (text != null && text.length > 10000) {
            return ValidationResult.Error("Message cannot exceed 10,000 characters")
        }
        return ValidationResult.Success
    }
    
    fun validateMessageContent(text: String?, imageUrl: String?, documentUrl: String?): ValidationResult {
        if (text.isNullOrBlank() && imageUrl.isNullOrBlank() && documentUrl.isNullOrBlank()) {
            return ValidationResult.Error("Message must have text, image, or document")
        }
        return ValidationResult.Success
    }
}
```

---


## Common Operations

### 1. Create and Join a Public Group

```kotlin
// Step 1: Create a public group
suspend fun createPublicGroup(name: String, description: String, subject: String): String {
    val group = hashMapOf(
        "name" to name,
        "description" to description,
        "subject" to subject,
        "joinCode" to generateJoinCode(),
        "memberIds" to listOf(currentUserId),
        "owner" to currentUserId,
        "isPublic" to true,
        "createdAt" to System.currentTimeMillis()
    )
    
    val docRef = firestore.collection("groups").add(group).await()
    docRef.update("id", docRef.id).await()
    return docRef.id
}

// Step 2: Search for public groups
suspend fun searchPublicGroups(subject: String? = null): List<Group> {
    val query = firestore.collection("groups")
        .whereEqualTo("isPublic", true)
    
    val finalQuery = if (subject != null) {
        query.whereEqualTo("subject", subject)
    } else {
        query
    }
    
    val snapshot = finalQuery.get().await()
    return snapshot.documents.mapNotNull { it.toObject(Group::class.java) }
}

// Step 3: Join a public group
suspend fun joinGroup(groupId: String) {
    firestore.collection("groups")
        .document(groupId)
        .update("memberIds", FieldValue.arrayUnion(currentUserId))
        .await()
}
```

### 2. Create a Group Task

```kotlin
suspend fun createGroupTask(
    groupId: String,
    title: String,
    description: String,
    dueDate: Long,
    assignedUserIds: List<String>
): String {
    // Validate group membership
    val group = firestore.collection("groups")
        .document(groupId)
        .get()
        .await()
        .toObject(Group::class.java)
    
    if (currentUserId !in group?.memberIds.orEmpty()) {
        throw IllegalStateException("You must be a group member to create group tasks")
    }
    
    // Validate assignments
    val validation = FirebaseRulesValidator.validateTaskAssignments(assignedUserIds)
    if (validation is ValidationResult.Error) {
        throw IllegalArgumentException(validation.message)
    }
    
    // Create task
    val task = hashMapOf(
        "title" to title,
        "description" to description,
        "category" to "Group",
        "priority" to "MEDIUM",
        "status" to "TODO",
        "dueDate" to dueDate,
        "groupId" to groupId,
        "assignedTo" to assignedUserIds,
        "userId" to currentUserId,
        "createdAt" to System.currentTimeMillis()
    )
    
    val docRef = firestore.collection("tasks").add(task).await()
    docRef.update("id", docRef.id).await()
    return docRef.id
}
```

### 3. Send a Message with Image

```kotlin
suspend fun sendImageMessage(chatId: String, imageUri: Uri): String {
    // Step 1: Validate file
    val contentResolver = context.contentResolver
    val validation = validateImageFile(imageUri, contentResolver)
    if (validation is ValidationResult.Error) {
        throw IllegalArgumentException(validation.message)
    }
    
    // Step 2: Upload to Firebase Storage
    val storageRef = FirebaseStorage.getInstance()
        .reference
        .child("chat_attachments/$chatId/${UUID.randomUUID()}.jpg")
    
    val uploadTask = storageRef.putFile(imageUri).await()
    val downloadUrl = storageRef.downloadUrl.await().toString()
    
    // Step 3: Create message
    val message = hashMapOf(
        "chatId" to chatId,
        "senderId" to currentUserId,
        "senderName" to currentUserName,
        "senderImageUrl" to currentUserImage,
        "imageUrl" to downloadUrl,
        "timestamp" to System.currentTimeMillis(),
        "readBy" to listOf(currentUserId),
        "status" to "SENT"
    )
    
    val docRef = firestore.collection("chats")
        .document(chatId)
        .collection("messages")
        .add(message)
        .await()
    
    // Step 4: Update chat's last message
    firestore.collection("chats")
        .document(chatId)
        .update(
            "lastMessage", "📷 Image",
            "lastMessageTime", System.currentTimeMillis(),
            "lastMessageSenderId", currentUserId
        )
        .await()
    
    return docRef.id
}

private fun validateImageFile(uri: Uri, contentResolver: ContentResolver): ValidationResult {
    val mimeType = contentResolver.getType(uri)
    val allowedTypes = listOf("image/jpeg", "image/png", "image/webp")
    
    if (mimeType !in allowedTypes) {
        return ValidationResult.Error("Only JPEG, PNG, and WebP images are supported")
    }
    
    val fileSize = getFileSize(uri, contentResolver)
    if (fileSize > 5 * 1024 * 1024) {
        return ValidationResult.Error("Image must be under 5MB")
    }
    
    return ValidationResult.Success
}
```

### 4. Upload Profile Picture

```kotlin
suspend fun uploadProfilePicture(imageUri: Uri): String {
    // Step 1: Validate image
    val contentResolver = context.contentResolver
    val validation = validateImageFile(imageUri, contentResolver)
    if (validation is ValidationResult.Error) {
        throw IllegalArgumentException(validation.message)
    }
    
    // Step 2: Upload to Firebase Storage
    val storageRef = FirebaseStorage.getInstance()
        .reference
        .child("profile_pictures/$currentUserId/${UUID.randomUUID()}.jpg")
    
    storageRef.putFile(imageUri).await()
    val downloadUrl = storageRef.downloadUrl.await().toString()
    
    // Step 3: Update user profile
    firestore.collection("users")
        .document(currentUserId)
        .update("profileImageUrl", downloadUrl)
        .await()
    
    return downloadUrl
}
```

### 5. Create a Direct Chat

```kotlin
suspend fun createDirectChat(otherUserId: String): String {
    // Step 1: Check if chat already exists
    val existingChat = firestore.collection("chats")
        .whereEqualTo("type", "DIRECT")
        .whereArrayContains("participants", currentUserId)
        .get()
        .await()
        .documents
        .firstOrNull { doc ->
            val participants = doc.get("participants") as? List<*>
            participants?.contains(otherUserId) == true
        }
    
    if (existingChat != null) {
        return existingChat.id
    }
    
    // Step 2: Get user details
    val currentUser = firestore.collection("users")
        .document(currentUserId)
        .get()
        .await()
        .toObject(User::class.java)!!
    
    val otherUser = firestore.collection("users")
        .document(otherUserId)
        .get()
        .await()
        .toObject(User::class.java)!!
    
    // Step 3: Validate participants
    val participants = listOf(currentUserId, otherUserId)
    val validation = FirebaseRulesValidator.validateChatParticipants(ChatType.DIRECT, participants)
    if (validation is ValidationResult.Error) {
        throw IllegalArgumentException(validation.message)
    }
    
    // Step 4: Create chat
    val chat = hashMapOf(
        "type" to "DIRECT",
        "participants" to participants,
        "participantDetails" to mapOf(
            currentUserId to mapOf(
                "name" to currentUser.name,
                "imageUrl" to currentUser.profileImageUrl.orEmpty()
            ),
            otherUserId to mapOf(
                "name" to otherUser.name,
                "imageUrl" to otherUser.profileImageUrl.orEmpty()
            )
        ),
        "lastMessage" to "",
        "lastMessageTime" to System.currentTimeMillis(),
        "lastMessageSenderId" to "",
        "unreadCount" to mapOf(
            currentUserId to 0,
            otherUserId to 0
        ),
        "createdAt" to System.currentTimeMillis()
    )
    
    val docRef = firestore.collection("chats").add(chat).await()
    docRef.update("id", docRef.id).await()
    return docRef.id
}
```

---


## Error Handling

### Common Firebase Errors

```kotlin
fun handleFirebaseError(exception: Exception): String {
    val message = exception.message ?: return "An error occurred"
    
    return when {
        // Permission errors
        message.contains("PERMISSION_DENIED", ignoreCase = true) -> {
            parsePermissionError(message)
        }
        
        // Network errors
        message.contains("UNAVAILABLE", ignoreCase = true) -> {
            "Network error. Please check your connection."
        }
        
        // Not found errors
        message.contains("NOT_FOUND", ignoreCase = true) -> {
            "Resource not found."
        }
        
        // Already exists errors
        message.contains("ALREADY_EXISTS", ignoreCase = true) -> {
            "Resource already exists."
        }
        
        else -> "An error occurred. Please try again."
    }
}

private fun parsePermissionError(message: String): String {
    return when {
        message.contains("participants") -> 
            "Invalid number of participants for this chat type"
        message.contains("memberIds") -> 
            "Group member limit exceeded or invalid membership"
        message.contains("assignedTo") -> 
            "Invalid task assignments. Check user limits."
        message.contains("text") && message.contains("size") -> 
            "Message is too long. Maximum 10,000 characters."
        message.contains("contentType") -> 
            "File type not supported"
        message.contains("size") -> 
            "File size exceeds limit"
        message.contains("isGroupMember") -> 
            "You must be a group member to perform this action"
        message.contains("isChatParticipant") -> 
            "You must be a chat participant to perform this action"
        else -> 
            "Permission denied. You don't have access to this resource."
    }
}
```

### Repository Error Handling Pattern

```kotlin
class ChatRepository {
    
    suspend fun sendMessage(chatId: String, text: String): Result<String> {
        return try {
            // Validate before sending
            val validation = FirebaseRulesValidator.validateMessageText(text)
            if (validation is ValidationResult.Error) {
                return Result.failure(IllegalArgumentException(validation.message))
            }
            
            val contentValidation = FirebaseRulesValidator.validateMessageContent(text, null, null)
            if (contentValidation is ValidationResult.Error) {
                return Result.failure(IllegalArgumentException(contentValidation.message))
            }
            
            // Create message
            val message = hashMapOf(
                "chatId" to chatId,
                "senderId" to currentUserId,
                "text" to text,
                "timestamp" to System.currentTimeMillis(),
                // ... other fields
            )
            
            val docRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .await()
            
            Result.success(docRef.id)
            
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            Result.failure(e)
        }
    }
}
```

### ViewModel Error Handling

```kotlin
class ChatViewModel : ViewModel() {
    
    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> = _errorState
    
    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            val result = chatRepository.sendMessage(chatId, text)
            
            result.onSuccess { messageId ->
                // Handle success
                _errorState.value = ""
            }.onFailure { exception ->
                // Handle error
                _errorState.value = handleFirebaseError(exception)
            }
        }
    }
}
```

---


## Best Practices

### 1. Always Validate Client-Side First

Validate data before attempting Firestore writes to provide immediate feedback:

```kotlin
// ❌ Bad: No validation
fun createGroup(name: String, memberIds: List<String>) {
    firestore.collection("groups").add(groupData)
        .addOnFailureListener { e ->
            // User sees generic Firebase error
            showError(e.message)
        }
}

// ✅ Good: Validate first
fun createGroup(name: String, memberIds: List<String>, ownerId: String) {
    val validation = FirebaseRulesValidator.validateGroupMembers(memberIds, ownerId)
    if (validation is ValidationResult.Error) {
        showError(validation.message) // Clear, immediate feedback
        return
    }
    
    firestore.collection("groups").add(groupData)
        .addOnFailureListener { e ->
            showError(handleFirebaseError(e))
        }
}
```

### 2. Use Firebase Storage for All Files

Never use external URLs for images or documents:

```kotlin
// ❌ Bad: External URL
val user = hashMapOf(
    "profileImageUrl" to "https://example.com/image.jpg" // Will be rejected
)

// ✅ Good: Firebase Storage URL
suspend fun uploadAndSetProfilePicture(imageUri: Uri) {
    val storageUrl = uploadToFirebaseStorage(imageUri)
    firestore.collection("users")
        .document(currentUserId)
        .update("profileImageUrl", storageUrl) // Valid Firebase Storage URL
}
```

### 3. Check Limits Before Operations

Prevent errors by checking limits before adding members or participants:

```kotlin
// ✅ Good: Check before adding
suspend fun addGroupMember(groupId: String, newUserId: String) {
    val group = firestore.collection("groups")
        .document(groupId)
        .get()
        .await()
        .toObject(Group::class.java)!!
    
    if (group.memberIds.size >= 100) {
        showError("Group has reached maximum capacity (100 members)")
        return
    }
    
    firestore.collection("groups")
        .document(groupId)
        .update("memberIds", FieldValue.arrayUnion(newUserId))
        .await()
}
```

### 4. Handle Permission Errors Gracefully

Show user-friendly messages when rules block operations:

```kotlin
// ✅ Good: User-friendly error messages
.addOnFailureListener { exception ->
    val userMessage = when {
        exception.message?.contains("PERMISSION_DENIED") == true -> {
            parsePermissionError(exception.message!!)
        }
        else -> "An error occurred. Please try again."
    }
    
    showError(userMessage)
}
```

### 5. Use Transactions for Complex Operations

Use transactions when updating multiple related documents:

```kotlin
// ✅ Good: Use transaction for consistency
suspend fun transferTaskOwnership(taskId: String, newOwnerId: String) {
    firestore.runTransaction { transaction ->
        val taskRef = firestore.collection("tasks").document(taskId)
        val task = transaction.get(taskRef).toObject(Task::class.java)!!
        
        // Verify current user is owner
        if (task.userId != currentUserId) {
            throw IllegalStateException("Only task owner can transfer ownership")
        }
        
        // Update task
        transaction.update(taskRef, mapOf(
            "userId" to newOwnerId,
            "assignedTo" to FieldValue.arrayUnion(newOwnerId)
        ))
    }.await()
}
```

### 6. Implement Proper Cleanup

Clean up related data when deleting resources:

```kotlin
// ✅ Good: Clean up related data
suspend fun deleteGroup(groupId: String) {
    // Delete group tasks
    val tasks = firestore.collection("tasks")
        .whereEqualTo("groupId", groupId)
        .get()
        .await()
    
    val batch = firestore.batch()
    tasks.documents.forEach { doc ->
        batch.delete(doc.reference)
    }
    
    // Delete group activities
    val activities = firestore.collection("group_activities")
        .whereEqualTo("groupId", groupId)
        .get()
        .await()
    
    activities.documents.forEach { doc ->
        batch.delete(doc.reference)
    }
    
    // Delete group
    batch.delete(firestore.collection("groups").document(groupId))
    
    batch.commit().await()
}
```

### 7. Use Pagination for Large Collections

Implement pagination to avoid loading too much data:

```kotlin
// ✅ Good: Paginated queries
class MessagePaginator(private val chatId: String) {
    private var lastDocument: DocumentSnapshot? = null
    private val pageSize = 20
    
    suspend fun loadNextPage(): List<Message> {
        val query = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())
        
        val finalQuery = lastDocument?.let { query.startAfter(it) } ?: query
        
        val snapshot = finalQuery.get().await()
        lastDocument = snapshot.documents.lastOrNull()
        
        return snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
    }
}
```

### 8. Test with Firebase Emulator

Always test rule changes in the emulator before deploying:

```bash
# Start emulator
firebase emulators:start

# Run tests
cd firestore-rules-tests
npm test
```

### 9. Monitor Production Errors

Set up monitoring to catch permission errors in production:

```kotlin
// Log Firebase errors to Crashlytics
.addOnFailureListener { exception ->
    if (exception.message?.contains("PERMISSION_DENIED") == true) {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_type", "permission_denied")
            setCustomKey("operation", "create_message")
            recordException(exception)
        }
    }
    
    showError(handleFirebaseError(exception))
}
```

### 10. Document Custom Validation Logic

Document any custom validation in your code:

```kotlin
/**
 * Creates a new group task.
 * 
 * Validation:
 * - User must be a member of the group
 * - assignedTo must contain 1-50 users
 * - Creator must be in assignedTo array
 * 
 * Firebase Rules:
 * - Enforces group membership via isGroupMember()
 * - Enforces assignment limits (1-50)
 * 
 * @throws IllegalStateException if user is not a group member
 * @throws IllegalArgumentException if validation fails
 */
suspend fun createGroupTask(
    groupId: String,
    title: String,
    assignedUserIds: List<String>
): String {
    // Implementation
}
```

---

## Related Documentation

- [Firebase Rules Features](./FIREBASE_RULES_FEATURES.md) - Detailed feature documentation
- [Migration Guide](./MIGRATION_GUIDE.md) - Migration and rollback procedures
- [Testing Guide](../../firestore-rules-tests/README-TESTING.md) - Testing with Firebase Emulator

---

## Quick Reference

### Validation Limits

- **Chat Participants:** 2 (DIRECT), 2-100 (GROUP)
- **Group Members:** 1-100
- **Task Assignments:** 1-50
- **Message Text:** 10,000 characters
- **Profile Pictures:** 5MB, images only
- **Chat Attachments:** 10MB, approved types

### Required Fields

- **Group:** name, description, subject, joinCode, memberIds, owner, createdAt
- **Task:** title, description, category, priority, status, dueDate, assignedTo, userId, createdAt
- **Chat:** type, participants, participantDetails, createdAt
- **Message:** chatId, senderId, senderName, timestamp, status (+ at least one of: text, imageUrl, documentUrl)

### Access Control

- **Users:** Read all, write own
- **Groups:** Read if member or public, write if owner/member
- **Tasks:** Read if creator/assigned/group member, write if creator/assigned
- **Chats:** Read/write if participant
- **Messages:** Read/write if chat participant, update/delete own only
- **Notifications:** Read/update/delete own only, create via Cloud Functions only

---

**Last Updated:** October 25, 2025
