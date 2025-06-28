plugins {
    id("common-conventions")
}

dependencies {
    val kotestVersion: String by project
    val mockkVersion: String by project
    val testContainersVersion: String by project

    api("io.kotest:kotest-assertions-core:${kotestVersion}")
    api("io.kotest:kotest-property:${kotestVersion}")
    api("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    api("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
    api("io.mockk:mockk:${mockkVersion}")
    api("org.testcontainers:testcontainers:${testContainersVersion}")
    api("org.testcontainers:mongodb:${testContainersVersion}")
    api("org.awaitility:awaitility-kotlin:4.2.0")
}
