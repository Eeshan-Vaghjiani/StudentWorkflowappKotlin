package com.example.loginandregistration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginandregistration.adapters.ChatAdapter
import com.example.loginandregistration.databinding.FragmentChatBinding
import com.example.loginandregistration.models.Chat
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.utils.collectWithLifecycle
import com.example.loginandregistration.viewmodels.ChatViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    companion object {
        private const val TAG = "ChatFragment"
    }

    private var _binding: FragmentChatBinding? = null
    private val binding: FragmentChatBinding?
        get() = _binding

    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRepository: ChatRepository
    private lateinit var errorStateManager: com.example.loginandregistration.utils.ErrorStateManager

    private var ensureChatsJob: Job? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        // Initialize repository and ViewModel
        chatRepository = ChatRepository()
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        errorStateManager =
                com.example.loginandregistration.utils.ErrorStateManager(requireContext())

        // Setup RecyclerView
        setupRecyclerView()

        // Setup listeners
        setupListeners()

        // Observe ViewModel
        observeViewModel()

        // Ensure group chats exist for all user's groups
        ensureChatsJob =
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = chatRepository.ensureGroupChatsExist()
                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        if (count > 0) {
                            Log.d(TAG, "Created $count group chats")
                        }
                    } else {
                        val exception = result.exceptionOrNull()
                        Log.e(TAG, "Failed to ensure group chats: ${exception?.message}")

                        // Only show error if view still exists
                        if (_binding != null && isAdded) {
                            showGroupChatsError(exception)
                        } else {
                            Log.d(TAG, "View destroyed, skipping error UI update")
                        }
                    }
                }
    }

    private fun showGroupChatsError(exception: Throwable?) {
        val binding =
                _binding
                        ?: run {
                            Log.d(TAG, "Cannot show group chats error: view is destroyed")
                            return
                        }

        // Use ErrorStateManager for consistent error handling
        exception?.let {
            val errorState = errorStateManager.categorizeError(it)
            errorStateManager.showError(errorState, binding.root, null)
        }
    }

    private fun setupRecyclerView() {
        val binding = _binding ?: return

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        chatAdapter = ChatAdapter(currentUserId) { chat -> onChatClick(chat) }

        binding.skeletonRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter =
                    com.example.loginandregistration.utils.SkeletonLoaderHelper
                            .createSkeletonAdapter(R.layout.item_chat_skeleton, 5)
            setHasFixedSize(true)
        }

        binding.chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        val binding = _binding ?: return

        // Search functionality
        binding.searchEditText.addTextChangedListener(
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
        binding.chatTabLayout.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.position?.let { position -> viewModel.setSelectedTab(position) }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                }
        )

        // Pull to refresh
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }

        // FAB for new chat
        binding.fabNewChat.setOnClickListener {
            val dialog = UserSearchDialog.newInstance()
            dialog.show(parentFragmentManager, UserSearchDialog.TAG)
        }
    }

    private fun observeViewModel() {
        // Use lifecycle-aware collection that automatically stops when Fragment is stopped
        // and resumes when Fragment is started again

        // Observe filtered chats
        viewModel.filteredChats.collectWithLifecycle(viewLifecycleOwner) { chats ->
            // Only update UI if view exists
            if (_binding == null || !isAdded) {
                Log.d(TAG, "View destroyed, skipping chats UI update")
                return@collectWithLifecycle
            }

            Log.d(TAG, "Received ${chats.size} chats")
            updateChatsUI(chats)
        }

        // Observe loading state
        viewModel.isLoading.collectWithLifecycle(viewLifecycleOwner) { isLoading ->
            // Only update UI if view exists
            if (_binding == null || !isAdded) {
                Log.d(TAG, "View destroyed, skipping loading UI update")
                return@collectWithLifecycle
            }

            updateLoadingUI(isLoading)
        }

        // Observe errors
        viewModel.error.collectWithLifecycle(viewLifecycleOwner) { error ->
            // Only show error if view exists
            if (_binding == null || !isAdded) {
                Log.d(TAG, "View destroyed, skipping error UI update")
                return@collectWithLifecycle
            }

            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateChatsUI(chats: List<Chat>) {
        val binding =
                _binding
                        ?: run {
                            Log.d(TAG, "Cannot update chats UI: view is destroyed")
                            return
                        }

        chatAdapter.submitList(chats)

        // Show/hide empty state
        if (chats.isEmpty()) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.emptyStateView.showNoChats {
                // Open user search dialog when action button clicked
                val dialog = UserSearchDialog.newInstance()
                dialog.show(parentFragmentManager, UserSearchDialog.TAG)
            }
            binding.chatsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.chatsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateLoadingUI(isLoading: Boolean) {
        val binding =
                _binding
                        ?: run {
                            Log.d(TAG, "Cannot update loading UI: view is destroyed")
                            return
                        }

        binding.swipeRefreshLayout.isRefreshing = isLoading

        // Show skeleton loader on initial load, hide on subsequent refreshes
        if (isLoading && chatAdapter.itemCount == 0) {
            com.example.loginandregistration.utils.SkeletonLoaderHelper.showSkeleton(
                    binding.skeletonRecyclerView,
                    binding.chatsRecyclerView
            )
        } else if (!isLoading) {
            com.example.loginandregistration.utils.SkeletonLoaderHelper.showContent(
                    binding.skeletonRecyclerView,
                    binding.chatsRecyclerView
            )
        }
    }

    private fun showError(message: String) {
        val binding =
                _binding
                        ?: run {
                            Log.d(TAG, "Cannot show error: view is destroyed")
                            return
                        }

        // Use ErrorStateManager for consistent error handling
        val errorState = errorStateManager.categorizeError(Exception(message))
        errorStateManager.showError(errorState, binding.root, null)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart - Firestore listeners will be attached via ViewModel")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop - Firestore listeners will be detached via ViewModel")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")

        // Cancel any ongoing operations
        ensureChatsJob?.cancel()
        ensureChatsJob = null

        // Clear binding reference
        _binding = null
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
