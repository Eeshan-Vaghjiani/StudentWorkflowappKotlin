package com.studentworkflow.routes

import com.studentworkflow.db.Tasks
import com.studentworkflow.db.Users
import com.studentworkflow.models.*
import com.studentworkflow.services.NotificationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * TaskRoutes handles task management endpoints including CRUD operations
 * for tasks, assignments, and task-related functionality.
 */
fun Application.configureTaskRoutes(
    notificationService: NotificationService
) {
    routing {
        authenticate("jwt") {
            route("/api/tasks") {
                
                // Get all tasks for user (with optional filtering)
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val status = call.request.queryParameters["status"]
                    val priority = call.request.queryParameters["priority"]
                    val groupId = call.request.queryParameters["groupId"]?.toIntOrNull()
                    val assignedToMe = call.request.queryParameters["assignedToMe"]?.toBoolean() ?: false
                    val createdByMe = call.request.queryParameters["createdByMe"]?.toBoolean() ?: false

                    val tasks = transaction {
                        var query = Tasks.selectAll()

                        // Apply filters
                        val conditions = mutableListOf<Op<Boolean>>()
                        
                        if (assignedToMe) {
                            conditions.add(Tasks.assigneeId eq userId)
                        }
                        if (createdByMe) {
                            conditions.add(Tasks.creatorId eq userId)
                        }
                        if (!assignedToMe && !createdByMe) {
                            // Show tasks either created by or assigned to the user
                            conditions.add((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId))
                        }
                        
                        status?.let { conditions.add(Tasks.status eq it) }
                        priority?.let { conditions.add(Tasks.priority eq it) }
                        groupId?.let { conditions.add(Tasks.groupId eq it) }

                        if (conditions.isNotEmpty()) {
                            query = query.where { conditions.reduce { acc, condition -> acc and condition } }
                        }

                        query.orderBy(Tasks.createdAt, SortOrder.DESC)
                            .map { row ->
                                Task(
                                    id = row[Tasks.id].value,
                                    title = row[Tasks.title],
                                    description = row[Tasks.description],
                                    dueDate = row[Tasks.dueDate]?.toString(),
                                    status = row[Tasks.status],
                                    priority = row[Tasks.priority],
                                    creatorId = row[Tasks.creatorId],
                                    assigneeId = row[Tasks.assigneeId],
                                    groupId = row[Tasks.groupId],
                                    createdAt = row[Tasks.createdAt].toString()
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "tasks" to tasks
                    ))
                }

                // Get specific task by ID
                get("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    if (taskId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task ID"))
                        return@get
                    }

                    val task = transaction {
                        Tasks.select { 
                            (Tasks.id eq taskId) and 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId))
                        }.singleOrNull()?.let { row ->
                            Task(
                                id = row[Tasks.id].value,
                                title = row[Tasks.title],
                                description = row[Tasks.description],
                                dueDate = row[Tasks.dueDate]?.toString(),
                                status = row[Tasks.status],
                                priority = row[Tasks.priority],
                                creatorId = row[Tasks.creatorId],
                                assigneeId = row[Tasks.assigneeId],
                                groupId = row[Tasks.groupId],
                                createdAt = row[Tasks.createdAt].toString()
                            )
                        }
                    }

                    if (task != null) {
                        call.respond(HttpStatusCode.OK, task)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
                    }
                }

                // Create new task
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<CreateTaskRequest>()

                    val taskId = transaction {
                        Tasks.insert {
                            it[title] = request.title
                            it[description] = request.description
                            it[dueDate] = request.dueDate?.let { dateStr ->
                                try {
                                    Instant.parse(dateStr)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            it[priority] = request.priority
                            it[creatorId] = userId
                            it[assigneeId] = request.assigneeId
                            it[groupId] = request.groupId
                            it[status] = "pending"
                            it[createdAt] = Instant.now()
                        } get Tasks.id
                    }

                    // TODO: Send notification to assignee if different from creator
                    // This would be implemented with proper async handling in a real application

                    call.respond(HttpStatusCode.Created, mapOf(
                        "success" to true,
                        "message" to "Task created successfully",
                        "task_id" to taskId.value
                    ))
                }

                // Update task
                put("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    if (taskId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task ID"))
                        return@put
                    }

                    val request = call.receive<UpdateTaskRequest>()

                    // Check if user has permission to update task
                    val hasPermission = transaction {
                        Tasks.select { 
                            (Tasks.id eq taskId) and 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId))
                        }.singleOrNull() != null
                    }

                    if (!hasPermission) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@put
                    }

                    val success = transaction {
                        Tasks.update({ Tasks.id eq taskId }) {
                            request.title?.let { it[Tasks.title] = request.title }
                            request.description?.let { it[Tasks.description] = request.description }
                            request.dueDate?.let { dateStr ->
                                it[Tasks.dueDate] = try {
                                    Instant.parse(dateStr)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            request.status?.let { it[Tasks.status] = request.status }
                            request.priority?.let { it[Tasks.priority] = request.priority }
                            request.assigneeId?.let { it[Tasks.assigneeId] = request.assigneeId }
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Task updated successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to update task"
                        ))
                    }
                }

                // Delete task
                delete("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    if (taskId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task ID"))
                        return@delete
                    }

                    // Check if user is creator of the task
                    val isCreator = transaction {
                        Tasks.select { 
                            (Tasks.id eq taskId) and (Tasks.creatorId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!isCreator) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only task creator can delete tasks"))
                        return@delete
                    }

                    val success = transaction {
                        Tasks.deleteWhere { Tasks.id eq taskId } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Task deleted successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Task not found"
                        ))
                    }
                }

                // Assign task to user
                post("/{id}/assign") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    if (taskId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task ID"))
                        return@post
                    }

                    val request = call.receive<TaskAssignment>()

                    // Check if user has permission to assign task
                    val hasPermission = transaction {
                        Tasks.select { 
                            (Tasks.id eq taskId) and (Tasks.creatorId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!hasPermission) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only task creator can assign tasks"))
                        return@post
                    }

                    val success = transaction {
                        Tasks.update({ Tasks.id eq taskId }) {
                            it[assigneeId] = request.assigned_user_id
                        } > 0
                    }

                    if (success) {
                        // TODO: Send notification to assignee
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Task assigned successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to assign task"
                        ))
                    }
                }

                // Complete task
                post("/{id}/complete") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    if (taskId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task ID"))
                        return@post
                    }

                    // Check if user is assignee of the task
                    val isAssignee = transaction {
                        Tasks.select { 
                            (Tasks.id eq taskId) and (Tasks.assigneeId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!isAssignee) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only task assignee can complete tasks"))
                        return@post
                    }

                    val success = transaction {
                        Tasks.update({ Tasks.id eq taskId }) {
                            it[status] = "completed"
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Task completed successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to complete task"
                        ))
                    }
                }

                // Get task statistics for user
                get("/stats") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val stats = transaction {
                        val totalTasks = Tasks.select { 
                            (Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId)
                        }.count()

                        val completedTasks = Tasks.select { 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId)) and 
                            (Tasks.status eq "completed")
                        }.count()

                        val pendingTasks = Tasks.select { 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId)) and 
                            (Tasks.status eq "pending")
                        }.count()

                        val inProgressTasks = Tasks.select { 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId)) and 
                            (Tasks.status eq "in_progress")
                        }.count()

                        val overdueTasks = Tasks.select { 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId)) and 
                            (Tasks.dueDate.isNotNull()) and 
                            (Tasks.dueDate less Instant.now()) and 
                            (Tasks.status neq "completed")
                        }.count()

                        mapOf(
                            "total" to totalTasks,
                            "completed" to completedTasks,
                            "pending" to pendingTasks,
                            "in_progress" to inProgressTasks,
                            "overdue" to overdueTasks
                        )
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "stats" to stats
                    ))
                }

                // Get due tasks (upcoming deadlines)
                get("/due") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 7
                    val dueDate = Instant.now().plusSeconds(days * 24 * 60 * 60L)

                    val dueTasks = transaction {
                        Tasks.select { 
                            ((Tasks.creatorId eq userId) or (Tasks.assigneeId eq userId)) and 
                            (Tasks.dueDate.isNotNull()) and 
                            (Tasks.dueDate lessEq dueDate) and 
                            (Tasks.dueDate greater Instant.now()) and 
                            (Tasks.status neq "completed")
                        }.orderBy(Tasks.dueDate, SortOrder.ASC)
                        .map { row ->
                            Task(
                                id = row[Tasks.id].value,
                                title = row[Tasks.title],
                                description = row[Tasks.description],
                                dueDate = row[Tasks.dueDate]?.toString(),
                                status = row[Tasks.status],
                                priority = row[Tasks.priority],
                                creatorId = row[Tasks.creatorId],
                                assigneeId = row[Tasks.assigneeId],
                                groupId = row[Tasks.groupId],
                                createdAt = row[Tasks.createdAt].toString()
                            )
                        }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "tasks" to dueTasks
                    ))
                }
            }
        }
    }
}