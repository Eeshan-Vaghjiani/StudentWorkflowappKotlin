# Task 43: Firestore Indexes Implementation Guide

## Overview
This guide explains the Firestore composite indexes created for optimizing query performance in the Team Collaboration app.

## Created Indexes

### 1. Messages Index (chatId + timestamp)
**Purpose**: Optimize message retrieval for chat rooms with pagination
```json
{
  "collectionGroup": "messages",
  "fields": [
    { "fieldPath": "chatId", "order": "ASCENDING" },
    { "fieldPath": "timestamp", "order": "DESCENDING" }
  ]
}
```
**Used by**: `ChatRepository.getChatMessages()` - Loads messages for a specific chat ordered by timestamp

### 2. Tasks Index (userId + dueDate)
**Purpose**: Optimize user task queries ordered by due date
```json
{
  "collectionGroup": "tasks",
  "fields": [
    { "fieldPath": "userId", "order": "ASCENDING" },
    { "fieldPath": "dueDate", "order": "ASCENDING" }
  ]
}
```
**Used by**: `TaskRepository.getUserTasks()` - Fetches all tasks for a user sorted by due date

### 3. Group Tasks Index (groupId + dueDate)
**Purpose**: Optimize group task queries ordered by due date
```json
{
  "collectionGroup": "tasks",
  "fields": [
    { "fieldPath": "groupId", "order": "ASCENDING" },
    { "fieldPath": "dueDate", "order": "ASCENDING" }
  ]
}
```
**Used by**: `TaskRepository.getGroupTasks()` - Fetches all tasks for a group sorted by due date

### 4. Tasks by Category Index (userId + category + dueDate)
**Purpose**: Optimize filtered task queries by category
```json
{
  "collectionGroup": "tasks",
  "fields": [
    { "fieldPath": "userId", "order": "ASCENDING" },
    { "fieldPath": "category", "order": "ASCENDING" },
    { "fieldPath": "dueDate", "order": "ASCENDING" }
  ]
}
```
**Used by**: `TaskRepository.getTasksByCategory()` - Fetches user tasks filtered by category

### 5. Tasks by Status Index (userId + status + dueDate)
**Purpose**: Optimize filtered task queries by status
```json
{
  "collectionGroup": "tasks",
  "fields": [
    { "fieldPath": "userId", "order": "ASCENDING" },
    { "fieldPath": "status", "order": "ASCENDING" },
    { "fieldPath": "dueDate", "order": "ASCENDING" }
  ]
}
```
**Used by**: `TaskRepository.getTasksByStatus()` - Fetches user tasks filtered by status

### 6. Chats Index (participants + lastMessageTime)
**Purpose**: Optimize chat list queries for users
```json
{
  "collectionGroup": "chats",
  "fields": [
    { "fieldPath": "participants", "arrayConfig": "CONTAINS" },
    { "fieldPath": "lastMessageTime", "order": "DESCENDING" }
  ]
}
```
**Used by**: `ChatRepository.getUserChats()` - Fetches all chats for a user sorted by last message time

## Deployment Instructions

### Method 1: Firebase Console (Manual)

1. **Open Firebase Console**
   - Go to https://console.firebase.google.com/
   - Select your project

2. **Navigate to Firestore Database**
   - Click on "Firestore Database" in the left sidebar
   - Click on the "Indexes" tab

3. **Create Each Index Manually**
   - Click "Create Index"
   - Select the collection group
   - Add fields with their order/array config
   - Click "Create"
   - Repeat for all 6 indexes

4. **Wait for Index Creation**
   - Indexes can take several minutes to build
   - Status will show "Building" then "Enabled"

### Method 2: Firebase CLI (Recommended)

1. **Install Firebase CLI** (if not already installed)
   ```bash
   npm install -g firebase-tools
   ```

2. **Login to Firebase**
   ```bash
   firebase login
   ```

3. **Initialize Firebase in Project** (if not already done)
   ```bash
   firebase init firestore
   ```
   - Select your Firebase project
   - Accept default file names or specify custom ones

4. **Deploy Indexes**
   ```bash
   firebase deploy --only firestore:indexes
   ```

