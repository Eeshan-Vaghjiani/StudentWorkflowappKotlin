package com.example.loginandregistration.utils

/**
 * Sealed class representing different states of data loading operations. Used to manage UI states
 * in repositories and ViewModels.
 *
 * @param T The type of data being loaded
 */
sealed class LoadingState<out T> {
    /** Represents the loading state when data is being fetched. */
    object Loading : LoadingState<Nothing>()

    /**
     * Represents a successful state with loaded data.
     *
     * @param data The successfully loaded data
     */
    data class Success<T>(val data: T) : LoadingState<T>()

    /**
     * Represents an error state when data loading fails.
     *
     * @param message User-friendly error message
     * @param throwable Optional throwable for debugging
     */
    data class Error(val message: String, val throwable: Throwable? = null) :
            LoadingState<Nothing>()

    /**
     * Represents an empty state when no data is available. Different from Error - this is a valid
     * state with no data.
     *
     * @param message Optional message to display in empty state
     */
    data class Empty(val message: String = "No data available") : LoadingState<Nothing>()

    /** Helper function to check if the state is loading. */
    fun isLoading(): Boolean = this is Loading

    /** Helper function to check if the state is successful. */
    fun isSuccess(): Boolean = this is Success

    /** Helper function to check if the state is an error. */
    fun isError(): Boolean = this is Error

    /** Helper function to check if the state is empty. */
    fun isEmpty(): Boolean = this is Empty

    /** Helper function to get data if state is Success, null otherwise. */
    fun getDataOrNull(): T? =
            when (this) {
                is Success -> data
                else -> null
            }

    /** Helper function to get error message if state is Error, null otherwise. */
    fun getErrorOrNull(): String? =
            when (this) {
                is Error -> message
                else -> null
            }
}

/** Extension function to map LoadingState data to a different type. */
fun <T, R> LoadingState<T>.map(transform: (T) -> R): LoadingState<R> {
    return when (this) {
        is LoadingState.Loading -> LoadingState.Loading
        is LoadingState.Success -> LoadingState.Success(transform(data))
        is LoadingState.Error -> LoadingState.Error(message, throwable)
        is LoadingState.Empty -> LoadingState.Empty(message)
    }
}

/** Extension function to handle each state with callbacks. */
inline fun <T> LoadingState<T>.handle(
        onLoading: () -> Unit = {},
        onSuccess: (T) -> Unit = {},
        onError: (String, Throwable?) -> Unit = { _, _ -> },
        onEmpty: (String) -> Unit = {}
) {
    when (this) {
        is LoadingState.Loading -> onLoading()
        is LoadingState.Success -> onSuccess(data)
        is LoadingState.Error -> onError(message, throwable)
        is LoadingState.Empty -> onEmpty(message)
    }
}
