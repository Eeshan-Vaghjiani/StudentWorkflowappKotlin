# Design Document - App Critical Fixes

## Overview

This design document outlines the technical approach to fix critical issues in the Study Planner Android app. The app currently has broken authentication UI, non-functional Google Sign-In, demo data instead of real Firestore data, and issues with groups, assignments, and chat functionality.

### Design Principles

1. **Mockup Alignment**: All UI changes must match the HTML mockups in `mobile-app-mockups/`
2. **Firestore-First**: Remove all demo data and fetch exclusively from Firestore
3. **Real-time Updates**: Use Firestore snapshot listeners for live data
4. **Error Handling**: Comprehensive error handling with user-friendly messages
5. **Security**: Update Firestore rules to allow necessary operations while maintaining security
6. **MVVM Architecture**: Maintain existing architecture pattern with proper separation of concerns

## Architecture

### Current Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  (Activities, Fragments, Adapters)                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     ViewModel Layer                          │
│  (ViewModels with LiveData/StateFlow)                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Repository Layer                         │
│  (Data access and business logic)                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Firebase Services                       │
│     (Firestore, Auth, Storage, Cloud Messaging)             │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Language**: Kotlin
- **Architecture**: MVVM with Repository Pattern
- **Backend**: Firebase (Firestore, Auth, Storage, FCM)
- **Async**: Kotlin Coroutines + Flow
- **UI**: ViewBinding, Material Design Components
- **Image Loading**: Coil
- **Calendar**: Kizitonwose Calendar View

## Components and Interfaces

### 1. Authentication UI Improvements

#### Design Approach

Match the mockup designs in `mobile-app-mockups/pages/login.html` and `mobile-app-mockups/pages/register.html` by updating XML layouts and applying Material Design 3 components.

#### UI Components to Update

**Login.kt Activity**
- Update `activity_login.xml` layout to match mockup
- Use MaterialButton for primary actions
- Use TextInputLayout with TextInputEditText for form fields
- Add proper spacing (16dp margins, 12dp padding)
- Implement password visibility toggle
- Add loading state with ProgressBar overlay
- Display inline error messages using TextInputLayout.error

**Register.kt Activity**
- Update `activity_register.xml` layout to match mockup
- Multi-step form with proper validation
- Password strength indicator (weak/medium/strong)
- Terms and conditions checkbox
- Profile picture selection (optional)
- Proper keyboard navigation (IME actions)

#### Color Scheme (from mockup spec)
```kotlin
object AppColors {
    val PrimaryBlue = Color(0xFF007AFF)
    val SuccessGreen = Color(0xFF34C759)
    val WarningOrange = Color(0xFFFF9500)
    val DangerRed = Color(0xFFFF3B30)
    val TextPrimary = Color(0xFF1D1D1F)
    val TextSecondary = Color(0xFF8E8E93)
    val Background = Color(0xFFF2F2F7)
    val Border = Color(0xFFE5E5EA)
}
```

#### Validation Rules
- Email: Valid email format using `Patterns.EMAIL_ADDRESS`
- Password: Minimum 8 characters, at least one uppercase, one lowercase, one number
- Name: Non-empty, 2-50 characters
- Real-time validation on text change
- Show errors only after user interaction

### 2. Google Sign-In Integration

#### Implementation Strategy

**GoogleSignInHelper.kt**
```kotlin
class GoogleSignInHelper(private val activity: Activity) {
    private val googleSignInClient: GoogleSignInClient
    
    fun signIn(): Intent
    fun handleSignInResult(data: Intent?): Task<GoogleSignInAccount>
    fun signOut()
}
```

**Integration Points**
1. Add Google Sign-In button to Login.kt
2. Configure Google Sign-In in Firebase Console
3. Add SHA-1 fingerprint to Firebase project
4. Update `google-services.json`
5. Handle sign-in result in `onActivityResult`
6. Create/update user document in Firestore after successful sign-in

