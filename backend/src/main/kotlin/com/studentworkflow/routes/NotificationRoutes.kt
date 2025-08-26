package com.studentworkflow.routes

import com.studentworkflow.models.NotificationMarkReadRequest
import com.studentworkflow.services.NotificationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * NotificationRoutes handles notification management endpoints
 * including retrieving, marking as read, and deleting notifications.
 */
fun Application.configureNotificationRoutes(
    notificationService: NotificationService
) {
    routing {
        authenticate("jwt") {
            route("/api/notifications") {
                
                // Get all notifications for user
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                    val notifications = notificationService.getNotifications(userId, limit, offset)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "notifications" to notifications
                    ))
                }

                // Get unread notification count
                get("/unread-count") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val count = notificationService.getUnreadCount(userId)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "unread_count" to count
                    ))
                }

                // Mark notifications as read
                put("/mark-read") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    val request = call.receive<NotificationMarkReadRequest>()
                    val success = notificationService.markAsRead(request.notificationIds, userId)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Notifications marked as read"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to mark notifications as read"
                        ))
                    }
                }

                // Mark all notifications as read
                put("/mark-all-read") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    val success = notificationService.markAllAsRead(userId)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "All notifications marked as read"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to mark notifications as read"
                        ))
                    }
                }

                // Delete notification
                delete("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val notificationId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    if (notificationId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid notification ID"))
                        return@delete
                    }

                    val success = notificationService.deleteNotification(notificationId, userId)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Notification deleted successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Notification not found"
                        ))
                    }
                }
            }
        }
    }
}