# Task 12: Task Reminder Notifications - Verification Checklist

## Pre-Testing Setup

### 1. Build and Install App
- [ ] Clean and rebuild the project
- [ ] Install app on test device or emulator
- [ ] Grant notification permission when prompted
- [ ] Ensure Firebase is properly configured

### 2. Verify WorkManager Setup
- [ ] Check that WorkManager dependency is in build.gradle
- [ ] Verify TaskReminderWorker is registered (automatic with WorkManager)
- [ ] Confirm notification channels are created

## Core Functionality Tests

### Test 1: Schedule Reminder for Future Task (>24 hours)
**Steps:**
1. Open the app and navigate to Tasks screen
2. Tap "+" to create a new task
3. Enter task details:
   - Title: "Test Reminder - Future"
   - Description: "This task is due in 3 days"
   - Due Date: 3 days from now
   - Priority: High
4. Save the task

**Expected Results:**
- [ ] Task is created successfully
- [ ] Reminder is scheduled for 24 hours before due date
- [ ] No immediate notification appears

**Verification:**
- [ ] Check WorkManager queue (use Android Studio's App Inspection or adb)
- [ ] Confirm work is scheduled with correct delay

### Test 2: Immediate Reminder for Soon-Due Task (<24 hours)
**Steps:**
1. Create a new task with due date in 12 hours
2. Enter task details:
   - Title: "Test Reminder - Soon"
   - Description: "This task is due soon"
   - Due Date: 12 hours from now
   - Priority: Medium

**Expected Results:**
- [ ] Task is created successfully
- [ ] Notification appears immediately (or within a few seconds)
- [ ] Notification shows task title, description, and due date
- [ ] Notification has "Mark Complete" and "View Task" buttons

### Test 3: No Reminder for Task Without Due Date
**Steps:**
1. Create a new task without setting a due date
2. Enter only title and description

**Expected Results:**
- [ ] Task is created successfully
- [ ] No reminder is scheduled
- [ ] No notification appears

### Test 4: Update Task Due Date (Reschedule)
**Steps:**
1. Create a task with due date 5 days from now
2. Wait a few seconds for reminder to be scheduled
3. Edit the task and change due date to 2 days from now
4. Save the changes

**Expected Results:**
- [ ] Task is updated successfully
- [ ] Old reminder is canceled
- [ ] New reminder is scheduled for 24 hours before new due date

### Test 5: Complete Task (Cancel Reminder)
**Steps:**
1. Create a task with due date 3 days from now
2. Wait for reminder to be scheduled
3. Mark the task as complete (swipe or tap complete button)

**Expected Results:**
- [ ] Task status changes to "Completed"
- [ ] Reminder is canceled
- [ ] No notification will appear at scheduled time

### Test 6: Delete Task (Cancel Reminder)
**Steps:**
1. Create a task with due date 3 days from now
2. Wait for reminder to be scheduled
3. Delete the task

**Expected Results:**
- [ ] Task is deleted successfully
- [ ] Reminder is canceled
- [ ] No notification will appear at scheduled time

## Notification Action Tests

### Test 7: "Mark Complete" Action Button
**Steps:**
1. Create a task with due date in 1 hour (to get immediate reminder)
2. Wait for notification to appear
3. Expand the notification
4. Tap "Mark Complete" button

**Expected Results:**
- [ ] App opens (if closed) or comes to foreground
- [ ] Task is marked as complete in Firestore
- [ ] Notification is dismissed
- [ ] Snackbar shows "Task marked as complete"
- [ ] Tasks screen shows updated task status

### Test 8: "View Task" Action Button
**Steps:**
1. Create a task with due date in 1 hour
2. Wait for notification to appear
3. Tap "View Task" button

**Expected Results:**
- [ ] App opens to Tasks screen
- [ ] Task list is visible
- [ ] User can see the task that triggered the notification

### Test 9: Tap Notification Body
**Steps:**
1. Create a task with due date in 1 hour
2. Wait for notification to appear
3. Tap the notification body (not action buttons)

**Expected Results:**
- [ ] App opens to Tasks screen
- [ ] Notification is dismissed

## Edge Case Tests

### Test 10: Task Already Completed When Reminder Fires
**Steps:**
1. Create a task with due date in 1 hour
2. Immediately mark the task as complete
3. Wait for the scheduled reminder time

**Expected Results:**
- [ ] No notification appears (worker checks task status)
- [ ] Reminder was canceled when task was completed

### Test 11: Task Deleted When Reminder Fires
**Steps:**
1. Create a task with due date in 1 hour
2. Delete the task
3. Wait for the scheduled reminder time

**Expected Results:**
- [ ] No notification appears (worker checks if task exists)
- [ ] Reminder was canceled when task was deleted

### Test 12: Multiple Task Reminders (Grouping)
**Steps:**
1. Create 3 tasks all due in 1 hour:
   - Task A: High priority
   - Task B: Medium priority
   - Task C: Low priority
2. Wait for notifications to appear

**Expected Results:**
- [ ] All 3 notifications appear
- [ ] Notifications are grouped together
- [ ] Summary notification shows "Task Reminders"
- [ ] Each notification shows correct priority emoji (🔴/🟡/🟢)
- [ ] Each notification has its own action buttons

### Test 13: Overdue Task (No Reminder)
**Steps:**
1. Create a task with due date in the past
2. Save the task

**Expected Results:**
- [ ] Task is created successfully
- [ ] Task shows as "Overdue" in the list
- [ ] No reminder is scheduled
- [ ] No notification appears

### Test 14: App Closed/Background
**Steps:**
1. Create a task with due date in 1 hour
2. Close the app completely (swipe away from recent apps)
3. Wait for reminder time

**Expected Results:**
- [ ] Notification appears even with app closed
- [ ] Tapping notification opens the app
- [ ] All actions work correctly

### Test 15: Device Restart
**Steps:**
1. Create a task with due date in 2 hours
2. Restart the device
3. Wait for reminder time

**Expected Results:**
- [ ] WorkManager persists across reboots
- [ ] Notification appears at scheduled time
- [ ] All functionality works correctly

## Notification Content Tests

### Test 16: Notification Content Verification
**Steps:**
1. Create a high-priority task with due date in 1 hour
2. Wait for notification

**Verify Notification Contains:**
- [ ] Small icon (app icon)
- [ ] Title: "🔴 Task Reminder: [Task Title]"
- [ ] Text: "Due: [Formatted Date]"
- [ ] Expanded view shows description
- [ ] Two action buttons visible
- [ ] Notification color matches task channel color (orange)

### Test 17: Priority Emoji Display
**Steps:**
1. Create 3 tasks due in 1 hour with different priorities
2. Wait for notifications

**Expected Results:**
- [ ] High priority shows 🔴 red circle
- [ ] Medium priority shows 🟡 yellow circle
- [ ] Low priority shows 🟢 green circle

### Test 18: Date Formatting
**Steps:**
1. Create a task with specific due date and time
2. Wait for notification

**Expected Results:**
- [ ] Date is formatted as "MMM dd, yyyy 'at' hh:mm a"
- [ ] Example: "Jan 15, 2025 at 02:30 PM"
- [ ] Time shows in 12-hour format with AM/PM

## Integration Tests

### Test 19: Real-Time Updates
**Steps:**
1. Create a task on Device A
2. Wait for reminder to be scheduled
3. Complete the task on Device B (different device, same account)

**Expected Results:**
- [ ] Task status syncs via Firestore
- [ ] Reminder on Device A is canceled (may require app restart)
- [ ] No notification appears on Device A

### Test 20: Notification Permission Denied
**Steps:**
1. Deny notification permission
2. Create a task with due date in 1 hour
3. Wait for reminder time

**Expected Results:**
- [ ] Task is created successfully
- [ ] Reminder is scheduled
- [ ] No notification appears (permission denied)
- [ ] App continues to function normally

## Performance Tests

### Test 21: Many Tasks
**Steps:**
1. Create 20 tasks with various due dates
2. Observe app performance

**Expected Results:**
- [ ] All reminders are scheduled successfully
- [ ] App remains responsive
- [ ] No memory issues
- [ ] WorkManager handles queue efficiently

### Test 22: Battery Optimization
**Steps:**
1. Enable battery optimization for the app
2. Create a task with due date in 2 hours
3. Lock device and wait

**Expected Results:**
- [ ] Notification may be delayed but eventually appears
- [ ] WorkManager respects battery optimization settings
- [ ] No app crashes or errors

## Cleanup Tests

### Test 23: Logout (Cancel All Reminders)
**Steps:**
1. Create several tasks with future due dates
2. Log out of the app
3. Wait for scheduled reminder times

**Expected Results:**
- [ ] All reminders should be canceled on logout (if implemented)
- [ ] No notifications appear after logout
- [ ] Clean state for next user

## Summary

### Test Results
- Total Tests: 23
- Passed: ___
- Failed: ___
- Skipped: ___

### Issues Found
1. 
2. 
3. 

### Notes
- 
- 
- 

### Sign-off
- [ ] All critical tests passed
- [ ] Known issues documented
- [ ] Ready for production

**Tester:** _______________
**Date:** _______________
**Device/Emulator:** _______________
**Android Version:** _______________
