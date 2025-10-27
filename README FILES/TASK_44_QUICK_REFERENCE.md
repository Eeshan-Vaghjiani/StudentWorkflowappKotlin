# Task 44: Lifecycle-Aware Listeners - Quick Reference

## What Was Implemented

Lifecycle-aware Firestore listener management to prevent memory leaks and optimize resource usage.

## Key Files Changed

1. **New File**: `utils/LifecycleAwareFlowCollector.kt` - Utility functions for lifecycle-aware Flow collection
2. **Updated**: `viewmodels/ChatViewModel.kt` - Added lifecycle logging and proper cleanup
3. **Updated**: `viewmodels/ChatRoomViewModel.kt` - Added lifecycle logging and proper cleanup
4. **Updated**: `ChatFragment.kt` - Using lifecycle-aware collection
5. **Updated**: `TasksFragment.kt` - Using lifecycle-aware collection
6. **Updated**: `GroupsFragment.kt` - Using lifecycle-aware collection

## How to Use

### In Fragments

**Before (Memory Leak Risk)**:
```kotlin
lifecycleScope.launch {
    viewModel.dataFlow.collect { data ->
        updateUI(data)
    }
}
```

**After (Lifecycle-Aware)**:
```kotlin
viewModel.dataFlow.collectWithLifecycle(viewLifecycleOwner) { data ->
    updateUI(data)
}
```

### In ViewModels

**Always use `viewModelScope`**:
```kotlin
class MyViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            // Your coroutine code
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // viewModelScope automatically cancels all coroutines
    }
}
```

## Benefits

✅ **No Memory Leaks**: Listeners automatically detach when Fragment stops
✅ **Better Performance**: No unnecessary network/database operations in background
✅ **Battery Savings**: Reduced CPU and network usage when app is backgrounded
✅ **Cost Savings**: Fewer Firestore read operations

## Testing

### Quick Test
1. Open app and navigate to Chat tab
2. Check logcat for: `ChatFragment: onStart - Firestore listeners will be attached`
3. Press Home button
4. Check logcat for: `ChatFragment: onStop - Firestore listeners will be detached`
5. Return to app
6. Check logcat for: `ChatFragment: onStart - Firestore listeners will be attached`

### Memory Leak Test
1. Open Android Profiler → Memory
2. Navigate between tabs 10 times
3. Force garbage collection
4. Check memory graph - should stabilize, not grow continuously

## Lifecycle States

```
CREATED → STARTED → RESUMED
          ↑         ↓
          STOPPED ← PAUSED
```

- **STARTED**: Listeners attach, data flows
- **STOPPED**: Listeners detach, no data flow
- **DESTROYED**: ViewModel cleared, all coroutines cancelled

## Common Patterns

### Pattern 1: Collect StateFlow
```kotlin
viewModel.stateFlow.collectWithLifecycle(viewLifecycleOwner) { state ->
    // Update UI
}
```

### Pattern 2: Collect Multiple Flows
```kotlin
viewModel.flow1.collectWithLifecycle(viewLifecycleOwner) { data1 ->
    // Handle data1
}

viewModel.flow2.collectWithLifecycle(viewLifecycleOwner) { data2 ->
    // Handle data2
}
```

### Pattern 3: ViewModel with Flow
```kotlin
class MyViewModel : ViewModel() {
    private val repository = MyRepository()
    
    val dataFlow = repository.getData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

## Troubleshooting

### Issue: Data not updating
**Solution**: Make sure you're using `viewLifecycleOwner`, not `this`

### Issue: Memory leaks
**Solution**: Verify you're using `collectWithLifecycle()` instead of `lifecycleScope.launch { collect {} }`

### Issue: Listeners not detaching
**Solution**: Check that `viewModelScope` is used in ViewModel, not `GlobalScope`

## Documentation

- **Implementation Summary**: `TASK_44_IMPLEMENTATION_SUMMARY.md`
- **Testing Guide**: `TASK_44_TESTING_GUIDE.md`
- **Verification Checklist**: `TASK_44_VERIFICATION_CHECKLIST.md`

## Requirements Satisfied

✅ 10.6: Detach Firestore listeners in onStop()
✅ 10.7: Re-attach listeners in onStart()
✅ Cancel coroutines in ViewModel onCleared()
✅ Use viewModelScope for coroutines
✅ Avoid memory leaks

## Next Steps

1. Complete manual testing with Android Profiler
2. Extend to remaining fragments (Calendar, Profile, Home)
3. Add automated tests
4. Monitor production metrics

## Key Takeaways

- Use `collectWithLifecycle()` for all Flow collection in Fragments
- Use `viewModelScope` for all coroutines in ViewModels
- Use `viewLifecycleOwner` in Fragments, not `this`
- Listeners automatically manage themselves - no manual cleanup needed
- Memory leaks are prevented by design
