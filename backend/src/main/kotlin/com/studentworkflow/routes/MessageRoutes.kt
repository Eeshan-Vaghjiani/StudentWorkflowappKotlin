package com.studentworkflow.routes

import com.studentworkflow.db.Messages
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

/**
 * MessageRoutes handles messaging functionality including sending,
 * receiving, and listing conversations.
 */
fun Application.configureMessageRoutes(
    notificationService: NotificationService
) {
    routing {
        authenticate("jwt") {
            route("/api/messages") {
                
                // Get messages for user (conversations)
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val conversationType = call.request.queryParameters["type"] // "direct" or "group"
                    val conversationId = call.request.queryParameters["conversationId"]?.toIntOrNull()

                    val messages = transaction {
                        var query = Messages.select { 
                            (Messages.senderId eq userId) or (Messages.receiverId eq userId) or 
                            (Messages.groupId.isNotNull() and (Messages.groupId eq conversationId))
                        }

                        if (conversationType == "direct" && conversationId != null) {
                            query = Messages.select {
                                ((Messages.senderId eq userId) and (Messages.receiverId eq conversationId)) or
                                ((Messages.senderId eq conversationId) and (Messages.receiverId eq userId))
                            }
                        } else if (conversationType == "group" && conversationId != null) {
                            query = Messages.select { Messages.groupId eq conversationId }
                        }

                        query.orderBy(Messages.createdAt, SortOrder.DESC)
                            .limit(50)
                            .map { row ->
                                Message(
                                    id = row[Messages.id].value,
                                    senderId = row[Messages.senderId],
                                    receiverId = row[Messages.receiverId],
                                    groupId = row[Messages.groupId],
                                    content = row[Messages.content],
                                    isRead = row[Messages.isRead],
                                    createdAt = row[Messages.createdAt].toString()
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "messages" to messages
                    ))
                }

                // Send message
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<SendMessageRequest>()

                    if (request.receiverId == null && request.groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Either receiverId or groupId must be provided"))
                        return@post
                    }

                    val messageId = transaction {
                        Messages.insert {
                            it[senderId] = userId
                            it[receiverId] = request.receiverId
                            it[groupId] = request.groupId
                            it[content] = request.content
                            it[isRead] = false
                            it[createdAt] = Instant.now()
                        } get Messages.id
                    }

                    // TODO: Send notifications
                    // In a real application, notifications would be sent here

                    call.respond(HttpStatusCode.Created, mapOf(
                        "success" to true,
                        "message" to "Message sent successfully",
                        "message_id" to messageId.value
                    ))
                }

                // Mark messages as read
                put("/read") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    val messageIds = call.receive<List<Int>>()

                    val success = transaction {
                        Messages.update({
                            (Messages.id inList messageIds) and (Messages.receiverId eq userId)
                        }) {
                            it[isRead] = true
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Messages marked as read"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to mark messages as read"
                        ))
                    }
                }

                // Get conversation list
                get("/conversations") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val conversations = transaction {
                        // Get latest message from each conversation
                        val directConversations = Messages.select { 
                            (Messages.senderId eq userId) or (Messages.receiverId eq userId)
                        }.groupBy { 
                            if (it[Messages.senderId] == userId) it[Messages.receiverId] else it[Messages.senderId]
                        }.mapNotNull { (otherUserId, messages) ->
                            if (otherUserId != null) {
                                val latestMessage = messages.maxByOrNull { it[Messages.createdAt] }
                                val otherUser = Users.select { Users.id eq otherUserId }.singleOrNull()
                                
                                latestMessage?.let { msg ->
                                    mapOf(
                                        "type" to "direct",
                                        "id" to otherUserId,
                                        "name" to (otherUser?.get(Users.name) ?: "Unknown"),
                                        "last_message" to msg[Messages.content],
                                        "last_message_time" to msg[Messages.createdAt].toString(),
                                        "unread_count" to messages.count { !it[Messages.isRead] && it[Messages.receiverId] == userId }
                                    )
                                }
                            } else null
                        }

                        // Get group conversations
                        val groupConversations = Messages.select { 
                            Messages.groupId.isNotNull()
                        }.groupBy { it[Messages.groupId] }.mapNotNull { (groupId, messages) ->
                            if (groupId != null) {
                                val latestMessage = messages.maxByOrNull { it[Messages.createdAt] }
                                val group = com.studentworkflow.db.StudyGroups.select { 
                                    com.studentworkflow.db.StudyGroups.id eq groupId 
                                }.singleOrNull()
                                
                                // Check if user is member of this group
                                val isMember = com.studentworkflow.db.GroupMemberships.select {
                                    (com.studentworkflow.db.GroupMemberships.groupId eq groupId) and
                                    (com.studentworkflow.db.GroupMemberships.userId eq userId)
                                }.singleOrNull() != null

                                if (isMember && latestMessage != null) {
                                    mapOf(
                                        "type" to "group",
                                        "id" to groupId,
                                        "name" to (group?.get(com.studentworkflow.db.StudyGroups.name) ?: "Unknown Group"),
                                        "last_message" to latestMessage[Messages.content],
                                        "last_message_time" to latestMessage[Messages.createdAt].toString(),
                                        "unread_count" to messages.count { 
                                            !it[Messages.isRead] && it[Messages.senderId] != userId 
                                        }
                                    )
                                } else null
                            } else null
                        }

                        directConversations + groupConversations
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "conversations" to conversations
                    ))
                }

                // Delete message
                delete("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val messageId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    if (messageId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid message ID"))
                        return@delete
                    }

                    // Check if user is sender of the message
                    val isSender = transaction {
                        Messages.select { 
                            (Messages.id eq messageId) and (Messages.senderId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!isSender) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only message sender can delete messages"))
                        return@delete
                    }

                    val success = transaction {
                        Messages.deleteWhere { Messages.id eq messageId } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Message deleted successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Message not found"
                        ))
                    }
                }
            }
        }
    }
}