package com.example.loginandregistration.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/** Configuration utility for Firebase Firestore. Handles offline persistence and cache settings. */
object FirestoreConfig {

    /**
     * Enables offline persistence for Firestore. This allows the app to cache data locally and work
     * offline.
     *
     * @param firestore The FirebaseFirestore instance to configure
     * @param cacheSizeBytes The cache size in bytes. Use CACHE_SIZE_UNLIMITED for unlimited cache.
     * ```
     *                       Default is unlimited to ensure all data is cached.
     * ```
     */
    @Suppress("DEPRECATION")
    fun enableOfflinePersistence(
            firestore: FirebaseFirestore,
            cacheSizeBytes: Long = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
    ) {
        try {
            val settings =
                    FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .setCacheSizeBytes(cacheSizeBytes)
                            .build()

            firestore.firestoreSettings = settings
            android.util.Log.d("FirestoreConfig", "Offline persistence enabled successfully")
        } catch (e: Exception) {
            android.util.Log.e("FirestoreConfig", "Failed to enable offline persistence", e)
        }
    }

    /**
     * Configures Firestore with custom settings.
     *
     * @param firestore The FirebaseFirestore instance to configure
     * @param persistenceEnabled Whether to enable offline persistence
     * @param cacheSizeBytes The cache size in bytes
     */
    @Suppress("DEPRECATION")
    fun configureFirestore(
            firestore: FirebaseFirestore,
            persistenceEnabled: Boolean = true,
            cacheSizeBytes: Long = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
    ) {
        try {
            val settings =
                    FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(persistenceEnabled)
                            .setCacheSizeBytes(cacheSizeBytes)
                            .build()

            firestore.firestoreSettings = settings
            android.util.Log.d("FirestoreConfig", "Firestore configured successfully")
        } catch (e: Exception) {
            android.util.Log.e("FirestoreConfig", "Failed to configure Firestore", e)
        }
    }
}
