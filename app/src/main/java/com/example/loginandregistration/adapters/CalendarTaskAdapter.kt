package com.example.loginandregistration.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.R
import com.example.loginandregistration.models.FirebaseTask

class CalendarTaskAdapter(
    private val onTaskClick: (FirebaseTask) -> Unit
) : ListAdapter<FirebaseTask, CalendarTaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_task, parent, false)
        return TaskViewHolder(view, onTaskClick)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        itemView: View,
        private val onTaskClick: (FirebaseTask) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskCategory: TextView = itemView.findViewById(R.id.taskCategory)
        private val taskPriority: TextView = itemView.findViewById(R.id.taskPriority)
        private val taskStatus: TextView = itemView.findViewById(R.id.taskStatus)

        fun bind(task: FirebaseTask) {
            taskTitle.text = task.title
            taskCategory.text = task.category.replaceFirstChar { it.uppercase() }
            taskPriority.text = "Priority: ${task.priority.replaceFirstChar { it.uppercase() }}"
            
            // Set priority indicator color
            val priorityColor = when (task.priority.lowercase()) {
                "high" -> Color.parseColor("#F44336")
                "medium" -> Color.parseColor("#FF9800")
                "low" -> Color.parseColor("#4CAF50")
                else -> Color.parseColor("#9E9E9E")
            }
            priorityIndicator.setBackgroundColor(priorityColor)
            
            // Set status text and color
            when (task.status.lowercase()) {
                "completed" -> {
                    taskStatus.text = "✓ Completed"
                    taskStatus.setTextColor(Color.parseColor("#4CAF50"))
                }
                "pending" -> {
                    taskStatus.text = "○ Pending"
                    taskStatus.setTextColor(Color.parseColor("#FF9800"))
                }
                "overdue" -> {
                    taskStatus.text = "! Overdue"
                    taskStatus.setTextColor(Color.parseColor("#F44336"))
                }
                else -> {
                    taskStatus.text = task.status.replaceFirstChar { it.uppercase() }
                    taskStatus.setTextColor(Color.parseColor("#9E9E9E"))
                }
            }
            
            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<FirebaseTask>() {
        override fun areItemsTheSame(oldItem: FirebaseTask, newItem: FirebaseTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FirebaseTask, newItem: FirebaseTask): Boolean {
            return oldItem == newItem
        }
    }
}
