plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
    id("test-library-conventions")
}

description = "Provides Kotest Testcontainer helpers for running Kafka tests against a Redpanda container."

dependencies {
    val testContainersVersion = providers.gradleProperty("testContainersVersion").get()

    implementation(project(":invirt-utils"))
    implementation(project(":invirt-kafka"))

    implementation("org.testcontainers:testcontainers-redpanda:${testContainersVersion}")
}
