package com.studentworkflow.routes

import com.studentworkflow.db.GroupMemberships
import com.studentworkflow.db.StudyGroups
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
 * StudyGroupRoutes handles study group management endpoints including
 * group creation, membership management, and invitations.
 */
fun Application.configureStudyGroupRoutes(
    notificationService: NotificationService
) {
    routing {
        authenticate("jwt") {
            route("/api/groups") {
                
                // Get all groups for user
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val groups = transaction {
                        (StudyGroups innerJoin GroupMemberships)
                            .select { GroupMemberships.userId eq userId }
                            .map { row ->
                                StudyGroup(
                                    id = row[StudyGroups.id].value,
                                    name = row[StudyGroups.name],
                                    description = row[StudyGroups.description],
                                    ownerId = row[StudyGroups.ownerId],
                                    createdAt = row[StudyGroups.createdAt].toString()
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "groups" to groups
                    ))
                }

                // Get specific group by ID
                get("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid group ID"))
                        return@get
                    }

                    // Check if user is member of the group
                    val isMember = transaction {
                        GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!isMember) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@get
                    }

                    val group = transaction {
                        StudyGroups.select { StudyGroups.id eq groupId }
                            .singleOrNull()?.let { row ->
                                StudyGroup(
                                    id = row[StudyGroups.id].value,
                                    name = row[StudyGroups.name],
                                    description = row[StudyGroups.description],
                                    ownerId = row[StudyGroups.ownerId],
                                    createdAt = row[StudyGroups.createdAt].toString()
                                )
                            }
                    }

                    if (group != null) {
                        call.respond(HttpStatusCode.OK, group)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Group not found"))
                    }
                }

                // Create new group
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<CreateGroupRequest>()

                    val groupId = transaction {
                        // Create group
                        val id = StudyGroups.insert {
                            it[name] = request.name
                            it[description] = request.description
                            it[ownerId] = userId
                            it[createdAt] = Instant.now()
                        } get StudyGroups.id

                        // Add creator as member with admin role
                        GroupMemberships.insert {
                            it[GroupMemberships.groupId] = id.value
                            it[GroupMemberships.userId] = userId
                            it[role] = "admin"
                            it[joinedAt] = Instant.now()
                        }

                        id.value
                    }

                    call.respond(HttpStatusCode.Created, mapOf(
                        "success" to true,
                        "message" to "Group created successfully",
                        "group_id" to groupId
                    ))
                }

                // Update group
                put("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid group ID"))
                        return@put
                    }

                    val request = call.receive<UpdateGroupRequest>()

                    // Check if user is owner or admin of the group
                    val hasPermission = transaction {
                        val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
                        val membership = GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq userId)
                        }.singleOrNull()

                        group?.let { it[StudyGroups.ownerId] == userId } == true ||
                        membership?.let { it[GroupMemberships.role] == "admin" } == true
                    }

                    if (!hasPermission) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@put
                    }

                    val success = transaction {
                        StudyGroups.update({ StudyGroups.id eq groupId }) {
                            request.name?.let { it[StudyGroups.name] = request.name }
                            request.description?.let { it[StudyGroups.description] = request.description }
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Group updated successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to update group"
                        ))
                    }
                }

                // Delete group
                delete("/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid group ID"))
                        return@delete
                    }

                    // Check if user is owner of the group
                    val isOwner = transaction {
                        StudyGroups.select { 
                            (StudyGroups.id eq groupId) and (StudyGroups.ownerId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!isOwner) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only group owner can delete groups"))
                        return@delete
                    }

                    val success = transaction {
                        // Delete memberships first
                        GroupMemberships.deleteWhere { GroupMemberships.groupId eq groupId }
                        // Delete group
                        StudyGroups.deleteWhere { StudyGroups.id eq groupId } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Group deleted successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Group not found"
                        ))
                    }
                }

                // Get group members
                get("/{id}/members") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid group ID"))
                        return@get
                    }

                    // Check if user is member of the group
                    val isMember = transaction {
                        GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq userId)
                        }.singleOrNull() != null
                    }

                    if (!isMember) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@get
                    }

                    val members = transaction {
                        (GroupMemberships innerJoin Users)
                            .select { GroupMemberships.groupId eq groupId }
                            .map { row ->
                                mapOf(
                                    "user_id" to row[Users.id],
                                    "name" to row[Users.name],
                                    "email" to row[Users.email],
                                    "role" to row[GroupMemberships.role],
                                    "joined_at" to row[GroupMemberships.joinedAt].toString()
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "members" to members
                    ))
                }

                // Invite user to group
                post("/{id}/invite") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid group ID"))
                        return@post
                    }

                    val request = call.receive<InviteToGroupRequest>()

                    // Check if user has permission to invite
                    val hasPermission = transaction {
                        val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
                        val membership = GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq userId)
                        }.singleOrNull()

                        group?.let { it[StudyGroups.ownerId] == userId } == true ||
                        membership?.let { it[GroupMemberships.role] in listOf("admin", "moderator") } == true
                    }

                    if (!hasPermission) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@post
                    }

                    // Find user by email
                    val invitedUser = transaction {
                        Users.select { Users.email eq request.userEmail }.singleOrNull()
                    }

                    if (invitedUser == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "User not found"
                        ))
                        return@post
                    }

                    val invitedUserId = invitedUser[Users.id]

                    // Check if user is already a member
                    val isAlreadyMember = transaction {
                        GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq invitedUserId)
                        }.singleOrNull() != null
                    }

                    if (isAlreadyMember) {
                        call.respond(HttpStatusCode.Conflict, mapOf(
                            "success" to false,
                            "message" to "User is already a member of this group"
                        ))
                        return@post
                    }

                    // Add user to group
                    transaction {
                        GroupMemberships.insert {
                            it[GroupMemberships.groupId] = groupId
                            it[GroupMemberships.userId] = invitedUserId
                            it[role] = request.role
                            it[joinedAt] = Instant.now()
                        }
                    }

                    // TODO: Send notification
                    // notificationService.createGroupInvitation(invitedUserId, groupId, userId)

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "User invited successfully"
                    ))
                }

                // Remove member from group
                delete("/{id}/members/{userId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    val targetUserId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (currentUserId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    if (groupId == null || targetUserId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid parameters"))
                        return@delete
                    }

                    // Check permissions
                    val hasPermission = transaction {
                        val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
                        val currentUserMembership = GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq currentUserId)
                        }.singleOrNull()

                        // Allow if user is owner, admin, or removing themselves
                        group?.let { it[StudyGroups.ownerId] == currentUserId } == true ||
                        currentUserMembership?.let { it[GroupMemberships.role] == "admin" } == true ||
                        currentUserId == targetUserId
                    }

                    if (!hasPermission) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@delete
                    }

                    val success = transaction {
                        GroupMemberships.deleteWhere {
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq targetUserId)
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Member removed successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Member not found"
                        ))
                    }
                }

                // Update member role
                put("/{id}/members/{userId}/role") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    val targetUserId = call.parameters["userId"]?.toIntOrNull()
                    
                    if (currentUserId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    if (groupId == null || targetUserId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid parameters"))
                        return@put
                    }

                    val newRole = call.receive<Map<String, String>>()["role"]

                    if (newRole == null || newRole !in listOf("member", "moderator", "admin")) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid role"))
                        return@put
                    }

                    // Check permissions (only owner or admin can change roles)
                    val hasPermission = transaction {
                        val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
                        val currentUserMembership = GroupMemberships.select { 
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq currentUserId)
                        }.singleOrNull()

                        group?.let { it[StudyGroups.ownerId] == currentUserId } == true ||
                        currentUserMembership?.let { it[GroupMemberships.role] == "admin" } == true
                    }

                    if (!hasPermission) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                        return@put
                    }

                    val success = transaction {
                        GroupMemberships.update({
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq targetUserId)
                        }) {
                            it[role] = newRole
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Role updated successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Member not found"
                        ))
                    }
                }

                // Leave group
                post("/{id}/leave") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val groupId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    if (groupId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid group ID"))
                        return@post
                    }

                    // Check if user is owner (can't leave own group)
                    val isOwner = transaction {
                        StudyGroups.select { 
                            (StudyGroups.id eq groupId) and (StudyGroups.ownerId eq userId)
                        }.singleOrNull() != null
                    }

                    if (isOwner) {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Group owner cannot leave the group. Transfer ownership first."
                        ))
                        return@post
                    }

                    val success = transaction {
                        GroupMemberships.deleteWhere {
                            (GroupMemberships.groupId eq groupId) and (GroupMemberships.userId eq userId)
                        } > 0
                    }

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Left group successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "You are not a member of this group"
                        ))
                    }
                }
            }
        }
    }
}