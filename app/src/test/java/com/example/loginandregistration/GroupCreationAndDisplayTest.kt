package com.example.loginandregistration

import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.GroupMember
import com.example.loginandregistration.models.GroupSettings
import com.google.firebase.Timestamp
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Group Creation and Display functionality
 * 
 * Tests verify:
 * - Correct field names (memberIds, not members)
 * - Proper initialization of group data
 * - Data model consistency
 */
class GroupCreationAndDisplayTest {

    @Test
    fun `test FirebaseGroup has correct field names`() {
        // Verify that FirebaseGroup model uses memberIds field
        val group = FirebaseGroup(
            id = "test-group-id",
            name = "Test Group",
            description = "Test Description",
            subject = "Computer Science",
            owner = "user-123",
            joinCode = "ABC123",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            members = listOf(
                GroupMember(
                    userId = "user-123",
                    email = "test@example.com",
                    displayName = "Test User",
                    role = "owner",
                    joinedAt = Timestamp.now()
                )
            ),
            memberIds = listOf("user-123"),
            tasks = emptyList(),
            settings = GroupSettings(isPublic = true),
            isActive = true
        )

        // Verify memberIds field exists and is populated
        assertNotNull("memberIds field should exist", group.memberIds)
        assertEquals("memberIds should contain one member", 1, group.memberIds.size)
        assertEquals("memberIds should contain user-123", "user-123", group.memberIds[0])
        
        // Verify members array also exists for detailed member info
        assertNotNull("members field should exist", group.members)
        assertEquals("members should contain one member", 1, group.members.size)
        assertEquals("member userId should match", "user-123", group.members[0].userId)
    }

    @Test
    fun `test group creation initializes all required fields`() {
        val userId = "user-123"
        val ownerMember = GroupMember(
            userId = userId,
            email = "owner@example.com",
            displayName = "Owner User",
            role = "owner",
            joinedAt = Timestamp.now()
        )

        val group = FirebaseGroup(
            name = "New Group",
            description = "Group Description",
            subject = "Mathematics",
            owner = userId,
            joinCode = "XYZ789",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            members = listOf(ownerMember),
            memberIds = listOf(userId),
            tasks = emptyList(),
            settings = GroupSettings(isPublic = false),
            isActive = true
        )

        // Verify all required fields are initialized
        assertNotNull("Group name should be set", group.name)
        assertNotNull("Group owner should be set", group.owner)
        assertNotNull("Group joinCode should be set", group.joinCode)
        assertNotNull("Group memberIds should be initialized", group.memberIds)
        assertNotNull("Group members should be initialized", group.members)
        assertTrue("Group should be active", group.isActive)
        
        // Verify owner is in memberIds
        assertTrue("Owner should be in memberIds", group.memberIds.contains(userId))
        
        // Verify owner member has correct role
        val ownerInMembers = group.members.find { it.userId == userId }
        assertNotNull("Owner should be in members list", ownerInMembers)
        assertEquals("Owner role should be 'owner'", "owner", ownerInMembers?.role)
    }

    @Test
    fun `test group settings isPublic field`() {
        // Test public group
        val publicGroup = FirebaseGroup(
            name = "Public Group",
            settings = GroupSettings(isPublic = true)
        )
        assertTrue("Public group should have isPublic = true", publicGroup.settings.isPublic)

        // Test private group
        val privateGroup = FirebaseGroup(
            name = "Private Group",
            settings = GroupSettings(isPublic = false)
        )
        assertFalse("Private group should have isPublic = false", privateGroup.settings.isPublic)
    }

    @Test
    fun `test memberIds and members arrays stay in sync`() {
        val member1 = GroupMember(
            userId = "user-1",
            email = "user1@example.com",
            displayName = "User One",
            role = "owner"
        )
        
        val member2 = GroupMember(
            userId = "user-2",
            email = "user2@example.com",
            displayName = "User Two",
            role = "member"
        )

        val group = FirebaseGroup(
            name = "Multi-member Group",
            members = listOf(member1, member2),
            memberIds = listOf("user-1", "user-2")
        )

        // Verify both arrays have same size
        assertEquals(
            "memberIds and members should have same size",
            group.members.size,
            group.memberIds.size
        )

        // Verify all memberIds have corresponding members
        group.memberIds.forEach { memberId ->
            val memberExists = group.members.any { it.userId == memberId }
            assertTrue("Member with id $memberId should exist in members array", memberExists)
        }

        // Verify all members have corresponding memberIds
        group.members.forEach { member ->
            assertTrue(
                "Member ${member.userId} should exist in memberIds array",
                group.memberIds.contains(member.userId)
            )
        }
    }

    @Test
    fun `test group member roles`() {
        val ownerMember = GroupMember(userId = "user-1", role = "owner")
        val adminMember = GroupMember(userId = "user-2", role = "admin")
        val regularMember = GroupMember(userId = "user-3", role = "member")

        assertEquals("Owner role should be 'owner'", "owner", ownerMember.role)
        assertEquals("Admin role should be 'admin'", "admin", adminMember.role)
        assertEquals("Member role should be 'member'", "member", regularMember.role)
    }

    @Test
    fun `test group no-arg constructor for Firestore`() {
        // Firestore requires a no-arg constructor
        val group = FirebaseGroup()

        // Verify default values
        assertEquals("Default id should be empty", "", group.id)
        assertEquals("Default name should be empty", "", group.name)
        assertTrue("Default memberIds should be empty", group.memberIds.isEmpty())
        assertTrue("Default members should be empty", group.members.isEmpty())
        assertTrue("Default should be active", group.isActive)
    }

    @Test
    fun `test join code format`() {
        val group = FirebaseGroup(
            name = "Test Group",
            joinCode = "ABC123"
        )

        assertEquals("Join code should be 6 characters", 6, group.joinCode.length)
        assertTrue("Join code should be alphanumeric", group.joinCode.matches(Regex("[A-Z0-9]+")))
    }
}
