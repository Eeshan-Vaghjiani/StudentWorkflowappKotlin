


package com.studentworkflow

import com.studentworkflow.db.DatabaseFactory
import com.studentworkflow.plugins.configureRouting
import com.studentworkflow.plugins.configureSecurity
import com.studentworkflow.plugins.configureSerialization
import com.studentworkflow.routes.*
import com.studentworkflow.services.*
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
    configureSecurity()

    // Initialize services
    val promptService = PromptService()
    val aiService = AIService(promptService)
    val twoFactorAuthenticationService = TwoFactorAuthenticationService()
    val pricingService = PricingService(promptService)
    val userService = UserService()
    val jwtService = JwtService()
    val emailService = EmailService()
    val passwordResetService = PasswordResetService(userService)
    val fileService = FileService()
    val notificationService = NotificationService()

    // Configure routing
    configureRouting()
    
    // Configure all route modules
    configureAIRoutes(aiService)
    configureAuthRoutes(userService, jwtService, twoFactorAuthenticationService, passwordResetService, emailService)
    configureUserRoutes(userService, fileService)
    configureTaskRoutes(notificationService)
    configureStudyGroupRoutes(notificationService)
    configureMessageRoutes(notificationService)
    configureStudySessionRoutes()
    configureNotificationRoutes(notificationService)
    configureSubscriptionRoutes(pricingService)
    configureAdminRoutes(userService, pricingService)
}


