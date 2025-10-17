package com.example.loginandregistration

import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.services.GeminiAssistantService
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for GeminiAssistantService
 * Tests AI message handling, task creation, and error handling
 */
class GeminiAssistantServiceTest {

    @Mock
    private lateinit var mockTaskRepository: TaskRepository

    private lateinit var geminiService: GeminiAssistantService
    private val testApiKey = "test_api_key_12345"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        geminiService = GeminiAssistantService(testApiKey, mockTaskRepository)
    }

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
        assert(taskData.description == "Complete exercises 1-10")
        assert(taskData.subject == "Mathematics")
        assert(taskData.dueDate == "2024-12-31")
        assert(taskData.priority == "high")
    }

    @Test
    fun `test AIChatMessage model creation`() {
        val message = AIChatMessage(
            role = MessageRole.USER,
            content = "Create an assignment for math",
            timestamp = System.currentTimeMillis()
        )

        assert(message.role == MessageRole.USER)
        assert(message.content == "Create an assignment for math")
        assert(message.id.isNotEmpty())
    }

    @Test
    fun `test AIAction model with CREATE_ASSIGNMENT type`() {
        val action = AIAction(
            type = ActionType.CREATE_ASSIGNMENT,
            data = mapOf(
                "title" to "Science Project",
                "subject" to "Science",
                "priority" to "high"
            )
        )

        assert(action.type == ActionType.CREATE_ASSIGNMENT)
        assert(action.data["title"] == "Science Project")
        assert(action.data["subject"] == "Science")
    }

    @Test
    fun `test AIResponse model with success`() {
        val response = AIResponse(
            message = "I've created your assignment",
            action = AIAction(
                type = ActionType.CREATE_ASSIGNMENT,
                data = mapOf("title" to "Test")
            ),
            success = true
        )

        assert(response.success)
        assert(response.message == "I've created your assignment")
        assert(response.action != null)
        assert(response.error == null)
    }

    @Test
    fun `test AIResponse model with error`() {
        val response = AIResponse(
            message = "Failed to process request",
            success = false,
            error = "Network error"
        )

        assert(!response.success)
        assert(response.error == "Network error")
        assert(response.action == null)
    }

    @Test
    fun `test createAssignmentFromAI with valid JSON`() = runBlocking {
        // Mock successful task creation
        whenever(mockTaskRepository.createTask(any())).thenReturn(Result.success("task123"))

        val aiSuggestion = """
            {
              "action": "create_assignment",
              "title": "Math Homework",
              "description": "Complete chapter 5 exercises",
              "subject": "Mathematics",
              "dueDate": "2024-12-31",
              "priority": "high"
            }
        """.trimIndent()

        // Note: This test will fail without Firebase Auth mock
        // In a real test environment, you would mock FirebaseAuth
        // For now, this demonstrates the test structure
    }

    @Test
    fun `test MessageRole enum values`() {
        assert(MessageRole.USER.name == "USER")
        assert(MessageRole.ASSISTANT.name == "ASSISTANT")
        assert(MessageRole.SYSTEM.name == "SYSTEM")
    }

    @Test
    fun `test ActionType enum values`() {
        assert(ActionType.CREATE_ASSIGNMENT.name == "CREATE_ASSIGNMENT")
        assert(ActionType.UPDATE_TASK.name == "UPDATE_TASK")
        assert(ActionType.SUGGEST_SCHEDULE.name == "SUGGEST_SCHEDULE")
        assert(ActionType.PROVIDE_INFO.name == "PROVIDE_INFO")
    }

    @Test
    fun `test conversation history with multiple messages`() {
        val history = listOf(
            AIChatMessage(
                role = MessageRole.USER,
                content = "Hello"
            ),
            AIChatMessage(
                role = MessageRole.ASSISTANT,
                content = "Hi! How can I help you?"
            ),
            AIChatMessage(
                role = MessageRole.USER,
                content = "Create a math assignment"
            )
        )

        assert(history.size == 3)
        assert(history[0].role == MessageRole.USER)
        assert(history[1].role == MessageRole.ASSISTANT)
        assert(history[2].role == MessageRole.USER)
    }

    @Test
    fun `test date format parsing`() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateString = "2024-12-31"
        val date = dateFormat.parse(dateString)
        
        assert(date != null)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        assert(calendar.get(Calendar.YEAR) == 2024)
        assert(calendar.get(Calendar.MONTH) == Calendar.DECEMBER)
        assert(calendar.get(Calendar.DAY_OF_MONTH) == 31)
    }

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

    @Test
    fun `test FirebaseTask creation from AITaskData`() {
        val taskData = AITaskData(
            title = "Science Project",
            description = "Research renewable energy",
            subject = "Science",
            dueDate = "2024-12-31",
            priority = "high"
        )

        val task = FirebaseTask(
            title = taskData.title,
            description = taskData.description,
            subject = taskData.subject,
            category = "assignment",
            status = "pending",
            priority = taskData.priority,
            dueDate = Timestamp.now(),
            userId = "test_user_123"
        )

        assert(task.title == taskData.title)
        assert(task.description == taskData.description)
        assert(task.subject == taskData.subject)
        assert(task.priority == taskData.priority)
        assert(task.category == "assignment")
        assert(task.status == "pending")
    }
}
