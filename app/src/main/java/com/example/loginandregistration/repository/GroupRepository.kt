package com.example.loginandregistration.repository

import android.util.Log
import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.FirebaseUser
import com.example.loginandregistration.models.GroupActivity
import com.example.loginandregistration.models.GroupMember
import com.example.loginandregistration.models.GroupSettings
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.random.Random
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                            settings = GroupSettings(isPublic = privacy == "public"),
                            isActive = true
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

    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    suspend fun joinGroupByCode(code: String): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val querySnapshot =
                    groupsCollection
                            .whereEqualTo("joinCode", code)
                            .whereEqualTo("isActive", true)
                            .get()
                            .await()

            if (querySnapshot.isEmpty) return false

            val groupDoc = querySnapshot.documents.first()
            val group = groupDoc.toObject(FirebaseGroup::class.java) ?: return false

            // Check if user is already a member
            if (group.memberIds.contains(user.uid)) return true

            val newMember =
                    GroupMember(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: "Unknown",
                            role = "member",
                            joinedAt = Timestamp.now()
                    )

            // Update the group with the new member
            groupDoc.reference
                    .update(
                            mapOf(
                                    "members" to FieldValue.arrayUnion(newMember),
                                    "memberIds" to FieldValue.arrayUnion(user.uid),
                                    "updatedAt" to Timestamp.now()
                            )
                    )
                    .await()

            // Add activity for joining
            addGroupActivity(
                    groupId = groupDoc.id,
                    type = "member_joined",
                    title = "New member joined",
                    description = "${user.displayName ?: "A new member"} joined the group"
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun joinGroup(groupId: String): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val groupDoc = groupsCollection.document(groupId).get().await()
            val group = groupDoc.toObject(FirebaseGroup::class.java) ?: return false

            // Check if user is already a member
            if (group.memberIds.contains(user.uid)) return true

            // Check if group is public
            if (!group.settings.isPublic) return false

            val newMember =
                    GroupMember(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName ?: "Unknown",
                            role = "member",
                            joinedAt = Timestamp.now()
                    )

            // Update the group with the new member
            groupDoc.reference
                    .update(
                            mapOf(
                                    "members" to FieldValue.arrayUnion(newMember),
                                    "memberIds" to FieldValue.arrayUnion(user.uid),
                                    "updatedAt" to Timestamp.now()
                            )
                    )
                    .await()

            // Add activity for joining
            addGroupActivity(
                    groupId = groupId,
                    type = "member_joined",
                    title = "New member joined",
                    description = "${user.displayName ?: "A new member"} joined the group"
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun addGroupActivity(
            groupId: String,
            type: String,
            title: String,
            description: String
    ) {
        val user = auth.currentUser ?: return
        try {
            val activity =
                    GroupActivity(
                            groupId = groupId,
                            type = type,
                            title = title,
                            description = description,
                            userId = user.uid,
                            userName = user.displayName ?: "Unknown",
                            createdAt = Timestamp.now()
                    )
            activitiesCollection.add(activity).await()
        } catch (e: Exception) {
            // Silently fail, activity logging is not critical
        }
    }

    suspend fun leaveGroup(groupId: String): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val groupDoc = groupsCollection.document(groupId).get().await()
            val group = groupDoc.toObject(FirebaseGroup::class.java) ?: return false

            // Check if user is a member
            if (!group.memberIds.contains(user.uid)) return true

            // Check if user is the owner
            if (group.owner == user.uid) {
                // Owner can't leave, they must delete or transfer ownership
                return false
            }

            // Find the member to remove
            val memberToRemove = group.members.find { it.userId == user.uid }

            // Update the group by removing the member
            groupDoc.reference
                    .update(
                            mapOf(
                                    "members" to FieldValue.arrayRemove(memberToRemove),
                                    "memberIds" to FieldValue.arrayRemove(user.uid),
                                    "updatedAt" to Timestamp.now()
                            )
                    )
                    .await()

            // Add activity for leaving
            addGroupActivity(
                    groupId = groupId,
                    type = "member_left",
                    title = "Member left",
                    description = "${user.displayName ?: "A member"} left the group"
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteGroup(groupId: String): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val groupDoc = groupsCollection.document(groupId).get().await()
            val group = groupDoc.toObject(FirebaseGroup::class.java) ?: return false

            // Check if user is the owner
            if (group.owner != user.uid) return false

            // Soft delete by marking as inactive
            groupDoc.reference
                    .update(mapOf("isActive" to false, "updatedAt" to Timestamp.now()))
                    .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserGroupsFlow(): Flow<List<FirebaseGroup>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d("GroupRepository", "No authenticated user, returning empty list")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d("GroupRepository", "Setting up real-time listener for user groups: $userId")

        val listener =
                groupsCollection
                        .whereArrayContains("memberIds", userId)
                        .whereEqualTo("isActive", true)
                        .orderBy("updatedAt", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(
                                        "GroupRepository",
                                        "Error getting user groups: ${error.message}",
                                        error
                                )
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            val groups =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        try {
                                            doc.toObject(FirebaseGroup::class.java)
                                                    ?.copy(id = doc.id)
                                        } catch (e: Exception) {
                                            Log.e(
                                                    "GroupRepository",
                                                    "Error parsing group document: ${doc.id}",
                                                    e
                                            )
                                            null
                                        }
                                    }
                                            ?: emptyList()

                            Log.d(
                                    "GroupRepository",
                                    "Received ${groups.size} groups from Firestore"
                            )
                            trySend(groups)
                        }

        awaitClose {
            Log.d("GroupRepository", "Removing groups listener")
            listener.remove()
        }
    }

    fun getGroupStatsFlow(): Flow<GroupStats> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(GroupStats())
            close()
            return@callbackFlow
        }

        // Listen to user's groups - this is the primary listener that will update the group count
        val groupsListener =
                groupsCollection
                        .whereArrayContains("memberIds", userId)
                        .whereEqualTo("isActive", true)
                        .addSnapshotListener { groupSnapshot, error ->
                            if (error != null) {
                                Log.e("GroupRepository", "Error getting groups: ${error.message}")
                                trySend(GroupStats())
                                return@addSnapshotListener
                            }

                            // Process the groups snapshot
                            val groups =
                                    groupSnapshot?.toObjects(FirebaseGroup::class.java)
                                            ?: emptyList()
                            val myGroupsCount = groups.size

                            // Always send an update with the current group count
                            Log.d("GroupRepository", "Group count update: $myGroupsCount groups")
                            trySend(GroupStats(myGroups = myGroupsCount))

                            // If there are groups, fetch additional stats asynchronously
                            if (groups.isNotEmpty()) {
                                val groupIds = groups.map { it.id }
                                fetchActivityStats(groupIds, myGroupsCount)
                            }
                        }

        // Register for cleanup when the flow is cancelled
        awaitClose { groupsListener.remove() }
    }

    // Helper method to fetch activity stats separately
    private fun ProducerScope<GroupStats>.fetchActivityStats(
            groupIds: List<String>,
            myGroupsCount: Int
    ) {
        try {
            // For activities, use a separate query to avoid nesting listeners
            activitiesCollection
                    .whereIn("groupId", groupIds.take(10)) // Firestore limit
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .addOnSuccessListener { activitySnapshot ->
                        val activities = activitySnapshot.toObjects(GroupActivity::class.java)
                        val newMessages = activities.filter { it.type == "message" }.size

                        // TODO: Calculate active assignments when task-group linking is implemented
                        val activeAssignments = 0

                        // Send updated stats with activity information
                        Log.d(
                                "GroupRepository",
                                "Activity stats update: $myGroupsCount groups, $newMessages messages"
                        )
                        trySend(
                                GroupStats(
                                        myGroups = myGroupsCount,
                                        activeAssignments = activeAssignments,
                                        newMessages = newMessages
                                )
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e("GroupRepository", "Error getting activities: ${error.message}")
                        // If activities query fails, still maintain the group count
                        trySend(GroupStats(myGroups = myGroupsCount))
                    }
        } catch (e: Exception) {
            Log.e("GroupRepository", "Exception in fetchActivityStats: ${e.message}")
            trySend(GroupStats(myGroups = myGroupsCount))
        }
    }

    // Helper method to get a group by ID
    suspend fun getGroupById(groupId: String): FirebaseGroup? {
        return try {
            val document = groupsCollection.document(groupId).get().await()
            document.toObject(FirebaseGroup::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Helper method to check if a user is an admin of a group
    suspend fun isUserGroupAdmin(groupId: String, userId: String): Boolean {
        val group = getGroupById(groupId) ?: return false
        val member = group.members.find { it.userId == userId } ?: return false
        return member.role == "admin" || member.role == "owner"
    }

    // Helper method to get a group's join code
    suspend fun getGroupJoinCode(groupId: String): String? {
        val group = getGroupById(groupId) ?: return null
        return group.joinCode
    }

    // Get recent group activities
    suspend fun getGroupActivities(limit: Int = 20): List<GroupActivity> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            activitiesCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()
                    .toObjects(GroupActivity::class.java)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error getting activities: ${e.message}")
            emptyList()
        }
    }

    suspend fun getGroupAdminInfo(groupId: String): Pair<FirebaseGroup?, String?> {
        val userId = auth.currentUser?.uid ?: return Pair(null, null)
        return try {
            val group = getGroupById(groupId)
            if (group != null && isUserGroupAdmin(groupId, userId)) {
                Pair(group, group.joinCode)
            } else {
                Pair(group, null) // Don't return join code if not admin
            }
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    // Remove a member from a group
    suspend fun removeMemberFromGroup(groupId: String, memberId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val group = getGroupById(groupId) ?: return false

        // Check if user is admin
        if (!isUserGroupAdmin(groupId, userId)) {
            return false
        }

        // Cannot remove the owner
        if (memberId == group.owner) {
            return false
        }

        try {
            // Update the group document
            groupsCollection
                    .document(groupId)
                    .update(
                            "members",
                            group.members.filter { it.userId != memberId },
                            "memberIds",
                            group.memberIds.filter { it != memberId },
                            "updatedAt",
                            Timestamp.now()
                    )
                    .await()

            // Add activity
            val user = auth.currentUser
            if (user != null) {
                val memberName =
                        group.members.find { it.userId == memberId }?.displayName ?: "A member"
                addGroupActivity(
                        groupId = groupId,
                        title = "Member removed",
                        description = "$memberName was removed from the group",
                        type = "member_left"
                )
            }

            return true
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error removing member: ${e.message}")
            return false
        }
    }

    // Regenerate a new join code for a group
    suspend fun regenerateJoinCode(groupId: String): String? {
        val userId = auth.currentUser?.uid ?: return null
        val group = getGroupById(groupId) ?: return null

        // Check if user is admin
        if (!isUserGroupAdmin(groupId, userId)) {
            return null
        }

        try {
            val newCode = generateJoinCode()
            groupsCollection
                    .document(groupId)
                    .update("joinCode", newCode, "updatedAt", Timestamp.now())
                    .await()

            return newCode
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error regenerating join code: ${e.message}")
            return null
        }
    }

    // Update group details
    suspend fun updateGroupDetails(
            groupId: String,
            name: String,
            description: String,
            subject: String,
            privacy: String
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val group = getGroupById(groupId) ?: return false

        // Check if user is admin
        if (!isUserGroupAdmin(groupId, userId)) {
            return false
        }

        try {
            groupsCollection
                    .document(groupId)
                    .update(
                            "name",
                            name,
                            "description",
                            description,
                            "subject",
                            subject,
                            "settings.isPublic",
                            privacy == "public",
                            "updatedAt",
                            Timestamp.now()
                    )
                    .await()

            // Add activity
            val user = auth.currentUser
            if (user != null) {
                addGroupActivity(
                        groupId = groupId,
                        title = "Group details updated",
                        description = "${user.displayName ?: "Someone"} updated the group details",
                        type = "group_updated"
                )
            }

            return true
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error updating group: ${e.message}")
            return false
        }
    }

    private val usersCollection = db.collection("users")

    // Add a member to a group
    suspend fun addMemberToGroup(groupId: String, userId: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false
        val group = getGroupById(groupId) ?: return false

        // Check if current user is admin
        if (!isUserGroupAdmin(groupId, currentUserId)) {
            return false
        }

        // Check if user is already a member
        if (group.memberIds.contains(userId)) {
            return false
        }

        try {
            // Get user details from users collection
            val userSnapshot = usersCollection.document(userId).get().await()
            val targetUser = userSnapshot.toObject(FirebaseUser::class.java) ?: return false

            val email = targetUser.email
            val displayName = targetUser.displayName

            // Create new member
            val newMember =
                    GroupMember(
                            userId = userId,
                            email = email,
                            displayName = displayName,
                            role = "member",
                            joinedAt = Timestamp.now(),
                            isActive = true
                    )

            // Update the group document
            val updatedMembers = group.members.toMutableList().apply { add(newMember) }
            val updatedMemberIds = group.memberIds.toMutableList().apply { add(userId) }

            groupsCollection
                    .document(groupId)
                    .update(
                            "members",
                            updatedMembers,
                            "memberIds",
                            updatedMemberIds,
                            "updatedAt",
                            Timestamp.now()
                    )
                    .await()

            // Add activity
            val user = auth.currentUser
            if (user != null) {
                addGroupActivity(
                        groupId = groupId,
                        title = "New member added",
                        description = "${displayName} was added to the group",
                        type = "member_joined"
                )
            }

            return true
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error adding member: ${e.message}")
            return false
        }
    }
}

data class GroupStats(
        val myGroups: Int = 0,
        val activeAssignments: Int = 0,
        val newMessages: Int = 0
)

data class DashboardStats(val groupsCount: Int = 0, val newMessages: Int = 0)
