
package com.studentworkflow.services

import com.studentworkflow.db.PricingPackages
import com.studentworkflow.db.Subscriptions
import com.studentworkflow.db.Users
import com.studentworkflow.models.PricingPackage
import com.studentworkflow.models.Subscription
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * PricingService handles pricing packages, subscriptions, and payment processing.
 * Manages CRUD operations on subscription tiers and user subscription management.
 */
class PricingService(private val promptService: PromptService) {

    /**
     * Get all active pricing packages
     */
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

    /**
     * Get a specific pricing package by ID
     */
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

    /**
     * Create a new pricing package
     */
    fun createPackage(
        name: String,
        description: String?,
        price: Double,
        promptsCount: Int,
        sortOrder: Int = 0
    ): PricingPackage? {
        return transaction {
            val id = PricingPackages.insert {
                it[PricingPackages.name] = name
                it[PricingPackages.description] = description
                it[PricingPackages.price] = price.toBigDecimal()
                it[PricingPackages.promptsCount] = promptsCount
                it[PricingPackages.isActive] = true
                it[PricingPackages.sortOrder] = sortOrder
                it[createdAt] = Instant.now()
            } get PricingPackages.id

            PricingPackage(
                id = id.value,
                name = name,
                description = description,
                price = price,
                promptsCount = promptsCount,
                isActive = true,
                sortOrder = sortOrder,
                createdAt = Instant.now().toString()
            )
        }
    }

    /**
     * Update a pricing package
     */
    fun updatePackage(
        id: Int,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        promptsCount: Int? = null,
        isActive: Boolean? = null,
        sortOrder: Int? = null
    ): Boolean {
        return transaction {
            PricingPackages.update({ PricingPackages.id eq id }) {
                name?.let { value -> it[PricingPackages.name] = value }
                description?.let { value -> it[PricingPackages.description] = value }
                price?.let { value -> it[PricingPackages.price] = value.toBigDecimal() }
                promptsCount?.let { value -> it[PricingPackages.promptsCount] = value }
                isActive?.let { value -> it[PricingPackages.isActive] = value }
                sortOrder?.let { value -> it[PricingPackages.sortOrder] = value }
            } > 0
        }
    }

    /**
     * Delete a pricing package (soft delete by setting isActive to false)
     */
    fun deletePackage(id: Int): Boolean {
        return transaction {
            PricingPackages.update({ PricingPackages.id eq id }) {
                it[isActive] = false
            } > 0
        }
    }

