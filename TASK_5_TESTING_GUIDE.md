# Task 5 Testing Guide

## Quick Test Scenarios

### Scenario 1: View Tasks in Tasks Screen
**Steps:**
1. Open the app and navigate to Tasks tab
2. Observe the task list

**Expected Results:**
- ✅ Tasks load from Firestore (not demo data)
- ✅ Task stats show correct counts (overdue, due today, completed)
- ✅ Tasks display with title, subject, and due date
- ✅ If no tasks exist, empty state shows with message "No tasks yet"

### Scenario 2: Filter Tasks by Category
**Steps:**
1. In Tasks tab, click "All Tasks" button
2. Click "Personal" button
3. Click "Group" button
4. Click "Assignments" button

**Expected Results:**
- ✅ "All Tasks" shows all tasks
- ✅ "Personal" shows only personal category tasks
- ✅ "Group" shows only group category tasks
- ✅ "Assignments" shows only assignment category tasks
- ✅ Empty state shows appropriate message if no tasks in category

### Scenario 3: View Calendar with Task Indicators
**Steps:**
1. Navigate to Calendar tab
2. Observe the calendar view

**Expected Results:**
- ✅ Calendar displays current month
- ✅ Dot indicators appear on dates that have tasks
- ✅ No dots on dates without tasks
- ✅ Today's date is highlighted

### Scenario 4: Select Date and View Tasks
**Steps:**
1. In Calendar tab, tap on a date with a dot indicator
2. Observe the task list below calendar

**Expected Results:**
- ✅ Selected date is highlighted
- ✅ "Tasks for [date]" header shows selected date
- ✅ Task list shows all tasks due on that date
- ✅ Task details are correct (title, subject, due date)

### Scenario 5: Select Date Without Tasks
**Steps:**
1. In Calendar tab, tap on a date without a dot indicator
2. Observe the area below calendar

**Expected Results:**
- ✅ Empty state shows with calendar emoji (📅)
- ✅ Message says "No tasks for this date"
- ✅ Task list is hidden

### Scenario 6: Filter Calendar Tasks
**Steps:**
1. In Calendar tab, select "All Tasks" chip
2. Select "My Tasks" chip
3. Select "Group Tasks" chip

**Expected Results:**
- ✅ "All Tasks" shows all tasks and all date dots
- ✅ "My Tasks" shows only user's tasks and relevant date dots
- ✅ "Group Tasks" shows only group tasks and relevant date dots
- ✅ Task list updates based on filter

### Scenario 7: Real-time Task Creation
**Steps:**
1. In Tasks tab, click "+" button to create new task
2. Fill in task details (title, subject, due date, category)
3. Click "Create"

**Expected Results:**
- ✅ Task appears immediately in task list
- ✅ Task stats update immediately
- ✅ If task has due date, dot appears on calendar date
- ✅ No page refresh needed

### Scenario 8: Real-time Task Update
**Steps:**
1. Create or select an existing task
2. Update the task's due date
3. Navigate to Calendar tab

**Expected Results:**
- ✅ Task appears on new date in calendar
- ✅ Dot indicator moves to new date
- ✅ Old date no longer has dot (if no other tasks)
- ✅ Updates happen immediately

### Scenario 9: Pull to Refresh
**Steps:**
1. In Tasks tab, pull down on the screen
2. Release to trigger refresh

**Expected Results:**
- ✅ Loading indicator shows
- ✅ Tasks reload from Firestore
- ✅ Loading indicator disappears
- ✅ Task list updates if data changed

### Scenario 10: Navigate Between Months
**Steps:**
1. In Calendar tab, click next month button (→)
2. Click previous month button (←)
3. Swipe left/right on calendar

**Expected Results:**
- ✅ Calendar scrolls to next/previous month
- ✅ Month/year header updates
- ✅ Dot indicators show for tasks in that month
- ✅ Smooth animation

## Edge Case Testing

### Edge Case 1: New User with No Tasks
**Setup:** Login with new user account that has no tasks

**Expected Results:**
- ✅ Tasks screen shows empty state
- ✅ Calendar shows no dot indicators
- ✅ Selecting any date shows empty state
- ✅ No errors or crashes

### Edge Case 2: Tasks Without Due Dates
**Setup:** Create tasks without setting due dates

**Expected Results:**
- ✅ Tasks appear in Tasks screen
- ✅ Tasks do NOT appear in Calendar
- ✅ No dot indicators for these tasks
- ✅ Task list shows "No due date" or similar

### Edge Case 3: Past Due Tasks
**Setup:** Create tasks with past due dates

**Expected Results:**
- ✅ Tasks appear in Tasks screen
- ✅ Tasks marked as "Overdue"
- ✅ Overdue count updates in stats
- ✅ Tasks appear on past dates in calendar

### Edge Case 4: Multiple Tasks on Same Date
**Setup:** Create 3+ tasks with same due date

**Expected Results:**
- ✅ Single dot indicator on that date
- ✅ Selecting date shows all tasks
- ✅ Tasks listed in order (by time or priority)
- ✅ All tasks visible (scrollable if needed)

### Edge Case 5: Offline Mode
**Setup:** Turn off device internet connection

