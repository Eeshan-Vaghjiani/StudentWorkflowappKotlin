package com.example.loginandregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.R
import com.example.loginandregistration.models.FirebaseTask
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class KanbanTaskAdapter(private val onTaskClick: (FirebaseTask) -> Unit) :
        ListAdapter<FirebaseTask, KanbanTaskAdapter.KanbanTaskViewHolder>(TaskDiffCallback()) {

    class KanbanTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipPriority: Chip = itemView.findViewById(R.id.chip_priority)
        private val chipCategory: Chip = itemView.findViewById(R.id.chip_category)
        private val taskTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        private val taskSubject: TextView = itemView.findViewById(R.id.tv_task_subject)
        private val dueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        private val layoutAssignees: LinearLayout = itemView.findViewById(R.id.layout_assignees)
        private val assigneeCount: TextView = itemView.findViewById(R.id.tv_assignee_count)

        fun bind(task: FirebaseTask, onTaskClick: (FirebaseTask) -> Unit) {
            taskTitle.text = task.title
            taskSubject.text = task.subject

            // Set priority chip
            chipPriority.text = task.priority.replaceFirstChar { it.uppercase() }
            chipPriority.setChipBackgroundColorResource(getPriorityColor(task.priority))

            // Set category chip
            chipCategory.text =
                    when (task.category) {
                        "personal" -> "Personal"
                        "group" -> "Group"
                        "assignment" -> "Assignment"
                        else -> task.category.replaceFirstChar { it.uppercase() }
                    }

            // Set due date
            dueDate.text = formatDueDate(task.dueDate)
            dueDate.setTextColor(getDueDateColor(task.dueDate))

            // Show assignee count for group tasks
            if (task.category == "group" && task.assignedTo.isNotEmpty()) {
                layoutAssignees.visibility = View.VISIBLE
                assigneeCount.text = task.assignedTo.size.toString()
            } else {
                layoutAssignees.visibility = View.GONE
            }

            itemView.setOnClickListener { onTaskClick(task) }
        }

        private fun getPriorityColor(priority: String): Int {
            return when (priority.lowercase()) {
                "high" -> R.color.high_priority_color
                "medium" -> R.color.medium_priority_color
                "low" -> R.color.low_priority_color
                else -> R.color.medium_priority_color
            }
        }

        private fun formatDueDate(dueDate: Timestamp?): String {
            if (dueDate == null) return "No due date"

            val now = System.currentTimeMillis()
            val due = dueDate.toDate().time
            val diff = due - now

            return when {
                diff < 0 -> "Overdue"
                diff < 24 * 60 * 60 * 1000 -> "Due today"
                diff < 2 * 24 * 60 * 60 * 1000 -> "Due tomorrow"
                diff < 7 * 24 * 60 * 60 * 1000 -> "Due in ${diff / (24 * 60 * 60 * 1000)} days"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    "Due ${dateFormat.format(dueDate.toDate())}"
                }
            }
        }

        private fun getDueDateColor(dueDate: Timestamp?): Int {
            if (dueDate == null)
                    return ContextCompat.getColor(itemView.context, R.color.text_secondary)

            val now = System.currentTimeMillis()
            val due = dueDate.toDate().time
            val diff = due - now

            return when {
                diff < 0 -> ContextCompat.getColor(itemView.context, R.color.overdue_color)
                diff < 24 * 60 * 60 * 1000 ->
                        ContextCompat.getColor(itemView.context, R.color.due_today_color)
                else -> ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanbanTaskViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_kanban_task, parent, false)
        return KanbanTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: KanbanTaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, onTaskClick)
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<FirebaseTask>() {
        override fun areItemsTheSame(oldItem: FirebaseTask, newItem: FirebaseTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FirebaseTask, newItem: FirebaseTask): Boolean {
            return oldItem == newItem
        }
    }
}
