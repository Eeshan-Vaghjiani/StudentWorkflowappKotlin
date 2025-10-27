# Firebase Security Rules - New Features Documentation

## Overview

This document describes the enhanced security features and validation rules implemented in the TeamSync Firebase Security Rules. These improvements strengthen data security, prevent abuse, and enable new features like public group discovery.

---

## 1. Public Group Discovery

### Feature Description

Groups can now be marked as public, allowing any authenticated user to discover and view them before joining. This enables users to find study groups and collaboration spaces that match their interests.

### Implementation

**Firestore Rule:**
```javascript
match /groups/{groupId} {
  allow read: if isAuthenticated() && 
    (resource.data.get('isPublic', false) == true ||
     request.auth.uid in resource.data.memberIds);
}
```

### Data Model

Add the `isPublic` field to group documents:

```typescript
{
  id: string
  name: string
  description: string
  subject: string
  joinCode: string
  memberIds: string[]
  owner: string
  isPublic: boolean  // NEW: Set to true for public groups
  createdAt: number
}
```

### Usage Examples

**Creating a Public Group:**
```kotlin
val group = hashMapOf(
    "name" to "Computer Science Study Group",
    "description" to "Weekly study sessions for CS students",
    "subject" to "Computer Science",
    "joinCode" to generateJoinCode(),
    "memberIds" to listOf(currentUserId),
    "owner" to currentUserId,
    "isPublic" to true,  // Make group discoverable
    "createdAt" to System.currentTimeMillis()
)

firestore.collection("groups").add(group)
```

**Querying Public Groups:**
```kotlin
// Find all public groups
firestore.collection("groups")
    .whereEqualTo("isPublic", true)
    .get()
    .addOnSuccessListener { documents ->
        // Display public groups
    }

// Find public groups by subject
firestore.collection("groups")
    .whereEqualTo("isPublic", true)
    .whereEqualTo("subject", "Mathematics")
    .get()
```

### Backward Compatibility

- Existing groups without `isPublic` field default to `false` (private)
- No migration required for existing data
- Private groups remain accessible only to members

---

## 2. Validation Limits

### 2.1 Chat Participant Limits

**Purpose:** Prevent abuse and maintain app performance by limiting chat sizes.

**Rules:**
- **Direct Chats:** Exactly 2 participants (enforced)
- **Group Chats:** 2-100 participants (enforced)

**Implementation:**
```javascript
allow create: if isAuthenticated() && 
  request.auth.uid in request.resource.data.participants &&
  ((request.resource.data.type == 'DIRECT' && 
    request.resource.data.participants.size() == 2) ||
   (request.resource.data.type == 'GROUP' && 
    isValidArraySize(request.resource.data.participants, 2, 100)));
```

**Client-Side Validation:**
```kotlin
// In ChatRepository.kt or similar
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
                    ValidationResult.Error("Group chats cannot exceed 100 participants")
                else -> ValidationResult.Success
            }
        }
    }
}
```

### 2.2 Group Member Limits

**Purpose:** Maintain app performance and prevent abuse.

**Rules:**
- Minimum: 1 member (the owner)
- Maximum: 100 members
- Owner must always be in memberIds array

**Implementation:**
```javascript
allow create: if isAuthenticated() && 
  request.auth.uid in request.resource.data.memberIds &&
  request.auth.uid == request.resource.data.owner &&
  isValidArraySize(request.resource.data.memberIds, 1, 100);
```

**Client-Side Validation:**
```kotlin
fun validateGroupMembers(memberIds: List<String>, ownerId: String): ValidationResult {
    return when {
        memberIds.isEmpty() -> 
            ValidationResult.Error("Group must have at least one member")
        memberIds.size > 100 -> 
            ValidationResult.Error("Groups cannot exceed 100 members")
        ownerId !in memberIds -> 
            ValidationResult.Error("Group owner must be a member")
        else -> ValidationResult.Success
    }
}
```

### 2.3 Task Assignment Limits

**Purpose:** Prevent abuse and maintain reasonable task complexity.

**Rules:**
- Minimum: 1 assigned user
- Maximum: 50 assigned users
- Creator must be in assignedTo array on creation

**Implementation:**
```javascript
allow create: if isAuthenticated() && 
  request.auth.uid == request.resource.data.userId &&
  isValidArraySize(request.resource.data.get('assignedTo', [request.auth.uid]), 1, 50);
```

**Client-Side Validation:**
```kotlin
fun validateTaskAssignments(assignedTo: List<String>, creatorId: String, isNewTask: Boolean): ValidationResult {
    return when {
        assignedTo.isEmpty() -> 
            ValidationResult.Error("Task must be assigned to at least one user")
        assignedTo.size > 50 -> 
            ValidationResult.Error("Tasks cannot be assigned to more than 50 users")
        isNewTask && creatorId !in assignedTo -> 
            ValidationResult.Error("Task creator must be in assigned users")
        else -> ValidationResult.Success
    }
}
```

