
plugins {
    kotlin("multiplatform") version "1.9.24" apply false
    kotlin("jvm") version "1.9.24" apply false
    kotlin("js") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
    id("io.ktor.plugin") version "2.3.8" apply false
    id("org.jetbrains.compose") version "1.5.10" apply false // For potential future use
}



allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
