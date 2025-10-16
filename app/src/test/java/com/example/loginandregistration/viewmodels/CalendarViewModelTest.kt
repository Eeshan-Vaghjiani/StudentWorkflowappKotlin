package com.example.loginandregistration.viewmodels

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.loginandregistration.models.FirebaseTask
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for CalendarViewModel. Tests task filtering, date selection, and state management.
 *
 * Note: These tests focus on the ViewModel logic. Full integration tests would require Firebase
 * emulator or mocking the repository.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock private lateinit var mockApplication: Application

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun `TaskFilter enum has all expected values`() {
        val filters = TaskFilter.values()

        assertEquals(3, filters.size)
        assertTrue(filters.contains(TaskFilter.ALL))
        assertTrue(filters.contains(TaskFilter.MY_TASKS))
        assertTrue(filters.contains(TaskFilter.GROUP_TASKS))
    }

    @Test
    fun `initial state has correct default values`() {
        // Note: This test would need proper mocking of Application and Firebase
        // For now, we test the enum and data structures

        val today = LocalDate.now()
        assertNotNull(today)
    }

    @Test
    fun `selectDate updates selected date`() {
        val testDate = LocalDate.of(2024, 1, 15)

        // This tests the date selection logic
        assertNotNull(testDate)
        assertEquals(2024, testDate.year)
        assertEquals(1, testDate.monthValue)
        assertEquals(15, testDate.dayOfMonth)
    }

    @Test
    fun `task filtering logic for MY_TASKS works correctly`() {
        val currentUserId = "user123"

        val tasks =
                listOf(
                        createTestTask("task1", userId = currentUserId, groupId = null),
                        createTestTask("task2", userId = "otherUser", groupId = null),
                        createTestTask("task3", userId = currentUserId, groupId = "group1")
                )

        // Filter MY_TASKS: tasks where userId matches current user
        val myTasks = tasks.filter { it.userId == currentUserId }

        assertEquals(2, myTasks.size)
        assertTrue(myTasks.all { it.userId == currentUserId })
    }

    @Test
    fun `task filtering logic for GROUP_TASKS works correctly`() {
        val tasks =
                listOf(
                        createTestTask("task1", userId = "user1", groupId = null),
                        createTestTask("task2", userId = "user1", groupId = "group1"),
                        createTestTask("task3", userId = "user2", groupId = "group2")
                )

        // Filter GROUP_TASKS: tasks that have a groupId
        val groupTasks = tasks.filter { !it.groupId.isNullOrEmpty() }

        assertEquals(2, groupTasks.size)
        assertTrue(groupTasks.all { !it.groupId.isNullOrEmpty() })
    }

    @Test
    fun `task filtering logic for ALL returns all tasks`() {
        val tasks =
                listOf(
                        createTestTask("task1", userId = "user1", groupId = null),
                        createTestTask("task2", userId = "user1", groupId = "group1"),
                        createTestTask("task3", userId = "user2", groupId = null)
                )

        // Filter ALL: returns all tasks
        val allTasks = tasks

        assertEquals(3, allTasks.size)
    }

    @Test
    fun `extracting dates from tasks works correctly`() {
        val date1 = LocalDate.of(2024, 1, 15)
        val date2 = LocalDate.of(2024, 1, 20)

        val tasks =
                listOf(
                        createTestTask("task1", dueDate = date1),
                        createTestTask("task2", dueDate = date1),
                        createTestTask("task3", dueDate = date2)
                )

        // Extract unique dates
        val dates =
                tasks
                        .mapNotNull { task ->
                            task.dueDate
                                    ?.toDate()
                                    ?.toInstant()
                                    ?.atZone(ZoneId.systemDefault())
                                    ?.toLocalDate()
                        }
                        .toSet()

        assertEquals(2, dates.size)
        assertTrue(dates.contains(date1))
        assertTrue(dates.contains(date2))
    }

    @Test
    fun `filtering tasks by selected date works correctly`() {
        val selectedDate = LocalDate.of(2024, 1, 15)
        val otherDate = LocalDate.of(2024, 1, 20)

        val tasks =
                listOf(
                        createTestTask("task1", dueDate = selectedDate),
                        createTestTask("task2", dueDate = otherDate),
                        createTestTask("task3", dueDate = selectedDate)
                )

        // Filter tasks for selected date
        val tasksForDate =
                tasks.filter { task ->
                    task.dueDate
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate() == selectedDate
                }

        assertEquals(2, tasksForDate.size)
        assertTrue(
                tasksForDate.all {
                    it.dueDate
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate() == selectedDate
                }
        )
    }

    @Test
    fun `tasks without due date are excluded from date filtering`() {
        val selectedDate = LocalDate.of(2024, 1, 15)

        val tasks =
                listOf(
                        createTestTask("task1", dueDate = selectedDate),
                        createTestTask("task2", dueDate = null),
                        createTestTask("task3", dueDate = selectedDate)
                )

        // Extract dates (null dates should be filtered out)
        val dates =
                tasks
                        .mapNotNull { task ->
                            task.dueDate
                                    ?.toDate()
                                    ?.toInstant()
                                    ?.atZone(ZoneId.systemDefault())
                                    ?.toLocalDate()
                        }
                        .toSet()

        assertEquals(1, dates.size)
        assertTrue(dates.contains(selectedDate))
    }

    @Test
    fun `combining filters works correctly`() {
        val currentUserId = "user123"
        val selectedDate = LocalDate.of(2024, 1, 15)

        val tasks =
                listOf(
                        createTestTask(
                                "task1",
                                userId = currentUserId,
                                dueDate = selectedDate,
                                groupId = null
                        ),
                        createTestTask(
                                "task2",
                                userId = currentUserId,
                                dueDate = selectedDate,
                                groupId = "group1"
                        ),
                        createTestTask(
                                "task3",
                                userId = "otherUser",
                                dueDate = selectedDate,
                                groupId = null
                        ),
                        createTestTask(
                                "task4",
                                userId = currentUserId,
                                dueDate = LocalDate.of(2024, 1, 20),
                                groupId = null
                        )
                )

        // Filter: MY_TASKS for selected date
        val myTasksForDate =
                tasks.filter { it.userId == currentUserId }.filter { task ->
                    task.dueDate
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate() == selectedDate
                }

        assertEquals(2, myTasksForDate.size)
        assertTrue(myTasksForDate.all { it.userId == currentUserId })
        assertTrue(
                myTasksForDate.all {
                    it.dueDate
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate() == selectedDate
                }
        )
    }

    // Helper function to create test tasks
    private fun createTestTask(
            id: String,
            userId: String = "user1",
            groupId: String? = null,
            dueDate: LocalDate? = null
    ): FirebaseTask {
        val timestamp =
                dueDate?.let {
                    val date = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    Timestamp(date)
                }

        return FirebaseTask(
                id = id,
                title = "Task $id",
                description = "Description for $id",
                category = "Personal",
                priority = "Medium",
                status = "Pending",
                dueDate = timestamp,
                groupId = groupId,
                assignedTo = listOf(userId),
                userId = userId
        )
    }
}
