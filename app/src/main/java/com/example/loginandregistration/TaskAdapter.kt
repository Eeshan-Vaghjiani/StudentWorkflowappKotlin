package com.example.loginandregistration

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class TaskAdapter(private val tasks: List<Task>, private val onTaskClick: (Task) -> Unit) :
        RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconBackground: MaterialCardView =
                itemView.findViewById<ImageView>(R.id.iv_task_icon).parent as MaterialCardView
        val taskIcon: ImageView = itemView.findViewById(R.id.iv_task_icon)
        val taskTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        val taskSubtitle: TextView = itemView.findViewById(R.id.tv_task_subtitle)
        val statusChip: Chip = itemView.findViewById(R.id.chip_task_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.taskTitle.text = task.title
        holder.taskSubtitle.text = task.subtitle
        holder.statusChip.text = task.status

        // Set icon background color
        try {
            val color = Color.parseColor(task.iconColor)
            holder.iconBackground.setCardBackgroundColor(color)
        } catch (e: IllegalArgumentException) {
            // Fallback to default color if parsing fails
            val defaultColor =
                    ContextCompat.getColor(holder.itemView.context, R.color.primary_color)
            holder.iconBackground.setCardBackgroundColor(defaultColor)
        }

        // Set status chip color
        try {
            val statusColor = Color.parseColor(task.statusColor)
            holder.statusChip.setChipBackgroundColorResource(android.R.color.transparent)
            holder.statusChip.setTextColor(statusColor)
        } catch (e: IllegalArgumentException) {
            // Fallback to default color
            val defaultColor =
                    ContextCompat.getColor(holder.itemView.context, R.color.primary_color)
            holder.statusChip.setTextColor(defaultColor)
        }

        // Set appropriate icon based on status
        when (task.status) {
            "Overdue" -> holder.taskIcon.setImageResource(R.drawable.ic_assignment)
            "Due Today" -> holder.taskIcon.setImageResource(R.drawable.ic_assignment)
            "Due Tomorrow" -> holder.taskIcon.setImageResource(R.drawable.ic_calendar)
            "Due Later" -> holder.taskIcon.setImageResource(R.drawable.ic_calendar)
            "Completed" -> holder.taskIcon.setImageResource(R.drawable.ic_tasks)
            else -> holder.taskIcon.setImageResource(R.drawable.ic_assignment)
        }

        // Handle completed tasks styling
        if (task.status == "Completed") {
            holder.itemView.alpha = 0.6f
            holder.taskTitle.paintFlags =
                    holder.taskTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.itemView.alpha = 1.0f
            holder.taskTitle.paintFlags =
                    holder.taskTitle.paintFlags and
                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.itemView.setOnClickListener { onTaskClick(task) }
    }

    override fun getItemCount(): Int = tasks.size
}
