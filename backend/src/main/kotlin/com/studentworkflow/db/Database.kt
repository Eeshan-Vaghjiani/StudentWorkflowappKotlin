package com.studentworkflow.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        try {
            println("Connecting to database...")
            val driverClassName = "org.h2.Driver"
            val jdbcURL = System.getenv("DATABASE_URL") ?: "jdbc:h2:file:./build/db"
            val username = System.getenv("DATABASE_USER") ?: "sa"
            val password = System.getenv("DATABASE_PASSWORD") ?: ""
            
            println("Database URL: $jdbcURL")
            val database = Database.connect(jdbcURL, driverClassName, username, password)
            println("Database connected successfully")
            
            println("Creating tables...")
            transaction(database) {
                // Create only the essential tables first
                SchemaUtils.createMissingTablesAndColumns(Users)
                println("Users table created")
                
                SchemaUtils.createMissingTablesAndColumns(StudyGroups)
                println("StudyGroups table created")
                
                SchemaUtils.createMissingTablesAndColumns(GroupMemberships)
                println("GroupMemberships table created")
                
                SchemaUtils.createMissingTablesAndColumns(Messages)
                println("Messages table created")
                
                SchemaUtils.createMissingTablesAndColumns(Tasks)
                println("Tasks table created")
                
                SchemaUtils.createMissingTablesAndColumns(StudySessions)
                println("StudySessions table created")
                
                SchemaUtils.createMissingTablesAndColumns(PomodoroSessions)
                println("PomodoroSessions table created")
                
                SchemaUtils.createMissingTablesAndColumns(UserSettings)
                println("UserSettings table created")
                
                SchemaUtils.createMissingTablesAndColumns(Subscriptions)
                println("Subscriptions table created")
                
                SchemaUtils.createMissingTablesAndColumns(GoogleCalendars)
                println("GoogleCalendars table created")
                
                SchemaUtils.createMissingTablesAndColumns(AIUsageLogs)
                println("AIUsageLogs table created")
                
                SchemaUtils.createMissingTablesAndColumns(Notifications)
                println("Notifications table created")
                
                SchemaUtils.createMissingTablesAndColumns(PricingPackages)
                println("PricingPackages table created")
                
                SchemaUtils.createMissingTablesAndColumns(PasswordResetTokens)
                println("PasswordResetTokens table created")
            }
            println("All tables created successfully")
        } catch (e: Exception) {
            println("Database initialization failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
