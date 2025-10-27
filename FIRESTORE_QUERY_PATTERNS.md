# Firestore Query Patterns Guide

## Overview

This guide documents the correct query patterns to use with the updated Firestore security rules. Following these patterns ensures your queries will succeed and not trigger permission errors.

**Last Updated:** October 20, 2025  
**Rules Version:** 2.0

## Core Principles

### 1. Always Filter by User Access

Never query entire collections without filtering by user access. The security rules require queries to include filters that match the permission checks.

**❌ WRONG:**
```kotlin
db.collection("groups").get()  // Will fail - no user filter
```

**✅ CORRECT:**
```kotlin
db.collection("groups")
  .whereArrayContains("memberIds", currentUserId)
  .get()
```

### 2. Use Array-Contains for Membership

When checking if a user is part of a group, task, or chat, use `whereArrayContains()`.

### 3. Handle Empty Results Gracefully

Queries that don't match any documents will return empty results, not errors. Always handle empty result sets in your UI.


## Groups Collection

### Query User's Groups

**Pattern:** Use `whereArrayContains("memberIds", userId)`

```kotlin
// Get all groups where user is a member
fun getUserGroups(userId: String): Flow<List<Group>> = callbackFlow {
    val listener = db.collection("groups")
        .whereArrayContains("memberIds", userId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val groups = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Group::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(groups)
        }
    
    awaitClose { listener.remove() }
}
```

### Query Active Groups

**Pattern:** Combine membership filter with status filter

```kotlin
// Get active groups where user is a member
db.collection("groups")
    .whereArrayContains("memberIds", userId)
    .whereEqualTo("isActive", true)
    .get()
```

### Get Single Group

**Pattern:** Direct document access (user must be in memberIds)

```kotlin
// Get specific group by ID
db.collection("groups")
    .document(groupId)
    .get()
    .addOnSuccessListener { document ->
        if (document.exists()) {
            val group = document.toObject(Group::class.java)
            // Process group
        }
    }
```


## Tasks Collection

### Query User's Created Tasks

**Pattern:** Use `whereEqualTo("userId", userId)`

```kotlin
// Get tasks created by user
fun getUserTasks(userId: String): Flow<List<Task>> = callbackFlow {
    val listener = db.collection("tasks")
        .whereEqualTo("userId", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(tasks)
        }
    
    awaitClose { listener.remove() }
}
```

### Query Assigned Tasks

**Pattern:** Use `whereArrayContains("assignedTo", userId)`

```kotlin
// Get tasks assigned to user
fun getAssignedTasks(userId: String): Flow<List<Task>> = callbackFlow {
    val listener = db.collection("tasks")
        .whereArrayContains("assignedTo", userId)
        .orderBy("dueDate", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(tasks)
        }
    
    awaitClose { listener.remove() }
}
```

### Query All User's Tasks (Created OR Assigned)

**Pattern:** Use two separate queries and merge results

```kotlin
// Get all tasks user has access to (created or assigned)
suspend fun getAllUserTasks(userId: String): List<Task> {
    val createdTasks = db.collection("tasks")
        .whereEqualTo("userId", userId)
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(Task::class.java)?.copy(id = it.id) }
    
    val assignedTasks = db.collection("tasks")
        .whereArrayContains("assignedTo", userId)
        .get()
        .await()
        .documents
        .mapNotNull { it.toObject(Task::class.java)?.copy(id = it.id) }
    
    // Merge and deduplicate
    return (createdTasks + assignedTasks).distinctBy { it.id }
}
```


## Chats Collection

### Query User's Chats

**Pattern:** Use `whereArrayContains("participants", userId)`

```kotlin
// Get all chats where user is a participant
fun getUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
    val listener = db.collection("chats")
        .whereArrayContains("participants", userId)
        .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val chats = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(chats)
        }
    
    awaitClose { listener.remove() }
}
```

### Query Chat Messages

**Pattern:** Access messages through parent chat (participants denormalized)

```kotlin
// Get messages for a specific chat
fun getChatMessages(chatId: String, participants: List<String>): Flow<List<Message>> = callbackFlow {
    val listener = db.collection("chats")
        .document(chatId)
        .collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(messages)
        }
    
    awaitClose { listener.remove() }
}
```

