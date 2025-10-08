package com.example.loginandregistration.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.loginandregistration.utils.ImageCompressor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Repository for handling file uploads and downloads with Firebase Storage.
 * Supports images, documents, and profile pictures with progress callbacks.
 */
class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val context: Context
) {
    companion object {
        private const val TAG = "StorageRepository"
        private const val PROFILE_IMAGES_PATH = "profile_images"
        private const val CHAT_IMAGES_PATH = "chat_images"
        private const val CHAT_DOCUMENTS_PATH = "chat_documents"
        private const val MAX_IMAGE_SIZE = 5 * 1024 * 1024L // 5MB
        private const val MAX_DOCUMENT_SIZE = 10 * 1024 * 1024L // 10MB
    }

    private fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    /**
     * Uploads an image with compression to Firebase Storage.
     * Images are compressed to max 1920x1080 at 80% quality.
     * 
     * @param uri URI of the image to upload
     * @param path Storage path (e.g., "chat_images/chatId")
     * @param onProgress Callback for upload progress (0-100)
     * @return Result with download URL on success
     */
    suspend fun uploadImage(
        uri: Uri,
        path: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Starting image upload to path: $path")

            // Compress image before upload
            val compressedFile = ImageCompressor.compressImage(context, uri)
            val fileSize = compressedFile.length()
            
            Log.d(TAG, "Image compressed to ${fileSize / 1024}KB")

            // Check file size
            if (fileSize > MAX_IMAGE_SIZE) {
                compressedFile.delete()
                return Result.failure(
                    Exception("Image size (${fileSize / 1024}KB) exceeds maximum allowed size (${MAX_IMAGE_SIZE / 1024}KB)")
                )
            }

            // Create storage reference
            val fileName = "${System.currentTimeMillis()}_${getCurrentUserId()}.jpg"
            val storageRef = storage.reference.child(path).child(fileName)

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploadedBy", getCurrentUserId())
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .build()

            // Upload file with progress tracking
            val uploadTask = storageRef.putFile(Uri.fromFile(compressedFile), metadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
                Log.d(TAG, "Upload progress: $progress%")
            }

            // Wait for upload to complete
            uploadTask.await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Clean up compressed file
            compressedFile.delete()

            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a document to Firebase Storage.
     * 
     * @param uri URI of the document to upload
     * @param path Storage path (e.g., "chat_documents/chatId")
     * @param onProgress Callback for upload progress (0-100)
     * @return Result with download URL on success
     */
    suspend fun uploadDocument(
        uri: Uri,
        path: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Starting document upload to path: $path")

            // Get file info
            val fileInfo = getFileInfo(uri)
            val fileName = fileInfo.first
            val fileSize = fileInfo.second
            val mimeType = fileInfo.third

            Log.d(TAG, "Document: $fileName, Size: ${fileSize / 1024}KB, Type: $mimeType")

            // Check file size
            if (fileSize > MAX_DOCUMENT_SIZE) {
                return Result.failure(
                    Exception("Document size (${fileSize / 1024}KB) exceeds maximum allowed size (${MAX_DOCUMENT_SIZE / 1024}KB)")
                )
            }

            // Create storage reference with timestamp to avoid conflicts
            val uniqueFileName = "${System.currentTimeMillis()}_${getCurrentUserId()}_$fileName"
            val storageRef = storage.reference.child(path).child(uniqueFileName)

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .setCustomMetadata("uploadedBy", getCurrentUserId())
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .setCustomMetadata("originalFileName", fileName)
                .setCustomMetadata("fileSize", fileSize.toString())
                .build()

            // Upload file with progress tracking
            val uploadTask = storageRef.putFile(uri, metadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
                Log.d(TAG, "Upload progress: $progress%")
            }

            // Wait for upload to complete
            uploadTask.await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Log.d(TAG, "Document uploaded successfully: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading document", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads a profile picture with compression and cropping.
     * Profile pictures are compressed to under 500KB.
     * 
     * @param uri URI of the image to upload
     * @param userId User ID (defaults to current user)
     * @param onProgress Callback for upload progress (0-100)
     * @return Result with download URL on success
     */
    suspend fun uploadProfilePicture(
        uri: Uri,
        userId: String = getCurrentUserId(),
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            if (getCurrentUserId().isEmpty()) {
                return Result.failure(Exception("User not authenticated"))
            }

            Log.d(TAG, "Starting profile picture upload for user: $userId")

            // Compress image with smaller dimensions for profile pictures
            val compressedFile = ImageCompressor.compressImage(
                context = context,
                uri = uri,
                maxWidth = 800,
                maxHeight = 800,
                quality = 85
            )
            
            val fileSize = compressedFile.length()
            Log.d(TAG, "Profile picture compressed to ${fileSize / 1024}KB")

            // Check file size (should be under 500KB for profile pictures)
            if (fileSize > 500 * 1024) {
                compressedFile.delete()
                return Result.failure(
                    Exception("Profile picture size (${fileSize / 1024}KB) exceeds maximum allowed size (500KB)")
                )
            }

            // Create storage reference
            val path = "$PROFILE_IMAGES_PATH/$userId"
            val fileName = "${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child(path).child(fileName)

            // Create metadata
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploadedBy", getCurrentUserId())
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .build()

            // Upload file with progress tracking
            val uploadTask = storageRef.putFile(Uri.fromFile(compressedFile), metadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
                Log.d(TAG, "Upload progress: $progress%")
            }

            // Wait for upload to complete
            uploadTask.await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Clean up compressed file
            compressedFile.delete()

            Log.d(TAG, "Profile picture uploaded successfully: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile picture", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads a file from Firebase Storage.
     * 
     * @param url Download URL from Firebase Storage
     * @param destinationFile File where the download will be saved
     * @param onProgress Callback for download progress (0-100)
     * @return Result with the downloaded file on success
     */
    suspend fun downloadFile(
        url: String,
        destinationFile: File,
        onProgress: (Int) -> Unit = {}
    ): Result<File> {
        return try {
            Log.d(TAG, "Starting file download from: $url")

            // Get storage reference from URL
            val storageRef = storage.getReferenceFromUrl(url)

            // Create parent directory if it doesn't exist
            destinationFile.parentFile?.mkdirs()

            // Download file with progress tracking
            val downloadTask = storageRef.getFile(destinationFile)

            downloadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
                Log.d(TAG, "Download progress: $progress%")
            }

            // Wait for download to complete
            downloadTask.await()

            Log.d(TAG, "File downloaded successfully to: ${destinationFile.absolutePath}")
            Result.success(destinationFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a file from Firebase Storage.
     * 
     * @param url Download URL of the file to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteFile(url: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting file: $url")

            // Get storage reference from URL
            val storageRef = storage.getReferenceFromUrl(url)

            // Delete file
            storageRef.delete().await()

            Log.d(TAG, "File deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            Result.failure(e)
        }
    }

    /**
     * Gets metadata for a file in Firebase Storage.
     * 
     * @param url Download URL of the file
     * @return Result with StorageMetadata on success
     */
    suspend fun getFileMetadata(url: String): Result<StorageMetadata> {
        return try {
            Log.d(TAG, "Getting metadata for: $url")

            // Get storage reference from URL
            val storageRef = storage.getReferenceFromUrl(url)

            // Get metadata
            val metadata = storageRef.metadata.await()

            Log.d(TAG, "Metadata retrieved: ${metadata.name}, ${metadata.sizeBytes} bytes")
            Result.success(metadata)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file metadata", e)
            Result.failure(e)
        }
    }

    /**
     * Gets file information from URI.
     * Returns (fileName, fileSize, mimeType)
     */
    private fun getFileInfo(uri: Uri): Triple<String, Long, String> {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        
        var fileName = "document"
        var fileSize = 0L
        
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex) ?: "document"
                }
                if (sizeIndex != -1) {
                    fileSize = it.getLong(sizeIndex)
                }
            }
        }
        
        // Get MIME type
        val mimeType = context.contentResolver.getType(uri) 
            ?: getMimeTypeFromExtension(fileName)
            ?: "application/octet-stream"
        
        return Triple(fileName, fileSize, mimeType)
    }

    /**
     * Gets MIME type from file extension.
     */
    private fun getMimeTypeFromExtension(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "")
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }

    /**
     * Helper method to get storage reference for chat images.
     */
    fun getChatImagePath(chatId: String): String {
        return "$CHAT_IMAGES_PATH/$chatId"
    }

    /**
     * Helper method to get storage reference for chat documents.
     */
    fun getChatDocumentPath(chatId: String): String {
        return "$CHAT_DOCUMENTS_PATH/$chatId"
    }

    /**
     * Helper method to get storage reference for profile images.
     */
    fun getProfileImagePath(userId: String): String {
        return "$PROFILE_IMAGES_PATH/$userId"
    }
}
