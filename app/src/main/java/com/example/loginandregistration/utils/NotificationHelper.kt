package com.example.loginandregistration.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.example.loginandregistration.ChatRoomActivity
import com.example.loginandregistration.MainActivity
import com.example.loginandregistration.R

/**
 * Utility class for creating and displaying notifications throughout the app. Handles chat
 * messages, task reminders, and group updates with appropriate styling and behavior.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        // Notification IDs
        private const val CHAT_NOTIFICATION_BASE_ID = 1000
        private const val TASK_NOTIFICATION_BASE_ID = 2000
        private const val GROUP_NOTIFICATION_BASE_ID = 3000

        // Group keys for notification grouping
        private const val CHAT_GROUP_KEY = "chat_messages_group"
        private const val TASK_GROUP_KEY = "task_reminders_group"
        private const val GROUP_UPDATE_KEY = "group_updates_group"

        // Colors
        private const val CHAT_COLOR = 0xFF2196F3.toInt() // Blue
        private const val TASK_COLOR = 0xFFFF9800.toInt() // Orange
        private const val GROUP_COLOR = 0xFF4CAF50.toInt() // Green
    }

    /**
     * Shows a notification for a new chat message.
     *
     * @param chatId Unique identifier for the chat
     * @param chatName Name of the chat or sender
     * @param senderName Name of the message sender
     * @param messageText Content of the message
     * @param senderImageUrl Optional URL for sender's profile picture
     * @param timestamp Message timestamp
     */
    fun showChatNotification(
            chatId: String,
            chatName: String,
            senderName: String,
            messageText: String,
            senderImageUrl: String? = null,
            timestamp: Long = System.currentTimeMillis()
    ) {
        val notificationId = CHAT_NOTIFICATION_BASE_ID + chatId.hashCode()

        // Create intent to open chat room directly
        val intent =
                Intent(context, ChatRoomActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatName", chatName)
                    putExtra("fromNotification", true)
                    flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        // Create person object for messaging style
        val sender =
                Person.Builder()
                        .setName(senderName)
                        .apply {
                            // Add icon if available (would need to load from URL in real
                            // implementation)
                            // For now, use default icon
                        }
                        .build()

        // Build notification with messaging style
        val notification =
                NotificationCompat.Builder(context, NotificationChannels.CHAT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(chatName)
                        .setContentText("$senderName: $messageText")
                        .setStyle(
                                NotificationCompat.MessagingStyle(sender)
                                        .addMessage(messageText, timestamp, sender)
                                        .setConversationTitle(chatName)
                        )
                        .setColor(CHAT_COLOR)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(CHAT_GROUP_KEY)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound and vibration
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .build()

        notificationManager.notify(notificationId, notification)

        // Show summary notification for grouped chats
        showChatSummaryNotification()
    }

    /**
     * Shows a summary notification for grouped chat messages. This appears when multiple chat
     * notifications are active.
     */
    private fun showChatSummaryNotification() {
        val summaryNotification =
                NotificationCompat.Builder(context, NotificationChannels.CHAT_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("New Messages")
                        .setContentText("You have new messages")
                        .setColor(CHAT_COLOR)
                        .setGroup(CHAT_GROUP_KEY)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(CHAT_NOTIFICATION_BASE_ID, summaryNotification)
    }

    /**
     * Shows a notification for a task reminder.
     *
     * @param taskId Unique identifier for the task
     * @param taskTitle Title of the task
     * @param taskDescription Description or details of the task
     * @param dueDate Due date of the task (formatted string)
     * @param priority Priority level (Low, Medium, High)
     */
    fun showTaskNotification(
            taskId: String,
            taskTitle: String,
            taskDescription: String,
            dueDate: String,
            priority: String = "Medium"
    ) {
        val notificationId = TASK_NOTIFICATION_BASE_ID + taskId.hashCode()

        // Create intent to open task details via MainActivity
        val intent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra("taskId", taskId)
                    putExtra("fromNotification", true)
                    flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        // Create "Mark Complete" action
        val markCompleteIntent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra("taskId", taskId)
                    putExtra("action", "mark_complete")
                    putExtra("fromNotification", true)
                    flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

        val markCompletePendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId + 1,
                        markCompleteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        // Determine priority icon
        val priorityEmoji =
                when (priority.lowercase()) {
                    "high" -> "🔴"
                    "medium" -> "🟡"
                    "low" -> "🟢"
                    else -> ""
                }

        // Build notification
        val notification =
                NotificationCompat.Builder(context, NotificationChannels.TASK_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("$priorityEmoji Task Reminder: $taskTitle")
                        .setContentText("Due: $dueDate")
                        .setStyle(
                                NotificationCompat.BigTextStyle()
                                        .bigText("$taskDescription\n\nDue: $dueDate")
                                        .setBigContentTitle("$priorityEmoji $taskTitle")
                        )
                        .setColor(TASK_COLOR)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .addAction(
                                R.drawable.ic_notification,
                                "Mark Complete",
                                markCompletePendingIntent
                        )
                        .addAction(R.drawable.ic_notification, "View Task", pendingIntent)
                        .setGroup(TASK_GROUP_KEY)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                        .setDefaults(
                                NotificationCompat.DEFAULT_SOUND or
                                        NotificationCompat.DEFAULT_VIBRATE
                        )
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .build()

        notificationManager.notify(notificationId, notification)

        // Show summary notification for grouped tasks
        showTaskSummaryNotification()
    }

    /**
     * Shows a summary notification for grouped task reminders. This appears when multiple task
     * notifications are active.
     */
    private fun showTaskSummaryNotification() {
        val summaryNotification =
                NotificationCompat.Builder(context, NotificationChannels.TASK_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Task Reminders")
                        .setContentText("You have pending tasks")
                        .setColor(TASK_COLOR)
                        .setGroup(TASK_GROUP_KEY)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(TASK_NOTIFICATION_BASE_ID, summaryNotification)
    }

    /**
     * Shows a notification for group updates.
     *
     * @param groupId Unique identifier for the group
     * @param groupName Name of the group
     * @param updateType Type of update (member_added, member_removed, settings_changed, etc.)
     * @param message Descriptive message about the update
     * @param actionUserName Optional name of user who performed the action
     */
    fun showGroupNotification(
            groupId: String,
            groupName: String,
            updateType: String,
            message: String,
            actionUserName: String? = null
    ) {
        val notificationId = GROUP_NOTIFICATION_BASE_ID + groupId.hashCode()

        // Create intent to open group details via MainActivity
        val intent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra("groupId", groupId)
                    putExtra("groupName", groupName)
                    putExtra("fromNotification", true)
                    flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

        val pendingIntent =
                PendingIntent.getActivity(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        // Determine icon based on update type
        val icon =
                when (updateType.lowercase()) {
                    "member_added" -> "👥"
                    "member_removed" -> "👋"
                    "settings_changed" -> "⚙️"
                    "role_changed" -> "🔑"
                    else -> "📢"
                }

        // Build notification style
        val bigTextStyle =
                NotificationCompat.BigTextStyle()
                        .bigText(message)
                        .setBigContentTitle("$icon $groupName")

        if (actionUserName != null) {
            bigTextStyle.setSummaryText("By $actionUserName")
        }

        // Build notification
        val notification =
                NotificationCompat.Builder(context, NotificationChannels.GROUP_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("$icon $groupName")
                        .setContentText(message)
                        .setStyle(bigTextStyle)
                        .setColor(GROUP_COLOR)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(GROUP_UPDATE_KEY)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                        .setDefaults(
                                NotificationCompat.DEFAULT_SOUND or
                                        NotificationCompat.DEFAULT_VIBRATE
                        )
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .build()

        notificationManager.notify(notificationId, notification)

        // Show summary notification for grouped updates
        showGroupSummaryNotification()
    }

    /**
     * Shows a summary notification for grouped group updates. This appears when multiple group
     * notifications are active.
     */
    private fun showGroupSummaryNotification() {
        val summaryNotification =
                NotificationCompat.Builder(context, NotificationChannels.GROUP_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Group Updates")
                        .setContentText("You have group updates")
                        .setColor(GROUP_COLOR)
                        .setGroup(GROUP_UPDATE_KEY)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(GROUP_NOTIFICATION_BASE_ID, summaryNotification)
    }

    /**
     * Cancels a specific notification.
     *
     * @param notificationType Type of notification (chat, task, group)
     * @param id Unique identifier for the notification
     */
    fun cancelNotification(notificationType: String, id: String) {
        val notificationId =
                when (notificationType.lowercase()) {
                    "chat" -> CHAT_NOTIFICATION_BASE_ID + id.hashCode()
                    "task" -> TASK_NOTIFICATION_BASE_ID + id.hashCode()
                    "group" -> GROUP_NOTIFICATION_BASE_ID + id.hashCode()
                    else -> return
                }

        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels all notifications of a specific type.
     *
     * @param notificationType Type of notification (chat, task, group, or all)
     */
    fun cancelAllNotifications(notificationType: String = "all") {
        when (notificationType.lowercase()) {
            "chat" -> {
                // Cancel individual chat notifications and summary
                notificationManager.cancel(CHAT_NOTIFICATION_BASE_ID)
            }
            "task" -> {
                // Cancel individual task notifications and summary
                notificationManager.cancel(TASK_NOTIFICATION_BASE_ID)
            }
            "group" -> {
                // Cancel individual group notifications and summary
                notificationManager.cancel(GROUP_NOTIFICATION_BASE_ID)
            }
            "all" -> {
                // Cancel all notifications
                notificationManager.cancelAll()
            }
        }
    }

    /**
     * Checks if notifications are enabled for the app.
     *
     * @return true if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
}
