
package com.studentworkflow.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val secret = System.getenv("JWT_SECRET") ?: "your-secret"
    val issuer = System.getenv("JWT_ISSUER") ?: "your-issuer"
    val audience = System.getenv("JWT_AUDIENCE") ?: "your-audience"
    val myRealm = System.getenv("JWT_REALM") ?: "your-realm"

    authentication {
        jwt {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build())
            validate { credential ->
                if (credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
