package com.studentworkflow.routes

import com.studentworkflow.db.PomodoroSessions
import com.studentworkflow.db.StudySessions
import com.studentworkflow.models.*
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

/**
 * StudySessionRoutes handles study session and Pomodoro session management
 * including creating, joining, and managing sessions.
 */
fun Application.configureStudySessionRoutes() {
    routing {
        authenticate("jwt") {
            route("/api/sessions") {
                
                // Get all study sessions for user
                get("/study") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val sessions = transaction {
                        StudySessions.select { StudySessions.userId eq userId }
                            .orderBy(StudySessions.createdAt, SortOrder.DESC)
                            .map { row ->
                                StudySession(
                                    id = row[StudySessions.id].value,
                                    userId = row[StudySessions.userId],
                                    title = row[StudySessions.title],
                                    startTime = row[StudySessions.startTime].toString(),
                                    endTime = row[StudySessions.endTime]?.toString(),
                                    isCompleted = row[StudySessions.isCompleted],
                                    createdAt = row[StudySessions.createdAt].toString()
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "sessions" to sessions
                    ))
                }

                // Create new study session
                post("/study") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<CreateStudySessionRequest>()

                    val sessionId = transaction {
                        StudySessions.insert {
                            it[StudySessions.userId] = userId
                            it[title] = request.title
                            it[startTime] = try {
                                Instant.parse(request.startTime)
                            } catch (e: Exception) {
                                Instant.now()
                            }
                            it[endTime] = null
                            it[isCompleted] = false
                            it[createdAt] = Instant.now()
                        } get StudySessions.id
                    }

                    call.respond(HttpStatusCode.Created, mapOf(
                        "success" to true,
                        "message" to "Study session created successfully",
                        "session_id" to sessionId.value
                    ))
                }

                // End study session
                put("/study/{id}/end") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val sessionId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    if (sessionId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid session ID"))
                        return@put
                    }

                    val success = transaction {
                        StudySessions.update({
                            (StudySessions.id eq sessionId) and (StudySessions.userId eq userId)
                        }) {
                            it[endTime] = Instant.now()
                            it[isCompleted] = true
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Study session ended successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Session not found"
                        ))
                    }
                }

                // Get Pomodoro sessions
                get("/pomodoro") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val taskId = call.request.queryParameters["taskId"]?.toIntOrNull()

                    val sessions = transaction {
                        var query = PomodoroSessions.select { PomodoroSessions.userId eq userId }
                        
                        taskId?.let {
                            query = query.andWhere { PomodoroSessions.taskId eq taskId }
                        }

                        query.orderBy(PomodoroSessions.startTime, SortOrder.DESC)
                            .map { row ->
                                PomodoroSession(
                                    id = row[PomodoroSessions.id].value,
                                    userId = row[PomodoroSessions.userId],
                                    taskId = row[PomodoroSessions.taskId],
                                    sessionType = row[PomodoroSessions.sessionType],
                                    startTime = row[PomodoroSessions.startTime].toString(),
                                    durationMinutes = row[PomodoroSessions.durationMinutes],
                                    isCompleted = row[PomodoroSessions.isCompleted]
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "sessions" to sessions
                    ))
                }

                // Create Pomodoro session
                post("/pomodoro") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<CreatePomodoroSessionRequest>()

                    val sessionId = transaction {
                        PomodoroSessions.insert {
                            it[PomodoroSessions.userId] = userId
                            it[taskId] = request.taskId
                            it[sessionType] = request.sessionType
                            it[startTime] = Instant.now()
                            it[durationMinutes] = request.durationMinutes
                            it[isCompleted] = false
                        } get PomodoroSessions.id
                    }

                    call.respond(HttpStatusCode.Created, mapOf(
                        "success" to true,
                        "message" to "Pomodoro session started successfully",
                        "session_id" to sessionId.value
                    ))
                }

                // Complete Pomodoro session
                put("/pomodoro/{id}/complete") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val sessionId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    if (sessionId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid session ID"))
                        return@put
                    }

                    val success = transaction {
                        PomodoroSessions.update({
                            (PomodoroSessions.id eq sessionId) and (PomodoroSessions.userId eq userId)
                        }) {
                            it[isCompleted] = true
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Pomodoro session completed successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Session not found"
                        ))
                    }
                }

                // Get session statistics
                get("/stats") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val stats = transaction {
                        val totalStudySessions = StudySessions.select { StudySessions.userId eq userId }.count()
                        val completedStudySessions = StudySessions.select { 
                            (StudySessions.userId eq userId) and (StudySessions.isCompleted eq true)
                        }.count()

                        val totalPomodoroSessions = PomodoroSessions.select { PomodoroSessions.userId eq userId }.count()
                        val completedPomodoroSessions = PomodoroSessions.select { 
                            (PomodoroSessions.userId eq userId) and (PomodoroSessions.isCompleted eq true)
                        }.count()

                        val totalFocusTime = PomodoroSessions.select { 
                            (PomodoroSessions.userId eq userId) and 
                            (PomodoroSessions.isCompleted eq true) and
                            (PomodoroSessions.sessionType eq "work")
                        }.sumOf { it[PomodoroSessions.durationMinutes] }

                        mapOf(
                            "total_study_sessions" to totalStudySessions,
                            "completed_study_sessions" to completedStudySessions,
                            "total_pomodoro_sessions" to totalPomodoroSessions,
                            "completed_pomodoro_sessions" to completedPomodoroSessions,
                            "total_focus_time_minutes" to totalFocusTime
                        )
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "stats" to stats
                    ))
                }
            }
        }
    }
}