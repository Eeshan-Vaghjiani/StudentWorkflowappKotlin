# Design Document - Team Collaboration App Features

## Overview

This design document outlines the technical architecture and implementation approach for completing the team collaboration Android app. The design follows MVVM architecture pattern, uses Firebase as the backend, and implements each feature as an independent, testable module.

### Design Principles

1. **Incremental Implementation**: Each feature can be built and tested independently
2. **MVVM Architecture**: Separation of concerns with Models, Views, and ViewModels
3. **Repository Pattern**: Centralized data access layer
4. **Real-time Updates**: Firestore listeners for live data synchronization
5. **Offline-First**: Local caching with automatic sync when online
6. **Material Design**: Consistent, modern UI following Android guidelines

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  (Activities, Fragments, Adapters, ViewModels)              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Repository Layer                         │
│  (ChatRepository, NotificationRepository, StorageRepository) │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Firebase Services                       │
│     (Firestore, Storage, Cloud Messaging, Auth)             │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository Pattern
- **Backend**: Firebase (Firestore, Storage, Cloud Messaging, Authentication)
- **Async**: Kotlin Coroutines + Flow
- **UI**: ViewBinding, RecyclerView, Material Design Components
- **Image Loading**: Coil
- **Calendar**: Kizitonwose Calendar View
- **Navigation**: Jetpack Navigation Component (optional enhancement)

## Components and Interfaces

### 1. Real-Time Chat System

#### Data Models

**Chat.kt**
```kotlin
data class Chat(
    val id: String = "",
    val type: ChatType = ChatType.DIRECT,
    val participants: List<String> = emptyList(),
    val participantDetails: Map<String, UserInfo> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val groupId: String? = null,
    val groupName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ChatType {
    DIRECT, GROUP
}

data class UserInfo(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val profileImageUrl: String = ""
)
```

**Message.kt**
```kotlin
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val documentUrl: String? = null,
    val documentName: String? = null,
    val documentSize: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val readBy: List<String> = emptyList(),
    val status: MessageStatus = MessageStatus.SENDING
)

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}
```

#### Repository

**ChatRepository.kt**
```kotlin
class ChatRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    // Get all chats for current user
    fun getUserChats(): Flow<List<Chat>>
    
    // Get or create direct chat between two users
    suspend fun getOrCreateDirectChat(otherUserId: String): Result<Chat>
    
    // Get or create group chat
    suspend fun getOrCreateGroupChat(groupId: String): Result<Chat>
    
    // Send text message
    suspend fun sendMessage(chatId: String, text: String): Result<Message>
    
    // Send image message
    suspend fun sendImageMessage(chatId: String, imageUri: Uri): Result<Message>
    
    // Send document message
    suspend fun sendDocumentMessage(chatId: String, documentUri: Uri): Result<Message>
    
    // Get messages for a chat (paginated)
    fun getChatMessages(chatId: String, limit: Int = 50): Flow<List<Message>>
    
    // Mark messages as read
    suspend fun markMessagesAsRead(chatId: String, messageIds: List<String>)
    
    // Update typing status
    suspend fun updateTypingStatus(chatId: String, isTyping: Boolean)
    
    // Get typing users
    fun getTypingUsers(chatId: String): Flow<List<String>>
    
    // Search users for direct chat
    suspend fun searchUsers(query: String): Result<List<UserInfo>>
}
```

#### UI Components

**ChatListFragment.kt**
- Displays list of all chats (groups + direct messages)
- Search bar for finding chats
- Tabs for "All", "Groups", "Direct"
- Shows last message, timestamp, unread count
- Pull-to-refresh
- Click to open chat room

**ChatRoomActivity.kt**
- Displays messages in RecyclerView (reverse layout)
- Message input field with send button
- Attachment button (camera, gallery, documents)
- Typing indicator at bottom
- Toolbar with chat name and participant count/status
- Real-time message updates
- Scroll to bottom button when new messages arrive

