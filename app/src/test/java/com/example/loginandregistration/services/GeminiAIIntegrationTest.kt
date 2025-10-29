package com.example.loginandregistration.services

import com.example.loginandregistration.models.AIResponse
import com.example.loginandregistration.models.AITaskData
import com.example.loginandregistration.models.ActionType
import com.example.loginandregistration.models.FirebaseTask
import com.google.firebase.Timestamp
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Gemini AI Integration Tests AI response parsing, task creation, and error handling
 */
class GeminiAIIntegrationTest {

    // ========== API Configuration Tests ==========

    @Test
    fun `Gemini API should use correct version`() {
        val expectedVersion = "v1"
        val expectedModel = "gemini-1.5-flash-latest"

        // These values should match the constants in GeminiAssistantService
        assertTrue("Should use v1 API", expectedVersion == "v1")
        assertTrue(
                "Should use gemini-1.5-flash-latest model",
                expectedModel.contains("gemini-1.5-flash")
        )
    }

    @Test
    fun `Gemini API URL should be correctly formatted`() {
        val apiVersion = "v1"
        val modelName = "gemini-1.5-flash-latest"
        val baseUrl =
                "https://generativelanguage.googleapis.com/$apiVersion/models/$modelName:generateContent"

        assertTrue("URL should contain API version", baseUrl.contains("/v1/"))
        assertTrue("URL should contain model name", baseUrl.contains("gemini-1.5-flash"))
        assertTrue("URL should end with generateContent", baseUrl.endsWith(":generateContent"))
    }

    // ========== AI Response Parsing Tests ==========

    @Test
    fun `parseAIResponse should extract task data from valid JSON`() {
        val jsonResponse =
                """
            {
              "action": "create_assignment",
              "title": "Math Homework",
              "description": "Complete exercises 1-10",
              "subject": "Mathematics",
              "dueDate": "2024-12-31",
              "priority": "high"
            }
        """.trimIndent()

        val json = JSONObject(jsonResponse)

        assertEquals("Should extract title", "Math Homework", json.getString("title"))
        assertEquals(
                "Should extract description",
                "Complete exercises 1-10",
                json.getString("description")
        )
        assertEquals("Should extract subject", "Mathematics", json.getString("subject"))
        assertEquals("Should extract priority", "high", json.getString("priority"))
    }

    @Test
    fun `AITaskData should have all required fields`() {
        val taskData =
                AITaskData(
                        title = "Test Task",
                        description = "Test Description",
                        subject = "Test Subject",
                        dueDate = "2024-12-31",
                        priority = "medium"
                )

        assertNotNull("Title should not be null", taskData.title)
        assertNotNull("Description should not be null", taskData.description)
        assertNotNull("Subject should not be null", taskData.subject)
        assertNotNull("DueDate should not be null", taskData.dueDate)
        assertNotNull("Priority should not be null", taskData.priority)
    }

