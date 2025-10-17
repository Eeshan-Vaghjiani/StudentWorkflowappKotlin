# Error Handling Guide - Quick Reference

## How to Use ErrorHandler in Your Code

### Basic Error Handling

```kotlin
try {
    // Your operation
    val result = repository.someOperation()
} catch (e: Exception) {
    ErrorHandler.handleError(
        context = requireContext(),
        exception = e,
        view = view,  // Optional: for Snackbar
        onRetry = {   // Optional: retry callback
            // Retry logic
        }
    )
}
```

### Show Success Message

```kotlin
// After successful operation
ErrorHandler.showSuccessMessage(
    context = requireContext(),
    view = view,  // Optional: for Snackbar, null for Toast
    message = getString(R.string.operation_successful)
)
```

### Handle Specific Error Types

```kotlin
// Network error with retry
ErrorHandler.handleNetworkError(
    context = requireContext(),
    view = view,
    onRetry = { loadData() }
)

// Validation error
ErrorHandler.handleValidationError(
    context = requireContext(),
    message = "Please enter a valid email"
)

// Generic error with custom message
ErrorHandler.handleGenericError(
    context = requireContext(),
    view = view,
    message = "Something went wrong",
    onRetry = { retryOperation() }
)
```

## Connection Monitoring

### Setup in Fragment/Activity

```kotlin
private lateinit var connectionMonitor: ConnectionMonitor
private lateinit var offlineIndicator: View

override fun onCreateView(...): View {
    val view = inflater.inflate(...)
    
    // Initialize
    connectionMonitor = ConnectionMonitor(requireContext())
    offlineIndicator = view.findViewById(R.id.offlineIndicator)
    
    // Monitor connection
    setupConnectionMonitoring()
    
    return view
}

private fun setupConnectionMonitoring() {
    connectionMonitor.isConnected.collectWithLifecycle(viewLifecycleOwner) { isConnected ->
        offlineIndicator.visibility = if (isConnected) View.GONE else View.VISIBLE
        
        if (!isConnected) {
            Log.w(TAG, "Device is offline")
        }
    }
}
```

### Add Offline Indicator to Layout

```xml
<!-- Add to your layout -->
<include layout="@layout/view_offline_indicator"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

## Loading States

### Add Loading Skeleton to Layout

```xml
<!-- Add to your layout -->
<include
    android:id="@+id/loading_skeleton"
    layout="@layout/loading_skeleton_groups"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:visibility="gone" />
```

### Show/Hide Loading

```kotlin
private lateinit var loadingSkeleton: View
private var isLoading = false

private fun showLoading(show: Boolean) {
    isLoading = show
    loadingSkeleton.visibility = if (show) View.VISIBLE else View.GONE
    contentView.visibility = if (show) View.GONE else View.VISIBLE
}

// Show loading before data fetch
showLoading(true)

// Hide loading after data arrives
repository.getData().collectWithLifecycle(viewLifecycleOwner) { data ->
    if (isLoading) {
        showLoading(false)
    }
    updateUI(data)
}
```

## Repository Pattern with Result<T>

### Suspend Function with Result

```kotlin
suspend fun createItem(item: Item): Result<String> = withContext(Dispatchers.IO) {
    try {
        val docRef = firestore.collection("items").add(item).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.e(TAG, "Error creating item", e)
        Result.failure(e)
    }
}
```

### Using Result in UI

```kotlin
lifecycleScope.launch {
    try {
        val result = repository.createItem(item)
        
        if (result.isSuccess) {
            val itemId = result.getOrThrow()
            ErrorHandler.showSuccessMessage(
                requireContext(),
                view,
                "Item created successfully"
            )
        } else {
            val error = result.exceptionOrNull()
            ErrorHandler.handleError(
                requireContext(),
                error ?: Exception("Unknown error"),
                view
            )
        }
    } catch (e: Exception) {
        ErrorHandler.handleError(requireContext(), e, view) {
            // Retry
            createItem()
        }
    }
}
```

## Flow-based Data with Error Handling

```kotlin
// In Repository
fun getItems(): Flow<List<Item>> = callbackFlow {
    val listener = firestore.collection("items")
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to items", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val items = snapshot?.toObjects(Item::class.java) ?: emptyList()
            trySend(items)
        }
    
    awaitClose { listener.remove() }
}

// In Fragment/Activity
repository.getItems().collectWithLifecycle(viewLifecycleOwner) { items ->
    if (isLoading) {
        showLoading(false)
    }
    updateUI(items)
}
```

## SwipeRefreshLayout Integration

```kotlin
private lateinit var swipeRefreshLayout: SwipeRefreshLayout

