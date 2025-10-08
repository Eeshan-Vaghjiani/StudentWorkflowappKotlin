package com.example.loginandregistration

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.loginandregistration.databinding.BottomSheetAttachmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AttachmentBottomSheet(
    private val onImageSelected: (Uri) -> Unit,
    private val onDocumentSelected: (Uri) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAttachmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var pickDocumentLauncher: ActivityResultLauncher<Array<String>>

    private var pendingCameraUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                showPermissionDeniedMessage("Camera permission is required to take photos")
            }
        }

        // Image permission launcher (for Android 13+)
        imagePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                showPermissionDeniedMessage("Storage permission is required to access photos")
            }
        }

        // Take picture launcher
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && pendingCameraUri != null) {
                onImageSelected(pendingCameraUri!!)
                dismiss()
            }
            pendingCameraUri = null
        }

        // Pick image launcher
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                onImageSelected(it)
                dismiss()
            }
        }

        // Pick document launcher
        pickDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                // Grant persistable URI permission
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onDocumentSelected(it)
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAttachmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraOption.setOnClickListener {
            handleCameraClick()
        }

        binding.galleryOption.setOnClickListener {
            handleGalleryClick()
        }

        binding.documentOption.setOnClickListener {
            handleDocumentClick()
        }
    }

    private fun handleCameraClick() {
        when {
            hasCameraPermission() -> openCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationale(
                    "Camera permission is needed to take photos",
                    Manifest.permission.CAMERA,
                    cameraPermissionLauncher
                )
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handleGalleryClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires READ_MEDIA_IMAGES permission
            when {
                hasImagePermission() -> openGallery()
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                    showPermissionRationale(
                        "Storage permission is needed to access photos",
                        Manifest.permission.READ_MEDIA_IMAGES,
                        imagePermissionLauncher
                    )
                }
                else -> imagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Android 12 and below
            when {
                hasLegacyStoragePermission() -> openGallery()
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionRationale(
                        "Storage permission is needed to access photos",
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        imagePermissionLauncher
                    )
                }
                else -> imagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun handleDocumentClick() {
        // Document picker doesn't require runtime permissions
        openDocumentPicker()
    }

    private fun openCamera() {
        try {
            // Create a temporary file for the photo
            val photoFile = createImageFile()
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Error opening camera: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun openDocumentPicker() {
        // Allow common document types
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "application/zip"
        )
        pickDocumentLauncher.launch(mimeTypes)
    }

    private fun createImageFile(): java.io.File {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
            .format(java.util.Date())
        val storageDir = requireContext().cacheDir
        return java.io.File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasImagePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasLegacyStoragePermission()
        }
    }

    private fun hasLegacyStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionRationale(
        message: String,
        permission: String,
        launcher: ActivityResultLauncher<String>
    ) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ ->
                launcher.launch(permission)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedMessage(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("$message. You can grant permission in app settings.")
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireContext().packageName}")
        )
        startActivity(intent)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
