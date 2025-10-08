package com.example.loginandregistration.utils

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.workers.TaskReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Utility class for scheduling and canceling task reminder notifications using WorkManager.
 * Schedules reminders 24 hours before the task due date.
 */
object TaskReminderScheduler {

    private const val TAG = "TaskReminderScheduler"
    private const val REMINDER_WORK_PREFIX = "task_reminder_"
    private const val HOURS_BEFORE_DUE = 24L

    /**
     * Schedules a reminder notification for a task 24 hours before its due date. If the due date is
     * less than 24 hours away, schedules immediately. If the task has no due date, does nothing.
     *
     * @param context Application context
     * @param task The task to schedule a reminder for
     */
    fun scheduleReminder(context: Context, task: FirebaseTask) {
        val dueDate =
                task.dueDate
                        ?: run {
                            Log.d(TAG, "Task ${task.id} has no due date, skipping reminder")
                            return
                        }

        if (task.status == "completed") {
            Log.d(TAG, "Task ${task.id} is already completed, skipping reminder")
            return
        }

        val dueDateMillis = dueDate.toDate().time
        val currentTimeMillis = System.currentTimeMillis()
        val reminderTimeMillis = dueDateMillis - TimeUnit.HOURS.toMillis(HOURS_BEFORE_DUE)

        // Calculate delay until reminder should be sent
        val delayMillis = reminderTimeMillis - currentTimeMillis

        if (delayMillis < 0) {
            // Due date is in the past or less than 24 hours away
            if (dueDateMillis > currentTimeMillis) {
                // Task is still in the future but less than 24 hours away
                // Schedule immediately
                Log.d(
                        TAG,
                        "Task ${task.id} due in less than 24 hours, scheduling immediate reminder"
                )
                scheduleImmediateReminder(context, task)
            } else {
                Log.d(TAG, "Task ${task.id} is overdue, skipping reminder")
            }
            return
        }

        // Create input data for the worker
        val inputData =
                Data.Builder()
                        .putString(TaskReminderWorker.TASK_ID_KEY, task.id)
                        .putString(TaskReminderWorker.TASK_TITLE_KEY, task.title)
                        .putString(TaskReminderWorker.TASK_DESCRIPTION_KEY, task.description)
                        .putLong(TaskReminderWorker.TASK_DUE_DATE_KEY, dueDateMillis)
                        .putString(TaskReminderWorker.TASK_PRIORITY_KEY, task.priority)
                        .build()

        // Create work request with delay
        val workRequest =
                OneTimeWorkRequestBuilder<TaskReminderWorker>()
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .addTag(getWorkTag(task.id))
                        .build()

        // Schedule the work with unique name to avoid duplicates
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        getWorkName(task.id),
                        ExistingWorkPolicy.REPLACE, // Replace existing work if task is updated
                        workRequest
                )

        Log.d(
                TAG,
                "Scheduled reminder for task ${task.id} (${task.title}) in ${delayMillis / 1000 / 60} minutes"
        )
    }

    /** Schedules an immediate reminder for tasks due in less than 24 hours. */
    private fun scheduleImmediateReminder(context: Context, task: FirebaseTask) {
        val dueDate = task.dueDate ?: return
        val dueDateMillis = dueDate.toDate().time

        val inputData =
                Data.Builder()
                        .putString(TaskReminderWorker.TASK_ID_KEY, task.id)
                        .putString(TaskReminderWorker.TASK_TITLE_KEY, task.title)
                        .putString(TaskReminderWorker.TASK_DESCRIPTION_KEY, task.description)
                        .putLong(TaskReminderWorker.TASK_DUE_DATE_KEY, dueDateMillis)
                        .putString(TaskReminderWorker.TASK_PRIORITY_KEY, task.priority)
                        .build()

        val workRequest =
                OneTimeWorkRequestBuilder<TaskReminderWorker>()
                        .setInputData(inputData)
                        .addTag(getWorkTag(task.id))
                        .build()

        WorkManager.getInstance(context)
                .enqueueUniqueWork(getWorkName(task.id), ExistingWorkPolicy.REPLACE, workRequest)

        Log.d(TAG, "Scheduled immediate reminder for task ${task.id} (${task.title})")
    }

    /**
     * Cancels a scheduled reminder for a task. Should be called when a task is completed or
     * deleted.
     *
     * @param context Application context
     * @param taskId The ID of the task whose reminder should be canceled
     */
    fun cancelReminder(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(getWorkName(taskId))
        Log.d(TAG, "Canceled reminder for task $taskId")
    }

    /**
     * Reschedules a reminder for a task. Should be called when a task's due date is changed.
     *
     * @param context Application context
     * @param task The task with updated information
     */
    fun rescheduleReminder(context: Context, task: FirebaseTask) {
        // Cancel existing reminder
        cancelReminder(context, task.id)

        // Schedule new reminder with updated information
        scheduleReminder(context, task)

        Log.d(TAG, "Rescheduled reminder for task ${task.id}")
    }

    /**
     * Cancels all task reminders. Useful for cleanup or when user logs out.
     *
     * @param context Application context
     */
    fun cancelAllReminders(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_reminder")
        Log.d(TAG, "Canceled all task reminders")
    }

    /** Gets the unique work name for a task reminder. */
    private fun getWorkName(taskId: String): String {
        return "$REMINDER_WORK_PREFIX$taskId"
    }

    /** Gets the tag for a task reminder work. */
    private fun getWorkTag(taskId: String): String {
        return "task_reminder"
    }
}
