package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.utils.DebugHelper
import com.example.loginandregistration.utils.UserProfileMigration
import kotlinx.coroutines.launch

class DebugActivity : AppCompatActivity() {

    private lateinit var btnCreateDemoChat: Button
    private lateinit var btnListUsers: Button
    private lateinit var btnMemoryMonitor: Button
    private lateinit var btnMigrateProfiles: Button
    private lateinit var btnVerifyProfiles: Button
    private lateinit var tvDebugOutput: TextView

    private val migration = UserProfileMigration()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        btnCreateDemoChat = findViewById(R.id.btnCreateDemoChat)
        btnListUsers = findViewById(R.id.btnListUsers)
        btnMemoryMonitor = findViewById(R.id.btnMemoryMonitor)
        btnMigrateProfiles = findViewById(R.id.btnMigrateProfiles)
        btnVerifyProfiles = findViewById(R.id.btnVerifyProfiles)
        tvDebugOutput = findViewById(R.id.tvDebugOutput)

        btnCreateDemoChat.setOnClickListener { createDemoChat() }
        btnListUsers.setOnClickListener { listUsers() }
        btnMemoryMonitor.setOnClickListener { openMemoryMonitor() }
        btnMigrateProfiles.setOnClickListener { migrateUserProfiles() }
        btnVerifyProfiles.setOnClickListener { verifyUserProfiles() }
    }

    private fun openMemoryMonitor() {
        val intent = Intent(this, MemoryMonitorActivity::class.java)
        startActivity(intent)
    }

    private fun createDemoChat() {
        tvDebugOutput.text = "Creating demo chat..."

        lifecycleScope.launch {
            val result = DebugHelper.createDemoChat()

            result
                    .onSuccess { chatId ->
                        tvDebugOutput.text = "Demo chat created successfully!\nChat ID: $chatId"
                        Toast.makeText(this@DebugActivity, "Demo chat created!", Toast.LENGTH_LONG)
                                .show()

                        // Open the chat
                        val intent =
                                Intent(this@DebugActivity, ChatRoomActivity::class.java).apply {
                                    putExtra(ChatRoomActivity.EXTRA_CHAT_ID, chatId)
                                    putExtra(ChatRoomActivity.EXTRA_CHAT_NAME, "Demo Chat")
                                }
                        startActivity(intent)
                    }
                    .onFailure { error ->
                        tvDebugOutput.text = "Error creating demo chat:\n${error.message}"
                        Toast.makeText(
                                        this@DebugActivity,
                                        "Error: ${error.message}",
                                        Toast.LENGTH_LONG
                                )
                                .show()
                    }
        }
    }

    private fun listUsers() {
        tvDebugOutput.text = "Fetching users..."

        lifecycleScope.launch {
            val result = DebugHelper.listAllUsers()

            result
                    .onSuccess { users ->
                        val output = buildString {
                            appendLine("Found ${users.size} users:")
                            appendLine()
                            users.forEach { user ->
                                appendLine("Name: ${user["displayName"]}")
                                appendLine("Email: ${user["email"]}")
                                appendLine("ID: ${user["id"]}")
                                appendLine("Online: ${user["online"]}")
                                appendLine("---")
                            }
                        }
                        tvDebugOutput.text = output
                    }
                    .onFailure { error ->
                        tvDebugOutput.text = "Error listing users:\n${error.message}"
                    }
        }
    }

    private fun migrateUserProfiles() {
        tvDebugOutput.text = "Starting user profile migration...\nThis may take a moment..."
        btnMigrateProfiles.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = migration.migrateExistingUsers()

                val output = buildString {
                    appendLine("=== MIGRATION COMPLETE ===")
                    appendLine()
                    appendLine("Total users checked: ${result.totalUsersChecked}")
                    appendLine("Profiles created: ${result.profilesCreated}")
                    appendLine("Profiles already existed: ${result.profilesAlreadyExisted}")
                    appendLine()

                    if (result.errors.isNotEmpty()) {
                        appendLine("Errors encountered:")
                        result.errors.forEach { error -> appendLine("  - $error") }
                        appendLine()
                    }

                    if (result.success) {
                        appendLine("✓ Migration completed successfully!")
                    } else {
                        appendLine("⚠ Migration completed with errors")
                    }
                }

                tvDebugOutput.text = output

                val message =
                        if (result.success) {
                            "Migration successful! Created ${result.profilesCreated} profiles"
                        } else {
                            "Migration completed with ${result.errors.size} errors"
                        }
                Toast.makeText(this@DebugActivity, message, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                tvDebugOutput.text = "Fatal error during migration:\n${e.message}"
                Toast.makeText(
                                this@DebugActivity,
                                "Migration failed: ${e.message}",
                                Toast.LENGTH_LONG
                        )
                        .show()
            } finally {
                btnMigrateProfiles.isEnabled = true
            }
        }
    }

    private fun verifyUserProfiles() {
        tvDebugOutput.text = "Verifying user profiles..."
        btnVerifyProfiles.isEnabled = false

        lifecycleScope.launch {
            try {
                val result: Pair<Int, Int> = migration.verifyAllProfilesExist()
                val (totalUsers, validProfiles) = result

                val output = buildString {
                    appendLine("=== PROFILE VERIFICATION ===")
                    appendLine()
                    appendLine("Total users: $totalUsers")
                    appendLine("Valid profiles: $validProfiles")
                    appendLine()

                    if (totalUsers == validProfiles) {
                        appendLine("✓ All users have valid profiles!")
                    } else {
                        val missing = totalUsers - validProfiles
                        appendLine("⚠ $missing users are missing profiles")
                        appendLine()
                        appendLine("Run 'Migrate User Profiles' to fix this.")
                    }
                }

                tvDebugOutput.text = output

                val message =
                        if (totalUsers == validProfiles) {
                            "All $totalUsers users have valid profiles"
                        } else {
                            "${totalUsers - validProfiles} users missing profiles"
                        }
                Toast.makeText(this@DebugActivity, message, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                tvDebugOutput.text = "Error verifying profiles:\n${e.message}"
                Toast.makeText(
                                this@DebugActivity,
                                "Verification failed: ${e.message}",
                                Toast.LENGTH_LONG
                        )
                        .show()
            } finally {
                btnVerifyProfiles.isEnabled = true
            }
        }
    }
}
