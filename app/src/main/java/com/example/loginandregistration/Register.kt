package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // UI Components
    private lateinit var firstNameInputLayout: TextInputLayout
    private lateinit var lastNameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etRegEmail: TextInputEditText
    private lateinit var etRegPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLogin: TextView
    private lateinit var passwordStrengthBar: ProgressBar
    private lateinit var tvPasswordStrength: TextView
    private lateinit var loadingOverlay: FrameLayout

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        firstNameInputLayout = findViewById(R.id.firstNameInputLayout)
        lastNameInputLayout = findViewById(R.id.lastNameInputLayout)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar)
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        // Setup real-time validation
        setupValidation()

        btnRegister.setOnClickListener {
            if (validateForm()) {
                performRegistration()
            }
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupValidation() {
        // First name validation
        etFirstName.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateFirstName()
                    }
                }
        )

        // Last name validation
        etLastName.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateLastName()
                    }
                }
        )

        // Email validation
        etRegEmail.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateEmail()
                    }
                }
        )

        // Password validation with strength indicator
        etRegPassword.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validatePassword()
                        updatePasswordStrength(s.toString())
                    }
                }
        )

        // Confirm password validation
        etConfirmPassword.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        validateConfirmPassword()
                    }
                }
        )
    }

    private fun validateFirstName(): Boolean {
        val firstName = etFirstName.text.toString().trim()
        return when {
            firstName.isEmpty() -> {
                firstNameInputLayout.error = null
                true
            }
            firstName.length < 2 -> {
                firstNameInputLayout.error = "First name must be at least 2 characters"
                false
            }
            else -> {
                firstNameInputLayout.error = null
                true
            }
        }
    }

    private fun validateLastName(): Boolean {
        val lastName = etLastName.text.toString().trim()
        return when {
            lastName.isEmpty() -> {
                lastNameInputLayout.error = null
                true
            }
            lastName.length < 2 -> {
                lastNameInputLayout.error = "Last name must be at least 2 characters"
                false
            }
            else -> {
                lastNameInputLayout.error = null
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = etRegEmail.text.toString().trim()
        return when {
            email.isEmpty() -> {
                emailInputLayout.error = null
                true
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

    private fun validatePassword(): Boolean {
        val password = etRegPassword.text.toString()
        return when {
            password.isEmpty() -> {
                passwordInputLayout.error = null
                true
            }
            password.length < 8 -> {
                passwordInputLayout.error = "Password must be at least 8 characters"
                false
            }
            !password.any { it.isUpperCase() } -> {
                passwordInputLayout.error = "Password must contain at least one uppercase letter"
                false
            }
            !password.any { it.isLowerCase() } -> {
                passwordInputLayout.error = "Password must contain at least one lowercase letter"
                false
            }
            !password.any { it.isDigit() } -> {
                passwordInputLayout.error = "Password must contain at least one number"
                false
            }
            else -> {
                passwordInputLayout.error = null
                true
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val password = etRegPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        return when {
            confirmPassword.isEmpty() -> {
                confirmPasswordInputLayout.error = null
                true
            }
            password != confirmPassword -> {
                confirmPasswordInputLayout.error = "Passwords don't match"
                false
            }
            else -> {
                confirmPasswordInputLayout.error = null
                true
            }
        }
    }

    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            passwordStrengthBar.progress = 0
            tvPasswordStrength.text = "Password strength"
            tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            return
        }

        var strength = 0

        // Length check
        if (password.length >= 8) strength += 20
        if (password.length >= 12) strength += 10

        // Character variety checks
        if (password.any { it.isLowerCase() }) strength += 20
        if (password.any { it.isUpperCase() }) strength += 20
        if (password.any { it.isDigit() }) strength += 20
        if (password.any { !it.isLetterOrDigit() }) strength += 10

        passwordStrengthBar.progress = strength

        when {
            strength < 40 -> {
                passwordStrengthBar.progressTintList =
                        ContextCompat.getColorStateList(this, R.color.auth_password_weak)
                tvPasswordStrength.text = "Weak password"
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.auth_password_weak)
                )
            }
            strength < 60 -> {
                passwordStrengthBar.progressTintList =
                        ContextCompat.getColorStateList(this, R.color.auth_password_fair)
                tvPasswordStrength.text = "Fair password"
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.auth_password_fair)
                )
            }
            strength < 80 -> {
                passwordStrengthBar.progressTintList =
                        ContextCompat.getColorStateList(this, R.color.auth_password_good)
                tvPasswordStrength.text = "Good password"
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.auth_password_good)
                )
            }
            else -> {
                passwordStrengthBar.progressTintList =
                        ContextCompat.getColorStateList(this, R.color.auth_password_strong)
                tvPasswordStrength.text = "Strong password"
                tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this, R.color.auth_password_strong)
                )
            }
        }
    }

    private fun validateForm(): Boolean {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etRegEmail.text.toString().trim()
        val password = etRegPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        var isValid = true

        if (firstName.isEmpty()) {
            firstNameInputLayout.error = "First name is required"
            isValid = false
        } else if (firstName.length < 2) {
            firstNameInputLayout.error = "First name must be at least 2 characters"
            isValid = false
        } else {
            firstNameInputLayout.error = null
        }

        if (lastName.isEmpty()) {
            lastNameInputLayout.error = "Last name is required"
            isValid = false
        } else if (lastName.length < 2) {
            lastNameInputLayout.error = "Last name must be at least 2 characters"
            isValid = false
        } else {
            lastNameInputLayout.error = null
        }

        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Please enter a valid email address"
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordInputLayout.error = "Password must be at least 8 characters"
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords don't match"
            isValid = false
        } else {
            confirmPasswordInputLayout.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
    }

    private fun performRegistration() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etRegEmail.text.toString().trim()
        val password = etRegPassword.text.toString().trim()

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createUserWithEmail:success")
                val user = auth.currentUser
                if (user != null) {
                    createUserInFirestore(user.uid, email, firstName, lastName)
                } else {
                    showLoading(false)
                    Toast.makeText(
                                    this,
                                    getString(R.string.registration_successful),
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    navigateToDashboard()
                }
            } else {
                showLoading(false)
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                val errorMessage =
                        when (task.exception?.message) {
                            "The email address is already in use by another account." ->
                                    "This email is already registered"
                            "The email address is badly formatted." -> "Invalid email format"
                            else -> task.exception?.message ?: getString(R.string.unknown_error)
                        }
                Toast.makeText(this, "Registration Failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createUserInFirestore(
            userId: String,
            email: String,
            firstName: String,
            lastName: String
    ) {
        val displayName = "$firstName $lastName"
        val userMap =
                hashMapOf(
                        "userId" to userId,
                        "email" to email,
                        "displayName" to displayName,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "photoUrl" to "",
                        "profileImageUrl" to "",
                        "online" to true,
                        "lastSeen" to Date(),
                        "lastActive" to Date(),
                        "aiPromptsUsed" to 0,
                        "aiPromptsLimit" to 10
                )

        firestore
                .collection("users")
                .document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    showLoading(false)
                    Log.d(TAG, "User document created successfully")
                    Toast.makeText(
                                    this,
                                    getString(R.string.registration_successful),
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    navigateToDashboard()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Log.e(TAG, "Error creating user document", e)
                    Toast.makeText(
                                    this,
                                    "Registration successful but profile creation failed",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    navigateToDashboard()
                }
    }

    private fun navigateToDashboard() { // Assuming MainActivity is DashboardActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish RegisterActivity
    }
}
