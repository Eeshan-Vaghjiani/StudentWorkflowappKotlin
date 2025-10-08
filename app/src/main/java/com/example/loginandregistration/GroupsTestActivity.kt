package com.example.loginandregistration

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.repository.GroupsRepository
import kotlinx.coroutines.launch

/**
 * Simple test activity to verify the repository works correctly
 * without Compose dependencies
 */
class GroupsTestActivity : AppCompatActivity() {
    
    private val repository = GroupsRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        val titleText = TextView(this).apply {
            text = "Groups Dashboard Test"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(titleText)
        
        val groupsCountText = TextView(this).apply {
            text = "My Groups: Loading..."
            textSize = 18f
            setPadding(0, 0, 0, 16)
        }
        layout.addView(groupsCountText)
        
        val tasksCountText = TextView(this).apply {
            text = "Active Tasks: Loading..."
            textSize = 18f
            setPadding(0, 0, 0, 16)
        }
        layout.addView(tasksCountText)
        
        val messagesCountText = TextView(this).apply {
            text = "New Messages: Loading..."
            textSize = 18f
            setPadding(0, 0, 0, 16)
        }
        layout.addView(messagesCountText)
        
        setContentView(layout)
        
        // Test the repository
        lifecycleScope.launch {
            try {
                val groupsCount = repository.getMyGroupsCount()
                val tasksCount = repository.getActiveAssignmentsCount()
                val messagesCount = repository.getNewMessagesCount()
                
                runOnUiThread {
                    groupsCountText.text = "My Groups: $groupsCount"
                    tasksCountText.text = "Active Tasks: $tasksCount"
                    messagesCountText.text = "New Messages: $messagesCount"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    groupsCountText.text = "Error: ${e.message}"
                    tasksCountText.text = ""
                    messagesCountText.text = ""
                }
            }
        }
    }
}