
package com.studentworkflow.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val createdAt = timestamp("created_at")
    val aiPromptsRemaining = integer("ai_prompts_remaining").default(0)
    val totalPromptsPurchased = integer("total_prompts_purchased").default(0)
    val isPaidUser = bool("is_paid_user").default(false)
    val lastPaymentDate = timestamp("last_payment_date").nullable()
    val twoFactorSecret = text("two_factor_secret").nullable()
    val twoFactorRecoveryCodes = text("two_factor_recovery_codes").nullable()
    val twoFactorConfirmedAt = timestamp("two_factor_confirmed_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object StudyGroups : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val ownerId = integer("owner_id").references(Users.id)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object GroupMemberships : Table() {
    val groupId = integer("group_id").references(StudyGroups.id)
    val userId = integer("user_id").references(Users.id)
    val role = varchar("role", 20).default("member")
    val joinedAt = timestamp("joined_at")
    override val primaryKey = PrimaryKey(groupId, userId)
}

object Messages : Table() {
    val id = integer("id").autoIncrement()
    val senderId = integer("sender_id").references(Users.id)
    val receiverId = integer("receiver_id").references(Users.id).nullable()
    val groupId = integer("group_id").references(StudyGroups.id).nullable()
    val content = text("content")
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val dueDate = timestamp("due_date").nullable()
    val status = varchar("status", 15).default("pending")
    val priority = varchar("priority", 10).default("medium")
    val creatorId = integer("creator_id").references(Users.id)
    val assigneeId = integer("assignee_id").references(Users.id).nullable()
    val groupId = integer("group_id").references(StudyGroups.id).nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object StudySessions : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val title = varchar("title", 255)
    val startTime = timestamp("start_time")
    val endTime = timestamp("end_time").nullable()
    val isCompleted = bool("is_completed").default(false)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PomodoroSessions : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val taskId = integer("task_id").references(Tasks.id).nullable()
    val sessionType = varchar("session_type", 15).default("work")
    val startTime = timestamp("start_time")
    val durationMinutes = integer("duration_minutes")
    val isCompleted = bool("is_completed").default(false)
    override val primaryKey = PrimaryKey(id)
}

object UserSettings : Table() {
    val userId = integer("user_id").references(Users.id)
    val workDurationMinutes = integer("work_duration_minutes").default(25)
    val shortBreakMinutes = integer("short_break_minutes").default(5)
    val longBreakMinutes = integer("long_break_minutes").default(15)
    val intervalsBeforeLongBreak = integer("intervals_before_long_break").default(4)
    val notificationsEnabled = bool("notifications_enabled").default(true)
    override val primaryKey = PrimaryKey(userId)
}

object Subscriptions : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val planName = varchar("plan_name", 50)
    val startDate = timestamp("start_date")
    val endDate = timestamp("end_date")
    val isActive = bool("is_active").default(true)
    val paymentId = varchar("payment_id", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}

object GoogleCalendars : Table() {
    val userId = integer("user_id").references(Users.id)
    val calendarId = varchar("calendar_id", 255)
    val accessToken = text("access_token")
    val refreshToken = text("refresh_token").nullable()
    val tokenExpiresAt = timestamp("token_expires_at").nullable()
    val lastSyncedAt = timestamp("last_synced_at").nullable()
    override val primaryKey = PrimaryKey(userId)
}

object AIUsageLogs : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val serviceType = varchar("service_type", 255)
    val promptsUsed = integer("prompts_used")
    val remainingPromptsAfter = integer("remaining_prompts_after")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Notifications : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val type = varchar("type", 255)
    val data = text("data") // Store as JSON string
    val read = bool("read").default(false)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PricingPackages : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val price = decimal("price", 10, 2)
    val promptsCount = integer("prompts_count")
    val isActive = bool("is_active").default(true)
    val sortOrder = integer("sort_order").default(0)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object PasswordResetTokens : Table() {
    val email = varchar("email", 255)
    val token = varchar("token", 255)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(email, token)
}

object PricingPackages : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val price = decimal("price", 10, 2)
    val promptsCount = integer("prompts_count")
    val isActive = bool("is_active").default(true)
    val sortOrder = integer("sort_order").default(0)
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}
