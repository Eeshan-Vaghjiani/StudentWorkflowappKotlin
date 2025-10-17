# Task 7: Firestore Security Rules - Deployment Guide

## Overview
Step-by-step guide to deploy Firestore security rules to production.

## Prerequisites

### 1. Install Firebase CLI (if not already installed)

**Windows (PowerShell)**
```powershell
npm install -g firebase-tools
```

**Verify Installation**
```powershell
firebase --version
```

### 2. Login to Firebase
```powershell
firebase login
```

This will open a browser window for authentication.

### 3. Initialize Firebase Project (if not already done)
```powershell
firebase init
```

Select:
- Firestore: Configure security rules and indexes files
- Choose existing project
- Accept default file names (firestore.rules, firestore.indexes.json)

## Deployment Methods

### Method 1: Firebase CLI (Recommended)

#### Step 1: Verify Rules File
```powershell
# Check if firestore.rules exists
Get-Content firestore.rules | Select-Object -First 10
```

#### Step 2: Deploy Rules Only
```powershell
firebase deploy --only firestore:rules
```

**Expected Output:**
```
=== Deploying to 'your-project-id'...

i  deploying firestore
i  firestore: checking firestore.rules for compilation errors...
✔  firestore: rules file firestore.rules compiled successfully
i  firestore: uploading rules firestore.rules...
✔  firestore: released rules firestore.rules to cloud.firestore

✔  Deploy complete!
```

#### Step 3: Verify Deployment
```powershell
firebase firestore:rules:get
```

### Method 2: Firebase Console (Manual)

#### Step 1: Open Firebase Console
1. Go to https://console.firebase.google.com
2. Select your project
3. Navigate to Firestore Database → Rules

#### Step 2: Copy Rules
1. Open `firestore.rules` in your editor
2. Copy the entire content
3. Paste into Firebase Console rules editor

#### Step 3: Publish Rules
1. Click "Publish" button
2. Confirm the deployment
3. Wait for confirmation message

#### Step 4: Verify
1. Check the "Rules" tab shows your new rules
2. Note the timestamp of deployment

## Pre-Deployment Checklist

- [ ] Backup current production rules
- [ ] Review all changes in firestore.rules
- [ ] Test rules in Firebase emulator (optional)
- [ ] Test rules in Firebase Console playground
- [ ] Verify no syntax errors
- [ ] Confirm all collections are covered
- [ ] Review helper functions
- [ ] Check authentication requirements

## Backup Current Rules

### Using Firebase CLI
```powershell
# Get current rules and save to backup file
firebase firestore:rules:get > firestore.rules.backup
```

### Using Firebase Console
1. Go to Firestore Database → Rules
2. Copy all current rules
3. Save to a file named `firestore.rules.backup`

## Testing Before Deployment

### Test in Firebase Console Playground

1. **Go to Rules Playground**
   - Firebase Console → Firestore Database → Rules
   - Click "Rules Playground" tab

2. **Test User Profile Read**
   ```
   Location: /users/testUser123
   Operation: get
   Authenticated: Yes (any user ID)
   Expected: Allow ✅
   ```

3. **Test Chat Creation**
   ```
   Location: /chats/testChat123
   Operation: create
   Authenticated: Yes (user123)
   Data: { "participants": ["user123", "user456"] }
   Expected: Allow ✅
   ```

4. **Test Unauthorized Access**
   ```
   Location: /users/user123
   Operation: update
   Authenticated: Yes (user456)
   Expected: Deny ❌
   ```

## Deployment Steps

### Step-by-Step Deployment

1. **Open PowerShell in Project Directory**
   ```powershell
   cd C:\Users\evagh\AndroidStudioProjects\loginandregistration
   ```

2. **Verify Firebase Project**
   ```powershell
   firebase projects:list
   ```

3. **Check Current Rules**
   ```powershell
   firebase firestore:rules:get
   ```

4. **Backup Current Rules**
   ```powershell
   firebase firestore:rules:get > firestore.rules.backup
   ```

5. **Deploy New Rules**
   ```powershell
   firebase deploy --only firestore:rules
   ```

6. **Verify Deployment**
   ```powershell
   firebase firestore:rules:get
   ```

7. **Test from Android App**
   - Launch the app
   - Test user profile access
   - Test group creation
   - Test task operations
   - Test chat creation
   - Test message sending

## Post-Deployment Verification

### Immediate Checks (First 5 Minutes)

- [ ] No errors in Firebase Console
- [ ] App launches successfully
- [ ] User can log in
- [ ] Dashboard loads data
- [ ] Groups display correctly
- [ ] Tasks display correctly
- [ ] Chat creation works
- [ ] Message sending works

### Short-Term Monitoring (First Hour)

- [ ] Check Firestore usage metrics
- [ ] Monitor for permission denied errors
- [ ] Review Cloud Logging for issues
- [ ] Test all major features
- [ ] Check real-time listeners work

