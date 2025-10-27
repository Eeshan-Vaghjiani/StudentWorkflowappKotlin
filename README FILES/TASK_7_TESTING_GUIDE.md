# Task 7: Firestore Security Rules - Testing Guide

## Overview
This guide provides comprehensive testing procedures for the Firestore security rules to ensure all operations work correctly while maintaining security.

## Testing Methods

### Method 1: Firebase Console Rules Playground

1. **Access Rules Playground**
   - Go to Firebase Console
   - Navigate to Firestore Database → Rules
   - Click "Rules Playground" tab

2. **Test User Profile Read**
   ```
   Location: /users/user123
   Operation: get
   Authenticated: Yes (any user ID)
   Expected: Allow
   ```

3. **Test User Profile Write (Own)**
   ```
   Location: /users/user123
   Operation: create/update
   Authenticated: Yes (user123)
   Expected: Allow
   ```

4. **Test User Profile Write (Other)**
   ```
   Location: /users/user123
   Operation: update
   Authenticated: Yes (user456)
   Expected: Deny
   ```

5. **Test Group Creation**
   ```
   Location: /groups/group123
   Operation: create
   Authenticated: Yes (any user)
   Expected: Allow
   ```

6. **Test Group Read (Member)**
   ```
   Location: /groups/group123
   Operation: get
   Authenticated: Yes (user in members array)
   Expected: Allow
   ```

7. **Test Task Read (Assigned)**
   ```
   Location: /tasks/task123
   Operation: get
   Authenticated: Yes (user in assignedTo array)
   Expected: Allow
   ```

8. **Test Chat Creation**
   ```
   Location: /chats/chat123
   Operation: create
   Authenticated: Yes (user in participants array)
   Data: { participants: ["user123", "user456"] }
   Expected: Allow
   ```

9. **Test Message Creation**
   ```
   Location: /chats/chat123/messages/msg123
   Operation: create
   Authenticated: Yes (user in chat participants)
   Expected: Allow
   ```

### Method 2: Firebase Emulator Testing

1. **Install Firebase CLI**
   ```bash
   npm install -g firebase-tools
   ```

2. **Initialize Firebase Emulator**
   ```bash
   firebase init emulators
   # Select Firestore
   ```

3. **Start Emulator**
   ```bash
   firebase emulators:start
   ```

4. **Access Emulator UI**
   - Open browser to `http://localhost:4000`
   - Navigate to Firestore tab
   - Test operations manually

5. **Create Test Script** (Optional)
   Create `firestore-rules-test.js`:
   ```javascript
   const firebase = require('@firebase/testing');
   const fs = require('fs');

   const PROJECT_ID = 'test-project';
   const rules = fs.readFileSync('firestore.rules', 'utf8');

   async function setup() {
     await firebase.loadFirestoreRules({
       projectId: PROJECT_ID,
       rules: rules
     });
   }

   async function testUserRead() {
     const db = firebase.initializeTestApp({
       projectId: PROJECT_ID,
       auth: { uid: 'user1' }
     }).firestore();

     const testDoc = db.collection('users').doc('user2');
     await firebase.assertSucceeds(testDoc.get());
   }

   async function testChatCreation() {
     const db = firebase.initializeTestApp({
       projectId: PROJECT_ID,
       auth: { uid: 'user1' }
     }).firestore();

     const testDoc = db.collection('chats').doc('chat1');
     await firebase.assertSucceeds(testDoc.set({
       participants: ['user1', 'user2'],
       createdAt: Date.now()
     }));
   }

   async function runTests() {
     await setup();
     await testUserRead();
     await testChatCreation();
     console.log('All tests passed!');
   }

   runTests().catch(console.error);
   ```

6. **Run Tests**
   ```bash
   node firestore-rules-test.js
   ```

### Method 3: Android App Testing

#### Test 1: User Profile Access
```kotlin
// In any Fragment or Activity
lifecycleScope.launch {
    try {
        // Should succeed - reading another user's profile
        val userDoc = firestore.collection("users")
            .document("otherUserId")
            .get()
            .await()
        
        Log.d("RulesTest", "✅ User profile read successful")
    } catch (e: Exception) {
        Log.e("RulesTest", "❌ User profile read failed", e)
    }
}
```

#### Test 2: Group Creation
```kotlin
lifecycleScope.launch {
    try {
        val currentUserId = auth.currentUser?.uid ?: return@launch
        
        val newGroup = hashMapOf(
            "name" to "Test Group",
            "members" to listOf(currentUserId),
            "admins" to listOf(currentUserId),
            "createdBy" to currentUserId,
            "createdAt" to System.currentTimeMillis()
        )
        
        // Should succeed
        val docRef = firestore.collection("groups")
            .add(newGroup)
            .await()
        
        Log.d("RulesTest", "✅ Group creation successful: ${docRef.id}")
    } catch (e: Exception) {
        Log.e("RulesTest", "❌ Group creation failed", e)
    }
}
```