**Expected Results:**
- ✅ Cached tasks still display
- ✅ No crashes or errors
- ✅ Creating tasks queues for sync
- ✅ Coming back online syncs changes

## Performance Testing

### Test 1: Load Time
**Steps:**
1. Clear app data
2. Login and navigate to Tasks tab
3. Measure time to display tasks

**Expected Results:**
- ✅ Tasks appear within 2 seconds
- ✅ Loading indicator shows during load
- ✅ No blank screen or freeze

### Test 2: Filter Response Time
**Steps:**
1. In Tasks tab with many tasks
2. Click different category filters rapidly

**Expected Results:**
- ✅ Filter changes are instant (< 100ms)
- ✅ No lag or delay
- ✅ Smooth transitions

### Test 3: Calendar Scroll Performance
**Steps:**
1. In Calendar tab, scroll through multiple months
2. Observe smoothness

**Expected Results:**
- ✅ Smooth scrolling (60fps)
- ✅ No stuttering or lag
- ✅ Dot indicators load quickly

### Test 4: Many Tasks Performance
**Setup:** Create 50+ tasks

**Expected Results:**
- ✅ Tasks screen still loads quickly
- ✅ Scrolling is smooth
- ✅ Filtering works without lag
- ✅ Calendar performs well

## Integration Testing

### Integration 1: Tasks ↔ Calendar Sync
**Steps:**
1. Create task in Tasks screen with due date
2. Navigate to Calendar tab
3. Verify task appears on correct date

**Expected Results:**
- ✅ Task appears on calendar immediately
- ✅ Dot indicator shows on date
- ✅ Selecting date shows task details

### Integration 2: Category Filter Consistency
**Steps:**
1. Filter to "Personal" in Tasks screen
2. Navigate to Calendar tab
3. Verify calendar respects filter

**Expected Results:**
- ✅ Calendar shows only personal tasks
- ✅ Dot indicators only for personal tasks
- ✅ Filter state maintained across tabs

### Integration 3: Real-time Multi-Screen Update
**Steps:**
1. Open Tasks screen
2. In another device/browser, create a task
3. Observe Tasks screen

**Expected Results:**
- ✅ New task appears automatically
- ✅ No manual refresh needed
- ✅ Stats update immediately

## Regression Testing

### Regression 1: Existing Features Still Work
**Tests:**
- ✅ Task creation dialog works
- ✅ Task stats calculation correct
- ✅ Navigation between tabs works
- ✅ Pull-to-refresh works
- ✅ Search and filter buttons work

### Regression 2: No Demo Data Visible
**Tests:**
- ✅ No hardcoded task names appear
- ✅ No "Research Paper Draft" or similar demo tasks
- ✅ All data comes from Firestore
- ✅ Empty state shows when no real data

### Regression 3: Authentication Still Works
**Tests:**
- ✅ Only logged-in user's tasks show
- ✅ Logging out clears tasks
- ✅ Logging in loads correct tasks
- ✅ No data leakage between users

## Automated Testing (Optional)

### Unit Tests
```kotlin
@Test
fun `getUserTasks returns flow of tasks`() = runTest {
    val tasks = taskRepository.getUserTasks("personal").first()
    assertTrue(tasks.isNotEmpty())
}

@Test
fun `getTasksForDate filters by date correctly`() = runTest {
    val date = LocalDate.now()
    val tasks = taskRepository.getTasksForDate(date).first()
    tasks.forEach { task ->
        val taskDate = task.dueDate?.toLocalDate()
        assertEquals(date, taskDate)
    }
}

@Test
fun `getDatesWithTasks returns unique dates`() = runTest {
    val dates = taskRepository.getDatesWithTasks().first()
    assertEquals(dates.size, dates.toSet().size)
}
```

### UI Tests
```kotlin
@Test
fun testTasksScreenDisplaysTasks() {
    onView(withId(R.id.recycler_tasks))
        .check(matches(isDisplayed()))
}

@Test
fun testEmptyStateShowsWhenNoTasks() {
    onView(withId(R.id.empty_state_layout))
        .check(matches(isDisplayed()))
}

@Test
fun testCategoryFilterWorks() {
    onView(withId(R.id.btn_personal)).perform(click())
    // Verify only personal tasks shown
}
```

## Bug Reporting Template

If you find issues, report them using this template:

```
**Bug Title:** [Brief description]

**Steps to Reproduce:**
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Result:**
[What should happen]

**Actual Result:**
[What actually happened]

**Screenshots:**
[Attach screenshots if applicable]

**Device Info:**
- Device: [e.g., Pixel 6]
- Android Version: [e.g., Android 13]
- App Version: [e.g., 1.0.0]

**Additional Context:**
[Any other relevant information]
```

## Test Sign-off

After completing all tests, sign off:

- [ ] All quick test scenarios passed
- [ ] All edge cases handled correctly
- [ ] Performance is acceptable
- [ ] Integration tests passed
- [ ] No regressions found
- [ ] Ready for production

**Tester Name:** _______________
**Date:** _______________
**Signature:** _______________

---

## Notes
- Test on multiple devices if possible (different screen sizes, Android versions)
- Test with both small and large datasets
- Test with slow network connections
- Document any issues found during testing
