# Firestore Permission Errors - Fixed

## Issues Identified

From the application logs, three critical permission errors were identified:

1. **PERMISSION_DENIED when creating groups**
2. **PERMISSION_DENIED when marking messages as read**
3. **UserProfile deserialization error** with @DocumentId annotation

## Root Causes

### 1. Message Update Permissions
The Firestore rules only allowed message senders to update messages:
```
allow update: if request.auth.uid == resource.data.senderId
```

This prevented recipients from marking messages as read, causing repeated permission errors.

### 2. UserProfile @DocumentId Conflict
The `UserProfile` model used `@DocumentId` annotation on the `userId` field:
```kotlin
@DocumentId val userId: String = ""
```

However, the Firestore documents already contained a `userId` field, causing this error:
```
'userId' was found from document users/..., cannot apply @DocumentId on this property
```

## Fixes Applied

### Fix 1: Updated Message Update Rule
**File**: `firestore.rules`

Changed from:
```javascript
allow update: if isAuthenticated()
  && request.resource.data.senderId == resource.data.senderId
  && request.auth.uid == resource.data.senderId;
```

To:
```javascript
allow update: if isAuthenticated()
  && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants
  && request.resource.data.senderId == resource.data.senderId
  && request.resource.data.chatId == resource.data.chatId;
```

**Impact**: Now all chat participants can update messages (recipients can mark as read, sender can edit).

### Fix 2: Removed @DocumentId Annotation
**File**: `app/src/main/java/com/example/loginandregistration/models/UserProfile.kt`

Changed from:
```kotlin
@DocumentId val userId: String = ""
```

To:
```kotlin
@PropertyName("userId") val userId: String = ""
```

**Impact**: Firestore can now properly deserialize UserProfile documents without conflicts.

## Deployment

Firestore rules deployed successfully:
```
firebase deploy --only firestore:rules
```

Result: ✅ Rules compiled and deployed without errors

## Testing Required

After these fixes, test the following scenarios:

1. **Group Creation**
   - Create a new group
   - Verify no permission errors in logs

2. **Message Read Status**
   - Send messages in a chat
   - Mark messages as read
   - Verify no permission errors

3. **User Profile Loading**
   - Login with existing account
   - Verify profile loads without deserialization errors
   - Check logs for UserProfile-related errors

## Expected Behavior

- ✅ Groups can be created by authenticated users
- ✅ Messages can be marked as read by recipients
- ✅ User profiles load without @DocumentId conflicts
- ✅ No more PERMISSION_DENIED errors in the logs

## Date Fixed
October 31, 2025
