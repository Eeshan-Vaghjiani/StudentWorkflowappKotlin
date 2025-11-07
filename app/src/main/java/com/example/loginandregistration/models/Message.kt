package com.example.loginandregistration.models

import android.util.Log
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
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
        val videoUrl: String? = null,
        val audioUrl: String? = null,
        val documentUrl: String? = null,
        val documentName: String? = null,
        val documentSize: Long? = null,

        // Enhanced attachment fields
        val attachmentUrl: String? = null,
        val attachmentFileName: String? = null,
        val attachmentFileSize: Long? = null,
        val attachmentMimeType: String? = null,
        val attachmentType: String? = null, // "image", "document", "audio", "video"
        @ServerTimestamp val timestamp: Date = Date(),
        val readBy: List<String> = emptyList(),
        val status: MessageStatus = MessageStatus.SENDING,
        val type: MessageType? = null
) {
    // No init block validation - validate at usage points instead to support GSON deserialization
    
    /** Validates that all required fields are populated */
    fun isValid(): Boolean {
        return id.isNotBlank() && chatId.isNotBlank() && senderId.isNotBlank()
    }
    
    /** Throws exception if message is invalid */
    fun validate() {
        require(isValid()) { 
            "Invalid message: id='$id', chatId='$chatId', senderId='$senderId'" 
        }
    }
    
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
        return !audioUrl.isNullOrEmpty() ||
                (attachmentType == "audio" && !attachmentUrl.isNullOrEmpty())
    }

    /** Check if message has video attachment */
    fun hasVideo(): Boolean {
        return !videoUrl.isNullOrEmpty() ||
                (attachmentType == "video" && !attachmentUrl.isNullOrEmpty())
    }

    /** Check if message has any attachment */
    fun hasAttachment(): Boolean {
        return hasImage() || hasDocument() || hasAudio() || hasVideo()
    }

    /** Get the attachment URL (supports both legacy and new fields) */
    fun resolveAttachmentUrl(): String? {
        return attachmentUrl ?: imageUrl ?: videoUrl ?: audioUrl ?: documentUrl
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

    companion object {
        private const val TAG = "Message"

        /**
         * Safely creates a Message from a Firestore DocumentSnapshot. Returns null if required
         * fields are missing or parsing fails.
         */
        fun fromFirestore(doc: DocumentSnapshot): Message? {
            return try {
                // Extract required fields - return null if any are missing
                val id = doc.id
                val chatId = doc.getString("chatId")
                val senderId = doc.getString("senderId")

                // Validate required fields
                if (chatId.isNullOrBlank() || senderId.isNullOrBlank()) {
                    Log.w(
                            TAG,
                            "Missing required fields in document ${doc.id}: chatId=$chatId, senderId=$senderId"
                    )
                    return null
                }

                // Extract optional fields with safe defaults
                val senderName = doc.getString("senderName") ?: ""
                val senderImageUrl = doc.getString("senderImageUrl") ?: ""
                val text = doc.getString("text") ?: ""

                // Legacy attachment fields
                val imageUrl = doc.getString("imageUrl")
                val videoUrl = doc.getString("videoUrl")
                val audioUrl = doc.getString("audioUrl")
                val documentUrl = doc.getString("documentUrl")
                val documentName = doc.getString("documentName")
                val documentSize = doc.getLong("documentSize")

                // Enhanced attachment fields
                val attachmentUrl = doc.getString("attachmentUrl")
                val attachmentFileName = doc.getString("attachmentFileName")
                val attachmentFileSize = doc.getLong("attachmentFileSize")
                val attachmentMimeType = doc.getString("attachmentMimeType")
                val attachmentType = doc.getString("attachmentType")

                // Timestamp - use current time if not available
                val timestamp = doc.getDate("timestamp") ?: Date()

                // Read by list
                @Suppress("UNCHECKED_CAST")
                val readBy = (doc.get("readBy") as? List<String>) ?: emptyList()

                // Status - parse from string or use default
                val status =
                        doc.getString("status")?.let { statusStr ->
                            try {
                                MessageStatus.valueOf(statusStr)
                            } catch (e: IllegalArgumentException) {
                                Log.w(
                                        TAG,
                                        "Invalid status value: $statusStr, using SENT as default"
                                )
                                MessageStatus.SENT
                            }
                        }
                                ?: MessageStatus.SENT

                // Type - parse from string or use default
                val type =
                        doc.getString("type")?.let { typeStr ->
                            try {
                                MessageType.valueOf(typeStr)
                            } catch (e: IllegalArgumentException) {
                                Log.w(TAG, "Invalid type value: $typeStr, using TEXT as default")
                                MessageType.TEXT
                            }
                        }

                Message(
                        id = id,
                        chatId = chatId,
                        senderId = senderId,
                        senderName = senderName,
                        senderImageUrl = senderImageUrl,
                        text = text,
                        imageUrl = imageUrl,
                        videoUrl = videoUrl,
                        audioUrl = audioUrl,
                        documentUrl = documentUrl,
                        documentName = documentName,
                        documentSize = documentSize,
                        attachmentUrl = attachmentUrl,
                        attachmentFileName = attachmentFileName,
                        attachmentFileSize = attachmentFileSize,
                        attachmentMimeType = attachmentMimeType,
                        attachmentType = attachmentType,
                        timestamp = timestamp,
                        readBy = readBy,
                        status = status,
                        type = type
                )
            } catch (e: IllegalArgumentException) {
                // This catches validation errors from the init block
                Log.e(
                        TAG,
                        "Validation error parsing message from Firestore document ${doc.id}: ${e.message}",
                        e
                )
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing message from Firestore document ${doc.id}", e)
                null
            }
        }
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
