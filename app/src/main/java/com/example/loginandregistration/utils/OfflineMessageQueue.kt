package com.example.loginandregistration.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import java.util.Date
import org.json.JSONArray
import org.json.JSONObject

/**
 * Helper class to manage offline message queue Stores messages locally when offline and sends them
 * when connection is restored
 */
class OfflineMessageQueue(context: Context) {

    companion object {
        private const val TAG = "OfflineMessageQueue"
        private const val PREFS_NAME = "offline_message_queue"
        private const val KEY_QUEUED_MESSAGES = "queued_messages"
    }

    private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Queue a message for sending when online */
    fun queueMessage(message: Message) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()

            // Update message status to SENDING
            val queuedMessage = message.copy(status = MessageStatus.SENDING)
            queuedMessages.add(queuedMessage)

            saveQueuedMessages(queuedMessages)
            Log.d(TAG, "Message queued: ${message.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error queueing message", e)
        }
    }

    /** Get all queued messages */
    fun getQueuedMessages(): List<Message> {
        return try {
            val json = prefs.getString(KEY_QUEUED_MESSAGES, null)
            if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                val jsonArray = JSONArray(json)
                val messages = mutableListOf<Message>()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    messages.add(messageFromJson(jsonObject))
                }
                messages
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting queued messages", e)
            emptyList()
        }
    }

    /** Remove a message from the queue */
    fun removeMessage(messageId: String) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            queuedMessages.removeAll { it.id == messageId }
            saveQueuedMessages(queuedMessages)
            Log.d(TAG, "Message removed from queue: $messageId")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing message from queue", e)
        }
    }

    /** Mark a message as failed */
    fun markMessageAsFailed(messageId: String) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            val index = queuedMessages.indexOfFirst { it.id == messageId }

            if (index != -1) {
                val failedMessage = queuedMessages[index].copy(status = MessageStatus.FAILED)
                queuedMessages[index] = failedMessage
                saveQueuedMessages(queuedMessages)
                Log.d(TAG, "Message marked as failed: $messageId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking message as failed", e)
        }
    }

    /** Update message status */
    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        try {
            val queuedMessages = getQueuedMessages().toMutableList()
            val index = queuedMessages.indexOfFirst { it.id == messageId }

            if (index != -1) {
                val updatedMessage = queuedMessages[index].copy(status = status)
                queuedMessages[index] = updatedMessage
                saveQueuedMessages(queuedMessages)
                Log.d(TAG, "Message status updated: $messageId -> $status")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating message status", e)
        }
    }

    /** Clear all queued messages */
    fun clearQueue() {
        try {
            prefs.edit().remove(KEY_QUEUED_MESSAGES).apply()
            Log.d(TAG, "Queue cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing queue", e)
        }
    }

    /** Get count of queued messages */
    fun getQueuedMessageCount(): Int {
        return getQueuedMessages().size
    }

    /** Check if a message is queued */
    fun isMessageQueued(messageId: String): Boolean {
        return getQueuedMessages().any { it.id == messageId }
    }

    /** Get queued messages for a specific chat */
    fun getQueuedMessagesForChat(chatId: String): List<Message> {
        return getQueuedMessages().filter { it.chatId == chatId }
    }

    private fun saveQueuedMessages(messages: List<Message>) {
        try {
            val jsonArray = JSONArray()
            messages.forEach { message -> jsonArray.put(messageToJson(message)) }
            prefs.edit().putString(KEY_QUEUED_MESSAGES, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving queued messages", e)
        }
    }

    private fun messageToJson(message: Message): JSONObject {
        return JSONObject().apply {
            put("id", message.id)
            put("chatId", message.chatId)
            put("senderId", message.senderId)
            put("senderName", message.senderName)
            put("senderImageUrl", message.senderImageUrl)
            put("text", message.text)
            put("timestamp", message.timestamp?.time ?: System.currentTimeMillis())
            put("status", message.status.name)
        }
    }

    private fun messageFromJson(json: JSONObject): Message {
        return Message(
                id = json.getString("id"),
                chatId = json.getString("chatId"),
                senderId = json.getString("senderId"),
                senderName = json.getString("senderName"),
                senderImageUrl = json.optString("senderImageUrl", ""),
                text = json.getString("text"),
                timestamp = Date(json.getLong("timestamp")),
                status = MessageStatus.valueOf(json.getString("status"))
        )
    }
}
