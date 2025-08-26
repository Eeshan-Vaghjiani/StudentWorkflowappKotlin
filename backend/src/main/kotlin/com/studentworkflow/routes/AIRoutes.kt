
package com.studentworkflow.routes

import com.studentworkflow.services.AIService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureAIRoutes(aiService: AIService) {
    routing {
        route("/api/ai") {
            post("/process-task") {
                val request = call.receive<Map<String, String>>()
                val prompt = request["prompt"] ?: ""
                val userId = request["userId"]?.toIntOrNull() ?: 0
                val groupId = request["groupId"]?.toIntOrNull() ?: 0
                val result = aiService.processTaskPrompt(prompt, userId, groupId)
                call.respond(result)
            }
        }
    }
}
