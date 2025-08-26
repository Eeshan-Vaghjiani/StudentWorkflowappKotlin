
plugins {
    kotlin("multiplatform") version "1.9.20" apply false
    kotlin("jvm") version "1.9.20" apply false
    id("io.ktor.plugin") version "2.3.6" apply false
    id("org.jetbrains.compose") version "1.5.10" apply false // For potential future use
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
