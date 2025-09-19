package com.example.loginandregistration

data class Group(
        val id: Int,
        val name: String,
        val details: String,
        val assignmentCount: Int,
        val iconColor: String,
        val iconResource: Int
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
