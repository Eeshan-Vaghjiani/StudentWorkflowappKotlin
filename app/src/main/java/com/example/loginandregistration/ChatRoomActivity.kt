package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginandregistration.adapters.MessageAdapter
import com.example.loginandregistration.databinding.ActivityChatRoomBinding
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.repository.StorageRepository
import com.example.loginandregistration.utils.AnimationUtils
import com.example.loginandregistration.utils.ConnectionMonitor
import com.example.loginandregistration.viewmodels.ChatRoomViewModel
import com.example.loginandregistration.viewmodels.ChatRoomViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.File
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
    private lateinit var storageRepository: StorageRepository
    private lateinit var connectionMonitor: ConnectionMonitor
    private var wasOffline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize storage repository
        storageRepository = StorageRepository(context = applicationContext)

        // Initialize connection monitor
        connectionMonitor = ConnectionMonitor(this)
        binding.connectionStatusView.hideImmediate()
        monitorConnectionStatus()

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
                        },
                        onDocumentClick = { message -> handleDocumentClick(message) },
                        onMessageLongClick = { message, view ->
                            showMessageContextMenu(message, view)
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
        binding.sendButton.setOnClickListener {
            AnimationUtils.buttonPress(it)
            sendMessage()
        }

        binding.attachmentButton.setOnClickListener {
            AnimationUtils.buttonPress(it)
            showAttachmentPicker()
        }

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
                                        AnimationUtils.smoothScrollToPosition(
                                                binding.messagesRecyclerView,
                                                messageAdapter.itemCount - 1,
                                                smooth = false
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
                                        AnimationUtils.smoothScrollToBottom(
                                                binding.messagesRecyclerView
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
                    // Show offline banner using ConnectionStatusView
                    binding.connectionStatusView.showOffline()
                } else {
                    // Hide banner or show "Connected" briefly
                    if (wasOffline) {
                        binding.connectionStatusView.showConnecting()
                        binding.connectionStatusView.postDelayed(
                                {
                                    binding.connectionStatusView.showOnline()
                                    wasOffline = false
                                },
                                1500
                        )
                    } else {
                        binding.connectionStatusView.showOnline()
                    }
                }
            }
        }
    }

    /**
     * Monitor network connection status and update the connection status banner. Shows "No internet
     * connection" when offline, "Connecting..." when reconnecting, and hides the banner when
     * online.
     */
    private fun monitorConnectionStatus() {
        lifecycleScope.launch {
            connectionMonitor.isConnected.collect { isConnected ->
                android.util.Log.d("ChatRoomActivity", "Connection status changed: $isConnected")

                when {
                    // Currently offline
                    !isConnected -> {
                        wasOffline = true
                        binding.connectionStatusView.showOffline()
                    }
                    // Just came back online
                    isConnected && wasOffline -> {
                        binding.connectionStatusView.showConnecting()
                        // Show "Connecting..." briefly, then hide
                        binding.connectionStatusView.postDelayed(
                                {
                                    binding.connectionStatusView.showOnline()
                                    wasOffline = false
                                },
                                1500
                        ) // Show for 1.5 seconds
                    }
                    // Already online
                    else -> {
                        binding.connectionStatusView.showOnline()
                        wasOffline = false
                    }
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

    private fun showAttachmentPicker() {
        val bottomSheet =
                AttachmentBottomSheet(
                        onImageSelected = { uri -> handleImageSelected(uri) },
                        onDocumentSelected = { uri -> handleDocumentSelected(uri) }
                )
        bottomSheet.show(supportFragmentManager, "AttachmentBottomSheet")
    }

    private fun handleImageSelected(uri: android.net.Uri) {
        lifecycleScope.launch {
            // Show progress dialog
            val progressDialog =
                    androidx.appcompat.app.AlertDialog.Builder(this@ChatRoomActivity)
                            .setTitle("Uploading Image")
                            .setMessage("0%")
                            .setCancelable(false)
                            .create()
            progressDialog.show()

            try {
                viewModel.sendImageMessage(uri) { progress ->
                    runOnUiThread { progressDialog.setMessage("$progress%") }
                }
                progressDialog.dismiss()
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(
                                this@ChatRoomActivity,
                                "Failed to send image: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }

    private fun handleDocumentSelected(uri: android.net.Uri) {
        lifecycleScope.launch {
            // Show progress dialog
            val progressDialog =
                    androidx.appcompat.app.AlertDialog.Builder(this@ChatRoomActivity)
                            .setTitle("Uploading Document")
                            .setMessage("0%")
                            .setCancelable(false)
                            .create()
            progressDialog.show()

            try {
                viewModel.sendDocumentMessage(uri) { progress ->
                    runOnUiThread { progressDialog.setMessage("$progress%") }
                }
                progressDialog.dismiss()
                Toast.makeText(
                                this@ChatRoomActivity,
                                "Document sent successfully",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(
                                this@ChatRoomActivity,
                                "Failed to send document: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
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

    private fun handleDocumentClick(message: Message) {
        val documentUrl = message.documentUrl ?: return
        val documentName = message.documentName ?: "document"

        lifecycleScope.launch {
            try {
                // Show progress dialog
                val progressDialog =
                        androidx.appcompat.app.AlertDialog.Builder(this@ChatRoomActivity)
                                .setTitle("Downloading Document")
                                .setMessage("0%")
                                .setCancelable(false)
                                .create()
                progressDialog.show()

                // Create destination file in Downloads folder
                val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                        )
                val destinationFile = File(downloadsDir, documentName)

                // Download file
                val result =
                        storageRepository.downloadFile(
                                url = documentUrl,
                                destinationFile = destinationFile,
                                onProgress = { progress ->
                                    runOnUiThread { progressDialog.setMessage("$progress%") }
                                }
                        )

                progressDialog.dismiss()

                result.fold(
                        onSuccess = { file ->
                            // Notify media scanner
                            sendBroadcast(
                                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                                        data = android.net.Uri.fromFile(file)
                                    }
                            )

                            // Try to open the document
                            openDocument(file)
                        },
                        onFailure = { error ->
                            Snackbar.make(
                                            binding.root,
                                            "Download failed: ${error.message}",
                                            Snackbar.LENGTH_LONG
                                    )
                                    .show()
                        }
                )
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun openDocument(file: File) {
        try {
            // Get MIME type
            val mimeType = getMimeType(file.name) ?: "*/*"

            // Create content URI using FileProvider
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

            // Create intent to open document
            val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // No app can open this file type
                Snackbar.make(
                                binding.root,
                                "No app found to open this file type. File saved to Downloads.",
                                Snackbar.LENGTH_LONG
                        )
                        .show()
            }
        } catch (e: Exception) {
            Snackbar.make(
                            binding.root,
                            "Cannot open document: ${e.message}. File saved to Downloads.",
                            Snackbar.LENGTH_LONG
                    )
                    .show()
        }
    }

    private fun getMimeType(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }

    private fun showMessageContextMenu(message: Message, view: View) {
        val popup = androidx.appcompat.widget.PopupMenu(this, view)

        // Add menu items
        popup.menu.add(0, 1, 0, "Copy")

        // Only show delete option if the message is from the current user
        if (message.senderId == viewModel.getCurrentUserId()) {
            popup.menu.add(0, 2, 1, "Delete")
        }

        // Add forward option (placeholder for future implementation)
        popup.menu.add(0, 3, 2, "Forward")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    // Copy message text to clipboard
                    copyMessageToClipboard(message)
                    true
                }
                2 -> {
                    // Delete message (only for sender)
                    showDeleteConfirmationDialog(message)
                    true
                }
                3 -> {
                    // Forward message (placeholder)
                    Toast.makeText(this, "Forward feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun copyMessageToClipboard(message: Message) {
        val clipboard =
                getSystemService(android.content.Context.CLIPBOARD_SERVICE) as
                        android.content.ClipboardManager

        // Determine what to copy based on message type
        val textToCopy =
                when {
                    message.text.isNotEmpty() -> message.text
                    message.hasImage() -> message.imageUrl ?: "Image"
                    message.hasDocument() -> message.documentName ?: "Document"
                    else -> "Message"
                }

        val clip = android.content.ClipData.newPlainText("Message", textToCopy)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog(message: Message) {
        androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage(
                        "Are you sure you want to delete this message? This action cannot be undone."
                )
                .setPositiveButton("Delete") { _, _ -> deleteMessage(message) }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun deleteMessage(message: Message) {
        lifecycleScope.launch {
            try {
                val result = viewModel.deleteMessage(message)

                if (result.isSuccess) {
                    Toast.makeText(this@ChatRoomActivity, "Message deleted", Toast.LENGTH_SHORT)
                            .show()
                } else {
                    Toast.makeText(
                                    this@ChatRoomActivity,
                                    "Failed to delete message: ${result.exceptionOrNull()?.message}",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@ChatRoomActivity,
                                "Error deleting message: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }
}
