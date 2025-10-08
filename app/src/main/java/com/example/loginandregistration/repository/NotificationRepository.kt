package com.example.loginandregistration.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    companion object {
        private const val TAG = "NotificationRepository"
        private const val USERS_COLLECTION = "users"
    }

    /** Get the current FCM token and save it to the user's Firestore document */
    suspend fun saveFcmToken(): Result<String> {
        return try {
            val userId =
                    auth.currentUser?.uid
                            ?: return Result.failure(Exception("User not authenticated"))

            // Get the FCM token
            val token = messaging.token.await()

            // Save to Firestore
            firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .update("fcmToken", token)
                    .await()

            Log.d(TAG, "FCM token saved successfully: $token")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save FCM token", e)
            Result.failure(e)
        }
    }

    /** Get FCM tokens for a list of user IDs */
    suspend fun getUserTokens(userIds: List<String>): Result<List<String>> {
        return try {
            val tokens = mutableListOf<String>()

            for (userId in userIds) {
                val document = firestore.collection(USERS_COLLECTION).document(userId).get().await()

                val token = document.getString("fcmToken")
                if (!token.isNullOrEmpty()) {
                    tokens.add(token)
                }
            }

            Result.success(tokens)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user tokens", e)
            Result.failure(e)
        }
    }

    /** Remove FCM token from user document (e.g., on logout) */
    suspend fun removeFcmToken(): Result<Unit> {
        return try {
            val userId =
                    auth.currentUser?.uid
                            ?: return Result.failure(Exception("User not authenticated"))

            firestore.collection(USERS_COLLECTION).document(userId).update("fcmToken", null).await()

            Log.d(TAG, "FCM token removed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove FCM token", e)
            Result.failure(e)
        }
    }
}
