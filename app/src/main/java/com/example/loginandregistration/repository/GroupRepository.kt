package com.example.loginandregistration.repository

import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.GroupActivity
import com.example.loginandregistration.models.GroupMember
import com.example.loginandregistration.models.GroupSettings
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.random.Random
import kotlinx.coroutines.tasks.await

class GroupRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val groupsCollection = db.collection("groups")
    private val activitiesCollection = db.collection("group_activities")

    suspend fun getUserGroups(): List<FirebaseGroup> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            groupsCollection
                    .whereArrayContains("memberIds", userId)
                    .whereEqualTo("isActive", true)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(FirebaseGroup::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPublicGroups(): List<FirebaseGroup> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            groupsCollection
                    .whereEqualTo("privacy", "public")
                    .whereEqualTo("isActive", true)
                    .limit(10)
                    .get()
                    .await()
                    .toObjects(FirebaseGroup::class.java)
                    .filter { !it.memberIds.contains(userId) } // Exclude groups user is already in
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createGroup(
            name: String,
            description: String,
            subject: String,
            privacy: String
    ): String? {
        val user = auth.currentUser ?: return null
        return try {
            val joinCode = generateJoinCode()
            val ownerMember =
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
                            members = listOf(ownerMember),
                            memberIds = listOf(user.uid),
                            tasks = emptyList(),
                            settings = GroupSettings()
                    )

            val docRef = groupsCollection.add(group).await()

            // Add activity for group creation
            addGroupActivity(
                    groupId = docRef.id,
                    type = "group_created",
                    title = "Group created",
                    description = "Group \"$name\" was created"
            )

            docRef.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinGroup(groupId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            // Add user to group members
            groupsCollection
                    .document(groupId)
                    .update("members", FieldValue.arrayUnion(userId))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupId,
                    type = "member_joined",
                    title = "New member joined",
                    description = "${auth.currentUser?.displayName ?: "Someone"} joined the group"
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun joinGroupByCode(joinCode: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            val querySnapshot =
                    groupsCollection
                            .whereEqualTo("joinCode", joinCode)
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val groupDoc = querySnapshot.documents.first()
                val group = groupDoc.toObject(FirebaseGroup::class.java)

                if (group != null && !group.memberIds.contains(userId)) {
                    joinGroup(groupDoc.id)
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getGroupActivities(limit: Int = 10): List<GroupActivity> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            // Get user's groups first
            val userGroups = getUserGroups()
            val groupIds = userGroups.map { it.id }

            if (groupIds.isEmpty()) return emptyList()

            activitiesCollection
                    .whereIn("groupId", groupIds)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()
                    .toObjects(GroupActivity::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

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

    suspend fun getGroupStats(): GroupStats {
        val userId = auth.currentUser?.uid ?: return GroupStats()
        return try {
            val groups = getUserGroups()
            val activities = getGroupActivities(50)

            val myGroupsCount = groups.size
            val activeAssignments = 0 // TODO: Implement when tasks are linked to groups
            val newMessages = activities.filter { it.type == "message" }.size

            GroupStats(myGroupsCount, activeAssignments, newMessages)
        } catch (e: Exception) {
            GroupStats()
        }
    }

    suspend fun getGroupById(groupId: String): FirebaseGroup? {
        return try {
            groupsCollection.document(groupId).get().await().toObject(FirebaseGroup::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addMemberToGroup(groupId: String, userId: String): Boolean {
        return try {
            groupsCollection
                    .document(groupId)
                    .update("members", FieldValue.arrayUnion(userId))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupId,
                    type = "member_added",
                    title = "Member added",
                    description = "A new member was added to the group"
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeMemberFromGroup(groupId: String, userId: String): Boolean {
        return try {
            groupsCollection
                    .document(groupId)
                    .update("members", FieldValue.arrayRemove(userId))
                    .await()

            // Add activity
            addGroupActivity(
                    groupId = groupId,
                    type = "member_removed",
                    title = "Member removed",
                    description = "A member was removed from the group"
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isUserGroupAdmin(groupId: String, userId: String): Boolean {
        return try {
            val group = getGroupById(groupId)
            group?.owner == userId ||
                    group?.members?.any {
                        it.userId == userId && it.role in listOf("owner", "admin")
                    } == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun regenerateJoinCode(groupId: String): String? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            // Check if user is admin
            if (!isUserGroupAdmin(groupId, userId)) return null

            val newCode = generateJoinCode()
            groupsCollection.document(groupId).update("joinCode", newCode).await()

            newCode
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDashboardStats(): DashboardStats {
        val userId = auth.currentUser?.uid ?: return DashboardStats()
        return try {
            val groups = getUserGroups()
            val activities = getGroupActivities(20)

            val myGroupsCount = groups.size
            val newMessagesCount = activities.filter { it.type == "message" }.size

            DashboardStats(groupsCount = myGroupsCount, newMessages = newMessagesCount)
        } catch (e: Exception) {
            DashboardStats()
        }
    }
}

data class GroupStats(
        val myGroups: Int = 0,
        val activeAssignments: Int = 0,
        val newMessages: Int = 0
)

data class DashboardStats(val groupsCount: Int = 0, val newMessages: Int = 0)
