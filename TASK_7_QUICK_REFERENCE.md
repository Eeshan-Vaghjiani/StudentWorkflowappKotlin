# Task 7: Firestore Security Rules - Quick Reference

## Overview
Quick reference for Firestore security rules implementation and common operations.

## Rule Summary

### Users Collection
```javascript
✅ Read: Any authenticated user
✅ Write: Own document only
❌ Write: Other users' documents
```

### Groups Collection
```javascript
✅ Read: Members or public groups
✅ Create: Any authenticated user
✅ Update: Admins only
✅ Delete: Admins only
❌ Read: Non-members (private groups)
```

### Tasks Collection
```javascript
✅ Read: Creator or assigned users
✅ Create: Any authenticated user
✅ Update: Creator or assigned users
✅ Delete: Creator only
❌ Read: Non-assigned users
```

### Chats Collection
```javascript
✅ Read: Participants only
✅ Create: If user in participants array
✅ Update: Participants only
✅ Delete: Participants only
❌ Access: Non-participants
```

### Messages Subcollection
```javascript
✅ Read: Chat participants
✅ Create: Chat participants
✅ Update: Message sender only
✅ Delete: Message sender only
❌ Access: Non-participants
```

## Helper Functions

### isAuthenticated()
```javascript
function isAuthenticated() {
  return request.auth != null;
}
```
**Usage**: Check if user is logged in

### isOwner(userId)
```javascript
function isOwner(userId) {
  return request.auth.uid == userId;
}
```
**Usage**: Check if user owns a resource

### isMember(groupId)
```javascript
function isMember(groupId) {
  return isAuthenticated() && 
    request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.members;
}
```
**Usage**: Check if user is a group member

### isGroupAdmin(groupId)
```javascript
function isGroupAdmin(groupId) {
  let group = get(/databases/$(database)/documents/groups/$(groupId)).data;
  return isAuthenticated() && request.auth.uid in group.admins;
}
```
**Usage**: Check if user is a group admin

### isParticipant(chatId)
```javascript
function isParticipant(chatId) {
  return isAuthenticated() && 
    request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
}
```
**Usage**: Check if user is a chat participant

## Common Patterns

### Reading Own Data
```javascript
allow read: if isAuthenticated() && isOwner(userId);
```

### Reading Shared Data (Groups)
```javascript
allow read: if isAuthenticated() && isMember(groupId);
```

### Reading Assigned Data (Tasks)
```javascript
allow read: if isAuthenticated() && 
  (isOwner(resource.data.createdBy) || 
   request.auth.uid in resource.data.assignedTo);
```

### Creating with Self as Participant
```javascript
allow create: if isAuthenticated() && 
  request.auth.uid in request.resource.data.participants;
```

### Admin-Only Operations
```javascript
allow update, delete: if isAuthenticated() && 
  (isGroupAdmin(groupId) || isOwner(resource.data.createdBy));
```

## Key Differences

### resource.data vs request.resource.data

**resource.data**: Existing document data
```javascript
// Use for checking existing documents
allow read: if request.auth.uid in resource.data.members;
```

**request.resource.data**: New/updated document data
```javascript
// Use for checking documents being created/updated
allow create: if request.auth.uid in request.resource.data.participants;
```

## Testing Commands

### Deploy Rules
```bash
firebase deploy --only firestore:rules
```

### Get Current Rules
```bash
firebase firestore:rules:get
```

### Start Emulator
```bash
firebase emulators:start
```

### Test with Emulator
```bash
firebase emulators:exec --only firestore "npm test"
```

## Common Errors and Solutions

### Error: PERMISSION_DENIED
**Cause**: User doesn't have required permissions  
**Check**:
- Is user authenticated?
- Is user in required array (members, participants, assignedTo)?
- Does document exist?

### Error: NOT_FOUND
**Cause**: Document doesn't exist  
**Solution**: Create document first or check document ID

### Error: Invalid argument
**Cause**: Trying to use `resource.data` on create operation  
**Solution**: Use `request.resource.data` for create operations

### Chat Creation Fails
**Cause**: Not checking `request.resource.data.participants`  
**Solution**: Ensure rule uses `request.resource.data` not `resource.data`

## Android Code Examples

### Check if Operation Will Succeed

```kotlin
// Before creating a chat, ensure current user is in participants
val participants = listOf(currentUserId, otherUserId)
if (currentUserId in participants) {
    // Will succeed
    firestore.collection("chats").add(chatData)
}
```

### Handle Permission Errors

```kotlin
try {
    firestore.collection("tasks").document(taskId).get().await()
} catch (e: FirebaseFirestoreException) {
    when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
            // User doesn't have access
            showError("You don't have permission to view this task")
        }
        else -> {
            // Other error
            showError("Error loading task: ${e.message}")
        }
    }
}
```

### Query with Security in Mind

```kotlin
// This query respects security rules
// Only returns tasks where user is in assignedTo array
firestore.collection("tasks")
    .whereArrayContains("assignedTo", currentUserId)
    .get()
    .await()
```

## Security Best Practices

1. **Always Require Authentication**
   - Every rule should check `isAuthenticated()`

2. **Use Array Fields for Membership**
   - `members: ["user1", "user2"]`
   - Check with `request.auth.uid in resource.data.members`

3. **Separate Read and Write Permissions**
   - Don't use `allow read, write` unless both have same rules
   - Be specific: `allow read`, `allow create`, `allow update`, `allow delete`

4. **Validate Data on Write**
   - Check required fields exist
   - Validate data types
   - Ensure user is in participant lists

5. **Use Helper Functions**
   - Keep rules DRY (Don't Repeat Yourself)
   - Make rules more readable
   - Easier to maintain

## Deployment Checklist

- [ ] Test rules in emulator
- [ ] Test rules in Firebase Console playground
- [ ] Test from Android app
- [ ] Backup current production rules
- [ ] Deploy: `firebase deploy --only firestore:rules`
- [ ] Verify deployment in Firebase Console
- [ ] Monitor for permission errors
- [ ] Test all app features

## Monitoring

### Firebase Console
1. Go to Firestore Database → Usage
2. Check "Denied requests" metric
3. Review Cloud Logging for errors

### Android App
```kotlin
// Enable Firestore logging
FirebaseFirestore.setLoggingEnabled(true)

// Check logcat
adb logcat | grep -i "firestore\|permission"
```

## Quick Fixes

### Users can't read profiles
```javascript
// Change from:
allow read: if isOwner(userId);
// To:
allow read: if isAuthenticated();
```

### Can't create chats
```javascript
// Change from:
allow create: if request.auth.uid in resource.data.participants;
// To:
allow create: if request.auth.uid in request.resource.data.participants;
```

### Can't update assigned tasks
```javascript
// Add to update rule:
allow update: if isAuthenticated() && 
  (isOwner(resource.data.createdBy) || 
   request.auth.uid in resource.data.assignedTo);
```

### Can't join groups
```javascript
// Ensure group update allows adding to members:
allow update: if isAuthenticated() && 
  (isGroupAdmin(groupId) || 
   // Allow users to add themselves to members
   request.auth.uid in request.resource.data.members);
```

## File Location
```
firestore.rules
```

## Related Files
- `TASK_7_IMPLEMENTATION_SUMMARY.md` - Detailed implementation
- `TASK_7_TESTING_GUIDE.md` - Testing procedures
- `TASK_7_VERIFICATION_CHECKLIST.md` - Verification checklist

## Next Steps
1. Deploy rules to Firebase
2. Test all operations from Android app
3. Monitor for 24 hours
4. Proceed to Task 8: Remove All Demo Data Dependencies
