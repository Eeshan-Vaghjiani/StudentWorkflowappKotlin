package com.example.loginandregistration.repository

/**
 * A generic wrapper for repository results that can represent loading, success, and error states.
 * Used for Flow emissions to provide better error handling and UI state management.
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Throwable, val message: String) : RepositoryResult<Nothing>()
    object Loading : RepositoryResult<Nothing>()
}
