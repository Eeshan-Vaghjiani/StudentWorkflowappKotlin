# Firestore Deployment Guide

## Overview
This guide provides step-by-step instructions to deploy Firestore security rules and indexes to fix PERMISSION_DENIED and FAILED_PRECONDITION errors in the TeamSync Collaboration app.

## Prerequisites

### 1. Install Firebase CLI
If you haven't installed Firebase CLI yet, run:

```bash
npm install -g firebase-tools
```

Or on Windows with PowerShell:
```powershell
npm install -g firebase-tools
```

### 2. Login to Firebase
```bash
firebase login
```

This will open a browser window for you to authenticate with your Google account.

### 3. Verify Project Configuration
Check that you're connected to the correct project:
```bash
firebase projects:list
```

You should see `android-logreg` in the list.

## Deployment Steps

### Step 1: Deploy Firestore Security Rules

Deploy the security rules to fix PERMISSION_DENIED errors:

```bash
firebase deploy --only firestore:rules
```

**Expected Output:**
```
✔  Deploy complete!

Project Console: https://console.firebase.google.com/project/android-logreg/overview
```

### Step 2: Deploy Firestore Indexes

Deploy the composite indexes to fix FAILED_PRECONDITION errors:

```bash
firebase deploy --only firestore:indexes
```

**Expected Output:**
```
✔  Deploy complete!
```

**Note:** Index creation can take several minutes. You'll see a message like:
```
Index creation in progress. Check status at:
https://console.firebase.google.com/project/android-logreg/firestore/indexes
```

### Step 3: Deploy Both Together (Alternative)

You can deploy both rules and indexes in one command:

```bash
firebase deploy --only firestore
```

## Verification Steps

### 1. Verify Rules Deployment

1. Open Firebase Console: https://console.firebase.google.com/project/android-logreg/firestore/rules
2. Check that the rules match the content in `firestore.rules`
3. Verify the "Last deployed" timestamp is recent

### 2. Verify Indexes Creation

1. Open Firebase Console: https://console.firebase.google.com/project/android-logreg/firestore/indexes
2. You should see three composite indexes:

   **Index 1: tasks**
   - Collection: `tasks`
   - Fields: `userId` (Ascending), `dueDate` (Ascending)
   - Status: Building → Enabled (wait for completion)

   **Index 2: groups**
   - Collection: `groups`
   - Fields: `memberIds` (Array-contains), `isActive` (Ascending), `updatedAt` (Descending)
   - Status: Building → Enabled (wait for completion)

   **Index 3: chats**
   - Collection: `chats`
   - Fields: `participants` (Array-contains), `lastMessageTime` (Descending)
   - Status: Building → Enabled (wait for completion)

3. Wait for all indexes to show "Enabled" status (this can take 5-15 minutes)

### 3. Test in the App

After deployment and index creation:

1. **Clear app data** (Settings → Apps → TeamSync → Storage → Clear Data)
2. **Restart the app**
3. **Login** with your test account
4. **Test each feature:**
   - ✅ Dashboard loads without errors
   - ✅ Groups screen displays groups
   - ✅ Tasks screen displays tasks sorted by due date
   - ✅ Calendar shows assignments
   - ✅ Chat messages load and send successfully
   - ✅ No PERMISSION_DENIED errors in logcat
   - ✅ No FAILED_PRECONDITION errors in logcat

### 4. Monitor Logcat

Run logcat to verify no errors:

```bash
adb logcat | findstr /i "firestore permission denied failed_precondition"
```

You should see **no** error messages related to permissions or missing indexes.

## What Was Fixed

### Security Rules Deployed
The `firestore.rules` file includes comprehensive security rules for:
- ✅ Users collection (read/write permissions)
- ✅ Groups collection (member-based access)
- ✅ Tasks collection (owner and assignee access)
- ✅ Chats collection (participant-based access)
- ✅ Messages subcollection (participant access)
- ✅ Notifications collection (user-specific access)
- ✅ Group activities collection (member access)

### Indexes Created
Three composite indexes were created to support complex queries:

1. **Tasks Index** (`userId` + `dueDate`)
   - Fixes: `FAILED_PRECONDITION` error when querying user tasks sorted by due date
   - Used by: TaskRepository, HomeFragment, CalendarFragment

2. **Groups Index** (`memberIds` + `isActive` + `updatedAt`)
   - Fixes: Query performance for active groups sorted by last update
   - Used by: GroupRepository, GroupsFragment, HomeFragment

3. **Chats Index** (`participants` + `lastMessageTime`)
   - Fixes: Query performance for user chats sorted by recent activity
   - Used by: ChatRepository, ChatsFragment

## Troubleshooting

### Issue: "Permission denied" when deploying
**Solution:** Make sure you're logged in and have owner/editor permissions:
```bash
firebase login --reauth
```

### Issue: "Project not found"
**Solution:** Verify the project ID in `.firebaserc` matches your Firebase project:
```bash
firebase use android-logreg
```

### Issue: Indexes still showing "Building" after 30 minutes
**Solution:** 
1. Check Firebase Console for any errors
2. Try deleting and recreating the index
3. Contact Firebase support if the issue persists

### Issue: App still shows PERMISSION_DENIED errors
**Solution:**
1. Verify rules were deployed successfully in Firebase Console
2. Clear app data and restart
3. Check that the user is properly authenticated
4. Verify the query field names match the security rules

### Issue: FAILED_PRECONDITION still occurs
**Solution:**
1. Wait for indexes to finish building (check Firebase Console)
2. Verify the index fields match the query exactly
3. Check that the index status is "Enabled" not "Building"

## Rollback Instructions

If you need to rollback the deployment:

### Rollback Rules
1. Go to Firebase Console → Firestore → Rules
2. Click "View History"
3. Select the previous version
4. Click "Restore"

### Rollback Indexes
**Note:** Indexes cannot be rolled back, but you can delete them:
1. Go to Firebase Console → Firestore → Indexes
2. Click the three dots next to the index
3. Select "Delete"

## Next Steps

After successful deployment:
1. ✅ Mark Task 1 as complete in the implementation plan
2. ➡️ Proceed to Task 2: Fix Gemini AI Model Configuration
3. 📊 Monitor Firebase Console for any query performance issues
4. 🔍 Review Firebase Usage & Billing to ensure you're within quota

## Support Resources

- Firebase Documentation: https://firebase.google.com/docs/firestore
- Security Rules Reference: https://firebase.google.com/docs/firestore/security/get-started
- Indexes Documentation: https://firebase.google.com/docs/firestore/query-data/indexing
- Firebase CLI Reference: https://firebase.google.com/docs/cli

## Summary

✅ **Files Updated:**
- `firestore.indexes.json` - Added 3 composite indexes
- `firebase.json` - Created Firebase configuration
- `.firebaserc` - Set default project to android-logreg

✅ **Deployment Commands:**
```bash
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

✅ **Verification:**
- Check Firebase Console for rules and indexes
- Test app functionality
- Monitor logcat for errors

🎯 **Expected Result:**
- No PERMISSION_DENIED errors
- No FAILED_PRECONDITION errors
- All Firestore queries execute successfully
- Real-time updates work correctly
