package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.repository.GroupsRepository
import kotlinx.coroutines.launch

/**
 * Enhanced Groups Fragment that demonstrates the new MVVM repository pattern This shows how the
 * stats work correctly without ANR issues
 */
class GroupsFragmentEnhanced : Fragment() {

    private val repository = GroupsRepository()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Use the existing groups fragment layout
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load stats using the new repository
        loadStatsWithNewRepository(view)
    }

    /** Demonstrates the new non-blocking repository approach */
    private fun loadStatsWithNewRepository(view: View) {
        lifecycleScope.launch {
            try {
                // Show loading state
                updateStatsUI(view, "Loading...", "Loading...", "Loading...")

                // Fetch data in parallel (non-blocking)
                val groupsCount = repository.getMyGroupsCount()
                val tasksCount = repository.getActiveAssignmentsCount()
                val messagesCount = repository.getNewMessagesCount()

                // Update UI with real data
                updateStatsUI(
                        view,
                        groupsCount.toString(),
                        tasksCount.toString(),
                        messagesCount.toString()
                )
            } catch (e: Exception) {
                // Handle errors gracefully
                updateStatsUI(view, "Error", "Error", "Error")
                android.util.Log.e("GroupsFragmentEnhanced", "Error loading stats", e)
            }
        }
    }

    /** Updates the stats UI elements */
    private fun updateStatsUI(view: View, groups: String, tasks: String, messages: String) {
        view.findViewById<TextView>(R.id.tv_my_groups_count)?.text = groups
        view.findViewById<TextView>(R.id.tv_active_assignments_count)?.text = tasks
        view.findViewById<TextView>(R.id.tv_new_messages_count)?.text = messages
    }
}
