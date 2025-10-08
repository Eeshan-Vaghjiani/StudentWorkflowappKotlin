# Task 20 Testing Guide: Calendar Integration

## Pre-Testing Setup

### 1. Ensure Test Data Exists
Before testing, make sure you have:
- At least one user account logged in
- Several tasks created with different due dates
- Tasks with various priorities (Low, Medium, High)
- Tasks with different statuses (Pending, Completed)
- Tasks spread across multiple dates

### 2. Build and Install
```bash
./gradlew assembleDebug
# Install on device or emulator
```

## Manual Testing Checklist

### Calendar Display Tests

#### Test 1: Initial Calendar Load
**Steps:**
1. Open the app
2. Navigate to Calendar tab/screen
3. Observe the calendar view

**Expected Results:**
- ✅ Calendar displays current month
- ✅ Month/year shown in header (e.g., "January 2025")
- ✅ Day of week headers visible (Sun, Mon, Tue, etc.)
- ✅ All dates for current month are visible
- ✅ Today's date has a border highlight
- ✅ Dates with tasks show blue dots below the number

#### Test 2: Today's Date Highlighting
**Steps:**
1. Open calendar screen
2. Locate today's date

**Expected Results:**
- ✅ Today's date has a circular border
- ✅ Border color matches app's primary color
- ✅ Date number is clearly visible
- ✅ If today has tasks, blue dot is visible

#### Test 3: Task Indicators
**Steps:**
1. Create tasks with due dates on different days
2. Open calendar screen
3. Check dates with tasks

**Expected Results:**
- ✅ Blue dots appear below dates that have tasks
- ✅ Dates without tasks have no dots
- ✅ Dots are visible and properly sized
- ✅ Multiple tasks on same date show single dot

### Date Selection Tests

#### Test 4: Select a Date
**Steps:**
1. Open calendar screen
2. Tap on any date in the current month
3. Observe the changes

**Expected Results:**
- ✅ Selected date gets filled circle background
- ✅ Date number turns white
- ✅ Selected date label updates below calendar
- ✅ Task list updates to show tasks for that date
- ✅ Previous selection is deselected

#### Test 5: Select Date with Tasks
**Steps:**
1. Tap on a date that has a blue dot
2. Observe the task list below

**Expected Results:**
- ✅ Tasks for that date appear in the list
- ✅ Each task shows title, category, priority, status
- ✅ Priority indicator bar shows correct color
- ✅ Status icon and text are correct
- ✅ Tasks are displayed in cards with proper spacing

#### Test 6: Select Date without Tasks
**Steps:**
1. Tap on a date with no blue dot
2. Observe the task list area

**Expected Results:**
- ✅ Empty state view appears
- ✅ Calendar emoji (📅) is visible
- ✅ "No tasks for this date" message shows
- ✅ Task list is hidden

### Month Navigation Tests

#### Test 7: Navigate to Next Month
**Steps:**
1. Open calendar screen
2. Tap the next month button (right arrow)
3. Observe the calendar

**Expected Results:**
- ✅ Calendar smoothly scrolls to next month
- ✅ Month/year header updates
- ✅ New month's dates are displayed
- ✅ Task dots update for new month
- ✅ Selected date remains or resets appropriately

#### Test 8: Navigate to Previous Month
**Steps:**
1. Open calendar screen
2. Tap the previous month button (left arrow)
3. Observe the calendar

**Expected Results:**
- ✅ Calendar smoothly scrolls to previous month
- ✅ Month/year header updates
- ✅ Previous month's dates are displayed
- ✅ Task dots update for previous month

#### Test 9: Navigate Multiple Months
**Steps:**
1. Navigate forward 3 months
2. Navigate backward 5 months
3. Return to current month

**Expected Results:**
- ✅ Navigation works smoothly in both directions
- ✅ No lag or performance issues
- ✅ Task dots update correctly for each month
- ✅ Can return to current month easily

### Task Display Tests

#### Test 10: Task Priority Colors
**Steps:**
1. Create tasks with different priorities
2. Select a date with multiple priority tasks
3. Observe the priority indicators

**Expected Results:**
- ✅ High priority: Red vertical bar
- ✅ Medium priority: Orange vertical bar
- ✅ Low priority: Green vertical bar
- ✅ Colors are clearly distinguishable

#### Test 11: Task Status Display
**Steps:**
1. Create tasks with different statuses
2. Select a date with various status tasks
3. Check status indicators

**Expected Results:**
- ✅ Completed: "✓ Completed" in green
- ✅ Pending: "○ Pending" in orange
- ✅ Overdue: "! Overdue" in red
- ✅ Status text is readable and properly colored

#### Test 12: Task Category Display
**Steps:**
1. Create tasks with different categories
2. Select a date with various category tasks
3. Check category badges

**Expected Results:**
- ✅ Category name displayed (Personal, Group, Assignment)
- ✅ Badge has rounded background
- ✅ Text is capitalized
- ✅ Badge is clearly visible

### Real-Time Update Tests

