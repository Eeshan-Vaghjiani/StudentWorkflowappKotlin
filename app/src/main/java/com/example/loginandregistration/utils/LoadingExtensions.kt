package com.example.loginandregistration.utils

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.loginandregistration.models.UiState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Extension functions for handling loading states in Fragments and Activities */

/** Show or hide a progress bar based on loading state */
fun ProgressBar.setLoading(isLoading: Boolean) {
    visibility = if (isLoading) View.VISIBLE else View.GONE
}

/** Show or hide a view based on loading state (inverse of progress bar) */
fun View.setVisibleWhenNotLoading(isLoading: Boolean) {
    visibility = if (isLoading) View.GONE else View.VISIBLE
}

/** Observe UiState from LiveData and handle loading, success, and error states */
fun <T> Fragment.observeUiState(
        liveData: LiveData<UiState<T>>,
        progressBar: ProgressBar? = null,
        contentView: View? = null,
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null
) {
    liveData.observe(viewLifecycleOwner) { state ->
        when (state) {
            is UiState.Idle -> {
                progressBar?.visibility = View.GONE
                contentView?.visibility = View.VISIBLE
            }
            is UiState.Loading -> {
                progressBar?.visibility = View.VISIBLE
                contentView?.visibility = View.GONE
            }
            is UiState.Success -> {
                progressBar?.visibility = View.GONE
                contentView?.visibility = View.VISIBLE
                onSuccess(state.data)
            }
            is UiState.Error -> {
                progressBar?.visibility = View.GONE
                contentView?.visibility = View.VISIBLE
                val errorMessage = state.message
                if (onError != null) {
                    onError(errorMessage)
                } else {
                    // Default error handling with Snackbar
                    view?.let {
                        Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG)
                                .setAction("Dismiss") {}
                                .show()
                    }
                }
            }
        }
    }
}

/** Observe UiState from StateFlow and handle loading, success, and error states */
fun <T> Fragment.observeUiStateFlow(
        stateFlow: StateFlow<UiState<T>>,
        progressBar: ProgressBar? = null,
        contentView: View? = null,
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null
) {
    viewLifecycleOwner.lifecycleScope.launch {
        stateFlow.collect { state ->
            when (state) {
                is UiState.Idle -> {
                    progressBar?.visibility = View.GONE
                    contentView?.visibility = View.VISIBLE
                }
                is UiState.Loading -> {
                    progressBar?.visibility = View.VISIBLE
                    contentView?.visibility = View.GONE
                }
                is UiState.Success -> {
                    progressBar?.visibility = View.GONE
                    contentView?.visibility = View.VISIBLE
                    onSuccess(state.data)
                }
                is UiState.Error -> {
                    progressBar?.visibility = View.GONE
                    contentView?.visibility = View.VISIBLE
                    val errorMessage = state.message
                    if (onError != null) {
                        onError(errorMessage)
                    } else {
                        // Default error handling with Snackbar
                        view?.let {
                            Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG)
                                    .setAction("Dismiss") {}
                                    .show()
                        }
                    }
                }
            }
        }
    }
}

/** Show a loading indicator during a suspend operation */
suspend fun <T> Fragment.withLoading(progressBar: ProgressBar, block: suspend () -> T): T {
    progressBar.visibility = View.VISIBLE
    try {
        return block()
    } finally {
        progressBar.visibility = View.GONE
    }
}

/** Execute a block and show error in Snackbar if it fails */
inline fun Fragment.executeWithErrorHandling(crossinline block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        view?.let {
            Snackbar.make(it, e.message ?: "An error occurred", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss") {}
                    .show()
        }
    }
}

/** Show error message in Snackbar */
fun Fragment.showError(message: String) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).setAction("Dismiss") {}.show() }
}

/** Show success message in Snackbar */
fun Fragment.showSuccess(message: String) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
}
