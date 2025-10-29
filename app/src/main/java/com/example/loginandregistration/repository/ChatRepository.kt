package com.example.loginandregistration.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.loginandregistration.models.*
import com.example.loginandregistration.utils.OfflineMessageQueue
import com.example.loginandregistration.utils.safeFirestoreCall
import com.example.loginandregistration.validation.FirestoreDataValidator
import com.example.loginandregistration.validation.ValidationException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRepository(
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
        private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
        private val context: Context? = null
) {
    private val storageRepository: StorageRepository? =
            context?.let { StorageRepository(storage, auth, it) }
    private val validator = FirestoreDataValidator()

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

    /**
     * Validates that the current user is a participant in the chat. This provides better error
     * messages before attempting Firestore operations.
     */
    private suspend fun validateParticipantAccess(chatId: String): Result<Unit> {
        return try {
            val chatDoc = firestore.collection(CHATS_COLLECTION).document(chatId).get().await()

            if (!chatDoc.exists()) {
                return Result.failure(Exception("Chat not found"))
            }

            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()

            if (getCurrentUserId() !in participants) {
                Log.w(TAG, "User ${getCurrentUserId()} is not a participant in chat $chatId")
                return Result.failure(Exception("You are not a participant in this chat"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error validating participant access", e)
            Result.failure(e)
        }
    }

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

                                // Handle specific Firestore errors
                                when (error) {
                                    is FirebaseFirestoreException -> {
                                        when (error.code) {
                                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                                Log.e(
                                                        TAG,
                                                        "PERMISSION_DENIED: User does not have permission to access chats"
                                                )
                                            }
                                            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                                                Log.e(
                                                        TAG,
                                                        "FIRESTORE INDEX REQUIRED: Create a composite index for 'chats' collection with fields: participants (array-contains) and lastMessageTime (descending)"
                                                )
                                            }
                                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                                Log.e(
                                                        TAG,
                                                        "UNAVAILABLE: Firestore service is temporarily unavailable"
                                                )
                                            }
                                            else -> {
                                                Log.e(TAG, "Firestore error code: ${error.code}")
                                            }
                                        }
                                    }
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

                            Log.d(
                                    TAG,
                                    "getUserChats: Received ${snapshot.documents.size} chat documents"
                            )

                            val chats =
                                    snapshot.documents
                                            .mapNotNull { doc ->
                                                try {
                                                    val chat = doc.toObject(Chat::class.java)
                                                    if (chat != null) {
                                                        Log.d(
                                                                TAG,
                                                                "Parsed chat: ${chat.chatId}, type: ${chat.type}, participants: ${chat.participants.size}"
                                                        )
                                                    }
                                                    chat
                                                } catch (e: Exception) {
                                                    Log.e(
                                                            TAG,
                                                            "Error parsing chat document ${doc.id}",
                                                            e
                                                    )
                                                    null
                                                }
                                            }
                                            // Sort manually by lastMessageTime since we can't use
                                            // orderBy without index
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
     * Creates a user document in Firestore from UserInfo. This is used when a user is found in
     * search but doesn't have a Firestore document yet.
     */
    private suspend fun createUserDocument(userInfo: UserInfo): Result<Unit> {
        return try {
            Log.d(TAG, "createUserDocument: Creating document for ${userInfo.displayName}")

            val userDoc =
                    mapOf(
                            "uid" to userInfo.userId,
                            "email" to userInfo.email,
                            "displayName" to userInfo.displayName,
                            "photoUrl" to userInfo.profileImageUrl,
                            "initials" to userInfo.initials,
                            "isOnline" to userInfo.online,
                            "lastActive" to (userInfo.lastSeen ?: Date())
                    )

            firestore.collection(USERS_COLLECTION).document(userInfo.userId).set(userDoc).await()

            Log.d(
                    TAG,
                    "createUserDocument: Successfully created document for ${userInfo.displayName}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "createUserDocument: Error creating user document", e)
            Result.failure(e)
        }
    }

    suspend fun getOrCreateDirectChat(
            otherUserId: String,
            otherUserInfo: UserInfo? = null
    ): Result<Chat> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    if (getCurrentUserId().isEmpty()) {
                        return@withContext Result.failure(Exception("User not authenticated"))
                    }

                    // Validate that we're not creating a chat with ourselves
                    if (otherUserId == getCurrentUserId()) {
                        return@withContext Result.failure(
                                Exception("Cannot create chat with yourself")
                        )
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
                        return@withContext if (chat != null) {
                            // Validate existing chat structure
                            if (chat.type != ChatType.DIRECT) {
                                Log.w(TAG, "Found chat with wrong type: ${chat.type}")
                                return@withContext Result.failure(Exception("Invalid chat type"))
                            }
                            if (chat.participants.size != 2) {
                                Log.w(
                                        TAG,
                                        "Direct chat has ${chat.participants.size} participants, expected 2"
                                )
                                return@withContext Result.failure(
                                        Exception("Invalid participant count for direct chat")
                                )
                            }
                            Result.success(chat)
                        } else {
                            Result.failure(Exception("Failed to parse existing chat"))
                        }
                    }

                    Log.d(TAG, "Fetching user info for otherUserId: $otherUserId")

                    // If UserInfo was provided (from search results), try to create the document
                    // first
                    if (otherUserInfo != null) {
                        Log.d(
                                TAG,
                                "UserInfo provided, attempting to create user document if needed"
                        )
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
                            Log.d(
                                    TAG,
                                    "Using UserInfo from search results: ${otherUserInfo.displayName}"
                            )
                        } else {
                            return@withContext Result.failure(
                                    Exception(
                                            "Cannot create chat: The other user needs to log into the app at least once to create their profile. Please ask them to open the app and sign in."
                                    )
                            )
                        }
                    }

                    val otherUser =
                            if (otherUserResult.isSuccess) {
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
                        return@withContext Result.failure(
                                Exception(
                                        "Current user not found: ${currentUserResult.exceptionOrNull()?.message}"
                                )
                        )
                    }
                    val currentUser = currentUserResult.getOrThrow()
                    Log.d(TAG, "Current user found: ${currentUser.displayName}")

                    val chatId = firestore.collection(CHATS_COLLECTION).document().id
                    val participants = listOf(getCurrentUserId(), otherUserId)

                    // Validate participants array for DIRECT chat
                    if (participants.size != 2) {
                        Log.e(
                                TAG,
                                "Direct chat must have exactly 2 participants, got ${participants.size}"
                        )
                        return@withContext Result.failure(
                                Exception("Direct chat must have exactly 2 participants")
                        )
                    }

                    if (participants.distinct().size != 2) {
                        Log.e(TAG, "Direct chat participants must be unique")
                        return@withContext Result.failure(
                                Exception("Cannot create chat with duplicate participants")
                        )
                    }

                    val chat =
                            Chat(
                                    chatId = chatId,
                                    type = ChatType.DIRECT,
                                    participants = participants,
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

                    Log.d(
                            TAG,
                            "Creating DIRECT chat with ${chat.participants.size} participants: ${chat.participants}"
                    )
                    firestore.collection(CHATS_COLLECTION).document(chatId).set(chat).await()

                    Result.success(chat)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating direct chat", e)
                    Result.failure(e)
                }
            }

    suspend fun getOrCreateGroupChat(groupId: String): Result<Chat> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    if (getCurrentUserId().isEmpty()) {
                        return@withContext Result.failure(Exception("User not authenticated"))
                    }

                    Log.d(
                            TAG,
                            "getOrCreateGroupChat: Checking for existing chat for group $groupId"
                    )

                    // Check for existing chat with error handling
                    val existingChats =
                            try {
                                firestore
                                        .collection(CHATS_COLLECTION)
                                        .whereEqualTo("type", ChatType.GROUP.name)
                                        .whereEqualTo("groupId", groupId)
                                        .get()
                                        .await()
                            } catch (e: FirebaseFirestoreException) {
                                Log.e(TAG, "getOrCreateGroupChat: Error checking existing chats", e)

                                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                    return@withContext Result.failure(
                                            Exception(
                                                    "You don't have permission to access this chat"
                                            )
                                    )
                                }

                                return@withContext Result.failure(e)
                            } catch (e: Exception) {
                                Log.e(
                                        TAG,
                                        "getOrCreateGroupChat: Unexpected error checking existing chats",
                                        e
                                )
                                return@withContext Result.failure(e)
                            }
                    if (!existingChats.isEmpty) {
                        val chat = existingChats.documents.first().toObject(Chat::class.java)
                        Log.d(TAG, "getOrCreateGroupChat: Found existing chat ${chat?.chatId}")
                        return@withContext if (chat != null) {
                            Result.success(chat)
                        } else {
                            Result.failure(Exception("Failed to parse existing chat"))
                        }
                    }

                    Log.d(TAG, "getOrCreateGroupChat: Creating new chat for group $groupId")

                    // Fetch group document with error handling
                    val groupDoc =
                            try {
                                firestore.collection("groups").document(groupId).get().await()
                            } catch (e: FirebaseFirestoreException) {
                                Log.e(TAG, "getOrCreateGroupChat: Error fetching group document", e)

                                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                    return@withContext Result.failure(
                                            Exception(
                                                    "You don't have permission to access this group"
                                            )
                                    )
                                }

                                return@withContext Result.failure(e)
                            } catch (e: Exception) {
                                Log.e(
                                        TAG,
                                        "getOrCreateGroupChat: Unexpected error fetching group document",
                                        e
                                )
                                return@withContext Result.failure(e)
                            }
                    if (!groupDoc.exists()) {
                        Log.e(
                                TAG,
                                "getOrCreateGroupChat: Group document does not exist for groupId: $groupId"
                        )
                        return@withContext Result.failure(Exception("Group not found"))
                    }

                    val groupName = groupDoc.getString("name") ?: "Group Chat"
                    // FIXED: Use memberIds instead of members to match the actual field name in
                    // groups
                    // collection
                    val memberIds = groupDoc.get("memberIds") as? List<String> ?: emptyList()

                    Log.d(
                            TAG,
                            "getOrCreateGroupChat: Group '$groupName' has ${memberIds.size} members"
                    )

                    // Verify all members are included in participants array
                    if (memberIds.isEmpty()) {
                        Log.e(TAG, "getOrCreateGroupChat: Group has no members")
                        return@withContext Result.failure(Exception("Group has no members"))
                    }

                    // Validate participants array for GROUP chat
                    if (memberIds.size < 2) {
                        Log.e(
                                TAG,
                                "Group chat must have at least 2 participants, got ${memberIds.size}"
                        )
                        return@withContext Result.failure(
                                Exception("Group chat must have at least 2 participants")
                        )
                    }

                    if (memberIds.size > 100) {
                        Log.e(
                                TAG,
                                "Group chat cannot have more than 100 participants, got ${memberIds.size}"
                        )
                        return@withContext Result.failure(
                                Exception("Group chat cannot have more than 100 participants")
                        )
                    }

                    if (memberIds.distinct().size != memberIds.size) {
                        Log.e(TAG, "Group chat participants must be unique")
                        return@withContext Result.failure(
                                Exception("Group chat has duplicate participants")
                        )
                    }

                    val participantDetails = mutableMapOf<String, UserInfo>()
                    for (memberId in memberIds) {
                        getUserInfo(memberId).getOrNull()?.let { userInfo ->
                            participantDetails[memberId] = userInfo
                        }
                    }

                    val chatId = firestore.collection(CHATS_COLLECTION).document().id
                    val chat =
                            Chat(
                                    chatId = chatId,
                                    type = ChatType.GROUP,
                                    participants =
                                            memberIds, // All group members included in participants
                                    participantDetails = participantDetails,
                                    lastMessage = "",
                                    lastMessageTime = Date(),
                                    lastMessageSenderId = "",
                                    unreadCount = memberIds.associateWith { 0 },
                                    groupId = groupId,
                                    groupName = groupName,
                                    createdAt = Date()
                            )

                    Log.d(
                            TAG,
                            "Creating GROUP chat with ${chat.participants.size} participants for group '$groupName'"
                    )

                    // Create chat with error handling
                    try {
                        firestore.collection(CHATS_COLLECTION).document(chatId).set(chat).await()
                    } catch (e: FirebaseFirestoreException) {
                        Log.e(TAG, "getOrCreateGroupChat: Error creating chat", e)

                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            return@withContext Result.failure(
                                    Exception(
                                            "You don't have permission to create a chat for this group"
                                    )
                            )
                        }

                        return@withContext Result.failure(e)
                    } catch (e: Exception) {
                        Log.e(TAG, "getOrCreateGroupChat: Unexpected error creating chat", e)
                        return@withContext Result.failure(e)
                    }

                    Log.d(
                            TAG,
                            "getOrCreateGroupChat: Successfully created chat $chatId for group $groupId with ${memberIds.size} participants"
                    )

                    Result.success(chat)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating group chat", e)

                    // Handle permission errors gracefully
                    if (e is FirebaseFirestoreException &&
                                    e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    ) {
                        return@withContext Result.failure(
                                Exception("You don't have permission to access this group chat")
                        )
                    }

                    Result.failure(e)
                }
            }

    /**
     * Auto-creates chats for all groups the user is a member of. This should be called when the
     * user opens the Chat tab or logs in.
     */
    suspend fun ensureGroupChatsExist(): Result<Int> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    if (getCurrentUserId().isEmpty()) {
                        return@withContext Result.failure(Exception("User not authenticated"))
                    }

                    Log.d(TAG, "ensureGroupChatsExist: Fetching user's groups")

                    // Get all groups where user is a member with error handling
                    // FIXED: Use memberIds instead of members to match the actual field name in
                    // groups
                    // collection
                    val groupsSnapshot =
                            try {
                                firestore
                                        .collection("groups")
                                        .whereArrayContains("memberIds", getCurrentUserId())
                                        .get()
                                        .await()
                            } catch (e: FirebaseFirestoreException) {
                                Log.e(TAG, "ensureGroupChatsExist: Error fetching groups", e)

                                // Handle permission errors gracefully
                                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                    Log.e(
                                            TAG,
                                            "ensureGroupChatsExist: Permission denied when fetching groups"
                                    )
                                    return@withContext Result.failure(
                                            Exception(
                                                    "You don't have permission to access groups. Please try logging out and back in."
                                            )
                                    )
                                }

                                return@withContext Result.failure(e)
                            } catch (e: Exception) {
                                Log.e(
                                        TAG,
                                        "ensureGroupChatsExist: Unexpected error fetching groups",
                                        e
                                )
                                return@withContext Result.failure(e)
                            }
                    Log.d(
                            TAG,
                            "ensureGroupChatsExist: Found ${groupsSnapshot.documents.size} groups"
                    )

                    var createdCount = 0
                    for (groupDoc in groupsSnapshot.documents) {
                        val groupId = groupDoc.id
                        val groupName = groupDoc.getString("name") ?: "Group"

                        Log.d(TAG, "ensureGroupChatsExist: Checking group '$groupName' ($groupId)")

                        // Check if chat already exists for this group with error handling
                        val existingChat =
                                try {
                                    firestore
                                            .collection(CHATS_COLLECTION)
                                            .whereEqualTo("type", ChatType.GROUP.name)
                                            .whereEqualTo("groupId", groupId)
                                            .limit(1)
                                            .get()
                                            .await()
                                } catch (e: Exception) {
                                    Log.e(
                                            TAG,
                                            "ensureGroupChatsExist: Error checking existing chat for group '$groupName'",
                                            e
                                    )
                                    // Continue to next group instead of failing completely
                                    continue
                                }
                        if (existingChat.isEmpty) {
                            // Create chat for this group
                            Log.d(
                                    TAG,
                                    "ensureGroupChatsExist: Creating chat for group '$groupName'"
                            )
                            val result = getOrCreateGroupChat(groupId)
                            if (result.isSuccess) {
                                createdCount++
                                Log.d(
                                        TAG,
                                        "ensureGroupChatsExist: Successfully created chat for group '$groupName'"
                                )
                            } else {
                                Log.e(
                                        TAG,
                                        "ensureGroupChatsExist: Failed to create chat for group '$groupName': ${result.exceptionOrNull()?.message}"
                                )
                                // Continue to next group instead of failing completely
                            }
                        } else {
                            Log.d(
                                    TAG,
                                    "ensureGroupChatsExist: Chat already exists for group '$groupName'"
                            )
                        }
                    }

                    Log.d(TAG, "ensureGroupChatsExist: Created $createdCount new group chats")
                    Result.success(createdCount)
                } catch (e: Exception) {
                    Log.e(TAG, "Error ensuring group chats exist", e)

                    // Handle permission errors gracefully
                    if (e is FirebaseFirestoreException &&
                                    e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    ) {
                        Log.e(TAG, "ensureGroupChatsExist: Permission denied error caught")
                        return@withContext Result.failure(
                                Exception(
                                        "You don't have permission to access chat data. Please try logging out and back in."
                                )
                        )
                    }

                    Result.failure(e)
                }
            }

    suspend fun sendMessage(chatId: String, text: String, retryCount: Int = 0): Result<Message> =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    if (getCurrentUserId().isEmpty()) {
                        return@withContext Result.failure(Exception("User not authenticated"))
                    }

                    // Validate participant access
                    val accessValidation = validateParticipantAccess(chatId)
                    if (accessValidation.isFailure) {
                        return@withContext Result.failure(
                                accessValidation.exceptionOrNull() ?: Exception("Access denied")
                        )
                    }

                    // Validate and sanitize message text
                    val (validation, sanitizedText) =
                            com.example.loginandregistration.utils.InputValidator
                                    .validateAndSanitizeMessage(text)
                    if (!validation.isValid) {
                        return@withContext Result.failure(
                                Exception(validation.errorMessage ?: "Invalid message")
                        )
                    }

                    if (sanitizedText.isNullOrBlank()) {
                        return@withContext Result.failure(Exception("Message cannot be empty"))
                    }

                    val currentUser =
                            getUserInfo(getCurrentUserId()).getOrNull()
                                    ?: return@withContext Result.failure(
                                            Exception("User not found")
                                    )

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
                                    text = sanitizedText,
                                    timestamp = Date(),
                                    readBy = listOf(getCurrentUserId()),
                                    status = MessageStatus.SENDING
                            )

                    // Validate message data before Firestore operation
                    val validationResult = validator.validateMessage(message)
                    if (!validationResult.isValid) {
                        return@withContext Result.failure(ValidationException(validationResult))
                    }

                    // Queue message for offline support
                    offlineQueue?.queueMessage(message)

                    // Use transaction to atomically create message and update chat metadata
                    val sendResult = safeFirestoreCall {
                        firestore
                                .runTransaction { transaction ->
                                    val messageRef =
                                            firestore
                                                    .collection(CHATS_COLLECTION)
                                                    .document(chatId)
                                                    .collection(MESSAGES_COLLECTION)
                                                    .document(messageId)

                                    val chatRef =
                                            firestore.collection(CHATS_COLLECTION).document(chatId)

                                    // Get current chat data to calculate unread counts
                                    val chatSnapshot = transaction.get(chatRef)
                                    val participants =
                                            chatSnapshot.get("participants") as? List<String>
                                                    ?: emptyList()
                                    val currentUnreadCount =
                                            chatSnapshot.get("unreadCount") as? Map<String, Long>
                                                    ?: emptyMap()

                                    // Calculate new unread counts
                                    val newUnreadCount =
                                            participants.associateWith { participantId ->
                                                if (participantId == getCurrentUserId()) {
                                                    0
                                                } else {
                                                    (currentUnreadCount[participantId] ?: 0) + 1
                                                }
                                            }

                                    // Set the message
                                    transaction.set(
                                            messageRef,
                                            message.copy(status = MessageStatus.SENT)
                                    )

                                    // Update chat metadata
                                    transaction.update(
                                            chatRef,
                                            mapOf(
                                                    "lastMessage" to sanitizedText,
                                                    "lastMessageTime" to Date(),
                                                    "lastMessageSenderId" to getCurrentUserId(),
                                                    "unreadCount" to newUnreadCount
                                            )
                                    )
                                }
                                .await()
                    }

                    if (sendResult.isFailure) {
                        val exception = sendResult.exceptionOrNull()
                        Log.e(TAG, "Error sending message (attempt ${retryCount + 1})", exception)

                        // Categorize the failure type
                        if (exception is FirebaseFirestoreException) {
                            when (exception.code) {
                                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                    // Permanent failure - mark as FAILED_PERMANENT
                                    offlineQueue?.markMessageAsFailedPermanent(messageId)
                                    Log.e(
                                            TAG,
                                            "Permission denied - marking message as permanently failed"
                                    )
                                    return@withContext Result.failure(
                                            Exception(
                                                    "Permission denied: You don't have access to send messages in this chat"
                                            )
                                    )
                                }
                                FirebaseFirestoreException.Code.UNAVAILABLE,
                                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                                FirebaseFirestoreException.Code.ABORTED -> {
                                    // Retryable failure - mark as FAILED_RETRYABLE
                                    offlineQueue?.markMessageAsFailedRetryable(messageId)
                                    Log.w(
                                            TAG,
                                            "Network/service error - marking message as retryable (${exception.code})"
                                    )
                                    return@withContext Result.failure(
                                            Exception(
                                                    "Network error: Message will be retried automatically"
                                            )
                                    )
                                }
                                else -> {
                                    // Unknown Firestore error - treat as retryable
                                    offlineQueue?.markMessageAsFailedRetryable(messageId)
                                    Log.w(
                                            TAG,
                                            "Unknown Firestore error - marking as retryable (${exception.code})"
                                    )
                                    return@withContext Result.failure(
                                            Exception(
                                                    "Failed to send message: ${exception.message}"
                                            )
                                    )
                                }
                            }
                        } else {
                            // Non-Firestore exception (network, etc.) - treat as retryable
                            offlineQueue?.markMessageAsFailedRetryable(messageId)
                            Log.w(TAG, "Non-Firestore error - marking as retryable")
                            return@withContext Result.failure(
                                    exception ?: Exception("Failed to send message")
                            )
                        }
                    }

                    // Remove from queue on success
                    offlineQueue?.removeMessage(messageId)

                    // Trigger notifications for recipients (don't fail if this fails)
                    try {
                        triggerNotifications(chatId, message)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to trigger notifications, but message was sent", e)
                    }

                    Result.success(message.copy(status = MessageStatus.SENT))
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message", e)
                    Result.failure(e)
                }
            }

    /**
     * Sends an image message to a chat. Compresses the image before upload and shows progress.
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

            val currentUser =
                    getUserInfo(getCurrentUserId()).getOrNull()
                            ?: return Result.failure(Exception("User not found"))

            // Create message ID first
            val messageId =
                    firestore
                            .collection(CHATS_COLLECTION)
                            .document(chatId)
                            .collection(MESSAGES_COLLECTION)
                            .document()
                            .id

            // Create temporary message with SENDING status
            val tempMessage =
                    Message(
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
                val uploadResult =
                        storageRepository.uploadImage(
                                uri = imageUri,
                                path = storagePath,
                                onProgress = onProgress
                        )

                if (uploadResult.isFailure) {
                    Log.e(
                            TAG,
                            "sendImageMessage: Image upload failed",
                            uploadResult.exceptionOrNull()
                    )
                    offlineQueue?.markMessageAsFailed(messageId)
                    return Result.failure(
                            uploadResult.exceptionOrNull() ?: Exception("Image upload failed")
                    )
                }

                val imageUrl = uploadResult.getOrThrow()
                Log.d(TAG, "sendImageMessage: Image uploaded successfully: $imageUrl")

                // Create final message with image URL
                val message = tempMessage.copy(imageUrl = imageUrl, status = MessageStatus.SENT)

                // Validate message data before Firestore operation
                val validationResult = validator.validateMessage(message)
                if (!validationResult.isValid) {
                    Log.e(
                            TAG,
                            "sendImageMessage: Validation failed: ${validationResult.getErrorMessage()}"
                    )
                    offlineQueue?.markMessageAsFailed(messageId)
                    return Result.failure(ValidationException(validationResult))
                }

                // Save message to Firestore and update chat metadata in transaction
                firestore
                        .runTransaction { transaction ->
                            val messageRef =
                                    firestore
                                            .collection(CHATS_COLLECTION)
                                            .document(chatId)
                                            .collection(MESSAGES_COLLECTION)
                                            .document(messageId)

                            val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

                            // Get current chat data
                            val chatSnapshot = transaction.get(chatRef)
                            val participants =
                                    chatSnapshot.get("participants") as? List<String> ?: emptyList()
                            val currentUnreadCount =
                                    chatSnapshot.get("unreadCount") as? Map<String, Long>
                                            ?: emptyMap()

                            // Calculate new unread counts
                            val newUnreadCount =
                                    participants.associateWith { participantId ->
                                        if (participantId == getCurrentUserId()) {
                                            0
                                        } else {
                                            (currentUnreadCount[participantId] ?: 0) + 1
                                        }
                                    }

                            // Set the message
                            transaction.set(messageRef, message)

                            // Update chat metadata
                            transaction.update(
                                    chatRef,
                                    mapOf(
                                            "lastMessage" to "📷 Photo",
                                            "lastMessageTime" to Date(),
                                            "lastMessageSenderId" to getCurrentUserId(),
                                            "unreadCount" to newUnreadCount
                                    )
                            )
                        }
                        .await()

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

    /**
     * Sends a document message to a chat. Uploads the document and shows progress.
     *
     * @param chatId ID of the chat
     * @param documentUri URI of the document to send
     * @param onProgress Callback for upload progress (0-100)
     * @return Result with the sent message on success
     */
    suspend fun sendDocumentMessage(
            chatId: String,
            documentUri: Uri,
            onProgress: (Int) -> Unit = {}
    ): Result<Message> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (storageRepository == null) {
                return Result.failure(Exception("Storage repository not initialized"))
            }

            Log.d(TAG, "sendDocumentMessage: Starting document upload for chat $chatId")

            val currentUser =
                    getUserInfo(getCurrentUserId()).getOrNull()
                            ?: return Result.failure(Exception("User not found"))

            // Get file info from URI
            val fileInfo = storageRepository.getFileInfoFromUri(documentUri)
            val fileName = fileInfo.first
            val fileSize = fileInfo.second

            Log.d(TAG, "sendDocumentMessage: Document name: $fileName, size: ${fileSize / 1024}KB")

            // Create message ID first
            val messageId =
                    firestore
                            .collection(CHATS_COLLECTION)
                            .document(chatId)
                            .collection(MESSAGES_COLLECTION)
                            .document()
                            .id

            // Create temporary message with SENDING status
            val tempMessage =
                    Message(
                            id = messageId,
                            chatId = chatId,
                            senderId = getCurrentUserId(),
                            senderName = currentUser.displayName,
                            senderImageUrl = currentUser.profileImageUrl,
                            text = "", // Empty text for document messages
                            documentUrl = null, // Will be set after upload
                            documentName = fileName,
                            documentSize = fileSize,
                            timestamp = Date(),
                            readBy = listOf(getCurrentUserId()),
                            status = MessageStatus.SENDING
                    )

            // Queue message for offline support
            offlineQueue?.queueMessage(tempMessage)

            try {
                // Upload document to Storage
                val storagePath = storageRepository.getChatDocumentPath(chatId)
                val uploadResult =
                        storageRepository.uploadDocument(
                                uri = documentUri,
                                path = storagePath,
                                onProgress = onProgress
                        )

                if (uploadResult.isFailure) {
                    Log.e(
                            TAG,
                            "sendDocumentMessage: Document upload failed",
                            uploadResult.exceptionOrNull()
                    )
                    offlineQueue?.markMessageAsFailed(messageId)
                    return Result.failure(
                            uploadResult.exceptionOrNull() ?: Exception("Document upload failed")
                    )
                }

                val documentUrl = uploadResult.getOrThrow()
                Log.d(TAG, "sendDocumentMessage: Document uploaded successfully: $documentUrl")

                // Create final message with document URL
                val message =
                        tempMessage.copy(documentUrl = documentUrl, status = MessageStatus.SENT)

                // Validate message data before Firestore operation
                val validationResult = validator.validateMessage(message)
                if (!validationResult.isValid) {
                    Log.e(
                            TAG,
                            "sendDocumentMessage: Validation failed: ${validationResult.getErrorMessage()}"
                    )
                    offlineQueue?.markMessageAsFailed(messageId)
                    return Result.failure(ValidationException(validationResult))
                }

                // Save message to Firestore and update chat metadata in transaction
                firestore
                        .runTransaction { transaction ->
                            val messageRef =
                                    firestore
                                            .collection(CHATS_COLLECTION)
                                            .document(chatId)
                                            .collection(MESSAGES_COLLECTION)
                                            .document(messageId)

                            val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)

                            // Get current chat data
                            val chatSnapshot = transaction.get(chatRef)
                            val participants =
                                    chatSnapshot.get("participants") as? List<String> ?: emptyList()
                            val currentUnreadCount =
                                    chatSnapshot.get("unreadCount") as? Map<String, Long>
                                            ?: emptyMap()

                            // Calculate new unread counts
                            val newUnreadCount =
                                    participants.associateWith { participantId ->
                                        if (participantId == getCurrentUserId()) {
                                            0
                                        } else {
                                            (currentUnreadCount[participantId] ?: 0) + 1
                                        }
                                    }

                            // Set the message
                            transaction.set(messageRef, message)

                            // Update chat metadata
                            transaction.update(
                                    chatRef,
                                    mapOf(
                                            "lastMessage" to "📄 $fileName",
                                            "lastMessageTime" to Date(),
                                            "lastMessageSenderId" to getCurrentUserId(),
                                            "unreadCount" to newUnreadCount
                                    )
                            )
                        }
                        .await()

                // Remove from queue on success
                offlineQueue?.removeMessage(messageId)

                // Trigger notifications for recipients
                triggerNotifications(chatId, message.copy(text = "📄 $fileName"))

                Log.d(TAG, "sendDocumentMessage: Document message sent successfully")
                Result.success(message)
            } catch (sendError: Exception) {
                Log.e(TAG, "sendDocumentMessage: Error sending document message", sendError)
                offlineQueue?.markMessageAsFailed(messageId)
                Result.failure(sendError)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendDocumentMessage: Unexpected error", e)
            Result.failure(e)
        }
    }

    fun getChatMessages(chatId: String, limit: Int = 50): Flow<List<Message>> = callbackFlow {
        Log.d(TAG, "getChatMessages: Setting up listener for chat $chatId")

        // Validate participant access before setting up listener
        try {
            val accessValidation = validateParticipantAccess(chatId)
            if (accessValidation.isFailure) {
                Log.w(TAG, "User is not a participant in chat $chatId")
                trySend(emptyList())
                close(accessValidation.exceptionOrNull())
                return@callbackFlow
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating participant access", e)
            trySend(emptyList())
            close(e)
            return@callbackFlow
        }

        val listener =
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .collection(MESSAGES_COLLECTION)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(TAG, "Error listening to messages: ${error.message}", error)

                                // Handle specific Firestore errors
                                when (error) {
                                    is FirebaseFirestoreException -> {
                                        when (error.code) {
                                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                                Log.e(
                                                        TAG,
                                                        "PERMISSION_DENIED: User does not have permission to read messages in chat $chatId"
                                                )
                                            }
                                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                                Log.e(
                                                        TAG,
                                                        "UNAVAILABLE: Firestore service is temporarily unavailable"
                                                )
                                            }
                                            else -> {
                                                Log.e(TAG, "Firestore error code: ${error.code}")
                                            }
                                        }
                                    }
                                }

                                // Send empty list on error to prevent UI from breaking
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            if (snapshot == null) {
                                Log.w(TAG, "getChatMessages: Snapshot is null")
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            Log.d(
                                    TAG,
                                    "getChatMessages: Received ${snapshot.documents.size} messages"
                            )

                            val messages =
                                    snapshot.documents
                                            .mapNotNull { doc ->
                                                try {
                                                    doc.toObject(Message::class.java)
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error parsing message ${doc.id}", e)
                                                    null
                                                }
                                            }
                                            .reversed() // Reverse to show oldest first

                            Log.d(TAG, "getChatMessages: Sending ${messages.size} messages to UI")
                            trySend(messages)
                        }

        awaitClose {
            Log.d(TAG, "getChatMessages: Closing listener for chat $chatId")
            listener.remove()
        }
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

    suspend fun markMessagesAsRead(chatId: String, messageIds: List<String>): Result<Unit> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            if (messageIds.isEmpty()) {
                return Result.success(Unit)
            }

            Log.d(
                    TAG,
                    "markMessagesAsRead: Marking ${messageIds.size} messages as read in chat $chatId"
            )

            // Use safeFirestoreCall for proper error handling
            val batchResult = safeFirestoreCall {
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
            }

            if (batchResult.isFailure) {
                val exception = batchResult.exceptionOrNull()
                Log.e(TAG, "Error marking messages as read (batch update)", exception)

                // If it's a permission error, log but don't fail completely
                if (exception is FirebaseFirestoreException &&
                                exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                ) {
                    Log.w(
                            TAG,
                            "Permission denied when marking messages as read - user may not have access"
                    )
                    return Result.failure(
                            Exception("Permission denied: Cannot mark messages as read")
                    )
                }

                return Result.failure(exception ?: Exception("Failed to mark messages as read"))
            }

            // Update unread count for the chat
            val unreadResult = safeFirestoreCall {
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .update("unreadCount.${getCurrentUserId()}", 0)
                        .await()
            }

            if (unreadResult.isFailure) {
                Log.w(
                        TAG,
                        "Failed to update unread count, but messages were marked as read",
                        unreadResult.exceptionOrNull()
                )
            }

            Log.d(TAG, "markMessagesAsRead: Successfully marked messages as read")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read", e)
            Result.failure(e)
        }
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

            Log.d(
                    TAG,
                    "searchUsers: Found ${nameResults.size()} by name, ${emailResults.size()} by email"
            )

            val users = mutableSetOf<UserInfo>()

            // Convert Firestore documents to UserInfo
            nameResults.documents.forEach { doc ->
                try {
                    val userId = doc.id
                    val displayName =
                            doc.getString("displayName")
                                    ?: doc.getString("display_name") ?: "Unknown"
                    val email = doc.getString("email") ?: ""
                    val profileImageUrl =
                            doc.getString("photoUrl") ?: doc.getString("profileImageUrl") ?: ""
                    val initials = doc.getString("initials") ?: generateInitials(displayName, email)
                    val online = doc.getBoolean("isOnline") ?: doc.getBoolean("online") ?: false
                    val lastSeen = doc.getDate("lastActive") ?: doc.getDate("lastSeen")

                    val userInfo =
                            UserInfo(
                                    userId = userId,
                                    displayName = displayName,
                                    email = email,
                                    profileImageUrl = profileImageUrl,
                                    initials = initials,
                                    online = online,
                                    lastSeen = lastSeen
                            )
                    users.add(userInfo)
                    Log.d(
                            TAG,
                            "searchUsers: Added user ${userInfo.displayName} (${userInfo.userId})"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "searchUsers: Error parsing user document ${doc.id}", e)
                }
            }

            emailResults.documents.forEach { doc ->
                try {
                    val userId = doc.id
                    // Skip if already added
                    if (users.any { it.userId == userId }) return@forEach

                    val displayName =
                            doc.getString("displayName")
                                    ?: doc.getString("display_name") ?: "Unknown"
                    val email = doc.getString("email") ?: ""
                    val profileImageUrl =
                            doc.getString("photoUrl") ?: doc.getString("profileImageUrl") ?: ""
                    val initials = doc.getString("initials") ?: generateInitials(displayName, email)
                    val online = doc.getBoolean("isOnline") ?: doc.getBoolean("online") ?: false
                    val lastSeen = doc.getDate("lastActive") ?: doc.getDate("lastSeen")

                    val userInfo =
                            UserInfo(
                                    userId = userId,
                                    displayName = displayName,
                                    email = email,
                                    profileImageUrl = profileImageUrl,
                                    initials = initials,
                                    online = online,
                                    lastSeen = lastSeen
                            )
                    users.add(userInfo)
                    Log.d(
                            TAG,
                            "searchUsers: Added user ${userInfo.displayName} (${userInfo.userId})"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "searchUsers: Error parsing user document ${doc.id}", e)
                }
            }

            val filteredUsers = users.filter { it.userId != getCurrentUserId() }
            Log.d(
                    TAG,
                    "searchUsers: Returning ${filteredUsers.size} users (filtered out current user)"
            )

            Result.success(filteredUsers.toList())
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users", e)
            Result.failure(e)
        }
    }

    private suspend fun getUserInfo(userId: String): Result<UserInfo> {
        return try {
            // Check cache first
            com.example.loginandregistration.utils.DataCache.getUser(userId)?.let {
                Log.d(TAG, "getUserInfo: Returning cached user info for $userId")
                return Result.success(it)
            }

            Log.d(TAG, "getUserInfo: Fetching user document for userId: $userId")
            val doc = firestore.collection(USERS_COLLECTION).document(userId).get().await()

            if (!doc.exists()) {
                Log.e(TAG, "getUserInfo: User document does not exist for userId: $userId")
                Log.e(
                        TAG,
                        "getUserInfo: Make sure the user has logged in at least once to create their profile"
                )
                return Result.failure(
                        Exception(
                                "User not found: Invalid document reference. Document ID: $userId"
                        )
                )
            }

            Log.d(TAG, "getUserInfo: User document found. Fields: ${doc.data?.keys}")

            // Handle both field name variations (displayName vs display_name, photoUrl vs
            // profileImageUrl, etc.)
            val displayName =
                    doc.getString("displayName")
                            ?: doc.getString("display_name") ?: doc.getString("name")
                                    ?: "Unknown User"

            val email = doc.getString("email") ?: ""

            val profileImageUrl =
                    doc.getString("profileImageUrl")
                            ?: doc.getString("photoUrl") ?: doc.getString("photo_url")
                                    ?: doc.getString("profile_image_url") ?: ""

            val online = doc.getBoolean("online") ?: doc.getBoolean("isOnline") ?: false

            val lastSeen =
                    doc.getDate("lastSeen")
                            ?: doc.getDate("lastActive") ?: doc.getDate("last_active")
                                    ?: doc.getTimestamp("lastActive")?.toDate()

            // Get initials from document or generate from displayName/email
            val initials = doc.getString("initials") ?: generateInitials(displayName, email)

            val userInfo =
                    UserInfo(
                            userId = userId,
                            displayName = displayName,
                            email = email,
                            profileImageUrl = profileImageUrl,
                            initials = initials,
                            online = online,
                            lastSeen = lastSeen
                    )

            // Cache the user info
            com.example.loginandregistration.utils.DataCache.cacheUser(userId, userInfo)

            Log.d(TAG, "getUserInfo: Successfully created UserInfo for ${userInfo.displayName}")
            Result.success(userInfo)
        } catch (e: Exception) {
            Log.e(TAG, "getUserInfo: Error getting user info for $userId", e)
            Result.failure(e)
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
        if (displayName.isNotEmpty() && displayName != "Unknown User") {
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
            Log.d(TAG, "retryMessage: Retrying message ${message.id}")

            // Reset message status to SENDING
            offlineQueue?.updateMessageStatus(message.id, MessageStatus.SENDING)

            // Attempt to send with retry count
            val result = sendMessage(message.chatId, message.text, retryCount = 0)

            if (result.isSuccess) {
                offlineQueue?.removeMessage(message.id)
                Log.d(TAG, "retryMessage: Successfully sent message ${message.id}")
            } else {
                Log.w(
                        TAG,
                        "retryMessage: Failed to send message ${message.id}",
                        result.exceptionOrNull()
                )
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
            val pendingMessages =
                    queuedMessages.filter {
                        it.status == MessageStatus.SENDING ||
                                it.status == MessageStatus.FAILED_RETRYABLE
                    }

            if (pendingMessages.isEmpty()) {
                Log.d(TAG, "No pending messages to process")
                return Result.success(0)
            }

            Log.d(TAG, "Processing ${pendingMessages.size} queued messages")
            var successCount = 0
            var failureCount = 0

            for (message in pendingMessages) {
                try {
                    // Add small delay between retries to avoid overwhelming the server
                    kotlinx.coroutines.delay(500)

                    // Increment attempt counter
                    offlineQueue?.incrementAttempts(message.id)

                    val result = sendMessage(message.chatId, message.text, retryCount = 0)
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "Successfully sent queued message ${message.id}")
                    } else {
                        failureCount++
                        Log.w(
                                TAG,
                                "Failed to send queued message ${message.id}",
                                result.exceptionOrNull()
                        )
                        // Failure categorization is already handled in sendMessage
                    }
                } catch (e: Exception) {
                    failureCount++
                    Log.e(TAG, "Error processing queued message ${message.id}", e)
                }
            }

            Log.d(TAG, "Processed queued messages: $successCount succeeded, $failureCount failed")
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing queued messages", e)
            Result.failure(e)
        }
    }

    /**
     * Automatically retry failed retryable messages when network is restored. This should be called
     * when network connectivity is detected.
     */
    suspend fun retryFailedMessages(): Result<Int> {
        return try {
            val retryableMessages = offlineQueue?.getAllRetryableMessages() ?: emptyList()

            if (retryableMessages.isEmpty()) {
                Log.d(TAG, "No retryable messages to process")
                return Result.success(0)
            }

            Log.d(TAG, "Retrying ${retryableMessages.size} failed messages")
            var successCount = 0

            for (message in retryableMessages) {
                try {
                    // Add delay between retries
                    kotlinx.coroutines.delay(500)

                    // Increment attempt counter
                    offlineQueue?.incrementAttempts(message.id)

                    val result = sendMessage(message.chatId, message.text, retryCount = 0)
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "Successfully retried message ${message.id}")
                    } else {
                        Log.w(
                                TAG,
                                "Failed to retry message ${message.id}",
                                result.exceptionOrNull()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error retrying message ${message.id}", e)
                }
            }

            Log.d(TAG, "Retry completed: $successCount succeeded")
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error in retryFailedMessages", e)
            Result.failure(e)
        }
    }

    /** Clear the offline message queue */
    fun clearOfflineQueue() {
        offlineQueue?.clearQueue()
    }

    /**
     * Retry messages with exponential backoff strategy. This should be called periodically (e.g.,
     * when app comes to foreground or network is restored)
     */
    suspend fun retryPendingMessagesWithBackoff(): Result<Int> {
        return try {
            val messagesToRetry = offlineQueue?.getMessagesNeedingRetry() ?: emptyList()

            if (messagesToRetry.isEmpty()) {
                Log.d(TAG, "No messages need retry")
                return Result.success(0)
            }

            Log.d(TAG, "Retrying ${messagesToRetry.size} pending messages with backoff")
            var successCount = 0

            for (message in messagesToRetry) {
                try {
                    // Calculate backoff delay based on message age
                    val messageAge = System.currentTimeMillis() - (message.timestamp?.time ?: 0)
                    val backoffDelay = calculateBackoffDelay(messageAge)

                    Log.d(TAG, "Retrying message ${message.id} after ${backoffDelay}ms backoff")
                    kotlinx.coroutines.delay(backoffDelay)

                    val result = retryMessage(message)
                    if (result.isSuccess) {
                        successCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error retrying message ${message.id} with backoff", e)
                }
            }

            Log.d(TAG, "Retry with backoff completed: $successCount succeeded")
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error in retry with backoff", e)
            Result.failure(e)
        }
    }

    /** Calculate exponential backoff delay based on message age */
    private fun calculateBackoffDelay(messageAgeMs: Long): Long {
        // Start with 1 second, double for each 30 seconds of age, max 30 seconds
        val baseDelay = 1000L
        val maxDelay = 30000L
        val doubleInterval = 30000L

        val multiplier = (messageAgeMs / doubleInterval).toInt()
        val delay = baseDelay * (1 shl multiplier.coerceAtMost(5)) // Max 2^5 = 32x

        return delay.coerceAtMost(maxDelay)
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

    /**
     * Deletes a message from a chat. Only the sender can delete their own messages.
     *
     * @param chatId ID of the chat
     * @param message Message to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Verify that the current user is the sender
            if (message.senderId != getCurrentUserId()) {
                return Result.failure(Exception("You can only delete your own messages"))
            }

            Log.d(TAG, "deleteMessage: Deleting message ${message.id} from chat $chatId")

            // Delete the message document
            firestore
                    .collection(CHATS_COLLECTION)
                    .document(chatId)
                    .collection(MESSAGES_COLLECTION)
                    .document(message.id)
                    .delete()
                    .await()

            // If this was the last message, update the chat's last message
            val chatDoc = firestore.collection(CHATS_COLLECTION).document(chatId).get().await()

            val lastMessageId = chatDoc.getString("lastMessageId")
            if (lastMessageId == message.id) {
                // Get the new last message
                val lastMessageSnapshot =
                        firestore
                                .collection(CHATS_COLLECTION)
                                .document(chatId)
                                .collection(MESSAGES_COLLECTION)
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .await()

                if (!lastMessageSnapshot.isEmpty) {
                    val newLastMessage =
                            lastMessageSnapshot.documents.first().toObject(Message::class.java)
                    if (newLastMessage != null) {
                        updateChatLastMessage(chatId, newLastMessage.text, newLastMessage.senderId)
                    }
                } else {
                    // No more messages, clear last message
                    firestore
                            .collection(CHATS_COLLECTION)
                            .document(chatId)
                            .update(
                                    mapOf(
                                            "lastMessage" to "",
                                            "lastMessageTime" to Date(),
                                            "lastMessageSenderId" to ""
                                    )
                            )
                            .await()
                }
            }

            Log.d(TAG, "deleteMessage: Message deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteMessage: Error deleting message", e)
            Result.failure(e)
        }
    }

    /**
     * Sets the typing status for the current user in a chat.
     *
     * @param chatId ID of the chat
     * @param isTyping Whether the user is currently typing
     * @return Result indicating success or failure
     */
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                Log.w(TAG, "setTypingStatus: User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "setTypingStatus: Setting typing status to $isTyping for chat $chatId")

            // Fix: Use Date() which Firestore automatically converts to Timestamp
            val typingData =
                    mapOf(
                            "userId" to getCurrentUserId(),
                            "isTyping" to isTyping,
                            "timestamp" to Date()
                    )

            // Use safeFirestoreCall for proper error handling
            val result = safeFirestoreCall {
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .collection(TYPING_STATUS_COLLECTION)
                        .document(getCurrentUserId())
                        .set(typingData)
                        .await()
            }

            if (result.isFailure) {
                val exception = result.exceptionOrNull()
                Log.e(TAG, "setTypingStatus: Failed to set typing status", exception)

                // Handle permission errors gracefully - don't fail the UI
                if (exception is FirebaseFirestoreException &&
                                exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                ) {
                    Log.w(TAG, "setTypingStatus: Permission denied, but continuing gracefully")
                    // Return success to prevent UI errors, but log the issue
                    return Result.success(Unit)
                }

                return Result.failure(exception ?: Exception("Failed to set typing status"))
            }

            Log.d(TAG, "setTypingStatus: Successfully set typing status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "setTypingStatus: Unexpected error", e)
            // Handle gracefully - typing indicators are not critical
            Result.success(Unit)
        }
    }

    /**
     * Gets a flow of users currently typing in a chat.
     *
     * @param chatId ID of the chat
     * @return Flow emitting list of user IDs who are currently typing
     */
    fun getTypingUsers(chatId: String): Flow<List<String>> = callbackFlow {
        if (getCurrentUserId().isEmpty()) {
            Log.w(TAG, "getTypingUsers: User not authenticated")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "getTypingUsers: Setting up listener for chat $chatId")

        val listener =
                firestore
                        .collection(CHATS_COLLECTION)
                        .document(chatId)
                        .collection(TYPING_STATUS_COLLECTION)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(
                                        TAG,
                                        "getTypingUsers: Error listening to typing status",
                                        error
                                )

                                // Handle permission errors gracefully
                                if (error is FirebaseFirestoreException &&
                                                error.code ==
                                                        FirebaseFirestoreException.Code
                                                                .PERMISSION_DENIED
                                ) {
                                    Log.w(
                                            TAG,
                                            "getTypingUsers: Permission denied, sending empty list"
                                    )
                                    trySend(emptyList())
                                    return@addSnapshotListener
                                }

                                // For other errors, send empty list and continue
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            if (snapshot == null) {
                                Log.w(TAG, "getTypingUsers: Snapshot is null")
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            try {
                                val currentTime = Date()
                                val typingUsers =
                                        snapshot.documents.mapNotNull { doc ->
                                            try {
                                                val userId = doc.getString("userId")
                                                val isTyping = doc.getBoolean("isTyping") ?: false
                                                // Fix: Use getTimestamp instead of getDate for
                                                // proper Firestore Timestamp parsing
                                                val timestamp =
                                                        doc.getTimestamp("timestamp")?.toDate()

                                                // Only include users who are typing and updated
                                                // within last 10 seconds (increased from 5 for
                                                // better UX)
                                                // Exclude current user from the list
                                                if (userId != null &&
                                                                userId != getCurrentUserId() &&
                                                                isTyping &&
                                                                timestamp != null &&
                                                                (currentTime.time -
                                                                        timestamp.time) < 10000
                                                ) {
                                                    userId
                                                } else {
                                                    null
                                                }
                                            } catch (e: Exception) {
                                                Log.e(
                                                        TAG,
                                                        "getTypingUsers: Error parsing typing status document ${doc.id}",
                                                        e
                                                )
                                                // Return null to filter out this document without
                                                // crashing
                                                null
                                            }
                                        }

                                Log.d(TAG, "getTypingUsers: ${typingUsers.size} users typing")
                                trySend(typingUsers)
                            } catch (e: Exception) {
                                Log.e(TAG, "getTypingUsers: Error processing typing status", e)
                                trySend(emptyList())
                            }
                        }

        awaitClose {
            Log.d(TAG, "getTypingUsers: Closing listener")
            listener.remove()
        }
    }

    /**
     * Clears the typing status for the current user in a chat. This should be called when the user
     * stops typing or sends a message.
     *
     * @param chatId ID of the chat
     * @return Result indicating success or failure
     */
    suspend fun clearTypingStatus(chatId: String): Result<Unit> {
        return setTypingStatus(chatId, false)
    }
}
