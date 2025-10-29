package com.example.loginandregistration.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.loginandregistration.ui.components.ErrorMessage
import com.example.loginandregistration.ui.components.LoadingButton

/** Compose screen for creating a new task */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreationScreen(
        onNavigateBack: () -> Unit,
        onCreateTask:
                (title: String, description: String, priority: String, category: String) -> Unit,
        isLoading: Boolean = false,
        error: String? = null,
        modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    var category by remember { mutableStateOf("assignment") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val priorities = listOf("low", "medium", "high")
    val categories = listOf("assignment", "project", "exam", "meeting", "other")

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Create Task") },
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
            // Title Field
            OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier =
                            Modifier.fillMaxWidth().semantics {
                                contentDescription = "Task title input"
                            },
                    singleLine = true,
                    isError = title.isBlank() && error != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier =
                            Modifier.fillMaxWidth().semantics {
                                contentDescription = "Task description input"
                            },
                    minLines = 3,
                    maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Dropdown
            ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = it }
            ) {
                OutlinedTextField(
                        value = priority.capitalize(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded)
                        },
                        modifier =
                                Modifier.fillMaxWidth().menuAnchor().semantics {
                                    contentDescription = "Priority selector, currently $priority"
                                }
                )
                ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                ) {
                    priorities.forEach { priorityOption ->
                        DropdownMenuItem(
                                text = { Text(priorityOption.capitalize()) },
                                onClick = {
                                    priority = priorityOption
                                    priorityExpanded = false
                                },
                                modifier =
                                        Modifier.semantics { contentDescription = priorityOption }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                        value = category.capitalize(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier =
                                Modifier.fillMaxWidth().menuAnchor().semantics {
                                    contentDescription = "Category selector, currently $category"
                                }
                )
                ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { categoryOption ->
                        DropdownMenuItem(
                                text = { Text(categoryOption.capitalize()) },
                                onClick = {
                                    category = categoryOption
                                    categoryExpanded = false
                                },
                                modifier =
                                        Modifier.semantics { contentDescription = categoryOption }
                        )
                    }
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
                    text = "Create Task",
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreateTask(title, description, priority, category)
                        }
                    },
                    isLoading = isLoading,
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
