# Design Document - Firebase Rules Audit & Fixes

## Overview

This design document provides detailed solutions for fixing and enhancing the Firebase Security Rules and Storage Rules for the TeamSync Collaboration app. The current rules are functional but have gaps that could lead to security issues, performance problems, or feature limitations.

### Design Principles

1. **Security First**: All rules must prevent unauthorized access while enabling legitimate use cases
2. **Performance**: Minimize `get()` calls and use denormalized data where appropriate
3. **Validation**: Enforce data integrity at the database level
4. **Scalability**: Support growing user base and data volume
5. **Maintainability**: Keep rules clear, well-documented, and consistent

## Architecture

### Current Rule Structure

```
firestore.rules
├── Helper Functions (isAuthenticated, isOwner)
├── Users Collection
├── Groups Collection
├── Tasks Collection
├── Chats Collection
│   ├── Messages Subcollection
│   └── Typing Status Subcollection
├── Notifications Collection
└── Group Activities Collection

storage.rules
├── Profile Pictures
├── Chat Attachments
└── Global Deny
```

### Proposed Enhancements

The design maintains the current structure but adds:
- Additional helper functions for validation
- Enhanced access control logic
- Content validation rules
- Size and type restrictions
- Better support for public/private resources


## Components and Interfaces

### 1. Enhanced Helper Functions

#### New Validation Helpers

```javascript
// Check if a string is a valid Firebase Storage URL
function isValidStorageUrl(url) {
  return url == null || url == '' || 
    url.matches('https://firebasestorage.googleapis.com/.*');
}

// Check if user is a member of a specific group
function isGroupMember(groupId) {
  return request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
}

// Check if user is a participant in a specific chat
function isChatParticipant(chatId) {
  return request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
}

// Validate array size
function isValidArraySize(arr, min, max) {
  return arr.size() >= min && arr.size() <= max;
}

// Validate string length
function isValidStringLength(str, max) {
  return str == null || str.size() <= max;
}

// Check if timestamp is recent (within 5 minutes)
function isRecentTimestamp(timestamp) {
  return timestamp > request.time.toMillis() - 300000 && 
         timestamp < request.time.toMillis() + 300000;
}
```

### 2. Users Collection - Enhanced

**Current Implementation**: ✅ Good
**Proposed Changes**: Add profile picture URL validation

```javascript
match /users/{userId} {
  allow read: if isAuthenticated();
  
  allow create: if isAuthenticated() && 
    isOwner(userId) &&
    isValidStorageUrl(request.resource.data.get('profileImageUrl', ''));
  
  allow update: if isAuthenticated() && 
    isOwner(userId) &&
    isValidStorageUrl(request.resource.data.get('profileImageUrl', ''));
}
```

**Rationale**: Prevents users from setting external URLs as profile pictures, which could be used for tracking or abuse.


### 3. Groups Collection - Enhanced

**Current Implementation**: ✅ Mostly good
**Proposed Changes**: Add public group support and member limits

```javascript
match /groups/{groupId} {
  // Allow reading public groups OR groups where user is a member
  allow read: if isAuthenticated() && 
    (resource.data.get('isPublic', false) == true ||
     request.auth.uid in resource.data.memberIds);
  
  // Any authenticated user can create a group
  allow create: if isAuthenticated() && 
    request.auth.uid in request.resource.data.memberIds &&
    request.auth.uid == request.resource.data.owner &&
    isValidArraySize(request.resource.data.memberIds, 1, 100);
  
  // Owner or members can update, with validation
  allow update: if isAuthenticated() && 
    (request.auth.uid == resource.data.owner ||
     request.auth.uid in resource.data.memberIds) &&
    // Ensure owner stays in memberIds
    request.resource.data.owner in request.resource.data.memberIds &&
    isValidArraySize(request.resource.data.memberIds, 1, 100);
  
  // Only owner can delete
  allow delete: if isAuthenticated() && 
    request.auth.uid == resource.data.owner;
}
```