### 2.4 Message Length Limits

**Purpose:** Prevent abuse and maintain reasonable message sizes.

**Rules:**
- Maximum text length: 10,000 characters
- At least one content field required (text, imageUrl, or documentUrl)
- Timestamp must be within 5 minutes of server time

**Implementation:**
```javascript
allow create: if isAuthenticated() && 
  isChatParticipant(chatId) &&
  (request.resource.data.get('text', '').size() > 0 ||
   request.resource.data.get('imageUrl', null) != null ||
   request.resource.data.get('documentUrl', null) != null) &&
  isValidStringLength(request.resource.data.get('text', ''), 10000) &&
  isRecentTimestamp(request.resource.data.timestamp);
```

**Client-Side Validation:**
```kotlin
fun validateMessage(text: String?, imageUrl: String?, documentUrl: String?): ValidationResult {
    // Check at least one content field
    if (text.isNullOrBlank() && imageUrl.isNullOrBlank() && documentUrl.isNullOrBlank()) {
        return ValidationResult.Error("Message must have text, image, or document")
    }
    
    // Check text length
    if (text != null && text.length > 10000) {
        return ValidationResult.Error("Message text cannot exceed 10,000 characters")
    }
    
    return ValidationResult.Success
}
```

---

## 3. File Type Restrictions

### 3.1 Profile Pictures

**Purpose:** Ensure only valid images are used as profile pictures.

**Allowed Types:**
- `image/jpeg`
- `image/png`
- `image/webp`

**Size Limit:** 5MB

**Storage Rule:**
```javascript
match /profile_pictures/{userId}/{fileName} {
  allow read: if isAuthenticated();
  allow write: if isAuthenticated() && 
                  isOwner(userId) && 
                  isValidImageSize() &&
                  isValidImageType();
}
```

**Client-Side Implementation:**
```kotlin
fun validateProfilePicture(uri: Uri, contentResolver: ContentResolver): ValidationResult {
    val mimeType = contentResolver.getType(uri)
    val allowedTypes = listOf("image/jpeg", "image/png", "image/webp")
    
    if (mimeType !in allowedTypes) {
        return ValidationResult.Error("Profile pictures must be JPEG, PNG, or WebP images")
    }
    
    val fileSize = getFileSize(uri, contentResolver)
    if (fileSize > 5 * 1024 * 1024) {
        return ValidationResult.Error("Profile pictures must be under 5MB")
    }
    
    return ValidationResult.Success
}
```

### 3.2 Chat Attachments

**Purpose:** Allow common file types while preventing malicious uploads.

**Allowed Types:**
- **Images:** `image/*` (all image types)
- **Documents:** 
  - `application/pdf`
  - `application/msword` (DOC)
  - `application/vnd.openxmlformats-officedocument.*` (DOCX, XLSX, PPTX)
  - `text/*` (TXT, CSV, etc.)

**Size Limits:**
- Images: 5MB
- Documents: 10MB

**Storage Rule:**
```javascript
match /chat_attachments/{chatId}/{fileName} {
  allow read: if isAuthenticated();
  allow write: if isAuthenticated() && 
                  isValidDocumentSize() &&
                  isValidDocumentType();
}
```

**Client-Side Implementation:**
```kotlin
fun validateChatAttachment(uri: Uri, contentResolver: ContentResolver): ValidationResult {
    val mimeType = contentResolver.getType(uri) ?: return ValidationResult.Error("Unknown file type")
    
    val allowedTypes = listOf(
        "image/",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument",
        "text/"
    )
    
    if (!allowedTypes.any { mimeType.startsWith(it) }) {
        return ValidationResult.Error("File type not supported. Use images, PDFs, or documents.")
    }
    
    val fileSize = getFileSize(uri, contentResolver)
    val maxSize = if (mimeType.startsWith("image/")) 5 * 1024 * 1024 else 10 * 1024 * 1024
    
    if (fileSize > maxSize) {
        val limit = if (mimeType.startsWith("image/")) "5MB" else "10MB"
        return ValidationResult.Error("File size exceeds $limit limit")
    }
    
    return ValidationResult.Success
}
```

### 3.3 URL Validation

**Purpose:** Ensure all file URLs point to Firebase Storage, preventing external tracking or abuse.

**Rule:** All `profileImageUrl`, `imageUrl`, and `documentUrl` fields must be:
- Empty/null, OR
- Valid Firebase Storage URLs starting with `https://firebasestorage.googleapis.com/`

**Firestore Rule:**
```javascript
function isValidStorageUrl(url) {
  return url == null || url == '' || 
    url.matches('https://firebasestorage.googleapis.com/.*');
}
```

