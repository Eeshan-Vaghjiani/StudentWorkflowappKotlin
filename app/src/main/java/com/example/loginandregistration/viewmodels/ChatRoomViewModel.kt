package com.example.loginandregistration.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.utils.ConnectionMonitor
import com.example.loginandregistration.utils.MessageValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatRoomViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ChatRoomViewModel"
    }

    private val chatRepository = ChatRepository(context = application.applicationContext)
    private val connectionMonitor = ConnectionMonitor(application.applicationContext)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var currentChatId: String? = null
    private var hasMoreMessages = true
    private var isLoadingMoreMessages = false

    init {
        Log.d(TAG, "ChatRoomViewModel initialized")

        // Monitor connection status
        viewModelScope.launch {
            connectionMonitor.isConnected.collect { connected ->
                _isConnected.value = connected
                if (connected) {
                    // Connection restored, process queued messages
                    processQueuedMessages()
                }
            }
        }
    }

    fun getCurrentUserId(): String {
        return chatRepository.getCurrentUserId()
    }

    /** Retry loading the current chat after an error */
    fun retryLoadChat() {
        val chatId = currentChatId
        if (chatId != null) {
            Log.d(TAG, "Retrying to load chat $chatId")
            _error.value = null // Clear previous error
            loadChat(chatId)
        } else {
            Log.w(TAG, "Cannot retry loading chat - no chat ID available")
            _error.value = "Unable to retry. Please select a chat again."
        }
    }

    fun loadChat(chatId: String) {
        currentChatId = chatId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                chatRepository.getChatMessages(chatId).collect { messageList ->
                    try {
                        // Filter out null messages from Firestore
                        val validFirestoreMessages = messageList.filterNotNull()

                        // Log if null messages were found from Firestore
                        if (validFirestoreMessages.size < messageList.size) {
                            val nullCount = messageList.size - validFirestoreMessages.size
                            Log.w(
                                    TAG,
                                    "Found $nullCount null messages from Firestore - source: Firestore, chatId: $chatId, userId: ${getCurrentUserId()}, operation: LOAD"
                            )
                        }

                        // Merge Firestore messages with queued messages
                        val queuedMessages = chatRepository.getQueuedMessagesForChat(chatId)

                        // Combine and filter null messages
                        val combinedMessages =
                                (validFirestoreMessages + queuedMessages)
                                        .filterNotNull()
                                        .filter { message ->
                                            // Validate each message before adding to UI state
                                            val validationResult =
                                                    MessageValidator.validate(message)
                                            if (validationResult is
                                                            MessageValidator.ValidationResult.Invalid
                                            ) {
                                                Log.w(
                                                        TAG,
                                                        "Skipping invalid message ${message.id}: ${validationResult.errors.joinToString()}"
                                                )
                                                false
                                            } else {
                                                true
                                            }
                                        }
                                        .distinctBy { it.id }
                                        .sortedBy { it.timestamp }

                        _messages.value = combinedMessages
                        _isLoading.value = false

                        // Mark messages as read
                        val unreadMessages =
                                validFirestoreMessages.filter { message ->
                                    !message.isReadBy(getCurrentUserId()) &&
                                            !message.isFromUser(getCurrentUserId())
                                }

                        if (unreadMessages.isNotEmpty()) {
                            markMessagesAsRead(chatId, unreadMessages.map { it.id })
                        }
                    } catch (e: NullPointerException) {
                        Log.e(
                                TAG,
                                "NullPointerException while processing messages - chatId: $chatId, userId: ${getCurrentUserId()}, messageCount: ${messageList.size}, error: ${e.message}",
                                e
                        )
                        _error.value = "Unable to load some messages. Please try again."
                        _isLoading.value = false
                    }
                }
            } catch (e: NullPointerException) {
                Log.e(
                        TAG,
                        "NullPointerException while loading chat - chatId: $chatId, userId: ${getCurrentUserId()}, error: ${e.message}",
                        e
                )
                _error.value = "Unable to load messages. Please try again."
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(
                        TAG,
                        "Error loading messages - chatId: $chatId, userId: ${getCurrentUserId()}, error: ${e.message}",
                        e
                )
                _error.value = "Failed to load messages. Please try again."
                _isLoading.value = false
            }
        }

        // Listen to typing users
        viewModelScope.launch {
            try {
                chatRepository.getTypingUsers(chatId).collect { typingUserIds ->
                    _typingUsers.value = typingUserIds
                }
            } catch (e: Exception) {
                Log.e(
                        TAG,
                        "Error listening to typing users - chatId: $chatId, userId: ${getCurrentUserId()}, error: ${e.message}",
                        e
                )
            }
        }
    }

    suspend fun sendMessage(text: String) {
        val chatId = currentChatId ?: return

        if (text.isBlank()) {
            _error.value = "Message cannot be empty"
            return
        }

        // Task 3.2: Implement optimistic UI updates
        // 1. Create temporary message with SENDING status
        val tempMessageId = java.util.UUID.randomUUID().toString()
        
        // Get current user info for display
        val currentUserName = try {
            chatRepository.getCurrentUserId()
        } catch (e: Exception) {
            "You"
        }
        
        val tempMessage = Message(
            id = tempMessageId,
            chatId = chatId,
            senderId = getCurrentUserId(),
            senderName = currentUserName,
            text = text,
            timestamp = java.util.Date(),
            status = com.example.loginandregistration.models.MessageStatus.SENDING,
            readBy = listOf(getCurrentUserId())
        )

        // 2. Show message immediately in UI with SENDING status (optimistic update)
        _messages.value = _messages.value + tempMessage
        _isSending.value = true

        // 3. Send to Firestore in background coroutine
        viewModelScope.launch {
            try {
                val result = chatRepository.sendMessage(chatId, text)

                if (result.isSuccess) {
                    // 4. Update message status to SENT on success
                    val sentMessage = result.getOrNull()
                    if (sentMessage != null) {
                        // Replace temp message with the actual message from Firestore
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == tempMessageId) {
                                sentMessage.copy(status = com.example.loginandregistration.models.MessageStatus.SENT)
                            } else {
                                msg
                            }
                        }
                        Log.d(TAG, "Message sent successfully: ${sentMessage.id}")
                    } else {
                        // Remove temp message if we got success but no message back
                        _messages.value = _messages.value.filter { it.id != tempMessageId }
                        Log.w(TAG, "Message sent but no message returned from repository")
                    }
                } else {
                    // 5. Update message status to FAILED_RETRYABLE on error
                    val exception = result.exceptionOrNull()
                    val errorMessage = exception?.message ?: "Unknown error"
                    
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == tempMessageId) {
                            msg.copy(status = com.example.loginandregistration.models.MessageStatus.FAILED_RETRYABLE)
                        } else {
                            msg
                        }
                    }
                    
                    _error.value = "Failed to send message: $errorMessage"
                    Log.e(TAG, "Failed to send message: $errorMessage", exception)
                }
            } catch (e: Exception) {
                Log.e(
                        TAG,
                        "Error sending message - chatId: $chatId, userId: ${getCurrentUserId()}, textLength: ${text.length}, error: ${e.message}",
                        e
                )
                // 6. Update message status to FAILED_RETRYABLE on exception
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == tempMessageId) {
                        msg.copy(status = com.example.loginandregistration.models.MessageStatus.FAILED_RETRYABLE)
                    } else {
                        msg
                    }
                }
                _error.value = "Failed to send message: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    /**
     * Sends an image message to the current chat.
     *
     * @param imageUri URI of the image to send
     * @param onProgress Callback for upload progress (0-100)
     */
    suspend fun sendImageMessage(imageUri: android.net.Uri, onProgress: (Int) -> Unit = {}) {
        val chatId = currentChatId ?: return

        _isSending.value = true

        try {
            val result = chatRepository.sendImageMessage(chatId, imageUri, onProgress)

            if (result.isFailure) {
                _error.value = "Failed to send image: ${result.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error sending image message - chatId: $chatId, userId: ${getCurrentUserId()}, imageUri: $imageUri, error: ${e.message}",
                    e
            )
            _error.value = "Failed to send image: ${e.message}"
        } finally {
            _isSending.value = false
        }
    }

    /**
     * Sends a document message to the current chat.
     *
     * @param documentUri URI of the document to send
     * @param onProgress Callback for upload progress (0-100)
     */
    suspend fun sendDocumentMessage(documentUri: android.net.Uri, onProgress: (Int) -> Unit = {}) {
        val chatId = currentChatId ?: return

        _isSending.value = true

        try {
            val result = chatRepository.sendDocumentMessage(chatId, documentUri, onProgress)

            if (result.isFailure) {
                _error.value = "Failed to send document: ${result.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error sending document message - chatId: $chatId, userId: ${getCurrentUserId()}, documentUri: $documentUri, error: ${e.message}",
                    e
            )
            _error.value = "Failed to send document: ${e.message}"
        } finally {
            _isSending.value = false
        }
    }

    private fun markMessagesAsRead(chatId: String, messageIds: List<String>) {
        viewModelScope.launch {
            try {
                chatRepository.markMessagesAsRead(chatId, messageIds)
            } catch (e: Exception) {
                Log.e(
                        TAG,
                        "Error marking messages as read - chatId: $chatId, userId: ${getCurrentUserId()}, messageCount: ${messageIds.size}, error: ${e.message}",
                        e
                )
            }
        }
    }

    /** Mark all messages in the current chat as read (called when opening from notification) */
    suspend fun markAllMessagesAsRead() {
        val chatId = currentChatId ?: return

        try {
            val unreadMessages =
                    _messages.value.filter { message ->
                        !message.isReadBy(getCurrentUserId()) &&
                                !message.isFromUser(getCurrentUserId())
                    }

            if (unreadMessages.isNotEmpty()) {
                markMessagesAsRead(chatId, unreadMessages.map { it.id })
            }
        } catch (e: Exception) {
            val unreadCount =
                    try {
                        _messages.value.count {
                            !it.isReadBy(getCurrentUserId()) && !it.isFromUser(getCurrentUserId())
                        }
                    } catch (ex: Exception) {
                        0
                    }
            Log.e(
                    TAG,
                    "Error marking all messages as read - chatId: $chatId, userId: ${getCurrentUserId()}, unreadCount: $unreadCount, error: ${e.message}",
                    e
            )
        }
    }

    fun updateTypingStatus(isTyping: Boolean) {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            try {
                val result = chatRepository.setTypingStatus(chatId, isTyping)
                if (result.isFailure) {
                    // Log but don't show error to user - typing indicators are not critical
                    Log.w(
                            TAG,
                            "Failed to update typing status: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                // Log but don't show error to user - typing indicators are not critical
                Log.w(TAG, "Error updating typing status", e)
            }
        }
    }

    fun loadMoreMessages() {
        val chatId = currentChatId ?: return

        // Prevent duplicate loading
        if (isLoadingMoreMessages || !hasMoreMessages) {
            return
        }

        val currentMessages = _messages.value
        if (currentMessages.isEmpty()) {
            return
        }

        val oldestMessage = currentMessages.first()

        isLoadingMoreMessages = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            try {
                val result = chatRepository.loadMoreMessages(chatId, oldestMessage, 50)

                if (result.isSuccess) {
                    val olderMessages = result.getOrNull() ?: emptyList()

                    if (olderMessages.isEmpty()) {
                        hasMoreMessages = false
                    } else {
                        // Prepend older messages to the beginning of the list
                        _messages.value = olderMessages + currentMessages
                    }
                } else {
                    _error.value =
                            "Failed to load more messages: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                Log.e(
                        TAG,
                        "Error loading more messages - chatId: $chatId, userId: ${getCurrentUserId()}, oldestMessageId: ${oldestMessage.id}, error: ${e.message}",
                        e
                )
                _error.value = "Failed to load more messages: ${e.message}"
            } finally {
                isLoadingMoreMessages = false
                _isLoadingMore.value = false
            }
        }
    }

    /** 
     * Retry sending a failed message with improved error handling and status updates.
     * Task 3.2: Add retry logic for failed messages
     */
    fun retryMessage(message: Message) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Retrying message ${message.id} in chat ${message.chatId}")
                
                // 1. Update message status to SENDING (optimistic update)
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == message.id) {
                        msg.copy(status = com.example.loginandregistration.models.MessageStatus.SENDING)
                    } else {
                        msg
                    }
                }

                _isSending.value = true
                
                // 2. Attempt to retry the message through repository
                val result = chatRepository.retryMessage(message)

                if (result.isSuccess) {
                    // 3. Update message status to SENT on success
                    val sentMessage = result.getOrNull()
                    if (sentMessage != null) {
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == message.id) {
                                sentMessage.copy(status = com.example.loginandregistration.models.MessageStatus.SENT)
                            } else {
                                msg
                            }
                        }
                        Log.d(TAG, "Message ${message.id} retried successfully")
                    } else {
                        // If no message returned, keep original with SENT status
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == message.id) {
                                msg.copy(status = com.example.loginandregistration.models.MessageStatus.SENT)
                            } else {
                                msg
                            }
                        }
                    }
                } else {
                    // 4. Update message status back to FAILED_RETRYABLE on failure
                    val exception = result.exceptionOrNull()
                    val errorMessage = exception?.message ?: "Unknown error"
                    
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == message.id) {
                            msg.copy(status = com.example.loginandregistration.models.MessageStatus.FAILED_RETRYABLE)
                        } else {
                            msg
                        }
                    }
                    
                    _error.value = "Failed to retry message: $errorMessage"
                    Log.e(TAG, "Failed to retry message ${message.id}: $errorMessage", exception)
                }
            } catch (e: Exception) {
                Log.e(
                        TAG,
                        "Error retrying message - messageId: ${message.id}, chatId: ${message.chatId}, userId: ${getCurrentUserId()}, error: ${e.message}",
                        e
                )
                // 5. Update message status back to FAILED_RETRYABLE on exception
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == message.id) {
                        msg.copy(status = com.example.loginandregistration.models.MessageStatus.FAILED_RETRYABLE)
                    } else {
                        msg
                    }
                }
                _error.value = "Failed to retry message: ${e.message}"
            } finally {
                _isSending.value = false
            }
        }
    }

    /** Process all queued messages when connection is restored */
    private fun processQueuedMessages() {
        viewModelScope.launch {
            try {
                val chatId = currentChatId
                if (chatId == null) {
                    Log.d(TAG, "No chat loaded, skipping queued message processing")
                    return@launch
                }

                val result = chatRepository.processQueuedMessages()
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    if (count > 0) {
                        Log.d(TAG, "Successfully processed $count queued messages")
                    }
                } else {
                    Log.e(
                            TAG,
                            "Failed to process queued messages: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: NullPointerException) {
                val chatId = currentChatId
                Log.e(
                        TAG,
                        "NullPointerException while processing queued messages - chatId: $chatId, userId: ${getCurrentUserId()}, error: ${e.message}",
                        e
                )
                // Don't crash - just log the error
            } catch (e: Exception) {
                val chatId = currentChatId
                Log.e(
                        TAG,
                        "Error processing queued messages - chatId: $chatId, userId: ${getCurrentUserId()}, error: ${e.message}",
                        e
                )
                // Don't crash - just log the error
            }
        }
    }

    /** Get queued messages for current chat */
    fun getQueuedMessages(): List<Message> {
        val chatId = currentChatId ?: return emptyList()
        return chatRepository.getQueuedMessagesForChat(chatId)
    }

    /**
     * Deletes a message from the current chat. Only the sender can delete their own messages.
     *
     * @param message Message to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteMessage(message: Message): Result<Unit> {
        val chatId = currentChatId ?: return Result.failure(Exception("No chat loaded"))

        return try {
            val result = chatRepository.deleteMessage(chatId, message)

            if (result.isSuccess) {
                // Remove message from local state immediately for better UX
                _messages.value = _messages.value.filter { it.id != message.id }
                Log.d(TAG, "Message deleted successfully")
            } else {
                _error.value = "Failed to delete message: ${result.exceptionOrNull()?.message}"
            }

            result
        } catch (e: Exception) {
            Log.e(
                    TAG,
                    "Error deleting message - messageId: ${message.id}, chatId: $chatId, userId: ${getCurrentUserId()}, error: ${e.message}",
                    e
            )
            _error.value = "Failed to delete message: ${e.message}"
            Result.failure(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ChatRoomViewModel cleared - all coroutines and listeners will be cancelled")
        // viewModelScope automatically cancels all coroutines when ViewModel is cleared
        // This includes all Flow collectors (messages, typing users, connection status)
    }
}