    /**
     * Process a purchase/subscription
     */
    fun processPurchase(userId: Int, packageId: Int, paymentId: String? = null): Map<String, Any> {
        val packageDetails = getPackage(packageId)

        if (packageDetails == null) {
            return mapOf(
                "success" to false,
                "message" to "Package not found"
            )
        }

        return transaction {
            try {
                // Add prompts to user account
                val success = promptService.addPrompts(userId, packageDetails.promptsCount)
                
                if (success) {
                    // Create subscription record
                    val subscriptionId = Subscriptions.insert {
                        it[Subscriptions.userId] = userId
                        it[planName] = packageDetails.name
                        it[startDate] = Instant.now()
                        it[endDate] = Instant.now().plus(30, ChronoUnit.DAYS) // 30-day subscription
                        it[isActive] = true
                        it[Subscriptions.paymentId] = paymentId
                    } get Subscriptions.id

                    // Update user payment status
                    Users.update({ Users.id eq userId }) {
                        it[isPaidUser] = true
                        it[lastPaymentDate] = Instant.now()
                        it[totalPromptsPurchased] = Users.totalPromptsPurchased + packageDetails.promptsCount
                    }

                    val totalPrompts = Users.select { Users.id eq userId }
                        .singleOrNull()?.get(Users.aiPromptsRemaining) ?: 0

                    mapOf(
                        "success" to true,
                        "message" to "Purchase successful",
                        "subscription_id" to subscriptionId.value,
                        "prompts_added" to packageDetails.promptsCount,
                        "total_prompts" to totalPrompts
                    )
                } else {
                    mapOf(
                        "success" to false,
                        "message" to "Failed to add prompts to user account"
                    )
                }
            } catch (e: Exception) {
                mapOf(
                    "success" to false,
                    "message" to "Purchase failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Get user's active subscription
     */
    fun getUserActiveSubscription(userId: Int): Subscription? {
        return transaction {
            Subscriptions.select { 
                (Subscriptions.userId eq userId) and 
                (Subscriptions.isActive eq true) and
                (Subscriptions.endDate greater Instant.now())
            }
            .orderBy(Subscriptions.endDate, SortOrder.DESC)
            .limit(1)
            .singleOrNull()?.let { row ->
                Subscription(
                    id = row[Subscriptions.id].value,
                    userId = row[Subscriptions.userId],
                    planName = row[Subscriptions.planName],
                    startDate = row[Subscriptions.startDate].toString(),
                    endDate = row[Subscriptions.endDate].toString(),
                    isActive = row[Subscriptions.isActive],
                    paymentId = row[Subscriptions.paymentId]
                )
            }
        }
    }

    /**
     * Get all user subscriptions (including expired)
     */
    fun getUserSubscriptions(userId: Int): List<Subscription> {
        return transaction {
            Subscriptions.select { Subscriptions.userId eq userId }
                .orderBy(Subscriptions.startDate, SortOrder.DESC)
                .map { row ->
                    Subscription(
                        id = row[Subscriptions.id].value,
                        userId = row[Subscriptions.userId],
                        planName = row[Subscriptions.planName],
                        startDate = row[Subscriptions.startDate].toString(),
                        endDate = row[Subscriptions.endDate].toString(),
                        isActive = row[Subscriptions.isActive],
                        paymentId = row[Subscriptions.paymentId]
                    )
                }
        }
    }

    /**
     * Cancel a subscription
     */
    fun cancelSubscription(subscriptionId: Int, userId: Int): Boolean {
        return transaction {
            Subscriptions.update({ 
                (Subscriptions.id eq subscriptionId) and (Subscriptions.userId eq userId)
            }) {
                it[isActive] = false
                it[endDate] = Instant.now()
            } > 0
        }
    }

    /**
     * Renew a subscription
     */
    fun renewSubscription(
        userId: Int, 
        packageId: Int, 
        paymentId: String? = null,
        durationDays: Int = 30
    ): Map<String, Any> {
        val packageDetails = getPackage(packageId)

        if (packageDetails == null) {
            return mapOf(
                "success" to false,
                "message" to "Package not found"
            )
        }

        return transaction {
            try {
                // Deactivate current subscription
                Subscriptions.update({ 
                    (Subscriptions.userId eq userId) and (Subscriptions.isActive eq true)
                }) {
                    it[isActive] = false
                }

                // Create new subscription
                val subscriptionId = Subscriptions.insert {
                    it[Subscriptions.userId] = userId
                    it[planName] = packageDetails.name
                    it[startDate] = Instant.now()
                    it[endDate] = Instant.now().plus(durationDays.toLong(), ChronoUnit.DAYS)
                    it[isActive] = true
                    it[Subscriptions.paymentId] = paymentId
                } get Subscriptions.id

                // Add prompts
                promptService.addPrompts(userId, packageDetails.promptsCount)

                // Update user status
                Users.update({ Users.id eq userId }) {
                    it[lastPaymentDate] = Instant.now()
                    it[totalPromptsPurchased] = Users.totalPromptsPurchased + packageDetails.promptsCount
                }

                mapOf(
                    "success" to true,
                    "message" to "Subscription renewed successfully",
                    "subscription_id" to subscriptionId.value,
                    "end_date" to Instant.now().plus(durationDays.toLong(), ChronoUnit.DAYS).toString()
                )
            } catch (e: Exception) {
                mapOf(
                    "success" to false,
                    "message" to "Renewal failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Check if user has active subscription
     */
    fun hasActiveSubscription(userId: Int): Boolean {
        return getUserActiveSubscription(userId) != null
    }

    /**
     * Get subscription statistics
     */
    fun getSubscriptionStats(): Map<String, Any> {
        return transaction {
            val totalSubscriptions = Subscriptions.selectAll().count()
            val activeSubscriptions = Subscriptions.select { 
                (Subscriptions.isActive eq true) and (Subscriptions.endDate greater Instant.now())
            }.count()
            val expiredSubscriptions = Subscriptions.select {
                (Subscriptions.isActive eq false) or (Subscriptions.endDate lessEq Instant.now())
            }.count()

            mapOf(
                "total_subscriptions" to totalSubscriptions,
                "active_subscriptions" to activeSubscriptions,
                "expired_subscriptions" to expiredSubscriptions,
                "revenue" to getTotalRevenue()
            )
        }
    }

    /**
     * Calculate total revenue from all subscriptions
     */
    fun getTotalRevenue(): Double {
        return transaction {
            val packages = getPackages()
            var totalRevenue = 0.0

            Subscriptions.selectAll().forEach { subscription ->
                val planName = subscription[Subscriptions.planName]
                val packagePrice = packages.find { it.name == planName }?.price ?: 0.0
                totalRevenue += packagePrice
            }

            totalRevenue
        }
    }

    /**
     * Get expiring subscriptions (within next 7 days)
     */
    fun getExpiringSubscriptions(days: Int = 7): List<Subscription> {
        return transaction {
            val expirationDate = Instant.now().plus(days.toLong(), ChronoUnit.DAYS)
            
            Subscriptions.select {
                (Subscriptions.isActive eq true) and
                (Subscriptions.endDate lessEq expirationDate) and
                (Subscriptions.endDate greater Instant.now())
            }.map { row ->
                Subscription(
                    id = row[Subscriptions.id].value,
                    userId = row[Subscriptions.userId],
                    planName = row[Subscriptions.planName],
                    startDate = row[Subscriptions.startDate].toString(),
                    endDate = row[Subscriptions.endDate].toString(),
                    isActive = row[Subscriptions.isActive],
                    paymentId = row[Subscriptions.paymentId]
                )
            }
        }
    }

    /**
     * Automatically expire subscriptions that have passed their end date
     */
    fun expireOldSubscriptions(): Int {
        return transaction {
            Subscriptions.update({
                (Subscriptions.isActive eq true) and (Subscriptions.endDate lessEq Instant.now())
            }) {
                it[isActive] = false
            }
        }
    }
}
