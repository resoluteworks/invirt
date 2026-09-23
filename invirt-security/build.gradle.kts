plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

description = "Provides authentication components, including a custom http4k filter, for transparently authenticating HTTP requests and exposing the current Principal."

dependencies {
    val http4kVersion = providers.gradleProperty("http4kVersion").get()

    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))
    implementation(project(":invirt-core"))

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")

    testImplementation(project(":invirt-test"))
    testImplementation(project(":invirt-security-test"))
    testImplementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
}
