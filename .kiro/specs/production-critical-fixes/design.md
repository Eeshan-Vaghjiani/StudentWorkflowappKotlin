# Design Document - Production Critical Fixes

## Overview

This design document outlines the technical approach to fix all critical production issues in the TeamSync Collaboration app. The fixes are organized into logical groups that can be implemented incrementally while maintaining app stability.

### Problem Summary

Based on logcat analysis, the app has the following critical issues:
1. **Firestore Permission Errors**: All queries to groups, tasks, and activities collections are failing with PERMISSION_DENIED
2. **Missing Firestore Indexes**: Task queries require a composite index on (userId, dueDate) that doesn't exist
3. **Gemini AI 404 Error**: Using deprecated model name `gemini-pro` instead of current model names
4. **Demo Data on Dashboard**: HomeFragment displays hardcoded statistics instead of real Firestore data
5. **Performance Issues**: Main thread blocking causing frame skips
6. **Missing Android 13+ Features**: OnBackInvokedCallback not enabled

### Solution Approach

We'll implement fixes in the following order:
1. Fix Firestore security rules (enables data access)
2. Create required Firestore indexes (enables queries)
3. Update Gemini AI model name (enables AI features)
4. Replace demo data with real Firestore queries (shows actual data)
5. Add proper error handling and loading states
6. Enable modern Android features
7. Optimize performance

## Architecture

### Current Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Home    │  │  Groups  │  │  Tasks   │  │ Calendar │   │
│  │ Fragment │  │ Fragment │  │ Fragment │  │ Fragment │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
        ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │    Group     │  │     Task     │  │     Chat     │     │
│  │  Repository  │  │  Repository  │  │  Repository  │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Firebase Firestore                       │   │
│  │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐    │   │
│  │  │ groups │  │ tasks  │  │ chats  │  │ users  │    │   │
│  │  └────────┘  └────────┘  └────────┘  └────────┘    │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

1. **Fragments**: UI layer that displays data and handles user interactions
2. **Repositories**: Data access layer that queries Firestore and manages real-time listeners
3. **ViewModels**: (Where implemented) Manages UI state and lifecycle
4. **Services**: External integrations (Gemini AI, FCM)

## Components and Interfaces

### 1. Firestore Security Rules Fix

#### Current Issue
The security rules are correctly written but the queries in the app are failing. Analysis shows:
- Rules use `memberIds` field for groups
- Rules use `participants` field for chats
- Rules use `userId` field for tasks
- All rules require authentication

#### Root Cause
Looking at the logcat errors:
```
Listen for Query(target=Query(groups where memberIdsarray_containsUzus7hlN3iV5RyP04aRXdVD8KpD3...
```

The query syntax is correct. The issue is likely:
1. The Firestore rules file hasn't been deployed to Firebase
2. OR the Firebase project configuration is incorrect
3. OR there's a mismatch between local rules and deployed rules

#### Solution
```kotlin
// Verify queries match security rules

// GroupRepository.kt - Correct query format
fun getUserGroups(userId: String): Flow<List<Group>> {
    return firestore.collection("groups")
        .whereArrayContains("memberIds", userId)
        .whereEqualTo("isActive", true)
        .orderBy("updatedAt", Query.Direction.DESCENDING)
        .snapshots()
}

// TaskRepository.kt - Correct query format
fun getUserTasks(userId: String): Flow<List<FirebaseTask>> {
    return firestore.collection("tasks")
        .whereEqualTo("userId", userId)
        .orderBy("dueDate", Query.Direction.ASCENDING)
        .snapshots()
}

// ChatRepository.kt - Correct query format
fun getUserChats(userId: String): Flow<List<Chat>> {
    return firestore.collection("chats")
        .whereArrayContains("participants", userId)
        .orderBy("lastMessageTime", Query.Direction.DESCENDING)
        .snapshots()
}
```

#### Deployment Steps
1. Verify `firestore.rules` file is correct (already verified above)
2. Deploy rules using Firebase CLI: `firebase deploy --only firestore:rules`
3. Verify deployment in Firebase Console
4. Test queries after deployment

### 2. Firestore Indexes Creation

#### Current Issue
```
FAILED_PRECONDITION: The query requires an index.
Query(tasks where userId==Uzus7hlN3iV5RyP04aRXdVD8KpD3 order by dueDate, __name__)
```

#### Solution
Update `firestore.indexes.json`:

```json
{
  "indexes": [
    {
      "collectionGroup": "tasks",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "userId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "dueDate",
          "order": "ASCENDING"
        }
      ]
    },
    {
      "collectionGroup": "groups",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "memberIds",
          "arrayConfig": "CONTAINS"
        },
        {
          "fieldPath": "isActive",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "updatedAt",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "chats",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "participants",
          "arrayConfig": "CONTAINS"
        },
        {
          "fieldPath": "lastMessageTime",
          "order": "DESCENDING"
        }
      ]
    }
  ],
  "fieldOverrides": []
}
```

#### Deployment
```bash
firebase deploy --only firestore:indexes
```

### 3. Gemini AI Model Update

