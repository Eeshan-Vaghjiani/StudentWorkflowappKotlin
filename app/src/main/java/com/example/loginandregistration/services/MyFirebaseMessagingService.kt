package com.example.loginandregistration.services

import android.util.Log
import com.example.loginandregistration.repository.NotificationRepository
import com.example.loginandregistration.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val notificationHelper by lazy { NotificationHelper(this) }
    private val notificationRepository by lazy { NotificationRepository() }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"

        // Notification channel IDs
        const val CHAT_CHANNEL_ID = "chat_messages"
        const val TASK_CHANNEL_ID = "task_reminders"
        const val GROUP_CHANNEL_ID = "group_updates"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Save token to user document in Firestore using NotificationRepository
        // Note: This will fail if user is not authenticated yet, which is expected
        serviceScope.launch {
            val result = notificationRepository.saveFcmToken()
            if (result.isSuccess) {
                Log.d(TAG, "FCM token saved successfully via repository")
            } else {
                val exception = result.exceptionOrNull()
                if (exception?.message?.contains("not authenticated") == true) {
                    Log.w(
                            TAG,
                            "FCM token not saved - user not authenticated yet. Token will be saved on login."
                    )
                } else {
                    Log.e(TAG, "Failed to save FCM token via repository", exception)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification body: ${it.body}")
            // If notification payload exists, it's handled automatically by system
            // We only need to handle data payload for custom behavior
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return

        when (type) {
            "chat" -> handleChatNotification(data)
            "task" -> handleTaskNotification(data)
            "group" -> handleGroupNotification(data)
            else -> Log.w(TAG, "Unknown notification type: $type")
        }
    }

    private fun handleChatNotification(data: Map<String, String>) {
        val chatId = data["chatId"] ?: return
        val senderName = data["senderName"] ?: "Someone"
        val message = data["message"] ?: "New message"
        val chatName = data["chatName"] ?: data["title"] ?: senderName
        val senderImageUrl = data["senderImageUrl"]
        val timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

        // Don't show notification if user is currently in this chat
        // This would be checked via a shared preference or app state

        notificationHelper.showChatNotification(
                chatId = chatId,
                chatName = chatName,
                senderName = senderName,
                messageText = message,
                senderImageUrl = senderImageUrl,
                timestamp = timestamp
        )
    }

    private fun handleTaskNotification(data: Map<String, String>) {
        val taskId = data["taskId"] ?: return
        val taskTitle = data["taskTitle"] ?: "Task Reminder"
        val taskDescription =
                data["taskDescription"] ?: data["message"] ?: "You have a task due soon"
        val dueDate = data["dueDate"] ?: "Soon"
        val priority = data["priority"] ?: "Medium"

        notificationHelper.showTaskNotification(
                taskId = taskId,
                taskTitle = taskTitle,
                taskDescription = taskDescription,
                dueDate = dueDate,
                priority = priority
        )
    }

    private fun handleGroupNotification(data: Map<String, String>) {
        val groupId = data["groupId"] ?: return
        val groupName = data["groupName"] ?: "Group Update"
        val message = data["message"] ?: "New group activity"
        val updateType = data["updateType"] ?: "general"
        val actionUserName = data["actionUserName"]

        notificationHelper.showGroupNotification(
                groupId = groupId,
                groupName = groupName,
                updateType = updateType,
                message = message,
                actionUserName = actionUserName
        )
    }
}
