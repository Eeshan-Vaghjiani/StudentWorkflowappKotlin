package com.example.loginandregistration.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.loginandregistration.ui.theme.Completed
import com.example.loginandregistration.ui.theme.InProgress
import com.example.loginandregistration.ui.theme.Overdue
import com.example.loginandregistration.ui.theme.Pending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A card component that displays task information
 *
 * @param title Task title
 * @param description Task description
 * @param status Task status (pending, in_progress, completed, overdue)
 * @param priority Task priority (low, medium, high)
 * @param dueDate Task due date
 * @param assigneeCount Number of assignees
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card
 */
@Composable
fun TaskCard(
        title: String,
        description: String,
        status: String,
        priority: String,
        dueDate: Date?,
        assigneeCount: Int,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val statusColor =
            when (status.lowercase()) {
                "completed" -> Completed
                "in_progress" -> InProgress
                "overdue" -> Overdue
                else -> Pending
            }

    val priorityText =
            when (priority.lowercase()) {
                "high" -> "High Priority"
                "medium" -> "Medium Priority"
                "low" -> "Low Priority"
                else -> "Normal"
            }

    Card(
            modifier =
                    modifier.fillMaxWidth().clickable(onClick = onClick).semantics {
                        contentDescription =
                                "Task: $title. Status: $status. Priority: $priorityText. " +
                                        "${assigneeCount} assignee${if (assigneeCount != 1) "s" else ""}"
                    },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Title and Status
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = status, color = statusColor)
            }

            // Description
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Row
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority and Due Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (priority.lowercase() == "high") {
                        Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = "High priority",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (dueDate != null) {
                        Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = formatDate(dueDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Assignee Count
                if (assigneeCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = assigneeCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, color: androidx.compose.ui.graphics.Color) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
                text = status.replace("_", " ").capitalize(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = color
        )
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(date)
}

private fun String.capitalize(locale: Locale): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}