**MessageAdapter.kt**
- Different ViewHolders for sent/received messages
- Text message layout
- Image message layout with thumbnail
- Document message layout with icon and details
- Timestamp grouping (show time every 5 minutes)
- Read receipts (checkmarks)
- Long-press menu (copy, delete, forward)

**UserSearchDialog.kt**
- Search input field
- RecyclerView of search results
- User profile picture and name
- Click to start direct chat

### 2. Push Notifications

#### Service

**MyFirebaseMessagingService.kt**
```kotlin
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String)
    override fun onMessageReceived(remoteMessage: RemoteMessage)
    
    private fun handleChatNotification(data: Map<String, String>)
    private fun handleTaskNotification(data: Map<String, String>)
    private fun handleGroupNotification(data: Map<String, String>)
    
    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        intent: Intent
    )
}
```

#### Repository

**NotificationRepository.kt**
```kotlin
class NotificationRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Save FCM token to user document
    suspend fun saveFcmToken(token: String): Result<Unit>
    
    // Get FCM tokens for users
    suspend fun getUserTokens(userIds: List<String>): Result<List<String>>
    
    // Send notification (via Cloud Function trigger)
    suspend fun sendChatNotification(
        recipientIds: List<String>,
        chatId: String,
        message: String
    ): Result<Unit>
    
    // Schedule task reminder
    suspend fun scheduleTaskReminder(taskId: String, dueDate: Long): Result<Unit>
}
```

#### Notification Channels

```kotlin
object NotificationChannels {
    const val CHAT_CHANNEL_ID = "chat_messages"
    const val TASK_CHANNEL_ID = "task_reminders"
    const val GROUP_CHANNEL_ID = "group_updates"
    
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chatChannel = NotificationChannel(
                CHAT_CHANNEL_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            
            val taskChannel = NotificationChannel(
                TASK_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            
            val groupChannel = NotificationChannel(
                GROUP_CHANNEL_ID,
                "Group Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(chatChannel, taskChannel, groupChannel))
        }
    }
}
```

#### Permission Handling

**NotificationPermissionHelper.kt**
```kotlin
class NotificationPermissionHelper(private val activity: Activity) {
    private val requestPermissionLauncher = 
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission granted, get FCM token
            } else {
                // Permission denied, show explanation
            }
        }
    
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
```

### 3. File and Image Sharing

#### Repository

**StorageRepository.kt**
```kotlin
class StorageRepository(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    // Upload image with compression
    suspend fun uploadImage(
        uri: Uri,
        path: String,
        onProgress: (Int) -> Unit
    ): Result<String>
    
    // Upload document
    suspend fun uploadDocument(
        uri: Uri,
        path: String,
        onProgress: (Int) -> Unit
    ): Result<String>
    
    // Upload profile picture
    suspend fun uploadProfilePicture(
        uri: Uri,
        userId: String,
        onProgress: (Int) -> Unit
    ): Result<String>
    
    // Download file
    suspend fun downloadFile(
        url: String,
        destinationFile: File,
        onProgress: (Int) -> Unit
    ): Result<File>
    
    // Delete file
    suspend fun deleteFile(url: String): Result<Unit>
    
    // Get file metadata
    suspend fun getFileMetadata(url: String): Result<StorageMetadata>
}
```

#### Image Compression

**ImageCompressor.kt**
```kotlin
object ImageCompressor {
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080,
        quality: Int = 80
    ): File {
        // Load bitmap
        // Resize if needed
        // Compress to JPEG
        // Save to cache file
        // Return file
    }
}
```

#### UI Components

**AttachmentBottomSheet.kt**
- Options: Camera, Gallery, Documents
- Permission handling for camera and storage
- Image picker using ActivityResultContracts
- Document picker using ActivityResultContracts

**ImageViewerActivity.kt**
- Full-screen image display
- Pinch to zoom
- Swipe to dismiss
- Share and download options

