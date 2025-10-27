# Firebase Rules Migration Guide

## Overview

This guide helps you migrate from the previous Firebase Security Rules to the enhanced version. The new rules add validation, improve security, and enable new features while maintaining backward compatibility with existing data.

---

## Migration Timeline

### Recommended Approach: Zero-Downtime Migration

1. **Pre-Deployment** (1-2 hours)
   - Review this guide
   - Backup current rules
   - Test new rules in emulator
   - Update client-side validation

2. **Deployment** (15-30 minutes)
   - Deploy new Firestore rules
   - Deploy new Storage rules
   - Monitor error logs

3. **Post-Deployment** (1-2 days)
   - Monitor Firebase Console for errors
   - Verify all features work correctly
   - Clean up any non-compliant data (optional)

---

## Backward Compatibility

### ✅ Fully Compatible Changes

These changes work seamlessly with existing data:

1. **Message Access Control**
   - **Old:** Required `participants` field in each message document
   - **New:** Uses parent chat document for access control
   - **Impact:** Existing messages work without modification
   - **Benefit:** No `participants` field needed in new messages

2. **Typing Status Access Control**
   - **Old:** Required `participants` field in typing_status documents
   - **New:** Uses parent chat document for access control
   - **Impact:** Existing typing status documents work without modification

3. **Public Groups**
   - **Old:** All groups were private (member-only access)
   - **New:** Groups can be marked as public with `isPublic: true`
   - **Impact:** Existing groups without `isPublic` field default to `false` (private)
   - **Action:** No migration needed; add `isPublic` field to new groups as desired

4. **Profile Picture URL Validation**
   - **Old:** No URL validation
   - **New:** URLs must be from Firebase Storage or empty
   - **Impact:** Existing valid Firebase Storage URLs continue to work
   - **Action:** Only affects new uploads

5. **Group Task Access**
   - **Old:** Only assigned users could read tasks
   - **New:** Group members can read group tasks
   - **Impact:** Expands access (no breaking changes)

### ⚠️ Potentially Breaking Changes

These changes may require data cleanup or client updates:

1. **Chat Participant Limits**
   - **New Rule:** Direct chats = 2 participants, Group chats = 2-100 participants
   - **Impact:** Existing chats with invalid participant counts still readable
   - **Action:** Audit and fix any chats with invalid participant counts (see cleanup section)

2. **Group Member Limits**
   - **New Rule:** Groups must have 1-100 members, owner must be in memberIds
   - **Impact:** Existing groups with >100 members or owner not in memberIds may fail updates
   - **Action:** Audit and fix any non-compliant groups (see cleanup section)

3. **Task Assignment Limits**
   - **New Rule:** Tasks must have 1-50 assigned users
   - **Impact:** Existing tasks with >50 assignments may fail updates
   - **Action:** Audit and fix any tasks with excessive assignments (see cleanup section)

4. **Message Content Validation**
   - **New Rule:** Messages must have text, imageUrl, or documentUrl; text max 10,000 chars
   - **Impact:** Existing messages remain readable; new messages must comply
   - **Action:** Update client to validate before sending

5. **Notification Creation**
   - **New Rule:** Clients cannot create notifications (Cloud Functions only)
   - **Impact:** Client-side notification creation will fail
   - **Action:** Ensure all notifications are created via Cloud Functions

6. **File Type Restrictions**
   - **New Rule:** Profile pictures must be images; chat attachments must be approved types
   - **Impact:** Existing files remain accessible; new uploads must comply
   - **Action:** Update client to validate file types before upload

---

## Pre-Deployment Checklist

### 1. Backup Current Rules

**Firestore Rules:**
```bash
# Download current rules
firebase firestore:rules:get > firestore.rules.backup-$(date +%Y%m%d-%H%M%S)
```

**Storage Rules:**
```bash
# Download current storage rules
firebase storage:rules:get > storage.rules.backup-$(date +%Y%m%d-%H%M%S)
```

**Alternative:** Use Firebase Console
1. Go to Firestore → Rules
2. Copy entire rules content to a backup file
3. Go to Storage → Rules
4. Copy entire rules content to a backup file

### 2. Test in Firebase Emulator

```bash
# Start emulators
cd firestore-rules-tests
npm install
firebase emulators:start

# In another terminal, run tests
npm test
```

Verify all tests pass before deploying to production.

### 3. Update Client-Side Validation

Update your Android app to include client-side validation:

