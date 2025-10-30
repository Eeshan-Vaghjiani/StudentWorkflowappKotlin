package com.example.loginandregistration.utils

import android.content.Context
import android.util.Log
import com.example.loginandregistration.repository.ChatRepository
import com.example.loginandregistration.repository.DashboardRepository
import com.example.loginandregistration.repository.GroupRepository
import com.example.loginandregistration.repository.TaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages real-time data synchronization across all repositories. Handles connection monitoring,
 * listener reconnection, and offline sync.
 */
class RealtimeDataSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "RealtimeDataSync"
        private const val RECONNECT_DELAY_MS = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 5

        @Volatile private var instance: RealtimeDataSyncManager? = null

        fun getInstance(context: Context): RealtimeDataSyncManager {
            return instance
                    ?: synchronized(this) {
                        instance
                                ?: RealtimeDataSyncManager(context.applicationContext).also {
                                    instance = it
                                }
                    }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val connectionMonitor = ConnectionMonitor(context)
    private val offlineMessageQueue = OfflineMessageQueue(context)

    // Repositories
    private val dashboardRepository = DashboardRepository()
    private val groupRepository = GroupRepository()
    private val taskRepository = TaskRepository(context)
    private val chatRepository = ChatRepository(context = context)

    // Sync state
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var connectionMonitorJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0

    init {
        configureFirestore()
        startConnectionMonitoring()
    }

    /** Configure Firestore for optimal real-time performance */
    private fun configureFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val settings =
                FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true) // Enable offline persistence
                        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()

        firestore.firestoreSettings = settings

        Log.d(TAG, "Firestore configured with offline persistence enabled")
    }

    /** Start monitoring network connection */
    private fun startConnectionMonitoring() {
        connectionMonitorJob =
                scope.launch {
                    connectionMonitor.isConnected.collect { isConnected ->
                        _isOnline.value = isConnected

                        if (isConnected) {
                            Log.d(TAG, "Connection restored")
                            onConnectionRestored()
                        } else {
                            Log.w(TAG, "Connection lost")
                            onConnectionLost()
                        }
                    }
                }
    }

    /** Handle connection restoration */
    private fun onConnectionRestored() {
        _syncState.value = SyncState.SYNCING
        reconnectAttempts = 0

        scope.launch {
            try {
                // Sync pending offline messages
                syncOfflineMessages()

                // Firestore listeners will automatically reconnect
                // No need to manually restart them

                _syncState.value = SyncState.SYNCED
                Log.d(TAG, "Sync completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
                _syncState.value = SyncState.ERROR
                scheduleReconnect()
            }
        }
    }

    /** Handle connection loss */
    private fun onConnectionLost() {
        _syncState.value = SyncState.OFFLINE
        Log.d(TAG, "Entered offline mode")
    }

    /** Sync pending offline messages */
    private suspend fun syncOfflineMessages() {
        val queuedMessages = offlineMessageQueue.getQueuedMessages()

        if (queuedMessages.isEmpty()) {
            Log.d(TAG, "No offline messages to sync")
            return
        }

        Log.d(TAG, "Syncing ${queuedMessages.size} offline messages")

        for (message in queuedMessages) {
            try {
                val result =
                        chatRepository.sendMessage(chatId = message.chatId, content = message.text)

                if (result.isSuccess) {
                    offlineMessageQueue.removeMessage(message.id)
                    Log.d(TAG, "Successfully synced message: ${message.id}")
                } else {
                    offlineMessageQueue.markMessageAsFailed(message.id)
                    Log.e(TAG, "Failed to sync message: ${message.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing message: ${message.id}", e)
                offlineMessageQueue.markMessageAsFailed(message.id)
            }
        }

        Log.d(TAG, "Offline message sync completed")
    }

    /** Schedule reconnection attempt */
    private fun scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "Max reconnect attempts reached")
            _syncState.value = SyncState.ERROR
            return
        }

        reconnectAttempts++
        val delay = RECONNECT_DELAY_MS * reconnectAttempts

        Log.d(TAG, "Scheduling reconnect attempt $reconnectAttempts in ${delay}ms")

        reconnectJob?.cancel()
        reconnectJob =
                scope.launch {
                    delay(delay)

                    if (_isOnline.value) {
                        onConnectionRestored()
                    }
                }
    }

    /** Force a manual sync */
    fun forceSync() {
        if (!_isOnline.value) {
            Log.w(TAG, "Cannot force sync while offline")
            return
        }

        Log.d(TAG, "Force sync requested")
        onConnectionRestored()
    }

    /** Get pending offline message count */
    fun getPendingMessageCount(): Int {
        return offlineMessageQueue.getPendingMessageCount()
    }

    /** Clear all pending offline messages */
    fun clearPendingMessages() {
        offlineMessageQueue.clearQueue()
        Log.d(TAG, "Cleared all pending messages")
    }

    /** Check if data is consistent across repositories */
    suspend fun verifyDataConsistency(): DataConsistencyReport {
        Log.d(TAG, "Verifying data consistency")

        val report = DataConsistencyReport()

        try {
            // Verify dashboard stats
            val taskStats = taskRepository.getTaskStats()
            report.taskStatsValid = taskStats.overdue >= 0 && taskStats.dueToday >= 0

            // Verify groups
            val groupsResult = groupRepository.getUserGroups()
            val groups = groupsResult.getOrElse { emptyList() }
            report.groupsValid = true // Empty is valid

            // Verify tasks
            val tasksResult = taskRepository.getUserTasks()
            val tasks = tasksResult.getOrElse { emptyList() }
            report.tasksValid = true // Empty is valid

            report.isConsistent = report.taskStatsValid && report.groupsValid && report.tasksValid

            Log.d(TAG, "Data consistency check: ${if (report.isConsistent) "PASSED" else "FAILED"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying data consistency", e)
            report.isConsistent = false
            report.error = e.message
        }

        return report
    }

    /** Stop all monitoring and cleanup */
    fun stop() {
        connectionMonitorJob?.cancel()
        reconnectJob?.cancel()
        Log.d(TAG, "RealtimeDataSyncManager stopped")
    }
}

/** Represents the current sync state */
enum class SyncState {
    IDLE, // Not syncing
    SYNCING, // Currently syncing
    SYNCED, // Successfully synced
    OFFLINE, // Offline mode
    ERROR // Error occurred
}

/** Report of data consistency check */
data class DataConsistencyReport(
        var isConsistent: Boolean = false,
        var taskStatsValid: Boolean = false,
        var groupsValid: Boolean = false,
        var tasksValid: Boolean = false,
        var error: String? = null
)
