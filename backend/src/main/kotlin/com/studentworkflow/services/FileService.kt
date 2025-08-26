
package com.studentworkflow.services

import com.studentworkflow.models.FileUploadResponse
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.*

/**
 * FileService handles file upload, storage, retrieval, and deletion operations.
 * Supports both local storage and configurable cloud storage backends.
 */
class FileService {

    private val baseStoragePath = "./uploads"
    private val maxFileSize = 10 * 1024 * 1024 // 10MB max file size
    private val allowedExtensions = setOf("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt", "zip")

    init {
        File(baseStoragePath).mkdirs()
    }

    /**
     * Get all files in a specific directory
     */
    fun getFiles(directory: String = ""): List<Map<String, Any>> {
        val dir = File("$baseStoragePath/$directory")
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        return dir.walkTopDown()
            .filter { it.isFile }
            .map { file ->
                mapOf(
                    "name" to file.name,
                    "path" to file.toRelativeString(File(baseStoragePath)),
                    "size" to file.length(),
                    "lastModified" to file.lastModified(),
                    "extension" to file.extension
                )
            }
            .toList()
    }

    /**
     * Get all directories in a specific directory
     */
    fun getDirectories(directory: String = ""): List<String> {
        val dir = File("$baseStoragePath/$directory")
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.walkTopDown()
            .filter { it.isDirectory && it != dir }
            .map { it.toRelativeString(File(baseStoragePath)) }
            .toList()
    }

    /**
     * Store a file with validation and unique naming
     */
    fun storeFile(
        fileContent: ByteArray, 
        originalFileName: String, 
        path: String = "",
        userId: Int? = null
    ): FileUploadResponse {
        try {
            // Validate file size
            if (fileContent.size > maxFileSize) {
                return FileUploadResponse(
                    success = false,
                    message = "File size exceeds maximum allowed size of ${maxFileSize / 1024 / 1024}MB"
                )
            }

            // Validate file extension
            val extension = originalFileName.substringAfterLast('.', "").lowercase()
            if (extension.isNotEmpty() && !allowedExtensions.contains(extension)) {
                return FileUploadResponse(
                    success = false,
                    message = "File type not allowed. Allowed types: ${allowedExtensions.joinToString(", ")}"
                )
            }

            // Generate unique filename
            val timestamp = Instant.now().epochSecond
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val fileName = "${timestamp}_${uuid}_${originalFileName}"

            // Create directory structure
            val userPath = if (userId != null) "$path/user_$userId" else path
            val targetDirectory = File("$baseStoragePath/$userPath")
            targetDirectory.mkdirs()
            
            val targetPath = Paths.get(targetDirectory.absolutePath, fileName)
            Files.write(targetPath, fileContent)
            
            val relativePath = Paths.get(userPath, fileName).toString()
            
            return FileUploadResponse(
                success = true,
                message = "File uploaded successfully",
                filePath = relativePath
            )
        } catch (e: Exception) {
            return FileUploadResponse(
                success = false,
                message = "Failed to upload file: ${e.message}"
            )
        }
    }

    /**
     * Store a file from InputStream
     */
    fun storeFile(
        inputStream: InputStream,
        originalFileName: String,
        path: String = "",
        userId: Int? = null
    ): FileUploadResponse {
        return try {
            val fileContent = inputStream.readBytes()
            storeFile(fileContent, originalFileName, path, userId)
        } catch (e: Exception) {
            FileUploadResponse(
                success = false,
                message = "Failed to read file content: ${e.message}"
            )
        }
    }

    /**
     * Get file content as ByteArray
     */
    fun getFileContent(path: String): ByteArray? {
        return try {
            val filePath = Paths.get(baseStoragePath, path)
            if (Files.exists(filePath)) {
                Files.readAllBytes(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get file as File object
     */
    fun getFile(path: String): File? {
        val filePath = Paths.get(baseStoragePath, path)
        val file = filePath.toFile()
        return if (file.exists() && file.isFile) file else null
    }

    /**
     * Delete a file
     */
    fun deleteFile(path: String): Boolean {
        return try {
            val filePath = Paths.get(baseStoragePath, path)
            Files.deleteIfExists(filePath)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete multiple files
     */
    fun deleteFiles(paths: List<String>): Map<String, Boolean> {
        return paths.associateWith { deleteFile(it) }
    }

    /**
     * Check if file exists
     */
    fun exists(path: String): Boolean {
        val filePath = Paths.get(baseStoragePath, path)
        return Files.exists(filePath)
    }

    /**
     * Move a file to a new location
     */
    fun moveFile(sourcePath: String, destinationPath: String): Boolean {
        return try {
            val source = Paths.get(baseStoragePath, sourcePath)
            val destination = Paths.get(baseStoragePath, destinationPath)
            
            // Create destination directory if it doesn't exist
            Files.createDirectories(destination.parent)
            
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Copy a file to a new location
     */
    fun copyFile(sourcePath: String, destinationPath: String): Boolean {
        return try {
            val source = Paths.get(baseStoragePath, sourcePath)
            val destination = Paths.get(baseStoragePath, destinationPath)
            
            // Create destination directory if it doesn't exist
            Files.createDirectories(destination.parent)
            
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get file metadata
     */
    fun getFileMetadata(path: String): Map<String, Any>? {
        return try {
            val filePath = Paths.get(baseStoragePath, path)
            val file = filePath.toFile()
            
            if (file.exists() && file.isFile) {
                mapOf(
                    "name" to file.name,
                    "path" to path,
                    "size" to file.length(),
                    "lastModified" to file.lastModified(),
                    "extension" to file.extension,
                    "readable" to file.canRead(),
                    "writable" to file.canWrite()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Create a directory
     */
    fun createDirectory(path: String): Boolean {
        return try {
            val dirPath = Paths.get(baseStoragePath, path)
            Files.createDirectories(dirPath)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a directory and its contents
     */
    fun deleteDirectory(path: String): Boolean {
        return try {
            val dirPath = Paths.get(baseStoragePath, path)
            val dir = dirPath.toFile()
            dir.deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get total storage usage for a user
     */
    fun getUserStorageUsage(userId: Int): Long {
        return try {
            val userDir = File("$baseStoragePath/user_$userId")
            if (userDir.exists() && userDir.isDirectory) {
                userDir.walkTopDown()
                    .filter { it.isFile }
                    .sumOf { it.length() }
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Clean up old temporary files (older than specified days)
     */
    fun cleanupOldFiles(days: Int = 30, tempPath: String = "temp"): Int {
        return try {
            val tempDir = File("$baseStoragePath/$tempPath")
            if (!tempDir.exists()) return 0
            
            val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            var deletedCount = 0
            
            tempDir.walkTopDown()
                .filter { it.isFile && it.lastModified() < cutoffTime }
                .forEach { 
                    if (it.delete()) deletedCount++
                }
            
            deletedCount
        } catch (e: Exception) {
            0
        }
    }
}
