package com.example.loginandregistration.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.loginandregistration.models.*
import com.example.loginandregistration.utils.OfflineMessageQueue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
        private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
        private val context: Context? = null
) {
    private val storageRepository: StorageRepository? = context?.let { StorageRepository(storage, auth, it) }
    companion object {
        private const val TAG = "ChatRepository"
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
        private const val USERS_COLLECTION = "users"
        private const val TYPING_STATUS_COLLECTION = "typing_status"
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    private val offlineQueue: OfflineMessageQueue? = context?.let { OfflineMessageQueue(it) }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    fun getUserChats(): Flow<List<Chat>> = callbackFlow {
        if (getCurrentUserId().isEmpty()) {
            Log.w(TAG, "getUserChats: User not authenticated")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "getUserChats: Setting up listener for user ${getCurrentUserId()}")

        val listener =
                firestore
                        .collection(CHATS_COLLECTION)
                        .whereArrayContains("participants", getCurrentUserId())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(TAG, "Error listening to chats: ${error.message}", error)
                                // If it's an index error, log specific message
                                if (error.message?.contains("index", ignoreCase = true) == true) {
                                    Log.e(TAG, "FIRESTORE INDEX REQUIRED: Create a composite index for 'chats' collection with fields: participants (array-contains) and lastMessageTime (descending)")
                                }
                                // Still try to send empty list rather than failing completely
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            if (snapshot == null) {
                                Log.w(TAG, "getUserChats: Snapshot is null")
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            Log.d(TAG, "getUserChats: Received ${snapshot.documents.size} chat documents")

                            val chats =
                                    snapshot.documents.mapNotNull { doc ->
                                        try {
                                            val chat = doc.toObject(Chat::class.java)
                                            if (chat != null) {
                                                Log.d(TAG, "Parsed chat: ${chat.chatId}, type: ${chat.type}, participants: ${chat.participants.size}")
                                            }
                                            chat
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error parsing chat document ${doc.id}", e)
                                            null
                                        }
                                    }
                                    // Sort manually by lastMessageTime since we can't use orderBy without index
                                    .sortedByDescending { it.lastMessageTime }

                            Log.d(TAG, "getUserChats: Sending ${chats.size} chats to UI")
                            trySend(chats)
                        }

        awaitClose {
            Log.d(TAG, "getUserChats: Closing listener")
            listener.remove()
        }
    }

    /**
     * Creates a user document in Firestore from UserInfo.
     * This is used when a user is found in search but doesn't have a Firestore document yet.
     */
    private suspend fun createUserDocument(userInfo: UserInfo): Result<Unit> {
        return try {
            Log.d(TAG, "createUserDocument: Creating document for ${userInfo.displayName}")
            
            val userDoc = mapOf(
                "uid" to userInfo.userId,
                "email" to userInfo.email,
                "displayName" to userInfo.displayName,
                "photoUrl" to userInfo.profileImageUrl,
                "isOnline" to userInfo.online,
                "lastActive" to (userInfo.lastSeen ?: Date())
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(userInfo.userId)
                .set(userDoc)
                .await()
            
            Log.d(TAG, "createUserDocument: Successfully created document for ${userInfo.displayName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "createUserDocument: Error creating user document", e)
            Result.failure(e)
        }
    }

    suspend fun getOrCreateDirectChat(otherUserId: String, otherUserInfo: UserInfo? = null): Result<Chat> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            val existingChats =
                    firestore
                            .collection(CHATS_COLLECTION)
                            .whereEqualTo("type", ChatType.DIRECT.name)
                            .whereArrayContains("participants", getCurrentUserId())
                            .get()
                            .await()

            val existingChat =
                    existingChats.documents.firstOrNull { doc ->
                        val participants = doc.get("participants") as? List<*>
                        participants?.contains(otherUserId) == true
                    }

            if (existingChat != null) {
                val chat = existingChat.toObject(Chat::class.java)
                return if (chat != null) {
                    Result.success(chat)
                } else {
                    Result.failure(Exception("Failed to parse existing chat"))
                }
            }

            Log.d(TAG, "Fetching user info for otherUserId: $otherUserId")
            
            // If UserInfo was provided (from search results), try to create the document first
            if (otherUserInfo != null) {
                Log.d(TAG, "UserInfo provided, attempting to create user document if needed")
                createUserDocument(otherUserInfo)
            }
            
            val otherUserResult = getUserInfo(otherUserId)
            if (otherUserResult.isFailure) {
                Log.e(
                        TAG,
                        "Failed to get other user info: ${otherUserResult.exceptionOrNull()?.message}"
                )
                
                // If we have UserInfo from search, use it directly
                if (otherUserInfo != null) {
                    Log.d(TAG, "Using UserInfo from search results: ${otherUserInfo.displayName}")
                } else {
                    return Result.failure(
                            Exception("Cannot create chat: The other user needs to log into the app at least once to create their profile. Please ask them to open the app and sign in.")
                    )
                }
            }
            
            val otherUser = if (otherUserResult.isSuccess) {
                otherUserResult.getOrThrow()
            } else {
                otherUserInfo!! // We know it's not null because we checked above
            }
            
            Log.d(TAG, "Other user found: ${otherUser.displayName}")

            Log.d(TAG, "Fetching user info for currentUserId: ${getCurrentUserId()}")
            val currentUserResult = getUserInfo(getCurrentUserId())
            if (currentUserResult.isFailure) {
                Log.e(
                        TAG,
                        "Failed to get current user info: ${currentUserResult.exceptionOrNull()?.message}"
                )
                return Result.failure(
                        Exception(
                                "Current user not found: ${currentUserResult.exceptionOrNull()?.message}"
                        )
                )
            }
            val currentUser = currentUserResult.getOrThrow()
            Log.d(TAG, "Current user found: ${currentUser.displayName}")

            val chatId = firestore.collection(CHATS_COLLECTION).document().id
            val chat =
                    Chat(
                            chatId = chatId,
                            type = ChatType.DIRECT,
                            participants = listOf(getCurrentUserId(), otherUserId),
                            participantDetails =
                                    mapOf(
                                            getCurrentUserId() to currentUser,
                                            otherUserId to otherUser
                                    ),
                            lastMessage = "",
                            lastMessageTime = Date(),
                            lastMessageSenderId = "",
                            unreadCount = mapOf(getCurrentUserId() to 0, otherUserId to 0),
                            createdAt = Date()
                    )

            firestore.collection(CHATS_COLLECTION).document(chatId).set(chat).await()

            Result.success(chat)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating direct chat", e)
            Result.failure(e)
        }
    }

    suspend fun getOrCreateGroupChat(groupId: String): Result<Chat> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "getOrCreateGroupChat: Checking for existing chat for group $groupId")

            val existingChats =
                    firestore
                            .collection(CHATS_COLLECTION)
                            .whereEqualTo("type", ChatType.GROUP.name)
                            .whereEqualTo("groupId", groupId)
                            .get()
                            .await()

            if (!existingChats.isEmpty) {
                val chat = existingChats.documents.first().toObject(Chat::class.java)
                Log.d(TAG, "getOrCreateGroupChat: Found existing chat ${chat?.chatId}")
                return if (chat != null) {
                    Result.success(chat)
                } else {
                    Result.failure(Exception("Failed to parse existing chat"))
                }
            }

            Log.d(TAG, "getOrCreateGroupChat: Creating new chat for group $groupId")
            val groupDoc = firestore.collection("groups").document(groupId).get().await()

            if (!groupDoc.exists()) {
                Log.e(TAG, "getOrCreateGroupChat: Group document does not exist for groupId: $groupId")
                return Result.failure(Exception("Group not found"))
            }

            val groupName = groupDoc.getString("name") ?: "Group Chat"
            val members = groupDoc.get("members") as? List<String> ?: emptyList()

            Log.d(TAG, "getOrCreateGroupChat: Group '$groupName' has ${members.size} members")

            val participantDetails = mutableMapOf<String, UserInfo>()
            for (memberId in members) {
                getUserInfo(memberId).getOrNull()?.let { userInfo ->
                    participantDetails[memberId] = userInfo
                }
            }

            val chatId = firestore.collection(CHATS_COLLECTION).document().id
            val chat =
                    Chat(
                            chatId = chatId,
                            type = ChatType.GROUP,
                            participants = members,
                            participantDetails = participantDetails,
                            lastMessage = "",
                            lastMessageTime = Date(),
                            lastMessageSenderId = "",
                            unreadCount = members.associateWith { 0 },
                            groupId = groupId,
                            groupName = groupName,
                            createdAt = Date()
                    )

            firestore.collection(CHATS_COLLECTION).document(chatId).set(chat).await()
            Log.d(TAG, "getOrCreateGroupChat: Successfully created chat $chatId for group $groupId")

            Result.success(chat)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating group chat", e)
            Result.failure(e)
        }
    }

    /**
     * Auto-creates chats for all groups the user is a member of.
     * This should be called when the user opens the Chat tab or logs in.
     */
    suspend fun ensureGroupChatsExist(): Result<Int> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "ensureGroupChatsExist: Fetching user's groups")

            // Get all groups where user is a member
            val groupsSnapshot = firestore
                .collection("groups")
                .whereArrayContains("members", getCurrentUserId())
                .get()
                .await()

            Log.d(TAG, "ensureGroupChatsExist: Found ${groupsSnapshot.documents.size} groups")

            var createdCount = 0
            for (groupDoc in groupsSnapshot.documents) {
                val groupId = groupDoc.id
                val groupName = groupDoc.getString("name") ?: "Group"
                
                Log.d(TAG, "ensureGroupChatsExist: Checking group '$groupName' ($groupId)")

                // Check if chat already exists for this group
                val existingChat = firestore
                    .collection(CHATS_COLLECTION)
                    .whereEqualTo("type", ChatType.GROUP.name)
                    .whereEqualTo("groupId", groupId)
                    .limit(1)
                    .get()
                    .await()

                if (existingChat.isEmpty) {
                    // Create chat for this group
                    Log.d(TAG, "ensureGroupChatsExist: Creating chat for group '$groupName'")
                    val result = getOrCreateGroupChat(groupId)
                    if (result.isSuccess) {
                        createdCount++
                        Log.d(TAG, "ensureGroupChatsExist: Successfully created chat for group '$groupName'")
                    } else {
                        Log.e(TAG, "ensureGroupChatsExist: Failed to create chat for group '$groupName': ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.d(TAG, "ensureGroupChatsExist: Chat already exists for group '$groupName'")
                }
            }

            Log.d(TAG, "ensureGroupChatsExist: Created $createdCount new group chats")
            Result.success(createdCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring group chats exist", e)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: String, text: String, retryCount: Int = 0): Result<Message> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (text.isBlank()) {
                return Result.failure(Exception("Message cannot be empty"))
            }

            val currentUser =
                    getUserInfo(getCurrentUserId()).getOrNull()
                            ?: return Result.failure(Exception("User not found"))

            val messageId =
                    firestore
                            .collection(CHATS_COLLECTION)
                            .document(chatId)
                            .collection(MESSAGES_COLLECTION)
                            .document()
                            .id

            val message =
                    Message(
                            id = messageId,
                            chatId = chatId,
                            senderId = getCurrentUserId(),
                            senderName = currentUser.displayName,
                            senderImageUrl = currentUser.profileImageUrl,
                            text = text,
                            timestamp = Date(),
                            readBy = listOf(getCurrentUserId()),
                            status = MessageStatus.SENDING
                    )

            // Queue message for offline support
            offlineQueue?.queueMessage(message)

            try {
                // Attempt to send message
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .collection(MESSAGES_COLLECTION)
                        .document(messageId)
                        .set(message.copy(status = MessageStatus.SENT))
                        .await()

                updateChatLastMessage(chatId, text, getCurrentUserId())

                // Remove from queue on success
                offlineQueue?.removeMessage(messageId)

                // Trigger notifications for recipients
                triggerNotifications(chatId, message)

                Result.success(message.copy(status = MessageStatus.SENT))
            } catch (sendError: Exception) {
                Log.e(TAG, "Error sending message (attempt ${retryCount + 1})", sendError)

                // If we've exceeded retry attempts, mark as failed
                if (retryCount >= MAX_RETRY_ATTEMPTS) {
                    offlineQueue?.markMessageAsFailed(messageId)
                    return Result.failure(
                            Exception("Failed to send message after $MAX_RETRY_ATTEMPTS attempts")
                    )
                }

                // Keep message in queue with SENDING status
                offlineQueue?.updateMessageStatus(messageId, MessageStatus.SENDING)
                throw sendError
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            Result.failure(e)
        }
    }

    /**
     * Sends an image message to a chat.
     * Compresses the image before upload and shows progress.
     * 
     * @param chatId ID of the chat
     * @param imageUri URI of the image to send
     * @param onProgress Callback for upload progress (0-100)
     * @return Result with the sent message on success
     */
    suspend fun sendImageMessage(
        chatId: String,
        imageUri: Uri,
        onProgress: (Int) -> Unit = {}
    ): Result<Message> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (storageRepository == null) {
                return Result.failure(Exception("Storage repository not initialized"))
            }

            Log.d(TAG, "sendImageMessage: Starting image upload for chat $chatId")

            val currentUser = getUserInfo(getCurrentUserId()).getOrNull()
                ?: return Result.failure(Exception("User not found"))

            // Create message ID first
            val messageId = firestore
                .collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_COLLECTION)
                .document()
                .id

            // Create temporary message with SENDING status
            val tempMessage = Message(
                id = messageId,
                chatId = chatId,
                senderId = getCurrentUserId(),
                senderName = currentUser.displayName,
                senderImageUrl = currentUser.profileImageUrl,
                text = "", // Empty text for image messages
                imageUrl = null, // Will be set after upload
                timestamp = Date(),
                readBy = listOf(getCurrentUserId()),
                status = MessageStatus.SENDING
            )

            // Queue message for offline support
            offlineQueue?.queueMessage(tempMessage)

            try {
                // Upload image to Storage
                val storagePath = storageRepository.getChatImagePath(chatId)
                val uploadResult = storageRepository.uploadImage(
                    uri = imageUri,
                    path = storagePath,
                    onProgress = onProgress
                )

                if (uploadResult.isFailure) {
                    Log.e(TAG, "sendImageMessage: Image upload failed", uploadResult.exceptionOrNull())
                    offlineQueue?.markMessageAsFailed(messageId)
                    return Result.failure(
                        uploadResult.exceptionOrNull() ?: Exception("Image upload failed")
                    )
                }

                val imageUrl = uploadResult.getOrThrow()
                Log.d(TAG, "sendImageMessage: Image uploaded successfully: $imageUrl")

                // Create final message with image URL
                val message = tempMessage.copy(
                    imageUrl = imageUrl,
                    status = MessageStatus.SENT
                )

                // Save message to Firestore
                firestore
                    .collection(CHATS_COLLECTION)
                    .document(chatId)
                    .collection(MESSAGES_COLLECTION)
                    .document(messageId)
                    .set(message)
                    .await()

                // Update chat's last message
                updateChatLastMessage(chatId, "📷 Photo", getCurrentUserId())

                // Remove from queue on success
                offlineQueue?.removeMessage(messageId)

                // Trigger notifications for recipients
                triggerNotifications(chatId, message.copy(text = "📷 Photo"))

                Log.d(TAG, "sendImageMessage: Image message sent successfully")
                Result.success(message)
            } catch (sendError: Exception) {
                Log.e(TAG, "sendImageMessage: Error sending image message", sendError)
                offlineQueue?.markMessageAsFailed(messageId)
                Result.failure(sendError)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendImageMessage: Unexpected error", e)
            Result.failure(e)
        }
    }

    fun getChatMessages(chatId: String, limit: Int = 50): Flow<List<Message>> = callbackFlow {
        val listener =
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .collection(MESSAGES_COLLECTION)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(TAG, "Error listening to messages", error)
                                return@addSnapshotListener
                            }

                            val messages =
                                    snapshot?.documents
                                            ?.mapNotNull { doc ->
                                                try {
                                                    doc.toObject(Message::class.java)
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error parsing message", e)
                                                    null
                                                }
                                            }
                                            ?.reversed()
                                            ?: emptyList()

                            trySend(messages)
                        }

        awaitClose { listener.remove() }
    }

    suspend fun loadMoreMessages(
            chatId: String,
            oldestMessage: Message,
            limit: Int = 50
    ): Result<List<Message>> {
        return try {
            val snapshot =
                    firestore
                            .collection(CHATS_COLLECTION)
                            .document(chatId)
                            .collection(MESSAGES_COLLECTION)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .startAfter(oldestMessage.timestamp)
                            .limit(limit.toLong())
                            .get()
                            .await()

            val messages =
                    snapshot.documents
                            .mapNotNull { doc ->
                                try {
                                    doc.toObject(Message::class.java)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing message", e)
                                    null
                                }
                            }
                            .reversed()

            Result.success(messages)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading more messages", e)
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(chatId: String, messageIds: List<String>) {
        try {
            if (getCurrentUserId().isEmpty()) return

            val batch = firestore.batch()

            for (messageId in messageIds) {
                val messageRef =
                        firestore
                                .collection(CHATS_COLLECTION)
                                .document(chatId)
                                .collection(MESSAGES_COLLECTION)
                                .document(messageId)

                // Add current user to readBy array
                batch.update(
                        messageRef,
                        mapOf(
                                "readBy" to
                                        com.google.firebase.firestore.FieldValue.arrayUnion(
                                                getCurrentUserId()
                                        ),
                                "status" to MessageStatus.READ.name
                        )
                )
            }

            batch.commit().await()

            firestore
                    .collection(CHATS_COLLECTION)
                    .document(chatId)
                    .update("unreadCount.${getCurrentUserId()}", 0)
                    .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
        }
    }

    suspend fun updateTypingStatus(chatId: String, isTyping: Boolean) {
        try {
            if (getCurrentUserId().isEmpty()) return

            firestore
                    .collection(CHATS_COLLECTION)
                    .document(chatId)
                    .collection(TYPING_STATUS_COLLECTION)
                    .document(getCurrentUserId())
                    .set(
                            TypingStatus(
                                    userId = getCurrentUserId(),
                                    isTyping = isTyping,
                                    timestamp = System.currentTimeMillis()
                            )
                    )
                    .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating typing status", e)
        }
    }

    fun getTypingUsers(chatId: String): Flow<List<String>> = callbackFlow {
        val listener =
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .collection(TYPING_STATUS_COLLECTION)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(TAG, "Error listening to typing status", error)
                                return@addSnapshotListener
                            }

                            val typingUsers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        try {
                                            val status = doc.toObject(TypingStatus::class.java)
                                            if (status?.isTyping == true &&
                                                            status.userId != getCurrentUserId()
                                            ) {
                                                status.userId
                                            } else {
                                                null
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error parsing typing status", e)
                                            null
                                        }
                                    }
                                            ?: emptyList()

                            trySend(typingUsers)
                        }

        awaitClose { listener.remove() }
    }

    suspend fun searchUsers(query: String): Result<List<UserInfo>> {
        return try {
            if (query.isBlank()) {
                Log.d(TAG, "searchUsers: Query is blank")
                return Result.success(emptyList())
            }

            Log.d(TAG, "searchUsers: Searching for '$query'")
            val queryLower = query.lowercase()

            val nameResults =
                    firestore
                            .collection(USERS_COLLECTION)
                            .orderBy("displayName")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .limit(20)
                            .get()
                            .await()

            val emailResults =
                    firestore
                            .collection(USERS_COLLECTION)
                            .orderBy("email")
                            .startAt(queryLower)
                            .endAt(queryLower + "\uf8ff")
                            .limit(20)
                            .get()
                            .await()

            Log.d(TAG, "searchUsers: Found ${nameResults.size()} by name, ${emailResults.size()} by email")

            val users = mutableSetOf<UserInfo>()

            // Convert Firestore documents to UserInfo
            nameResults.documents.forEach { doc ->
                try {
                    val userId = doc.id
                    val displayName = doc.getString("displayName") ?: doc.getString("display_name") ?: "Unknown"
                    val email = doc.getString("email") ?: ""
                    val profileImageUrl = doc.getString("photoUrl") ?: doc.getString("profileImageUrl") ?: ""
                    val online = doc.getBoolean("isOnline") ?: doc.getBoolean("online") ?: false
                    val lastSeen = doc.getDate("lastActive") ?: doc.getDate("lastSeen")
                    
                    val userInfo = UserInfo(
                        userId = userId,
                        displayName = displayName,
                        email = email,
                        profileImageUrl = profileImageUrl,
                        online = online,
                        lastSeen = lastSeen
                    )
                    users.add(userInfo)
                    Log.d(TAG, "searchUsers: Added user ${userInfo.displayName} (${userInfo.userId})")
                } catch (e: Exception) {
                    Log.e(TAG, "searchUsers: Error parsing user document ${doc.id}", e)
                }
            }

            emailResults.documents.forEach { doc ->
                try {
                    val userId = doc.id
                    // Skip if already added
                    if (users.any { it.userId == userId }) return@forEach
                    
                    val displayName = doc.getString("displayName") ?: doc.getString("display_name") ?: "Unknown"
                    val email = doc.getString("email") ?: ""
                    val profileImageUrl = doc.getString("photoUrl") ?: doc.getString("profileImageUrl") ?: ""
                    val online = doc.getBoolean("isOnline") ?: doc.getBoolean("online") ?: false
                    val lastSeen = doc.getDate("lastActive") ?: doc.getDate("lastSeen")
                    
                    val userInfo = UserInfo(
                        userId = userId,
                        displayName = displayName,
                        email = email,
                        profileImageUrl = profileImageUrl,
                        online = online,
                        lastSeen = lastSeen
                    )
                    users.add(userInfo)
                    Log.d(TAG, "searchUsers: Added user ${userInfo.displayName} (${userInfo.userId})")
                } catch (e: Exception) {
                    Log.e(TAG, "searchUsers: Error parsing user document ${doc.id}", e)
                }
            }

            val filteredUsers = users.filter { it.userId != getCurrentUserId() }
            Log.d(TAG, "searchUsers: Returning ${filteredUsers.size} users (filtered out current user)")

            Result.success(filteredUsers.toList())
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users", e)
            Result.failure(e)
        }
    }

    private suspend fun getUserInfo(userId: String): Result<UserInfo> {
        return try {
            Log.d(TAG, "getUserInfo: Fetching user document for userId: $userId")
            val doc = firestore.collection(USERS_COLLECTION).document(userId).get().await()

            if (!doc.exists()) {
                Log.e(TAG, "getUserInfo: User document does not exist for userId: $userId")
                Log.e(TAG, "getUserInfo: Make sure the user has logged in at least once to create their profile")
                return Result.failure(Exception("User not found: Invalid document reference. Document ID: $userId"))
            }

            Log.d(TAG, "getUserInfo: User document found. Fields: ${doc.data?.keys}")

            // Handle both field name variations (displayName vs display_name, photoUrl vs profileImageUrl, etc.)
            val displayName = doc.getString("displayName") 
                ?: doc.getString("display_name")
                ?: doc.getString("name")
                ?: "Unknown User"
            
            val email = doc.getString("email") ?: ""
            
            val profileImageUrl = doc.getString("profileImageUrl")
                ?: doc.getString("photoUrl")
                ?: doc.getString("photo_url")
                ?: doc.getString("profile_image_url")
                ?: ""
            
            val online = doc.getBoolean("online") 
                ?: doc.getBoolean("isOnline")
                ?: false
            
            val lastSeen = doc.getDate("lastSeen") 
                ?: doc.getDate("lastActive")
                ?: doc.getDate("last_active")
                ?: doc.getTimestamp("lastActive")?.toDate()

            val userInfo =
                    UserInfo(
                            userId = userId,
                            displayName = displayName,
                            email = email,
                            profileImageUrl = profileImageUrl,
                            online = online,
                            lastSeen = lastSeen
                    )

            Log.d(TAG, "getUserInfo: Successfully created UserInfo for ${userInfo.displayName}")
            Result.success(userInfo)
        } catch (e: Exception) {
            Log.e(TAG, "getUserInfo: Error getting user info for $userId", e)
            Result.failure(e)
        }
    }

    private suspend fun updateChatLastMessage(chatId: String, message: String, senderId: String) {
        try {
            val chatDoc = firestore.collection(CHATS_COLLECTION).document(chatId).get().await()

            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            val currentUnreadCount = chatDoc.get("unreadCount") as? Map<String, Long> ?: emptyMap()

            val newUnreadCount =
                    participants.associateWith { participantId ->
                        if (participantId == senderId) {
                            0
                        } else {
                            (currentUnreadCount[participantId] ?: 0) + 1
                        }
                    }

            firestore
                    .collection(CHATS_COLLECTION)
                    .document(chatId)
                    .update(
                            mapOf(
                                    "lastMessage" to message,
                                    "lastMessageTime" to Date(),
                                    "lastMessageSenderId" to senderId,
                                    "unreadCount" to newUnreadCount
                            )
                    )
                    .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last message", e)
        }
    }

    /** Get queued messages for a specific chat */
    fun getQueuedMessagesForChat(chatId: String): List<Message> {
        return offlineQueue?.getQueuedMessagesForChat(chatId) ?: emptyList()
    }

    /** Get all queued messages */
    fun getAllQueuedMessages(): List<Message> {
        return offlineQueue?.getQueuedMessages() ?: emptyList()
    }

    /** Retry sending a failed message */
    suspend fun retryMessage(message: Message): Result<Message> {
        return try {
            // Reset message status to SENDING
            offlineQueue?.updateMessageStatus(message.id, MessageStatus.SENDING)

            // Attempt to send
            val result = sendMessage(message.chatId, message.text, retryCount = 0)

            if (result.isSuccess) {
                offlineQueue?.removeMessage(message.id)
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error retrying message", e)
            offlineQueue?.markMessageAsFailed(message.id)
            Result.failure(e)
        }
    }

    /** Process all queued messages (called when connection is restored) */
    suspend fun processQueuedMessages(): Result<Int> {
        return try {
            val queuedMessages = offlineQueue?.getQueuedMessages() ?: emptyList()
            var successCount = 0

            for (message in queuedMessages) {
                if (message.status == MessageStatus.SENDING) {
                    val result = sendMessage(message.chatId, message.text, retryCount = 0)
                    if (result.isSuccess) {
                        successCount++
                    }
                }
            }

            Log.d(TAG, "Processed $successCount queued messages")
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing queued messages", e)
            Result.failure(e)
        }
    }

    /** Clear the offline message queue */
    fun clearOfflineQueue() {
        offlineQueue?.clearQueue()
    }

    /**
     * Trigger notifications for chat message recipients. This method creates a notification
     * document in Firestore that will be picked up by Cloud Functions to send FCM notifications.
     * Only sends notifications to recipients who are not currently viewing the chat.
     */
    private suspend fun triggerNotifications(chatId: String, message: Message) {
        try {
            // Get chat details to find recipients
            val chatDoc = firestore.collection(CHATS_COLLECTION).document(chatId).get().await()

            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            val chatType = chatDoc.getString("type") ?: ChatType.DIRECT.name
            val chatName =
                    chatDoc.getString("groupName")
                            ?: chatDoc.get("participantDetails")?.let { details ->
                                @Suppress("UNCHECKED_CAST")
                                val detailsMap = details as? Map<String, Map<String, Any>>
                                detailsMap
                                        ?.values
                                        ?.firstOrNull { userMap ->
                                            userMap["userId"] != getCurrentUserId()
                                        }
                                        ?.get("displayName") as?
                                        String
                            }
                                    ?: "Chat"

            // Filter out the sender from recipients
            val recipients = participants.filter { it != getCurrentUserId() }

            if (recipients.isEmpty()) {
                Log.d(TAG, "No recipients to notify for chat $chatId")
                return
            }

            // Create notification data for Cloud Functions to process
            // In a production app, you would have a Cloud Function that listens to this
            // collection and sends FCM notifications
            val notificationData =
                    mapOf(
                            "type" to "chat",
                            "chatId" to chatId,
                            "chatName" to chatName,
                            "chatType" to chatType,
                            "senderName" to message.senderName,
                            "senderId" to message.senderId,
                            "senderImageUrl" to (message.senderImageUrl ?: ""),
                            "message" to message.text,
                            "timestamp" to (message.timestamp?.time ?: System.currentTimeMillis()),
                            "recipients" to recipients,
                            "createdAt" to
                                    com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

            // Add to notifications collection for Cloud Functions to process
            firestore.collection("notifications").add(notificationData).await()

            Log.d(TAG, "Notification triggered for ${recipients.size} recipients")
        } catch (e: Exception) {
            // Don't fail the message send if notification fails
            Log.e(TAG, "Error triggering notifications", e)
        }
    }
}
