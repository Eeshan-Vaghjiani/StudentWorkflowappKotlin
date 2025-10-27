package com.example.loginandregistration

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.Coil
import com.example.loginandregistration.utils.ImageLoaderConfig
import com.example.loginandregistration.utils.MemoryManager

/**
 * Activity for monitoring memory usage and testing memory management. This is useful for debugging
 * and performance testing.
 *
 * Features:
 * - Real-time memory statistics
 * - Manual cache clearing
 * - Image cache statistics
 * - Auto-refresh every 2 seconds
 */
class MemoryMonitorActivity : AppCompatActivity() {

    private lateinit var memoryStatsTextView: TextView
    private lateinit var cacheStatsTextView: TextView
    private lateinit var clearCacheButton: Button
    private lateinit var refreshButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var isAutoRefreshing = false

    private val refreshRunnable =
            object : Runnable {
                override fun run() {
                    if (isAutoRefreshing) {
                        updateStats()
                        handler.postDelayed(this, 2000) // Refresh every 2 seconds
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_monitor)

        // Initialize views
        memoryStatsTextView = findViewById(R.id.memoryStatsTextView)
        cacheStatsTextView = findViewById(R.id.cacheStatsTextView)
        clearCacheButton = findViewById(R.id.clearCacheButton)
        refreshButton = findViewById(R.id.refreshButton)

        // Set up toolbar
        supportActionBar?.apply {
            title = "Memory Monitor"
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up button listeners
        clearCacheButton.setOnClickListener { clearAllCaches() }

        refreshButton.setOnClickListener { updateStats() }

        // Initial stats update
        updateStats()

        // Start auto-refresh
        isAutoRefreshing = true
        handler.post(refreshRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        isAutoRefreshing = false
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun updateStats() {
        // Get memory statistics
        val memoryStats = MemoryManager.getMemoryStats(this)

        val memoryText = buildString {
            appendLine("=== MEMORY STATISTICS ===\n")
            appendLine("Total Memory: ${formatBytes(memoryStats.totalMemory)}")
            appendLine("Available Memory: ${formatBytes(memoryStats.availableMemory)}")
            appendLine("Used Memory: ${formatBytes(memoryStats.usedMemory)}")
            appendLine("Max Memory: ${formatBytes(memoryStats.maxMemory)}")
            appendLine("Percent Available: ${(memoryStats.percentAvailable * 100).toInt()}%")
            appendLine("Low Memory: ${if (memoryStats.isLowMemory) "YES ⚠️" else "NO ✓"}")
            appendLine("Threshold: ${formatBytes(memoryStats.threshold)}")
            appendLine()

            // Memory status
            val status =
                    when {
                        MemoryManager.isCriticalMemory(this@MemoryMonitorActivity) -> "CRITICAL 🔴"
                        MemoryManager.isLowMemory(this@MemoryMonitorActivity) -> "LOW 🟡"
                        else -> "GOOD 🟢"
                    }
            appendLine("Memory Status: $status")

            // Recommended dimensions
            val (width, height) =
                    MemoryManager.getRecommendedImageDimensions(this@MemoryMonitorActivity)
            appendLine("Recommended Image Size: ${width}x${height}")
        }

        memoryStatsTextView.text = memoryText

        // Get cache statistics
        val imageLoader = Coil.imageLoader(this)
        val cacheStats = ImageLoaderConfig.getCacheStats(imageLoader)

        val cacheText = buildString {
            appendLine("=== CACHE STATISTICS ===\n")
            appendLine("Memory Cache:")
            appendLine("  Size: ${formatBytes(cacheStats["memoryCacheSize"] as Long)}")
            appendLine("  Max: ${formatBytes(cacheStats["memoryCacheMaxSize"] as Long)}")
            val memoryPercent =
                    if ((cacheStats["memoryCacheMaxSize"] as Long) > 0) {
                        ((cacheStats["memoryCacheSize"] as Long).toFloat() /
                                        (cacheStats["memoryCacheMaxSize"] as Long).toFloat() * 100)
                                .toInt()
                    } else 0
            appendLine("  Usage: $memoryPercent%")
            appendLine()
            appendLine("Disk Cache:")
            appendLine("  Size: ${formatBytes(cacheStats["diskCacheSize"] as Long)}")
            appendLine("  Max: ${formatBytes(cacheStats["diskCacheMaxSize"] as Long)}")
            val diskPercent =
                    if ((cacheStats["diskCacheMaxSize"] as Long) > 0) {
                        ((cacheStats["diskCacheSize"] as Long).toFloat() /
                                        (cacheStats["diskCacheMaxSize"] as Long).toFloat() * 100)
                                .toInt()
                    } else 0
            appendLine("  Usage: $diskPercent%")
        }

        cacheStatsTextView.text = cacheText
    }

    private fun clearAllCaches() {
        val imageLoader = Coil.imageLoader(this)
        ImageLoaderConfig.clearCache(imageLoader)

        // Update stats after clearing
        handler.postDelayed({ updateStats() }, 500)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