**Key Changes**:
- Supports `isPublic` field for group discovery
- Enforces 1-100 member limit
- Ensures owner is always in memberIds
- Allows queries like `.where('isPublic', '==', true)`


### 4. Tasks Collection - Enhanced

**Current Implementation**: ✅ Good
**Proposed Changes**: Add group membership validation and assignment limits

```javascript
match /tasks/{taskId} {
  // Read if user created it, is assigned to it, or is a member of the task's group
  allow read: if isAuthenticated() && 
    (request.auth.uid == resource.data.userId ||
     request.auth.uid in resource.data.get('assignedTo', []) ||
     (resource.data.get('groupId', null) != null && 
      isGroupMember(resource.data.groupId)));
  
  // Create with validation
  allow create: if isAuthenticated() && 
    request.auth.uid == request.resource.data.userId &&
    // If groupId is set, user must be a member
    (request.resource.data.get('groupId', null) == null ||
     isGroupMember(request.resource.data.groupId)) &&
    // Validate assignedTo array
    isValidArraySize(request.resource.data.get('assignedTo', [request.auth.uid]), 1, 50);
  
  // Update if creator or assigned user
  allow update: if isAuthenticated() && 
    (request.auth.uid == resource.data.userId ||
     request.auth.uid in resource.data.get('assignedTo', [])) &&
    // If changing groupId, verify membership in new group
    (request.resource.data.get('groupId', null) == null ||
     isGroupMember(request.resource.data.groupId)) &&
    isValidArraySize(request.resource.data.get('assignedTo', []), 1, 50);
  
  // Only creator can delete
  allow delete: if isAuthenticated() && 
    request.auth.uid == resource.data.userId;
}
```

**Key Changes**:
- Validates group membership for group tasks
- Enforces 1-50 user limit on assignedTo
- Supports reading group tasks by group members
- Prevents orphaned tasks (assignedTo must have at least 1 user)


### 5. Chats Collection - Enhanced

**Current Implementation**: ✅ Good
**Proposed Changes**: Add participant limits and chat type validation

```javascript
match /chats/{chatId} {
  // Only participants can read
  allow read: if isAuthenticated() && 
    request.auth.uid in resource.data.participants;
  
  // Create with validation
  allow create: if isAuthenticated() && 
    request.auth.uid in request.resource.data.participants &&
    // Validate participant count based on chat type
    ((request.resource.data.type == 'DIRECT' && 
      request.resource.data.participants.size() == 2) ||
     (request.resource.data.type == 'GROUP' && 
      isValidArraySize(request.resource.data.participants, 2, 100)));
  
  // Only participants can update
  allow update: if isAuthenticated() && 
    request.auth.uid in resource.data.participants &&
    // Prevent changing participants array (use separate operations)
    request.resource.data.participants == resource.data.participants;
  
  // Only participants can delete
  allow delete: if isAuthenticated() && 
    request.auth.uid in resource.data.participants;
}
```

**Key Changes**:
- Enforces exactly 2 participants for DIRECT chats
- Enforces 2-100 participants for GROUP chats
- Prevents modifying participants array via update (should use dedicated operations)
- Maintains existing participant-based access control


### 6. Messages Subcollection - Fixed

**Current Implementation**: ⚠️ Requires participants in each message
**Proposed Changes**: Check parent chat for participants

