# Task 8: Calendar Integration - Testing Guide

## Overview
This guide provides step-by-step instructions for testing the calendar integration with Firestore.

## Test Environment Setup

### Prerequisites
1. **Device/Emulator**: Android 8.0+ (API 26+)
2. **Network**: Active internet connection
3. **Firebase**: Project configured with Firestore
4. **User Account**: Valid test user credentials
5. **Test Data**: Sample tasks with various due dates

### Initial Setup Steps

#### 1. Install the App
```bash
# Build and install
./gradlew installDebug

# Or use Android Studio
Run > Run 'app'
```

#### 2. Login
- Open the app
- Login with test credentials
- Verify successful authentication

#### 3. Create Test Data
Create the following test tasks:

**Task 1: Due Today**
- Title: "Complete Daily Report"
- Due Date: Today at 2:00 PM
- Priority: High
- Category: Work

**Task 2: Due Tomorrow**
- Title: "Team Meeting Prep"
- Due Date: Tomorrow at 10:00 AM
- Priority: Medium
- Category: Work

**Task 3: Due Next Week**
- Title: "Project Milestone Review"
- Due Date: 7 days from now at 3:00 PM
- Priority: High
- Category: Work

**Task 4: Overdue**
- Title: "Submit Timesheet"
- Due Date: Yesterday at 5:00 PM
- Priority: High
- Category: Work

**Task 5: Group Task**
- Title: "Group Study Session"
- Due Date: 3 days from now at 6:00 PM
- Priority: Medium
- Category: Study
- Group: Any test group

**Task 6: No Due Date**
- Title: "Read Documentation"
- Due Date: None
- Priority: Low
- Category: Personal

## Test Cases

### Test Case 1: Calendar Loads with Tasks

**Objective**: Verify calendar loads and displays task indicators

**Steps**:
1. Open the app
2. Navigate to Calendar tab
3. Observe the calendar view

**Expected Results**:
- ✅ Calendar displays current month
- ✅ Month/year header shows correct date
- ✅ Blue dots appear on dates with tasks
- ✅ Today's date is highlighted
- ✅ Loading indicator appears briefly then disappears
- ✅ No error messages

**Pass Criteria**: All expected results met

---

### Test Case 2: Date Selection

**Objective**: Verify date selection and task list display

**Steps**:
1. Open Calendar screen
2. Click on today's date (should have task indicator)
3. Observe the task list below calendar

**Expected Results**:
- ✅ Selected date has blue background
- ✅ "Tasks for [Date]" header updates
- ✅ Task list shows all tasks for today
- ✅ Each task shows title, time, priority, status
- ✅ Tasks are clickable

**Pass Criteria**: All expected results met

---

### Test Case 3: Empty Date Selection

**Objective**: Verify empty state when no tasks exist

**Steps**:
1. Open Calendar screen
2. Click on a date without task indicator
3. Observe the area below calendar

**Expected Results**:
- ✅ Empty state appears
- ✅ Calendar emoji (📅) displayed
- ✅ Message: "No tasks for this date"
- ✅ No task list shown

**Pass Criteria**: All expected results met

---

### Test Case 4: Month Navigation (Buttons)

**Objective**: Verify month navigation using buttons

**Steps**:
1. Open Calendar screen
2. Note current month
3. Click "Next Month" button (►)
4. Observe calendar
5. Click "Previous Month" button (◄)
6. Observe calendar

**Expected Results**:
- ✅ Calendar scrolls smoothly to next month
- ✅ Month/year header updates correctly
- ✅ Task indicators update for new month
- ✅ Calendar scrolls back to previous month
- ✅ Returns to original month view

**Pass Criteria**: All expected results met

---

### Test Case 5: Month Navigation (Swipe)

**Objective**: Verify month navigation using swipe gestures

**Steps**:
1. Open Calendar screen
2. Swipe left on calendar
3. Observe calendar
4. Swipe right on calendar
5. Observe calendar

**Expected Results**:
- ✅ Swipe left moves to next month
- ✅ Swipe right moves to previous month
- ✅ Smooth animation during swipe
- ✅ Month/year header updates
- ✅ Task indicators update

**Pass Criteria**: All expected results met

---

### Test Case 6: Task Click Navigation

**Objective**: Verify navigation to task details

**Steps**:
1. Open Calendar screen
2. Select a date with tasks
3. Click on a task in the list
4. Observe TaskDetailsActivity

**Expected Results**:
- ✅ TaskDetailsActivity opens
- ✅ Correct task details displayed
- ✅ All task fields shown (title, date, priority, etc.)
- ✅ Edit/Delete buttons available

**Pass Criteria**: All expected results met

---

### Test Case 7: Real-time Task Creation

**Objective**: Verify calendar updates when new task created

**Steps**:
1. Open Calendar screen
2. Note a date without tasks
3. Navigate to Tasks screen
4. Create new task with due date = noted date
5. Navigate back to Calendar
6. Observe the calendar