### Long-Term Monitoring (First 24 Hours)

- [ ] Monitor denied requests metric
- [ ] Check for user complaints
- [ ] Review error logs
- [ ] Verify data consistency
- [ ] Check performance metrics

## Monitoring Commands

### View Firestore Metrics
```powershell
# Open Firebase Console
start https://console.firebase.google.com/project/YOUR_PROJECT_ID/firestore/usage
```

### Check Logs
```powershell
# View recent logs
firebase functions:log
```

### Android App Logging
```powershell
# Monitor app logs
adb logcat | Select-String -Pattern "Firestore|Permission"
```

## Rollback Procedure

### If Issues Occur

1. **Identify the Problem**
   - Check error logs
   - Identify which operations are failing
   - Determine if it's a rules issue

2. **Quick Fix (if possible)**
   - Edit rules in Firebase Console
   - Make minimal change to fix issue
   - Publish immediately

3. **Full Rollback (if needed)**
   ```powershell
   # Restore from backup
   Copy-Item firestore.rules.backup firestore.rules
   
   # Deploy backup rules
   firebase deploy --only firestore:rules
   ```

4. **Verify Rollback**
   - Test affected operations
   - Confirm app is working
   - Review what went wrong

## Common Deployment Issues

### Issue 1: Firebase CLI Not Found
**Error**: `firebase: The term 'firebase' is not recognized`

**Solution**:
```powershell
npm install -g firebase-tools
```

### Issue 2: Not Logged In
**Error**: `Error: Not logged in`

**Solution**:
```powershell
firebase login
```

### Issue 3: Wrong Project
**Error**: `Error: Invalid project id`

**Solution**:
```powershell
# List projects
firebase projects:list

# Use correct project
firebase use YOUR_PROJECT_ID
```

### Issue 4: Syntax Error in Rules
**Error**: `Error: Compilation error in firestore.rules`

**Solution**:
- Check for missing brackets
- Verify function names
- Check for typos in field names
- Test in Firebase Console first

### Issue 5: Permission Denied After Deployment
**Error**: App shows permission errors

**Solution**:
- Check if user is authenticated
- Verify user is in required arrays
- Test specific rule in Console playground
- Review recent changes

## Alternative Deployment (Without CLI)

If Firebase CLI is not available:

1. **Open Firebase Console**
   - Go to https://console.firebase.google.com
   - Select your project

2. **Navigate to Rules**
   - Click "Firestore Database"
   - Click "Rules" tab

3. **Copy Local Rules**
   - Open `firestore.rules` in editor
   - Copy entire content (Ctrl+A, Ctrl+C)

4. **Paste in Console**
   - Select all in Console editor (Ctrl+A)
   - Paste (Ctrl+V)

5. **Publish**
   - Click "Publish" button
   - Wait for confirmation

6. **Verify**
   - Check timestamp updated
   - Test from app

## Success Criteria

✅ Rules deployed without errors  
✅ All app features working  
✅ No permission denied errors  
✅ Real-time listeners functioning  
✅ Users can create groups  
✅ Users can create chats  
✅ Users can send messages  
✅ Tasks display correctly  
✅ Dashboard shows real data  

## Next Steps After Deployment

1. **Monitor for 24 Hours**
   - Check metrics daily
   - Review error logs
   - Address any issues

2. **Document Any Issues**
   - Note what went wrong
   - Document solutions
   - Update rules if needed

3. **Proceed to Next Task**
   - Once stable for 24 hours
   - Mark Task 7 as complete
   - Begin Task 8: Remove All Demo Data Dependencies

## Support Resources

### Firebase Documentation
- Rules Reference: https://firebase.google.com/docs/firestore/security/rules-structure
- Rules Cookbook: https://firebase.google.com/docs/firestore/security/rules-cookbook
- Testing Rules: https://firebase.google.com/docs/firestore/security/test-rules-emulator

### Firebase Console
- Project Console: https://console.firebase.google.com
- Firestore Rules: https://console.firebase.google.com/project/YOUR_PROJECT_ID/firestore/rules

### Community Support
- Stack Overflow: https://stackoverflow.com/questions/tagged/google-cloud-firestore
- Firebase Community: https://firebase.google.com/community

## Contact Information

If you encounter issues:
1. Check Firebase Console logs
2. Review this deployment guide
3. Test in Rules Playground
4. Check Stack Overflow
5. Contact Firebase Support

## Deployment Log Template

```
Deployment Date: _______________
Deployed By: _______________
Project ID: _______________
Backup File: firestore.rules.backup
Deployment Method: [ ] CLI  [ ] Console
Deployment Time: _______________
Issues Encountered: _______________
Resolution: _______________
Status: [ ] Success  [ ] Rollback  [ ] Partial
```

## Final Notes

- Always backup before deploying
- Test in playground first
- Monitor after deployment
- Keep backup for 30 days
- Document all changes
- Update team on deployment

**Current Status**: Rules ready for deployment ✅
