package com.example.loginandregistration.models

import java.util.*
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for Chat data model. Tests chat properties and helper methods. */
class ChatTest {

    @Test
    fun `getUnreadCountForUser returns correct count`() {
        val chat = Chat(chatId = "chat1", unreadCount = mapOf("user1" to 5, "user2" to 3))

        assertEquals(5, chat.getUnreadCountForUser("user1"))
        assertEquals(3, chat.getUnreadCountForUser("user2"))
    }

    @Test
    fun `getUnreadCountForUser returns 0 for user not in map`() {
        val chat = Chat(chatId = "chat1", unreadCount = mapOf("user1" to 5))

        assertEquals(0, chat.getUnreadCountForUser("user3"))
    }

    @Test
    fun `getDisplayName returns group name for group chat`() {
        val chat = Chat(chatId = "chat1", type = ChatType.GROUP, groupName = "Team Alpha")

        assertEquals("Team Alpha", chat.getDisplayName("user1"))
    }

    @Test
    fun `getDisplayName returns default for group chat without name`() {
        val chat = Chat(chatId = "chat1", type = ChatType.GROUP, groupName = null)

        assertEquals("Group Chat", chat.getDisplayName("user1"))
    }

    @Test
    fun `getDisplayName returns other participant name for direct chat`() {
        val chat =
                Chat(
                        chatId = "chat1",
                        type = ChatType.DIRECT,
                        participants = listOf("user1", "user2"),
                        participantDetails =
                                mapOf(
                                        "user1" to
                                                UserInfo(userId = "user1", displayName = "Alice"),
                                        "user2" to UserInfo(userId = "user2", displayName = "Bob")
                                )
                )

        assertEquals("Bob", chat.getDisplayName("user1"))
        assertEquals("Alice", chat.getDisplayName("user2"))
    }

    @Test
    fun `getDisplayName returns Unknown User when participant not found`() {
        val chat =
                Chat(
                        chatId = "chat1",
                        type = ChatType.DIRECT,
                        participants = listOf("user1", "user2"),
                        participantDetails = emptyMap()
                )

        assertEquals("Unknown User", chat.getDisplayName("user1"))
    }

    @Test
    fun `getDisplayImageUrl returns empty string for group chat`() {
        val chat = Chat(chatId = "chat1", type = ChatType.GROUP)

        assertEquals("", chat.getDisplayImageUrl("user1"))
    }

    @Test
    fun `getDisplayImageUrl returns other participant image for direct chat`() {
        val chat =
                Chat(
                        chatId = "chat1",
                        type = ChatType.DIRECT,
                        participants = listOf("user1", "user2"),
                        participantDetails =
                                mapOf(
                                        "user1" to
                                                UserInfo(
                                                        userId = "user1",
                                                        profileImageUrl = "image1.jpg"
                                                ),
                                        "user2" to
                                                UserInfo(
                                                        userId = "user2",
                                                        profileImageUrl = "image2.jpg"
                                                )
                                )
                )

        assertEquals("image2.jpg", chat.getDisplayImageUrl("user1"))
        assertEquals("image1.jpg", chat.getDisplayImageUrl("user2"))
    }

    @Test
    fun `isParticipant returns true when user is in participants list`() {
        val chat = Chat(chatId = "chat1", participants = listOf("user1", "user2", "user3"))

        assertTrue(chat.isParticipant("user1"))
        assertTrue(chat.isParticipant("user2"))
        assertTrue(chat.isParticipant("user3"))
    }

    @Test
    fun `isParticipant returns false when user is not in participants list`() {
        val chat = Chat(chatId = "chat1", participants = listOf("user1", "user2"))

        assertFalse(chat.isParticipant("user3"))
    }

    @Test
    fun `chat with default values has correct defaults`() {
        val chat = Chat()

        assertEquals("", chat.chatId)
        assertEquals(ChatType.DIRECT, chat.type)
        assertTrue(chat.participants.isEmpty())
        assertTrue(chat.participantDetails.isEmpty())
        assertEquals("", chat.lastMessage)
        assertNull(chat.lastMessageTime)
        assertTrue(chat.unreadCount.isEmpty())
    }

    @Test
    fun `chat type enum has expected values`() {
        val types = ChatType.values()

        assertTrue(types.contains(ChatType.DIRECT))
        assertTrue(types.contains(ChatType.GROUP))
        assertEquals(2, types.size)
    }
}
