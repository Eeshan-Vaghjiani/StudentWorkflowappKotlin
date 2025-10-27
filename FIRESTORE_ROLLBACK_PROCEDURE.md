# Firestore Rules Rollback Procedure

## Overview

This document provides step-by-step instructions for rolling back Firestore security rules changes in case of issues or unexpected behavior after deployment.

**Last Updated:** October 20, 2025  
**Current Version:** 2.0  
**Backup Version:** 1.0

## When to Rollback

Consider rolling back if you observe:

- Increased permission denied errors in production logs
- App crash rate increases significantly (> 1%)
- Users reporting inability to access features
- Query success rate drops below 95%
- Unexpected security vulnerabilities discovered
- Data access patterns not working as expected

## Pre-Rollback Checklist

Before initiating a rollback, verify:

- [ ] Issue is confirmed and reproducible
- [ ] Issue is directly related to rules changes (not client code)
- [ ] Backup rules file exists and is accessible
- [ ] You have Firebase project admin access
- [ ] Team has been notified of the rollback
- [ ] Monitoring dashboard is open to track impact

## Rollback Methods

### Method 1: Firebase Console (Recommended for Quick Rollback)

**Time Required:** 2-3 minutes

1. Open [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Navigate to Firestore Database → Rules
4. Click on the "Rules" tab
5. Click "View History" button
6. Find the previous working version (usually labeled with timestamp)
7. Click "Restore" next to the desired version
8. Review the rules in the editor
9. Click "Publish" to deploy

**Advantages:**
- Fastest method
- Visual confirmation of changes
- Built-in version history

**Disadvantages:**
- Requires manual navigation
- No command-line automation

### Method 2: Firebase CLI (Recommended for Automation)

**Time Required:** 1-2 minutes


**Prerequisites:**
- Firebase CLI installed (`npm install -g firebase-tools`)
- Authenticated to Firebase (`firebase login`)
- In project root directory

**Steps:**

1. Verify backup file exists:
```bash
dir firestore.rules.backup
```

2. Copy backup to main rules file:
```bash
copy firestore.rules.backup firestore.rules
```

3. Deploy the rules:
```bash
firebase deploy --only firestore:rules
```

4. Verify deployment:
```bash
firebase firestore:rules:get
```

**Advantages:**
- Fast and scriptable
- Can be automated
- Version controlled

**Disadvantages:**
- Requires CLI setup
- Command-line only

### Method 3: Manual File Restoration

**Time Required:** 3-5 minutes

1. Locate backup file: `firestore.rules.backup`
2. Open both `firestore.rules` and `firestore.rules.backup`
3. Copy entire content from backup file
4. Paste into `firestore.rules`, replacing all content
5. Save the file
6. Deploy using Firebase CLI or Console


## Quick Rollback Script (Windows)

Create a file named `rollback-firestore-rules.bat`:

```batch
@echo off
echo ========================================
echo Firestore Rules Rollback Script
echo ========================================
echo.

echo Step 1: Backing up current rules...
copy firestore.rules firestore.rules.rollback-backup-%date:~-4,4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%
echo Current rules backed up.
echo.

echo Step 2: Restoring previous rules...
copy firestore.rules.backup firestore.rules
echo Rules restored from backup.
echo.

echo Step 3: Deploying to Firebase...
firebase deploy --only firestore:rules
echo.

echo Step 4: Verifying deployment...
firebase firestore:rules:get
echo.

echo ========================================
echo Rollback Complete!
echo ========================================
echo Please monitor the app for 15 minutes to ensure stability.
pause
```

**Usage:**
```bash
rollback-firestore-rules.bat
```


## Post-Rollback Verification

After rolling back, verify the following within 15 minutes:

### 1. Check Firebase Console
- [ ] Rules show correct version in Firebase Console
- [ ] Deployment timestamp is recent
- [ ] No syntax errors in rules

### 2. Monitor Error Logs
- [ ] Check Firestore logs for permission errors
- [ ] Verify error rate has decreased
- [ ] Check for any new unexpected errors

### 3. Test Critical Flows
- [ ] User can log in
- [ ] Groups screen loads without crashing
- [ ] User can create a new group
- [ ] Tasks screen loads correctly
- [ ] Chat functionality works

### 4. Check Metrics
- [ ] App crash rate returns to baseline
- [ ] Query success rate improves
- [ ] No spike in support tickets


## Backup File Locations

Current backup files in the project:

| File | Description | Date |
|------|-------------|------|
| `firestore.rules.backup` | Main backup (pre-fix) | Oct 18, 2025 |
| `firestore.rules.backup-20251020-183456` | Timestamped backup | Oct 20, 2025 |
| `firestore.rules` | Current production rules | Oct 20, 2025 |

## Emergency Contacts

If rollback doesn't resolve the issue:

1. Check the troubleshooting guide: `FIRESTORE_TROUBLESHOOTING.md`
2. Review recent changes: `FIRESTORE_RULES_CHANGES.md`
3. Contact the development team
4. Escalate to Firebase support if needed

## Common Rollback Scenarios

### Scenario 1: Permission Errors Increase

**Symptoms:**
- Users getting "Permission Denied" errors
- Firestore logs show increased PERMISSION_DENIED

**Action:**
1. Immediate rollback using Method 2 (CLI)
2. Verify error rate decreases
3. Investigate root cause before redeploying

### Scenario 2: App Crashes Increase

**Symptoms:**
- Crash analytics show spike in crashes
- Crashes related to Firestore operations

**Action:**
1. Rollback using Method 1 (Console) for speed
2. Check if crashes are client-side or rules-related
3. May need to rollback both rules and client code

### Scenario 3: Query Performance Degradation

**Symptoms:**
- Slow query response times
- Timeout errors
- Users reporting slow app performance

**Action:**
1. Verify issue is rules-related (check query patterns)
2. Rollback if confirmed
3. Review query optimization strategies


## Rollback Decision Matrix

| Severity | Error Rate | Crash Rate | Action | Timeline |
|----------|-----------|------------|--------|----------|
| Critical | > 10% | > 5% | Immediate rollback | < 5 min |
| High | 5-10% | 2-5% | Rollback within 15 min | < 15 min |
| Medium | 2-5% | 1-2% | Investigate, prepare rollback | < 30 min |
| Low | < 2% | < 1% | Monitor, no immediate action | Ongoing |

## Testing After Rollback

Once rollback is complete and verified:

1. **Root Cause Analysis:**
   - Identify what went wrong
   - Document the issue
   - Determine if it was rules or client code

2. **Fix Development:**
   - Develop fix in development environment
   - Test thoroughly with Firebase Emulator
   - Run integration tests

3. **Staged Redeployment:**
   - Deploy to test environment first
   - Monitor for 24 hours
   - Deploy to production with monitoring

## Prevention Strategies

To avoid future rollbacks:

1. **Always test in emulator first**
2. **Use staged deployments** (dev → staging → production)
3. **Monitor metrics closely** after deployment
4. **Keep backup files** with timestamps
5. **Document all changes** thoroughly
6. **Have rollback script ready** before deployment

## Version History

| Version | Date | Status | Notes |
|---------|------|--------|-------|
| 2.0 | Oct 20, 2025 | Current | Fixed circular dependencies |
| 1.5 | Oct 19, 2025 | Rolled back | Client error handling only |
| 1.0 | Oct 18, 2025 | Deprecated | Had circular dependencies |

## References

- [Firebase Rules Documentation](https://firebase.google.com/docs/firestore/security/get-started)
- [Rules Changes Log](./FIRESTORE_RULES_CHANGES.md)
- [Query Patterns Guide](./FIRESTORE_QUERY_PATTERNS.md)
- [Troubleshooting Guide](./FIRESTORE_TROUBLESHOOTING.md)
