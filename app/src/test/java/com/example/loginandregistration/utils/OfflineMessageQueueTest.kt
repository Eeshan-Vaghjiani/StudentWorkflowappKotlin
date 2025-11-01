package com.example.loginandregistration.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/** Unit tests for OfflineMessageQueue error handling and validation. */
class OfflineMessageQueueTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var queue: OfflineMessageQueue
    private val gson = Gson()

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)

        queue = OfflineMessageQueue(mockContext)
    }

    @Test
    fun `queueMessage validates message before adding to queue`() {
        val validMessage =
                Message(id = "msg123", chatId = "chat123", senderId = "user123", text = "Hello")

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        queue.queueMessage(validMessage)

        // Verify that save was called (message was queued)
        verify(mockEditor).putString(anyString(), anyString())
        verify(mockEditor).apply()
    }

    @Test
    fun `queueMessage rejects invalid message with blank id`() {
        val invalidMessage =
                Message(id = "", chatId = "chat123", senderId = "user123", text = "Hello")

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        queue.queueMessage(invalidMessage)

        // Verify that save was NOT called (message was rejected)
        verify(mockEditor, never()).putString(anyString(), anyString())
    }

    @Test
    fun `queueMessage rejects invalid message with blank chatId`() {
        val invalidMessage =
                Message(id = "msg123", chatId = "", senderId = "user123", text = "Hello")

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        queue.queueMessage(invalidMessage)

        // Verify that save was NOT called (message was rejected)
        verify(mockEditor, never()).putString(anyString(), anyString())
    }

    @Test
    fun `queueMessage rejects invalid message with blank senderId`() {
        val invalidMessage =
                Message(id = "msg123", chatId = "chat123", senderId = "", text = "Hello")

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        queue.queueMessage(invalidMessage)

        // Verify that save was NOT called (message was rejected)
        verify(mockEditor, never()).putString(anyString(), anyString())
    }

    @Test
    fun `queueMessage handles null message gracefully`() {
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        queue.queueMessage(null)

        // Verify that save was NOT called (null message was rejected)
        verify(mockEditor, never()).putString(anyString(), anyString())
    }

    @Test
    fun `getQueuedMessages skips invalid messages during deserialization`() {
        val validMessage =
                QueuedMessage(
                        message =
                                Message(
                                        id = "msg123",
                                        chatId = "chat123",
                                        senderId = "user123",
                                        text = "Valid"
                                )
                )

        val invalidMessage =
                QueuedMessage(
                        message =
                                Message(
                                        id = "",
                                        chatId = "chat123",
                                        senderId = "user123",
                                        text = "Invalid"
                                )
                )

        val queuedMessages = listOf(validMessage, invalidMessage)
        val json = gson.toJson(queuedMessages)

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        val messages = queue.getQueuedMessages()

        // Should only return the valid message
        assertEquals(1, messages.size)
        assertEquals("msg123", messages[0].id)
    }

    @Test
    fun `getQueuedMessages returns empty list when JSON parsing fails`() {
        val invalidJson = "{ invalid json }"

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(invalidJson)

        val messages = queue.getQueuedMessages()

        // Should return empty list instead of crashing
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getQueuedMessages returns empty list when SharedPreferences is empty`() {
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        val messages = queue.getQueuedMessages()

        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getQueuedMessages filters out corrupted messages`() {
        // Create a JSON with one valid and one corrupted message
        val validMessage =
                Message(id = "msg123", chatId = "chat123", senderId = "user123", text = "Valid")

        val queuedValid = QueuedMessage(message = validMessage)
        val json = gson.toJson(listOf(queuedValid))

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        val messages = queue.getQueuedMessages()

        // Should return only valid messages
        assertEquals(1, messages.size)
        assertTrue(messages[0].isValid())
    }

    @Test
    fun `getQueuedMessagesForChat filters by chatId correctly`() {
        val message1 =
                QueuedMessage(
                        message =
                                Message(
                                        id = "msg1",
                                        chatId = "chat123",
                                        senderId = "user123",
                                        text = "Message 1"
                                )
                )

        val message2 =
                QueuedMessage(
                        message =
                                Message(
                                        id = "msg2",
                                        chatId = "chat456",
                                        senderId = "user123",
                                        text = "Message 2"
                                )
                )

        val queuedMessages = listOf(message1, message2)
        val json = gson.toJson(queuedMessages)

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        val messages = queue.getQueuedMessagesForChat("chat123")

        assertEquals(1, messages.size)
        assertEquals("msg1", messages[0].id)
        assertEquals("chat123", messages[0].chatId)
    }

    @Test
    fun `getQueuedMessagesForChat handles errors gracefully`() {
        val invalidJson = "{ invalid json }"

        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(invalidJson)

        val messages = queue.getQueuedMessagesForChat("chat123")

        // Should return empty list instead of crashing
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `removeMessage handles null message gracefully`() {
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(null)

        queue.removeMessage(null as Message?)

        // Should not crash, verify no save was attempted
        verify(mockEditor, never()).putString(anyString(), anyString())
    }

    @Test
    fun `markMessageAsFailedRetryable increments attempt counter`() {
        val message =
                QueuedMessage(
                        message =
                                Message(
                                        id = "msg123",
                                        chatId = "chat123",
                                        senderId = "user123",
                                        text = "Test",
                                        status = MessageStatus.SENDING
                                ),
                        attempts = 0
                )

        val json = gson.toJson(listOf(message))
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        queue.markMessageAsFailedRetryable("msg123")

        // Verify that save was called with updated message
        verify(mockEditor).putString(anyString(), anyString())
        verify(mockEditor).apply()
    }

    @Test
    fun `markMessageAsFailedRetryable marks as permanent after max attempts`() {
        val message =
                QueuedMessage(
                        message =
                                Message(
                                        id = "msg123",
                                        chatId = "chat123",
                                        senderId = "user123",
                                        text = "Test",
                                        status = MessageStatus.FAILED_RETRYABLE
                                ),
                        attempts = 4 // One less than max (5)
                )

        val json = gson.toJson(listOf(message))
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        queue.markMessageAsFailedRetryable("msg123")

        // After this call, attempts should be 5 and status should be FAILED_PERMANENT
        verify(mockEditor).putString(anyString(), anyString())
        verify(mockEditor).apply()
    }

    @Test
    fun `getPendingMessageCount returns correct count`() {
        val messages =
                listOf(
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg1",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.SENDING
                                        )
                        ),
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg2",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.SENDING
                                        )
                        ),
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg3",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_RETRYABLE
                                        )
                        )
                )

        val json = gson.toJson(messages)
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        val count = queue.getPendingMessageCount()

        assertEquals(2, count)
    }

    @Test
    fun `getFailedRetryableCount returns correct count`() {
        val messages =
                listOf(
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg1",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_RETRYABLE
                                        )
                        ),
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg2",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_RETRYABLE
                                        )
                        ),
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg3",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_PERMANENT
                                        )
                        )
                )

        val json = gson.toJson(messages)
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        val count = queue.getFailedRetryableCount()

        assertEquals(2, count)
    }

    @Test
    fun `getFailedPermanentCount returns correct count`() {
        val messages =
                listOf(
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg1",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_PERMANENT
                                        )
                        ),
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg2",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_RETRYABLE
                                        )
                        )
                )

        val json = gson.toJson(messages)
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        val count = queue.getFailedPermanentCount()

        assertEquals(1, count)
    }

    @Test
    fun `clearQueue removes all messages`() {
        queue.clearQueue()

        verify(mockEditor).remove(anyString())
        verify(mockEditor).apply()
    }

    @Test
    fun `clearFailedMessages removes only failed messages`() {
        val messages =
                listOf(
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg1",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.SENDING
                                        )
                        ),
                        QueuedMessage(
                                message =
                                        Message(
                                                id = "msg2",
                                                chatId = "chat123",
                                                senderId = "user123",
                                                status = MessageStatus.FAILED_RETRYABLE
                                        )
                        )
                )

        val json = gson.toJson(messages)
        `when`(mockPrefs.getString(anyString(), isNull())).thenReturn(json)

        queue.clearFailedMessages()

        verify(mockEditor).putString(anyString(), anyString())
        verify(mockEditor).apply()
    }
}
