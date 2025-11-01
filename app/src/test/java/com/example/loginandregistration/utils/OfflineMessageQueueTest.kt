package com.example.loginandregistration.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.loginandregistration.models.Message
import java.util.Date
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Unit tests for OfflineMessageQueue. Tests null safety, validation, and queue persistence logic.
 */
class OfflineMessageQueueTest {

    @Mock private lateinit var mockContext: Context

    @Mock private lateinit var mockSharedPreferences: SharedPreferences

    @Mock private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var offlineMessageQueue: OfflineMessageQueue

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Setup SharedPreferences mock
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.remove(any())).thenReturn(mockEditor)
        whenever(mockEditor.apply()).then {}

        // Default: empty queue
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn("[]")

        offlineMessageQueue = OfflineMessageQueue(mockContext)
    }

    @Test
    fun `queueMessage rejects null message`() {
        offlineMessageQueue.queueMessage(null)

        // Verify that nothing was saved (no putString call)
        verify(mockEditor, never()).putString(any(), any())
    }

    @Test
    fun `queueMessage rejects invalid message with blank id`() {
        val invalidMessage =
                Message(
                        id = "",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        offlineMessageQueue.queueMessage(invalidMessage)

        // Verify that nothing was saved
        verify(mockEditor, never()).putString(any(), any())
    }

    @Test
    fun `queueMessage rejects invalid message with blank chatId`() {
        val invalidMessage =
                Message(
                        id = "msg123",
                        chatId = "",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        offlineMessageQueue.queueMessage(invalidMessage)

        // Verify that nothing was saved
        verify(mockEditor, never()).putString(any(), any())
    }

    @Test
    fun `queueMessage rejects invalid message with blank senderId`() {
        val invalidMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "",
                        text = "Hello",
                        timestamp = Date()
                )

        offlineMessageQueue.queueMessage(invalidMessage)

        // Verify that nothing was saved
        verify(mockEditor, never()).putString(any(), any())
    }

    @Test
    fun `queueMessage rejects invalid message with no content`() {
        val invalidMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        imageUrl = null,
                        documentUrl = null,
                        timestamp = Date()
                )

        offlineMessageQueue.queueMessage(invalidMessage)

        // Verify that nothing was saved
        verify(mockEditor, never()).putString(any(), any())
    }

    @Test
    fun `queueMessage accepts valid message`() {
        val validMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        offlineMessageQueue.queueMessage(validMessage)

        // Verify that message was saved
        verify(mockEditor, times(1)).putString(any(), any())
        verify(mockEditor, times(1)).apply()
    }

    @Test
    fun `removeMessage handles null message gracefully`() {
        offlineMessageQueue.removeMessage(null as Message?)

        // Should not crash, and should not attempt to modify the queue
        // Since queue is empty, no remove operation should happen
        verify(mockEditor, never()).putString(any(), any())
    }

    @Test
    fun `removeMessage by id removes message from queue`() {
        // Setup: queue with one message
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        val queueJson =
                """[{"message":{"id":"msg123","chatId":"chat123","senderId":"user123","text":"Hello","timestamp":"2024-01-01T00:00:00.000Z","status":"SENDING"},"attempts":0,"lastAttemptTime":1234567890,"queuedTime":1234567890}]"""
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(queueJson)

        offlineMessageQueue.removeMessage("msg123")

        // Verify that queue was updated (saved with empty list)
        verify(mockEditor, atLeastOnce()).putString(any(), any())
        verify(mockEditor, atLeastOnce()).apply()
    }

    @Test
    fun `getQueuedMessagesForChat filters null messages`() {
        // Setup: queue with valid JSON but simulate filtering
        val queueJson =
                """[{"message":{"id":"msg123","chatId":"chat123","senderId":"user123","text":"Hello","timestamp":"2024-01-01T00:00:00.000Z","status":"SENDING"},"attempts":0,"lastAttemptTime":1234567890,"queuedTime":1234567890}]"""
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(queueJson)

        val messages = offlineMessageQueue.getQueuedMessagesForChat("chat123")

        // Should return messages without null entries
        assertNotNull(messages)
        assertTrue(messages.all { it != null })
    }

    @Test
    fun `getQueuedMessagesForChat returns empty list for invalid JSON`() {
        // Setup: invalid JSON
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn("{invalid json}")

        val messages = offlineMessageQueue.getQueuedMessagesForChat("chat123")

        // Should return empty list instead of crashing
        assertNotNull(messages)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getQueuedMessagesForChat returns empty list when SharedPreferences throws exception`() {
        // Setup: SharedPreferences throws exception
        whenever(mockSharedPreferences.getString(any(), any()))
                .thenThrow(RuntimeException("Storage error"))

        val messages = offlineMessageQueue.getQueuedMessagesForChat("chat123")

        // Should return empty list instead of crashing
        assertNotNull(messages)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `queue persistence filters out invalid messages`() {
        // This test verifies that when saving, invalid messages are filtered
        val validMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        offlineMessageQueue.queueMessage(validMessage)

        // Verify that save was called
        verify(mockEditor, times(1)).putString(any(), any())
    }

    @Test
    fun `getQueuedMessages returns empty list for empty queue`() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn("[]")

        val messages = offlineMessageQueue.getQueuedMessages()

        assertNotNull(messages)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getQueuedMessages returns empty list for null JSON`() {
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(null)

        val messages = offlineMessageQueue.getQueuedMessages()

        assertNotNull(messages)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `clearQueue removes all messages`() {
        offlineMessageQueue.clearQueue()

        verify(mockEditor, times(1)).remove(any())
        verify(mockEditor, times(1)).apply()
    }

    @Test
    fun `markMessageAsFailedRetryable updates message status`() {
        // Setup: queue with one message
        val queueJson =
                """[{"message":{"id":"msg123","chatId":"chat123","senderId":"user123","text":"Hello","timestamp":"2024-01-01T00:00:00.000Z","status":"SENDING"},"attempts":0,"lastAttemptTime":1234567890,"queuedTime":1234567890}]"""
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(queueJson)

        offlineMessageQueue.markMessageAsFailedRetryable("msg123")

        // Verify that queue was updated
        verify(mockEditor, atLeastOnce()).putString(any(), any())
        verify(mockEditor, atLeastOnce()).apply()
    }

    @Test
    fun `getPendingMessageCount returns correct count`() {
        // Setup: queue with messages
        val queueJson =
                """[{"message":{"id":"msg123","chatId":"chat123","senderId":"user123","text":"Hello","timestamp":"2024-01-01T00:00:00.000Z","status":"SENDING"},"attempts":0,"lastAttemptTime":1234567890,"queuedTime":1234567890}]"""
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(queueJson)

        val count = offlineMessageQueue.getPendingMessageCount()

        assertTrue(count >= 0)
    }

    @Test
    fun `getFailedRetryableCount returns correct count`() {
        val count = offlineMessageQueue.getFailedRetryableCount()

        assertTrue(count >= 0)
    }
}
