package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.utils.ThemeUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var priorityIndicator: View
    private lateinit var tvTaskTitle: TextView
    private lateinit var tvTaskStatus: TextView
    private lateinit var tvTaskDescription: TextView
    private lateinit var tvTaskCategory: TextView
    private lateinit var tvTaskPriority: TextView
    private lateinit var tvTaskSubject: TextView
    private lateinit var tvTaskDueDate: TextView
    private lateinit var tvTaskCreated: TextView
    private lateinit var layoutSubject: View
    private lateinit var btnMarkComplete: MaterialButton
    private lateinit var btnEditTask: MaterialButton
    private lateinit var btnDeleteTask: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var taskRepository: TaskRepository
    private var taskId: String? = null
    private var currentTask: FirebaseTask? = null

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

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
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        priorityIndicator = findViewById(R.id.priorityIndicator)
        tvTaskTitle = findViewById(R.id.tv_task_title)
        tvTaskStatus = findViewById(R.id.tv_task_status)
        tvTaskDescription = findViewById(R.id.tv_task_description)
        tvTaskCategory = findViewById(R.id.tv_task_category)
        tvTaskPriority = findViewById(R.id.tv_task_priority)
        tvTaskSubject = findViewById(R.id.tv_task_subject)
        tvTaskDueDate = findViewById(R.id.tv_task_due_date)
        tvTaskCreated = findViewById(R.id.tv_task_created)
        layoutSubject = findViewById(R.id.layout_subject)
        btnMarkComplete = findViewById(R.id.btn_mark_complete)
        btnEditTask = findViewById(R.id.btn_edit_task)
        btnDeleteTask = findViewById(R.id.btn_delete_task)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadTaskDetails() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Fetch task from Firestore
                val result = taskRepository.getUserTasks()
                val tasks = result.getOrElse { emptyList() }
                currentTask = tasks.find { it.id == taskId }

                if (currentTask != null) {
                    displayTaskDetails(currentTask!!)
                } else {
                    Toast.makeText(this@TaskDetailsActivity, "Task not found", Toast.LENGTH_SHORT)
                            .show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@TaskDetailsActivity,
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

    private fun displayTaskDetails(task: FirebaseTask) {
        // Title and Status
        tvTaskTitle.text = task.title

        // Status with icon and color
        when (task.status.lowercase()) {
            "completed" -> {
                tvTaskStatus.text = "✓ Completed"
                tvTaskStatus.setTextColor(ThemeUtils.getStatusColor(this, "completed"))
                btnMarkComplete.visibility = View.GONE
            }
            "pending" -> {
                tvTaskStatus.text = "○ Pending"
                tvTaskStatus.setTextColor(ThemeUtils.getStatusColor(this, "pending"))
                btnMarkComplete.visibility = View.VISIBLE
                btnMarkComplete.text = "Mark as Complete"
            }
            "overdue" -> {
                tvTaskStatus.text = "! Overdue"
                tvTaskStatus.setTextColor(ThemeUtils.getStatusColor(this, "overdue"))
                btnMarkComplete.visibility = View.VISIBLE
                btnMarkComplete.text = "Mark as Complete"
            }
            else -> {
                tvTaskStatus.text = task.status.replaceFirstChar { it.uppercase() }
                tvTaskStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                btnMarkComplete.visibility = View.VISIBLE
            }
        }

        // Description
        if (task.description.isNotEmpty()) {
            tvTaskDescription.text = task.description
        } else {
            tvTaskDescription.text = "No description provided"
            tvTaskDescription.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        // Category
        tvTaskCategory.text = task.category.replaceFirstChar { it.uppercase() }

        // Priority with color
        val priorityText = task.priority.replaceFirstChar { it.uppercase() }
        tvTaskPriority.text = priorityText

        val priorityColor = ThemeUtils.getPriorityColor(this, task.priority)
        priorityIndicator.setBackgroundColor(priorityColor)
        tvTaskPriority.setTextColor(priorityColor)

        // Subject
        if (task.subject.isNotEmpty()) {
            tvTaskSubject.text = task.subject
            layoutSubject.visibility = View.VISIBLE
        } else {
            layoutSubject.visibility = View.GONE
        }

        // Due Date
        if (task.dueDate != null) {
            tvTaskDueDate.text = dateFormat.format(task.dueDate!!.toDate())
        } else {
            tvTaskDueDate.text = "No due date"
        }

        // Created Date
        tvTaskCreated.text = dateTimeFormat.format(task.createdAt.toDate())
    }

    private fun setupClickListeners() {
        btnMarkComplete.setOnClickListener { showMarkCompleteDialog() }

        btnEditTask.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra(EditTaskActivity.EXTRA_TASK_ID, taskId)
            startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
        }

        btnDeleteTask.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun showMarkCompleteDialog() {
        AlertDialog.Builder(this)
                .setTitle("Mark Task as Complete")
                .setMessage("Are you sure you want to mark this task as complete?")
                .setPositiveButton("Complete") { _, _ -> markTaskAsComplete() }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun markTaskAsComplete() {
        val taskId = this.taskId ?: return

        showLoading(true)
        lifecycleScope.launch {
            try {
                val success = taskRepository.completeTask(taskId)

                if (success) {
                    Toast.makeText(
                                    this@TaskDetailsActivity,
                                    "Task marked as complete",
                                    Toast.LENGTH_SHORT
                            )
                            .show()

                    // Reload task details to update UI
                    loadTaskDetails()

                    // Set result to notify calendar to refresh
                    setResult(RESULT_OK)
                } else {
                    Toast.makeText(
                                    this@TaskDetailsActivity,
                                    "Failed to update task",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage(
                        "Are you sure you want to delete this task? This action cannot be undone."
                )
                .setPositiveButton("Delete") { _, _ -> deleteTask() }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun deleteTask() {
        val taskId = this.taskId ?: return

        showLoading(true)
        lifecycleScope.launch {
            try {
                val result = taskRepository.deleteTask(taskId)

                if (result.isSuccess) {
                    Toast.makeText(
                                    this@TaskDetailsActivity,
                                    "Task deleted successfully",
                                    Toast.LENGTH_SHORT
                            )
                            .show()

                    // Set result to notify calendar to refresh
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                                    this@TaskDetailsActivity,
                                    "Failed to delete task",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnMarkComplete.isEnabled = !show
        btnEditTask.isEnabled = !show
        btnDeleteTask.isEnabled = !show
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: android.content.Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            // Reload task details after edit
            loadTaskDetails()
            setResult(RESULT_OK)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        private const val EDIT_TASK_REQUEST_CODE = 100
    }
}
