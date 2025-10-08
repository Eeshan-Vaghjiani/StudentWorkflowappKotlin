package com.example.loginandregistration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.loginandregistration.adapters.ChatAdapter
import com.example.loginandregistration.models.Chat
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.viewmodels.ChatViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRepository: ChatRepository

    // Views
    private lateinit var searchEditText: EditText
    private lateinit var chatTabLayout: TabLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var fabNewChat: FloatingActionButton

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repository and ViewModel
        chatRepository = ChatRepository()
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Initialize views
        initializeViews(view)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup listeners
        setupListeners()

        // Observe ViewModel
        observeViewModel()

        // Ensure group chats exist for all user's groups
        lifecycleScope.launch {
            val result = chatRepository.ensureGroupChatsExist()
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                if (count > 0) {
                    android.util.Log.d("ChatFragment", "Created $count group chats")
                }
            } else {
                android.util.Log.e("ChatFragment", "Failed to ensure group chats: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun initializeViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        chatTabLayout = view.findViewById(R.id.chatTabLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        fabNewChat = view.findViewById(R.id.fabNewChat)
    }

    private fun setupRecyclerView() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        chatAdapter = ChatAdapter(currentUserId) { chat -> onChatClick(chat) }

        chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // Search functionality
        searchEditText.addTextChangedListener(
                object : TextWatcher {
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
                        viewModel.setSearchQuery(s?.toString() ?: "")
                    }

                    override fun afterTextChanged(s: Editable?) {}
                }
        )

        // Tab selection
        chatTabLayout.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.position?.let { position -> viewModel.setSelectedTab(position) }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                }
        )

        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }

        // FAB for new chat
        fabNewChat.setOnClickListener {
            val dialog = UserSearchDialog.newInstance()
            dialog.show(parentFragmentManager, UserSearchDialog.TAG)
        }
    }

    private fun observeViewModel() {
        // Observe filtered chats
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredChats.collect { chats ->
                chatAdapter.submitList(chats)

                // Show/hide empty state
                if (chats.isEmpty()) {
                    emptyStateLayout.visibility = View.VISIBLE
                    chatsRecyclerView.visibility = View.GONE
                } else {
                    emptyStateLayout.visibility = View.GONE
                    chatsRecyclerView.visibility = View.VISIBLE
                }
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading -> swipeRefreshLayout.isRefreshing = isLoading }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun onChatClick(chat: Chat) {
        val intent =
                android.content.Intent(requireContext(), ChatRoomActivity::class.java).apply {
                    putExtra(ChatRoomActivity.EXTRA_CHAT_ID, chat.chatId)
                    putExtra(
                            ChatRoomActivity.EXTRA_CHAT_NAME,
                            chat.getDisplayName(chatRepository.getCurrentUserId())
                    )
                    putExtra(
                            ChatRoomActivity.EXTRA_CHAT_IMAGE_URL,
                            chat.getDisplayImageUrl(chatRepository.getCurrentUserId())
                    )
                }
        startActivity(intent)
    }
}
