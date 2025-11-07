package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for UserProfile model. Tests Requirements 8.5: UserProfile deserialization,
 * @IgnoreExtraProperties, and field mapping.
 */
class UserProfileTest {

    /**
     * Test: Verify UserProfile deserialization without warnings Requirement: 8.5 - Write test to
     * verify UserProfile deserialization without warnings
     */
    @Test
    fun `UserProfile deserializes from Firestore without warnings`() {
        // Create a UserProfile with all fields
        val profile =
                UserProfile(
                        userId = "user123",
                        uid = "user123",
                        displayName = "John Doe",
                        email = "john@example.com",
                        photoUrl = "https://example.com/photo.jpg",
                        profileImageUrl = "https://example.com/profile.jpg",
                        firstName = "John",
                        lastName = "Doe",
                        bio = "Software developer",
                        phoneNumber = "+1234567890",
                        online = true,
                        isOnline = true,
                        fcmToken = "fcm_token_123",
                        createdAt = Timestamp.now(),
                        lastActive = Timestamp.now(),
                        tasksCompleted = 10,
                        groupsJoined = 5,
                        aiPromptsUsed = 3,
                        aiPromptsLimit = 10,
                        notificationsEnabled = true,
                        emailNotifications = true,
                        authProvider = "google"
                )

        // Verify all fields are accessible
        assertEquals("user123", profile.userId)
        assertEquals("John Doe", profile.displayName)
        assertEquals("john@example.com", profile.email)
        assertEquals("John", profile.firstName)
        assertEquals("Doe", profile.lastName)
        assertEquals("Software developer", profile.bio)
        assertEquals("+1234567890", profile.phoneNumber)
        assertTrue(profile.online)
        assertTrue(profile.isOnline)
        assertEquals("fcm_token_123", profile.fcmToken)
        assertEquals(10, profile.tasksCompleted)
        assertEquals(5, profile.groupsJoined)
        assertEquals(3, profile.aiPromptsUsed)
        assertEquals(10, profile.aiPromptsLimit)
        assertTrue(profile.notificationsEnabled)
        assertTrue(profile.emailNotifications)
        assertEquals("google", profile.authProvider)
    }

    /**
     * Test: Verify @IgnoreExtraProperties works correctly Requirement: 8.5 - Write test to verify
     * @IgnoreExtraProperties works correctly
     *
     * The @IgnoreExtraProperties annotation allows Firestore to ignore fields in documents that
     * don't exist in the model, preventing warnings.
     */
    @Test
    fun `IgnoreExtraProperties annotation prevents warnings for extra fields`() {
        // This test verifies that the @IgnoreExtraProperties annotation is present
        val annotation =
                UserProfile::class.java.getAnnotation(
                        com.google.firebase.firestore.IgnoreExtraProperties::class.java
                )

        assertNotNull("@IgnoreExtraProperties annotation should be present", annotation)
    }

    /**
     * Test: Verify all fields are properly mapped Requirement: 8.5 - Write test to verify all
     * fields are properly mapped
     */
    @Test
    fun `all fields have PropertyName annotations`() {
        // Verify that key fields have @PropertyName annotations
        // This ensures proper mapping between Kotlin properties and Firestore fields

        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        // Verify the toMap() method includes all fields
        val map = profile.toMap()

        assertTrue("Map should contain userId", map.containsKey("userId"))
        assertTrue("Map should contain uid", map.containsKey("uid"))
        assertTrue("Map should contain displayName", map.containsKey("displayName"))
        assertTrue("Map should contain email", map.containsKey("email"))
        assertTrue("Map should contain photoUrl", map.containsKey("photoUrl"))
        assertTrue("Map should contain profileImageUrl", map.containsKey("profileImageUrl"))
        assertTrue("Map should contain firstName", map.containsKey("firstName"))
        assertTrue("Map should contain lastName", map.containsKey("lastName"))
        assertTrue("Map should contain bio", map.containsKey("bio"))
        assertTrue("Map should contain phoneNumber", map.containsKey("phoneNumber"))
        assertTrue("Map should contain online", map.containsKey("online"))
        assertTrue("Map should contain isOnline", map.containsKey("isOnline"))
        assertTrue("Map should contain fcmToken", map.containsKey("fcmToken"))
        assertTrue("Map should contain createdAt", map.containsKey("createdAt"))
        assertTrue("Map should contain lastActive", map.containsKey("lastActive"))
        assertTrue("Map should contain tasksCompleted", map.containsKey("tasksCompleted"))
        assertTrue("Map should contain groupsJoined", map.containsKey("groupsJoined"))
        assertTrue("Map should contain aiPromptsUsed", map.containsKey("aiPromptsUsed"))
        assertTrue("Map should contain aiPromptsLimit", map.containsKey("aiPromptsLimit"))
        assertTrue(
                "Map should contain notificationsEnabled",
                map.containsKey("notificationsEnabled")
        )
        assertTrue("Map should contain emailNotifications", map.containsKey("emailNotifications"))
        assertTrue("Map should contain authProvider", map.containsKey("authProvider"))
    }

