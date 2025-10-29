package com.example.loginandregistration.validation

import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.models.GroupMember
import com.example.loginandregistration.models.GroupSettings
import com.example.loginandregistration.models.Message
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for FirestoreDataValidator Tests validation logic for tasks, groups, and messages */
class FirestoreDataValidatorTest {

    private lateinit var validator: FirestoreDataValidator

    @Before
    fun setup() {
        validator = FirestoreDataValidator()
    }

    // ========== Task Validation Tests ==========

    @Test
    fun `validateTask with valid task should return success`() {
        val validTask =
                FirebaseTask(
                        id = "task123",
                        title = "Complete homework",
                        description = "Math exercises 1-10",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now(),
                        status = "pending",
                        priority = "medium",
                        category = "personal"
                )

        val result = validator.validateTask(validTask)

        assertTrue("Valid task should pass validation", result.isValid)
        assertTrue("Valid task should have no errors", result.errors.isEmpty())
    }

    @Test
    fun `validateTask with blank title should fail`() {
        val invalidTask =
                FirebaseTask(
                        title = "",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with blank title should fail", result.isValid)
        assertTrue(
                "Should contain title error",
                result.errors.any { it.contains("title", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with blank userId should fail`() {
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with blank userId should fail", result.isValid)
        assertTrue(
                "Should contain user ID error",
                result.errors.any { it.contains("User ID", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with empty assignedTo should fail`() {
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "user123",
                        assignedTo = emptyList(),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with empty assignedTo should fail", result.isValid)
        assertTrue(
                "Should contain assignee error",
                result.errors.any { it.contains("assignee", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with too many assignees should fail`() {
        val tooManyAssignees = (1..51).map { "user$it" }
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "user1",
                        assignedTo = tooManyAssignees,
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with >50 assignees should fail", result.isValid)
        assertTrue(
                "Should contain maximum assignees error",
                result.errors.any { it.contains("50", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with userId not in assignedTo should fail`() {
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "user123",
                        assignedTo = listOf("user456"),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task creator not in assignedTo should fail", result.isValid)
        assertTrue(
                "Should contain creator error",
                result.errors.any { it.contains("creator", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with old timestamp should fail`() {
        val oldTimestamp =
                Timestamp(Date(System.currentTimeMillis() - 10 * 60 * 1000)) // 10 minutes ago
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = oldTimestamp
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with old timestamp should fail", result.isValid)
        assertTrue(
                "Should contain timestamp error",
                result.errors.any { it.contains("timestamp", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with invalid status should fail`() {
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now(),
                        status = "invalid_status"
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with invalid status should fail", result.isValid)
        assertTrue(
                "Should contain status error",
                result.errors.any { it.contains("status", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with invalid priority should fail`() {
        val invalidTask =
                FirebaseTask(
                        title = "Test Task",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now(),
                        priority = "urgent"
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with invalid priority should fail", result.isValid)
        assertTrue(
                "Should contain priority error",
                result.errors.any { it.contains("priority", ignoreCase = true) }
        )
    }

    @Test
    fun `validateTask with title too long should fail`() {
        val longTitle = "a".repeat(501)
        val invalidTask =
                FirebaseTask(
                        title = longTitle,
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with title >500 chars should fail", result.isValid)
        assertTrue(
                "Should contain title length error",
                result.errors.any { it.contains("500", ignoreCase = true) }
        )
    }

    // ========== Group Validation Tests ==========

    @Test
    fun `validateGroup with valid group should return success`() {
        val validGroup =
                FirebaseGroup(
                        id = "group123",
                        name = "Study Group",
                        description = "Math study group",
                        owner = "user123",
                        memberIds = listOf("user123"),
                        members =
                                listOf(
                                        GroupMember(
                                                userId = "user123",
                                                email = "user@example.com",
                                                displayName = "User",
                                                role = "owner",
                                                joinedAt = Timestamp.now()
                                        )
                                ),
                        joinCode = "ABC123",
                        createdAt = Timestamp.now(),
                        settings = GroupSettings(isPublic = false)
                )

        val result = validator.validateGroup(validGroup)

        assertTrue("Valid group should pass validation", result.isValid)
        assertTrue("Valid group should have no errors", result.errors.isEmpty())
    }

    @Test
    fun `validateGroup with blank name should fail`() {
        val invalidGroup =
                FirebaseGroup(name = "", owner = "user123", memberIds = listOf("user123"))

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with blank name should fail", result.isValid)
        assertTrue(
                "Should contain name error",
                result.errors.any { it.contains("name", ignoreCase = true) }
        )
    }

    @Test
    fun `validateGroup with blank owner should fail`() {
        val invalidGroup =
                FirebaseGroup(name = "Test Group", owner = "", memberIds = listOf("user123"))

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with blank owner should fail", result.isValid)
        assertTrue(
                "Should contain owner error",
                result.errors.any { it.contains("owner", ignoreCase = true) }
        )
    }

    @Test
    fun `validateGroup with empty memberIds should fail`() {
        val invalidGroup =
                FirebaseGroup(name = "Test Group", owner = "user123", memberIds = emptyList())

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with empty memberIds should fail", result.isValid)
        assertTrue(
                "Should contain member error",
                result.errors.any { it.contains("member", ignoreCase = true) }
        )
    }

    @Test
    fun `validateGroup with too many members should fail`() {
        val tooManyMembers = (1..101).map { "user$it" }
        val invalidGroup =
                FirebaseGroup(name = "Test Group", owner = "user1", memberIds = tooManyMembers)

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with >100 members should fail", result.isValid)
        assertTrue(
                "Should contain maximum members error",
                result.errors.any { it.contains("100", ignoreCase = true) }
        )
    }

    @Test
    fun `validateGroup with owner not in memberIds should fail`() {
        val invalidGroup =
                FirebaseGroup(name = "Test Group", owner = "user123", memberIds = listOf("user456"))

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group owner not in memberIds should fail", result.isValid)
        assertTrue(
                "Should contain owner error",
                result.errors.any { it.contains("owner", ignoreCase = true) }
        )
    }

    @Test
    fun `validateGroup with invalid join code length should fail`() {
        val invalidGroup =
                FirebaseGroup(
                        name = "Test Group",
                        owner = "user123",
                        memberIds = listOf("user123"),
                        joinCode = "ABC"
                )

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with invalid join code should fail", result.isValid)
        assertTrue(
                "Should contain join code error",
                result.errors.any { it.contains("6 characters", ignoreCase = true) }
        )
    }

    @Test
    fun `validateGroup with non-alphanumeric join code should fail`() {
        val invalidGroup =
                FirebaseGroup(
                        name = "Test Group",
                        owner = "user123",
                        memberIds = listOf("user123"),
                        joinCode = "ABC-12"
                )

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with non-alphanumeric join code should fail", result.isValid)
        assertTrue(
                "Should contain alphanumeric error",
                result.errors.any { it.contains("alphanumeric", ignoreCase = true) }
        )
    }

    // ========== Message Validation Tests ==========

    @Test
    fun `validateMessage with valid text message should return success`() {
        val validMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello, world!",
                        timestamp = Date()
                )

        val result = validator.validateMessage(validMessage)

        assertTrue("Valid message should pass validation", result.isValid)
        assertTrue("Valid message should have no errors", result.errors.isEmpty())
    }

    @Test
    fun `validateMessage with valid image message should return success`() {
        val validMessage =
                Message(
                        id = "msg123",
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        imageUrl =
                                "https://firebasestorage.googleapis.com/v0/b/project/o/image.jpg",
                        timestamp = Date()
                )

        val result = validator.validateMessage(validMessage)

        assertTrue("Valid image message should pass validation", result.isValid)
        assertTrue("Valid image message should have no errors", result.errors.isEmpty())
    }

    @Test
    fun `validateMessage with blank chatId should fail`() {
        val invalidMessage = Message(chatId = "", senderId = "user123", text = "Hello")

        val result = validator.validateMessage(invalidMessage)

        assertFalse("Message with blank chatId should fail", result.isValid)
        assertTrue(
                "Should contain chat ID error",
                result.errors.any { it.contains("Chat ID", ignoreCase = true) }
        )
    }

    @Test
    fun `validateMessage with blank senderId should fail`() {
        val invalidMessage = Message(chatId = "chat123", senderId = "", text = "Hello")

        val result = validator.validateMessage(invalidMessage)

        assertFalse("Message with blank senderId should fail", result.isValid)
        assertTrue(
                "Should contain sender ID error",
                result.errors.any { it.contains("Sender ID", ignoreCase = true) }
        )
    }

    @Test
    fun `validateMessage with no content should fail`() {
        val invalidMessage = Message(chatId = "chat123", senderId = "user123", text = "")

        val result = validator.validateMessage(invalidMessage)

        assertFalse("Message with no content should fail", result.isValid)
        assertTrue(
                "Should contain content error",
                result.errors.any { it.contains("content", ignoreCase = true) }
        )
    }

    @Test
    fun `validateMessage with text too long should fail`() {
        val longText = "a".repeat(10001)
        val invalidMessage = Message(chatId = "chat123", senderId = "user123", text = longText)

        val result = validator.validateMessage(invalidMessage)

        assertFalse("Message with text >10000 chars should fail", result.isValid)
        assertTrue(
                "Should contain text length error",
                result.errors.any { it.contains("10000", ignoreCase = true) }
        )
    }

    @Test
    fun `validateMessage with invalid image URL should fail`() {
        val invalidMessage =
                Message(
                        chatId = "chat123",
                        senderId = "user123",
                        text = "",
                        imageUrl = "http://example.com/image.jpg"
                )

        val result = validator.validateMessage(invalidMessage)

        assertFalse("Message with non-Firebase URL should fail", result.isValid)
        assertTrue(
                "Should contain Firebase Storage error",
                result.errors.any { it.contains("Firebase Storage", ignoreCase = true) }
        )
    }

    @Test
    fun `validateMessage with old timestamp should fail`() {
        val oldDate = Date(System.currentTimeMillis() - 10 * 60 * 1000) // 10 minutes ago
        val invalidMessage =
                Message(
                        chatId = "chat123",
                        senderId = "user123",
                        text = "Hello",
                        timestamp = oldDate
                )

        val result = validator.validateMessage(invalidMessage)

        assertFalse("Message with old timestamp should fail", result.isValid)
        assertTrue(
                "Should contain timestamp error",
                result.errors.any { it.contains("timestamp", ignoreCase = true) }
        )
    }

    // ========== Multiple Errors Tests ==========

    @Test
    fun `validateTask with multiple errors should return all errors`() {
        val invalidTask =
                FirebaseTask(
                        title = "",
                        userId = "",
                        assignedTo = emptyList(),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse("Task with multiple errors should fail", result.isValid)
        assertTrue("Should have multiple errors", result.errors.size >= 3)
    }

    @Test
    fun `validateGroup with multiple errors should return all errors`() {
        val invalidGroup = FirebaseGroup(name = "", owner = "", memberIds = emptyList())

        val result = validator.validateGroup(invalidGroup)

        assertFalse("Group with multiple errors should fail", result.isValid)
        assertTrue("Should have multiple errors", result.errors.size >= 3)
    }

    @Test
    fun `validation errors should be user-friendly`() {
        val invalidTask =
                FirebaseTask(
                        title = "",
                        userId = "user123",
                        assignedTo = listOf("user123"),
                        createdAt = Timestamp.now()
                )

        val result = validator.validateTask(invalidTask)

        assertFalse(result.isValid)
        result.errors.forEach { error ->
            assertFalse(
                    "Error should not contain technical jargon",
                    error.contains("null") || error.contains("Exception")
            )
            assertTrue("Error should be descriptive", error.length > 10)
        }
    }
}
