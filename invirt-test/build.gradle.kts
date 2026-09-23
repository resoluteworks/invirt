plugins {
    id("common-conventions")
    id("publish-conventions")
    id("test-conventions")
    id("test-library-conventions")
}

description = "Provides test helpers for building form requests and asserting view responses, validation errors, redirects and cookies."

dependencies {
    val http4kVersion = providers.gradleProperty("http4kVersion").get()
    val validkVersion = providers.gradleProperty("validkVersion").get()

    implementation(project(":invirt-core"))

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-config")
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-multipart")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-template-pebble")

    implementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
    implementation("works.resolute:validk:${validkVersion}")
}
