package com.example.loginandregistration

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ActivityAdapter(
        private val activities: List<Activity>,
        private val onActivityClick: (Activity) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconBackground: MaterialCardView =
                itemView.findViewById<ImageView>(R.id.iv_activity_icon).parent as MaterialCardView
        val activityIcon: ImageView = itemView.findViewById(R.id.iv_activity_icon)
        val activityTitle: TextView = itemView.findViewById(R.id.tv_activity_title)
        val activityDetails: TextView = itemView.findViewById(R.id.tv_activity_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]

        holder.activityTitle.text = activity.title
        holder.activityDetails.text = activity.details

        // Set icon background color
        try {
            val color = Color.parseColor(activity.iconColor)
            holder.iconBackground.setCardBackgroundColor(color)
        } catch (e: IllegalArgumentException) {
            holder.iconBackground.setCardBackgroundColor(Color.parseColor("#007AFF"))
        }

        // Set icon resource
        holder.activityIcon.setImageResource(activity.iconResource)

        holder.itemView.setOnClickListener { onActivityClick(activity) }
    }

    override fun getItemCount(): Int = activities.size
}
