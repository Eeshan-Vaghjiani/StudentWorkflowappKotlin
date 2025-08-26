
package com.studentworkflow.services

import com.studentworkflow.db.GroupMemberships
import com.studentworkflow.db.Users
import com.studentworkflow.models.*
import io.ktor.client.* 
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.* 
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class AIService(private val promptService: PromptService) {

    private val apiKey: String = System.getenv("OPENROUTER_API_KEY") ?: ""
    private val baseUrl = "https://openrouter.ai/api/v1"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val availableModels = listOf(
        "qwen/qwen3-4b:free",
        "mistralai/mistral-7b-instruct",
        "openai/opus",
        "meta-llama/llama-3-8b-instruct",
        "anthropic/claude-3-haiku"
    )
    private var lastWorkingModel: String? = null

    suspend fun getWorkingModel(): String {
        if (lastWorkingModel != null) {
            return lastWorkingModel as String
        }

        try {
            val response: HttpResponse = client.get("$baseUrl/models") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }

            if (response.status.isSuccess()) {
                val modelsResponse = response.body<OpenRouterModelsResponse>()
                val availableModelIds = modelsResponse.data.map { it.id }

                for (modelId in availableModels) {
                    if (modelId in availableModelIds) {
                        lastWorkingModel = modelId
                        return modelId
                    }
                }
            }
        } catch (e: Exception) {
            // Log the error
        }

        // Fallback to a default model
        val fallbackModel = availableModels[0]
        lastWorkingModel = fallbackModel
        return fallbackModel
    }

    suspend fun processTaskPrompt(prompt: String, userId: Int, groupId: Int): Map<String, Any> {
        if (!promptService.hasPromptsRemaining(userId)) {
            return mapOf(
                "success" to false,
                "error" to "You have no AI prompts remaining. Please purchase more to continue using AI services.",
                "redirect_to_pricing" to true
            )
        }

        val groupMembers = transaction {
            GroupMemberships.innerJoin(Users)
                .select { GroupMemberships.groupId eq groupId }
                .map { it[Users.name] }
        }

        val systemMessage = """
            You are a helpful assistant specialized in creating structured task assignments. Your ONLY job is to return valid, parseable JSON.
            When the user describes assignment tasks they want to create, extract the following information:
            1. The title of the assignment
            2. The unit name for the assignment (course code or subject name)
            3. The description of the assignment
            4. The individual tasks within the assignment
            
            The following group members are available to be assigned tasks. Please distribute tasks evenly among them:
            ${groupMembers.joinToString(", ")}
            
            For each task, specify:
            - Title (short but descriptive)
            - Description (more details)
            - Who it is assigned to (choose from the available group members)
            - Start date (relative to now, must not be in the past)
            - End date (absolute date or relative to start date, must be between start date and assignment due date)
            - Priority (low, medium, or high)
            - Effort hours (estimated time needed to complete the task, from 1-20 hours)
            - Importance (scale of 1-5, where 5 is most important)

            CRITICAL: Your entire response must ONLY be valid JSON with no additional text before or after. DO NOT include markdown backticks, explanations, or any other text.

            IMPORTANT DATE RULES:
            - Today's date is ${LocalDate.now()}
            - All task start dates must be today or in the future, never in the past
            - All task end dates must be before or on the assignment due date
            - For simple tasks (1-3 effort hours), end dates should be within 1-3 days of start date
            - For medium tasks (4-8 effort hours), end dates should be within 3-7 days of start date
            - For complex tasks (9-20 effort hours), end dates should be within 7-14 days of start date

            The JSON structure must be:
            {
                "assignment": {
                    "title": "Assignment title",
                    "unit_name": "Unit or course name",
                    "description": "Assignment description",
                    "due_date": "YYYY-MM-DD"
                },
                "tasks": [
                    {
                        "title": "Task 1 title",
                        "description": "Task 1 description",
                        "assigned_to_name": "Person's Name",
                        "start_date": "YYYY-MM-DD",
                        "end_date": "YYYY-MM-DD",
                        "priority": "medium",
                        "effort_hours": 3,
                        "importance": 4
                    }
                ]
            }

            When determining effort hours and importance, consider:
            - Effort hours: Realistic estimate of how many hours it would take to complete the task (1-20)
              - Simple tasks: 1-3 hours
              - Medium tasks: 4-8 hours
              - Complex tasks: 9-20 hours
            - Importance: How critical the task is to the overall assignment success
              - 5: Critical path task, project cannot succeed without it
              - 4: Very important task with major impact
              - 3: Standard task with moderate impact
              - 2: Supporting task with minor impact
              - 1: Nice-to-have task with minimal impact
            - When assigning tasks, consider:
              1. Balancing workload by considering both effort AND importance
              2. Assigning related tasks to the same person when logical
              3. Each member should have a mix of high and low importance tasks

            Always use the YYYY-MM-DD format for dates. Your ENTIRE response must be parseable as JSON.
        """.trimIndent()

        val modelToUse = getWorkingModel()

        val request = OpenAIChatRequest(
            model = modelToUse,
            messages = listOf(
                OpenAIChatMessage("system", systemMessage),
                OpenAIChatMessage("user", prompt)
            )
        )

        return try {
            val response: HttpResponse = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val chatResponse = response.body<OpenAIChatResponse>()
                val content = chatResponse.choices.first().message.content
                val aiResponse = Json.decodeFromString<AIResponse>(content)
                promptService.usePrompt(userId, "task_creation")
                mapOf(
                    "success" to true,
                    "assignment" to aiResponse.assignment,
                    "tasks" to aiResponse.tasks,
                    "model_used" to modelToUse
                )
            } else {
                mapOf(
                    "success" to false,
                    "error" to "API call failed: ${response.status} ${response.bodyAsText()}"
                )
            }
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to "An error occurred while processing your request: ${e.message}"
            )
        }
    }

    suspend fun distributeTasks(tasks: List<TaskInfo>, groupMembers: List<GroupMemberInfo>): List<TaskAssignment> {
        if (tasks.isEmpty() || groupMembers.isEmpty()) {
            return emptyList()
        }

        val systemMessage = """
            You are a task distribution AI. Your job is to optimally distribute tasks among team members based on effort hours and importance.

            IMPORTANT: You MUST return a valid JSON array of task assignments that ONLY contains the task ID and the assigned user ID. Nothing else.

            Distribute tasks evenly considering:
            1. Balance total effort hours among members
            2. Balance total importance points among members
            3. Consider both factors together (effort * importance) for fairness
            4. Return ONLY the task IDs and assigned user IDs

            The response format must be a JSON array like this:
            [
                {"id": 1, "assigned_user_id": 505},
                {"id": 2, "assigned_user_id": 506},
                {"id": 3, "assigned_user_id": 505}
            ]
        """.trimIndent()

        val userMessage = Json.encodeToString(mapOf("tasks" to tasks, "members" to groupMembers))

        val modelToUse = getWorkingModel()

        val request = OpenAIChatRequest(
            model = modelToUse,
            messages = listOf(
                OpenAIChatMessage("system", systemMessage),
                OpenAIChatMessage("user", userMessage)
            ),
            temperature = 0.1
        )

        return try {
            val response: HttpResponse = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val chatResponse = response.body<OpenAIChatResponse>()
                val content = chatResponse.choices.first().message.content
                Json.decodeFromString<List<TaskAssignment>>(content)
            } else {
                fallbackDistributeTasks(tasks, groupMembers)
            }
        } catch (e: Exception) {
            fallbackDistributeTasks(tasks, groupMembers)
        }
    }

    private fun fallbackDistributeTasks(tasks: List<TaskInfo>, groupMembers: List<GroupMemberInfo>): List<TaskAssignment> {
        val sortedTasks = tasks.sortedByDescending { (it.effort_hours) * (it.importance) }
        val memberWorkloads = groupMembers.associate { it.id to 0 }.toMutableMap()
        val assignments = mutableListOf<TaskAssignment>()

        for (task in sortedTasks) {
            val minMemberId = memberWorkloads.minByOrNull { it.value }?.key
            if (minMemberId != null) {
                assignments.add(TaskAssignment(task.id, minMemberId))
                memberWorkloads[minMemberId] = memberWorkloads.getOrDefault(minMemberId, 0) + (task.effort_hours * task.importance)
            }
        }
        return assignments
    }

    suspend fun testConnection(): Map<String, Any> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/models") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            mapOf(
                "success" to response.status.isSuccess(),
                "response" to response.bodyAsText()
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }
}
