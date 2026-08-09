plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val kotlinVersion = "2.3.21"

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}
