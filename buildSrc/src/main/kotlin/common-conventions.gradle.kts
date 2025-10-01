plugins {
    kotlin("jvm")
    id("artifact-conventions")
    id("org.jmailen.kotlinter")
    id("org.jetbrains.dokka")
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
    val kotlinLoggingVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("io.github.oshai:kotlin-logging-jvm:${kotlinLoggingVersion}")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")
}

java {
    withJavadocJar()
    withSourcesJar()
}

afterEvaluate {
    tasks["test"].dependsOn(tasks["lintKotlin"])
}

tasks.dokkaHtml {
    outputDirectory.set(layout.projectDirectory.dir("../docs/dokka"))
    suppressInheritedMembers = true
}
