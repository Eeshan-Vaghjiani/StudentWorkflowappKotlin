package com.studentworkflow.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: String // Using String for simplicity, will be formatted timestamp
)

@Serializable
data class StudyGroup(
    val id: Int,
    val name: String,
    val description: String?,
    val ownerId: Int,
    val createdAt: String
)

@Serializable
data class GroupMembership(
    val groupId: Int,
    val userId: Int,
    val role: String,
    val joinedAt: String
)

@Serializable
data class Message(
    val id: Int,
    val senderId: Int,
    val receiverId: Int?,
    val groupId: Int?,
    val content: String,
    val isRead: Boolean,
    val createdAt: String
)

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val dueDate: String?,
    val status: String,
    val priority: String,
    val creatorId: Int,
    val assigneeId: Int?,
    val groupId: Int?,
    val createdAt: String
)

@Serializable
data class StudySession(
    val id: Int,
    val userId: Int,
    val title: String,
    val startTime: String,
    val endTime: String?,
    val isCompleted: Boolean,
    val createdAt: String
)

@Serializable
data class PomodoroSession(
    val id: Int,
    val userId: Int,
    val taskId: Int?,
    val sessionType: String,
    val startTime: String,
    val durationMinutes: Int,
    val isCompleted: Boolean
)

@Serializable
data class UserSetting(
    val userId: Int,
    val workDurationMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val intervalsBeforeLongBreak: Int,
    val notificationsEnabled: Boolean
)

@Serializable
data class Subscription(
    val id: Int,
    val userId: Int,
    val planName: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val paymentId: String?
)

@Serializable
data class GoogleCalendar(
    val userId: Int,
    val calendarId: String,
    val connectedAt: String,
    val expiresAt: String?
)

@Serializable
data class AIUsageLog(
    val id: Int,
    val userId: Int,
    val serviceType: String,
    val promptsUsed: Int,
    val remainingPromptsAfter: Int,
    val createdAt: String
)

@Serializable
data class Notification(
    val id: Int,
    val userId: Int,
    val type: String,
    val data: String, // JSON string
    val read: Boolean,
    val createdAt: String
)

@Serializable
data class PricingPackage(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val promptsCount: Int,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: String
)

// Authentication Models
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val userId: Int? = null,
    val twoFactorRequired: Boolean? = null
)

@Serializable
data class TwoFactorVerifyRequest(
    val userId: Int,
    val code: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

// AI Models
@Serializable
data class OpenRouterModel(
    val id: String,
    val name: String
)

@Serializable
data class OpenRouterModelsResponse(
    val data: List<OpenRouterModel>
)

@Serializable
data class OpenAIChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIChatMessage>,
    val temperature: Double = 1.0,
    val response_format: Map<String, String> = mapOf("type" to "json_object")
)

@Serializable
data class OpenAIChatChoice(
    val message: OpenAIChatMessage
)

@Serializable
data class OpenAIChatResponse(
    val choices: List<OpenAIChatChoice>
)

@Serializable
data class Assignment(
    val title: String,
    val unit_name: String,
    val description: String,
    val due_date: String
)

@Serializable
data class TaskResponse(
    val title: String,
    val description: String,
    val assigned_to_name: String,
    val start_date: String,
    val end_date: String,
    val priority: String,
    val effort_hours: Int,
    val importance: Int
)

@Serializable
data class AIResponse(
    val assignment: Assignment,
    val tasks: List<TaskResponse>
)

@Serializable
data class TaskAssignment(
    val id: Int,
    val assigned_user_id: Int
)

@Serializable
data class TaskInfo(
    val id: Int,
    val title: String,
    val effort_hours: Int,
    val importance: Int
)

@Serializable
data class GroupMemberInfo(
    val id: Int,
    val name: String
)

// Additional request/response models
@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val dueDate: String?,
    val priority: String,
    val assigneeId: Int?,
    val groupId: Int?
)

@Serializable
data class UpdateTaskRequest(
    val title: String?,
    val description: String?,
    val dueDate: String?,
    val status: String?,
    val priority: String?,
    val assigneeId: Int?
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val description: String?
)

@Serializable
data class UpdateGroupRequest(
    val name: String?,
    val description: String?
)

@Serializable
data class SendMessageRequest(
    val content: String,
    val receiverId: Int?,
    val groupId: Int?
)

@Serializable
data class CreateStudySessionRequest(
    val title: String,
    val startTime: String
)

@Serializable
data class CreatePomodoroSessionRequest(
    val taskId: Int?,
    val sessionType: String,
    val durationMinutes: Int
)

@Serializable
data class UpdateUserSettingsRequest(
    val workDurationMinutes: Int?,
    val shortBreakMinutes: Int?,
    val longBreakMinutes: Int?,
    val intervalsBeforeLongBreak: Int?,
    val notificationsEnabled: Boolean?
)

@Serializable
data class SubscribeToPlanRequest(
    val planId: Int,
    val paymentId: String?
)

@Serializable
data class FileUploadResponse(
    val success: Boolean,
    val message: String,
    val filePath: String? = null
)

@Serializable
data class NotificationMarkReadRequest(
    val notificationIds: List<Int>
)

@Serializable
data class InviteToGroupRequest(
    val userEmail: String,
    val role: String = "member"
)

@Serializable
data class UpdateProfileRequest(
    val name: String?,
    val email: String?
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)