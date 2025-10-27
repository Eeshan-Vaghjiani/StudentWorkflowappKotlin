# Firestore Security Rules - Testing Checklist

## 🧪 Comprehensive Testing Guide

Use this checklist to verify that your Firestore security rules are working correctly.

---

## 📋 Pre-Testing Setup

- [ ] Rules deployed to Firebase Console
- [ ] Firebase project is active
- [ ] Test users created in Firebase Auth
- [ ] Sample data exists in Firestore collections

---

## 1️⃣ Users Collection Tests

### Read Operations
- [ ] ✅ **ALLOW**: Authenticated user reads any user profile
  ```
  User A reads User B's profile → Should succeed
  ```

- [ ] ❌ **DENY**: Unauthenticated user reads user profile
  ```
  Anonymous reads User A's profile → Should fail
  ```

### Write Operations
- [ ] ✅ **ALLOW**: User updates their own profile
  ```
  User A updates User A's profile → Should succeed
  ```

- [ ] ❌ **DENY**: User updates another user's profile
  ```
  User A updates User B's profile → Should fail
  ```

- [ ] ❌ **DENY**: Unauthenticated user creates profile
  ```
  Anonymous creates user profile → Should fail
  ```

---

## 2️⃣ Groups Collection Tests

### Read Operations
- [ ] ✅ **ALLOW**: Member reads group data
  ```
  User A (member) reads Group X → Should succeed
  ```

- [ ] ✅ **ALLOW**: Anyone reads public group
  ```
  User B (non-member) reads public Group Y → Should succeed
  ```

- [ ] ❌ **DENY**: Non-member reads private group
  ```
  User B (non-member) reads private Group X → Should fail
  ```

### Create Operations
- [ ] ✅ **ALLOW**: Authenticated user creates group
  ```
  User A creates new group → Should succeed
  ```

- [ ] ❌ **DENY**: Unauthenticated user creates group
  ```
  Anonymous creates group → Should fail
  ```

### Update Operations
- [ ] ✅ **ALLOW**: Admin updates group
  ```
  User A (admin) updates Group X → Should succeed
  ```

- [ ] ✅ **ALLOW**: Creator updates group
  ```
  User A (creator) updates Group X → Should succeed
  ```

- [ ] ❌ **DENY**: Regular member updates group
  ```
  User B (member, not admin) updates Group X → Should fail
  ```

### Delete Operations
- [ ] ✅ **ALLOW**: Admin deletes group
  ```
  User A (admin) deletes Group X → Should succeed
  ```

- [ ] ❌ **DENY**: Regular member deletes group
  ```
  User B (member) deletes Group X → Should fail
  ```

---

## 3️⃣ Tasks Collection Tests

### Read Operations
- [ ] ✅ **ALLOW**: Creator reads their task
  ```
  User A reads task created by User A → Should succeed
  ```

- [ ] ✅ **ALLOW**: Assigned user reads task
  ```
  User B reads task assigned to User B → Should succeed
  ```

- [ ] ❌ **DENY**: Unrelated user reads task
  ```
  User C reads task (not creator, not assigned) → Should fail
  ```

### Create Operations
- [ ] ✅ **ALLOW**: Authenticated user creates task
  ```
  User A creates new task → Should succeed
  ```

- [ ] ❌ **DENY**: Unauthenticated user creates task
  ```
  Anonymous creates task → Should fail
  ```

### Update Operations
- [ ] ✅ **ALLOW**: Creator updates task
  ```
  User A updates task created by User A → Should succeed
  ```

- [ ] ✅ **ALLOW**: Assigned user updates task
  ```
  User B updates task assigned to User B → Should succeed
  ```

- [ ] ❌ **DENY**: Unrelated user updates task
  ```
  User C updates task (not creator, not assigned) → Should fail
  ```

### Delete Operations
- [ ] ✅ **ALLOW**: Creator deletes task
  ```
  User A deletes task created by User A → Should succeed
  ```

- [ ] ❌ **DENY**: Assigned user deletes task
  ```
  User B deletes task (assigned but not creator) → Should fail
  ```

