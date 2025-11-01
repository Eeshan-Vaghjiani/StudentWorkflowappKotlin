package com.example.loginandregistration.utils

import com.example.loginandregistration.models.Message
import java.util.Date
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for MessageValidator utility class. Tests null safety and validation logic. */
class MessageValidatorTest {

    @Test
    fun `validate returns Invalid when message is null`() {
        val result = MessageValidator.validate(null)

        assertTrue(result is MessageValidator.ValidationResult.Invalid)
        val invalid = result as MessageValidator.ValidationResult.Invalid
        assertEquals(1, invalid.errors.size)
        assertEquals("Message is null", invalid.errors[0])
    }

    @Test
    fun `validate returns Invalid when message id is blank`() {
        val message =
                Message(
                        id = "",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Invalid)
        val invalid = result as MessageValidator.ValidationResult.Invalid
        assertTrue(invalid.errors.contains("Message ID is blank"))
    }

    @Test
    fun `validate returns Invalid when chatId is blank`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Invalid)
        val invalid = result as MessageValidator.ValidationResult.Invalid
        assertTrue(invalid.errors.contains("Chat ID is blank"))
    }

    @Test
    fun `validate returns Invalid when senderId is blank`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "",
                        text = "Hello",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Invalid)
        val invalid = result as MessageValidator.ValidationResult.Invalid
        assertTrue(invalid.errors.contains("Sender ID is blank"))
    }

    @Test
    fun `validate returns Invalid when message has no content`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        imageUrl = null,
                        documentUrl = null,
                        audioUrl = null,
                        videoUrl = null,
                        attachmentUrl = null,
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Invalid)
        val invalid = result as MessageValidator.ValidationResult.Invalid
        assertTrue(invalid.errors.contains("Message has no content (text or attachment)"))
    }

    @Test
    fun `validate returns Valid for message with text content`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello, world!",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Valid)
        val valid = result as MessageValidator.ValidationResult.Valid
        assertEquals(message, valid.message)
    }

    @Test
    fun `validate returns Valid for message with image attachment`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        imageUrl = "https://example.com/image.jpg",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate returns Valid for message with document attachment`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        documentUrl = "https://example.com/doc.pdf",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate returns Valid for message with new attachment format`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        attachmentUrl = "https://example.com/file.pdf",
                        attachmentType = "document",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate returns Invalid with multiple errors when multiple fields are blank`() {
        val message = Message(id = "", chatId = "", senderId = "", text = "", timestamp = Date())

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Invalid)
        val invalid = result as MessageValidator.ValidationResult.Invalid
        assertTrue(invalid.errors.size >= 4) // id, chatId, senderId, content
        assertTrue(invalid.errors.contains("Message ID is blank"))
        assertTrue(invalid.errors.contains("Chat ID is blank"))
        assertTrue(invalid.errors.contains("Sender ID is blank"))
        assertTrue(invalid.errors.contains("Message has no content (text or attachment)"))
    }

    @Test
    fun `isValid returns true for valid message`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        assertTrue(MessageValidator.isValid(message))
    }

    @Test
    fun `isValid returns false for null message`() {
        assertFalse(MessageValidator.isValid(null))
    }

    @Test
    fun `isValid returns false for invalid message`() {
        val message =
                Message(
                        id = "",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = Date()
                )

        assertFalse(MessageValidator.isValid(message))
    }

    @Test
    fun `validate accepts message with audio attachment`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        attachmentUrl = "https://example.com/audio.mp3",
                        attachmentType = "audio",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Valid)
    }

    @Test
    fun `validate accepts message with video attachment`() {
        val message =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        attachmentUrl = "https://example.com/video.mp4",
                        attachmentType = "video",
                        timestamp = Date()
                )

        val result = MessageValidator.validate(message)

        assertTrue(result is MessageValidator.ValidationResult.Valid)
    }
}
