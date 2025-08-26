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

external interface WorkflowPageProps : Props

val WorkflowPage = FC<WorkflowPageProps> { props ->
    val (showAddTask, setShowAddTask) = useState(false)
    val (tasks, setTasks) = useState(listOf(
        Task("1", "Complete Research Paper Outline", "High", "In Progress", "2024-01-15", "Research paper for Advanced Algorithms course"),
        Task("2", "Review Calculus Problems", "Medium", "Pending", "2024-01-16", "Chapter 5 problems from Calculus textbook"),
        Task("3", "Prepare for Group Presentation", "Low", "Completed", "2024-01-14", "Group project presentation for Business Ethics")
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
                        +"Workflow Management"
                    }
                    p {
                        className = "text-gray-600"
                        +"Organize and track your academic tasks and projects."
                    }
                }
                button {
                    className = "bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-md"
                    onClick = { setShowAddTask(true) }
                    +"Add New Task"
                }
            }
        }
        
        // Add Task Modal
        if (showAddTask) {
            AddTaskModal(
                onClose = { setShowAddTask(false) },
                onAdd = { task ->
                    setTasks { it + task }
                    setShowAddTask(false)
                }
            )
        }
        
        // Task Filters
        section {
            className = "mb-6"
            div {
                className = "bg-white p-4 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex flex-wrap gap-4"
                    button {
                        className = "px-4 py-2 rounded-md text-sm font-medium bg-primary-100 text-primary-800"
                        +"All Tasks"
                    }
                    button {
                        className = "px-4 py-2 rounded-md text-sm font-medium text-gray-600 hover:bg-gray-100"
                        +"High Priority"
                    }
                    button {
                        className = "px-4 py-2 rounded-md text-sm font-medium text-gray-600 hover:bg-gray-100"
                        +"In Progress"
                    }
                    button {
                        className = "px-4 py-2 rounded-md text-sm font-medium text-gray-600 hover:bg-gray-100"
                        +"Completed"
                    }
                }
            }
        }
        
        // Tasks Grid
        section {
            className = "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
            
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onStatusChange = { newStatus ->
                        setTasks { currentTasks ->
                            currentTasks.map { 
                                if (it.id == task.id) it.copy(status = newStatus) else it 
                            }
                        }
                    },
                    onDelete = {
                        setTasks { currentTasks ->
                            currentTasks.filter { it.id != task.id }
                        }
                    }
                )
            }
        }
    }
}

val AddTaskModal = FC<AddTaskModalProps> { props ->
    val (formData, setFormData) = useState(TaskForm())
    
    val handleSubmit = { event: dynamic ->
        event.preventDefault()
        val newTask = Task(
            id = (System.currentTimeMillis() / 1000).toString(),
            title = formData.title,
            priority = formData.priority,
            status = "Pending",
            dueDate = formData.dueDate,
            description = formData.description
        )
        props.onAdd(newTask)
    }
    
    div {
        className = "fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50"
        div {
            className = "relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white"
            div {
                className = "mt-3"
                h3 {
                    className = "text-lg font-medium text-gray-900 mb-4"
                    +"Add New Task"
                }
                
                form {
                    onSubmit = handleSubmit
                    className = "space-y-4"
                    
                    div {
                        label {
                            htmlFor = "title"
                            className = "block text-sm font-medium text-gray-700"
                            +"Task Title"
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
                            htmlFor = "priority"
                            className = "block text-sm font-medium text-gray-700"
                            +"Priority"
                        }
                        select {
                            id = "priority"
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                            value = formData.priority
                            onChange = { event -> setFormData { it.copy(priority = event.target.value) } }
                            
                            option {
                                value = "Low"
                                +"Low"
                            }
                            option {
                                value = "Medium"
                                +"Medium"
                            }
                            option {
                                value = "High"
                                +"High"
                            }
                        }
                    }
                    
                    div {
                        label {
                            htmlFor = "dueDate"
                            className = "block text-sm font-medium text-gray-700"
                            +"Due Date"
                        }
                        input {
                            type = InputType.date
                            id = "dueDate"
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                            value = formData.dueDate
                            onChange = { event -> setFormData { it.copy(dueDate = event.target.value) } }
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
                            +"Add Task"
                        }
                    }
                }
            }
        }
    }
}

val TaskCard = FC<TaskCardProps> { props ->
    val priorityColors = mapOf(
        "High" to "bg-red-100 text-red-800",
        "Medium" to "bg-yellow-100 text-yellow-800",
        "Low" to "bg-green-100 text-green-800"
    )
    
    val statusColors = mapOf(
        "Pending" to "bg-gray-100 text-gray-800",
        "In Progress" to "bg-blue-100 text-blue-800",
        "Completed" to "bg-green-100 text-green-800"
    )
    
    div {
        className = "bg-white rounded-lg shadow-sm border border-gray-200 p-6"
        div {
            className = "flex justify-between items-start mb-4"
            h3 {
                className = "text-lg font-semibold text-gray-900"
                +props.task.title
            }
            div {
                className = "flex space-x-2"
                button {
                    className = "text-gray-400 hover:text-gray-600"
                    onClick = { /* TODO: Edit task */ }
                    +"✏️"
                }
                button {
                    className = "text-gray-400 hover:text-red-600"
                    onClick = { props.onDelete() }
                    +"🗑️"
                }
            }
        }
        
        p {
            className = "text-gray-600 mb-4"
            +props.task.description
        }
        
        div {
            className = "flex justify-between items-center mb-4"
            span {
                className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${priorityColors[props.task.priority] ?: "bg-gray-100 text-gray-800"}"
                +props.task.priority
            }
            span {
                className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors[props.task.status] ?: "bg-gray-100 text-gray-800"}"
                +props.task.status
            }
        }
        
        div {
            className = "text-sm text-gray-500 mb-4"
            +"Due: ${props.task.dueDate}"
        }
        
        div {
            className = "flex space-x-2"
            select {
                className = "flex-1 px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                value = props.task.status
                onChange = { event -> props.onStatusChange(event.target.value) }
                
                option {
                    value = "Pending"
                    +"Pending"
                }
                option {
                    value = "In Progress"
                    +"In Progress"
                }
                option {
                    value = "Completed"
                    +"Completed"
                }
            }
        }
    }
}

data class Task(
    val id: String,
    val title: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val description: String
)

data class TaskForm(
    val title: String = "",
    val priority: String = "Medium",
    val dueDate: String = "",
    val description: String = ""
)

external interface AddTaskModalProps : Props {
    var onClose: () -> Unit
    var onAdd: (Task) -> Unit
}

external interface TaskCardProps : Props {
    var task: Task
    var onStatusChange: (String) -> Unit
    var onDelete: () -> Unit
}