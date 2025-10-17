# Task 13: AI Assistant Service - Testing Guide

## Overview
This guide covers testing the GeminiAssistantService implementation, including unit tests, integration tests, and manual testing procedures.

## Unit Tests

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests GeminiAssistantServiceTest

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Coverage

#### 1. Model Tests
```kotlin
@Test
fun `test AITaskData model creation`() {
    val taskData = AITaskData(
        title = "Math Homework",
        description = "Complete exercises 1-10",
        subject = "Mathematics",
        dueDate = "2024-12-31",
        priority = "high"
    )
    
    assert(taskData.title == "Math Homework")
    assert(taskData.priority == "high")
}
```

#### 2. Message Role Tests
```kotlin
@Test
fun `test MessageRole enum values`() {
    assert(MessageRole.USER.name == "USER")
    assert(MessageRole.ASSISTANT.name == "ASSISTANT")
    assert(MessageRole.SYSTEM.name == "SYSTEM")
}
```

#### 3. Action Type Tests
```kotlin
@Test
fun `test ActionType enum values`() {
    assert(ActionType.CREATE_ASSIGNMENT.name == "CREATE_ASSIGNMENT")
    assert(ActionType.UPDATE_TASK.name == "UPDATE_TASK")
}
```

#### 4. Response Handling Tests
```kotlin
@Test
fun `test AIResponse with success`() {
    val response = AIResponse(
        message = "Task created",
        success = true
    )
    assert(response.success)
    assert(response.error == null)
}

@Test
fun `test AIResponse with error`() {
    val response = AIResponse(
        message = "Failed",
        success = false,
        error = "Network error"
    )
    assert(!response.success)
    assert(response.error != null)
}
```

#### 5. Date Parsing Tests
```kotlin
@Test
fun `test date format parsing`() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = dateFormat.parse("2024-12-31")
    assert(date != null)
}
```

#### 6. Priority Validation Tests
```kotlin
@Test
fun `test priority values are valid`() {
    val validPriorities = listOf("low", "medium", "high")
    validPriorities.forEach { priority ->
        val taskData = AITaskData(
            title = "Test",
            description = "Test",
            subject = "Test",
            dueDate = "2024-12-31",
            priority = priority
        )
        assert(taskData.priority in validPriorities)
    }
}
```

## Integration Tests

### Setup Test Environment
```kotlin
@Before
fun setup() {
    MockitoAnnotations.openMocks(this)
    
    // Mock Firebase Auth
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)
    whenever(mockAuth.currentUser).thenReturn(mockUser)
    whenever(mockUser.uid).thenReturn("test_user_123")
    
    // Mock TaskRepository
    mockTaskRepository = mock(TaskRepository::class.java)
    
    // Initialize service
    geminiService = GeminiAssistantService(testApiKey, mockTaskRepository)
}
```

### Test API Integration (Mock)
```kotlin
@Test
fun `test sendMessage with mock API`() = runBlocking {
    // This would require mocking OkHttp responses
    // For actual API testing, use a test API key
    
    val message = "Create a math assignment"
    val result = geminiService.sendMessage(message)
    
    // Verify result structure
    result.onSuccess { response ->
        assert(response.message.isNotEmpty())
    }
}
```

### Test Task Creation
```kotlin
@Test
fun `test createAssignmentFromAI with valid data`() = runBlocking {
    // Mock successful task creation
    whenever(mockTaskRepository.createTask(any()))
        .thenReturn(Result.success("task123"))
    
    val aiSuggestion = """
        {
          "action": "create_assignment",
          "title": "Math Homework",
          "description": "Complete chapter 5",
          "subject": "Mathematics",
          "dueDate": "2024-12-31",
          "priority": "high"
        }
    """.trimIndent()
    
    val result = geminiService.createAssignmentFromAI(aiSuggestion)
    
    result.onSuccess { task ->
        assert(task.title == "Math Homework")
        assert(task.subject == "Mathematics")
        assert(task.priority == "high")
    }
}
```

## Manual Testing

### Prerequisites
1. Valid Gemini API key
2. Firebase project configured
3. User authenticated
4. Internet connection

### Test Scenarios

