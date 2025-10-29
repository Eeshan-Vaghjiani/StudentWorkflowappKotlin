package com.example.loginandregistration.models

/**
 * Sealed class representing different UI states for ViewModels. This provides a consistent way to
 * handle loading, success, and error states across the app.
 */
sealed class UiState<out T> {
    /** Initial state before any operation */
    object Idle : UiState<Nothing>()

    /** Loading state during async operations */
    object Loading : UiState<Nothing>()

    /** Success state with data */
    data class Success<T>(val data: T) : UiState<T>()

    /** Error state with error message and optional exception */
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()

    /** Check if current state is loading */
    val isLoading: Boolean
        get() = this is Loading

    /** Check if current state is success */
    val isSuccess: Boolean
        get() = this is Success

    /** Check if current state is error */
    val isError: Boolean
        get() = this is Error

    /** Get data if state is Success, null otherwise */
    fun getDataOrNull(): T? =
            when (this) {
                is Success -> data
                else -> null
            }

    /** Get error message if state is Error, null otherwise */
    fun getErrorOrNull(): String? =
            when (this) {
                is Error -> message
                else -> null
            }
}

/** Extension function to map UiState data */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Idle -> UiState.Idle
        is UiState.Loading -> UiState.Loading
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> UiState.Error(message, exception)
    }
}

/** Extension function to handle UiState in a functional way */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) {
        action(data)
    }
    return this
}

inline fun <T> UiState<T>.onError(action: (String, Throwable?) -> Unit): UiState<T> {
    if (this is UiState.Error) {
        action(message, exception)
    }
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) {
        action()
    }
    return this
}
