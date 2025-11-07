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

    // Null safety and validation tests

    @Test
    fun `message creation with valid data succeeds`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello, world!",
                        timestamp = Date()
                )

        assertEquals("msg123", message.id)
        assertEquals("chat123", message.chatId)
        assertEquals("user123", message.senderId)
        assertEquals("Hello, world!", message.text)
    }

    @Test
    fun `message creation with blank id succeeds but isValid returns false`() {
        val message = Message(id = "", chatId = "chat123", senderId = "user123", text = "Hello")
        assertFalse(message.isValid())
    }

    @Test
    fun `message creation with blank chatId succeeds but isValid returns false`() {
        val message = Message(id = "msg123", chatId = "", senderId = "user123", text = "Hello")
        assertFalse(message.isValid())
    }

    @Test
    fun `message creation with blank senderId succeeds but isValid returns false`() {
        val message = Message(id = "msg123", chatId = "chat123", senderId = "", text = "Hello")
        assertFalse(message.isValid())
    }
    
    @Test
    fun `isValid returns true for valid message`() {
        val message = Message(id = "msg123", chatId = "chat123", senderId = "user123", text = "Hello")
        assertTrue(message.isValid())
    }
    
    @Test
    fun `validate throws exception for invalid message`() {
        val message = Message(id = "", chatId = "chat123", senderId = "user123", text = "Hello")
        try {
            message.validate()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid message") == true)
        }
    }
    
    @Test
    fun `validate succeeds for valid message`() {
        val message = Message(id = "msg123", chatId = "chat123", senderId = "user123", text = "Hello")
        // Should not throw exception
        message.validate()
    }

    @Test
    fun `fromFirestore with valid document returns message`() {
        // Create a mock DocumentSnapshot
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderName")).thenReturn("John Doe")
        org.mockito.Mockito.`when`(mockDoc.getString("senderImageUrl")).thenReturn("")
        org.mockito.Mockito.`when`(mockDoc.getString("text")).thenReturn("Hello")
        org.mockito.Mockito.`when`(mockDoc.getDate("timestamp")).thenReturn(Date())
        org.mockito.Mockito.`when`(mockDoc.get("readBy")).thenReturn(emptyList<String>())
        org.mockito.Mockito.`when`(mockDoc.getString("status")).thenReturn("SENT")

        val message = Message.fromFirestore(mockDoc)

        assertNotNull(message)
        assertEquals("msg123", message?.id)
        assertEquals("chat123", message?.chatId)
        assertEquals("user123", message?.senderId)
        assertEquals("Hello", message?.text)
    }

    @Test
    fun `fromFirestore with missing chatId returns null`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn(null)
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")

        val message = Message.fromFirestore(mockDoc)

        assertNull(message)
    }

    @Test
    fun `fromFirestore with missing senderId returns null`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn(null)

        val message = Message.fromFirestore(mockDoc)

        assertNull(message)
    }

    @Test
    fun `fromFirestore with blank chatId returns null`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")

        val message = Message.fromFirestore(mockDoc)

        assertNull(message)
    }

    @Test
    fun `fromFirestore with blank senderId returns null`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("")

        val message = Message.fromFirestore(mockDoc)

        assertNull(message)
    }

    @Test
    fun `fromFirestore with invalid data types returns null`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")
        // Simulate an exception during parsing
        org.mockito.Mockito.`when`(mockDoc.getString("text"))
                .thenThrow(RuntimeException("Parse error"))

        val message = Message.fromFirestore(mockDoc)

        assertNull(message)
    }

    @Test
    fun `fromFirestore uses default values for optional fields`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderName")).thenReturn(null)
        org.mockito.Mockito.`when`(mockDoc.getString("text")).thenReturn(null)
        org.mockito.Mockito.`when`(mockDoc.getDate("timestamp")).thenReturn(null)
        org.mockito.Mockito.`when`(mockDoc.get("readBy")).thenReturn(null)
        org.mockito.Mockito.`when`(mockDoc.getString("status")).thenReturn(null)

        val message = Message.fromFirestore(mockDoc)

        assertNotNull(message)
        assertEquals("", message?.senderName)
        assertEquals("", message?.text)
        assertNotNull(message?.timestamp)
        assertTrue(message?.readBy?.isEmpty() == true)
        assertEquals(MessageStatus.SENT, message?.status)
    }

    @Test
    fun `fromFirestore handles invalid status enum gracefully`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")
        org.mockito.Mockito.`when`(mockDoc.getString("text")).thenReturn("Hello")
        org.mockito.Mockito.`when`(mockDoc.getDate("timestamp")).thenReturn(Date())
        org.mockito.Mockito.`when`(mockDoc.get("readBy")).thenReturn(emptyList<String>())
        org.mockito.Mockito.`when`(mockDoc.getString("status")).thenReturn("INVALID_STATUS")

        val message = Message.fromFirestore(mockDoc)

        assertNotNull(message)
        // Should default to SENT when status is invalid
        assertEquals(MessageStatus.SENT, message?.status)
    }

    @Test
    fun `fromFirestore handles invalid type enum gracefully`() {
        val mockDoc =
                org.mockito.Mockito.mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

        org.mockito.Mockito.`when`(mockDoc.id).thenReturn("msg123")
        org.mockito.Mockito.`when`(mockDoc.getString("chatId")).thenReturn("chat123")
        org.mockito.Mockito.`when`(mockDoc.getString("senderId")).thenReturn("user123")
        org.mockito.Mockito.`when`(mockDoc.getString("text")).thenReturn("Hello")
        org.mockito.Mockito.`when`(mockDoc.getDate("timestamp")).thenReturn(Date())
        org.mockito.Mockito.`when`(mockDoc.get("readBy")).thenReturn(emptyList<String>())
        org.mockito.Mockito.`when`(mockDoc.getString("type")).thenReturn("INVALID_TYPE")

        val message = Message.fromFirestore(mockDoc)

        assertNotNull(message)
        // Should default to TEXT when type is invalid
        assertEquals(MessageType.TEXT, message?.type)
    }
}
