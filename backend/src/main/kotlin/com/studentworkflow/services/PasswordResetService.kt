
package com.studentworkflow.services

import com.studentworkflow.db.PasswordResetTokens
import com.studentworkflow.db.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.util.UUID

class PasswordResetService(private val userService: UserService) {

    fun createPasswordResetToken(email: String): String? {
        return transaction {
            val user = userService.findUserByEmail(email)
            if (user == null) {
                null
            } else {
                val token = UUID.randomUUID().toString()
                PasswordResetTokens.insert {
                    it[PasswordResetTokens.email] = email
                    it[PasswordResetTokens.token] = token
                    it[createdAt] = Instant.now()
                }
                token
            }
        }
    }

    fun resetPassword(token: String, newPassword: String): Boolean {
        return transaction {
            val resetToken = PasswordResetTokens.select { PasswordResetTokens.token eq token }.singleOrNull()
            if (resetToken == null) {
                false
            } else {
                val email = resetToken[PasswordResetTokens.email]
                val user = userService.findUserByEmail(email)
                if (user == null) {
                    false
                } else {
                    val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                    Users.update({ Users.id eq user.id }) {
                        it[Users.password] = hashedPassword
                    }
                    PasswordResetTokens.deleteWhere { PasswordResetTokens.token eq token }
                    true
                }
            }
        }
    }
}
