package com.example.loginandregistration.models

import com.google.gson.Gson
import java.util.*
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for Message GSON serialization/deserialization. */
class MessageGsonTest {

    private val gson = Gson()

    @Test
    fun `GSON deserializes message with empty fields without throwing exception`() {
        val json = """{"id":"","chatId":"","senderId":""}"""

        // Should not throw exception during deserialization
        val message = gson.fromJson(json, Message::class.java)

        assertNotNull(message)
        assertEquals("", message.id)
        assertEquals("", message.chatId)
        assertEquals("", message.senderId)
    }

    @Test
    fun `GSON deserializes message with valid fields successfully`() {
        val json = """{"id":"msg123","chatId":"chat123","senderId":"user123","text":"Hello"}"""

        val message = gson.fromJson(json, Message::class.java)

        assertNotNull(message)
        assertEquals("msg123", message.id)
        assertEquals("chat123", message.chatId)
        assertEquals("user123", message.senderId)
        assertEquals("Hello", message.text)
    }

    @Test
    fun `isValid returns false for message deserialized with empty required fields`() {
        val json = """{"id":"","chatId":"","senderId":""}"""
        val message = gson.fromJson(json, Message::class.java)

        assertFalse(message.isValid())
    }

    @Test
    fun `isValid returns true for message deserialized with valid required fields`() {
        val json = """{"id":"msg123","chatId":"chat123","senderId":"user123"}"""
        val message = gson.fromJson(json, Message::class.java)

        assertTrue(message.isValid())
    }

    @Test
    fun `validate throws exception for invalid deserialized message`() {
        val json = """{"id":"","chatId":"chat123","senderId":"user123"}"""
        val message = gson.fromJson(json, Message::class.java)

        try {
            message.validate()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid message") == true)
        }
    }

    @Test
    fun `validate succeeds for valid deserialized message`() {
        val json = """{"id":"msg123","chatId":"chat123","senderId":"user123"}"""
        val message = gson.fromJson(json, Message::class.java)

        // Should not throw exception
        message.validate()
    }

    @Test
    fun `GSON serializes and deserializes message with all fields`() {
        val originalMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        senderName = "John Doe",
                        senderImageUrl = "https://example.com/avatar.jpg",
                        text = "Hello, world!",
                        imageUrl = "https://example.com/image.jpg",
                        timestamp = Date(1234567890000),
                        readBy = listOf("user456", "user789"),
                        status = MessageStatus.SENT
                )

        val json = gson.toJson(originalMessage)
        val deserializedMessage = gson.fromJson(json, Message::class.java)

        assertEquals(originalMessage.id, deserializedMessage.id)
        assertEquals(originalMessage.chatId, deserializedMessage.chatId)
        assertEquals(originalMessage.senderId, deserializedMessage.senderId)
        assertEquals(originalMessage.senderName, deserializedMessage.senderName)
        assertEquals(originalMessage.text, deserializedMessage.text)
        assertEquals(originalMessage.imageUrl, deserializedMessage.imageUrl)
        assertEquals(originalMessage.readBy, deserializedMessage.readBy)
        assertEquals(originalMessage.status, deserializedMessage.status)
    }

    @Test
    fun `GSON deserializes message with missing optional fields using defaults`() {
        val json = """{"id":"msg123","chatId":"chat123","senderId":"user123"}"""
        val message = gson.fromJson(json, Message::class.java)

        assertEquals("msg123", message.id)
        assertEquals("chat123", message.chatId)
        assertEquals("user123", message.senderId)
        assertEquals("", message.senderName)
        assertEquals("", message.text)
        assertNull(message.imageUrl)
        assertTrue(message.readBy.isEmpty())
        assertEquals(MessageStatus.SENDING, message.status)
    }

    @Test
    fun `GSON deserializes message with null values for optional fields`() {
        val json =
                """{"id":"msg123","chatId":"chat123","senderId":"user123","imageUrl":null,"documentUrl":null}"""
        val message = gson.fromJson(json, Message::class.java)

        assertNull(message.imageUrl)
        assertNull(message.documentUrl)
    }

    @Test
    fun `GSON deserializes message with status enum correctly`() {
        val statuses =
                listOf(
                        MessageStatus.SENDING,
                        MessageStatus.SENT,
                        MessageStatus.DELIVERED,
                        MessageStatus.READ,
                        MessageStatus.FAILED,
                        MessageStatus.FAILED_RETRYABLE,
                        MessageStatus.FAILED_PERMANENT
                )

        for (status in statuses) {
            val json =
                    """{"id":"msg123","chatId":"chat123","senderId":"user123","status":"$status"}"""
            val message = gson.fromJson(json, Message::class.java)

            assertEquals(status, message.status)
        }
    }

    @Test
    fun `GSON deserializes message with type enum correctly`() {
        val types =
                listOf(
                        MessageType.TEXT,
                        MessageType.IMAGE,
                        MessageType.DOCUMENT,
                        MessageType.AUDIO,
                        MessageType.VIDEO
                )

        for (type in types) {
            val json = """{"id":"msg123","chatId":"chat123","senderId":"user123","type":"$type"}"""
            val message = gson.fromJson(json, Message::class.java)

            assertEquals(type, message.type)
        }
    }

    @Test
    fun `GSON deserializes message with readBy array correctly`() {
        val json =
                """{"id":"msg123","chatId":"chat123","senderId":"user123","readBy":["user456","user789"]}"""
        val message = gson.fromJson(json, Message::class.java)

        assertEquals(2, message.readBy.size)
        assertTrue(message.readBy.contains("user456"))
        assertTrue(message.readBy.contains("user789"))
    }

    @Test
    fun `GSON deserializes message with empty readBy array correctly`() {
        val json = """{"id":"msg123","chatId":"chat123","senderId":"user123","readBy":[]}"""
        val message = gson.fromJson(json, Message::class.java)

        assertTrue(message.readBy.isEmpty())
    }

    @Test
    fun `GSON deserializes message with attachment fields correctly`() {
        val json =
                """
            {
                "id":"msg123",
                "chatId":"chat123",
                "senderId":"user123",
                "attachmentUrl":"https://example.com/file.pdf",
                "attachmentFileName":"document.pdf",
                "attachmentFileSize":1024000,
                "attachmentMimeType":"application/pdf",
                "attachmentType":"document"
            }
        """.trimIndent()

        val message = gson.fromJson(json, Message::class.java)

        assertEquals("https://example.com/file.pdf", message.attachmentUrl)
        assertEquals("document.pdf", message.attachmentFileName)
        assertEquals(1024000L, message.attachmentFileSize)
        assertEquals("application/pdf", message.attachmentMimeType)
        assertEquals("document", message.attachmentType)
    }

    @Test
    fun `GSON round-trip preserves message validity`() {
        val originalMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Test message"
                )

        assertTrue(originalMessage.isValid())

        val json = gson.toJson(originalMessage)
        val deserializedMessage = gson.fromJson(json, Message::class.java)

        assertTrue(deserializedMessage.isValid())
    }
}
