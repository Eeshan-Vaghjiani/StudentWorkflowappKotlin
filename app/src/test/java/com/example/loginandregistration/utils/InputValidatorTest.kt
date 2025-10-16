package com.example.loginandregistration.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for InputValidator utility class. Tests validation logic for messages, files, emails,
 * and group names.
 */
class InputValidatorTest {

    // Message Text Validation Tests
    @Test
    fun `validateMessageText returns success for valid message`() {
        val result = InputValidator.validateMessageText("Hello, world!")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validateMessageText returns failure for null message`() {
        val result = InputValidator.validateMessageText(null)
        assertFalse(result.isValid)
        assertEquals("Message cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateMessageText returns failure for empty message`() {
        val result = InputValidator.validateMessageText("")
        assertFalse(result.isValid)
        assertEquals("Message cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateMessageText returns failure for blank message`() {
        val result = InputValidator.validateMessageText("   ")
        assertFalse(result.isValid)
        assertEquals("Message cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateMessageText returns failure for message exceeding max length`() {
        val longMessage = "a".repeat(InputValidator.MAX_MESSAGE_LENGTH + 1)
        val result = InputValidator.validateMessageText(longMessage)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("too long"))
    }

    @Test
    fun `validateMessageText returns success for message at max length`() {
        val maxMessage = "a".repeat(InputValidator.MAX_MESSAGE_LENGTH)
        val result = InputValidator.validateMessageText(maxMessage)
        assertTrue(result.isValid)
    }

    // Image Size Validation Tests
    @Test
    fun `validateImageSize returns success for valid size`() {
        val result = InputValidator.validateImageSize(1024 * 1024) // 1MB
        assertTrue(result.isValid)
    }

    @Test
    fun `validateImageSize returns failure for zero size`() {
        val result = InputValidator.validateImageSize(0)
        assertFalse(result.isValid)
        assertEquals("Invalid file size", result.errorMessage)
    }

    @Test
    fun `validateImageSize returns failure for negative size`() {
        val result = InputValidator.validateImageSize(-100)
        assertFalse(result.isValid)
        assertEquals("Invalid file size", result.errorMessage)
    }

    @Test
    fun `validateImageSize returns failure for size exceeding limit`() {
        val result = InputValidator.validateImageSize(InputValidator.MAX_IMAGE_SIZE_BYTES + 1)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("too large"))
    }

    @Test
    fun `validateImageSize returns success for size at limit`() {
        val result = InputValidator.validateImageSize(InputValidator.MAX_IMAGE_SIZE_BYTES.toLong())
        assertTrue(result.isValid)
    }

    // Document Size Validation Tests
    @Test
    fun `validateDocumentSize returns success for valid size`() {
        val result = InputValidator.validateDocumentSize(5 * 1024 * 1024) // 5MB
        assertTrue(result.isValid)
    }

    @Test
    fun `validateDocumentSize returns failure for size exceeding limit`() {
        val result = InputValidator.validateDocumentSize(InputValidator.MAX_DOCUMENT_SIZE_BYTES + 1)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("too large"))
    }

    // Email Validation Tests
    @Test
    fun `validateEmail returns success for valid email`() {
        val validEmails =
                listOf(
                        "test@example.com",
                        "user.name@example.co.uk",
                        "user+tag@example.com",
                        "123@example.com"
                )
        validEmails.forEach { email ->
            val result = InputValidator.validateEmail(email)
            assertTrue("Failed for: $email", result.isValid)
        }
    }

