package com.example.loginandregistration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.loginandregistration.utils.DefaultAvatarGenerator
import com.google.android.material.chip.Chip

class MembersAdapter(
        private val members: List<Member>,
        private val onMemberClick: (Member) -> Unit
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        val memberInitial: TextView = itemView.findViewById(R.id.tv_member_initial)
        val memberName: TextView = itemView.findViewById(R.id.tv_member_name)
        val memberEmail: TextView = itemView.findViewById(R.id.tv_member_email)
        val roleChip: Chip = itemView.findViewById(R.id.chip_member_role)
        val optionsButton: ImageButton = itemView.findViewById(R.id.btn_member_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]

        holder.memberName.text = member.name
        holder.memberEmail.text = member.email

        // Load profile image or show avatar with initials
        if (member.profileImageUrl.isNotEmpty()) {
            holder.profileImageView.visibility = View.VISIBLE
            holder.memberInitial.visibility = View.GONE
            holder.profileImageView.load(member.profileImageUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.circle_background)
                error(R.drawable.circle_background)
            }
        } else {
            holder.profileImageView.visibility = View.GONE
            holder.memberInitial.visibility = View.VISIBLE
            
            // Generate avatar with initials using DefaultAvatarGenerator
            val initials = DefaultAvatarGenerator.getInitials(member.name)
            holder.memberInitial.text = initials
            
            // Generate consistent color based on user ID
            val color = DefaultAvatarGenerator.generateColorFromString(member.userId)
            holder.memberInitial.setBackgroundColor(color)
        }

        // Show role
        if (member.isAdmin) {
            holder.roleChip.text = "Admin"
            holder.roleChip.visibility = View.VISIBLE
        } else {
            holder.roleChip.text = "Member"
            holder.roleChip.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener { onMemberClick(member) }
        holder.optionsButton.setOnClickListener { onMemberClick(member) }
    }

    override fun getItemCount(): Int = members.size
}
