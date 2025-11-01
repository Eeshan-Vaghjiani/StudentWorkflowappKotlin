package com.example.loginandregistration.utils

import android.content.Context
import android.util.Log
import com.example.loginandregistration.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Manages automatic retry of failed messages when network connectivity is restored. Observes
 * network state and triggers retry of FAILED_RETRYABLE messages.
 */
class MessageRetryManager(
        private val context: Context,
        private val chatRepository: ChatRepository,
        private val scope: CoroutineScope
) {
    private val networkObserver = NetworkConnectivityObserver(context)
    private var observerJob: Job? = null
    private var lastRetryTime = 0L

    companion object {
        private const val TAG = "MessageRetryManager"
        private const val MIN_RETRY_INTERVAL_MS = 30000L // 30 seconds minimum between retries
    }

    /** Start observing network connectivity and automatically retry failed messages */
    fun startObserving() {
        if (observerJob?.isActive == true) {
            Log.d(TAG, "Already observing network connectivity")
            return
        }

        Log.d(TAG, "Starting network connectivity observation")

        observerJob =
                scope.launch(Dispatchers.IO) {
                    networkObserver.observe().collectLatest { isConnected ->
                        if (isConnected) {
                            Log.d(TAG, "Network connected - checking for messages to retry")
                            handleNetworkRestored()
                        } else {
                            Log.d(TAG, "Network disconnected")
                        }
                    }
                }
    }

    /** Stop observing network connectivity */
    fun stopObserving() {
        Log.d(TAG, "Stopping network connectivity observation")
        observerJob?.cancel()
        observerJob = null
    }

    /** Handle network restoration - retry failed messages */
    private suspend fun handleNetworkRestored() {
        try {
            // Throttle retries to avoid overwhelming the server
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastRetryTime < MIN_RETRY_INTERVAL_MS) {
                Log.d(TAG, "Skipping retry - too soon since last attempt")
                return
            }

            lastRetryTime = currentTime

            // Small delay to ensure network is stable
            delay(2000)

            Log.d(TAG, "Attempting to retry failed messages")
            val result = chatRepository.retryFailedMessages()

            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                if (count > 0) {
                    Log.d(TAG, "Successfully retried $count messages")
                } else {
                    Log.d(TAG, "No messages needed retry")
                }
            } else {
                Log.w(TAG, "Failed to retry messages", result.exceptionOrNull())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling network restoration", e)
        }
    }

    /** Manually trigger retry of failed messages */
    suspend fun manualRetry(): Result<Int> {
        return try {
            Log.d(TAG, "Manual retry triggered")
            chatRepository.retryFailedMessages()
        } catch (e: Exception) {
            Log.e(TAG, "Error in manual retry", e)
            Result.failure(e)
        }
    }

    /** Check if network is currently available */
    fun isNetworkAvailable(): Boolean {
        return networkObserver.isNetworkAvailable()
    }
}