**User Document Structure**
```kotlin
data class FirebaseUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val authProvider: String = "email", // "email" or "google"
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis()
)
```

### 3. Dashboard Stats from Firestore

#### DashboardRepository Enhancement

**Current Issue**: Using demo data instead of Firestore queries

**Solution**: Implement real-time Firestore queries

```kotlin
class DashboardRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Get task statistics
    fun getTaskStats(): Flow<TaskStats> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("tasks")
            .whereArrayContains("assignedTo", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { 
                    it.toObject(FirebaseTask::class.java) 
                } ?: emptyList()
                
                val stats = TaskStats(
                    total = tasks.size,
                    completed = tasks.count { it.status == "completed" },
                    pending = tasks.count { it.status == "pending" },
                    overdue = tasks.count { 
                        it.status != "completed" && 
                        it.dueDate < System.currentTimeMillis() 
                    }
                )
                
                trySend(stats)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get group count
    fun getGroupCount(): Flow<Int> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("groups")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                trySend(snapshot?.size() ?: 0)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get study session stats
    fun getSessionStats(): Flow<SessionStats> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("sessions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val sessions = snapshot?.documents?.mapNotNull {
                    it.toObject(FirebaseSession::class.java)
                } ?: emptyList()
                
                val stats = SessionStats(
                    totalSessions = sessions.size,
                    totalMinutes = sessions.sumOf { it.duration },
                    todaySessions = sessions.count { 
                        it.startTime > getTodayStartMillis() 
                    }
                )
                
                trySend(stats)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get AI usage stats
    fun getAIUsageStats(): Flow<AIUsageStats> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val user = snapshot?.toObject(FirebaseUser::class.java)
                val stats = AIUsageStats(
                    used = user?.aiPromptsUsed ?: 0,
                    limit = user?.aiPromptsLimit ?: 10
                )
                
                trySend(stats)
            }
        
        awaitClose { listener.remove() }
    }
}
```

**Data Models**
```kotlin
data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0
)

data class SessionStats(
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val todaySessions: Int = 0
)

data class AIUsageStats(
    val used: Int = 0,
    val limit: Int = 10
)
```

**HomeFragment.kt Updates**
- Remove all demo data references
- Collect flows from DashboardRepository
- Update UI with real-time data
- Show loading skeletons while fetching
- Display empty states when no data exists

### 4. Groups Display and Management

#### Current Issue
Groups exist in Firestore but not displaying in GroupsFragment

#### Root Cause Analysis
1. Query might be incorrect (wrong field names)
2. Listener not properly attached
3. Adapter not updating with new data
4. Empty state showing incorrectly

#### Solution: Enhanced GroupRepository

```kotlin
class GroupRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Get all groups where user is a member
    fun getUserGroups(): Flow<List<FirebaseGroup>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("groups")
            .whereArrayContains("members", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GroupRepository", "Error fetching groups", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseGroup::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                Log.d("GroupRepository", "Fetched ${groups.size} groups")
                trySend(groups)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Create new group
    suspend fun createGroup(group: FirebaseGroup): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(
                Exception("User not authenticated")
            )
            
            val groupData = group.copy(
                createdBy = userId,
                members = listOf(userId),
                admins = listOf(userId),
                joinCode = generateJoinCode(),
                createdAt = System.currentTimeMillis()
            )
            
            val docRef = firestore.collection("groups").add(groupData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error creating group", e)
            Result.failure(e)
        }
    }
    
    // Delete group
    suspend fun deleteGroup(groupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(
                Exception("User not authenticated")
            )
            
            // Check if user is admin
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            val group = groupDoc.toObject(FirebaseGroup::class.java)
            
            if (group?.admins?.contains(userId) != true) {
                return@withContext Result.failure(
                    Exception("Only admins can delete groups")
                )
            }
            
            firestore.collection("groups").document(groupId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error deleting group", e)
            Result.failure(e)
        }
    }
    
    // Join group by code
    suspend fun joinGroupByCode(joinCode: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext Result.failure(
                Exception("User not authenticated")
            )
            
            val querySnapshot = firestore.collection("groups")
                .whereEqualTo("joinCode", joinCode)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                return@withContext Result.failure(
                    Exception("Group not found")
                )
            }
            
            val groupDoc = querySnapshot.documents[0]
            val groupId = groupDoc.id
            
            // Add user to members array
            firestore.collection("groups").document(groupId)
                .update("members", FieldValue.arrayUnion(userId))
                .await()
            
            Result.success(groupId)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error joining group", e)
            Result.failure(e)
        }
    }
    
    private fun generateJoinCode(): String {
        return (100000..999999).random().toString()
    }
}
```

