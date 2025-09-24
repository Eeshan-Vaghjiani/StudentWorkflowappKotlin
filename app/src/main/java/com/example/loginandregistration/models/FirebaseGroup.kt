package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FirebaseGroup(
        @DocumentId val id: String = "",
        val name: String = "",
        val description: String = "",
        val subject: String = "",
        val owner: String = "", // Owner user ID
        val joinCode: String = "", // 6-digit alphanumeric code
        val createdAt: Timestamp = Timestamp.now(),
        val updatedAt: Timestamp = Timestamp.now(),
        val members: List<GroupMember> = emptyList(), // Enhanced member structure
        val memberIds: List<String> = emptyList(), // For easy querying
        val tasks: List<GroupTask> = emptyList(), // Tasks within the group
        val settings: GroupSettings = GroupSettings(),
        val isActive: Boolean = true
) {
        // No-argument constructor for Firestore
        constructor() :
                this(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        Timestamp.now(),
                        Timestamp.now(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        GroupSettings(),
                        true
                )
}

data class GroupMember(
        val userId: String = "",
        val email: String = "",
        val displayName: String = "",
        val role: String = "member", // owner, admin, member
        val joinedAt: Timestamp = Timestamp.now(),
        val isActive: Boolean = true
) {
        constructor() : this("", "", "", "member", Timestamp.now(), true)
}

data class GroupTask(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val assignedTo: List<String> = emptyList(), // User IDs
        val createdBy: String = "",
        val status: String = "pending", // pending, in_progress, completed
        val priority: String = "medium", // low, medium, high
        val dueDate: Timestamp? = null,
        val createdAt: Timestamp = Timestamp.now(),
        val updatedAt: Timestamp = Timestamp.now()
) {
        constructor() :
                this(
                        "",
                        "",
                        "",
                        emptyList(),
                        "",
                        "pending",
                        "medium",
                        null,
                        Timestamp.now(),
                        Timestamp.now()
                )
}

data class GroupSettings(
        val isPublic: Boolean = false,
        val allowMemberInvites: Boolean = true,
        val maxMembers: Int = 50,
        val requireApproval: Boolean = false
) {
        constructor() : this(false, true, 50, false)
}

data class GroupActivity(
        @DocumentId val id: String = "",
        val groupId: String = "",
        val type: String =
                "", // message, assignment, member_joined, member_left, task_created, task_completed
        val title: String = "",
        val description: String = "",
        val userId: String = "",
        val userName: String = "",
        val createdAt: Timestamp = Timestamp.now()
) {
        constructor() : this("", "", "", "", "", "", "", Timestamp.now())
}
