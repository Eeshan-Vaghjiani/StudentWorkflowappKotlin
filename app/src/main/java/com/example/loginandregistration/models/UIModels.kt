package com.example.loginandregistration.models

// UI Data classes for RecyclerView adapters
data class Group(
        val id: Int,
        val name: String,
        val details: String,
        val assignmentCount: Int,
        val iconColor: String,
        val iconResource: Int
)

data class Task(
        val id: Int,
        val title: String,
        val subtitle: String,
        val status: String,
        val iconColor: String,
        val statusColor: String
)

data class Activity(
        val id: Int,
        val title: String,
        val details: String,
        val iconColor: String,
        val iconResource: Int
)

data class DiscoverGroup(
        val id: Int,
        val name: String,
        val details: String,
        val iconColor: String,
        val iconResource: Int
)