private fun setupViews(view: View) {
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
    swipeRefreshLayout.setOnRefreshListener { refreshData() }
}

private fun refreshData() {
    swipeRefreshLayout.isRefreshing = true
    loadData()
}

private fun loadData() {
    lifecycleScope.launch {
        try {
            val data = repository.getData()
            updateUI(data)
            swipeRefreshLayout.isRefreshing = false
        } catch (e: Exception) {
            swipeRefreshLayout.isRefreshing = false
            ErrorHandler.handleError(requireContext(), e, view) {
                refreshData()
            }
        }
    }
}
```

## Firebase Crashlytics

### Automatic Logging
All errors handled by ErrorHandler are automatically logged to Crashlytics with:
- Error type (network, auth, firestore, storage, etc.)
- Error message
- Stack trace
- Custom keys for context

### Manual Logging
```kotlin
// Log custom event
FirebaseCrashlytics.getInstance().log("User performed action X")

// Set custom key
FirebaseCrashlytics.getInstance().setCustomKey("user_id", userId)

// Record exception
FirebaseCrashlytics.getInstance().recordException(exception)
```

## Best Practices

1. **Always provide retry callbacks for network operations**
   ```kotlin
   ErrorHandler.handleError(context, e, view) {
       retryOperation()
   }
   ```

2. **Use appropriate feedback for different operations**
   - Snackbar for operations with retry option
   - Toast for simple confirmations
   - Dialog for critical errors requiring user action

3. **Show loading states for all async operations**
   - Use skeletons for initial load
   - Use SwipeRefreshLayout for manual refresh
   - Use ProgressBar for button operations

4. **Monitor connection status in data-heavy screens**
   - Add offline indicator
   - Disable operations when offline
   - Queue operations for when connection returns

5. **Provide clear success feedback**
   - Confirm successful operations
   - Use consistent messaging
   - Keep messages brief and actionable

## Common Patterns

### Create Operation
```kotlin
btnCreate.setOnClickListener {
    lifecycleScope.launch {
        try {
            val result = repository.create(data)
            if (result.isSuccess) {
                ErrorHandler.showSuccessMessage(
                    requireContext(),
                    view,
                    getString(R.string.created_successfully)
                )
                dismiss()
            } else {
                ErrorHandler.handleGenericError(
                    requireContext(),
                    view,
                    getString(R.string.error_creating)
                )
            }
        } catch (e: Exception) {
            ErrorHandler.handleError(requireContext(), e, view) {
                btnCreate.performClick()
            }
        }
    }
}
```

### Load Data with Loading State
```kotlin
private fun loadData() {
    showLoading(true)
    
    repository.getData().collectWithLifecycle(viewLifecycleOwner) { data ->
        if (isLoading) {
            showLoading(false)
        }
        
        if (data.isEmpty()) {
            showEmptyState()
        } else {
            updateUI(data)
        }
    }
}
```

### Handle Form Validation
```kotlin
private fun validateAndSubmit() {
    val email = etEmail.text.toString()
    
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        ErrorHandler.handleValidationError(
            requireContext(),
            "Please enter a valid email"
        )
        return
    }
    
    submitForm()
}
```

## Available String Resources

```xml
<!-- Success Messages -->
<string name="operation_successful">Operation completed successfully</string>
<string name="group_created">Group created successfully</string>
<string name="group_joined">Joined group successfully</string>
<string name="task_created">Task created successfully</string>
<string name="task_updated">Task updated successfully</string>
<string name="message_sent">Message sent</string>
<string name="profile_updated">Profile updated successfully</string>

<!-- Offline -->
<string name="offline_message">No internet connection</string>
```

## Troubleshooting

### Crashlytics not logging
1. Ensure Firebase Crashlytics is initialized in Application class
2. Check that google-services.json is up to date
3. Force a crash to verify setup: `throw RuntimeException("Test crash")`
4. Check Firebase Console after 5-10 minutes

### Offline indicator not showing
1. Verify ConnectionMonitor is initialized
2. Check that view ID matches: `R.id.offlineIndicator`
3. Ensure collectWithLifecycle is used (not collect)
4. Test with airplane mode

### Loading skeleton not hiding
1. Verify showLoading(false) is called after data loads
2. Check that isLoading flag is properly managed
3. Ensure visibility is set correctly
4. Test with slow network to see loading state

### Retry not working
1. Verify onRetry callback is provided
2. Check that retry logic is correct
3. Ensure operation can be retried (idempotent)
4. Test with network errors
