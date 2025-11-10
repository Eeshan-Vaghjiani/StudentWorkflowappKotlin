package com.example.loginandregistration.repository

import android.content.Context
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.utils.TaskReminderScheduler
import com.example.loginandregistration.utils.safeFirestoreCall
import com.example.loginandregistration.validation.FirestoreDataValidator
import com.example.loginandregistration.validation.ValidationException
import com.example.loginandregistration.validation.ValidationResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TaskRepository(private val context: Context? = null) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val tasksCollection = db.collection("tasks")
    private val validator = FirestoreDataValidator()

    suspend fun getUserTasks(): Result<List<FirebaseTask>> =
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                val userId = auth.currentUser?.uid ?: return@withContext Result.success(emptyList())
                return@withContext safeFirestoreCall {
                    val snapshot =
                            tasksCollection
                                    .whereEqualTo("userId", userId)
                                    .orderBy("dueDate", Query.Direction.ASCENDING)
                                    .get()
                                    .await()

                    // Map documents to FirebaseTask objects with proper ID assignment
                    val tasks =
                            snapshot.documents.mapNotNull { doc ->
                                doc.toObject(FirebaseTask::class.java)?.copy(id = doc.id)
                            }
                    val duration = System.currentTimeMillis() - startTime
                    android.util.Log.d(
                            "TaskRepository",
                            "getUserTasks took ${duration}ms, found ${tasks.size} tasks"
                    )
                    tasks
                }
            }

    // Get user tasks with category filtering (supports assignedTo array)
    fun getUserTasks(category: String? = null): Flow<List<FirebaseTask>> =
            callbackFlow {
                        val userId = auth.currentUser?.uid
                        if (userId == null) {
                            trySend(emptyList())
                            close()
                            return@callbackFlow
                        }

                        var query = tasksCollection.whereEqualTo("userId", userId)

                        // Filter by category if specified and not "All"
                        if (category != null && category.lowercase() != "all") {
                            query = query.whereEqualTo("category", category.lowercase())
                        }

                        val listener =
                                query.orderBy("dueDate", Query.Direction.ASCENDING)
                                        .addSnapshotListener { snapshot, error ->
                                            if (error != null) {
                                                trySend(emptyList())
                                                return@addSnapshotListener
                                            }

                                            val tasks =
                                                    snapshot?.toObjects(FirebaseTask::class.java)
                                                            ?: emptyList()
                                            trySend(tasks)
                                        }

                        awaitClose { listener.remove() }
                    }
                    .flowOn(Dispatchers.IO)

    // Get tasks for specific date (for calendar integration)
    fun getTasksForDate(date: java.time.LocalDate): Flow<List<FirebaseTask>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val startOfDay =
                date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay =
                date.plusDays(1)
                        .atStartOfDay(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

        val listener =
                tasksCollection.whereEqualTo("userId", userId).addSnapshotListener { snapshot, error
                    ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val allTasks = snapshot?.toObjects(FirebaseTask::class.java) ?: emptyList()

                    // Filter tasks by date range (since Firestore doesn't support multiple range
                    // queries)
                    val tasksForDate =
                            allTasks.filter { task ->
                                val taskTime = task.dueDate?.toDate()?.time ?: 0
                                taskTime >= startOfDay && taskTime < endOfDay
                            }

                    trySend(tasksForDate)
                }

        awaitClose { listener.remove() }
    }

    // Get dates that have tasks (for calendar indicators)
    fun getDatesWithTasks(): Flow<Set<java.time.LocalDate>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        val listener =
                tasksCollection.whereEqualTo("userId", userId).addSnapshotListener { snapshot, error
                    ->
                    if (error != null) {
                        trySend(emptySet())
                        return@addSnapshotListener
                    }

                    val dates =
                            snapshot?.documents
                                    ?.mapNotNull { doc ->
                                        val task = doc.toObject(FirebaseTask::class.java)
                                        task?.dueDate
                                                ?.toDate()
                                                ?.toInstant()
                                                ?.atZone(java.time.ZoneId.systemDefault())
                                                ?.toLocalDate()
                                    }
                                    ?.toSet()
                                    ?: emptySet()

                    trySend(dates)
                }

        awaitClose { listener.remove() }
    }

    suspend fun createTask(task: FirebaseTask): Result<String> =
            withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                val userId =
                        auth.currentUser?.uid
                                ?: return@withContext Result.failure(
                                        Exception("User not authenticated")
                                )

                // Ensure all required fields are properly initialized
                val taskWithUser =
                        task.copy(
                                userId = userId,
                                createdAt = Timestamp.now(),
                                updatedAt = Timestamp.now(),
                                status = if (task.status.isEmpty()) "pending" else task.status,
                                category =
                                        if (task.category.isEmpty()) "personal" else task.category,
                                priority = if (task.priority.isEmpty()) "medium" else task.priority
                        )

                // Validate task data before Firestore operation
                val validationResult = validator.validateTask(taskWithUser)
                if (!validationResult.isValid) {
                    return@withContext Result.failure(ValidationException(validationResult))
                }

                return@withContext safeFirestoreCall {
                    // Add task to Firestore
                    val docRef = tasksCollection.add(taskWithUser).await()
                    val taskId = docRef.id

                    // Schedule reminder notification if context is available
                    context?.let {
                        val taskWithId = taskWithUser.copy(id = taskId)
                        TaskReminderScheduler.scheduleReminder(it, taskWithId)
                    }

                    val duration = System.currentTimeMillis() - startTime
                    android.util.Log.d(
                            "TaskRepository",
                            "Task created successfully with ID: $taskId in ${duration}ms"
                    )
                    taskId
                }
            }

    suspend fun updateTask(task: FirebaseTask): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val taskId = task.id
                    if (taskId.isEmpty()) {
                        return@withContext Result.failure(Exception("Task ID is required"))
                    }

                    // Update the task with current timestamp
                    val updatedTask = task.copy(updatedAt = Timestamp.now())

                    // Validate basic fields (skip timestamp validation for updates)
                    // Only validate required fields and constraints
                    val errors = mutableListOf<String>()
                    
                    if (updatedTask.title.isBlank()) {
                        errors.add("Task title is required")
                    } else if (updatedTask.title.length > 500) {
                        errors.add("Task title must not exceed 500 characters")
                    }
                    
                    if (updatedTask.userId.isBlank()) {
                        errors.add("User ID is required")
                    }
                    
                    if (updatedTask.assignedTo.isEmpty()) {
                        errors.add("At least one assignee is required")
                    } else if (updatedTask.assignedTo.size > 50) {
                        errors.add("Maximum 50 assignees allowed")
                    }
                    
                    if (updatedTask.description.length > 5000) {
                        errors.add("Task description must not exceed 5000 characters")
                    }
                    
                    if (errors.isNotEmpty()) {
                        return@withContext Result.failure(
                            ValidationException(ValidationResult.failure(errors))
                        )
                    }

                    // Perform the update using safeFirestoreCall
                    val result: Result<Unit> =
                            safeFirestoreCall("updateTask") {
                                tasksCollection.document(taskId).set(updatedTask).await()
                                Unit
                            }

                    // Reschedule reminder if needed
                    if (result.isSuccess) {
                        context?.let {
                            if (updatedTask.status == "completed") {
                                TaskReminderScheduler.cancelReminder(it, taskId)
                            } else {
                                TaskReminderScheduler.rescheduleReminder(it, updatedTask)
                            }
                        }
                    }

                    return@withContext result
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

    suspend fun updateTask(taskId: String, updates: Map<String, Any>): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    // Fetch current task to validate the updated version
                    val currentTask =
                            tasksCollection
                                    .document(taskId)
                                    .get()
                                    .await()
                                    .toObject(FirebaseTask::class.java)
                                    ?: return@withContext Result.failure(
                                            Exception("Task not found")
                                    )

                    // Apply updates to create a complete task object for validation
                    val updatedTaskData =
                            currentTask.copy(
                                    title = updates["title"] as? String ?: currentTask.title,
                                    description = updates["description"] as? String
                                                    ?: currentTask.description,
                                    status = updates["status"] as? String ?: currentTask.status,
                                    priority = updates["priority"] as? String
                                                    ?: currentTask.priority,
                                    category = updates["category"] as? String
                                                    ?: currentTask.category,
                                    dueDate = updates["dueDate"] as? Timestamp
                                                    ?: currentTask.dueDate,
                                    assignedTo = updates["assignedTo"] as? List<String>
                                                    ?: currentTask.assignedTo,
                                    updatedAt = Timestamp.now()
                            )

                    // Validate the updated task
                    val validationResult = validator.validateTask(updatedTaskData)
                    if (!validationResult.isValid) {
                        return@withContext Result.failure(ValidationException(validationResult))
                    }

                    // Perform the update using safeFirestoreCall
                    @Suppress("UNCHECKED_CAST")
                    val result: Result<Unit> =
                            safeFirestoreCall("updateTask") {
                                val updatesWithTimestamp = updates.toMutableMap()
                                updatesWithTimestamp["updatedAt"] = Timestamp.now()
                                tasksCollection
                                        .document(taskId)
                                        .update(updatesWithTimestamp)
                                        .await() as
                                        Unit
                            }

                    // If due date or status changed, reschedule reminder
                    if (result.isSuccess) {
                        context?.let {
                            if (updates.containsKey("dueDate") || updates.containsKey("status")) {
                                // Fetch updated task to reschedule
                                val updatedTask =
                                        tasksCollection
                                                .document(taskId)
                                                .get()
                                                .await()
                                                .toObject(FirebaseTask::class.java)
                                updatedTask?.let { task ->
                                    if (task.status == "completed") {
                                        TaskReminderScheduler.cancelReminder(it, taskId)
                                    } else {
                                        TaskReminderScheduler.rescheduleReminder(it, task)
                                    }
                                }
                            }
                        }
                    }

                    return@withContext result
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

    suspend fun deleteTask(taskId: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                return@withContext safeFirestoreCall {
                    // Cancel reminder before deleting task
                    context?.let { TaskReminderScheduler.cancelReminder(it, taskId) }

                    tasksCollection.document(taskId).delete().await()
                }
                        .map { Unit }
            }

    suspend fun getTaskStats(): TaskStats =
            withContext(Dispatchers.IO) {
                val userId = auth.currentUser?.uid ?: return@withContext TaskStats()
                return@withContext try {
                    val result = getUserTasks()
                    val tasks = result.getOrElse { emptyList() }
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
    suspend fun getGroupTasks(groupId: String): List<FirebaseTask> =
            withContext(Dispatchers.IO) {
                return@withContext try {
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

    suspend fun assignTaskToUser(taskId: String, assignedUserId: String): Boolean =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    tasksCollection
                            .document(taskId)
                            .update(
                                    mapOf(
                                            "userId" to assignedUserId,
                                            "updatedAt" to Timestamp.now()
                                    )
                            )
                            .await()
                    true
                } catch (e: Exception) {
                    false
                }
            }

    suspend fun completeTask(taskId: String): Boolean =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    // Cancel reminder when task is completed
                    context?.let { TaskReminderScheduler.cancelReminder(it, taskId) }

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

    suspend fun getDashboardTaskStats(): DashboardTaskStats =
            withContext(Dispatchers.IO) {
                val userId = auth.currentUser?.uid ?: return@withContext DashboardTaskStats()
                return@withContext try {
                    val result = getUserTasks()
                    val tasks = result.getOrElse { emptyList() }
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

    // Real-time listeners for task statistics
    fun getUserTasksFlow(): Flow<List<FirebaseTask>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener =
                tasksCollection
                        .whereEqualTo("userId", userId)
                        .orderBy("dueDate", Query.Direction.ASCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            val tasks = snapshot?.toObjects(FirebaseTask::class.java) ?: emptyList()
                            trySend(tasks)
                        }

        awaitClose { listener.remove() }
    }

    fun getTaskStatsFlow(): Flow<TaskStats> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(TaskStats())
            close()
            return@callbackFlow
        }

        val listener =
                tasksCollection.whereEqualTo("userId", userId).addSnapshotListener { snapshot, error
                    ->
                    if (error != null) {
                        trySend(TaskStats())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot?.toObjects(FirebaseTask::class.java) ?: emptyList()
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

                    trySend(TaskStats(overdue, dueToday, completed))
                }

        awaitClose { listener.remove() }
    }

    fun getDashboardTaskStatsFlow(): Flow<DashboardTaskStats> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(DashboardTaskStats())
            close()
            return@callbackFlow
        }

        val listener =
                tasksCollection.whereEqualTo("userId", userId).addSnapshotListener { snapshot, error
                    ->
                    if (error != null) {
                        trySend(DashboardTaskStats())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot?.toObjects(FirebaseTask::class.java) ?: emptyList()
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

                    trySend(DashboardTaskStats(tasksDue, overdue, dueToday, completed))
                }

        awaitClose { listener.remove() }
    }

    suspend fun getTasksByCategory(category: String): List<FirebaseTask> =
            withContext(Dispatchers.IO) {
                val userId = auth.currentUser?.uid ?: return@withContext emptyList()
                return@withContext try {
                    tasksCollection
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("category", category)
                            .orderBy("dueDate", Query.Direction.ASCENDING)
                            .get()
                            .await()
                            .toObjects(FirebaseTask::class.java)
                } catch (e: Exception) {
                    emptyList()
                }
            }

    suspend fun getTasksByStatus(status: String): List<FirebaseTask> =
            withContext(Dispatchers.IO) {
                val userId = auth.currentUser?.uid ?: return@withContext emptyList()
                return@withContext try {
                    tasksCollection
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("status", status)
                            .orderBy("dueDate", Query.Direction.ASCENDING)
                            .get()
                            .await()
                            .toObjects(FirebaseTask::class.java)
                } catch (e: Exception) {
                    emptyList()
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
