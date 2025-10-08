package com.example.loginandregistration.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** Represents a message in a chat */
data class Message(
        @DocumentId val id: String = "",
        val chatId: String = "",
        val senderId: String = "",
        val senderName: String = "",
        val senderImageUrl: String = "",
        val text: String = "",
        val imageUrl: String? = null,
        val documentUrl: String? = null,
        val documentName: String? = null,
        val documentSize: Long? = null,
        @ServerTimestamp val timestamp: Date? = null,
        val readBy: List<String> = emptyList(),
        val status: MessageStatus = MessageStatus.SENDING
) {
    /** Check if message has been read by user */
    fun isReadBy(userId: String): Boolean {
        return readBy.contains(userId)
    }

    /** Check if message is from user */
    fun isFromUser(userId: String): Boolean {
        return senderId == userId
    }

    /** Check if message has image attachment */
    fun hasImage(): Boolean {
        return !imageUrl.isNullOrEmpty()
    }

    /** Check if message has document attachment */
    fun hasDocument(): Boolean {
        return !documentUrl.isNullOrEmpty()
    }

    /** Get formatted file size */
    fun getFormattedFileSize(): String {
        val size = documentSize ?: return ""
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    /** Get message type for display */
    fun getMessageType(): MessageType {
        return when {
            hasImage() -> MessageType.IMAGE
            hasDocument() -> MessageType.DOCUMENT
            else -> MessageType.TEXT
        }
    }
}

/** Status of a message */
enum class MessageStatus {
    SENDING, // Message is being sent
    SENT, // Message sent to server
    DELIVERED, // Message delivered to recipient
    READ, // Message read by recipient
    FAILED // Message failed to send
}

/** Type of message content */
enum class MessageType {
    TEXT,
    IMAGE,
    DOCUMENT
}

/** Typing status for a chat */
data class TypingStatus(
        val userId: String = "",
        val isTyping: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
)
