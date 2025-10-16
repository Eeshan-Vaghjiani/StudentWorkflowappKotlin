package com.example.loginandregistration

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.adapters.UserSearchAdapter
import com.example.loginandregistration.models.UserInfo
import com.example.loginandregistration.repository.ChatRepository
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserSearchDialog : DialogFragment() {

    private lateinit var chatRepository: ChatRepository
    private lateinit var userSearchAdapter: UserSearchAdapter
    private var searchJob: Job? = null

    private lateinit var searchEditText: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var cancelButton: MaterialButton

    companion object {
        const val TAG = "UserSearchDialog"

        fun newInstance(): UserSearchDialog {
            return UserSearchDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_LoginAndRegistration)
        chatRepository = ChatRepository()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_user_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupListeners()
    }

    private fun initializeViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        cancelButton = view.findViewById(R.id.cancelButton)
    }

    private fun setupRecyclerView() {
        userSearchAdapter = UserSearchAdapter { user -> onUserClick(user) }

        searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userSearchAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
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
                        searchJob?.cancel()
                        searchJob =
                                lifecycleScope.launch {
                                    delay(300)
                                    searchUsers(s?.toString() ?: "")
                                }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                }
        )

        cancelButton.setOnClickListener { dismiss() }

        searchEditText.requestFocus()
    }

    private fun searchUsers(query: String) {
        if (query.isBlank()) {
            userSearchAdapter.submitList(emptyList())
            showEmptyState(false)
            searchEditText.error = null
            return
        }

        // Validate email format if query looks like an email
        if (query.contains("@")) {
            val validation =
                    com.example.loginandregistration.utils.InputValidator.validateEmail(query)
            if (!validation.isValid) {
                searchEditText.error = validation.errorMessage
                userSearchAdapter.submitList(emptyList())
                showEmptyState(true)
                return
            }
        }

        searchEditText.error = null
        showLoading(true)

        lifecycleScope.launch {
            val result = chatRepository.searchUsers(query)

            showLoading(false)

            result
                    .onSuccess { users ->
                        userSearchAdapter.submitList(users)
                        showEmptyState(users.isEmpty())
                    }
                    .onFailure { error ->
                        Toast.makeText(
                                        requireContext(),
                                        "Error searching users: ${error.message}",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                        showEmptyState(true)
                    }
        }
    }

    private fun onUserClick(user: UserInfo) {
        showLoading(true)

        lifecycleScope.launch {
            // Pass the UserInfo so the repository can create the user document if needed
            val result = chatRepository.getOrCreateDirectChat(user.userId, user)

            showLoading(false)

            result
                    .onSuccess { chat ->
                        val intent =
                                Intent(requireContext(), ChatRoomActivity::class.java).apply {
                                    putExtra(ChatRoomActivity.EXTRA_CHAT_ID, chat.chatId)
                                    putExtra(ChatRoomActivity.EXTRA_CHAT_NAME, user.displayName)
                                    putExtra(
                                            ChatRoomActivity.EXTRA_CHAT_IMAGE_URL,
                                            user.profileImageUrl
                                    )
                                }
                        startActivity(intent)
                        dismiss()
                    }
                    .onFailure { error ->
                        Toast.makeText(
                                        requireContext(),
                                        "Error creating chat: ${error.message}",
                                        Toast.LENGTH_LONG
                                )
                                .show()
                    }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        searchRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        searchRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}
