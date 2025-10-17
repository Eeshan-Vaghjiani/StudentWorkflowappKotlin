# Task 5 Quick Reference Guide

## Overview
Task 5 implements real-time task display with category filtering for both Tasks and Calendar screens, replacing all demo data with live Firestore queries.

## Key Components

### TaskRepository Methods

#### 1. getUserTasks(category: String?)
```kotlin
// Get all tasks with optional category filter
taskRepository.getUserTasks("personal") // Personal tasks only
taskRepository.getUserTasks("group")    // Group tasks only
taskRepository.getUserTasks("assignment") // Assignments only
taskRepository.getUserTasks("all")      // All tasks
taskRepository.getUserTasks(null)       // All tasks
```

**Returns**: `Flow<List<FirebaseTask>>`
**Real-time**: Yes, updates automatically

#### 2. getTasksForDate(date: LocalDate)
```kotlin
// Get tasks for specific date
val date = LocalDate.of(2024, 1, 15)
taskRepository.getTasksForDate(date)
```

**Returns**: `Flow<List<FirebaseTask>>`
**Real-time**: Yes, updates automatically

#### 3. getDatesWithTasks()
```kotlin
// Get all dates that have tasks
taskRepository.getDatesWithTasks()
```

**Returns**: `Flow<Set<LocalDate>>`
**Real-time**: Yes, updates automatically

### TasksFragment Usage

#### Collecting Tasks with Filter
```kotlin
taskRepository.getUserTasks(currentFilter)
    .collectWithLifecycle(viewLifecycleOwner) { tasks ->
        updateTasksList(tasks)
        showEmptyStateIfNeeded(tasks.isEmpty())
    }
```

#### Showing Empty State
```kotlin
private fun showEmptyStateIfNeeded(isEmpty: Boolean) {
    if (isEmpty) {
        emptyStateView?.visibility = View.VISIBLE
        recyclerTasks.visibility = View.GONE
        emptyStateText?.text = "No tasks yet"
    } else {
        emptyStateView?.visibility = View.GONE
        recyclerTasks.visibility = View.VISIBLE
    }
}
```

### CalendarViewModel Usage

#### Setting Up Real-time Listeners
```kotlin
private fun setupRealTimeListeners() {
    viewModelScope.launch {
        taskRepository.getUserTasks(null).collect { tasks ->
            _allTasks.value = tasks
            applyFilter(_currentFilter.value)
        }
    }
    
    viewModelScope.launch {
        taskRepository.getDatesWithTasks().collect { dates ->
            _datesWithTasks.value = dates
        }
    }
}
```

#### Applying Filters
```kotlin
fun setFilter(filter: TaskFilter) {
    _currentFilter.value = filter
    applyFilter(filter)
}

private fun applyFilter(filter: TaskFilter) {
    val filteredTasks = when (filter) {
        TaskFilter.ALL -> _allTasks.value
        TaskFilter.MY_TASKS -> _allTasks.value.filter { 
            it.userId == currentUserId 
        }
        TaskFilter.GROUP_TASKS -> _allTasks.value.filter { 
            !it.groupId.isNullOrEmpty() 
        }
    }
    _tasks.value = filteredTasks
    updateTasksForSelectedDate()
}
```

## Category Values

### Task Categories
- `"personal"` - Personal tasks
- `"group"` - Group tasks
- `"assignment"` - Assignments
- `"all"` - All tasks (filter value)

### Task Status
- `"pending"` - Not yet completed
- `"completed"` - Completed
- `"overdue"` - Past due date

### Task Priority
- `"low"` - Low priority
- `"medium"` - Medium priority
- `"high"` - High priority

## Layout IDs

### TasksFragment (fragment_tasks.xml)
```xml
<!-- Task List -->
<RecyclerView android:id="@+id/recycler_tasks" />

<!-- Empty State -->
<LinearLayout android:id="@+id/empty_state_layout" />
<TextView android:id="@+id/empty_state_text" />

<!-- Stats -->
<TextView android:id="@+id/tv_overdue_count" />
<TextView android:id="@+id/tv_due_today_count" />
<TextView android:id="@+id/tv_completed_count" />

<!-- Category Buttons -->
<MaterialButton android:id="@+id/btn_all_tasks" />
<MaterialButton android:id="@+id/btn_personal" />
<MaterialButton android:id="@+id/btn_group" />
<MaterialButton android:id="@+id/btn_assignments" />
```

