package com.example.loginandregistration.models

import org.junit.Assert.*
import org.junit.Test

/** Unit tests for UserInfo data model. Tests user info properties and helper methods. */
class UserInfoTest {

    @Test
    fun `getInitials returns first letters of first and last name`() {
        val userInfo = UserInfo(userId = "user1", displayName = "John Doe")

        assertEquals("JD", userInfo.getInitials())
    }

    @Test
    fun `getInitials returns first two letters for single name`() {
        val userInfo = UserInfo(userId = "user1", displayName = "John")

        assertEquals("JO", userInfo.getInitials())
    }

    @Test
    fun `getInitials handles names with extra spaces`() {
        val userInfo = UserInfo(userId = "user1", displayName = "  John   Doe  ")

        assertEquals("JD", userInfo.getInitials())
    }

    @Test
    fun `getInitials returns uppercase initials`() {
        val userInfo = UserInfo(userId = "user1", displayName = "john doe")

        assertEquals("JD", userInfo.getInitials())
    }

    @Test
    fun `getInitials returns question mark for empty name`() {
        val userInfo = UserInfo(userId = "user1", displayName = "")

        assertEquals("?", userInfo.getInitials())
    }

    @Test
    fun `getInitials handles three or more names`() {
        val userInfo = UserInfo(userId = "user1", displayName = "John Michael Doe")

        assertEquals("JM", userInfo.getInitials())
    }

    @Test
    fun `getInitials handles single character name`() {
        val userInfo = UserInfo(userId = "user1", displayName = "J")

        assertEquals("J", userInfo.getInitials())
    }

    @Test
    fun `userInfo with default values has correct defaults`() {
        val userInfo = UserInfo()

        assertEquals("", userInfo.userId)
        assertEquals("", userInfo.displayName)
        assertEquals("", userInfo.email)
        assertEquals("", userInfo.profileImageUrl)
        assertFalse(userInfo.online)
        assertNull(userInfo.lastSeen)
    }

    @Test
    fun `userInfo can be created with all properties`() {
        val userInfo =
                UserInfo(
                        userId = "user1",
                        displayName = "John Doe",
                        email = "john@example.com",
                        profileImageUrl = "https://example.com/profile.jpg",
                        online = true
                )

        assertEquals("user1", userInfo.userId)
        assertEquals("John Doe", userInfo.displayName)
        assertEquals("john@example.com", userInfo.email)
        assertEquals("https://example.com/profile.jpg", userInfo.profileImageUrl)
        assertTrue(userInfo.online)
    }
}
