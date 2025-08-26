package com.studentworkflow.pages

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.tr
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.section

external interface AdminDashboardPageProps : Props

val AdminDashboardPage = FC<AdminDashboardPageProps> { props ->
    val (activeTab, setActiveTab) = useState("overview")
    val (searchTerm, setSearchTerm) = useState("")
    val (selectedUser, setSelectedUser) = useState("")
    
    div {
        className = "max-w-7xl mx-auto"
        
        // Header
        section {
            className = "mb-8"
            h1 {
                className = "text-3xl font-bold text-gray-900 mb-2"
                +"Admin Dashboard"
            }
            p {
                className = "text-gray-600"
                +"Manage users, monitor system performance, and view analytics."
            }
        }
        
        // Tab Navigation
        section {
            className = "mb-8"
            div {
                className = "border-b border-gray-200"
                div {
                    className = "flex space-x-8"
                    button {
                        className = "py-2 px-1 border-b-2 font-medium text-sm ${if (activeTab == "overview") "border-primary-500 text-primary-600" else "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}"
                        onClick = { setActiveTab("overview") }
                        +"Overview"
                    }
                    button {
                        className = "py-2 px-1 border-b-2 font-medium text-sm ${if (activeTab == "users") "border-primary-500 text-primary-600" else "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}"
                        onClick = { setActiveTab("users") }
                        +"User Management"
                    }
                    button {
                        className = "py-2 px-1 border-b-2 font-medium text-sm ${if (activeTab == "analytics") "border-primary-500 text-primary-600" else "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}"
                        onClick = { setActiveTab("analytics") }
                        +"Analytics"
                    }
                    button {
                        className = "py-2 px-1 border-b-2 font-medium text-sm ${if (activeTab == "system") "border-primary-500 text-primary-600" else "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"}"
                        onClick = { setActiveTab("system") }
                        +"System"
                    }
                }
            }
        }
        
        // Tab Content
        when (activeTab) {
            "overview" -> OverviewTab()
            "users" -> UsersTab()
            "analytics" -> AnalyticsTab()
            "system" -> SystemTab()
        }
    }
}

val OverviewTab = FC<Props> {
    div {
        className = "space-y-8"
        
        // Quick Stats
        div {
            className = "grid grid-cols-1 md:grid-cols-4 gap-6"
            
            // Total Users
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-blue-100 rounded-lg"
                        span {
                            className = "text-2xl text-blue-600"
                            +"👥"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"Total Users"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"1,247"
                        }
                    }
                }
            }
            
            // Active Users
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-green-100 rounded-lg"
                        span {
                            className = "text-2xl text-green-600"
                            +"🟢"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"Active Users"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"892"
                        }
                    }
                }
            }
            
            // New Users This Month
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-purple-100 rounded-lg"
                        span {
                            className = "text-2xl text-purple-600"
                            +"📈"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"New This Month"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"156"
                        }
                    }
                }
            }
            
            // System Health
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-green-100 rounded-lg"
                        span {
                            className = "text-2xl text-green-600"
                            +"💚"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"System Health"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"98%"
                        }
                    }
                }
            }
        }
        
        // Recent Activity
        div {
            className = "bg-white rounded-lg shadow-sm border border-gray-200"
            div {
                className = "px-6 py-4 border-b border-gray-200"
                h2 {
                    className = "text-lg font-semibold text-gray-900"
                    +"Recent Activity"
                }
            }
            div {
                className = "p-6"
                div {
                    className = "space-y-4"
                    div {
                        className = "flex items-center p-4 bg-gray-50 rounded-lg"
                        div {
                            className = "w-2 h-2 bg-blue-500 rounded-full"
                        }
                        div {
                            className = "ml-3 flex-1"
                            p {
                                className = "text-sm font-medium text-gray-900"
                                +"New user registration: john.doe@university.edu"
                            }
                            p {
                                className = "text-xs text-gray-500"
                                +"2 minutes ago"
                            }
                        }
                    }
                    div {
                        className = "flex items-center p-4 bg-gray-50 rounded-lg"
                        div {
                            className = "w-2 h-2 bg-green-500 rounded-full"
                        }
                        div {
                            className = "ml-3 flex-1"
                            p {
                                className = "text-sm font-medium text-gray-900"
                                +"System backup completed successfully"
                            }
                            p {
                                className = "text-xs text-gray-500"
                                +"15 minutes ago"
                            }
                        }
                    }
                    div {
                        className = "flex items-center p-4 bg-gray-50 rounded-lg"
                        div {
                            className = "w-2 h-2 bg-yellow-500 rounded-full"
                        }
                        div {
                            className = "ml-3 flex-1"
                            p {
                                className = "text-sm font-medium text-gray-900"
                                +"API rate limit warning: 85% of daily limit used"
                            }
                            p {
                                className = "text-xs text-gray-500"
                                +"1 hour ago"
                            }
                        }
                    }
                }
            }
        }
    }
}

