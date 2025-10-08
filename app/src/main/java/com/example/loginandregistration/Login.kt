package com.example.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth // Correct import
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth // KTX import for Firebase.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase // KTX import for Firebase.auth
import java.util.Date

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        if (account != null) {
                            Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")
                            firebaseAuthWithGoogle(account)
                        } else {
                            Log.w(TAG, "Google sign in failed: Account is null")
                            Toast.makeText(
                                            this,
                                            getString(
                                                    R.string.error_google_signin_failed_no_account
                                            ),
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    } catch (e: ApiException) {
                        Log.w(TAG, "Google sign in failed", e)
                        Toast.makeText(
                                        this,
                                        getString(
                                                R.string.error_google_signin_failed_status,
                                                e.statusCode
                                        ),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                } else {
                    Log.w(
                            TAG,
                            "Google sign in cancelled or failed: resultCode=${result.resultCode}"
                    )
                    if (result.resultCode != Activity.RESULT_CANCELED
                    ) { // Show message if not just cancelled by user
                        Toast.makeText(
                                        this,
                                        getString(R.string.error_google_signin_cancelled),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth // Using KTX version
        firestore = FirebaseFirestore.getInstance()

        val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                                this,
                                getString(R.string.error_empty_credentials),
                                Toast.LENGTH_SHORT
                        )
                        .show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Create or update user document in Firestore
                        createOrUpdateUserInFirestore(
                                userId = user.uid,
                                email = user.email ?: email,
                                displayName = user.displayName ?: email.substringBefore("@"),
                                profileImageUrl = user.photoUrl?.toString() ?: ""
                        )
                    }
                    Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT)
                            .show()
                    navigateToDashboard()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                                    this,
                                    getString(
                                            R.string.login_failed,
                                            task.exception?.message
                                                    ?: getString(R.string.unknown_error)
                                    ),
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            }
        }

        tvRegister.setOnClickListener { startActivity(Intent(this, Register::class.java)) }

        btnGoogleLogin.setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    // Create or update user document in Firestore
                    createOrUpdateUserInFirestore(
                            userId = user.uid,
                            email = user.email ?: "",
                            displayName = account.displayName
                                            ?: user.email?.substringBefore("@") ?: "User",
                            profileImageUrl = account.photoUrl?.toString() ?: ""
                    )
                }
                Toast.makeText(
                                this,
                                getString(R.string.google_login_successful),
                                Toast.LENGTH_SHORT
                        )
                        .show()
                navigateToDashboard()
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                Toast.makeText(
                                this,
                                getString(
                                        R.string.google_login_failed,
                                        task.exception?.message ?: getString(R.string.unknown_error)
                                ),
                                Toast.LENGTH_LONG
                        )
                        .show()
            }
        }
    }

    private fun createOrUpdateUserInFirestore(
            userId: String,
            email: String,
            displayName: String,
            profileImageUrl: String
    ) {
        val userMap =
                hashMapOf(
                        "userId" to userId,
                        "email" to email,
                        "displayName" to displayName,
                        "photoUrl" to profileImageUrl,
                        "profileImageUrl" to profileImageUrl,
                        "online" to true,
                        "lastSeen" to Date(),
                        "lastActive" to Date()
                )

        firestore
                .collection("users")
                .document(userId)
                .set(userMap)
                .addOnSuccessListener { Log.d(TAG, "User document created/updated successfully") }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error creating/updating user document", e)
                }
    }

    private fun updateUserOnlineStatus(userId: String, online: Boolean) {
        val updates =
                hashMapOf<String, Any>(
                        "online" to online,
                        "lastSeen" to Date(),
                        "lastActive" to Date()
                )

        firestore
                .collection("users")
                .document(userId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener { e -> Log.e(TAG, "Error updating user status", e) }
    }

    private fun navigateToDashboard() { // Assuming MainActivity is DashboardActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
