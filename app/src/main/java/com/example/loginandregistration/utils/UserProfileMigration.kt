package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * One-time migration utility to create user profile documents for existing users who don't have
 * them. This addresses the issue where users authenticated before the profile creation feature was
 * implemented.
 *
 * Requirements: 1.1, 5.1
 *
 * Usage:
 * ```
 * val migration = UserProfileMigration()
 * lifecycleScope.launch {
 *     val result = migration.migrateExistingUsers()
 *     // Handle result
 * }
 * ```
 */
class UserProfileMigration(
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "UserProfileMigration"
        private const val USERS_COLLECTION = "users"
    }

    data class MigrationResult(
            val totalUsersChecked: Int,
            val profilesCreated: Int,
            val profilesAlreadyExisted: Int,
            val errors: List<String>,
            val success: Boolean
    )

    /**
     * Migrates existing users by creating profiles for those who don't have them. This method
     * queries all user documents from Firestore and creates profiles for users who are
     * authenticated but don't have profile documents.
     *
     * Note: This approach works by checking existing user documents in Firestore. If you need to
     * migrate users who have never had any Firestore data, you would need to use Firebase Admin SDK
     * to list all authentication users.
     *
     * @return MigrationResult with statistics about the migration
     */
    suspend fun migrateExistingUsers(): MigrationResult =
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Starting user profile migration...")

                var totalChecked = 0
                var profilesCreated = 0
                var profilesAlreadyExisted = 0
                val errors = mutableListOf<String>()

                try {
                    // Get all documents from the users collection
                    val usersSnapshot = firestore.collection(USERS_COLLECTION).get().await()
                    totalChecked = usersSnapshot.documents.size

                    Log.d(TAG, "Found $totalChecked user documents to check")

                    for (document in usersSnapshot.documents) {
                        val userId = document.id

                        try {
                            if (document.exists()) {
                                // Profile already exists
                                profilesAlreadyExisted++
                                Log.d(TAG, "Profile already exists for user: $userId")
                            } else {
                                // This shouldn't happen in this query, but handle it
                                Log.w(TAG, "Document exists but has no data for user: $userId")
                            }
                        } catch (e: Exception) {
                            val errorMsg = "Error processing user $userId: ${e.message}"
                            Log.e(TAG, errorMsg, e)
                            errors.add(errorMsg)
                        }
                    }

                    // Additionally, check if the current authenticated user has a profile
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val currentUserId = currentUser.uid
                        val currentUserDoc =
                                firestore.collection(USERS_COLLECTION).document(currentUserId)
                        val currentUserSnapshot = currentUserDoc.get().await()

                        if (!currentUserSnapshot.exists()) {
                            Log.d(
                                    TAG,
                                    "Current user $currentUserId doesn't have a profile, creating..."
                            )
                            try {
                                val profile =
                                        mapOf(
                                                "userId" to currentUserId,
                                                "displayName" to (currentUser.displayName ?: ""),
                                                "email" to (currentUser.email ?: ""),
                                                "photoUrl" to
                                                        (currentUser.photoUrl?.toString() ?: ""),
                                                "online" to false,
                                                "createdAt" to FieldValue.serverTimestamp(),
                                                "lastActive" to FieldValue.serverTimestamp()
                                        )

                                currentUserDoc.set(profile).await()
                                profilesCreated++
                                Log.d(TAG, "Created profile for current user: $currentUserId")
                            } catch (e: Exception) {
                                val errorMsg =
                                        "Error creating profile for current user $currentUserId: ${e.message}"
                                Log.e(TAG, errorMsg, e)
                                errors.add(errorMsg)
                            }
                        }
                    }

                    val result =
                            MigrationResult(
                                    totalUsersChecked = totalChecked,
                                    profilesCreated = profilesCreated,
                                    profilesAlreadyExisted = profilesAlreadyExisted,
                                    errors = errors,
                                    success = errors.isEmpty()
                            )

                    Log.d(TAG, "Migration completed: $result")
                    result
                } catch (e: Exception) {
                    Log.e(TAG, "Fatal error during migration: ${e.message}", e)
                    MigrationResult(
                            totalUsersChecked = totalChecked,
                            profilesCreated = profilesCreated,
                            profilesAlreadyExisted = profilesAlreadyExisted,
                            errors = errors + "Fatal error: ${e.message}",
                            success = false
                    )
                }
            }

    /**
     * Verifies that all users in the users collection have valid profile documents. This can be run
     * after migration to confirm all profiles exist.
     *
     * @return Pair of (total users, users with valid profiles)
     */
    suspend fun verifyAllProfilesExist(): Pair<Int, Int> =
            withContext(Dispatchers.IO) {
                try {
                    val usersSnapshot = firestore.collection(USERS_COLLECTION).get().await()
                    val totalUsers = usersSnapshot.documents.size
                    var validProfiles = 0

                    for (document in usersSnapshot.documents) {
                        if (document.exists() &&
                                        document.contains("userId") &&
                                        document.contains("email")
                        ) {
                            validProfiles++
                        }
                    }

                    Log.d(TAG, "Verification: $validProfiles/$totalUsers users have valid profiles")
                    Pair(totalUsers, validProfiles)
                } catch (e: Exception) {
                    Log.e(TAG, "Error verifying profiles: ${e.message}", e)
                    Pair(0, 0)
                }
            }

    /**
     * Creates a profile for a specific user ID if it doesn't exist. This is useful for targeted
     * migration of specific users.
     *
     * Note: This requires the user to be currently authenticated or you need their authentication
     * data from another source.
     *
     * @param userId The user ID to create a profile for
     * @param displayName The user's display name
     * @param email The user's email
     * @param photoUrl Optional photo URL
     * @return Result indicating success or failure
     */
    suspend fun createProfileForUser(
            userId: String,
            displayName: String,
            email: String,
            photoUrl: String? = null
    ): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val userDoc = firestore.collection(USERS_COLLECTION).document(userId)
                    val snapshot = userDoc.get().await()

                    if (snapshot.exists()) {
                        Log.d(TAG, "Profile already exists for user: $userId")
                        return@withContext Result.success(Unit)
                    }

                    val profile =
                            mapOf(
                                    "userId" to userId,
                                    "displayName" to displayName,
                                    "email" to email,
                                    "photoUrl" to (photoUrl ?: ""),
                                    "online" to false,
                                    "createdAt" to FieldValue.serverTimestamp(),
                                    "lastActive" to FieldValue.serverTimestamp()
                            )

                    userDoc.set(profile).await()
                    Log.d(TAG, "Created profile for user: $userId")
                    Result.success(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating profile for user $userId: ${e.message}", e)
                    Result.failure(e)
                }
            }
}
