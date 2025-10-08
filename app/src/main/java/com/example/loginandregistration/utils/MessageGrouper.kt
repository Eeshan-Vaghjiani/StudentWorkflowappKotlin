package com.example.loginandregistration.utils

import com.example.loginandregistration.models.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for grouping messages for display in chat. Groups consecutive messages from the
 * same sender and adds timestamp headers.
 */
object MessageGrouper {

    private const val MESSAGE_GROUP_TIME_THRESHOLD = 5 * 60 * 1000L // 5 minutes in milliseconds

    /** Represents a message item that can be displayed in the chat */
    sealed class MessageItem {
        data class MessageData(
                val message: Message,
                val showSenderInfo: Boolean,
                val showTimestamp: Boolean
        ) : MessageItem()

        data class TimestampHeader(val timestamp: String) : MessageItem()
    }

    /** Represents a group of consecutive messages from the same sender */
    data class MessageGroup(
            val senderId: String,
            val senderName: String,
            val senderImageUrl: String,
            val messages: List<Message>,
            val timestamp: Long
    )

    /**
     * Groups messages for display with timestamp headers and sender grouping.
     *
     * Rules:
     * - Consecutive messages from the same sender within 5 minutes are grouped
     * - Only the first message in a group shows sender info (name and picture)
     * - Timestamp headers are shown when the date changes
     * - Individual message timestamps are shown every 5 minutes
     *
     * @param messages List of messages to group (should be sorted by timestamp)
     * @param currentUserId ID of the current user (to determine sent vs received)
     * @return List of MessageItems with proper grouping and headers
     */
    fun groupMessages(messages: List<Message>, currentUserId: String): List<MessageItem> {
        if (messages.isEmpty()) return emptyList()

        val items = mutableListOf<MessageItem>()
        var lastTimestampHeader: String? = null
        var lastSenderId: String? = null
        var lastMessageTime: Long = 0

        messages.forEach { message ->
            val timestamp = message.timestamp?.time ?: 0
            val timestampHeader = getTimestampHeader(timestamp)

            // Add timestamp header if it's different from the last one
            if (timestampHeader != lastTimestampHeader) {
                items.add(MessageItem.TimestampHeader(timestampHeader))
                lastTimestampHeader = timestampHeader
            }

            // Determine if we should show sender info
            // For received messages: show if different sender OR more than 5 minutes since last
            // message
            // For sent messages: never show sender info (it's always the current user)
            val showSenderInfo =
                    if (message.senderId != currentUserId) {
                        message.senderId != lastSenderId ||
                                (timestamp - lastMessageTime) > MESSAGE_GROUP_TIME_THRESHOLD
                    } else {
                        false
                    }

            // Show individual timestamp if more than 5 minutes since last message
            val showTimestamp = (timestamp - lastMessageTime) > MESSAGE_GROUP_TIME_THRESHOLD

            items.add(
                    MessageItem.MessageData(
                            message = message,
                            showSenderInfo = showSenderInfo,
                            showTimestamp = showTimestamp
                    )
            )

            lastSenderId = message.senderId
            lastMessageTime = timestamp
        }

        return items
    }

    /**
     * Groups consecutive messages from the same sender.
     *
     * @param messages List of messages to group
     * @return List of message groups
     */
    fun groupConsecutiveMessages(messages: List<Message>): List<MessageGroup> {
        if (messages.isEmpty()) return emptyList()

        val groups = mutableListOf<MessageGroup>()
        var currentGroup = mutableListOf<Message>()
        var currentSenderId: String? = null
        var lastMessageTime: Long = 0

        messages.forEach { message ->
            val timestamp = message.timestamp?.time ?: 0

            // Start a new group if:
            // 1. Different sender
            // 2. More than 5 minutes since last message
            if (currentSenderId != message.senderId ||
                            (timestamp - lastMessageTime) > MESSAGE_GROUP_TIME_THRESHOLD
            ) {
                // Save the current group if it's not empty
                if (currentGroup.isNotEmpty()) {
                    val firstMessage = currentGroup.first()
                    groups.add(
                            MessageGroup(
                                    senderId = currentSenderId ?: "",
                                    senderName = firstMessage.senderName,
                                    senderImageUrl = firstMessage.senderImageUrl,
                                    messages = currentGroup.toList(),
                                    timestamp = firstMessage.timestamp?.time ?: 0
                            )
                    )
                }

                // Start a new group
                currentGroup = mutableListOf(message)
                currentSenderId = message.senderId
            } else {
                // Add to current group
                currentGroup.add(message)
            }

            lastMessageTime = timestamp
        }

        // Add the last group
        if (currentGroup.isNotEmpty()) {
            val firstMessage = currentGroup.first()
            groups.add(
                    MessageGroup(
                            senderId = currentSenderId ?: "",
                            senderName = firstMessage.senderName,
                            senderImageUrl = firstMessage.senderImageUrl,
                            messages = currentGroup.toList(),
                            timestamp = firstMessage.timestamp?.time ?: 0
                    )
            )
        }

        return groups
    }

    /**
     * Gets a human-readable timestamp header for a given timestamp. Returns "Today", "Yesterday",
     * or a formatted date.
     */
    private fun getTimestampHeader(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(messageDate, today) -> "Today"
            isSameDay(messageDate, yesterday) -> "Yesterday"
            else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    /** Checks if two Calendar instances represent the same day */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /** Formats a timestamp to a time string (e.g., "2:30 PM") */
    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    /** Formats a timestamp to a date string (e.g., "Jan 15, 2024") */
    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    /** Formats a timestamp to a full date and time string (e.g., "Jan 15, 2024 at 2:30 PM") */
    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                .format(Date(timestamp))
    }
}
