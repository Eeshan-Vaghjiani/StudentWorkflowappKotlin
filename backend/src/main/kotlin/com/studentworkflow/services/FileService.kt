
package com.studentworkflow.services

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class FileService {

    private val baseStoragePath = "./uploads"

    init {
        File(baseStoragePath).mkdirs()
    }

    fun getFiles(directory: String = ""): List<String> {
        val dir = File("$baseStoragePath/$directory")
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.walkTopDown()
            .filter { it.isFile }
            .map { it.toRelativeString(File(baseStoragePath)) }
            .toList()
    }

    fun getDirectories(directory: String = ""): List<String> {
        val dir = File("$baseStoragePath/$directory")
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.walkTopDown()
            .filter { it.isDirectory && it != dir }
            .map { it.toRelativeString(File(baseStoragePath)) }
            .toList()
    }

    fun storeFile(fileContent: ByteArray, fileName: String, path: String = ""): String {
        val targetDirectory = File("$baseStoragePath/$path")
        targetDirectory.mkdirs()
        val targetPath = Paths.get(targetDirectory.absolutePath, fileName)
        Files.write(targetPath, fileContent)
        return Paths.get(path, fileName).toString()
    }

    fun deleteFile(path: String): Boolean {
        val filePath = Paths.get(baseStoragePath, path)
        return Files.deleteIfExists(filePath)
    }

    fun exists(path: String): Boolean {
        val filePath = Paths.get(baseStoragePath, path)
        return Files.exists(filePath)
    }
}
