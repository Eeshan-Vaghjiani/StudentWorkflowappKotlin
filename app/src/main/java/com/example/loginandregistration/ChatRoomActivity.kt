package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginandregistration.adapters.MessageAdapter
import com.example.loginandregistration.databinding.ActivityChatRoomBinding
import com.example.loginandregistration.viewmodels.ChatRoomViewModel
import com.example.loginandregistration.viewmodels.ChatRoomViewModelFactory
import kotlinx.coroutines.launch

class ChatRoomActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHAT_ID = "extra_chat_id"
        const val EXTRA_CHAT_NAME = "extra_chat_name"
        const val EXTRA_CHAT_IMAGE_URL = "extra_chat_image_url"
    }

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var messageAdapter: MessageAdapter
    private val viewModel: ChatRoomViewModel by viewModels { ChatRoomViewModelFactory(application) }
    private var typingTimer: android.os.Handler? = null
    private var isTyping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle both regular intents and notification intents
        val chatId = intent.getStringExtra(EXTRA_CHAT_ID) ?: intent.getStringExtra("chatId")
        val chatName = intent.getStringExtra(EXTRA_CHAT_NAME) ?: intent.getStringExtra("chatName")
        val chatImageUrl = intent.getStringExtra(EXTRA_CHAT_IMAGE_URL)
        val fromNotification = intent.getBooleanExtra("fromNotification", false)

        if (chatId == null) {
            Toast.makeText(this, "Error: Chat ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar(chatName, chatImageUrl)
        setupRecyclerView()
        setupMessageInput()
        observeViewModel()

        viewModel.loadChat(chatId)

        // If opened from notification, mark messages as read
        if (fromNotification) {
            lifecycleScope.launch { viewModel.markAllMessagesAsRead() }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle new notification while activity is already open
        val chatId = intent.getStringExtra("chatId")
        val fromNotification = intent.getBooleanExtra("fromNotification", false)

        if (chatId != null && fromNotification) {
            viewModel.loadChat(chatId)
            lifecycleScope.launch { viewModel.markAllMessagesAsRead() }
        }
    }

    private fun setupToolbar(chatName: String?, chatImageUrl: String?) {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.chatNameTextView.text = chatName ?: "Chat"

        // Show avatar with initials
        if (!chatName.isNullOrEmpty()) {
            binding.chatAvatarTextView.text = getInitials(chatName)
            binding.chatAvatarTextView.visibility = View.VISIBLE
            binding.chatProfileImageView.visibility = View.GONE
        }

        // TODO: Load profile image if available using Coil
        // if (!chatImageUrl.isNullOrEmpty()) {
        //     binding.chatProfileImageView.load(chatImageUrl)
        //     binding.chatProfileImageView.visibility = View.VISIBLE
        //     binding.chatAvatarTextView.visibility = View.GONE
        // }
    }

    private fun setupRecyclerView() {
        messageAdapter =
                MessageAdapter(
                        currentUserId = viewModel.getCurrentUserId(),
                        onRetryMessage = { message ->
                            // Show confirmation dialog before retrying
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Retry Message")
                                    .setMessage("Do you want to retry sending this message?")
                                    .setPositiveButton("Retry") { _, _ ->
                                        viewModel.retryMessage(message)
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                        }
                )

        val layoutManager = LinearLayoutManager(this@ChatRoomActivity).apply { stackFromEnd = true }

        binding.messagesRecyclerView.apply {
            adapter = messageAdapter
            this.layoutManager = layoutManager

            // Add scroll listener to detect when user scrolls to top
            addOnScrollListener(
                    object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                        override fun onScrolled(
                                recyclerView: androidx.recyclerview.widget.RecyclerView,
                                dx: Int,
                                dy: Int
                        ) {
                            super.onScrolled(recyclerView, dx, dy)

                            // Check if scrolled to top (first visible item is at position 0)
                            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

                            if (firstVisiblePosition == 0 && dy < 0) {
                                // User scrolled to top, load more messages
                                viewModel.loadMoreMessages()
                            }
                        }
                    }
            )
        }
    }

    private fun setupMessageInput() {
        binding.sendButton.setOnClickListener { sendMessage() }

        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // Handle typing indicator
        binding.messageEditText.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}

                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {
                        if (!s.isNullOrEmpty() && !isTyping) {
                            isTyping = true
                            viewModel.updateTypingStatus(true)
                        }

                        // Reset timer
                        typingTimer?.removeCallbacksAndMessages(null)
                        typingTimer =
                                android.os.Handler(android.os.Looper.getMainLooper()).apply {
                                    postDelayed(
                                            {
                                                if (isTyping) {
                                                    isTyping = false
                                                    viewModel.updateTypingStatus(false)
                                                }
                                            },
                                            2000
                                    ) // Stop typing after 2 seconds of inactivity
                                }
                    }

                    override fun afterTextChanged(s: android.text.Editable?) {}
                }
        )
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text?.toString()?.trim()

        if (messageText.isNullOrEmpty()) {
            return
        }

        // Stop typing indicator when sending
        if (isTyping) {
            isTyping = false
            viewModel.updateTypingStatus(false)
        }

        lifecycleScope.launch {
            viewModel.sendMessage(messageText)
            binding.messageEditText.text?.clear()
        }
    }

    private fun observeViewModel() {
        var previousMessageCount = 0
        var isFirstLoad = true

        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                val currentMessageCount = messages.size
                val layoutManager =
                        binding.messagesRecyclerView.layoutManager as? LinearLayoutManager

                // Save scroll position before updating
                val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: 0

                messageAdapter.submitMessages(messages)

                // Handle scroll position based on context
                if (messages.isNotEmpty()) {
                    when {
                        // First load: scroll to bottom
                        isFirstLoad -> {
                            binding.messagesRecyclerView.postDelayed(
                                    {
                                        binding.messagesRecyclerView.scrollToPosition(
                                                messageAdapter.itemCount - 1
                                        )
                                    },
                                    100
                            )
                            isFirstLoad = false
                        }
                        // New message added at the end: scroll to bottom if already near bottom
                        currentMessageCount > previousMessageCount &&
                                firstVisiblePosition >= previousMessageCount - 5 -> {
                            binding.messagesRecyclerView.postDelayed(
                                    {
                                        binding.messagesRecyclerView.smoothScrollToPosition(
                                                messageAdapter.itemCount - 1
                                        )
                                    },
                                    100
                            )
                        }
                        // Older messages loaded: maintain scroll position
                        currentMessageCount > previousMessageCount && firstVisiblePosition < 5 -> {
                            val itemsAdded = currentMessageCount - previousMessageCount
                            binding.messagesRecyclerView.postDelayed(
                                    {
                                        layoutManager?.scrollToPositionWithOffset(
                                                firstVisiblePosition + itemsAdded,
                                                0
                                        )
                                    },
                                    50
                            )
                        }
                    }
                }

                previousMessageCount = currentMessageCount
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoadingMore.collect { isLoadingMore ->
                binding.loadMoreProgressBar.visibility =
                        if (isLoadingMore) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isSending.collect { isSending ->
                binding.sendingProgressBar.visibility = if (isSending) View.VISIBLE else View.GONE
                binding.sendButton.isEnabled = !isSending
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let { Toast.makeText(this@ChatRoomActivity, it, Toast.LENGTH_SHORT).show() }
            }
        }

        lifecycleScope.launch {
            viewModel.typingUsers.collect { typingUserIds ->
                if (typingUserIds.isNotEmpty()) {
                    // Get user names from chat participant details
                    val chatId = intent.getStringExtra(EXTRA_CHAT_ID)
                    val typingText =
                            if (typingUserIds.size == 1) {
                                "typing..."
                            } else {
                                "${typingUserIds.size} people are typing..."
                            }
                    binding.typingIndicatorTextView.text = typingText
                    binding.typingIndicatorLayout.visibility = View.VISIBLE
                } else {
                    binding.typingIndicatorLayout.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isConnected.collect { isConnected ->
                if (!isConnected) {
                    // Show offline indicator
                    Toast.makeText(
                                    this@ChatRoomActivity,
                                    "No internet connection. Messages will be sent when connection is restored.",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up typing indicator
        if (isTyping) {
            viewModel.updateTypingStatus(false)
        }
        typingTimer?.removeCallbacksAndMessages(null)
    }

    private fun getInitials(name: String): String {
        val names = name.trim().split(" ")
        return when {
            names.size >= 2 -> "${names[0].first()}${names[1].first()}".uppercase()
            names.isNotEmpty() -> names[0].take(2).uppercase()
            else -> "?"
        }
    }
}
