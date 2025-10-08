package com.example.loginandregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.loginandregistration.R
import com.example.loginandregistration.models.Chat
import com.example.loginandregistration.utils.DefaultAvatarGenerator
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val currentUserId: String, private val onChatClick: (Chat) -> Unit) :
        ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view, currentUserId, onChatClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(
            itemView: View,
            private val currentUserId: String,
            private val onChatClick: (Chat) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val avatarTextView: TextView = itemView.findViewById(R.id.avatarTextView)
        private val chatNameTextView: TextView = itemView.findViewById(R.id.chatNameTextView)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val unreadBadgeTextView: TextView = itemView.findViewById(R.id.unreadBadgeTextView)

        fun bind(chat: Chat) {
            // Set chat name
            chatNameTextView.text = chat.getDisplayName(currentUserId)

            // Set last message
            lastMessageTextView.text =
                    if (chat.lastMessage.isNotEmpty()) {
                        chat.lastMessage
                    } else {
                        "No messages yet"
                    }

            // Set timestamp
            timestampTextView.text = formatTimestamp(chat.lastMessageTime)

            // Set unread count
            val unreadCount = chat.getUnreadCountForUser(currentUserId)
            if (unreadCount > 0) {
                unreadBadgeTextView.visibility = View.VISIBLE
                unreadBadgeTextView.text = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                unreadBadgeTextView.visibility = View.GONE
            }

            // Set profile image or avatar
            val imageUrl = chat.getDisplayImageUrl(currentUserId)
            if (imageUrl.isNotEmpty()) {
                profileImageView.visibility = View.VISIBLE
                avatarTextView.visibility = View.GONE
                profileImageView.load(imageUrl) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.circle_background)
                    error(R.drawable.circle_background)
                }
            } else {
                profileImageView.visibility = View.GONE
                avatarTextView.visibility = View.VISIBLE

                // Generate avatar with initials using DefaultAvatarGenerator
                val displayName = chat.getDisplayName(currentUserId)
                val initials = DefaultAvatarGenerator.getInitials(displayName)
                avatarTextView.text = initials

                // Generate color based on chat ID for consistency
                val color = DefaultAvatarGenerator.generateColorFromString(chat.chatId)
                avatarTextView.setBackgroundColor(color)
            }

            // Set click listener
            itemView.setOnClickListener { onChatClick(chat) }
        }

        private fun formatTimestamp(date: Date?): String {
            if (date == null) return ""

            val now = Calendar.getInstance()
            val messageTime = Calendar.getInstance().apply { time = date }

            return when {
                // Today - show time
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                }
                // Yesterday
                now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1 &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    "Yesterday"
                }
                // This week - show day name
                now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) &&
                        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                }
                // This year - show date without year
                now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
                }
                // Older - show full date
                else -> {
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
                }
            }
        }


    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}
