package com.example.loginandregistration

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class GroupAdapter(private val onGroupClick: (Group) -> Unit) :
        ListAdapter<Group, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconBackground: MaterialCardView =
                itemView.findViewById<ImageView>(R.id.iv_group_icon).parent as MaterialCardView
        private val groupIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)
        private val groupName: TextView = itemView.findViewById(R.id.tv_group_name)
        private val groupDetails: TextView = itemView.findViewById(R.id.tv_group_details)
        private val assignmentChip: Chip = itemView.findViewById(R.id.chip_assignment_count)

        fun bind(group: Group, onGroupClick: (Group) -> Unit) {
            groupName.text = group.name
            groupDetails.text = group.details
            assignmentChip.text = group.assignmentCount.toString()

            // Set icon background color
            try {
                val color = Color.parseColor(group.iconColor)
                iconBackground.setCardBackgroundColor(color)
            } catch (e: IllegalArgumentException) {
                iconBackground.setCardBackgroundColor(Color.parseColor("#007AFF"))
            }

            // Set icon resource
            groupIcon.setImageResource(group.iconResource)

            itemView.setOnClickListener { onGroupClick(group) }
        }

        fun updateAssignmentCount(count: Int) {
            assignmentChip.text = count.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group, onGroupClick)
    }

    override fun onBindViewHolder(
            holder: GroupViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val group = getItem(position)
            if (payloads.contains("ASSIGNMENT_COUNT_CHANGED")) {
                // Only update assignment count
                holder.updateAssignmentCount(group.assignmentCount)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    override fun onViewRecycled(holder: GroupViewHolder) {
        super.onViewRecycled(holder)
        // Clear click listener to prevent memory leaks
        holder.itemView.setOnClickListener(null)
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Group, newItem: Group): Any? {
            // Return payload for partial updates
            if (oldItem.name == newItem.name) {
                if (oldItem.assignmentCount != newItem.assignmentCount) {
                    return "ASSIGNMENT_COUNT_CHANGED"
                }
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}
