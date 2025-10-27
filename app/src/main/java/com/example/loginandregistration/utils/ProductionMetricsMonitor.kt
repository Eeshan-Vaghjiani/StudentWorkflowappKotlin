package com.example.loginandregistration.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Production metrics monitoring utility for tracking Firestore permission errors, query success
 * rates, and app crash analytics.
 *
 * This monitor tracks:
 * - Permission denied errors
 * - Query success/failure rates
 * - Error patterns and trends
 * - App crash metrics related to Firestore
 */
object ProductionMetricsMonitor {
    private const val TAG = "ProductionMetrics"
    private const val PREFS_NAME = "production_metrics"
    private const val KEY_PERMISSION_ERRORS = "permission_errors_count"
    private const val KEY_TOTAL_QUERIES = "total_queries_count"
    private const val KEY_SUCCESSFUL_QUERIES = "successful_queries_count"
    private const val KEY_FAILED_QUERIES = "failed_queries_count"
    private const val KEY_LAST_RESET = "last_reset_timestamp"
    private const val KEY_SESSION_START = "session_start_timestamp"

    // In-memory counters for current session
    private val sessionPermissionErrors = AtomicInteger(0)
    private val sessionTotalQueries = AtomicInteger(0)
    private val sessionSuccessfulQueries = AtomicInteger(0)
    private val sessionFailedQueries = AtomicInteger(0)
    private val sessionStartTime = AtomicLong(System.currentTimeMillis())

    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var prefs: SharedPreferences

    /** Initialize the metrics monitor Call this from Application.onCreate() */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sessionStartTime.set(System.currentTimeMillis())

        // Log session start
        Log.i(TAG, "Production metrics monitoring initialized")
        crashlytics.log("Production metrics monitoring started")

        // Set custom keys for Crashlytics
        try {
            crashlytics.setCustomKey("monitoring_enabled", true)
            crashlytics.setCustomKey("session_start", Date().toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set Crashlytics custom keys", e)
        }
    }

    /** Record a Firestore query attempt */
    fun recordQueryAttempt(operation: String) {
        sessionTotalQueries.incrementAndGet()
        incrementPersistentCounter(KEY_TOTAL_QUERIES)

        Log.d(TAG, "Query attempt recorded: $operation")
    }

    /** Record a successful Firestore query */
    fun recordQuerySuccess(operation: String) {
        sessionSuccessfulQueries.incrementAndGet()
        incrementPersistentCounter(KEY_SUCCESSFUL_QUERIES)

        Log.d(TAG, "Query success recorded: $operation")
    }

    /** Record a failed Firestore query */
    fun recordQueryFailure(operation: String, exception: Throwable) {
        sessionFailedQueries.incrementAndGet()
        incrementPersistentCounter(KEY_FAILED_QUERIES)

        // Check if it's a permission error
        if (exception is FirebaseFirestoreException &&
                        exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        ) {
            recordPermissionError(operation, exception)
        }

        Log.e(TAG, "Query failure recorded: $operation", exception)

        // Log to Crashlytics
        crashlytics.log("Query failed: $operation - ${exception.message}")
        crashlytics.recordException(exception)
    }

    /** Record a permission denied error */
    fun recordPermissionError(operation: String, exception: FirebaseFirestoreException) {
        sessionPermissionErrors.incrementAndGet()
        incrementPersistentCounter(KEY_PERMISSION_ERRORS)

        Log.e(TAG, "PERMISSION_DENIED error in $operation: ${exception.message}", exception)

        // Log detailed information to Crashlytics
        crashlytics.apply {
            setCustomKey("last_permission_error", operation)
            setCustomKey("permission_error_count", getPermissionErrorCount())
            setCustomKey("permission_error_time", Date().toString())
            log("PERMISSION_DENIED: $operation - ${exception.message}")
            recordException(exception)
        }

        // Alert if permission errors are increasing
        if (sessionPermissionErrors.get() > 5) {
            Log.w(
                    TAG,
                    "WARNING: High number of permission errors detected in this session: ${sessionPermissionErrors.get()}"
            )
            crashlytics.log(
                    "WARNING: High permission error count: ${sessionPermissionErrors.get()}"
            )
        }
    }

    /** Get current session metrics */
    fun getSessionMetrics(): SessionMetrics {
        val totalQueries = sessionTotalQueries.get()
        val successfulQueries = sessionSuccessfulQueries.get()
        val failedQueries = sessionFailedQueries.get()
        val permissionErrors = sessionPermissionErrors.get()

        val successRate =
                if (totalQueries > 0) {
                    (successfulQueries.toDouble() / totalQueries.toDouble()) * 100
                } else {
                    0.0
                }

        val sessionDuration = System.currentTimeMillis() - sessionStartTime.get()

        return SessionMetrics(
                totalQueries = totalQueries,
                successfulQueries = successfulQueries,
                failedQueries = failedQueries,
                permissionErrors = permissionErrors,
                successRate = successRate,
                sessionDurationMs = sessionDuration
        )
    }

    /** Get persistent metrics (across all sessions) */
    fun getPersistentMetrics(): PersistentMetrics {
        val totalQueries = prefs.getLong(KEY_TOTAL_QUERIES, 0)
        val successfulQueries = prefs.getLong(KEY_SUCCESSFUL_QUERIES, 0)
        val failedQueries = prefs.getLong(KEY_FAILED_QUERIES, 0)
        val permissionErrors = prefs.getLong(KEY_PERMISSION_ERRORS, 0)
        val lastReset = prefs.getLong(KEY_LAST_RESET, 0)

        val successRate =
                if (totalQueries > 0) {
                    (successfulQueries.toDouble() / totalQueries.toDouble()) * 100
                } else {
                    0.0
                }

        return PersistentMetrics(
                totalQueries = totalQueries,
                successfulQueries = successfulQueries,
                failedQueries = failedQueries,
                permissionErrors = permissionErrors,
                successRate = successRate,
                lastResetTimestamp = lastReset
        )
    }