### CalendarFragment (fragment_calendar.xml)
```xml
<!-- Calendar -->
<CalendarView android:id="@+id/calendarView" />

<!-- Navigation -->
<ImageButton android:id="@+id/previousMonthButton" />
<ImageButton android:id="@+id/nextMonthButton" />
<TextView android:id="@+id/monthYearText" />

<!-- Filter Chips -->
<Chip android:id="@+id/chipAllTasks" />
<Chip android:id="@+id/chipMyTasks" />
<Chip android:id="@+id/chipGroupTasks" />

<!-- Task List -->
<TextView android:id="@+id/selectedDateText" />
<RecyclerView android:id="@+id/tasksRecyclerView" />
<LinearLayout android:id="@+id/emptyStateLayout" />
<ProgressBar android:id="@+id/loadingIndicator" />
```

## Common Patterns

### Pattern 1: Real-time Task Collection
```kotlin
// In Fragment
taskRepository.getUserTasks(category)
    .collectWithLifecycle(viewLifecycleOwner) { tasks ->
        // Update UI
    }
```

### Pattern 2: Filter Change
```kotlin
// Update filter and re-setup listeners
currentFilter = "personal"
setupRealTimeListeners()
```

### Pattern 3: Empty State Management
```kotlin
if (tasks.isEmpty()) {
    showEmptyState()
} else {
    showTaskList()
}
```

### Pattern 4: Date-based Filtering
```kotlin
// In ViewModel
private fun updateTasksForSelectedDate() {
    val selected = _selectedDate.value
    val tasksForDate = _tasks.value.filter { task ->
        task.dueDate?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate() == selected
    }
    _tasksForSelectedDate.value = tasksForDate
}
```

## Testing Commands

### Build Project
```bash
./gradlew build
```

### Run on Device
```bash
./gradlew installDebug
```

### Check for Errors
```bash
./gradlew check
```

## Troubleshooting

### Issue: Tasks not showing
**Solution**: Check Firestore rules, ensure user is authenticated, verify tasks exist in Firestore

### Issue: Real-time updates not working
**Solution**: Check listener setup, verify Flow collection, ensure lifecycle is active

### Issue: Empty state not showing
**Solution**: Verify layout IDs match code, check visibility logic, ensure empty state view exists

### Issue: Category filter not working
**Solution**: Check category values match Firestore data, verify filter logic, ensure listener re-setup

### Issue: Calendar dots not appearing
**Solution**: Verify getDatesWithTasks() is collecting, check date conversion logic, ensure tasks have due dates

## Performance Tips

1. **Use lifecycle-aware collection**: Always use `collectWithLifecycle` to prevent memory leaks
2. **Minimize listener re-creation**: Only re-setup listeners when filter actually changes
3. **Efficient filtering**: Filter in repository when possible, not in UI
4. **Proper cleanup**: Listeners auto-cleanup with `awaitClose { listener.remove() }`

## Best Practices

1. **Always handle empty states**: Provide clear feedback when no data
2. **Show loading indicators**: Let users know data is loading
3. **Handle errors gracefully**: Don't crash on network errors
4. **Use proper data types**: LocalDate for dates, Timestamp for Firestore
5. **Follow MVVM**: Keep business logic in ViewModel/Repository

## Quick Debugging

### Check if tasks exist in Firestore
```kotlin
lifecycleScope.launch {
    val tasks = taskRepository.getUserTasks()
    Log.d("DEBUG", "Task count: ${tasks.size}")
}
```

### Check current filter
```kotlin
Log.d("DEBUG", "Current filter: $currentFilter")
```

### Check Flow collection
```kotlin
taskRepository.getUserTasks(null).collect { tasks ->
    Log.d("DEBUG", "Received ${tasks.size} tasks")
}
```

### Check empty state visibility
```kotlin
Log.d("DEBUG", "Empty state visible: ${emptyStateView?.visibility == View.VISIBLE}")
Log.d("DEBUG", "RecyclerView visible: ${recyclerTasks.visibility == View.VISIBLE}")
```

## Related Files

- `TaskRepository.kt` - Data access layer
- `TasksFragment.kt` - Tasks screen UI
- `CalendarViewModel.kt` - Calendar business logic
- `CalendarFragment.kt` - Calendar screen UI
- `FirebaseTask.kt` - Task data model
- `fragment_tasks.xml` - Tasks screen layout
- `fragment_calendar.xml` - Calendar screen layout

## Next Steps

After Task 5, proceed to:
- **Task 6**: Fix Chat Functionality and Firestore Rules
- **Task 7**: Update Firestore Security Rules
- **Task 8**: Remove All Demo Data Dependencies
- **Task 9**: Implement Comprehensive Error Handling
- **Task 10**: Implement Real-time Data Updates
- **Task 11**: Final Testing and Verification