**ProfilePictureManager.kt**
- Crop image to square
- Compress to under 500KB
- Upload with progress
- Update user document
- Cache locally

### 4. Enhanced Calendar View

#### ViewModel

**CalendarViewModel.kt**
```kotlin
class CalendarViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<FirebaseTask>>(emptyList())
    val tasks: StateFlow<List<FirebaseTask>> = _tasks
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate
    
    val tasksForSelectedDate: StateFlow<List<FirebaseTask>>
    
    val datesWithTasks: StateFlow<Set<LocalDate>>
    
    fun selectDate(date: LocalDate)
    fun loadTasks()
    fun filterByGroup(groupId: String?)
    fun filterByCategory(category: String?)
}
```

#### UI Components

**CalendarFragment.kt**
- Kizitonwose CalendarView for month display
- Dot indicators on dates with tasks
- Task list below calendar
- Filter chips (All, My Tasks, Group)
- Swipe between months
- Click date to see tasks
- Click task to open details

**DayBinder.kt**
- Custom day cell rendering
- Show date number
- Show colored dots for tasks
- Highlight selected date
- Highlight today's date

### 5. Profile Picture Management

#### UI Components

**ProfileFragment.kt**
- Display current profile picture or default avatar
- Edit button to change picture
- Options: Take Photo, Choose from Gallery
- Crop and compress before upload
- Progress indicator during upload
- Update UI when upload completes

**DefaultAvatarGenerator.kt**
```kotlin
object DefaultAvatarGenerator {
    fun generateAvatar(name: String, size: Int): Bitmap {
        // Extract initials (first letter of first and last name)
        // Generate colored background
        // Draw initials in white
        // Return bitmap
    }
}
```

### 6. Message Rich Content

#### Link Detection

**LinkifyHelper.kt**
```kotlin
object LinkifyHelper {
    fun detectLinks(text: String): List<LinkSpan>
    fun makeLinksClickable(textView: TextView, text: String)
}
```

#### Message Status

**MessageStatusView.kt**
- Custom view showing message status
- Sending: Clock icon
- Sent: Single checkmark
- Delivered: Double checkmark (gray)
- Read: Double checkmark (blue)
- Failed: Error icon with retry

#### Message Grouping

**MessageGrouper.kt**
```kotlin
object MessageGrouper {
    fun groupMessages(messages: List<Message>): List<MessageGroup>
    
    data class MessageGroup(
        val senderId: String,
        val senderName: String,
        val senderImageUrl: String,
        val messages: List<Message>,
        val timestamp: Long,
        val showHeader: Boolean
    )
}
```

### 7. Offline Support

#### Configuration

**FirestoreConfig.kt**
```kotlin
object FirestoreConfig {
    fun enableOfflinePersistence(firestore: FirebaseFirestore) {
        val settings = firestoreSettings {
            isPersistenceEnabled = true
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
        }
        firestore.firestoreSettings = settings
    }
}
```

#### Offline Queue

**OfflineMessageQueue.kt**
```kotlin
class OfflineMessageQueue(private val context: Context) {
    private val prefs = context.getSharedPreferences("offline_queue", Context.MODE_PRIVATE)
    
    fun queueMessage(message: Message)
    fun getQueuedMessages(): List<Message>
    fun removeMessage(messageId: String)
    fun clearQueue()
}
```

#### Connection Monitor

**ConnectionMonitor.kt**
```kotlin
class ConnectionMonitor(private val context: Context) {
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val isConnected: Flow<Boolean>
    
    fun startMonitoring()
    fun stopMonitoring()
}
```

### 8. User Experience Enhancements

#### Loading States

**SkeletonLoader.kt**
- Shimmer effect for loading states
- Placeholder layouts for chat list, message list, task list
- Smooth transition from skeleton to actual content