```javascript
match /chats/{chatId}/messages/{messageId} {
  // Read if user is a participant in the parent chat
  allow read: if isAuthenticated() && 
    isChatParticipant(chatId);
  
  // Create with validation
  allow create: if isAuthenticated() && 
    isChatParticipant(chatId) &&
    request.auth.uid == request.resource.data.senderId &&
    // Validate message content
    (request.resource.data.get('text', '').size() > 0 ||
     request.resource.data.get('imageUrl', null) != null ||
     request.resource.data.get('documentUrl', null) != null) &&
    // Validate text length
    isValidStringLength(request.resource.data.get('text', ''), 10000) &&
    // Validate URLs are from Firebase Storage
    isValidStorageUrl(request.resource.data.get('imageUrl', '')) &&
    isValidStorageUrl(request.resource.data.get('documentUrl', '')) &&
    // Validate timestamp is recent
    isRecentTimestamp(request.resource.data.timestamp);
  
  // Update own messages only
  allow update: if isAuthenticated() && 
    request.auth.uid == resource.data.senderId &&
    isChatParticipant(chatId);
  
  // Delete own messages only
  allow delete: if isAuthenticated() && 
    request.auth.uid == resource.data.senderId;
}
```

**Key Changes**:
- Uses `isChatParticipant(chatId)` helper to check parent chat
- Removes requirement for participants field in each message
- Validates message has content (text, image, or document)
- Enforces 10,000 character limit on text
- Validates URLs are from Firebase Storage
- Prevents backdating messages (timestamp within 5 minutes)


### 7. Typing Status Subcollection - Fixed

**Current Implementation**: ⚠️ Requires participants in each status document
**Proposed Changes**: Check parent chat for participants

```javascript
match /chats/{chatId}/typing_status/{userId} {
  // Read if user is a participant in the parent chat
  allow read: if isAuthenticated() && 
    isChatParticipant(chatId);
  
  // Write own typing status only, if participant
  allow write: if isAuthenticated() && 
    request.auth.uid == userId &&
    isChatParticipant(chatId);
}
```

**Key Changes**:
- Uses `isChatParticipant(chatId)` helper to check parent chat
- Removes requirement for participants field in typing_status documents
- Simplifies the rule significantly
- Maintains security by checking parent chat membership

### 8. Notifications Collection - Enhanced

**Current Implementation**: ✅ Good
**Proposed Changes**: Restrict creation to server-side only

```javascript
match /notifications/{notificationId} {
  // Users can only read their own notifications
  allow read: if isAuthenticated() && 
    isOwner(resource.data.userId);
  
  // Users can only mark their own notifications as read
  allow update: if isAuthenticated() && 
    isOwner(resource.data.userId) &&
    // Only allow updating the 'read' field
    request.resource.data.diff(resource.data).affectedKeys().hasOnly(['read']);
  
  // Prevent client-side creation (should be done via Cloud Functions)
  allow create: if false;
  
  // Users can delete their own notifications
  allow delete: if isAuthenticated() && 
    isOwner(resource.data.userId);
}
```

**Key Changes**:
- Blocks client-side notification creation
- Only allows updating the `read` field
- Prevents notification spam
- Cloud Functions can still create notifications using admin SDK


### 9. Group Activities Collection - Enhanced

**Current Implementation**: ✅ Uses denormalized memberIds
**Proposed Changes**: Add fallback to group membership check

```javascript
match /group_activities/{activityId} {
  // Read if user is in denormalized memberIds OR is a member of the group
  allow read: if isAuthenticated() && 
    (request.auth.uid in resource.data.get('memberIds', []) ||
     isGroupMember(resource.data.groupId));
  
  // Create if user is a group member
  allow create: if isAuthenticated() && 
    isGroupMember(request.resource.data.groupId) &&
    request.auth.uid in request.resource.data.get('memberIds', []);
  
  // Only activity creator can update
  allow update: if isAuthenticated() && 
    request.auth.uid == resource.data.userId;
  
  // Only activity creator can delete
  allow delete: if isAuthenticated() && 
    request.auth.uid == resource.data.userId;
}
```

**Key Changes**:
- Adds fallback to `isGroupMember()` if memberIds is not denormalized
- Validates group membership on creation
- Maintains existing access control patterns

## Data Models

### Updated Firestore Document Structures

