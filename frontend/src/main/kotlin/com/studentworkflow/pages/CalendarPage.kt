package com.studentworkflow.pages

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.textarea
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.ul
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.section
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.tr
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.td

external interface CalendarPageProps : Props

val CalendarPage = FC<CalendarPageProps> { props ->
    val (currentDate, setCurrentDate) = useState("2024-01-15")
    val (showAddEvent, setShowAddEvent) = useState(false)
    val (events, setEvents) = useState(listOf(
        CalendarEvent("1", "Study Group Meeting", "2024-01-15", "15:00", "16:00", "Library Study Room", "Weekly study session for Advanced Algorithms"),
        CalendarEvent("2", "Office Hours", "2024-01-16", "10:00", "11:00", "Professor Smith's Office", "Questions about research paper"),
        CalendarEvent("3", "Group Project Meeting", "2024-01-17", "14:00", "15:30", "Engineering Building Room 201", "Final presentation preparation")
    ))
    
    div {
        className = "max-w-7xl mx-auto"
        
        // Header
        section {
            className = "mb-8"
            div {
                className = "flex justify-between items-center"
                div {
                    h1 {
                        className = "text-3xl font-bold text-gray-900 mb-2"
                        +"Calendar"
                    }
                    p {
                        className = "text-gray-600"
                        +"Manage your academic schedule and events."
                    }
                }
                div {
                    className = "flex space-x-3"
                    button {
                        className = "px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                        onClick = { /* TODO: Sync with Google Calendar */ }
                        +"Sync Calendar"
                    }
                    button {
                        className = "bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-md"
                        onClick = { setShowAddEvent(true) }
                        +"Add Event"
                    }
                }
            }
        }
        
        // Add Event Modal
        if (showAddEvent) {
            AddEventModal(
                onClose = { setShowAddEvent(false) },
                onAdd = { event ->
                    setEvents { it + event }
                    setShowAddEvent(false)
                }
            )
        }
        
        // Calendar Navigation
        section {
            className = "mb-6"
            div {
                className = "bg-white p-4 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex justify-between items-center"
                    button {
                        className = "p-2 text-gray-400 hover:text-gray-600"
                        onClick = { /* TODO: Previous month */ }
                        +"←"
                    }
                    h2 {
                        className = "text-lg font-semibold text-gray-900"
                        +"January 2024"
                    }
                    button {
                        className = "p-2 text-gray-400 hover:text-gray-600"
                        onClick = { /* TODO: Next month */ }
                        +"→"
                    }
                }
            }
        }
        
        // Calendar Grid
        section {
            className = "mb-8"
            div {
                className = "bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden"
                CalendarGrid(
                    currentDate = currentDate,
                    events = events,
                    onDateSelect = { setCurrentDate(it) }
                )
            }
        }
        
        // Upcoming Events
        section {
            className = "mb-8"
            div {
                className = "bg-white rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "px-6 py-4 border-b border-gray-200"
                    h2 {
                        className = "text-lg font-semibold text-gray-900"
                        +"Upcoming Events"
                    }
                }
                div {
                    className = "p-6"
                    if (events.isEmpty()) {
                        p {
                            className = "text-gray-500 text-center py-8"
                            +"No upcoming events. Add your first event to get started!"
                        }
                    } else {
                        ul {
                            className = "space-y-4"
                            events.sortedBy { it.date + it.startTime }.forEach { event ->
                                EventItem(
                                    event = event,
                                    onEdit = { /* TODO: Edit event */ },
                                    onDelete = {
                                        setEvents { currentEvents ->
                                            currentEvents.filter { it.id != event.id }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

val CalendarGrid = FC<CalendarGridProps> { props ->
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val currentDate = props.currentDate
    val events = props.events
    
    div {
        className = "p-6"
        
        // Days of week header
        div {
            className = "grid grid-cols-7 gap-1 mb-2"
            daysOfWeek.forEach { day ->
                div {
                    className = "text-center text-sm font-medium text-gray-500 py-2"
                    +day
                }
            }
        }
        
        // Calendar days grid (simplified - showing current week)
        div {
            className = "grid grid-cols-7 gap-1"
            
            // Generate calendar days (simplified for demo)
            (1..7).forEach { day ->
                val date = "2024-01-${String.format("%02d", day + 7)}" // Start from Monday
                val dayEvents = events.filter { it.date == date }
                
                div {
                    className = "min-h-24 p-2 border border-gray-200 hover:bg-gray-50 cursor-pointer"
                    onClick = { props.onDateSelect(date) }
                    
                    div {
                        className = "text-sm font-medium text-gray-900 mb-1"
                        +day.toString()
                    }
                    
                    if (dayEvents.isNotEmpty()) {
                        div {
                            className = "space-y-1"
                            dayEvents.take(2).forEach { event ->
                                div {
                                    className = "text-xs p-1 bg-primary-100 text-primary-800 rounded truncate"
                                    +event.title
                                }
                            }
                            if (dayEvents.size > 2) {
                                div {
                                    className = "text-xs text-gray-500 text-center"
                                    +"+${dayEvents.size - 2} more"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val AddEventModal = FC<AddEventModalProps> { props ->
    val (formData, setFormData) = useState(EventForm())
    
    val handleSubmit = { event: dynamic ->
        event.preventDefault()
        val newEvent = CalendarEvent(
            id = (System.currentTimeMillis() / 1000).toString(),
            title = formData.title,
            date = formData.date,
            startTime = formData.startTime,
            endTime = formData.endTime,
            location = formData.location,
            description = formData.description
        )
        props.onAdd(newEvent)
    }
    
    div {
        className = "fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
        div {
            className = "relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white"
            div {
                className = "mt-3"
                h3 {
                    className = "text-lg font-medium text-gray-900 mb-4"
                    +"Add New Event"
                }
                
                form {
                    onSubmit = handleSubmit
                    className = "space-y-4"
                    
                    div {
                        label {
                            htmlFor = "title"
                            className = "block text-sm font-medium text-gray-700"
                            +"Event Title"
                        }
                        input {
                            type = InputType.text
                            id = "title"
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                            value = formData.title
                            onChange = { event -> setFormData { it.copy(title = event.target.value) } }
                        }
                    }
                    
                    div {
                        label {
                            htmlFor = "date"
                            className = "block text-sm font-medium text-gray-700"
                            +"Date"
                        }
                        input {
                            type = InputType.date
                            id = "date"
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                            value = formData.date
                            onChange = { event -> setFormData { it.copy(date = event.target.value) } }
                        }
                    }
                    
                    div {
                        className = "grid grid-cols-2 gap-4"
                        div {
                            label {
                                htmlFor = "startTime"
                                className = "block text-sm font-medium text-gray-700"
                                +"Start Time"
                            }
                            input {
                                type = InputType.time
                                id = "startTime"
                                required = true
                                className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                                value = formData.startTime
                                onChange = { event -> setFormData { it.copy(startTime = event.target.value) } }
                            }
                        }
                        div {
                            label {
                                htmlFor = "endTime"
                                className = "block text-sm font-medium text-gray-700"
                                +"End Time"
                            }
                            input {
                                type = InputType.time
                                id = "endTime"
                                required = true
                                className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                                value = formData.endTime
                                onChange = { event -> setFormData { it.copy(endTime = event.target.value) } }
                            }
                        }
                    }
                    
                    div {
                        label {
                            htmlFor = "location"
                            className = "block text-sm font-medium text-gray-700"
                            +"Location"
                        }
                        input {
                            type = InputType.text
                            id = "location"
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                            value = formData.location
                            onChange = { event -> setFormData { it.copy(location = event.target.value) } }
                        }
                    }
                    
                    div {
                        label {
                            htmlFor = "description"
                            className = "block text-sm font-medium text-gray-700"
                            +"Description"
                        }
                        textarea {
                            id = "description"
                            rows = 3
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                            value = formData.description
                            onChange = { event -> setFormData { it.copy(description = event.target.value) } }
                        }
                    }
                    
                    div {
                        className = "flex justify-end space-x-3"
                        button {
                            type = ButtonType.button
                            className = "px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                            onClick = { props.onClose() }
                            +"Cancel"
                        }
                        button {
                            type = ButtonType.submit
                            className = "px-4 py-2 bg-primary-600 text-white rounded-md text-sm font-medium hover:bg-primary-700"
                            +"Add Event"
                        }
                    }
                }
            }
        }
    }
}

val EventItem = FC<EventItemProps> { props ->
    div {
        className = "flex items-center p-4 bg-gray-50 rounded-lg"
        div {
            className = "flex-1"
            h3 {
                className = "text-sm font-medium text-gray-900"
                +props.event.title
            }
            p {
                className = "text-xs text-gray-500"
                +"${props.event.date} at ${props.event.startTime} - ${props.event.endTime}"
            }
            if (props.event.location.isNotEmpty()) {
                p {
                    className = "text-xs text-gray-500"
                    +"📍 ${props.event.location}"
                }
            }
        }
        div {
            className = "flex space-x-2"
            button {
                className = "text-primary-600 hover:text-primary-900 text-sm"
                onClick = { props.onEdit() }
                +"Edit"
            }
            button {
                className = "text-red-600 hover:text-red-900 text-sm"
                onClick = { props.onDelete() }
                +"Delete"
            }
        }
    }
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val description: String
)

data class EventForm(
    val title: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val description: String = ""
)

external interface CalendarGridProps : Props {
    var currentDate: String
    var events: List<CalendarEvent>
    var onDateSelect: (String) -> Unit
}

external interface AddEventModalProps : Props {
    var onClose: () -> Unit
    var onAdd: (CalendarEvent) -> Unit
}

external interface EventItemProps : Props {
    var event: CalendarEvent
    var onEdit: () -> Unit
    var onDelete: () -> Unit
}