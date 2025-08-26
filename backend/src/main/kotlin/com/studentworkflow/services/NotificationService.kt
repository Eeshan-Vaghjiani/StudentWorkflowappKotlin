
package com.studentworkflow.services

import com.studentworkflow.db.Notifications
import com.studentworkflow.db.StudyGroups
import com.studentworkflow.db.Tasks
import com.studentworkflow.db.Users
import com.studentworkflow.models.Notification
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class NotificationService {

    fun createGroupInvitation(userId: Int, groupId: Int, inviterId: Int): Notification? {
        return transaction {
            val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
            val inviter = Users.select { Users.id eq inviterId }.singleOrNull()

            if (group == null || inviter == null) return@transaction null

            val data = Json.encodeToString(mapOf(
                "group_id" to groupId,
                "group_name" to group[StudyGroups.name],
                "inviter_id" to inviterId,
                "inviter_name" to inviter[Users.name]
            ))

            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[type] = "group_invitation"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, "group_invitation", data, false, Instant.now().toString())
        }
    }

    fun createAssignmentDue(userId: Int, assignmentId: Int): Notification? {
        // Assuming assignment details can be fetched from Tasks table or a dedicated Assignment table
        // For now, let's use Tasks table as a proxy for assignment
        return transaction {
            val task = Tasks.select { Tasks.id eq assignmentId }.singleOrNull()
            if (task == null) return@transaction null

            val data = Json.encodeToString(mapOf(
                "assignment_id" to assignmentId,
                "assignment_title" to task[Tasks.title],
                "due_date" to task[Tasks.dueDate].toString()
            ))

            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[type] = "assignment_due"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, "assignment_due", data, false, Instant.now().toString())
        }
    }

    fun createNewAssignment(userId: Int, assignmentId: Int, creatorId: Int): Notification? {
        // This method in PHP iterates through group members. We'll simplify for now.
        return transaction {
            val task = Tasks.select { Tasks.id eq assignmentId }.singleOrNull()
            val creator = Users.select { Users.id eq creatorId }.singleOrNull()

            if (task == null || creator == null) return@transaction null

            val data = Json.encodeToString(mapOf(
                "assignment_id" to assignmentId,
                "assignment_title" to task[Tasks.title],
                "creator_id" to creatorId,
                "creator_name" to creator[Users.name],
                "due_date" to task[Tasks.dueDate].toString()
            ))

            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[type] = "assignment_created"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, "assignment_created", data, false, Instant.now().toString())
        }
    }

    fun createTaskAssignment(userId: Int, taskId: Int, assignerId: Int): Notification? {
        return transaction {
            val task = Tasks.select { Tasks.id eq taskId }.singleOrNull()
            val assigner = Users.select { Users.id eq assignerId }.singleOrNull()

            if (task == null || assigner == null) return@transaction null

            val data = Json.encodeToString(mapOf(
                "task_id" to taskId,
                "task_title" to task[Tasks.title],
                "assigner_id" to assignerId,
                "assigner_name" to assigner[Users.name]
            ))

            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[type] = "task_assignment"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, "task_assignment", data, false, Instant.now().toString())
        }
    }

    fun createDeadlineReminders() {
        // This method typically runs as a scheduled job. Implementation will involve querying tasks
        // and creating notifications for approaching deadlines.
        // TODO: Implement logic to find tasks and create deadline reminders
    }

    fun createMessageNotification(userId: Int, senderName: String, content: String, type: String, sourceId: Int): Notification? {
        return transaction {
            val truncatedContent = if (content.length > 50) content.substring(0, 50) + "..." else content

            val dataMap = mutableMapOf(
                "sender_name" to senderName,
                "content" to truncatedContent
            )

            if (type == "direct") {
                dataMap["sender_id"] = sourceId
            } else {
                dataMap["group_id"] = sourceId
            }

            val data = Json.encodeToString(dataMap)

            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[Notifications.type] = type + "_message"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, type + "_message", data, false, Instant.now().toString())
        }
    }

    fun createGroupJoinRequest(groupId: Int, requesterId: Int): Notification? {
        // This method in PHP finds group leaders. We'll simplify for now.
        return transaction {
            val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
            val requester = Users.select { Users.id eq requesterId }.singleOrNull()

            if (group == null || requester == null) return@transaction null

            val data = Json.encodeToString(mapOf(
                "group_id" to groupId,
                "group_name" to group[StudyGroups.name],
                "requester_id" to requesterId,
                "requester_name" to requester[Users.name]
            ))

            val id = Notifications.insert {
                it[Notifications.userId] = Users.select { Users.id eq group[StudyGroups.ownerId] }.singleOrNull()?.get(Users.id) ?: return@transaction null
                it[type] = "group_join_request"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, Users.select { Users.id eq group[StudyGroups.ownerId] }.singleOrNull()?.get(Users.id) ?: return@transaction null, "group_join_request", data, false, Instant.now().toString())
        }
    }

    fun createGroupJoinApproved(userId: Int, groupId: Int, approverId: Int): Notification? {
        return transaction {
            val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
            val approver = Users.select { Users.id eq approverId }.singleOrNull()

            if (group == null || approver == null) return@transaction null

            val data = Json.encodeToString(mapOf(
                "group_id" to groupId,
                "group_name" to group[StudyGroups.name],
                "approver_id" to approverId,
                "approver_name" to approver[Users.name]
            ))

            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[type] = "group_join_approved"
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, "group_join_approved", data, false, Instant.now().toString())
        }
    }
}
