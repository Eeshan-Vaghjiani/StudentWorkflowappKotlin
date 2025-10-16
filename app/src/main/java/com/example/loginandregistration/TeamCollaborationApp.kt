package com.example.loginandregistration

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import coil.Coil
import com.example.loginandregistration.utils.FirestoreConfig
import com.example.loginandregistration.utils.ImageLoaderConfig
import com.example.loginandregistration.utils.MemoryManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Custom Application class for the Team Collaboration app. Initializes Firebase and enables offline
 * persistence. Also handles memory management and cache clearing.
 */
class TeamCollaborationApp : Application() {

    private val TAG = "TeamCollaborationApp"

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")

            // Get Firestore instance
            val firestore = FirebaseFirestore.getInstance()

            // Enable offline persistence with unlimited cache
            FirestoreConfig.enableOfflinePersistence(firestore)

            Log.d(TAG, "Firestore offline persistence enabled")

            // Configure Coil ImageLoader with disk caching for offline support
            val imageLoader = ImageLoaderConfig.createImageLoader(this)
            Coil.setImageLoader(imageLoader)

            Log.d(TAG, "Coil ImageLoader configured with 50MB disk cache")

            // Log initial memory statistics
            MemoryManager.logMemoryStats(this, TAG)
        } catch (e: Exception) {
            Log.e(TAG, "Error during app initialization", e)
            // Don't crash the app, just log the error
            // The app can still function without offline persistence
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        // Handle memory trim events
        val imageLoader = Coil.imageLoader(this)
        MemoryManager.handleMemoryTrim(imageLoader, level)

        // Log memory trim event
        val levelName =
                when (level) {
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
                    ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
                    ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
                    ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
                    ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
                    else -> "UNKNOWN($level)"
                }

        Log.w(TAG, "onTrimMemory called with level: $levelName")
        MemoryManager.logMemoryStats(this, TAG)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        // Clear all caches when system is critically low on memory
        Log.e(TAG, "onLowMemory called - clearing all caches")
        val imageLoader = Coil.imageLoader(this)
        ImageLoaderConfig.clearCache(imageLoader)

        MemoryManager.logMemoryStats(this, TAG)
    }
}
