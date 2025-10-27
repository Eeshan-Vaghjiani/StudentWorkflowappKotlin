package com.example.loginandregistration.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** Represents a chat conversation (either direct or group) */
data class Chat(
        @DocumentId var chatId: String = "",
        val type: ChatType = ChatType.DIRECT,
        val participants: List<String> = emptyList(),
        val participantDetails: Map<String, UserInfo> = emptyMap(),
        val lastMessage: String = "",
        @ServerTimestamp val lastMessageTime: Date? = null,
        val lastMessageSenderId: String = "",
        val unreadCount: Map<String, Int> = emptyMap(),
        val groupId: String? = null,
        val groupName: String? = null,
        @ServerTimestamp val createdAt: Date? = null
) {
    /** Get unread count for specific user */
    fun getUnreadCountForUser(userId: String): Int {
        return unreadCount[userId] ?: 0
    }

    /** Get chat display name for a specific user */
    fun getDisplayName(currentUserId: String): String {
        return when (type) {
            ChatType.GROUP -> groupName ?: "Group Chat"
            ChatType.DIRECT -> {
                // For direct chats, show the other participant's name
                val otherParticipant = participants.firstOrNull { it != currentUserId }
                participantDetails[otherParticipant]?.displayName ?: "Unknown User"
            }
        }
    }

    /** Get chat display image URL for a specific user */
    fun getDisplayImageUrl(currentUserId: String): String {
        return when (type) {
            ChatType.GROUP -> "" // Groups don't have profile pictures (could add group icon later)
            ChatType.DIRECT -> {
                val otherParticipant = participants.firstOrNull { it != currentUserId }
                participantDetails[otherParticipant]?.profileImageUrl ?: ""
            }
        }
    }

    /** Check if user is participant */
    fun isParticipant(userId: String): Boolean {
        return participants.contains(userId)
    }
}

/** Type of chat conversation */
enum class ChatType {
    DIRECT, // One-on-one chat
    GROUP // Group chat
}

/** User information for chat participants */
data class UserInfo(
        val userId: String = "",
        val displayName: String = "",
        val email: String = "",
        val profileImageUrl: String = "",
        val initials: String = "",
        val online: Boolean = false,
        val lastSeen: Date? = null
)
