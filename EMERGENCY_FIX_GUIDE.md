# Emergency Fix Guide - Complete App Restoration

## Current Issues
- ❌ Can't send messages
- ❌ Can't create groups
- ❌ Can't see group stats
- ❌ Can't see task stats
- ❌ Homepage shows no stats
- ❌ Can't create new chats
- ❌ Google Sign-In not redirecting

## Root Cause
The UserProfile documents in Firestore are missing the `userId` field, causing deserialization to fail. This cascades into all features that depend on user profiles.

## Quick Fix Options

### Option 1: Manual Firestore Console Fix (FASTEST - 2 minutes)

1. Open Firebase Console: https://console.firebase.google.com/project/android-logreg/firestore
2. Navigate to `users` collection
3. For EACH user document:
   - Click on the document
   - Click "Add field"
   - Field name: `userId`
   - Field value: Copy the document ID
   - Click "Add"
4. Restart the app

### Option 2: Run Migration Script (5 minutes)

**Prerequisites:**
```bash
npm install firebase-admin
```

**Get Service Account Key:**
1. Go to Firebase Console → Project Settings → Service Accounts
2. Click "Generate new private key"
3. Save as `serviceAccountKey.json` in project root

**Run Migration:**
```bash
node fix-user-profiles.js
```

### Option 3: Force User Re-creation (10 minutes)

**In Firebase Console:**
1. Go to Firestore Database
2. Delete all documents in `users` collection
3. In the app, sign out all test users
4. Sign in again - profiles will be recreated correctly

## Verification Steps

After applying any fix:

1. **Check Firestore Console**
   - Open `users` collection
   - Verify each document has `userId` field matching document ID

2. **Test in App**
   ```
   ✅ Login with email
   ✅ Login with Google
   ✅ Homepage shows stats
   ✅ Create a group
   ✅ Send a message
   ✅ Create a new chat
   ```

3. **Check Logs**
   ```bash
   adb logcat | findstr "UserProfile"
   ```
   Should see NO errors about @DocumentId or deserialization

## Additional Firestore Rules Check

If issues persist after fixing user profiles, verify rules are deployed:

```bash
firebase deploy --only firestore:rules
```

Expected output:
```
✔ firestore: released rules firestore.rules to cloud.firestore
```

## Test Account Credentials

Use these for testing:
- **Email**: evaghjiani04@gmail.com
- **Password**: (your test password)

## Emergency Rollback

If nothing works, rollback to last working state:

1. **Restore Previous Rules:**
   ```bash
   git checkout HEAD~1 firestore.rules
   firebase deploy --only firestore:rules
   ```

2. **Restore Previous Model:**
   ```bash
   git checkout HEAD~1 app/src/main/java/com/example/loginandregistration/models/UserProfile.kt
   ```

3. **Rebuild:**
   ```bash
   ./gradlew clean assembleDebug
   ```

## Support Checklist

- [ ] User profiles have `userId` field in Firestore
- [ ] Firestore rules deployed successfully
- [ ] UserProfile.kt has `@PropertyName("userId")` (not `@DocumentId`)
- [ ] App rebuilt after model changes
- [ ] Test user can login
- [ ] Homepage loads without errors
- [ ] Can create groups
- [ ] Can send messages

## Contact Points

If all else fails:
1. Check Firebase Console for quota/billing issues
2. Verify internet connectivity
3. Check Android Studio logcat for specific errors
4. Verify Firebase project is active

---
**Last Updated:** October 31, 2025
**Status:** CRITICAL - Requires immediate attention