#### Scenario 1: Basic Message Sending
```kotlin
// In your test activity or fragment
val geminiService = GeminiAssistantService(
    BuildConfig.GEMINI_API_KEY,
    TaskRepository(context)
)

lifecycleScope.launch {
    val result = geminiService.sendMessage("Hello, can you help me?")
    result.onSuccess { response ->
        Log.d("Test", "AI Response: ${response.message}")
        // Expected: Friendly greeting and offer to help
    }
    result.onFailure { error ->
        Log.e("Test", "Error: ${error.message}")
    }
}
```

**Expected Result**: AI responds with a friendly greeting

#### Scenario 2: Create Assignment with Full Details
```kotlin
val message = """
    Create a math assignment for tomorrow.
    Title: Algebra Practice
    Description: Solve equations from chapter 3
    Subject: Mathematics
    Priority: High
""".trimIndent()

lifecycleScope.launch {
    val result = geminiService.sendMessage(message)
    result.onSuccess { response ->
        Log.d("Test", "Response: ${response.message}")
        
        if (response.action?.type == ActionType.CREATE_ASSIGNMENT) {
            val taskResult = geminiService.createAssignmentFromAI(response.message)
            taskResult.onSuccess { task ->
                Log.d("Test", "Task created: ${task.title}")
                // Verify task in Firestore
            }
        }
    }
}
```

**Expected Result**: 
- AI creates JSON with assignment details
- Task is created in Firestore
- Task appears in tasks list

#### Scenario 3: Create Assignment with Minimal Details
```kotlin
val message = "Create a science homework assignment"

lifecycleScope.launch {
    val result = geminiService.sendMessage(message)
    result.onSuccess { response ->
        if (response.action?.type == ActionType.CREATE_ASSIGNMENT) {
            val taskResult = geminiService.createAssignmentFromAI(response.message)
            taskResult.onSuccess { task ->
                // Verify defaults are applied
                assert(task.subject == "Science" || task.subject == "General")
                assert(task.priority == "medium")
                assert(task.dueDate != null)
            }
        }
    }
}
```

**Expected Result**: Task created with sensible defaults

#### Scenario 4: Conversation Flow
```kotlin
val history = mutableListOf<AIChatMessage>()

// Message 1
history.add(AIChatMessage(MessageRole.USER, "Hello"))
var result = geminiService.sendMessage("Hello", history)
result.onSuccess { response ->
    history.add(AIChatMessage(MessageRole.ASSISTANT, response.message))
}

// Message 2
history.add(AIChatMessage(MessageRole.USER, "I need help with homework"))
result = geminiService.sendMessage("I need help with homework", history)
result.onSuccess { response ->
    history.add(AIChatMessage(MessageRole.ASSISTANT, response.message))
}

// Message 3
history.add(AIChatMessage(MessageRole.USER, "Create a math assignment"))
result = geminiService.sendMessage("Create a math assignment", history)
```

**Expected Result**: AI maintains context across messages

#### Scenario 5: Error Handling - No Internet
```kotlin
// Turn off internet connection
val result = geminiService.sendMessage("Test message")
result.onFailure { error ->
    Log.e("Test", "Error: ${error.message}")
    // Expected: Network error message
}
```

**Expected Result**: Graceful error handling with user-friendly message

#### Scenario 6: Error Handling - Invalid API Key
```kotlin
val invalidService = GeminiAssistantService(
    "invalid_key",
    TaskRepository(context)
)

val result = invalidService.sendMessage("Test")
result.onFailure { error ->
    // Expected: API authentication error
}
```

**Expected Result**: API error detected and handled

#### Scenario 7: Date Parsing
```kotlin
val testDates = listOf(
    "2024-12-31",
    "12/31/2024",
    "31/12/2024"
)

testDates.forEach { dateString ->
    val aiSuggestion = """
        {
          "title": "Test",
          "description": "Test",
          "subject": "Test",
          "dueDate": "$dateString",
          "priority": "medium"
        }
    """.trimIndent()
    
    val result = geminiService.createAssignmentFromAI(aiSuggestion)
    result.onSuccess { task ->
        assert(task.dueDate != null)
        Log.d("Test", "Date parsed successfully: $dateString")
    }
}
```

