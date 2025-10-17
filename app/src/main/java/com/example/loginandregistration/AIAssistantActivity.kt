package com.example.loginandregistration

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginandregistration.adapters.AIMessageAdapter
import com.example.loginandregistration.databinding.ActivityAiAssistantBinding
import com.example.loginandregistration.models.AIChatMessage
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.services.GeminiAssistantService
import com.example.loginandregistration.viewmodels.AIAssistantViewModel

/**
 * Activity for AI Assistant chat interface
 * Allows users to interact with AI to create assignments and get help
 */
class AIAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiAssistantBinding
    private lateinit var viewModel: AIAssistantViewModel
    private lateinit var adapter: AIMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        // Get API key from BuildConfig or local.properties
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isEmpty() || apiKey == "AIzaSyBWn5wPqt6OeqiBxlevwzQGz00P7Oc4ZP0") {
            Toast.makeText(
                this,
                "Please configure Gemini API key in local.properties",
                Toast.LENGTH_LONG
            ).show()
        }

        val taskRepository = TaskRepository(this)
        val geminiService = GeminiAssistantService(apiKey, taskRepository)
        
        // Create ViewModel with factory
        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AIAssistantViewModel(geminiService) as T
            }
        }
        
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[AIAssistantViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = AIMessageAdapter { message ->
            onActionButtonClick(message)
        }

        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AIAssistantActivity)
            adapter = this@AIAssistantActivity.adapter
        }
    }

    private fun setupListeners() {
        // Send button click
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        // Enter key to send
        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        // Observe messages
        viewModel.messages.observe(this) { messages ->
            adapter.updateMessages(messages)
            
            // Show/hide empty state
            if (messages.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.messagesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.messagesRecyclerView.visibility = View.VISIBLE
                
                // Scroll to bottom when new message arrives
                binding.messagesRecyclerView.postDelayed({
                    binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                }, 100)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendButton.isEnabled = !isLoading
            binding.messageEditText.isEnabled = !isLoading
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Observe task creation
        viewModel.taskCreated.observe(this) { task ->
            if (task != null) {
                Toast.makeText(
                    this,
                    "Assignment created: ${task.title}",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearTaskCreated()
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()
        
        if (messageText.isNotEmpty()) {
            viewModel.sendMessage(messageText)
            binding.messageEditText.text?.clear()
        }
    }

    private fun onActionButtonClick(message: AIChatMessage) {
        // Handle action button click (e.g., create assignment)
        viewModel.createAssignmentFromAI(message)
    }
}
