# Fragment Code Review Checklist

Use this checklist when reviewing Fragment code to ensure lifecycle safety, proper resource management, and crash prevention.

---

## 🔒 View Binding Safety

### Binding Property Declaration
- [ ] Binding property uses nullable getter: `private val binding get() = _binding`
- [ ] No use of `!!` operator with binding property
- [ ] Backing field is properly declared: `private var _binding: FragmentXxxBinding? = null`

### Binding Lifecycle Management
- [ ] Binding is inflated in `onCreateView()`
- [ ] Binding is cleared in `onDestroyView()`: `_binding = null`
- [ ] `onCreateView()` returns `_binding!!.root` (safe because just created)
- [ ] No binding access in `onDestroy()` (too late)

### Binding Access Patterns
- [ ] All UI update methods check `_binding != null` before access
- [ ] Early return pattern used: `val binding = _binding ?: return`
- [ ] Safe call operator used where appropriate: `_binding?.textView?.text = "..."`
- [ ] No direct access to `binding` without null check
- [ ] Debug logs added when skipping UI updates due to destroyed view

### Common Binding Access Points
- [ ] Click listeners check binding before access
- [ ] Data observers check binding before updating UI
- [ ] Error handlers check binding before showing error UI
- [ ] Loading state methods check binding before updating UI
- [ ] Success state methods check binding before updating UI

---

## ⚙️ Coroutine Lifecycle Management

### Coroutine Scope Selection
- [ ] UI-related coroutines use `viewLifecycleOwner.lifecycleScope`
- [ ] Background operations use appropriate scope (`lifecycleScope` or `viewModelScope`)
- [ ] No use of `GlobalScope` (unless absolutely necessary and documented)
- [ ] Coroutines that update UI are tied to view lifecycle

### View Existence Checks
- [ ] All UI updates in coroutines check: `_binding != null && isAdded`
- [ ] Error handlers check view existence before showing error UI
- [ ] Success handlers check view existence before updating UI
- [ ] Flow collectors check view existence before each emission

### Job Management
- [ ] Job references stored for manual cancellation: `private var dataJob: Job? = null`
- [ ] Existing jobs cancelled before starting new ones: `dataJob?.cancel()`
- [ ] Jobs cancelled in `onDestroyView()` for immediate cleanup
- [ ] Job references cleared after cancellation: `dataJob = null`

### Coroutine Error Handling
- [ ] All coroutines have error handling (`.catch {}` or `try-catch`)
- [ ] `JobCancellationException` handled separately (logged, not shown as error)
- [ ] View existence checked before showing error UI
- [ ] Errors logged with appropriate context and stack traces
- [ ] User-friendly error messages provided

---

## 🛡️ Error Handling

### Defensive Error Handling
- [ ] All coroutine error handlers are defensive
- [ ] View existence checked before showing error UI
- [ ] Errors don't cause crashes themselves
- [ ] Error messages are user-friendly and actionable

### Error Categorization
- [ ] Permission errors handled with appropriate messages
- [ ] Network errors handled with retry options
- [ ] Cancellation errors logged but not shown to users
- [ ] Unexpected errors logged with full context

### Error UI Updates
- [ ] Error state methods check binding before access
- [ ] Error messages displayed only when view exists
- [ ] Loading states cleared when showing errors
- [ ] Retry mechanisms provided for recoverable errors

### Logging
- [ ] Errors logged with appropriate level (ERROR, WARN, DEBUG)
- [ ] Stack traces included for unexpected errors
- [ ] Context provided (view state, user action, etc.)
- [ ] No sensitive information in logs

---

## 🧹 Resource Management

### Cleanup in onDestroyView
- [ ] Binding cleared: `_binding = null`
- [ ] Jobs cancelled: `job?.cancel()` and `job = null`
- [ ] Listeners removed (if manually registered)
- [ ] Observers removed (if manually registered)
- [ ] Timers/handlers cancelled

### Memory Leak Prevention
- [ ] No long-lived references to Fragment or View
- [ ] No static references to Fragment or View
- [ ] Adapters cleared or nullified if needed
- [ ] RecyclerView adapters don't hold Fragment references

### Lifecycle Observers
- [ ] LiveData observers use `viewLifecycleOwner`
- [ ] Flow collectors use `viewLifecycleOwner.lifecycleScope`
- [ ] Manual observers removed in `onDestroyView()`

---

## 🧪 Testing Considerations

