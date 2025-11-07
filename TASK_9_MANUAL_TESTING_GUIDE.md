# Task 9: Manual Testing Guide - Chat Functionality with Profiles

## Overview
This guide provides step-by-step instructions for manually testing the complete chat functionality with two different user accounts to verify that profiles are created correctly and chat operations work without errors.

**Requirements Tested:** 1.1, 4.4, 4.5

## Prerequisites

### Test Accounts
You'll need two Google accounts or Firebase test accounts:
- **Account 1**: Your primary test account
- **Account 2**: A secondary test account (can be a different Google account)

### Devices/Emulators
You can test using:
- **Option A**: Two physical devices
- **Option B**: One physical device + one emulator
- **Option C**: Two emulators running simultaneously

### Firebase Console Access
- Access to Firebase Console to verify Firestore data
- Navigate to: https://console.firebase.google.com
- Select your project
- Go to Firestore Database

## Test Procedure

### Phase 1: User 1 Sign-In and Profile Verification

#### Step 1.1: Sign in with Account 1
1. Open the app on Device/Emulator 1
2. Click "Sign in with Google"
3. Select Account 1 (e.g., testuser1@gmail.com)
4. Complete the sign-in process

**Expected Result:**
- ✅ Sign-in succeeds without errors
- ✅ App navigates to home screen
- ✅ No error dialogs appear

#### Step 1.2: Verify Profile Creation in Firestore
1. Open Firebase Console
2. Navigate to Firestore Database
3. Open the `users` collection
4. Find the document with ID matching Account 1's UID

**Expected Result:**
- ✅ User profile document exists
- ✅ Document contains:
  - `userId`: matches Firebase Auth UID
  - `displayName`: user's name from Google account
  - `email`: testuser1@gmail.com
  - `photoUrl`: user's profile photo URL (if available)
  - `createdAt`: timestamp
  - `lastActive`: timestamp
  - `online`: false

**Screenshot Location:** Take a screenshot of the Firestore document

#### Step 1.3: Note User 1 Details
Record the following for later use:
- User 1 UID: `_________________`
- User 1 Display Name: `_________________`
- User 1 Email: `_________________`

---

### Phase 2: User 2 Sign-In and Profile Verification

#### Step 2.1: Sign in with Account 2
1. Open the app on Device/Emulator 2
2. Click "Sign in with Google"
3. Select Account 2 (e.g., testuser2@gmail.com)
4. Complete the sign-in process

**Expected Result:**
- ✅ Sign-in succeeds without errors
- ✅ App navigates to home screen
- ✅ No error dialogs appear

#### Step 2.2: Verify Profile Creation in Firestore
1. Refresh Firebase Console (Firestore Database)
2. Open the `users` collection
3. Find the document with ID matching Account 2's UID

**Expected Result:**
- ✅ User profile document exists
- ✅ Document contains all required fields (same as User 1)
- ✅ Both user profiles now exist in Firestore

**Screenshot Location:** Take a screenshot showing both user documents

#### Step 2.3: Note User 2 Details
Record the following:
- User 2 UID: `_________________`
- User 2 Display Name: `_________________`
- User 2 Email: `_________________`

---

### Phase 3: Search for User and Create Chat

#### Step 3.1: Navigate to Chat Tab (User 2's Device)
1. On Device/Emulator 2 (logged in as User 2)
2. Tap the "Chat" tab in the bottom navigation
3. Tap the "+" or "New Chat" button

**Expected Result:**
- ✅ Chat screen loads without errors
- ✅ New chat dialog or screen appears

#### Step 3.2: Search for User 1
1. In the search field, enter User 1's email or display name
2. Wait for search results to appear

**Expected Result:**
- ✅ User 1 appears in search results
- ✅ User 1's display name is shown correctly
- ✅ User 1's profile photo is shown (if available)
- ✅ No "User not found" error appears

**Screenshot Location:** Take a screenshot of search results

#### Step 3.3: Create Direct Chat
1. Tap on User 1 in the search results
2. Wait for chat to be created

