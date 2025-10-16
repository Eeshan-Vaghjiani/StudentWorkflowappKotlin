package com.example.loginandregistration.models

import java.util.*
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for Message data model. Tests message properties and helper methods. */
class MessageTest {

    @Test
    fun `isReadBy returns true when user is in readBy list`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        readBy = listOf("user2", "user3")
                )

        assertTrue(message.isReadBy("user2"))
        assertTrue(message.isReadBy("user3"))
    }

    @Test
    fun `isReadBy returns false when user is not in readBy list`() {
        val message =
                Message(id = "1", chatId = "chat1", senderId = "user1", readBy = listOf("user2"))

        assertFalse(message.isReadBy("user3"))
    }

    @Test
    fun `isFromUser returns true when senderId matches userId`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1")

        assertTrue(message.isFromUser("user1"))
    }

    @Test
    fun `isFromUser returns false when senderId does not match userId`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1")

        assertFalse(message.isFromUser("user2"))
    }

    @Test
    fun `hasImage returns true when imageUrl is not null or empty`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        imageUrl = "https://example.com/image.jpg"
                )

        assertTrue(message.hasImage())
    }

    @Test
    fun `hasImage returns false when imageUrl is null`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1", imageUrl = null)

        assertFalse(message.hasImage())
    }

    @Test
    fun `hasImage returns false when imageUrl is empty`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1", imageUrl = "")

        assertFalse(message.hasImage())
    }

    @Test
    fun `hasDocument returns true when documentUrl is not null or empty`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        documentUrl = "https://example.com/doc.pdf"
                )

        assertTrue(message.hasDocument())
    }

    @Test
    fun `hasDocument returns false when documentUrl is null`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1", documentUrl = null)

        assertFalse(message.hasDocument())
    }

    @Test
    fun `getFormattedFileSize returns empty string when documentSize is null`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1", documentSize = null)

        assertEquals("", message.getFormattedFileSize())
    }

    @Test
    fun `getFormattedFileSize formats bytes correctly`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1", documentSize = 500)

        assertEquals("500 B", message.getFormattedFileSize())
    }

    @Test
    fun `getFormattedFileSize formats kilobytes correctly`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        documentSize = 2048 // 2 KB
                )

        assertEquals("2 KB", message.getFormattedFileSize())
    }

    @Test
    fun `getFormattedFileSize formats megabytes correctly`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        documentSize = 3 * 1024 * 1024 // 3 MB
                )

        assertEquals("3 MB", message.getFormattedFileSize())
    }

    @Test
    fun `getMessageType returns TEXT for text-only message`() {
        val message = Message(id = "1", chatId = "chat1", senderId = "user1", text = "Hello")

        assertEquals(MessageType.TEXT, message.getMessageType())
    }

    @Test
    fun `getMessageType returns IMAGE for message with image`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        imageUrl = "https://example.com/image.jpg"
                )

        assertEquals(MessageType.IMAGE, message.getMessageType())
    }

    @Test
    fun `getMessageType returns DOCUMENT for message with document`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        documentUrl = "https://example.com/doc.pdf"
                )

        assertEquals(MessageType.DOCUMENT, message.getMessageType())
    }

    @Test
    fun `getMessageType prioritizes IMAGE over DOCUMENT`() {
        val message =
                Message(
                        id = "1",
                        chatId = "chat1",
                        senderId = "user1",
                        imageUrl = "https://example.com/image.jpg",
                        documentUrl = "https://example.com/doc.pdf"
                )

        assertEquals(MessageType.IMAGE, message.getMessageType())
    }

    @Test
    fun `message with default values has correct defaults`() {
        val message = Message()

        assertEquals("", message.id)
        assertEquals("", message.chatId)
        assertEquals("", message.senderId)
        assertEquals("", message.text)
        assertNull(message.imageUrl)
        assertNull(message.documentUrl)
        assertTrue(message.readBy.isEmpty())
        assertEquals(MessageStatus.SENDING, message.status)
    }

    @Test
    fun `message status enum has all expected values`() {
        val statuses = MessageStatus.values()

        assertTrue(statuses.contains(MessageStatus.SENDING))
        assertTrue(statuses.contains(MessageStatus.SENT))
        assertTrue(statuses.contains(MessageStatus.DELIVERED))
        assertTrue(statuses.contains(MessageStatus.READ))
        assertTrue(statuses.contains(MessageStatus.FAILED))
    }

    @Test
    fun `message type enum has all expected values`() {
        val types = MessageType.values()

        assertTrue(types.contains(MessageType.TEXT))
        assertTrue(types.contains(MessageType.IMAGE))
        assertTrue(types.contains(MessageType.DOCUMENT))
    }
}
