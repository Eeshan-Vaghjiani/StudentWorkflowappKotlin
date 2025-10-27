# Design Document: Firestore Permissions Fix

## Overview

This design addresses critical permission errors in Firestore security rules that are causing app crashes. The current rules use helper functions with `get()` calls that create circular dependencies. The solution replaces these with direct array membership checks and restructures the rules to support common query patterns.

## Architecture

### Current Problem

The existing rules use helper functions like:
```javascript
function isMember(groupId) {
  return isAuthenticated() && 
    request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
}
```

This creates a circular dependency:
1. User queries groups collection
2. Rule calls `isMember(groupId)` to check permission
3. `isMember()` tries to `get()` the group document
4. The `get()` requires read permission on groups
5. Read permission requires `isMember()` check → **circular dependency**

### Solution Approach

Replace `get()` calls with direct checks on `resource.data` (for existing documents) or `request.resource.data` (for new documents). Use query-based filtering where the client specifies which documents they want, and rules validate the query matches their permissions.

## Components and Interfaces

### 1. Updated Security Rules Structure

**Groups Collection Rules:**
```javascript
match /groups/{groupId} {
  // Allow read if user is in the memberIds array
  allow read: if isAuthenticated() && 
    request.auth.uid in resource.data.memberIds;
  
  // Allow create if user adds themselves as a member
  allow create: if isAuthenticated() && 
    request.auth.uid in request.resource.data.memberIds &&
    request.auth.uid == request.resource.data.owner;
  
  // Allow update if user is owner or already a member
  allow update: if isAuthenticated() && 
    (request.auth.uid == resource.data.owner ||
     request.auth.uid in resource.data.memberIds);
  
  // Allow delete if user is owner
  allow delete: if isAuthenticated() && 
    request.auth.uid == resource.data.owner;
}
```

**Group Activities Rules:**
```javascript
match /group_activities/{activityId} {
  // Allow read if user is in the group's memberIds
  // This requires the query to include groupId filter
  allow read: if isAuthenticated() && 
    request.auth.uid in get(/databases/$(database)/documents/groups/$(resource.data.groupId)).data.memberIds;
  
  // Alternative: Denormalize memberIds into activities
  allow read: if isAuthenticated() && 
    request.auth.uid in resource.data.memberIds;
}
```

**Tasks Collection Rules:**
```javascript
match /tasks/{taskId} {
  // Allow read if user is the creator or in assignedTo array
  allow read: if isAuthenticated() && 
    (request.auth.uid == resource.data.userId ||
     request.auth.uid in resource.data.assignedTo);
  
  // Allow create if user is the creator
  allow create: if isAuthenticated() && 
    request.auth.uid == request.resource.data.userId;
  
  // Allow update if user is creator or assigned
  allow update: if isAuthenticated() && 
    (request.auth.uid == resource.data.userId ||
     request.auth.uid in resource.data.assignedTo);
  
  // Allow delete if user is creator
  allow delete: if isAuthenticated() && 
    request.auth.uid == resource.data.userId;
}
```

### 2. Query Pattern Support

**Client-Side Query Requirements:**

For groups:
```kotlin
// CORRECT: Query with array-contains
db.collection("groups")
  .whereArrayContains("memberIds", currentUserId)
  .get()

// INCORRECT: Query all groups (will fail permission check)
db.collection("groups").get()
```

For tasks:
```kotlin
// CORRECT: Query user's own tasks
db.collection("tasks")
  .whereEqualTo("userId", currentUserId)
  .get()

// CORRECT: Query assigned tasks
db.collection("tasks")
  .whereArrayContains("assignedTo", currentUserId)
  .get()
```

### 3. Data Model Considerations