**GroupsFragment.kt Updates**
- Collect groups flow from repository
- Update RecyclerView adapter with new data
- Show loading state while fetching
- Display empty state when no groups
- Add pull-to-refresh
- Handle errors gracefully

### 5. Assignments Display in Tasks and Calendar

#### Current Issue
Assignments exist in Firestore but not showing in TasksFragment or CalendarFragment

#### Solution Strategy

**TaskRepository Enhancement**
```kotlin
class TaskRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Get all tasks including assignments
    fun getUserTasks(category: String? = null): Flow<List<FirebaseTask>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        var query = firestore.collection("tasks")
            .whereArrayContains("assignedTo", userId)
        
        // Filter by category if specified
        if (category != null && category != "All") {
            query = query.whereEqualTo("category", category)
        }
        
        val listener = query
            .orderBy("dueDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TaskRepository", "Error fetching tasks", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseTask::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                Log.d("TaskRepository", "Fetched ${tasks.size} tasks")
                trySend(tasks)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get tasks for specific date (for calendar)
    fun getTasksForDate(date: LocalDate): Flow<List<FirebaseTask>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val listener = firestore.collection("tasks")
            .whereArrayContains("assignedTo", userId)
            .whereGreaterThanOrEqualTo("dueDate", startOfDay)
            .whereLessThan("dueDate", endOfDay)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebaseTask::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(tasks)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get dates that have tasks (for calendar indicators)
    fun getDatesWithTasks(): Flow<Set<LocalDate>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("tasks")
            .whereArrayContains("assignedTo", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val dates = snapshot?.documents?.mapNotNull { doc ->
                    val task = doc.toObject(FirebaseTask::class.java)
                    task?.dueDate?.let { 
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                }?.toSet() ?: emptySet()
                
                trySend(dates)
            }
        
        awaitClose { listener.remove() }
    }
}
```

**TasksFragment.kt Updates**
- Remove demo data
- Collect tasks flow from repository
- Filter by category (All, Personal, Group, Assignment)
- Update adapter with real data
- Show empty state when no tasks
- Add pull-to-refresh

**CalendarFragment.kt Updates**
- Collect tasks for selected date
- Collect dates with tasks for indicators
- Display dot indicators on dates with tasks
- Show task list below calendar
- Update when date is selected
- Handle month changes

### 6. Chat Functionality Fixes

#### Current Issue
Cannot create new chats - getting Firestore errors

#### Root Cause
Firestore security rules require chat to exist before checking if user is participant, but we need to create the chat first.

#### Solution: Update Firestore Rules

**Modified Chat Rules**
```javascript
match /chats/{chatId} {
  // Allow read if user is participant
  allow read: if isAuthenticated() && 
    request.auth.uid in resource.data.participants;
  
  // Allow create if user is in the participants array being created
  allow create: if isAuthenticated() && 
    request.auth.uid in request.resource.data.participants;
  
  // Allow update if user is participant
  allow update: if isAuthenticated() && 
    request.auth.uid in resource.data.participants;
  
  // Messages subcollection
  match /messages/{messageId} {
    allow read: if isAuthenticated() && 
      request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
    
    allow create: if isAuthenticated() && 
      request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
    
    allow update, delete: if isAuthenticated() && 
      isOwner(resource.data.senderId);
  }
}
```

