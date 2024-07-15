plugins {
    kotlin("jvm")
    application
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

    implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
    implementation("io.resoluteworks:invirt-core")

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-template-pebble")

    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
}

application {
    mainClass.set("examples.quickstart.ApplicationKt")
}
