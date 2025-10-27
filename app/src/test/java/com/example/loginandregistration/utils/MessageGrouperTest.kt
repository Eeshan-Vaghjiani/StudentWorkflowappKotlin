package com.example.loginandregistration.utils

import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import java.util.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MessageGrouper utility class. Tests message grouping logic and timestamp
 * formatting.
 */
class MessageGrouperTest {

    private val currentUserId = "user1"
    private val otherUserId = "user2"

    private fun createMessage(
            id: String,
            senderId: String,
            text: String,
            timestampMillis: Long
    ): Message {
        return Message(
                id = id,
                chatId = "chat1",
                senderId = senderId,
                senderName = if (senderId == currentUserId) "Current User" else "Other User",
                senderImageUrl = "",
                text = text,
                timestamp = Date(timestampMillis),
                status = MessageStatus.SENT
        )
    }

    @Test
    fun `groupMessages returns empty list for empty input`() {
        val result = MessageGrouper.groupMessages(emptyList(), currentUserId)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groupMessages adds timestamp header for single message`() {
        val now = System.currentTimeMillis()
        val messages = listOf(createMessage("1", otherUserId, "Hello", now))

        val result = MessageGrouper.groupMessages(messages, currentUserId)

        assertTrue(result.isNotEmpty())
        assertTrue(result.first() is MessageGrouper.MessageItem.TimestampHeader)
    }

    @Test
    fun `groupMessages shows sender info for first message from other user`() {
        val now = System.currentTimeMillis()
        val messages = listOf(createMessage("1", otherUserId, "Hello", now))

        val result = MessageGrouper.groupMessages(messages, currentUserId)

        val messageItem = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>().first()
        assertTrue(messageItem.showSenderInfo)
    }

    @Test
    fun `groupMessages does not show sender info for sent messages`() {
        val now = System.currentTimeMillis()
        val messages = listOf(createMessage("1", currentUserId, "Hello", now))

        val result = MessageGrouper.groupMessages(messages, currentUserId)

        val messageItem = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>().first()
        assertFalse(messageItem.showSenderInfo)
    }

    @Test
    fun `groupMessages groups consecutive messages from same sender within 5 minutes`() {
        val now = System.currentTimeMillis()
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage(
                                "2",
                                otherUserId,
                                "How are you?",
                                now + 60000
                        ), // 1 minute later
                        createMessage(
                                "3",
                                otherUserId,
                                "Are you there?",
                                now + 120000
                        ) // 2 minutes later
                )

        val result = MessageGrouper.groupMessages(messages, currentUserId)
        val messageItems = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>()

        // First message should show sender info
        assertTrue(messageItems[0].showSenderInfo)
        // Second and third should not (same sender, within 5 minutes)
        assertFalse(messageItems[1].showSenderInfo)
        assertFalse(messageItems[2].showSenderInfo)
    }

    @Test
    fun `groupMessages starts new group after 5 minutes`() {
        val now = System.currentTimeMillis()
        val fiveMinutesOneSecond = 5 * 60 * 1000 + 1000
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage("2", otherUserId, "Still there?", now + fiveMinutesOneSecond)
                )

        val result = MessageGrouper.groupMessages(messages, currentUserId)
        val messageItems = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>()

        // Both messages should show sender info (more than 5 minutes apart)
        assertTrue(messageItems[0].showSenderInfo)
        assertTrue(messageItems[1].showSenderInfo)
    }

    @Test
    fun `groupMessages starts new group for different sender`() {
        val now = System.currentTimeMillis()
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage("2", currentUserId, "Hi there", now + 1000),
                        createMessage("3", otherUserId, "How are you?", now + 2000)
                )

        val result = MessageGrouper.groupMessages(messages, currentUserId)
        val messageItems = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>()

        // First and third should show sender info (different senders)
        assertTrue(messageItems[0].showSenderInfo)
        assertFalse(messageItems[1].showSenderInfo) // Current user never shows sender info
        assertTrue(messageItems[2].showSenderInfo)
    }

    @Test
    fun `groupConsecutiveMessages creates correct groups`() {
        val now = System.currentTimeMillis()
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage("2", otherUserId, "How are you?", now + 60000),
                        createMessage("3", currentUserId, "I'm good", now + 120000),
                        createMessage("4", currentUserId, "Thanks", now + 180000)
                )

        val groups = MessageGrouper.groupConsecutiveMessages(messages)

        assertEquals(2, groups.size)
        assertEquals(2, groups[0].messages.size)
        assertEquals(otherUserId, groups[0].senderId)
        assertEquals(2, groups[1].messages.size)
        assertEquals(currentUserId, groups[1].senderId)
    }

    @Test
    fun `groupConsecutiveMessages splits group after 5 minutes`() {
        val now = System.currentTimeMillis()
        val fiveMinutesOneSecond = 5 * 60 * 1000 + 1000
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage("2", otherUserId, "Still there?", now + fiveMinutesOneSecond)
                )

        val groups = MessageGrouper.groupConsecutiveMessages(messages)

        assertEquals(2, groups.size)
    }

    @Test
    fun `formatTime returns correct time format`() {
        val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 14)
                    set(Calendar.MINUTE, 30)
                }

        val formatted = MessageGrouper.formatTime(calendar.timeInMillis)
        assertTrue(formatted.contains("2:30") || formatted.contains("14:30"))
    }

    @Test
    fun `formatDate returns correct date format`() {
        val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2024)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 15)
                }

        val formatted = MessageGrouper.formatDate(calendar.timeInMillis)
        assertTrue(formatted.contains("Jan"))
        assertTrue(formatted.contains("15"))
        assertTrue(formatted.contains("2024"))
    }

    @Test
    fun `formatDateTime returns correct datetime format`() {
        val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2024)
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 15)
                    set(Calendar.HOUR_OF_DAY, 14)
                    set(Calendar.MINUTE, 30)
                }

        val formatted = MessageGrouper.formatDateTime(calendar.timeInMillis)
        assertTrue(formatted.contains("Jan"))
        assertTrue(formatted.contains("15"))
        assertTrue(formatted.contains("2024"))
        assertTrue(formatted.contains("at"))
    }

    @Test
    fun `groupMessages shows timestamp for messages more than 5 minutes apart`() {
        val now = System.currentTimeMillis()
        val fiveMinutesOneSecond = 5 * 60 * 1000 + 1000
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage("2", otherUserId, "Still there?", now + fiveMinutesOneSecond)
                )

        val result = MessageGrouper.groupMessages(messages, currentUserId)
        val messageItems = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>()

        assertTrue(messageItems[0].showTimestamp)
        assertTrue(messageItems[1].showTimestamp)
    }

    @Test
    fun `groupMessages does not show timestamp for messages within 5 minutes`() {
        val now = System.currentTimeMillis()
        val messages =
                listOf(
                        createMessage("1", otherUserId, "Hello", now),
                        createMessage(
                                "2",
                                otherUserId,
                                "How are you?",
                                now + 60000
                        ) // 1 minute later
                )

        val result = MessageGrouper.groupMessages(messages, currentUserId)
        val messageItems = result.filterIsInstance<MessageGrouper.MessageItem.MessageData>()

        assertTrue(messageItems[0].showTimestamp)
        assertFalse(messageItems[1].showTimestamp)
    }
}
