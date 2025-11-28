plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
    id("test-library-conventions")
}

dependencies {
    val testContainersVersion: String by project

    implementation(project(":invirt-utils"))
    implementation(project(":invirt-kafka"))

    implementation("org.testcontainers:testcontainers-redpanda:${testContainersVersion}")
}
