
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }
        val jvmMain by getting
        val jsMain by getting
    }
}
