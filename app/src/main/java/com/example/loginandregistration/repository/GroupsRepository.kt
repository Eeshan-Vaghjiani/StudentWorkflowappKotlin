// app/src/main/java/com/example/loginandregistration/repository/GroupsRepository.kt
package com.example.loginandregistration.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for Groups screen data. Handles all Firestore operations with proper coroutine context
 * switching.
 */
class GroupsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Gets the count of groups where the current user is a member. Uses IO dispatcher to avoid
     * blocking the main thread.
     */
    suspend fun getMyGroupsCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val userId = auth.currentUser?.uid ?: return@withContext 0

                    val snapshot =
                            db.collection("groups")
                                    .whereArrayContains("memberIds", userId)
                                    .whereEqualTo("isActive", true)
                                    .get()
                                    .await()

                    snapshot.size()
                } catch (e: Exception) {
                    // Log error in production
                    android.util.Log.e("GroupsRepository", "Error getting groups count", e)
                    0
                }
            }

    /**
     * Gets the count of active assignments for the current user. Active assignments are tasks that
     * are not completed.
     */
    suspend fun getActiveAssignmentsCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val userId = auth.currentUser?.uid ?: return@withContext 0

                    val snapshot =
                            db.collection("tasks")
                                    .whereEqualTo("userId", userId)
                                    .whereNotEqualTo("status", "completed")
                                    .get()
                                    .await()

                    snapshot.size()
                } catch (e: Exception) {
                    android.util.Log.e("GroupsRepository", "Error getting assignments count", e)
                    0
                }
            }

    /**
     * Gets the count of new messages for the current user. This is a placeholder implementation -
     * in a real app, you'd track read/unread status.
     */
    suspend fun getNewMessagesCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val userId = auth.currentUser?.uid ?: return@withContext 0

                    // Get user's groups first
                    val groupsSnapshot =
                            db.collection("groups")
                                    .whereArrayContains("memberIds", userId)
                                    .whereEqualTo("isActive", true)
                                    .get()
                                    .await()

                    val groupIds = groupsSnapshot.documents.map { it.id }

                    if (groupIds.isEmpty()) return@withContext 0

                    // Count recent activities of type "message" in user's groups
                    // Note: Firestore has a limit of 10 items for 'in' queries
                    val limitedGroupIds = groupIds.take(10)

                    val activitiesSnapshot =
                            db.collection("group_activities")
                                    .whereIn("groupId", limitedGroupIds)
                                    .whereEqualTo("type", "message")
                                    .get()
                                    .await()

                    activitiesSnapshot.size()
                } catch (e: Exception) {
                    android.util.Log.e("GroupsRepository", "Error getting messages count", e)
                    0
                }
            }

    /** Gets total tasks count for additional statistics. */
    suspend fun getTotalTasksCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val userId = auth.currentUser?.uid ?: return@withContext 0

                    val snapshot =
                            db.collection("tasks").whereEqualTo("userId", userId).get().await()

                    snapshot.size()
                } catch (e: Exception) {
                    android.util.Log.e("GroupsRepository", "Error getting total tasks count", e)
                    0
                }
            }

    /** Gets completed tasks count for completion rate calculation. */
    suspend fun getCompletedTasksCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val userId = auth.currentUser?.uid ?: return@withContext 0

                    val snapshot =
                            db.collection("tasks")
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("status", "completed")
                                    .get()
                                    .await()

                    snapshot.size()
                } catch (e: Exception) {
                    android.util.Log.e("GroupsRepository", "Error getting completed tasks count", e)
                    0
                }
            }

    /** Gets overdue tasks count. */
    suspend fun getOverdueTasksCount(): Int =
            withContext(Dispatchers.IO) {
                try {
                    val userId = auth.currentUser?.uid ?: return@withContext 0
                    val now = com.google.firebase.Timestamp.now()

                    val snapshot =
                            db.collection("tasks")
                                    .whereEqualTo("userId", userId)
                                    .whereNotEqualTo("status", "completed")
                                    .whereLessThan("dueDate", now)
                                    .get()
                                    .await()

                    snapshot.size()
                } catch (e: Exception) {
                    android.util.Log.e("GroupsRepository", "Error getting overdue tasks count", e)
                    0
                }
            }
}
