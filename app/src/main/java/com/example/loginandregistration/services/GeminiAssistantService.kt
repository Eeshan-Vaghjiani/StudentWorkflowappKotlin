package com.example.loginandregistration.services

import android.util.Log
import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.TaskRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Service for interacting with Google Gemini AI API Provides AI-powered assignment creation and
 * task management assistance
 */
class GeminiAssistantService(
        private val apiKey: String,
        private val taskRepository: TaskRepository
) {

    companion object {
        private const val TAG = "GeminiAssistantService"
        // Updated to use v1 API and correct model name
        private const val API_VERSION = "v1"
        private const val MODEL_NAME = "gemini-1.5-flash-latest"
        private const val BASE_URL =
                "https://generativelanguage.googleapis.com/$API_VERSION/models/$MODEL_NAME:generateContent"
        private const val TIMEOUT_SECONDS = 30L
    }

    private val client =
            OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build()

    private val gson = Gson()

    /**
     * Send a message to the AI and get a response
     * @param message The user's message
     * @param conversationHistory Previous messages in the conversation
     * @return Result containing AIResponse or error
     */
    suspend fun sendMessage(
            message: String,
            conversationHistory: List<AIChatMessage> = emptyList()
    ): Result<AIResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending message to Gemini API: $message")

                val prompt = buildPrompt(message, conversationHistory)
                val response = callGeminiAPI(prompt)

                Log.d(TAG, "Received response from Gemini API")
                Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message to Gemini API", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Create an assignment/task from AI suggestion
     * @param aiSuggestion The AI's suggestion text (can be JSON or natural language)
     * @return Result containing the created FirebaseTask or error
     */
    suspend fun createAssignmentFromAI(aiSuggestion: String): Result<FirebaseTask> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating assignment from AI suggestion")

                val taskData = parseAIResponse(aiSuggestion)
                val userId =
                        FirebaseAuth.getInstance().currentUser?.uid
                                ?: return@withContext Result.failure(
                                        Exception("User not authenticated")
                                )

                // Validate AI-generated task data before processing
                val validationErrors = validateAITaskData(taskData)
                if (validationErrors.isNotEmpty()) {
                    val errorMessage = "Invalid task data: ${validationErrors.joinToString(", ")}"
                    Log.e(TAG, errorMessage)
                    return@withContext Result.failure(Exception(errorMessage))
                }

                // Convert due date string to Timestamp
                val dueDate = parseDateString(taskData.dueDate)

                val task =
                        FirebaseTask(
                                id = "", // Will be set by Firestore
                                title = taskData.title,
                                description = taskData.description,
                                subject = taskData.subject,
                                category = "assignment",
                                status = "pending",
                                priority = taskData.priority,
                                dueDate = dueDate,
                                createdAt = Timestamp.now(),
                                updatedAt = Timestamp.now(),
                                userId = userId,
                                groupId = null,
                                assignedTo = listOf(userId), // User must be in assignedTo array
                                completedAt = null
                        )

                // Validate the complete task object
                val taskValidationErrors = validateFirebaseTask(task)
                if (taskValidationErrors.isNotEmpty()) {
                    val errorMessage =
                            "Task validation failed: ${taskValidationErrors.joinToString(", ")}"
                    Log.e(TAG, errorMessage)
                    return@withContext Result.failure(Exception(errorMessage))
                }

                // Create task in repository
                val result = taskRepository.createTask(task)

                if (result.isSuccess) {
                    val taskId = result.getOrNull() ?: ""
                    Log.d(TAG, "Task created successfully with ID: $taskId")
                    Result.success(task.copy(id = taskId))
                } else {
                    Log.e(TAG, "Failed to create task", result.exceptionOrNull())
                    Result.failure(result.exceptionOrNull() ?: Exception("Failed to create task"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating assignment from AI", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Validate AI-generated task data
     * @param taskData The task data parsed from AI response
     * @return List of validation errors (empty if valid)
     */
    private fun validateAITaskData(taskData: AITaskData): List<String> {
        val errors = mutableListOf<String>()

        // Validate required fields are present
        if (taskData.title.isBlank()) {
            errors.add("Title is required")
        }

        // Validate title length
        if (taskData.title.length > 200) {
            errors.add("Title must be 200 characters or less")
        }

        // Validate description length
        if (taskData.description.length > 5000) {
            errors.add("Description must be 5000 characters or less")
        }

        // Validate subject
        if (taskData.subject.isBlank()) {
            errors.add("Subject is required")
        }

        // Validate priority
        val validPriorities = listOf("low", "medium", "high")
        if (taskData.priority.lowercase() !in validPriorities) {
            errors.add("Priority must be one of: low, medium, high")
        }

        // Validate due date format (YYYY-MM-DD)
        val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        if (!datePattern.matches(taskData.dueDate)) {
            errors.add("Due date must be in YYYY-MM-DD format")
        }

        return errors
    }

    /**
     * Validate complete FirebaseTask object
     * @param task The task to validate
     * @return List of validation errors (empty if valid)
     */
    private fun validateFirebaseTask(task: FirebaseTask): List<String> {
        val errors = mutableListOf<String>()

        // Validate required fields
        if (task.title.isBlank()) {
            errors.add("Task title is required")
        }

        if (task.userId.isBlank()) {
            errors.add("User ID is required")
        }

        // Validate assignedTo array
        if (task.assignedTo.isEmpty()) {
            errors.add("At least one assignee is required")
        }

        if (task.assignedTo.size > 50) {
            errors.add("Maximum 50 assignees allowed")
        }

        // Validate user is in assignedTo array
        if (task.userId.isNotBlank() && task.userId !in task.assignedTo) {
            errors.add("Task creator must be in assignedTo array")
        }

        // Validate timestamp is recent (within 5 minutes)
        val now = System.currentTimeMillis()
        val taskTime = task.createdAt.toDate().time
        if (Math.abs(now - taskTime) > 300000) { // 5 minutes in milliseconds
            errors.add("Task timestamp must be within 5 minutes of current time")
        }

        return errors
    }

    /** Build the prompt for the AI including system instructions and conversation history */
    private fun buildPrompt(message: String, history: List<AIChatMessage>): String {
        val systemPrompt =
                """
            You are an AI assistant helping students manage their assignments and tasks.
            You can help create assignments, suggest due dates, organize tasks by subject, and provide study tips.
            
            When a user asks you to create an assignment or task, respond with a JSON object in this exact format:
            {
              "action": "create_assignment",
              "title": "Assignment title",
              "description": "Detailed description of the assignment",
              "subject": "Subject name (e.g., Math, Science, English)",
              "dueDate": "YYYY-MM-DD",
              "priority": "high|medium|low"
            }
            
            Guidelines for creating assignments:
            - Extract the assignment title from the user's message
            - Create a helpful description based on what the user said
            - Infer the subject if mentioned, otherwise use "General"
            - Suggest a reasonable due date (default to 7 days from now if not specified)
            - Set priority based on urgency mentioned (default to "medium")
            
            For general conversation or questions, respond naturally and helpfully without JSON.
            Be concise, friendly, and supportive.
        """.trimIndent()

        // Build conversation context
        val conversationContext =
                if (history.isNotEmpty()) {
                    history.takeLast(5).joinToString("\n") { "${it.role.name}: ${it.content}" }
                } else {
                    ""
                }

        return if (conversationContext.isNotEmpty()) {
            "$systemPrompt\n\nConversation history:\n$conversationContext\n\nUser: $message\nAssistant:"
        } else {
            "$systemPrompt\n\nUser: $message\nAssistant:"
        }
    }

    /** Call the Gemini API with the given prompt */
    private suspend fun callGeminiAPI(prompt: String): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                // Build request body according to Gemini API format
                val requestBody =
                        JSONObject().apply {
                            put(
                                    "contents",
                                    JSONArray().apply {
                                        put(
                                                JSONObject().apply {
                                                    put(
                                                            "parts",
                                                            JSONArray().apply {
                                                                put(
                                                                        JSONObject().apply {
                                                                            put("text", prompt)
                                                                        }
                                                                )
                                                            }
                                                    )
                                                }
                                        )
                                    }
                            )
                            put(
                                    "generationConfig",
                                    JSONObject().apply {
                                        put("temperature", 0.7)
                                        put("topK", 40)
                                        put("topP", 0.95)
                                        put(
                                                "maxOutputTokens",
                                                2048
                                        ) // Increased for better responses
                                    }
                            )
                        }

                val request =
                        Request.Builder()
                                .url("$BASE_URL?key=$apiKey")
                                .post(
                                        requestBody
                                                .toString()
                                                .toRequestBody("application/json".toMediaType())
                                )
                                .addHeader("Content-Type", "application/json")
                                .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    // Log detailed error information for debugging
                    Log.e(TAG, "API call failed with status ${response.code}")
                    Log.e(TAG, "Response message: ${response.message}")
                    Log.e(TAG, "Response body: $responseBody")

                    // Enhanced error handling with user-friendly messages
                    val errorMessage =
                            when (response.code) {
                                400 -> "Invalid request. Please try rephrasing your message."
                                401, 403 ->
                                        "AI service authentication failed. Please contact support."
                                404 ->
                                        "AI model not found. The service may be temporarily unavailable. Please try again later."
                                429 -> "Too many requests. Please wait a moment and try again."
                                500, 502, 503 ->
                                        "AI service is temporarily unavailable. Please try again later."
                                504 -> "Request timed out on the server. Please try again."
                                else ->
                                        "Unable to reach AI assistant (Error ${response.code}). Please try again."
                            }

                    return@withContext AIResponse(
                            message = errorMessage,
                            success = false,
                            error =
                                    "HTTP ${response.code}: ${response.message} - Body: ${responseBody?.take(200)}"
                    )
                }

                if (responseBody == null) {
                    return@withContext AIResponse(
                            message = "AI assistant returned an empty response. Please try again.",
                            success = false,
                            error = "Empty response body"
                    )
                }

                parseGeminiResponse(responseBody)
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Request timeout", e)
                AIResponse(
                        message =
                                "Request timed out. Please check your internet connection and try again.",
                        success = false,
                        error = "Timeout: ${e.message}"
                )
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Network error - no internet connection", e)
                AIResponse(
                        message =
                                "No internet connection. Please check your network and try again.",
                        success = false,
                        error = "Network error: ${e.message}"
                )
            } catch (e: java.io.IOException) {
                Log.e(TAG, "Network I/O error", e)
                AIResponse(
                        message =
                                "Network error occurred. Please check your connection and try again.",
                        success = false,
                        error = "I/O error: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error calling Gemini API", e)
                AIResponse(
                        message = "An unexpected error occurred. Please try again later.",
                        success = false,
                        error = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    /** Parse the response from Gemini API */
    private fun parseGeminiResponse(responseBody: String): AIResponse {
        try {
            val jsonResponse = JSONObject(responseBody)

            // Extract the text from the response
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return AIResponse(
                        message = "Sorry, I couldn't generate a response.",
                        success = false,
                        error = "No candidates in response"
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.getJSONObject(0)?.optString("text") ?: ""

            if (text.isEmpty()) {
                return AIResponse(
                        message = "Sorry, I couldn't generate a response.",
                        success = false,
                        error = "Empty text in response"
                )
            }

            // Check if the response contains a JSON action
            val action = extractActionFromText(text)

            return AIResponse(message = text, action = action, success = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
            return AIResponse(
                    message = "Sorry, I had trouble understanding the response.",
                    success = false,
                    error = e.message
            )
        }
    }

    /** Extract action JSON from AI response text */
    private fun extractActionFromText(text: String): AIAction? {
        try {
            // Look for JSON object in the text
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1

            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return null
            }

            val jsonString = text.substring(jsonStart, jsonEnd)
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject

            val actionType = jsonObject.get("action")?.asString

            if (actionType == "create_assignment") {
                val data = mutableMapOf<String, Any>()
                jsonObject.entrySet().forEach { (key, value) ->
                    if (key != "action") {
                        data[key] = value.asString
                    }
                }

                return AIAction(type = ActionType.CREATE_ASSIGNMENT, data = data)
            }
        } catch (e: Exception) {
            Log.d(TAG, "No action found in text (this is normal for conversational responses)")
        }

        return null
    }

    /** Parse AI response to extract task data Handles both JSON format and natural language */
    private fun parseAIResponse(aiSuggestion: String): AITaskData {
        try {
            // Try to parse as JSON first
            val jsonStart = aiSuggestion.indexOf("{")
            val jsonEnd = aiSuggestion.lastIndexOf("}") + 1

            if (jsonStart != -1 && jsonEnd > jsonStart) {
                val jsonString = aiSuggestion.substring(jsonStart, jsonEnd)
                val jsonObject = JsonParser.parseString(jsonString).asJsonObject

                return AITaskData(
                        title = jsonObject.get("title")?.asString ?: "New Assignment",
                        description = jsonObject.get("description")?.asString ?: "",
                        subject = jsonObject.get("subject")?.asString ?: "General",
                        dueDate = jsonObject.get("dueDate")?.asString ?: getDefaultDueDate(),
                        priority = jsonObject.get("priority")?.asString?.lowercase() ?: "medium"
                )
            }

            // If not JSON, create a basic task from the text
            return AITaskData(
                    title = aiSuggestion.take(50),
                    description = aiSuggestion,
                    subject = "General",
                    dueDate = getDefaultDueDate(),
                    priority = "medium"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AI response", e)
            // Return default task data
            return AITaskData(
                    title = "New Assignment",
                    description = aiSuggestion,
                    subject = "General",
                    dueDate = getDefaultDueDate(),
                    priority = "medium"
            )
        }
    }

    /** Get default due date (7 days from now) */
    private fun getDefaultDueDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }

    /** Parse date string to Timestamp Supports formats: YYYY-MM-DD, MM/DD/YYYY */
    private fun parseDateString(dateString: String): Timestamp {
        try {
            val formats =
                    listOf(
                            SimpleDateFormat("yyyy-MM-dd", Locale.US),
                            SimpleDateFormat("MM/dd/yyyy", Locale.US),
                            SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    )

            for (format in formats) {
                try {
                    val date = format.parse(dateString)
                    if (date != null) {
                        return Timestamp(date)
                    }
                } catch (e: Exception) {
                    // Try next format
                }
            }

            // If all formats fail, return 7 days from now
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            return Timestamp(calendar.time)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date string: $dateString", e)
            // Default to 7 days from now
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            return Timestamp(calendar.time)
        }
    }
}
