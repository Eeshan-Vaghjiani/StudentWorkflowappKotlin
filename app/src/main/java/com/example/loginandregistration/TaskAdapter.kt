package com.example.loginandregistration

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class TaskAdapter(private val onTaskClick: (Task) -> Unit) :
        ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconBackground: MaterialCardView =
                itemView.findViewById<ImageView>(R.id.iv_task_icon).parent as MaterialCardView
        private val taskIcon: ImageView = itemView.findViewById(R.id.iv_task_icon)
        private val taskTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        private val taskSubtitle: TextView = itemView.findViewById(R.id.tv_task_subtitle)
        private val statusChip: Chip = itemView.findViewById(R.id.chip_task_status)

        fun bind(task: Task, onTaskClick: (Task) -> Unit) {
            taskTitle.text = task.title
            taskSubtitle.text = task.subtitle
            statusChip.text = task.status

            // Set icon background color
            try {
                val color = Color.parseColor(task.iconColor)
                iconBackground.setCardBackgroundColor(color)
            } catch (e: IllegalArgumentException) {
                // Fallback to default color if parsing fails
                val defaultColor = ContextCompat.getColor(itemView.context, R.color.primary_color)
                iconBackground.setCardBackgroundColor(defaultColor)
            }

            updateStatus(task)
            itemView.setOnClickListener { onTaskClick(task) }
        }

        fun updateStatus(task: Task) {
            // Set status chip color
            try {
                val statusColor = Color.parseColor(task.statusColor)
                statusChip.setChipBackgroundColorResource(android.R.color.transparent)
                statusChip.setTextColor(statusColor)
            } catch (e: IllegalArgumentException) {
                // Fallback to default color
                val defaultColor = ContextCompat.getColor(itemView.context, R.color.primary_color)
                statusChip.setTextColor(defaultColor)
            }

            statusChip.text = task.status

            // Set appropriate icon based on status
            when (task.status) {
                "Overdue" -> taskIcon.setImageResource(R.drawable.ic_assignment)
                "Due Today" -> taskIcon.setImageResource(R.drawable.ic_assignment)
                "Due Tomorrow" -> taskIcon.setImageResource(R.drawable.ic_calendar)
                "Due Later" -> taskIcon.setImageResource(R.drawable.ic_calendar)
                "Completed" -> taskIcon.setImageResource(R.drawable.ic_tasks)
                else -> taskIcon.setImageResource(R.drawable.ic_assignment)
            }

            // Handle completed tasks styling
            if (task.status == "Completed") {
                itemView.alpha = 0.6f
                taskTitle.paintFlags =
                        taskTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                itemView.alpha = 1.0f
                taskTitle.paintFlags =
                        taskTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task, onTaskClick)
    }

    override fun onBindViewHolder(
            holder: TaskViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val task = getItem(position)
            if (payloads.contains("STATUS_CHANGED")) {
                // Only update status-related views
                holder.updateStatus(task)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        super.onViewRecycled(holder)
        // Clear click listener to prevent memory leaks
        holder.itemView.setOnClickListener(null)
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.title == newItem.title && oldItem.subtitle == newItem.subtitle
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Task, newItem: Task): Any? {
            // Return payload for partial updates
            if (oldItem.title == newItem.title && oldItem.subtitle == newItem.subtitle) {
                if (oldItem.status != newItem.status) {
                    return "STATUS_CHANGED"
                }
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}