**Chat Document**:
```typescript
{
  id: string
  type: 'DIRECT' | 'GROUP'
  participants: string[]  // 2 for DIRECT, 2-100 for GROUP
  participantDetails: Map<string, UserInfo>
  lastMessage: string
  lastMessageTime: number
  lastMessageSenderId: string
  unreadCount: Map<string, number>
  groupId?: string
  groupName?: string
  createdAt: number
}
```

**Message Document** (participants field no longer required):
```typescript
{
  id: string
  chatId: string
  senderId: string
  senderName: string
  senderImageUrl: string
  text?: string  // Max 10,000 chars
  imageUrl?: string  // Firebase Storage URL only
  documentUrl?: string  // Firebase Storage URL only
  documentName?: string
  documentSize?: number
  timestamp: number  // Must be within 5 minutes of server time
  readBy: string[]
  status: MessageStatus
}
```


**Group Document**:
```typescript
{
  id: string
  name: string
  description: string
  subject: string
  joinCode: string
  memberIds: string[]  // 1-100 members
  owner: string  // Must be in memberIds
  isPublic: boolean  // New field for public group discovery
  createdAt: number
}
```

**Task Document**:
```typescript
{
  id: string
  title: string
  description: string
  category: string
  priority: string
  status: string
  dueDate: number
  groupId?: string  // If set, creator must be group member
  assignedTo: string[]  // 1-50 users
  userId: string  // Creator
  createdAt: number
}
```

## Storage Rules Design

### Enhanced Storage Rules

```javascript
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    function isValidImageSize() {
      return request.resource.size < 5 * 1024 * 1024; // 5MB
    }
    
    function isValidDocumentSize() {
      return request.resource.size < 10 * 1024 * 1024; // 10MB
    }
    
    function isValidImageType() {
      return request.resource.contentType.matches('image/.*');
    }
    
    function isValidDocumentType() {
      return request.resource.contentType.matches('image/.*') ||
             request.resource.contentType.matches('application/pdf') ||
             request.resource.contentType.matches('application/msword') ||
             request.resource.contentType.matches('application/vnd.openxmlformats-officedocument.*') ||
             request.resource.contentType.matches('text/.*');
    }
    
    // Profile pictures - images only, 5MB limit
    match /profile_pictures/{userId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && 
                      isOwner(userId) && 
                      isValidImageSize() &&
                      isValidImageType();
    }
    
    // Chat attachments - images and documents, size limits
    match /chat_attachments/{chatId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && 
                      isValidDocumentSize() &&
                      isValidDocumentType();
    }
    
    // Deny all other paths
    match /{allPaths=**} {
      allow read: if false;
      allow write: if false;
    }
  }
}
```

**Key Changes**:
- Validates file types (images for profiles, images/docs for chat)
- Enforces size limits (5MB for images, 10MB for documents)
- Prevents uploading executable or dangerous file types
- Maintains existing access control patterns


## Error Handling

### Rule Violation Scenarios

1. **Participant Limit Exceeded**
   - Error: `PERMISSION_DENIED`
   - Client handling: Show "Maximum participants reached" message
   - Prevention: Validate on client before attempting write

2. **Invalid Storage URL**
   - Error: `PERMISSION_DENIED`
   - Client handling: Show "Invalid image URL" message
   - Prevention: Only allow uploads through Firebase Storage SDK

3. **Non-Group Member Creating Group Task**
   - Error: `PERMISSION_DENIED`
   - Client handling: Show "You must be a group member to create group tasks"
   - Prevention: Validate group membership on client

4. **Message Too Long**
   - Error: `PERMISSION_DENIED`
   - Client handling: Show "Message exceeds 10,000 characters"
   - Prevention: Validate length on client before sending

5. **Client-Side Notification Creation**
   - Error: `PERMISSION_DENIED`
   - Client handling: Not applicable (should never happen)
   - Prevention: Only create notifications via Cloud Functions

### Performance Considerations

