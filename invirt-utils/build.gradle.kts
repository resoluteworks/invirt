plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

description = "A small library of zero-dependency Kotlin utilities for strings, dates, currency, IDs, enums, files, threads and classpath resources."

dependencies {
    implementation("com.github.f4b6a3:uuid-creator:6.1.1")
    implementation("commons-codec:commons-codec:1.19.0")
}
