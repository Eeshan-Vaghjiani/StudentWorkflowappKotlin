package com.example.loginandregistration

import android.app.Application
import coil.Coil
import com.example.loginandregistration.utils.FirestoreConfig
import com.example.loginandregistration.utils.ImageLoaderConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Custom Application class for the Team Collaboration app. Initializes Firebase and enables offline
 * persistence.
 */
class TeamCollaborationApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Get Firestore instance
        val firestore = FirebaseFirestore.getInstance()

        // Enable offline persistence with unlimited cache
        FirestoreConfig.enableOfflinePersistence(firestore)

        android.util.Log.d("TeamCollaborationApp", "Firestore offline persistence enabled")

        // Configure Coil ImageLoader with disk caching for offline support
        val imageLoader = ImageLoaderConfig.createImageLoader(this)
        Coil.setImageLoader(imageLoader)

        android.util.Log.d(
                "TeamCollaborationApp",
                "Coil ImageLoader configured with 50MB disk cache"
        )
    }
}
