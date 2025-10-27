# Task 10: Manual Testing Guide for Firestore Permissions Fix

## Overview
This guide provides step-by-step instructions for manually testing the app with updated Firestore rules to ensure all functionality works correctly and error messages are user-friendly.

**Requirements Tested:** 1.1, 4.5, 5.4

## Prerequisites
- App installed on device or emulator
- User account created and logged in
- Firebase project with updated rules deployed
- Internet connection active

---

## Test 1: Groups Screen Navigation

### Objective
Verify that navigating to the Groups screen does not crash the app.

### Steps
1. Launch the app
2. Log in with valid credentials
3. Navigate to the Groups screen (tap Groups tab/button)
4. Wait for data to load

### Expected Results
✅ App does not crash
✅ Groups screen loads successfully
✅ Either groups are displayed OR empty state is shown
✅ No permission error dialogs appear
✅ Loading indicator appears briefly then disappears

### Error Scenarios to Check
- If no groups exist: Should show empty state message
- If network error: Should show network error message (not permission error)
- If permission error: Should show user-friendly message, not crash

### Pass Criteria
- [ ] App navigates to Groups screen without crashing
- [ ] No "PERMISSION_DENIED" errors visible
- [ ] Appropriate UI state displayed (loading/data/empty)

---

## Test 2: Creating a New Group

### Objective
Verify that users can create new groups and are automatically added as members.

### Steps
1. Navigate to Groups screen
2. Tap "Create Group" or "+" button
3. Enter group details:
   - Name: "Test Group [timestamp]"
   - Description: "Testing permissions fix"
4. Tap "Create" or "Save"
5. Wait for group creation to complete

### Expected Results
✅ Group creation dialog/screen appears
✅ Form accepts input without errors
✅ Group is created successfully
✅ Success message or toast appears
✅ New group appears in groups list
✅ User is automatically a member of the group

### Error Scenarios to Check
- If creation fails: Should show clear error message
- If network error: Should indicate network issue
- If validation error: Should show which field is invalid

### Pass Criteria
- [ ] Group creation completes successfully
- [ ] New group appears in user's groups list
- [ ] No permission errors during creation
- [ ] User can immediately access the created group

---

## Test 3: Viewing Group Activities

### Objective
Verify that users can view activities for groups they belong to.

### Steps
1. Navigate to Groups screen
2. Select a group you are a member of
3. Navigate to group details or activities section
4. Observe the activities list

### Expected Results
✅ Group details screen opens
✅ Activities section loads without errors
✅ Activities are displayed (if any exist)
✅ Empty state shown if no activities
✅ No permission errors

### Activities to Look For
- Group creation activity
- Member join/leave activities
- Task creation activities
- Any other group-related activities

### Pass Criteria
- [ ] Activities load without crashing
- [ ] No permission denied errors
- [ ] Activities display correctly or empty state shown
- [ ] Can scroll through activities if multiple exist

---

## Test 4: Tasks Screen Functionality

### Objective
Verify that the Tasks screen works correctly with proper permission filtering.

### Steps
1. Navigate to Tasks screen
2. Observe task list loading
3. Try to create a new task:
   - Title: "Test Task [timestamp]"
   - Description: "Testing permissions"
   - Due date: Tomorrow
4. Save the task
5. Verify task appears in list

### Expected Results
✅ Tasks screen loads without crashing
✅ User's tasks are displayed
✅ Can create new tasks
✅ Can view task details
✅ Can update task status
✅ No permission errors

### Test Both Query Types
- **Own Tasks**: Tasks created by the user
- **Assigned Tasks**: Tasks assigned to the user by others

### Pass Criteria
- [ ] Tasks screen loads successfully
- [ ] Can view own tasks
- [ ] Can view assigned tasks
- [ ] Can create new tasks
- [ ] No permission errors occur

---

## Test 5: Chat Functionality

### Objective
Verify that chat functionality works without permission errors.