5. **Verify Deployment**
   - Check the Firebase Console to see indexes building
   - Wait for all indexes to show "Enabled" status

### Method 3: Automatic Index Creation

When you run a query that requires an index, Firestore will provide an error message with a direct link to create the index. You can:

1. Run the app and trigger queries that need indexes
2. Check Logcat for error messages containing index creation links
3. Click the link to automatically create the index in Firebase Console

## Testing the Indexes

### 1. Test Chat Messages Query
```kotlin
// In ChatRoomActivity or test
chatRepository.getChatMessages(chatId, limit = 50)
    .collect { messages ->
        // Should load quickly without index warnings
        Log.d("IndexTest", "Loaded ${messages.size} messages")
    }
```

### 2. Test User Tasks Query
```kotlin
// In TasksFragment or test
taskRepository.getUserTasks()
    .also { tasks ->
        // Should load quickly without index warnings
        Log.d("IndexTest", "Loaded ${tasks.size} tasks")
    }
```

### 3. Test Chat List Query
```kotlin
// In ChatFragment or test
chatRepository.getUserChats()
    .collect { chats ->
        // Should load quickly without index warnings
        Log.d("IndexTest", "Loaded ${chats.size} chats")
    }
```

### 4. Monitor Logcat
Look for these indicators:
- ✅ **Success**: No index-related warnings or errors
- ✅ **Success**: Queries complete in < 1 second
- ❌ **Needs Index**: Error messages mentioning "index" or "composite index required"
- ❌ **Needs Index**: Slow query performance (> 2 seconds)

## Performance Expectations

### Before Indexes
- Chat list loading: 2-5 seconds
- Message loading: 1-3 seconds
- Task queries: 1-3 seconds
- Possible timeout errors on large datasets

### After Indexes
- Chat list loading: < 500ms
- Message loading: < 300ms
- Task queries: < 500ms
- No timeout errors
- Smooth scrolling and pagination

## Troubleshooting

### Index Not Working
1. **Check Index Status**: Ensure index shows "Enabled" in Firebase Console
2. **Wait for Build**: Large datasets may take 10-30 minutes to index
3. **Verify Query**: Ensure query fields match index fields exactly
4. **Check Order**: Field order in query must match index definition

### Index Creation Failed
1. **Check Permissions**: Ensure you have Owner/Editor role in Firebase project
2. **Check Quota**: Free tier has limits on number of indexes
3. **Check Syntax**: Verify firestore.indexes.json is valid JSON
4. **Try Manual**: Create index manually in Firebase Console

### Query Still Slow
1. **Check Data Size**: Large documents slow down queries
2. **Check Network**: Slow internet affects query performance
3. **Enable Persistence**: Firestore offline persistence helps with repeated queries
4. **Check Pagination**: Ensure pagination is implemented correctly

## Maintenance

### When to Add New Indexes
- When adding new query combinations (e.g., filtering by multiple fields)
- When Firestore error messages suggest an index
- When query performance degrades with data growth

### When to Remove Indexes
- When queries are no longer used in the app
- When simplifying query patterns
- To stay within index quota limits

### Monitoring Index Usage
1. Go to Firebase Console > Firestore > Indexes
2. Check "Usage" column to see query frequency
3. Remove unused indexes to optimize quota

## Related Files
- `firestore.indexes.json` - Index definitions
- `app/src/main/java/com/example/loginandregistration/repository/ChatRepository.kt` - Chat queries
- `app/src/main/java/com/example/loginandregistration/repository/TaskRepository.kt` - Task queries

## Requirements Covered
- ✅ Requirement 10.5: Composite indexes for complex queries
- ✅ Messages index (chatId + timestamp)
- ✅ Tasks indexes (userId/groupId + dueDate)
- ✅ Chats index (participants + lastMessageTime)
- ✅ Additional indexes for category and status filtering

## Next Steps
1. Deploy indexes using Firebase CLI or Console
2. Wait for indexes to build (check status)
3. Test app performance with indexes enabled
4. Monitor Logcat for any remaining index warnings
5. Proceed to Task 44: Implement lifecycle-aware listeners
