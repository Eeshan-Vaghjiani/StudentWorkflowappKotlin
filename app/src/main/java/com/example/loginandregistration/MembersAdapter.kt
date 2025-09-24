package com.example.loginandregistration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class MembersAdapter(
        private val members: List<Member>,
        private val onMemberClick: (Member) -> Unit
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        // Set member initial
        holder.memberInitial.text = member.name.firstOrNull()?.toString()?.uppercase() ?: "?"

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
