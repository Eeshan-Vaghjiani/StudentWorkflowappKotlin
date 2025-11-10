package com.example.loginandregistration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.repository.TaskRepository

import com.example.loginandregistration.validation.TaskCreationValidator
import com.example.loginandregistration.validation.TaskValidationResult
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

class EditTaskActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTaskTitle: TextInputEditText
    private lateinit var tilTaskTitle: TextInputLayout
    private lateinit var etTaskDescription: TextInputEditText
    private lateinit var tilTaskDescription: TextInputLayout
    private lateinit var etTaskSubject: TextInputEditText
    private lateinit var etDueDate: TextInputEditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipGroupPriority: ChipGroup
    private lateinit var layoutGroupSelection: TextInputLayout
    private lateinit var spinnerGroup: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnEditWithAI: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var taskRepository: TaskRepository
    private val taskValidator = TaskCreationValidator()
    private val auth = FirebaseAuth.getInstance()
    private var taskId: String? = null
    private var currentTask: FirebaseTask? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        initializeViews()
        setupToolbar()

        taskRepository = TaskRepository(this)
        taskId = intent.getStringExtra(EXTRA_TASK_ID)

        if (taskId == null) {
            Toast.makeText(this, "Error: Task ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTaskDetails()
        setupValidation()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etTaskTitle = findViewById(R.id.et_task_title)
        tilTaskTitle = etTaskTitle.parent.parent as TextInputLayout
        etTaskDescription = findViewById(R.id.et_task_description)
        tilTaskDescription = etTaskDescription.parent.parent as TextInputLayout
        etTaskSubject = findViewById(R.id.et_task_subject)
        etDueDate = findViewById(R.id.et_due_date)
        chipGroupCategory = findViewById(R.id.chip_group_category)
        chipGroupPriority = findViewById(R.id.chip_group_priority)
        layoutGroupSelection = findViewById(R.id.layout_group_selection)
        spinnerGroup = findViewById(R.id.spinner_group)
        btnSave = findViewById(R.id.btn_save)
        btnEditWithAI = findViewById(R.id.btn_edit_with_ai)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Task"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadTaskDetails() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                android.util.Log.d("EditTaskActivity", "Loading task with ID: $taskId")
                val result = taskRepository.getUserTasks()
                val tasks = result.getOrElse { emptyList() }
                android.util.Log.d("EditTaskActivity", "Found ${tasks.size} tasks")
                
                currentTask = tasks.find { it.id == taskId }

                if (currentTask != null) {
                    android.util.Log.d("EditTaskActivity", "Task found: ${currentTask?.title}, ID: ${currentTask?.id}")
                    populateTaskDetails(currentTask!!)
                } else {
                    android.util.Log.e("EditTaskActivity", "Task not found with ID: $taskId")
                    Toast.makeText(this@EditTaskActivity, "Task not found", Toast.LENGTH_SHORT)
                            .show()
                    finish()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditTaskActivity", "Error loading task", e)
                Toast.makeText(
                                this@EditTaskActivity,
                                "Error loading task: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
                finish()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun populateTaskDetails(task: FirebaseTask) {
        etTaskTitle.setText(task.title)
        etTaskDescription.setText(task.description)
        etTaskSubject.setText(task.subject)

        // Set due date
        task.dueDate?.let { etDueDate.setText(dateFormat.format(it.toDate())) }

        // Set category
        when (task.category) {
            "personal" -> chipGroupCategory.check(R.id.chip_personal)
            "group" -> chipGroupCategory.check(R.id.chip_group_task)
            "assignment" -> chipGroupCategory.check(R.id.chip_assignment)
        }

        // Set priority
        when (task.priority) {
            "low" -> chipGroupPriority.check(R.id.chip_low)
            "medium" -> chipGroupPriority.check(R.id.chip_medium)
            "high" -> chipGroupPriority.check(R.id.chip_high)
        }

        // Load groups if category is group
        if (task.category == "group") {
            layoutGroupSelection.visibility = View.VISIBLE
            loadUserGroups()
        }
    }

    private fun loadUserGroups() {
        lifecycleScope.launch {
            try {
                val groupRepository = com.example.loginandregistration.repository.GroupRepository()
                val result = groupRepository.getUserGroups()
                val userGroups = result.getOrElse { emptyList() }
                val groupNames = userGroups.map { it.name }
                val adapter =
                        android.widget.ArrayAdapter(
                                this@EditTaskActivity,
                                android.R.layout.simple_dropdown_item_1line,
                                groupNames
                        )
                spinnerGroup.setAdapter(adapter)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun setupValidation() {
        etTaskTitle.addTextChangedListener(
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
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateAndUpdateUI()
                    }
                }
        )

        etTaskDescription.addTextChangedListener(
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
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateAndUpdateUI()
                    }
                }
        )

        chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            layoutGroupSelection.visibility =
                    if (checkedIds.contains(R.id.chip_group_task)) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            validateAndUpdateUI()
        }

        chipGroupPriority.setOnCheckedStateChangeListener { _, _ -> validateAndUpdateUI() }
    }

    private fun validateAndUpdateUI() {
        val title = etTaskTitle.text.toString().trim()
        val description = etTaskDescription.text.toString().trim()

        val titleError = taskValidator.validateTitle(title)
        val descriptionError = taskValidator.validateDescription(description)

        tilTaskTitle.error = titleError
        tilTaskDescription.error = descriptionError

        btnSave.isEnabled = titleError == null && descriptionError == null && title.isNotEmpty()
    }

    private fun setupClickListeners() {
        etDueDate.setOnClickListener { showDatePicker() }

        btnSave.setOnClickListener { saveTask() }

        btnEditWithAI.setOnClickListener { showAIEditDialog() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        currentTask?.dueDate?.let { calendar.time = it.toDate() }

        val datePickerDialog =
                android.app.DatePickerDialog(
                        this,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = Calendar.getInstance()
                            selectedDate.set(year, month, dayOfMonth)
                            etDueDate.setText(dateFormat.format(selectedDate.time))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
        datePickerDialog.show()
    }

    private fun saveTask() {
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

        var dueDate: Timestamp? = null
        if (dueDateText.isNotEmpty()) {
            try {
                val date = dateFormat.parse(dueDateText)
                if (date != null) {
                    dueDate = Timestamp(date)
                }
            } catch (e: Exception) {
                // Invalid date format, ignore
            }
        }

        if (currentTask == null) {
            Toast.makeText(this, "Error: Task data not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: ""
        
        // Preserve the original assignedTo list, ensuring current user is included
        val assignedToList = if (currentTask!!.assignedTo.isEmpty()) {
            listOf(currentUserId)
        } else {
            currentTask!!.assignedTo
        }

        android.util.Log.d("EditTaskActivity", "Current task ID: ${currentTask?.id}")
        android.util.Log.d("EditTaskActivity", "AssignedTo list: $assignedToList")
        
        // Update task with new values, keeping original data
        val updatedTask =
                currentTask!!.copy(
                        title = title,
                        description = description,
                        subject = subject,
                        category = category,
                        priority = priority,
                        dueDate = dueDate,
                        assignedTo = assignedToList, // Preserve assignedTo
                        updatedAt = Timestamp.now()
                )

        android.util.Log.d("EditTaskActivity", "Updated task ID: ${updatedTask.id}")
        
        // Validate the updated task
        val validation =
                taskValidator.validateTaskCreation(
                        title = updatedTask.title,
                        description = updatedTask.description,
                        assignedTo = updatedTask.assignedTo,
                        creatorId = updatedTask.userId,
                        dueDate = updatedTask.dueDate,
                        priority = updatedTask.priority,
                        category = updatedTask.category
                )

        when (validation) {
            is TaskValidationResult.Valid -> {
                android.util.Log.d("EditTaskActivity", "Validation passed, updating task")
                updateTaskInFirestore(updatedTask)
            }
            is TaskValidationResult.Invalid -> {
                val errorMessage = validation.errors.joinToString("\n")
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateTaskInFirestore(task: FirebaseTask) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                android.util.Log.d("EditTaskActivity", "Updating task: ${task.id}")
                val result = taskRepository.updateTask(task)

                if (result.isSuccess) {
                    android.util.Log.d("EditTaskActivity", "Task updated successfully")
                    Toast.makeText(
                                    this@EditTaskActivity,
                                    "Task updated successfully",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val error = result.exceptionOrNull()
                    android.util.Log.e("EditTaskActivity", "Failed to update task", error)
                    Toast.makeText(
                                    this@EditTaskActivity,
                                    "Failed to update task: ${error?.message ?: "Unknown error"}",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditTaskActivity", "Exception updating task", e)
                Toast.makeText(this@EditTaskActivity, "Error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showAIEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ai_edit_task, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()

        val etAIPrompt = dialogView.findViewById<TextInputEditText>(R.id.et_ai_prompt)
        val btnAICancel = dialogView.findViewById<MaterialButton>(R.id.btn_ai_cancel)
        val btnAIApply = dialogView.findViewById<MaterialButton>(R.id.btn_ai_apply)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar)

        btnAICancel.setOnClickListener { dialog.dismiss() }

        btnAIApply.setOnClickListener {
            val prompt = etAIPrompt.text.toString().trim()
            if (prompt.isEmpty()) {
                Toast.makeText(this, "Please describe what you want to change", Toast.LENGTH_SHORT)
                        .show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnAIApply.isEnabled = false
            btnAICancel.isEnabled = false
            etAIPrompt.isEnabled = false

            applyAIEdits(prompt, dialog, progressBar, btnAIApply, btnAICancel, etAIPrompt)
        }

        dialog.show()
    }

    private fun applyAIEdits(
            prompt: String,
            dialog: androidx.appcompat.app.AlertDialog,
            progressBar: ProgressBar,
            btnApply: MaterialButton,
            btnCancel: MaterialButton,
            etPrompt: TextInputEditText
    ) {
        lifecycleScope.launch {
            try {
                // Check if API key is available
                val apiKey = com.example.loginandregistration.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty()) {
                    Toast.makeText(
                                    this@EditTaskActivity,
                                    "AI Assistant is not configured. Please add GEMINI_API_KEY to local.properties",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                    progressBar.visibility = View.GONE
                    btnApply.isEnabled = true
                    btnCancel.isEnabled = true
                    etPrompt.isEnabled = true
                    return@launch
                }

                val geminiService =
                        com.example.loginandregistration.services.GeminiAssistantService(
                                apiKey,
                                taskRepository
                        )

                val fullPrompt =
                        """
                    I need to update a task. Here are the current details:
                    Title: ${etTaskTitle.text}
                    Description: ${etTaskDescription.text}
                    Subject: ${etTaskSubject.text}
                    
                    User's change request: $prompt
                    
                    Please provide the updated task details in this exact JSON format:
                    {
                      "title": "updated title here",
                      "description": "updated description here",
                      "subject": "updated subject here"
                    }
                """.trimIndent()

                val result = geminiService.sendMessage(fullPrompt)

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null && response.success) {
                        // Try to parse JSON from response
                        val jsonStart = response.message.indexOf("{")
                        val jsonEnd = response.message.lastIndexOf("}") + 1

                        if (jsonStart != -1 && jsonEnd > jsonStart) {
                            val jsonString = response.message.substring(jsonStart, jsonEnd)
                            val jsonObject = org.json.JSONObject(jsonString)

                            jsonObject.optString("title")?.let {
                                if (it.isNotEmpty()) etTaskTitle.setText(it)
                            }
                            jsonObject.optString("description")?.let {
                                if (it.isNotEmpty()) etTaskDescription.setText(it)
                            }
                            jsonObject.optString("subject")?.let {
                                if (it.isNotEmpty()) etTaskSubject.setText(it)
                            }

                            Toast.makeText(
                                            this@EditTaskActivity,
                                            "AI suggestions applied",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                            this@EditTaskActivity,
                                            "Could not parse AI response",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    } else {
                        Toast.makeText(
                                        this@EditTaskActivity,
                                        response?.error ?: "AI request failed",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                } else {
                    Toast.makeText(
                                    this@EditTaskActivity,
                                    "AI edit failed: ${result.exceptionOrNull()?.message}",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@EditTaskActivity,
                                "AI edit failed: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            } finally {
                progressBar.visibility = View.GONE
                btnApply.isEnabled = true
                btnCancel.isEnabled = true
                etPrompt.isEnabled = true
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSave.isEnabled = !show
        btnEditWithAI.isEnabled = !show
    }
}