**Expected Result**: All date formats parsed correctly

## Performance Testing

### Test Response Time
```kotlin
val startTime = System.currentTimeMillis()

val result = geminiService.sendMessage("Create an assignment")

result.onSuccess { response ->
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    Log.d("Performance", "Response time: ${duration}ms")
    // Expected: < 5000ms for most requests
}
```

### Test Timeout Handling
```kotlin
// Service has 30-second timeout
// Test with slow network or large prompt
val largePrompt = "Create assignment " + "x".repeat(10000)

val result = geminiService.sendMessage(largePrompt)
// Should timeout gracefully after 30 seconds
```

## Verification Checklist

### ✅ Models
- [ ] AIChatMessage creates with all fields
- [ ] MessageRole enum has all values
- [ ] ActionType enum has all values
- [ ] AIResponse handles success and error
- [ ] AITaskData validates all fields

### ✅ Service Methods
- [ ] sendMessage() returns valid response
- [ ] createAssignmentFromAI() creates task
- [ ] Prompt building includes system instructions
- [ ] API calls use correct endpoint
- [ ] Response parsing handles JSON

### ✅ Error Handling
- [ ] Network errors handled gracefully
- [ ] API errors logged and reported
- [ ] Parse errors don't crash app
- [ ] Authentication errors detected
- [ ] Timeout errors handled

### ✅ Date Handling
- [ ] YYYY-MM-DD format parsed
- [ ] MM/DD/YYYY format parsed
- [ ] DD/MM/YYYY format parsed
- [ ] Invalid dates use default
- [ ] Default is 7 days from now

### ✅ Task Creation
- [ ] Task saved to Firestore
- [ ] All required fields populated
- [ ] Defaults applied correctly
- [ ] User ID set correctly
- [ ] Timestamps set correctly

### ✅ Conversation
- [ ] History maintained correctly
- [ ] Context passed to AI
- [ ] Last 5 messages used
- [ ] Messages ordered correctly

## Common Issues and Solutions

### Issue: API Key Not Found
**Solution**: 
```kotlin
// Check local.properties
GEMINI_API_KEY=your_key_here

// Verify BuildConfig
Log.d("Test", "API Key: ${BuildConfig.GEMINI_API_KEY}")
```

### Issue: Network Timeout
**Solution**: Increase timeout or check connection
```kotlin
private const val TIMEOUT_SECONDS = 60L // Increase if needed
```

### Issue: Parse Error
**Solution**: Add more robust error handling
```kotlin
try {
    val json = JsonParser.parseString(text)
} catch (e: JsonSyntaxException) {
    // Fallback to text-only response
}
```

### Issue: Task Not Created
**Solution**: Check Firebase Auth and permissions
```kotlin
val user = FirebaseAuth.getInstance().currentUser
if (user == null) {
    Log.e("Test", "User not authenticated")
}
```

## Test Data

### Sample AI Responses
```json
{
  "action": "create_assignment",
  "title": "Math Homework",
  "description": "Complete exercises 1-10 from chapter 5",
  "subject": "Mathematics",
  "dueDate": "2024-12-31",
  "priority": "high"
}
```

### Sample Conversation
```
User: Hello
AI: Hi! I'm here to help you manage your assignments. What would you like to do?

User: Create a math assignment
AI: I'd be happy to help! Could you provide more details about the assignment?

User: Algebra practice for tomorrow, high priority
AI: {JSON with assignment details}
```

## Continuous Testing

### Automated Tests
Run tests on every commit:
```bash
# In CI/CD pipeline
./gradlew test
./gradlew connectedAndroidTest
```

### Manual Testing Schedule
- **Daily**: Basic message sending
- **Weekly**: Full conversation flow
- **Before Release**: All scenarios

## Success Criteria

✅ All unit tests pass
✅ API integration works with real key
✅ Tasks created successfully
✅ Errors handled gracefully
✅ Performance within acceptable limits
✅ No memory leaks
✅ Logs provide useful debugging info

## Next Steps

After testing is complete:
1. Proceed to Task 14 (AI Assistant UI)
2. Integrate service with ViewModel
3. Add user-facing error messages
4. Implement loading states
5. Add analytics tracking
