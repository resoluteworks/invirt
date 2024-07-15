plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    val http4kVersion: String by project
    val kotestVersion: String by project
    val mockkVersion: String by project
    val testContainersVersion: String by project

    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))
    implementation(project(":invirt-core"))
    implementation(project(":invirt-mongodb"))

    implementation("io.kotest:kotest-assertions-core:${kotestVersion}")
    implementation("io.kotest:kotest-property:${kotestVersion}")
    implementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    implementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
    implementation("io.mockk:mockk:${mockkVersion}")
    implementation("org.testcontainers:testcontainers:${testContainersVersion}")
    implementation("org.testcontainers:mongodb:${testContainersVersion}")
    implementation("org.awaitility:awaitility-kotlin:4.2.0")
    implementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
}
