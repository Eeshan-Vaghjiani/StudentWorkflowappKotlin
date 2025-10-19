package com.example.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.adapters.CalendarTaskAdapter
import com.example.loginandregistration.utils.CalendarDayBinder
import com.example.loginandregistration.utils.ErrorMessages
import com.example.loginandregistration.viewmodels.CalendarViewModel
import com.example.loginandregistration.viewmodels.TaskFilter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {

    private val viewModel: CalendarViewModel by viewModels()

    private lateinit var calendarView: CalendarView
    private lateinit var monthYearText: TextView
    private lateinit var previousMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var chipAllTasks: Chip
    private lateinit var chipMyTasks: Chip
    private lateinit var chipGroupTasks: Chip
    private lateinit var selectedDateText: TextView
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
    private lateinit var loadingIndicator: ProgressBar

    private lateinit var taskAdapter: CalendarTaskAdapter
    private var currentMonth = YearMonth.now()
    private lateinit var gestureDetector: GestureDetectorCompat

    // Activity result launcher for task details
    private val taskDetailsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Task was modified or deleted, reload tasks
                    viewModel.loadTasks()
                }
            }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCalendar()
        setupTasksList()
        setupFilterChips()
        setupSwipeGesture()
        observeViewModel()
        setupMonthNavigation()
    }

    private fun initViews(view: View) {
        calendarView = view.findViewById(R.id.calendarView)
        monthYearText = view.findViewById(R.id.monthYearText)
        previousMonthButton = view.findViewById(R.id.previousMonthButton)
        nextMonthButton = view.findViewById(R.id.nextMonthButton)
        filterChipGroup = view.findViewById(R.id.filterChipGroup)
        chipAllTasks = view.findViewById(R.id.chipAllTasks)
        chipMyTasks = view.findViewById(R.id.chipMyTasks)
        chipGroupTasks = view.findViewById(R.id.chipGroupTasks)
        selectedDateText = view.findViewById(R.id.selectedDateText)
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
    }

    private fun setupCalendar() {
        val daysOfWeek = daysOfWeek()

        // Setup month header
        calendarView.monthHeaderBinder =
                object : MonthHeaderFooterBinder<MonthHeaderViewContainer> {
                    override fun create(view: View) = MonthHeaderViewContainer(view)
                    override fun bind(container: MonthHeaderViewContainer, data: CalendarMonth) {
                        if (container.titlesContainer.tag == null) {
                            container.titlesContainer.tag = data.yearMonth
                            container.titlesContainer.children
                                    .map { it as TextView }
                                    .forEachIndexed { index, textView ->
                                        val dayOfWeek = daysOfWeek[index]
                                        val title =
                                                dayOfWeek.getDisplayName(
                                                        TextStyle.SHORT,
                                                        Locale.getDefault()
                                                )
                                        textView.text = title
                                    }
                        }
                    }
                }

        // Setup calendar range (show 12 months: 6 before and 6 after current month)
        val startMonth = currentMonth.minusMonths(6)
        val endMonth = currentMonth.plusMonths(6)
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        // Update month/year text
        updateMonthYearText(currentMonth)

        // Listen for month scroll
        calendarView.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            updateMonthYearText(currentMonth)
        }
    }

    private fun setupTasksList() {
        taskAdapter = CalendarTaskAdapter { task ->
            // Navigate to task details
            val intent =
                    Intent(requireContext(), TaskDetailsActivity::class.java).apply {
                        putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.id)
                    }
            taskDetailsLauncher.launch(intent)
        }

        tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.datesWithTasks.collect { dates ->
                updateCalendarDayBinder(dates, viewModel.selectedDate.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { selectedDate ->
                updateCalendarDayBinder(viewModel.datesWithTasks.value, selectedDate)
                updateSelectedDateText(selectedDate)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tasksForSelectedDate.collect { tasks ->
                taskAdapter.submitList(tasks)

                if (tasks.isEmpty()) {
                    tasksRecyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.VISIBLE
                } else {
                    tasksRecyclerView.visibility = View.VISIBLE
                    emptyStateLayout.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error -> error?.let { showError(it) } }
        }
    }

    /** Show error message to user with retry option */
    private fun showError(errorMessage: String) {
        val message =
                when {
                    errorMessage.contains("PERMISSION_DENIED", ignoreCase = true) ->
                            ErrorMessages.PERMISSION_DENIED
                    errorMessage.contains("UNAVAILABLE", ignoreCase = true) ->
                            ErrorMessages.NETWORK_ERROR
                    errorMessage.contains("FAILED_PRECONDITION", ignoreCase = true) ->
                            ErrorMessages.INDEX_MISSING
                    errorMessage.contains("NOT_FOUND", ignoreCase = true) ->
                            ErrorMessages.TASK_NOT_FOUND
                    else -> ErrorMessages.CALENDAR_LOAD_FAILED
                }

        view?.let { v ->
            Snackbar.make(v, message, Snackbar.LENGTH_LONG)
                    .setAction(ErrorMessages.RETRY_PROMPT) { viewModel.loadTasks() }
                    .show()
        }
    }

    private fun updateCalendarDayBinder(datesWithTasks: Set<LocalDate>, selectedDate: LocalDate) {
        val dayBinder =
                CalendarDayBinder(
                        datesWithTasks = datesWithTasks,
                        selectedDate = selectedDate,
                        onDateSelected = { date -> viewModel.selectDate(date) }
                )

        calendarView.dayBinder =
                object : MonthDayBinder<CalendarDayBinder.DayViewContainer> {
                    override fun create(view: View) = dayBinder.DayViewContainer(view)
                    override fun bind(
                            container: CalendarDayBinder.DayViewContainer,
                            data: CalendarDay
                    ) {
                        dayBinder.bind(container, data)
                    }
                }
    }

    private fun setupMonthNavigation() {
        previousMonthButton.setOnClickListener {
            val previousMonth = currentMonth.minusMonths(1)
            calendarView.smoothScrollToMonth(previousMonth)
        }

        nextMonthButton.setOnClickListener {
            val nextMonth = currentMonth.plusMonths(1)
            calendarView.smoothScrollToMonth(nextMonth)
        }
    }

    private fun setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val filter =
                    when (checkedIds[0]) {
                        R.id.chipAllTasks -> TaskFilter.ALL
                        R.id.chipMyTasks -> TaskFilter.MY_TASKS
                        R.id.chipGroupTasks -> TaskFilter.GROUP_TASKS
                        else -> TaskFilter.ALL
                    }

            viewModel.setFilter(filter)
        }
    }

    private fun setupSwipeGesture() {
        gestureDetector =
                GestureDetectorCompat(
                        requireContext(),
                        object : GestureDetector.SimpleOnGestureListener() {
                            private val SWIPE_THRESHOLD = 100
                            private val SWIPE_VELOCITY_THRESHOLD = 100

                            override fun onFling(
                                    e1: MotionEvent?,
                                    e2: MotionEvent,
                                    velocityX: Float,
                                    velocityY: Float
                            ): Boolean {
                                if (e1 == null) return false

                                val diffX = e2.x - e1.x
                                val diffY = e2.y - e1.y

                                if (abs(diffX) > abs(diffY)) {
                                    if (abs(diffX) > SWIPE_THRESHOLD &&
                                                    abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                                    ) {
                                        if (diffX > 0) {
                                            // Swipe right - go to previous month
                                            val previousMonth = currentMonth.minusMonths(1)
                                            calendarView.smoothScrollToMonth(previousMonth)
                                        } else {
                                            // Swipe left - go to next month
                                            val nextMonth = currentMonth.plusMonths(1)
                                            calendarView.smoothScrollToMonth(nextMonth)
                                        }
                                        return true
                                    }
                                }
                                return false
                            }
                        }
                )

        calendarView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun updateMonthYearText(yearMonth: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        monthYearText.text = yearMonth.format(formatter)
    }

    private fun updateSelectedDateText(date: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
        selectedDateText.text = "Tasks for ${date.format(formatter)}"
    }

    // Helper class for month header
    class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
        val titlesContainer = view as ViewGroup
    }

    private val ViewGroup.children: Sequence<View>
        get() = sequence {
            for (i in 0 until childCount) {
                yield(getChildAt(i))
            }
        }
}
