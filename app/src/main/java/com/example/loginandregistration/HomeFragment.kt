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
        // Show loading state
        showLoadingState()

        // Set up real-time listeners for continuous updates
        setupRealTimeListeners()
    }

    private fun showLoadingState() {
        // Show loading indicators
        binding.tvTasksDueCount.text = "..."
        binding.tvGroupsCount.text = "..."
        binding.tvSessionsCount.text = "..."
        binding.tvAiPromptsLeft.text = "..."
        binding.progressBarAiUsage.progress = 0
        binding.tvAiUsageDetails.text = getString(R.string.loading)
    }

    private fun setupRealTimeListeners() {
        val dashboardRepository = com.example.loginandregistration.repository.DashboardRepository()

        // Set up real-time listener for task stats
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                dashboardRepository.getTaskStats().collect { taskStats ->
                    // Update UI on main thread
                    val tasksDue = taskStats.overdue + taskStats.dueToday
                    binding.tvTasksDueCount.text = tasksDue.toString()

                    // Check if we should show empty state
                    val total = taskStats.overdue + taskStats.dueToday + taskStats.completed
                    checkAndShowEmptyState(total)
                }
            } catch (e: Exception) {
                // Handle error - show 0 as fallback
                binding.tvTasksDueCount.text = "0"
            }
        }

        // Set up real-time listener for group count
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                dashboardRepository.getGroupCount().collect { groupCount ->
                    // Update UI on main thread
                    binding.tvGroupsCount.text = groupCount.toString()
                }
            } catch (e: Exception) {
                // Handle error - show 0 as fallback
                binding.tvGroupsCount.text = "0"
            }
        }

        // Set up real-time listener for session stats
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                dashboardRepository.getSessionStats().collect { sessionStats ->
                    // Update UI on main thread
                    binding.tvSessionsCount.text = sessionStats.totalSessions.toString()
                }
            } catch (e: Exception) {
                // Handle error - show 0 as fallback
                binding.tvSessionsCount.text = "0"
            }
        }

        // Set up real-time listener for AI usage stats
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                dashboardRepository.getAIUsageStats().collect { aiStats ->
                    // Update UI on main thread
                    val remaining = aiStats.limit - aiStats.used
                    binding.tvAiPromptsLeft.text =
                            getString(R.string.home_ai_prompts_left_template, remaining)

                    val progress =
                            if (aiStats.limit > 0) {
                                (aiStats.used * 100) / aiStats.limit
                            } else {
                                0
                            }
                    binding.progressBarAiUsage.progress = progress

                    binding.tvAiUsageDetails.text =
                            getString(
                                    R.string.home_ai_prompts_usage_template,
                                    aiStats.used,
                                    aiStats.limit
                            )
                }
            } catch (e: Exception) {
                // Handle error - show default values
                binding.tvAiPromptsLeft.text = getString(R.string.home_ai_prompts_left_template, 10)
                binding.progressBarAiUsage.progress = 0
                binding.tvAiUsageDetails.text =
                        getString(R.string.home_ai_prompts_usage_template, 0, 10)
            }
        }
    }

    private fun checkAndShowEmptyState(totalTasks: Int) {
        // For now, we'll just log this - empty state can be shown in a future enhancement
        // when we have more comprehensive data about user's overall activity
        if (totalTasks == 0) {
            // Could show empty state view here
            android.util.Log.d("HomeFragment", "User has no tasks")
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
