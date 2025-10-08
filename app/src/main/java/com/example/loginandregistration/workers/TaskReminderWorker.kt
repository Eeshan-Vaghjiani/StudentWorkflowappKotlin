package com.example.loginandregistration.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.loginandregistration.repository.TaskRepository
import com.example.loginandregistration.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * WorkManager worker that sends task reminder notifications 24 hours before the due date. Checks if
 * the task is still pending before sending the notification.
 */
class TaskReminderWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params) {

    companion object {
        const val TASK_ID_KEY = "task_id"
        const val TASK_TITLE_KEY = "task_title"
        const val TASK_DESCRIPTION_KEY = "task_description"
        const val TASK_DUE_DATE_KEY = "task_due_date"
        const val TASK_PRIORITY_KEY = "task_priority"

        private const val TAG = "TaskReminderWorker"
    }

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(TASK_ID_KEY) ?: return Result.failure()
        val taskTitle = inputData.getString(TASK_TITLE_KEY) ?: "Task Reminder"
        val taskDescription = inputData.getString(TASK_DESCRIPTION_KEY) ?: ""
        val dueDateMillis = inputData.getLong(TASK_DUE_DATE_KEY, 0L)
        val priority = inputData.getString(TASK_PRIORITY_KEY) ?: "Medium"

        // Verify task still exists and is not completed
        val taskRepository = TaskRepository()
        val tasks = taskRepository.getUserTasks()
        val task = tasks.find { it.id == taskId }

        if (task == null) {
            // Task was deleted, no need to send notification
            return Result.success()
        }

        if (task.status == "completed") {
            // Task is already completed, no need to send notification
            return Result.success()
        }

        // Format due date for display
        val dueDate =
                if (dueDateMillis > 0) {
                    val dateFormat =
                            SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                    dateFormat.format(dueDateMillis)
                } else {
                    "Soon"
                }

        // Send notification
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showTaskNotification(
                taskId = taskId,
                taskTitle = taskTitle,
                taskDescription = taskDescription,
                dueDate = dueDate,
                priority = priority
        )

        return Result.success()
    }
}
