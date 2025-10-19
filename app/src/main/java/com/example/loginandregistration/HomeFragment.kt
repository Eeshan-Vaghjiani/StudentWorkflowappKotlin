package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.databinding.FragmentHomeBinding
import com.example.loginandregistration.models.DashboardStats
import com.example.loginandregistration.repository.GroupRepository
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.repository.UserRepository
import com.example.loginandregistration.utils.ErrorMessages
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var auth: FirebaseAuth

    // Repositories for fetching real-time data
    private lateinit var taskRepository: TaskRepository
    private lateinit var groupRepository: GroupRepository
    private lateinit var userRepository: UserRepository

    // Job for managing coroutines lifecycle
    private var statsJob: Job? = null

    // Current dashboard stats
    private var currentStats = DashboardStats()

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // Initialize repositories
        taskRepository = TaskRepository(requireContext())
        groupRepository = GroupRepository()
        userRepository = UserRepository()
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
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not authenticated, cannot load dashboard data")
            showErrorState("Please sign in to view your dashboard")
            return
        }

        // Show loading state
        showLoadingState()

        // Cancel any existing stats job
        statsJob?.cancel()

        // Set up real-time listeners for continuous updates
        statsJob =
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // Collect task statistics
                        launch { collectTaskStats(userId) }

                        // Collect group statistics
                        launch { collectGroupStats(userId) }

                        // Collect AI usage statistics
                        launch { collectAIStats(userId) }

                        // Collect session statistics (placeholder for now)
                        launch { collectSessionStats(userId) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading dashboard stats", e)
                        showErrorState(e.message ?: "Failed to load dashboard data")
                    }
                }
    }

    private fun showLoadingState() {
        Log.d(TAG, "Showing loading state")
        currentStats = currentStats.copy(isLoading = true, error = null)

        // Show loading indicators
        binding.tvTasksDueCount.text = "..."
        binding.tvGroupsCount.text = "..."
        binding.tvSessionsCount.text = "..."
        binding.tvAiPromptsLeft.text = "..."
        binding.progressBarAiUsage.progress = 0
        binding.tvAiUsageDetails.text = getString(R.string.loading)
    }

    /** Show error state with fallback values Displays error message and allows user to retry */
    private fun showErrorState(message: String) {
        Log.e(TAG, "Showing error state: $message")
        currentStats = currentStats.copy(isLoading = false, error = message)

        // Show error indicators with fallback values
        binding.tvTasksDueCount.text = "0"
        binding.tvGroupsCount.text = "0"
        binding.tvSessionsCount.text = "0"
        binding.tvAiPromptsLeft.text = getString(R.string.home_ai_prompts_left_template, 10)
        binding.progressBarAiUsage.progress = 0
        binding.tvAiUsageDetails.text = getString(R.string.home_ai_prompts_usage_template, 0, 10)

        // Show toast with error message and retry option
        val errorMessage =
                when {
                    message.contains("permission", ignoreCase = true) ->
                            ErrorMessages.PERMISSION_DENIED
                    message.contains("network", ignoreCase = true) ||
                            message.contains("connection", ignoreCase = true) ->
                            ErrorMessages.NETWORK_ERROR
                    message.contains("index", ignoreCase = true) -> ErrorMessages.INDEX_MISSING
                    else -> message
                }

        Toast.makeText(
                        context,
                        "$errorMessage\n${ErrorMessages.PULL_TO_REFRESH}",
                        Toast.LENGTH_LONG
                )
                .show()
    }

    /** Retry loading dashboard data Called when user wants to retry after an error */
    private fun retryLoadDashboardData() {
        Log.d(TAG, "Retrying dashboard data load")
        loadDashboardData()
    }

    /**
     * Collect real-time task statistics from Firestore Calculates total, completed, pending, and
     * overdue task counts
     */
    private suspend fun collectTaskStats(userId: String) {
        taskRepository
                .getUserTasksFlow()
                .catch { e ->
                    Log.e(TAG, "Error collecting task stats", e)
                    val errorMessage =
                            when {
                                e.message?.contains("PERMISSION_DENIED") == true ->
                                        ErrorMessages.PERMISSION_DENIED
                                e.message?.contains("UNAVAILABLE") == true ->
                                        ErrorMessages.NETWORK_ERROR
                                e.message?.contains("FAILED_PRECONDITION") == true ->
                                        ErrorMessages.INDEX_MISSING
                                else -> ErrorMessages.TASK_LOAD_FAILED
                            }
                    showErrorState(errorMessage)
                    // Update with zero values on error
                    updateTaskStatsUI(0, 0, 0, 0)
                }
                .collect { tasks ->
                    Log.d(TAG, "Received ${tasks.size} tasks from Firestore")

                    val now = Timestamp.now()
                    var totalTasks = tasks.size
                    var completedTasks = 0
                    var pendingTasks = 0
                    var overdueTasks = 0
                    var tasksDue = 0

                    tasks.forEach { task ->
                        when {
                            task.status == "completed" -> completedTasks++
                            task.dueDate != null && task.dueDate!! < now -> {
                                overdueTasks++
                                tasksDue++
                            }
                            task.dueDate != null && isSameDay(task.dueDate!!, now) -> {
                                tasksDue++
                            }
                            else -> pendingTasks++
                        }
                    }

                    // Update stats model
                    currentStats =
                            currentStats.copy(
                                    totalTasks = totalTasks,
                                    completedTasks = completedTasks,
                                    pendingTasks = pendingTasks,
                                    overdueTasks = overdueTasks,
                                    tasksDue = tasksDue,
                                    isLoading = false
                            )

                    // Update UI
                    updateTaskStatsUI(tasksDue, totalTasks, completedTasks, overdueTasks)
                }
    }

    /** Update task statistics in the UI */
    private fun updateTaskStatsUI(tasksDue: Int, total: Int, completed: Int, overdue: Int) {
        Log.d(
                TAG,
                "Updating task stats UI: due=$tasksDue, total=$total, completed=$completed, overdue=$overdue"
        )
        binding.tvTasksDueCount.text = tasksDue.toString()
    }

    /** Helper method to check if two timestamps are on the same day */
    private fun isSameDay(date1: Timestamp, date2: Timestamp): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1.toDate() }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2.toDate() }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /**
     * Collect real-time group statistics from Firestore Counts active groups where user is a member
     */
    private suspend fun collectGroupStats(userId: String) {
        groupRepository
                .getUserGroupsFlow()
                .catch { e ->
                    Log.e(TAG, "Error collecting group stats", e)
                    val errorMessage =
                            when {
                                e.message?.contains("PERMISSION_DENIED") == true ->
                                        ErrorMessages.PERMISSION_DENIED
                                e.message?.contains("UNAVAILABLE") == true ->
                                        ErrorMessages.NETWORK_ERROR
                                else -> ErrorMessages.GROUP_LOAD_FAILED
                            }
                    showErrorState(errorMessage)
                    // Update with zero value on error
                    updateGroupStatsUI(0)
                }
                .collect { groups ->
                    Log.d(TAG, "Received ${groups.size} groups from Firestore")

                    val activeGroups = groups.count { it.isActive }

                    // Update stats model
                    currentStats = currentStats.copy(activeGroups = activeGroups, isLoading = false)

                    // Update UI
                    updateGroupStatsUI(activeGroups)
                }
    }

    /** Update group statistics in the UI */
    private fun updateGroupStatsUI(activeGroups: Int) {
        Log.d(TAG, "Updating group stats UI: activeGroups=$activeGroups")
        binding.tvGroupsCount.text = activeGroups.toString()
    }

    /** Collect AI usage statistics from user profile Extracts aiUsageCount from user document */
    private suspend fun collectAIStats(userId: String) {
        userRepository
                .getCurrentUserProfileFlow()
                .catch { e ->
                    Log.e(TAG, "Error collecting AI stats", e)
                    val errorMessage =
                            when {
                                e.message?.contains("PERMISSION_DENIED") == true ->
                                        ErrorMessages.PERMISSION_DENIED
                                e.message?.contains("UNAVAILABLE") == true ->
                                        ErrorMessages.NETWORK_ERROR
                                else -> ErrorMessages.PROFILE_LOAD_FAILED
                            }
                    showErrorState(errorMessage)
                    // Update with default values on error
                    updateAIStatsUI(0, 10)
                }
                .collect { user ->
                    if (user != null) {
                        Log.d(TAG, "Received user profile with AI usage: ${user.aiUsageCount}")

                        val aiUsageCount = user.aiUsageCount
                        val aiUsageLimit = 10 // Default limit

                        // Update stats model
                        currentStats =
                                currentStats.copy(
                                        aiUsageCount = aiUsageCount,
                                        aiUsageLimit = aiUsageLimit,
                                        isLoading = false
                                )

                        // Update UI
                        updateAIStatsUI(aiUsageCount, aiUsageLimit)
                    } else {
                        Log.w(TAG, "User profile not found")
                        updateAIStatsUI(0, 10)
                    }
                }
    }

    /** Update AI usage statistics in the UI */
    private fun updateAIStatsUI(used: Int, limit: Int) {
        Log.d(TAG, "Updating AI stats UI: used=$used, limit=$limit")

        val remaining = (limit - used).coerceAtLeast(0)
        binding.tvAiPromptsLeft.text = getString(R.string.home_ai_prompts_left_template, remaining)

        val progress =
                if (limit > 0) {
                    (used * 100) / limit
                } else {
                    0
                }
        binding.progressBarAiUsage.progress = progress

        binding.tvAiUsageDetails.text =
                getString(R.string.home_ai_prompts_usage_template, used, limit)
    }

    /** Collect session statistics Placeholder for future session tracking feature */
    private suspend fun collectSessionStats(userId: String) {
        Log.d(TAG, "collectSessionStats - using placeholder value")
        binding.tvSessionsCount.text = "0"
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
        // Cancel stats collection job
        statsJob?.cancel()
        _binding = null
    }
}