**ChatRepository Enhancement**
```kotlin
class ChatRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Get or create direct chat
    suspend fun getOrCreateDirectChat(otherUserId: String): Result<Chat> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return@withContext Result.failure(
                Exception("User not authenticated")
            )
            
            // Check if chat already exists
            val existingChat = firestore.collection("chats")
                .whereEqualTo("type", "DIRECT")
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Chat::class.java)?.copy(id = it.id) }
                .firstOrNull { it.participants.contains(otherUserId) }
            
            if (existingChat != null) {
                return@withContext Result.success(existingChat)
            }
            
            // Create new chat
            val otherUser = firestore.collection("users")
                .document(otherUserId)
                .get()
                .await()
                .toObject(FirebaseUser::class.java)
            
            val currentUser = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()
                .toObject(FirebaseUser::class.java)
            
            val newChat = Chat(
                type = ChatType.DIRECT,
                participants = listOf(currentUserId, otherUserId),
                participantDetails = mapOf(
                    currentUserId to UserInfo(
                        userId = currentUserId,
                        displayName = currentUser?.displayName ?: "",
                        email = currentUser?.email ?: "",
                        profileImageUrl = currentUser?.profileImageUrl ?: ""
                    ),
                    otherUserId to UserInfo(
                        userId = otherUserId,
                        displayName = otherUser?.displayName ?: "",
                        email = otherUser?.email ?: "",
                        profileImageUrl = otherUser?.profileImageUrl ?: ""
                    )
                ),
                createdAt = System.currentTimeMillis()
            )
            
            val docRef = firestore.collection("chats").add(newChat).await()
            Result.success(newChat.copy(id = docRef.id))
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error creating chat", e)
            Result.failure(e)
        }
    }
    
    // Send message
    suspend fun sendMessage(chatId: String, text: String): Result<Message> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return@withContext Result.failure(
                Exception("User not authenticated")
            )
            
            val currentUser = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()
                .toObject(FirebaseUser::class.java)
            
            val message = Message(
                chatId = chatId,
                senderId = currentUserId,
                senderName = currentUser?.displayName ?: "",
                senderImageUrl = currentUser?.profileImageUrl ?: "",
                text = text,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT
            )
            
            // Add message to subcollection
            val docRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .await()
            
            // Update chat's last message
            firestore.collection("chats").document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to message.timestamp,
                    "lastMessageSenderId" to currentUserId
                )
            ).await()
            
            Result.success(message.copy(id = docRef.id))
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            Result.failure(e)
        }
    }
}
```

### 7. Error Handling and User Feedback

#### Error Handling Strategy

**ErrorHandler.kt**
```kotlin
object ErrorHandler {
    fun handleError(context: Context, error: Throwable, onRetry: (() -> Unit)? = null) {
        val message = when (error) {
            is FirebaseNetworkException -> "No internet connection"
            is FirebaseAuthException -> "Authentication error: ${error.message}"
            is FirebaseFirestoreException -> when (error.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                    "Permission denied. Please check your access."
                FirebaseFirestoreException.Code.NOT_FOUND -> 
                    "Data not found."
                FirebaseFirestoreException.Code.UNAVAILABLE -> 
                    "Service temporarily unavailable. Please try again."
                else -> "An error occurred: ${error.message}"
            }
            else -> "An unexpected error occurred"
        }
        
        if (onRetry != null) {
            Snackbar.make(
                (context as? Activity)?.findViewById(android.R.id.content) ?: return,
                message,
                Snackbar.LENGTH_LONG
            ).setAction("Retry") {
                onRetry()
            }.show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        
        // Log to Crashlytics
        FirebaseCrashlytics.getInstance().recordException(error)
    }
}
```

