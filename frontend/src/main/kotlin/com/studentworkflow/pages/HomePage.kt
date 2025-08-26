package com.studentworkflow.pages

import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.section
import react.dom.html.ReactHTML.span
import react.router.dom.useNavigate
import react.router.dom.Link

external interface HomePageProps : Props

val HomePage = FC<HomePageProps> { props ->
    val navigate = useNavigate()
    
    div {
        className = "max-w-7xl mx-auto"
        
        // Hero Section
        section {
            className = "text-center py-20"
            h1 {
                className = "text-4xl md:text-6xl font-bold text-gray-900 mb-6"
                +"Streamline Your Academic Workflow"
            }
            p {
                className = "text-xl text-gray-600 mb-8 max-w-3xl mx-auto"
                +"A comprehensive platform designed to help university students manage their academic tasks, deadlines, and schedules efficiently."
            }
            div {
                className = "flex flex-col sm:flex-row gap-4 justify-center"
                button {
                    className = "bg-primary-600 hover:bg-primary-700 text-white px-8 py-3 rounded-lg text-lg font-semibold"
                    onClick = { navigate("/register") }
                    +"Get Started Free"
                }
                button {
                    className = "border border-gray-300 hover:border-gray-400 text-gray-700 px-8 py-3 rounded-lg text-lg font-semibold"
                    onClick = { navigate("/login") }
                    +"Sign In"
                }
            }
        }
        
        // Features Section
        section {
            className = "py-20 bg-white"
            div {
                className = "max-w-7xl mx-auto px-4"
                h2 {
                    className = "text-3xl font-bold text-center text-gray-900 mb-16"
                    +"Why Choose Student Workflow?"
                }
                
                div {
                    className = "grid md:grid-cols-3 gap-8"
                    
                    // Feature 1
                    div {
                        className = "text-center p-6"
                        div {
                            className = "w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4"
                            span {
                                className = "text-2xl text-primary-600"
                                +"📚"
                            }
                        }
                        h3 {
                            className = "text-xl font-semibold text-gray-900 mb-2"
                            +"Task Management"
                        }
                        p {
                            className = "text-gray-600"
                            +"Organize your assignments, projects, and deadlines in one centralized location."
                        }
                    }
                    
                    // Feature 2
                    div {
                        className = "text-center p-6"
                        div {
                            className = "w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4"
                            span {
                                className = "text-2xl text-primary-600"
                                +"📅"
                            }
                        }
                        h3 {
                            className = "text-xl font-semibold text-gray-900 mb-2"
                            +"Calendar Integration"
                        }
                        p {
                            className = "text-gray-600"
                            +"Sync with Google Calendar to keep your academic and personal schedules in sync."
                        }
                    }
                    
                    // Feature 3
                    div {
                        className = "text-center p-6"
                        div {
                            className = "w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4"
                            span {
                                className = "text-2xl text-primary-600"
                                +"🤖"
                            }
                        }
                        h3 {
                            className = "text-xl font-semibold text-gray-900 mb-2"
                            +"AI-Powered Insights"
                        }
                        p {
                            className = "text-gray-600"
                            +"Get intelligent suggestions and insights to optimize your study schedule and productivity."
                        }
                    }
                }
            }
        }
        
        // CTA Section
        section {
            className = "py-20 bg-primary-50"
            div {
                className = "text-center max-w-4xl mx-auto px-4"
                h2 {
                    className = "text-3xl font-bold text-gray-900 mb-4"
                    +"Ready to Transform Your Academic Experience?"
                }
                p {
                    className = "text-xl text-gray-600 mb-8"
                    +"Join thousands of students who have already improved their productivity and organization."
                }
                Link {
                    to = "/register"
                    className = "inline-block bg-primary-600 hover:bg-primary-700 text-white px-8 py-3 rounded-lg text-lg font-semibold"
                    +"Start Your Free Trial"
                }
            }
        }
    }
}