package com.example.loginandregistration.utils

import android.util.Log

/**
 * Utility class for monitoring performance of operations and detecting frame skipping. Logs
 * warnings when operations exceed the 16ms frame budget.
 *
 * Requirements: 3.3, 3.6
 */
object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private const val FRAME_BUDGET_MS = 16 // 60 FPS = 16ms per frame
    private const val WARNING_THRESHOLD_MS = FRAME_BUDGET_MS

    /**
     * Measures the duration of an operation and logs a warning if it exceeds the frame budget.
     *
     * @param operationName Name of the operation being measured
     * @param block The operation to measure
     * @return The result of the operation
     */
    fun <T> measureOperation(operationName: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime

        if (duration > WARNING_THRESHOLD_MS) {
            Log.w(
                    TAG,
                    "SLOW OPERATION: '$operationName' took ${duration}ms (exceeds ${WARNING_THRESHOLD_MS}ms frame budget)"
            )
        } else {
            Log.d(TAG, "Operation '$operationName' completed in ${duration}ms")
        }

        return result
    }

    /**
     * Measures the duration of a suspend operation and logs a warning if it exceeds the frame
     * budget.
     *
     * @param operationName Name of the operation being measured
     * @param block The suspend operation to measure
     * @return The result of the operation
     */
    suspend fun <T> measureSuspendOperation(operationName: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime

        if (duration > WARNING_THRESHOLD_MS) {
            Log.w(
                    TAG,
                    "SLOW SUSPEND OPERATION: '$operationName' took ${duration}ms (exceeds ${WARNING_THRESHOLD_MS}ms frame budget)"
            )
        } else {
            Log.d(TAG, "Suspend operation '$operationName' completed in ${duration}ms")
        }

        return result
    }

    /**
     * Logs frame skipping detected by Choreographer.
     *
     * @param framesSkipped Number of frames skipped
     * @param activityOrFragment Name of the current activity or fragment
     * @param operation Optional description of the operation causing the skip
     */
    fun logFrameSkip(framesSkipped: Int, activityOrFragment: String, operation: String? = null) {
        Log.w(
                TAG,
                "FRAME SKIP: Skipped $framesSkipped frames in $activityOrFragment (${framesSkipped * FRAME_BUDGET_MS}ms delay)"
        )

        // Log to ErrorLogger for comprehensive tracking
        ErrorLogger.logFrameSkip(framesSkipped, activityOrFragment, operation)
    }

    /**
     * Logs a UI operation that might cause frame skipping.
     *
     * @param operation Description of the UI operation
     * @param durationMs Duration of the operation in milliseconds
     */
    fun logUIOperation(operation: String, durationMs: Long) {
        if (durationMs > WARNING_THRESHOLD_MS) {
            Log.w(
                    TAG,
                    "UI OPERATION WARNING: '$operation' took ${durationMs}ms on main thread (exceeds ${WARNING_THRESHOLD_MS}ms frame budget)"
            )
        }
    }

    /**
     * Logs RecyclerView adapter operations that might cause frame skipping.
     *
     * @param adapterName Name of the adapter
     * @param operation Type of operation (e.g., "onBindViewHolder", "onCreateViewHolder")
     * @param durationMs Duration of the operation in milliseconds
     */
    fun logAdapterOperation(adapterName: String, operation: String, durationMs: Long) {
        if (durationMs > WARNING_THRESHOLD_MS) {
            Log.w(
                    TAG,
                    "ADAPTER WARNING: $adapterName.$operation took ${durationMs}ms (exceeds ${WARNING_THRESHOLD_MS}ms frame budget)"
            )
        }
    }

    /**
     * Logs database operations that might block the main thread.
     *
     * @param operation Description of the database operation
     * @param durationMs Duration of the operation in milliseconds
     * @param isMainThread Whether the operation ran on the main thread
     */
    fun logDatabaseOperation(operation: String, durationMs: Long, isMainThread: Boolean) {
        if (isMainThread && durationMs > WARNING_THRESHOLD_MS) {
            Log.e(
                    TAG,
                    "DATABASE ERROR: '$operation' took ${durationMs}ms on MAIN THREAD (should use background thread)"
            )
        } else if (durationMs > 100) {
            Log.w(TAG, "SLOW DATABASE: '$operation' took ${durationMs}ms")
        }
    }

    /**
     * Logs network operations that might block the main thread.
     *
     * @param operation Description of the network operation
     * @param durationMs Duration of the operation in milliseconds
     * @param isMainThread Whether the operation ran on the main thread
     */
    fun logNetworkOperation(operation: String, durationMs: Long, isMainThread: Boolean) {
        if (isMainThread) {
            Log.e(
                    TAG,
                    "NETWORK ERROR: '$operation' took ${durationMs}ms on MAIN THREAD (should use background thread)"
            )
        } else if (durationMs > 1000) {
            Log.w(TAG, "SLOW NETWORK: '$operation' took ${durationMs}ms")
        }
    }

    /**
     * Checks if the current thread is the main thread.
     *
     * @return true if running on main thread, false otherwise
     */
    fun isMainThread(): Boolean {
        return android.os.Looper.myLooper() == android.os.Looper.getMainLooper()
    }

    /**
     * Logs a warning if the current operation is running on the main thread when it shouldn't.
     *
     * @param operation Description of the operation
     */
    fun warnIfMainThread(operation: String) {
        if (isMainThread()) {
            Log.w(
                    TAG,
                    "MAIN THREAD WARNING: '$operation' is running on main thread (should use background thread)"
            )
        }
    }

    /** Logs memory usage statistics. */
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val availableMemory = maxMemory - usedMemory

        Log.d(
                TAG,
                "MEMORY: Used ${usedMemory}MB / Max ${maxMemory}MB (${availableMemory}MB available)"
        )

        if (availableMemory < 50) {
            Log.w(TAG, "LOW MEMORY WARNING: Only ${availableMemory}MB available")
        }
    }
}