### Steps
1. Navigate to Chat screen
2. Observe chat list loading
3. Select an existing chat (if any)
4. Try to send a message
5. Verify message appears

### Alternative Test (if no chats exist)
1. Create a new group
2. Navigate to group chat
3. Send a test message
4. Verify message delivery

### Expected Results
✅ Chat screen loads without crashing
✅ Chat list displays correctly
✅ Can open individual chats
✅ Can send messages
✅ Messages appear in chat
✅ No permission errors

### Pass Criteria
- [ ] Chat screen loads successfully
- [ ] Can view chat list
- [ ] Can open and view chat messages
- [ ] Can send new messages
- [ ] No permission errors occur

---

## Test 6: Error Message User-Friendliness

### Objective
Verify that error messages are clear and actionable, not technical.

### Steps
1. Test various error scenarios:
   - Turn off internet connection
   - Try to access data while offline
   - Try to perform actions with poor connectivity
2. Observe error messages displayed

### Expected Error Messages (Examples)
✅ "Unable to load groups. Please check your connection."
✅ "Failed to create group. Please try again."
✅ "You don't have permission to access this data."
❌ NOT: "PERMISSION_DENIED: Missing or insufficient permissions"
❌ NOT: "FirebaseFirestoreException: Code 7"

### Pass Criteria
- [ ] Error messages are in plain language
- [ ] Errors provide actionable guidance
- [ ] No technical error codes shown to users
- [ ] App doesn't crash on errors

---

## Test 7: Edge Cases and Stress Testing

### Objective
Verify app handles edge cases gracefully.

### Test Scenarios

#### A. Empty States
1. New user with no groups
2. User with no tasks
3. User with no chats
- **Expected**: Appropriate empty state messages, no crashes

#### B. Large Data Sets
1. User with many groups (10+)
2. User with many tasks (20+)
3. Group with many activities (50+)
- **Expected**: Data loads correctly, pagination works, no performance issues

#### C. Rapid Navigation
1. Quickly switch between tabs
2. Rapidly open and close groups
3. Navigate back and forth between screens
- **Expected**: No crashes, data loads correctly, no permission errors

#### D. Concurrent Operations
1. Create group while loading tasks
2. Send message while creating task
3. Update profile while viewing groups
- **Expected**: All operations complete successfully

### Pass Criteria
- [ ] All edge cases handled gracefully
- [ ] No crashes in any scenario
- [ ] Appropriate feedback for all states
- [ ] Performance remains acceptable

---

## Test 8: Permission Boundary Testing

### Objective
Verify that permission rules work correctly at boundaries.

### Test Scenarios

