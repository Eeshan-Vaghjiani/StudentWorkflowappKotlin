package com.studentworkflow.routes

import com.studentworkflow.db.Users
import com.studentworkflow.models.*
import com.studentworkflow.services.FileService
import com.studentworkflow.services.UserService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * UserRoutes handles user management endpoints including profile updates,
 * password changes, user settings, and file uploads.
 */
fun Application.configureUserRoutes(
    userService: UserService,
    fileService: FileService
) {
    routing {
        authenticate("jwt") {
            route("/api/users") {
                
                // Get current user profile
                get("/profile") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val user = transaction { userService.findUserById(userId) }
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }

                // Update user profile
                put("/profile") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    val request = call.receive<UpdateProfileRequest>()
                    
                    val success = transaction {
                        Users.update({ Users.id eq userId }) {
                            request.name?.let { it[Users.name] = request.name }
                            request.email?.let { it[Users.email] = request.email }
                        } > 0
                    }

                    if (success) {
                        val updatedUser = transaction { userService.findUserById(userId) }
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Profile updated successfully",
                            "user" to updatedUser
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to update profile"
                        ))
                    }
                }

                // Change password
                put("/password") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    val request = call.receive<ChangePasswordRequest>()
                    val user = transaction { userService.findUserById(userId) }
                    
                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                        return@put
                    }

                    // Verify current password
                    if (!userService.verifyPassword(user, request.currentPassword)) {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Current password is incorrect"
                        ))
                        return@put
                    }

                    // Update password
                    val success = userService.updatePassword(userId, request.newPassword)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Password updated successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to update password"
                        ))
                    }
                }

                // Get user settings
                get("/settings") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val settings = transaction {
                        com.studentworkflow.db.UserSettings.select { 
                            com.studentworkflow.db.UserSettings.userId eq userId 
                        }.singleOrNull()?.let { row ->
                            UserSetting(
                                userId = row[com.studentworkflow.db.UserSettings.userId],
                                workDurationMinutes = row[com.studentworkflow.db.UserSettings.workDurationMinutes],
                                shortBreakMinutes = row[com.studentworkflow.db.UserSettings.shortBreakMinutes],
                                longBreakMinutes = row[com.studentworkflow.db.UserSettings.longBreakMinutes],
                                intervalsBeforeLongBreak = row[com.studentworkflow.db.UserSettings.intervalsBeforeLongBreak],
                                notificationsEnabled = row[com.studentworkflow.db.UserSettings.notificationsEnabled]
                            )
                        }
                    }

                    if (settings != null) {
                        call.respond(HttpStatusCode.OK, settings)
                    } else {
                        // Return default settings if none exist
                        val defaultSettings = UserSetting(
                            userId = userId,
                            workDurationMinutes = 25,
                            shortBreakMinutes = 5,
                            longBreakMinutes = 15,
                            intervalsBeforeLongBreak = 4,
                            notificationsEnabled = true
                        )
                        call.respond(HttpStatusCode.OK, defaultSettings)
                    }
                }

                // Update user settings
                put("/settings") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    val request = call.receive<UpdateUserSettingsRequest>()
                    
                    val success = transaction {
                        val existingSettings = com.studentworkflow.db.UserSettings.select { 
                            com.studentworkflow.db.UserSettings.userId eq userId 
                        }.singleOrNull()

                        if (existingSettings != null) {
                            // Update existing settings
                            com.studentworkflow.db.UserSettings.update({ 
                                com.studentworkflow.db.UserSettings.userId eq userId 
                            }) {
                                request.workDurationMinutes?.let { value -> 
                                    it[com.studentworkflow.db.UserSettings.workDurationMinutes] = value 
                                }
                                request.shortBreakMinutes?.let { value -> 
                                    it[com.studentworkflow.db.UserSettings.shortBreakMinutes] = value 
                                }
                                request.longBreakMinutes?.let { value -> 
                                    it[com.studentworkflow.db.UserSettings.longBreakMinutes] = value 
                                }
                                request.intervalsBeforeLongBreak?.let { value -> 
                                    it[com.studentworkflow.db.UserSettings.intervalsBeforeLongBreak] = value 
                                }
                                request.notificationsEnabled?.let { value -> 
                                    it[com.studentworkflow.db.UserSettings.notificationsEnabled] = value 
                                }
                            } > 0
                        } else {
                            // Insert new settings
                            com.studentworkflow.db.UserSettings.insert {
                                it[com.studentworkflow.db.UserSettings.userId] = userId
                                it[workDurationMinutes] = request.workDurationMinutes ?: 25
                                it[shortBreakMinutes] = request.shortBreakMinutes ?: 5
                                it[longBreakMinutes] = request.longBreakMinutes ?: 15
                                it[intervalsBeforeLongBreak] = request.intervalsBeforeLongBreak ?: 4
                                it[notificationsEnabled] = request.notificationsEnabled ?: true
                            }
                            true
                        }
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Settings updated successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to update settings"
                        ))
                    }
                }

                // Upload profile picture or files
                post("/upload") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val multipartData = call.receiveMultipart()
                    var uploadResult: FileUploadResponse? = null

                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                val fileName = part.originalFileName ?: "unknown"
                                val inputStream = part.streamProvider()
                                uploadResult = fileService.storeFile(
                                    inputStream = inputStream,
                                    originalFileName = fileName,
                                    path = "profiles",
                                    userId = userId
                                )
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    if (uploadResult != null) {
                        call.respond(HttpStatusCode.OK, uploadResult!!)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, FileUploadResponse(
                            success = false,
                            message = "No file provided"
                        ))
                    }
                }

                // Get user's files
                get("/files") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val files = fileService.getFiles("user_$userId")
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "files" to files
                    ))
                }

                // Delete a file
                delete("/files/{path...}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    val filePath = call.parameters.getAll("path")?.joinToString("/") ?: ""
                    
                    // Ensure user can only delete their own files
                    if (!filePath.startsWith("user_$userId/")) {
                        call.respond(HttpStatusCode.Forbidden, mapOf(
                            "success" to false,
                            "message" to "Access denied"
                        ))
                        return@delete
                    }

                    val success = fileService.deleteFile(filePath)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "File deleted successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "File not found"
                        ))
                    }
                }

                // Get storage usage
                get("/storage-usage") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val usage = fileService.getUserStorageUsage(userId)
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "usage_bytes" to usage,
                        "usage_mb" to String.format("%.2f", usage / (1024.0 * 1024.0))
                    ))
                }

                // Get user statistics
                get("/stats") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val stats = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        if (user != null) {
                            mapOf(
                                "ai_prompts_remaining" to user[Users.aiPromptsRemaining],
                                "total_prompts_purchased" to user[Users.totalPromptsPurchased],
                                "is_paid_user" to user[Users.isPaidUser],
                                "last_payment_date" to user[Users.lastPaymentDate]?.toString(),
                                "two_factor_enabled" to (user[Users.twoFactorSecret] != null)
                            )
                        } else null
                    }

                    if (stats != null) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "stats" to stats
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "User not found"
                        ))
                    }
                }
            }
        }
    }
}