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
    implementation("ch.qos.logback:logback-classic:1.6.3")

    constraints {
        // kafka-clients and kotest-extensions-testcontainers pin lz4-java 1.10.x, which can crash the JVM on invalid byte ranges (CVE-2026-59949)
        implementation("at.yawk.lz4:lz4-java:1.11.3")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

afterEvaluate {
    tasks["test"].dependsOn(tasks["lintKotlin"])
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("../docs/dokka/${project.name}"))
        suppressInheritedMembers.set(true)
    }
}

// Dokka pulls its documentation generator onto its own dokka* configurations, at the Jackson and jsoup
// versions baked into the plugin. Both trail their security releases, and Dependabot reads the resolved
// graph, so the generator runtime is held at the Jackson the modules build on and at a current jsoup.
// jackson-annotations is left to the bom: it carries no patch component since 2.20, so jacksonVersion
// does not resolve for it.
val jacksonVersion: String by project

configurations.matching { it.name.startsWith("dokka") }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group.startsWith("com.fasterxml.jackson") && requested.name != "jackson-annotations") {
            useVersion(jacksonVersion)
            because("Dokka's generator runtime pins Jackson 2.15.3, which lags its security releases")
        }
        if (requested.group == "org.jsoup") {
            useVersion("1.23.2")
            because("Dokka's generator runtime pins jsoup 1.16.1, which lags its security releases")
        }
    }
}
