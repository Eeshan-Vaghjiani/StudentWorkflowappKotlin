package com.example.loginandregistration.utils

import android.util.Log

/**
 * Generic pagination helper class for loading data in pages.
 *
 * @param T The type of items being paginated
 * @param pageSize Number of items to load per page (default: 50)
 * @param loadMore Suspend function that loads the next page of items given the last item
 */
class PaginationHelper<T>(
        private val pageSize: Int = 50,
        private val loadMore: suspend (lastItem: T?) -> List<T>
) {
    companion object {
        private const val TAG = "PaginationHelper"
    }

    private var lastItem: T? = null
    private var isLoading = false
    private var hasMore = true

    /**
     * Loads the next page of items.
     *
     * @return List of items loaded, or empty list if no more items or already loading
     */
    suspend fun loadNextPage(): List<T> {
        // Prevent duplicate loading
        if (isLoading) {
            Log.d(TAG, "loadNextPage: Already loading, skipping")
            return emptyList()
        }

        // Check if there are more items to load
        if (!hasMore) {
            Log.d(TAG, "loadNextPage: No more items to load")
            return emptyList()
        }

        isLoading = true
        Log.d(TAG, "loadNextPage: Loading next page with pageSize=$pageSize")

        return try {
            val items = loadMore(lastItem)

            Log.d(TAG, "loadNextPage: Loaded ${items.size} items")

            // Update state
            if (items.isEmpty()) {
                hasMore = false
                Log.d(TAG, "loadNextPage: No more items available")
            } else {
                lastItem = items.lastOrNull()

                // If we got fewer items than the page size, we've reached the end
                if (items.size < pageSize) {
                    hasMore = false
                    Log.d(TAG, "loadNextPage: Reached end of data (got ${items.size} < $pageSize)")
                }
            }

            items
        } catch (e: Exception) {
            Log.e(TAG, "loadNextPage: Error loading page", e)
            emptyList()
        } finally {
            isLoading = false
        }
    }

    /** Resets the pagination state to start from the beginning. */
    fun reset() {
        Log.d(TAG, "reset: Resetting pagination state")
        lastItem = null
        isLoading = false
        hasMore = true
    }

    /** Returns whether there are more items to load. */
    fun hasMoreItems(): Boolean = hasMore

    /** Returns whether a page is currently being loaded. */
    fun isCurrentlyLoading(): Boolean = isLoading

    /** Returns the current page size. */
    fun getPageSize(): Int = pageSize
}
