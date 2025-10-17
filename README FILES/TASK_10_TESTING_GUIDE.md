# Task 10: Group Creation and Display - Testing Guide

## Automated Tests

### Run All Tests
```bash
./gradlew test --tests "com.example.loginandregistration.GroupCreationAndDisplayTest"
```

### Test Coverage

#### 1. Field Name Verification
**Test**: `test FirebaseGroup has correct field names`
- Verifies `memberIds` field exists
- Verifies `members` field exists
- Checks both arrays are populated correctly

#### 2. Group Creation
**Test**: `test group creation initializes all required fields`
- Verifies all required fields are set
- Checks owner is in `memberIds`
- Validates owner role is "owner"

#### 3. Public/Private Groups
**Test**: `test group settings isPublic field`
- Tests public group has `isPublic = true`
- Tests private group has `isPublic = false`

#### 4. Array Synchronization
**Test**: `test memberIds and members arrays stay in sync`
- Verifies both arrays have same size
- Checks all memberIds have corresponding members
- Validates all members have corresponding memberIds

#### 5. Member Roles
**Test**: `test group member roles`
- Validates "owner" role
- Validates "admin" role
- Validates "member" role

#### 6. Firestore Compatibility
**Test**: `test group no-arg constructor for Firestore`
- Verifies no-arg constructor exists
- Checks default values are correct

#### 7. Join Code Format
**Test**: `test join code format`
- Validates 6-character length
- Checks alphanumeric format

## Manual Testing

### Test 1: Create a New Group

**Steps**:
1. Open the app and sign in
2. Navigate to Groups tab
3. Click "Create Group" button
4. Fill in the form:
   - Name: "Test Group 1"
   - Description: "Testing group creation"
   - Subject: "Computer Science"
5. Click "Create"

**Expected Results**:
- ✅ Success message appears
- ✅ Dialog closes
- ✅ Group appears immediately in "My Groups" section
- ✅ Group shows correct member count (1)
- ✅ Group shows correct subject

**Verification**:
- Check Firestore console:
  - Group document exists
  - `memberIds` array contains your user ID
  - `members` array contains your user details
  - `owner` field is your user ID
  - `settings.isPublic` is set correctly
  - `isActive` is true

### Test 2: Join Group by Code

**Prerequisites**:
- Have another user create a group and share the join code
- OR use an existing group's join code

**Steps**:
1. Click "Join Group" button
2. Enter the 6-character join code
3. Click "Join"

**Expected Results**:
- ✅ Success message appears
- ✅ Dialog closes
- ✅ Group appears immediately in "My Groups" section
- ✅ Member count increases by 1

**Verification**:
- Check Firestore console:
  - Your user ID is in `memberIds` array
  - Your user details are in `members` array
  - Your role is "member"

### Test 3: View Public Groups

**Prerequisites**:
- Have at least one public group you're not a member of

**Steps**:
1. Scroll to "Discover Groups" section
2. Observe the list of public groups

**Expected Results**:
- ✅ Public groups are displayed
- ✅ Groups you're already a member of are NOT shown
- ✅ Each group shows member count and subject

**Verification**:
- Check Firestore console:
  - Query for `settings.isPublic == true` returns groups
  - Your user ID is NOT in their `memberIds` arrays

### Test 4: Join Public Group

**Steps**:
1. In "Discover Groups" section, click "Join" on a group
2. Wait for confirmation

**Expected Results**:
- ✅ Success message appears
- ✅ Group moves from "Discover Groups" to "My Groups"
- ✅ Group appears immediately without refresh

**Verification**:
- Check Firestore console:
  - Your user ID is now in `memberIds` array
  - Your user details are in `members` array

### Test 5: Real-time Updates

**Prerequisites**:
- Two devices or users

**Steps**:
1. User A creates a group
2. User A shares the join code with User B
3. User B joins the group using the code
4. User A observes their screen (without refreshing)

**Expected Results**:
- ✅ User A sees member count increase automatically
- ✅ No manual refresh needed
- ✅ Update happens within 1-2 seconds

### Test 6: Error Handling - Invalid Join Code

**Steps**:
1. Click "Join Group" button
2. Enter an invalid code (e.g., "XXXXXX")
3. Click "Join"

