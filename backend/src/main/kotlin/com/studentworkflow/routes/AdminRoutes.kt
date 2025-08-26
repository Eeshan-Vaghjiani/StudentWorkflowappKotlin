package com.studentworkflow.routes

import com.studentworkflow.db.*
import com.studentworkflow.models.*
import com.studentworkflow.services.PricingService
import com.studentworkflow.services.UserService
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
import java.time.temporal.ChronoUnit

/**
 * AdminRoutes handles admin dashboard endpoints including
 * user and group management, analytics, and system administration.
 */
fun Application.configureAdminRoutes(
    userService: UserService,
    pricingService: PricingService
) {
    routing {
        authenticate("jwt") {
            route("/api/admin") {
                
                // Middleware to check admin permissions
                // For now, we'll assume admin status is determined by a specific email or user property
                // In a real app, you'd have a proper role system
                
                // Get system statistics
                get("/stats") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    // Simple admin check - you'd implement proper role checking
                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@get
                    }

                    val stats = transaction {
                        val totalUsers = Users.selectAll().count()
                        val paidUsers = Users.select { Users.isPaidUser eq true }.count()
                        val totalGroups = StudyGroups.selectAll().count()
                        val totalTasks = Tasks.selectAll().count()
                        val completedTasks = Tasks.select { Tasks.status eq "completed" }.count()
                        val totalMessages = Messages.selectAll().count()
                        val totalStudySessions = StudySessions.selectAll().count()
                        val totalNotifications = Notifications.selectAll().count()

                        // Recent activity (last 30 days)
                        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)
                        val newUsersLast30Days = Users.select { Users.createdAt greater thirtyDaysAgo }.count()
                        val newGroupsLast30Days = StudyGroups.select { StudyGroups.createdAt greater thirtyDaysAgo }.count()
                        val newTasksLast30Days = Tasks.select { Tasks.createdAt greater thirtyDaysAgo }.count()

                        mapOf(
                            "total_users" to totalUsers,
                            "paid_users" to paidUsers,
                            "total_groups" to totalGroups,
                            "total_tasks" to totalTasks,
                            "completed_tasks" to completedTasks,
                            "total_messages" to totalMessages,
                            "total_study_sessions" to totalStudySessions,
                            "total_notifications" to totalNotifications,
                            "new_users_last_30_days" to newUsersLast30Days,
                            "new_groups_last_30_days" to newGroupsLast30Days,
                            "new_tasks_last_30_days" to newTasksLast30Days
                        )
                    }

                    val subscriptionStats = pricingService.getSubscriptionStats()
                    
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "system_stats" to stats,
                        "subscription_stats" to subscriptionStats
                    ))
                }

                // Get all users with pagination
                get("/users") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@get
                    }

                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val search = call.request.queryParameters["search"]

                    val users = transaction {
                        var query = Users.selectAll()
                        
                        search?.let { searchTerm ->
                            query = query.andWhere { 
                                (Users.name like "%$searchTerm%") or (Users.email like "%$searchTerm%")
                            }
                        }

                        query.limit(limit, offset.toLong())
                            .orderBy(Users.createdAt, SortOrder.DESC)
                            .map { row ->
                                mapOf(
                                    "id" to row[Users.id],
                                    "name" to row[Users.name],
                                    "email" to row[Users.email],
                                    "created_at" to row[Users.createdAt].toString(),
                                    "is_paid_user" to row[Users.isPaidUser],
                                    "ai_prompts_remaining" to row[Users.aiPromptsRemaining],
                                    "total_prompts_purchased" to row[Users.totalPromptsPurchased],
                                    "last_payment_date" to row[Users.lastPaymentDate]?.toString(),
                                    "two_factor_enabled" to (row[Users.twoFactorSecret] != null)
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "users" to users
                    ))
                }

                // Get specific user details
                get("/users/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal?.payload?.getClaim("userId")?.asInt()
                    val targetUserId = call.parameters["id"]?.toIntOrNull()
                    
                    if (currentUserId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    if (targetUserId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                        return@get
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq currentUserId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || currentUserId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@get
                    }

                    val userDetails = transaction {
                        val user = Users.select { Users.id eq targetUserId }.singleOrNull()
                        
                        if (user != null) {
                            val subscriptions = Subscriptions.select { Subscriptions.userId eq targetUserId }
                                .orderBy(Subscriptions.startDate, SortOrder.DESC)
                                .map { sub ->
                                    mapOf(
                                        "id" to sub[Subscriptions.id].value,
                                        "plan_name" to sub[Subscriptions.planName],
                                        "start_date" to sub[Subscriptions.startDate].toString(),
                                        "end_date" to sub[Subscriptions.endDate].toString(),
                                        "is_active" to sub[Subscriptions.isActive]
                                    )
                                }

                            val groups = (GroupMemberships innerJoin StudyGroups)
                                .select { GroupMemberships.userId eq targetUserId }
                                .map { row ->
                                    mapOf(
                                        "id" to row[StudyGroups.id].value,
                                        "name" to row[StudyGroups.name],
                                        "role" to row[GroupMemberships.role],
                                        "joined_at" to row[GroupMemberships.joinedAt].toString()
                                    )
                                }

                            mapOf(
                                "user" to mapOf(
                                    "id" to user[Users.id],
                                    "name" to user[Users.name],
                                    "email" to user[Users.email],
                                    "created_at" to user[Users.createdAt].toString(),
                                    "is_paid_user" to user[Users.isPaidUser],
                                    "ai_prompts_remaining" to user[Users.aiPromptsRemaining],
                                    "total_prompts_purchased" to user[Users.totalPromptsPurchased],
                                    "last_payment_date" to user[Users.lastPaymentDate]?.toString(),
                                    "two_factor_enabled" to (user[Users.twoFactorSecret] != null)
                                ),
                                "subscriptions" to subscriptions,
                                "groups" to groups
                            )
                        } else null
                    }

                    if (userDetails != null) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "data" to userDetails
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    }
                }

                // Get all groups with details
                get("/groups") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@get
                    }

                    val groups = transaction {
                        StudyGroups.selectAll()
                            .orderBy(StudyGroups.createdAt, SortOrder.DESC)
                            .map { row ->
                                val memberCount = GroupMemberships.select { 
                                    GroupMemberships.groupId eq row[StudyGroups.id].value 
                                }.count()
                                
                                val owner = Users.select { Users.id eq row[StudyGroups.ownerId] }.singleOrNull()
                                
                                mapOf(
                                    "id" to row[StudyGroups.id].value,
                                    "name" to row[StudyGroups.name],
                                    "description" to row[StudyGroups.description],
                                    "owner_id" to row[StudyGroups.ownerId],
                                    "owner_name" to (owner?.get(Users.name) ?: "Unknown"),
                                    "member_count" to memberCount,
                                    "created_at" to row[StudyGroups.createdAt].toString()
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "groups" to groups
                    ))
                }

                // Create pricing package
                post("/pricing/packages") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@post
                    }

                    val packageData = call.receive<Map<String, Any>>()
                    val name = packageData["name"] as? String
                    val description = packageData["description"] as? String
                    val price = (packageData["price"] as? Number)?.toDouble()
                    val promptsCount = (packageData["prompts_count"] as? Number)?.toInt()
                    val sortOrder = (packageData["sort_order"] as? Number)?.toInt() ?: 0

                    if (name == null || price == null || promptsCount == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing required fields"))
                        return@post
                    }

                    val newPackage = pricingService.createPackage(name, description, price, promptsCount, sortOrder)
                    
                    if (newPackage != null) {
                        call.respond(HttpStatusCode.Created, mapOf(
                            "success" to true,
                            "message" to "Package created successfully",
                            "package" to newPackage
                        ))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "success" to false,
                            "message" to "Failed to create package"
                        ))
                    }
                }

                // Update pricing package
                put("/pricing/packages/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val packageId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@put
                    }

                    if (packageId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid package ID"))
                        return@put
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@put
                    }

                    val packageData = call.receive<Map<String, Any>>()
                    val name = packageData["name"] as? String
                    val description = packageData["description"] as? String
                    val price = (packageData["price"] as? Number)?.toDouble()
                    val promptsCount = (packageData["prompts_count"] as? Number)?.toInt()
                    val isActive = packageData["is_active"] as? Boolean
                    val sortOrder = (packageData["sort_order"] as? Number)?.toInt()

                    val success = pricingService.updatePackage(
                        id = packageId,
                        name = name,
                        description = description,
                        price = price,
                        promptsCount = promptsCount,
                        isActive = isActive,
                        sortOrder = sortOrder
                    )

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Package updated successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Package not found"
                        ))
                    }
                }

                // Delete pricing package
                delete("/pricing/packages/{id}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val packageId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@delete
                    }

                    if (packageId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid package ID"))
                        return@delete
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@delete
                    }

                    val success = pricingService.deletePackage(packageId)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Package deleted successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Package not found"
                        ))
                    }
                }

                // System maintenance operations
                post("/maintenance/cleanup") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val isAdmin = transaction {
                        val user = Users.select { Users.id eq userId }.singleOrNull()
                        user?.get(Users.email)?.endsWith("@admin.com") == true || userId == 1
                    }

                    if (!isAdmin) {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Admin access required"))
                        return@post
                    }

                    // Perform maintenance tasks
                    val expiredSubscriptions = pricingService.expireOldSubscriptions()
                    
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "message" to "Maintenance completed",
                        "expired_subscriptions" to expiredSubscriptions
                    ))
                }
            }
        }
    }
}