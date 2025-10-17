package com.example.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.loginandregistration.repository.NotificationRepository
import com.example.loginandregistration.utils.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private val notificationRepository by lazy { NotificationRepository() }
    private val loginScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // UI Components
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogleLogin: MaterialButton
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var loadingOverlay: FrameLayout

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = googleSignInHelper.handleSignInResult(result.data)
                    task
                            .addOnSuccessListener { account: GoogleSignInAccount ->
                                Log.d(
                                        TAG,
                                        "Google sign-in successful, authenticating with Firebase"
                                )
                                authenticateWithFirebase(account)
                            }
                            .addOnFailureListener { e: Exception ->
                                showLoading(false)
                                Log.w(TAG, "Google sign in failed", e)
                                val errorMessage =
                                        if (e is ApiException) {
                                            when (e.statusCode) {
                                                12501 -> "Sign-in was cancelled"
                                                12500 ->
                                                        "Sign-in configuration error. Please contact support."
                                                else -> "Google sign in failed: ${e.statusCode}"
                                            }
                                        } else {
                                            "Google sign in failed: ${e.message}"
                                        }
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            }
                } else {
                    showLoading(false)
                    Log.w(
                            TAG,
                            "Google sign in cancelled or failed: resultCode=${result.resultCode}"
                    )
                    if (result.resultCode != Activity.RESULT_CANCELED) {
                        Toast.makeText(this, "Google sign-in cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        googleSignInHelper = GoogleSignInHelper(this)

        // Initialize UI components
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        // Setup real-time validation
        setupValidation()

        btnLogin.setOnClickListener {
            if (validateForm()) {
                performLogin()
            }
        }

        tvRegister.setOnClickListener { startActivity(Intent(this, Register::class.java)) }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        btnGoogleLogin.setOnClickListener { signInWithGoogle() }
    }

    private fun setupValidation() {
        // Email validation
        etEmail.addTextChangedListener(
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

        // Password validation
        etPassword.addTextChangedListener(
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
                    }
                }
        )
    }

    private fun validateEmail(): Boolean {
        val email = etEmail.text.toString().trim()
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
        val password = etPassword.text.toString()
        return when {
            password.isEmpty() -> {
                passwordInputLayout.error = null
                true
            }
            password.length < 6 -> {
                passwordInputLayout.error = "Password must be at least 6 characters"
                false
            }
            else -> {
                passwordInputLayout.error = null
                true
            }
        }
    }

    private fun validateForm(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        var isValid = true

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
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnGoogleLogin.isEnabled = !show
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            showLoading(false)

            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    createOrUpdateUserInFirestore(
                            userId = user.uid,
                            email = user.email ?: email,
                            displayName = user.displayName ?: email.substringBefore("@"),
                            profileImageUrl = user.photoUrl?.toString() ?: ""
                    )
                    // Save FCM token after successful login
                    saveFcmTokenAfterLogin()
                }
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
            } else {
                Log.w(TAG, "signInWithEmail:failure", task.exception)
                val errorMessage =
                        when (task.exception?.message) {
                            "The email address is badly formatted." -> "Invalid email format"
                            "The password is invalid or the user does not have a password." ->
                                    "Invalid password"
                            "There is no user record corresponding to this identifier. The user may have been deleted." ->
                                    "No account found with this email"
                            else -> task.exception?.message ?: "Unknown error"
                        }
                Toast.makeText(this, "Login Failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        showLoading(true)
        val signInIntent = googleSignInHelper.getSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }

    private fun authenticateWithFirebase(account: GoogleSignInAccount) {
        showLoading(true)

        googleSignInHelper.authenticateWithFirebase(
                account = account,
                onSuccess = { userId: String ->
                    showLoading(false)
                    Log.d(TAG, "Firebase authentication successful for user: $userId")
                    // Save FCM token after successful Google login
                    saveFcmTokenAfterLogin()
                    Toast.makeText(this, "Google login successful", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                },
                onFailure = { errorMessage: String ->
                    showLoading(false)
                    Log.e(TAG, "Firebase authentication failed: $errorMessage")
                    Toast.makeText(this, "Google login failed: $errorMessage", Toast.LENGTH_LONG)
                            .show()
                }
        )
    }

    private fun createOrUpdateUserInFirestore(
            userId: String,
            email: String,
            displayName: String,
            profileImageUrl: String
    ) {
        val userRef = firestore.collection("users").document(userId)

        // Check if user exists first
        userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // User exists, update only necessary fields
                        val updates =
                                hashMapOf<String, Any>(
                                        "displayName" to displayName,
                                        "photoUrl" to profileImageUrl,
                                        "profileImageUrl" to profileImageUrl,
                                        "lastActive" to com.google.firebase.Timestamp.now(),
                                        "isOnline" to true
                                )

                        userRef.update(updates)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User document updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error updating user document", e)
                                }
                    } else {
                        // User doesn't exist, create new document
                        val userData =
                                hashMapOf(
                                        "uid" to userId,
                                        "email" to email,
                                        "displayName" to displayName,
                                        "photoUrl" to profileImageUrl,
                                        "profileImageUrl" to profileImageUrl,
                                        "authProvider" to "email",
                                        "createdAt" to com.google.firebase.Timestamp.now(),
                                        "lastActive" to com.google.firebase.Timestamp.now(),
                                        "isOnline" to true,
                                        "fcmToken" to "",
                                        "aiPromptsUsed" to 0,
                                        "aiPromptsLimit" to 10
                                )

                        userRef.set(userData)
                                .addOnSuccessListener {
                                    Log.d(TAG, "User document created successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error creating user document", e)
                                }
                    }
                }
                .addOnFailureListener { e -> Log.e(TAG, "Error checking user document", e) }
    }

    private fun saveFcmTokenAfterLogin() {
        loginScope.launch {
            val result = notificationRepository.saveFcmToken()
            if (result.isSuccess) {
                Log.d(TAG, "FCM token saved successfully after login")
            } else {
                Log.w(
                        TAG,
                        "Failed to save FCM token after login: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    private fun navigateToDashboard() { // Assuming MainActivity is DashboardActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        loginScope.coroutineContext.cancelChildren()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToDashboard() // Changed to navigateToDashboard if user already logged in
        }
    }
}
