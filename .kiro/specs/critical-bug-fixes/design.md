# Design Document - Critical Bug Fixes & Feature Improvements

## Overview

This design addresses critical production issues in the TeamSync Collaboration Android app. The primary root cause is mismatched field names between Firestore security rules and the application code, causing all database operations to fail with PERMISSION_DENIED errors. Additionally, there are implementation issues in chat functionality, storage integration, and UI consistency. This design provides a systematic approach to fix all issues and add an AI-powered assignment assistant feature.

## Architecture

### Current Architecture Issues

1. **Security Rules Mismatch**: Rules reference `members` and `admins` arrays, but code uses `memberIds`
2. **RecyclerView State Management**: Chat adapter doesn't properly handle view lifecycle
3. **Storage Integration**: No Firebase Storage implementation for file uploads
4. **Error Handling**: Permission errors not caught or handled gracefully
5. **Theme System**: Inconsistent color application across activities

### Proposed Architecture Improvements

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Activities│  │Fragments │  │Adapters  │  │ViewModels│   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
└───────┼─────────────┼─────────────┼─────────────┼──────────┘
        │             │             │             │
┌───────┼─────────────┼─────────────┼─────────────┼──────────┐
│       │        Business Logic Layer              │          │
│  ┌────▼─────┐  ┌──▼──────┐  ┌───▼──────┐  ┌───▼──────┐   │
│  │Repository│  │Use Cases│  │Validators│  │AI Service│   │
│  └────┬─────┘  └──┬──────┘  └───┬──────┘  └───┬──────┘   │
└───────┼───────────┼─────────────┼─────────────┼───────────┘
        │           │             │             │
┌───────┼───────────┼─────────────┼─────────────┼───────────┐
│       │         Data Layer                     │           │
│  ┌────▼─────┐  ┌─▼────────┐  ┌─▼──────────┐ ┌▼─────────┐ │
│  │Firestore │  │Storage   │  │Auth        │ │Gemini API│ │
│  │(Fixed    │  │(New)     │  │(Fixed)     │ │(New)     │ │
│  │Rules)    │  │          │  │            │ │          │ │
│  └──────────┘  └──────────┘  └────────────┘ └──────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Fixed Firestore Security Rules

**Purpose**: Correct field name references to match application code

**Key Changes**:
- Replace `members` with `memberIds` in group rules
- Replace `admins` with `memberIds` and check `role` field
- Fix `participants` array checks in chat rules
- Add proper `typing_status` subcollection rules
- Fix `group_activities` collection rules

**Interface**:
```javascript
// Helper function updates
function isMember(groupId) {
  return isAuthenticated() && 
    request.auth.uid in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
}

function isGroupAdmin(groupId) {
  let group = get(/databases/$(database)/documents/groups/$(groupId)).data;
  return isAuthenticated() && 
    exists(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid)) &&
    get(/databases/$(database)/documents/groups/$(groupId)/members/$(request.auth.uid)).data.role == 'admin';
}
```

### 2. Chat RecyclerView Fix

**Purpose**: Prevent IllegalArgumentException when attaching views

**Root Cause**: Adapter trying to attach views that are already attached due to improper state management

**Solution**:
```kotlin
class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val messages = mutableListOf<Message>()
    
    fun updateMessages(newMessages: List<Message>) {
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        messages.clear()
        messages.addAll(newMessages)
        
        diffResult.dispatchUpdatesTo(this)
    }
    
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        // Clean up any listeners or resources
        when (holder) {
            is SentMessageViewHolder -> holder.cleanup()
            is ReceivedMessageViewHolder -> holder.cleanup()
        }
    }
}

class MessageDiffCallback(
    private val oldList: List<Message>,
    private val newList: List<Message>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    
    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos].id == newList[newPos].id
    }
    
    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }
}
```

### 3. Firebase Storage Integration

**Purpose**: Enable file uploads for profile pictures and chat attachments

**Components**:

**StorageRepository.kt**:
```kotlin
class StorageRepository {
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    
    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val ref = storageRef.child("profile_pictures/$userId/${UUID.randomUUID()}.jpg")
                val uploadTask = ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await()
                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun uploadChatAttachment(
        chatId: String, 
        fileUri: Uri, 
        fileName: String
    ): Result<AttachmentData> {
        return withContext(Dispatchers.IO) {
            try {
                val ref = storageRef.child("chat_attachments/$chatId/${UUID.randomUUID()}_$fileName")
                val uploadTask = ref.putFile(fileUri).await()
                val downloadUrl = ref.downloadUrl.await()
                val metadata = uploadTask.metadata
                
                Result.success(AttachmentData(
                    url = downloadUrl.toString(),
                    fileName = fileName,
                    fileSize = metadata?.sizeBytes ?: 0,
                    mimeType = metadata?.contentType ?: "application/octet-stream"
                ))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

data class AttachmentData(
    val url: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)
```

**Storage Security Rules** (storage.rules):
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // Profile pictures
    match /profile_pictures/{userId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isOwner(userId);
    }
    
    // Chat attachments
    match /chat_attachments/{chatId}/{fileName} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated();
    }
    
    // Limit file size to 10MB
    match /{allPaths=**} {
      allow write: if request.resource.size < 10 * 1024 * 1024;
    }
  }
}
```

### 4. AI Assignment Assistant

**Purpose**: Use Google Gemini API to help users create and manage assignments

**Architecture**:

```kotlin
// AI Service Interface
interface AIAssistantService {
    suspend fun sendMessage(message: String, conversationHistory: List<ChatMessage>): Result<AIResponse>
    suspend fun createAssignmentFromAI(aiSuggestion: String): Result<Task>
}

// Implementation
class GeminiAssistantService(
    private val apiKey: String,
    private val taskRepository: TaskRepository
) : AIAssistantService {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
    
    override suspend fun sendMessage(
        message: String, 
        conversationHistory: List<ChatMessage>
    ): Result<AIResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(message, conversationHistory)
                val response = callGeminiAPI(prompt)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun createAssignmentFromAI(aiSuggestion: String): Result<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val taskData = parseAIResponse(aiSuggestion)
                val task = Task(
                    id = UUID.randomUUID().toString(),
                    title = taskData.title,
                    description = taskData.description,
                    subject = taskData.subject,
                    dueDate = taskData.dueDate,
                    priority = taskData.priority,
                    userId = Firebase.auth.currentUser?.uid ?: "",
                    createdAt = Timestamp.now(),
                    status = "pending"
                )
                taskRepository.createTask(task)
                Result.success(task)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun buildPrompt(message: String, history: List<ChatMessage>): String {
        val systemPrompt = """
            You are an AI assistant helping students manage their assignments and tasks.
            You can help create assignments, suggest due dates, organize tasks by subject, and provide study tips.
            
            When creating an assignment, respond in this JSON format:
            {
              "action": "create_assignment",
              "title": "Assignment title",
              "description": "Detailed description",
              "subject": "Subject name",
              "dueDate": "YYYY-MM-DD",
              "priority": "high|medium|low"
            }
            
            For general conversation, respond naturally and helpfully.
        """.trimIndent()
        
        val conversationContext = history.joinToString("\n") { 
            "${it.role}: ${it.content}" 
        }
        
        return "$systemPrompt\n\n$conversationContext\nUser: $message\nAssistant:"
    }
    
    private suspend fun callGeminiAPI(prompt: String): AIResponse {
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
        }
        
        val request = Request.Builder()
            .url("$baseUrl?key=$apiKey")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        return parseGeminiResponse(responseBody)
    }
}