**EmptyStateView.kt**
- Custom view for empty states
- Icon, title, description
- Optional action button
- Different states: No chats, No tasks, No internet, etc.

#### Animations

**AnimationUtils.kt**
```kotlin
object AnimationUtils {
    fun fadeIn(view: View, duration: Long = 300)
    fun fadeOut(view: View, duration: Long = 300)
    fun slideUp(view: View, duration: Long = 300)
    fun slideDown(view: View, duration: Long = 300)
    fun scaleIn(view: View, duration: Long = 200)
}
```

#### Swipe Actions

**SwipeToRefreshHelper.kt**
- Implement SwipeRefreshLayout on all list screens
- Custom refresh indicator colors
- Refresh data from Firestore

**MessageSwipeHelper.kt**
- Swipe message to reply
- Long-press for context menu
- Swipe to delete (with undo)

### 9. Security and Privacy

#### Firestore Security Rules

**firestore.rules**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    function isMember(groupId) {
      return request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.members;
    }
    
    function isParticipant(chatId) {
      return request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId);
    }
    
    // Groups collection
    match /groups/{groupId} {
      allow read: if isAuthenticated() && isMember(groupId);
      allow create: if isAuthenticated();
      allow update, delete: if isAuthenticated() && 
        request.auth.uid in resource.data.admins;
    }
    
    // Tasks collection
    match /tasks/{taskId} {
      allow read: if isAuthenticated() && 
        (isOwner(resource.data.createdBy) || 
         request.auth.uid in resource.data.assignedTo);
      allow create: if isAuthenticated();
      allow update, delete: if isAuthenticated() && 
        isOwner(resource.data.createdBy);
    }
    
    // Chats collection
    match /chats/{chatId} {
      allow read, write: if isAuthenticated() && isParticipant(chatId);
    }
    
    // Messages subcollection
    match /chats/{chatId}/messages/{messageId} {
      allow read: if isAuthenticated() && isParticipant(chatId);
      allow create: if isAuthenticated() && isParticipant(chatId);
      allow update, delete: if isAuthenticated() && 
        isOwner(resource.data.senderId);
    }
  }
}
```

#### Storage Security Rules

**storage.rules**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    function isValidImageSize() {
      return request.resource.size < 5 * 1024 * 1024; // 5MB
    }
    
    function isValidDocumentSize() {
      return request.resource.size < 10 * 1024 * 1024; // 10MB
    }
    
    // Profile pictures
    match /profile_images/{userId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isOwner(userId) && isValidImageSize();
    }
    
    // Chat images
    match /chat_images/{chatId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isValidImageSize();
    }
    
    // Chat documents
    match /chat_documents/{chatId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isValidDocumentSize();
    }
  }
}
```

#### ProGuard Rules

**proguard-rules.pro**
```
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep data models
-keep class com.example.loginandregistration.models.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
```

### 10. Performance Optimization

#### Pagination

**PaginationHelper.kt**
```kotlin
class PaginationHelper<T>(
    private val pageSize: Int = 50,
    private val loadMore: suspend (lastItem: T?) -> List<T>
) {
    private var lastItem: T? = null
    private var isLoading = false
    private var hasMore = true
    
    suspend fun loadNextPage(): List<T>
    fun reset()
}
```

#### Image Caching

**Coil Configuration**
```kotlin
object ImageLoaderConfig {
    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 25% of app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .build()
    }
}
```

#### Firestore Indexes

**firestore.indexes.json**
```json
{
  "indexes": [
    {
      "collectionGroup": "messages",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "chatId", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "tasks",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "assignedTo", "arrayConfig": "CONTAINS" },
        { "fieldPath": "dueDate", "order": "ASCENDING" }
      ]
    }
  ]
}
```

## Data Models

### Firestore Collections Structure

