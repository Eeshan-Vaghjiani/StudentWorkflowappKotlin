package com.example.loginandregistration.validation

/** Represents the result of a validation operation */
data class ValidationResult(val isValid: Boolean, val errors: List<String> = emptyList()) {
    companion object {
        fun success() = ValidationResult(isValid = true, errors = emptyList())

        fun failure(vararg errors: String) =
                ValidationResult(isValid = false, errors = errors.toList())

        fun failure(errors: List<String>) = ValidationResult(isValid = false, errors = errors)
    }

    /** Get a formatted error message combining all errors */
    fun getErrorMessage(): String {
        return errors.joinToString("; ")
    }
}
