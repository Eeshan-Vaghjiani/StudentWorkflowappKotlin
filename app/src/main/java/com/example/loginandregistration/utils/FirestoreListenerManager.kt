package com.example.loginandregistration.utils

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages Firestore listeners with automatic reconnection on failure. Ensures listeners are
 * properly cleaned up and can recover from connection issues.
 */
class FirestoreListenerManager {

    companion object {
        private const val TAG = "FirestoreListenerMgr"
        private const val RECONNECT_DELAY_MS = 2000L
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val activeListeners = ConcurrentHashMap<String, ManagedListener>()

    /** Register a listener with automatic reconnection */
    fun registerListener(
            listenerId: String,
            listenerFactory: () -> ListenerRegistration,
            onError: ((Exception) -> Unit)? = null
    ) {
        // Remove existing listener if present
        removeListener(listenerId)

        val managedListener =
                ManagedListener(id = listenerId, factory = listenerFactory, onError = onError)

        activeListeners[listenerId] = managedListener
        managedListener.start()

        Log.d(TAG, "Registered listener: $listenerId")
    }

    /** Remove a specific listener */
    fun removeListener(listenerId: String) {
        activeListeners.remove(listenerId)?.let { listener ->
            listener.stop()
            Log.d(TAG, "Removed listener: $listenerId")
        }
    }

    /** Remove all listeners */
    fun removeAllListeners() {
        Log.d(TAG, "Removing all ${activeListeners.size} listeners")
        activeListeners.values.forEach { it.stop() }
        activeListeners.clear()
    }

    /** Reconnect all listeners (useful after connection restoration) */
    fun reconnectAll() {
        Log.d(TAG, "Reconnecting all ${activeListeners.size} listeners")
        activeListeners.values.forEach { it.reconnect() }
    }

    /** Get count of active listeners */
    fun getActiveListenerCount(): Int = activeListeners.size

    /** Check if a listener is active */
    fun isListenerActive(listenerId: String): Boolean {
        return activeListeners[listenerId]?.isActive() == true
    }

    /** Managed listener with reconnection logic */
    private inner class ManagedListener(
            val id: String,
            val factory: () -> ListenerRegistration,
            val onError: ((Exception) -> Unit)?
    ) {
        private var registration: ListenerRegistration? = null
        private var reconnectAttempts = 0
        private var isStopped = false

        fun start() {
            if (isStopped) return

            try {
                registration = factory()
                reconnectAttempts = 0
                Log.d(TAG, "Started listener: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting listener: $id", e)
                onError?.invoke(e)
                scheduleReconnect()
            }
        }

        fun stop() {
            isStopped = true
            registration?.remove()
            registration = null
            Log.d(TAG, "Stopped listener: $id")
        }

        fun reconnect() {
            if (isStopped) return

            Log.d(TAG, "Reconnecting listener: $id")
            stop()
            isStopped = false
            start()
        }

        fun isActive(): Boolean {
            return registration != null && !isStopped
        }

        private fun scheduleReconnect() {
            if (isStopped || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    Log.e(TAG, "Max reconnect attempts reached for listener: $id")
                }
                return
            }

            reconnectAttempts++
            val delay = RECONNECT_DELAY_MS * reconnectAttempts

            Log.d(
                    TAG,
                    "Scheduling reconnect for listener: $id (attempt $reconnectAttempts in ${delay}ms)"
            )

            scope.launch {
                delay(delay)
                if (!isStopped) {
                    start()
                }
            }
        }
    }
}

/** Extension function to create a managed listener */
fun FirestoreListenerManager.managedListener(
        listenerId: String,
        listenerFactory: () -> ListenerRegistration
) {
    registerListener(listenerId, listenerFactory)
}
