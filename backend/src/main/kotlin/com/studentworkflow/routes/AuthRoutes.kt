
package com.studentworkflow.routes

import com.studentworkflow.models.AuthResponse
import com.studentworkflow.models.LoginRequest
import com.studentworkflow.models.RegisterRequest
import com.studentworkflow.models.TwoFactorVerifyRequest
import com.studentworkflow.services.JwtService
import com.studentworkflow.services.TwoFactorAuthenticationService
import com.studentworkflow.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureAuthRoutes(
    userService: UserService,
    jwtService: JwtService,
    twoFactorAuthenticationService: TwoFactorAuthenticationService,
    passwordResetService: com.studentworkflow.services.PasswordResetService,
    emailService: com.studentworkflow.services.EmailService
) {
    routing {
        route("/api/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                val user = transaction { userService.findUserByEmail(request.email) }
                if (user != null) {
                    call.respond(HttpStatusCode.Conflict, AuthResponse(false, "User with this email already exists."))
                    return@post
                }

                val newUser = userService.createUser(request.name, request.email, request.password)
                call.respond(HttpStatusCode.Created, AuthResponse(true, "User registered successfully.", userId = newUser.id)) // Pass userId
            }

            post("/login") {
                val request = call.receive<LoginRequest>()
                val user = transaction { userService.findUserByEmail(request.email) }
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, "Invalid credentials."))
                    return@post
                }

                if (!userService.verifyPassword(user, request.password)) {
                    call.respond(HttpStatusCode.Unauthorized, AuthResponse(false, "Invalid credentials."))
                    return@post
                }

                // Check if 2FA is enabled for this user (simplified check for now)
                val twoFactorEnabled = false // TODO: Implement proper 2FA check
                if (twoFactorEnabled) {
                    call.respond(HttpStatusCode.OK, AuthResponse(true, "2FA required.", userId = user.id, twoFactorRequired = true)) // Pass userId
                } else {
                    val token = jwtService.generateToken(user.id, user.email)
                    call.respond(HttpStatusCode.OK, AuthResponse(true, "Login successful.", token = token, userId = user.id)) // Pass userId
                }
            }

            post("/logout") {
                // For JWT, logout is typically handled client-side by discarding the token.
                // On the server, we just confirm the action.
                call.respond(HttpStatusCode.OK, AuthResponse(true, "Logged out successfully."))
            }

            post("/two-factor-setup") {
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, AuthResponse(false, "User ID is required."))
                    return@post
                }

                val secret = twoFactorAuthenticationService.generateSecretKey()
                val qrCodeSvg = twoFactorAuthenticationService.qrCodeSvg("StudentWorkflowApp", "user@example.com", secret) // Replace with actual user email
                
                // Store the secret temporarily or associate it with the user for confirmation
                // For now, we'll just return it. In a real app, you'd store it and confirm later.
                call.respond(HttpStatusCode.OK, mapOf("secret" to secret, "qrCodeSvg" to qrCodeSvg))
            }

            post("/two-factor-verify") {
                val request = call.receive<TwoFactorVerifyRequest>()
                val userId = request.userId
                val code = request.code

                // TODO: Implement proper 2FA verification
                // For now, just return success to allow compilation
                call.respond(HttpStatusCode.OK, AuthResponse(true, "2FA verification not implemented yet.", userId = userId))
            }
        }
    }
}
