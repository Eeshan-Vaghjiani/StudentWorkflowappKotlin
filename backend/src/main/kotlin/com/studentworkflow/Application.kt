package com.studentworkflow

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
    println("Starting minimal test application on port $port")
    
    try {
        embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
            .start(wait = true)
    } catch (e: Exception) {
        println("Failed to start application: ${e.message}")
        e.printStackTrace()
    }
}

fun Application.module() {
    try {
        println("Configuring minimal routing...")
        routing {
            get("/") {
                call.respondText("Hello World! Application is working!", ContentType.Text.Plain)
            }
            
            get("/health") {
                call.respondText("OK", ContentType.Text.Plain)
            }
            
            get("/test") {
                call.respondText("Test endpoint working!", ContentType.Text.Plain)
            }
        }
        println("Minimal routing configured successfully")
        
        println("Application module configured successfully")
    } catch (e: Exception) {
        println("Error in module configuration: ${e.message}")
        e.printStackTrace()
        throw e
    }
}
