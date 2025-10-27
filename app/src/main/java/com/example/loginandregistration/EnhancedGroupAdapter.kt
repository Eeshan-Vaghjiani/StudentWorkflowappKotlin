package com.example.loginandregistration

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.models.FirebaseGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth

class EnhancedGroupAdapter(private val onGroupClick: (FirebaseGroup) -> Unit) :
        ListAdapter<FirebaseGroup, EnhancedGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupIcon: ImageView = itemView.findViewById(R.id.iv_group_icon)
        val groupName: TextView = itemView.findViewById(R.id.tv_group_name)
        val groupDetails: TextView = itemView.findViewById(R.id.tv_group_details)
        val adminBadge: Chip = itemView.findViewById(R.id.chip_admin_badge)
        val assignmentCount: Chip = itemView.findViewById(R.id.chip_assignment_count)
        val joinCodeLayout: View = itemView.findViewById(R.id.layout_join_code)
        val joinCodeText: TextView = itemView.findViewById(R.id.tv_join_code)
        val copyJoinCode: ImageView = itemView.findViewById(R.id.iv_copy_join_code)
        val adminActionsLayout: View = itemView.findViewById(R.id.layout_admin_actions)
        val manageMembersBtn: MaterialButton = itemView.findViewById(R.id.btn_manage_members)
        val editGroupBtn: MaterialButton = itemView.findViewById(R.id.btn_edit_group)
        val iconBackground: MaterialCardView = itemView.findViewById(R.id.card_group_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_enhanced_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        val context = holder.itemView.context

        // Basic group info
        holder.groupName.text = group.name
        holder.groupDetails.text = buildGroupDetails(group)

        // Set icon color based on group subject or use default
        val iconColor = getGroupIconColor(group.subject)
        holder.iconBackground.setCardBackgroundColor(Color.parseColor(iconColor))

        // Assignment count (placeholder - will be updated when task-group linking is implemented)
        val assignmentCount = 0 // TODO: Calculate from linked tasks
        holder.assignmentCount.text = assignmentCount.toString()

        // Check if current user is admin/owner
        val isUserAdmin = isUserGroupAdmin(group)

        // Show/hide admin badge
        holder.adminBadge.visibility = if (isUserAdmin) View.VISIBLE else View.GONE

        // Show/hide join code section for admins
        if (isUserAdmin && group.joinCode.isNotEmpty()) {
            holder.joinCodeLayout.visibility = View.VISIBLE
            holder.joinCodeText.text = group.joinCode

            // Copy join code functionality
            holder.copyJoinCode.setOnClickListener {
                copyToClipboard(context, group.joinCode)
                Toast.makeText(context, "Join code copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        } else {
            holder.joinCodeLayout.visibility = View.GONE
        }

        // Show/hide admin actions
        if (isUserAdmin) {
            holder.adminActionsLayout.visibility = View.VISIBLE

            holder.manageMembersBtn.setOnClickListener {
                // Navigate to group details for member management
                val intent = Intent(context, GroupDetailsActivity::class.java)
                intent.putExtra("GROUP_ID", group.id)
                context.startActivity(intent)
            }

            holder.editGroupBtn.setOnClickListener {
                // Navigate to group details for editing
                val intent = Intent(context, GroupDetailsActivity::class.java)
                intent.putExtra("GROUP_ID", group.id)
                intent.putExtra("EDIT_MODE", true)
                context.startActivity(intent)
            }
        } else {
            holder.adminActionsLayout.visibility = View.GONE
        }

        // Main click listener
        holder.itemView.setOnClickListener { onGroupClick(group) }
    }

    override fun onViewRecycled(holder: GroupViewHolder) {
        super.onViewRecycled(holder)
        // Clear click listeners to prevent memory leaks
        holder.itemView.setOnClickListener(null)
        holder.copyJoinCode.setOnClickListener(null)
        holder.manageMembersBtn.setOnClickListener(null)
        holder.editGroupBtn.setOnClickListener(null)
    }

    private fun buildGroupDetails(group: FirebaseGroup): String {
        val memberCount = group.members.size
        val memberText = if (memberCount == 1) "1 member" else "$memberCount members"

        // Calculate time since last activity
        val lastActiveText = formatLastActive(group.updatedAt)

        return "$memberText • ${group.subject} • $lastActiveText"
    }

    private fun formatLastActive(timestamp: com.google.firebase.Timestamp): String {
        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        return when {
            diff < 60 * 1000 -> "Active now"
            diff < 60 * 60 * 1000 -> "Active ${diff / (60 * 1000)} min ago"
            diff < 24 * 60 * 60 * 1000 -> "Active ${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "Active ${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> "Active over a week ago"
        }
    }

    private fun isUserGroupAdmin(group: FirebaseGroup): Boolean {
        return currentUserId != null &&
                (group.owner == currentUserId ||
                        group.members.any {
                            it.userId == currentUserId && it.role in listOf("owner", "admin")
                        })
    }

    private fun getGroupIconColor(subject: String): String {
        return when (subject.lowercase()) {
            "computer science", "programming", "coding" -> "#007AFF"
            "mathematics", "math", "calculus", "algebra" -> "#FF9500"
            "science", "physics", "chemistry", "biology" -> "#34C759"
            "english", "literature", "writing" -> "#AF52DE"
            "history", "social studies" -> "#FF3B30"
            "art", "design", "creative" -> "#5856D6"
            else -> "#8E8E93"
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Join Code", text)
        clipboard.setPrimaryClip(clip)
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<FirebaseGroup>() {
        override fun areItemsTheSame(oldItem: FirebaseGroup, newItem: FirebaseGroup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FirebaseGroup, newItem: FirebaseGroup): Boolean {
            return oldItem == newItem
        }
    }
}
