package com.example.loginandregistration.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.loginandregistration.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class for managing notification permissions on Android 13+ devices. Handles permission
 * requests, rationale dialogs, and FCM token management.
 */
class NotificationPermissionHelper(
        private val activity: Activity,
        private val onPermissionResult: (Boolean) -> Unit = {}
) {

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null
    private val notificationRepository = NotificationRepository()

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_PERMISSION_DENIED_COUNT = "permission_denied_count"
        private const val KEY_DONT_ASK_AGAIN = "dont_ask_again"

        /**
         * Checks if notification permission is granted. On Android 12 and below, always returns
         * true.
         */
        fun isNotificationPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // Notifications are enabled by default on Android 12 and below
                true
            }
        }

        /** Checks if we should show rationale for notification permission. */
        fun shouldShowRationale(activity: Activity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.shouldShowRequestPermissionRationale(
                        Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                false
            }
        }
    }

    /**
     * Initializes the permission launcher. Must be called before requesting permission. Should be
     * called in onCreate() or during fragment initialization.
     */
    fun initialize(launcher: ActivityResultLauncher<String>) {
        requestPermissionLauncher = launcher
    }

    /**
     * Creates and returns an ActivityResultLauncher for notification permission. This should be
     * registered in the activity/fragment.
     */
    fun createPermissionLauncher(): ActivityResultLauncher<String> {
        return (activity as androidx.activity.ComponentActivity).registerForActivityResult(
                ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean -> handlePermissionResult(isGranted) }
    }

    /** Requests notification permission with appropriate handling for different scenarios. */
    fun requestNotificationPermission() {
        // Only request on Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Permission not needed, get FCM token directly
            saveFcmToken()
            onPermissionResult(true)
            return
        }

        // Check if already granted
        if (isNotificationPermissionGranted(activity)) {
            saveFcmToken()
            onPermissionResult(true)
            return
        }

        // Check if user has permanently denied
        if (hasUserPermanentlyDenied()) {
            showSettingsDialog()
            return
        }

        // Check if we should show rationale
        if (shouldShowRationale(activity)) {
            showRationaleDialog()
        } else {
            // Request permission directly
            launchPermissionRequest()
        }
    }

    /** Launches the permission request. */
    private fun launchPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                    ?: throw IllegalStateException(
                            "Permission launcher not initialized. Call initialize() or createPermissionLauncher() first."
                    )
        }
    }

    /** Handles the result of the permission request. */
    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            // Permission granted, save FCM token
            saveFcmToken()
            resetDenialCount()
            onPermissionResult(true)
        } else {
            // Permission denied
            incrementDenialCount()

            // Check if user selected "Don't ask again"
            if (!shouldShowRationale(activity) && getDenialCount() > 1) {
                markAsPermanentlyDenied()
            }

            onPermissionResult(false)
        }
    }

    /** Shows a rationale dialog explaining why notification permission is needed. */
    private fun showRationaleDialog() {
        AlertDialog.Builder(activity)
                .setTitle("Enable Notifications")
                .setMessage(
                        "Stay connected with your team!\n\n" +
                                "Notifications help you:\n" +
                                "• Receive new messages instantly\n" +
                                "• Get task reminders before deadlines\n" +
                                "• Stay updated on group activities\n\n" +
                                "You can change this setting anytime in app settings."
                )
                .setPositiveButton("Allow") { dialog, _ ->
                    dialog.dismiss()
                    launchPermissionRequest()
                }
                .setNegativeButton("Not Now") { dialog, _ ->
                    dialog.dismiss()
                    incrementDenialCount()
                    onPermissionResult(false)
                }
                .setCancelable(false)
                .show()
    }

    /** Shows a dialog directing user to app settings when permission is permanently denied. */
    private fun showSettingsDialog() {
        AlertDialog.Builder(activity)
                .setTitle("Notifications Disabled")
                .setMessage(
                        "You've disabled notifications for this app.\n\n" +
                                "To receive important updates about messages, tasks, and group activities, " +
                                "please enable notifications in app settings.\n\n" +
                                "Would you like to open settings now?"
                )
                .setPositiveButton("Open Settings") { dialog, _ ->
                    dialog.dismiss()
                    openAppSettings()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    onPermissionResult(false)
                }
                .setCancelable(true)
                .show()
    }

    /** Opens the app's notification settings page. */
    private fun openAppSettings() {
        val intent =
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
        activity.startActivity(intent)
    }

    /** Saves the FCM token to Firestore after permission is granted. */
    private fun saveFcmToken() {
        CoroutineScope(Dispatchers.IO).launch { notificationRepository.saveFcmToken() }
    }

    // Preference management methods

    private fun getPrefs() = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getDenialCount(): Int {
        return getPrefs().getInt(KEY_PERMISSION_DENIED_COUNT, 0)
    }

    private fun incrementDenialCount() {
        val count = getDenialCount() + 1
        getPrefs().edit().putInt(KEY_PERMISSION_DENIED_COUNT, count).apply()
    }

    private fun resetDenialCount() {
        getPrefs().edit().putInt(KEY_PERMISSION_DENIED_COUNT, 0).apply()
        getPrefs().edit().putBoolean(KEY_DONT_ASK_AGAIN, false).apply()
    }

    private fun hasUserPermanentlyDenied(): Boolean {
        return getPrefs().getBoolean(KEY_DONT_ASK_AGAIN, false)
    }

    private fun markAsPermanentlyDenied() {
        getPrefs().edit().putBoolean(KEY_DONT_ASK_AGAIN, true).apply()
    }

    /**
     * Resets the permission state. Useful for testing or if user manually enables notifications in
     * system settings.
     */
    fun resetPermissionState() {
        resetDenialCount()
    }
}
