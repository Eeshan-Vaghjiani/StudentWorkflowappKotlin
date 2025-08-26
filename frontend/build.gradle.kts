
plugins {
    kotlin("js")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "main.js"
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(npm("react", "18.2.0"))
    implementation(npm("react-dom", "18.2.0"))
    implementation(npm("react-router-dom", "6.21.1"))
}
