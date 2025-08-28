
plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.studentworkflow.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))
    // Ktor dependencies
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    // Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
    // H2 database for local development
    implementation("com.h2database:h2:2.2.224")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    // Google Calendar
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.11.0")
    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")
    // JWT Authentication
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    // QR Code generation (for 2FA)
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")
    // TOTP for 2FA
    implementation("de.taimos:totp:1.0")
    
    // Testing dependencies
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.postgresql:postgresql:42.7.1")
}

tasks.test {
    useJUnitPlatform()
}
