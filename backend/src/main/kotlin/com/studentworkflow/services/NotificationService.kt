package com.studentworkflow.services

import com.studentworkflow.models.Notification

class NotificationService {
    fun createGroupInvitation(userId: Int, groupId: Int, inviterId: Int): Notification? = null
    fun createAssignmentDue(userId: Int, assignmentId: Int): Notification? = null
    fun createNewAssignment(userId: Int, assignmentId: Int, creatorId: Int): Notification? = null
    fun createTaskAssignment(userId: Int, taskId: Int, assignerId: Int): Notification? = null
    fun createDeadlineReminders() {}
    fun createMessageNotification(userId: Int, senderName: String, content: String, type: String, sourceId: Int): Notification? = null
    fun createGroupJoinRequest(groupId: Int, requesterId: Int): Notification? = null
    fun createGroupJoinApproved(userId: Int, groupId: Int, approverId: Int): Notification? = null
}