// UI Component
class AIAssistantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiAssistantBinding
    private lateinit var viewModel: AIAssistantViewModel
    private lateinit var adapter: AIMessageAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isNotBlank()) {
                viewModel.sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
    }
}
```

### 5. Theme and Color System

**Purpose**: Ensure consistent UI across all screens

**Implementation**:

**themes.xml** (values/themes.xml):
```xml
<resources>
    <style name="Theme.TeamSync" parent="Theme.Material3.Light.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/secondary</item>
        <item name="colorOnSecondary">@color/white</item>
        <item name="colorError">@color/error</item>
        <item name="colorOnError">@color/white</item>
        <item name="android:statusBarColor">@color/primary</item>
        <item name="android:navigationBarColor">@color/surface</item>
    </style>
</resources>
```

**themes.xml** (values-night/themes.xml):
```xml
<resources>
    <style name="Theme.TeamSync" parent="Theme.Material3.Dark.NoActionBar">
        <item name="colorPrimary">@color/primary_dark</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/secondary_dark</item>
        <item name="colorOnSecondary">@color/white</item>
        <item name="colorSurface">@color/surface_dark</item>
        <item name="colorOnSurface">@color/white</item>
        <item name="android:statusBarColor">@color/primary_dark</item>
    </style>
</resources>
```

**colors.xml**:
```xml
<resources>
    <!-- Light theme colors -->
    <color name="primary">#6200EE</color>
    <color name="secondary">#03DAC6</color>
    <color name="surface">#FFFFFF</color>
    <color name="error">#B00020</color>
    <color name="white">#FFFFFF</color>
    <color name="black">#000000</color>
    
    <!-- Dark theme colors -->
    <color name="primary_dark">#BB86FC</color>
    <color name="secondary_dark">#03DAC6</color>
    <color name="surface_dark">#121212</color>
    
    <!-- Text colors -->
    <color name="text_primary_light">#000000</color>
    <color name="text_secondary_light">#757575</color>
    <color name="text_primary_dark">#FFFFFF</color>
    <color name="text_secondary_dark">#B3B3B3</color>
</resources>
```

### 6. Error Handling Strategy

**Purpose**: Gracefully handle all error scenarios

**Implementation**:

```kotlin
sealed class AppError {
    data class PermissionDenied(val message: String) : AppError()
    data class NetworkError(val message: String) : AppError()
    data class ValidationError(val field: String, val message: String) : AppError()
    data class StorageError(val message: String) : AppError()
    data class AuthError(val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}

class ErrorHandler {
    fun handle(error: AppError, context: Context) {
        when (error) {
            is AppError.PermissionDenied -> {
                showDialog(context, "Permission Denied", 
                    "You don't have permission to perform this action. Please check your account settings.")
            }
            is AppError.NetworkError -> {
                showSnackbar(context, "Network error. Please check your connection.")
            }
            is AppError.ValidationError -> {
                showSnackbar(context, "${error.field}: ${error.message}")
            }
            is AppError.StorageError -> {
                showDialog(context, "Upload Failed", error.message)
            }
            is AppError.AuthError -> {
                showDialog(context, "Authentication Error", error.message)
            }
            is AppError.UnknownError -> {
                Log.e("ErrorHandler", "Unknown error", error.throwable)
                showSnackbar(context, "An unexpected error occurred")
            }
        }
    }
}

// Extension function for repositories
suspend fun <T> safeFirestoreCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: FirebaseFirestoreException) {
        when (e.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                Result.failure(AppError.PermissionDenied(e.message ?: "Permission denied"))
            FirebaseFirestoreException.Code.UNAVAILABLE -> 
                Result.failure(AppError.NetworkError("Service unavailable"))
            else -> 
                Result.failure(AppError.UnknownError(e))
        }
    } catch (e: Exception) {
        Result.failure(AppError.UnknownError(e))
    }
}
```

## Data Models

### Updated Message Model

```kotlin
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT,
    val readBy: List<String> = emptyList(),
    
    // Attachment fields
    val attachmentUrl: String? = null,
    val attachmentFileName: String? = null,
    val attachmentFileSize: Long? = null,
    val attachmentMimeType: String? = null
) {
    enum class MessageType {
        TEXT, IMAGE, FILE, AUDIO
    }
    
    enum class MessageStatus {
        SENDING, SENT, DELIVERED, READ, FAILED
    }
}
```

### AI Chat Message Model

```kotlin
data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: AIAction? = null
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class AIAction(
    val type: ActionType,
    val data: Map<String, Any>
)