**Expected Results**:
- ✅ Error message appears
- ✅ Message is user-friendly
- ✅ Dialog remains open for retry

### Test 7: Error Handling - Empty Group Name

**Steps**:
1. Click "Create Group" button
2. Leave name field empty
3. Fill in other fields
4. Click "Create"

**Expected Results**:
- ✅ Validation error appears
- ✅ Message indicates name is required
- ✅ Dialog remains open for correction

### Test 8: Offline Behavior

**Steps**:
1. Turn off device internet connection
2. Try to create a group
3. Turn internet back on

**Expected Results**:
- ✅ Offline indicator appears
- ✅ Error message indicates no connection
- ✅ When online, retry works correctly

## Security Rules Testing

### Test 1: Member Can Read Group
```javascript
// In Firestore Rules Playground
match /groups/{groupId} {
  allow read: if request.auth.uid in resource.data.memberIds;
}

// Test with:
// - Auth: User ID that's in memberIds
// - Expected: ALLOW
```

### Test 2: Non-member Cannot Read Private Group
```javascript
// Test with:
// - Auth: User ID NOT in memberIds
// - Group: settings.isPublic = false
// - Expected: DENY
```

### Test 3: Anyone Can Read Public Group
```javascript
// Test with:
// - Auth: Any authenticated user
// - Group: settings.isPublic = true
// - Expected: ALLOW
```

### Test 4: Authenticated User Can Create Group
```javascript
// Test with:
// - Auth: Any authenticated user
// - Operation: Create group with user in memberIds
// - Expected: ALLOW
```

### Test 5: Admin Can Update Group
```javascript
// Test with:
// - Auth: User with role "admin" or "owner"
// - Operation: Update group details
// - Expected: ALLOW
```

### Test 6: Member Cannot Update Group
```javascript
// Test with:
// - Auth: User with role "member"
// - Operation: Update group details
// - Expected: DENY
```

## Performance Testing

### Test 1: Large Member List
**Steps**:
1. Create a group with 50 members
2. Observe load time and UI responsiveness

**Expected**:
- ✅ List loads in < 2 seconds
- ✅ Scrolling is smooth
- ✅ No UI lag

### Test 2: Many Groups
**Steps**:
1. Join 20+ groups
2. Navigate to Groups tab
3. Observe load time

**Expected**:
- ✅ Groups load in < 3 seconds
- ✅ Real-time updates don't cause lag
- ✅ UI remains responsive

## Regression Testing

After making changes, verify:
- ✅ Existing groups still display correctly
- ✅ Group chat still works
- ✅ Group tasks still work
- ✅ Group activities still log correctly
- ✅ Group settings can still be updated

## Test Results Template

```
Test Date: ___________
Tester: ___________
Device: ___________
OS Version: ___________

| Test Case | Status | Notes |
|-----------|--------|-------|
| Create Group | ☐ Pass ☐ Fail | |
| Join by Code | ☐ Pass ☐ Fail | |
| View Public Groups | ☐ Pass ☐ Fail | |
| Join Public Group | ☐ Pass ☐ Fail | |
| Real-time Updates | ☐ Pass ☐ Fail | |
| Error Handling | ☐ Pass ☐ Fail | |
| Offline Behavior | ☐ Pass ☐ Fail | |
| Security Rules | ☐ Pass ☐ Fail | |

Overall Result: ☐ Pass ☐ Fail

Issues Found:
1. ___________
2. ___________
3. ___________
```

## Troubleshooting

### Issue: Groups don't appear after creation
**Check**:
1. Firestore console - is group created?
2. Is `memberIds` array populated?
3. Are security rules deployed?
4. Check app logs for errors

### Issue: Permission denied errors
**Check**:
1. Are security rules deployed?
2. Is user authenticated?
3. Is user in `memberIds` array?
4. Check Firestore Rules Playground

### Issue: Real-time updates not working
**Check**:
1. Is internet connection stable?
2. Are listeners properly attached?
3. Check for listener cleanup in onStop()
4. Verify Flow collection is lifecycle-aware

### Issue: Public groups not showing
**Check**:
1. Query uses `settings.isPublic == true`
2. Groups have `isActive == true`
3. User is not already a member
4. Security rules allow reading public groups