### Manual Testing Scenarios
- [ ] Fragment tested with rapid navigation (tap back quickly)
- [ ] Fragment tested with configuration changes (rotation)
- [ ] Fragment tested with background/foreground transitions
- [ ] Fragment tested with slow network (loading states)
- [ ] Fragment tested with error conditions (permission denied, network error)

### Edge Cases
- [ ] Fragment handles being destroyed during data loading
- [ ] Fragment handles errors occurring after view destruction
- [ ] Fragment handles multiple rapid navigation events
- [ ] Fragment handles process death and recreation

### Crash Prevention
- [ ] No `NullPointerException` from binding access
- [ ] No crashes from coroutine cancellation
- [ ] No crashes from error handling
- [ ] No crashes from lifecycle transitions

---

## 📋 Code Quality

### Code Organization
- [ ] Lifecycle methods in correct order (onCreate, onCreateView, onViewCreated, onDestroyView, onDestroy)
- [ ] UI update methods grouped together
- [ ] Coroutine launch methods grouped together
- [ ] Helper methods clearly named and documented

### Naming Conventions
- [ ] Binding backing field named `_binding`
- [ ] Binding property named `binding`
- [ ] Job references named descriptively (e.g., `dataJob`, `statsJob`)
- [ ] UI update methods clearly named (e.g., `showLoadingState`, `updateUI`)

### Documentation
- [ ] Complex lifecycle interactions documented
- [ ] Non-obvious null checks explained
- [ ] Error handling strategy documented
- [ ] Public methods have KDoc comments

---

## ✅ Quick Verification

Run through these quick checks:

1. **Search for `!!` operator** - Should only appear in `onCreateView()` return statement
2. **Search for `binding.`** - Each access should have null check
3. **Search for `lifecycleScope.launch`** - Should be `viewLifecycleOwner.lifecycleScope.launch` for UI operations
4. **Search for `.collect {`** - Should have `.catch {}` and view existence checks
5. **Check `onDestroyView()`** - Should clear binding and cancel jobs

---

## 🚨 Red Flags

Watch out for these common issues:

- ❌ `private val binding get() = _binding!!` - Will crash
- ❌ `binding.textView.text = "..."` without null check - May crash
- ❌ `lifecycleScope.launch { updateUI() }` - Wrong scope for UI updates
- ❌ Missing `_binding = null` in `onDestroyView()` - Memory leak
- ❌ No error handling in coroutines - Unhandled exceptions
- ❌ Showing error UI without checking view exists - May crash
- ❌ Not cancelling jobs in `onDestroyView()` - Delayed cleanup

---

## 📝 Review Comments Template

Use these templates when providing feedback:

### Binding Safety Issue
```
⚠️ Binding access is not safe here. The view may be destroyed when this code runs.

Consider using:
```kotlin
val binding = _binding ?: return
// Use binding safely here
```
```

### Wrong Coroutine Scope
```
⚠️ This coroutine should use `viewLifecycleOwner.lifecycleScope` instead of `lifecycleScope` 
because it updates UI. The current scope will survive view destruction and may cause crashes.
```

### Missing View Existence Check
```
⚠️ This UI update should check if the view exists before accessing binding:

```kotlin
if (_binding != null && isAdded) {
    showError(message)
}
```
```

### Missing Error Handling
```
⚠️ This coroutine needs error handling. Add a `.catch {}` block to handle exceptions gracefully:

```kotlin
.catch { exception ->
    if (exception is JobCancellationException) {
        Log.d(TAG, "Cancelled")
        return@catch
    }
    if (_binding != null && isAdded) {
        showError(exception.message)
    }
}
```
```

---

## 🎯 Approval Criteria

A Fragment is ready for approval when:

- ✅ All checklist items are verified
- ✅ No red flags present
- ✅ Manual testing completed successfully
- ✅ No crashes in rapid navigation scenarios
- ✅ Error handling is defensive and user-friendly
- ✅ Code follows established patterns
- ✅ Documentation is clear and complete

---

## 📚 Related Resources

- [Fragment Lifecycle Best Practices](./FRAGMENT_LIFECYCLE_BEST_PRACTICES.md)
- [Android Fragment Lifecycle](https://developer.android.com/guide/fragments/lifecycle)
- [View Binding Guide](https://developer.android.com/topic/libraries/view-binding)
- [Kotlin Coroutines Best Practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)

---

## 🔄 Checklist Updates

This checklist should be updated when:
- New lifecycle-related issues are discovered
- New patterns are established
- Android best practices change
- Team conventions evolve

Last updated: 2025-10-25
