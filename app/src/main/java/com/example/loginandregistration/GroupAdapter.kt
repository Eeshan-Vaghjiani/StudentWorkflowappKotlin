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
        val iconBackground: MaterialCardView =
                itemView.findViewById<ImageView>(R.id.iv_group_icon).parent as MaterialCardView
        val groupIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)
        val groupName: TextView = itemView.findViewById(R.id.tv_group_name)
        val groupDetails: TextView = itemView.findViewById(R.id.tv_group_details)
        val assignmentChip: Chip = itemView.findViewById(R.id.chip_assignment_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)

        holder.groupName.text = group.name
        holder.groupDetails.text = group.details
        holder.assignmentChip.text = group.assignmentCount.toString()

        // Set icon background color
        try {
            val color = Color.parseColor(group.iconColor)
            holder.iconBackground.setCardBackgroundColor(color)
        } catch (e: IllegalArgumentException) {
            holder.iconBackground.setCardBackgroundColor(Color.parseColor("#007AFF"))
        }

        // Set icon resource
        holder.groupIcon.setImageResource(group.iconResource)

        holder.itemView.setOnClickListener { onGroupClick(group) }
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
    }
}
