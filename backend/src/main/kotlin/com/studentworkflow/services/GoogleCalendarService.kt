
package com.studentworkflow.services

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.studentworkflow.db.GoogleCalendars
import com.studentworkflow.db.Tasks
import com.studentworkflow.models.Task
import io.ktor.client.* 
import io.ktor.client.engine.cio.* 
import io.ktor.client.request.* 
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class GoogleCalendarService {

    private val clientId = System.getenv("GOOGLE_CLIENT_ID") ?: ""
    private val clientSecret = System.getenv("GOOGLE_CLIENT_SECRET") ?: ""
    private val redirectUri = System.getenv("GOOGLE_REDIRECT_URI") ?: ""

    private val scopes = listOf("https://www.googleapis.com/auth/calendar", "https://www.googleapis.com/auth/calendar.events")

    private val flow = GoogleAuthorizationCodeFlow.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        clientId,
        clientSecret,
        scopes
    ).setAccessType("offline").setApprovalPrompt("force").build()

    private val client = HttpClient(CIO)

    fun getAuthorizationUrl(): String {
        return flow.newAuthorizationUrl().setRedirectUri(redirectUri).build()
    }

    suspend fun handleCallback(code: String, userId: Int): Boolean {
        return try {
            val response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute()
            val credentials = flow.createAndStoreCredential(response, userId.toString())

            transaction {
                val existing = GoogleCalendars.select { GoogleCalendars.userId eq userId }.singleOrNull()
                if (existing != null) {
                    GoogleCalendars.update({ GoogleCalendars.userId eq userId }) {
                        it[accessToken] = credentials.accessToken
                        it[refreshToken] = credentials.refreshToken
                        it[tokenExpiresAt] = Instant.ofEpochMilli(credentials.expirationTimeMilliseconds)
                    }
                } else {
                    GoogleCalendars.insert {
                        it[GoogleCalendars.userId] = userId
                        it[calendarId] = "primary" // Default to primary calendar
                        it[accessToken] = credentials.accessToken
                        it[refreshToken] = credentials.refreshToken
                        it[tokenExpiresAt] = Instant.ofEpochMilli(credentials.expirationTimeMilliseconds)
                    }
                }
            }
            true
        } catch (e: Exception) {
            // Log the error
            false
        }
    }

    suspend fun disconnect(userId: Int) {
        val token = transaction {
            GoogleCalendars.select { GoogleCalendars.userId eq userId }.singleOrNull()?.get(GoogleCalendars.accessToken)
        }

        if (token != null) {
            try {
                client.get("https://accounts.google.com/o/oauth2/revoke") {
                    url {
                        parameters.append("token", token)
                    }
                }
            } catch (e: Exception) {
                // Log the error
            }
        }

        transaction {
            GoogleCalendars.deleteWhere { GoogleCalendars.userId eq userId }
        }
    }

    private fun getCredential(userId: Int): GoogleCredential? {
        val result = transaction {
            GoogleCalendars.select { GoogleCalendars.userId eq userId }.singleOrNull()
        } ?: return null

        val credential = GoogleCredential.Builder()
            .setClientSecrets(clientId, clientSecret)
            .setJsonFactory(GsonFactory.getDefaultInstance())
            .setTransport(NetHttpTransport())
            .build()
            .setAccessToken(result[GoogleCalendars.accessToken])
            .setRefreshToken(result[GoogleCalendars.refreshToken])
            .setExpiresInSeconds(result[GoogleCalendars.tokenExpiresAt]?.epochSecond?.minus(Instant.now().epochSecond))

        if (credential.expiresInSeconds != null && credential.expiresInSeconds!! <= 60) {
            try {
                credential.refreshToken()
                transaction {
                    GoogleCalendars.update({ GoogleCalendars.userId eq userId }) {
                        it[accessToken] = credential.accessToken
                        it[tokenExpiresAt] = Instant.ofEpochMilli(credential.expirationTimeMilliseconds)
                    }
                }
            } catch (e: Exception) {
                // Log the error
                return null
            }
        }
        return credential
    }

    suspend fun syncEvents(userId: Int) {
        val credential = getCredential(userId) ?: return

        val calendar = Calendar.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Student Workflow")
            .build()

        val calendarId = transaction {
            GoogleCalendars.select { GoogleCalendars.userId eq userId }.singleOrNull()?.get(GoogleCalendars.calendarId) ?: "primary"
        }

        // Fetch tasks from the database
        val localTasks = transaction {
            Tasks.select { Tasks.assigneeId eq userId }.map {
                Task(
                    id = it[Tasks.id],
                    title = it[Tasks.title],
                    description = it[Tasks.description],
                    dueDate = it[Tasks.dueDate].toString(),
                    status = it[Tasks.status],
                    priority = it[Tasks.priority],
                    creatorId = it[Tasks.creatorId],
                    assigneeId = it[Tasks.assigneeId],
                    groupId = it[Tasks.groupId],
                    createdAt = it[Tasks.createdAt].toString()
                )
            }
        }

        // Fetch events from Google Calendar
        val events = calendar.events().list(calendarId).execute().items
        val eventMap = events.associateBy { it.extendedProperties?.private?.get("taskId") }

        for (task in localTasks) {
            val event = eventMap[task.id.toString()]
            if (event == null) {
                // Create new event
                val newEvent = taskToGoogleEvent(task)
                calendar.events().insert(calendarId, newEvent).execute()
            } else {
                // Update existing event
                val updatedEvent = taskToGoogleEvent(task)
                if (isEventDifferent(event, updatedEvent)) {
                    calendar.events().update(calendarId, event.id, updatedEvent).execute()
                }
            }
        }

        // Delete events that are no longer in the local database
        for (event in events) {
            val taskId = event.extendedProperties?.private?.get("taskId")?.toIntOrNull()
            if (taskId == null || localTasks.none { it.id == taskId }) {
                calendar.events().delete(calendarId, event.id).execute()
            }
        }
    }

    private fun taskToGoogleEvent(task: Task): Event {
        val event = Event()
            .setSummary(task.title)
            .setDescription(task.description)

        val startDateTime = DateTime(Date.from(Instant.parse(task.createdAt)))
        val endDateTime = task.dueDate?.let { DateTime(Date.from(Instant.parse(it))) }

        event.start = EventDateTime().setDateTime(startDateTime)
        event.end = EventDateTime().setDateTime(endDateTime)

        val extendedProperties = Event.ExtendedProperties().setPrivate(mapOf("taskId" to task.id.toString()))
        event.extendedProperties = extendedProperties

        return event
    }

    private fun isEventDifferent(event1: Event, event2: Event): Boolean {
        return event1.summary != event2.summary ||
                event1.description != event2.description ||
                event1.start.dateTime.value != event2.start.dateTime.value ||
                event1.end.dateTime.value != event2.end.dateTime.value
    }
}
