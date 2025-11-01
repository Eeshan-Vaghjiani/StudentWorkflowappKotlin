package com.example.loginandregistration.validation

import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.models.Message
import kotlin.math.abs

/** Implementation of DataValidator for Firestore data validation */
class FirestoreDataValidator : DataValidator {

    companion object {
        // Task validation constants
        private const val MIN_TASK_ASSIGNMENTS = 1
        private const val MAX_TASK_ASSIGNMENTS = 50
        private const val MAX_TITLE_LENGTH = 500
        private const val MAX_DESCRIPTION_LENGTH = 5000
        private const val TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L // 5 minutes

        // Group validation constants
        private const val MIN_GROUP_MEMBERS = 1
        private const val MAX_GROUP_MEMBERS = 100
        private const val MAX_GROUP_NAME_LENGTH = 200
        private const val MAX_GROUP_DESCRIPTION_LENGTH = 2000

        // Message validation constants
        private const val MAX_MESSAGE_TEXT_LENGTH = 10000
        private const val FIREBASE_STORAGE_URL_PREFIX = "https://firebasestorage.googleapis.com"
    }

    override fun validateTask(task: FirebaseTask): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate required fields
        if (task.title.isBlank()) {
            errors.add("Task title is required")
        } else if (task.title.length > MAX_TITLE_LENGTH) {
            errors.add("Task title must not exceed $MAX_TITLE_LENGTH characters")
        }

        if (task.userId.isBlank()) {
            errors.add("User ID is required")
        }

        // Validate assignedTo array
        if (task.assignedTo.isEmpty()) {
            errors.add("At least $MIN_TASK_ASSIGNMENTS assignee is required")
        } else if (task.assignedTo.size > MAX_TASK_ASSIGNMENTS) {
            errors.add("Maximum $MAX_TASK_ASSIGNMENTS assignees allowed")
        }

        // Validate user is in assignedTo array
        if (task.userId.isNotBlank() && task.userId !in task.assignedTo) {
            errors.add("Task creator must be in assignedTo array")
        }

        // Validate description length
        if (task.description.length > MAX_DESCRIPTION_LENGTH) {
            errors.add("Task description must not exceed $MAX_DESCRIPTION_LENGTH characters")
        }

        // Validate timestamp is within acceptable range (within 5 minutes of current time)
        val now = System.currentTimeMillis()
        val taskTime = task.createdAt.toDate().time
        if (abs(now - taskTime) > TIMESTAMP_TOLERANCE_MS) {
            errors.add("Task timestamp must be within 5 minutes of current time")
        }

        // Validate status
        val validStatuses = listOf("pending", "completed", "overdue", "in_progress")
        if (task.status.isNotBlank() && task.status !in validStatuses) {
            errors.add("Invalid task status: ${task.status}")
        }

        // Validate priority
        val validPriorities = listOf("low", "medium", "high")
        if (task.priority.isNotBlank() && task.priority !in validPriorities) {
            errors.add("Invalid task priority: ${task.priority}")
        }

        // Validate category
        val validCategories = listOf("personal", "group", "assignment")
        if (task.category.isNotBlank() && task.category !in validCategories) {
            errors.add("Invalid task category: ${task.category}")
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }

    override fun validateGroup(group: FirebaseGroup): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate required fields
        if (group.name.isBlank()) {
            errors.add("Group name is required")
        } else if (group.name.length > MAX_GROUP_NAME_LENGTH) {
            errors.add("Group name must not exceed $MAX_GROUP_NAME_LENGTH characters")
        }

        if (group.owner.isBlank()) {
            errors.add("Group owner is required")
        }

        // Validate description length
        if (group.description.length > MAX_GROUP_DESCRIPTION_LENGTH) {
            errors.add("Group description must not exceed $MAX_GROUP_DESCRIPTION_LENGTH characters")
        }

        // Validate memberIds array
        if (group.memberIds.isEmpty()) {
            errors.add("At least $MIN_GROUP_MEMBERS member is required")
        } else if (group.memberIds.size > MAX_GROUP_MEMBERS) {
            errors.add("Maximum $MAX_GROUP_MEMBERS members allowed")
        }

        // Validate owner is in memberIds
        if (group.owner.isNotBlank() && group.owner !in group.memberIds) {
            errors.add("Group owner must be in memberIds array")
        }

        // Validate join code format (6 alphanumeric characters)
        if (group.joinCode.isNotBlank()) {
            if (group.joinCode.length != 6) {
                errors.add("Join code must be exactly 6 characters")
            } else if (!group.joinCode.matches(Regex("^[A-Za-z0-9]{6}$"))) {
                errors.add("Join code must contain only alphanumeric characters")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }

    override fun validateMessage(message: Message): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate required fields
        if (message.chatId.isBlank()) {
            errors.add("Chat ID is required")
        }

        if (message.senderId.isBlank()) {
            errors.add("Sender ID is required")
        }

        // Validate at least one content field is present
        val hasText = message.text.isNotBlank()
        val hasImage =
                !message.imageUrl.isNullOrBlank() ||
                        (message.attachmentType == "image" &&
                                !message.attachmentUrl.isNullOrBlank())
        val hasDocument =
                !message.documentUrl.isNullOrBlank() ||
                        (message.attachmentType == "document" &&
                                !message.attachmentUrl.isNullOrBlank())
        val hasAudio = message.attachmentType == "audio" && !message.attachmentUrl.isNullOrBlank()
        val hasVideo = message.attachmentType == "video" && !message.attachmentUrl.isNullOrBlank()

        if (!hasText && !hasImage && !hasDocument && !hasAudio && !hasVideo) {
            errors.add(
                    "Message must have at least one content field (text, image, document, audio, or video)"
            )
        }

        // Validate text length
        if (message.text.length > MAX_MESSAGE_TEXT_LENGTH) {
            errors.add("Message text must not exceed $MAX_MESSAGE_TEXT_LENGTH characters")
        }

        // Validate URLs are Firebase Storage URLs
        message.imageUrl?.let { url ->
            if (url.isNotBlank() && !url.startsWith(FIREBASE_STORAGE_URL_PREFIX)) {
                errors.add("Image URL must be a Firebase Storage URL")
            }
        }

        message.documentUrl?.let { url ->
            if (url.isNotBlank() && !url.startsWith(FIREBASE_STORAGE_URL_PREFIX)) {
                errors.add("Document URL must be a Firebase Storage URL")
            }
        }

        message.attachmentUrl?.let { url ->
            if (url.isNotBlank() && !url.startsWith(FIREBASE_STORAGE_URL_PREFIX)) {
                errors.add("Attachment URL must be a Firebase Storage URL")
            }
        }

        // Validate timestamp is recent (within 5 minutes)
        message.timestamp?.let { timestamp ->
            val now = System.currentTimeMillis()
            val messageTime = timestamp.time
            if (abs(now - messageTime) > TIMESTAMP_TOLERANCE_MS) {
                errors.add("Message timestamp must be within 5 minutes of current time")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }
}