    /** Get permission error count */
    fun getPermissionErrorCount(): Long {
        return prefs.getLong(KEY_PERMISSION_ERRORS, 0)
    }

    /** Get query success rate */
    fun getQuerySuccessRate(): Double {
        val totalQueries = prefs.getLong(KEY_TOTAL_QUERIES, 0)
        val successfulQueries = prefs.getLong(KEY_SUCCESSFUL_QUERIES, 0)

        return if (totalQueries > 0) {
            (successfulQueries.toDouble() / totalQueries.toDouble()) * 100
        } else {
            0.0
        }
    }

    /** Log current metrics to console and Crashlytics */
    fun logMetrics() {
        val sessionMetrics = getSessionMetrics()
        val persistentMetrics = getPersistentMetrics()

        val report = buildString {
            appendLine("=== Production Metrics Report ===")
            appendLine()
            appendLine("SESSION METRICS:")
            appendLine("  Total Queries: ${sessionMetrics.totalQueries}")
            appendLine("  Successful: ${sessionMetrics.successfulQueries}")
            appendLine("  Failed: ${sessionMetrics.failedQueries}")
            appendLine("  Permission Errors: ${sessionMetrics.permissionErrors}")
            appendLine("  Success Rate: ${"%.2f".format(sessionMetrics.successRate)}%")
            appendLine("  Session Duration: ${sessionMetrics.sessionDurationMs / 1000}s")
            appendLine()
            appendLine("PERSISTENT METRICS (All Time):")
            appendLine("  Total Queries: ${persistentMetrics.totalQueries}")
            appendLine("  Successful: ${persistentMetrics.successfulQueries}")
            appendLine("  Failed: ${persistentMetrics.failedQueries}")
            appendLine("  Permission Errors: ${persistentMetrics.permissionErrors}")
            appendLine("  Success Rate: ${"%.2f".format(persistentMetrics.successRate)}%")
            appendLine(
                    "  Last Reset: ${if (persistentMetrics.lastResetTimestamp > 0) Date(persistentMetrics.lastResetTimestamp) else "Never"}"
            )
            appendLine("================================")
        }

        Log.i(TAG, report)
        crashlytics.log(report)

        // Set custom keys for dashboard
        crashlytics.apply {
            setCustomKey("session_total_queries", sessionMetrics.totalQueries)
            setCustomKey("session_success_rate", sessionMetrics.successRate)
            setCustomKey("session_permission_errors", sessionMetrics.permissionErrors)
            setCustomKey("total_permission_errors", persistentMetrics.permissionErrors)
            setCustomKey("overall_success_rate", persistentMetrics.successRate)
        }
    }

    /** Reset all metrics */
    fun resetMetrics() {
        prefs.edit().apply {
            putLong(KEY_PERMISSION_ERRORS, 0)
            putLong(KEY_TOTAL_QUERIES, 0)
            putLong(KEY_SUCCESSFUL_QUERIES, 0)
            putLong(KEY_FAILED_QUERIES, 0)
            putLong(KEY_LAST_RESET, System.currentTimeMillis())
            apply()
        }

        sessionPermissionErrors.set(0)
        sessionTotalQueries.set(0)
        sessionSuccessfulQueries.set(0)
        sessionFailedQueries.set(0)

        Log.i(TAG, "Metrics reset")
        crashlytics.log("Production metrics reset")
    }

    /** Check if metrics indicate healthy app state */
    fun isHealthy(): Boolean {
        val metrics = getSessionMetrics()

        // Healthy if:
        // 1. No permission errors
        // 2. Success rate > 95%
        // 3. At least some queries have been made
        return metrics.permissionErrors == 0 &&
                metrics.successRate > 95.0 &&
                metrics.totalQueries > 0
    }

    /** Get health status message */
    fun getHealthStatus(): String {
        val metrics = getSessionMetrics()

        return when {
            metrics.permissionErrors > 0 -> {
                "⚠️ UNHEALTHY: ${metrics.permissionErrors} permission errors detected"
            }
            metrics.successRate < 95.0 && metrics.totalQueries > 10 -> {
                "⚠️ DEGRADED: Success rate is ${"%.2f".format(metrics.successRate)}%"
            }
            metrics.totalQueries == 0 -> {
                "ℹ️ NO DATA: No queries recorded yet"
            }
            else -> {
                "✅ HEALTHY: Success rate ${"%.2f".format(metrics.successRate)}%"
            }
        }
    }

    // Private helper methods

    private fun incrementPersistentCounter(key: String) {
        monitorScope.launch {
            val current = prefs.getLong(key, 0)
            prefs.edit().putLong(key, current + 1).apply()
        }
    }

    /** Data classes for metrics */
    data class SessionMetrics(
            val totalQueries: Int,
            val successfulQueries: Int,
            val failedQueries: Int,
            val permissionErrors: Int,
            val successRate: Double,
            val sessionDurationMs: Long
    )

    data class PersistentMetrics(
            val totalQueries: Long,
            val successfulQueries: Long,
            val failedQueries: Long,
            val permissionErrors: Long,
            val successRate: Double,
            val lastResetTimestamp: Long
    )
}
