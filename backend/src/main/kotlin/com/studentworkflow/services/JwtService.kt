
package com.studentworkflow.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtService {

    private val secret = System.getenv("JWT_SECRET") ?: "your-secret"
    private val issuer = System.getenv("JWT_ISSUER") ?: "your-issuer"
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: Int, email: String): String = JWT.create()
        .withAudience(email)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000 * 60 * 24)) // 24 hours
        .sign(algorithm)

    fun verifyToken(token: String): Int? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
            val decodedToken = verifier.verify(token)
            decodedToken.getClaim("userId").asInt()
        } catch (e: Exception) {
            null
        }
    }
}