```
users/
  {userId}/
    - email: string
    - displayName: string
    - profileImageUrl: string
    - fcmToken: string
    - online: boolean
    - lastSeen: timestamp
    - createdAt: timestamp

groups/
  {groupId}/
    - name: string
    - description: string
    - subject: string
    - joinCode: string
    - members: array<string>
    - admins: array<string>
    - isPublic: boolean
    - createdBy: string
    - createdAt: timestamp

tasks/
  {taskId}/
    - title: string
    - description: string
    - category: string (Personal, Group, Assignment)
    - priority: string (Low, Medium, High)
    - status: string (Pending, InProgress, Completed)
    - dueDate: timestamp
    - groupId: string (optional)
    - assignedTo: array<string>
    - createdBy: string
    - createdAt: timestamp

chats/
  {chatId}/
    - type: string (DIRECT, GROUP)
    - participants: array<string>
    - participantDetails: map<string, object>
    - lastMessage: string
    - lastMessageTime: timestamp
    - lastMessageSenderId: string
    - unreadCount: map<string, number>
    - groupId: string (optional)
    - groupName: string (optional)
    - createdAt: timestamp
    
    messages/
      {messageId}/
        - senderId: string
        - senderName: string
        - senderImageUrl: string
        - text: string
        - imageUrl: string (optional)
        - documentUrl: string (optional)
        - documentName: string (optional)
        - documentSize: number (optional)
        - timestamp: timestamp
        - readBy: array<string>
        - status: string

typing_status/
  {chatId}/
    - {userId}: boolean

notifications/
  {notificationId}/
    - userId: string
    - type: string (CHAT, TASK, GROUP)
    - title: string
    - body: string
    - data: map
    - read: boolean
    - createdAt: timestamp
```

### Storage Structure

```
profile_images/
  {userId}/
    {timestamp}.jpg

chat_images/
  {chatId}/
    {timestamp}_{userId}.jpg

chat_documents/
  {chatId}/
    {timestamp}_{userId}_{filename}
```

## Error Handling

### Error Types

```kotlin
sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class AuthError(val message: String) : AppError()
    data class PermissionError(val message: String) : AppError()
    data class StorageError(val message: String) : AppError()
    data class ValidationError(val message: String) : AppError()
    data class UnknownError(val message: String) : AppError()
}
```

### Error Handling Strategy

1. **Network Errors**: Show retry button, queue operations for later
2. **Auth Errors**: Redirect to login screen
3. **Permission Errors**: Show explanation dialog with settings button
4. **Storage Errors**: Show error message, allow retry
5. **Validation Errors**: Show inline error messages
6. **Unknown Errors**: Log to Crashlytics, show generic error message

## Testing Strategy

### Unit Tests

- Repository methods
- ViewModel logic
- Utility functions (compression, link detection, etc.)
- Data model conversions

### Integration Tests

- Firebase operations with emulator
- End-to-end user flows
- Real-time listener updates

### UI Tests

- Navigation between screens
- User interactions (click, swipe, type)
- Display of data
- Error states

### Manual Testing Checklist

Each requirement will have a specific testing checklist to verify functionality before moving to the next requirement.

## Implementation Phases

Each requirement will be implemented as a separate, testable phase:

1. **Phase 1**: Real-Time Chat System (Data models, Repository, UI)
2. **Phase 2**: Push Notifications (Service, Permissions, Channels)
3. **Phase 3**: File and Image Sharing (Storage, Compression, Upload/Download)
4. **Phase 4**: Enhanced Calendar View (Calendar library, Task visualization)
5. **Phase 5**: Profile Picture Management (Upload, Display, Caching)
6. **Phase 6**: Message Rich Content (Links, Status, Grouping)
7. **Phase 7**: Offline Support (Persistence, Queue, Sync)
8. **Phase 8**: User Experience Enhancements (Animations, Empty states)
9. **Phase 9**: Security and Privacy (Rules, ProGuard)
10. **Phase 10**: Performance Optimization (Pagination, Caching, Indexes)

After each phase, the app will be fully functional and testable with the new feature integrated.