**Expected Result:**
- ✅ Chat is created successfully
- ✅ Chat screen opens showing empty conversation
- ✅ User 1's name appears in the chat header
- ✅ No error messages appear
- ✅ No "User not found" errors

**Screenshot Location:** Take a screenshot of the new chat screen

---

### Phase 4: Send Messages

#### Step 4.1: Send Message from User 2 to User 1
1. On Device/Emulator 2 (User 2's device)
2. In the chat with User 1, type a message: "Hello from User 2!"
3. Tap the send button

**Expected Result:**
- ✅ Message sends successfully
- ✅ Message appears in the chat with timestamp
- ✅ No error messages appear
- ✅ Message shows as sent (check mark or sent indicator)

**Screenshot Location:** Take a screenshot of the sent message

#### Step 4.2: Verify Message in Firestore
1. Open Firebase Console
2. Navigate to Firestore Database
3. Open the `chats` collection
4. Find the chat document (should have both user IDs in participants array)
5. Open the `messages` subcollection
6. Find the message document

**Expected Result:**
- ✅ Chat document exists with:
  - `type`: "DIRECT"
  - `participants`: array containing both user IDs
  - `lastMessage`: "Hello from User 2!"
  - `lastMessageTime`: recent timestamp
  - `lastMessageSenderId`: User 2's UID
- ✅ Message document exists with:
  - `senderId`: User 2's UID
  - `text`: "Hello from User 2!"
  - `timestamp`: recent timestamp

**Screenshot Location:** Take a screenshot of the Firestore chat document

#### Step 4.3: Receive Message on User 1's Device
1. On Device/Emulator 1 (User 1's device)
2. Navigate to the Chat tab
3. Look for the new chat from User 2

**Expected Result:**
- ✅ Chat appears in User 1's chat list
- ✅ Chat shows User 2's name
- ✅ Last message preview shows "Hello from User 2!"
- ✅ Unread indicator appears (if implemented)

**Screenshot Location:** Take a screenshot of User 1's chat list

#### Step 4.4: Open Chat and Reply (User 1)
1. Tap on the chat with User 2
2. Verify the message is visible
3. Type a reply: "Hello from User 1! Message received."
4. Send the message

**Expected Result:**
- ✅ Previous message from User 2 is visible
- ✅ Reply sends successfully
- ✅ Reply appears in the chat
- ✅ No errors occur

**Screenshot Location:** Take a screenshot of the conversation

#### Step 4.5: Verify Reply on User 2's Device
1. On Device/Emulator 2 (User 2's device)
2. Check the chat with User 1

**Expected Result:**
- ✅ User 1's reply appears in the chat
- ✅ Both messages are visible in correct order
- ✅ Timestamps are correct
- ✅ No errors or missing messages

**Screenshot Location:** Take a screenshot of the full conversation

---

### Phase 5: Additional Message Tests

#### Step 5.1: Send Multiple Messages
1. From both devices, send several messages back and forth
2. Test different message types:
   - Short messages
   - Long messages
   - Messages with emojis
   - Messages with special characters

**Expected Result:**
- ✅ All messages send successfully
- ✅ All messages appear on both devices
- ✅ Message order is correct
- ✅ No "User not found" errors
- ✅ No permission errors

#### Step 5.2: Test Message Persistence
1. Close the app on both devices
2. Reopen the app on both devices
3. Navigate to the chat

**Expected Result:**
- ✅ All messages are still visible
- ✅ Chat history is preserved
- ✅ No data loss

---

### Phase 6: Error Scenario Testing

#### Step 6.1: Test with Network Disconnected
1. On one device, disable network connectivity
2. Try to send a message
3. Re-enable network

**Expected Result:**
- ✅ Appropriate error message appears
- ✅ Message queues for retry (if offline support implemented)
- ✅ Message sends when network is restored
- ✅ No "User not found" errors

#### Step 6.2: Test Profile Validation
1. Verify that both users can continue chatting
2. Check that no profile-related errors occur

**Expected Result:**
- ✅ Chat continues to work normally
- ✅ No profile validation errors
- ✅ No "User not found" errors

---

## Success Criteria Checklist

### Profile Creation (Requirement 1.1)
- [ ] User 1 profile created automatically on sign-in
- [ ] User 2 profile created automatically on sign-in
- [ ] Both profiles contain all required fields
- [ ] Profiles are visible in Firestore Console

### User Search (Requirement 4.4)
- [ ] User 2 can search for User 1
- [ ] Search returns correct user information
- [ ] User profile data is displayed correctly
- [ ] No "User not found" errors during search

### Chat Creation (Requirement 4.4)
- [ ] Direct chat created successfully
- [ ] Chat document contains both participants
- [ ] Chat appears in both users' chat lists
- [ ] No permission errors during chat creation

### Message Sending (Requirement 4.5)
- [ ] Messages send successfully from User 2 to User 1
- [ ] Messages send successfully from User 1 to User 2
- [ ] Messages appear on both devices
- [ ] Message data is correct in Firestore

### Error Handling (Requirement 4.5)
- [ ] No "User not found" errors occurred
- [ ] No permission denied errors occurred
- [ ] Error messages are clear and actionable (if any errors)
- [ ] App handles network errors gracefully

### Overall Functionality
- [ ] Complete chat flow works end-to-end
- [ ] Both users can communicate successfully
- [ ] No critical errors or crashes
- [ ] User experience is smooth and intuitive

---

## Troubleshooting

### Issue: "User not found" error when searching
**Possible Causes:**
- User profile not created during sign-in
- Firestore security rules blocking read access

**Solutions:**
1. Check Firestore Console to verify both user profiles exist
2. Verify Firestore rules allow authenticated users to read user profiles
3. Sign out and sign back in to trigger profile creation

### Issue: "Permission denied" when creating chat
**Possible Causes:**
- Firestore security rules not updated
- User not authenticated properly

**Solutions:**
1. Verify Firestore rules allow chat creation
2. Check that user is authenticated (check Firebase Auth console)
3. Review firestore.rules file for correct permissions

### Issue: Messages not appearing on other device
**Possible Causes:**
- Real-time listener not set up correctly
- Network connectivity issues
- Firestore offline persistence issues

**Solutions:**
1. Check network connectivity on both devices
2. Restart the app on both devices
3. Check Firestore Console to verify messages are being saved

### Issue: Profile not created on sign-in
**Possible Causes:**
- UserProfileRepository.ensureUserProfileExists() not called
- Firestore rules blocking profile creation

**Solutions:**
1. Check Login.kt to verify profile creation is called after sign-in
2. Check MainActivity.kt to verify profile validation on app start
3. Review Firestore rules for user profile creation permissions

---

## Test Results

### Test Execution Date: _______________
### Tester Name: _______________
### App Version: _______________

### Overall Result: [ ] PASS [ ] FAIL

### Notes:
```
[Add any additional observations, issues, or comments here]
```

### Screenshots Attached:
- [ ] User 1 profile in Firestore
- [ ] User 2 profile in Firestore
- [ ] Search results showing User 1
- [ ] New chat screen
- [ ] Sent message from User 2
- [ ] Chat document in Firestore
- [ ] User 1's chat list
- [ ] Full conversation view
- [ ] Any error messages (if applicable)

---

## Next Steps

After completing this manual test:

1. **If all tests pass:**
   - Mark Task 9 as complete
   - Proceed to Task 10 (Monitor and Validate in Production)
   - Document any observations or improvements

2. **If any tests fail:**
   - Document the specific failure
   - Check the Troubleshooting section
   - Review relevant code (UserProfileRepository, ChatRepository, Login.kt)
   - Fix the issue and re-test

3. **Report findings:**
   - Update the task status in tasks.md
   - Create a summary document with test results
   - Share screenshots with the team (if applicable)

---

## Automated Test Alternative

If you prefer automated testing, run the integration test:

```bash
./gradlew connectedAndroidTest --tests ChatFunctionalityIntegrationTest
```

**Note:** The automated test requires test accounts to be set up in Firebase Authentication with the following credentials:
- testuser1@example.com / TestPassword123!
- testuser2@example.com / TestPassword123!

You'll need to create these accounts in Firebase Console > Authentication before running the automated test.
