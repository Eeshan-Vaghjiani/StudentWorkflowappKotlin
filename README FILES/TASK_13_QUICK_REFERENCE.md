# Task 13: AI Assistant Service - Quick Reference

## Quick Start

### Initialize Service
```kotlin
val taskRepository = TaskRepository(context)
val apiKey = BuildConfig.GEMINI_API_KEY
val geminiService = GeminiAssistantService(apiKey, taskRepository)
```

### Send Message to AI
```kotlin
val result = geminiService.sendMessage(
    message = "Create a math homework assignment",
    conversationHistory = listOf()
)

result.onSuccess { response ->
    println(response.message)
}
```

### Create Task from AI
```kotlin
val taskResult = geminiService.createAssignmentFromAI(aiResponse)
taskResult.onSuccess { task ->
    println("Created: ${task.title}")
}
```

## Key Classes

### AIChatMessage
```kotlin
AIChatMessage(
    role = MessageRole.USER,
    content = "Create an assignment",
    timestamp = System.currentTimeMillis()
)
```

### AIResponse
```kotlin
AIResponse(
    message = "I've created your assignment",
    action = AIAction(ActionType.CREATE_ASSIGNMENT, data),
    success = true
)
```

### AITaskData
```kotlin
AITaskData(
    title = "Math Homework",
    description = "Complete exercises 1-10",
    subject = "Mathematics",
    dueDate = "2024-12-31",
    priority = "high"
)
```

## Message Roles
- `MessageRole.USER` - User messages
- `MessageRole.ASSISTANT` - AI responses
- `MessageRole.SYSTEM` - System messages

## Action Types
- `ActionType.CREATE_ASSIGNMENT` - Create new task
- `ActionType.UPDATE_TASK` - Update existing task
- `ActionType.SUGGEST_SCHEDULE` - Suggest schedule
- `ActionType.PROVIDE_INFO` - Information only

## Priority Levels
- `"low"` - Low priority
- `"medium"` - Medium priority (default)
- `"high"` - High priority

## Date Format
Use `YYYY-MM-DD` format:
```
"2024-12-31"
```

## Error Handling
```kotlin
result.onFailure { error ->
    when (error) {
        is IOException -> println("Network error")
        is JsonSyntaxException -> println("Parse error")
        else -> println("Unknown error: ${error.message}")
    }
}
```

## API Configuration

### 1. Get API Key
Visit: https://makersuite.google.com/app/apikey

### 2. Add to local.properties
```properties
GEMINI_API_KEY=your_api_key_here
```

### 3. Add to build.gradle.kts
```kotlin
android {
    buildFeatures {
        buildConfig = true
    }
    
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", 
            "\"${project.findProperty("GEMINI_API_KEY")}\"")
    }
}
```

### 4. Use in Code
```kotlin
val apiKey = BuildConfig.GEMINI_API_KEY
```

## Common Patterns

### Conversation Flow
```kotlin
val history = mutableListOf<AIChatMessage>()

// User message
history.add(AIChatMessage(MessageRole.USER, "Hello"))

// Send to AI
val response = geminiService.sendMessage("Hello", history)

// Add AI response to history
response.onSuccess { aiResponse ->
    history.add(AIChatMessage(MessageRole.ASSISTANT, aiResponse.message))
}
```

### Task Creation Flow
```kotlin
// 1. User asks to create task
val userMessage = "Create math homework for tomorrow"

// 2. Send to AI
val response = geminiService.sendMessage(userMessage)

// 3. Check for action
response.onSuccess { aiResponse ->
    if (aiResponse.action?.type == ActionType.CREATE_ASSIGNMENT) {
        // 4. Create task
        val taskResult = geminiService.createAssignmentFromAI(aiResponse.message)
        
        taskResult.onSuccess { task ->
            println("Task created: ${task.id}")
        }
    }
}
```

## Testing

### Run Unit Tests
```bash
./gradlew test --tests GeminiAssistantServiceTest
```

### Test Models
```kotlin
@Test
fun testAIChatMessage() {
    val message = AIChatMessage(
        role = MessageRole.USER,
        content = "Test message"
    )
    assert(message.role == MessageRole.USER)
}
```

## Logging

Enable detailed logging:
```kotlin
import android.util.Log

Log.d("GeminiService", "Sending message: $message")
Log.e("GeminiService", "Error occurred", exception)
```

## Performance Tips

1. **Limit conversation history** to last 5 messages
2. **Use timeouts** (default: 30 seconds)
3. **Cache responses** when appropriate
4. **Handle errors gracefully** with fallbacks

## Security Best Practices

1. ✅ Store API key in `local.properties`
2. ✅ Never commit API keys to version control
3. ✅ Validate user authentication before task creation
4. ✅ Use HTTPS for all API calls
5. ✅ Sanitize user input before sending to AI

## Troubleshooting

### API Key Not Found
```
Error: GEMINI_API_KEY not found
```
**Solution**: Add API key to `local.properties`

### Network Timeout
```
Error: timeout
```
**Solution**: Check internet connection, increase timeout

### Parse Error
```
Error: Failed to parse response
```
**Solution**: Check API response format, update parsing logic

### Authentication Error
```
Error: User not authenticated
```
**Solution**: Ensure user is signed in with Firebase Auth

## Dependencies Required

```kotlin
// OkHttp
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Gson
implementation("com.google.code.gson:gson:2.10.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

// Firebase
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
```

## File Locations

- Models: `models/AIModels.kt`
- Service: `services/GeminiAssistantService.kt`
- Tests: `test/.../GeminiAssistantServiceTest.kt`

## Next Steps

1. Create AIAssistantActivity (Task 14)
2. Create AIAssistantViewModel (Task 14)
3. Integrate with task creation UI (Task 15)
4. Add API key configuration (Task 16)