---

## 4️⃣ Chats Collection Tests

### Read Operations
- [ ] ✅ **ALLOW**: Participant reads chat
  ```
  User A (participant) reads Chat X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-participant reads chat
  ```
  User C (not participant) reads Chat X → Should fail
  ```

### Write Operations
- [ ] ✅ **ALLOW**: Participant updates chat metadata
  ```
  User A (participant) updates Chat X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-participant updates chat
  ```
  User C (not participant) updates Chat X → Should fail
  ```

---

## 5️⃣ Messages Subcollection Tests

### Read Operations
- [ ] ✅ **ALLOW**: Participant reads messages
  ```
  User A (participant) reads messages in Chat X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-participant reads messages
  ```
  User C (not participant) reads messages in Chat X → Should fail
  ```

### Create Operations
- [ ] ✅ **ALLOW**: Participant sends message
  ```
  User A (participant) creates message in Chat X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-participant sends message
  ```
  User C (not participant) creates message in Chat X → Should fail
  ```

### Update Operations
- [ ] ✅ **ALLOW**: Sender updates their message
  ```
  User A updates message sent by User A → Should succeed
  ```

- [ ] ❌ **DENY**: Other user updates message
  ```
  User B updates message sent by User A → Should fail
  ```

### Delete Operations
- [ ] ✅ **ALLOW**: Sender deletes their message
  ```
  User A deletes message sent by User A → Should succeed
  ```

- [ ] ❌ **DENY**: Other user deletes message
  ```
  User B deletes message sent by User A → Should fail
  ```

---

## 6️⃣ Typing Status Tests

### Read Operations
- [ ] ✅ **ALLOW**: Participant reads typing status
  ```
  User A (participant) reads typing status for Chat X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-participant reads typing status
  ```
  User C (not participant) reads typing status for Chat X → Should fail
  ```

### Write Operations
- [ ] ✅ **ALLOW**: Participant updates typing status
  ```
  User A (participant) updates typing status → Should succeed
  ```

- [ ] ❌ **DENY**: Non-participant updates typing status
  ```
  User C (not participant) updates typing status → Should fail
  ```

---

## 7️⃣ Notifications Tests

### Read Operations
- [ ] ✅ **ALLOW**: User reads their own notifications
  ```
  User A reads notifications for User A → Should succeed
  ```

- [ ] ❌ **DENY**: User reads another user's notifications
  ```
  User A reads notifications for User B → Should fail
  ```

### Write Operations
- [ ] ✅ **ALLOW**: User updates their own notifications
  ```
  User A marks their notification as read → Should succeed
  ```

- [ ] ❌ **DENY**: User updates another user's notifications
  ```
  User A updates User B's notification → Should fail
  ```

---

## 8️⃣ Group Activities Tests

### Read Operations
- [ ] ✅ **ALLOW**: Member reads group activities
  ```
  User A (member) reads activities for Group X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-member reads group activities
  ```
  User C (not member) reads activities for Group X → Should fail
  ```

### Create Operations
- [ ] ✅ **ALLOW**: Member creates activity
  ```
  User A (member) creates activity in Group X → Should succeed
  ```

- [ ] ❌ **DENY**: Non-member creates activity
  ```
  User C (not member) creates activity in Group X → Should fail
  ```

### Update Operations
- [ ] ✅ **ALLOW**: Creator updates their activity
  ```
  User A updates activity created by User A → Should succeed
  ```

- [ ] ❌ **DENY**: Other user updates activity
  ```
  User B updates activity created by User A → Should fail
  ```

### Delete Operations
- [ ] ✅ **ALLOW**: Creator deletes their activity
  ```
  User A deletes activity created by User A → Should succeed
  ```

- [ ] ✅ **ALLOW**: Admin deletes any activity
  ```
  User B (admin) deletes activity created by User A → Should succeed
  ```

- [ ] ❌ **DENY**: Regular member deletes other's activity
  ```
  User C (member, not admin) deletes activity by User A → Should fail
  ```

