plugins {
    kotlin("jvm")
    id("com.gorylenko.gradle-git-properties")
    id("application")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    val kotlinVersion: String by project
    val invirtVersion: String by project
    val http4kVersion: String by project
    val validkVersion: String by project
    val awaitilityVersion: String by project
    val kotlinLoggingVersion: String by project

    // Core, minimal dependencies
    implementation(platform("dev.invirt:invirt-bom:${invirtVersion}"))
    implementation("dev.invirt:invirt-core")
    implementation("dev.invirt:invirt-utils")
    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-config")
    implementation("org.http4k:http4k-template-pebble")

    // Additional libraries for specific use cases
    implementation("dev.invirt:invirt-data")
    implementation("dev.invirt:invirt-security")
    implementation("io.resoluteworks:validk:${validkVersion}")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:${kotlinLoggingVersion}")

    // Testing
    val kotestVersion: String by project
    val mockkVersion: String by project
    testImplementation("dev.invirt:invirt-test")
    testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
    testImplementation("io.kotest:kotest-property:${kotestVersion}")
    testImplementation("io.kotest.extensions:kotest-extensions-clock:1.0.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
}

tasks.named<JavaExec>("run") {
    debugOptions {
        enabled = true
        suspend = false
    }
}
