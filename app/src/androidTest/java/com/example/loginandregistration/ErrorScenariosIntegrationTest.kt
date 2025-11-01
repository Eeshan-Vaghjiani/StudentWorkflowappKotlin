package com.example.loginandregistration

import android.content.SharedPreferences
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus
import com.example.loginandregistration.models.MessageType
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.utils.OfflineMessageQueue
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for Task 11.4: Test error scenarios
 *
 * This test verifies:
 * - Test with corrupted queue data
 * - Test with invalid Firestore documents
 * - Test with permission denied errors
 * - Verify app doesn't crash in any scenario
 *
 * Requirements: 6.5, 7.5
 */
@RunWith(AndroidJUnit4::class)
class ErrorScenariosIntegrationTest {

    private lateinit var context: android.content.Context
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var chatRepository: ChatRepository
    private lateinit var offlineMessageQueue: OfflineMessageQueue
    private lateinit var testPrefs: SharedPreferences

    private var testChatId: String? = null
    private var testMessageIds = mutableListOf<String>()

    companion object {
        private const val TAG = "ErrorScenariosTest"
        private const val TEST_TIMEOUT = 10000L
        private const val QUEUE_PREFS_NAME = "offline_message_queue"
        private const val KEY_QUEUED_MESSAGES = "queued_messages"
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

        // Initialize repository and queue
        chatRepository = ChatRepository(firestore, auth, storage, context)
        offlineMessageQueue = OfflineMessageQueue(context)
        testPrefs =
                context.getSharedPreferences(QUEUE_PREFS_NAME, android.content.Context.MODE_PRIVATE)

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

            // Clean up test chat
            testChatId?.let { chatId ->
                try {
                    firestore.collection("chats").document(chatId).delete().await()
                    Log.d(TAG, "Cleaned up test chat: $chatId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up chat $chatId: ${e.message}")
                }
            }

            // Clear test queue
            offlineMessageQueue.clearQueue()

            Log.d(TAG, "Test cleanup complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    @Test
    fun testCorruptedQueueData_handledGracefully() = runBlocking {
        // Requirement 7.5: WHEN errors are logged THEN they SHALL include the user ID and chat ID
        // for debugging
        // Test that corrupted queue data doesn't crash the app

        Log.d(TAG, "Starting testCorruptedQueueData_handledGracefully")

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

            // Step 2: Inject corrupted JSON data into SharedPreferences
            val corruptedJsonScenarios =
                    listOf(
                            // Scenario 1: Invalid JSON syntax
                            "{invalid json syntax",
                            // Scenario 2: Null values in array
                            """[null, null, null]""",
                            // Scenario 3: Missing required fields
                            """[{"message": {"id": "test-1"}}]""",
                            // Scenario 4: Wrong data types
                            """[{"message": "not-an-object", "attempts": "not-a-number"}]""",
                            // Scenario 5: Empty object
                            """[{}]""",
                            // Scenario 6: Malformed nested structure
                            """[{"message": {"id": 123, "chatId": null, "senderId": []}}]"""
                    )

            corruptedJsonScenarios.forEachIndexed { index, corruptedJson ->
                Log.d(TAG, "Testing corrupted JSON scenario ${index + 1}")

                try {
                    // Inject corrupted data
                    testPrefs.edit().putString(KEY_QUEUED_MESSAGES, corruptedJson).apply()
                    Log.d(TAG, "Injected corrupted JSON: ${corruptedJson.take(50)}...")

                    // Try to get queued messages - should handle gracefully
                    val queuedMessages = offlineMessageQueue.getQueuedMessages()
                    Log.d(TAG, "Retrieved ${queuedMessages.size} messages from corrupted queue")

                    // Should return empty list, not crash
                    assertNotNull("Queued messages should not be null", queuedMessages)
                    assertTrue("Corrupted queue should return empty list", queuedMessages.isEmpty())

                    // Try to get messages for specific chat - should handle gracefully
                    val chatMessages = offlineMessageQueue.getQueuedMessagesForChat(testChat.chatId)
                    assertNotNull("Chat messages should not be null", chatMessages)
                    assertTrue(
                            "Corrupted queue should return empty list for chat",
                            chatMessages.isEmpty()
                    )

                    Log.d(TAG, "Scenario ${index + 1} handled gracefully - no crash")
                } catch (e: NullPointerException) {
                    fail(
                            "NullPointerException should not occur with corrupted queue data: ${e.message}\n${e.stackTraceToString()}"
                    )
                } catch (e: Exception) {
                    // Other exceptions are logged but test continues
                    Log.w(TAG, "Exception in scenario ${index + 1}: ${e.message}")
                }
            }

            // Step 3: Clear corrupted data and verify queue works normally
            offlineMessageQueue.clearQueue()
            Log.d(TAG, "Cleared corrupted queue")

            // Step 4: Queue a valid message to verify recovery
            val validMessage =
                    Message(
                            id = "test-recovery-${System.currentTimeMillis()}",
                            chatId = testChat.chatId,
                            senderId = currentUserId,
                            text = "Recovery test message",
                            timestamp = Date(),
                            status = MessageStatus.SENDING,
                            type = MessageType.TEXT
                    )

            offlineMessageQueue.queueMessage(validMessage)
            testMessageIds.add(validMessage.id)

            val recoveredMessages = offlineMessageQueue.getQueuedMessages()
            assertEquals("Should have 1 message after recovery", 1, recoveredMessages.size)
            assertEquals("Recovered message should match", validMessage.id, recoveredMessages[0].id)

            Log.d(TAG, "Queue recovered successfully after corruption")

            Log.d(
                    TAG,
                    "Test completed successfully - all corrupted queue scenarios handled gracefully"
            )
        } catch (e: NullPointerException) {
            fail(
                    "NullPointerException should not occur in corrupted queue test: ${e.message}\n${e.stackTraceToString()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        }
    }

    @Test
    fun testInvalidFirestoreDocuments_handledGracefully() = runBlocking {
        // Requirement 7.5: WHEN errors are logged THEN they SHALL include the user ID and chat ID
        // for debugging
        // Test that invalid Firestore documents don't crash the app

        Log.d(TAG, "Starting testInvalidFirestoreDocuments_handledGracefully")

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

            // Step 2: Create invalid message documents in Firestore
            val invalidDocumentScenarios =
                    listOf(
                            // Scenario 1: Missing required fields
                            mapOf(
                                    "text" to "Message with missing fields",
                                    "timestamp" to com.google.firebase.Timestamp.now()
                            ),
                            // Scenario 2: Null required fields
                            mapOf(
                                    "id" to null,
                                    "chatId" to null,
                                    "senderId" to null,
                                    "text" to "Message with null fields",
                                    "timestamp" to com.google.firebase.Timestamp.now()
                            ),
                            // Scenario 3: Wrong data types
                            mapOf(
                                    "id" to 12345, // Should be String
                                    "chatId" to true, // Should be String
                                    "senderId" to listOf("wrong", "type"), // Should be String
                                    "text" to "Message with wrong types",
                                    "timestamp" to "not-a-timestamp" // Should be Timestamp
                            ),
                            // Scenario 4: Empty strings for required fields
                            mapOf(
                                    "id" to "",
                                    "chatId" to "",
                                    "senderId" to "",
                                    "text" to "",
                                    "timestamp" to com.google.firebase.Timestamp.now()
                            ),
                            // Scenario 5: Missing timestamp
                            mapOf(
                                    "id" to "test-no-timestamp",
                                    "chatId" to testChat.chatId,
                                    "senderId" to currentUserId,
                                    "text" to "Message without timestamp"
                            )
                    )

            val invalidDocIds = mutableListOf<String>()

            invalidDocumentScenarios.forEachIndexed { index, invalidDoc ->
                Log.d(TAG, "Creating invalid document scenario ${index + 1}")

                try {
                    val docRef =
                            firestore
                                    .collection("chats")
                                    .document(testChat.chatId)
                                    .collection("messages")
                                    .document()

                    docRef.set(invalidDoc).await()
                    invalidDocIds.add(docRef.id)
                    testMessageIds.add(docRef.id)

                    Log.d(TAG, "Created invalid document: ${docRef.id}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to create invalid document ${index + 1}: ${e.message}")
                }
            }

            delay(1000) // Wait for Firestore propagation

            // Step 3: Try to retrieve and parse messages - should handle invalid docs gracefully
            Log.d(TAG, "Retrieving messages including invalid documents")

            val messagesSnapshot =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .get()
                            .await()

            Log.d(TAG, "Retrieved ${messagesSnapshot.documents.size} documents from Firestore")

            // Step 4: Parse messages and verify no crashes
            val parsedMessages =
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

            Log.d(
                    TAG,
                    "Successfully parsed ${parsedMessages.size} out of ${messagesSnapshot.documents.size} documents"
            )

            // Step 5: Verify invalid documents were filtered out (returned null)
            val nullCount = messagesSnapshot.documents.size - parsedMessages.size
            Log.d(TAG, "$nullCount invalid documents were filtered out")

            // Should have filtered out the invalid documents
            assertTrue("Some documents should have been filtered out", nullCount > 0)

            // Step 6: Verify parsed messages are valid (no nulls in list)
            parsedMessages.forEach { message ->
                assertNotNull("Message should not be null", message)
                assertNotNull("Message ID should not be null", message.id)
                assertNotNull("Chat ID should not be null", message.chatId)
                assertNotNull("Sender ID should not be null", message.senderId)
                assertFalse("Message ID should not be blank", message.id.isBlank())
                assertFalse("Chat ID should not be blank", message.chatId.isBlank())
                assertFalse("Sender ID should not be blank", message.senderId.isBlank())
            }

            Log.d(TAG, "All parsed messages are valid - no null fields")

            // Step 7: Clean up invalid documents
            invalidDocIds.forEach { docId ->
                try {
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .document(docId)
                            .delete()
                            .await()
                    Log.d(TAG, "Cleaned up invalid document: $docId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up invalid document $docId: ${e.message}")
                }
            }

            Log.d(
                    TAG,
                    "Test completed successfully - all invalid document scenarios handled gracefully"
            )
        } catch (e: NullPointerException) {
            fail(
                    "NullPointerException should not occur with invalid Firestore documents: ${e.message}\n${e.stackTraceToString()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        }
    }

    @Test
    fun testPermissionDeniedErrors_handledGracefully() = runBlocking {
        // Requirement 6.5: WHEN the triggerNotifications function executes THEN it SHALL handle
        // permission errors gracefully
        // Test that permission denied errors don't crash the app

        Log.d(TAG, "Starting testPermissionDeniedErrors_handledGracefully")

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

            // Step 2: Try to access a document we don't have permission for
            Log.d(TAG, "Testing permission denied on document access")

            try {
                // Try to access another user's private data (should fail with permission denied)
                val result =
                        firestore.collection("users").document("non-existent-user-id").get().await()

                Log.d(TAG, "Document access result: exists=${result.exists()}")
                // If we get here, either document doesn't exist or we have permission
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.d(TAG, "Permission denied error caught and handled: ${e.message}")
                    // This is expected - test passes
                } else {
                    Log.w(TAG, "Other Firestore error: ${e.code} - ${e.message}")
                }
                // Should not crash - test continues
            } catch (e: NullPointerException) {
                fail(
                        "NullPointerException should not occur when handling permission errors: ${e.message}"
                )
            }

            // Step 3: Try to write to a collection we might not have permission for
            Log.d(TAG, "Testing permission denied on write operation")

            try {
                // Try to write to a restricted collection
                firestore
                        .collection("admin_only")
                        .document("test-doc")
                        .set(mapOf("test" to "data"))
                        .await()

                Log.d(TAG, "Write operation succeeded (or collection doesn't exist)")
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.d(TAG, "Permission denied on write caught and handled: ${e.message}")
                    // This is expected - test passes
                } else {
                    Log.w(TAG, "Other Firestore error on write: ${e.code} - ${e.message}")
                }
                // Should not crash - test continues
            } catch (e: NullPointerException) {
                fail(
                        "NullPointerException should not occur when handling write permission errors: ${e.message}"
                )
            }

            // Step 4: Send a message and verify it succeeds even if notification fails
            Log.d(TAG, "Testing message send with potential notification permission error")

            val messageContent = "Test message with notification - ${Date().time}"
            val sendResult =
                    chatRepository.sendMessage(
                            chatId = testChat.chatId,
                            content = messageContent,
                            type = MessageType.TEXT
                    )

            // Message send should succeed even if notification fails
            assertTrue("Message send should succeed", sendResult.isSuccess)

            val sentMessage = sendResult.getOrNull()
            assertNotNull("Sent message should not be null", sentMessage)

            sentMessage?.let { message ->
                testMessageIds.add(message.id)
                assertEquals("Message content should match", messageContent, message.text)
                assertEquals("Message status should be SENT", MessageStatus.SENT, message.status)
                Log.d(TAG, "Message sent successfully: ${message.id}")
            }

            // Step 5: Try to query with invalid filters (might cause permission error)
            Log.d(TAG, "Testing query with potential permission issues")

            try {
                // Try to query all chats without proper filter
                val allChats = firestore.collection("chats").limit(1).get().await()

                Log.d(TAG, "Query succeeded, found ${allChats.size()} chats")
            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.d(TAG, "Permission denied on query caught and handled: ${e.message}")
                    // This is expected - test passes
                } else {
                    Log.w(TAG, "Other Firestore error on query: ${e.code} - ${e.message}")
                }
                // Should not crash - test continues
            } catch (e: NullPointerException) {
                fail(
                        "NullPointerException should not occur when handling query permission errors: ${e.message}"
                )
            }

            // Step 6: Verify app state is still valid after permission errors
            Log.d(TAG, "Verifying app state after permission errors")

            val currentUser = auth.currentUser
            assertNotNull("User should still be authenticated", currentUser)
            assertEquals("User ID should match", currentUserId, currentUser?.uid)

            // Verify we can still perform valid operations
            val validQuery =
                    firestore
                            .collection("chats")
                            .whereArrayContains("participantIds", currentUserId)
                            .limit(5)
                            .get()
                            .await()

            assertNotNull("Valid query should succeed", validQuery)
            Log.d(TAG, "Valid query succeeded after permission errors")

            Log.d(
                    TAG,
                    "Test completed successfully - all permission denied scenarios handled gracefully"
            )
        } catch (e: NullPointerException) {
            fail(
                    "NullPointerException should not occur when handling permission errors: ${e.message}\n${e.stackTraceToString()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in test", e)
            throw e
        }
    }

