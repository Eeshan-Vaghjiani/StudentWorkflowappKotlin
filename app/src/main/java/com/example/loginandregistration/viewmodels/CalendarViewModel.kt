package com.example.loginandregistration.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginandregistration.models.FirebaseTask
import com.example.loginandregistration.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL,
    MY_TASKS,
    GROUP_TASKS
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val taskRepository = TaskRepository(application)
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _allTasks = MutableStateFlow<List<FirebaseTask>>(emptyList())

    private val _tasks = MutableStateFlow<List<FirebaseTask>>(emptyList())
    val tasks: StateFlow<List<FirebaseTask>> = _tasks.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _tasksForSelectedDate = MutableStateFlow<List<FirebaseTask>>(emptyList())
    val tasksForSelectedDate: StateFlow<List<FirebaseTask>> = _tasksForSelectedDate.asStateFlow()

    private val _datesWithTasks = MutableStateFlow<Set<LocalDate>>(emptySet())
    val datesWithTasks: StateFlow<Set<LocalDate>> = _datesWithTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    init {
        loadTasks()
        setupRealTimeListeners()
    }

    private fun setupRealTimeListeners() {
        // Listen to all tasks with real-time updates
        viewModelScope.launch {
            taskRepository.getUserTasks(null).collect { tasks ->
                _allTasks.value = tasks
                applyFilter(_currentFilter.value)
            }
        }

        // Listen to dates with tasks
        viewModelScope.launch {
            taskRepository.getDatesWithTasks().collect { dates -> _datesWithTasks.value = dates }
        }
    }

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Initial load - real-time listeners will handle updates
                taskRepository.getUserTasks(null).collect { tasks ->
                    _allTasks.value = tasks
                    applyFilter(_currentFilter.value)
                    _isLoading.value = false
                    return@collect // Only collect once for initial load
                }
            } catch (e: Exception) {
                // Handle error
                _allTasks.value = emptyList()
                _tasks.value = emptyList()
                _datesWithTasks.value = emptySet()
                _isLoading.value = false
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateTasksForSelectedDate()
    }

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: TaskFilter) {
        val filteredTasks =
                when (filter) {
                    TaskFilter.ALL -> _allTasks.value
                    TaskFilter.MY_TASKS ->
                            _allTasks.value.filter { task ->
                                // Tasks where current user is the creator (userId matches)
                                task.userId == currentUserId
                            }
                    TaskFilter.GROUP_TASKS ->
                            _allTasks.value.filter { task ->
                                // Tasks that have a groupId (belong to a group)
                                !task.groupId.isNullOrEmpty()
                            }
                }

        _tasks.value = filteredTasks

        // Extract dates that have tasks (after filtering)
        val dates =
                filteredTasks
                        .mapNotNull { task ->
                            task.dueDate
                                    ?.toDate()
                                    ?.toInstant()
                                    ?.atZone(ZoneId.systemDefault())
                                    ?.toLocalDate()
                        }
                        .toSet()
        _datesWithTasks.value = dates

        // Update tasks for selected date
        updateTasksForSelectedDate()
    }

    private fun updateTasksForSelectedDate() {
        val selected = _selectedDate.value
        val tasksForDate =
                _tasks.value.filter { task ->
                    task.dueDate
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate() == selected
                }
        _tasksForSelectedDate.value = tasksForDate
    }
}
