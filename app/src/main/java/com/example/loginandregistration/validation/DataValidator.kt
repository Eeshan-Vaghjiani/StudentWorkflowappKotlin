package com.example.loginandregistration.validation

import com.example.loginandregistration.models.FirebaseGroup
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.models.Message

/** Interface for validating data before Firestore operations */
interface DataValidator {
    /** Validate a task before creating or updating */
    fun validateTask(task: FirebaseTask): ValidationResult

    /** Validate a group before creating or updating */
    fun validateGroup(group: FirebaseGroup): ValidationResult

    /** Validate a message before sending */
    fun validateMessage(message: Message): ValidationResult
}