### Create New Chat

**Pattern:** Include current user in participants array

```kotlin
// Create a new chat
suspend fun createChat(currentUserId: String, otherUserId: String): String {
    val participants = listOf(currentUserId, otherUserId).sorted()
    
    val chat = hashMapOf(
        "participants" to participants,
        "createdAt" to FieldValue.serverTimestamp(),
        "lastMessage" to "",
        "lastMessageTimestamp" to FieldValue.serverTimestamp()
    )
    
    val docRef = db.collection("chats").add(chat).await()
    return docRef.id
}
```


## Group Activities Collection

### Query Group Activities

**Pattern:** Activities include denormalized memberIds

```kotlin
// Get activities for groups user is member of
fun getUserGroupActivities(userId: String): Flow<List<GroupActivity>> = callbackFlow {
    val listener = db.collection("group_activities")
        .whereArrayContains("memberIds", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(50)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val activities = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GroupActivity::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(activities)
        }
    
    awaitClose { listener.remove() }
}
```

### Query Activities for Specific Group

**Pattern:** Filter by groupId and memberIds

```kotlin
// Get activities for a specific group
fun getGroupActivities(groupId: String, userId: String): Flow<List<GroupActivity>> = callbackFlow {
    val listener = db.collection("group_activities")
        .whereEqualTo("groupId", groupId)
        .whereArrayContains("memberIds", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val activities = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GroupActivity::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(activities)
        }
    
    awaitClose { listener.remove() }
}
```

### Create Group Activity

**Pattern:** Include memberIds from parent group

```kotlin
// Create a new group activity
suspend fun createGroupActivity(
    groupId: String,
    userId: String,
    type: String,
    description: String,
    memberIds: List<String>
) {
    val activity = hashMapOf(
        "groupId" to groupId,
        "userId" to userId,
        "type" to type,
        "description" to description,
        "memberIds" to memberIds,  // Denormalized from group
        "createdAt" to FieldValue.serverTimestamp()
    )
    
    db.collection("group_activities").add(activity).await()
}
```


## Error Handling Patterns

### Safe Query Wrapper

Use this wrapper for all Firestore queries:

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
                Log.e("Firestore", "Permission denied", e)
                onPermissionDenied()
            }
            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                Log.e("Firestore", "Service unavailable", e)
                onError(e)
            }
            else -> {
                Log.e("Firestore", "Firestore error: ${e.code}", e)
                onError(e)
            }
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Unexpected error", e)
        onError(e)
    }
}
```

### Usage Example

```kotlin
// In Repository
suspend fun getUserGroups(userId: String): List<Group> {
    return safeFirestoreCall(
        operation = {
            db.collection("groups")
                .whereArrayContains("memberIds", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Group::class.java)?.copy(id = it.id) }
        },
        onPermissionDenied = {
            Log.w("GroupRepository", "Permission denied for user $userId")
            emptyList()
        },
        onError = { exception ->
            Log.e("GroupRepository", "Error fetching groups", exception)
            emptyList()
        }
    )
}
```


## Common Mistakes to Avoid

### ❌ Mistake 1: Querying Without Filters

```kotlin
// WRONG - Will fail with permission error
db.collection("groups").get()

// CORRECT
db.collection("groups")
    .whereArrayContains("memberIds", currentUserId)
    .get()
```

### ❌ Mistake 2: Using Wrong Field for Filter

```kotlin
// WRONG - Field doesn't exist or isn't indexed
db.collection("groups")
    .whereEqualTo("members", currentUserId)
    .get()

// CORRECT - Use the actual field name
db.collection("groups")
    .whereArrayContains("memberIds", currentUserId)
    .get()
```

### ❌ Mistake 3: Not Handling Empty Results

```kotlin
// WRONG - Assumes data always exists
val groups = snapshot.documents.map { it.toObject(Group::class.java)!! }

