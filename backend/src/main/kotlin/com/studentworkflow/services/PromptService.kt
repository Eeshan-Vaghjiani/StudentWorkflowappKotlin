
package com.studentworkflow.services

import com.studentworkflow.db.AIUsageLogs
import com.studentworkflow.db.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class PromptService {

    fun hasPromptsRemaining(userId: Int): Boolean {
        return transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
            (user?.get(Users.aiPromptsRemaining) ?: 0) > 0
        }
    }

    fun usePrompt(userId: Int, serviceType: String, count: Int = 1): Boolean {
        return transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
            if (user != null) {
                val remaining = user[Users.aiPromptsRemaining]
                if (remaining >= count) {
                    Users.update({ Users.id eq userId }) {
                        it[aiPromptsRemaining] = remaining - count
                    }
                    AIUsageLogs.insert {
                        it[AIUsageLogs.userId] = userId
                        it[AIUsageLogs.serviceType] = serviceType
                        it[promptsUsed] = count
                        it[remainingPromptsAfter] = remaining - count
                        it[createdAt] = Instant.now()
                    }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    fun addPrompts(userId: Int, count: Int) {
        transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
            if (user != null) {
                val remaining = user[Users.aiPromptsRemaining]
                val total = user[Users.totalPromptsPurchased]
                Users.update({ Users.id eq userId }) {
                    it[aiPromptsRemaining] = remaining + count
                    it[totalPromptsPurchased] = total + count
                    it[isPaidUser] = true
                    it[lastPaymentDate] = Instant.now()
                }
            }
        }
    }
}
