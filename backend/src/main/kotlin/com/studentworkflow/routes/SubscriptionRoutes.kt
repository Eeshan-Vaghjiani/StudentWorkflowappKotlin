package com.studentworkflow.routes

import com.studentworkflow.models.SubscribeToPlanRequest
import com.studentworkflow.services.PricingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * SubscriptionRoutes handles subscription management endpoints
 * including plan retrieval and subscription management.
 */
fun Application.configureSubscriptionRoutes(
    pricingService: PricingService
) {
    routing {
        authenticate("jwt") {
            route("/api/subscriptions") {
                
                // Get available pricing plans
                get("/plans") {
                    val plans = pricingService.getPackages()
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "plans" to plans
                    ))
                }

                // Get specific plan
                get("/plans/{id}") {
                    val planId = call.parameters["id"]?.toIntOrNull()
                    
                    if (planId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid plan ID"))
                        return@get
                    }

                    val plan = pricingService.getPackage(planId)
                    
                    if (plan != null) {
                        call.respond(HttpStatusCode.OK, plan)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Plan not found"))
                    }
                }

                // Subscribe to a plan
                post("/subscribe") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<SubscribeToPlanRequest>()
                    val result = pricingService.processPurchase(userId, request.planId, request.paymentId)
                    
                    if (result["success"] == true) {
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, result)
                    }
                }

                // Get user's current subscription
                get("/current") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val subscription = pricingService.getUserActiveSubscription(userId)
                    
                    if (subscription != null) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "subscription" to subscription
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "No active subscription found"
                        ))
                    }
                }

                // Get user's subscription history
                get("/history") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val subscriptions = pricingService.getUserSubscriptions(userId)
                    
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "subscriptions" to subscriptions
                    ))
                }

                // Cancel subscription
                post("/{id}/cancel") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    val subscriptionId = call.parameters["id"]?.toIntOrNull()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    if (subscriptionId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid subscription ID"))
                        return@post
                    }

                    val success = pricingService.cancelSubscription(subscriptionId, userId)
                    
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf(
                            "success" to true,
                            "message" to "Subscription cancelled successfully"
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf(
                            "success" to false,
                            "message" to "Subscription not found"
                        ))
                    }
                }

                // Renew subscription
                post("/renew") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@post
                    }

                    val request = call.receive<SubscribeToPlanRequest>()
                    val result = pricingService.renewSubscription(
                        userId = userId,
                        packageId = request.planId,
                        paymentId = request.paymentId
                    )
                    
                    if (result["success"] == true) {
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, result)
                    }
                }

                // Check subscription status
                get("/status") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asInt()
                    
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                        return@get
                    }

                    val hasActive = pricingService.hasActiveSubscription(userId)
                    val currentSubscription = pricingService.getUserActiveSubscription(userId)
                    
                    call.respond(HttpStatusCode.OK, mapOf(
                        "success" to true,
                        "has_active_subscription" to hasActive,
                        "current_subscription" to currentSubscription
                    ))
                }
            }
        }
    }
}