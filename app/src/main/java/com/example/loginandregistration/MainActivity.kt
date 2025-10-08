package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.databinding.ActivityMainBinding
import com.example.loginandregistration.repository.UserRepository
import com.example.loginandregistration.utils.NotificationChannels
import com.example.loginandregistration.utils.NotificationPermissionHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationPermissionHelper: NotificationPermissionHelper

    // Register permission launcher
    private val notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permission granted, FCM token will be saved by the helper
                } else {
                    // Permission denied, app will still work but without notifications
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if (auth.currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        // Create notification channels
        NotificationChannels.createChannels(this)

        // Initialize notification permission helper
        notificationPermissionHelper =
                NotificationPermissionHelper(this) { isGranted ->
                    if (!isGranted) {
                        // User denied permission, but app continues to work
                    }
                }
        notificationPermissionHelper.initialize(notificationPermissionLauncher)

        // Create or update user profile in Firestore
        lifecycleScope.launch {
            val userRepository = UserRepository()
            userRepository.createOrUpdateUser()
        }

        // Save FCM token on app start
        lifecycleScope.launch {
            val notificationRepository =
                    com.example.loginandregistration.repository.NotificationRepository()
            val result = notificationRepository.saveFcmToken()
            if (result.isFailure) {
                // Token will be saved when permission is granted or token is refreshed
                android.util.Log.d("MainActivity", "FCM token not yet available")
            }
        }

        // Request notification permission (will also save FCM token if granted)
        notificationPermissionHelper.requestNotificationPermission()

        val bottomNavView: BottomNavigationView = binding.bottomNavigation
        bottomNavView.setOnItemSelectedListener { menuItem ->
            var selectedFragment: Fragment = HomeFragment() // Default
            when (menuItem.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_groups -> selectedFragment = GroupsFragment()
                R.id.nav_tasks -> selectedFragment = TasksFragment()
                R.id.nav_calendar -> selectedFragment = CalendarFragment()
                R.id.nav_chat -> selectedFragment = ChatFragment()
            // Add cases for other menu items if any
            }
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
            true
        }

        // Load default fragment
        if (savedInstanceState == null) {
            bottomNavView.selectedItemId = R.id.nav_home // Set Home as default
        }

        // Handle deep linking from notifications
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Handles deep linking from notification taps. Routes to appropriate screen based on
     * notification data.
     */
    private fun handleNotificationIntent(intent: Intent?) {
        intent ?: return

        val fromNotification = intent.getBooleanExtra("fromNotification", false)
        if (!fromNotification) return

        when {
            // Handle chat notification
            intent.hasExtra("chatId") -> {
                val chatId = intent.getStringExtra("chatId")
                val chatName = intent.getStringExtra("chatName")
                if (chatId != null) {
                    openChatRoom(chatId, chatName ?: "Chat")
                }
            }
            // Handle task notification
            intent.hasExtra("taskId") -> {
                val taskId = intent.getStringExtra("taskId")
                val action = intent.getStringExtra("action")

                if (action == "mark_complete" && taskId != null) {
                    // Handle mark complete action
                    handleMarkTaskComplete(taskId)
                } else if (taskId != null) {
                    // Navigate to tasks screen
                    // Since we don't have TaskDetailsActivity yet, navigate to tasks tab
                    navigateToTasksScreen()
                }
            }
            // Handle group notification
            intent.hasExtra("groupId") -> {
                val groupId = intent.getStringExtra("groupId")
                if (groupId != null) {
                    // Navigate to groups screen
                    navigateToGroupsScreen()
                }
            }
        }
    }

    /** Opens a specific chat room. */
    private fun openChatRoom(chatId: String, chatName: String) {
        val intent =
                Intent(this, ChatRoomActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatName", chatName)
                }
        startActivity(intent)
    }

    /** Handles marking a task as complete from notification action. */
    private fun handleMarkTaskComplete(taskId: String) {
        lifecycleScope.launch {
            val taskRepository =
                    com.example.loginandregistration.repository.TaskRepository(this@MainActivity)
            val success = taskRepository.completeTask(taskId)

            if (success) {
                // Show success message
                val snackbar =
                        com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "Task marked as complete",
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        )
                snackbar.show()

                // Cancel the notification
                val notificationHelper =
                        com.example.loginandregistration.utils.NotificationHelper(this@MainActivity)
                notificationHelper.cancelNotification("task", taskId)
            } else {
                // Show error message
                val snackbar =
                        com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "Failed to mark task as complete",
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        )
                snackbar.show()
            }

            // Navigate to tasks screen to show updated list
            navigateToTasksScreen()
        }
    }

    // Helper method for fragments to request navigation to ProfileFragment
    fun navigateToProfile() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null) // Optional: Add to back stack
                .commit()
    }

    fun navigateToTasksScreen() {
        binding.bottomNavigation.selectedItemId = R.id.nav_tasks
    }

    fun navigateToCalendarScreen() {
        binding.bottomNavigation.selectedItemId = R.id.nav_calendar
    }

    fun navigateToGroupsScreen() {
        binding.bottomNavigation.selectedItemId = R.id.nav_groups
    }
}
