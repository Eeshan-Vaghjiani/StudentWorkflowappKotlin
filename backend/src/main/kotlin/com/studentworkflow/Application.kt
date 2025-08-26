


package com.studentworkflow

import com.studentworkflow.db.DatabaseFactory
import com.studentworkflow.plugins.configureRouting
import com.studentworkflow.plugins.configureSecurity
import com.studentworkflow.plugins.configureSerialization
import com.studentworkflow.routes.configureAIRoutes
import com.studentworkflow.routes.configureAuthRoutes
import com.studentworkflow.services.AIService
import com.studentworkflow.services.EmailService
import com.studentworkflow.services.JwtService
import com.studentworkflow.services.PasswordResetService
import com.studentworkflow.services.PricingService
import com.studentworkflow.services.PromptService
import com.studentworkflow.services.TwoFactorAuthenticationService
import com.studentworkflow.services.UserService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureSecurity() // Call configureSecurity here

    val promptService = PromptService()
    val aiService = AIService(promptService)
    val twoFactorAuthenticationService = TwoFactorAuthenticationService()
    val pricingService = PricingService(promptService)
    val userService = UserService()
    val jwtService = JwtService()
    val emailService = EmailService() // New
    val passwordResetService = PasswordResetService(userService) // New

    configureRouting()
    configureAIRoutes(aiService)
    configureAuthRoutes(userService, jwtService, twoFactorAuthenticationService, passwordResetService, emailService) // Updated
}