    @Test
    fun `validateEmail returns failure for null email`() {
        val result = InputValidator.validateEmail(null)
        assertFalse(result.isValid)
        assertEquals("Email cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateEmail returns failure for empty email`() {
        val result = InputValidator.validateEmail("")
        assertFalse(result.isValid)
        assertEquals("Email cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateEmail returns failure for invalid email format`() {
        val invalidEmails =
                listOf("notanemail", "@example.com", "user@", "user @example.com", "user@.com")
        invalidEmails.forEach { email ->
            val result = InputValidator.validateEmail(email)
            assertFalse("Should fail for: $email", result.isValid)
            assertEquals("Invalid email format", result.errorMessage)
        }
    }

    // Group Name Validation Tests
    @Test
    fun `validateGroupName returns success for valid name`() {
        val validNames = listOf("Team Alpha", "Project-2024", "Dev_Team", "Group123")
        validNames.forEach { name ->
            val result = InputValidator.validateGroupName(name)
            assertTrue("Failed for: $name", result.isValid)
        }
    }

    @Test
    fun `validateGroupName returns failure for null name`() {
        val result = InputValidator.validateGroupName(null)
        assertFalse(result.isValid)
        assertEquals("Group name cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateGroupName returns failure for empty name`() {
        val result = InputValidator.validateGroupName("")
        assertFalse(result.isValid)
        assertEquals("Group name cannot be empty", result.errorMessage)
    }

    @Test
    fun `validateGroupName returns failure for name too short`() {
        val result = InputValidator.validateGroupName("AB")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("at least"))
    }

    @Test
    fun `validateGroupName returns failure for name too long`() {
        val longName = "a".repeat(InputValidator.MAX_GROUP_NAME_LENGTH + 1)
        val result = InputValidator.validateGroupName(longName)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("too long"))
    }

    @Test
    fun `validateGroupName returns failure for invalid characters`() {
        val invalidNames = listOf("Team@Alpha", "Group#1", "Team!", "Group$")
        invalidNames.forEach { name ->
            val result = InputValidator.validateGroupName(name)
            assertFalse("Should fail for: $name", result.isValid)
            assertTrue(result.errorMessage!!.contains("can only contain"))
        }
    }

    // Group Description Validation Tests
    @Test
    fun `validateGroupDescription returns success for null description`() {
        val result = InputValidator.validateGroupDescription(null)
        assertTrue(result.isValid)
    }

    @Test
    fun `validateGroupDescription returns success for valid description`() {
        val result = InputValidator.validateGroupDescription("This is a valid description")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateGroupDescription returns failure for description too long`() {
        val longDesc = "a".repeat(InputValidator.MAX_GROUP_DESCRIPTION_LENGTH + 1)
        val result = InputValidator.validateGroupDescription(longDesc)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("too long"))
    }

    // Sanitization Tests
    @Test
    fun `sanitizeInput removes HTML characters`() {
        val input = "<script>alert('xss')</script>"
        val sanitized = InputValidator.sanitizeInput(input)
        assertFalse(sanitized.contains("<"))
        assertFalse(sanitized.contains(">"))
    }

    @Test
    fun `sanitizeInput removes quotes`() {
        val input = "Test \"quoted\" and 'single' quotes"
        val sanitized = InputValidator.sanitizeInput(input)
        assertFalse(sanitized.contains("\""))
        assertFalse(sanitized.contains("'"))
    }

    @Test
    fun `sanitizeInput normalizes whitespace`() {
        val input = "Test    multiple   spaces"
        val sanitized = InputValidator.sanitizeInput(input)
        assertEquals("Test multiple spaces", sanitized)
    }

    @Test
    fun `sanitizeInput trims input`() {
        val input = "  Test  "
        val sanitized = InputValidator.sanitizeInput(input)
        assertEquals("Test", sanitized)
    }

    // Combined Validation and Sanitization Tests
    @Test
    fun `validateAndSanitizeMessage returns sanitized text for valid message`() {
        val (result, sanitized) = InputValidator.validateAndSanitizeMessage("  Hello <world>  ")
        assertTrue(result.isValid)
        assertNotNull(sanitized)
        assertEquals("Hello world", sanitized)
    }

    @Test
    fun `validateAndSanitizeMessage returns null for invalid message`() {
        val (result, sanitized) = InputValidator.validateAndSanitizeMessage("")
        assertFalse(result.isValid)
        assertNull(sanitized)
    }

    @Test
    fun `validateAndSanitizeGroupName returns sanitized name for valid group name`() {
        val (result, sanitized) = InputValidator.validateAndSanitizeGroupName("  Team Alpha  ")
        assertTrue(result.isValid)
        assertNotNull(sanitized)
        assertEquals("Team Alpha", sanitized)
    }

    // File Size Formatting Tests
    @Test
    fun `formatFileSize formats bytes correctly`() {
        assertEquals("500 B", InputValidator.formatFileSize(500))
    }

    @Test
    fun `formatFileSize formats kilobytes correctly`() {
        val result = InputValidator.formatFileSize(1536) // 1.5 KB
        assertTrue(result.contains("1.5 KB"))
    }

    @Test
    fun `formatFileSize formats megabytes correctly`() {
        val result = InputValidator.formatFileSize(2 * 1024 * 1024) // 2 MB
        assertTrue(result.contains("2.0 MB"))
    }

    @Test
    fun `formatFileSize formats gigabytes correctly`() {
        val result = InputValidator.formatFileSize(3L * 1024 * 1024 * 1024) // 3 GB
        assertTrue(result.contains("3.0 GB"))
    }
}
