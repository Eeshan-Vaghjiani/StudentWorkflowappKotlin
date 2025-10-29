package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Represents a notification in the app. Notifications are created when:
 * - User is assigned to a task
 * - User is invited to a group
 * - User receives a new message
 * - Task deadline is approaching
 */
data class Notification(
        @DocumentId val id: String = "",
        val userId: String = "",
        val type: NotificationType = NotificationType.TASK_ASSIGNED,
        val title: String = "",
        val message: String = "",
        val timestamp: Timestamp = Timestamp.now(),
        val isRead: Boolean = false,

        // Optional fields for navigation
        val taskId: String? = null,
        val groupId: String? = null,
        val chatId: String? = null,
        val senderId: String? = null
)

enum class NotificationType {
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_DUE_SOON,
    GROUP_INVITE,
    GROUP_JOINED,
    NEW_MESSAGE,
    MENTION,
    SYSTEM
}
