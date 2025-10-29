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

        // Legacy attachment fields (kept for backward compatibility)
        val imageUrl: String? = null,
        val documentUrl: String? = null,
        val documentName: String? = null,
        val documentSize: Long? = null,

        // Enhanced attachment fields
        val attachmentUrl: String? = null,
        val attachmentFileName: String? = null,
        val attachmentFileSize: Long? = null,
        val attachmentMimeType: String? = null,
        val attachmentType: String? = null, // "image", "document", "audio", "video"
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
        return !imageUrl.isNullOrEmpty() ||
                (attachmentType == "image" && !attachmentUrl.isNullOrEmpty())
    }

    /** Check if message has document attachment */
    fun hasDocument(): Boolean {
        return !documentUrl.isNullOrEmpty() ||
                (attachmentType == "document" && !attachmentUrl.isNullOrEmpty())
    }

    /** Check if message has audio attachment */
    fun hasAudio(): Boolean {
        return attachmentType == "audio" && !attachmentUrl.isNullOrEmpty()
    }

    /** Check if message has video attachment */
    fun hasVideo(): Boolean {
        return attachmentType == "video" && !attachmentUrl.isNullOrEmpty()
    }

    /** Check if message has any attachment */
    fun hasAttachment(): Boolean {
        return hasImage() || hasDocument() || hasAudio() || hasVideo()
    }

    /** Get the attachment URL (supports both legacy and new fields) */
    fun resolveAttachmentUrl(): String? {
        return attachmentUrl ?: imageUrl ?: documentUrl
    }

    /** Get the attachment file name (supports both legacy and new fields) */
    fun resolveAttachmentFileName(): String? {
        return attachmentFileName ?: documentName
    }

    /** Get the attachment file size (supports both legacy and new fields) */
    fun resolveAttachmentFileSize(): Long? {
        return attachmentFileSize ?: documentSize
    }

    /** Get formatted file size */
    fun getFormattedFileSize(): String {
        val size = resolveAttachmentFileSize() ?: return ""
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
        }
    }

    /** Get message type for display */
    fun getMessageType(): MessageType {
        return when {
            hasImage() -> MessageType.IMAGE
            hasDocument() -> MessageType.DOCUMENT
            hasAudio() -> MessageType.AUDIO
            hasVideo() -> MessageType.VIDEO
            else -> MessageType.TEXT
        }
    }

    /** Get MIME type for attachment */
    fun getMimeType(): String? {
        return attachmentMimeType
    }

    /** Check if attachment is an image based on MIME type */
    fun isImageMimeType(): Boolean {
        return attachmentMimeType?.startsWith("image/") == true
    }

    /** Check if attachment is a video based on MIME type */
    fun isVideoMimeType(): Boolean {
        return attachmentMimeType?.startsWith("video/") == true
    }

    /** Check if attachment is an audio based on MIME type */
    fun isAudioMimeType(): Boolean {
        return attachmentMimeType?.startsWith("audio/") == true
    }
}

/** Status of a message */
enum class MessageStatus {
    SENDING, // Message is being sent
    SENT, // Message sent to server
    DELIVERED, // Message delivered to recipient
    READ, // Message read by recipient
    FAILED, // Message failed to send (generic)
    FAILED_RETRYABLE, // Message failed but can be retried (network issues)
    FAILED_PERMANENT // Message failed permanently (permission errors)
}

/** Type of message content */
enum class MessageType {
    TEXT,
    IMAGE,
    DOCUMENT,
    AUDIO,
    VIDEO
}

/** Typing status for a chat */
data class TypingStatus(
        val userId: String = "",
        val isTyping: Boolean = false,
        val timestamp: Date = Date()
)
