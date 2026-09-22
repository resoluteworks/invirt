import java.util.Properties

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val rootProperties: Properties = Properties().apply {
    rootDir.parentFile.resolve("gradle.properties").inputStream().use { load(it) }
}

val kotlinVersion: String = rootProperties.getProperty("kotlinVersion")
    ?: error("kotlinVersion missing from gradle.properties")
val jacksonVersion: String = rootProperties.getProperty("jacksonVersion")
    ?: error("jacksonVersion missing from gradle.properties")

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("org.jacoco:org.jacoco.core:0.8.15")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")
    implementation("org.jmailen.gradle:kotlinter-gradle:5.7.0")
    implementation("com.github.nbaztec:coveralls-jacoco-gradle-plugin:1.2.20")

    constraints {
        // Dokka's dokka-core resolves Jackson through jackson-bom 2.15.3, a line that trails its security
        // releases, and Dependabot reads the resolved graph. The plugin classpath is held at the same Jackson
        // the modules build on, so the build carries one Jackson line rather than two. jackson-annotations is
        // left to the bom: it carries no patch component since 2.20, so jacksonVersion does not resolve for it.
        implementation("com.fasterxml.jackson:jackson-bom:$jacksonVersion")
        implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

        // The Coveralls plugin declares JGit 5.10.0, which trails the fixes for the advisories against it.
        // 5.13.5 is the head of the same major line, so the lib and revwalk APIs the plugin calls are unchanged.
        implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.5.202508271544-r")
    }
}
