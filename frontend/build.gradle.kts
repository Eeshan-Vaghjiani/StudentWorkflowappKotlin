
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "main.js"
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                
                // React and React DOM
                implementation(npm("react", "18.2.0"))
                implementation(npm("react-dom", "18.2.0"))
                
                // React Router
                implementation(npm("react-router-dom", "6.21.1"))
                
                // State Management
                implementation(npm("zustand", "4.4.7"))
                
                // UI Components and Styling
                implementation(npm("tailwindcss", "3.4.0"))
                implementation(npm("autoprefixer", "10.4.16"))
                implementation(npm("postcss", "8.4.32"))
                
                // HTTP Client
                implementation(npm("axios", "1.6.2"))
                
                // Date handling
                implementation(npm("date-fns", "2.30.0"))
                
                // Form handling
                implementation(npm("react-hook-form", "7.48.2"))
                
                // Icons
                implementation(npm("lucide-react", "0.294.0"))
                
                // Charts (for admin dashboard)
                implementation(npm("recharts", "2.8.0"))
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }
    }
}
