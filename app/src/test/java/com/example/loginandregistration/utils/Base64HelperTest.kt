package com.example.loginandregistration.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Base64Helper utility class. Tests validation and size calculation methods
 * (non-Android dependent methods).
 */
class Base64HelperTest {

    @Test
    fun `validateBase64Size returns true for valid size`() {
        val base64String = "a".repeat(500 * 1024) // 500KB
        assertTrue(Base64Helper.validateBase64Size(base64String))
    }

    @Test
    fun `validateBase64Size returns false for size exceeding limit`() {
        val base64String = "a".repeat(Base64Helper.MAX_BASE64_SIZE_BYTES + 1)
        assertFalse(Base64Helper.validateBase64Size(base64String))
    }

    @Test
    fun `validateBase64Size returns true for size at limit`() {
        val base64String = "a".repeat(Base64Helper.MAX_BASE64_SIZE_BYTES)
        assertTrue(Base64Helper.validateBase64Size(base64String))
    }

    @Test
    fun `validateBase64Size works with custom max size`() {
        val base64String = "a".repeat(100 * 1024) // 100KB
        assertTrue(Base64Helper.validateBase64Size(base64String, 200 * 1024))
        assertFalse(Base64Helper.validateBase64Size(base64String, 50 * 1024))
    }

    @Test
    fun `getBase64Size returns correct size in bytes`() {
        val base64String = "a".repeat(1000)
        assertEquals(1000, Base64Helper.getBase64Size(base64String))
    }

    @Test
    fun `getBase64Size returns 0 for empty string`() {
        assertEquals(0, Base64Helper.getBase64Size(""))
    }

    @Test
    fun `getBase64SizeKB returns correct size in kilobytes`() {
        val base64String = "a".repeat(2048) // 2KB
        assertEquals(2.0, Base64Helper.getBase64SizeKB(base64String), 0.01)
    }

    @Test
    fun `getBase64SizeKB returns fractional KB correctly`() {
        val base64String = "a".repeat(1536) // 1.5KB
        assertEquals(1.5, Base64Helper.getBase64SizeKB(base64String), 0.01)
    }

    @Test
    fun `getBase64SizeMB returns correct size in megabytes`() {
        val base64String = "a".repeat(2 * 1024 * 1024) // 2MB
        assertEquals(2.0, Base64Helper.getBase64SizeMB(base64String), 0.01)
    }

    @Test
    fun `getBase64SizeMB returns fractional MB correctly`() {
        val base64String = "a".repeat((1.5 * 1024 * 1024).toInt()) // 1.5MB
        assertEquals(1.5, Base64Helper.getBase64SizeMB(base64String), 0.01)
    }

    @Test
    fun `MAX_BASE64_SIZE_BYTES constant is 1MB`() {
        assertEquals(1 * 1024 * 1024, Base64Helper.MAX_BASE64_SIZE_BYTES)
    }

    @Test
    fun `MAX_PROFILE_BASE64_SIZE_BYTES constant is 200KB`() {
        assertEquals(200 * 1024, Base64Helper.MAX_PROFILE_BASE64_SIZE_BYTES)
    }

    @Test
    fun `validateBase64Size with profile size limit`() {
        val base64String = "a".repeat(150 * 1024) // 150KB
        assertTrue(
                Base64Helper.validateBase64Size(
                        base64String,
                        Base64Helper.MAX_PROFILE_BASE64_SIZE_BYTES
                )
        )

        val largeString = "a".repeat(250 * 1024) // 250KB
        assertFalse(
                Base64Helper.validateBase64Size(
                        largeString,
                        Base64Helper.MAX_PROFILE_BASE64_SIZE_BYTES
                )
        )
    }
}
