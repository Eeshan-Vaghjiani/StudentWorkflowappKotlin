# Firestore Rules Deployment - Monitoring Guide

## Quick Access Links

**Firebase Console:** https://console.firebase.google.com/project/android-logreg/overview  
**Firestore Database:** https://console.firebase.google.com/project/android-logreg/firestore  
**Cloud Logging:** https://console.firebase.google.com/project/android-logreg/logs

---

## Monitoring Checklist

### Immediate (First Hour - Check Every 15 Minutes)

- [ ] **15 min:** Check Firestore logs for PERMISSION_DENIED errors
- [ ] **15 min:** Verify app crash rate hasn't increased
- [ ] **30 min:** Check Firestore logs again
- [ ] **30 min:** Review any error patterns
- [ ] **45 min:** Check Firestore logs
- [ ] **60 min:** Full verification check

### Short-term (First 24 Hours - Check Every 2 Hours)

- [ ] **2 hours:** Review Firestore error logs
- [ ] **4 hours:** Check app crash analytics
- [ ] **6 hours:** Verify query success rates
- [ ] **8 hours:** Review user feedback/reports
- [ ] **12 hours:** Check Firestore usage metrics
- [ ] **24 hours:** Full deployment verification

### Long-term (First Week - Daily Checks)

- [ ] **Day 1:** Complete 24-hour monitoring
- [ ] **Day 2:** Review daily metrics
- [ ] **Day 3:** Check for any delayed issues
- [ ] **Day 7:** Final verification and sign-off

---

## Key Metrics to Track

### 1. Permission Errors
**Target:** 0 PERMISSION_DENIED errors for authenticated users

**How to Check:**
```
Firebase Console → Firestore → Logs
Filter: severity:ERROR
Search: "PERMISSION_DENIED"
```

### 2. App Crash Rate
**Target:** No increase from baseline (should decrease)

**How to Check:**
```
Firebase Console → Crashlytics → Dashboard
Look for: FirebaseFirestoreException
Compare: Before vs After deployment
```

### 3. Query Success Rate
**Target:** 100% for properly filtered queries

**How to Check:**
```
Firebase Console → Firestore → Usage
Monitor: Read operations
Check: Error rate percentage
```

### 4. Response Times
**Target:** No degradation in query performance

**How to Check:**
```
Firebase Console → Performance Monitoring
Check: Network request latency
Compare: Before vs After deployment
```

---

## What to Look For

### ✅ Good Signs
- Zero PERMISSION_DENIED errors in logs
- Decreased app crash rate
- Stable query performance
- No user complaints about access issues
- Groups/Tasks/Chats loading correctly

### ⚠️ Warning Signs
- Occasional permission errors (investigate cause)
- Slight increase in query latency
- User reports of slow loading
- Increased error logs (non-permission)

### 🚨 Critical Issues (Requires Rollback)
- Widespread PERMISSION_DENIED errors
- App crash rate increase >5%
- Users unable to access their data
- Critical features broken
- Data integrity issues

---

## Firestore Log Queries

### Check for Permission Errors
```
resource.type="cloud_firestore_database"
severity="ERROR"
protoPayload.status.message=~"PERMISSION_DENIED"
timestamp>="2025-10-20T18:30:00Z"
```

### Check for All Firestore Errors
```
resource.type="cloud_firestore_database"
severity="ERROR"
timestamp>="2025-10-20T18:30:00Z"
```

### Monitor Specific Collections
```
resource.type="cloud_firestore_database"
protoPayload.resourceName=~"groups"
severity>="WARNING"
```

---

## Manual Testing Script

Run these tests to verify the deployment:

### Test 1: Groups Access
```
1. Open the app
2. Navigate to Groups screen
3. Expected: Groups load without errors
4. Create a new group
5. Expected: Group created successfully
6. View group activities
7. Expected: Activities load correctly
```

