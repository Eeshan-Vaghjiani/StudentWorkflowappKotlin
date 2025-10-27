# Error Handling Framework Guide

This guide explains how to use the error handling framework implemented in the TeamSync app.

## Overview

The error handling framework consists of three main components:

1. **AppError sealed class** - Categorizes different types of errors
2. **ErrorHandler utility** - Provides UI feedback for errors
3. **safeFirestoreCall extension function** - Wraps Firestore operations with automatic error handling

## Components

### 1. AppError Sealed Class

Located in `ErrorHandler.kt`, the `AppError` sealed class categorizes errors:

```kotlin
sealed class AppError {
    data class NetworkError(val message: String, val exception: Exception? = null) : AppError()
    data class AuthError(val message: String, val exception: Exception? = null) : AppError()
    data class PermissionError(val message: String, val permission: String? = null) : AppError()
    data class StorageError(val message: String, val exception: Exception? = null) : AppError()
    data class ValidationError(val message: String) : AppError()
    data class FirestoreError(val message: String, val exception: Exception? = null) : AppError()
    data class UnknownError(val message: String, val exception: Exception? = null) : AppError()
}
```

### 2. ErrorHandler Utility

The `ErrorHandler` object provides methods to display errors to users:

```kotlin
// Handle any exception automatically
ErrorHandler.handleError(context, exception, view, onRetry)

// Show specific error types
ErrorHandler.handleNetworkError(context, view, onRetry)
ErrorHandler.handleAuthError(context, message)
ErrorHandler.handleValidationError(context, message)
ErrorHandler.handlePermissionDenied(context, permissionName, rationale)
```

### 3. safeFirestoreCall Extension Function

Wraps Firestore operations and converts exceptions to `Result<T>`:

```kotlin
suspend fun <T> safeFirestoreCall(block: suspend () -> T): Result<T>
```

## Usage in Repositories

### Before (without error handling):

```kotlin
suspend fun getUserGroups(): List<FirebaseGroup> {
    val userId = auth.currentUser?.uid ?: return emptyList()
    return try {
        groupsCollection
            .whereArrayContains("memberIds", userId)
            .get()
            .await()
            .toObjects(FirebaseGroup::class.java)
    } catch (e: Exception) {
        emptyList()
    }
}
```

### After (with error handling):

```kotlin
suspend fun getUserGroups(): Result<List<FirebaseGroup>> {
    val userId = auth.currentUser?.uid ?: return Result.success(emptyList())
    return safeFirestoreCall {
        groupsCollection
            .whereArrayContains("memberIds", userId)
            .get()
            .await()
            .toObjects(FirebaseGroup::class.java)
    }
}
```

## Usage in ViewModels

ViewModels should:
1. Expose error state as `StateFlow<AppError?>`
2. Handle Result types from repositories
3. Convert exceptions to AppError types

### Example ViewModel:

```kotlin
class GroupsViewModel : ViewModel() {
    private val repository = GroupRepository()

    private val _groups = MutableStateFlow<List<FirebaseGroup>>(emptyList())
    val groups: StateFlow<List<FirebaseGroup>> = _groups.asStateFlow()

    private val _error = MutableStateFlow<ErrorHandler.AppError?>(null)
    val error: StateFlow<ErrorHandler.AppError?> = _error.asStateFlow()

    fun loadUserGroups() {
        viewModelScope.launch {
            val result = repository.getUserGroups()
            
            result.fold(
                onSuccess = { groupList ->
                    _groups.value = groupList
                },
                onFailure = { exception ->
                    _error.value = when (exception) {
                        is ErrorHandler.AppError -> exception
                        else -> ErrorHandler.AppError.UnknownError(
                            exception.message ?: "Failed to load groups",
                            exception as? Exception
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}
```

## Usage in Activities/Fragments

Activities and Fragments should:
1. Observe error state from ViewModel
2. Use ErrorHandler to display errors to users
3. Clear errors after displaying

### Example Activity:

```kotlin
class GroupsActivity : AppCompatActivity() {
    private val viewModel: GroupsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Observe error state
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    // Display error using ErrorHandler
                    ErrorHandler.handleError(
                        context = this@GroupsActivity,
                        exception = when (it) {
                            is ErrorHandler.AppError.NetworkError -> 
                                Exception(it.message)
                            is ErrorHandler.AppError.FirestoreError -> 
                                it.exception ?: Exception(it.message)
                            // ... handle other error types
                            else -> Exception(it.getMessage())
                        },
                        view = binding.root,
                        onRetry = { viewModel.loadUserGroups() }
                    )
                    
                    // Clear error after displaying
                    viewModel.clearError()
                }
            }
        }
    }
}
```

### Alternative: Direct Error Handling

You can also handle errors directly without converting back to exceptions:

