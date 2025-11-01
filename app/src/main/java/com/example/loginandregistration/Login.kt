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
import com.example.loginandregistration.monitoring.ProductionMonitor
import com.example.loginandregistration.repository.NotificationRepository
import com.example.loginandregistration.repository.UserProfileRepository
import com.example.loginandregistration.utils.ErrorHandler
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
    private val userProfileRepository by lazy { UserProfileRepository() }
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
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
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
                                    handleGoogleSignInError(e)
                                }
                    }
                    Activity.RESULT_CANCELED -> {
                        // User cancelled the sign-in flow - handle gracefully
                        showLoading(false)
                        Log.d(TAG, "Google sign-in cancelled by user")
                        // Don't show error message for user cancellation
                    }
                    else -> {
                        // Unexpected result code
                        showLoading(false)
                        Log.w(TAG, "Google sign-in failed with result code: ${result.resultCode}")
                        ErrorHandler.handleAuthError(this, "Sign-in failed. Please try again.")
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize ErrorLogger for comprehensive error tracking
        com.example.loginandregistration.utils.ErrorLogger.initialize(this)

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
        ProductionMonitor.logSignInAttempt("email")

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    ProductionMonitor.logSignInSuccess(user.uid, "email")

                    // Ensure user profile exists in Firestore
                    loginScope.launch {
                        val profileResult = userProfileRepository.ensureUserProfileExists()

                        if (profileResult.isSuccess) {
                            Log.d(TAG, "User profile created/updated successfully")

                            // Save FCM token after successful login
                            saveFcmTokenAfterLogin { success ->
                                showLoading(false)
                                if (success) {
                                    Log.d(TAG, "FCM token saved successfully after email login")
                                } else {
                                    Log.w(
                                            TAG,
                                            "Failed to save FCM token, but continuing with login"
                                    )
                                }
                                ErrorHandler.showSuccessMessage(
                                        this@Login,
                                        findViewById(android.R.id.content),
                                        "Welcome back!"
                                )
                                navigateToDashboard()
                            }
                        } else {
                            // Profile creation failed
                            showLoading(false)
                            val exception = profileResult.exceptionOrNull()
                            val errorMessage =
                                    exception?.message
                                            ?: getString(R.string.error_profile_creation_failed)
                            Log.e(TAG, "Profile creation failed: $errorMessage", exception)
                            showProfileCreationErrorDialog(errorMessage)
                        }
                    }
                } else {
                    showLoading(false)
                    ProductionMonitor.logSignInFailure(
                            "email",
                            "NULL_USER",
                            "User is null after successful auth"
                    )
                    ErrorHandler.handleAuthError(this, "Login failed. Please try again.")
                }
            } else {
                showLoading(false)
                Log.w(TAG, "signInWithEmail:failure", task.exception)

                val exception = task.exception
                val errorType = exception?.javaClass?.simpleName ?: "UNKNOWN"
                val errorMessage = exception?.message ?: "Unknown error"
                ProductionMonitor.logSignInFailure("email", errorType, errorMessage)

                // Use ErrorHandler for better error messages
                exception?.let {
                    ErrorHandler.handleError(this, it, findViewById(android.R.id.content))
                }
                        ?: run {
                            ErrorHandler.handleAuthError(this, "Login failed. Please try again.")
                        }
            }
        }
    }

    private fun signInWithGoogle() {
        showLoading(true)
        ProductionMonitor.logSignInAttempt("google")
        val signInIntent = googleSignInHelper.getSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInError(exception: Exception) {
        val errorMessage =
                if (exception is ApiException) {
                    when (exception.statusCode) {
                        12501 -> {
                            // User cancelled - don't show error
                            Log.d(TAG, "User cancelled Google sign-in")
                            return
                        }
                        12500 -> "Sign-in configuration error. Please contact support."
                        7 -> "Network error. Please check your connection and try again."
                        10 -> "Developer error. Please contact support."
                        else -> "Sign-in failed (Error ${exception.statusCode}). Please try again."
                    }
                } else {
                    "Sign-in failed: ${exception.message ?: "Unknown error"}"
                }

        ErrorHandler.handleAuthError(this, errorMessage)
    }

    private fun authenticateWithFirebase(account: GoogleSignInAccount) {
        showLoading(true)

        googleSignInHelper.authenticateWithFirebase(
                account = account,
                onSuccess = { userId: String ->
                    Log.d(TAG, "Firebase authentication successful for user: $userId")
                    ProductionMonitor.logSignInSuccess(userId, "google")

                    // Ensure user profile exists in Firestore
                    loginScope.launch {
                        val profileResult = userProfileRepository.ensureUserProfileExists()

                        if (profileResult.isSuccess) {
                            Log.d(TAG, "User profile created/updated successfully")

                            // Save FCM token after successful Google login
                            saveFcmTokenAfterLogin { success ->
                                showLoading(false)
                                if (success) {
                                    Log.d(TAG, "FCM token saved successfully after Google sign-in")
                                } else {
                                    Log.w(
                                            TAG,
                                            "Failed to save FCM token, but continuing with login"
                                    )
                                }
                                ErrorHandler.showSuccessMessage(
                                        this@Login,
                                        findViewById(android.R.id.content),
                                        "Welcome back!"
                                )
                                navigateToDashboard()
                            }
                        } else {
                            // Profile creation failed
                            showLoading(false)
                            val exception = profileResult.exceptionOrNull()
                            val errorMessage =
                                    exception?.message
                                            ?: getString(R.string.error_profile_creation_failed)
                            Log.e(TAG, "Profile creation failed: $errorMessage", exception)
                            showProfileCreationErrorDialog(errorMessage)
                        }
                    }
                },
                onFailure = { errorMessage: String ->
                    showLoading(false)
                    Log.e(TAG, "Firebase authentication failed: $errorMessage")
                    ProductionMonitor.logSignInFailure("google", "AUTH_FAILED", errorMessage)
                    ErrorHandler.handleAuthError(this, "Google sign-in failed: $errorMessage")
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
                                        "isOnline" to true,
                                        "authProvider" to "email"
                                )

                        userRef.update(updates)
                                .addOnSuccessListener {
                                    Log.d(
                                            TAG,
                                            "User document updated successfully for user: $userId"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error updating user document", e)
                                    // Don't block login flow if Firestore update fails
                                }
                    } else {
                        // User doesn't exist, create new document with all required fields
                        val userData =
                                hashMapOf(
                                        // Core identity fields
                                        "uid" to userId,
                                        "email" to email,
                                        "displayName" to displayName,
                                        "photoUrl" to profileImageUrl,
                                        "profileImageUrl" to profileImageUrl,
                                        "authProvider" to "email",

                                        // Timestamps
                                        "createdAt" to com.google.firebase.Timestamp.now(),
                                        "lastActive" to com.google.firebase.Timestamp.now(),

                                        // Status fields
                                        "isOnline" to true,
                                        "fcmToken" to "",

                                        // AI features
                                        "aiPromptsUsed" to 0,
                                        "aiPromptsLimit" to 10,

                                        // Additional profile fields
                                        "bio" to "",
                                        "phoneNumber" to "",

                                        // Preferences
                                        "notificationsEnabled" to true,
                                        "emailNotifications" to true,

                                        // Statistics
                                        "tasksCompleted" to 0,
                                        "groupsJoined" to 0
                                )

                        userRef.set(userData)
                                .addOnSuccessListener {
                                    Log.d(
                                            TAG,
                                            "User document created successfully with all required fields for user: $userId"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error creating user document", e)
                                    // Don't block login flow if Firestore creation fails
                                }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking user document existence", e)
                    // Don't block login flow if Firestore check fails
                }
    }

    private fun saveFcmTokenAfterLogin(onComplete: ((Boolean) -> Unit)? = null) {
        loginScope.launch {
            try {
                val result = notificationRepository.saveFcmToken()
                if (result.isSuccess) {
                    Log.d(TAG, "FCM token saved successfully after login: ${result.getOrNull()}")
                    onComplete?.invoke(true)
                } else {
                    Log.w(
                            TAG,
                            "Failed to save FCM token after login: ${result.exceptionOrNull()?.message}"
                    )
                    // Don't block login flow if FCM token save fails
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving FCM token", e)
                // Don't block login flow if FCM token save fails
                onComplete?.invoke(false)
            }
        }
    }

    private fun navigateToDashboard() { // Assuming MainActivity is DashboardActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Shows an error dialog when profile creation fails. Provides options to retry or sign out
     * based on the error type.
     */
    private fun showProfileCreationErrorDialog(errorMessage: String) {
        // Determine if this is a permission error or network error
        val isPermissionError =
                errorMessage.contains("Permission denied", ignoreCase = true) ||
                        errorMessage.contains("PERMISSION_DENIED", ignoreCase = true)
        val isNetworkError =
                errorMessage.contains("Network error", ignoreCase = true) ||
                        errorMessage.contains("UNAVAILABLE", ignoreCase = true) ||
                        errorMessage.contains("connection", ignoreCase = true)

        // Use appropriate error message from resources
        val displayMessage =
                when {
                    isPermissionError -> getString(R.string.error_profile_creation_permission)
                    isNetworkError -> getString(R.string.error_profile_creation_network)
                    else -> errorMessage
                }

        val builder =
                androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.profile_error_title))
                        .setMessage(displayMessage)
                        .setCancelable(false)

        if (isPermissionError) {
            // For permission errors, offer sign out option
            builder.setPositiveButton(getString(R.string.profile_error_sign_out)) { _, _ ->
                auth.signOut()
                googleSignInHelper.signOut()
                // Stay on login screen
            }
            builder.setNegativeButton(getString(R.string.profile_error_retry)) { _, _ ->
                // Retry by attempting to sign in again
                retryProfileCreation()
            }
        } else {
            // For network or other errors, offer retry option
            builder.setPositiveButton(getString(R.string.profile_error_retry)) { _, _ ->
                retryProfileCreation()
            }
            builder.setNegativeButton(getString(R.string.profile_error_sign_out)) { _, _ ->
                auth.signOut()
                googleSignInHelper.signOut()
                // Stay on login screen
            }
        }

        builder.show()
    }

    /** Retries profile creation for the currently authenticated user. */
    private fun retryProfileCreation() {
        if (auth.currentUser == null) {
            ErrorHandler.handleAuthError(this, getString(R.string.error_auth_required))
            return
        }

        showLoading(true)
        loginScope.launch {
            val profileResult = userProfileRepository.ensureUserProfileExists()

            if (profileResult.isSuccess) {
                Log.d(TAG, "Profile created successfully on retry")

                // Save FCM token after successful profile creation
                saveFcmTokenAfterLogin { success ->
                    showLoading(false)
                    if (success) {
                        Log.d(TAG, "FCM token saved successfully after retry")
                    } else {
                        Log.w(TAG, "Failed to save FCM token, but continuing with login")
                    }
                    ErrorHandler.showSuccessMessage(
                            this@Login,
                            findViewById(android.R.id.content),
                            "Welcome back!"
                    )
                    navigateToDashboard()
                }
            } else {
                showLoading(false)
                val exception = profileResult.exceptionOrNull()
                val errorMessage =
                        exception?.message ?: getString(R.string.error_profile_creation_failed)
                Log.e(TAG, "Profile creation retry failed: $errorMessage", exception)
                showProfileCreationErrorDialog(errorMessage)
            }
        }
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
