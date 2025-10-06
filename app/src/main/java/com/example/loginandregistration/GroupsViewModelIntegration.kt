package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.ui.groups.GroupsViewModel
import kotlinx.coroutines.launch

/** Fragment that demonstrates the new MVVM architecture integrated with the existing View system */
class GroupsViewModelIntegration : Fragment() {

    private lateinit var viewModel: GroupsViewModel
    private var myGroupsText: TextView? = null
    private var activeTasksText: TextView? = null
    private var newMessagesText: TextView? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Create a simple layout programmatically
        val layout =
                android.widget.LinearLayout(requireContext()).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 32)
                }

        // Title
        val titleText =
                TextView(requireContext()).apply {
                    text = "New MVVM Groups Dashboard"
                    textSize = 24f
                    setPadding(0, 0, 0, 32)
                }
        layout.addView(titleText)

        // Stats
        myGroupsText =
                TextView(requireContext()).apply {
                    text = "My Groups: Loading..."
                    textSize = 18f
                    setPadding(0, 0, 0, 16)
                }
        layout.addView(myGroupsText)

        activeTasksText =
                TextView(requireContext()).apply {
                    text = "Active Tasks: Loading..."
                    textSize = 18f
                    setPadding(0, 0, 0, 16)
                }
        layout.addView(activeTasksText)

        newMessagesText =
                TextView(requireContext()).apply {
                    text = "New Messages: Loading..."
                    textSize = 18f
                    setPadding(0, 0, 0, 16)
                }
        layout.addView(newMessagesText)

        // Refresh button
        val refreshButton =
                android.widget.Button(requireContext()).apply {
                    text = "Refresh Data"
                    setOnClickListener { viewModel.loadDashboardData() }
                }
        layout.addView(refreshButton)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[GroupsViewModel::class.java]

        // Observe UI state
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when {
                    uiState.isLoading -> {
                        myGroupsText?.text = "My Groups: Loading..."
                        activeTasksText?.text = "Active Tasks: Loading..."
                        newMessagesText?.text = "New Messages: Loading..."
                    }
                    uiState.hasError -> {
                        myGroupsText?.text = "Error: ${uiState.errorMessage}"
                        activeTasksText?.text = ""
                        newMessagesText?.text = ""
                    }
                    else -> {
                        myGroupsText?.text = "My Groups: ${uiState.myGroupsCount}"
                        activeTasksText?.text = "Active Tasks: ${uiState.activeAssignmentsCount}"
                        newMessagesText?.text = "New Messages: ${uiState.newMessagesCount}"
                    }
                }
            }
        }
    }
}
