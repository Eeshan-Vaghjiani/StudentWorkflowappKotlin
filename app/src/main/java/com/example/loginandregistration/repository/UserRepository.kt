package com.example.loginandregistration.repository

import com.example.loginandregistration.models.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun createOrUpdateUser(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val firebaseUser =
                    FirebaseUser(
                            uid = user.uid,
                            email = user.email ?: "",
                            displayName = user.displayName
                                            ?: user.email?.substringBefore("@") ?: "User",
                            photoUrl = user.photoUrl?.toString() ?: "",
                            lastActive = Timestamp.now(),
                            isOnline = true
                    )

            usersCollection.document(user.uid).set(firebaseUser).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchUsers(query: String): List<FirebaseUser> {
        if (query.length < 2) return emptyList()

        return try {
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

    suspend fun getUserById(userId: String): FirebaseUser? {
        return try {
            usersCollection.document(userId).get().await().toObject(FirebaseUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): List<FirebaseUser> {
        if (userIds.isEmpty()) return emptyList()

        return try {
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

    suspend fun updateUserOnlineStatus(isOnline: Boolean): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            usersCollection
                    .document(userId)
                    .update(mapOf("isOnline" to isOnline, "lastActive" to Timestamp.now()))
                    .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
