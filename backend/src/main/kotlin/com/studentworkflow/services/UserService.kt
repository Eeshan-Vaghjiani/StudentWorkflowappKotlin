
package com.studentworkflow.services

import com.studentworkflow.db.Users
import com.studentworkflow.models.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

class UserService {

    fun createUser(name: String, email: String, password: String): User {
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        return transaction {
            val id = Users.insert {
                it[Users.name] = name
                it[Users.email] = email
                it[Users.password] = hashedPassword
                it[Users.createdAt] = Instant.now()
            } get Users.id

            User(id.value, name, email, Instant.now().toString())
        }
    }

    fun findUserByEmail(email: String): User? {
        return transaction {
            Users.select { Users.email eq email }.singleOrNull()?.let {
                User(
                    id = it[Users.id].value,
                    name = it[Users.name],
                    email = it[Users.email],
                    createdAt = it[Users.createdAt].toString()
                )
            }
        }
    }

    fun verifyPassword(user: User, password: String): Boolean {
        return transaction {
            val hashedPassword = Users.select { Users.id eq user.id }.singleOrNull()?.get(Users.password)
            if (hashedPassword != null) {
                BCrypt.checkpw(password, hashedPassword)
            } else {
                false
            }
        }
    }
}
