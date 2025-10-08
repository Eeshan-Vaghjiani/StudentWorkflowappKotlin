package com.example.loginandregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.R
import com.example.loginandregistration.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
        private val currentUserId: String,
        private val onRetryMessage: ((Message) -> Unit)? = null
) : ListAdapter<MessageAdapter.MessageItem, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_TIMESTAMP_HEADER = 3
    }

    sealed class MessageItem {
        data class MessageData(val message: Message, val showSenderInfo: Boolean) : MessageItem()
        data class TimestampHeader(val timestamp: String) : MessageItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is MessageItem.MessageData -> {
                if (item.message.senderId == currentUserId) {
                    VIEW_TYPE_SENT
                } else {
                    VIEW_TYPE_RECEIVED
                }
            }
            is MessageItem.TimestampHeader -> VIEW_TYPE_TIMESTAMP_HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = inflater.inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = inflater.inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            VIEW_TYPE_TIMESTAMP_HEADER -> {
                val view = inflater.inflate(R.layout.item_timestamp_header, parent, false)
                TimestampHeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is MessageItem.MessageData -> {
                when (holder) {
                    is SentMessageViewHolder -> holder.bind(item.message, onRetryMessage)
                    is ReceivedMessageViewHolder -> holder.bind(item.message, item.showSenderInfo)
                }
            }
            is MessageItem.TimestampHeader -> {
                (holder as TimestampHeaderViewHolder).bind(item.timestamp)
            }
        }
    }

    fun submitMessages(messages: List<Message>) {
        val items = mutableListOf<MessageItem>()
        var lastTimestampHeader: String? = null
        var lastSenderId: String? = null
        var lastMessageTime: Long = 0

        messages.forEach { message ->
            val timestamp = message.timestamp?.time ?: 0
            val timestampHeader = getTimestampHeader(timestamp)

            // Add timestamp header if it's different from the last one
            if (timestampHeader != lastTimestampHeader) {
                items.add(MessageItem.TimestampHeader(timestampHeader))
                lastTimestampHeader = timestampHeader
            }

            // Determine if we should show sender info
            // Show if: different sender OR more than 5 minutes since last message
            val showSenderInfo =
                    message.senderId != currentUserId &&
                            (message.senderId != lastSenderId ||
                                    (timestamp - lastMessageTime) > 5 * 60 * 1000)

            items.add(MessageItem.MessageData(message, showSenderInfo))

            lastSenderId = message.senderId
            lastMessageTime = timestamp
        }

        submitList(items)
    }

    private fun getTimestampHeader(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(messageDate, today) -> "Today"
            isSameDay(messageDate, yesterday) -> "Yesterday"
            else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val readReceiptImageView: ImageView =
                itemView.findViewById(R.id.readReceiptImageView)

        fun bind(message: Message, onRetryMessage: ((Message) -> Unit)?) {
            messageTextView.text = message.text
            timestampTextView.text = formatTime(message.timestamp?.time ?: 0)

            // Update read receipt icon based on message status
            updateReadReceipt(message)

            // Set click listener for failed messages
            if (message.status == com.example.loginandregistration.models.MessageStatus.FAILED) {
                itemView.setOnClickListener { onRetryMessage?.invoke(message) }
                itemView.isClickable = true
            } else {
                itemView.setOnClickListener(null)
                itemView.isClickable = false
            }
        }

        private fun updateReadReceipt(message: Message) {
            when {
                message.status == com.example.loginandregistration.models.MessageStatus.SENDING -> {
                    // Sending: show clock icon
                    readReceiptImageView.setImageResource(android.R.drawable.ic_menu_recent_history)
                    readReceiptImageView.setColorFilter(
                            itemView.context.getColor(android.R.color.white)
                    )
                    readReceiptImageView.visibility = View.VISIBLE
                }
                message.status == com.example.loginandregistration.models.MessageStatus.FAILED -> {
                    // Failed: show error icon
                    readReceiptImageView.setImageResource(android.R.drawable.ic_dialog_alert)
                    readReceiptImageView.setColorFilter(
                            itemView.context.getColor(android.R.color.holo_red_light)
                    )
                    readReceiptImageView.visibility = View.VISIBLE
                }
                message.readBy.size > 1 -> {
                    // Read by others: double checkmark (blue)
                    readReceiptImageView.setImageResource(R.drawable.ic_check_double)
                    readReceiptImageView.setColorFilter(
                            itemView.context.getColor(android.R.color.holo_blue_light)
                    )
                    readReceiptImageView.visibility = View.VISIBLE
                }
                message.status ==
                        com.example.loginandregistration.models.MessageStatus.DELIVERED -> {
                    // Delivered: double checkmark (gray)
                    readReceiptImageView.setImageResource(R.drawable.ic_check_double)
                    readReceiptImageView.setColorFilter(
                            itemView.context.getColor(android.R.color.white)
                    )
                    readReceiptImageView.visibility = View.VISIBLE
                }
                message.status == com.example.loginandregistration.models.MessageStatus.SENT -> {
                    // Sent: single checkmark
                    readReceiptImageView.setImageResource(R.drawable.ic_check)
                    readReceiptImageView.setColorFilter(
                            itemView.context.getColor(android.R.color.white)
                    )
                    readReceiptImageView.visibility = View.VISIBLE
                }
                else -> {
                    readReceiptImageView.visibility = View.GONE
                }
            }
        }

        private fun formatTime(timestamp: Long): String {
            return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderProfileImageView: ImageView =
                itemView.findViewById(R.id.senderProfileImageView)
        private val senderAvatarTextView: TextView =
                itemView.findViewById(R.id.senderAvatarTextView)
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: Message, showSenderInfo: Boolean) {
            messageTextView.text = message.text
            timestampTextView.text = formatTime(message.timestamp?.time ?: 0)

            if (showSenderInfo) {
                senderNameTextView.visibility = View.VISIBLE
                senderNameTextView.text = message.senderName

                // Show avatar with initials
                senderAvatarTextView.text = getInitials(message.senderName)
                senderAvatarTextView.visibility = View.VISIBLE
                senderProfileImageView.visibility = View.GONE

                // TODO: Load profile image if available using Coil
                // if (message.senderImageUrl.isNotEmpty()) {
                //     senderProfileImageView.load(message.senderImageUrl)
                //     senderProfileImageView.visibility = View.VISIBLE
                //     senderAvatarTextView.visibility = View.GONE
                // }
            } else {
                senderNameTextView.visibility = View.GONE
                senderAvatarTextView.visibility = View.INVISIBLE
                senderProfileImageView.visibility = View.INVISIBLE
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

        private fun formatTime(timestamp: Long): String {
            return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
    }

    class TimestampHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timestampHeaderTextView: TextView =
                itemView.findViewById(R.id.timestampHeaderTextView)

        fun bind(timestamp: String) {
            timestampHeaderTextView.text = timestamp
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return when {
                oldItem is MessageItem.MessageData && newItem is MessageItem.MessageData ->
                        oldItem.message.id == newItem.message.id
                oldItem is MessageItem.TimestampHeader && newItem is MessageItem.TimestampHeader ->
                        oldItem.timestamp == newItem.timestamp
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem == newItem
        }
    }
}
