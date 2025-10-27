# Fragment Lifecycle Best Practices

## Overview

This guide provides essential patterns and practices for managing Fragment lifecycles in Android, with a focus on preventing crashes related to view binding and coroutine management. These practices are based on lessons learned from production crashes in the TeamSync Collaboration app.

## Table of Contents

1. [Safe View Binding Pattern](#safe-view-binding-pattern)
2. [Coroutine Lifecycle Management](#coroutine-lifecycle-management)
3. [Common Pitfalls and Solutions](#common-pitfalls-and-solutions)
4. [Code Review Checklist](#code-review-checklist)
5. [Examples and Anti-Patterns](#examples-and-anti-patterns)

---

## Safe View Binding Pattern

### The Problem

When using View Binding in Fragments, the binding reference must be cleared in `onDestroyView()` to prevent memory leaks. However, this creates a window where coroutines or callbacks can attempt to access the binding after it's been set to null, causing `NullPointerException` crashes.

```kotlin
// ❌ DANGEROUS PATTERN - DO NOT USE
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!  // Throws NPE if accessed after onDestroyView
    
    private fun updateUI() {
        binding.textView.text = "Hello"  // CRASH if view is destroyed!
    }
}
```

### The Solution

Always use a nullable getter and check for null before accessing the binding:

```kotlin
// ✅ SAFE PATTERN - USE THIS
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding: FragmentMyBinding?
        get() = _binding  // Returns null safely
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return _binding!!.root  // Safe here - just created
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clear reference to prevent memory leaks
    }
    
    private fun updateUI() {
        // Early return pattern
        val binding = _binding ?: run {
            Log.d(TAG, "Cannot update UI: view is destroyed")
            return
        }
        
        // Safe to access binding here
        binding.textView.text = "Hello"
    }
}
```

### Key Principles

1. **Always use nullable getter**: `get() = _binding` (not `get() = _binding!!`)
2. **Check before access**: Use early return pattern or safe call operator
3. **Clear in onDestroyView**: Set `_binding = null` to prevent memory leaks
4. **Log when skipping**: Help with debugging by logging when UI updates are skipped

---

## Coroutine Lifecycle Management

### The Problem

Coroutines that outlive the fragment's view can attempt to update UI after the view is destroyed, causing crashes or unexpected behavior.

```kotlin
// ❌ DANGEROUS PATTERN - DO NOT USE
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Wrong scope - survives view destruction
        lifecycleScope.launch {
            repository.getData().collect { data ->
                binding.textView.text = data  // CRASH if view destroyed!
            }
        }
    }
}
```

### The Solution

Use `viewLifecycleOwner.lifecycleScope` for UI-related coroutines and always check view existence before updating UI:

```kotlin
// ✅ SAFE PATTERN - USE THIS
class MyFragment : Fragment() {
    private var dataJob: Job? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }
    
    private fun loadData() {
        // Cancel any existing job
        dataJob?.cancel()
        
        // Use viewLifecycleOwner - automatically cancelled on view destruction
        dataJob = viewLifecycleOwner.lifecycleScope.launch {
            repository.getData()
                .catch { exception ->
                    Log.e(TAG, "Error loading data", exception)
                    
                    // Only update UI if view still exists
                    if (_binding != null && isAdded) {
                        showError(exception.message ?: "Unknown error")
                    } else {
                        Log.d(TAG, "View destroyed, skipping error UI")
                    }
                }
                .collect { data ->
                    // Check view exists before updating UI
                    _binding?.let { binding ->
                        binding.textView.text = data
                    }
                }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        dataJob?.cancel()
        dataJob = null
        _binding = null
    }
}
```

### Key Principles

1. **Use viewLifecycleOwner**: For UI-related coroutines, use `viewLifecycleOwner.lifecycleScope`
2. **Check view existence**: Always verify `_binding != null && isAdded` before UI updates
3. **Handle cancellation**: Catch `JobCancellationException` separately and don't show error UI
4. **Cancel in onDestroyView**: Explicitly cancel jobs for immediate cleanup
5. **Store job references**: Keep references to jobs so you can cancel them

---

## Common Pitfalls and Solutions

### Pitfall 1: Using `!!` Operator with Binding

**Problem:**
```kotlin
private val binding get() = _binding!!  // ❌ Throws NPE
```

**Solution:**
```kotlin
private val binding get() = _binding  // ✅ Returns null safely
```

---

### Pitfall 2: Accessing Binding in Callbacks

**Problem:**
```kotlin
button.setOnClickListener {
    binding.textView.text = "Clicked"  // ❌ May crash if view destroyed
}
```

**Solution:**
```kotlin
button.setOnClickListener {
    _binding?.textView?.text = "Clicked"  // ✅ Safe call operator
}
```

---

### Pitfall 3: Wrong Coroutine Scope

**Problem:**
```kotlin
lifecycleScope.launch {  // ❌ Survives view destruction
    updateUI()
}
```

**Solution:**
```kotlin
viewLifecycleOwner.lifecycleScope.launch {  // ✅ Cancelled with view
    updateUI()
}
```

---

### Pitfall 4: Not Checking View Existence in Error Handlers

**Problem:**
```kotlin
.catch { exception ->
    showError(exception.message)  // ❌ May crash if view destroyed
}
```

**Solution:**
```kotlin
.catch { exception ->
    if (_binding != null && isAdded) {  // ✅ Check before showing error
        showError(exception.message)
    } else {
        Log.d(TAG, "View destroyed, skipping error UI")
    }
}
```

---

### Pitfall 5: Not Handling JobCancellationException

**Problem:**
```kotlin
.catch { exception ->
    showError(exception.message)  // ❌ Shows error for normal cancellation
}
```

**Solution:**
```kotlin
.catch { exception ->
    when (exception) {
        is JobCancellationException -> {
            Log.d(TAG, "Job cancelled")  // ✅ Log only, don't show error
        }
        else -> {
            if (_binding != null && isAdded) {
                showError(exception.message)
            }
        }
    }
}
```

---

### Pitfall 6: Memory Leaks from Not Clearing Binding

**Problem:**
```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    // ❌ Forgot to clear binding - memory leak!
}
```

**Solution:**
```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // ✅ Always clear binding
}
```

---

### Pitfall 7: Updating UI in onDestroy Instead of onDestroyView

**Problem:**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    _binding = null  // ❌ Too late - view already destroyed
}
```

**Solution:**
```kotlin
override fun onDestroyView() {
    super.onDestroyView()
    _binding = null  // ✅ Clear when view is destroyed
}
```

---

## Code Review Checklist

When reviewing Fragment code, verify:

### View Binding Safety
- [ ] Binding property uses nullable getter: `get() = _binding`
- [ ] All UI update methods check `_binding != null` before access
- [ ] Binding is cleared in `onDestroyView()`: `_binding = null`
- [ ] No use of `!!` operator with binding property
- [ ] Safe call operator (`?.`) or null checks used consistently

### Coroutine Lifecycle
- [ ] UI-related coroutines use `viewLifecycleOwner.lifecycleScope`
- [ ] Background operations use appropriate scope (lifecycleScope or viewModelScope)
- [ ] View existence checked before UI updates in coroutines: `_binding != null && isAdded`
- [ ] Error handlers check view existence before showing error UI
- [ ] `JobCancellationException` handled separately (logged, not shown as error)
- [ ] Jobs cancelled in `onDestroyView()` for immediate cleanup

### Error Handling
- [ ] All coroutine error handlers are defensive
- [ ] Errors logged with appropriate context
- [ ] User-friendly error messages displayed when view exists
- [ ] Transient errors handled gracefully (retry logic)
- [ ] No crashes from error handling itself

### Resource Management
- [ ] All resources cleaned up in `onDestroyView()`
- [ ] No memory leaks from retained references
- [ ] Listeners and observers properly removed
- [ ] Jobs and coroutines properly cancelled

### Testing
- [ ] Fragment tested with rapid navigation
- [ ] Fragment tested with configuration changes (rotation)
- [ ] Fragment tested with background/foreground transitions
- [ ] Error scenarios tested (network errors, permission errors)

---

## Examples and Anti-Patterns

### Example 1: Safe UI Update Method

```kotlin
private fun showLoadingState() {
    // Early return if view destroyed
    val binding = _binding ?: run {
        Log.d(TAG, "Cannot show loading state: view is destroyed")
        return
    }
    
    Log.d(TAG, "Showing loading state")
    binding.progressBar.visibility = View.VISIBLE
    binding.contentLayout.visibility = View.GONE
    binding.errorLayout.visibility = View.GONE
}
```

### Example 2: Safe Data Collection

```kotlin
private fun observeData() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.dataFlow
            .catch { exception ->
                Log.e(TAG, "Error collecting data", exception)
                
                // Handle cancellation separately
                if (exception is JobCancellationException) {
                    Log.d(TAG, "Data collection cancelled")
                    return@catch
                }
                
                // Only show error if view exists
                if (_binding != null && isAdded) {
                    showError(exception.message ?: "Unknown error")
                }
            }
            .collect { data ->
                // Check view exists before updating
                _binding?.let { binding ->
                    binding.textView.text = data.toString()
                    binding.progressBar.visibility = View.GONE
                }
            }
    }
}
```

### Example 3: Safe Click Listener

```kotlin
private fun setupClickListeners() {
    _binding?.let { binding ->
        binding.retryButton.setOnClickListener {
            // Check view still exists when clicked
            _binding?.let {
                loadData()
            }
        }
        
        binding.closeButton.setOnClickListener {
            _binding?.let {
                findNavController().navigateUp()
            }
        }
    }
}
```

### Anti-Pattern 1: Unsafe Binding Access

```kotlin
// ❌ DON'T DO THIS
private fun updateUI(data: String) {
    binding.textView.text = data  // Will crash if view destroyed
}
```

```kotlin
// ✅ DO THIS INSTEAD
private fun updateUI(data: String) {
    _binding?.textView?.text = data
}
```

### Anti-Pattern 2: Wrong Scope for UI Updates

```kotlin
// ❌ DON'T DO THIS
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    lifecycleScope.launch {  // Wrong scope!
        delay(5000)
        binding.textView.text = "Updated"  // May crash
    }
}
```

```kotlin
// ✅ DO THIS INSTEAD
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    viewLifecycleOwner.lifecycleScope.launch {
        delay(5000)
        _binding?.textView?.text = "Updated"
    }
}
```

### Anti-Pattern 3: No Error Handling in Coroutines

```kotlin
// ❌ DON'T DO THIS
viewLifecycleOwner.lifecycleScope.launch {
    repository.getData().collect { data ->
        binding.textView.text = data  // No error handling!
    }
}
```

```kotlin
// ✅ DO THIS INSTEAD
viewLifecycleOwner.lifecycleScope.launch {
    repository.getData()
        .catch { exception ->
            if (_binding != null && isAdded) {
                showError(exception.message ?: "Error loading data")
            }
        }
        .collect { data ->
            _binding?.textView?.text = data
        }
}
```

---

## Quick Reference

### Safe Binding Template

```kotlin
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding: FragmentMyBinding?
        get() = _binding
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return _binding!!.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun updateUI() {
        val binding = _binding ?: return
        // Use binding safely here
    }
}
```

### Safe Coroutine Template

```kotlin
private var dataJob: Job? = null

