package com.example.loginandregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.R
import com.example.loginandregistration.models.AIChatMessage
import com.example.loginandregistration.models.MessageRole
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying AI chat messages
 */
class AIMessageAdapter(
    private val onActionClick: (AIChatMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_ASSISTANT = 2
    }

    private val messages = mutableListOf<AIChatMessage>()
    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].role) {
            MessageRole.USER -> VIEW_TYPE_USER
            MessageRole.ASSISTANT, MessageRole.SYSTEM -> VIEW_TYPE_ASSISTANT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ai_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ai_message_assistant, parent, false)
                AssistantMessageViewHolder(view, onActionClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AssistantMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Update messages with DiffUtil for efficient updates
     */
    fun updateMessages(newMessages: List<AIChatMessage>) {
        val diffCallback = AIMessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages.clear()
        messages.addAll(newMessages)

        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Add a single message to the list
     */
    fun addMessage(message: AIChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    /**
     * ViewHolder for user messages
     */
    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: AIChatMessage) {
            messageTextView.text = message.content
            timestampTextView.text = dateFormat.format(Date(message.timestamp))
        }
    }

    /**
     * ViewHolder for assistant messages
     */
    inner class AssistantMessageViewHolder(
        itemView: View,
        private val onActionClick: (AIChatMessage) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.actionButton)

        fun bind(message: AIChatMessage) {
            // Display the message content (clean text without JSON)
            val displayText = cleanMessageText(message.content)
            messageTextView.text = displayText

            timestampTextView.text = dateFormat.format(Date(message.timestamp))

            // Show action button if there's an action
            if (message.action != null) {
                actionButton.visibility = View.VISIBLE
                actionButton.setOnClickListener {
                    onActionClick(message)
                }
            } else {
                actionButton.visibility = View.GONE
            }
        }

        /**
         * Clean message text by removing JSON blocks
         */
        private fun cleanMessageText(text: String): String {
            // Remove JSON blocks from the message
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1

            return if (jsonStart != -1 && jsonEnd > jsonStart) {
                // Remove the JSON part and clean up
                val beforeJson = text.substring(0, jsonStart).trim()
                val afterJson = text.substring(jsonEnd).trim()
                "$beforeJson $afterJson".trim()
            } else {
                text
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class AIMessageDiffCallback(
        private val oldList: List<AIChatMessage>,
        private val newList: List<AIChatMessage>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
