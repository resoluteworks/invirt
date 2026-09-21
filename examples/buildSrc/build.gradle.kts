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
}
