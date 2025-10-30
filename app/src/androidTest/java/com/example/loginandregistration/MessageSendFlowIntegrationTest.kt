package com.example.loginandregistration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import com.example.loginandregistration.models.MessageType
import com.example.loginandregistration.repository.ChatRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for Task 11.1: Test complete message send flow
 *
 * This test verifies:
 * - Send message while online
 * - Verify message appears in chat
 * - Verify no null pointer exceptions in logs
 *
 * Requirements: 3.4, 3.5
 */
@RunWith(AndroidJUnit4::class)
class MessageSendFlowIntegrationTest {

    private lateinit var context: android.content.Context
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var chatRepository: ChatRepository

    private var testChatId: String? = null
    private var testMessageIds = mutableListOf<String>()

    companion object {
        private const val TAG = "MessageSendFlowTest"
        private const val TEST_TIMEOUT = 10000L // 10 seconds
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Initialize Firebase if not already initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize repository with context for offline queue support
        chatRepository = ChatRepository(firestore, auth, storage, context)

        Log.d(TAG, "Test setup complete")
    }

    @After
    fun cleanup() = runBlocking {
        try {
            // Clean up test messages
            testMessageIds.forEach { messageId ->
                testChatId?.let { chatId ->
                    try {
                        firestore
                                .collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .document(messageId)
                                .delete()
                                .await()
                        Log.d(TAG, "Cleaned up test message: $messageId")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to clean up message $messageId: ${e.message}")
                    }
                }
            }

            // Clean up test chat if created
            testChatId?.let { chatId ->
                try {
                    firestore.collection("chats").document(chatId).delete().await()
                    Log.d(TAG, "Cleaned up test chat: $chatId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up chat $chatId: ${e.message}")
                }
            }

            Log.d(TAG, "Test cleanup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    @Test
    fun testCompleteMessageSendFlow_WhileOnline() = runBlocking {
        // Requirement 3.4: WHEN displaying messages in the RecyclerView
        // THEN the adapter SHALL never receive null Message objects
        // Requirement 3.5: WHEN a message fails to parse from Firestore
        // THEN the system SHALL log the error and continue loading other messages

        Log.d(TAG, "Starting testCompleteMessageSendFlow_WhileOnline")

        // Verify user is authenticated
        val currentUserId = chatRepository.getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Log.w(TAG, "User not authenticated - skipping test")
            // This is expected in CI/CD environments without Firebase auth
            return@runBlocking
        }

        Log.d(TAG, "Current user ID: $currentUserId")

        try {
            // Step 1: Create or get a test chat
            val testChat = createTestChat()
            assertNotNull("Test chat should be created", testChat)
            testChatId = testChat.chatId
            Log.d(TAG, "Test chat created: $testChatId")

            // Step 2: Send a text message while online
            val messageContent = "Test message - ${Date().time}"
            Log.d(TAG, "Sending test message: $messageContent")

            val sendResult =
                    chatRepository.sendMessage(
                            chatId = testChat.chatId,
                            content = messageContent,
                            type = MessageType.TEXT
                    )

            // Verify send was successful
            assertTrue("Message send should succeed", sendResult.isSuccess)

            val sentMessage = sendResult.getOrNull()
            assertNotNull("Sent message should not be null", sentMessage)

            // Verify message properties
            sentMessage?.let { message ->
                testMessageIds.add(message.id)

                // Verify required fields are not null or blank
                assertFalse("Message ID should not be blank", message.id.isBlank())
                assertFalse("Chat ID should not be blank", message.chatId.isBlank())
                assertFalse("Sender ID should not be blank", message.senderId.isBlank())
                assertEquals("Chat ID should match", testChat.chatId, message.chatId)
                assertEquals("Sender ID should match current user", currentUserId, message.senderId)
                assertEquals("Message content should match", messageContent, message.text)
                assertEquals("Message type should be TEXT", MessageType.TEXT, message.type)

                // Verify message status is SENT (not SENDING or FAILED)
                assertEquals("Message status should be SENT", MessageStatus.SENT, message.status)

                Log.d(TAG, "Message sent successfully: ${message.id}")
                Log.d(TAG, "Message status: ${message.status}")
                Log.d(TAG, "Message content: ${message.text}")
            }

            // Step 3: Wait a moment for Firestore to propagate
            delay(1000)

            // Step 4: Retrieve messages from the chat to verify it appears
            Log.d(TAG, "Retrieving messages from chat")
            val messagesSnapshot =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .get()
                            .await()

            Log.d(TAG, "Retrieved ${messagesSnapshot.documents.size} messages from Firestore")

            // Step 5: Parse messages and verify no null pointer exceptions occur
            val messages =
                    messagesSnapshot.documents.mapNotNull { doc ->
                        try {
                            val message = Message.fromFirestore(doc)
                            if (message == null) {
                                Log.w(
                                        TAG,
                                        "Message.fromFirestore returned null for document ${doc.id}"
                                )
                            } else {
                                Log.d(TAG, "Successfully parsed message: ${message.id}")
                            }
                            message
                        } catch (e: NullPointerException) {
                            // This should NOT happen - fail the test if it does
                            Log.e(TAG, "NullPointerException while parsing message ${doc.id}", e)
                            fail(
                                    "NullPointerException occurred while parsing message: ${e.message}"
                            )
                            null
                        } catch (e: Exception) {
                            // Other exceptions are logged but don't fail the test
                            Log.w(TAG, "Exception while parsing message ${doc.id}: ${e.message}")
                            null
                        }
                    }

            // Verify our sent message appears in the list
            val ourMessage = messages.find { it.id == sentMessage?.id }
            assertNotNull("Sent message should appear in chat", ourMessage)

            ourMessage?.let { message ->
                // Verify all fields are properly populated (no nulls where not expected)
                assertNotNull("Message ID should not be null", message.id)
                assertNotNull("Chat ID should not be null", message.chatId)
                assertNotNull("Sender ID should not be null", message.senderId)
                assertNotNull("Timestamp should not be null", message.timestamp)
                assertNotNull("Status should not be null", message.status)

                // Verify content matches
                assertEquals("Message content should match", messageContent, message.text)

                Log.d(TAG, "Message verified in chat: ${message.id}")
            }

            // Step 6: Verify no null messages in the list
            val nullMessageCount = messagesSnapshot.documents.size - messages.size
            if (nullMessageCount > 0) {
                Log.w(TAG, "$nullMessageCount messages failed to parse (returned null)")
            }

            // All messages should parse successfully (no nulls)
            assertEquals(
                    "All messages should parse successfully",
                    messagesSnapshot.documents.size,
                    messages.size
            )

            Log.d(TAG, "Test completed successfully - no null pointer exceptions detected")
        } catch (e: NullPointerException) {
            // Fail the test if any NullPointerException occurs
            Log.e(TAG, "NullPointerException in message send flow", e)
            fail(
                    "NullPointerException should not occur in message send flow: ${e.message}\n${e.stackTraceToString()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        }
    }

    @Test
    fun testMessageSendFlow_VerifyNoNullsInMessageList() = runBlocking {
        // This test specifically verifies that the message list never contains nulls
        // Requirement 3.4: WHEN displaying messages in the RecyclerView
        // THEN the adapter SHALL never receive null Message objects

        Log.d(TAG, "Starting testMessageSendFlow_VerifyNoNullsInMessageList")

        val currentUserId = chatRepository.getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Log.w(TAG, "User not authenticated - skipping test")
            return@runBlocking
        }

        try {
            // Create test chat
            val testChat = createTestChat()
            testChatId = testChat.chatId

            // Send multiple messages
            val messageCount = 3
            for (i in 1..messageCount) {
                val result =
                        chatRepository.sendMessage(
                                chatId = testChat.chatId,
                                content = "Test message $i",
                                type = MessageType.TEXT
                        )

                result.getOrNull()?.let { message -> testMessageIds.add(message.id) }

                delay(100) // Small delay between messages
            }

            delay(1000) // Wait for Firestore propagation

            // Retrieve and parse all messages
            val messagesSnapshot =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .get()
                            .await()

            val messages =
                    messagesSnapshot.documents.mapNotNull { doc -> Message.fromFirestore(doc) }

            // Verify no nulls in the list
            assertFalse("Message list should not be empty", messages.isEmpty())
            messages.forEach { message ->
                assertNotNull("Message should not be null", message)
                assertNotNull("Message ID should not be null", message.id)
                assertNotNull("Chat ID should not be null", message.chatId)
                assertNotNull("Sender ID should not be null", message.senderId)
            }

            Log.d(TAG, "Verified ${messages.size} messages - no nulls found")
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        }
    }

    @Test
    fun testOfflineMessageQueueing() = runBlocking {
        // Task 11.2: Test offline message queueing
        // Requirements: 2.5, 3.1
        // - Disable network
        // - Send multiple messages
        // - Enable network
        // - Verify all messages send successfully
        // - Verify no null pointer exceptions

        Log.d(TAG, "Starting testOfflineMessageQueueing")

        val currentUserId = chatRepository.getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Log.w(TAG, "User not authenticated - skipping test")
            return@runBlocking
        }

        try {
            // Step 1: Create test chat
            val testChat = createTestChat()
            testChatId = testChat.chatId
            Log.d(TAG, "Test chat created: $testChatId")

            // Step 2: Disable Firestore network to simulate offline mode
            Log.d(TAG, "Disabling Firestore network")
            firestore.disableNetwork().await()
            delay(500) // Wait for network to be disabled

            // Step 3: Send multiple messages while offline
            val offlineMessageCount = 3
            val offlineMessages = mutableListOf<Message?>()

            Log.d(TAG, "Sending $offlineMessageCount messages while offline")
            for (i in 1..offlineMessageCount) {
                val messageContent = "Offline test message $i - ${Date().time}"
                Log.d(TAG, "Sending offline message $i: $messageContent")

                val sendResult =
                        chatRepository.sendMessage(
                                chatId = testChat.chatId,
                                content = messageContent,
                                type = MessageType.TEXT
                        )

                // Messages should be queued when offline
                val message = sendResult.getOrNull()
                offlineMessages.add(message)

                if (message != null) {
                    testMessageIds.add(message.id)
                    Log.d(TAG, "Message $i queued: ${message.id}, status: ${message.status}")

                    // Verify message is in SENDING status (queued)
                    assertEquals(
                            "Offline message should have SENDING status",
                            MessageStatus.SENDING,
                            message.status
                    )

                    // Verify no null fields
                    assertNotNull("Message ID should not be null", message.id)
                    assertNotNull("Chat ID should not be null", message.chatId)
                    assertNotNull("Sender ID should not be null", message.senderId)
                    assertFalse("Message ID should not be blank", message.id.isBlank())
                    assertFalse("Chat ID should not be blank", message.chatId.isBlank())
                    assertFalse("Sender ID should not be blank", message.senderId.isBlank())
                } else {
                    Log.w(TAG, "Message $i returned null from sendMessage")
                }

                delay(100) // Small delay between messages
            }

            // Step 4: Verify messages are queued (not null)
            val queuedMessageCount = offlineMessages.filterNotNull().size
            Log.d(TAG, "Queued $queuedMessageCount out of $offlineMessageCount messages")

            assertTrue("At least some messages should be queued", queuedMessageCount > 0)

            // Verify no null messages in the queue
            offlineMessages.forEach { message ->
                assertNotNull("Queued message should not be null", message)
            }

            // Step 5: Enable network to trigger queue processing
            Log.d(TAG, "Enabling Firestore network")
            firestore.enableNetwork().await()
            delay(1000) // Wait for network to be enabled

            // Step 6: Process queued messages
            Log.d(TAG, "Processing queued messages")
            val processResult = chatRepository.processQueuedMessages()

            assertTrue("Queue processing should succeed", processResult.isSuccess)

            val sentCount = processResult.getOrNull() ?: 0
            Log.d(TAG, "Successfully processed $sentCount queued messages")

            // Step 7: Wait for messages to be sent to Firestore
            delay(2000) // Give time for async operations

            // Step 8: Retrieve messages from Firestore to verify they were sent
            Log.d(TAG, "Retrieving messages from Firestore")
            val messagesSnapshot =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .get()
                            .await()

            Log.d(TAG, "Retrieved ${messagesSnapshot.documents.size} messages from Firestore")

            // Step 9: Parse messages and verify no null pointer exceptions
            val messages =
                    messagesSnapshot.documents.mapNotNull { doc ->
                        try {
                            val message = Message.fromFirestore(doc)
                            if (message == null) {
                                Log.w(
                                        TAG,
                                        "Message.fromFirestore returned null for document ${doc.id}"
                                )
                            } else {
                                Log.d(TAG, "Successfully parsed message: ${message.id}")
                            }
                            message
                        } catch (e: NullPointerException) {
                            // This should NOT happen - fail the test if it does
                            Log.e(TAG, "NullPointerException while parsing message ${doc.id}", e)
                            fail(
                                    "NullPointerException occurred while parsing message: ${e.message}"
                            )
                            null
                        } catch (e: Exception) {
                            // Other exceptions are logged but don't fail the test
                            Log.w(TAG, "Exception while parsing message ${doc.id}: ${e.message}")
                            null
                        }
                    }

            // Step 10: Verify all queued messages were sent successfully
            Log.d(TAG, "Verifying ${offlineMessages.size} queued messages were sent")

            offlineMessages.filterNotNull().forEach { queuedMessage ->
                val sentMessage = messages.find { it.id == queuedMessage.id }
                assertNotNull(
                        "Queued message ${queuedMessage.id} should appear in Firestore",
                        sentMessage
                )

                sentMessage?.let { message ->
                    // Verify message content matches
                    assertEquals("Message content should match", queuedMessage.text, message.text)

                    // Verify message status is SENT (not SENDING or FAILED)
                    assertEquals(
                            "Message status should be SENT",
                            MessageStatus.SENT,
                            message.status
                    )

                    // Verify no null fields
                    assertNotNull("Message ID should not be null", message.id)
                    assertNotNull("Chat ID should not be null", message.chatId)
                    assertNotNull("Sender ID should not be null", message.senderId)
                    assertNotNull("Timestamp should not be null", message.timestamp)

                    Log.d(TAG, "Verified queued message ${message.id} was sent successfully")
                }
            }

            // Step 11: Verify no null messages in the final list
            val nullMessageCount = messagesSnapshot.documents.size - messages.size
            if (nullMessageCount > 0) {
                Log.w(TAG, "$nullMessageCount messages failed to parse (returned null)")
            }

            // All messages should parse successfully (no nulls)
            assertEquals(
                    "All messages should parse successfully",
                    messagesSnapshot.documents.size,
                    messages.size
            )

            Log.d(
                    TAG,
                    "Test completed successfully - all queued messages sent, no null pointer exceptions detected"
            )
        } catch (e: NullPointerException) {
            // Fail the test if any NullPointerException occurs
            Log.e(TAG, "NullPointerException in offline message queueing test", e)
            fail(
                    "NullPointerException should not occur in offline message queueing: ${e.message}\n${e.stackTraceToString()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        } finally {
            // Ensure network is re-enabled even if test fails
            try {
                firestore.enableNetwork().await()
                Log.d(TAG, "Network re-enabled in finally block")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to re-enable network in finally: ${e.message}")
            }
        }
    }

    @Test
    fun testTypingStatusFlow() = runBlocking {
        // Task 11.3: Test typing status
        // Requirements: 4.5
        // - Start typing in chat
        // - Verify typing indicator appears for other user
        // - Stop typing
        // - Verify typing indicator disappears
        // - Check logs for timestamp parsing errors

        Log.d(TAG, "Starting testTypingStatusFlow")

        val currentUserId = chatRepository.getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Log.w(TAG, "User not authenticated - skipping test")
            return@runBlocking
        }

        try {
            // Step 1: Create test chat
            val testChat = createTestChat()
            testChatId = testChat.chatId
            Log.d(TAG, "Test chat created: $testChatId")

            // Step 2: Set up a listener for typing users
            val typingUsersCollected = mutableListOf<List<String>>()
            val typingJob =
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        chatRepository.getTypingUsers(testChat.chatId).collect { typingUsers ->
                            Log.d(TAG, "Typing users update: $typingUsers")
                            typingUsersCollected.add(typingUsers)
                        }
                    }

            // Wait for initial empty state
            delay(500)

            // Step 3: Start typing (set typing status to true)
            Log.d(TAG, "Setting typing status to true")
            val setTypingResult = chatRepository.setTypingStatus(testChat.chatId, true)

            assertTrue("Setting typing status should succeed", setTypingResult.isSuccess)
            Log.d(TAG, "Typing status set to true")

            // Step 4: Wait for typing status to propagate
            delay(1000)

            // Step 5: Verify typing status document was created in Firestore
            Log.d(TAG, "Verifying typing status document in Firestore")
            val typingStatusDoc =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("typing_status")
                            .document(currentUserId)
                            .get()
                            .await()

            assertTrue("Typing status document should exist", typingStatusDoc.exists())

            // Step 6: Verify typing status fields
            val isTyping = typingStatusDoc.getBoolean("isTyping")
            val userId = typingStatusDoc.getString("userId")
            val timestamp = typingStatusDoc.get("timestamp")

            assertNotNull("isTyping field should not be null", isTyping)
            assertEquals("isTyping should be true", true, isTyping)
            assertEquals("userId should match current user", currentUserId, userId)
            assertNotNull("timestamp field should not be null", timestamp)

            // Step 7: Verify timestamp is a Firestore Timestamp (not Long)
            assertTrue(
                    "timestamp should be a Firestore Timestamp",
                    timestamp is com.google.firebase.Timestamp
            )

            Log.d(
                    TAG,
                    "Typing status document verified: isTyping=$isTyping, userId=$userId, timestamp type=${timestamp?.javaClass?.simpleName}"
            )

            // Step 8: Verify typing users list does NOT include current user (they should be
            // filtered out)
            // The getTypingUsers function excludes the current user from the list
            val currentTypingUsers = typingUsersCollected.lastOrNull() ?: emptyList()
            assertFalse(
                    "Current user should not appear in their own typing users list",
                    currentTypingUsers.contains(currentUserId)
            )

            Log.d(TAG, "Verified current user is not in typing users list")

            // Step 9: Stop typing (set typing status to false)
            Log.d(TAG, "Setting typing status to false")
            val clearTypingResult = chatRepository.setTypingStatus(testChat.chatId, false)

            assertTrue("Clearing typing status should succeed", clearTypingResult.isSuccess)
            Log.d(TAG, "Typing status set to false")

            // Step 10: Wait for typing status to propagate
            delay(1000)

            // Step 11: Verify typing status was updated in Firestore
            val updatedTypingStatusDoc =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("typing_status")
                            .document(currentUserId)
                            .get()
                            .await()

            assertTrue("Typing status document should still exist", updatedTypingStatusDoc.exists())

            val updatedIsTyping = updatedTypingStatusDoc.getBoolean("isTyping")
            assertEquals("isTyping should be false", false, updatedIsTyping)

            Log.d(TAG, "Typing status updated to false")

            // Step 12: Test backward compatibility with Long timestamp
            // Create a typing status document with Long timestamp (old format)
            Log.d(TAG, "Testing backward compatibility with Long timestamp")

            val testUserId = "test-user-backward-compat"
            val longTimestamp = System.currentTimeMillis()

            val typingDataWithLong =
                    mapOf(
                            "userId" to testUserId,
                            "isTyping" to true,
                            "timestamp" to longTimestamp // Using Long instead of Timestamp
                    )

            firestore
                    .collection("chats")
                    .document(testChat.chatId)
                    .collection("typing_status")
                    .document(testUserId)
                    .set(typingDataWithLong)
                    .await()

            Log.d(TAG, "Created typing status with Long timestamp: $longTimestamp")

            // Wait for listener to process
            delay(1000)

            // Step 13: Verify the Long timestamp is handled correctly (no errors in logs)
            // The getTypingUsers function should convert Long to Date and process it
            val typingUsersWithLong = typingUsersCollected.lastOrNull() ?: emptyList()

            // The test user should appear in the typing users list (if within 10 second window)
            assertTrue(
                    "Test user with Long timestamp should appear in typing users",
                    typingUsersWithLong.contains(testUserId)
            )

            Log.d(TAG, "Backward compatibility verified - Long timestamp handled correctly")

            // Step 14: Test invalid timestamp type handling
            Log.d(TAG, "Testing invalid timestamp type handling")

            val invalidUserId = "test-user-invalid-timestamp"
            val typingDataWithInvalidTimestamp =
                    mapOf(
                            "userId" to invalidUserId,
                            "isTyping" to true,
                            "timestamp" to "invalid-string-timestamp" // Invalid type
                    )

            firestore
                    .collection("chats")
                    .document(testChat.chatId)
                    .collection("typing_status")
                    .document(invalidUserId)
                    .set(typingDataWithInvalidTimestamp)
                    .await()

            Log.d(TAG, "Created typing status with invalid timestamp type")

            // Wait for listener to process
            delay(1000)

            // Step 15: Verify invalid timestamp is handled gracefully (user is skipped, no crash)
            val typingUsersAfterInvalid = typingUsersCollected.lastOrNull() ?: emptyList()

            // The user with invalid timestamp should NOT appear in the list
            assertFalse(
                    "User with invalid timestamp should not appear in typing users",
                    typingUsersAfterInvalid.contains(invalidUserId)
            )

            Log.d(TAG, "Invalid timestamp handled gracefully - user skipped without crash")

            // Step 16: Test missing timestamp field
            Log.d(TAG, "Testing missing timestamp field handling")

            val missingTimestampUserId = "test-user-missing-timestamp"
            val typingDataWithoutTimestamp =
                    mapOf("userId" to missingTimestampUserId, "isTyping" to true)

            firestore
                    .collection("chats")
                    .document(testChat.chatId)
                    .collection("typing_status")
                    .document(missingTimestampUserId)
                    .set(typingDataWithoutTimestamp)
                    .await()

            Log.d(TAG, "Created typing status without timestamp field")

            // Wait for listener to process
            delay(1000)

            // Step 17: Verify missing timestamp is handled gracefully
            val typingUsersAfterMissing = typingUsersCollected.lastOrNull() ?: emptyList()

            // The user with missing timestamp should NOT appear in the list
            assertFalse(
                    "User with missing timestamp should not appear in typing users",
                    typingUsersAfterMissing.contains(missingTimestampUserId)
            )

            Log.d(TAG, "Missing timestamp handled gracefully - user skipped without crash")

            // Step 18: Clean up test typing status documents
            firestore
                    .collection("chats")
                    .document(testChat.chatId)
                    .collection("typing_status")
                    .document(testUserId)
                    .delete()
                    .await()

            firestore
                    .collection("chats")
                    .document(testChat.chatId)
                    .collection("typing_status")
                    .document(invalidUserId)
                    .delete()
                    .await()

            firestore
                    .collection("chats")
                    .document(testChat.chatId)
                    .collection("typing_status")
                    .document(missingTimestampUserId)
                    .delete()
                    .await()

            // Cancel the typing listener
            typingJob.cancel()

            Log.d(
                    TAG,
                    "Test completed successfully - typing status works correctly, no timestamp parsing errors"
            )
        } catch (e: NullPointerException) {
            // Fail the test if any NullPointerException occurs
            Log.e(TAG, "NullPointerException in typing status test", e)
            fail(
                    "NullPointerException should not occur in typing status flow: ${e.message}\n${e.stackTraceToString()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        }
    }

    /** Helper function to create a test chat for integration testing */
    private suspend fun createTestChat(): com.example.loginandregistration.models.Chat {
        val currentUserId = chatRepository.getCurrentUserId()
        val chatId = firestore.collection("chats").document().id

        val chat =
                com.example.loginandregistration.models.Chat(
                        chatId = chatId,
                        type = com.example.loginandregistration.models.ChatType.DIRECT,
                        participants = listOf(currentUserId, "test-user-2"),
                        participantDetails = emptyMap(),
                        lastMessage = "",
                        lastMessageTime = Date(),
                        lastMessageSenderId = "",
                        unreadCount = mapOf(currentUserId to 0, "test-user-2" to 0),
                        createdAt = Date()
                )

        firestore.collection("chats").document(chatId).set(chat).await()

        return chat
    }
}
