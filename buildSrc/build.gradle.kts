import java.util.Properties

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val kotlinVersion: String = Properties().apply {
    rootDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
}.getProperty("kotlinVersion") ?: error("kotlinVersion missing from gradle.properties")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("org.jacoco:org.jacoco.core:0.8.14")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")
    implementation("org.jmailen.gradle:kotlinter-gradle:5.5.0")
}
