package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.utils.ThemeUtils
import com.example.loginandregistration.utils.collectWithLifecycle
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

        companion object {
                private const val TAG = "TasksFragment"
        }

        private lateinit var recyclerTasks: RecyclerView
        private lateinit var taskAdapter: TaskAdapter
        private lateinit var taskRepository: TaskRepository
        private lateinit var swipeRefreshLayout: SwipeRefreshLayout
        private var currentView: View? = null
        private var currentFilter: String = "all"

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
                val view = inflater.inflate(R.layout.fragment_tasks, container, false)
                currentView = view

                taskRepository = TaskRepository(requireContext())
                setupViews(view)
                setupRecyclerView(view)
                setupClickListeners(view)
                setupRealTimeListeners()

                return view
        }

        private fun setupViews(view: View) {
                recyclerTasks = view.findViewById(R.id.recycler_tasks)
                swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

                // Setup swipe refresh
                swipeRefreshLayout.setOnRefreshListener { refreshData() }
        }

        private fun setupRecyclerView(view: View) {
                taskAdapter =
                        TaskAdapter(emptyList()) { task ->
                                Toast.makeText(
                                                context,
                                                getString(R.string.task_clicked, task.title),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                        }

                recyclerTasks.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = taskAdapter
                }
        }

        private fun setupRealTimeListeners() {
                // Use lifecycle-aware collection that automatically stops when Fragment is stopped
                // and resumes when Fragment is started again

                // Real-time listener for task statistics
                taskRepository.getTaskStatsFlow().collectWithLifecycle(viewLifecycleOwner) { stats
                        ->
                        Log.d(
                                TAG,
                                "Received task stats: overdue=${stats.overdue}, dueToday=${stats.dueToday}, completed=${stats.completed}"
                        )
                        currentView?.findViewById<android.widget.TextView>(R.id.tv_overdue_count)
                                ?.text = stats.overdue.toString()
                        currentView?.findViewById<android.widget.TextView>(R.id.tv_due_today_count)
                                ?.text = stats.dueToday.toString()
                        currentView?.findViewById<android.widget.TextView>(R.id.tv_completed_count)
                                ?.text = stats.completed.toString()

                        // Hide refresh indicator when data loads
                        swipeRefreshLayout.isRefreshing = false
                }

                // Real-time listener for user's tasks
                taskRepository.getUserTasksFlow().collectWithLifecycle(viewLifecycleOwner) {
                        firebaseTasks ->
                        Log.d(TAG, "Received ${firebaseTasks.size} tasks")
                        updateTasksList(firebaseTasks)
                        // Hide refresh indicator when data loads
                        swipeRefreshLayout.isRefreshing = false
                }
        }

        override fun onStart() {
                super.onStart()
                Log.d(TAG, "onStart - Firestore listeners will be attached")
        }

        override fun onStop() {
                super.onStop()
                Log.d(TAG, "onStop - Firestore listeners will be detached")
        }

        private fun refreshData() {
                // Show loading indicator
                swipeRefreshLayout.isRefreshing = true

                // The real-time listeners will automatically update the data
                // and hide the refresh indicator when complete
                lifecycleScope.launch {
                        try {
                                // Force a refresh by re-fetching data
                                // The Flow listeners will pick up the changes
                                taskRepository.getUserTasksFlow().collect { firebaseTasks ->
                                        updateTasksList(firebaseTasks)
                                        swipeRefreshLayout.isRefreshing = false
                                        return@collect // Only collect once for refresh
                                }
                        } catch (e: Exception) {
                                Log.e("TasksFragment", "Error refreshing tasks: ${e.message}")
                                Toast.makeText(
                                                context,
                                                "Error refreshing tasks",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                swipeRefreshLayout.isRefreshing = false
                        }
                }
        }

        private fun updateTasksList(firebaseTasks: List<FirebaseTask>) {
                val filteredTasks =
                        when (currentFilter) {
                                "personal" -> firebaseTasks.filter { it.category == "personal" }
                                "group" -> firebaseTasks.filter { it.category == "group" }
                                "assignments" ->
                                        firebaseTasks.filter { it.category == "assignment" }
                                else -> firebaseTasks
                        }

                val displayTasks =
                        filteredTasks.map { firebaseTask ->
                                Task(
                                        id = firebaseTask.id.hashCode(),
                                        title = firebaseTask.title,
                                        subtitle =
                                                "${firebaseTask.subject} • ${formatDueDate(firebaseTask.dueDate)}",
                                        status =
                                                firebaseTask.status.replaceFirstChar {
                                                        it.uppercase()
                                                },
                                        iconColor = getStatusColor(firebaseTask.status),
                                        statusColor = getStatusColor(firebaseTask.status)
                                )
                        }

                taskAdapter =
                        TaskAdapter(displayTasks) { task ->
                                Toast.makeText(
                                                context,
                                                getString(R.string.task_clicked, task.title),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                        }
                recyclerTasks.adapter = taskAdapter
        }

        // This is now handled by the updateTasksList method above

        private fun setupClickListeners(view: View) {
                // Header buttons
                view.findViewById<ImageButton>(R.id.btn_filter)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.filter_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<ImageButton>(R.id.btn_search)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.search_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<ImageButton>(R.id.btn_add_task)?.setOnClickListener {
                        showCreateTaskDialog()
                }

                // Category buttons
                view.findViewById<MaterialButton>(R.id.btn_all_tasks)?.setOnClickListener {
                        currentFilter = "all"
                        // No need to fetch tasks manually, the real-time listener will update with
                        // the new filter
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "All Tasks"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_personal)?.setOnClickListener {
                        currentFilter = "personal"
                        // No need to fetch tasks manually, the real-time listener will update with
                        // the new filter
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "Personal"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_group)?.setOnClickListener {
                        currentFilter = "group"
                        // No need to fetch tasks manually, the real-time listener will update with
                        // the new filter
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "Group"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_assignments)?.setOnClickListener {
                        currentFilter = "assignments"
                        // No need to fetch tasks manually, the real-time listener will update with
                        // the new filter
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "Assignments"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                // View toggle buttons
                view.findViewById<ImageButton>(R.id.btn_list_view)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.view_changed, "List"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<ImageButton>(R.id.btn_kanban_view)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.view_changed, "Kanban"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                // Quick action buttons
                view.findViewById<MaterialButton>(R.id.btn_new_task)?.setOnClickListener {
                        showCreateTaskDialog()
                }

                view.findViewById<MaterialButton>(R.id.btn_kanban_view_action)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.view_changed, "Kanban"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_ai_assistant)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.ai_assistant_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_export)?.setOnClickListener {
                        Toast.makeText(context, "Export clicked", Toast.LENGTH_SHORT).show()
                }
        }

        private fun getStatusColor(status: String): String {
                return ThemeUtils.getStatusColorHex(requireContext(), status)
        }

        private fun formatDueDate(dueDate: com.google.firebase.Timestamp?): String {
                if (dueDate == null) return "No due date"

                val now = System.currentTimeMillis()
                val due = dueDate.toDate().time
                val diff = due - now

                return when {
                        diff < 0 -> "Overdue"
                        diff < 24 * 60 * 60 * 1000 -> "Due today"
                        diff < 2 * 24 * 60 * 60 * 1000 -> "Due tomorrow"
                        else -> "Due in ${diff / (24 * 60 * 60 * 1000)} days"
                }
        }

        private fun getDummyTasks(): List<Task> {
                return listOf(
                        Task(
                                id = 1,
                                title = "Research Paper Draft",
                                subtitle = "Overdue • Computer Science • Due 2 days ago",
                                status = "Overdue",
                                iconColor = "#FF3B30",
                                statusColor = "#FF3B30"
                        ),
                        Task(
                                id = 2,
                                title = "Math Assignment",
                                subtitle = "Due today • Mathematics • Due in 4 hours",
                                status = "Due Today",
                                iconColor = "#FF9500",
                                statusColor = "#FF9500"
                        ),
                        Task(
                                id = 3,
                                title = "Lab Report",
                                subtitle = "Due tomorrow • Chemistry • Due in 1 day",
                                status = "Due Tomorrow",
                                iconColor = "#007AFF",
                                statusColor = "#007AFF"
                        ),
                        Task(
                                id = 4,
                                title = "Literature Review",
                                subtitle = "Due in 3 days • English • Due in 3 days",
                                status = "Due Later",
                                iconColor = "#34C759",
                                statusColor = "#34C759"
                        ),
                        Task(
                                id = 5,
                                title = "Physics Quiz",
                                subtitle = "Completed • Physics • Completed yesterday",
                                status = "Completed",
                                iconColor = "#8E8E93",
                                statusColor = "#8E8E93"
                        )
                )
        }

        private fun showCreateTaskDialog() {
                val dialogView =
                        LayoutInflater.from(context).inflate(R.layout.dialog_create_task, null)
                val dialog =
                        android.app.AlertDialog.Builder(requireContext())
                                .setView(dialogView)
                                .create()

                val etTaskTitle =
                        dialogView.findViewById<
                                com.google.android.material.textfield.TextInputEditText>(
                                R.id.et_task_title
                        )
                val etTaskDescription =
                        dialogView.findViewById<
                                com.google.android.material.textfield.TextInputEditText>(
                                R.id.et_task_description
                        )
                val etTaskSubject =
                        dialogView.findViewById<
                                com.google.android.material.textfield.TextInputEditText>(
                                R.id.et_task_subject
                        )
                val etDueDate =
                        dialogView.findViewById<
                                com.google.android.material.textfield.TextInputEditText>(
                                R.id.et_due_date
                        )

                val chipGroupCategory =
                        dialogView.findViewById<com.google.android.material.chip.ChipGroup>(
                                R.id.chip_group_category
                        )
                val chipGroupPriority =
                        dialogView.findViewById<com.google.android.material.chip.ChipGroup>(
                                R.id.chip_group_priority
                        )
                val chipGroupTask =
                        dialogView.findViewById<com.google.android.material.chip.Chip>(
                                R.id.chip_group_task
                        )
                val layoutGroupSelection =
                        dialogView.findViewById<
                                com.google.android.material.textfield.TextInputLayout>(
                                R.id.layout_group_selection
                        )
                val spinnerGroup =
                        dialogView.findViewById<android.widget.AutoCompleteTextView>(
                                R.id.spinner_group
                        )

                val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
                val btnCreate = dialogView.findViewById<MaterialButton>(R.id.btn_create_task)

                // Setup group selection visibility
                chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
                        layoutGroupSelection.visibility =
                                if (checkedIds.contains(R.id.chip_group_task)) {
                                        android.view.View.VISIBLE
                                } else {
                                        android.view.View.GONE
                                }
                }

                // Load user's groups for selection
                lifecycleScope.launch {
                        try {
                                val groupRepository =
                                        com.example.loginandregistration.repository
                                                .GroupRepository()
                                val userGroups = groupRepository.getUserGroups()
                                val groupNames = userGroups.map { it.name }
                                val adapter =
                                        android.widget.ArrayAdapter(
                                                requireContext(),
                                                android.R.layout.simple_dropdown_item_1line,
                                                groupNames
                                        )
                                spinnerGroup.setAdapter(adapter)
                        } catch (e: Exception) {
                                // Handle error silently
                        }
                }

                // Setup due date picker
                etDueDate.setOnClickListener {
                        val calendar = java.util.Calendar.getInstance()
                        val datePickerDialog =
                                android.app.DatePickerDialog(
                                        requireContext(),
                                        { _, year, month, dayOfMonth ->
                                                val selectedDate = java.util.Calendar.getInstance()
                                                selectedDate.set(year, month, dayOfMonth)
                                                val dateFormat =
                                                        java.text.SimpleDateFormat(
                                                                "MMM dd, yyyy",
                                                                java.util.Locale.getDefault()
                                                        )
                                                etDueDate.setText(
                                                        dateFormat.format(selectedDate.time)
                                                )
                                        },
                                        calendar.get(java.util.Calendar.YEAR),
                                        calendar.get(java.util.Calendar.MONTH),
                                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                )
                        datePickerDialog.show()
                }

                btnCancel.setOnClickListener { dialog.dismiss() }

                btnCreate.setOnClickListener {
                        val title = etTaskTitle.text.toString().trim()
                        val description = etTaskDescription.text.toString().trim()
                        val subject = etTaskSubject.text.toString().trim()
                        val dueDateText = etDueDate.text.toString().trim()

                        if (title.isEmpty()) {
                                Toast.makeText(
                                                context,
                                                getString(R.string.fill_required_fields),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                return@setOnClickListener
                        }

                        // Get selected category
                        val category =
                                when (chipGroupCategory.checkedChipId) {
                                        R.id.chip_personal -> "personal"
                                        R.id.chip_group_task -> "group"
                                        R.id.chip_assignment -> "assignment"
                                        else -> "personal"
                                }

                        // Get selected priority
                        val priority =
                                when (chipGroupPriority.checkedChipId) {
                                        R.id.chip_low -> "low"
                                        R.id.chip_medium -> "medium"
                                        R.id.chip_high -> "high"
                                        else -> "medium"
                                }

                        // Parse due date
                        var dueDate: com.google.firebase.Timestamp? = null
                        if (dueDateText.isNotEmpty()) {
                                try {
                                        val dateFormat =
                                                java.text.SimpleDateFormat(
                                                        "MMM dd, yyyy",
                                                        java.util.Locale.getDefault()
                                                )
                                        val date = dateFormat.parse(dueDateText)
                                        if (date != null) {
                                                dueDate = com.google.firebase.Timestamp(date)
                                        }
                                } catch (e: Exception) {
                                        // Invalid date format, ignore
                                }
                        }

                        lifecycleScope.launch {
                                val task =
                                        com.example.loginandregistration.models.FirebaseTask(
                                                title = title,
                                                description = description,
                                                subject = subject,
                                                category = category,
                                                status = "pending",
                                                priority = priority,
                                                dueDate = dueDate
                                        )

                                val taskId = taskRepository.createTask(task)
                                if (taskId != null) {
                                        Toast.makeText(
                                                        context,
                                                        getString(
                                                                R.string.task_created_successfully
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        dialog.dismiss()
                                        // Data will refresh automatically via real-time listener
                                } else {
                                        Toast.makeText(
                                                        context,
                                                        getString(R.string.error_creating_task),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                }
                        }
                }

                dialog.show()
        }
}
