package com.example.loginandregistration

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.loginandregistration.databinding.FragmentProfileBinding
import com.example.loginandregistration.repository.StorageRepository
import com.example.loginandregistration.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Fragment for displaying and managing user profile.
 * Allows users to view their profile information and update their profile picture.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var storageRepository: StorageRepository
    private lateinit var userRepository: UserRepository
    private lateinit var firestore: FirebaseFirestore

    private var tempPhotoUri: Uri? = null
    private var currentPhotoUrl: String? = null

    companion object {
        private const val TAG = "ProfileFragment"
    }

    // Camera permission launcher
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required to take photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Gallery picker launcher
    private val pickImageFromGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadProfilePicture(it)
        }
    }

    // Camera launcher
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            uploadProfilePicture(tempPhotoUri!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        storageRepository = StorageRepository(context = requireContext())
        userRepository = UserRepository()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        updateUI(currentUser)
        loadUserProfile()

        binding.fabEditProfilePicture.setOnClickListener {
            showProfilePictureOptions()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Loads user profile data from Firestore.
     */
    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                user?.let {
                    currentPhotoUrl = it.photoUrl
                    displayProfilePicture(it.photoUrl)
                    binding.tvProfileName.text = it.displayName
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user profile", e)
            }
        }
    }

    /**
     * Displays profile picture using Coil.
     */
    private fun displayProfilePicture(photoUrl: String?) {
        if (photoUrl.isNullOrEmpty()) {
            // Show default avatar with user initials
            val displayName = auth.currentUser?.displayName ?: "User"
            val initials = getInitials(displayName)
            
            // For now, use a placeholder. In task 19, we'll implement DefaultAvatarGenerator
            binding.ivProfilePicture.load(R.drawable.ic_person) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
            }
        } else {
            binding.ivProfilePicture.load(photoUrl) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
            }
        }
    }

    /**
     * Gets initials from display name.
     */
    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "U"
        }
    }

    /**
     * Shows bottom sheet with profile picture options.
     */
    private fun showProfilePictureOptions() {
        val bottomSheet = ProfilePictureBottomSheet(
            onTakePhoto = { checkCameraPermissionAndLaunch() },
            onChooseGallery = { pickImageFromGallery.launch("image/*") }
        )
        bottomSheet.show(parentFragmentManager, "ProfilePictureBottomSheet")
    }

    /**
     * Checks camera permission and launches camera.
     */
    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Launches camera to take a photo.
     */
    private fun launchCamera() {
        try {
            val photoFile = File(
                requireContext().cacheDir,
                "profile_${System.currentTimeMillis()}.jpg"
            )
            
            tempPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            
            takePicture.launch(tempPhotoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching camera", e)
            Toast.makeText(
                requireContext(),
                "Error launching camera: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Uploads profile picture to Firebase Storage and updates user document.
     */
    private fun uploadProfilePicture(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                // Show progress
                binding.progressUpload.visibility = View.VISIBLE
                binding.tvUploadStatus.visibility = View.VISIBLE
                binding.fabEditProfilePicture.isEnabled = false

                // Upload to Storage
                val result = storageRepository.uploadProfilePicture(
                    uri = uri,
                    userId = userId,
                    onProgress = { progress ->
                        binding.progressUpload.progress = progress
                        binding.tvUploadStatus.text = "Uploading... $progress%"
                    }
                )

                result.onSuccess { downloadUrl ->
                    // Update Firestore user document
                    updateUserPhotoUrl(userId, downloadUrl)
                    
                    // Update UI
                    currentPhotoUrl = downloadUrl
                    displayProfilePicture(downloadUrl)
                    
                    // Hide progress
                    binding.progressUpload.visibility = View.GONE
                    binding.tvUploadStatus.visibility = View.GONE
                    binding.fabEditProfilePicture.isEnabled = true
                    
                    Toast.makeText(
                        requireContext(),
                        "Profile picture updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    Log.d(TAG, "Profile picture uploaded: $downloadUrl")
                }.onFailure { exception ->
                    // Hide progress
                    binding.progressUpload.visibility = View.GONE
                    binding.tvUploadStatus.visibility = View.GONE
                    binding.fabEditProfilePicture.isEnabled = true
                    
                    Toast.makeText(
                        requireContext(),
                        "Upload failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    Log.e(TAG, "Error uploading profile picture", exception)
                }
            } catch (e: Exception) {
                // Hide progress
                binding.progressUpload.visibility = View.GONE
                binding.tvUploadStatus.visibility = View.GONE
                binding.fabEditProfilePicture.isEnabled = true
                
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                
                Log.e(TAG, "Error in uploadProfilePicture", e)
            }
        }
    }

    /**
     * Updates user's photoUrl in Firestore.
     */
    private suspend fun updateUserPhotoUrl(userId: String, photoUrl: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .update("photoUrl", photoUrl)
                .await()
            
            Log.d(TAG, "User photoUrl updated in Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user photoUrl", e)
            throw e
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.tvProfileEmail.text = user.email ?: "N/A"
            binding.tvProfileUid.text = user.uid
            binding.tvProfileName.text = user.displayName ?: user.email?.substringBefore("@") ?: "User"
        } else {
            binding.tvProfileEmail.text = "Not logged in"
            binding.tvProfileUid.text = "N/A"
            binding.tvProfileName.text = "Guest"
            binding.btnLogout.isEnabled = false
            binding.fabEditProfilePicture.isEnabled = false
        }
    }

    private fun logoutUser() {
        auth.signOut()

        // Sign out from Google as well if the user signed in with Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            navigateToLoginScreen()
        }
    }

    private fun navigateToLoginScreen() {
        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}