**Expected Results**:
- ✅ Task indicator appears on the date within 2 seconds
- ✅ No manual refresh needed
- ✅ Select the date shows new task in list

**Pass Criteria**: All expected results met

---

### Test Case 8: Real-time Task Update

**Objective**: Verify calendar updates when task due date changes

**Steps**:
1. Open Calendar screen
2. Note a date with task indicator
3. Select the date and note the task
4. Navigate to Tasks screen
5. Edit the task and change due date to different date
6. Navigate back to Calendar
7. Observe both dates

**Expected Results**:
- ✅ Indicator disappears from old date (if no other tasks)
- ✅ Indicator appears on new date within 2 seconds
- ✅ Task appears in list for new date
- ✅ Task removed from list for old date

**Pass Criteria**: All expected results met

---

### Test Case 9: Real-time Task Deletion

**Objective**: Verify calendar updates when task deleted

**Steps**:
1. Open Calendar screen
2. Note a date with single task
3. Navigate to Tasks screen
4. Delete the task
5. Navigate back to Calendar
6. Observe the date

**Expected Results**:
- ✅ Task indicator disappears within 2 seconds
- ✅ Date shows no indicator
- ✅ Selecting date shows empty state

**Pass Criteria**: All expected results met

---

### Test Case 10: Filter - All Tasks

**Objective**: Verify "All Tasks" filter works correctly

**Steps**:
1. Open Calendar screen
2. Ensure "All Tasks" chip is selected
3. Observe calendar indicators
4. Select various dates with tasks

**Expected Results**:
- ✅ All user tasks show indicators
- ✅ Both personal and group tasks visible
- ✅ Task list shows all tasks for selected date

**Pass Criteria**: All expected results met

---

### Test Case 11: Filter - My Tasks

**Objective**: Verify "My Tasks" filter works correctly

**Steps**:
1. Open Calendar screen
2. Click "My Tasks" chip
3. Observe calendar indicators
4. Select dates with tasks

**Expected Results**:
- ✅ Only personal tasks show indicators
- ✅ Group task indicators hidden
- ✅ Task list shows only personal tasks
- ✅ Group tasks not in list

**Pass Criteria**: All expected results met

---

### Test Case 12: Filter - Group Tasks

**Objective**: Verify "Group Tasks" filter works correctly

**Steps**:
1. Open Calendar screen
2. Click "Group Tasks" chip
3. Observe calendar indicators
4. Select dates with tasks

**Expected Results**:
- ✅ Only group tasks show indicators
- ✅ Personal task indicators hidden
- ✅ Task list shows only group tasks
- ✅ Personal tasks not in list

**Pass Criteria**: All expected results met

---

### Test Case 13: Multiple Tasks Same Date

**Objective**: Verify handling of multiple tasks on same date

**Steps**:
1. Create 3 tasks with same due date
2. Open Calendar screen
3. Observe the date
4. Click the date

**Expected Results**:
- ✅ Single indicator shown on date
- ✅ All 3 tasks appear in list
- ✅ Tasks sorted appropriately
- ✅ All task details visible

**Pass Criteria**: All expected results met

---

### Test Case 14: Task Without Due Date

**Objective**: Verify tasks without due dates don't appear

**Steps**:
1. Create task without due date
2. Open Calendar screen
3. Scan all dates on calendar

**Expected Results**:
- ✅ No indicator appears for this task
- ✅ Task doesn't appear in any date's list
- ✅ Calendar functions normally

**Pass Criteria**: All expected results met

---

### Test Case 15: Overdue Tasks

**Objective**: Verify overdue tasks still show on calendar

**Steps**:
1. Open Calendar screen
2. Navigate to previous month
3. Locate date with overdue task
4. Click the date

**Expected Results**:
- ✅ Indicator shows on past date
- ✅ Overdue task appears in list
- ✅ Task shows correct details
- ✅ No special overdue styling (handled elsewhere)

**Pass Criteria**: All expected results met

---

### Test Case 16: Activity Result Handling

**Objective**: Verify calendar updates after task modification

**Steps**:
1. Open Calendar screen
2. Select date with task
3. Click task to open details
4. Mark task as complete
5. Press back button
6. Observe calendar

**Expected Results**:
- ✅ Calendar refreshes automatically
- ✅ Task list updates (task marked complete)
- ✅ Indicator remains (task still exists)
- ✅ Task status updated in list

**Pass Criteria**: All expected results met

---

### Test Case 17: Network Offline

**Objective**: Verify calendar works with cached data offline

**Steps**:
1. Open Calendar with network on
2. Let data load
3. Turn off network (airplane mode)
4. Navigate between months
5. Select dates

**Expected Results**:
- ✅ Cached data displays
- ✅ Navigation works
- ✅ Task indicators show from cache
- ✅ Task lists show cached data
- ✅ No crash or error