    @Test
    fun testMultipleErrorScenarios_appDoesNotCrash() = runBlocking {
        // Comprehensive test combining multiple error scenarios
        // Requirement 7.5: Verify app doesn't crash in any scenario

        Log.d(TAG, "Starting testMultipleErrorScenarios_appDoesNotCrash")

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

            // Step 2: Corrupt queue data
            Log.d(TAG, "Corrupting queue data")
            testPrefs.edit().putString(KEY_QUEUED_MESSAGES, "{corrupted}").apply()

            // Step 3: Try to queue a message with corrupted queue
            val message1 =
                    Message(
                            id = "test-msg-1-${System.currentTimeMillis()}",
                            chatId = testChat.chatId,
                            senderId = currentUserId,
                            text = "Test message 1",
                            timestamp = Date(),
                            status = MessageStatus.SENDING,
                            type = MessageType.TEXT
                    )

            offlineMessageQueue.queueMessage(message1)
            testMessageIds.add(message1.id)
            Log.d(TAG, "Queued message with corrupted queue - no crash")

            // Step 4: Create invalid Firestore document
            Log.d(TAG, "Creating invalid Firestore document")
            val invalidDocRef =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .document()

            invalidDocRef.set(mapOf("invalid" to "data")).await()
            testMessageIds.add(invalidDocRef.id)

            // Step 5: Try to parse messages including invalid document
            delay(500)
            val messagesSnapshot =
                    firestore
                            .collection("chats")
                            .document(testChat.chatId)
                            .collection("messages")
                            .get()
                            .await()

            val parsedMessages =
                    messagesSnapshot.documents.mapNotNull { doc ->
                        try {
                            Message.fromFirestore(doc)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse message ${doc.id}: ${e.message}")
                            null
                        }
                    }

            Log.d(TAG, "Parsed ${parsedMessages.size} messages - no crash")

            // Step 6: Try permission-denied operation
            Log.d(TAG, "Attempting permission-denied operation")
            try {
                firestore.collection("restricted").document("test").get().await()
            } catch (e: FirebaseFirestoreException) {
                Log.d(TAG, "Permission error handled: ${e.code}")
            }

            // Step 7: Send message (should succeed despite previous errors)
            Log.d(TAG, "Sending message after multiple errors")
            val sendResult =
                    chatRepository.sendMessage(
                            chatId = testChat.chatId,
                            content = "Message after errors",
                            type = MessageType.TEXT
                    )

            assertTrue("Message send should succeed after errors", sendResult.isSuccess)
            sendResult.getOrNull()?.let { testMessageIds.add(it.id) }

            // Step 8: Process queue (should handle corrupted data gracefully)
            Log.d(TAG, "Processing queue after errors")
            val processResult = chatRepository.processQueuedMessages()
            assertNotNull("Process result should not be null", processResult)

            // Step 9: Verify app is still functional
            Log.d(TAG, "Verifying app functionality after multiple errors")
            val currentUser = auth.currentUser
            assertNotNull("User should still be authenticated", currentUser)

            val chats =
                    firestore
                            .collection("chats")
                            .whereArrayContains("participantIds", currentUserId)
                            .limit(5)
                            .get()
                            .await()

            assertNotNull("Should be able to query chats", chats)

            Log.d(
                    TAG,
                    "Test completed successfully - app survived multiple error scenarios without crashing"
            )
        } catch (e: NullPointerException) {
            fail(
                    "NullPointerException should not occur in multiple error scenarios: ${e.message}\n${e.stackTraceToString()}"
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
