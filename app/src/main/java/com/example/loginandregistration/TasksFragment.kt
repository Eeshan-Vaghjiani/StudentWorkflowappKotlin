package com.example.loginandregistration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.utils.ErrorHandler
import com.example.loginandregistration.utils.ErrorMessages
import com.example.loginandregistration.utils.ThemeUtils
import com.example.loginandregistration.utils.collectWithLifecycle
import com.example.loginandregistration.viewmodels.TasksViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

        companion object {
                private const val TAG = "TasksFragment"
        }

        private val viewModel: TasksViewModel by viewModels()
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
                observeViewModel()

                return view
        }

        private fun setupViews(view: View) {
                recyclerTasks = view.findViewById(R.id.recycler_tasks)
                swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

                // Setup swipe refresh
                swipeRefreshLayout.setOnRefreshListener { refreshData() }
        }

        private fun setupRecyclerView(view: View) {
                taskAdapter = TaskAdapter { task ->
                        Toast.makeText(context, "Clicked: ${task.title}", Toast.LENGTH_SHORT).show()
                }

                recyclerTasks.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = taskAdapter
                }
        }

        private fun observeViewModel() {
                // Observe tasks from ViewModel
                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.tasks.collect { firebaseTasks ->
                                Log.d(TAG, "Received ${firebaseTasks.size} tasks from ViewModel")

                                // Filter tasks based on current filter
                                val filteredTasks =
                                        when (currentFilter) {
                                                "personal" ->
                                                        firebaseTasks.filter {
                                                                it.category == "personal"
                                                        }
                                                "group" ->
                                                        firebaseTasks.filter {
                                                                it.category == "group"
                                                        }
                                                "assignment" ->
                                                        firebaseTasks.filter {
                                                                it.category == "assignment"
                                                        }
                                                else -> firebaseTasks
                                        }

                                updateTasksList(filteredTasks)
                                showEmptyStateIfNeeded(filteredTasks.isEmpty())
                                swipeRefreshLayout.isRefreshing = false
                        }
                }

                // Observe loading state
                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.isLoading.collect { isLoading ->
                                swipeRefreshLayout.isRefreshing = isLoading
                        }
                }

                // Observe errors
                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.error.collect { error ->
                                error?.let {
                                        showError(it)
                                        viewModel.clearError()
                                }
                        }
                }

                // Observe success messages
                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.successMessage.collect { message ->
                                message?.let {
                                        showSuccess(it)
                                        viewModel.clearSuccessMessage()
                                }
                        }
                }

                // Real-time listener for task statistics (still using repository directly for
                // stats)
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
                }
        }

        private fun showError(error: ErrorHandler.AppError) {
                currentView?.let { view ->
                        val message =
                                when (error) {
                                        is ErrorHandler.AppError.PermissionError ->
                                                ErrorMessages.PERMISSION_DENIED
                                        is ErrorHandler.AppError.NetworkError ->
                                                ErrorMessages.NETWORK_ERROR
                                        is ErrorHandler.AppError.FirestoreError -> {
                                                // Check if this is a FAILED_PRECONDITION error
                                                // (missing index)
                                                val exception = error.exception
                                                if (exception is
                                                                com.google.firebase.firestore.FirebaseFirestoreException &&
                                                                exception.code ==
                                                                        com.google.firebase
                                                                                .firestore
                                                                                .FirebaseFirestoreException
                                                                                .Code
                                                                                .FAILED_PRECONDITION
                                                ) {
                                                        ErrorMessages.INDEX_MISSING
                                                } else {
                                                        ErrorMessages.TASK_LOAD_FAILED
                                                }
                                        }
                                        is ErrorHandler.AppError.UnknownError ->
                                                ErrorMessages.GENERIC_ERROR
                                        else -> ErrorMessages.UNEXPECTED_ERROR
                                }

                        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                                .setAction("Retry") { viewModel.loadUserTasks() }
                                .show()
                }
        }

        private fun showSuccess(message: String) {
                currentView?.let { view ->
                        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
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
                // Trigger a manual refresh through the ViewModel
                viewModel.loadUserTasks()
        }

        private fun updateTasksList(firebaseTasks: List<FirebaseTask>) {
                val displayTasks =
                        firebaseTasks.map { firebaseTask ->
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

                taskAdapter = TaskAdapter { task ->
                        Toast.makeText(context, "Clicked: ${task.title}", Toast.LENGTH_SHORT).show()
                }
                taskAdapter.submitList(displayTasks)
                recyclerTasks.adapter = taskAdapter
        }

        private fun showEmptyStateIfNeeded(isEmpty: Boolean) {
                currentView?.let { view ->
                        val emptyStateView = view.findViewById<View>(R.id.empty_state_layout)
                        val emptyStateText = view.findViewById<TextView>(R.id.empty_state_text)

                        if (isEmpty) {
                                emptyStateView?.visibility = View.VISIBLE
                                recyclerTasks.visibility = View.GONE

                                val message =
                                        when (currentFilter) {
                                                "personal" -> "No personal tasks yet"
                                                "group" -> "No group tasks yet"
                                                "assignment" -> "No assignments yet"
                                                else -> "No tasks yet"
                                        }
                                emptyStateText?.text = message
                        } else {
                                emptyStateView?.visibility = View.GONE
                                recyclerTasks.visibility = View.VISIBLE
                        }
                }
        }

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
                        // Trigger UI update with current tasks
                        val currentTasks = viewModel.tasks.value
                        updateTasksList(currentTasks)
                        showEmptyStateIfNeeded(currentTasks.isEmpty())
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "All Tasks"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_personal)?.setOnClickListener {
                        currentFilter = "personal"
                        // Trigger UI update with filtered tasks
                        val filteredTasks =
                                viewModel.tasks.value.filter { it.category == "personal" }
                        updateTasksList(filteredTasks)
                        showEmptyStateIfNeeded(filteredTasks.isEmpty())
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "Personal"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_group)?.setOnClickListener {
                        currentFilter = "group"
                        // Trigger UI update with filtered tasks
                        val filteredTasks = viewModel.tasks.value.filter { it.category == "group" }
                        updateTasksList(filteredTasks)
                        showEmptyStateIfNeeded(filteredTasks.isEmpty())
                        Toast.makeText(
                                        context,
                                        getString(R.string.category_selected, "Group"),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_assignments)?.setOnClickListener {
                        currentFilter = "assignment"
                        // Trigger UI update with filtered tasks
                        val filteredTasks =
                                viewModel.tasks.value.filter { it.category == "assignment" }
                        updateTasksList(filteredTasks)
                        showEmptyStateIfNeeded(filteredTasks.isEmpty())
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
                        // Launch AI Assistant Activity
                        val intent =
                                android.content.Intent(
                                        requireContext(),
                                        AIAssistantActivity::class.java
                                )
                        startActivity(intent)
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
                val btnCreateWithAI =
                        dialogView.findViewById<MaterialButton>(R.id.btn_create_with_ai)

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
                                val result = groupRepository.getUserGroups()
                                val userGroups = result.getOrElse { emptyList() }
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

                // Handle "Create with AI" button
                btnCreateWithAI.setOnClickListener {
                        if (!viewModel.isAIServiceAvailable()) {
                                Toast.makeText(
                                                context,
                                                "AI Assistant is not configured. Please add GEMINI_API_KEY to local.properties",
                                                Toast.LENGTH_LONG
                                        )
                                        .show()
                                return@setOnClickListener
                        }

                        // Show AI prompt dialog
                        showAIPromptDialog(dialog)
                }

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

                        // Create task using ViewModel
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

                        viewModel.createTask(task)
                        dialog.dismiss()
                        // Success/error messages will be shown via ViewModel observers
                }

                dialog.show()
        }

        /** Show AI prompt dialog for creating tasks with AI assistance */
        private fun showAIPromptDialog(parentDialog: android.app.AlertDialog) {
                val aiDialogView =
                        LayoutInflater.from(context).inflate(R.layout.dialog_ai_prompt, null)
                val aiDialog =
                        android.app.AlertDialog.Builder(requireContext())
                                .setView(aiDialogView)
                                .create()

                val etAIPrompt =
                        aiDialogView.findViewById<
                                com.google.android.material.textfield.TextInputEditText>(
                                R.id.et_ai_prompt
                        )
                val btnAICancel = aiDialogView.findViewById<MaterialButton>(R.id.btn_ai_cancel)
                val btnAICreate = aiDialogView.findViewById<MaterialButton>(R.id.btn_ai_create)
                val progressBar =
                        aiDialogView.findViewById<android.widget.ProgressBar>(R.id.progress_bar)

                btnAICancel.setOnClickListener { aiDialog.dismiss() }

                btnAICreate.setOnClickListener {
                        val prompt = etAIPrompt.text.toString().trim()

                        if (prompt.isEmpty()) {
                                Toast.makeText(
                                                context,
                                                "Please describe the task you want to create",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                return@setOnClickListener
                        }

                        // Show loading state
                        progressBar.visibility = android.view.View.VISIBLE
                        btnAICreate.isEnabled = false
                        btnAICancel.isEnabled = false
                        etAIPrompt.isEnabled = false

                        // Create task from AI
                        viewModel.createTaskFromAI(prompt)

                        // Observe the result
                        viewLifecycleOwner.lifecycleScope.launch {
                                // Wait a bit for the operation to complete
                                kotlinx.coroutines.delay(500)

                                // Check if loading is done
                                viewModel.isLoading.collect { isLoading ->
                                        if (!isLoading) {
                                                progressBar.visibility = android.view.View.GONE
                                                btnAICreate.isEnabled = true
                                                btnAICancel.isEnabled = true
                                                etAIPrompt.isEnabled = true

                                                // Close both dialogs on success
                                                if (viewModel.successMessage.value != null) {
                                                        aiDialog.dismiss()
                                                        parentDialog.dismiss()
                                                }
                                        }
                                }
                        }
                }

                aiDialog.show()
        }
}
