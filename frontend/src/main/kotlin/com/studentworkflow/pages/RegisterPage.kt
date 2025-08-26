package com.studentworkflow.pages

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.option
import react.router.dom.Link
import react.router.dom.useNavigate

external interface RegisterPageProps : Props

val RegisterPage = FC<RegisterPageProps> { props ->
    val navigate = useNavigate()
    val (formData, setFormData) = useState(RegistrationForm())
    val (isLoading, setIsLoading) = useState(false)
    val (error, setError) = useState("")
    
    val handleInputChange = { field: String, value: String ->
        setFormData { it.copy(field to value) }
    }
    
    val handleSubmit = { event: dynamic ->
        event.preventDefault()
        setIsLoading(true)
        setError("")
        
        // TODO: Implement actual registration logic
        // For now, simulate API call
        setTimeout({
            if (formData.isValid()) {
                navigate("/dashboard")
            } else {
                setError("Please fill in all required fields")
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
                    +"Create your account"
                }
                p {
                    className = "mt-2 text-sm text-gray-600"
                    +"Already have an account? "
                    Link {
                        to = "/login"
                        className = "font-medium text-primary-600 hover:text-primary-500"
                        +"Sign in"
                    }
                }
            }
            
            form {
                className = "mt-8 space-y-6"
                onSubmit = handleSubmit
                
                if (error.isNotEmpty()) {
                    div {
                        className = "bg-red-50 border border-red-200 rounded-md p-4"
                        p {
                            className = "text-sm text-red-800"
                            +error
                        }
                    }
                }
                
                div {
                    className = "space-y-4"
                    
                    // Name fields
                    div {
                        className = "grid grid-cols-2 gap-4"
                        div {
                            label {
                                htmlFor = "firstName"
                                className = "block text-sm font-medium text-gray-700"
                                +"First name"
                            }
                            input {
                                id = "firstName"
                                name = "firstName"
                                type = InputType.text
                                required = true
                                className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                                value = formData.firstName
                                onChange = { event -> handleInputChange("firstName", event.target.value) }
                            }
                        }
                        div {
                            label {
                                htmlFor = "lastName"
                                className = "block text-sm font-medium text-gray-700"
                                +"Last name"
                            }
                            input {
                                id = "lastName"
                                name = "lastName"
                                type = InputType.text
                                required = true
                                className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                                value = formData.lastName
                                onChange = { event -> handleInputChange("lastName", event.target.value) }
                            }
                        }
                    }
                    
                    // Email
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
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                            value = formData.email
                            onChange = { event -> handleInputChange("email", event.target.value) }
                        }
                    }
                    
                    // University
                    div {
                        label {
                            htmlFor = "university"
                            className = "block text-sm font-medium text-gray-700"
                            +"University"
                        }
                        select {
                            id = "university"
                            name = "university"
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                            value = formData.university
                            onChange = { event -> handleInputChange("university", event.target.value) }
                            
                            option {
                                value = ""
                                +"Select your university"
                            }
                            option {
                                value = "stanford"
                                +"Stanford University"
                            }
                            option {
                                value = "mit"
                                +"Massachusetts Institute of Technology"
                            }
                            option {
                                value = "harvard"
                                +"Harvard University"
                            }
                            option {
                                value = "berkeley"
                                +"University of California, Berkeley"
                            }
                            option {
                                value = "other"
                                +"Other"
                            }
                        }
                    }
                    
                    // Study Level
                    div {
                        label {
                            htmlFor = "studyLevel"
                            className = "block text-sm font-medium text-gray-700"
                            +"Study Level"
                        }
                        select {
                            id = "studyLevel"
                            name = "studyLevel"
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                            value = formData.studyLevel
                            onChange = { event -> handleInputChange("studyLevel", event.target.value) }
                            
                            option {
                                value = ""
                                +"Select your study level"
                            }
                            option {
                                value = "undergraduate"
                                +"Undergraduate"
                            }
                            option {
                                value = "graduate"
                                +"Graduate"
                            }
                            option {
                                value = "phd"
                                +"PhD"
                            }
                        }
                    }
                    
                    // Password
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
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                            value = formData.password
                            onChange = { event -> handleInputChange("password", event.target.value) }
                        }
                    }
                    
                    // Confirm Password
                    div {
                        label {
                            htmlFor = "confirmPassword"
                            className = "block text-sm font-medium text-gray-700"
                            +"Confirm password"
                        }
                        input {
                            id = "confirmPassword"
                            name = "confirmPassword"
                            type = InputType.password
                            required = true
                            className = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                            value = formData.confirmPassword
                            onChange = { event -> handleInputChange("confirmPassword", event.target.value) }
                        }
                    }
                }
                
                button {
                    type = ButtonType.submit
                    disabled = isLoading
                    className = "group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    +if (isLoading) "Creating account..." else "Create account"
                }
                
                p {
                    className = "text-xs text-gray-500 text-center"
                    +"By creating an account, you agree to our Terms of Service and Privacy Policy."
                }
            }
        }
    }
}

data class RegistrationForm(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val university: String = "",
    val studyLevel: String = "",
    val password: String = "",
    val confirmPassword: String = ""
) {
    fun isValid(): Boolean {
        return firstName.isNotEmpty() && 
               lastName.isNotEmpty() && 
               email.isNotEmpty() && 
               university.isNotEmpty() && 
               studyLevel.isNotEmpty() && 
               password.isNotEmpty() && 
               password == confirmPassword
    }
}