package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.loginandregistration.databinding.ActivityImageViewerBinding
import com.example.loginandregistration.repository.StorageRepository
import com.example.loginandregistration.utils.AnimationUtils
import com.google.android.material.snackbar.Snackbar
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

/**
 * Full-screen image viewer with pinch-to-zoom and swipe-to-dismiss gestures. Supports downloading
 * images to the device.
 */
class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var storageRepository: StorageRepository

    // Gesture detectors
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetectorCompat

    // Zoom and pan state
    private var scaleFactor = 1.0f
    private var focusX = 0f
    private var focusY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    // Swipe to dismiss state
    private var initialY = 0f
    private var isDragging = false

    private var imageUrl: String? = null
    private var isDownloading = false

    companion object {
        const val EXTRA_IMAGE_URL = "image_url"
        private const val MIN_SCALE = 1.0f
        private const val MAX_SCALE = 5.0f
        private const val SWIPE_THRESHOLD = 200f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        storageRepository = StorageRepository(context = applicationContext)

        // Get image URL from intent
        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        if (imageUrl == null) {
            Toast.makeText(this, "Error: No image URL provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup UI
        setupImageView()
        setupGestureDetectors()
        setupClickListeners()

        // Load image
        loadImage()
    }

    private fun setupImageView() {
        // Make activity full screen
        window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun setupGestureDetectors() {
        // Scale gesture detector for pinch-to-zoom
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        // Gesture detector for swipe-to-dismiss
        gestureDetector = GestureDetectorCompat(this, GestureListener())
    }

    private fun setupClickListeners() {
        // Close button
        binding.btnClose.setOnClickListener {
            AnimationUtils.buttonPress(it)
            finish()
        }

        // Download button
        binding.btnDownload.setOnClickListener {
            AnimationUtils.buttonPress(it)
            downloadImage()
        }
    }

    private fun loadImage() {
        binding.progressBar.visibility = View.VISIBLE

        // Add fade-in animation for the image
        binding.imageView.alpha = 0f

        binding.imageView.load(imageUrl) {
            crossfade(true)
            listener(
                    onSuccess = { _, _ ->
                        binding.progressBar.visibility = View.GONE
                        AnimationUtils.fadeIn(binding.imageView, duration = 300)
                    },
                    onError = { _, _ ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                                        this@ImageViewerActivity,
                                        "Failed to load image",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
            )
        }
    }

    private fun downloadImage() {
        if (isDownloading) return

        val url = imageUrl ?: return

        isDownloading = true
        binding.downloadProgress.visibility = View.VISIBLE
        binding.btnDownload.isEnabled = false

        lifecycleScope.launch {
            try {
                // Create destination file in Downloads folder
                val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                        )
                val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(downloadsDir, fileName)

                // Download file
                val result =
                        storageRepository.downloadFile(
                                url = url,
                                destinationFile = destinationFile,
                                onProgress = { progress ->
                                    runOnUiThread {
                                        binding.downloadProgressBar.progress = progress
                                    }
                                }
                        )

                result.fold(
                        onSuccess = { file ->
                            // Notify media scanner
                            sendBroadcast(
                                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                                        data = android.net.Uri.fromFile(file)
                                    }
                            )

                            Snackbar.make(
                                            binding.root,
                                            "Image saved to Downloads",
                                            Snackbar.LENGTH_LONG
                                    )
                                    .show()
                        },
                        onFailure = { error ->
                            Snackbar.make(
                                            binding.root,
                                            "Download failed: ${error.message}",
                                            Snackbar.LENGTH_LONG
                                    )
                                    .show()
                        }
                )
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Download failed: ${e.message}", Snackbar.LENGTH_LONG)
                        .show()
            } finally {
                isDownloading = false
                binding.downloadProgress.visibility = View.GONE
                binding.btnDownload.isEnabled = true
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Handle scale gestures
        scaleGestureDetector.onTouchEvent(event)

        // Handle other gestures
        gestureDetector.onTouchEvent(event)

        // Handle pan when zoomed
        if (scaleFactor > 1.0f) {
            handlePan(event)
        } else {
            handleSwipeToDismiss(event)
        }

        return true
    }

    private fun handlePan(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                posX += dx
                posY += dy

                // Constrain panning to image bounds
                constrainPan()

                lastTouchX = event.x
                lastTouchY = event.y

                updateImageTransform()
            }
        }
    }

    private fun handleSwipeToDismiss(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialY = event.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - initialY

                if (Math.abs(deltaY) > 10) {
                    isDragging = true
                }

                if (isDragging) {
                    // Move image with finger
                    binding.imageView.translationY = deltaY

                    // Fade background based on distance
                    val alpha = 1.0f - (Math.abs(deltaY) / SWIPE_THRESHOLD).coerceIn(0f, 1f)
                    binding.root.alpha = alpha
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val deltaY = event.y - initialY

                if (isDragging && Math.abs(deltaY) > SWIPE_THRESHOLD) {
                    // Dismiss activity
                    finish()
                } else {
                    // Reset position
                    binding.imageView.animate().translationY(0f).setDuration(200).start()

                    binding.root.animate().alpha(1.0f).setDuration(200).start()
                }

                isDragging = false
            }
        }
    }

    private fun constrainPan() {
        val imageView = binding.imageView
        val maxX = (imageView.width * (scaleFactor - 1)) / 2
        val maxY = (imageView.height * (scaleFactor - 1)) / 2

        posX = posX.coerceIn(-maxX, maxX)
        posY = posY.coerceIn(-maxY, maxY)
    }

    private fun updateImageTransform() {
        binding.imageView.apply {
            scaleX = scaleFactor
            scaleY = scaleFactor
            translationX = posX
            translationY = posY
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(MIN_SCALE, min(scaleFactor, MAX_SCALE))

            // Reset pan when zooming out to minimum
            if (scaleFactor == MIN_SCALE) {
                posX = 0f
                posY = 0f
            }

            updateImageTransform()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Toggle between zoomed and normal
            if (scaleFactor > MIN_SCALE) {
                // Zoom out
                scaleFactor = MIN_SCALE
                posX = 0f
                posY = 0f
            } else {
                // Zoom in to 2x
                scaleFactor = 2.0f
            }

            updateImageTransform()
            return true
        }
    }

    override fun finish() {
        super.finish()
        // Smooth exit animation
        overridePendingTransition(0, android.R.anim.fade_out)
    }
}