#### Current Issue
```
404 - models/gemini-pro is not found for API version v1beta
```

#### Root Cause
The `gemini-pro` model name has been deprecated. Current available models:
- `gemini-1.5-flash` (recommended for most use cases, faster)
- `gemini-1.5-pro` (more capable, slower)
- `gemini-2.0-flash-exp` (experimental)

#### Solution
Update `GeminiAssistantService.kt`:

```kotlin
companion object {
    private const val TAG = "GeminiAssistantService"
    // Updated model name
    private const val MODEL_NAME = "gemini-1.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"
    private const val TIMEOUT_SECONDS = 30L
}
```

#### API Configuration
```kotlin
// Ensure proper error handling for API responses
private suspend fun callGeminiAPI(prompt: String): AIResponse {
    return withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topK", 40)
                    put("topP", 0.95)
                    put("maxOutputTokens", 2048) // Increased for better responses
                })
            }
            
            val request = Request.Builder()
                .url(BASE_URL + "?key=$apiKey")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed: ${response.code} - $responseBody")
                return@withContext AIResponse(
                    message = "I'm having trouble connecting to the AI service. Please try again later.",
                    success = false,
                    error = "HTTP ${response.code}: ${response.message}"
                )
            }
            
            // Parse response...
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            AIResponse(
                message = "Unable to reach AI service. Please check your internet connection.",
                success = false,
                error = e.message
            )
        }
    }
}
```

### 4. Dashboard Real Data Integration

#### Current Issue
HomeFragment displays hardcoded demo statistics instead of querying Firestore.

#### Solution Architecture

```kotlin
// HomeFragment.kt - Data Flow
class HomeFragment : Fragment() {
    private lateinit var taskRepository: TaskRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var userRepository: UserRepository
    
    private var statsJob: Job? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize repositories
        taskRepository = TaskRepository()
        groupRepository = GroupRepository()
        userRepository = UserRepository()
        
        // Load real-time stats
        loadDashboardStats()
    }
    
    private fun loadDashboardStats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Show loading state
        showLoadingState()
        
        statsJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Collect all stats in parallel
                launch { collectTaskStats(userId) }
                launch { collectGroupStats(userId) }
                launch { collectAIStats(userId) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard stats", e)
                showErrorState(e.message)
            }
        }
    }
    
    private suspend fun collectTaskStats(userId: String) {
        taskRepository.getUserTasks(userId).collect { tasks ->
            val total = tasks.size
            val completed = tasks.count { it.status == "completed" }
            val pending = tasks.count { it.status == "pending" }
            val overdue = tasks.count { task ->
                task.status != "completed" && 
                task.dueDate.toDate().before(Date())
            }
            
            updateTaskStats(total, completed, pending, overdue)
        }
    }
    
    private suspend fun collectGroupStats(userId: String) {
        groupRepository.getUserGroups(userId).collect { groups ->
            val activeGroups = groups.count { it.isActive }
            updateGroupStats(activeGroups)
        }
    }
    
    private suspend fun collectAIStats(userId: String) {
        userRepository.getUserProfile(userId).collect { user ->
            val aiUsageCount = user?.aiUsageCount ?: 0
            updateAIStats(aiUsageCount)
        }
    }
}
```

#### Data Models

```kotlin
// DashboardStats.kt - Data class for dashboard statistics
data class DashboardStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val activeGroups: Int = 0,
    val aiUsageCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### 5. Error Handling and Loading States

#### Error Handling Strategy

```kotlin
// SafeFirestoreCall.kt - Wrapper for Firestore operations
object SafeFirestoreCall {
    suspend fun <T> execute(
        operation: suspend () -> T,
        onError: (Exception) -> Unit = {}
    ): Result<T> {
        return try {
            Result.success(operation())
        } catch (e: FirebaseFirestoreException) {
            Log.e("SafeFirestoreCall", "Firestore error: ${e.code}", e)
            onError(e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("You don't have permission to access this data"))
                }
                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                    Result.failure(Exception("Service temporarily unavailable. Please try again"))
                }
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    Result.failure(Exception("Database configuration error. Please contact support"))
                }
                else -> {
                    Result.failure(Exception("An error occurred: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e("SafeFirestoreCall", "Unexpected error", e)
            onError(e)
            Result.failure(Exception("An unexpected error occurred"))
        }
    }
}
```

#### Loading States

```kotlin
// LoadingState.kt - Sealed class for UI states
sealed class LoadingState<out T> {
    object Loading : LoadingState<Nothing>()
    data class Success<T>(val data: T) : LoadingState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : LoadingState<Nothing>()
    object Empty : LoadingState<Nothing>()
}
```

### 6. Android Manifest Updates

#### OnBackInvokedCallback

```xml
<!-- AndroidManifest.xml -->
<application
    android:name=".TeamCollaborationApp"
    android:allowBackup="true"
    android:enableOnBackInvokedCallback="true"
    ...>