**Pass Criteria**: All expected results met

---

### Test Case 18: Network Reconnect

**Objective**: Verify calendar syncs when network returns

**Steps**:
1. Start with network off
2. Open Calendar (shows cached data)
3. Turn network back on
4. Wait a few seconds

**Expected Results**:
- ✅ Calendar automatically syncs
- ✅ New tasks appear
- ✅ Updated tasks reflect changes
- ✅ Deleted tasks disappear
- ✅ No manual refresh needed

**Pass Criteria**: All expected results met

---

### Test Case 19: Performance - Load Time

**Objective**: Verify calendar loads quickly

**Steps**:
1. Close app completely
2. Open app and login
3. Navigate to Calendar tab
4. Start timer when tab clicked
5. Stop timer when data appears

**Expected Results**:
- ✅ Calendar loads in < 2 seconds with network
- ✅ Calendar loads in < 500ms with cache
- ✅ No lag or stuttering
- ✅ Smooth animations

**Pass Criteria**: Load time meets expectations

---

### Test Case 20: Performance - Month Navigation

**Objective**: Verify smooth month navigation

**Steps**:
1. Open Calendar screen
2. Rapidly swipe between months 10 times
3. Observe performance

**Expected Results**:
- ✅ No lag or stuttering
- ✅ Smooth animations
- ✅ No frame drops
- ✅ App remains responsive

**Pass Criteria**: Navigation is smooth

---

## Regression Testing

### After Code Changes
Run these critical tests:
1. Test Case 1 (Calendar Loads)
2. Test Case 2 (Date Selection)
3. Test Case 7 (Real-time Creation)
4. Test Case 10 (All Tasks Filter)

### Before Release
Run all 20 test cases

## Automated Testing

### Unit Tests
```kotlin
// CalendarViewModelTest.kt
@Test
fun `selectDate updates selectedDate StateFlow`() {
    val date = LocalDate.now()
    viewModel.selectDate(date)
    assertEquals(date, viewModel.selectedDate.value)
}

@Test
fun `setFilter updates currentFilter StateFlow`() {
    viewModel.setFilter(TaskFilter.MY_TASKS)
    assertEquals(TaskFilter.MY_TASKS, viewModel.currentFilter.value)
}
```

### Integration Tests
```kotlin
// CalendarFragmentTest.kt
@Test
fun `clicking date updates task list`() {
    // Launch fragment
    // Click date
    // Verify task list updates
}
```

## Bug Reporting Template

```markdown
**Bug Title**: [Brief description]

**Test Case**: [Test case number and name]

**Steps to Reproduce**:
1. 
2. 
3. 

**Expected Result**:
[What should happen]

**Actual Result**:
[What actually happened]

**Screenshots**:
[Attach screenshots]

**Logcat**:
[Paste relevant logcat output]

**Device Info**:
- Device: [e.g., Pixel 5]
- Android Version: [e.g., Android 12]
- App Version: [e.g., 1.0.0]

**Severity**:
- [ ] Critical (app crashes)
- [ ] High (feature broken)
- [ ] Medium (feature partially works)
- [ ] Low (cosmetic issue)
```

## Test Results Template

```markdown
# Calendar Integration Test Results

**Date**: [Test date]
**Tester**: [Your name]
**Build**: [Build number]
**Device**: [Device info]

## Summary
- Total Tests: 20
- Passed: __
- Failed: __
- Blocked: __
- Pass Rate: __%

## Test Results

| # | Test Case | Status | Notes |
|---|-----------|--------|-------|
| 1 | Calendar Loads | ✅ | |
| 2 | Date Selection | ✅ | |
| 3 | Empty Date | ✅ | |
| 4 | Month Nav (Buttons) | ✅ | |
| 5 | Month Nav (Swipe) | ✅ | |
| 6 | Task Click | ✅ | |
| 7 | Real-time Creation | ✅ | |
| 8 | Real-time Update | ✅ | |
| 9 | Real-time Deletion | ✅ | |
| 10 | Filter All | ✅ | |
| 11 | Filter My Tasks | ✅ | |
| 12 | Filter Group Tasks | ✅ | |
| 13 | Multiple Tasks | ✅ | |
| 14 | No Due Date | ✅ | |
| 15 | Overdue Tasks | ✅ | |
| 16 | Activity Result | ✅ | |
| 17 | Network Offline | ✅ | |
| 18 | Network Reconnect | ✅ | |
| 19 | Load Performance | ✅ | |
| 20 | Nav Performance | ✅ | |

## Issues Found
1. [Issue description]
2. [Issue description]

## Overall Assessment
[Pass/Fail with notes]
```

## Conclusion

This testing guide covers:
- ✅ 20 comprehensive test cases
- ✅ Setup instructions
- ✅ Expected results for each test
- ✅ Regression testing strategy
- ✅ Bug reporting template
- ✅ Test results template

Follow this guide to thoroughly test the calendar integration!
