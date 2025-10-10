package com.example.loginandregistration.adapters

import android.content.Intent
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
import com.example.loginandregistration.ImageViewerActivity
import com.example.loginandregistration.R
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.utils.AnimationUtils
import com.example.loginandregistration.utils.DefaultAvatarGenerator
import com.example.loginandregistration.utils.LinkifyHelper
import com.example.loginandregistration.utils.MessageGrouper
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
        private val currentUserId: String,
        private val onRetryMessage: ((Message) -> Unit)? = null,
        private val onDocumentClick: ((Message) -> Unit)? = null,
        private val onMessageLongClick: ((Message, View) -> Unit)? = null
) : ListAdapter<MessageGrouper.MessageItem, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_TIMESTAMP_HEADER = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is MessageGrouper.MessageItem.MessageData -> {
                if (item.message.senderId == currentUserId) {
                    VIEW_TYPE_SENT
                } else {
                    VIEW_TYPE_RECEIVED
                }
            }
            is MessageGrouper.MessageItem.TimestampHeader -> VIEW_TYPE_TIMESTAMP_HEADER
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
            is MessageGrouper.MessageItem.MessageData -> {
                when (holder) {
                    is SentMessageViewHolder ->
                            holder.bind(
                                    item.message,
                                    onRetryMessage,
                                    onDocumentClick,
                                    onMessageLongClick
                            )
                    is ReceivedMessageViewHolder ->
                            holder.bind(
                                    item.message,
                                    item.showSenderInfo,
                                    onDocumentClick,
                                    onMessageLongClick
                            )
                }
                // Add fade-in animation for new messages
                AnimationUtils.fadeIn(holder.itemView, duration = 200)
            }
            is MessageGrouper.MessageItem.TimestampHeader -> {
                (holder as TimestampHeaderViewHolder).bind(item.timestamp)
            }
        }
    }

    /**
     * Submit messages to the adapter with proper grouping. Uses MessageGrouper utility to group
     * consecutive messages from the same sender and add timestamp headers.
     */
    fun submitMessages(messages: List<Message>) {
        val groupedItems = MessageGrouper.groupMessages(messages, currentUserId)
        submitList(groupedItems)
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageImageView: ImageView = itemView.findViewById(R.id.messageImageView)
        private val documentContainer: View = itemView.findViewById(R.id.documentContainer)
        private val documentIconImageView: ImageView =
                itemView.findViewById(R.id.documentIconImageView)
        private val documentNameTextView: TextView =
                itemView.findViewById(R.id.documentNameTextView)
        private val documentSizeTextView: TextView =
                itemView.findViewById(R.id.documentSizeTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val messageStatusView: com.example.loginandregistration.views.MessageStatusView =
                itemView.findViewById(R.id.messageStatusView)

        fun bind(
                message: Message,
                onRetryMessage: ((Message) -> Unit)?,
                onDocumentClick: ((Message) -> Unit)?,
                onMessageLongClick: ((Message, View) -> Unit)?
        ) {
            // Handle document messages
            if (message.hasDocument()) {
                documentContainer.visibility = View.VISIBLE
                messageImageView.visibility = View.GONE
                messageTextView.visibility =
                        if (message.text.isNotEmpty()) View.VISIBLE else View.GONE

                documentNameTextView.text = message.documentName ?: "Document"
                documentSizeTextView.text = message.getFormattedFileSize()

                // Set icon based on file type
                documentIconImageView.setImageResource(getDocumentIcon(message.documentName ?: ""))

                // Click to download/open document
                documentContainer.setOnClickListener { onDocumentClick?.invoke(message) }
            }
            // Handle image messages
            else if (message.hasImage()) {
                messageImageView.visibility = View.VISIBLE
                documentContainer.visibility = View.GONE
                messageTextView.visibility =
                        if (message.text.isNotEmpty()) View.VISIBLE else View.GONE

                // Load image using Coil
                messageImageView.load(message.imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }

                // Click to view full screen
                messageImageView.setOnClickListener {
                    val intent =
                            Intent(itemView.context, ImageViewerActivity::class.java).apply {
                                putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, message.imageUrl)
                            }
                    itemView.context.startActivity(intent)
                }
            } else {
                messageImageView.visibility = View.GONE
                documentContainer.visibility = View.GONE
                messageTextView.visibility = View.VISIBLE
            }

            // Make URLs clickable in message text
            if (message.text.isNotEmpty()) {
                LinkifyHelper.makeLinksClickable(messageTextView, message.text)
            } else {
                messageTextView.text = message.text
            }
            timestampTextView.text = formatTime(message.timestamp?.time ?: 0)

            // Update message status view
            messageStatusView.setMessage(message, message.senderId)
            messageStatusView.setOnRetryClickListener { onRetryMessage?.invoke(message) }

            // Set long-click listener on the entire item view
            itemView.setOnLongClickListener {
                onMessageLongClick?.invoke(message, it)
                true
            }
        }

        private fun getDocumentIcon(fileName: String): Int {
            return when (fileName.substringAfterLast('.', "").lowercase()) {
                "pdf" -> android.R.drawable.ic_menu_save
                "doc", "docx" -> android.R.drawable.ic_menu_edit
                "xls", "xlsx" -> android.R.drawable.ic_menu_sort_by_size
                "ppt", "pptx" -> android.R.drawable.ic_menu_slideshow
                "zip", "rar" -> android.R.drawable.ic_menu_upload
                "txt" -> android.R.drawable.ic_menu_edit
                else -> android.R.drawable.ic_menu_save
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
        private val messageImageView: ImageView = itemView.findViewById(R.id.messageImageView)
        private val documentContainer: View = itemView.findViewById(R.id.documentContainer)
        private val documentIconImageView: ImageView =
                itemView.findViewById(R.id.documentIconImageView)
        private val documentNameTextView: TextView =
                itemView.findViewById(R.id.documentNameTextView)
        private val documentSizeTextView: TextView =
                itemView.findViewById(R.id.documentSizeTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(
                message: Message,
                showSenderInfo: Boolean,
                onDocumentClick: ((Message) -> Unit)?,
                onMessageLongClick: ((Message, View) -> Unit)?
        ) {
            // Handle document messages
            if (message.hasDocument()) {
                documentContainer.visibility = View.VISIBLE
                messageImageView.visibility = View.GONE
                messageTextView.visibility =
                        if (message.text.isNotEmpty()) View.VISIBLE else View.GONE

                documentNameTextView.text = message.documentName ?: "Document"
                documentSizeTextView.text = message.getFormattedFileSize()

                // Set icon based on file type
                documentIconImageView.setImageResource(getDocumentIcon(message.documentName ?: ""))

                // Click to download/open document
                documentContainer.setOnClickListener { onDocumentClick?.invoke(message) }
            }
            // Handle image messages
            else if (message.hasImage()) {
                messageImageView.visibility = View.VISIBLE
                documentContainer.visibility = View.GONE
                messageTextView.visibility =
                        if (message.text.isNotEmpty()) View.VISIBLE else View.GONE

                // Load image using Coil
                messageImageView.load(message.imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }

                // Click to view full screen
                messageImageView.setOnClickListener {
                    val intent =
                            Intent(itemView.context, ImageViewerActivity::class.java).apply {
                                putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, message.imageUrl)
                            }
                    itemView.context.startActivity(intent)
                }
            } else {
                messageImageView.visibility = View.GONE
                documentContainer.visibility = View.GONE
                messageTextView.visibility = View.VISIBLE
            }

            // Make URLs clickable in message text
            if (message.text.isNotEmpty()) {
                LinkifyHelper.makeLinksClickable(messageTextView, message.text)
            } else {
                messageTextView.text = message.text
            }
            timestampTextView.text = formatTime(message.timestamp?.time ?: 0)

            if (showSenderInfo) {
                senderNameTextView.visibility = View.VISIBLE
                senderNameTextView.text = message.senderName

                // Load profile image if available, otherwise show avatar with initials
                if (message.senderImageUrl.isNotEmpty()) {
                    senderProfileImageView.visibility = View.VISIBLE
                    senderAvatarTextView.visibility = View.GONE
                    senderProfileImageView.load(message.senderImageUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    senderProfileImageView.visibility = View.GONE
                    senderAvatarTextView.visibility = View.VISIBLE

                    // Generate avatar with initials
                    val initials = DefaultAvatarGenerator.getInitials(message.senderName)
                    senderAvatarTextView.text = initials

                    // Generate consistent color based on sender ID
                    val color = DefaultAvatarGenerator.generateColorFromString(message.senderId)
                    senderAvatarTextView.setBackgroundColor(color)
                }
            } else {
                senderNameTextView.visibility = View.GONE
                senderAvatarTextView.visibility = View.INVISIBLE
                senderProfileImageView.visibility = View.INVISIBLE
            }

            // Set long-click listener on the entire item view
            itemView.setOnLongClickListener {
                onMessageLongClick?.invoke(message, it)
                true
            }
        }

        private fun getDocumentIcon(fileName: String): Int {
            return when (fileName.substringAfterLast('.', "").lowercase()) {
                "pdf" -> android.R.drawable.ic_menu_save
                "doc", "docx" -> android.R.drawable.ic_menu_edit
                "xls", "xlsx" -> android.R.drawable.ic_menu_sort_by_size
                "ppt", "pptx" -> android.R.drawable.ic_menu_slideshow
                "zip", "rar" -> android.R.drawable.ic_menu_upload
                "txt" -> android.R.drawable.ic_menu_edit
                else -> android.R.drawable.ic_menu_save
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

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageGrouper.MessageItem>() {
        override fun areItemsTheSame(
                oldItem: MessageGrouper.MessageItem,
                newItem: MessageGrouper.MessageItem
        ): Boolean {
            return when {
                oldItem is MessageGrouper.MessageItem.MessageData &&
                        newItem is MessageGrouper.MessageItem.MessageData ->
                        oldItem.message.id == newItem.message.id
                oldItem is MessageGrouper.MessageItem.TimestampHeader &&
                        newItem is MessageGrouper.MessageItem.TimestampHeader ->
                        oldItem.timestamp == newItem.timestamp
                else -> false
            }
        }

        override fun areContentsTheSame(
                oldItem: MessageGrouper.MessageItem,
                newItem: MessageGrouper.MessageItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
