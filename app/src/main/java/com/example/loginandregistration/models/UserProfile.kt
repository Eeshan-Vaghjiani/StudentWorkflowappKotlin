package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * User profile data model for Firestore user documents. This model represents the complete user
 * profile information stored in the 'users' collection.
 *
 * Required fields: userId, displayName, email, createdAt, lastActive Optional fields: All other
 * fields have default values
 *
 * Note: @IgnoreExtraProperties allows Firestore to ignore fields in documents that don't exist in
 * this model, preventing "No setter/field found" warnings for legacy or extra fields.
 *
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6
 */
@IgnoreExtraProperties
data class UserProfile(
        // Core identity fields
        @PropertyName("userId") val userId: String = "",
        @PropertyName("uid") val uid: String = "", // Alternative field name
        @PropertyName("displayName") val displayName: String = "",
        @PropertyName("email") val email: String = "",
        @PropertyName("photoUrl") val photoUrl: String? = null,
        @PropertyName("profileImageUrl") val profileImageUrl: String? = null,

        // Additional profile fields (from Firestore documents)
        @PropertyName("firstName") val firstName: String? = null,
        @PropertyName("lastName") val lastName: String? = null,
        @PropertyName("bio") val bio: String? = null,
        @PropertyName("phoneNumber") val phoneNumber: String? = null,

        // Status fields
        @PropertyName("isOnline") val isOnline: Boolean = false,
        @PropertyName("fcmToken") val fcmToken: String? = null,

        // Timestamps
        @ServerTimestamp @PropertyName("createdAt") val createdAt: Timestamp? = null,
        @ServerTimestamp @PropertyName("lastActive") val lastActive: Timestamp? = null,

        // Statistics
        @PropertyName("tasksCompleted") val tasksCompleted: Int = 0,
        @PropertyName("groupsJoined") val groupsJoined: Int = 0,
        @PropertyName("aiPromptsUsed") val aiPromptsUsed: Int = 0,
        @PropertyName("aiPromptsLimit") val aiPromptsLimit: Int = 10,

        // Preferences
        @PropertyName("notificationsEnabled") val notificationsEnabled: Boolean = true,
        @PropertyName("emailNotifications") val emailNotifications: Boolean = true,
        @PropertyName("authProvider") val authProvider: String = "email"
) {
        /** No-argument constructor required by Firestore for deserialization */
        constructor() :
                this(
                        userId = "",
                        uid = "",
                        displayName = "",
                        email = "",
                        photoUrl = null,
                        profileImageUrl = null,
                        firstName = null,
                        lastName = null,
                        bio = null,
                        phoneNumber = null,
                        isOnline = false,
                        fcmToken = null,
                        createdAt = null,
                        lastActive = null,
                        tasksCompleted = 0,
                        groupsJoined = 0,
                        aiPromptsUsed = 0,
                        aiPromptsLimit = 10,
                        notificationsEnabled = true,
                        emailNotifications = true,
                        authProvider = "email"
                )

        /** Converts this UserProfile to a map for Firestore operations */
        fun toMap(): Map<String, Any?> {
                return mapOf(
                        "userId" to userId,
                        "uid" to uid,
                        "displayName" to displayName,
                        "email" to email,
                        "photoUrl" to photoUrl,
                        "profileImageUrl" to profileImageUrl,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "bio" to bio,
                        "phoneNumber" to phoneNumber,
                        "isOnline" to isOnline,
                        "fcmToken" to fcmToken,
                        "createdAt" to createdAt,
                        "lastActive" to lastActive,
                        "tasksCompleted" to tasksCompleted,
                        "groupsJoined" to groupsJoined,
                        "aiPromptsUsed" to aiPromptsUsed,
                        "aiPromptsLimit" to aiPromptsLimit,
                        "notificationsEnabled" to notificationsEnabled,
                        "emailNotifications" to emailNotifications,
                        "authProvider" to authProvider
                )
        }

        /** Checks if all required fields are populated */
        fun isValid(): Boolean {
                return userId.isNotBlank() && displayName.isNotBlank() && email.isNotBlank()
        }
}