### Test 2: Tasks Access
```
1. Navigate to Tasks screen
2. Expected: Tasks load without errors
3. Create a new task
4. Expected: Task created successfully
5. Assign task to another user
6. Expected: Assignment works correctly
```

### Test 3: Chat Functionality
```
1. Open a chat conversation
2. Expected: Messages load correctly
3. Send a new message
4. Expected: Message sent and appears
5. Check typing indicators
6. Expected: Typing status works
```

### Test 4: Error Handling
```
1. Test with poor network connection
2. Expected: Graceful error messages
3. Test with no network
4. Expected: Offline mode works
5. Reconnect network
6. Expected: Data syncs correctly
```

---

## Troubleshooting Common Issues

### Issue: Permission Denied Errors Still Appearing

**Possible Causes:**
1. Client app using old query patterns (not using filters)
2. Rules not fully deployed (wait 5 minutes)
3. Client cache needs clearing

**Solutions:**
1. Verify queries use `whereArrayContains` or `whereEqualTo`
2. Wait for rule propagation (up to 5 minutes)
3. Clear app data and restart
4. Check if error is for specific collection

### Issue: Increased Query Latency

**Possible Causes:**
1. Array-contains queries on large arrays
2. Missing indexes
3. Increased read operations

**Solutions:**
1. Monitor index creation in Firebase Console
2. Check firestore.indexes.json is deployed
3. Review query patterns for optimization

### Issue: App Crashes Persist

**Possible Causes:**
1. Client-side error handling not deployed
2. Different error type (not permission-related)
3. Network issues

**Solutions:**
1. Verify app version includes error handling updates
2. Check crash logs for actual error type
3. Test with stable network connection

---

## Rollback Procedure

If critical issues are detected, follow these steps:

### Step 1: Assess Severity
- Is data access completely broken? → Immediate rollback
- Are some users affected? → Investigate first
- Is it a minor issue? → Monitor and fix forward

### Step 2: Execute Rollback
```bash
# Restore backup rules
copy firestore.rules.backup-20251020-183456 firestore.rules

# Deploy previous rules
firebase deploy --only firestore:rules --force
```

### Step 3: Verify Rollback
- Check logs for errors clearing
- Verify app functionality restored
- Monitor for 30 minutes

### Step 4: Post-Rollback
- Document what went wrong
- Fix issues in development
- Test thoroughly before redeploying

---

## Success Criteria

Mark deployment as successful when:

✅ **24 hours** with zero permission errors  
✅ **App crash rate** returned to or below baseline  
✅ **All user flows** working correctly  
✅ **Query performance** stable or improved  
✅ **No user complaints** about access issues  

---

## Reporting Template

Use this template for status updates:

```
Firestore Rules Deployment - Status Update
Time: [Current Time]
Hours Since Deployment: [X hours]

Metrics:
- Permission Errors: [Count]
- App Crash Rate: [Percentage]
- Query Success Rate: [Percentage]
- User Reports: [Count]

Status: [Green/Yellow/Red]
Issues: [None/List issues]
Action Required: [None/Describe]

Next Check: [Time]
```

---

## Contact Information

**If Critical Issues Arise:**
1. Check this monitoring guide
2. Review rollback procedure
3. Execute rollback if necessary
4. Document incident for post-mortem

**Resources:**
- Firebase Documentation: https://firebase.google.com/docs/firestore/security
- Security Rules Reference: https://firebase.google.com/docs/rules
- Support: Firebase Console → Support

---

## Completion Checklist

Before marking Task 9 as complete:

- [x] Rules backed up successfully
- [x] Rules deployed to Firebase
- [ ] Monitored for first hour (no errors)
- [ ] Monitored for 24 hours (stable)
- [ ] Manual testing completed
- [ ] All metrics within acceptable range
- [ ] Documentation updated
- [ ] Team notified of deployment

**Current Status:** Deployment complete, monitoring in progress
