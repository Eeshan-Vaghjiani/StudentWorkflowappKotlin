package com.example.loginandregistration.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object NotificationChannels {

    const val CHAT_CHANNEL_ID = "chat_messages"
    const val TASK_CHANNEL_ID = "task_reminders"
    const val GROUP_CHANNEL_ID = "group_updates"

    private const val CHAT_CHANNEL_NAME = "Chat Messages"
    private const val TASK_CHANNEL_NAME = "Task Reminders"
    private const val GROUP_CHANNEL_NAME = "Group Updates"

    private const val CHAT_CHANNEL_DESC = "Notifications for new chat messages"
    private const val TASK_CHANNEL_DESC = "Reminders for upcoming tasks and deadlines"
    private const val GROUP_CHANNEL_DESC = "Updates about group activities and changes"

    /**
     * Creates all notification channels for the app. Should be called when the app starts (e.g., in
     * MainActivity or Application class).
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create Chat Messages channel
            val chatChannel =
                    NotificationChannel(
                                    CHAT_CHANNEL_ID,
                                    CHAT_CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_HIGH
                            )
                            .apply {
                                description = CHAT_CHANNEL_DESC
                                enableLights(true)
                                enableVibration(true)
                                setShowBadge(true)

                                // Set custom sound
                                val soundUri =
                                        RingtoneManager.getDefaultUri(
                                                RingtoneManager.TYPE_NOTIFICATION
                                        )
                                val audioAttributes =
                                        AudioAttributes.Builder()
                                                .setContentType(
                                                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                                                )
                                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                                .build()
                                setSound(soundUri, audioAttributes)
                            }

            // Create Task Reminders channel
            val taskChannel =
                    NotificationChannel(
                                    TASK_CHANNEL_ID,
                                    TASK_CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_DEFAULT
                            )
                            .apply {
                                description = TASK_CHANNEL_DESC
                                enableLights(true)
                                enableVibration(true)
                                setShowBadge(true)
                            }

            // Create Group Updates channel
            val groupChannel =
                    NotificationChannel(
                                    GROUP_CHANNEL_ID,
                                    GROUP_CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_DEFAULT
                            )
                            .apply {
                                description = GROUP_CHANNEL_DESC
                                enableLights(true)
                                enableVibration(true)
                                setShowBadge(true)
                            }

            // Register all channels
            notificationManager.createNotificationChannels(
                    listOf(chatChannel, taskChannel, groupChannel)
            )
        }
    }

    /** Deletes all notification channels. Useful for testing or resetting notification settings. */
    fun deleteChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHAT_CHANNEL_ID)
            notificationManager.deleteNotificationChannel(TASK_CHANNEL_ID)
            notificationManager.deleteNotificationChannel(GROUP_CHANNEL_ID)
        }
    }
}
