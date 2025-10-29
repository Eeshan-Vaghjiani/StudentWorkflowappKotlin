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
    fun queueMessage(message: Message) {
        try {
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
            Log.e(TAG, "Error queueing message", e)
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
            Log.e(TAG, "Error removing message from queue", e)
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
            Log.e(TAG, "Error marking message as failed retryable", e)
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
            Log.e(TAG, "Error marking message as failed permanent", e)
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
            Log.e(TAG, "Error updating message status", e)
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
            Log.e(TAG, "Error incrementing attempts", e)
        }
    }

    /** Get all queued messages (returns Message objects for backward compatibility) */
    fun getQueuedMessages(): List<Message> {
        return getQueuedMessagesInternal().map { it.message }
    }

    /** Get queued messages for a specific chat */
    fun getQueuedMessagesForChat(chatId: String): List<Message> {
        return getQueuedMessagesInternal().filter { it.message.chatId == chatId }.map { it.message }
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
            Log.e(TAG, "Error clearing queue", e)
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
            Log.e(TAG, "Error clearing failed messages", e)
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
            Log.e(TAG, "Error clearing permanently failed messages", e)
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
                val type = object : TypeToken<List<QueuedMessage>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting queued messages", e)
            emptyList()
        }
    }

    /** Save queue to SharedPreferences */
    private fun saveQueueInternal(messages: List<QueuedMessage>) {
        try {
            val json = gson.toJson(messages)
            prefs.edit().putString(KEY_QUEUED_MESSAGES, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving queue", e)
        }
    }
}