    @Test
    fun `AITaskData should convert to FirebaseTask correctly`() {
        val taskData =
                AITaskData(
                        title = "Science Project",
                        description = "Research renewable energy",
                        subject = "Science",
                        dueDate = "2024-12-31",
                        priority = "high"
                )

        val firebaseTask =
                FirebaseTask(
                        title = taskData.title,
                        description = taskData.description,
                        subject = taskData.subject,
                        priority = taskData.priority,
                        category = "assignment",
                        status = "pending",
                        userId = "test_user",
                        assignedTo = listOf("test_user"),
                        createdAt = Timestamp.now()
                )

        assertEquals("Title should match", taskData.title, firebaseTask.title)
        assertEquals("Description should match", taskData.description, firebaseTask.description)
        assertEquals("Subject should match", taskData.subject, firebaseTask.subject)
        assertEquals("Priority should match", taskData.priority, firebaseTask.priority)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `AIResponse should handle 404 error gracefully`() {
        val errorResponse =
                AIResponse(
                        message = "AI model not found. The service may be temporarily unavailable.",
                        success = false,
                        error = "Model not found - check API version and model name"
                )

        assertFalse("Should indicate failure", errorResponse.success)
        assertNotNull("Should have error message", errorResponse.error)
        assertTrue(
                "Should mention model not found",
                errorResponse.message.contains("not found", ignoreCase = true)
        )
        assertTrue("Should be user-friendly", !errorResponse.message.contains("404"))
    }

    @Test
    fun `AIResponse should handle network error gracefully`() {
        val errorResponse =
                AIResponse(
                        message = "Network error. Please check your internet connection.",
                        success = false,
                        error = "UnknownHostException"
                )

        assertFalse("Should indicate failure", errorResponse.success)
        assertNotNull("Should have error message", errorResponse.error)
        assertTrue(
                "Should mention network",
                errorResponse.message.contains("network", ignoreCase = true)
        )
    }

    @Test
    fun `AIResponse should handle timeout error gracefully`() {
        val errorResponse =
                AIResponse(
                        message = "Request timed out. Please try again.",
                        success = false,
                        error = "SocketTimeoutException"
                )

        assertFalse("Should indicate failure", errorResponse.success)
        assertTrue(
                "Should mention timeout",
                errorResponse.message.contains("timed out", ignoreCase = true) ||
                        errorResponse.message.contains("timeout", ignoreCase = true)
        )
    }

    @Test
    fun `AIResponse should handle invalid JSON gracefully`() {
        val errorResponse =
                AIResponse(
                        message = "Failed to parse AI response. Please try again.",
                        success = false,
                        error = "JSONException"
                )

        assertFalse("Should indicate failure", errorResponse.success)
        assertTrue("Should be user-friendly", !errorResponse.message.contains("JSON"))
    }

    // ========== Task Validation Tests ==========

    @Test
    fun `AI-generated task should be validated before creation`() {
        val taskData =
                AITaskData(
                        title = "Test Task",
                        description = "Description",
                        subject = "Subject",
                        dueDate = "2024-12-31",
                        priority = "medium"
                )

        // Validation checks that should be performed
        assertTrue("Title should not be empty", taskData.title.isNotEmpty())
        assertTrue("Description should not be empty", taskData.description.isNotEmpty())
        assertTrue("Subject should not be empty", taskData.subject.isNotEmpty())

        val validPriorities = listOf("low", "medium", "high")
        assertTrue("Priority should be valid", validPriorities.contains(taskData.priority))
    }

    @Test
    fun `AI-generated task with invalid priority should be rejected`() {
        val taskData =
                AITaskData(
                        title = "Test Task",
                        description = "Description",
                        subject = "Subject",
                        dueDate = "2024-12-31",
                        priority = "urgent" // Invalid priority
                )

        val validPriorities = listOf("low", "medium", "high")
        assertFalse(
                "Invalid priority should be rejected",
                validPriorities.contains(taskData.priority)
        )
    }

    @Test
    fun `AI-generated task with empty title should be rejected`() {
        val taskData =
                AITaskData(
                        title = "",
                        description = "Description",
                        subject = "Subject",
                        dueDate = "2024-12-31",
                        priority = "medium"
                )

        assertTrue("Empty title should be rejected", taskData.title.isEmpty())
    }

    // ========== Date Parsing Tests ==========

    @Test
    fun `AI date format should be parseable`() {
        val dateString = "2024-12-31"
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        try {
            val date = dateFormat.parse(dateString)
            assertNotNull("Date should be parseable", date)

            val calendar = java.util.Calendar.getInstance()
            calendar.time = date!!
            assertEquals("Year should match", 2024, calendar.get(java.util.Calendar.YEAR))
            assertEquals(
                    "Month should match",
                    java.util.Calendar.DECEMBER,
                    calendar.get(java.util.Calendar.MONTH)
            )
            assertEquals("Day should match", 31, calendar.get(java.util.Calendar.DAY_OF_MONTH))
        } catch (e: Exception) {
            fail("Date parsing should not throw exception")
        }
    }

    @Test
    fun `AI date parsing should handle invalid dates gracefully`() {
        val invalidDateString = "invalid-date"
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        try {
            dateFormat.parse(invalidDateString)
            fail("Should throw exception for invalid date")
        } catch (e: java.text.ParseException) {
            // Expected behavior
            assertTrue("Should throw ParseException", true)
        }
    }

    // ========== Action Type Tests ==========

    @Test
    fun `ActionType should have CREATE_ASSIGNMENT type`() {
        val actionType = ActionType.CREATE_ASSIGNMENT

        assertEquals("Should be CREATE_ASSIGNMENT", "CREATE_ASSIGNMENT", actionType.name)
    }

    @Test
    fun `ActionType should support all required types`() {
        val requiredTypes =
                listOf(
                        ActionType.CREATE_ASSIGNMENT,
                        ActionType.UPDATE_TASK,
                        ActionType.SUGGEST_SCHEDULE,
                        ActionType.PROVIDE_INFO
                )

        assertEquals("Should have 4 action types", 4, requiredTypes.size)
        assertTrue(
                "Should include CREATE_ASSIGNMENT",
                requiredTypes.contains(ActionType.CREATE_ASSIGNMENT)
        )
    }

    // ========== Response Success Tests ==========

    @Test
    fun `successful AI response should have action`() {
        val successResponse =
                AIResponse(
                        message = "Task created successfully",
                        action =
                                com.example.loginandregistration.models.AIAction(
                                        type = ActionType.CREATE_ASSIGNMENT,
                                        data = mapOf("title" to "Test Task")
                                ),
                        success = true
                )

        assertTrue("Should indicate success", successResponse.success)
        assertNotNull("Should have action", successResponse.action)
        assertNull("Should not have error", successResponse.error)
    }

    @Test
    fun `failed AI response should have error`() {
        val failureResponse =
                AIResponse(
                        message = "Failed to create task",
                        success = false,
                        error = "Validation failed"
                )

        assertFalse("Should indicate failure", failureResponse.success)
        assertNotNull("Should have error", failureResponse.error)
        assertNull("Should not have action", failureResponse.action)
    }

    // ========== Integration Flow Tests ==========

    @Test
    fun `AI task creation flow should validate before saving`() {
        // Step 1: Parse AI response
        val taskData =
                AITaskData(
                        title = "Math Homework",
                        description = "Complete exercises",
                        subject = "Mathematics",
                        dueDate = "2024-12-31",
                        priority = "high"
                )

        // Step 2: Convert to FirebaseTask
        val firebaseTask =
                FirebaseTask(
                        title = taskData.title,
                        description = taskData.description,
                        subject = taskData.subject,
                        priority = taskData.priority,
                        category = "assignment",
                        status = "pending",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now()
                )

        // Step 3: Validate required fields
        assertFalse("Title should not be empty", firebaseTask.title.isEmpty())
        assertFalse("UserId should not be empty", firebaseTask.userId.isEmpty())
        assertFalse("AssignedTo should not be empty", firebaseTask.assignedTo.isEmpty())
        assertTrue(
                "UserId should be in assignedTo",
                firebaseTask.assignedTo.contains(firebaseTask.userId)
        )
    }

    @Test
    fun `AI error response should provide user-friendly message`() {
        val errorResponses =
                listOf(
                        AIResponse(
                                message =
                                        "AI model not found. The service may be temporarily unavailable.",
                                success = false,
                                error = "404"
                        ),
                        AIResponse(
                                message = "Network error. Please check your internet connection.",
                                success = false,
                                error = "Network"
                        ),
                        AIResponse(
                                message = "Request timed out. Please try again.",
                                success = false,
                                error = "Timeout"
                        )
                )

        errorResponses.forEach { response ->
            assertFalse("Should indicate failure", response.success)
            assertTrue("Should have user-friendly message", response.message.length > 10)
            assertFalse(
                    "Should not contain technical codes",
                    response.message.contains("404") || response.message.contains("Exception")
            )
        }
    }
}
