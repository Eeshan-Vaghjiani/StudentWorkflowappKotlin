package com.example.loginandregistration.repository

import com.example.loginandregistration.models.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlin.random.Random
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EnhancedGroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val groupsCollection = db.collection("groups")
    private val activitiesCollection = db.collection("group_activities")
    private val usersCollection = db.collection("users")

    // ==================== GROUP OPERATIONS ====================

    suspend fun createGroup(
            name: String,
            description: String,
            subject: String = "",
            isPublic: Boolean = false
    ): Result<String> {
        val user = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val joinCode = generateJoinCode()
            val owner =
                    GroupMember(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: "Unknown",
                            role = "owner",
                            joinedAt = Timestamp.now()
                    )

            val group =
                    FirebaseGroup(
                            name = name,
                            description = description,
                            subject = subject,
                            owner = user.uid,
                            joinCode = joinCode,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now(),
                            members = listOf(owner),
                            tasks = emptyList(),
                            settings =
                                    GroupSettings(
                                            isPublic = isPublic,
                                            allowMemberInvites = true,
                                            maxMembers = 50
                                    )
                    )

            val docRef = groupsCollection.add(group).await()

            // Add activity
            addGroupActivity(
                    groupId = docRef.id,
                    type = "group_created",
                    title = "Group created",
                    description = "Group \"$name\" was created"
            )

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserGroups(): Flow<List<FirebaseGroup>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener =
                groupsCollection
                        .whereArrayContains("members.userId", userId)
                        .whereEqualTo("isActive", true)
                        .orderBy("updatedAt", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val groups =
                                    snapshot?.toObjects(FirebaseGroup::class.java) ?: emptyList()
                            trySend(groups)
                        }

        awaitClose { listener.remove() }
    }

    suspend fun getGroupById(groupId: String): Result<FirebaseGroup> {
        return try {
            val snapshot = groupsCollection.document(groupId).get().await()
            val group = snapshot.toObject(FirebaseGroup::class.java)
            if (group != null) {
                Result.success(group)
            } else {
                Result.failure(Exception("Group not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroupByCode(joinCode: String): Result<String> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        val userEmail = auth.currentUser?.email ?: ""
        val userName = auth.currentUser?.displayName ?: "Unknown"

        return try {
            val querySnapshot =
                    groupsCollection
                            .whereEqualTo("joinCode", joinCode.uppercase())
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()

            if (querySnapshot.documents.isEmpty()) {
                return Result.failure(Exception("Invalid join code"))
            }

            val groupDoc = querySnapshot.documents.first()
            val group =
                    groupDoc.toObject(FirebaseGroup::class.java)
                            ?: return Result.failure(Exception("Group data error"))

            // Check if user is already a member
            if (group.members.any { it.userId == userId }) {
                return Result.failure(Exception("You are already a member of this group"))
            }

            // Check member limit
            if (group.members.size >= group.settings.maxMembers) {
                return Result.failure(Exception("Group has reached maximum member limit"))
            }

            // Add new member
            val newMember =
                    GroupMember(
                            userId = userId,
                            email = userEmail,
                            displayName = userName,
                            role = "member",
                            joinedAt = Timestamp.now()
                    )

            val updatedMembers = group.members + newMember

            groupsCollection
                    .document(groupDoc.id)
                    .update(mapOf("members" to updatedMembers, "updatedAt" to Timestamp.now()))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupDoc.id,
                    type = "member_joined",
                    title = "New member joined",
                    description = "$userName joined the group"
            )

            Result.success(groupDoc.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGroupSettings(
            groupId: String,
            name: String? = null,
            description: String? = null,
            settings: GroupSettings? = null
    ): Result<Unit> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            // Check if user is owner
            val group = getGroupById(groupId).getOrThrow()
            if (group.owner != userId) {
                return Result.failure(Exception("Only group owner can update settings"))
            }

            val updates = mutableMapOf<String, Any>("updatedAt" to Timestamp.now())

            name?.let { updates["name"] = it }
            description?.let { updates["description"] = it }
            settings?.let { updates["settings"] = it }

            groupsCollection.document(groupId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun regenerateJoinCode(groupId: String): Result<String> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val group = getGroupById(groupId).getOrThrow()
            if (group.owner != userId) {
                return Result.failure(Exception("Only group owner can regenerate join code"))
            }

            val newCode = generateJoinCode()
            groupsCollection
                    .document(groupId)
                    .update(mapOf("joinCode" to newCode, "updatedAt" to Timestamp.now()))
                    .await()

            Result.success(newCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== MEMBER OPERATIONS ====================

    suspend fun addMemberByEmail(groupId: String, memberEmail: String): Result<Unit> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val group = getGroupById(groupId).getOrThrow()

            // Check permissions
            val currentMember = group.members.find { it.userId == userId }
            if (currentMember?.role !in listOf("owner", "admin")) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            // Find user by email
            val userQuery =
                    usersCollection.whereEqualTo("email", memberEmail.lowercase()).get().await()

            if (userQuery.documents.isEmpty()) {
                return Result.failure(Exception("User not found"))
            }

            val userData = userQuery.documents.first().data
            val targetUserId =
                    userData?.get("uid") as? String
                            ?: return Result.failure(Exception("Invalid user data"))

            // Check if already a member
            if (group.members.any { it.userId == targetUserId }) {
                return Result.failure(Exception("User is already a member"))
            }

            // Add new member
            val newMember =
                    GroupMember(
                            userId = targetUserId,
                            email = memberEmail,
                            displayName = userData["displayName"] as? String ?: "Unknown",
                            role = "member",
                            joinedAt = Timestamp.now()
                    )

            val updatedMembers = group.members + newMember

            groupsCollection
                    .document(groupId)
                    .update(mapOf("members" to updatedMembers, "updatedAt" to Timestamp.now()))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupId,
                    type = "member_added",
                    title = "Member added",
                    description = "${newMember.displayName} was added to the group"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(groupId: String, targetUserId: String): Result<Unit> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val group = getGroupById(groupId).getOrThrow()

            // Check permissions
            val currentMember = group.members.find { it.userId == userId }
            val targetMember = group.members.find { it.userId == targetUserId }

            if (currentMember?.role !in listOf("owner", "admin") && userId != targetUserId) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            if (targetMember?.role == "owner") {
                return Result.failure(Exception("Cannot remove group owner"))
            }

            val updatedMembers = group.members.filter { it.userId != targetUserId }

            groupsCollection
                    .document(groupId)
                    .update(mapOf("members" to updatedMembers, "updatedAt" to Timestamp.now()))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupId,
                    type = "member_removed",
                    title = "Member removed",
                    description = "${targetMember?.displayName ?: "A member"} left the group"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMemberRole(
            groupId: String,
            targetUserId: String,
            newRole: String
    ): Result<Unit> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val group = getGroupById(groupId).getOrThrow()

            // Only owner can change roles
            if (group.owner != userId) {
                return Result.failure(Exception("Only group owner can change member roles"))
            }

            val updatedMembers =
                    group.members.map { member ->
                        if (member.userId == targetUserId) {
                            member.copy(role = newRole)
                        } else {
                            member
                        }
                    }

            groupsCollection
                    .document(groupId)
                    .update(mapOf("members" to updatedMembers, "updatedAt" to Timestamp.now()))
                    .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== TASK OPERATIONS ====================

    suspend fun createTask(
            groupId: String,
            title: String,
            description: String,
            assignedTo: List<String> = emptyList(),
            priority: String = "medium",
            dueDate: Timestamp? = null
    ): Result<String> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val group = getGroupById(groupId).getOrThrow()

            // Check if user is member
            if (!group.members.any { it.userId == userId }) {
                return Result.failure(Exception("You are not a member of this group"))
            }

            val taskId = db.collection("temp").document().id // Generate unique ID
            val task =
                    GroupTask(
                            id = taskId,
                            title = title,
                            description = description,
                            assignedTo = assignedTo,
                            createdBy = userId,
                            status = "pending",
                            priority = priority,
                            dueDate = dueDate,
                            createdAt = Timestamp.now(),
                            updatedAt = Timestamp.now()
                    )

            val updatedTasks = group.tasks + task

            groupsCollection
                    .document(groupId)
                    .update(mapOf("tasks" to updatedTasks, "updatedAt" to Timestamp.now()))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupId,
                    type = "task_created",
                    title = "New task created",
                    description = "Task \"$title\" was created"
            )

            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaskStatus(groupId: String, taskId: String, newStatus: String): Result<Unit> {
        val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val group = getGroupById(groupId).getOrThrow()

            val updatedTasks =
                    group.tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(status = newStatus, updatedAt = Timestamp.now())
                        } else {
                            task
                        }
                    }

            groupsCollection
                    .document(groupId)
                    .update(mapOf("tasks" to updatedTasks, "updatedAt" to Timestamp.now()))
                    .await()

            // Add activity if completed
            if (newStatus == "completed") {
                val task = group.tasks.find { it.id == taskId }
                addGroupActivity(
                        groupId = groupId,
                        type = "task_completed",
                        title = "Task completed",
                        description = "Task \"${task?.title}\" was completed"
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== UTILITY FUNCTIONS ====================

    private suspend fun addGroupActivity(
            groupId: String,
            type: String,
            title: String,
            description: String
    ) {
        try {
            val activity =
                    GroupActivity(
                            groupId = groupId,
                            type = type,
                            title = title,
                            description = description,
                            userId = auth.currentUser?.uid ?: "",
                            userName = auth.currentUser?.displayName ?: "Unknown",
                            createdAt = Timestamp.now()
                    )
            activitiesCollection.add(activity).await()
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }

    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    suspend fun getGroupActivities(groupId: String, limit: Int = 20): Flow<List<GroupActivity>> =
            callbackFlow {
                val listener =
                        activitiesCollection
                                .whereEqualTo("groupId", groupId)
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .limit(limit.toLong())
                                .addSnapshotListener { snapshot, error ->
                                    if (error != null) {
                                        close(error)
                                        return@addSnapshotListener
                                    }

                                    val activities =
                                            snapshot?.toObjects(GroupActivity::class.java)
                                                    ?: emptyList()
                                    trySend(activities)
                                }

                awaitClose { listener.remove() }
            }

    suspend fun isUserGroupOwner(groupId: String, userId: String): Boolean {
        return try {
            val group = getGroupById(groupId).getOrNull()
            group?.owner == userId
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserRole(groupId: String, userId: String): String? {
        return try {
            val group = getGroupById(groupId).getOrNull()
            group?.members?.find { it.userId == userId }?.role
        } catch (e: Exception) {
            null
        }
    }
}
