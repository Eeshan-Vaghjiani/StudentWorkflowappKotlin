# Task 5 Quick Reference Guide

## What Was Implemented
Replaced all hardcoded demo data in the HomeFragment dashboard with real-time Firestore queries.

## Files Changed

### Created
- `app/src/main/java/com/example/loginandregistration/models/DashboardStats.kt`

### Modified
- `app/src/main/java/com/example/loginandregistration/HomeFragment.kt`
- `app/src/main/java/com/example/loginandregistration/repository/UserRepository.kt`

## Key Components

### DashboardStats Data Class
```kotlin
data class DashboardStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val tasksDue: Int = 0,
    val activeGroups: Int = 0,
    val aiUsageCount: Int = 0,
    val aiUsageLimit: Int = 10,
    val totalSessions: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### HomeFragment Methods

#### Data Collection
- `collectTaskStats(userId)` - Collects real-time task statistics
- `collectGroupStats(userId)` - Collects real-time group statistics
- `collectAIStats(userId)` - Collects real-time AI usage statistics
- `collectSessionStats(userId)` - Placeholder for session statistics

#### UI Updates
- `updateTaskStatsUI(tasksDue, total, completed, overdue)` - Updates task UI
- `updateGroupStatsUI(activeGroups)` - Updates group UI
- `updateAIStatsUI(used, limit)` - Updates AI usage UI

#### State Management
- `showLoadingState()` - Shows loading indicators
- `showErrorState(message)` - Shows error state with fallback values
- `retryLoadDashboardData()` - Retries loading after error

### UserRepository Enhancement
```kotlin
fun getCurrentUserProfileFlow(): Flow<FirebaseUser?> = callbackFlow {
    // Returns real-time user profile updates
}
```

## Data Flow

```
User Opens Home Screen
    ↓
loadDashboardData()
    ↓
showLoadingState() (shows "...")
    ↓
Launch 4 parallel coroutines:
    ├── collectTaskStats() → TaskRepository.getUserTasksFlow()
    ├── collectGroupStats() → GroupRepository.getUserGroupsFlow()
    ├── collectAIStats() → UserRepository.getCurrentUserProfileFlow()
    └── collectSessionStats() → Placeholder (shows "0")
    ↓
Each Flow emits data from Firestore
    ↓
Update DashboardStats model
    ↓
Update UI with real data
```

## Real-time Updates

All statistics use Firestore snapshot listeners via Kotlin Flow:
- **Tasks**: Updates when tasks are created, modified, or deleted
- **Groups**: Updates when groups are joined, created, or modified
- **AI Usage**: Updates when AI assistant is used
- **No manual refresh needed**

## Error Handling

### Flow Level
```kotlin
.catch { e ->
    Log.e(TAG, "Error collecting stats", e)
    updateUI(fallbackValues)
}
```

### Coroutine Level
```kotlin
try {
    launch { collectTaskStats(userId) }
    // ...
} catch (e: Exception) {
    showErrorState(e.message ?: "Failed to load")
}
```

## Testing Quick Commands

### Check Compilation
```bash
./gradlew :app:compileDebugKotlin
```

### Run App
```bash
./gradlew :app:installDebug
```

### Check Logcat
```bash
adb logcat | grep HomeFragment
```

## Common Issues & Solutions

### Issue: "Unresolved reference: Timestamp"
**Solution:** Add import: `import com.google.firebase.Timestamp`

### Issue: "Unresolved reference: catch"
**Solution:** Add import: `import kotlinx.coroutines.flow.catch`

### Issue: Data not updating in real-time
**Solution:** 
1. Check Firestore rules are deployed
2. Check internet connection
3. Check logcat for errors
4. Verify Flow is being collected

### Issue: "PERMISSION_DENIED" errors
**Solution:** Deploy Firestore rules (Task 1)

### Issue: "FAILED_PRECONDITION" errors
**Solution:** Create Firestore indexes (Task 1)

## Quick Test Scenarios

### Test 1: Initial Load
1. Open app
2. Navigate to Home
3. Should see loading indicators briefly
4. Should see real data load

### Test 2: Real-time Task Update
1. Open Home screen (note task count)
2. Navigate to Tasks screen
3. Create new task
4. Navigate back to Home
5. Task count should increase immediately

### Test 3: Real-time Group Update
1. Open Home screen (note group count)
2. Navigate to Groups screen
3. Join or create group
4. Navigate back to Home
5. Group count should increase immediately

### Test 4: Error Handling
1. Turn off internet
2. Open Home screen
3. Should see error toast
4. Should see "0" fallback values
5. Turn on internet
6. Pull to refresh
7. Should load data successfully

## Code Snippets

### Adding New Statistic
```kotlin
// 1. Add field to DashboardStats
data class DashboardStats(
    // ...
    val newStat: Int = 0
)

// 2. Create collection method
private suspend fun collectNewStat(userId: String) {
    repository.getNewStatFlow()
        .catch { e ->
            Log.e(TAG, "Error collecting new stat", e)
            updateNewStatUI(0)
        }
        .collect { data ->
            currentStats = currentStats.copy(newStat = data)
            updateNewStatUI(data)
        }
}

// 3. Create UI update method
private fun updateNewStatUI(value: Int) {
    binding.tvNewStat.text = value.toString()
}

// 4. Add to loadDashboardData()
launch { collectNewStat(userId) }
```

### Adding Retry Button
```kotlin
// In layout XML
<Button
    android:id="@+id/btnRetry"
    android:text="Retry"
    android:visibility="gone" />

// In HomeFragment
private fun showErrorState(message: String) {
    // ... existing code ...
    binding.btnRetry.visibility = View.VISIBLE
}

private fun setupClickListeners() {
    // ... existing code ...
    binding.btnRetry.setOnClickListener {
        binding.btnRetry.visibility = View.GONE
        retryLoadDashboardData()
    }
}
```

## Performance Tips

1. **Use Flow instead of LiveData** - Better for Firestore real-time updates
2. **Cancel jobs in onDestroyView()** - Prevents memory leaks
3. **Use viewLifecycleOwner.lifecycleScope** - Proper lifecycle management
4. **Batch UI updates** - Update multiple fields at once
5. **Use .catch {} on Flows** - Prevents crashes from stream errors

## Next Steps

After verifying Task 5:
1. Test all real-time updates work
2. Test error handling
3. Test lifecycle management
4. Move to Task 6: Fix Groups Display
5. Move to Task 7: Fix Tasks Display
6. Move to Task 8: Integrate Tasks with Calendar

## Support

If you encounter issues:
1. Check logcat for errors
2. Verify Firestore rules are deployed
3. Verify Firestore indexes are created
4. Check internet connection
5. Review this guide's "Common Issues" section
6. Check Firebase Console for data structure