```

### 7. Performance Optimizations

#### Main Thread Optimization

```kotlin
// Repository pattern - All Firestore operations on background threads
class TaskRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val dispatcher = Dispatchers.IO
    
    fun getUserTasks(userId: String): Flow<List<FirebaseTask>> = flow {
        val snapshot = withContext(dispatcher) {
            firestore.collection("tasks")
                .whereEqualTo("userId", userId)
                .orderBy("dueDate")
                .get()
                .await()
        }
        
        val tasks = snapshot.documents.mapNotNull { doc ->
            doc.toObject(FirebaseTask::class.java)?.copy(id = doc.id)
        }
        
        emit(tasks)
    }.flowOn(dispatcher)
}
```

#### RecyclerView Optimization

```kotlin
// Use DiffUtil for efficient list updates
class TaskDiffCallback : DiffUtil.ItemCallback<FirebaseTask>() {
    override fun areItemsTheSame(oldItem: FirebaseTask, newItem: FirebaseTask): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: FirebaseTask, newItem: FirebaseTask): Boolean {
        return oldItem == newItem
    }
}

// Adapter with ListAdapter
class TaskAdapter : ListAdapter<FirebaseTask, TaskViewHolder>(TaskDiffCallback()) {
    // Implementation
}
```

## Data Models

### UserInfo Model Update

```kotlin
// models/UserInfo.kt - Add missing initials field
data class UserInfo(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val initials: String = "", // Add this field to fix Firestore warning
    val createdAt: Timestamp = Timestamp.now(),
    val lastLoginAt: Timestamp = Timestamp.now(),
    val fcmToken: String = "",
    val aiUsageCount: Int = 0
) {
    // Helper function to generate initials
    fun getInitials(): String {
        if (initials.isNotEmpty()) return initials
        
        return displayName
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { email.firstOrNull()?.uppercase()?.toString() ?: "?" }
    }
}
```

## Error Handling

### Comprehensive Error Handling Flow

```
User Action
    │
    ▼
Repository Method
    │
    ├─► Try Firestore Operation
    │   │
    │   ├─► Success ──► Return Result.success(data)
    │   │
    │   └─► Exception
    │       │
    │       ├─► PERMISSION_DENIED ──► Log + User-friendly message
    │       ├─► UNAVAILABLE ──► Log + Retry option
    │       ├─► FAILED_PRECONDITION ──► Log + Contact support
    │       └─► Other ──► Log + Generic error
    │
    ▼
Fragment/Activity
    │
    ├─► Success ──► Update UI with data
    ├─► Error ──► Show error message + retry button
    └─► Loading ──► Show loading indicator
```

### Error Messages

```kotlin
object ErrorMessages {
    const val PERMISSION_DENIED = "You don't have permission to access this data. Please try logging out and back in."
    const val NETWORK_ERROR = "Unable to connect. Please check your internet connection."
    const val INDEX_MISSING = "Database is being configured. Please try again in a few moments."
    const val GENERIC_ERROR = "Something went wrong. Please try again."
    const val AI_UNAVAILABLE = "AI assistant is temporarily unavailable. Please try again later."
}
```

## Testing Strategy

### Unit Tests
- Repository methods with mocked Firestore
- Data model transformations
- Error handling logic
- Date parsing and formatting

### Integration Tests
- Firestore security rules validation
- Query performance with indexes
- Real-time listener behavior
- API integration with Gemini

### Manual Testing Checklist
1. **Authentication**: Login/logout flow
2. **Dashboard**: All stats display real data
3. **Groups**: Create, view, join groups
4. **Tasks**: Create, view, update, delete tasks
5. **Calendar**: Tasks appear on correct dates
6. **Chat**: Send/receive messages
7. **AI Assistant**: Send messages and create tasks
8. **Offline**: App handles offline gracefully
9. **Permissions**: All Firestore operations succeed
10. **Performance**: No frame drops or ANRs

## Deployment Plan

### Phase 1: Infrastructure Fixes (Critical)
1. Deploy Firestore security rules
2. Create Firestore indexes
3. Verify rules and indexes in Firebase Console
4. Test basic queries

### Phase 2: Code Fixes (High Priority)
1. Update Gemini AI model name
2. Add OnBackInvokedCallback to manifest
3. Fix UserInfo model (add initials field)
4. Deploy and test AI functionality

### Phase 3: Dashboard Integration (High Priority)
1. Replace demo data with real Firestore queries
2. Add loading states
3. Add error handling
4. Test real-time updates

### Phase 4: Performance & Polish (Medium Priority)
1. Optimize main thread operations
2. Add comprehensive error messages
3. Improve loading indicators
4. Add retry mechanisms

### Phase 5: Verification (Required)
1. Run through manual testing checklist
2. Verify all logcat errors are resolved
3. Test on multiple devices/Android versions
4. Performance profiling

## Rollback Plan

If issues occur during deployment:
1. **Firestore Rules**: Revert to previous rules version in Firebase Console
2. **Indexes**: Cannot be rolled back, but old queries will still work
3. **Code Changes**: Revert Git commits and redeploy APK
4. **Monitoring**: Watch Firebase Console for error rates and query performance

