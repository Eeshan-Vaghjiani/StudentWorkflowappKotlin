package com.example.loginandregistration.models

import com.google.firebase.Timestamp

data class FirebaseSession(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp? = null,
    val durationMinutes: Int = 0,
    val subject: String = "",
    val groupId: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)