```kotlin
lifecycleScope.launch {
    viewModel.error.collect { error ->
        error?.let {
            when (it) {
                is ErrorHandler.AppError.NetworkError -> {
                    ErrorHandler.showNetworkErrorSnackbar(
                        this@GroupsActivity,
                        binding.root,
                        it.message,
                        onRetry = { viewModel.loadUserGroups() }
                    )
                }
                is ErrorHandler.AppError.PermissionError -> {
                    ErrorHandler.showPermissionErrorDialog(
                        this@GroupsActivity,
                        it.message,
                        it.permission
                    )
                }
                is ErrorHandler.AppError.ValidationError -> {
                    ErrorHandler.showValidationErrorToast(
                        this@GroupsActivity,
                        it.message
                    )
                }
                else -> {
                    ErrorHandler.showErrorToast(
                        this@GroupsActivity,
                        it.getMessage()
                    )
                }
            }
            viewModel.clearError()
        }
    }
}
```

## Error Types and When to Use Them

### NetworkError
- No internet connection
- Service unavailable
- Timeout errors

**Example:**
```kotlin
ErrorHandler.AppError.NetworkError(
    "No internet connection. Please check your network and try again.",
    exception
)
```

### AuthError
- User not authenticated
- Invalid credentials
- Token expired

**Example:**
```kotlin
ErrorHandler.AppError.AuthError(
    "Please sign in to continue.",
    exception
)
```

### PermissionError
- Firestore permission denied
- Missing app permissions
- Unauthorized access

**Example:**
```kotlin
ErrorHandler.AppError.PermissionError(
    "You don't have permission to access this data.",
    null
)
```

### StorageError
- File upload failed
- File not found
- Storage quota exceeded

**Example:**
```kotlin
ErrorHandler.AppError.StorageError(
    "Failed to upload file. Please try again.",
    exception
)
```

### ValidationError
- Invalid user input
- Missing required fields
- Format errors

**Example:**
```kotlin
ErrorHandler.AppError.ValidationError(
    "Please enter a valid email address."
)
```

### FirestoreError
- Database operation failed
- Document not found
- Query errors

**Example:**
```kotlin
ErrorHandler.AppError.FirestoreError(
    "Failed to save data. Please try again.",
    exception
)
```

### UnknownError
- Unexpected errors
- Unhandled exceptions

**Example:**
```kotlin
ErrorHandler.AppError.UnknownError(
    "An unexpected error occurred. Please try again.",
    exception
)
```

## Best Practices

1. **Always use safeFirestoreCall for Firestore operations**
   ```kotlin
   return safeFirestoreCall {
       firestore.collection("users").document(userId).get().await()
   }
   ```

2. **Return Result types from repositories**
   ```kotlin
   suspend fun createGroup(...): Result<String>
   ```

3. **Expose error state in ViewModels**
   ```kotlin
   private val _error = MutableStateFlow<ErrorHandler.AppError?>(null)
   val error: StateFlow<ErrorHandler.AppError?> = _error.asStateFlow()
   ```

4. **Handle errors in UI layer**
   ```kotlin
   viewModel.error.collect { error ->
       error?.let { ErrorHandler.handleError(...) }
   }
   ```

5. **Always clear errors after displaying**
   ```kotlin
   viewModel.clearError()
   ```

6. **Provide retry options for recoverable errors**
   ```kotlin
   ErrorHandler.showErrorSnackbar(
       context, view, message,
       onRetry = { viewModel.retryOperation() }
   )
   ```

7. **Log errors for debugging**
   - ErrorHandler automatically logs errors to Logcat and Firebase Crashlytics
   - No need to manually log errors when using ErrorHandler

## Migration Checklist

To migrate existing code to use the error handling framework:

- [ ] Update repository methods to return `Result<T>` instead of nullable types
- [ ] Wrap Firestore operations with `safeFirestoreCall`
- [ ] Add error state to ViewModels (`StateFlow<AppError?>`)
- [ ] Update ViewModel methods to handle Result types
- [ ] Add error observation in Activities/Fragments
- [ ] Use ErrorHandler to display errors to users
- [ ] Test error scenarios (network errors, permission errors, etc.)

## Testing Error Handling

### Test Network Errors
1. Turn off device internet
2. Attempt operations
3. Verify error messages are shown
4. Turn on internet
5. Verify retry works

### Test Permission Errors
1. Deploy incorrect security rules
2. Attempt operations
3. Verify permission error messages
4. Fix security rules
5. Verify operations work

### Test Validation Errors
1. Enter invalid data
2. Verify validation error messages
3. Enter valid data
4. Verify operations succeed

## Examples

See the following files for complete examples:
- `GroupsViewModel.kt` - Example ViewModel with error handling
- `TasksViewModel.kt` - Example ViewModel with error handling
- `GroupRepository.kt` - Example repository with safeFirestoreCall
- `TaskRepository.kt` - Example repository with safeFirestoreCall
