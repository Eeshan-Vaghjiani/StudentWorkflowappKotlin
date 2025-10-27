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
import com.example.loginandregistration.viewmodels.CalendarViewModel
import com.example.loginandregistration.viewmodels.TaskFilter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
    private lateinit var errorStateManager: com.example.loginandregistration.utils.ErrorStateManager

    // Make views nullable for lifecycle safety
    private var calendarView: CalendarView? = null
    private var monthYearText: TextView? = null
    private var previousMonthButton: ImageButton? = null
    private var nextMonthButton: ImageButton? = null
    private var filterChipGroup: ChipGroup? = null
    private var chipAllTasks: Chip? = null
    private var chipMyTasks: Chip? = null
    private var chipGroupTasks: Chip? = null
    private var selectedDateText: TextView? = null
    private var tasksRecyclerView: RecyclerView? = null
    private var emptyStateLayout: View? = null
    private var loadingIndicator: ProgressBar? = null

    private var taskAdapter: CalendarTaskAdapter? = null
    private var currentMonth = YearMonth.now()
    private var gestureDetector: GestureDetectorCompat? = null

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

        errorStateManager =
                com.example.loginandregistration.utils.ErrorStateManager(requireContext())
        initViews(view)
        setupCalendar()
        setupTasksList()
        setupFilterChips()
        setupSwipeGesture()
        observeViewModel()
        setupMonthNavigation()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Clear all view references to prevent memory leaks
        calendarView = null
        monthYearText = null
        previousMonthButton = null
        nextMonthButton = null
        filterChipGroup = null
        chipAllTasks = null
        chipMyTasks = null
        chipGroupTasks = null
        selectedDateText = null
        tasksRecyclerView = null
        emptyStateLayout = null
        loadingIndicator = null
        taskAdapter = null
        gestureDetector = null
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
        val calendar = calendarView ?: return
        val daysOfWeek = daysOfWeek()

        // Setup month header
        calendar.monthHeaderBinder =
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
        calendar.setup(startMonth, endMonth, daysOfWeek.first())
        calendar.scrollToMonth(currentMonth)

        // Update month/year text
        updateMonthYearText(currentMonth)

        // Listen for month scroll
        calendar.monthScrollListener = { month ->
            currentMonth = month.yearMonth
            updateMonthYearText(currentMonth)
        }
    }

    private fun setupTasksList() {
        val recyclerView = tasksRecyclerView ?: return

        taskAdapter = CalendarTaskAdapter { task ->
            // Navigate to task details
            val intent =
                    Intent(requireContext(), TaskDetailsActivity::class.java).apply {
                        putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.id)
                    }
            taskDetailsLauncher.launch(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.datesWithTasks.collect { dates ->
                    // Only update UI if view still exists
                    if (isAdded && calendarView != null) {
                        updateCalendarDayBinder(dates, viewModel.selectedDate.value)
                    }
                }
            } catch (e: Exception) {
                if (isAdded && view != null) {
                    android.util.Log.e("CalendarFragment", "Error collecting dates with tasks", e)
                    showError(e.message ?: "Error loading calendar data")
                } else {
                    android.util.Log.d(
                            "CalendarFragment",
                            "View destroyed, skipping error UI update"
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.selectedDate.collect { selectedDate ->
                    // Only update UI if view still exists
                    if (isAdded && calendarView != null) {
                        updateCalendarDayBinder(viewModel.datesWithTasks.value, selectedDate)
                        updateSelectedDateText(selectedDate)
                    }
                }
            } catch (e: Exception) {
                if (isAdded && view != null) {
                    android.util.Log.e("CalendarFragment", "Error collecting selected date", e)
                } else {
                    android.util.Log.d(
                            "CalendarFragment",
                            "View destroyed, skipping error UI update"
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.tasksForSelectedDate.collect { tasks ->
                    // Only update UI if view still exists
                    if (isAdded && tasksRecyclerView != null && emptyStateLayout != null) {
                        taskAdapter?.submitList(tasks)

                        if (tasks.isEmpty()) {
                            tasksRecyclerView?.visibility = View.GONE
                            emptyStateLayout?.visibility = View.VISIBLE
                        } else {
                            tasksRecyclerView?.visibility = View.VISIBLE
                            emptyStateLayout?.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                if (isAdded && view != null) {
                    android.util.Log.e("CalendarFragment", "Error collecting tasks", e)
                    showError(e.message ?: "Error loading tasks")
                } else {
                    android.util.Log.d(
                            "CalendarFragment",
                            "View destroyed, skipping error UI update"
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.isLoading.collect { isLoading ->
                    // Only update UI if view still exists
                    if (isAdded && loadingIndicator != null) {
                        loadingIndicator?.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) {
                if (isAdded && view != null) {
                    android.util.Log.e("CalendarFragment", "Error collecting loading state", e)
                } else {
                    android.util.Log.d(
                            "CalendarFragment",
                            "View destroyed, skipping error UI update"
                    )
                }
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.error.collect { error ->
                    // Only show error if view still exists
                    if (isAdded && view != null) {
                        error?.let { showError(it) }
                    }
                }
            } catch (e: Exception) {
                if (isAdded && view != null) {
                    android.util.Log.e("CalendarFragment", "Error collecting error state", e)
                } else {
                    android.util.Log.d(
                            "CalendarFragment",
                            "View destroyed, skipping error UI update"
                    )
                }
            }
        }
    }

    /** Show error message to user with retry option */
    private fun showError(errorMessage: String) {
        // Check if view still exists
        if (!isAdded || view == null) {
            android.util.Log.d("CalendarFragment", "Cannot show error: view is destroyed")
            return
        }

        // Use ErrorStateManager for consistent error handling
        val exception = Exception(errorMessage)
        val errorState = errorStateManager.categorizeError(exception)

        view?.let { v -> errorStateManager.showError(errorState, v) { viewModel.loadTasks() } }
    }

    private fun updateCalendarDayBinder(datesWithTasks: Set<LocalDate>, selectedDate: LocalDate) {
        // Check if view still exists
        val calendar = calendarView ?: return

        val dayBinder =
                CalendarDayBinder(
                        datesWithTasks = datesWithTasks,
                        selectedDate = selectedDate,
                        onDateSelected = { date -> viewModel.selectDate(date) }
                )

        calendar.dayBinder =
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
        previousMonthButton?.setOnClickListener {
            val previousMonth = currentMonth.minusMonths(1)
            calendarView?.smoothScrollToMonth(previousMonth)
        }

        nextMonthButton?.setOnClickListener {
            val nextMonth = currentMonth.plusMonths(1)
            calendarView?.smoothScrollToMonth(nextMonth)
        }
    }

    private fun setupFilterChips() {
        filterChipGroup?.setOnCheckedStateChangeListener { _, checkedIds ->
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
                                            calendarView?.smoothScrollToMonth(previousMonth)
                                        } else {
                                            // Swipe left - go to next month
                                            val nextMonth = currentMonth.plusMonths(1)
                                            calendarView?.smoothScrollToMonth(nextMonth)
                                        }
                                        return true
                                    }
                                }
                                return false
                            }
                        }
                )

        calendarView?.setOnTouchListener { _, event ->
            gestureDetector?.onTouchEvent(event)
            false
        }
    }

    private fun updateMonthYearText(yearMonth: YearMonth) {
        // Check if view still exists
        val textView = monthYearText ?: return

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        textView.text = yearMonth.format(formatter)
    }

    private fun updateSelectedDateText(date: LocalDate) {
        // Check if view still exists
        val textView = selectedDateText ?: return

        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
        textView.text = "Tasks for ${date.format(formatter)}"
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
