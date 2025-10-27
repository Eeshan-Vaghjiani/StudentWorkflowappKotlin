package com.example.loginandregistration.repository

import com.example.loginandregistration.models.FirebaseSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SessionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sessionsCollection = db.collection("study_sessions")

    suspend fun getUserSessions(): List<FirebaseSession> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            sessionsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("startTime", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(FirebaseSession::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getSessionStatsFlow(): Flow<SessionStats> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(SessionStats())
            close()
            return@callbackFlow
        }

        val listener =
                sessionsCollection.whereEqualTo("userId", userId).addSnapshotListener {
                        snapshot,
                        error ->
                    if (error != null) {
                        trySend(SessionStats())
                        return@addSnapshotListener
                    }

                    val sessions = snapshot?.toObjects(FirebaseSession::class.java) ?: emptyList()
                    val totalSessions = sessions.size
                    val totalDuration = sessions.sumOf { it.durationMinutes }

                    trySend(
                            SessionStats(
                                    totalSessions = totalSessions,
                                    totalMinutes = totalDuration
                            )
                    )
                }

        awaitClose { listener.remove() }
    }
}

data class SessionStats(
        val totalSessions: Int = 0,
        val totalMinutes: Int = 0,
        val todaySessions: Int = 0
)
