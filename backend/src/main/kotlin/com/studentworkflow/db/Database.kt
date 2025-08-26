
package com.studentworkflow.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.drop(Users)
            SchemaUtils.create(Users, StudyGroups, GroupMemberships, Messages, Tasks, StudySessions, PomodoroSessions, UserSettings, Subscriptions, GoogleCalendars, AIUsageLogs, Notifications, PricingPackages, PasswordResetTokens)
        }
    }
}
