package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.databinding.ActivityMainBinding
import com.example.loginandregistration.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

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

        // Create or update user profile in Firestore
        lifecycleScope.launch {
            val userRepository = UserRepository()
            userRepository.createOrUpdateUser()
        }

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
