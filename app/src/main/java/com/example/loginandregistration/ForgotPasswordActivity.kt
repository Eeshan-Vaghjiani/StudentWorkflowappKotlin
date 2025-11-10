package com.example.loginandregistration

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupToolbar()
        setupValidation()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        etEmail = findViewById(R.id.etEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reset Password"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupValidation() {
        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail()
            }
        })
    }

    private fun validateEmail(): Boolean {
        val email = etEmail.text.toString().trim()
        return when {
            email.isEmpty() -> {
                emailInputLayout.error = null
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInputLayout.error = "Please enter a valid email address"
                false
            }
            else -> {
                emailInputLayout.error = null
                true
            }
        }
    }

    private fun setupClickListeners() {
        btnResetPassword.setOnClickListener {
            if (validateForm()) {
                sendPasswordResetEmail()
            }
        }
    }

    private fun validateForm(): Boolean {
        val email = etEmail.text.toString().trim()

        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Please enter a valid email address"
            return false
        }

        emailInputLayout.error = null
        return true
    }

    private fun sendPasswordResetEmail() {
        val email = etEmail.text.toString().trim()

        showLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    showSuccessDialog()
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record", ignoreCase = true) == true ->
                            "No account found with this email address"
                        task.exception?.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Please check your connection and try again"
                        else -> "Failed to send reset email. Please try again"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showSuccessDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Email Sent")
            .setMessage("A password reset link has been sent to ${etEmail.text}. Please check your email and follow the instructions to reset your password.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnResetPassword.isEnabled = !show
        etEmail.isEnabled = !show
    }
}
