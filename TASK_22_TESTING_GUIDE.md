# Task 22 Testing Guide: Task Details Navigation

## Pre-Testing Setup

### Prerequisites
1. App installed on device/emulator
2. User logged in with valid account
3. At least 3-5 tasks created with different:
   - Priorities (High, Medium, Low)
   - Statuses (Pending, Completed)
   - Categories (Personal, Group, Assignment)
   - Due dates (past, today, future)
   - Some with descriptions, some without
   - Some with subjects, some without

### Test Data Setup
Create the following test tasks:

1. **High Priority Overdue Task**
   - Title: "Submit Project Report"
   - Priority: High
   - Due Date: Yesterday
   - Status: Pending
   - Description: "Complete and submit the final project report"

2. **Medium Priority Task Due Today**
   - Title: "Team Meeting"
   - Priority: Medium
   - Due Date: Today
   - Status: Pending
   - Description: "Weekly team sync meeting"

3. **Low Priority Future Task**
   - Title: "Review Documentation"
   - Priority: Low
   - Due Date: Next week
   - Status: Pending
   - Description: ""

4. **Completed Task**
   - Title: "Code Review"
   - Priority: Medium
   - Due Date: Yesterday
   - Status: Completed
   - Description: "Review pull requests"

## Test Cases

### Test Case 1: Basic Navigation
**Objective:** Verify task details screen opens correctly

**Steps:**
1. Open the app and navigate to Calendar tab
2. Tap on a date that has tasks (should have colored dots)
3. Observe the task list below the calendar
4. Tap on any task in the list

**Expected Results:**
- ✅ Task details screen opens
- ✅ Toolbar shows "Task Details"
- ✅ Back arrow appears in toolbar
- ✅ Task information loads and displays

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 2: Task Information Display
**Objective:** Verify all task details display correctly

**Steps:**
1. Navigate to task details for "Submit Project Report"
2. Verify all information is displayed

**Expected Results:**
- ✅ Title: "Submit Project Report"
- ✅ Status: "! Overdue" in red
- ✅ Priority indicator: Red vertical bar
- ✅ Description: "Complete and submit the final project report"
- ✅ Category: "Personal" (or appropriate category)
- ✅ Priority: "High" in red text
- ✅ Due Date: Yesterday's date formatted correctly
- ✅ Created Date: Shows date and time

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 3: Priority Color Coding
**Objective:** Verify priority colors are correct

**Test 3a: High Priority**
1. Open a high priority task
2. Check priority indicator color

