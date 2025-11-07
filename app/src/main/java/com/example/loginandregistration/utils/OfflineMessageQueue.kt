package com.example.loginandregistration.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Wrapper for queued messages with retry metadata */
data class QueuedMessage(
        val message: Message,
        val attempts: Int = 0,
        val lastAttemptTime: Long = System.currentTimeMillis(),
        val queuedTime: Long = System.currentTimeMillis()
)

/**
 * Manages offline message queue for reliable message delivery. Stores messages locally when they
 * fail to send and retries them when connection is restored. Distinguishes between retryable
 * failures (network issues) and permanent failures (permission errors).
 */
class OfflineMessageQueue(context: Context) {

    private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val TAG = "OfflineMessageQueue"
        private const val PREFS_NAME = "offline_message_queue"
        private const val KEY_QUEUED_MESSAGES = "queued_messages"
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    /** Add a message to the queue */
    fun queueMessage(message: Message?) {
        // Check if message parameter is null before processing
        if (message == null) {
            // Log where the null message came from and queue state
            Log.e(
                    TAG,
                    "Attempted to queue null message - source: queueMessage(), queueSize: ${getQueuedMessagesInternal().size}, operation: ADD"
            )
            return
        }

        // Validate message using Message.isValid() before adding to queue
        try {
            if (!message.isValid()) {
                Log.e(
                        TAG,
                        "Invalid message, cannot queue: id='${message.id}', chatId='${message.chatId}', senderId='${message.senderId}'"
                )
                return
            }

            // Message is valid, proceed with queueing
            val queuedMessages = getQueuedMessagesInternal().toMutableList()

            // Check if message already exists in queue
            val existingIndex = queuedMessages.indexOfFirst { it.message.id == message.id }
            if (existingIndex != -1) {
                // Update existing message, preserve attempt count
                val existing = queuedMessages[existingIndex]
                queuedMessages[existingIndex] = existing.copy(message = message)
                Log.d(TAG, "Updated message ${message.id} in queue")
            } else {
                // Add new message
                queuedMessages.add(QueuedMessage(message = message))
                Log.d(TAG, "Added message ${message.id} to queue")
            }

            saveQueueInternal(queuedMessages)
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error queueing message - messageId: ${message.id}, chatId: ${message.chatId}, senderId: ${message.senderId}, error: ${e.message}",
                    e
            )
        }
    }

    /** Remove a message from the queue (after successful send) */
    fun removeMessage(messageId: String) {
        try {
            val queuedMessages = getQueuedMessagesInternal().toMutableList()
            val removed = queuedMessages.removeAll { it.message.id == messageId }

            if (removed) {
                saveQueueInternal(queuedMessages)
                Log.d(TAG, "Removed message $messageId from queue")
            }
        } catch (e: Exception) {
            val queueSize =
                    try {
                        getQueuedMessagesInternal().size
                    } catch (ex: Exception) {
                        -1
                    }
            Log.e(
                    TAG,
                    "Error removing message from queue - messageId: $messageId, queueSize: $queueSize, error: ${e.message}",
                    e
            )
        }
    }

    /** Remove a message from the queue by Message object (after successful send) */
    fun removeMessage(message: Message?) {
        // Check if message parameter is null before processing
        if (message == null) {
            // Log where the null message came from and queue state
            val queueSize =
                    try {
                        getQueuedMessagesInternal().size
                    } catch (ex: Exception) {
                        -1
                    }
            Log.e(
                    TAG,
                    "Attempted to remove null message from queue - source: removeMessage(Message), queueSize: $queueSize, operation: REMOVE"
            )
            return
        }

        try {
            val queuedMessages = getQueuedMessagesInternal().toMutableList()
            // Use safe null check when comparing message IDs
            val removed = queuedMessages.removeAll { it.message.id == message.id }

            if (removed) {
                saveQueueInternal(queuedMessages)
                Log.d(TAG, "Removed message ${message.id} from queue")
            }
        } catch (e: Exception) {
            val queueSize =
                    try {
                        getQueuedMessagesInternal().size
                    } catch (ex: Exception) {
                        -1
                    }
            Log.e(
                    TAG,
                    "Error removing message from queue - messageId: ${message.id}, chatId: ${message.chatId}, queueSize: $queueSize, error: ${e.message}",
                    e
            )
        }
    }

    /** Mark a message as failed with retryable status */
    fun markMessageAsFailedRetryable(messageId: String) {
        try {
            val queuedMessages = getQueuedMessagesInternal().toMutableList()
            val messageIndex = queuedMessages.indexOfFirst { it.message.id == messageId }

            if (messageIndex != -1) {
                val queued = queuedMessages[messageIndex]
                val newAttempts = queued.attempts + 1

                // Check if we've exceeded max attempts
                val newStatus =
                        if (newAttempts >= MAX_RETRY_ATTEMPTS) {
                            Log.w(
                                    TAG,
                                    "Message $messageId exceeded max retry attempts, marking as permanently failed"
                            )
                            MessageStatus.FAILED_PERMANENT
                        } else {
                            MessageStatus.FAILED_RETRYABLE
                        }

                queuedMessages[messageIndex] =
                        queued.copy(
                                message = queued.message.copy(status = newStatus),
                                attempts = newAttempts,
                                lastAttemptTime = System.currentTimeMillis()
                        )
                saveQueueInternal(queuedMessages)
                Log.d(TAG, "Marked message $messageId as $newStatus (attempt $newAttempts)")
            }
        } catch (e: Exception) {
            val attempts =
                    try {
                        getQueuedMessagesInternal().find { it.message.id == messageId }?.attempts
                                ?: 0
                    } catch (ex: Exception) {
                        0
                    }
            Log.e(
                    TAG,
                    "Error marking message as failed retryable - messageId: $messageId, attempts: $attempts, error: ${e.message}",
                    e
            )
        }
    }

    /** Mark a message as permanently failed (e.g., permission errors) */
    fun markMessageAsFailedPermanent(messageId: String) {
        try {
            val queuedMessages = getQueuedMessagesInternal().toMutableList()
            val messageIndex = queuedMessages.indexOfFirst { it.message.id == messageId }

            if (messageIndex != -1) {
                val queued = queuedMessages[messageIndex]
                queuedMessages[messageIndex] =
                        queued.copy(
                                message =
                                        queued.message.copy(status = MessageStatus.FAILED_PERMANENT)
                        )
                saveQueueInternal(queuedMessages)
                Log.d(TAG, "Marked message $messageId as FAILED_PERMANENT")
            }
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error marking message as failed permanent - messageId: $messageId, error: ${e.message}",
                    e
            )
        }
    }

    /** Mark a message as failed (legacy method for backward compatibility) */
    fun markMessageAsFailed(messageId: String) {
        markMessageAsFailedRetryable(messageId)
    }

    /** Update message status */
    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        try {
            val queuedMessages = getQueuedMessagesInternal().toMutableList()
            val messageIndex = queuedMessages.indexOfFirst { it.message.id == messageId }

            if (messageIndex != -1) {
                val queued = queuedMessages[messageIndex]
                queuedMessages[messageIndex] =
                        queued.copy(message = queued.message.copy(status = status))
                saveQueueInternal(queuedMessages)
                Log.d(TAG, "Updated message $messageId status to $status")
            }
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error updating message status - messageId: $messageId, newStatus: $status, error: ${e.message}",
                    e
            )
        }
    }

    /** Increment retry attempt counter for a message */
    fun incrementAttempts(messageId: String) {
        try {
            val queuedMessages = getQueuedMessagesInternal().toMutableList()
            val messageIndex = queuedMessages.indexOfFirst { it.message.id == messageId }

            if (messageIndex != -1) {
                val queued = queuedMessages[messageIndex]
                queuedMessages[messageIndex] =
                        queued.copy(
                                attempts = queued.attempts + 1,
                                lastAttemptTime = System.currentTimeMillis()
                        )
                saveQueueInternal(queuedMessages)
                Log.d(TAG, "Incremented attempts for message $messageId to ${queued.attempts + 1}")
            }
        } catch (e: Exception) {
            val attempts =
                    try {
                        getQueuedMessagesInternal().find { it.message.id == messageId }?.attempts
                                ?: 0
                    } catch (ex: Exception) {
                        0
                    }
            Log.e(
                    TAG,
                    "Error incrementing attempts - messageId: $messageId, currentAttempts: $attempts, error: ${e.message}",
                    e
            )
        }
    }

    /** Get all queued messages (returns Message objects for backward compatibility) */
    fun getQueuedMessages(): List<Message> {
        return getQueuedMessagesInternal().map { it.message }
    }

    /** Get queued messages for a specific chat */
    fun getQueuedMessagesForChat(chatId: String): List<Message> {
        return getQueuedMessagesInternal()
                .mapNotNull { queuedMessage ->
                    try {
                        // Wrap message.chatId access in try-catch to handle any remaining null
                        // issues
                        val message = queuedMessage.message
                        if (message.chatId == chatId) {
                            message
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        // Log errors when filtering fails
                        Log.e(
                                TAG,
                                "Error filtering message in getQueuedMessagesForChat: ${e.message}",
                                e
                        )
                        null
                    }
                }
                .filterNotNull() // Add filterNotNull() call to remove any null entries from queue
    }

    /** Get count of pending messages (SENDING status) */
    fun getPendingMessageCount(): Int {
        return getQueuedMessagesInternal().count { it.message.status == MessageStatus.SENDING }
    }

    /** Get count of failed retryable messages */
    fun getFailedRetryableCount(): Int {
        return getQueuedMessagesInternal().count {
            it.message.status == MessageStatus.FAILED_RETRYABLE
        }
    }

    /** Get count of permanently failed messages */
    fun getFailedPermanentCount(): Int {
        return getQueuedMessagesInternal().count {
            it.message.status == MessageStatus.FAILED_PERMANENT
        }
    }

    /** Get count of all failed messages (legacy method) */
    fun getFailedMessageCount(): Int {
        return getQueuedMessagesInternal().count {
            it.message.status == MessageStatus.FAILED ||
                    it.message.status == MessageStatus.FAILED_RETRYABLE ||
                    it.message.status == MessageStatus.FAILED_PERMANENT
        }
    }

    /** Clear all messages from queue */
    fun clearQueue() {
        try {
            prefs.edit().remove(KEY_QUEUED_MESSAGES).apply()
            Log.d(TAG, "Cleared message queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing queue - error: ${e.message}", e)
        }
    }

    /** Clear only failed messages from queue */
    fun clearFailedMessages() {
        try {
            val queuedMessages =
                    getQueuedMessagesInternal().filter {
                        it.message.status != MessageStatus.FAILED &&
                                it.message.status != MessageStatus.FAILED_RETRYABLE &&
                                it.message.status != MessageStatus.FAILED_PERMANENT
                    }
            saveQueueInternal(queuedMessages)
            Log.d(TAG, "Cleared failed messages from queue")
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error clearing failed messages - queueSize: ${getQueuedMessagesInternal().size}, error: ${e.message}",
                    e
            )
        }
    }

    /** Clear only permanently failed messages from queue */
    fun clearPermanentlyFailedMessages() {
        try {
            val queuedMessages =
                    getQueuedMessagesInternal().filter {
                        it.message.status != MessageStatus.FAILED_PERMANENT
                    }
            saveQueueInternal(queuedMessages)
            Log.d(TAG, "Cleared permanently failed messages from queue")
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error clearing permanently failed messages - queueSize: ${getQueuedMessagesInternal().size}, error: ${e.message}",
                    e
            )
        }
    }

    /** Get messages that need retry (FAILED_RETRYABLE status and ready for retry) */
    fun getMessagesNeedingRetry(retryThresholdMs: Long = 30000): List<Message> {
        val currentTime = System.currentTimeMillis()
        return getQueuedMessagesInternal()
                .filter { queued ->
                    queued.message.status == MessageStatus.FAILED_RETRYABLE &&
                            queued.attempts < MAX_RETRY_ATTEMPTS &&
                            (currentTime - queued.lastAttemptTime) > retryThresholdMs
                }
                .map { it.message }
    }

    /** Get messages with SENDING status that are old enough to retry */
    fun getPendingMessagesNeedingRetry(retryThresholdMs: Long = 30000): List<Message> {
        val currentTime = System.currentTimeMillis()
        return getQueuedMessagesInternal()
                .filter { queued ->
                    queued.message.status == MessageStatus.SENDING &&
                            queued.message.timestamp != null &&
                            (currentTime - queued.message.timestamp.time) > retryThresholdMs
                }
                .map { it.message }
    }

    /** Get all messages that can be retried (both SENDING and FAILED_RETRYABLE) */
    fun getAllRetryableMessages(retryThresholdMs: Long = 30000): List<Message> {
        val currentTime = System.currentTimeMillis()
        return getQueuedMessagesInternal()
                .filter { queued ->
                    (queued.message.status == MessageStatus.SENDING ||
                            queued.message.status == MessageStatus.FAILED_RETRYABLE) &&
                            queued.attempts < MAX_RETRY_ATTEMPTS &&
                            (currentTime - queued.lastAttemptTime) > retryThresholdMs
                }
                .map { it.message }
    }

    /** Get internal queue with metadata */
    private fun getQueuedMessagesInternal(): List<QueuedMessage> {
        return try {
            val json = prefs.getString(KEY_QUEUED_MESSAGES, null)
            if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                // Add try-catch around JSON parsing
                try {
                    val type = object : TypeToken<List<QueuedMessage>>() {}.type
                    val parsedMessages: List<QueuedMessage>? = gson.fromJson(json, type)

                    // Filter out null and invalid messages after parsing
                    val validMessages =
                            parsedMessages?.filterNotNull()?.mapNotNull { queuedMessage ->
                                try {
                                    // Validate message after deserialization
                                    if (queuedMessage.message.isValid()) {
                                        queuedMessage
                                    } else {
                                        Log.w(
                                                TAG,
                                                "Skipping invalid queued message during deserialization: " +
                                                        "id='${queuedMessage.message.id}', " +
                                                        "chatId='${queuedMessage.message.chatId}', " +
                                                        "senderId='${queuedMessage.message.senderId}'"
                                        )
                                        null
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error validating queued message: ${e.message}", e)
                                    null
                                }
                            }
                                    ?: emptyList()

                    // Log if we filtered out any messages
                    val filteredCount = (parsedMessages?.size ?: 0) - validMessages.size
                    if (filteredCount > 0) {
                        Log.w(
                                TAG,
                                "Filtered out $filteredCount invalid/corrupted messages from queue"
                        )
                    }

                    validMessages
                } catch (parseException: Exception) {
                    // Log detailed error information including JSON snippet for debugging
                    val jsonSnippet =
                            if (json.length > 200) {
                                json.substring(0, 200) + "... (truncated)"
                            } else {
                                json
                            }
                    Log.e(
                            TAG,
                            "Error parsing queued messages JSON. JSON length: ${json.length}, " +
                                    "Error: ${parseException.message}, JSON snippet: $jsonSnippet",
                            parseException
                    )

                    // Log to ErrorLogger for comprehensive tracking
                    ErrorLogger.logGsonDeserializationError(
                            jsonString = json,
                            targetClass = QueuedMessage::class.java,
                            exception = parseException
                    )

                    // Return empty list if parsing fails completely
                    emptyList()
                }
            }
        } catch (e: Exception) {
            // Log detailed error information
            Log.e(TAG, "Error getting queued messages from SharedPreferences: ${e.message}", e)
            // Return empty list if parsing fails
            emptyList()
        }
    }

    /** Save queue to SharedPreferences */
    private fun saveQueueInternal(messages: List<QueuedMessage>) {
        try {
            // Filter out null and invalid messages before saving
            val validMessages =
                    messages.filterNotNull().filter { queuedMessage ->
                        // Validate messages before serialization using Message.isValid()
                        val isValid = queuedMessage.message.isValid()
                        if (!isValid) {
                            Log.w(
                                    TAG,
                                    "Skipping invalid message during queue save: " +
                                            "id='${queuedMessage.message.id}', " +
                                            "chatId='${queuedMessage.message.chatId}', " +
                                            "senderId='${queuedMessage.message.senderId}'"
                            )
                        }
                        isValid
                    }

            val json = gson.toJson(validMessages)
            prefs.edit().putString(KEY_QUEUED_MESSAGES, json).apply()

            if (validMessages.size < messages.size) {
                Log.w(
                        TAG,
                        "Filtered out ${messages.size - validMessages.size} invalid messages during save"
                )
            }
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error saving queue - messageCount: ${messages.size}, error: ${e.message}",
                    e
            )
        }
    }
}
