package com.example.loginandregistration

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.loginandregistration.utils.ProductionMetricsMonitor
import com.google.android.material.card.MaterialCardView

/**
 * Dashboard activity for monitoring production metrics Shows Firestore query success rates,
 * permission errors, and crash analytics
 */
class MonitoringDashboardActivity : AppCompatActivity() {

    private lateinit var sessionMetricsCard: MaterialCardView
    private lateinit var persistentMetricsCard: MaterialCardView
    private lateinit var healthStatusCard: MaterialCardView

    private lateinit var sessionTotalQueriesText: TextView
    private lateinit var sessionSuccessfulQueriesText: TextView
    private lateinit var sessionFailedQueriesText: TextView
    private lateinit var sessionPermissionErrorsText: TextView
    private lateinit var sessionSuccessRateText: TextView
    private lateinit var sessionDurationText: TextView

    private lateinit var persistentTotalQueriesText: TextView
    private lateinit var persistentSuccessfulQueriesText: TextView
    private lateinit var persistentFailedQueriesText: TextView
    private lateinit var persistentPermissionErrorsText: TextView
    private lateinit var persistentSuccessRateText: TextView
    private lateinit var persistentLastResetText: TextView

    private lateinit var healthStatusText: TextView
    private lateinit var healthStatusIcon: TextView

