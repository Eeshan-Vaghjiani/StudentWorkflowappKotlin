# Task 9: Quick Verification Checklist

## Chat Functionality with Profiles - Quick Test

### Setup (5 minutes)
- [ ] Two test accounts ready (Google or Firebase)
- [ ] Two devices/emulators available
- [ ] Firebase Console open (Firestore Database)
- [ ] App installed on both devices

### User 1 Setup (2 minutes)
- [ ] Sign in with Account 1
- [ ] No errors during sign-in
- [ ] Profile document exists in Firestore `users` collection
- [ ] Profile has: userId, displayName, email, createdAt, lastActive

### User 2 Setup (2 minutes)
- [ ] Sign in with Account 2 on second device
- [ ] No errors during sign-in
- [ ] Profile document exists in Firestore `users` collection
- [ ] Both user profiles now visible in Firestore

### Search & Chat Creation (3 minutes)
- [ ] User 2: Navigate to Chat tab
- [ ] User 2: Tap "New Chat" or "+"
- [ ] User 2: Search for User 1 by email/name
- [ ] User 1 appears in search results ✅
- [ ] No "User not found" error ✅
- [ ] Tap User 1 to create chat
- [ ] Chat created successfully ✅
- [ ] Chat screen opens without errors ✅

### Message Exchange (5 minutes)
- [ ] User 2: Send message "Hello from User 2!"
- [ ] Message sends successfully ✅
- [ ] Message appears in chat ✅
- [ ] No errors ✅
- [ ] User 1: Check Chat tab
- [ ] New chat appears in list ✅
- [ ] User 1: Open chat with User 2
- [ ] Message from User 2 is visible ✅
- [ ] User 1: Reply "Hello from User 1!"
- [ ] Reply sends successfully ✅
- [ ] User 2: See reply in chat ✅
- [ ] Both messages visible on both devices ✅

### Firestore Verification (2 minutes)
- [ ] Chat document exists in `chats` collection
- [ ] Chat has `type: "DIRECT"`
- [ ] Chat has 2 participants (both user IDs)
- [ ] Messages exist in `chats/{chatId}/messages` subcollection
- [ ] Message documents have correct senderId and text

### Final Checks
- [ ] No "User not found" errors occurred ✅
- [ ] No permission denied errors ✅
- [ ] No app crashes ✅
- [ ] Chat functionality works end-to-end ✅

---

## Result: [ ] PASS [ ] FAIL

**Total Time:** ~20 minutes

**Issues Found:**
```
[List any issues here]
```

**Requirements Verified:**
- ✅ Requirement 1.1: Automatic profile creation on sign-in
- ✅ Requirement 4.4: Profile validation before chat operations
- ✅ Requirement 4.5: Clear error messages (no errors should occur)

---

## Quick Commands

### Run Automated Test
```bash
./gradlew connectedAndroidTest --tests ChatFunctionalityIntegrationTest
```

### Check Firestore Rules
```bash
firebase deploy --only firestore:rules
```

### View Logs
```bash
adb logcat | grep -E "ChatRepository|UserProfileRepository|ChatFragment"
```

---

## Common Issues & Quick Fixes

| Issue | Quick Fix |
|-------|-----------|
| User not found | Sign out both users, sign back in |
| Permission denied | Deploy updated firestore.rules |
| Profile missing | Check Login.kt calls ensureUserProfileExists() |
| Chat not appearing | Refresh app, check network connection |
| Messages not syncing | Check Firestore real-time listeners |

---

## Success Criteria (All Must Pass)

1. ✅ Both user profiles created automatically
2. ✅ Users can search for each other
3. ✅ Direct chat created successfully
4. ✅ Messages sent and received on both devices
5. ✅ No "User not found" errors
6. ✅ No permission errors
7. ✅ Data persists in Firestore correctly

**If all criteria pass → Task 9 Complete! ✅**
