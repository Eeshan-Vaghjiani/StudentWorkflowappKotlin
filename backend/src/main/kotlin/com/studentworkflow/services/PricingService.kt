package com.studentworkflow.services

import com.studentworkflow.models.PricingPackage

class PricingService(private val promptService: PromptService) {
    fun getPackages(): List<PricingPackage> = emptyList()
    fun getPackage(id: Int): PricingPackage? = null
    fun processPurchase(userId: Int, packageId: Int): Map<String, Any> = mapOf("success" to true)
}