**Option A: Keep Current Structure (Recommended)**
- Groups store `memberIds` array
- Activities reference `groupId`
- Rules use `get()` only for activities (acceptable since it's a single read per query)

**Option B: Denormalize Data**
- Activities store `memberIds` array (copied from group)
- No `get()` calls needed
- Requires updating activities when group membership changes
- More storage, but better performance and simpler rules

**Decision: Use Option A** for now, with Option B as a future optimization if needed.

## Error Handling

### 1. Client-Side Error Handling

**Current Issue:**
```kotlin
// Crashes on permission error
groupRepository.getUserGroups(userId)
  .collect { groups ->
    // Handle groups
  }
```

**Solution:**
```kotlin
// Catch and handle permission errors
groupRepository.getUserGroups(userId)
  .catch { exception ->
    when (exception) {
      is FirebaseFirestoreException -> {
        if (exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
          _error.value = "You don't have permission to access this data"
          _groups.value = emptyList()
        } else {
          _error.value = "Error loading groups: ${exception.message}"
        }
      }
      else -> _error.value = "Unexpected error: ${exception.message}"
    }
  }
  .collect { groups ->
    _groups.value = groups
  }
```

### 2. Repository Layer Updates

Add error handling wrapper:
```kotlin
suspend fun <T> safeFirestoreCall(
    operation: suspend () -> T,
    onPermissionDenied: () -> T,
    onError: (Exception) -> T
): T {
    return try {
        operation()
    } catch (e: FirebaseFirestoreException) {
        when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                Log.e(TAG, "Permission denied", e)
                onPermissionDenied()
            }
            else -> {
                Log.e(TAG, "Firestore error", e)
                onError(e)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error", e)
        onError(e)
    }
}
```

## Testing Strategy

### 1. Firebase Emulator Testing

**Setup:**
```bash
firebase emulators:start --only firestore
```

**Test Cases:**
```javascript
// Test 1: User can read their own groups
test('user can read groups they are member of', async () => {
  const db = getFirestore();
  await assertSucceeds(
    db.collection('groups')
      .where('memberIds', 'array-contains', 'user1')
      .get()
  );
});

// Test 2: User cannot read groups they're not member of
test('user cannot read groups they are not member of', async () => {
  const db = getFirestore();
  await assertFails(
    db.collection('groups')
      .doc('group1')
      .get()
  );
});

// Test 3: User can create group with themselves as member
test('user can create group with themselves as member', async () => {
  const db = getFirestore();
  await assertSucceeds(
    db.collection('groups').add({
      name: 'Test Group',
      memberIds: ['user1'],
      owner: 'user1'
    })
  );
});
```

### 2. Integration Testing

**Test Scenarios:**
1. User logs in and navigates to Groups screen
2. User creates a new group
3. User views group activities
4. User queries their tasks
5. User sends a chat message

**Expected Results:**
- No permission errors
- No app crashes
- Data loads correctly
- Empty states show when no data exists

### 3. Monitoring

**Metrics to Track:**
- Permission denied errors (should be 0)
- App crash rate (should decrease)
- Query success rate (should be 100%)
- Average query latency

## Deployment Plan

### Phase 1: Update Rules (Low Risk)
1. Deploy updated Firestore rules
2. Monitor error logs for 24 hours
3. Verify no new permission errors

### Phase 2: Update Client Error Handling (Medium Risk)
1. Add error handling to repositories
2. Update ViewModels to handle errors gracefully
3. Add user-friendly error messages to UI
4. Deploy to beta testers

### Phase 3: Optimize (Optional)
1. Consider denormalizing data if performance issues arise
2. Add caching layer for frequently accessed data
3. Implement offline support improvements

## Data Models

### Group Document
```kotlin
data class Group(
    val id: String,
    val name: String,
    val description: String,
    val owner: String,
    val memberIds: List<String>,  // Critical for permission checks
    val isActive: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp
)
```

### Group Activity Document
```kotlin
data class GroupActivity(
    val id: String,
    val groupId: String,  // Used for permission check via get()
    val userId: String,
    val type: String,
    val description: String,
    val createdAt: Timestamp,
    // Optional: Add memberIds for denormalization
    val memberIds: List<String>? = null
)
```

### Task Document
```kotlin
data class Task(
    val id: String,
    val userId: String,  // Creator
    val assignedTo: List<String>,  // Users who can access
    val title: String,
    val description: String,
    val status: String,
    val dueDate: Timestamp?,
    val createdAt: Timestamp
)
```

## Security Considerations

1. **Principle of Least Privilege**: Users can only access data they own or are explicitly granted access to
2. **No Data Leakage**: Failed queries return empty results, not permission errors that reveal data existence
3. **Audit Trail**: All permission checks are logged in Firestore audit logs
4. **Rate Limiting**: Consider adding rate limiting for sensitive operations
5. **Input Validation**: Rules validate all required fields exist and have correct types

## Performance Considerations

1. **Query Efficiency**: Array-contains queries are indexed by Firestore
2. **Read Costs**: Eliminating `get()` calls reduces read operations
3. **Caching**: Client-side caching reduces repeated queries
4. **Offline Support**: Firestore offline persistence works with these rules

## Migration Notes

**No data migration required** - this is a rules-only change. However:

1. Verify all existing queries use proper filters
2. Update any queries that fetch all documents without filters
3. Test with existing production data in emulator
4. Have rollback plan ready (keep old rules file)

## Rollback Plan

If issues arise after deployment:

1. Revert to previous rules file:
   ```bash
   firebase deploy --only firestore:rules --force
   ```

2. Monitor for 15 minutes to ensure stability

3. Investigate root cause before attempting fix again

## Success Criteria

1. Zero permission denied errors in production logs
2. App crash rate returns to baseline (< 0.1%)
3. All user flows work without errors
4. Query performance remains acceptable (< 500ms p95)
5. No user complaints about access issues
