package com.example.loginandregistration.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FirebaseUser(
    @DocumentId
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastActive: Timestamp = Timestamp.now(),
    val isOnline: Boolean = false
) {
    constructor() : this("", "", "", "", Timestamp.now(), Timestamp.now(), false)
}