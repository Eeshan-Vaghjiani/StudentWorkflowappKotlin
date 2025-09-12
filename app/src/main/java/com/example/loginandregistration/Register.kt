package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth // Correct import
import com.google.firebase.auth.ktx.auth // KTX import for Firebase.auth
import com.google.firebase.ktx.Firebase // KTX import for Firebase.auth

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth // Using KTX version

        val etRegEmail = findViewById<EditText>(R.id.etRegEmail)
        val etRegPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Set text/hint from string resources (assuming these exist)
        // etRegEmail.hint = getString(R.string.email_hint)
        // etRegPassword.hint = getString(R.string.password_hint)
        // btnRegister.text = getString(R.string.register_button)
        // tvLogin.text = getString(R.string.go_to_login_prompt)

        btnRegister.setOnClickListener {
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_credentials_register), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                        // Optional: Automatically sign in the user or send verification email
                        // val user = auth.currentUser
                        navigateToDashboard() // Changed to navigateToDashboard
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, getString(R.string.registration_failed, task.exception?.message ?: getString(R.string.unknown_error)), Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Finish RegisterActivity
        }
    }

    private fun navigateToDashboard() { // Assuming MainActivity is DashboardActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish RegisterActivity
    }
}
