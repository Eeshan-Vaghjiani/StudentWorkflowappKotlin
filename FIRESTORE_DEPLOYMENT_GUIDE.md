# Firestore Rules Deployment Guide

## Overview

This guide provides step-by-step instructions for deploying Firestore security rules to production safely.

## Prerequisites

- Firebase CLI installed (`npm install -g firebase-tools`)
- Authenticated with Firebase (`firebase login`)
- Project configured (`.firebaserc` exists)
- Firestore rules file (`firestore.rules`) ready for deployment

## Deployment Steps

### Option 1: Automated Deployment (Recommended)

Use the provided deployment scripts:

**Windows:**
```cmd
deploy-firestore-rules.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x deploy-firestore-rules.sh
./deploy-firestore-rules.sh
```

### Option 2: Manual Deployment

#### Step 1: Backup Current Rules

```bash
firebase firestore:rules:get > firestore.rules.backup-$(date +%Y%m%d-%H%M%S)
```

**Windows:**
```cmd
firebase firestore:rules:get > firestore.rules.backup-%date:~-4%%date:~-10,2%%date:~-7,2%
```

#### Step 2: Test Rules Locally

Start the Firebase Emulator:

```bash
firebase emulators:start --only firestore
```

The emulator will:
- Start Firestore emulator on port 8080
- Start Emulator UI on port 4000
- Load your local `firestore.rules` file

**Test your rules:**
1. Open http://localhost:4000 in your browser
2. Navigate to Firestore tab
3. Manually test operations or run automated tests
4. Check for rule violations in the console

**Run automated tests (if available):**
```bash
cd firestore-rules-tests
npm test
```

#### Step 3: Deploy to Production

```bash
firebase deploy --only firestore:rules
```

This command:
- Uploads `firestore.rules` to Firebase
- Validates rule syntax
- Applies rules to production database
- Takes effect immediately

#### Step 4: Monitor Deployment

**Check Firebase Console:**
1. Go to https://console.firebase.google.com
2. Select your project
3. Navigate to Firestore Database → Rules
4. Verify the rules were updated (check timestamp)

**Monitor for violations:**
1. Go to Firestore Database → Usage tab
2. Check for permission denied errors
3. Review error patterns in the first 15-30 minutes

#### Step 5: Verify Application Behavior

**Test critical operations:**
- [ ] Create a task
- [ ] Create a group
- [ ] Send a chat message
- [ ] View group activities
- [ ] Query public groups

**Check application logs:**
- Monitor for PERMISSION_DENIED errors
- Verify operations complete successfully
- Check error rates in Firebase Crashlytics

## Rollback Procedure

If issues occur after deployment:

### Quick Rollback

1. **Restore backup file:**
   ```bash
   cp firestore.rules.backup-YYYYMMDD-HHMMSS firestore.rules
   ```

2. **Redeploy:**
   ```bash
   firebase deploy --only firestore:rules
   ```

### Emergency Rollback via Console

1. Go to Firebase Console → Firestore → Rules
2. Click "Rules History" tab
3. Select previous working version
4. Click "Restore"

## Monitoring After Deployment

### Firebase Console Monitoring

**Check these metrics for 24-48 hours:**

1. **Permission Denied Errors**
   - Location: Firestore → Usage → Errors
   - Expected: Should decrease significantly
   - Alert if: Errors increase or new patterns emerge

2. **Read/Write Operations**
   - Location: Firestore → Usage → Operations
   - Expected: Normal operation patterns
   - Alert if: Sudden drop in operations (may indicate blocking rules)

3. **Rule Evaluation Time**
   - Location: Firestore → Usage → Performance
   - Expected: < 100ms per operation
   - Alert if: > 500ms (complex rules may need optimization)

### Application Monitoring

**Monitor these in your app:**

1. **Error Rates**
   - Check Firebase Crashlytics
   - Look for FirebaseFirestoreException
   - Filter by error code: PERMISSION_DENIED

2. **User Reports**
   - Monitor support channels
   - Look for "can't create task/group/message" reports
   - Check for "permission denied" user feedback

3. **Analytics Events**
   - Track successful operations
   - Monitor completion rates
   - Compare pre/post deployment metrics

## Common Issues and Solutions

### Issue 1: Permission Denied on Task Creation

**Symptom:** Users can't create tasks

**Check:**
- User is authenticated
- `userId` matches `request.auth.uid`
- User is in `assignedTo` array
- `assignedTo` array size is 1-50

**Solution:**
```javascript
// Ensure validation in app code
if (!task.assignedTo.contains(currentUserId)) {
    task.assignedTo.add(currentUserId)
}
```

### Issue 2: Can't Read Group Activities

**Symptom:** Group activities don't load

**Check:**
- User is member of the group
- `groupId` field exists in activity document
- Group document exists and has `memberIds` array

**Solution:**
- Verify group membership before querying activities
- Ensure `groupId` is set correctly when creating activities

### Issue 3: Chat Messages Fail to Send

**Symptom:** Messages stuck in "sending" state

**Check:**
- User is in chat `participants` array
- `senderId` matches authenticated user
- Message has content (text, imageUrl, or documentUrl)
- Timestamp is recent (within 5 minutes)

**Solution:**
```kotlin
// Ensure timestamp is set correctly
message.timestamp = Timestamp.now()

// Validate before sending
if (message.text.isEmpty() && message.imageUrl == null && message.documentUrl == null) {
    throw ValidationException("Message must have content")
}
```

## Testing Checklist

Before deploying to production, verify:

- [ ] Rules backup created
- [ ] Rules tested in emulator
- [ ] Automated tests pass (if available)
- [ ] Manual testing completed for all operations
- [ ] Rollback procedure documented and tested
- [ ] Monitoring alerts configured
- [ ] Team notified of deployment
- [ ] Deployment window scheduled (low traffic time)

## Deployment Best Practices

1. **Deploy during low-traffic periods**
   - Minimize impact on active users
   - Easier to monitor and rollback if needed

2. **Staged rollout (if possible)**
   - Deploy to staging environment first
   - Test with internal users
   - Monitor for 24 hours before production

3. **Communication**
   - Notify team before deployment
   - Have support team ready for user reports
   - Document deployment in change log

4. **Monitoring**
   - Watch metrics for first 30 minutes actively
   - Check periodically for 24 hours
   - Set up automated alerts for anomalies

5. **Documentation**
   - Keep deployment log
   - Document any issues encountered
   - Update runbook with lessons learned

## Emergency Contacts

**If critical issues occur:**

1. **Immediate rollback** using backup rules
2. **Notify team lead** about the issue
3. **Check Firebase Status** (https://status.firebase.google.com)
4. **Review logs** for error patterns
5. **Document incident** for post-mortem

## Post-Deployment Verification

**24 Hours After Deployment:**

- [ ] No increase in permission errors
- [ ] All features working normally
- [ ] No user complaints about access issues
- [ ] Performance metrics stable
- [ ] Error rates within normal range

**1 Week After Deployment:**

- [ ] Long-term stability confirmed
- [ ] No unexpected rule violations
- [ ] User satisfaction maintained
- [ ] Document lessons learned
- [ ] Update deployment procedures if needed

## Additional Resources

- [Firebase Security Rules Documentation](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite)
- [Firestore Security Rules Testing](https://firebase.google.com/docs/rules/unit-tests)
- [Firebase CLI Reference](https://firebase.google.com/docs/cli)

## Support

For issues or questions:
- Check Firebase Console for error details
- Review application logs
- Consult team documentation
- Contact Firebase Support (if needed)
