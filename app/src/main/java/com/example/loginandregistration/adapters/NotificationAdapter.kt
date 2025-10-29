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
import com.example.loginandregistration.models.Notification
import com.example.loginandregistration.models.NotificationType
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(private val onNotificationClick: (Notification) -> Unit) :
        ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(
                NotificationDiffCallback()
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onNotificationClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
            itemView: View,
            private val onNotificationClick: (Notification) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val iconImageView: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        private val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        private val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        fun bind(notification: Notification) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            timestampTextView.text = formatTimestamp(notification.timestamp.toDate().time)

            // Show/hide unread indicator
            unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            // Set icon based on notification type
            val iconRes =
                    when (notification.type) {
                        NotificationType.TASK_ASSIGNED -> R.drawable.ic_tasks
                        NotificationType.TASK_COMPLETED -> R.drawable.ic_tasks
                        NotificationType.TASK_DUE_SOON -> R.drawable.ic_calendar
                        NotificationType.GROUP_INVITE -> R.drawable.ic_groups
                        NotificationType.GROUP_JOINED -> R.drawable.ic_groups
                        NotificationType.NEW_MESSAGE -> R.drawable.ic_chat
                        NotificationType.MENTION -> R.drawable.ic_chat
                        NotificationType.SYSTEM -> android.R.drawable.ic_dialog_info
                    }
            iconImageView.setImageResource(iconRes)

            // Set background alpha based on read status
            itemView.alpha = if (notification.isRead) 0.6f else 1.0f

            itemView.setOnClickListener { onNotificationClick(notification) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> "${diff / 60_000}m ago"
                diff < 86400_000 -> "${diff / 3600_000}h ago"
                diff < 604800_000 -> "${diff / 86400_000}d ago"
                else -> {
                    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                    sdf.format(timestamp)
                }
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}
