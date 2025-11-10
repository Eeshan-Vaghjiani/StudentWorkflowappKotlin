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
import com.example.loginandregistration.databinding.FragmentTasksBinding
import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.utils.ErrorHandler
import com.example.loginandregistration.utils.ThemeUtils
import com.example.loginandregistration.utils.collectWithLifecycle
import com.example.loginandregistration.validation.TaskCreationValidator
import com.example.loginandregistration.validation.TaskValidationResult
import com.example.loginandregistration.viewmodels.TasksViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

        companion object {
                private const val TAG = "TasksFragment"
        }

        private var _binding: FragmentTasksBinding? = null
        private val binding
                get() = _binding

        private val viewModel: TasksViewModel by viewModels()
        private lateinit var taskAdapter: TaskAdapter
        private lateinit var taskRepository: TaskRepository
        private lateinit var errorStateManager:
                com.example.loginandregistration.utils.ErrorStateManager
        private val taskValidator = TaskCreationValidator()
        private val auth = FirebaseAuth.getInstance()
        private var currentFilter: String = "all"
        private var isKanbanView: Boolean = false
        private var taskMapping = mutableMapOf<String, FirebaseTask>()

        // Kanban adapters
        private lateinit var pendingAdapter:
                com.example.loginandregistration.adapters.KanbanTaskAdapter
        private lateinit var inProgressAdapter:
                com.example.loginandregistration.adapters.KanbanTaskAdapter
        private lateinit var completedAdapter:
                com.example.loginandregistration.adapters.KanbanTaskAdapter

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View {
                _binding = FragmentTasksBinding.inflate(inflater, container, false)

                taskRepository = TaskRepository(requireContext())
                errorStateManager =
                        com.example.loginandregistration.utils.ErrorStateManager(requireContext())
                setupViews()
                setupRecyclerView()
                setupKanbanAdapters()
                setupClickListeners()
                observeViewModel()

                return binding!!.root
        }

        private fun setupKanbanAdapters() {
                pendingAdapter =
                        com.example.loginandregistration.adapters.KanbanTaskAdapter { task ->
                                openTaskDetails(task)
                        }
                inProgressAdapter =
                        com.example.loginandregistration.adapters.KanbanTaskAdapter { task ->
                                openTaskDetails(task)
                        }
                completedAdapter =
                        com.example.loginandregistration.adapters.KanbanTaskAdapter { task ->
                                openTaskDetails(task)
                        }
        }

        override fun onDestroyView() {
                super.onDestroyView()
                _binding = null
        }

        private fun setupViews() {
                binding?.swipeRefreshLayout?.setOnRefreshListener { refreshData() }
        }

        private fun setupRecyclerView() {
                val binding = binding ?: return

                taskAdapter = TaskAdapter { task ->
                        val firebaseTask = taskMapping[task.title]
                        if (firebaseTask != null) {
                                openTaskDetails(firebaseTask)
                        } else {
                                Toast.makeText(context, "Task not found", Toast.LENGTH_SHORT).show()
                        }
                }

                binding.recyclerTasks.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = taskAdapter
                }
        }
        private fun observeViewModel() {
                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.tasks.collect { firebaseTasks ->
                                if (_binding == null || !isAdded) {
                                        Log.d(TAG, "View destroyed, skipping tasks UI update")
                                        return@collect
                                }

                                Log.d(TAG, "Received ${firebaseTasks.size} tasks from ViewModel")

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

                                if (isKanbanView) {
                                        updateKanbanView(firebaseTasks)
                                } else {
                                        updateTasksList(filteredTasks)
                                        showEmptyStateIfNeeded(filteredTasks.isEmpty())
                                        _binding?.swipeRefreshLayout?.isRefreshing = false
                                }
                        }
                }

                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.isLoading.collect { isLoading ->
                                if (_binding == null || !isAdded) {
                                        Log.d(TAG, "View destroyed, skipping loading state update")
                                        return@collect
                                }
                                _binding?.swipeRefreshLayout?.isRefreshing = isLoading
                        }
                }

                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.error.collect { error ->
                                error?.let {
                                        if (_binding != null && isAdded) {
                                                showError(it)
                                        } else {
                                                Log.d(
                                                        TAG,
                                                        "View destroyed, skipping error UI update"
                                                )
                                        }
                                        viewModel.clearError()
                                }
                        }
                }

                viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.successMessage.collect { message ->
                                message?.let {
                                        if (_binding != null && isAdded) {
                                                showSuccess(it)
                                        } else {
                                                Log.d(
                                                        TAG,
                                                        "View destroyed, skipping success UI update"
                                                )
                                        }
                                        viewModel.clearSuccessMessage()
                                }
                        }
                }

                taskRepository.getTaskStatsFlow().collectWithLifecycle(viewLifecycleOwner) { stats
                        ->
                        val binding = _binding
                        if (binding == null || !isAdded) {
                                Log.d(TAG, "View destroyed, skipping stats UI update")
                                return@collectWithLifecycle
                        }

                        Log.d(
                                TAG,
                                "Received task stats: overdue=${stats.overdue}, dueToday=${stats.dueToday}, completed=${stats.completed}"
                        )
                        binding.tvOverdueCount.text = stats.overdue.toString()
                        binding.tvDueTodayCount.text = stats.dueToday.toString()
                        binding.tvCompletedCount.text = stats.completed.toString()
                }
        }
        private fun showError(error: ErrorHandler.AppError) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(TAG, "Cannot show error: view is destroyed")
                                        return
                                }

                val exception =
                        Exception(
                                when (error) {
                                        is ErrorHandler.AppError.Permission ->
                                                "You don't have permission to ${error.operation}"
                                        is ErrorHandler.AppError.Network ->
                                                "Network error: ${error.cause}"
                                        is ErrorHandler.AppError.Validation ->
                                                "Please fix: ${error.fields.joinToString(", ")}"
                                        is ErrorHandler.AppError.NotFound ->
                                                "${error.resource} not found"
                                        is ErrorHandler.AppError.Unknown -> error.message
                                        is ErrorHandler.AppError.PermissionError -> error.message
                                        is ErrorHandler.AppError.NetworkError -> error.message
                                        is ErrorHandler.AppError.FirestoreError -> error.message
                                        is ErrorHandler.AppError.UnknownError -> error.message
                                        is ErrorHandler.AppError.AuthError -> error.message
                                        is ErrorHandler.AppError.StorageError -> error.message
                                        is ErrorHandler.AppError.ValidationError -> error.message
                                }
                        )

                val errorState = errorStateManager.categorizeError(exception)
                errorStateManager.showError(errorState, binding.root) { viewModel.loadUserTasks() }
        }

        private fun showSuccess(message: String) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(TAG, "Cannot show success: view is destroyed")
                                        return
                                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
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
                viewModel.loadUserTasks()
        }

        private fun updateTasksList(firebaseTasks: List<FirebaseTask>) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(TAG, "Cannot update tasks list: view is destroyed")
                                        return
                                }

                taskMapping.clear()

                val displayTasks =
                        firebaseTasks.map { firebaseTask ->
                                val displayTask =
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
                                taskMapping[firebaseTask.title] = firebaseTask
                                displayTask
                        }

                taskAdapter.submitList(displayTasks)
        }

        private fun openTaskDetails(task: FirebaseTask) {
                val intent =
                        android.content.Intent(requireContext(), TaskDetailsActivity::class.java)
                intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.id)
                startActivity(intent)
        }

        private fun showEmptyStateIfNeeded(isEmpty: Boolean) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(TAG, "Cannot show empty state: view is destroyed")
                                        return
                                }

                if (isEmpty) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.recyclerTasks.visibility = View.GONE

                        val message =
                                when (currentFilter) {
                                        "personal" -> "No personal tasks yet"
                                        "group" -> "No group tasks yet"
                                        "assignment" -> "No assignments yet"
                                        else -> "No tasks yet"
                                }
                        binding.emptyStateText.text = message
                } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.recyclerTasks.visibility = View.VISIBLE
                }
        }
        private fun setupClickListeners() {
                val binding = binding ?: return

                binding.btnFilter.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.filter_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                binding.btnSearch.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.search_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                binding.btnAddTask.setOnClickListener { showCreateTaskDialog() }

                binding.btnAllTasks.setOnClickListener {
                        currentFilter = "all"
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

                binding.btnPersonal.setOnClickListener {
                        currentFilter = "personal"
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

                binding.btnGroup.setOnClickListener {
                        currentFilter = "group"
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

                binding.btnAssignments.setOnClickListener {
                        currentFilter = "assignment"
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

                binding.btnListView.setOnClickListener {
                        if (isKanbanView) {
                                switchToListView()
                        }
                }

                binding.btnKanbanView.setOnClickListener {
                        if (!isKanbanView) {
                                switchToKanbanView()
                        }
                }

                binding.btnNewTask.setOnClickListener { showCreateTaskDialog() }

                binding.btnKanbanViewAction.setOnClickListener {
                        if (!isKanbanView) {
                                switchToKanbanView()
                        }
                }

                binding.btnAiAssistant.setOnClickListener {
                        val intent =
                                android.content.Intent(
                                        requireContext(),
                                        AIAssistantActivity::class.java
                                )
                        startActivity(intent)
                }

                binding.btnExport.setOnClickListener {
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
                val tilTaskTitle = etTaskTitle.parent.parent as TextInputLayout

                val etTaskDescription =
                        dialogView.findViewById<
                                com.google.android.material.textfield.TextInputEditText>(
                                R.id.et_task_description
                        )
                val tilTaskDescription = etTaskDescription.parent.parent as TextInputLayout
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

                val currentUserId = auth.currentUser?.uid ?: ""
                val assignedToList = mutableListOf(currentUserId)

                fun validateAndUpdateUI() {
                        val title = etTaskTitle.text.toString().trim()
                        val description = etTaskDescription.text.toString().trim()
                        val category =
                                when (chipGroupCategory.checkedChipId) {
                                        R.id.chip_personal -> "personal"
                                        R.id.chip_group_task -> "group"
                                        R.id.chip_assignment -> "assignment"
                                        else -> "personal"
                                }
                        val priority =
                                when (chipGroupPriority.checkedChipId) {
                                        R.id.chip_low -> "low"
                                        R.id.chip_medium -> "medium"
                                        R.id.chip_high -> "high"
                                        else -> "medium"
                                }

                        val titleError = taskValidator.validateTitle(title)
                        val descriptionError = taskValidator.validateDescription(description)

                        tilTaskTitle.error = titleError
                        tilTaskDescription.error = descriptionError

                        val validation =
                                taskValidator.validateTaskCreation(
                                        title = title,
                                        description = description,
                                        assignedTo = assignedToList,
                                        creatorId = currentUserId,
                                        dueDate = null,
                                        priority = priority,
                                        category = category
                                )

                        btnCreate.isEnabled = validation is TaskValidationResult.Valid
                }
                etTaskTitle.addTextChangedListener(
                        object : android.text.TextWatcher {
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
                                ) {}
                                override fun afterTextChanged(s: android.text.Editable?) {
                                        validateAndUpdateUI()
                                }
                        }
                )

                etTaskDescription.addTextChangedListener(
                        object : android.text.TextWatcher {
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
                                ) {}
                                override fun afterTextChanged(s: android.text.Editable?) {
                                        validateAndUpdateUI()
                                }
                        }
                )

                chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
                        layoutGroupSelection.visibility =
                                if (checkedIds.contains(R.id.chip_group_task)) {
                                        android.view.View.VISIBLE
                                } else {
                                        android.view.View.GONE
                                }
                        validateAndUpdateUI()
                }

                chipGroupPriority.setOnCheckedStateChangeListener { _, _ -> validateAndUpdateUI() }

                validateAndUpdateUI()

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
                        showAIPromptDialog(dialog)
                }
                btnCreate.setOnClickListener {
                        val title = etTaskTitle.text.toString().trim()
                        val description = etTaskDescription.text.toString().trim()
                        val subject = etTaskSubject.text.toString().trim()
                        val dueDateText = etDueDate.text.toString().trim()

                        val category =
                                when (chipGroupCategory.checkedChipId) {
                                        R.id.chip_personal -> "personal"
                                        R.id.chip_group_task -> "group"
                                        R.id.chip_assignment -> "assignment"
                                        else -> "personal"
                                }

                        val priority =
                                when (chipGroupPriority.checkedChipId) {
                                        R.id.chip_low -> "low"
                                        R.id.chip_medium -> "medium"
                                        R.id.chip_high -> "high"
                                        else -> "medium"
                                }

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

                        val validation =
                                taskValidator.validateTaskCreation(
                                        title = title,
                                        description = description,
                                        assignedTo = assignedToList,
                                        creatorId = currentUserId,
                                        dueDate = dueDate,
                                        priority = priority,
                                        category = category
                                )

                        when (validation) {
                                is TaskValidationResult.Valid -> {
                                        val task =
                                                com.example.loginandregistration.models
                                                        .FirebaseTask(
                                                                title = title,
                                                                description = description,
                                                                subject = subject,
                                                                category = category,
                                                                status = "pending",
                                                                priority = priority,
                                                                dueDate = dueDate,
                                                                userId = currentUserId,
                                                                assignedTo = assignedToList
                                                        )
                                        viewModel.createTask(task)
                                        dialog.dismiss()
                                }
                                is TaskValidationResult.Invalid -> {
                                        val errorMessage = validation.errors.joinToString("\n")
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG)
                                                .show()
                                }
                        }
                }

                dialog.show()
        }
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

                        progressBar.visibility = android.view.View.VISIBLE
                        btnAICreate.isEnabled = false
                        btnAICancel.isEnabled = false
                        etAIPrompt.isEnabled = false

                        viewModel.createTaskFromAI(prompt)

                        viewLifecycleOwner.lifecycleScope.launch {
                                kotlinx.coroutines.delay(500)

                                viewModel.isLoading.collect { isLoading ->
                                        if (!isLoading) {
                                                progressBar.visibility = android.view.View.GONE
                                                btnAICreate.isEnabled = true
                                                btnAICancel.isEnabled = true
                                                etAIPrompt.isEnabled = true

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
        private fun switchToKanbanView() {
                isKanbanView = true

                val kanbanView =
                        LayoutInflater.from(context).inflate(R.layout.fragment_tasks_kanban, null)

                val parent = _binding?.root as? ViewGroup
                parent?.removeAllViews()
                parent?.addView(kanbanView)

                setupKanbanView(kanbanView)

                val currentTasks = viewModel.tasks.value
                updateKanbanView(currentTasks)
        }

        private fun switchToListView() {
                isKanbanView = false
                parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
        }

        private fun setupKanbanView(kanbanView: View) {
                val recyclerPending =
                        kanbanView.findViewById<androidx.recyclerview.widget.RecyclerView>(
                                R.id.recycler_pending
                        )
                val recyclerInProgress =
                        kanbanView.findViewById<androidx.recyclerview.widget.RecyclerView>(
                                R.id.recycler_in_progress
                        )
                val recyclerCompleted =
                        kanbanView.findViewById<androidx.recyclerview.widget.RecyclerView>(
                                R.id.recycler_completed
                        )

                recyclerPending.apply {
                        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                        adapter = pendingAdapter
                }

                recyclerInProgress.apply {
                        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                        adapter = inProgressAdapter
                }

                recyclerCompleted.apply {
                        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                        adapter = completedAdapter
                }

                val swipeRefresh =
                        kanbanView.findViewById<
                                androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                                R.id.swipeRefreshLayout
                        )
                swipeRefresh.setOnRefreshListener { refreshData() }

                val btnListView = kanbanView.findViewById<ImageButton>(R.id.btn_list_view)
                btnListView.setOnClickListener { switchToListView() }

                val btnFilter = kanbanView.findViewById<ImageButton>(R.id.btn_filter)
                btnFilter.setOnClickListener {
                        Toast.makeText(context, "Filter clicked", Toast.LENGTH_SHORT).show()
                }
        }
        private fun updateKanbanView(firebaseTasks: List<FirebaseTask>) {
                if (!isKanbanView) return

                val filteredTasks =
                        when (currentFilter) {
                                "personal" -> firebaseTasks.filter { it.category == "personal" }
                                "group" -> firebaseTasks.filter { it.category == "group" }
                                "assignment" -> firebaseTasks.filter { it.category == "assignment" }
                                else -> firebaseTasks
                        }

                val pendingTasks = filteredTasks.filter { it.status == "pending" }
                val inProgressTasks = filteredTasks.filter { it.status == "in_progress" }
                val completedTasks = filteredTasks.filter { it.status == "completed" }

                pendingAdapter.submitList(pendingTasks)
                inProgressAdapter.submitList(inProgressTasks)
                completedAdapter.submitList(completedTasks)

                val rootView = (_binding?.root as? ViewGroup)?.getChildAt(0)
                rootView?.let { view ->
                        view.findViewById<TextView>(R.id.tv_pending_count)?.text =
                                pendingTasks.size.toString()
                        view.findViewById<TextView>(R.id.tv_in_progress_count)?.text =
                                inProgressTasks.size.toString()
                        view.findViewById<TextView>(R.id.tv_completed_count)?.text =
                                completedTasks.size.toString()

                        view.findViewById<View>(R.id.empty_pending)?.visibility =
                                if (pendingTasks.isEmpty()) View.VISIBLE else View.GONE
                        view.findViewById<View>(R.id.empty_in_progress)?.visibility =
                                if (inProgressTasks.isEmpty()) View.VISIBLE else View.GONE
                        view.findViewById<View>(R.id.empty_completed)?.visibility =
                                if (completedTasks.isEmpty()) View.VISIBLE else View.GONE

                        view.findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                                        R.id.swipeRefreshLayout
                                )
                                ?.isRefreshing = false
                }
        }
}
