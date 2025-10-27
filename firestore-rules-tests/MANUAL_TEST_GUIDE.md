# Manual Testing Guide for Firestore Rules

This guide provides step-by-step instructions for manually testing the Firestore security rules using the Firebase Emulator UI.

## Prerequisites

1. Firebase CLI installed (`firebase --version` should work)
2. Firebase project configured (`.firebaserc` file exists)

## Setup

1. Start the Firebase Emulator with UI:
```bash
firebase emulators:start --only firestore
```

2. Open the Emulator UI in your browser:
```
http://localhost:4000
```

3. Navigate to the Firestore tab

## Test Cases

### Test 1: Groups Collection - Read Permissions

#### Test 1.1: User can read groups they are a member of

**Setup:**
1. In Firestore Emulator UI, create a document in `groups` collection:
   - Document ID: `group1`
   - Fields:
     ```json
     {
       "name": "Test Group",
       "owner": "user1",
       "memberIds": ["user1", "user2"],
       "isActive": true
     }
     ```

**Test:**
1. Go to the "Rules Playground" tab
2. Set authentication: `user1`
3. Try to read: `get /databases/(default)/documents/groups/group1`
4. **Expected Result:** ✅ Success - User can read the group

**Test:**
1. Set authentication: `user3`
2. Try to read: `get /databases/(default)/documents/groups/group1`
3. **Expected Result:** ❌ Permission Denied - User cannot read the group

#### Test 1.2: User can query groups with array-contains filter

**Test:**
1. Set authentication: `user1`
2. Try query: `list /databases/(default)/documents/groups where memberIds array-contains user1`
3. **Expected Result:** ✅ Success - Returns group1

**Test:**
1. Set authentication: `user3`
2. Try query: `list /databases/(default)/documents/groups where memberIds array-contains user3`
3. **Expected Result:** ✅ Success - Returns empty list (no permission error)

### Test 2: Groups Collection - Create Permissions

#### Test 2.1: User can create group with themselves as member and owner

**Test:**
1. Set authentication: `user1`
2. Try to create: `create /databases/(default)/documents/groups/group2`
3. Data:
   ```json
   {
     "name": "New Group",
     "owner": "user1",
     "memberIds": ["user1"],
     "isActive": true
   }
   ```
4. **Expected Result:** ✅ Success - Group created

#### Test 2.2: User cannot create group without themselves as member

**Test:**
1. Set authentication: `user1`
2. Try to create: `create /databases/(default)/documents/groups/group3`
3. Data:
   ```json
   {
     "name": "Invalid Group",
     "owner": "user1",
     "memberIds": ["user2"],
     "isActive": true
   }
   ```
4. **Expected Result:** ❌ Permission Denied - Cannot create group

#### Test 2.3: User cannot create group with different owner

**Test:**
1. Set authentication: `user1`
2. Try to create: `create /databases/(default)/documents/groups/group4`
3. Data:
   ```json
   {
     "name": "Invalid Group",
     "owner": "user2",
     "memberIds": ["user1", "user2"],
     "isActive": true
   }
   ```
4. **Expected Result:** ❌ Permission Denied - Cannot create group

### Test 3: Tasks Collection - Read Permissions

#### Test 3.1: User can read tasks they created

**Setup:**
1. Create a document in `tasks` collection:
   - Document ID: `task1`
   - Fields:
     ```json
     {
       "title": "Test Task",
       "userId": "user1",
       "assignedTo": [],
       "status": "pending"
     }
     ```

**Test:**
1. Set authentication: `user1`
2. Try to read: `get /databases/(default)/documents/tasks/task1`
3. **Expected Result:** ✅ Success - User can read their task

**Test:**
1. Set authentication: `user2`
2. Try to read: `get /databases/(default)/documents/tasks/task1`
3. **Expected Result:** ❌ Permission Denied - User cannot read the task

#### Test 3.2: User can read tasks they are assigned to

**Setup:**
1. Create a document in `tasks` collection:
   - Document ID: `task2`
   - Fields:
     ```json
     {
       "title": "Assigned Task",
       "userId": "user1",
       "assignedTo": ["user2", "user3"],
       "status": "pending"
     }
     ```

