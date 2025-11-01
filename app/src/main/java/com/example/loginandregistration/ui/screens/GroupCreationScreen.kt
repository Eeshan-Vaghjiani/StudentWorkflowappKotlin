package com.example.loginandregistration.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.loginandregistration.ui.components.ErrorMessage
import com.example.loginandregistration.ui.components.LoadingButton

/** Compose screen for creating a new group */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreationScreen(
        onNavigateBack: () -> Unit,
        onCreateGroup: (name: String, description: String, isPublic: Boolean) -> Unit,
        isLoading: Boolean = false,
        error: String? = null,
        modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Create Group") },
                        navigationIcon = {
                            IconButton(
                                    onClick = onNavigateBack,
                                    modifier =
                                            Modifier.semantics {
                                                contentDescription = "Navigate back"
                                            }
                            ) {
                                Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        modifier.fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
        ) {
            // Group Name Field
            OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier =
                            Modifier.fillMaxWidth().semantics {
                                contentDescription = "Group name input"
                            },
                    singleLine = true,
                    isError = groupName.isBlank() && error != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier =
                            Modifier.fillMaxWidth().semantics {
                                contentDescription = "Group description input"
                            },
                    minLines = 3,
                    maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Public Group Checkbox
            Row(
                    modifier =
                            Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                                contentDescription =
                                        if (isPublic) {
                                            "Public group, checked"
                                        } else {
                                            "Public group, unchecked"
                                        }
                            },
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isPublic, onCheckedChange = { isPublic = it })
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = "Public Group", style = MaterialTheme.typography.bodyLarge)
                    Text(
                            text = "Anyone can discover and join this group",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (error != null) {
                ErrorMessage(message = error, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Create Button
            LoadingButton(
                    text = "Create Group",
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateGroup(groupName, description, isPublic)
                        }
                    },
                    isLoading = isLoading,
                    enabled = groupName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info Text
            Text(
                    text =
                            "You will be added as the group owner and can invite members after creation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
