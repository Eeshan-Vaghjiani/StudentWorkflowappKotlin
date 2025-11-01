package com.example.loginandregistration.validation

/** Exception thrown when data validation fails */
class ValidationException(message: String, val errors: List<String> = listOf(message)) :
        Exception(message) {

    constructor(
            validationResult: ValidationResult
    ) : this(message = validationResult.getErrorMessage(), errors = validationResult.errors)

    /** Get a user-friendly error message */
    fun getUserMessage(): String {
        return if (errors.size == 1) {
            errors.first()
        } else {
            "Validation failed:\n${errors.joinToString("\n• ", prefix = "• ")}"
        }
    }
}
