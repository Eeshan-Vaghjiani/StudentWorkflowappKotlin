package com.example.loginandregistration.repository

import com.example.loginandregistration.models.FirebaseTask
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tasksCollection = db.collection("tasks")

    suspend fun getUserTasks(): List<FirebaseTask> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            tasksCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("dueDate", Query.Direction.ASCENDING)
                    .get()
                    .await()
                    .toObjects(FirebaseTask::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createTask(task: FirebaseTask): String? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val taskWithUser =
                    task.copy(
                            userId = userId,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now()
                    )
            val docRef = tasksCollection.add(taskWithUser).await()
            docRef.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateTask(taskId: String, updates: Map<String, Any>): Boolean {
        return try {
            val updatesWithTimestamp = updates.toMutableMap()
            updatesWithTimestamp["updatedAt"] = Timestamp.now()
            tasksCollection.document(taskId).update(updatesWithTimestamp).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTask(taskId: String): Boolean {
        return try {
            tasksCollection.document(taskId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTaskStats(): TaskStats {
        val userId = auth.currentUser?.uid ?: return TaskStats()
        return try {
            val tasks = getUserTasks()
            val now = Timestamp.now()

            var overdue = 0
            var dueToday = 0
            var completed = 0

            tasks.forEach { task ->
                when {
                    task.status == "completed" -> completed++
                    task.dueDate != null && task.dueDate!! < now -> overdue++
                    task.dueDate != null && isSameDay(task.dueDate!!, now) -> dueToday++
                }
            }

            TaskStats(overdue, dueToday, completed)
        } catch (e: Exception) {
            TaskStats()
        }
    }

    private fun isSameDay(date1: Timestamp, date2: Timestamp): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1.toDate() }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2.toDate() }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
    suspend fun getGroupTasks(groupId: String): List<FirebaseTask> {
        return try {
            tasksCollection
                    .whereEqualTo("groupId", groupId)
                    .orderBy("dueDate", Query.Direction.ASCENDING)
                    .get()
                    .await()
                    .toObjects(FirebaseTask::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun assignTaskToUser(taskId: String, assignedUserId: String): Boolean {
        return try {
            tasksCollection
                    .document(taskId)
                    .update(mapOf("userId" to assignedUserId, "updatedAt" to Timestamp.now()))
                    .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun completeTask(taskId: String): Boolean {
        return try {
            tasksCollection
                    .document(taskId)
                    .update(
                            mapOf(
                                    "status" to "completed",
                                    "completedAt" to Timestamp.now(),
                                    "updatedAt" to Timestamp.now()
                            )
                    )
                    .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getDashboardTaskStats(): DashboardTaskStats {
        val userId = auth.currentUser?.uid ?: return DashboardTaskStats()
        return try {
            val tasks = getUserTasks()
            val now = Timestamp.now()

            var tasksDue = 0
            var overdue = 0
            var dueToday = 0
            var completed = 0

            tasks.forEach { task ->
                when {
                    task.status == "completed" -> completed++
                    task.dueDate != null && task.dueDate!! < now -> {
                        overdue++
                        tasksDue++
                    }
                    task.dueDate != null && isSameDay(task.dueDate!!, now) -> {
                        dueToday++
                        tasksDue++
                    }
                    task.dueDate != null -> tasksDue++
                }
            }

            DashboardTaskStats(
                    tasksDue = tasksDue,
                    overdue = overdue,
                    dueToday = dueToday,
                    completed = completed
            )
        } catch (e: Exception) {
            DashboardTaskStats()
        }
    }
}

data class TaskStats(val overdue: Int = 0, val dueToday: Int = 0, val completed: Int = 0)

data class DashboardTaskStats(
        val tasksDue: Int = 0,
        val overdue: Int = 0,
        val dueToday: Int = 0,
        val completed: Int = 0
)