// CORRECT - Handle null and empty cases
val groups = snapshot?.documents?.mapNotNull { 
    it.toObject(Group::class.java)?.copy(id = it.id) 
} ?: emptyList()
```

### ❌ Mistake 4: Forgetting to Denormalize Data

```kotlin
// WRONG - Creating activity without memberIds
val activity = hashMapOf(
    "groupId" to groupId,
    "userId" to userId,
    "description" to description
)

// CORRECT - Include denormalized memberIds
val activity = hashMapOf(
    "groupId" to groupId,
    "userId" to userId,
    "description" to description,
    "memberIds" to memberIds  // Required for permission check
)
```


## Performance Optimization

### 1. Use Pagination for Large Result Sets

```kotlin
// Paginate results to improve performance
fun getGroupsPaginated(userId: String, pageSize: Int = 20): Flow<List<Group>> = callbackFlow {
    var lastDocument: DocumentSnapshot? = null
    
    val listener = db.collection("groups")
        .whereArrayContains("memberIds", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(pageSize.toLong())
        .apply { lastDocument?.let { startAfter(it) } }
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val groups = snapshot?.documents?.mapNotNull { doc ->
                lastDocument = doc
                doc.toObject(Group::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            
            trySend(groups)
        }
    
    awaitClose { listener.remove() }
}
```

### 2. Use Indexes for Complex Queries

For queries with multiple filters, create composite indexes:

```json
// firestore.indexes.json
{
  "indexes": [
    {
      "collectionGroup": "tasks",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "dueDate", "order": "ASCENDING" }
      ]
    }
  ]
}
```

### 3. Cache Frequently Accessed Data

```kotlin
// Use Firestore offline persistence
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()

db.firestoreSettings = settings
```


## Testing Query Patterns

### Unit Test Example

```kotlin
@Test
fun `getUserGroups returns only groups where user is member`() = runTest {
    // Arrange
    val userId = "user123"
    val mockDb = mockk<FirebaseFirestore>()
    val repository = GroupRepository(mockDb)
    
    // Act
    val groups = repository.getUserGroups(userId)
    
    // Assert
    verify {
        mockDb.collection("groups")
            .whereArrayContains("memberIds", userId)
            .get()
    }
}
```

### Integration Test with Emulator

```kotlin
@Test
fun `query respects security rules`() = runTest {
    // Setup emulator
    val db = Firebase.firestore
    db.useEmulator("10.0.2.2", 8080)
    
    // Create test data
    val groupId = db.collection("groups").add(
        hashMapOf(
            "name" to "Test Group",
            "memberIds" to listOf("user1"),
            "owner" to "user1"
        )
    ).await().id
    
    // Test as user1 (should succeed)
    Firebase.auth.signInWithEmailAndPassword("user1@test.com", "password").await()
    val groups = db.collection("groups")
        .whereArrayContains("memberIds", "user1")
        .get()
        .await()
    
    assertEquals(1, groups.size())
    
    // Test as user2 (should return empty)
    Firebase.auth.signInWithEmailAndPassword("user2@test.com", "password").await()
    val groups2 = db.collection("groups")
        .whereArrayContains("memberIds", "user2")
        .get()
        .await()
    
    assertEquals(0, groups2.size())
}
```

## Quick Reference

| Collection | Filter Field | Query Type | Example |
|------------|--------------|------------|---------|
| groups | memberIds | array-contains | `.whereArrayContains("memberIds", userId)` |
| tasks | userId | equals | `.whereEqualTo("userId", userId)` |
| tasks | assignedTo | array-contains | `.whereArrayContains("assignedTo", userId)` |
| chats | participants | array-contains | `.whereArrayContains("participants", userId)` |
| group_activities | memberIds | array-contains | `.whereArrayContains("memberIds", userId)` |
| notifications | userId | equals | `.whereEqualTo("userId", userId)` |

## References

- [Firestore Query Documentation](https://firebase.google.com/docs/firestore/query-data/queries)
- [Security Rules Guide](https://firebase.google.com/docs/firestore/security/get-started)
- [Rules Changes Log](./FIRESTORE_RULES_CHANGES.md)
- [Troubleshooting Guide](./FIRESTORE_TROUBLESHOOTING.md)
