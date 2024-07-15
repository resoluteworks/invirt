plugins {
    kotlin("jvm")
    id("com.gorylenko.gradle-git-properties")
    id("application")
    id("com.github.johnrengelman.shadow")
    id("com.github.node-gradle.node")
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "invirt.examples"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    val kotlinVersion: String by project
    val invirtVersion: String by project
    val http4kVersion: String by project
    val validkVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")

    implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
    implementation("io.resoluteworks:invirt-core")
    implementation("io.resoluteworks:invirt-utils")
    implementation("io.resoluteworks:validk:${validkVersion}")

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-template-pebble")

    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}