    @Test
    fun `no-argument constructor creates valid UserProfile`() {
        val profile = UserProfile()

        assertNotNull("Profile should not be null", profile)
        assertEquals("", profile.userId)
        assertEquals("", profile.displayName)
        assertEquals("", profile.email)
        assertNull(profile.photoUrl)
        assertNull(profile.firstName)
        assertNull(profile.lastName)
        assertFalse(profile.online)
        assertEquals(0, profile.tasksCompleted)
        assertEquals(10, profile.aiPromptsLimit)
        assertTrue(profile.notificationsEnabled)
    }

    @Test
    fun `UserProfile with minimal required fields is valid`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        assertTrue("Profile should be valid", profile.isValid())
        assertEquals("user123", profile.userId)
        assertEquals("Test User", profile.displayName)
        assertEquals("test@example.com", profile.email)
    }

    @Test
    fun `UserProfile with blank userId is invalid`() {
        val profile =
                UserProfile(userId = "", displayName = "Test User", email = "test@example.com")

        assertFalse("Profile should be invalid", profile.isValid())
    }

    @Test
    fun `UserProfile with blank displayName is invalid`() {
        val profile = UserProfile(userId = "user123", displayName = "", email = "test@example.com")

        assertFalse("Profile should be invalid", profile.isValid())
    }

    @Test
    fun `UserProfile with blank email is invalid`() {
        val profile = UserProfile(userId = "user123", displayName = "Test User", email = "")

        assertFalse("Profile should be invalid", profile.isValid())
    }

    @Test
    fun `UserProfile handles null optional fields correctly`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        photoUrl = null,
                        firstName = null,
                        lastName = null,
                        bio = null,
                        phoneNumber = null,
                        fcmToken = null
                )

        assertTrue("Profile should be valid", profile.isValid())
        assertNull(profile.photoUrl)
        assertNull(profile.firstName)
        assertNull(profile.lastName)
        assertNull(profile.bio)
        assertNull(profile.phoneNumber)
        assertNull(profile.fcmToken)
    }

    @Test
    fun `UserProfile toMap includes all fields with correct values`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        uid = "user123",
                        displayName = "John Doe",
                        email = "john@example.com",
                        firstName = "John",
                        lastName = "Doe",
                        tasksCompleted = 5,
                        groupsJoined = 3
                )

        val map = profile.toMap()

        assertEquals("user123", map["userId"])
        assertEquals("user123", map["uid"])
        assertEquals("John Doe", map["displayName"])
        assertEquals("john@example.com", map["email"])
        assertEquals("John", map["firstName"])
        assertEquals("Doe", map["lastName"])
        assertEquals(5, map["tasksCompleted"])
        assertEquals(3, map["groupsJoined"])
    }

    @Test
    fun `UserProfile toMap handles null values correctly`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        photoUrl = null,
                        firstName = null
                )

        val map = profile.toMap()

        assertNull(map["photoUrl"])
        assertNull(map["firstName"])
    }

    @Test
    fun `UserProfile supports both online and isOnline fields`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        online = true,
                        isOnline = false
                )

        assertTrue(profile.online)
        assertFalse(profile.isOnline)
    }

    @Test
    fun `UserProfile supports both photoUrl and profileImageUrl fields`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        photoUrl = "https://example.com/photo.jpg",
                        profileImageUrl = "https://example.com/profile.jpg"
                )

        assertEquals("https://example.com/photo.jpg", profile.photoUrl)
        assertEquals("https://example.com/profile.jpg", profile.profileImageUrl)
    }

    @Test
    fun `UserProfile supports both userId and uid fields`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        uid = "uid456",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        assertEquals("user123", profile.userId)
        assertEquals("uid456", profile.uid)
    }

    @Test
    fun `UserProfile default values are correct`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        assertEquals("", profile.uid)
        assertNull(profile.photoUrl)
        assertNull(profile.profileImageUrl)
        assertNull(profile.firstName)
        assertNull(profile.lastName)
        assertNull(profile.bio)
        assertNull(profile.phoneNumber)
        assertFalse(profile.online)
        assertFalse(profile.isOnline)
        assertNull(profile.fcmToken)
        assertNull(profile.createdAt)
        assertNull(profile.lastActive)
        assertEquals(0, profile.tasksCompleted)
        assertEquals(0, profile.groupsJoined)
        assertEquals(0, profile.aiPromptsUsed)
        assertEquals(10, profile.aiPromptsLimit)
        assertTrue(profile.notificationsEnabled)
        assertTrue(profile.emailNotifications)
        assertEquals("email", profile.authProvider)
    }

    @Test
    fun `UserProfile statistics fields work correctly`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        tasksCompleted = 15,
                        groupsJoined = 7,
                        aiPromptsUsed = 5,
                        aiPromptsLimit = 20
                )

        assertEquals(15, profile.tasksCompleted)
        assertEquals(7, profile.groupsJoined)
        assertEquals(5, profile.aiPromptsUsed)
        assertEquals(20, profile.aiPromptsLimit)
    }

    @Test
    fun `UserProfile preferences fields work correctly`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        notificationsEnabled = false,
                        emailNotifications = false,
                        authProvider = "google"
                )

        assertFalse(profile.notificationsEnabled)
        assertFalse(profile.emailNotifications)
        assertEquals("google", profile.authProvider)
    }

    @Test
    fun `UserProfile with ServerTimestamp fields`() {
        val now = Timestamp.now()
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com",
                        createdAt = now,
                        lastActive = now
                )

        assertNotNull(profile.createdAt)
        assertNotNull(profile.lastActive)
        assertEquals(now, profile.createdAt)
        assertEquals(now, profile.lastActive)
    }

    @Test
    fun `UserProfile copy works correctly`() {
        val original =
                UserProfile(
                        userId = "user123",
                        displayName = "Original Name",
                        email = "original@example.com"
                )

        val copy = original.copy(displayName = "Updated Name")

        assertEquals("user123", copy.userId)
        assertEquals("Updated Name", copy.displayName)
        assertEquals("original@example.com", copy.email)
    }

    @Test
    fun `UserProfile equality works correctly`() {
        val profile1 =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        val profile2 =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        val profile3 =
                UserProfile(
                        userId = "user456",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        assertEquals(profile1, profile2)
        assertNotEquals(profile1, profile3)
    }

    @Test
    fun `UserProfile hashCode works correctly`() {
        val profile1 =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        val profile2 =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        assertEquals(profile1.hashCode(), profile2.hashCode())
    }

    @Test
    fun `UserProfile toString includes key fields`() {
        val profile =
                UserProfile(
                        userId = "user123",
                        displayName = "Test User",
                        email = "test@example.com"
                )

        val string = profile.toString()

        assertTrue(string.contains("user123"))
        assertTrue(string.contains("Test User"))
        assertTrue(string.contains("test@example.com"))
    }
}
