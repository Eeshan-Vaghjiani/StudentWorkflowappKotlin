package com.example.loginandregistration.repository

import com.example.loginandregistration.models.FirebaseTask
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskManagementRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tasksCollection = db.collection("tasks")

    /** Get tasks with advanced filtering and sorting */
    suspend fun getTasksWithFilters(
            category: String? = null,
            status: String? = null,
            priority: String? = null,
            sortBy: TaskSortOption = TaskSortOption.DUE_DATE,
            sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<FirebaseTask> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            var query = tasksCollection.whereEqualTo("userId", userId)

            // Apply filters
            category?.let { query = query.whereEqualTo("category", it) }
            status?.let { query = query.whereEqualTo("status", it) }
            priority?.let { query = query.whereEqualTo("priority", it) }

            // Apply sorting
            val orderDirection =
                    if (sortOrder == SortOrder.ASCENDING) Query.Direction.ASCENDING
                    else Query.Direction.DESCENDING

            query =
                    when (sortBy) {
                        TaskSortOption.DUE_DATE -> query.orderBy("dueDate", orderDirection)
                        TaskSortOption.CREATED_DATE -> query.orderBy("createdAt", orderDirection)
                        TaskSortOption.PRIORITY -> query.orderBy("priority", orderDirection)
                        TaskSortOption.TITLE -> query.orderBy("title", orderDirection)
                        TaskSortOption.STATUS -> query.orderBy("status", orderDirection)
                    }

            query.get().await().toObjects(FirebaseTask::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Real-time filtered tasks flow */
    fun getFilteredTasksFlow(
            category: String? = null,
            status: String? = null,
            priority: String? = null
    ): Flow<List<FirebaseTask>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var query = tasksCollection.whereEqualTo("userId", userId)

        // Apply filters
        category?.let { query = query.whereEqualTo("category", it) }
        status?.let { query = query.whereEqualTo("status", it) }
        priority?.let { query = query.whereEqualTo("priority", it) }

        val listener =
                query.orderBy("dueDate", Query.Direction.ASCENDING).addSnapshotListener {
                        snapshot,
                        error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot?.toObjects(FirebaseTask::class.java) ?: emptyList()
                    trySend(tasks)
                }

        awaitClose { listener.remove() }
    }

    /** Bulk operations for tasks */
    suspend fun bulkUpdateTasks(taskIds: List<String>, updates: Map<String, Any>): Boolean {
        return try {
            val batch = db.batch()
            val updatesWithTimestamp = updates.toMutableMap()
            updatesWithTimestamp["updatedAt"] = Timestamp.now()

            taskIds.forEach { taskId ->
                val taskRef = tasksCollection.document(taskId)
                batch.update(taskRef, updatesWithTimestamp)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun bulkDeleteTasks(taskIds: List<String>): Boolean {
        return try {
            val batch = db.batch()

            taskIds.forEach { taskId ->
                val taskRef = tasksCollection.document(taskId)
                batch.delete(taskRef)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markTasksAsCompleted(taskIds: List<String>): Boolean {
        return bulkUpdateTasks(
                taskIds,
                mapOf("status" to "completed", "completedAt" to Timestamp.now())
        )
    }

    /** Advanced task analytics */
    suspend fun getTaskAnalytics(): TaskAnalytics {
        val userId = auth.currentUser?.uid ?: return TaskAnalytics()

        return try {
            val tasks =
                    tasksCollection
                            .whereEqualTo("userId", userId)
                            .get()
                            .await()
                            .toObjects(FirebaseTask::class.java)

            val now = Timestamp.now()
            val oneWeekAgo = Timestamp(java.util.Date(now.toDate().time - 7 * 24 * 60 * 60 * 1000))
            val oneMonthAgo =
                    Timestamp(java.util.Date(now.toDate().time - 30L * 24 * 60 * 60 * 1000))

            // Category breakdown
            val categoryBreakdown = tasks.groupBy { it.category }.mapValues { it.value.size }

            // Priority breakdown
            val priorityBreakdown = tasks.groupBy { it.priority }.mapValues { it.value.size }

            // Status breakdown
            val statusBreakdown = tasks.groupBy { it.status }.mapValues { it.value.size }

            // Time-based analytics
            val tasksCreatedThisWeek =
                    tasks.count { it.createdAt != null && it.createdAt!! > oneWeekAgo }
            val tasksCompletedThisWeek =
                    tasks.count { it.completedAt != null && it.completedAt!! > oneWeekAgo }
            val tasksCreatedThisMonth =
                    tasks.count { it.createdAt != null && it.createdAt!! > oneMonthAgo }
            val tasksCompletedThisMonth =
                    tasks.count { it.completedAt != null && it.completedAt!! > oneMonthAgo }

            // Overdue analysis
            val overdueTasks =
                    tasks.filter {
                        it.dueDate != null && it.dueDate!! < now && it.status != "completed"
                    }
            val averageCompletionTime = calculateAverageCompletionTime(tasks)

            TaskAnalytics(
                    totalTasks = tasks.size,
                    categoryBreakdown = categoryBreakdown,
                    priorityBreakdown = priorityBreakdown,
                    statusBreakdown = statusBreakdown,
                    tasksCreatedThisWeek = tasksCreatedThisWeek,
                    tasksCompletedThisWeek = tasksCompletedThisWeek,
                    tasksCreatedThisMonth = tasksCreatedThisMonth,
                    tasksCompletedThisMonth = tasksCompletedThisMonth,
                    overdueTasks = overdueTasks.size,
                    averageCompletionDays = averageCompletionTime,
                    completionRate =
                            if (tasks.isNotEmpty())
                                    (tasks.count { it.status == "completed" } * 100) / tasks.size
                            else 0
            )
        } catch (e: Exception) {
            TaskAnalytics()
        }
    }

    private fun calculateAverageCompletionTime(tasks: List<FirebaseTask>): Double {
        val completedTasks =
                tasks.filter {
                    it.status == "completed" && it.createdAt != null && it.completedAt != null
                }

        if (completedTasks.isEmpty()) return 0.0

        val totalDays =
                completedTasks.sumOf { task ->
                    val created = task.createdAt!!.toDate().time
                    val completed = task.completedAt!!.toDate().time
                    val diffInDays = (completed - created) / (24 * 60 * 60 * 1000.0)
                    diffInDays
                }

        return totalDays / completedTasks.size
    }

    /** Search tasks by title or description */
    suspend fun searchTasks(query: String): List<FirebaseTask> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            // Note: Firestore doesn't support full-text search natively
            // This is a simple implementation that gets all user tasks and filters locally
            val allTasks =
                    tasksCollection
                            .whereEqualTo("userId", userId)
                            .get()
                            .await()
                            .toObjects(FirebaseTask::class.java)

            allTasks.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true) ||
                        task.subject.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Get tasks due in the next N days */
    suspend fun getTasksDueInDays(days: Int): List<FirebaseTask> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        return try {
            val now = Timestamp.now()
            val futureDate =
                    Timestamp(java.util.Date(now.toDate().time + days * 24 * 60 * 60 * 1000L))

            tasksCollection
                    .whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("dueDate", now)
                    .whereLessThanOrEqualTo("dueDate", futureDate)
                    .whereNotEqualTo("status", "completed")
                    .orderBy("dueDate", Query.Direction.ASCENDING)
                    .get()
                    .await()
                    .toObjects(FirebaseTask::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

enum class TaskSortOption {
    DUE_DATE,
    CREATED_DATE,
    PRIORITY,
    TITLE,
    STATUS
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

data class TaskAnalytics(
        val totalTasks: Int = 0,
        val categoryBreakdown: Map<String, Int> = emptyMap(),
        val priorityBreakdown: Map<String, Int> = emptyMap(),
        val statusBreakdown: Map<String, Int> = emptyMap(),
        val tasksCreatedThisWeek: Int = 0,
        val tasksCompletedThisWeek: Int = 0,
        val tasksCreatedThisMonth: Int = 0,
        val tasksCompletedThisMonth: Int = 0,
        val overdueTasks: Int = 0,
        val averageCompletionDays: Double = 0.0,
        val completionRate: Int = 0
)
