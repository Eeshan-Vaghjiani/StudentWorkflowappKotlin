package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FirebaseUser(
        @DocumentId val uid: String = "",
        val userId: String = "", // For backward compatibility
        val email: String = "",
        val displayName: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val photoUrl: String = "",
        val profileImageUrl: String = "",
        val initials: String = "",
        val online: Boolean = false,
        val createdAt: Timestamp = Timestamp.now(),
        val lastActive: Timestamp = Timestamp.now(),
        val lastSeen: Timestamp = Timestamp.now(),
        val fcmToken: String = "",
        val aiUsageCount: Int = 0,
        val aiPromptsUsed: Int = 0,
        val aiPromptsLimit: Int = 10
) {
    constructor() :
            this(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    false,
                    Timestamp.now(),
                    Timestamp.now(),
                    Timestamp.now(),
                    "",
                    0,
                    0,
                    10
            )
}
