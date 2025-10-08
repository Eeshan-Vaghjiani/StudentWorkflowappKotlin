package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.utils.DebugHelper
import kotlinx.coroutines.launch

class DebugActivity : AppCompatActivity() {

    private lateinit var btnCreateDemoChat: Button
    private lateinit var btnListUsers: Button
    private lateinit var tvDebugOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        btnCreateDemoChat = findViewById(R.id.btnCreateDemoChat)
        btnListUsers = findViewById(R.id.btnListUsers)
        tvDebugOutput = findViewById(R.id.tvDebugOutput)

        btnCreateDemoChat.setOnClickListener { createDemoChat() }

        btnListUsers.setOnClickListener { listUsers() }
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
}
