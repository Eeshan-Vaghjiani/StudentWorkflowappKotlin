package com.example.loginandregistration

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DiscoverGroupAdapter(
        private val groups: List<DiscoverGroup>,
        private val onJoinClick: (DiscoverGroup) -> Unit
) : RecyclerView.Adapter<DiscoverGroupAdapter.DiscoverGroupViewHolder>() {

    class DiscoverGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconBackground: MaterialCardView =
                itemView.findViewById<ImageView>(R.id.iv_discover_group_icon).parent as
                        MaterialCardView
        val groupIcon: ImageView = itemView.findViewById(R.id.iv_discover_group_icon)
        val groupName: TextView = itemView.findViewById(R.id.tv_discover_group_name)
        val groupDetails: TextView = itemView.findViewById(R.id.tv_discover_group_details)
        val joinButton: MaterialButton = itemView.findViewById(R.id.btn_join_discover_group)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverGroupViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_discover_group, parent, false)
        return DiscoverGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscoverGroupViewHolder, position: Int) {
        val group = groups[position]

        holder.groupName.text = group.name
        holder.groupDetails.text = group.details

        // Set icon background color
        try {
            val color = Color.parseColor(group.iconColor)
            holder.iconBackground.setCardBackgroundColor(color)
        } catch (e: IllegalArgumentException) {
            holder.iconBackground.setCardBackgroundColor(Color.parseColor("#007AFF"))
        }

        // Set icon resource
        holder.groupIcon.setImageResource(group.iconResource)

        holder.joinButton.setOnClickListener { onJoinClick(group) }
    }

    override fun getItemCount(): Int = groups.size
}