**Expected:** Red (#F44336) vertical bar

**Test 3b: Medium Priority**
1. Open a medium priority task
2. Check priority indicator color

**Expected:** Orange (#FF9800) vertical bar

**Test 3c: Low Priority**
1. Open a low priority task
2. Check priority indicator color

**Expected:** Green (#4CAF50) vertical bar

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 4: Status Display
**Objective:** Verify status displays correctly for different states

**Test 4a: Pending Task**
1. Open a pending task
2. Check status display

**Expected:**
- ✅ Status: "○ Pending" in orange
- ✅ "Mark as Complete" button visible

**Test 4b: Completed Task**
1. Open a completed task
2. Check status display

**Expected:**
- ✅ Status: "✓ Completed" in green
- ✅ "Mark as Complete" button hidden

**Test 4c: Overdue Task**
1. Open an overdue task
2. Check status display

**Expected:**
- ✅ Status: "! Overdue" in red
- ✅ "Mark as Complete" button visible

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 5: Empty Fields Handling
**Objective:** Verify graceful handling of missing data

**Steps:**
1. Open a task with no description
2. Check description field
3. Open a task with no subject
4. Check subject field

**Expected Results:**
- ✅ No description: Shows "No description provided" in gray
- ✅ No subject: Subject row is hidden
- ✅ No due date: Shows "No due date"

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 6: Mark as Complete - Success Flow
**Objective:** Verify task can be marked as complete

**Steps:**
1. Open a pending task
2. Tap "Mark as Complete" button
3. Observe confirmation dialog
4. Tap "Complete" in dialog
5. Wait for operation to complete
6. Observe UI changes
7. Press back to return to calendar
8. Check calendar display

**Expected Results:**
- ✅ Confirmation dialog appears with message
- ✅ Dialog has "Complete" and "Cancel" buttons
- ✅ Loading indicator shows during operation
- ✅ Toast message: "Task marked as complete"
- ✅ Status updates to "✓ Completed" in green
- ✅ "Mark as Complete" button disappears
- ✅ Calendar refreshes on return
- ✅ Task still appears in list (if filtering allows)

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 7: Mark as Complete - Cancel
**Objective:** Verify canceling mark as complete works

**Steps:**
1. Open a pending task
2. Tap "Mark as Complete" button
3. Tap "Cancel" in confirmation dialog

**Expected Results:**
- ✅ Dialog closes
- ✅ Task remains pending
- ✅ No changes to task status
- ✅ No toast message

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 8: Delete Task - Success Flow
**Objective:** Verify task can be deleted

**Steps:**
1. Open any task
2. Scroll down to "Delete Task" button
3. Tap "Delete Task" button
4. Observe confirmation dialog
5. Tap "Delete" in dialog
6. Wait for operation to complete
7. Observe result

**Expected Results:**
- ✅ Confirmation dialog appears with warning message
- ✅ Dialog has "Delete" and "Cancel" buttons
- ✅ Loading indicator shows during operation
- ✅ Toast message: "Task deleted successfully"
- ✅ Returns to calendar automatically
- ✅ Calendar refreshes
- ✅ Task no longer appears in task list
- ✅ Calendar dot may disappear if no other tasks on that date

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 9: Delete Task - Cancel
**Objective:** Verify canceling delete works

**Steps:**
1. Open any task
2. Tap "Delete Task" button
3. Tap "Cancel" in confirmation dialog

**Expected Results:**
- ✅ Dialog closes
- ✅ Task remains in database
- ✅ Still on task details screen
- ✅ No toast message

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 10: Edit Task Button
**Objective:** Verify edit button is present (placeholder)

**Steps:**
1. Open any task
2. Tap "Edit Task" button

**Expected Results:**
- ✅ Button is visible and enabled
- ✅ Toast message: "Edit functionality coming soon"
- ✅ Remains on task details screen

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 11: Back Navigation
**Objective:** Verify back navigation works correctly

**Test 11a: Toolbar Back Button**
1. Open task details
2. Tap back arrow in toolbar

**Expected:** Returns to calendar

**Test 11b: System Back Button**
1. Open task details
2. Press device back button

**Expected:** Returns to calendar

**Test 11c: After Modification**
1. Open task details
2. Mark as complete
3. Press back

**Expected:** Returns to calendar with refreshed data

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 12: Loading States
**Objective:** Verify loading indicators work correctly

**Steps:**
1. Open task details (observe initial load)
2. Mark task as complete (observe operation load)
3. Try to tap buttons during loading

**Expected Results:**
- ✅ Progress bar shows during initial load
- ✅ Progress bar shows during mark complete
- ✅ Progress bar shows during delete
- ✅ Buttons disabled during loading
- ✅ Cannot trigger duplicate operations

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 13: Error Handling
**Objective:** Verify error scenarios are handled gracefully

**Test 13a: Invalid Task ID**
1. Manually create intent with invalid task ID
2. Launch TaskDetailsActivity

**Expected:**
- ✅ Error toast appears
- ✅ Activity closes automatically

**Test 13b: Network Error (Airplane Mode)**
1. Enable airplane mode
2. Open task details
3. Try to mark as complete

**Expected:**
- ✅ Error message displays
- ✅ User can retry or go back

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 14: Calendar Refresh Integration
**Objective:** Verify calendar updates after task modifications

**Steps:**
1. Note current tasks on a specific date
2. Open one of those tasks
3. Mark it as complete
4. Return to calendar
5. Observe the task list for that date

**Expected Results:**
- ✅ Task list refreshes automatically
- ✅ Completed task shows updated status
- ✅ Calendar dots update if needed
- ✅ Filter settings maintained

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 15: Multiple Task Types
**Objective:** Verify details work for all task types

**Test 15a: Personal Task**
1. Open a personal task
2. Verify category shows "Personal"

**Test 15b: Group Task**
1. Open a group task
2. Verify category shows "Group"
3. Verify subject displays if present

**Test 15c: Assignment Task**
1. Open an assignment task
2. Verify category shows "Assignment"
3. Verify subject displays

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 16: Date Formatting
**Objective:** Verify dates display in correct format

**Steps:**
1. Open various tasks with different dates
2. Check date formatting

**Expected Results:**
- ✅ Due Date: "MMM dd, yyyy" (e.g., "Dec 31, 2024")
- ✅ Created Date: "MMM dd, yyyy 'at' hh:mm a" (e.g., "Dec 1, 2024 at 02:30 PM")
- ✅ Dates are in local timezone
- ✅ Dates are readable and consistent

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 17: UI Responsiveness
**Objective:** Verify UI is responsive and smooth

**Steps:**
1. Open task details
2. Scroll through content
3. Tap buttons
4. Navigate back and forth multiple times

**Expected Results:**
- ✅ Smooth scrolling
- ✅ No lag or stuttering
- ✅ Buttons respond immediately
- ✅ Transitions are smooth
- ✅ No visual glitches

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

### Test Case 18: Accessibility
**Objective:** Verify accessibility features work

**Steps:**
1. Enable TalkBack
2. Navigate to task details
3. Try to interact with all elements

**Expected Results:**
- ✅ All text is readable by TalkBack
- ✅ Buttons have proper content descriptions
- ✅ Navigation is logical
- ✅ Color contrast is sufficient

**Status:** ⬜ Pass / ⬜ Fail

**Notes:**
_______________________________________

---

## Checkpoint 4 Verification

### ✅ Checkpoint 4: Test Calendar
Complete the following verification steps:

- [ ] Run app and open calendar screen
- [ ] Verify current month is displayed
- [ ] Verify dates with tasks show colored dots
- [ ] Tap a date and verify tasks for that date appear below
- [ ] **Tap a task and verify details screen opens** ⭐ NEW
- [ ] Swipe to change months
- [ ] Test filter chips (All, My Tasks, Group)
- [ ] Create a new task with due date and verify calendar updates

**Overall Status:** ⬜ Pass / ⬜ Fail

---

## Bug Report Template

If you encounter any issues, use this template:

**Bug ID:** _______
**Test Case:** _______
**Severity:** ⬜ Critical / ⬜ High / ⬜ Medium / ⬜ Low

**Description:**
_______________________________________

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Result:**
_______________________________________

**Actual Result:**
_______________________________________

**Screenshots/Logs:**
_______________________________________

**Device Info:**
- Device: _______
- Android Version: _______
- App Version: _______

---

## Test Summary

**Date:** _______
**Tester:** _______
**Build Version:** _______

**Results:**
- Total Test Cases: 18
- Passed: _______
- Failed: _______
- Blocked: _______
- Not Tested: _______

**Pass Rate:** _______%

**Critical Issues Found:**
_______________________________________

**Recommendations:**
_______________________________________

**Sign-off:** ⬜ Approved / ⬜ Needs Fixes

---

## Notes for Testers

### Tips
1. Test with various task types and states
2. Try edge cases (very long titles, no description, etc.)
3. Test on different screen sizes if possible
4. Check both light and dark mode if supported
5. Test with slow network to see loading states

### Common Issues to Watch For
- Task not loading
- Calendar not refreshing after changes
- Buttons not responding
- Incorrect date formatting
- Missing error messages
- Memory leaks (test by opening/closing many times)

### Performance Checks
- Task details should load within 1-2 seconds
- Mark as complete should complete within 1-2 seconds
- Delete should complete within 1-2 seconds
- No noticeable lag when scrolling
- No crashes or ANRs

---

## Automated Testing Recommendations

For future automated testing, consider:

1. **Unit Tests:**
   - Task loading logic
   - Date formatting
   - Status determination

2. **Integration Tests:**
   - TaskRepository operations
   - Activity result handling

3. **UI Tests:**
   - Navigation flow
   - Button clicks
   - Dialog interactions
   - Data display

---

**End of Testing Guide**
