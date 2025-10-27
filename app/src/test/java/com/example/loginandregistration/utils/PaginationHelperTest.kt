package com.example.loginandregistration.utils

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for PaginationHelper utility class. Tests pagination logic and state management. */
class PaginationHelperTest {

    @Test
    fun `loadNextPage loads first page successfully`() = runTest {
        val items = listOf("item1", "item2", "item3")
        val helper = PaginationHelper<String>(pageSize = 10) { items }

        val result = helper.loadNextPage()

        assertEquals(items, result)
        assertTrue(helper.hasMoreItems())
    }

    @Test
    fun `loadNextPage returns empty list when no more items`() = runTest {
        var callCount = 0
        val helper =
                PaginationHelper<String>(pageSize = 10) {
                    callCount++
                    if (callCount == 1) listOf("item1", "item2") else emptyList()
                }

        // First call returns items
        val firstPage = helper.loadNextPage()
        assertEquals(2, firstPage.size)

        // Second call returns empty
        val secondPage = helper.loadNextPage()
        assertTrue(secondPage.isEmpty())
        assertFalse(helper.hasMoreItems())
    }

    @Test
    fun `loadNextPage prevents duplicate loading`() = runTest {
        var loadCount = 0
        val helper =
                PaginationHelper<String>(pageSize = 10) {
                    loadCount++
                    // Simulate slow loading
                    kotlinx.coroutines.delay(100)
                    listOf("item$loadCount")
                }

        // Start first load
        val job1 = kotlinx.coroutines.async { helper.loadNextPage() }
        // Try to start second load immediately (should be prevented)
        kotlinx.coroutines.delay(10) // Small delay to ensure first load started
        val result2 = helper.loadNextPage()

        // Second call should return empty (prevented)
        assertTrue(result2.isEmpty())

        // First call should complete successfully
        val result1 = job1.await()
        assertEquals(1, result1.size)

        // Only one load should have occurred
        assertEquals(1, loadCount)
    }

    @Test
    fun `loadNextPage sets hasMore to false when items less than pageSize`() = runTest {
        val helper =
                PaginationHelper<String>(pageSize = 10) {
                    listOf("item1", "item2", "item3") // Only 3 items, less than pageSize of 10
                }

        val result = helper.loadNextPage()

        assertEquals(3, result.size)
        assertFalse(helper.hasMoreItems())
    }

    @Test
    fun `loadNextPage sets hasMore to true when items equal pageSize`() = runTest {
        val items = (1..10).map { "item$it" }
        val helper = PaginationHelper<String>(pageSize = 10) { items }

        val result = helper.loadNextPage()

        assertEquals(10, result.size)
        assertTrue(helper.hasMoreItems())
    }

    @Test
    fun `loadNextPage passes lastItem to loadMore function`() = runTest {
        var receivedLastItem: String? = null
        val helper =
                PaginationHelper<String>(pageSize = 5) { lastItem ->
                    receivedLastItem = lastItem
                    if (lastItem == null) {
                        listOf("item1", "item2", "item3", "item4", "item5")
                    } else {
                        listOf("item6", "item7")
                    }
                }

        // First page
        val firstPage = helper.loadNextPage()
        assertNull(receivedLastItem) // First call should have null lastItem
        assertEquals(5, firstPage.size)

        // Second page
        val secondPage = helper.loadNextPage()
        assertEquals("item5", receivedLastItem) // Should receive last item from first page
        assertEquals(2, secondPage.size)
    }

    @Test
    fun `reset clears pagination state`() = runTest {
        val helper = PaginationHelper<String>(pageSize = 10) { listOf("item1", "item2") }

        // Load first page
        helper.loadNextPage()
        assertFalse(helper.hasMoreItems())

        // Reset
        helper.reset()

        // Should be able to load again
        assertTrue(helper.hasMoreItems())
        assertFalse(helper.isCurrentlyLoading())
    }

    @Test
    fun `loadNextPage handles exceptions gracefully`() = runTest {
        val helper =
                PaginationHelper<String>(pageSize = 10) { throw RuntimeException("Test exception") }

        val result = helper.loadNextPage()

        assertTrue(result.isEmpty())
        assertFalse(helper.isCurrentlyLoading())
    }

    @Test
    fun `getPageSize returns correct page size`() {
        val helper = PaginationHelper<String>(pageSize = 25) { emptyList() }

        assertEquals(25, helper.getPageSize())
    }

    @Test
    fun `isCurrentlyLoading returns false initially`() {
        val helper = PaginationHelper<String>(pageSize = 10) { emptyList() }

        assertFalse(helper.isCurrentlyLoading())
    }

    @Test
    fun `hasMoreItems returns true initially`() {
        val helper = PaginationHelper<String>(pageSize = 10) { emptyList() }

        assertTrue(helper.hasMoreItems())
    }

    @Test
    fun `loadNextPage with empty result sets hasMore to false`() = runTest {
        val helper = PaginationHelper<String>(pageSize = 10) { emptyList() }

        val result = helper.loadNextPage()

        assertTrue(result.isEmpty())
        assertFalse(helper.hasMoreItems())
    }

    @Test
    fun `multiple pages can be loaded sequentially`() = runTest {
        var pageNumber = 0
        val helper =
                PaginationHelper<String>(pageSize = 5) {
                    pageNumber++
                    when (pageNumber) {
                        1 -> listOf("1-1", "1-2", "1-3", "1-4", "1-5")
                        2 -> listOf("2-1", "2-2", "2-3", "2-4", "2-5")
                        3 -> listOf("3-1", "3-2") // Less than pageSize
                        else -> emptyList()
                    }
                }

        // Load page 1
        val page1 = helper.loadNextPage()
        assertEquals(5, page1.size)
        assertTrue(helper.hasMoreItems())

        // Load page 2
        val page2 = helper.loadNextPage()
        assertEquals(5, page2.size)
        assertTrue(helper.hasMoreItems())

        // Load page 3 (partial)
        val page3 = helper.loadNextPage()
        assertEquals(2, page3.size)
        assertFalse(helper.hasMoreItems())

        // Try to load page 4 (should return empty)
        val page4 = helper.loadNextPage()
        assertTrue(page4.isEmpty())
    }

    @Test
    fun `reset allows reloading from beginning`() = runTest {
        var callCount = 0
        val helper =
                PaginationHelper<String>(pageSize = 10) {
                    callCount++
                    listOf("call$callCount")
                }

        // First load
        val result1 = helper.loadNextPage()
        assertEquals("call1", result1[0])

        // Reset and load again
        helper.reset()
        val result2 = helper.loadNextPage()
        assertEquals("call2", result2[0])

        assertEquals(2, callCount)
    }
}