---

## 🔧 Testing Methods

### Method 1: Firebase Console Rules Playground

1. Go to Firebase Console → Firestore → Rules
2. Click "Rules Playground" tab
3. Configure test:
   - Select collection path
   - Choose operation (get, list, create, update, delete)
   - Set authentication status
   - Set auth UID
   - Add document data if needed
4. Click "Run" to test
5. Verify expected result (Allow/Deny)

### Method 2: Firebase Emulator

```bash
# Start emulator
firebase emulators:start

# Access emulator UI
# Open http://localhost:4000
# Navigate to Firestore tab
# Test operations manually
```

### Method 3: App Testing

1. Build and run the app
2. Login with test user
3. Try various operations:
   - View profiles
   - Join groups
   - Create tasks
   - Send messages
   - Access other users' data
4. Monitor logcat for permission errors
5. Check Firebase Console for denied requests

### Method 4: Automated Tests (Advanced)

```javascript
// Example using @firebase/rules-unit-testing
const firebase = require('@firebase/rules-unit-testing');

describe('Firestore Security Rules', () => {
  it('allows user to read their own profile', async () => {
    const db = firebase.initializeTestApp({
      projectId: 'test-project',
      auth: { uid: 'user1' }
    }).firestore();
    
    const doc = db.collection('users').doc('user1');
    await firebase.assertSucceeds(doc.get());
  });
  
  it('denies user from updating another profile', async () => {
    const db = firebase.initializeTestApp({
      projectId: 'test-project',
      auth: { uid: 'user1' }
    }).firestore();
    
    const doc = db.collection('users').doc('user2');
    await firebase.assertFails(doc.update({ name: 'Hacked' }));
  });
});
```

---

## 📊 Test Results Template

Use this template to document your test results:

```
Date: _______________
Tester: _______________
Environment: [ ] Development [ ] Staging [ ] Production

Users Collection:
- Read (authenticated): [ ] Pass [ ] Fail
- Read (unauthenticated): [ ] Pass [ ] Fail
- Write (own): [ ] Pass [ ] Fail
- Write (other): [ ] Pass [ ] Fail

Groups Collection:
- Read (member): [ ] Pass [ ] Fail
- Read (non-member, private): [ ] Pass [ ] Fail
- Update (admin): [ ] Pass [ ] Fail
- Update (member): [ ] Pass [ ] Fail

Tasks Collection:
- Read (creator): [ ] Pass [ ] Fail
- Read (assigned): [ ] Pass [ ] Fail
- Read (unrelated): [ ] Pass [ ] Fail
- Delete (creator): [ ] Pass [ ] Fail

Chats Collection:
- Read (participant): [ ] Pass [ ] Fail
- Read (non-participant): [ ] Pass [ ] Fail

Messages Subcollection:
- Create (participant): [ ] Pass [ ] Fail
- Update (sender): [ ] Pass [ ] Fail
- Update (other): [ ] Pass [ ] Fail
- Delete (sender): [ ] Pass [ ] Fail

Overall Result: [ ] All Pass [ ] Some Failures

Notes:
_________________________________
_________________________________
```

---

## ✅ Success Criteria

All tests should pass with these results:
- ✅ All "ALLOW" scenarios succeed
- ❌ All "DENY" scenarios fail (as expected)
- No unexpected permission errors in app
- No security warnings in Firebase Console

---

## 🚨 If Tests Fail

1. **Review the rule** in `firestore.rules`
2. **Check helper functions** for logic errors
3. **Verify test data** exists in Firestore
4. **Confirm authentication** is working
5. **Re-deploy rules** after fixes
6. **Re-run tests** to verify

---

## 📝 Notes

- Test with multiple user accounts
- Test both authenticated and unauthenticated scenarios
- Test edge cases (empty arrays, missing fields)
- Document any unexpected behavior
- Update rules if legitimate use cases are blocked

---

## ✨ Testing Complete!

Once all tests pass, your Firestore security rules are properly configured and protecting your data.

**Next Step**: Monitor production usage for any permission issues.
