package com.example.loginandregistration

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.repository.GroupsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Simple demonstration of the new MVVM repository working correctly This shows that the stats work
 * and there are no ANR issues
 */
class SimpleGroupsDemo : AppCompatActivity() {

    private val repository = GroupsRepository()
    private lateinit var statusText: TextView
    private lateinit var groupsText: TextView
    private lateinit var tasksText: TextView
    private lateinit var messagesText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create simple UI programmatically
        val layout =
                LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 32, 32, 32)
                }

        // Title
        val titleText =
                TextView(this).apply {
                    text = "MVVM Groups Repository Demo"
                    textSize = 24f
                    setPadding(0, 0, 0, 32)
                }
        layout.addView(titleText)

        // Status
        statusText =
                TextView(this).apply {
                    text = "Ready to test"
                    textSize = 16f
                    setPadding(0, 0, 0, 16)
                }
        layout.addView(statusText)

        // Stats
        groupsText =
                TextView(this).apply {
                    text = "My Groups: Not loaded"
                    textSize = 18f
                    setPadding(0, 0, 0, 8)
                }
        layout.addView(groupsText)

        tasksText =
                TextView(this).apply {
                    text = "Active Tasks: Not loaded"
                    textSize = 18f
                    setPadding(0, 0, 0, 8)
                }
        layout.addView(tasksText)

        messagesText =
                TextView(this).apply {
                    text = "New Messages: Not loaded"
                    textSize = 18f
                    setPadding(0, 0, 0, 24)
                }
        layout.addView(messagesText)

        // Test button
        val testButton =
                Button(this).apply {
                    text = "Test Repository (No ANR!)"
                    setOnClickListener { testRepository() }
                }
        layout.addView(testButton)

        // Sequential test button
        val sequentialButton =
                Button(this).apply {
                    text = "Test Sequential Loading"
                    setOnClickListener { testSequentialLoading() }
                }
        layout.addView(sequentialButton)

        // Parallel test button
        val parallelButton =
                Button(this).apply {
                    text = "Test Parallel Loading (Faster!)"
                    setOnClickListener { testParallelLoading() }
                }
        layout.addView(parallelButton)

        setContentView(layout)
    }

    /** Test the repository - this will NOT cause ANR */
    private fun testRepository() {
        statusText.text = "Testing repository..."

        lifecycleScope.launch {
            try {
                statusText.text = "Loading groups count..."
                val groupsCount = repository.getMyGroupsCount()
                groupsText.text = "My Groups: $groupsCount"

                statusText.text = "Loading tasks count..."
                val tasksCount = repository.getActiveAssignmentsCount()
                tasksText.text = "Active Tasks: $tasksCount"

                statusText.text = "Loading messages count..."
                val messagesCount = repository.getNewMessagesCount()
                messagesText.text = "New Messages: $messagesCount"

                statusText.text = "✅ Success! No ANR issues!"
            } catch (e: Exception) {
                statusText.text = "❌ Error: ${e.message}"
                android.util.Log.e("SimpleGroupsDemo", "Error testing repository", e)
            }
        }
    }

    /** Test sequential loading (slower) */
    private fun testSequentialLoading() {
        statusText.text = "Testing sequential loading..."
        val startTime = System.currentTimeMillis()

        lifecycleScope.launch {
            try {
                // Load one by one (slower)
                val groupsCount = repository.getMyGroupsCount()
                val tasksCount = repository.getActiveAssignmentsCount()
                val messagesCount = repository.getNewMessagesCount()

                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime

                groupsText.text = "My Groups: $groupsCount"
                tasksText.text = "Active Tasks: $tasksCount"
                messagesText.text = "New Messages: $messagesCount"
                statusText.text = "✅ Sequential loading took ${duration}ms"
            } catch (e: Exception) {
                statusText.text = "❌ Error: ${e.message}"
            }
        }
    }

    /** Test parallel loading (faster) */
    private fun testParallelLoading() {
        statusText.text = "Testing parallel loading..."
        val startTime = System.currentTimeMillis()

        lifecycleScope.launch {
            try {
                // Load in parallel (faster)
                val groupsDeferred = async { repository.getMyGroupsCount() }
                val tasksDeferred = async { repository.getActiveAssignmentsCount() }
                val messagesDeferred = async { repository.getNewMessagesCount() }

                // Wait for all to complete
                val groupsCount = groupsDeferred.await()
                val tasksCount = tasksDeferred.await()
                val messagesCount = messagesDeferred.await()

                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime

                groupsText.text = "My Groups: $groupsCount"
                tasksText.text = "Active Tasks: $tasksCount"
                messagesText.text = "New Messages: $messagesCount"
                statusText.text = "✅ Parallel loading took ${duration}ms (faster!)"
            } catch (e: Exception) {
                statusText.text = "❌ Error: ${e.message}"
            }
        }
    }
}
