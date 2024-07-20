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

    // Core, minimal dependencies
    implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
    implementation("io.resoluteworks:invirt-core")
    implementation("io.resoluteworks:invirt-utils")
    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-template-pebble")

    // Additional libraries for specific use cases
    implementation("io.resoluteworks:invirt-data")
    implementation("io.resoluteworks:validk:${validkVersion}")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
}
