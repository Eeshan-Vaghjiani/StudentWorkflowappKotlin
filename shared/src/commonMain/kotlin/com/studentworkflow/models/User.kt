
package com.studentworkflow.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: String // Using String for simplicity, will be formatted timestamp
)
