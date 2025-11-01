plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.loginandregistration"
    compileSdk = 34 // use latest stable, not 36 yet

    defaultConfig {
        applicationId = "com.teamsync.collaboration"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // App metadata
        setProperty("archivesBaseName", "TeamSync-v$versionName")
        
        // Read API keys from local.properties
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        
        // Gemini API Key for AI Assistant
        val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: "your_gemini_api_key_here"
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    signingConfigs {
        create("release") {
            // For production, use keystore file
            // Store these values in local.properties or environment variables
            // DO NOT commit keystore credentials to version control
            
            // Example configuration (uncomment and configure for actual release):
            // storeFile = file("path/to/your/keystore.jks")
            // storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            // keyAlias = System.getenv("KEY_ALIAS") ?: ""
            // keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Uncomment when signing config is set up:
            // signingConfig = signingConfigs.getByName("release")
            
            // Enable R8 full mode for better optimization
            isDebuggable = false
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            // Removed applicationIdSuffix to match google-services.json
            // applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    
    // Disable file-based caching to prevent locking issues
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        incremental = false
    }
}

dependencies {
    // Firebase BOM – manages versions
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // AndroidX + Material
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // ViewModel and Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Firebase Services (versions managed by BOM)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    
    // Image Loading - Coil (Kotlin-first, modern)
    implementation("io.coil-kt:coil:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    
    // Calendar View
    implementation("com.kizitonwose.calendar:view:2.6.1")
    
    // Work Manager (for background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // OkHttp for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // ExifInterface for image rotation handling
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Compose Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.5")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    debugImplementation("androidx.fragment:fragment-testing-manifest:1.8.5")
}
