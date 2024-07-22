plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val kotlinVersion = "2.0.0"

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("org.jacoco:org.jacoco.core:0.8.11")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    implementation("org.jmailen.gradle:kotlinter-gradle:4.3.0")
    implementation("com.gradleup.nmcp:com.gradleup.nmcp.gradle.plugin:0.0.9")
}
