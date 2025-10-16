package com.example.loginandregistration

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUI()
        setupClickListeners()
    }

    private fun setupToolbar() {
        // Setup Toolbar
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarHome)
        (activity as? AppCompatActivity)?.supportActionBar?.title =
                getString(R.string.dashboard_title)

        // Setup menu using the new MenuProvider API
        requireActivity()
                .addMenuProvider(
                        object : MenuProvider {
                            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                                menuInflater.inflate(R.menu.home_toolbar_menu, menu)
                            }

                            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                                return when (menuItem.itemId) {
                                    R.id.action_notifications -> {
                                        Toast.makeText(
                                                        context,
                                                        R.string.navigating_to_notifications,
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        // TODO: Implement navigation to Notifications
                                        // screen/fragment
                                        true
                                    }
                                    R.id.action_profile -> {
                                        (activity as? MainActivity)?.navigateToProfile()
                                        true
                                    }
                                    R.id.action_debug -> {
                                        startActivity(
                                                android.content.Intent(
                                                        requireContext(),
                                                        DebugActivity::class.java
                                                )
                                        )
                                        true
                                    }
                                    else -> false
                                }
                            }
                        },
                        viewLifecycleOwner,
                        Lifecycle.State.RESUMED
                )
    }

    private fun setupUI() {
        // Update Welcome Message
        val user = auth.currentUser
        val welcomeName = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "User"
        binding.tvWelcomeTitle.text =
                getString(R.string.welcome_message, welcomeName)
                        .replace("👋 ", "")
                        .replace(",", "") // Basic formatting

        // Load real data from Firebase
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Initialize UI with placeholder values
        binding.tvTasksDueCount.text = "0"
        binding.tvGroupsCount.text = "0"
        binding.tvSessionsCount.text = "0"

        // AI usage - keep static for now
        binding.tvAiPromptsLeft.text = getString(R.string.home_ai_prompts_left_template, 7)
        binding.progressBarAiUsage.progress = 30
        binding.tvAiUsageDetails.text = getString(R.string.home_ai_prompts_usage_template, 3, 10)

        // Set up real-time listeners for continuous updates
        setupRealTimeListeners()
    }

    private fun setupRealTimeListeners() {
        // Set up real-time listener for tasks using Flow with lifecycle awareness
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val taskRepository = com.example.loginandregistration.repository.TaskRepository()
                taskRepository.getDashboardTaskStatsFlow().collect { taskStats ->
                    // Update UI on main thread
                    binding.tvTasksDueCount.text = taskStats.tasksDue.toString()
                }
            } catch (e: Exception) {
                // Handle error silently - show 0 as fallback
                binding.tvTasksDueCount.text = "0"
            }
        }

        // Set up real-time listener for groups using Flow with lifecycle awareness
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val groupRepository = com.example.loginandregistration.repository.GroupRepository()
                groupRepository.getGroupStatsFlow().collect { groupStats ->
                    // Update UI on main thread
                    binding.tvGroupsCount.text = groupStats.myGroups.toString()
                }
            } catch (e: Exception) {
                // Handle error silently - show 0 as fallback
                binding.tvGroupsCount.text = "0"
            }
        }

        // Set up real-time listener for study sessions using Flow with lifecycle awareness
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sessionRepository =
                        com.example.loginandregistration.repository.SessionRepository()
                sessionRepository.getSessionStatsFlow().collect { sessionStats ->
                    // Update UI on main thread
                    binding.tvSessionsCount.text = sessionStats.totalSessions.toString()
                }
            } catch (e: Exception) {
                // Handle error silently - show 0 as fallback
                binding.tvSessionsCount.text = "0"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnViewAllTasksHome.setOnClickListener {
            Toast.makeText(context, R.string.button_view_all_tasks_clicked, Toast.LENGTH_SHORT)
                    .show()
            (activity as? MainActivity)?.navigateToTasksScreen()
        }

        binding.btnViewAllGroupsHome.setOnClickListener {
            Toast.makeText(context, R.string.button_view_all_groups_clicked, Toast.LENGTH_SHORT)
                    .show()
            (activity as? MainActivity)?.navigateToGroupsScreen()
        }

        binding.btnViewAllScheduleHome.setOnClickListener {
            Toast.makeText(context, R.string.button_view_all_schedule_clicked, Toast.LENGTH_SHORT)
                    .show()
            (activity as? MainActivity)?.navigateToCalendarScreen()
        }

        binding.btnNewTaskHome.setOnClickListener {
            Toast.makeText(context, R.string.new_task_clicked, Toast.LENGTH_SHORT).show()
            // TODO: Implement navigation or dialog for new task
        }

        binding.btnCalendarQuickHome.setOnClickListener {
            Toast.makeText(context, R.string.calendar_quick_action_clicked, Toast.LENGTH_SHORT)
                    .show()
            (activity as? MainActivity)?.navigateToCalendarScreen()
        }

        binding.btnGroupsQuickHome.setOnClickListener {
            Toast.makeText(context, R.string.groups_quick_action_clicked, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToGroupsScreen()
        }

        binding.btnAiAssistantHome.setOnClickListener {
            Toast.makeText(context, R.string.ai_assistant_clicked, Toast.LENGTH_SHORT).show()
            // TODO: Implement navigation to AI Assistant
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