**Add validation utilities:**
```kotlin
// Already implemented in:
// - app/src/main/java/com/example/loginandregistration/utils/InputValidator.kt
// - app/src/main/java/com/example/loginandregistration/utils/FirebaseRulesValidator.kt

// Ensure these are used before Firestore writes
```

**Update repositories to use validation:**
```kotlin
// Example: ChatRepository
fun createChat(type: ChatType, participants: List<String>): Task<String> {
    // Validate before creating
    val validation = FirebaseRulesValidator.validateChatParticipants(type, participants)
    if (validation is ValidationResult.Error) {
        return Tasks.forException(IllegalArgumentException(validation.message))
    }
    
    // Proceed with creation
    // ...
}
```

### 4. Review Error Handling

Ensure your app handles these new error scenarios:

```kotlin
// In your repositories or ViewModels
.addOnFailureListener { exception ->
    val errorMessage = when {
        exception.message?.contains("PERMISSION_DENIED") == true -> {
            // Parse Firebase error and show user-friendly message
            parseFirebaseError(exception)
        }
        else -> "An error occurred. Please try again."
    }
    
    // Show error to user
    _errorState.value = errorMessage
}

fun parseFirebaseError(exception: Exception): String {
    val message = exception.message ?: return "Permission denied"
    
    return when {
        message.contains("participants") -> "Invalid number of participants"
        message.contains("memberIds") -> "Group member limit exceeded"
        message.contains("assignedTo") -> "Too many users assigned to task"
        message.contains("text") -> "Message is too long"
        message.contains("contentType") -> "File type not supported"
        message.contains("size") -> "File size exceeds limit"
        else -> "Permission denied. Please check your access rights."
    }
}
```

---

## Deployment Steps

### Step 1: Deploy Firestore Rules

```bash
# Deploy new Firestore rules
firebase deploy --only firestore:rules

# Verify deployment
firebase firestore:rules:get
```

**Expected output:** New rules should be visible in the output.

### Step 2: Deploy Storage Rules

```bash
# Deploy new Storage rules
firebase deploy --only storage

# Verify deployment
firebase storage:rules:get
```

### Step 3: Monitor Firebase Console

