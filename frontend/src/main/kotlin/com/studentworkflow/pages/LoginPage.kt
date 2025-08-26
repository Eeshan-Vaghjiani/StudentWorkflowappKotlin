package com.studentworkflow.pages

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import react.router.dom.useNavigate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

external interface LoginPageProps : Props

val LoginPage = FC<LoginPageProps> { props ->
    val navigate = useNavigate()
    val (email, setEmail) = useState("")
    val (password, setPassword) = useState("")
    val (isLoading, setIsLoading) = useState(false)
    val (error, setError) = useState("")
    
    val handleSubmit = { event: dynamic ->
        event.preventDefault()
        setIsLoading(true)
        setError("")
        
        // TODO: Implement actual login logic
        // For now, simulate API call
        setTimeout({
            if (email.isNotEmpty() && password.isNotEmpty()) {
                navigate("/dashboard")
            } else {
                setError("Please fill in all fields")
            }
            setIsLoading(false)
        }, 1000)
    }
    
    div {
        className = "min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8"
        div {
            className = "max-w-md w-full space-y-8"
            
            div {
                className = "text-center"
                h1 {
                    className = "text-3xl font-bold text-gray-900"
                    +"Sign in to your account"
                }
                p {
                    className = "mt-2 text-sm text-gray-600"
                    +"Or "
                    Link {
                        to = "/register"
                        className = "font-medium text-primary-600 hover:text-primary-500"
                        +"create a new account"
                    }
                }
            }
            
            form {
                className = "mt-8 space-y-6"
                onSubmit = handleSubmit
                
                if (error.isNotEmpty()) {
                    div {
                        className = "bg-red-50 border border-red-200 rounded-md p-4"
                        div {
                            className = "flex"
                            div {
                                className = "flex-shrink-0"
                                span {
                                    className = "text-red-400"
                                    +"⚠"
                                }
                            }
                            div {
                                className = "ml-3"
                                p {
                                    className = "text-sm text-red-800"
                                    +error
                                }
                            }
                        }
                    }
                }
                
                div {
                    className = "space-y-4"
                    
                    div {
                        label {
                            htmlFor = "email"
                            className = "block text-sm font-medium text-gray-700"
                            +"Email address"
                        }
                        input {
                            id = "email"
                            name = "email"
                            type = InputType.email
                            required = true
                            className = "mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500 focus:z-10 sm:text-sm"
                            placeholder = "Enter your email"
                            value = email
                            onChange = { event -> setEmail(event.target.value) }
                        }
                    }
                    
                    div {
                        label {
                            htmlFor = "password"
                            className = "block text-sm font-medium text-gray-700"
                            +"Password"
                        }
                        input {
                            id = "password"
                            name = "password"
                            type = InputType.password
                            required = true
                            className = "mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500 focus:z-10 sm:text-sm"
                            placeholder = "Enter your password"
                            value = password
                            onChange = { event -> setPassword(event.target.value) }
                        }
                    }
                }
                
                div {
                    className = "flex items-center justify-between"
                    div {
                        className = "flex items-center"
                        input {
                            id = "remember-me"
                            name = "remember-me"
                            type = InputType.checkbox
                            className = "h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                        }
                        label {
                            htmlFor = "remember-me"
                            className = "ml-2 block text-sm text-gray-900"
                            +"Remember me"
                        }
                    }
                    
                    div {
                        className = "text-sm"
                        Link {
                            to = "/forgot-password"
                            className = "font-medium text-primary-600 hover:text-primary-500"
                            +"Forgot your password?"
                        }
                    }
                }
                
                button {
                    type = ButtonType.submit
                    disabled = isLoading
                    className = "group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    +if (isLoading) "Signing in..." else "Sign in"
                }
            }
            
            div {
                className = "mt-6"
                div {
                    className = "relative"
                    div {
                        className = "absolute inset-0 flex items-center"
                        div {
                            className = "w-full border-t border-gray-300"
                        }
                    }
                    div {
                        className = "relative flex justify-center text-sm"
                        span {
                            className = "px-2 bg-gray-50 text-gray-500"
                            +"Or continue with"
                        }
                    }
                }
                
                div {
                    className = "mt-6 grid grid-cols-1 gap-3"
                    button {
                        type = ButtonType.button
                        className = "w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
                        onClick = { /* TODO: Implement Google OAuth */ }
                        +"Google"
                    }
                }
            }
        }
    }
}
