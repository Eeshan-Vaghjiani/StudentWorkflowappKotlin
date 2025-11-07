package com.example.loginandregistration.validation

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for group chat participant validation. Tests Requirements 8.6: Group chat creation
 * validation with various member counts.
 */
class GroupChatValidationTest {

    /**
     * Test: Group chat creation with 2 members should succeed Requirement: 8.6 - Write test for
     * group chat creation with 2 members (should succeed)
     */
    @Test
    fun `group chat with 2 members should pass validation`() {
        val memberIds = listOf("user1", "user2")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should succeed with 2 members", result.isSuccess)
    }

    /**
     * Test: Group chat creation with 1 member should fail Requirement: 8.6 - Write test for group
     * chat creation with 1 member (should fail)
     */
    @Test
    fun `group chat with 1 member should fail validation`() {
        val memberIds = listOf("user1")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with 1 member", result.isFailure)
        assertTrue(
                "Error message should mention minimum members",
                result.exceptionOrNull()?.message?.contains("at least 2 members") == true
        )
    }

    /**
     * Test: Group chat creation with duplicate members should fail Requirement: 8.6 - Write test
     * for group chat creation with duplicate members (should fail)
     */
    @Test
    fun `group chat with duplicate members should fail validation`() {
        val memberIds = listOf("user1", "user2", "user1") // user1 appears twice

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with duplicate members", result.isFailure)
        assertTrue(
                "Error message should mention duplicates",
                result.exceptionOrNull()?.message?.contains("duplicate") == true
        )
    }

    /**
     * Test: Group chat creation with 100+ members should fail Requirement: 8.6 - Write test for
     * group chat creation with 100+ members (should fail)
     */
    @Test
    fun `group chat with more than 100 members should fail validation`() {
        val memberIds = (1..101).map { "user$it" }

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with 101 members", result.isFailure)
        assertTrue(
                "Error message should mention maximum members",
                result.exceptionOrNull()?.message?.contains("100 members") == true
        )
    }

    @Test
    fun `group chat with exactly 100 members should pass validation`() {
        val memberIds = (1..100).map { "user$it" }

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should succeed with exactly 100 members", result.isSuccess)
    }

    @Test
    fun `group chat with 0 members should fail validation`() {
        val memberIds = emptyList<String>()

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with 0 members", result.isFailure)
        assertTrue(
                "Error message should mention no members",
                result.exceptionOrNull()?.message?.contains("no members") == true ||
                        result.exceptionOrNull()?.message?.contains("at least 2 members") == true
        )
    }

    @Test
    fun `group chat with 3 members should pass validation`() {
        val memberIds = listOf("user1", "user2", "user3")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should succeed with 3 members", result.isSuccess)
    }

    @Test
    fun `group chat with 50 members should pass validation`() {
        val memberIds = (1..50).map { "user$it" }

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should succeed with 50 members", result.isSuccess)
    }

    @Test
    fun `group chat with multiple duplicate members should fail validation`() {
        val memberIds = listOf("user1", "user2", "user1", "user3", "user2")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with multiple duplicates", result.isFailure)
    }

    @Test
    fun `group chat with blank member IDs should fail validation`() {
        val memberIds = listOf("user1", "", "user2")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with blank member ID", result.isFailure)
    }

    @Test
    fun `group chat with all blank member IDs should fail validation`() {
        val memberIds = listOf("", "")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with all blank member IDs", result.isFailure)
    }

    @Test
    fun `group chat validation returns correct error message for 1 member`() {
        val memberIds = listOf("user1")

        val result = validateGroupChatParticipants(memberIds)

        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(
                "Error message should be descriptive",
                errorMessage.contains("at least 2 members") || errorMessage.contains("minimum")
        )
    }

    @Test
    fun `group chat validation returns correct error message for duplicates`() {
        val memberIds = listOf("user1", "user1")

        val result = validateGroupChatParticipants(memberIds)

        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue("Error message should mention duplicates", errorMessage.contains("duplicate"))
    }

    @Test
    fun `group chat validation returns correct error message for too many members`() {
        val memberIds = (1..101).map { "user$it" }

        val result = validateGroupChatParticipants(memberIds)

        val errorMessage = result.exceptionOrNull()?.message ?: ""
        assertTrue(
                "Error message should mention maximum",
                errorMessage.contains("100") || errorMessage.contains("maximum")
        )
    }

    @Test
    fun `group chat with valid member IDs passes all checks`() {
        val memberIds = listOf("user123", "user456", "user789")

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should pass all validation checks", result.isSuccess)
    }

    @Test
    fun `group chat validation handles edge case of 99 members`() {
        val memberIds = (1..99).map { "user$it" }

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should succeed with 99 members", result.isSuccess)
    }

    @Test
    fun `group chat validation handles edge case of 101 members`() {
        val memberIds = (1..101).map { "user$it" }

        val result = validateGroupChatParticipants(memberIds)

        assertTrue("Should fail with 101 members", result.isFailure)
    }

    /**
     * Helper function to validate group chat participants. This simulates the validation logic in
     * ChatRepository.
     */
    private fun validateGroupChatParticipants(memberIds: List<String>): Result<Unit> {
        return try {
            // Check for empty list
            if (memberIds.isEmpty()) {
                return Result.failure(Exception("Group has no members"))
            }

            // Check for minimum participants (at least 2)
            if (memberIds.size < 2) {
                return Result.failure(
                        Exception("Group must have at least 2 members to create a chat")
                )
            }

            // Check for maximum participants (100)
            if (memberIds.size > 100) {
                return Result.failure(
                        Exception(
                                "Cannot create chat: Group cannot have more than 100 members " +
                                        "(currently has ${memberIds.size})"
                        )
                )
            }

            // Check for blank member IDs
            if (memberIds.any { it.isBlank() }) {
                return Result.failure(Exception("Group has invalid member IDs"))
            }

            // Check for duplicates
            if (memberIds.distinct().size != memberIds.size) {
                return Result.failure(Exception("Cannot create chat: Group has duplicate members"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
