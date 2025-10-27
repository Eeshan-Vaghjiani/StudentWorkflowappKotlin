# Task 21 Testing Guide: Date Selection and Task Filtering

## Pre-Testing Setup

### 1. Ensure Test Data Exists
Before testing, make sure you have:
- At least 3-5 tasks with different due dates
- Some tasks assigned to you (My Tasks)
- Some tasks in groups (Group Tasks)
- Some tasks on different dates this month
- Some tasks on dates in next/previous months

### 2. Build and Install
```bash
./gradlew clean assembleDebug
./gradlew installDebug
```

## Test Scenarios

### Scenario 1: Date Selection
**Objective**: Verify date selection works correctly

**Steps:**
1. Open the app and navigate to Calendar tab
2. Observe the calendar view
3. Click on today's date
   - ✅ Date should be highlighted with colored background
   - ✅ "Tasks for [date]" text should appear below calendar
   - ✅ Tasks for today should appear in list (or empty state)
4. Click on a different date with tasks
   - ✅ Previous date should unhighlight
   - ✅ New date should highlight
   - ✅ Task list should update to show tasks for new date
5. Click on a date without tasks
   - ✅ Empty state should appear: "📅 No tasks for this date"

**Expected Results:**
- Only one date highlighted at a time
- Task list updates immediately when date changes
- Selected date text shows correct formatted date
- Empty state appears when no tasks exist

---

### Scenario 2: Filter - All Tasks
**Objective**: Verify "All Tasks" filter shows all tasks

**Steps:**
1. Navigate to Calendar tab
2. Ensure "All Tasks" chip is selected (should be default)
3. Observe calendar dots
   - ✅ Dots should appear on all dates that have any tasks
4. Click on a date with tasks
   - ✅ All tasks for that date should appear
5. Count the number of dates with dots
6. Note the total number of tasks shown

**Expected Results:**
- All tasks visible regardless of assignment or group
- Calendar dots on all dates with tasks
- This is the baseline for comparing other filters

---

### Scenario 3: Filter - My Tasks
**Objective**: Verify "My Tasks" filter shows only assigned/created tasks

**Steps:**
1. Navigate to Calendar tab
2. Click "My Tasks" chip
   - ✅ Chip should become selected (highlighted)
   - ✅ "All Tasks" chip should deselect
3. Observe calendar dots
   - ✅ Dots should only appear on dates with tasks assigned to you
   - ✅ Some dots may disappear compared to "All Tasks"
4. Click on a date with dots
   - ✅ Only tasks created by you should appear
5. Click on a date that had dots in "All Tasks" but not in "My Tasks"
   - ✅ Empty state should appear

**Expected Results:**
- Only tasks where you are the creator (userId matches current user)
- Calendar dots update to reflect filtered tasks
- Task count should be equal or less than "All Tasks"

---

### Scenario 4: Filter - Group Tasks
**Objective**: Verify "Group Tasks" filter shows only group-related tasks

**Steps:**
1. Navigate to Calendar tab
2. Click "Group Tasks" chip
   - ✅ Chip should become selected
   - ✅ Previous chip should deselect
3. Observe calendar dots
   - ✅ Dots should only appear on dates with group tasks
4. Click on a date with dots
   - ✅ Only tasks with a groupId should appear
   - ✅ Task category should show "Group" or similar
5. Verify personal tasks don't appear
   - ✅ Tasks with category "Personal" should not be visible

**Expected Results:**
- Only tasks belonging to groups (have groupId)
- Calendar dots update accordingly
- Personal tasks are filtered out

---

### Scenario 5: Month Navigation - Buttons
**Objective**: Verify month navigation buttons work

**Steps:**
1. Navigate to Calendar tab
2. Note the current month/year displayed
3. Click "Previous Month" button (left arrow)
   - ✅ Calendar should scroll to previous month
   - ✅ Month/year text should update
   - ✅ Animation should be smooth
4. Click "Next Month" button (right arrow) twice
   - ✅ Calendar should scroll forward two months
   - ✅ Month/year text should update each time
5. Navigate back to current month
   - ✅ Today's date should be visible and highlighted differently

**Expected Results:**
- Smooth scrolling animation between months
- Month/year text always matches displayed month
- Can navigate multiple months in either direction

---

### Scenario 6: Month Navigation - Swipe Gestures
**Objective**: Verify swipe gestures change months

**Steps:**
1. Navigate to Calendar tab
2. Place finger on calendar and swipe LEFT (towards left edge)
   - ✅ Calendar should scroll to NEXT month
   - ✅ Month/year text should update
3. Swipe RIGHT (towards right edge)
   - ✅ Calendar should scroll to PREVIOUS month
   - ✅ Month/year text should update
4. Try a short swipe (less than 100 pixels)
   - ✅ Should NOT change month (threshold not met)
5. Try a slow swipe
   - ✅ Should NOT change month (velocity too low)
6. Try a vertical swipe
   - ✅ Should NOT change month (wrong direction)

**Expected Results:**
- Swipe left = next month
- Swipe right = previous month
- Short/slow swipes ignored
- Vertical swipes ignored
- Smooth animation

