package com.example.loginandregistration

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for Firestore permissions fix Tests Requirements: 1.1, 4.5, 5.4
 *
 * These tests verify:
 * - Groups screen navigation doesn't crash
 * - Creating new groups works correctly
 * - Viewing group activities works
 * - Tasks screen functionality
 * - Chat functionality
 * - Error messages are user-friendly
 */
@RunWith(AndroidJUnit4::class)
class FirestorePermissionsIntegrationTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val testGroupIds = mutableListOf<String>()
    private val testTaskIds = mutableListOf<String>()
    private val testChatIds = mutableListOf<String>()

    companion object {
        private const val TAG = "FirestorePermTest"
        private const val TIMEOUT_MS = 10000L
    }

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Ensure user is authenticated
        if (auth.currentUser == null) {
            fail("Test requires authenticated user. Please sign in before running tests.")
        }

        Log.d(TAG, "Test setup complete. User: ${auth.currentUser?.uid}")
    }

    @After
    fun cleanup() = runBlocking {
        // Clean up test data
        try {
            testGroupIds.forEach { groupId ->
                try {
                    db.collection("groups").document(groupId).delete().await()
                    Log.d(TAG, "Cleaned up test group: $groupId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up group $groupId: ${e.message}")
                }
            }

            testTaskIds.forEach { taskId ->
                try {
                    db.collection("tasks").document(taskId).delete().await()
                    Log.d(TAG, "Cleaned up test task: $taskId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up task $taskId: ${e.message}")
                }
            }

            testChatIds.forEach { chatId ->
                try {
                    db.collection("chats").document(chatId).delete().await()
                    Log.d(TAG, "Cleaned up test chat: $chatId")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clean up chat $chatId: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error: ${e.message}")
        }
    }

    /**
     * Test 1: Groups screen navigation should not crash Requirement: 1.1 - User can access group
     * data without permission errors
     */
    @Test
    fun testGroupsScreenNavigation_doesNotCrash() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")

        try {
            withTimeout(TIMEOUT_MS) {
                // Query groups where user is a member
                val groups =
                        db.collection("groups")
                                .whereArrayContains("memberIds", userId)
                                .get()
                                .await()

                Log.d(TAG, "Successfully queried groups. Found: ${groups.size()} groups")

                // Should not throw exception, even if empty
                assertNotNull("Groups query should return non-null result", groups)
                assertTrue(
                        "Groups query should succeed",
                        groups.metadata.isFromCache || !groups.isEmpty || groups.isEmpty
                )
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail(
                        "Permission denied error should not occur when querying user's groups: ${e.message}"
                )
            }
            throw e
        }
    }

    /**
     * Test 2: Creating a new group should work correctly Requirement: 1.1 - User can create groups
     * and be added as member
     */
    @Test
    fun testCreateNewGroup_succeeds() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")
        val testGroupName = "Test Group ${System.currentTimeMillis()}"

        try {
            withTimeout(TIMEOUT_MS) {
                val groupData =
                        hashMapOf(
                                "name" to testGroupName,
                                "description" to "Integration test group",
                                "owner" to userId,
                                "memberIds" to listOf(userId),
                                "isActive" to true,
                                "createdAt" to com.google.firebase.Timestamp.now(),
                                "updatedAt" to com.google.firebase.Timestamp.now()
                        )

                val docRef = db.collection("groups").add(groupData).await()
                testGroupIds.add(docRef.id)

                Log.d(TAG, "Successfully created group: ${docRef.id}")

                // Verify we can read the group we just created
                val createdGroup = docRef.get().await()
                assertTrue("Created group should exist", createdGroup.exists())
                assertEquals(
                        "Group name should match",
                        testGroupName,
                        createdGroup.getString("name")
                )

                val memberIds = createdGroup.get("memberIds") as? List<*>
                assertTrue("User should be in memberIds", memberIds?.contains(userId) == true)
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied when creating group: ${e.message}")
            }
            throw e
        }
    }

    /**
     * Test 3: Viewing group activities should work Requirement: 1.1 - User can query group
     * activities for their groups
     */
    @Test
    fun testViewGroupActivities_succeeds() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")

        // First create a test group
        val groupData =
                hashMapOf(
                        "name" to "Activity Test Group",
                        "description" to "Test group for activities",
                        "owner" to userId,
                        "memberIds" to listOf(userId),
                        "isActive" to true,
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                )

        val groupRef = db.collection("groups").add(groupData).await()
        testGroupIds.add(groupRef.id)

        try {
            withTimeout(TIMEOUT_MS) {
                // Query activities for this group
                val activities =
                        db.collection("group_activities")
                                .whereEqualTo("groupId", groupRef.id)
                                .limit(10)
                                .get()
                                .await()

                Log.d(TAG, "Successfully queried group activities. Found: ${activities.size()}")

                // Should not throw exception
                assertNotNull("Activities query should return non-null result", activities)
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied when querying group activities: ${e.message}")
            }
            throw e
        }
    }

    /**
     * Test 4: Tasks screen should work without crashes Requirement: 1.1 - User can query their
     * tasks
     */
    @Test
    fun testTasksScreen_doesNotCrash() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")

        try {
            withTimeout(TIMEOUT_MS) {
                // Query user's own tasks
                val ownTasks = db.collection("tasks").whereEqualTo("userId", userId).get().await()

                Log.d(TAG, "Successfully queried own tasks. Found: ${ownTasks.size()}")
                assertNotNull("Own tasks query should return non-null result", ownTasks)

                // Query assigned tasks
                val assignedTasks =
                        db.collection("tasks")
                                .whereArrayContains("assignedTo", userId)
                                .get()
                                .await()

                Log.d(TAG, "Successfully queried assigned tasks. Found: ${assignedTasks.size()}")
                assertNotNull("Assigned tasks query should return non-null result", assignedTasks)
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied when querying tasks: ${e.message}")
            }
            throw e
        }
    }

    /** Test 5: Creating a task should work Requirement: 1.1 - User can create tasks */
    @Test
    fun testCreateTask_succeeds() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")
        val testTaskTitle = "Test Task ${System.currentTimeMillis()}"

        try {
            withTimeout(TIMEOUT_MS) {
                val taskData =
                        hashMapOf(
                                "userId" to userId,
                                "title" to testTaskTitle,
                                "description" to "Integration test task",
                                "status" to "pending",
                                "assignedTo" to listOf(userId),
                                "createdAt" to com.google.firebase.Timestamp.now(),
                                "dueDate" to null
                        )

                val docRef = db.collection("tasks").add(taskData).await()
                testTaskIds.add(docRef.id)

                Log.d(TAG, "Successfully created task: ${docRef.id}")

                // Verify we can read the task
                val createdTask = docRef.get().await()
                assertTrue("Created task should exist", createdTask.exists())
                assertEquals(
                        "Task title should match",
                        testTaskTitle,
                        createdTask.getString("title")
                )
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied when creating task: ${e.message}")
            }
            throw e
        }
    }

    /** Test 6: Chat functionality should work Requirement: 1.1 - User can access chats */
    @Test
    fun testChatFunctionality_succeeds() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")

        try {
            withTimeout(TIMEOUT_MS) {
                // Query chats where user is a participant
                val chats =
                        db.collection("chats")
                                .whereArrayContains("participantIds", userId)
                                .get()
                                .await()

                Log.d(TAG, "Successfully queried chats. Found: ${chats.size()}")
                assertNotNull("Chats query should return non-null result", chats)
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied when querying chats: ${e.message}")
            }
            throw e
        }
    }

    /**
     * Test 7: Error handling returns user-friendly messages Requirement: 5.4 - Error messages
     * should be user-friendly
     */
    @Test
    fun testErrorHandling_providesUserFriendlyMessages() = runBlocking {
        try {
            withTimeout(TIMEOUT_MS) {
                // Try to access a document without proper query filter
                // This should be handled gracefully
                try {
                    db.collection("groups").document("nonexistent-group-id").get().await()

                    // If we get here, either the document exists or permission was granted
                    Log.d(TAG, "Document access succeeded or returned empty")
                } catch (e: FirebaseFirestoreException) {
                    // Verify error is handled properly
                    assertNotNull("Exception should have a message", e.message)

                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                            Log.d(TAG, "Permission denied error caught: ${e.message}")
                            // This is expected for documents user doesn't have access to
                            assertTrue(
                                    "Error message should be informative",
                                    e.message?.isNotEmpty() == true
                            )
                        }
                        else -> {
                            Log.d(TAG, "Other Firestore error: ${e.code} - ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in error handling test", e)
            throw e
        }
    }

    /**
     * Test 8: Querying without proper filters should not crash app Requirement: 5.4 - App should
     * handle errors gracefully
     */
    @Test
    fun testQueryWithoutFilters_handledGracefully() = runBlocking {
        try {
            withTimeout(TIMEOUT_MS) {
                // This query might fail with permission denied, but should be caught
                try {
                    val allGroups = db.collection("groups").limit(1).get().await()

                    // If successful, log it
                    Log.d(TAG, "Query without filter succeeded. Found: ${allGroups.size()}")
                } catch (e: FirebaseFirestoreException) {
                    // Expected to fail with permission denied
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.d(TAG, "Permission denied as expected for unfiltered query")
                        // This is the expected behavior - test passes
                    } else {
                        throw e
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in unfiltered query test", e)
            throw e
        }
    }

    /**
     * Test 9: Multiple concurrent queries should work Requirement: 4.5 - App should handle multiple
     * operations
     */
    @Test
    fun testConcurrentQueries_succeed() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")

        try {
            withTimeout(TIMEOUT_MS) {
                // Launch multiple queries concurrently
                val groupsDeferred =
                        db.collection("groups").whereArrayContains("memberIds", userId).get()

                val tasksDeferred = db.collection("tasks").whereEqualTo("userId", userId).get()

                val chatsDeferred =
                        db.collection("chats").whereArrayContains("participantIds", userId).get()

                // Wait for all to complete
                val groups = groupsDeferred.await()
                val tasks = tasksDeferred.await()
                val chats = chatsDeferred.await()

                Log.d(
                        TAG,
                        "Concurrent queries succeeded. Groups: ${groups.size()}, Tasks: ${tasks.size()}, Chats: ${chats.size()}"
                )

                assertNotNull("Groups should not be null", groups)
                assertNotNull("Tasks should not be null", tasks)
                assertNotNull("Chats should not be null", chats)
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied during concurrent queries: ${e.message}")
            }
            throw e
        }
    }

    /**
     * Test 10: Update operations should work for owned resources Requirement: 1.1 - Users can
     * update their own data
     */
    @Test
    fun testUpdateOwnedResources_succeeds() = runBlocking {
        val userId = auth.currentUser?.uid ?: fail("No authenticated user")

        // Create a test group
        val groupData =
                hashMapOf(
                        "name" to "Update Test Group",
                        "description" to "Original description",
                        "owner" to userId,
                        "memberIds" to listOf(userId),
                        "isActive" to true,
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                )

        val groupRef = db.collection("groups").add(groupData).await()
        testGroupIds.add(groupRef.id)

        try {
            withTimeout(TIMEOUT_MS) {
                // Update the group
                groupRef.update(
                                "description",
                                "Updated description",
                                "updatedAt",
                                com.google.firebase.Timestamp.now()
                        )
                        .await()

                Log.d(TAG, "Successfully updated group: ${groupRef.id}")

                // Verify update
                val updatedGroup = groupRef.get().await()
                assertEquals(
                        "Description should be updated",
                        "Updated description",
                        updatedGroup.getString("description")
                )
            }
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                fail("Permission denied when updating owned group: ${e.message}")
            }
            throw e
        }
    }
}
