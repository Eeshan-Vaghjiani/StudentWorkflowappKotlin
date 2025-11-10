package com.example.loginandregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.R
import com.example.loginandregistration.models.AIChatMessage
import com.example.loginandregistration.models.MessageRole
import com.example.loginandregistration.utils.MarkdownRenderer
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

/** Adapter for displaying AI chat messages with markdown support */
class AIMessageAdapter(private val onActionClick: (AIChatMessage) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                val view =
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_ai_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            else -> {
                val view =
                        LayoutInflater.from(parent.context)
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

    /** Update messages with DiffUtil for efficient updates */
    fun updateMessages(newMessages: List<AIChatMessage>) {
        val diffCallback = AIMessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages.clear()
        messages.addAll(newMessages)

        diffResult.dispatchUpdatesTo(this)
    }

    /** Add a single message to the list */
    fun addMessage(message: AIChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    /** ViewHolder for user messages */
    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(message: AIChatMessage) {
            messageTextView.text = message.content
            timestampTextView.text = dateFormat.format(Date(message.timestamp))
        }
    }

    /** ViewHolder for assistant messages with markdown support */
    inner class AssistantMessageViewHolder(
            itemView: View,
            private val onActionClick: (AIChatMessage) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.actionButton)
        private val codeBlocksContainer: LinearLayout =
                itemView.findViewById(R.id.codeBlocksContainer)

        fun bind(message: AIChatMessage) {
            val context = itemView.context

            // Clean and extract code blocks
            val cleanedText = cleanMessageText(message.content)
            val codeBlocks = MarkdownRenderer.extractCodeBlocks(cleanedText)

            // Remove code blocks from main text and render markdown
            var textWithoutCodeBlocks = cleanedText
            codeBlocks.forEach { block ->
                textWithoutCodeBlocks =
                        textWithoutCodeBlocks.replace(
                                "```${block.language}\n${block.code}\n```",
                                ""
                        )
                textWithoutCodeBlocks = textWithoutCodeBlocks.replace("```\n${block.code}\n```", "")
            }

            // Render markdown formatting (bold, italic, inline code)
            val formattedText = MarkdownRenderer.parseMarkdown(textWithoutCodeBlocks.trim())
            messageTextView.text = formattedText

            // Add code blocks
            codeBlocksContainer.removeAllViews()
            if (codeBlocks.isNotEmpty()) {
                codeBlocksContainer.visibility = View.VISIBLE
                codeBlocks.forEach { codeBlock ->
                    val codeBlockView =
                            LayoutInflater.from(context)
                                    .inflate(R.layout.item_code_block, codeBlocksContainer, false)

                    val languageTextView =
                            codeBlockView.findViewById<TextView>(R.id.languageTextView)
                    val codeTextView = codeBlockView.findViewById<TextView>(R.id.codeTextView)
                    val copyButton = codeBlockView.findViewById<MaterialButton>(R.id.copyButton)

                    languageTextView.text = codeBlock.language.ifEmpty { "code" }
                    codeTextView.text = codeBlock.code

                    copyButton.setOnClickListener {
                        MarkdownRenderer.copyToClipboard(context, codeBlock.code, "Code")
                    }

                    codeBlocksContainer.addView(codeBlockView)
                }
            } else {
                codeBlocksContainer.visibility = View.GONE
            }

            timestampTextView.text = dateFormat.format(Date(message.timestamp))

            // Show action button if there's an action
            if (message.action != null) {
                actionButton.visibility = View.VISIBLE
                actionButton.setOnClickListener { onActionClick(message) }
            } else {
                actionButton.visibility = View.GONE
            }
        }

        /** Clean message text by removing JSON blocks */
        private fun cleanMessageText(text: String): String {
            // Remove JSON blocks from the message
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}") + 1

            return if (jsonStart != -1 && jsonEnd > jsonStart) {
                // Check if it's actually JSON (not part of code block)
                val beforeJson = text.substring(0, jsonStart)
                if (!beforeJson.contains("```")) {
                    val afterJson = text.substring(jsonEnd).trim()
                    "$beforeJson $afterJson".trim()
                } else {
                    text
                }
            } else {
                text
            }
        }
    }

    /** DiffUtil callback for efficient list updates */
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
