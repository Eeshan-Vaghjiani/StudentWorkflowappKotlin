package com.example.loginandregistration

import com.example.loginandregistration.models.FirebaseTask
import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for Task Creation and Display functionality
 * 
 * Tests verify:
 * - Task field initialization
 * - Task data model validation
 * - Calendar date conversion
 * - Task filtering logic
 */
class TaskCreationAndDisplayTest {

    @Test
    fun `test FirebaseTask has all required fields initialized`() {
        // Create a task with minimal data
        val task = FirebaseTask(
            title = "Test Task",
            description = "Test Description",
            subject = "Math"
        )

        // Verify default values are properly initialized
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals("Math", task.subject)
        assertEquals("personal", task.category)
        assertEquals("pending", task.status)
        assertEquals("medium", task.priority)
        assertNotNull(task.createdAt)
        assertNotNull(task.updatedAt)
        assertEquals("", task.userId) // Will be set by repository
        assertNull(task.groupId)
        assertNull(task.completedAt)
    }

    @Test
    fun `test FirebaseTask with all fields`() {
        val now = Timestamp.now()
        val dueDate = Timestamp(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 7)
        }.time)

        val task = FirebaseTask(
            id = "task123",
            title = "Complete Assignment",
            description = "Finish math homework",
            subject = "Mathematics",
            category = "assignment",
            status = "pending",
            priority = "high",
            dueDate = dueDate,
            createdAt = now,
            updatedAt = now,
            userId = "user123",
            groupId = "group456",
            completedAt = null
        )

        assertEquals("task123", task.id)
        assertEquals("Complete Assignment", task.title)
        assertEquals("Finish math homework", task.description)
        assertEquals("Mathematics", task.subject)
        assertEquals("assignment", task.category)
        assertEquals("pending", task.status)
        assertEquals("high", task.priority)
        assertEquals(dueDate, task.dueDate)
        assertEquals("user123", task.userId)
        assertEquals("group456", task.groupId)
        assertNull(task.completedAt)
    }

    @Test
    fun `test task category validation`() {
        val validCategories = listOf("personal", "group", "assignment")
        
        validCategories.forEach { category ->
            val task = FirebaseTask(
                title = "Test",
                category = category
            )
            assertTrue(validCategories.contains(task.category))
        }
    }

    @Test
    fun `test task status validation`() {
        val validStatuses = listOf("pending", "completed", "overdue")
        
        validStatuses.forEach { status ->
            val task = FirebaseTask(
                title = "Test",
                status = status
            )
            assertTrue(validStatuses.contains(task.status))
        }
    }

    @Test
    fun `test task priority validation`() {
        val validPriorities = listOf("low", "medium", "high")
        
        validPriorities.forEach { priority ->
            val task = FirebaseTask(
                title = "Test",
                priority = priority
            )
            assertTrue(validPriorities.contains(task.priority))
        }
    }

    @Test
    fun `test task with group assignment`() {
        val task = FirebaseTask(
            title = "Group Project",
            category = "group",
            groupId = "group123"
        )

        assertEquals("group", task.category)
        assertEquals("group123", task.groupId)
        assertNotNull(task.groupId)
    }

    @Test
    fun `test task without group assignment`() {
        val task = FirebaseTask(
            title = "Personal Task",
            category = "personal"
        )

        assertEquals("personal", task.category)
        assertNull(task.groupId)
    }

    @Test
    fun `test task due date is in future`() {
        val futureDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 7)
        }.time
        
        val task = FirebaseTask(
            title = "Future Task",
            dueDate = Timestamp(futureDate)
        )

        assertNotNull(task.dueDate)
        assertTrue(task.dueDate!!.toDate().after(Calendar.getInstance().time))
    }

    @Test
    fun `test task completion updates status and timestamp`() {
        val task = FirebaseTask(
            title = "Task to Complete",
            status = "pending"
        )

        // Simulate completion
        val completedTask = task.copy(
            status = "completed",
            completedAt = Timestamp.now()
        )

        assertEquals("completed", completedTask.status)
        assertNotNull(completedTask.completedAt)
    }

    @Test
    fun `test task filtering by category`() {
        val tasks = listOf(
            FirebaseTask(title = "Personal 1", category = "personal"),
            FirebaseTask(title = "Group 1", category = "group"),
            FirebaseTask(title = "Assignment 1", category = "assignment"),
            FirebaseTask(title = "Personal 2", category = "personal")
        )

        val personalTasks = tasks.filter { it.category == "personal" }
        val groupTasks = tasks.filter { it.category == "group" }
        val assignmentTasks = tasks.filter { it.category == "assignment" }

        assertEquals(2, personalTasks.size)
        assertEquals(1, groupTasks.size)
        assertEquals(1, assignmentTasks.size)
    }

    @Test
    fun `test task filtering by status`() {
        val tasks = listOf(
            FirebaseTask(title = "Task 1", status = "pending"),
            FirebaseTask(title = "Task 2", status = "completed"),
            FirebaseTask(title = "Task 3", status = "pending"),
            FirebaseTask(title = "Task 4", status = "overdue")
        )

        val pendingTasks = tasks.filter { it.status == "pending" }
        val completedTasks = tasks.filter { it.status == "completed" }
        val overdueTasks = tasks.filter { it.status == "overdue" }

        assertEquals(2, pendingTasks.size)
        assertEquals(1, completedTasks.size)
        assertEquals(1, overdueTasks.size)
    }

    @Test
    fun `test task filtering by user`() {
        val userId = "user123"
        val tasks = listOf(
            FirebaseTask(title = "My Task 1", userId = userId),
            FirebaseTask(title = "Other Task", userId = "user456"),
            FirebaseTask(title = "My Task 2", userId = userId)
        )

        val myTasks = tasks.filter { it.userId == userId }

        assertEquals(2, myTasks.size)
        assertTrue(myTasks.all { it.userId == userId })
    }

    @Test
    fun `test task date conversion for calendar`() {
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 10, 30, 0)
        }
        
        val task = FirebaseTask(
            title = "Calendar Task",
            dueDate = Timestamp(calendar.time)
        )

        assertNotNull(task.dueDate)
        
        val taskDate = Calendar.getInstance().apply {
            time = task.dueDate!!.toDate()
        }

        assertEquals(2024, taskDate.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, taskDate.get(Calendar.MONTH))
        assertEquals(15, taskDate.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `test multiple tasks on same date`() {
        val calendar = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15)
        }
        val sameDate = Timestamp(calendar.time)

        val tasks = listOf(
            FirebaseTask(title = "Task 1", dueDate = sameDate),
            FirebaseTask(title = "Task 2", dueDate = sameDate),
            FirebaseTask(title = "Task 3", dueDate = sameDate)
        )

        val tasksOnDate = tasks.filter { task ->
            task.dueDate?.let { dueDate ->
                val taskCal = Calendar.getInstance().apply { time = dueDate.toDate() }
                taskCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                taskCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            } ?: false
        }

        assertEquals(3, tasksOnDate.size)
    }

    @Test
    fun `test task copy with updates`() {
        val originalTask = FirebaseTask(
            title = "Original Title",
            status = "pending",
            priority = "low"
        )

        val updatedTask = originalTask.copy(
            title = "Updated Title",
            status = "completed",
            priority = "high",
            completedAt = Timestamp.now()
        )

        assertEquals("Updated Title", updatedTask.title)
        assertEquals("completed", updatedTask.status)
        assertEquals("high", updatedTask.priority)
        assertNotNull(updatedTask.completedAt)
        
        // Original should remain unchanged
        assertEquals("Original Title", originalTask.title)
        assertEquals("pending", originalTask.status)
        assertEquals("low", originalTask.priority)
    }

    @Test
    fun `test task with empty optional fields`() {
        val task = FirebaseTask(
            title = "Minimal Task"
        )

        assertEquals("Minimal Task", task.title)
        assertEquals("", task.description)
        assertEquals("", task.subject)
        assertNull(task.dueDate)
        assertNull(task.groupId)
        assertNull(task.completedAt)
    }

    @Test
    fun `test task sorting by due date`() {
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }

        val tasks = listOf(
            FirebaseTask(title = "Next Week", dueDate = Timestamp(nextWeek.time)),
            FirebaseTask(title = "Today", dueDate = Timestamp(today.time)),
            FirebaseTask(title = "Tomorrow", dueDate = Timestamp(tomorrow.time))
        )

        val sortedTasks = tasks.sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }

        assertEquals("Today", sortedTasks[0].title)
        assertEquals("Tomorrow", sortedTasks[1].title)
        assertEquals("Next Week", sortedTasks[2].title)
    }

    @Test
    fun `test task with null due date sorts last`() {
        val today = Calendar.getInstance()
        
        val tasks = listOf(
            FirebaseTask(title = "No Due Date", dueDate = null),
            FirebaseTask(title = "Has Due Date", dueDate = Timestamp(today.time))
        )

        val sortedTasks = tasks.sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }

        assertEquals("Has Due Date", sortedTasks[0].title)
        assertEquals("No Due Date", sortedTasks[1].title)
    }
}
