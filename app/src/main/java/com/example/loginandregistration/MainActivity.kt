package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        // Check if user is signed in (optional, can be done in LoginActivity more effectively)
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        bottomNav = findViewById(R.id.bottom_navigation)

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home // Highlight home icon
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_groups -> replaceFragment(GroupsFragment())
                R.id.nav_tasks -> replaceFragment(TasksFragment())
                R.id.nav_calendar -> replaceFragment(CalendarFragment())
                R.id.nav_chat -> replaceFragment(ChatFragment())
                // Add a potential logout item here if you want it in bottom nav
                // R.id.nav_logout -> logoutUserAndNavigateToLogin()
                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Example: If you want a logout option from an ActionBar/Toolbar menu instead
    // override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    //     menuInflater.inflate(R.menu.main_activity_options_menu, menu) // Create this menu file
    //     return true
    // }

    // override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //     return when (item.itemId) {
    //         R.id.action_logout -> {
    //             logoutUserAndNavigateToLogin()
    //             true
    //         }
    //         else -> super.onOptionsItemSelected(item)
    //     }
    // }

    // private fun logoutUserAndNavigateToLogin() {
    //     auth.signOut()
    //     // Optional: Google Sign Out if you use it
    //     // val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    //     // val googleSignInClient = GoogleSignIn.getClient(this, gso)
    //     // googleSignInClient.signOut().addOnCompleteListener { navigateToLogin() }
    //     navigateToLogin()
    // }
}