#### Test 13: Create New Task
**Steps:**
1. Open calendar screen
2. Note a date without tasks
3. Create a new task with that date as due date
4. Return to calendar screen

**Expected Results:**
- ✅ Blue dot appears on the new task's date
- ✅ If date is selected, task appears in list
- ✅ Update happens automatically (no manual refresh needed)

#### Test 14: Complete a Task
**Steps:**
1. Select a date with pending tasks
2. Mark a task as completed (from task details)
3. Return to calendar

**Expected Results:**
- ✅ Task status updates to "✓ Completed"
- ✅ Status color changes to green
- ✅ Task remains visible on that date

#### Test 15: Delete a Task
**Steps:**
1. Select a date with one task
2. Delete that task
3. Return to calendar

**Expected Results:**
- ✅ Blue dot disappears from that date
- ✅ Empty state appears if date is selected
- ✅ Task list updates automatically

### Edge Case Tests

#### Test 16: Month with No Tasks
**Steps:**
1. Navigate to a future month with no tasks
2. Observe the calendar

**Expected Results:**
- ✅ Calendar displays normally
- ✅ No blue dots appear
- ✅ All dates are selectable
- ✅ Empty state shows when any date selected

#### Test 17: Date with Many Tasks
**Steps:**
1. Create 10+ tasks for the same date
2. Select that date
3. Scroll through task list

**Expected Results:**
- ✅ All tasks are displayed
- ✅ List scrolls smoothly
- ✅ No performance issues
- ✅ Single blue dot (not multiple)

#### Test 18: First Day of Month
**Steps:**
1. Navigate to any month
2. Select the 1st day of the month

**Expected Results:**
- ✅ Date selects properly
- ✅ Tasks display correctly
- ✅ No layout issues

#### Test 19: Last Day of Month
**Steps:**
1. Navigate to any month
2. Select the last day (28, 29, 30, or 31)

**Expected Results:**
- ✅ Date selects properly
- ✅ Tasks display correctly
- ✅ No layout issues

### UI/UX Tests

#### Test 20: Visual Consistency
**Steps:**
1. Compare calendar colors with rest of app
2. Check text sizes and fonts
3. Verify spacing and padding

**Expected Results:**
- ✅ Colors match app theme
- ✅ Text is readable and properly sized
- ✅ Spacing is consistent
- ✅ No visual glitches or overlaps

#### Test 21: Touch Targets
**Steps:**
1. Try tapping dates, especially small ones
2. Tap navigation buttons
3. Tap tasks in the list

**Expected Results:**
- ✅ All dates are easily tappable
- ✅ No accidental selections
- ✅ Navigation buttons respond well
- ✅ Task cards are easily tappable

#### Test 22: Loading States
**Steps:**
1. Clear app data
2. Open calendar screen
3. Observe loading behavior

**Expected Results:**
- ✅ Loading indicator appears briefly
- ✅ Calendar loads smoothly
- ✅ No blank screens or errors
- ✅ Tasks load and display properly

## Performance Testing

### Test 23: Scroll Performance
**Steps:**
1. Rapidly scroll through multiple months
2. Observe smoothness and responsiveness

**Expected Results:**
- ✅ Smooth scrolling with no lag
- ✅ Dates render quickly
- ✅ No frame drops or stuttering

### Test 24: Memory Usage
**Steps:**
1. Open calendar screen
2. Navigate through many months
3. Monitor app memory usage

**Expected Results:**
- ✅ No memory leaks
- ✅ Reasonable memory consumption
- ✅ App remains responsive

## Regression Testing

### Test 25: Other Screens Still Work
**Steps:**
1. Test calendar screen
2. Navigate to other screens (Tasks, Groups, Chat, Profile)
3. Return to calendar

**Expected Results:**
- ✅ All other screens function normally
- ✅ Navigation works properly
- ✅ Calendar state is preserved or reloads correctly

## Bug Reporting Template

If you find issues, report them with:

```
**Bug Title:** [Brief description]

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Screenshots:**
[If applicable]

**Device Info:**
- Device: [e.g., Pixel 5]
- Android Version: [e.g., Android 13]
- App Version: [e.g., 1.0]
```

## Test Results Summary

After completing all tests, fill out:

| Test Category | Tests Passed | Tests Failed | Notes |
|--------------|--------------|--------------|-------|
| Calendar Display | __/6 | __/6 | |
| Date Selection | __/3 | __/3 | |
| Month Navigation | __/3 | __/3 | |
| Task Display | __/3 | __/3 | |
| Real-Time Updates | __/3 | __/3 | |
| Edge Cases | __/4 | __/4 | |
| UI/UX | __/3 | __/3 | |
| Performance | __/2 | __/2 | |
| Regression | __/1 | __/1 | |
| **TOTAL** | __/28 | __/28 | |

## Sign-Off

- [ ] All critical tests passed
- [ ] No blocking bugs found
- [ ] Performance is acceptable
- [ ] Ready for next task

**Tester Name:** _______________
**Date:** _______________
**Signature:** _______________
