package com.example.loginandregistration.repository

import com.example.loginandregistration.models.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun createOrUpdateUser(): Boolean =
            withContext(Dispatchers.IO) {
                val user = auth.currentUser ?: return@withContext false
                return@withContext try {
                    val displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User"

                    val firebaseUser =
                            FirebaseUser(
                                    uid = user.uid,
                                    email = user.email ?: "",
                                    displayName = displayName,
                                    photoUrl = user.photoUrl?.toString() ?: "",
                                    initials = generateInitials(displayName, user.email ?: ""),
                                    lastActive = Timestamp.now(),
                                    isOnline = true
                            )

                    usersCollection.document(user.uid).set(firebaseUser).await()
                    true
                } catch (e: Exception) {
                    false
                }
            }

    /**
     * Generate initials from display name or email
     * @param displayName User's display name
     * @param email User's email address
     * @return Initials string (up to 2 characters)
     */
    private fun generateInitials(displayName: String, email: String): String {
        // Generate from display name
        if (displayName.isNotEmpty()) {
            return displayName
                    .trim()
                    .split(" ")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString("")
        }

        // Fallback to email first character
        return email.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }

    suspend fun searchUsers(query: String): List<FirebaseUser> =
            withContext(Dispatchers.IO) {
                if (query.length < 2) return@withContext emptyList()

                return@withContext try {
                    val emailResults =
                            usersCollection
                                    .whereGreaterThanOrEqualTo("email", query.lowercase())
                                    .whereLessThanOrEqualTo("email", query.lowercase() + "\uf8ff")
                                    .limit(10)
                                    .get()
                                    .await()
                                    .toObjects(FirebaseUser::class.java)

                    val nameResults =
                            usersCollection
                                    .whereGreaterThanOrEqualTo("displayName", query)
                                    .whereLessThanOrEqualTo("displayName", query + "\uf8ff")
                                    .limit(10)
                                    .get()
                                    .await()
                                    .toObjects(FirebaseUser::class.java)

                    (emailResults + nameResults).distinctBy { it.uid }
                } catch (e: Exception) {
                    emptyList()
                }
            }

    suspend fun getUserById(userId: String): FirebaseUser? =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    usersCollection
                            .document(userId)
                            .get()
                            .await()
                            .toObject(FirebaseUser::class.java)
                } catch (e: Exception) {
                    null
                }
            }

    suspend fun getUsersByIds(userIds: List<String>): List<FirebaseUser> =
            withContext(Dispatchers.IO) {
                if (userIds.isEmpty()) return@withContext emptyList()

                return@withContext try {
                    val users = mutableListOf<FirebaseUser>()
                    // Firestore 'in' queries are limited to 10 items
                    userIds.chunked(10).forEach { chunk ->
                        val result =
                                usersCollection
                                        .whereIn("uid", chunk)
                                        .get()
                                        .await()
                                        .toObjects(FirebaseUser::class.java)
                        users.addAll(result)
                    }
                    users
                } catch (e: Exception) {
                    emptyList()
                }
            }

    suspend fun updateUserOnlineStatus(isOnline: Boolean): Boolean =
            withContext(Dispatchers.IO) {
                val userId = auth.currentUser?.uid ?: return@withContext false
                return@withContext try {
                    usersCollection
                            .document(userId)
                            .update(mapOf("isOnline" to isOnline, "lastActive" to Timestamp.now()))
                            .await()
                    true
                } catch (e: Exception) {
                    false
                }
            }

    /**
     * Get current user's profile with real-time updates
     * @return Flow of FirebaseUser or null if not found
     */
    fun getCurrentUserProfileFlow(): kotlinx.coroutines.flow.Flow<FirebaseUser?> =
            callbackFlow {
                        val userId = auth.currentUser?.uid
                        if (userId == null) {
                            trySend(null)
                            close()
                            return@callbackFlow
                        }

                        val listener =
                                usersCollection.document(userId).addSnapshotListener {
                                        snapshot,
                                        error ->
                                    if (error != null) {
                                        trySend(null)
                                        return@addSnapshotListener
                                    }

                                    val user = snapshot?.toObject(FirebaseUser::class.java)
                                    trySend(user)
                                }

                        awaitClose { listener.remove() }
                    }
                    .flowOn(Dispatchers.IO)
}
