package com.studentworkflow.services

import com.studentworkflow.db.Notifications
import com.studentworkflow.db.StudyGroups
import com.studentworkflow.db.Tasks
import com.studentworkflow.db.Users
import com.studentworkflow.models.Notification
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel

/**
 * NotificationService handles creating, sending, and managing notifications.
 * Supports database storage, email notifications, and real-time WebSocket notifications.
 */
class NotificationService {

    // WebSocket connections for real-time notifications
    private val webSocketConnections = ConcurrentHashMap<Int, SendChannel<Frame>>()

    // Email configuration
    private val emailProperties = Properties().apply {
        put("mail.smtp.host", System.getenv("SMTP_HOST") ?: "localhost")
        put("mail.smtp.port", System.getenv("SMTP_PORT") ?: "587")
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
    }
    private val emailSession = Session.getInstance(emailProperties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(
                System.getenv("SMTP_USERNAME") ?: "",
                System.getenv("SMTP_PASSWORD") ?: ""
            )
        }
    })

    /**
     * Register a WebSocket connection for real-time notifications
     */
    fun registerWebSocketConnection(userId: Int, channel: SendChannel<Frame>) {
        webSocketConnections[userId] = channel
    }

    /**
     * Unregister a WebSocket connection
     */
    fun unregisterWebSocketConnection(userId: Int) {
        webSocketConnections.remove(userId)
    }

    /**
     * Send real-time notification via WebSocket
     */
    private suspend fun sendRealTimeNotification(userId: Int, notification: Notification) {
        webSocketConnections[userId]?.let { channel ->
            try {
                val notificationJson = Json.encodeToString(notification)
                channel.send(Frame.Text(notificationJson))
            } catch (e: Exception) {
                // Remove connection if it's closed
                webSocketConnections.remove(userId)
            }
        }
    }

    /**
     * Send email notification
     */
    private fun sendEmailNotification(
        userEmail: String,
        subject: String,
        content: String,
        isHtml: Boolean = false
    ) {
        try {
            val message = MimeMessage(emailSession).apply {
                setFrom(InternetAddress(System.getenv("SMTP_FROM") ?: "noreply@studentworkflow.com"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail))
                setSubject(subject)
                if (isHtml) {
                    setContent(content, "text/html; charset=utf-8")
                } else {
                    setText(content)
                }
            }
            Transport.send(message)
        } catch (e: Exception) {
            // Log error or handle email sending failure
            println("Failed to send email to $userEmail: ${e.message}")
        }
    }

    /**
     * Create and send notification with all supported channels
     */
    private suspend fun createAndSendNotification(
        userId: Int,
        type: String,
        data: String,
        emailSubject: String? = null,
        emailContent: String? = null
    ): Notification? {
        val notification = transaction {
            val id = Notifications.insert {
                it[Notifications.userId] = userId
                it[Notifications.type] = type
                it[Notifications.data] = data
                it[read] = false
                it[createdAt] = Instant.now()
            } get Notifications.id

            Notification(id.value, userId, type, data, false, Instant.now().toString())
        }

        if (notification != null) {
            // Send real-time notification
            sendRealTimeNotification(userId, notification)

            // Send email notification if configured
            if (emailSubject != null && emailContent != null) {
                transaction {
                    val user = Users.select { Users.id eq userId }.singleOrNull()
                    user?.let {
                        val userEmail = it[Users.email]
                        sendEmailNotification(userEmail, emailSubject, emailContent)
                    }
                }
            }
        }

        return notification
    }

    /**
     * Get all notifications for a user
     */
    fun getNotifications(userId: Int, limit: Int = 50, offset: Int = 0): List<Notification> {
        return transaction {
            Notifications.select { Notifications.userId eq userId }
                .orderBy(Notifications.createdAt, SortOrder.DESC)
                .limit(limit, offset.toLong())
                .map { row ->
                    Notification(
                        id = row[Notifications.id].value,
                        userId = row[Notifications.userId],
                        type = row[Notifications.type],
                        data = row[Notifications.data],
                        read = row[Notifications.read],
                        createdAt = row[Notifications.createdAt].toString()
                    )
                }
        }
    }

    /**
     * Get unread notification count for a user
     */
    fun getUnreadCount(userId: Int): Int {
        return transaction {
            Notifications.select { 
                (Notifications.userId eq userId) and (Notifications.read eq false) 
            }.count().toInt()
        }
    }

    /**
     * Mark notifications as read
     */
    fun markAsRead(notificationIds: List<Int>, userId: Int): Boolean {
        return transaction {
            Notifications.update({
                (Notifications.id inList notificationIds) and (Notifications.userId eq userId)
            }) {
                it[read] = true
            } > 0
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    fun markAllAsRead(userId: Int): Boolean {
        return transaction {
            Notifications.update({ 
                (Notifications.userId eq userId) and (Notifications.read eq false) 
            }) {
                it[read] = true
            } > 0
        }
    }

    /**
     * Delete a notification
     */
    fun deleteNotification(notificationId: Int, userId: Int): Boolean {
        return transaction {
            Notifications.deleteWhere {
                (Notifications.id eq notificationId) and (Notifications.userId eq userId)
            } > 0
        }
    }

    /**
     * Create group invitation notification
     */
    suspend fun createGroupInvitation(userId: Int, groupId: Int, inviterId: Int): Notification? {
        return transaction {
            val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
            val inviter = Users.select { Users.id eq inviterId }.singleOrNull()

            if (group == null || inviter == null) return@transaction null

            val groupName = group[StudyGroups.name]
            val inviterName = inviter[Users.name]
            
            val data = Json.encodeToString(mapOf(
                "group_id" to groupId,
                "group_name" to groupName,
                "inviter_id" to inviterId,
                "inviter_name" to inviterName
            ))

            val emailSubject = "Study Group Invitation"
            val emailContent = """
                You have been invited to join the study group "$groupName" by $inviterName.
                
                Log in to your Student Workflow account to accept or decline this invitation.
            """.trimIndent()

            null
        }?.let {
            createAndSendNotification(
                userId, "group_invitation", 
                Json.encodeToString(mapOf(
                    "group_id" to groupId,
                    "group_name" to transaction { StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()?.get(StudyGroups.name) },
                    "inviter_id" to inviterId,
                    "inviter_name" to transaction { Users.select { Users.id eq inviterId }.singleOrNull()?.get(Users.name) }
                )),
                "Study Group Invitation",
                "You have been invited to join a study group."
            )
        }
    }

    /**
     * Create assignment due notification
     */
    suspend fun createAssignmentDue(userId: Int, assignmentId: Int): Notification? {
        return transaction {
            val task = Tasks.select { Tasks.id eq assignmentId }.singleOrNull()
            if (task == null) return@transaction null

            val taskTitle = task[Tasks.title]
            val dueDate = task[Tasks.dueDate]
            
            val data = Json.encodeToString(mapOf(
                "assignment_id" to assignmentId,
                "assignment_title" to taskTitle,
                "due_date" to dueDate.toString()
            ))

            val emailSubject = "Assignment Due Soon"
            val emailContent = """
                Reminder: Your assignment "$taskTitle" is due soon.
                
                Due date: $dueDate
                
                Don't forget to complete your work on time!
            """.trimIndent()

            null
        }?.let {
            createAndSendNotification(
                userId, "assignment_due",
                Json.encodeToString(mapOf(
                    "assignment_id" to assignmentId,
                    "assignment_title" to transaction { Tasks.select { Tasks.id eq assignmentId }.singleOrNull()?.get(Tasks.title) },
                    "due_date" to transaction { Tasks.select { Tasks.id eq assignmentId }.singleOrNull()?.get(Tasks.dueDate).toString() }
                )),
                "Assignment Due Soon",
                "You have an assignment due soon."
            )
        }
    }

    /**
     * Create new assignment notification
     */
    suspend fun createNewAssignment(userId: Int, assignmentId: Int, creatorId: Int): Notification? {
        return transaction {
            val task = Tasks.select { Tasks.id eq assignmentId }.singleOrNull()
            val creator = Users.select { Users.id eq creatorId }.singleOrNull()

            if (task == null || creator == null) return@transaction null

            val taskTitle = task[Tasks.title]
            val creatorName = creator[Users.name]
            val dueDate = task[Tasks.dueDate]
            
            val data = Json.encodeToString(mapOf(
                "assignment_id" to assignmentId,
                "assignment_title" to taskTitle,
                "creator_id" to creatorId,
                "creator_name" to creatorName,
                "due_date" to dueDate.toString()
            ))

            val emailSubject = "New Assignment Created"
            val emailContent = """
                A new assignment "$taskTitle" has been created by $creatorName.
                
                Due date: $dueDate
                
                Check your dashboard for more details.
            """.trimIndent()

            null
        }?.let {
            createAndSendNotification(
                userId, "assignment_created",
                Json.encodeToString(mapOf(
                    "assignment_id" to assignmentId,
                    "assignment_title" to transaction { Tasks.select { Tasks.id eq assignmentId }.singleOrNull()?.get(Tasks.title) },
                    "creator_id" to creatorId,
                    "creator_name" to transaction { Users.select { Users.id eq creatorId }.singleOrNull()?.get(Users.name) },
                    "due_date" to transaction { Tasks.select { Tasks.id eq assignmentId }.singleOrNull()?.get(Tasks.dueDate).toString() }
                )),
                "New Assignment Created",
                "A new assignment has been created for you."
            )
        }
    }

    /**
     * Create task assignment notification
     */
    suspend fun createTaskAssignment(userId: Int, taskId: Int, assignerId: Int): Notification? {
        return transaction {
            val task = Tasks.select { Tasks.id eq taskId }.singleOrNull()
            val assigner = Users.select { Users.id eq assignerId }.singleOrNull()

            if (task == null || assigner == null) return@transaction null

            val taskTitle = task[Tasks.title]
            val assignerName = assigner[Users.name]
            
            val data = Json.encodeToString(mapOf(
                "task_id" to taskId,
                "task_title" to taskTitle,
                "assigner_id" to assignerId,
                "assigner_name" to assignerName
            ))

            val emailSubject = "Task Assigned"
            val emailContent = """
                You have been assigned a task: "$taskTitle" by $assignerName.
                
                Please check your dashboard for details and start working on it.
            """.trimIndent()

            null
        }?.let {
            createAndSendNotification(
                userId, "task_assignment",
                Json.encodeToString(mapOf(
                    "task_id" to taskId,
                    "task_title" to transaction { Tasks.select { Tasks.id eq taskId }.singleOrNull()?.get(Tasks.title) },
                    "assigner_id" to assignerId,
                    "assigner_name" to transaction { Users.select { Users.id eq assignerId }.singleOrNull()?.get(Users.name) }
                )),
                "Task Assigned",
                "You have been assigned a new task."
            )
        }
    }

    /**
     * Create deadline reminders for approaching tasks
     */
    suspend fun createDeadlineReminders() {
        transaction {
            val tomorrow = Instant.now().plusSeconds(24 * 60 * 60)
            val dayAfter = Instant.now().plusSeconds(48 * 60 * 60)
            
            // Find tasks due within 24-48 hours that don't have completed status
            Tasks.select { 
                (Tasks.dueDate.isNotNull()) and 
                (Tasks.dueDate greater Instant.now()) and 
                (Tasks.dueDate lessEq tomorrow) and
                (Tasks.status neq "completed")
            }.forEach { task ->
                val assigneeId = task[Tasks.assigneeId]
                if (assigneeId != null) {
                    // Create reminder notification for assignee
                    val data = Json.encodeToString(mapOf(
                        "task_id" to task[Tasks.id].value,
                        "task_title" to task[Tasks.title],
                        "due_date" to task[Tasks.dueDate].toString()
                    ))
                    
                    Notifications.insert {
                        it[userId] = assigneeId
                        it[type] = "deadline_reminder"
                        it[Notifications.data] = data
                        it[read] = false
                        it[createdAt] = Instant.now()
                    }
                }
            }
        }
    }

    /**
     * Create message notification
     */
    suspend fun createMessageNotification(
        userId: Int, 
        senderName: String, 
        content: String, 
        type: String, 
        sourceId: Int
    ): Notification? {
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
        
        val emailSubject = "New Message"
        val emailContent = """
            You have received a new message from $senderName.
            
            Message: $truncatedContent
            
            Log in to your account to view the full message and reply.
        """.trimIndent()

        return createAndSendNotification(
            userId, type + "_message", data,
            emailSubject, emailContent
        )
    }

    /**
     * Create group join request notification
     */
    suspend fun createGroupJoinRequest(groupId: Int, requesterId: Int): Notification? {
        return transaction {
            val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
            val requester = Users.select { Users.id eq requesterId }.singleOrNull()

            if (group == null || requester == null) return@transaction null

            val ownerId = group[StudyGroups.ownerId]
            val groupName = group[StudyGroups.name]
            val requesterName = requester[Users.name]
            
            val data = Json.encodeToString(mapOf(
                "group_id" to groupId,
                "group_name" to groupName,
                "requester_id" to requesterId,
                "requester_name" to requesterName
            ))

            val emailSubject = "Study Group Join Request"
            val emailContent = """
                $requesterName has requested to join your study group "$groupName".
                
                Please review and approve or decline this request in your dashboard.
            """.trimIndent()

            ownerId
        }?.let { ownerId ->
            createAndSendNotification(
                ownerId, "group_join_request",
                Json.encodeToString(mapOf(
                    "group_id" to groupId,
                    "group_name" to transaction { StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()?.get(StudyGroups.name) },
                    "requester_id" to requesterId,
                    "requester_name" to transaction { Users.select { Users.id eq requesterId }.singleOrNull()?.get(Users.name) }
                )),
                "Study Group Join Request",
                "Someone wants to join your study group."
            )
        }
    }

    /**
     * Create group join approved notification
     */
    suspend fun createGroupJoinApproved(userId: Int, groupId: Int, approverId: Int): Notification? {
        return transaction {
            val group = StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()
            val approver = Users.select { Users.id eq approverId }.singleOrNull()

            if (group == null || approver == null) return@transaction null

            val groupName = group[StudyGroups.name]
            val approverName = approver[Users.name]
            
            val data = Json.encodeToString(mapOf(
                "group_id" to groupId,
                "group_name" to groupName,
                "approver_id" to approverId,
                "approver_name" to approverName
            ))

            val emailSubject = "Study Group Join Approved"
            val emailContent = """
                Your request to join the study group "$groupName" has been approved by $approverName.
                
                You can now participate in group activities and discussions.
            """.trimIndent()

            null
        }?.let {
            createAndSendNotification(
                userId, "group_join_approved",
                Json.encodeToString(mapOf(
                    "group_id" to groupId,
                    "group_name" to transaction { StudyGroups.select { StudyGroups.id eq groupId }.singleOrNull()?.get(StudyGroups.name) },
                    "approver_id" to approverId,
                    "approver_name" to transaction { Users.select { Users.id eq approverId }.singleOrNull()?.get(Users.name) }
                )),
                "Study Group Join Approved",
                "Your request to join a study group has been approved."
            )
        }
    }
}