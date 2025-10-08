package com.example.loginandregistration.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DashboardRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val groupsCollection = db.collection("groups")
    private val tasksCollection = db.collection("tasks")
    private val activitiesCollection = db.collection("group_activities")

    /** Provides real-time comprehensive dashboard statistics */
    fun getDashboardStatsFlow(): Flow<ComprehensiveDashboardStats> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(ComprehensiveDashboardStats())
            close()
            return@callbackFlow
        }

        // Listen to user's groups
        val groupsListener =
                groupsCollection
                        .whereArrayContains("memberIds", userId)
                        .whereEqualTo("isActive", true)
                        .addSnapshotListener { groupSnapshot, error ->
                            if (error != null) {
                                trySend(ComprehensiveDashboardStats())
                                return@addSnapshotListener
                            }

                            val groups =
                                    groupSnapshot?.toObjects(
                                            com.example.loginandregistration.models
                                                            .FirebaseGroup::class
                                                    .java
                                    )
                                            ?: emptyList()
                            val groupIds = groups.map { it.id }

                            // Listen to user's tasks
                            val tasksListener =
                                    tasksCollection.whereEqualTo("userId", userId)
                                            .addSnapshotListener { taskSnapshot, taskError ->
                                                if (taskError != null) {
                                                    trySend(
                                                            ComprehensiveDashboardStats(
                                                                    totalGroups = groups.size,
                                                                    myGroups = groups.size,
                                                                    adminGroups =
                                                                            groups.count {
                                                                                it.owner == userId
                                                                            }
                                                            )
                                                    )
                                                    return@addSnapshotListener
                                                }

                                                val tasks =
                                                        taskSnapshot?.toObjects(
                                                                com.example.loginandregistration
                                                                                .models
                                                                                .FirebaseTask::class
                                                                        .java
                                                        )
                                                                ?: emptyList()
                                                val now = com.google.firebase.Timestamp.now()

                                                // Calculate task statistics
                                                var overdueTasks = 0
                                                var dueTodayTasks = 0
                                                var completedTasks = 0
                                                var totalTasks = tasks.size
                                                var personalTasks = 0
                                                var groupTasks = 0
                                                var assignmentTasks = 0

                                                tasks.forEach { task ->
                                                    when (task.category) {
                                                        "personal" -> personalTasks++
                                                        "group" -> groupTasks++
                                                        "assignment" -> assignmentTasks++
                                                    }

                                                    when {
                                                        task.status == "completed" ->
                                                                completedTasks++
                                                        task.dueDate != null &&
                                                                task.dueDate!! < now ->
                                                                overdueTasks++
                                                        task.dueDate != null &&
                                                                isSameDay(task.dueDate!!, now) ->
                                                                dueTodayTasks++
                                                    }
                                                }

                                                // Listen to activities for message count
                                                if (groupIds.isNotEmpty()) {
                                                    val activitiesListener =
                                                            activitiesCollection
                                                                    .whereIn(
                                                                            "groupId",
                                                                            groupIds.take(10)
                                                                    ) // Firestore limit
                                                                    .orderBy(
                                                                            "createdAt",
                                                                            Query.Direction
                                                                                    .DESCENDING
                                                                    )
                                                                    .limit(50)
                                                                    .addSnapshotListener {
                                                                            activitySnapshot,
                                                                            activityError ->
                                                                        val activities =
                                                                                activitySnapshot
                                                                                        ?.toObjects(
                                                                                                com.example
                                                                                                                .loginandregistration
                                                                                                                .models
                                                                                                                .GroupActivity::class
                                                                                                        .java
                                                                                        )
                                                                                        ?: emptyList()
                                                                        val newMessages =
                                                                                activities
                                                                                        .filter {
                                                                                            it.type ==
                                                                                                    "message"
                                                                                        }
                                                                                        .size
                                                                        val activeAssignments =
                                                                                activities
                                                                                        .filter {
                                                                                            it.type ==
                                                                                                    "assignment"
                                                                                        }
                                                                                        .size

                                                                        trySend(
                                                                                ComprehensiveDashboardStats(
                                                                                        // Group
                                                                                        // stats
                                                                                        totalGroups =
                                                                                                groups.size,
                                                                                        myGroups =
                                                                                                groups.size,
                                                                                        adminGroups =
                                                                                                groups
                                                                                                        .count {
                                                                                                            it.owner ==
                                                                                                                    userId
                                                                                                        },
                                                                                        activeAssignments =
                                                                                                activeAssignments,
                                                                                        newMessages =
                                                                                                newMessages,

                                                                                        // Task
                                                                                        // stats
                                                                                        totalTasks =
                                                                                                totalTasks,
                                                                                        overdueTasks =
                                                                                                overdueTasks,
                                                                                        dueTodayTasks =
                                                                                                dueTodayTasks,
                                                                                        completedTasks =
                                                                                                completedTasks,
                                                                                        personalTasks =
                                                                                                personalTasks,
                                                                                        groupTasks =
                                                                                                groupTasks,
                                                                                        assignmentTasks =
                                                                                                assignmentTasks,

                                                                                        // Calculated stats
                                                                                        tasksDue =
                                                                                                overdueTasks +
                                                                                                        dueTodayTasks,
                                                                                        completionRate =
                                                                                                if (totalTasks >
                                                                                                                0
                                                                                                )
                                                                                                        (completedTasks *
                                                                                                                100) /
                                                                                                                totalTasks
                                                                                                else
                                                                                                        0
                                                                                )
                                                                        )
                                                                    }
                                                } else {
                                                    trySend(
                                                            ComprehensiveDashboardStats(
                                                                    totalTasks = totalTasks,
                                                                    overdueTasks = overdueTasks,
                                                                    dueTodayTasks = dueTodayTasks,
                                                                    completedTasks = completedTasks,
                                                                    personalTasks = personalTasks,
                                                                    groupTasks = groupTasks,
                                                                    assignmentTasks =
                                                                            assignmentTasks,
                                                                    tasksDue =
                                                                            overdueTasks +
                                                                                    dueTodayTasks,
                                                                    completionRate =
                                                                            if (totalTasks > 0)
                                                                                    (completedTasks *
                                                                                            100) /
                                                                                            totalTasks
                                                                            else 0
                                                            )
                                                    )
                                                }
                                            }
                        }

        awaitClose {
            // Cleanup listeners if needed
        }
    }

    private fun isSameDay(
            date1: com.google.firebase.Timestamp,
            date2: com.google.firebase.Timestamp
    ): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1.toDate() }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2.toDate() }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /** Get quick stats for widgets or summary views */
    suspend fun getQuickStats(): QuickStats {
        val userId = auth.currentUser?.uid ?: return QuickStats()
        return try {
            // Get groups count
            val groupsSnapshot =
                    groupsCollection
                            .whereArrayContains("memberIds", userId)
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()
            val groupsCount = groupsSnapshot.size()

            // Get tasks count
            val tasksSnapshot = tasksCollection.whereEqualTo("userId", userId).get().await()
            val tasks =
                    tasksSnapshot.toObjects(
                            com.example.loginandregistration.models.FirebaseTask::class.java
                    )
            val now = com.google.firebase.Timestamp.now()

            val overdue =
                    tasks.count {
                        it.dueDate != null && it.dueDate!! < now && it.status != "completed"
                    }
            val dueToday =
                    tasks.count {
                        it.dueDate != null &&
                                isSameDay(it.dueDate!!, now) &&
                                it.status != "completed"
                    }

            QuickStats(
                    groupsCount = groupsCount,
                    tasksDue = overdue + dueToday,
                    overdueTasks = overdue,
                    dueTodayTasks = dueToday
            )
        } catch (e: Exception) {
            QuickStats()
        }
    }
}

data class ComprehensiveDashboardStats(
        // Group statistics
        val totalGroups: Int = 0,
        val myGroups: Int = 0,
        val adminGroups: Int = 0,
        val activeAssignments: Int = 0,
        val newMessages: Int = 0,

        // Task statistics
        val totalTasks: Int = 0,
        val overdueTasks: Int = 0,
        val dueTodayTasks: Int = 0,
        val completedTasks: Int = 0,
        val personalTasks: Int = 0,
        val groupTasks: Int = 0,
        val assignmentTasks: Int = 0,

        // Calculated statistics
        val tasksDue: Int = 0,
        val completionRate: Int = 0
)

data class QuickStats(
        val groupsCount: Int = 0,
        val tasksDue: Int = 0,
        val overdueTasks: Int = 0,
        val dueTodayTasks: Int = 0
)
