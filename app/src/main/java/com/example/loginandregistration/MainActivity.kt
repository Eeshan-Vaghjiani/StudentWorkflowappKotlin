package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val user = auth.currentUser
        if (user == null) {
            // If no user is signed in, redirect to Login screen
            startActivity(Intent(this, Login::class.java))
            finish() // Finish MainActivity to prevent user from going back to it
            return // Skip the rest of onCreate
        }

        // Display welcome message
        tvWelcome.text = "👋 Welcome, ${user.email}"

        btnLogout.setOnClickListener {
            // Sign out from Firebase
            auth.signOut()

            // Sign out from Google
            val googleSignInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Google sign out failed", Toast.LENGTH_SHORT).show()
                }
                // Redirect to Login activity regardless of Google sign-out success
                startActivity(Intent(this, Login::class.java))
                finish() // Finish MainActivity
            }
        }
    }
}