**`get()` Call Usage**:
- `isGroupMember()`: Used for group tasks and activities (acceptable, infrequent)
- `isChatParticipant()`: Used for messages and typing status (acceptable, cached by Firebase)

**Optimization Strategy**:
- Denormalize memberIds in group_activities (already done)
- Consider denormalizing participants in messages if performance issues arise
- Use Firestore's built-in caching to minimize `get()` calls

**Query Support**:
- All rules support common query patterns
- Public groups: `.where('isPublic', '==', true)`
- User groups: `.where('memberIds', 'array-contains', userId)`
- User tasks: `.where('userId', '==', userId)` or `.where('assignedTo', 'array-contains', userId)`
- User chats: `.where('participants', 'array-contains', userId)`


## Testing Strategy

### Unit Tests (Firebase Emulator)

**Test Categories**:
1. **Authentication Tests**
   - Unauthenticated users cannot access any data
   - Authenticated users can access their own data

2. **Groups Tests**
   - Members can read group data
   - Non-members cannot read private groups
   - Anyone can read public groups
   - Owner can delete groups
   - Member limits are enforced

3. **Tasks Tests**
   - Users can read their own tasks
   - Users can read tasks they're assigned to
   - Group members can read group tasks
   - Non-members cannot read group tasks
   - Assignment limits are enforced

4. **Chats Tests**
   - Participants can read/write chats
   - Non-participants cannot access chats
   - Direct chats enforce 2 participants
   - Group chats enforce 2-100 participants

5. **Messages Tests**
   - Participants can read/write messages
   - Non-participants cannot access messages
   - Empty messages are rejected
   - Messages over 10,000 chars are rejected
   - External URLs are rejected

6. **Storage Tests**
   - Users can upload to their profile folder
   - Users cannot upload non-images to profile folder
   - Files over size limits are rejected
   - Invalid content types are rejected

### Integration Tests

**Test Scenarios**:
1. Create group → Create group task → Verify member access
2. Create chat → Send message → Verify participant access
3. Upload profile picture → Verify URL validation
4. Create public group → Verify non-member can discover
5. Add user to group → Verify they can access group tasks

### Manual Testing Checklist

- [ ] Create and join groups
- [ ] Create personal and group tasks
- [ ] Send messages in direct and group chats
- [ ] Upload profile pictures
- [ ] Upload chat attachments
- [ ] Search for public groups
- [ ] Verify permission errors are user-friendly
- [ ] Test with multiple users simultaneously


## Implementation Phases

### Phase 1: Critical Fixes (High Priority)
- Fix message subcollection to use parent chat participants
- Fix typing status to use parent chat participants
- Add message content validation
- Update storage rules with file type validation

### Phase 2: Security Enhancements (High Priority)
- Add profile picture URL validation
- Restrict notification creation to server-side
- Add participant/member limit validation
- Add timestamp validation for messages

### Phase 3: Feature Support (Medium Priority)
- Add public group discovery support
- Add group membership validation for tasks
- Enhance group activities access control

### Phase 4: Validation & Polish (Medium Priority)
- Add task assignment validation
- Add string length validation
- Add array size validation helpers
- Comprehensive testing

## Migration Strategy

### Backward Compatibility

**Existing Data**:
- Messages without participants field: ✅ Will work (uses parent chat)
- Typing status without participants: ✅ Will work (uses parent chat)
- Groups without isPublic field: ✅ Will work (defaults to false)
- Tasks with large assignedTo arrays: ⚠️ May need cleanup

**Migration Steps**:
1. Deploy new rules to Firebase
2. Monitor error logs for any unexpected permission denials
3. Update client code to handle new validation errors
4. Clean up any data that violates new constraints
5. Update documentation with new field requirements

### Rollback Plan

If issues arise:
1. Revert to previous rules version in Firebase Console
2. Identify problematic rule changes
3. Fix and test in emulator
4. Redeploy with fixes

