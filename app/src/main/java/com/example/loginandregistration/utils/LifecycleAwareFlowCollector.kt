package com.example.loginandregistration.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Extension function to collect a Flow in a lifecycle-aware manner. The collection will
 * automatically start when the lifecycle is at least STARTED and stop when it goes below STARTED
 * (e.g., onStop).
 *
 * This prevents memory leaks and unnecessary network/database operations when the UI is not
 * visible.
 *
 * @param lifecycleOwner The lifecycle owner (Fragment or Activity)
 * @param minActiveState The minimum lifecycle state to collect (default: STARTED)
 * @param collector The collector function to handle emitted values
 */
fun <T> Flow<T>.collectWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        collector: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) { collect { value -> collector(value) } }
    }
}

/**
 * Extension function to launch a coroutine that respects the lifecycle. The coroutine will
 * automatically start when the lifecycle is at least STARTED and cancel when it goes below STARTED.
 *
 * @param lifecycleOwner The lifecycle owner (Fragment or Activity)
 * @param minActiveState The minimum lifecycle state (default: STARTED)
 * @param block The coroutine block to execute
 */
fun CoroutineScope.launchWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend CoroutineScope.() -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) { block() }
    }
}
