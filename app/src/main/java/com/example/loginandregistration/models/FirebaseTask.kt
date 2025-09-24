package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FirebaseTask(
        @DocumentId val id: String = "",
        val title: String = "",
        val description: String = "",
        val subject: String = "",
        val category: String = "personal", // personal, group, assignment
        val status: String = "pending", // pending, completed, overdue
        val priority: String = "medium", // low, medium, high
        val dueDate: Timestamp? = null,
        val createdAt: Timestamp = Timestamp.now(),
        val updatedAt: Timestamp = Timestamp.now(),
        val userId: String = "",
        val groupId: String? = null,
        val completedAt: Timestamp? = null
) {
    // No-argument constructor for Firestore
    constructor() :
            this(
                    "",
                    "",
                    "",
                    "",
                    "personal",
                    "pending",
                    "medium",
                    null,
                    Timestamp.now(),
                    Timestamp.now(),
                    "",
                    null,
                    null
            )
}
