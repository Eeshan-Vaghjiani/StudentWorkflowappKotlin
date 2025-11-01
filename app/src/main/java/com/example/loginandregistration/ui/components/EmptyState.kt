package com.example.loginandregistration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A component that displays an empty state with icon, message, and optional action
 *
 * @param message The main message to display
 * @param modifier Modifier for the empty state
 * @param icon Icon to display (defaults to Inbox)
 * @param description Optional additional description text
 * @param actionText Optional text for action button
 * @param onAction Optional callback when action button is clicked
 */
@Composable
fun EmptyState(
        message: String,
        modifier: Modifier = Modifier,
        icon: ImageVector = Icons.Default.Inbox,
        description: String? = null,
        actionText: String? = null,
        onAction: (() -> Unit)? = null
) {
    Column(
            modifier =
                    modifier.fillMaxSize().padding(32.dp).semantics {
                        contentDescription =
                                "Empty state: $message${description?.let { ". $it" } ?: ""}"
                    },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
            )
        }

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                    onClick = onAction,
                    modifier = Modifier.semantics { contentDescription = actionText }
            ) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = actionText)
            }
        }
    }
}

/** Predefined empty states for common scenarios */
object EmptyStates {
    @Composable
    fun NoTasks(onCreateTask: () -> Unit, modifier: Modifier = Modifier) {
        EmptyState(
                message = "No tasks yet",
                description = "Create your first task to get started",
                actionText = "Create Task",
                onAction = onCreateTask,
                modifier = modifier
        )
    }

    @Composable
    fun NoGroups(onCreateGroup: () -> Unit, modifier: Modifier = Modifier) {
        EmptyState(
                message = "No groups yet",
                description = "Join or create a group to collaborate",
                actionText = "Create Group",
                onAction = onCreateGroup,
                modifier = modifier
        )
    }

    @Composable
    fun NoMessages(modifier: Modifier = Modifier) {
        EmptyState(
                message = "No messages yet",
                description = "Start a conversation",
                modifier = modifier
        )
    }

    @Composable
    fun NoNotifications(modifier: Modifier = Modifier) {
        EmptyState(
                message = "No notifications",
                description = "You're all caught up!",
                modifier = modifier
        )
    }
}

private object ButtonDefaults {
    val IconSpacing = 8.dp
}
