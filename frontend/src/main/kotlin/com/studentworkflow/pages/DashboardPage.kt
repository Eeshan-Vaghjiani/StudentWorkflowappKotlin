package com.studentworkflow.pages

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.ul
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.section
import react.router.dom.Link
import react.router.dom.useNavigate

external interface DashboardPageProps : Props

val DashboardPage = FC<DashboardPageProps> { props ->
    val navigate = useNavigate()
    
    div {
        className = "max-w-7xl mx-auto"
        
        // Welcome Section
        section {
            className = "mb-8"
            h1 {
                className = "text-3xl font-bold text-gray-900 mb-2"
                +"Welcome back, Student!"
            }
            p {
                className = "text-gray-600"
                +"Here's what's happening with your academic workflow today."
            }
        }
        
        // Quick Stats
        section {
            className = "grid grid-cols-1 md:grid-cols-4 gap-6 mb-8"
            
            // Tasks Due Today
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-red-100 rounded-lg"
                        span {
                            className = "text-2xl text-red-600"
                            +"📅"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"Tasks Due Today"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"3"
                        }
                    }
                }
            }
            
            // Completed Tasks
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-green-100 rounded-lg"
                        span {
                            className = "text-2xl text-green-600"
                            +"✅"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"Completed This Week"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"12"
                        }
                    }
                }
            }
            
            // Upcoming Deadlines
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-yellow-100 rounded-lg"
                        span {
                            className = "text-2xl text-yellow-600"
                            +"⚠️"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"Upcoming Deadlines"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"5"
                        }
                    }
                }
            }
            
            // Study Hours
            div {
                className = "bg-white p-6 rounded-lg shadow-sm border border-gray-200"
                div {
                    className = "flex items-center"
                    div {
                        className = "p-2 bg-blue-100 rounded-lg"
                        span {
                            className = "text-2xl text-blue-600"
                            +"⏰"
                        }
                    }
                    div {
                        className = "ml-4"
                        p {
                            className = "text-sm font-medium text-gray-600"
                            +"Study Hours"
                        }
                        p {
                            className = "text-2xl font-semibold text-gray-900"
                            +"24h"
                        }
                    }
                }
            }
        }
        
        // Main Content Grid
        div {
            className = "grid grid-cols-1 lg:grid-cols-3 gap-8"
            
            // Tasks Section
            div {
                className = "lg:col-span-2"
                div {
                    className = "bg-white rounded-lg shadow-sm border border-gray-200"
                    div {
                        className = "px-6 py-4 border-b border-gray-200"
                        div {
                            className = "flex items-center justify-between"
                            h2 {
                                className = "text-lg font-semibold text-gray-900"
                                +"Today's Tasks"
                            }
                            button {
                                className = "text-sm text-primary-600 hover:text-primary-700 font-medium"
                                onClick = { navigate("/workflow") }
                                +"View All"
                            }
                        }
                    }
                    
                    div {
                        className = "p-6"
                        ul {
                            className = "space-y-4"
                            
                            // Task 1
                            li {
                                className = "flex items-center p-4 bg-gray-50 rounded-lg"
                                div {
                                    className = "flex-shrink-0 w-4 h-4 border-2 border-gray-300 rounded"
                                }
                                div {
                                    className = "ml-3 flex-1"
                                    h3 {
                                        className = "text-sm font-medium text-gray-900"
                                        +"Complete Research Paper Outline"
                                    }
                                    p {
                                        className = "text-sm text-gray-500"
                                        +"Due: Today at 5:00 PM"
                                    }
                                }
                                span {
                                    className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800"
                                    +"High Priority"
                                }
                            }
                            
                            // Task 2
                            li {
                                className = "flex items-center p-4 bg-gray-50 rounded-lg"
                                div {
                                    className = "flex-shrink-0 w-4 h-4 border-2 border-gray-300 rounded"
                                }
                                div {
                                    className = "ml-3 flex-1"
                                    h3 {
                                        className = "text-sm font-medium text-gray-900"
                                        +"Review Calculus Problems"
                                    }
                                    p {
                                        className = "text-sm text-gray-500"
                                        +"Due: Tomorrow at 9:00 AM"
                                    }
                                }
                                span {
                                    className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800"
                                    +"Medium Priority"
                                }
                            }
                            
                            // Task 3
                            li {
                                className = "flex items-center p-4 bg-gray-50 rounded-lg"
                                div {
                                    className = "flex-shrink-0 w-4 h-4 border-2 border-gray-300 rounded"
                                }
                                div {
                                    className = "ml-3 flex-1"
                                    h3 {
                                        className = "text-sm font-medium text-gray-900"
                                        +"Prepare for Group Presentation"
                                    }
                                    p {
                                        className = "text-sm text-gray-500"
                                        +"Due: Friday at 2:00 PM"
                                    }
                                }
                                span {
                                    className = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800"
                                    +"Low Priority"
                                }
                            }
                        }
                    }
                }
            }
            
            // Sidebar
            div {
                className = "space-y-6"
                
                // Quick Actions
                div {
                    className = "bg-white rounded-lg shadow-sm border border-gray-200 p-6"
                    h3 {
                        className = "text-lg font-semibold text-gray-900 mb-4"
                        +"Quick Actions"
                    }
                    div {
                        className = "space-y-3"
                        button {
                            className = "w-full flex items-center justify-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                            onClick = { navigate("/workflow") }
                            +"Add New Task"
                        }
                        button {
                            className = "w-full flex items-center justify-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                            onClick = { navigate("/calendar") }
                            +"View Calendar"
                        }
                        button {
                            className = "w-full flex items-center justify-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                            onClick = { /* TODO: Implement AI assistant */ }
                            +"AI Assistant"
                        }
                    }
                }
                
                // Upcoming Events
                div {
                    className = "bg-white rounded-lg shadow-sm border border-gray-200 p-6"
                    h3 {
                        className = "text-lg font-semibold text-gray-900 mb-4"
                        +"Upcoming Events"
                    }
                    div {
                        className = "space-y-3"
                        div {
                            className = "flex items-center p-3 bg-blue-50 rounded-lg"
                            div {
                                className = "w-2 h-2 bg-blue-500 rounded-full"
                            }
                            div {
                                className = "ml-3"
                                p {
                                    className = "text-sm font-medium text-gray-900"
                                    +"Study Group Meeting"
                                }
                                p {
                                    className = "text-xs text-gray-500"
                                    +"Tomorrow at 3:00 PM"
                                }
                            }
                        }
                        div {
                            className = "flex items-center p-3 bg-purple-50 rounded-lg"
                            div {
                                className = "w-2 h-2 bg-purple-500 rounded-full"
                            }
                            div {
                                className = "ml-3"
                                p {
                                    className = "text-sm font-medium text-gray-900"
                                    +"Office Hours"
                                }
                                p {
                                    className = "text-xs text-gray-500"
                                    +"Wednesday at 10:00 AM"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}