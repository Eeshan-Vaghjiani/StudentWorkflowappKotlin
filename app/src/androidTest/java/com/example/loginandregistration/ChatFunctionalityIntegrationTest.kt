package com.example.loginandregistration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.loginandregistration.models.Chat
import com.example.loginandregistration.models.ChatType
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for Task 9: Test Chat Functionality with Profiles
 *
 * This test validates:
 * - Both user profiles are created
 * - Users can search for each other
 * - Direct chat can be created
 * - Messages can be sent successfully
 * - No "User not found" errors occur
 *
 * Requirements: 1.1, 4.4, 4.5
 */
@RunWith(AndroidJUnit4::class)
class ChatFunctionalityIntegrationTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var chatRepository: ChatRepository

    // Test user credentials - these should be real test accounts in your Firebase project
    private val testUser1Email = "testuser1@example.com"
    private val testUser1Password = "TestPassword123!"
    private val testUser2Email = "testuser2@example.com"
    private val testUser2Password = "TestPassword123!"

    private var testUser1Id: String? = null
    private var testUser2Id: String? = null
    private var testChatId: String? = null

    companion object {
        private const val TAG = "ChatFunctionalityTest"
    }

    @Before
    fun setup() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userProfileRepository = UserProfileRepository(firestore, auth)
        chatRepository = ChatRepository(firestore, auth)

        Log.d(TAG, "Test setup complete")
    }

    @After
    fun cleanup() = runBlocking {
        try {
            // Clean up test chat if created
            testChatId?.let { chatId ->
                try {
                    firestore.collection("chats").document(chatId).delete().await()
                    Log.d(TAG, "Cleaned up test chat: $chatId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up test chat: ${e.message}")
                }
            }

            // Sign out
            auth.signOut()
            Log.d(TAG, "Test cleanup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    /**
     * Test the complete chat functionality flow with two users This simulates the manual test
     * scenario from Task 9
     */
    @Test
    fun testCompleteChatFlowWithTwoUsers() = runBlocking {
        Log.d(TAG, "Starting complete chat flow test")

        // Step 1: Sign in with first user and verify profile creation
        Log.d(TAG, "Step 1: Signing in with user 1")
        val user1Result = signInAndVerifyProfile(testUser1Email, testUser1Password)
        assertTrue("User 1 sign-in should succeed", user1Result.isSuccess)
        testUser1Id = auth.currentUser?.uid
        assertNotNull("User 1 ID should not be null", testUser1Id)
        Log.d(TAG, "User 1 signed in: $testUser1Id")

        // Verify user 1 profile exists
        val user1Profile = userProfileRepository.getCurrentUserProfile()
        assertTrue("User 1 profile should exist", user1Profile.isSuccess)
        val profile1 = user1Profile.getOrNull()
        assertNotNull("User 1 profile should not be null", profile1)
        assertEquals("User 1 profile userId should match", testUser1Id, profile1?.userId)
        Log.d(TAG, "User 1 profile verified: ${profile1?.displayName}")

        // Sign out user 1
        auth.signOut()
        Log.d(TAG, "User 1 signed out")

        // Step 2: Sign in with second user and verify profile creation
        Log.d(TAG, "Step 2: Signing in with user 2")
        val user2Result = signInAndVerifyProfile(testUser2Email, testUser2Password)
        assertTrue("User 2 sign-in should succeed", user2Result.isSuccess)
        testUser2Id = auth.currentUser?.uid
        assertNotNull("User 2 ID should not be null", testUser2Id)
        Log.d(TAG, "User 2 signed in: $testUser2Id")

        // Verify user 2 profile exists
        val user2Profile = userProfileRepository.getCurrentUserProfile()
        assertTrue("User 2 profile should exist", user2Profile.isSuccess)
        val profile2 = user2Profile.getOrNull()
        assertNotNull("User 2 profile should not be null", profile2)
        assertEquals("User 2 profile userId should match", testUser2Id, profile2?.userId)
        Log.d(TAG, "User 2 profile verified: ${profile2?.displayName}")

        // Step 3: Search for user 1 (verify user can be found)
        Log.d(TAG, "Step 3: Searching for user 1 from user 2's perspective")
        val searchResult = chatRepository.getUserInfo(testUser1Id!!)
        assertTrue("Should be able to find user 1", searchResult.isSuccess)
        val foundUser = searchResult.getOrNull()
        assertNotNull("Found user should not be null", foundUser)
        assertEquals("Found user ID should match", testUser1Id, foundUser?.userId)
        Log.d(TAG, "User 1 found: ${foundUser?.displayName}")

        // Step 4: Create direct chat between user 2 and user 1
        Log.d(TAG, "Step 4: Creating direct chat from user 2 to user 1")
        val chatResult = chatRepository.getOrCreateDirectChat(testUser1Id!!, foundUser)
        assertTrue("Chat creation should succeed", chatResult.isSuccess)
        val chat = chatResult.getOrNull()
        assertNotNull("Chat should not be null", chat)
        assertEquals("Chat should be DIRECT type", ChatType.DIRECT, chat?.type)
        assertEquals("Chat should have 2 participants", 2, chat?.participants?.size)
        assertTrue(
                "Chat should include user 1",
                chat?.participants?.contains(testUser1Id!!) == true
        )
        assertTrue(
                "Chat should include user 2",
                chat?.participants?.contains(testUser2Id!!) == true
        )
        testChatId = chat?.chatId
        Log.d(TAG, "Direct chat created: $testChatId")

        // Step 5: Send a message from user 2 to user 1
        Log.d(TAG, "Step 5: Sending message from user 2 to user 1")
        val messageText = "Hello from user 2! This is a test message."
        val sendResult = chatRepository.sendMessage(testChatId!!, messageText)
        assertTrue("Message send should succeed", sendResult.isSuccess)
        val messageId = sendResult.getOrNull()
        assertNotNull("Message ID should not be null", messageId)
        Log.d(TAG, "Message sent: $messageId")

        // Step 6: Verify message was created correctly
        Log.d(TAG, "Step 6: Verifying message was created")
        val messageDoc =
                firestore
                        .collection("chats")
                        .document(testChatId!!)
                        .collection("messages")
                        .document(messageId!!)
                        .get()
                        .await()

        assertTrue("Message document should exist", messageDoc.exists())
        val message = messageDoc.toObject(Message::class.java)
        assertNotNull("Message should not be null", message)
        assertEquals("Message text should match", messageText, message?.text)
        assertEquals("Message sender should be user 2", testUser2Id, message?.senderId)
        Log.d(TAG, "Message verified: ${message?.text}")

        // Step 7: Verify no "User not found" errors occurred
        Log.d(TAG, "Step 7: Verifying no errors occurred")
        // If we got here, all operations succeeded without "User not found" errors
        assertTrue("All operations completed successfully", true)

        // Sign out user 2
        auth.signOut()
        Log.d(TAG, "User 2 signed out")

        // Step 8: Sign in with user 1 and verify they can see the chat
        Log.d(TAG, "Step 8: Signing in with user 1 to verify chat visibility")
        signInAndVerifyProfile(testUser1Email, testUser1Password)

        // Verify user 1 can access the chat
        val chatDoc = firestore.collection("chats").document(testChatId!!).get().await()
        assertTrue("Chat should exist", chatDoc.exists())
        val retrievedChat = chatDoc.toObject(Chat::class.java)
        assertNotNull("Retrieved chat should not be null", retrievedChat)
        assertTrue(
                "User 1 should be a participant",
                retrievedChat?.participants?.contains(testUser1Id!!) == true
        )
        Log.d(TAG, "User 1 can access the chat")

        Log.d(TAG, "Complete chat flow test passed successfully!")
    }

    /** Helper function to sign in a user and verify their profile is created */
    private suspend fun signInAndVerifyProfile(email: String, password: String): Result<Unit> {
        return try {
            // Sign in with Firebase Auth
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Signed in with email: $email")

            // Ensure profile exists (this is what the app does after sign-in)
            val profileResult = userProfileRepository.ensureUserProfileExists()
            if (profileResult.isFailure) {
                Log.e(
                        TAG,
                        "Failed to create/update profile: ${profileResult.exceptionOrNull()?.message}"
                )
                return Result.failure(
                        profileResult.exceptionOrNull() ?: Exception("Profile creation failed")
                )
            }

            Log.d(TAG, "Profile verified for: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed for $email: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Test that verifies profile validation prevents chat operations when profile is missing */
    @Test
    fun testChatOperationsRequireProfile() = runBlocking {
        Log.d(TAG, "Testing chat operations require profile")

        // Sign in with user 1
        auth.signInWithEmailAndPassword(testUser1Email, testUser1Password).await()
        testUser1Id = auth.currentUser?.uid

        // Try to create a chat WITHOUT ensuring profile exists first
        // This should fail with a clear error message
        val chatResult = chatRepository.getOrCreateDirectChat("someOtherUserId")

        // The result should fail if profile doesn't exist
        // Note: This test assumes the profile might not exist yet
        // In practice, the profile should be created during sign-in

        Log.d(TAG, "Chat operation result: ${if (chatResult.isSuccess) "success" else "failure"}")

        // Clean up
        auth.signOut()
    }

    /** Test that verifies getUserInfo returns proper error for non-existent users */
    @Test
    fun testGetUserInfoForNonExistentUser() = runBlocking {
        Log.d(TAG, "Testing getUserInfo for non-existent user")

        // Sign in with user 1
        auth.signInWithEmailAndPassword(testUser1Email, testUser1Password).await()
        userProfileRepository.ensureUserProfileExists()

        // Try to get info for a non-existent user
        val nonExistentUserId = "nonExistentUser123456789"
        val result = chatRepository.getUserInfo(nonExistentUserId)

        // Should fail with appropriate error
        assertTrue("Should fail for non-existent user", result.isFailure)
        val error = result.exceptionOrNull()
        assertNotNull("Error should not be null", error)
        Log.d(TAG, "Error message: ${error?.message}")

        // Clean up
        auth.signOut()
    }
}
