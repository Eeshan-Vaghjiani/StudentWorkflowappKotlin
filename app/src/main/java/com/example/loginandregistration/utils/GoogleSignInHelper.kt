package com.example.loginandregistration.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.loginandregistration.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Helper class for Google Sign-In operations Handles Google Sign-In flow, Firebase authentication,
 * and Firestore user creation
 */
class GoogleSignInHelper(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    init {
        // Configure Google Sign-In
        val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(activity.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .requestProfile()
                        .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    /**
     * Get the sign-in intent to launch Google Sign-In flow
     * @return Intent to start Google Sign-In activity
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Handle the result from Google Sign-In activity
     * @param data Intent data from onActivityResult
     * @return Task containing GoogleSignInAccount if successful
     */
    fun handleSignInResult(data: Intent?): Task<GoogleSignInAccount> {
        return GoogleSignIn.getSignedInAccountFromIntent(data)
    }

    /**
     * Process Google Sign-In account and authenticate with Firebase
     * @param account GoogleSignInAccount from successful sign-in
     * @param onSuccess Callback when authentication succeeds
     * @param onFailure Callback when authentication fails with error message
     */
    fun authenticateWithFirebase(
            account: GoogleSignInAccount,
            onSuccess: (String) -> Unit,
            onFailure: (String) -> Unit
    ) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        Log.d(TAG, "signInWithCredential:success - userId: ${user.uid}")

                        // Create or update user in Firestore
                        createOrUpdateUserInFirestore(
                                userId = user.uid,
                                email = user.email ?: account.email ?: "",
                                displayName = account.displayName
                                                ?: user.displayName
                                                        ?: user.email?.substringBefore("@")
                                                        ?: "User",
                                profileImageUrl = account.photoUrl?.toString()
                                                ?: user.photoUrl?.toString() ?: "",
                                authProvider = "google",
                                onSuccess = { onSuccess(user.uid) },
                                onFailure = { error ->
                                    Log.e(TAG, "Failed to create user in Firestore: $error")
                                    // Still consider auth successful even if Firestore fails
                                    onSuccess(user.uid)
                                }
                        )
                    } else {
                        Log.w(TAG, "signInWithCredential:success but user is null")
                        onFailure("Authentication succeeded but user data is unavailable")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "signInWithCredential:failure", exception)
                    onFailure(exception.message ?: "Authentication failed")
                }
    }

    /**
     * Create or update user document in Firestore
     * @param userId Firebase user ID
     * @param email User email
     * @param displayName User display name
     * @param profileImageUrl User profile image URL
     * @param authProvider Authentication provider (email or google)
     * @param onSuccess Callback when Firestore operation succeeds
     * @param onFailure Callback when Firestore operation fails
     */
    private fun createOrUpdateUserInFirestore(
            userId: String,
            email: String,
            displayName: String,
            profileImageUrl: String,
            authProvider: String = "google",
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
    ) {
        val userRef = firestore.collection("users").document(userId)

        // First check if user exists
        userRef.get()
                .addOnSuccessListener { document ->
                    val currentTime = System.currentTimeMillis()

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
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error updating user document", e)
                                    onFailure(e.message ?: "Failed to update user")
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
                                        "authProvider" to authProvider,
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
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error creating user document", e)
                                    onFailure(e.message ?: "Failed to create user")
                                }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking user document", e)
                    onFailure(e.message ?: "Failed to check user")
                }
    }

    /** Sign out from Google and Firebase */
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "Google sign out completed")
        }
    }

    /** Revoke Google access (complete disconnect) */
    fun revokeAccess() {
        auth.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            Log.d(TAG, "Google access revoked")
        }
    }

    /**
     * Check if user is currently signed in with Google
     * @return true if signed in, false otherwise
     */
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(activity) != null
    }

    /**
     * Get the currently signed in Google account
     * @return GoogleSignInAccount if signed in, null otherwise
     */
    fun getCurrentAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(activity)
    }
}
