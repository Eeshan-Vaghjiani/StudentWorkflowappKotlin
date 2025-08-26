
package com.studentworkflow.services

import com.studentworkflow.db.PricingPackages
import com.studentworkflow.db.Users
import com.studentworkflow.models.PricingPackage
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PricingService(private val promptService: PromptService) {

    fun getPackages(): List<PricingPackage> {
        return transaction {
            PricingPackages.select { PricingPackages.isActive eq true }
                .orderBy(PricingPackages.sortOrder)
                .map { row ->
                    PricingPackage(
                        id = row[PricingPackages.id].value,
                        name = row[PricingPackages.name],
                        description = row[PricingPackages.description],
                        price = row[PricingPackages.price].toDouble(),
                        promptsCount = row[PricingPackages.promptsCount],
                        isActive = row[PricingPackages.isActive],
                        sortOrder = row[PricingPackages.sortOrder],
                        createdAt = row[PricingPackages.createdAt].toString()
                    )
                }
        }
    }

    fun getPackage(id: Int): PricingPackage? {
        return transaction {
            PricingPackages.select { (PricingPackages.id eq id) and (PricingPackages.isActive eq true) }
                .singleOrNull()?.let { row ->
                    PricingPackage(
                        id = row[PricingPackages.id].value,
                        name = row[PricingPackages.name],
                        description = row[PricingPackages.description],
                        price = row[PricingPackages.price].toDouble(),
                        promptsCount = row[PricingPackages.promptsCount],
                        isActive = row[PricingPackages.isActive],
                        sortOrder = row[PricingPackages.sortOrder],
                        createdAt = row[PricingPackages.createdAt].toString()
                    )
                }
        }
    }

    fun processPurchase(userId: Int, packageId: Int): Map<String, Any> {
        val packageDetails = getPackage(packageId)

        if (packageDetails == null) {
            return mapOf(
                "success" to false,
                "message" to "Package not found"
            )
        }

        val success = promptService.addPrompts(userId, packageDetails.promptsCount)

        return if (success) {
            val totalPrompts = transaction {
                Users.select { Users.id eq userId }.singleOrNull()?.get(Users.aiPromptsRemaining)
            }
            mapOf(
                "success" to true,
                "message" to "Purchase successful",
                "prompts_added" to packageDetails.promptsCount,
                "total_prompts" to (totalPrompts ?: 0)
            )
        } else {
            mapOf(
                "success" to false,
                "message" to "Failed to add prompts to user account"
            )
        }
    }
}
