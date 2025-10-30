package com.example.loginandregistration.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Manages user registration with transactional rollback capability. Ensures atomic registration by
 * creating Firebase Auth user first, then Firestore document, with automatic rollback on failure.
 */
class RegistrationManager(
        private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "RegistrationManager"
    }

    /**
     * Registers a new user with transactional rollback.
     *
     * Process:
     * 1. Create Firebase Auth user
     * 2. Create Firestore user document
     * 3. If step 2 fails, delete Auth user (rollback)
     *
     * @param email User's email address
     * @param password User's password
     * @param firstName User's first name
     * @param lastName User's last name
     * @return RegistrationResult indicating success, failure, or critical error
     */
    suspend fun registerUser(
            email: String,
            password: String,
            firstName: String,
            lastName: String
    ): RegistrationResult {
        var authUserId: String? = null

        try {
            // Step 1: Create Firebase Auth user
            Log.d(TAG, "Creating Firebase Auth user for email: $email")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val authUser = authResult.user ?: throw Exception("Auth user creation returned null")

            authUserId = authUser.uid
            Log.d(TAG, "Firebase Auth user created successfully: $authUserId")

            // Step 2: Create Firestore document
            Log.d(TAG, "Creating Firestore user document for: $authUserId")
            val userDoc = createUserDocument(authUserId, email, firstName, lastName)
            firestore.collection("users").document(authUserId).set(userDoc).await()

            Log.d(TAG, "Firestore user document created successfully")

            // Step 3: Save FCM token (non-blocking, can fail without rollback)
            try {
                saveFCMToken(authUserId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save FCM token, but registration succeeded", e)
            }

            return RegistrationResult.Success(authUserId)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)

            // Rollback: Delete Auth user if Firestore creation failed
            if (authUserId != null) {
                try {
                    Log.d(TAG, "Attempting to rollback Auth user: $authUserId")
                    auth.currentUser?.delete()?.await()
                    Log.d(TAG, "Successfully rolled back Auth user creation")
                    return RegistrationResult.Failure(e)
                } catch (rollbackError: Exception) {
                    Log.e(TAG, "CRITICAL: Failed to rollback Auth user", rollbackError)
                    // This is a critical error - user exists in Auth but not Firestore
                    return RegistrationResult.CriticalError(
                            "Registration partially failed. Please contact support with code: $authUserId",
                            authUserId
                    )
                }
            }

            return RegistrationResult.Failure(e)
        }
    }

    /**
     * Creates a user document map with all required fields.
     *
     * @param userId Firebase Auth user ID
     * @param email User's email address
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Map containing all user document fields
     */
    private fun createUserDocument(
            userId: String,
            email: String,
            firstName: String,
            lastName: String
    ): Map<String, Any> {
        val displayName = "$firstName $lastName"
        val now = Timestamp.now()

        return hashMapOf(
                "uid" to userId, // Primary field matching FirebaseUser model
                "userId" to userId, // Keep for backward compatibility
                "email" to email,
                "displayName" to displayName,
                "firstName" to firstName,
                "lastName" to lastName,
                "photoUrl" to "",
                "profileImageUrl" to "",
                "initials" to getInitials(firstName, lastName),
                "online" to true, // User is online after registration
                "isOnline" to true, // Keep for backward compatibility
                "createdAt" to now,
                "lastActive" to now,
                "lastSeen" to now,
                "fcmToken" to "",
                "aiUsageCount" to 0,
                "aiPromptsUsed" to 0,
                "aiPromptsLimit" to 10
        )
    }

    /** Generates user initials from first and last name. */
    private fun getInitials(firstName: String, lastName: String): String {
        val firstInitial = firstName.firstOrNull()?.uppercase() ?: ""
        val lastInitial = lastName.firstOrNull()?.uppercase() ?: ""
        return "$firstInitial$lastInitial"
    }

    /**
     * Saves FCM token to user document (non-blocking). This is a best-effort operation that won't
     * cause registration to fail.
     */
    private suspend fun saveFCMToken(userId: String) {
        // TODO: Implement FCM token retrieval and saving
        // This is a placeholder for future implementation
        Log.d(TAG, "FCM token saving not yet implemented")
    }
}

/** Sealed class representing the result of a registration attempt. */
sealed class RegistrationResult {
    /**
     * Registration completed successfully.
     * @param userId The Firebase Auth user ID
     */
    data class Success(val userId: String) : RegistrationResult()

    /**
     * Registration failed, but system is in consistent state. Auth user was either not created or
     * successfully rolled back.
     * @param error The exception that caused the failure
     */
    data class Failure(val error: Exception) : RegistrationResult()

    /**
     * Critical error: Auth user exists but Firestore document doesn't. Manual intervention
     * required.
     * @param message User-facing error message
     * @param zombieUserId The Auth user ID that couldn't be rolled back
     */
    data class CriticalError(val message: String, val zombieUserId: String) : RegistrationResult()
}
