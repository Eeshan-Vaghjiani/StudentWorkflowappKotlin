package com.example.loginandregistration.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.models.Chat
import com.example.loginandregistration.models.ChatType
import com.example.loginandregistration.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository = ChatRepository()) : ViewModel() {

    // All chats from repository
    private val allChats =
            chatRepository
                    .getUserChats()
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected tab (0 = All, 1 = Groups, 2 = Direct)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Filtered chats based on search and tab
    val filteredChats: StateFlow<List<Chat>> =
            combine(allChats, searchQuery, selectedTab) { chats, query, tab ->
                        var filtered = chats

                        // Filter by tab
                        filtered =
                                when (tab) {
                                    1 -> filtered.filter { it.type == ChatType.GROUP }
                                    2 -> filtered.filter { it.type == ChatType.DIRECT }
                                    else -> filtered // All
                                }

                        // Filter by search query
                        if (query.isNotBlank()) {
                            val queryLower = query.lowercase()
                            filtered =
                                    filtered.filter { chat ->
                                        chat.getDisplayName(chatRepository.getCurrentUserId())
                                                .lowercase()
                                                .contains(queryLower) ||
                                                chat.lastMessage.lowercase().contains(queryLower)
                                    }
                        }

                        filtered
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            // The Flow will automatically update with new data
            kotlinx.coroutines.delay(500) // Small delay for UX
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