**Client-Side Implementation:**
```kotlin
fun isValidFirebaseStorageUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return true
    return url.startsWith("https://firebasestorage.googleapis.com/")
}

// Use after uploading to Firebase Storage
fun uploadAndGetUrl(file: File, path: String): Task<String> {
    val storageRef = FirebaseStorage.getInstance().reference.child(path)
    return storageRef.putFile(Uri.fromFile(file))
        .continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            storageRef.downloadUrl
        }
        .continueWith { task ->
            task.result.toString() // This will be a valid Firebase Storage URL
        }
}
```

---

## 4. Enhanced Access Control

### 4.1 Chat Message Access

**Change:** Messages no longer require a denormalized `participants` field. Access is controlled through the parent chat document.

**Benefits:**
- Reduces data duplication
- Simplifies message creation
- Maintains security through parent chat verification

**Implementation:**
```javascript
function isChatParticipant(chatId) {
  return request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
}

match /chats/{chatId}/messages/{messageId} {
  allow read: if isAuthenticated() && isChatParticipant(chatId);
  allow create: if isAuthenticated() && isChatParticipant(chatId) && ...;
}
```

### 4.2 Group Task Access

**Feature:** Group members can now access group tasks without being explicitly assigned.

**Implementation:**
```javascript
function isGroupMember(groupId) {
  return request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
}

match /tasks/{taskId} {
  allow read: if isAuthenticated() && 
    (request.auth.uid == resource.data.userId ||
     request.auth.uid in resource.data.get('assignedTo', []) ||
     (resource.data.get('groupId', null) != null && 
      isGroupMember(resource.data.groupId)));
}
```

### 4.3 Notification Restrictions

**Change:** Clients can no longer create notifications directly. Only Cloud Functions can create notifications using the Admin SDK.

**Purpose:** Prevent notification spam and abuse.

**Implementation:**
```javascript
match /notifications/{notificationId} {
  allow create: if false;  // Block all client-side creation
  allow update: if isAuthenticated() && 
    isOwner(resource.data.userId) &&
    request.resource.data.diff(resource.data).affectedKeys().hasOnly(['read']);
}
```

**Cloud Function Example:**
```typescript
// In Cloud Functions (has admin privileges)
export const createNotification = functions.firestore
  .document('chats/{chatId}/messages/{messageId}')
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const chat = await admin.firestore()
      .collection('chats')
      .doc(context.params.chatId)
      .get();
    
    const participants = chat.data()?.participants || [];
    
    // Create notifications for all participants except sender
    const notifications = participants
      .filter(uid => uid !== message.senderId)
      .map(uid => ({
        userId: uid,
        type: 'NEW_MESSAGE',
        title: 'New message',
        message: `${message.senderName}: ${message.text}`,
        chatId: context.params.chatId,
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      }));
    
    // Admin SDK bypasses security rules
    const batch = admin.firestore().batch();
    notifications.forEach(notification => {
      const ref = admin.firestore().collection('notifications').doc();
      batch.set(ref, notification);
    });
    
    await batch.commit();
  });
```

---

## Summary of Validation Limits

| Resource | Field | Minimum | Maximum | Notes |
|----------|-------|---------|---------|-------|
| Chat (DIRECT) | participants | 2 | 2 | Exactly 2 required |
| Chat (GROUP) | participants | 2 | 100 | - |
| Group | memberIds | 1 | 100 | Owner must be included |
| Task | assignedTo | 1 | 50 | Creator must be included on creation |
| Message | text | 0 | 10,000 chars | At least one content field required |
| Profile Picture | file size | - | 5MB | Images only |
| Chat Attachment | file size | - | 10MB | Images and documents |

---

## Error Messages

When validation fails, users will see descriptive error messages:

- **"Maximum participants reached"** - Chat or group has too many members
- **"Invalid image URL"** - URL is not from Firebase Storage
- **"Message too long"** - Text exceeds 10,000 characters
- **"File type not supported"** - Uploaded file type is not allowed
- **"File size exceeds limit"** - File is too large
- **"You must be a group member"** - Attempting to access group resources without membership
- **"Task must be assigned to at least one user"** - Empty assignedTo array
- **"Group owner must be a member"** - Owner not in memberIds

---

## Best Practices

1. **Always validate on client-side first** - Provide immediate feedback before attempting Firestore writes
2. **Use Firebase Storage for all files** - Never use external URLs for images or documents
3. **Check limits before operations** - Validate array sizes before adding members or participants
4. **Handle permission errors gracefully** - Show user-friendly messages when rules block operations
5. **Test with Firebase Emulator** - Verify rule behavior before deploying to production

---

## Related Documentation

- [Migration Guide](./MIGRATION_GUIDE.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [Testing Guide](../../firestore-rules-tests/README-TESTING.md)
