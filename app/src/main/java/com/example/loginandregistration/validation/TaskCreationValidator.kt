package com.example.loginandregistration.validation

import com.google.firebase.Timestamp

/**
 * Validator for task creation with comprehensive validation rules. Ensures all task data meets
 * requirements before Firestore operations.
 */
class TaskCreationValidator {

    /**
     * Validates all task creation fields
     * @param title Task title
     * @param description Task description
     * @param assignedTo List of user IDs assigned to the task
     * @param creatorId ID of the user creating the task
     * @param dueDate Optional due date
     * @param priority Task priority (low/medium/high)
     * @param category Task category (personal/group/assignment)
     * @return ValidationResult indicating if valid or containing error messages
     */
    fun validateTaskCreation(
            title: String,
            description: String,
            assignedTo: List<String>,
            creatorId: String,
            dueDate: Timestamp?,
            priority: String,
            category: String
    ): TaskValidationResult {
        val errors = mutableListOf<String>()

        // Title validation (required, 1-200 chars)
        when {
            title.isBlank() -> errors.add("Title is required")
            title.length > 200 -> errors.add("Title must be 200 characters or less")
        }

        // Description validation (0-1000 chars)
        if (description.length > 1000) {
            errors.add("Description must be 1000 characters or less")
        }

        // Assignee validation (at least 1, max 50, must include creator)
        when {
            assignedTo.isEmpty() -> errors.add("At least one assignee is required")
            assignedTo.size > 50 -> errors.add("Maximum 50 assignees allowed")
            !assignedTo.contains(creatorId) ->
                    errors.add("Task creator must be in assignedTo array")
        }

        // Priority validation (must be low/medium/high)
        if (priority !in listOf("low", "medium", "high")) {
            errors.add("Invalid priority value. Must be low, medium, or high")
        }

        // Category validation (must be personal/group/assignment)
        if (category !in listOf("personal", "group", "assignment")) {
            errors.add("Invalid category value. Must be personal, group, or assignment")
        }

        return if (errors.isEmpty()) {
            TaskValidationResult.Valid
        } else {
            TaskValidationResult.Invalid(errors)
        }
    }

    /** Validates only the title field for real-time validation */
    fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "Title is required"
            title.length > 200 -> "Title must be 200 characters or less"
            else -> null
        }
    }

    /** Validates only the description field for real-time validation */
    fun validateDescription(description: String): String? {
        return if (description.length > 1000) {
            "Description must be 1000 characters or less"
        } else {
            null
        }
    }

    /** Validates assignees list for real-time validation */
    fun validateAssignees(assignedTo: List<String>, creatorId: String): String? {
        return when {
            assignedTo.isEmpty() -> "At least one assignee is required"
            assignedTo.size > 50 -> "Maximum 50 assignees allowed"
            !assignedTo.contains(creatorId) -> "You must be assigned to the task"
            else -> null
        }
    }
}

/** Sealed class representing the result of task validation */
sealed class TaskValidationResult {
    /** Validation passed - task data is valid */
    object Valid : TaskValidationResult()

    /** Validation failed - contains list of error messages */
    data class Invalid(val errors: List<String>) : TaskValidationResult()
}
