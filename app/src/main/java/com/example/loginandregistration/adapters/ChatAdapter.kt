package com.example.loginandregistration.adapters

import android.graphics.Color
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

                // Generate avatar with initials
                val displayName = chat.getDisplayName(currentUserId)
                val initials = getInitials(displayName)
                avatarTextView.text = initials

                // Generate color based on chat ID for consistency
                val color = generateColorFromString(chat.chatId)
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

        private fun getInitials(name: String): String {
            val names = name.trim().split(" ")
            return when {
                names.size >= 2 -> "${names[0].first()}${names[1].first()}".uppercase()
                names.isNotEmpty() -> names[0].take(2).uppercase()
                else -> "?"
            }
        }

        private fun generateColorFromString(str: String): Int {
            // Generate a consistent color based on the string
            val hash = str.hashCode()
            val colors =
                    listOf(
                            Color.parseColor("#FF6B6B"), // Red
                            Color.parseColor("#4ECDC4"), // Teal
                            Color.parseColor("#45B7D1"), // Blue
                            Color.parseColor("#FFA07A"), // Orange
                            Color.parseColor("#98D8C8"), // Mint
                            Color.parseColor("#F7DC6F"), // Yellow
                            Color.parseColor("#BB8FCE"), // Purple
                            Color.parseColor("#85C1E2"), // Sky Blue
                            Color.parseColor("#F8B88B"), // Peach
                            Color.parseColor("#A8E6CF") // Light Green
                    )
            return colors[Math.abs(hash) % colors.size]
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
