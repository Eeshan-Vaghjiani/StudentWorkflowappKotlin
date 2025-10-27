package com.example.loginandregistration.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Manages offline message queue for reliable message delivery.
 * Stores messages locally when they fail to send and retries them when connection is restored.
 */
class OfflineMessageQueue(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "OfflineMessageQueue"
        private const val PREFS_NAME = "offline_message_queue"
        private const val KEY_QUEUED_MESSAGES = "queued_messages"
    }
    
    /**
     * Add a message to the queue
     */
    fun queueMessage(message: Message) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            
            // Check if message already exists in queue
            val existingIndex = queuedMessages.indexOfFirst { it.id == message.id }
            if (existingIndex != -1) {
                // Update existing message
                queuedMessages[existingIndex] = message
                Log.d(TAG, "Updated message ${message.id} in queue")
            } else {
                // Add new message
                queuedMessages.add(message)
                Log.d(TAG, "Added message ${message.id} to queue")
            }
            
            saveQueue(queuedMessages)
        } catch (e: Exception) {
            Log.e(TAG, "Error queueing message", e)
        }
    }
    
    /**
     * Remove a message from the queue (after successful send)
     */
    fun removeMessage(messageId: String) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            val removed = queuedMessages.removeAll { it.id == messageId }
            
            if (removed) {
                saveQueue(queuedMessages)
                Log.d(TAG, "Removed message $messageId from queue")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing message from queue", e)
        }
    }
    
    /**
     * Mark a message as failed
     */
    fun markMessageAsFailed(messageId: String) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            val messageIndex = queuedMessages.indexOfFirst { it.id == messageId }
            
            if (messageIndex != -1) {
                queuedMessages[messageIndex] = queuedMessages[messageIndex].copy(
                    status = MessageStatus.FAILED
                )
                saveQueue(queuedMessages)
                Log.d(TAG, "Marked message $messageId as FAILED")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking message as failed", e)
        }
    }
    
    /**
     * Update message status
     */
    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            val messageIndex = queuedMessages.indexOfFirst { it.id == messageId }
            
            if (messageIndex != -1) {
                queuedMessages[messageIndex] = queuedMessages[messageIndex].copy(
                    status = status
                )
                saveQueue(queuedMessages)
                Log.d(TAG, "Updated message $messageId status to $status")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating message status", e)
        }
    }
    
    /**
     * Get all queued messages
     */
    fun getQueuedMessages(): List<Message> {
        return try {
            val json = prefs.getString(KEY_QUEUED_MESSAGES, null)
            if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<Message>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting queued messages", e)
            emptyList()
        }
    }
    
    /**
     * Get queued messages for a specific chat
     */
    fun getQueuedMessagesForChat(chatId: String): List<Message> {
        return getQueuedMessages().filter { it.chatId == chatId }
    }
    
    /**
     * Get count of pending messages (SENDING status)
     */
    fun getPendingMessageCount(): Int {
        return getQueuedMessages().count { it.status == MessageStatus.SENDING }
    }
    
    /**
     * Get count of failed messages
     */
    fun getFailedMessageCount(): Int {
        return getQueuedMessages().count { it.status == MessageStatus.FAILED }
    }
    
    /**
     * Clear all messages from queue
     */
    fun clearQueue() {
        try {
            prefs.edit().remove(KEY_QUEUED_MESSAGES).apply()
            Log.d(TAG, "Cleared message queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing queue", e)
        }
    }
    
    /**
     * Clear only failed messages from queue
     */
    fun clearFailedMessages() {
        try {
            val queuedMessages = getQueuedMessages().filter { it.status != MessageStatus.FAILED }
            saveQueue(queuedMessages)
            Log.d(TAG, "Cleared failed messages from queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing failed messages", e)
        }
    }
    
    /**
     * Get messages that need retry (SENDING status and older than retry threshold)
     */
    fun getMessagesNeedingRetry(retryThresholdMs: Long = 30000): List<Message> {
        val currentTime = System.currentTimeMillis()
        return getQueuedMessages().filter { message ->
            message.status == MessageStatus.SENDING &&
            message.timestamp != null &&
            (currentTime - message.timestamp.time) > retryThresholdMs
        }
    }
    
    /**
     * Save queue to SharedPreferences
     */
    private fun saveQueue(messages: List<Message>) {
        try {
            val json = gson.toJson(messages)
            prefs.edit().putString(KEY_QUEUED_MESSAGES, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving queue", e)
        }
    }
}
