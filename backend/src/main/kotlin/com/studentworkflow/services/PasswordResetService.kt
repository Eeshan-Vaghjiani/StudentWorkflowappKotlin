package com.studentworkflow.services

class PasswordResetService(private val userService: UserService) {
    fun createPasswordResetToken(email: String): String? = "placeholder-token"
    fun resetPassword(token: String, newPassword: String): Boolean = true
}