val UsersTab = FC<Props> {
    div {
        className = "space-y-6"
        
        // Search and Filters
        div {
            className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
            div {
                className = "flex flex-col md:flex-row gap-4"
                div {
                    className = "flex-1"
                    input {
                        type = InputType.text
                        placeholder = "Search users by name or email..."
                        className = "w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                        value = searchTerm
                        onChange = { event -> setSearchTerm(event.target.value) }
                    }
                }
                select {
                    className = "px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                    value = selectedUser
                    onChange = { event -> setSelectedUser(event.target.value) }
                    
                    option {
                        value = ""
                        +"All Users"
                    }
                    option {
                        value = "active"
                        +"Active Users"
                    }
                    option {
                        value = "inactive"
                        +"Inactive Users"
                    }
                    option {
                        value = "admin"
                        +"Admin Users"
                    }
                }
                button {
                    className = "px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
                    onClick = { /* TODO: Implement user creation */ }
                    +"Add User"
                }
            }
        }
        
        // Users Table
        div {
            className = "bg-white rounded-lg shadow-sm border border-gray-200"
            div {
                className = "overflow-x-auto"
                table {
                    className = "min-w-full divide-y divide-gray-200"
                    thead {
                        className = "bg-gray-50"
                        tr {
                            th {
                                className = "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                +"User"
                            }
                            th {
                                className = "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                +"Status"
                            }
                            th {
                                className = "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                +"Role"
                            }
                            th {
                                className = "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                +"Last Active"
                            }
                            th {
                                className = "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                                +"Actions"
                            }
                        }
                    }
                    tbody {
                        className = "bg-white divide-y divide-gray-200"
                        
                        // Sample user rows
                        tr {
                            td {
                                className = "px-6 py-4 whitespace-nowrap"
                                div {
                                    className = "flex items-center"
                                    div {
                                        className = "w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center"
                                        span {
                                            className = "text-sm font-medium text-gray-700"
                                            +"JD"
                                        }
                                    }
                                    div {
                                        className = "ml-4"
                                        div {
                                            className = "text-sm font-medium text-gray-900"
                                            +"John Doe"
                                        }
                                        div {
                                            className = "text-sm text-gray-500"
                                            +"john.doe@university.edu"
                                        }
                                    }
                                }
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap"
                                span {
                                    className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800"
                                    +"Active"
                                }
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                                +"Student"
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                                +"2 hours ago"
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap text-sm font-medium"
                                div {
                                    className = "flex space-x-2"
                                    button {
                                        className = "text-primary-600 hover:text-primary-900"
                                        onClick = { /* TODO: Edit user */ }
                                        +"Edit"
                                    }
                                    button {
                                        className = "text-red-600 hover:text-red-900"
                                        onClick = { /* TODO: Delete user */ }
                                        +"Delete"
                                    }
                                }
                            }
                        }
                        
                        // More sample users...
                        tr {
                            td {
                                className = "px-6 py-4 whitespace-nowrap"
                                div {
                                    className = "flex items-center"
                                    div {
                                        className = "w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center"
                                        span {
                                            className = "text-sm font-medium text-gray-700"
                                            +"JS"
                                        }
                                    }
                                    div {
                                        className = "ml-4"
                                        div {
                                            className = "text-sm font-medium text-gray-900"
                                            +"Jane Smith"
                                        }
                                        div {
                                            className = "text-sm text-gray-500"
                                            +"jane.smith@university.edu"
                                        }
                                    }
                                }
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap"
                                span {
                                    className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800"
                                    +"Inactive"
                                }
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                                +"Student"
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                                +"1 day ago"
                            }
                            td {
                                className = "px-6 py-4 whitespace-nowrap text-sm font-medium"
                                div {
                                    className = "flex space-x-2"
                                    button {
                                        className = "text-primary-600 hover:text-primary-900"
                                        onClick = { /* TODO: Edit user */ }
                                        +"Edit"
                                    }
                                    button {
                                        className = "text-red-600 hover:text-red-900"
                                        onClick = { /* TODO: Delete user */ }
                                        +"Delete"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val AnalyticsTab = FC<Props> {
    div {
        className = "space-y-6"
        
        // Analytics Overview
        div {
            className = "grid grid-cols-1 md:grid-cols-2 gap-6"
            
            // User Growth Chart
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                h3 {
                    className = "text-lg font-semibold text-gray-900 mb-4"
                    +"User Growth"
                }
                div {
                    className = "h-64 bg-gray-100 rounded flex items-center justify-center"
                    p {
                        className = "text-gray-500"
                        +"Chart placeholder - User growth over time"
                    }
                }
            }
            
            // Usage Statistics
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                h3 {
                    className = "text-lg font-semibold text-gray-900 mb-4"
                    +"Usage Statistics"
                }
                div {
                    className = "space-y-4"
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Daily Active Users"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"892"
                        }
                    }
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Tasks Created Today"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"1,247"
                        }
                    }
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"API Calls Today"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"45,892"
                        }
                    }
                }
            }
        }
    }
}

val SystemTab = FC<Props> {
    div {
        className = "space-y-6"
        
        // System Status
        div {
            className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
            h3 {
                className = "text-lg font-semibold text-gray-900 mb-4"
                +"System Status"
            }
            div {
                className = "grid grid-cols-1 md:grid-cols-3 gap-4"
                div {
                    className = "p-4 bg-green-50 rounded-lg"
                    div {
                        className = "flex items-center"
                        div {
                            className = "w-2 h-2 bg-green-500 rounded-full"
                        }
                        span {
                            className = "ml-2 text-sm font-medium text-green-800"
                            +"Database: Online"
                        }
                    }
                }
                div {
                    className = "p-4 bg-green-50 rounded-lg"
                    div {
                        className = "flex items-center"
                        div {
                            className = "w-2 h-2 bg-green-500 rounded-full"
                        }
                        span {
                            className = "ml-2 text-sm font-medium text-green-800"
                            +"API: Operational"
                        }
                    }
                }
                div {
                    className = "p-4 bg-yellow-50 rounded-lg"
                    div {
                        className = "flex items-center"
                        div {
                            className = "w-2 h-2 bg-yellow-500 rounded-full"
                        }
                        span {
                            className = "ml-2 text-sm font-medium text-yellow-800"
                            +"Storage: 85% Full"
                        }
                    }
                }
            }
        }
        
        // System Metrics
        div {
            className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
            h3 {
                className = "text-lg font-semibold text-gray-900 mb-4"
                +"System Metrics"
            }
            div {
                className = "grid grid-cols-1 md:grid-cols-2 gap-6"
                div {
                    className = "space-y-4"
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"CPU Usage"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"23%"
                        }
                    }
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Memory Usage"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"67%"
                        }
                    }
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Disk Usage"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"85%"
                        }
                    }
                }
                div {
                    className = "space-y-4"
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Network In"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"2.4 GB/s"
                        }
                    }
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Network Out"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"1.8 GB/s"
                        }
                    }
                    div {
                        className = "flex justify-between items-center"
                        span {
                            className = "text-sm text-gray-600"
                            +"Uptime"
                        }
                        span {
                            className = "text-sm font-medium text-gray-900"
                            +"99.9%"
                        }
                    }
                }
            }
        }
    }
}