**Loading States**
- Use SkeletonLoader for list screens
- Use ProgressBar overlay for operations
- Disable buttons during operations
- Show success feedback after operations

**Empty States**
- Use EmptyStateView component
- Different messages for different scenarios
- Call-to-action buttons where appropriate

### 8. Remove Demo Data Dependencies

#### Files to Update

**HomeFragment.kt**
- Remove `getDemoTaskStats()`, `getDemoGroupCount()`, etc.
- Use DashboardRepository flows

**TasksFragment.kt**
- Remove `getDemoTasks()`
- Use TaskRepository flows

**GroupsFragment.kt**
- Remove `getDemoGroups()`
- Use GroupRepository flows

**CalendarFragment.kt**
- Remove `getDemoEvents()`
- Use TaskRepository flows

#### Search and Replace Strategy
1. Find all references to "demo" or "Demo" in code
2. Replace with repository calls
3. Remove demo data classes
4. Update adapters to handle empty lists

## Data Models

### Firestore Document Structures

**users/{userId}**
```kotlin
data class FirebaseUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val authProvider: String = "email",
    val fcmToken: String = "",
    val aiPromptsUsed: Int = 0,
    val aiPromptsLimit: Int = 10,
    val createdAt: Long = 0,
    val lastSeen: Long = 0
)
```

**groups/{groupId}**
```kotlin
data class FirebaseGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subject: String = "",
    val joinCode: String = "",
    val members: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val isPublic: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = 0
)
```

**tasks/{taskId}**
```kotlin
data class FirebaseTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Personal", // Personal, Group, Assignment
    val priority: String = "Medium", // Low, Medium, High
    val status: String = "pending", // pending, in-progress, completed
    val dueDate: Long = 0,
    val groupId: String? = null,
    val assignedTo: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = 0
)
```

**chats/{chatId}**
```kotlin
data class Chat(
    val id: String = "",
    val type: ChatType = ChatType.DIRECT,
    val participants: List<String> = emptyList(),
    val participantDetails: Map<String, UserInfo> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val lastMessageSenderId: String = "",
    val groupId: String? = null,
    val createdAt: Long = 0
)
```

**chats/{chatId}/messages/{messageId}**
```kotlin
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0,
    val status: MessageStatus = MessageStatus.SENT
)
```

## Testing Strategy

### Manual Testing Checklist

**Authentication**
- [ ] Login with email/password works
- [ ] Registration creates user in Firestore
- [ ] Google Sign-In works and creates/updates user
- [ ] Error messages display correctly
- [ ] Auto-login works on app restart

**Dashboard**
- [ ] Task stats show real data from Firestore
- [ ] Group count shows real data
- [ ] Session stats show real data
- [ ] AI usage shows real data
- [ ] Stats update in real-time

**Groups**
- [ ] Groups list shows all user's groups
- [ ] Create group works
- [ ] Join group by code works
- [ ] Delete group works (admin only)
- [ ] Empty state shows when no groups

**Tasks & Assignments**
- [ ] Tasks list shows all tasks including assignments
- [ ] Filter by category works
- [ ] Calendar shows tasks on correct dates
- [ ] Selecting date shows tasks for that date
- [ ] Empty state shows when no tasks

**Chat**
- [ ] Can create new direct chat
- [ ] Can send messages
- [ ] Messages appear in real-time
- [ ] Chat list shows recent chats
- [ ] No Firestore permission errors

## Implementation Phases

1. **Phase 1**: Authentication UI + Google Sign-In
2. **Phase 2**: Dashboard Stats from Firestore
3. **Phase 3**: Groups Display and Management
4. **Phase 4**: Tasks and Assignments Display
5. **Phase 5**: Chat Functionality Fixes
6. **Phase 6**: Remove Demo Data
7. **Phase 7**: Error Handling and Polish