1. Open [Firebase Console](https://console.firebase.google.com/)
2. Navigate to your project
3. Go to Firestore → Usage tab
4. Monitor for permission denied errors
5. Go to Storage → Usage tab
6. Monitor for storage errors

### Step 4: Test Critical Flows

Manually test these flows in your app:

- [ ] Create a new chat (direct and group)
- [ ] Send messages with text, images, and documents
- [ ] Create a new group (private and public)
- [ ] Add members to a group
- [ ] Create personal and group tasks
- [ ] Upload a profile picture
- [ ] Upload chat attachments
- [ ] Search for public groups

---

## Data Cleanup (Optional)

### Audit Script

Use this script to identify non-compliant data:

```javascript
// audit-data.js
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

async function auditData() {
  const issues = [];
  
  // Check chats
  const chats = await db.collection('chats').get();
  chats.forEach(doc => {
    const data = doc.data();
    const participantCount = data.participants?.length || 0;
    
    if (data.type === 'DIRECT' && participantCount !== 2) {
      issues.push({
        collection: 'chats',
        id: doc.id,
        issue: `Direct chat has ${participantCount} participants (expected 2)`
      });
    }
    
    if (data.type === 'GROUP' && (participantCount < 2 || participantCount > 100)) {
      issues.push({
        collection: 'chats',
        id: doc.id,
        issue: `Group chat has ${participantCount} participants (expected 2-100)`
      });
    }
  });
  
  // Check groups
  const groups = await db.collection('groups').get();
  groups.forEach(doc => {
    const data = doc.data();
    const memberCount = data.memberIds?.length || 0;
    
    if (memberCount < 1 || memberCount > 100) {
      issues.push({
        collection: 'groups',
        id: doc.id,
        issue: `Group has ${memberCount} members (expected 1-100)`
      });
    }
    
    if (!data.memberIds?.includes(data.owner)) {
      issues.push({
        collection: 'groups',
        id: doc.id,
        issue: 'Owner is not in memberIds array'
      });
    }
  });
  
  // Check tasks
  const tasks = await db.collection('tasks').get();
  tasks.forEach(doc => {
    const data = doc.data();
    const assignedCount = data.assignedTo?.length || 0;
    
    if (assignedCount === 0) {
      issues.push({
        collection: 'tasks',
        id: doc.id,
        issue: 'Task has no assigned users'
      });
    }
    
    if (assignedCount > 50) {
      issues.push({
        collection: 'tasks',
        id: doc.id,
        issue: `Task has ${assignedCount} assigned users (max 50)`
      });
    }
  });
  
  // Check messages
  const allChats = await db.collection('chats').get();
  for (const chatDoc of allChats.docs) {
    const messages = await db.collection('chats').doc(chatDoc.id)
      .collection('messages').get();
    
    messages.forEach(msgDoc => {
      const data = msgDoc.data();
      const hasContent = data.text || data.imageUrl || data.documentUrl;
      
      if (!hasContent) {
        issues.push({
          collection: 'messages',
          id: `${chatDoc.id}/${msgDoc.id}`,
          issue: 'Message has no content (text, image, or document)'
        });
      }
      
      if (data.text && data.text.length > 10000) {
        issues.push({
          collection: 'messages',
          id: `${chatDoc.id}/${msgDoc.id}`,
          issue: `Message text is ${data.text.length} characters (max 10,000)`
        });
      }
    });
  }
  
  // Print report
  console.log('\n=== Data Audit Report ===\n');
  console.log(`Total issues found: ${issues.length}\n`);
  
  if (issues.length === 0) {
    console.log('✅ All data is compliant with new rules!');
  } else {
    issues.forEach(issue => {
      console.log(`❌ ${issue.collection}/${issue.id}`);
      console.log(`   ${issue.issue}\n`);
    });
  }
  
  return issues;
}

auditData()
  .then(() => process.exit(0))
  .catch(error => {
    console.error('Error:', error);
    process.exit(1);
  });
```

**Run the audit:**
```bash
node audit-data.js
```

### Cleanup Script

If issues are found, use this script to fix them:

```javascript
// cleanup-data.js
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

async function cleanupData() {
  const batch = db.batch();
  let updateCount = 0;
  
  // Fix groups: ensure owner is in memberIds
  const groups = await db.collection('groups').get();
  groups.forEach(doc => {
    const data = doc.data();
    if (!data.memberIds?.includes(data.owner)) {
      console.log(`Fixing group ${doc.id}: adding owner to memberIds`);
      batch.update(doc.ref, {
        memberIds: admin.firestore.FieldValue.arrayUnion(data.owner)
      });
      updateCount++;
    }
  });
  
  // Fix tasks: ensure assignedTo has at least creator
  const tasks = await db.collection('tasks').get();
  tasks.forEach(doc => {
    const data = doc.data();
    if (!data.assignedTo || data.assignedTo.length === 0) {
      console.log(`Fixing task ${doc.id}: adding creator to assignedTo`);
      batch.update(doc.ref, {
        assignedTo: [data.userId]
      });
      updateCount++;
    }
  });
  
  if (updateCount > 0) {
    console.log(`\nCommitting ${updateCount} updates...`);
    await batch.commit();
    console.log('✅ Cleanup complete!');
  } else {
    console.log('✅ No cleanup needed!');
  }
}

cleanupData()
  .then(() => process.exit(0))
  .catch(error => {
    console.error('Error:', error);
    process.exit(1);
  });
```

**Run the cleanup:**
```bash
node cleanup-data.js
```

---

## Rollback Procedure

If issues arise after deployment, follow these steps to rollback:

### Option 1: Rollback via Firebase Console (Fastest)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Navigate to Firestore → Rules
3. Click "View History" or "Rules History"
4. Find the previous version (before your deployment)
5. Click "Restore" or copy the old rules
6. Click "Publish"
7. Repeat for Storage → Rules

### Option 2: Rollback via CLI

```bash
# Restore Firestore rules from backup
cp firestore.rules.backup-YYYYMMDD-HHMMSS firestore.rules
firebase deploy --only firestore:rules

# Restore Storage rules from backup
cp storage.rules.backup-YYYYMMDD-HHMMSS storage.rules
firebase deploy --only storage
```

### Option 3: Emergency Rollback (Open Access)

**⚠️ WARNING: Only use in emergency. This opens your database to all authenticated users.**

```javascript
// emergency-rules.txt
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Deploy emergency rules:
```bash
cp emergency-rules.txt firestore.rules
firebase deploy --only firestore:rules
```

**Important:** Immediately investigate the issue and redeploy proper rules.

### Post-Rollback Actions

1. **Identify the Issue**
   - Check Firebase Console error logs
   - Review client-side error reports
   - Test specific failing operations

2. **Fix the Issue**
   - Update rules in your local environment
   - Test thoroughly in emulator
   - Verify fix resolves the issue

3. **Redeploy**
   - Follow deployment steps again
   - Monitor closely for first 30 minutes

---

## Monitoring and Validation

### Firebase Console Monitoring

**Firestore Usage Tab:**
- Monitor "Permission Denied" errors
- Check for unusual spikes in read/write operations
- Review error messages for patterns

**Storage Usage Tab:**
- Monitor upload failures
- Check for file type rejection errors
- Review bandwidth usage

### Application Monitoring

**Add logging to your app:**
```kotlin
// In your repositories
.addOnFailureListener { exception ->
    // Log to Firebase Crashlytics or your logging service
    FirebaseCrashlytics.getInstance().recordException(exception)
    
    // Log details for debugging
    Log.e("FirebaseRules", "Operation failed", exception)
    
    // Show user-friendly error
    showError(parseFirebaseError(exception))
}
```

### Success Metrics

After deployment, verify these metrics:

- [ ] No increase in permission denied errors
- [ ] All critical user flows working
- [ ] File uploads succeeding
- [ ] Chat messages sending successfully
- [ ] Group operations working
- [ ] Task creation and updates working
- [ ] No user complaints about access issues

---

## Troubleshooting

### Issue: "Permission Denied" on Message Creation

**Cause:** User is not a participant in the chat

**Solution:**
```kotlin
// Verify user is in participants before creating message
val chat = chatRepository.getChat(chatId).await()
if (currentUserId !in chat.participants) {
    throw IllegalStateException("You are not a participant in this chat")
}
```

### Issue: "Permission Denied" on Group Task Creation

**Cause:** User is not a member of the group

**Solution:**
```kotlin
// Verify user is group member before creating group task
if (groupId != null) {
    val group = groupRepository.getGroup(groupId).await()
    if (currentUserId !in group.memberIds) {
        throw IllegalStateException("You must be a group member to create group tasks")
    }
}
```

### Issue: "Permission Denied" on File Upload

**Cause:** File type or size exceeds limits

**Solution:**
```kotlin
// Validate file before upload
val validation = FirebaseRulesValidator.validateFile(uri, contentResolver, fileType)
if (validation is ValidationResult.Error) {
    showError(validation.message)
    return
}

// Proceed with upload
```

### Issue: Cannot Create Notifications

**Cause:** Client-side notification creation is blocked

**Solution:** Move notification creation to Cloud Functions:
```typescript
// functions/src/index.ts
export const onNewMessage = functions.firestore
  .document('chats/{chatId}/messages/{messageId}')
  .onCreate(async (snap, context) => {
    // Create notifications using admin SDK
    // (bypasses security rules)
  });
```

### Issue: Existing Chat Has Wrong Participant Count

**Cause:** Chat was created before validation rules

**Solution:** Run cleanup script or manually fix:
```javascript
// Fix specific chat
await db.collection('chats').doc(chatId).update({
  participants: [userId1, userId2] // Correct participant list
});
```

---

## FAQ

**Q: Will existing data be deleted?**
A: No. The new rules only affect new writes. Existing data remains readable.

**Q: Do I need to migrate existing messages?**
A: No. Messages without `participants` field work fine with new rules.

**Q: Can I gradually roll out the new rules?**
A: No. Firebase rules are all-or-nothing. However, the rules are designed for backward compatibility.

**Q: What if I have groups with >100 members?**
A: Existing groups remain readable. You'll need to reduce members before updating the group.

**Q: How do I make a group public?**
A: Add `isPublic: true` field when creating or updating a group.

**Q: Can I still create notifications from the client?**
A: No. Use Cloud Functions to create notifications. Clients can only mark them as read.

**Q: What happens if a file upload fails?**
A: The upload is rejected before completion. No storage space is used. Show user a clear error message.

**Q: How do I test rules before deploying?**
A: Use Firebase Emulator Suite. See `firestore-rules-tests/README-TESTING.md` for details.

---

## Support

If you encounter issues during migration:

1. Check Firebase Console error logs
2. Review this guide's troubleshooting section
3. Test in Firebase Emulator to isolate the issue
4. Use rollback procedure if necessary
5. Review the [Firebase Rules Features documentation](./FIREBASE_RULES_FEATURES.md)

---

## Summary

✅ **Backward Compatible:** Most changes work with existing data
⚠️ **Validation Added:** New writes must comply with limits
🔒 **Security Enhanced:** Better access control and validation
📊 **Monitoring Required:** Watch for errors in first 24-48 hours
🔄 **Rollback Ready:** Backup rules available if needed

The migration is designed to be smooth and non-disruptive. Follow the checklist, test thoroughly, and monitor after deployment.