---

### Scenario 7: Filter Persistence
**Objective**: Verify filter selection persists

**Steps:**
1. Navigate to Calendar tab
2. Select "My Tasks" filter
3. Select a specific date
4. Navigate to a different tab (e.g., Tasks or Groups)
5. Navigate back to Calendar tab
   - ✅ "My Tasks" filter should still be selected
   - ✅ Same date should still be selected
   - ✅ Task list should show same filtered tasks

**Expected Results:**
- Filter selection survives tab changes
- Selected date survives tab changes
- ViewModel maintains state

---

### Scenario 8: Real-time Updates
**Objective**: Verify calendar updates when tasks change

**Steps:**
1. Navigate to Calendar tab
2. Select "All Tasks" filter
3. Note a date without any dots
4. Navigate to Tasks tab
5. Create a new task with due date = the date without dots
6. Navigate back to Calendar tab
   - ✅ The date should now have a dot
7. Click on that date
   - ✅ The new task should appear in the list

**Expected Results:**
- Calendar dots update when tasks are added
- Task list updates with new tasks
- Changes reflect immediately

---

### Scenario 9: Edge Cases
**Objective**: Test edge cases and error conditions

**Steps:**
1. **No Tasks at All**
   - Delete all tasks or use a new account
   - Navigate to Calendar tab
   - ✅ No dots should appear on any date
   - ✅ Empty state should show for any selected date

2. **Many Tasks on One Date**
   - Create 10+ tasks for the same date
   - Navigate to Calendar tab
   - Click on that date
   - ✅ All tasks should appear in scrollable list
   - ✅ Only one dot should appear (not multiple)

3. **Tasks Spanning Multiple Months**
   - Create tasks in previous, current, and next month
   - Navigate between months
   - ✅ Each month should show correct dots
   - ✅ Filter should work across all months

4. **Rapid Filter Changes**
   - Quickly click between all three filter chips
   - ✅ UI should update smoothly without crashes
   - ✅ No flickering or incorrect data

**Expected Results:**
- App handles edge cases gracefully
- No crashes or errors
- UI remains responsive

---

## Performance Testing

### Test 1: Large Dataset
**Setup:** Create 50+ tasks across multiple months

**Verify:**
- Calendar loads quickly (< 2 seconds)
- Scrolling between months is smooth
- Filter changes are instant
- No lag when selecting dates

### Test 2: Rapid Interactions
**Actions:**
- Quickly swipe between months
- Rapidly click different dates
- Quickly change filters

**Verify:**
- No crashes
- UI remains responsive
- No memory leaks
- Animations don't stutter

---

## Visual Verification

### Calendar Appearance
- [ ] Month/year text is clearly visible
- [ ] Previous/Next buttons are intuitive
- [ ] Calendar grid is properly aligned
- [ ] Day numbers are readable
- [ ] Task dots are visible and colored
- [ ] Selected date has distinct background
- [ ] Today's date is highlighted differently

### Filter Chips
- [ ] Three chips are visible
- [ ] Selected chip is highlighted
- [ ] Chips are horizontally scrollable if needed
- [ ] Chip text is readable
- [ ] Selection changes are visually clear

### Task List
- [ ] Tasks are clearly separated
- [ ] Priority indicator is visible
- [ ] All task details are readable
- [ ] Empty state is centered and clear
- [ ] Scrolling works if many tasks

---

## Regression Testing

Verify previous functionality still works:

### From Task 20
- [ ] Calendar displays current month
- [ ] Day cells show date numbers
- [ ] Task dots appear on dates with tasks
- [ ] Today's date is highlighted
- [ ] Month header shows day names

### General App
- [ ] Login/logout still works
- [ ] Other tabs (Tasks, Groups, Chat, Profile) work
- [ ] Task creation still works
- [ ] Navigation between screens works

---

## Bug Reporting Template

If you find issues, report using this format:

```
**Bug Title:** [Brief description]

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Filter Active:** [All Tasks / My Tasks / Group Tasks]

**Date Selected:** [Date]

**Screenshots:** [If applicable]

**Device:** [Device model and Android version]
```

---

## Success Criteria

Task 21 is considered complete when:

✅ Date selection works correctly
✅ All three filters work as expected
✅ Calendar dots update based on active filter
✅ Task list updates based on filter and selected date
✅ Month navigation works via buttons
✅ Month navigation works via swipe gestures
✅ Filter selection persists during navigation
✅ No crashes or errors
✅ UI is responsive and smooth
✅ Empty states display correctly

---

## Known Limitations

1. **Filter Persistence**: Filter resets to "All Tasks" when app is closed and reopened (by design)
2. **Swipe Sensitivity**: Swipe gestures may not work if you start the swipe on a date cell (date click takes priority)
3. **Task Details**: Clicking a task doesn't navigate to details yet (Task 22)

---

## Next Steps After Testing

If all tests pass:
1. Mark Task 21 as complete
2. Update tasks.md
3. Proceed to Task 22: Add task details navigation

If tests fail:
1. Document the failures
2. Fix the issues
3. Re-test
4. Update implementation summary
