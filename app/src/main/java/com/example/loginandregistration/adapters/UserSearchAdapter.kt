package com.example.loginandregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.loginandregistration.R
import com.example.loginandregistration.models.UserInfo
import com.example.loginandregistration.utils.DefaultAvatarGenerator

class UserSearchAdapter(private val onUserClick: (UserInfo) -> Unit) :
        ListAdapter<UserInfo, UserSearchAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_user_search, parent, false)
        return UserViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(itemView: View, private val onUserClick: (UserInfo) -> Unit) :
            RecyclerView.ViewHolder(itemView) {

        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val avatarTextView: TextView = itemView.findViewById(R.id.avatarTextView)
        private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val userEmailTextView: TextView = itemView.findViewById(R.id.userEmailTextView)

        fun bind(user: UserInfo) {
            // Set user name
            userNameTextView.text = user.displayName

            // Set user email
            userEmailTextView.text = user.email

            // Set profile image or avatar
            if (user.profileImageUrl.isNotEmpty()) {
                profileImageView.visibility = View.VISIBLE
                avatarTextView.visibility = View.GONE
                profileImageView.load(user.profileImageUrl) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.circle_background)
                    error(R.drawable.circle_background)
                }
            } else {
                profileImageView.visibility = View.GONE
                avatarTextView.visibility = View.VISIBLE

                // Generate avatar with initials using DefaultAvatarGenerator
                val initials = DefaultAvatarGenerator.getInitials(user.displayName)
                avatarTextView.text = initials

                // Generate color based on user ID for consistency
                val color = DefaultAvatarGenerator.generateColorFromString(user.userId)
                avatarTextView.setBackgroundColor(color)
            }

            // Set click listener
            itemView.setOnClickListener { onUserClick(user) }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }
}
