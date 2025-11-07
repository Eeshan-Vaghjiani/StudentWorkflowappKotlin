package com.example.loginandregistration.repository

import android.util.Log
import com.example.loginandregistration.models.UserProfile
import com.example.loginandregistration.monitoring.ProductionMonitor
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for managing user profile documents in Firestore. Handles profile creation, updates,
 * and validation with comprehensive error handling.
 */
class UserProfileRepository(
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "UserProfileRepository"
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Ensures a user profile exists in Firestore for the current authenticated user. Creates a new
     * profile if it doesn't exist, or updates lastActive if it does.
     *
     * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2, 4.3
     *
     * @return Result.success(Unit) if profile exists/created successfully
     * @return Result.failure with descriptive error message if operation fails
     */
    suspend fun ensureUserProfileExists(): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        Log.e(TAG, "ensureUserProfileExists: No authenticated user")
                        return@withContext Result.failure(
                                Exception("No authenticated user. Please sign in first.")
                        )
                    }

                    val userId = currentUser.uid
                    val userDoc = firestore.collection(USERS_COLLECTION).document(userId)

                    // Check if profile exists
                    val snapshot = userDoc.get().await()

                    if (snapshot.exists()) {
                        // Profile exists, update lastActive timestamp
                        ProductionMonitor.logProfileCreationAttempt(userId, isNewUser = false)
                        userDoc.update("lastActive", FieldValue.serverTimestamp()).await()
                        Log.d(TAG, "Profile exists for user $userId, updated lastActive")
                        ProductionMonitor.logProfileUpdateSuccess(userId)
                        return@withContext Result.success(Unit)
                    }

                    // Profile doesn't exist, create it
                    ProductionMonitor.logProfileCreationAttempt(userId, isNewUser = true)

                    val profile =
                            mapOf(
                                    "userId" to userId,
                                    "uid" to userId, // Alternative field name for compatibility
                                    "displayName" to (currentUser.displayName ?: ""),
                                    "email" to (currentUser.email ?: ""),
                                    "photoUrl" to (currentUser.photoUrl?.toString() ?: ""),
                                    "profileImageUrl" to (currentUser.photoUrl?.toString() ?: ""),
                                    "online" to false,
                                    "isOnline" to false,
                                    "createdAt" to FieldValue.serverTimestamp(),
                                    "lastActive" to FieldValue.serverTimestamp(),
                                    "tasksCompleted" to 0,
                                    "groupsJoined" to 0,
                                    "aiPromptsUsed" to 0,
                                    "aiPromptsLimit" to 10,
                                    "notificationsEnabled" to true,
                                    "emailNotifications" to true,
                                    "authProvider" to "google"
                            )

                    userDoc.set(profile).await()
                    Log.d(TAG, "Created new profile for user $userId")
                    ProductionMonitor.logProfileCreationSuccess(userId, isNewUser = true)

                    Result.success(Unit)
                } catch (e: FirebaseFirestoreException) {
                    val userId = auth.currentUser?.uid ?: "unknown"
                    val errorType = e.code.toString()
                    val errorMessage =
                            when (e.code) {
                                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                    Log.e(
                                            TAG,
                                            "Permission denied creating profile: ${e.message}",
                                            e
                                    )
                                    ProductionMonitor.logProfileCreationFailure(
                                            userId,
                                            errorType,
                                            "Permission denied"
                                    )
                                    "Unable to create profile. Please try signing out and back in."
                                }
                                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                    Log.e(TAG, "Firestore unavailable: ${e.message}", e)
                                    ProductionMonitor.logProfileCreationFailure(
                                            userId,
                                            errorType,
                                            "Network unavailable"
                                    )
                                    "Network error. Please check your connection and try again."
                                }
                                else -> {
                                    Log.e(
                                            TAG,
                                            "Firestore error creating profile: ${e.code} - ${e.message}",
                                            e
                                    )
                                    ProductionMonitor.logProfileCreationFailure(
                                            userId,
                                            errorType,
                                            e.message ?: "Unknown error"
                                    )
                                    "Failed to create profile: ${e.message}"
                                }
                            }
                    Result.failure(Exception(errorMessage))
                } catch (e: FirebaseNetworkException) {
                    val userId = auth.currentUser?.uid ?: "unknown"
                    Log.e(TAG, "Network error creating profile: ${e.message}", e)
                    ProductionMonitor.logProfileCreationFailure(
                            userId,
                            "NETWORK_ERROR",
                            e.message ?: "Network error"
                    )
                    Result.failure(
                            Exception("Network error. Please check your connection and try again.")
                    )
                } catch (e: Exception) {
                    val userId = auth.currentUser?.uid ?: "unknown"
                    Log.e(TAG, "Unexpected error creating profile: ${e.message}", e)
                    ProductionMonitor.logProfileCreationFailure(
                            userId,
                            "UNEXPECTED_ERROR",
                            e.message ?: "Unknown error"
                    )
                    Result.failure(Exception("Unexpected error: ${e.message}"))
                }
            }

    /**
     * Gets the current authenticated user's profile from Firestore.
     *
     * Requirements: 1.1, 4.4, 4.5
     *
     * @return Result.success(UserProfile) if profile found
     * @return Result.failure with error message if profile not found or error occurs
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile> =
            withContext(Dispatchers.IO) {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        Log.e(TAG, "getCurrentUserProfile: No authenticated user")
                        return@withContext Result.failure(
                                Exception("No authenticated user. Please sign in first.")
                        )
                    }

                    val userId = currentUser.uid
                    val userDoc = firestore.collection(USERS_COLLECTION).document(userId)
                    val snapshot = userDoc.get().await()

                    if (!snapshot.exists()) {
                        Log.e(TAG, "Profile not found for user $userId")
                        return@withContext Result.failure(
                                Exception(
                                        "Your profile is not set up. Please sign out and back in."
                                )
                        )
                    }

                    val profile = snapshot.toObject(UserProfile::class.java)
                    if (profile == null) {
                        Log.e(TAG, "Failed to deserialize profile for user $userId")
                        return@withContext Result.failure(
                                Exception("Failed to load profile. Please try again.")
                        )
                    }

                    Log.d(TAG, "Retrieved profile for user $userId")
                    Result.success(profile)
                } catch (e: FirebaseFirestoreException) {
                    val errorMessage =
                            when (e.code) {
                                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                    Log.e(TAG, "Permission denied reading profile: ${e.message}", e)
                                    "Unable to access profile. Please try signing out and back in."
                                }
                                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                    Log.e(TAG, "Firestore unavailable: ${e.message}", e)
                                    "Network error. Please check your connection and try again."
                                }
                                else -> {
                                    Log.e(
                                            TAG,
                                            "Firestore error reading profile: ${e.code} - ${e.message}",
                                            e
                                    )
                                    "Failed to load profile: ${e.message}"
                                }
                            }
                    Result.failure(Exception(errorMessage))
                } catch (e: FirebaseNetworkException) {
                    Log.e(TAG, "Network error reading profile: ${e.message}", e)
                    Result.failure(
                            Exception("Network error. Please check your connection and try again.")
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error reading profile: ${e.message}", e)
                    Result.failure(Exception("Unexpected error: ${e.message}"))
                }
            }

    /**
     * Updates specific fields in the current user's profile. Preserves existing fields that are not
     * included in the updates map.
     *
     * Requirements: 1.4, 5.1, 5.2, 5.3
     *
     * @param updates Map of field names to new values
     * @return Result.success(Unit) if update successful
     * @return Result.failure with error message if update fails
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        Log.e(TAG, "updateUserProfile: No authenticated user")
                        return@withContext Result.failure(
                                Exception("No authenticated user. Please sign in first.")
                        )
                    }

                    val userId = currentUser.uid
                    val userDoc = firestore.collection(USERS_COLLECTION).document(userId)

                    // Add lastActive timestamp to updates
                    val updatesWithTimestamp = updates.toMutableMap()
                    updatesWithTimestamp["lastActive"] = FieldValue.serverTimestamp()

                    userDoc.update(updatesWithTimestamp).await()
                    Log.d(TAG, "Updated profile for user $userId with ${updates.keys}")
                    ProductionMonitor.logProfileUpdateSuccess(userId)

                    Result.success(Unit)
                } catch (e: FirebaseFirestoreException) {
                    val userId = auth.currentUser?.uid ?: "unknown"
                    val errorType = e.code.toString()
                    val errorMessage =
                            when (e.code) {
                                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                    Log.e(
                                            TAG,
                                            "Permission denied updating profile: ${e.message}",
                                            e
                                    )
                                    ProductionMonitor.logProfileUpdateFailure(
                                            userId,
                                            errorType,
                                            "Permission denied"
                                    )
                                    "Unable to update profile. Please try signing out and back in."
                                }
                                FirebaseFirestoreException.Code.NOT_FOUND -> {
                                    Log.e(TAG, "Profile not found for update: ${e.message}", e)
                                    ProductionMonitor.logProfileUpdateFailure(
                                            userId,
                                            errorType,
                                            "Profile not found"
                                    )
                                    "Your profile is not set up. Please sign out and back in."
                                }
                                FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                    Log.e(TAG, "Firestore unavailable: ${e.message}", e)
                                    ProductionMonitor.logProfileUpdateFailure(
                                            userId,
                                            errorType,
                                            "Network unavailable"
                                    )
                                    "Network error. Please check your connection and try again."
                                }
                                else -> {
                                    Log.e(
                                            TAG,
                                            "Firestore error updating profile: ${e.code} - ${e.message}",
                                            e
                                    )
                                    ProductionMonitor.logProfileUpdateFailure(
                                            userId,
                                            errorType,
                                            e.message ?: "Unknown error"
                                    )
                                    "Failed to update profile: ${e.message}"
                                }
                            }
                    Result.failure(Exception(errorMessage))
                } catch (e: FirebaseNetworkException) {
                    val userId = auth.currentUser?.uid ?: "unknown"
                    Log.e(TAG, "Network error updating profile: ${e.message}", e)
                    ProductionMonitor.logProfileUpdateFailure(
                            userId,
                            "NETWORK_ERROR",
                            e.message ?: "Network error"
                    )
                    Result.failure(
                            Exception("Network error. Please check your connection and try again.")
                    )
                } catch (e: Exception) {
                    val userId = auth.currentUser?.uid ?: "unknown"
                    Log.e(TAG, "Unexpected error updating profile: ${e.message}", e)
                    ProductionMonitor.logProfileUpdateFailure(
                            userId,
                            "UNEXPECTED_ERROR",
                            e.message ?: "Unknown error"
                    )
                    Result.failure(Exception("Unexpected error: ${e.message}"))
                }
            }

    /**
     * Checks if a user profile exists in Firestore.
     *
     * Requirements: 1.1, 4.4
     *
     * @param userId The user ID to check
     * @return true if profile exists, false otherwise
     */
    suspend fun profileExists(userId: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val userDoc = firestore.collection(USERS_COLLECTION).document(userId)
                    val snapshot = userDoc.get().await()
                    val exists = snapshot.exists()
                    Log.d(TAG, "Profile exists check for user $userId: $exists")
                    exists
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking if profile exists for user $userId: ${e.message}", e)
                    false
                }
            }
}
