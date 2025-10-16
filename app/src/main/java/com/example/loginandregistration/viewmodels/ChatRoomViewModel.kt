package com.example.loginandregistration.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.utils.ConnectionMonitor
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

    fun loadChat(chatId: String) {
        currentChatId = chatId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                chatRepository.getChatMessages(chatId).collect { messageList ->
                    // Merge Firestore messages with queued messages
                    val queuedMessages = chatRepository.getQueuedMessagesForChat(chatId)
                    val allMessages =
                            (messageList + queuedMessages).distinctBy { it.id }.sortedBy {
                                it.timestamp
                            }

                    _messages.value = allMessages
                    _isLoading.value = false

                    // Mark messages as read
                    val unreadMessages =
                            messageList.filter { message ->
                                !message.isReadBy(getCurrentUserId()) &&
                                        !message.isFromUser(getCurrentUserId())
                            }

                    if (unreadMessages.isNotEmpty()) {
                        markMessagesAsRead(chatId, unreadMessages.map { it.id })
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages", e)
                _error.value = "Failed to load messages: ${e.message}"
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
                Log.e(TAG, "Error listening to typing users", e)
            }
        }
    }

    suspend fun sendMessage(text: String) {
        val chatId = currentChatId ?: return

        if (text.isBlank()) {
            _error.value = "Message cannot be empty"
            return
        }

        _isSending.value = true

        try {
            val result = chatRepository.sendMessage(chatId, text)

            if (result.isFailure) {
                _error.value = "Failed to send message: ${result.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            _error.value = "Failed to send message: ${e.message}"
        } finally {
            _isSending.value = false
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
            Log.e(TAG, "Error sending image message", e)
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
            Log.e(TAG, "Error sending document message", e)
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
                Log.e(TAG, "Error marking messages as read", e)
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
            Log.e(TAG, "Error marking all messages as read", e)
        }
    }

    fun updateTypingStatus(isTyping: Boolean) {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            try {
                chatRepository.updateTypingStatus(chatId, isTyping)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating typing status", e)
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
                Log.e(TAG, "Error loading more messages", e)
                _error.value = "Failed to load more messages: ${e.message}"
            } finally {
                isLoadingMoreMessages = false
                _isLoadingMore.value = false
            }
        }
    }

    /** Retry sending a failed message */
    fun retryMessage(message: Message) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                val result = chatRepository.retryMessage(message)

                if (result.isFailure) {
                    _error.value = "Failed to retry message: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying message", e)
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
                val result = chatRepository.processQueuedMessages()
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    if (count > 0) {
                        Log.d(TAG, "Successfully sent $count queued messages")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing queued messages", e)
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
            Log.e(TAG, "Error deleting message", e)
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
