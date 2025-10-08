package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.tasks.await

object DebugHelper {
    private const val TAG = "DebugHelper"

    /** Creates a demo chat between two users Call this from your app to test chat functionality */
    suspend fun createDemoChat(
            user1Email: String = "evaghjiani04@gmail.com",
            user2Email: String = "eeshan.vaghjiani@strathmore.edu"
    ): Result<String> {
        return try {
            val firestore = FirebaseFirestore.getInstance()

            // First, get the user IDs from the users collection
            Log.d(TAG, "Fetching user documents...")

            val user1Query =
                    firestore.collection("users").whereEqualTo("email", user1Email).get().await()

            val user2Query =
                    firestore.collection("users").whereEqualTo("email", user2Email).get().await()

            if (user1Query.isEmpty) {
                return Result.failure(Exception("User 1 not found: $user1Email"))
            }

            if (user2Query.isEmpty) {
                return Result.failure(Exception("User 2 not found: $user2Email"))
            }

            val user1Doc = user1Query.documents[0]
            val user2Doc = user2Query.documents[0]

            val user1Id = user1Doc.id
            val user2Id = user2Doc.id

            val user1Name = user1Doc.getString("displayName") ?: "User 1"
            val user2Name = user2Doc.getString("displayName") ?: "User 2"

            Log.d(TAG, "User 1: $user1Name ($user1Id)")
            Log.d(TAG, "User 2: $user2Name ($user2Id)")

            // Create a chat document
            val chatId = firestore.collection("chats").document().id

            val chatData =
                    hashMapOf(
                            "id" to chatId,
                            "type" to "DIRECT",
                            "participants" to listOf(user1Id, user2Id),
                            "participantDetails" to
                                    mapOf(
                                            user1Id to
                                                    mapOf(
                                                            "userId" to user1Id,
                                                            "displayName" to user1Name,
                                                            "email" to user1Email,
                                                            "profileImageUrl" to
                                                                    (user1Doc.getString(
                                                                            "profileImageUrl"
                                                                    )
                                                                            ?: ""),
                                                            "online" to
                                                                    (user1Doc.getBoolean("online")
                                                                            ?: false)
                                                    ),
                                            user2Id to
                                                    mapOf(
                                                            "userId" to user2Id,
                                                            "displayName" to user2Name,
                                                            "email" to user2Email,
                                                            "profileImageUrl" to
                                                                    (user2Doc.getString(
                                                                            "profileImageUrl"
                                                                    )
                                                                            ?: ""),
                                                            "online" to
                                                                    (user2Doc.getBoolean("online")
                                                                            ?: false)
                                                    )
                                    ),
                            "lastMessage" to "Demo chat created",
                            "lastMessageTime" to Date(),
                            "lastMessageSenderId" to user1Id,
                            "unreadCount" to mapOf(user1Id to 0, user2Id to 0),
                            "createdAt" to Date()
                    )

            firestore.collection("chats").document(chatId).set(chatData).await()

            Log.d(TAG, "Demo chat created successfully: $chatId")

            // Create a demo message
            val messageId =
                    firestore
                            .collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .document()
                            .id

            val messageData =
                    hashMapOf(
                            "id" to messageId,
                            "chatId" to chatId,
                            "senderId" to user1Id,
                            "senderName" to user1Name,
                            "senderImageUrl" to (user1Doc.getString("profileImageUrl") ?: ""),
                            "text" to "Hello! This is a demo message.",
                            "timestamp" to Date(),
                            "readBy" to listOf(user1Id),
                            "status" to "SENT"
                    )

            firestore
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document(messageId)
                    .set(messageData)
                    .await()

            Log.d(TAG, "Demo message created successfully")

            Result.success(chatId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating demo chat", e)
            Result.failure(e)
        }
    }

    /** Lists all users in the database for debugging */
    suspend fun listAllUsers(): Result<List<Map<String, Any?>>> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("users").get().await()

            val users =
                    snapshot.documents.map { doc ->
                        mapOf(
                                "id" to doc.id,
                                "email" to doc.getString("email"),
                                "displayName" to doc.getString("displayName"),
                                "online" to doc.getBoolean("online")
                        )
                    }

            users.forEach { user ->
                Log.d(TAG, "User: ${user["displayName"]} (${user["email"]}) - ID: ${user["id"]}")
            }

            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing users", e)
            Result.failure(e)
        }
    }
}