#### Test 3: Task Assignment Access
```kotlin
lifecycleScope.launch {
    try {
        val currentUserId = auth.currentUser?.uid ?: return@launch
        
        // Create a task assigned to current user
        val newTask = hashMapOf(
            "title" to "Test Task",
            "assignedTo" to listOf(currentUserId),
            "createdBy" to currentUserId,
            "status" to "pending",
            "dueDate" to System.currentTimeMillis()
        )
        
        val docRef = firestore.collection("tasks")
            .add(newTask)
            .await()
        
        // Should succeed - reading assigned task
        val taskDoc = firestore.collection("tasks")
            .document(docRef.id)
            .get()
            .await()
        
        Log.d("RulesTest", "✅ Task read successful")
        
        // Should succeed - updating assigned task
        firestore.collection("tasks")
            .document(docRef.id)
            .update("status", "completed")
            .await()
        
        Log.d("RulesTest", "✅ Task update successful")
    } catch (e: Exception) {
        Log.e("RulesTest", "❌ Task operations failed", e)
    }
}
```

#### Test 4: Chat Creation and Messaging
```kotlin
lifecycleScope.launch {
    try {
        val currentUserId = auth.currentUser?.uid ?: return@launch
        val otherUserId = "someOtherUserId" // Replace with actual user ID
        
        // Should succeed - creating chat with self as participant
        val newChat = hashMapOf(
            "type" to "DIRECT",
            "participants" to listOf(currentUserId, otherUserId),
            "createdAt" to System.currentTimeMillis()
        )
        
        val chatRef = firestore.collection("chats")
            .add(newChat)
            .await()
        
        Log.d("RulesTest", "✅ Chat creation successful: ${chatRef.id}")
        
        // Should succeed - sending message as participant
        val newMessage = hashMapOf(
            "senderId" to currentUserId,
            "text" to "Test message",
            "timestamp" to System.currentTimeMillis()
        )
        
        chatRef.collection("messages")
            .add(newMessage)
            .await()
        
        Log.d("RulesTest", "✅ Message send successful")
    } catch (e: Exception) {
        Log.e("RulesTest", "❌ Chat operations failed", e)
    }
}
```

#### Test 5: Permission Denials (Should Fail)
```kotlin
lifecycleScope.launch {
    try {
        val currentUserId = auth.currentUser?.uid ?: return@launch
        
        // Should FAIL - trying to update another user's profile
        firestore.collection("users")
            .document("otherUserId")
            .update("displayName", "Hacked Name")
            .await()
        
        Log.e("RulesTest", "❌ SECURITY ISSUE: Could update other user's profile!")
    } catch (e: FirebaseFirestoreException) {
        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            Log.d("RulesTest", "✅ Correctly denied unauthorized user update")
        } else {
            Log.e("RulesTest", "❌ Unexpected error", e)
        }
    }
}
```

## Comprehensive Test Scenarios

### Scenario 1: New User Registration
1. User creates account
2. User document is created in Firestore
3. User can read their own profile ✅
4. User can read other users' profiles ✅
5. User cannot modify other users' profiles ✅

### Scenario 2: Group Collaboration
1. User A creates a group ✅
2. User A is added as admin ✅
3. User B joins group via code ✅
4. User B can read group data ✅
5. User B cannot delete group (not admin) ✅
6. User A can delete group (is admin) ✅

### Scenario 3: Task Management
1. User A creates task assigned to User B ✅
2. User B can read the task ✅
3. User B can update task status ✅
4. User C cannot read the task (not assigned) ✅
5. User B cannot delete task (not creator) ✅
6. User A can delete task (is creator) ✅

### Scenario 4: Chat Communication
1. User A starts chat with User B ✅
2. Chat document is created with both as participants ✅
3. User A can send messages ✅
4. User B can send messages ✅
5. User C cannot read messages (not participant) ✅
6. User A can only edit their own messages ✅

## Monitoring and Debugging

### Enable Firestore Debug Logging
```kotlin
// In Application class or MainActivity
FirebaseFirestore.setLoggingEnabled(true)
```

### Check Logcat for Permission Errors
```bash
adb logcat | grep -i "permission\|firestore"
```

### Common Error Messages
- `PERMISSION_DENIED: Missing or insufficient permissions`
  - Check if user is authenticated
  - Verify user is in required array (members, participants, assignedTo)
  - Check if document exists before accessing resource.data

- `NOT_FOUND: Document not found`
  - Verify document ID is correct
  - Check if document was created successfully

## Production Deployment

### Deploy Rules
```bash
# Deploy to production
firebase deploy --only firestore:rules

# Verify deployment
firebase firestore:rules:get
```

### Monitor Production
1. Go to Firebase Console → Firestore → Usage
2. Check for spike in denied requests
3. Review error logs in Cloud Logging

### Rollback if Needed
```bash
# List previous versions
firebase firestore:rules:list

# Rollback to previous version
firebase firestore:rules:release <version>
```

## Success Criteria

✅ All authenticated users can read user profiles  
✅ Users can only write to their own profile  
✅ Any authenticated user can create groups  
✅ Group members can read group data  
✅ Only admins can modify/delete groups  
✅ Assigned users can read and update tasks  
✅ Users can create chats where they are participants  
✅ Chat participants can send messages  
✅ Unauthorized operations are properly denied  
✅ No permission errors in production logs  

## Troubleshooting

### Issue: Chat creation fails
**Solution**: Ensure `request.resource.data.participants` includes current user

### Issue: Task queries return empty
**Solution**: Verify `assignedTo` is an array field, not a string

### Issue: Group read fails for members
**Solution**: Check that `members` array contains user ID

### Issue: Message send fails
**Solution**: Verify chat document exists and user is in participants array

## Next Steps
After successful testing:
1. Deploy rules to production
2. Monitor for 24 hours
3. Proceed to Task 8: Remove All Demo Data Dependencies
