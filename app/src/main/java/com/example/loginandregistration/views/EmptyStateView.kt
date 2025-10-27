package com.example.loginandregistration.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.example.loginandregistration.R
import com.example.loginandregistration.databinding.ViewEmptyStateBinding

/**
 * Custom view for displaying empty states throughout the app Includes icon, title, description, and
 * optional action button
 */
class EmptyStateView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewEmptyStateBinding

    init {
        binding = ViewEmptyStateBinding.inflate(LayoutInflater.from(context), this, true)
        orientation = VERTICAL
    }

    /**
     * Configure the empty state view
     * @param icon Resource ID for the icon drawable
     * @param title Title text to display
     * @param description Description text to display
     * @param actionButtonText Optional text for action button (null to hide button)
     * @param onActionClick Optional click listener for action button
     */
    fun configure(
            @DrawableRes icon: Int,
            title: String,
            description: String,
            actionButtonText: String? = null,
            onActionClick: (() -> Unit)? = null
    ) {
        binding.emptyStateIcon.setImageResource(icon)
        binding.emptyStateTitle.text = title
        binding.emptyStateDescription.text = description

        if (actionButtonText != null && onActionClick != null) {
            binding.emptyStateActionButton.isVisible = true
            binding.emptyStateActionButton.text = actionButtonText
            binding.emptyStateActionButton.setOnClickListener { onActionClick() }
        } else {
            binding.emptyStateActionButton.isVisible = false
        }
    }

    /** Show "No chats yet" empty state */
    fun showNoChats(onCreateChat: (() -> Unit)? = null) {
        configure(
                icon = R.drawable.ic_chat,
                title = context.getString(R.string.empty_state_no_chats_title),
                description = context.getString(R.string.empty_state_no_chats_description),
                actionButtonText =
                        if (onCreateChat != null)
                                context.getString(R.string.empty_state_no_chats_action)
                        else null,
                onActionClick = onCreateChat
        )
    }

    /** Show "No messages" empty state */
    fun showNoMessages() {
        configure(
                icon = R.drawable.ic_message,
                title = context.getString(R.string.empty_state_no_messages_title),
                description = context.getString(R.string.empty_state_no_messages_description)
        )
    }

    /** Show "No tasks" empty state */
    fun showNoTasks(onCreateTask: (() -> Unit)? = null) {
        configure(
                icon = R.drawable.ic_task,
                title = context.getString(R.string.empty_state_no_tasks_title),
                description = context.getString(R.string.empty_state_no_tasks_description),
                actionButtonText =
                        if (onCreateTask != null)
                                context.getString(R.string.empty_state_no_tasks_action)
                        else null,
                onActionClick = onCreateTask
        )
    }

    /** Show "No internet connection" empty state */
    fun showNoInternet(onRetry: (() -> Unit)? = null) {
        configure(
                icon = R.drawable.ic_no_internet,
                title = context.getString(R.string.empty_state_no_internet_title),
                description = context.getString(R.string.empty_state_no_internet_description),
                actionButtonText =
                        if (onRetry != null)
                                context.getString(R.string.empty_state_no_internet_action)
                        else null,
                onActionClick = onRetry
        )
    }

    /** Show "No groups" empty state */
    fun showNoGroups(onCreateGroup: (() -> Unit)? = null) {
        configure(
                icon = R.drawable.ic_group,
                title = context.getString(R.string.empty_state_no_groups_title),
                description = context.getString(R.string.empty_state_no_groups_description),
                actionButtonText =
                        if (onCreateGroup != null)
                                context.getString(R.string.empty_state_no_groups_action)
                        else null,
                onActionClick = onCreateGroup
        )
    }

    /** Show "No search results" empty state */
    fun showNoSearchResults() {
        configure(
                icon = R.drawable.ic_search,
                title = context.getString(R.string.empty_state_no_search_results_title),
                description = context.getString(R.string.empty_state_no_search_results_description)
        )
    }

    /** Show "No notifications" empty state */
    fun showNoNotifications() {
        configure(
                icon = R.drawable.ic_notification,
                title = context.getString(R.string.empty_state_no_notifications_title),
                description = context.getString(R.string.empty_state_no_notifications_description)
        )
    }

    /** Show custom empty state */
    fun showCustom(
            @DrawableRes icon: Int,
            title: String,
            description: String,
            actionButtonText: String? = null,
            onActionClick: (() -> Unit)? = null
    ) {
        configure(icon, title, description, actionButtonText, onActionClick)
    }
}
