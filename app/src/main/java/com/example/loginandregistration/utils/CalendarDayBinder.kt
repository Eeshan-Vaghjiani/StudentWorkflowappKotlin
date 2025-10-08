package com.example.loginandregistration.utils

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.loginandregistration.R
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate

class CalendarDayBinder(
    private val datesWithTasks: Set<LocalDate>,
    private val selectedDate: LocalDate,
    private val onDateSelected: (LocalDate) -> Unit
) {
    
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
        val taskIndicator: View = view.findViewById(R.id.taskIndicator)
        val dayContainer: View = view.findViewById(R.id.dayContainer)
        
        lateinit var day: CalendarDay
        
        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    onDateSelected(day.date)
                }
            }
        }
    }
    
    fun bind(container: DayViewContainer, day: CalendarDay) {
        container.day = day
        val context = container.dayText.context
        
        container.dayText.text = day.date.dayOfMonth.toString()
        
        when (day.position) {
            DayPosition.MonthDate -> {
                // Current month dates
                container.dayText.visibility = View.VISIBLE
                
                // Check if this date has tasks
                val hasTasks = datesWithTasks.contains(day.date)
                container.taskIndicator.visibility = if (hasTasks) View.VISIBLE else View.GONE
                
                // Highlight today's date
                val isToday = day.date == LocalDate.now()
                
                // Highlight selected date
                val isSelected = day.date == selectedDate
                
                when {
                    isSelected -> {
                        // Selected date styling
                        container.dayContainer.setBackgroundResource(R.drawable.selected_day_background)
                        container.dayText.setTextColor(Color.WHITE)
                    }
                    isToday -> {
                        // Today's date styling
                        container.dayContainer.setBackgroundResource(R.drawable.today_background)
                        container.dayText.setTextColor(
                            ContextCompat.getColor(context, R.color.colorPrimary)
                        )
                    }
                    else -> {
                        // Normal date styling
                        container.dayContainer.background = null
                        container.dayText.setTextColor(
                            ContextCompat.getColor(context, android.R.color.black)
                        )
                    }
                }
            }
            else -> {
                // Dates from previous/next month
                container.dayText.visibility = View.INVISIBLE
                container.taskIndicator.visibility = View.GONE
                container.dayContainer.background = null
            }
        }
    }
}
