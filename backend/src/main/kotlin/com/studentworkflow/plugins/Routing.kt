
package com.studentworkflow.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!", ContentType.Text.Plain)
        }
        
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain)
        }
        
        // Add other routes here
    }
}