private fun loadData() {
    dataJob?.cancel()
    
    dataJob = viewLifecycleOwner.lifecycleScope.launch {
        repository.getData()
            .catch { exception ->
                if (exception is JobCancellationException) {
                    Log.d(TAG, "Cancelled")
                    return@catch
                }
                
                if (_binding != null && isAdded) {
                    showError(exception.message)
                }
            }
            .collect { data ->
                _binding?.let { binding ->
                    // Update UI
                }
            }
    }
}

override fun onDestroyView() {
    super.onDestroyView()
    dataJob?.cancel()
    dataJob = null
    _binding = null
}
```

---

## Additional Resources

- [Android Fragment Lifecycle Documentation](https://developer.android.com/guide/fragments/lifecycle)
- [View Binding Guide](https://developer.android.com/topic/libraries/view-binding)
- [Kotlin Coroutines on Android](https://developer.android.com/kotlin/coroutines)
- [Lifecycle-aware Components](https://developer.android.com/topic/libraries/architecture/lifecycle)

---

## Summary

Following these best practices will help you avoid common Fragment lifecycle crashes:

1. **Always use nullable binding getter** - Never use `!!` operator
2. **Check view existence before UI updates** - Use early return pattern
3. **Use correct coroutine scope** - `viewLifecycleOwner.lifecycleScope` for UI operations
4. **Handle errors defensively** - Check view exists before showing error UI
5. **Clean up in onDestroyView** - Cancel jobs and clear binding
6. **Test thoroughly** - Rapid navigation, rotation, background transitions

Remember: **When in doubt, check if the view exists before accessing it!**