#### A. Group Membership
1. Create a group (you're owner and member)
2. Have another user try to access your group
- **Expected**: You can access, they cannot (unless added as member)

#### B. Task Assignment
1. Create a task assigned to yourself
2. Create a task assigned to another user
- **Expected**: You can see both, other user sees only theirs

#### C. Chat Participation
1. Create a group chat
2. Send messages
3. Have non-member try to access
- **Expected**: Only participants can access chat

### Pass Criteria
- [ ] Permissions correctly restrict access
- [ ] No unauthorized data access
- [ ] Proper error handling for denied access
- [ ] No data leakage

---

## Test 9: Update and Delete Operations

### Objective
Verify that update and delete operations work correctly.

### Steps

#### Update Tests
1. Update group name/description
2. Update task status
3. Update profile information
- **Expected**: All updates succeed for owned resources

#### Delete Tests
1. Delete a task you created
2. Try to delete a group you own
3. Try to delete a task assigned to you (but not owned)
- **Expected**: Can delete owned resources, appropriate errors for others

### Pass Criteria
- [ ] Can update owned resources
- [ ] Can delete owned resources
- [ ] Appropriate errors for unauthorized operations
- [ ] No permission errors for valid operations

---

## Test 10: Cross-Feature Integration

### Objective
Verify that features work together correctly.

### Test Flow
1. Create a group
2. Create a task in that group
3. Assign task to group member
4. Send chat message about the task
5. View group activities
6. Complete the task
7. Verify activity logged

### Expected Results
✅ All operations complete successfully
✅ Data consistency across features
✅ Activities logged correctly
✅ No permission errors
✅ Real-time updates work

### Pass Criteria
- [ ] Complete workflow executes without errors
- [ ] Data remains consistent
- [ ] All features integrate properly
- [ ] No crashes or permission issues

---

## Regression Testing Checklist

Verify that existing functionality still works:

- [ ] User login/logout
- [ ] Profile viewing/editing
- [ ] Image upload
- [ ] Notifications
- [ ] Search functionality
- [ ] Filters and sorting
- [ ] Dark mode toggle
- [ ] Settings changes
- [ ] Offline mode
- [ ] Data synchronization

---

## Performance Verification

Monitor app performance during testing:

### Metrics to Check
- **Load Times**: Groups/Tasks/Chats should load in < 2 seconds
- **Response Time**: Actions should complete in < 1 second
- **Memory Usage**: Should remain stable, no memory leaks
- **Battery Usage**: Should not drain excessively
- **Network Usage**: Should be reasonable, no excessive queries

### Tools
- Android Studio Profiler
- Firebase Performance Monitoring
- Device battery stats

### Pass Criteria
- [ ] Load times acceptable
- [ ] No performance degradation
- [ ] Memory usage stable
- [ ] No excessive network calls

---

## Error Logging Verification

### Check Logs For
1. Open Android Studio Logcat
2. Filter by app package name
3. Look for:
   - ✅ Successful operations logged
   - ✅ Errors logged with context
   - ❌ No PERMISSION_DENIED errors
   - ❌ No unhandled exceptions

### Log Levels to Check
- **ERROR**: Should only contain handled errors
- **WARN**: Should contain warnings, not crashes
- **INFO**: Should contain operation logs
- **DEBUG**: Should contain detailed debug info

### Pass Criteria
- [ ] No permission denied errors in logs
- [ ] All errors are handled gracefully
- [ ] Logging provides useful debugging info
- [ ] No crash logs

---

## Final Verification Checklist

Before marking task as complete, verify:

### Functionality
- [ ] All screens load without crashing
- [ ] All CRUD operations work correctly
- [ ] All queries return appropriate results
- [ ] No permission errors occur

### User Experience
- [ ] Error messages are user-friendly
- [ ] Loading states are clear
- [ ] Empty states are informative
- [ ] Success feedback is provided

### Technical
- [ ] No crashes in logs
- [ ] No permission denied errors
- [ ] Performance is acceptable
- [ ] Memory usage is stable

### Requirements Coverage
- [ ] Requirement 1.1: Users can access group data without errors ✓
- [ ] Requirement 4.5: App handles operations correctly ✓
- [ ] Requirement 5.4: Error messages are user-friendly ✓

---

## Reporting Issues

If any test fails, document:

1. **Test Name**: Which test failed
2. **Steps to Reproduce**: Exact steps taken
3. **Expected Result**: What should happen
4. **Actual Result**: What actually happened
5. **Error Messages**: Any errors displayed
6. **Logs**: Relevant log entries
7. **Screenshots**: Visual evidence if applicable
8. **Device Info**: Device model, Android version, app version

---

## Success Criteria Summary

Task 10 is complete when:

✅ All manual tests pass
✅ No permission errors occur
✅ No app crashes
✅ Error messages are user-friendly
✅ All features work as expected
✅ Performance is acceptable
✅ Integration tests pass
✅ Regression tests pass

---

## Next Steps

After completing all tests:

1. Document any issues found
2. Verify fixes for any issues
3. Update test documentation
4. Mark task as complete
5. Proceed to Task 11: Monitor Production Metrics