enum class ActionType {
    CREATE_ASSIGNMENT,
    UPDATE_TASK,
    SUGGEST_SCHEDULE,
    PROVIDE_INFO
}
```

## Testing Strategy

### Unit Tests

1. **Security Rules Testing**:
   - Test each rule with valid and invalid user contexts
   - Verify field name corrections
   - Test edge cases (empty arrays, null values)

2. **Repository Tests**:
   - Mock Firestore responses
   - Test error handling paths
   - Verify data transformations

3. **ViewModel Tests**:
   - Test state management
   - Verify error propagation
   - Test UI state updates

### Integration Tests

1. **Storage Upload Flow**:
   - Test profile picture upload end-to-end
   - Test chat attachment upload
   - Verify error handling

2. **AI Assistant Flow**:
   - Test message sending
   - Test assignment creation from AI
   - Verify API error handling

3. **Chat Flow**:
   - Test message sending and receiving
   - Test RecyclerView updates
   - Verify no crashes on rapid updates

### Manual Testing Checklist

1. Create new Firebase project and configure
2. Deploy security rules
3. Test Google Sign-In
4. Create group and verify it appears
5. Create task and verify it appears
6. Send chat message with attachment
7. Upload profile picture
8. Use AI assistant to create assignment
9. Test in both light and dark modes
10. Test offline scenarios

## Firebase Project Setup Guide

### Step 1: Create New Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "TeamSync-Collaboration-New"
4. Disable Google Analytics (optional)
5. Click "Create project"

### Step 2: Add Android App

1. Click "Add app" → Android icon
2. Enter package name: `com.teamsync.collaboration`
3. Enter app nickname: "TeamSync Android"
4. Download `google-services.json`
5. Place in `app/` directory

### Step 3: Enable Authentication

1. Go to Authentication → Sign-in method
2. Enable "Email/Password"
3. Enable "Google"
4. Add SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Download updated `google-services.json`

### Step 4: Setup Firestore

1. Go to Firestore Database
2. Click "Create database"
3. Choose "Start in production mode"
4. Select region (closest to users)
5. Deploy security rules from `firestore.rules`

### Step 5: Setup Storage

1. Go to Storage
2. Click "Get started"
3. Start in production mode
4. Deploy security rules from `storage.rules`

### Step 6: Setup Cloud Messaging

1. Go to Cloud Messaging
2. Note the Server Key
3. Add to app configuration

### Step 7: Update App Configuration

Files to update:
- `app/google-services.json` (replace entire file)
- `app/build.gradle.kts` (verify dependencies)
- No code changes needed (uses Firebase SDK)

### Step 8: Deploy Rules

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Initialize project
firebase init

# Deploy rules
firebase deploy --only firestore:rules
firebase deploy --only storage:rules
```

### Step 9: Verification

1. Run app and sign in with Google
2. Check Firestore console for user document
3. Create a group - verify in Firestore
4. Send a message - verify in Firestore
5. Upload a file - verify in Storage

## Error Handling

All components will implement consistent error handling:

1. **Network Errors**: Retry with exponential backoff
2. **Permission Errors**: Show user-friendly message with action steps
3. **Validation Errors**: Highlight specific fields
4. **Storage Errors**: Show upload progress and failure reasons
5. **AI API Errors**: Fallback to manual task creation

## Performance Considerations

1. **Image Loading**: Use Coil with disk caching
2. **Message Pagination**: Load 50 messages at a time
3. **Firestore Queries**: Use proper indexes
4. **Storage Uploads**: Compress images before upload
5. **AI Requests**: Debounce user input, show loading states

## Security Considerations

1. **Authentication**: Verify user on all operations
2. **Storage**: Validate file types and sizes
3. **AI API**: Store API key securely (not in code)
4. **Input Validation**: Sanitize all user inputs
5. **Rate Limiting**: Implement on AI requests