    private lateinit var refreshButton: Button
    private lateinit var resetButton: Button
    private lateinit var exportButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring_dashboard)

        // Setup toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Production Metrics"
        }

        initializeViews()
        setupClickListeners()
        refreshMetrics()
    }

    private fun initializeViews() {
        // Cards
        sessionMetricsCard = findViewById(R.id.sessionMetricsCard)
        persistentMetricsCard = findViewById(R.id.persistentMetricsCard)
        healthStatusCard = findViewById(R.id.healthStatusCard)

        // Session metrics
        sessionTotalQueriesText = findViewById(R.id.sessionTotalQueries)
        sessionSuccessfulQueriesText = findViewById(R.id.sessionSuccessfulQueries)
        sessionFailedQueriesText = findViewById(R.id.sessionFailedQueries)
        sessionPermissionErrorsText = findViewById(R.id.sessionPermissionErrors)
        sessionSuccessRateText = findViewById(R.id.sessionSuccessRate)
        sessionDurationText = findViewById(R.id.sessionDuration)

        // Persistent metrics
        persistentTotalQueriesText = findViewById(R.id.persistentTotalQueries)
        persistentSuccessfulQueriesText = findViewById(R.id.persistentSuccessfulQueries)
        persistentFailedQueriesText = findViewById(R.id.persistentFailedQueries)
        persistentPermissionErrorsText = findViewById(R.id.persistentPermissionErrors)
        persistentSuccessRateText = findViewById(R.id.persistentSuccessRate)
        persistentLastResetText = findViewById(R.id.persistentLastReset)

        // Health status
        healthStatusText = findViewById(R.id.healthStatusText)
        healthStatusIcon = findViewById(R.id.healthStatusIcon)

        // Buttons
        refreshButton = findViewById(R.id.refreshButton)
        resetButton = findViewById(R.id.resetButton)
        exportButton = findViewById(R.id.exportButton)
    }

    private fun setupClickListeners() {
        refreshButton.setOnClickListener { refreshMetrics() }

        resetButton.setOnClickListener { showResetConfirmationDialog() }

        exportButton.setOnClickListener { exportMetrics() }
    }

    private fun refreshMetrics() {
        // Get session metrics
        val sessionMetrics = ProductionMetricsMonitor.getSessionMetrics()
        sessionTotalQueriesText.text = sessionMetrics.totalQueries.toString()
        sessionSuccessfulQueriesText.text = sessionMetrics.successfulQueries.toString()
        sessionFailedQueriesText.text = sessionMetrics.failedQueries.toString()
        sessionPermissionErrorsText.text = sessionMetrics.permissionErrors.toString()
        sessionSuccessRateText.text = "%.2f%%".format(sessionMetrics.successRate)
        sessionDurationText.text = formatDuration(sessionMetrics.sessionDurationMs)

        // Get persistent metrics
        val persistentMetrics = ProductionMetricsMonitor.getPersistentMetrics()
        persistentTotalQueriesText.text = persistentMetrics.totalQueries.toString()
        persistentSuccessfulQueriesText.text = persistentMetrics.successfulQueries.toString()
        persistentFailedQueriesText.text = persistentMetrics.failedQueries.toString()
        persistentPermissionErrorsText.text = persistentMetrics.permissionErrors.toString()
        persistentSuccessRateText.text = "%.2f%%".format(persistentMetrics.successRate)
        persistentLastResetText.text =
                if (persistentMetrics.lastResetTimestamp > 0) {
                    java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(persistentMetrics.lastResetTimestamp))
                } else {
                    "Never"
                }

        // Update health status
        val healthStatus = ProductionMetricsMonitor.getHealthStatus()
        healthStatusText.text = healthStatus

        // Update health status icon and color
        when {
            healthStatus.contains("HEALTHY") -> {
                healthStatusIcon.text = "✅"
                healthStatusCard.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
            }
            healthStatus.contains("DEGRADED") -> {
                healthStatusIcon.text = "⚠️"
                healthStatusCard.setCardBackgroundColor(getColor(android.R.color.holo_orange_light))
            }
            healthStatus.contains("UNHEALTHY") -> {
                healthStatusIcon.text = "❌"
                healthStatusCard.setCardBackgroundColor(getColor(android.R.color.holo_red_light))
            }
            else -> {
                healthStatusIcon.text = "ℹ️"
                healthStatusCard.setCardBackgroundColor(getColor(android.R.color.darker_gray))
            }
        }

        // Log metrics to console and Crashlytics
        ProductionMetricsMonitor.logMetrics()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
                .setTitle("Reset Metrics")
                .setMessage(
                        "Are you sure you want to reset all metrics? This action cannot be undone."
                )
                .setPositiveButton("Reset") { _, _ ->
                    ProductionMetricsMonitor.resetMetrics()
                    refreshMetrics()
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun exportMetrics() {
        val sessionMetrics = ProductionMetricsMonitor.getSessionMetrics()
        val persistentMetrics = ProductionMetricsMonitor.getPersistentMetrics()

        val report = buildString {
            appendLine("=== Production Metrics Export ===")
            appendLine()
            appendLine("Export Date: ${java.util.Date()}")
            appendLine()
            appendLine("SESSION METRICS:")
            appendLine("  Total Queries: ${sessionMetrics.totalQueries}")
            appendLine("  Successful: ${sessionMetrics.successfulQueries}")
            appendLine("  Failed: ${sessionMetrics.failedQueries}")
            appendLine("  Permission Errors: ${sessionMetrics.permissionErrors}")
            appendLine("  Success Rate: ${"%.2f".format(sessionMetrics.successRate)}%")
            appendLine("  Session Duration: ${formatDuration(sessionMetrics.sessionDurationMs)}")
            appendLine()
            appendLine("PERSISTENT METRICS:")
            appendLine("  Total Queries: ${persistentMetrics.totalQueries}")
            appendLine("  Successful: ${persistentMetrics.successfulQueries}")
            appendLine("  Failed: ${persistentMetrics.failedQueries}")
            appendLine("  Permission Errors: ${persistentMetrics.permissionErrors}")
            appendLine("  Success Rate: ${"%.2f".format(persistentMetrics.successRate)}%")
            appendLine()
            appendLine("HEALTH STATUS:")
            appendLine("  ${ProductionMetricsMonitor.getHealthStatus()}")
            appendLine("================================")
        }

        // Show export dialog
        AlertDialog.Builder(this)
                .setTitle("Metrics Report")
                .setMessage(report)
                .setPositiveButton("OK", null)
                .setNeutralButton("Copy") { _, _ ->
                    val clipboard =
                            getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Metrics Report", report)
                    clipboard.setPrimaryClip(clip)
                    android.widget.Toast.makeText(
                                    this,
                                    "Report copied to clipboard",
                                    android.widget.Toast.LENGTH_SHORT
                            )
                            .show()
                }
                .show()
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
