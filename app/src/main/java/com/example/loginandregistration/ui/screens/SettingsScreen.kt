package com.example.loginandregistration.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Compose screen for app settings */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        onNavigateBack: () -> Unit,
        onProfileClick: () -> Unit,
        onNotificationsClick: () -> Unit,
        onPrivacyClick: () -> Unit,
        onAboutClick: () -> Unit,
        onLogoutClick: () -> Unit,
        isDarkMode: Boolean,
        onDarkModeToggle: (Boolean) -> Unit,
        notificationsEnabled: Boolean,
        onNotificationsToggle: (Boolean) -> Unit,
        modifier: Modifier = Modifier
) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Settings") },
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
                                .verticalScroll(rememberScrollState())
        ) {
            // Account Section
            SettingsSection(title = "Account") {
                SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = "Manage your profile information",
                        onClick = onProfileClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences Section
            SettingsSection(title = "Preferences") {
                SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle
                )
                HorizontalDivider()
                SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Enable push notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle
                )
                HorizontalDivider()
                SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notification Settings",
                        subtitle = "Customize notification preferences",
                        onClick = onNotificationsClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Security Section
            SettingsSection(title = "Privacy & Security") {
                SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy",
                        subtitle = "Manage your privacy settings",
                        onClick = onPrivacyClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About TeamSync",
                        subtitle = "Version 1.0.0",
                        onClick = onAboutClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Section
            SettingsSection(title = "") {
                SettingsItem(
                        icon = Icons.Default.Logout,
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        onClick = onLogoutClick,
                        isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        if (title.isNotEmpty()) {
            Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) { content() }
    }
}

@Composable
private fun SettingsItem(
        icon: ImageVector,
        title: String,
        subtitle: String,
        onClick: () -> Unit,
        isDestructive: Boolean = false
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp).semantics {
                        contentDescription = "$title. $subtitle"
                    },
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                        if (isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                            if (isDestructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
            )
            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
        icon: ImageVector,
        title: String,
        subtitle: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable { onCheckedChange(!checked) }
                            .padding(16.dp)
                            .semantics {
                                contentDescription =
                                        "$title. $subtitle. ${if (checked) "Enabled" else "Disabled"}"
                            },
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