**Test:**
1. Set authentication: `user2`
2. Try to read: `get /databases/(default)/documents/tasks/task2`
3. **Expected Result:** ✅ Success - User can read assigned task

#### Test 3.3: User can query their tasks

**Test:**
1. Set authentication: `user1`
2. Try query: `list /databases/(default)/documents/tasks where userId == user1`
3. **Expected Result:** ✅ Success - Returns task1

**Test:**
1. Set authentication: `user2`
2. Try query: `list /databases/(default)/documents/tasks where assignedTo array-contains user2`
3. **Expected Result:** ✅ Success - Returns task2

### Test 4: Chats Collection - Read Permissions

#### Test 4.1: User can read chats they are a participant in

**Setup:**
1. Create a document in `chats` collection:
   - Document ID: `chat1`
   - Fields:
     ```json
     {
       "participants": ["user1", "user2"],
       "lastMessage": "Hello",
       "lastMessageTime": "2024-01-01T00:00:00Z"
     }
     ```

**Test:**
1. Set authentication: `user1`
2. Try to read: `get /databases/(default)/documents/chats/chat1`
3. **Expected Result:** ✅ Success - User can read the chat

**Test:**
1. Set authentication: `user3`
2. Try to read: `get /databases/(default)/documents/chats/chat1`
3. **Expected Result:** ❌ Permission Denied - User cannot read the chat

#### Test 4.2: User can query their chats

**Test:**
1. Set authentication: `user1`
2. Try query: `list /databases/(default)/documents/chats where participants array-contains user1`
3. **Expected Result:** ✅ Success - Returns chat1

**Test:**
1. Set authentication: `user3`
2. Try query: `list /databases/(default)/documents/chats where participants array-contains user3`
3. **Expected Result:** ✅ Success - Returns empty list (no permission error)

### Test 5: Group Activities - Read Permissions

#### Test 5.1: User can read activities from groups they are members of

**Setup:**
1. Create a document in `group_activities` collection:
   - Document ID: `activity1`
   - Fields:
     ```json
     {
       "groupId": "group1",
       "userId": "user1",
       "type": "task_created",
       "description": "Created a task",
       "memberIds": ["user1", "user2"],
       "createdAt": "2024-01-01T00:00:00Z"
     }
     ```

**Test:**
1. Set authentication: `user1`
2. Try to read: `get /databases/(default)/documents/group_activities/activity1`
3. **Expected Result:** ✅ Success - User can read the activity

**Test:**
1. Set authentication: `user3`
2. Try to read: `get /databases/(default)/documents/group_activities/activity1`
3. **Expected Result:** ❌ Permission Denied - User cannot read the activity

## Test Results Summary

Create a checklist as you complete each test:

- [ ] Test 1.1: User can read groups they are a member of
- [ ] Test 1.2: User can query groups with array-contains filter
- [ ] Test 2.1: User can create group with themselves as member and owner
- [ ] Test 2.2: User cannot create group without themselves as member
- [ ] Test 2.3: User cannot create group with different owner
- [ ] Test 3.1: User can read tasks they created
- [ ] Test 3.2: User can read tasks they are assigned to
- [ ] Test 3.3: User can query their tasks
- [ ] Test 4.1: User can read chats they are a participant in
- [ ] Test 4.2: User can query their chats
- [ ] Test 5.1: User can read activities from groups they are members of

## Troubleshooting

### Emulator Won't Start
- Check if port 8080 is already in use
- Try: `firebase emulators:start --only firestore --project test-project`

### Rules Not Loading
- Ensure `firestore.rules` file exists in the project root
- Check the `firebase.json` configuration
- Restart the emulator

### Permission Errors Not Showing
- Make sure you're setting the authentication in the Rules Playground
- Verify the user ID matches the data in the documents
- Check the browser console for detailed error messages

## Next Steps

After completing manual testing:
1. Document any issues found
2. Update rules if needed
3. Re-test failed scenarios
4. Proceed to deployment when all tests pass
