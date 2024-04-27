plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("application")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("invirt.examples.hotwire.ApplicationKt")
}

dependencies {
    val invirtVersion: String by project
    val http4kVersion: String by project

    implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
    implementation("io.resoluteworks:invirt-http4k")
    implementation("io.resoluteworks:invirt-utils")

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-template-pebble")

    implementation("io.resoluteworks:validk:1.2.7")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

tasks.named<JavaExec>("run") {
    doFirst {
        jvmArgs = listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
    }
}
