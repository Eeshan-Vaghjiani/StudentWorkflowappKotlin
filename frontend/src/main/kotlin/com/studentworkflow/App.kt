package com.studentworkflow

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.main
import react.router.dom.BrowserRouter
import react.router.dom.Routes
import react.router.dom.Route
import react.router.dom.Link
import react.router.dom.useNavigate
import react.router.dom.useLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

external interface AppProps : Props

val App = FC<AppProps> { props ->
    BrowserRouter {
        div {
            className = "min-h-screen bg-gray-50"
            Header()
            main {
                className = "container mx-auto px-4 py-8"
                Routes {
                    Route {
                        path = "/"
                        element = HomePage.create()
                    }
                    Route {
                        path = "/login"
                        element = LoginPage.create()
                    }
                    Route {
                        path = "/register"
                        element = RegisterPage.create()
                    }
                    Route {
                        path = "/dashboard"
                        element = DashboardPage.create()
                    }
                    Route {
                        path = "/admin"
                        element = AdminDashboardPage.create()
                    }
                    Route {
                        path = "/workflow"
                        element = WorkflowPage.create()
                    }
                    Route {
                        path = "/calendar"
                        element = CalendarPage.create()
                    }
                }
            }
        }
    }
}

val Header = FC<Props> {
    val navigate = useNavigate()
    val location = useLocation()
    val isAuthenticated = useAuthState()
    
    nav {
        className = "bg-white shadow-sm border-b border-gray-200"
        div {
            className = "container mx-auto px-4"
            div {
                className = "flex justify-between items-center h-16"
                
                // Logo
                div {
                    className = "flex items-center"
                    h1 {
                        className = "text-xl font-bold text-gray-900 cursor-pointer"
                        onClick = { navigate("/") }
                        +"Student Workflow"
                    }
                }
                
                // Navigation Links
                div {
                    className = "hidden md:flex items-center space-x-8"
                    if (isAuthenticated) {
                        Link {
                            to = "/dashboard"
                            className = "text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium"
                            +"Dashboard"
                        }
                        Link {
                            to = "/workflow"
                            className = "text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium"
                            +"Workflow"
                        }
                        Link {
                            to = "/calendar"
                            className = "text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium"
                            +"Calendar"
                        }
                        if (isAuthenticated.isAdmin) {
                            Link {
                                to = "/admin"
                                className = "text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium"
                                +"Admin"
                            }
                        }
                    }
                }
                
                // Auth Buttons
                div {
                    className = "flex items-center space-x-4"
                    if (isAuthenticated) {
                        button {
                            className = "bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                            onClick = { 
                                // TODO: Implement logout
                                navigate("/login")
                            }
                            +"Logout"
                        }
                    } else {
                        Link {
                            to = "/login"
                            className = "text-gray-700 hover:text-primary-600 px-3 py-2 rounded-md text-sm font-medium"
                            +"Login"
                        }
                        Link {
                            to = "/register"
                            className = "bg-primary-600 hover:bg-primary-700 text-white px-4 py-2 rounded-md text-sm font-medium"
                            +"Sign Up"
                        }
                    }
                }
            }
        }
    }
}

// Custom hook for authentication state
fun useAuthState(): AuthState {
    // TODO: Implement proper authentication state management
    return AuthState(
        isAuthenticated = false,
        isAdmin = false,
        user = null
    )
}

data class AuthState(
    val isAuthenticated: Boolean,
    val isAdmin: Boolean,
    val user: User?
)
