plugins {
    id("common-conventions")
}

dependencies {
    val kotestVersion = providers.gradleProperty("kotestVersion").get()
    val mockkVersion = providers.gradleProperty("mockkVersion").get()
    val testContainersVersion = providers.gradleProperty("testContainersVersion").get()
    val awaitilityVersion = providers.gradleProperty("awaitilityVersion").get()

    api("io.kotest:kotest-assertions-core:${kotestVersion}")
    api("io.kotest:kotest-property:${kotestVersion}")
    api("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    api("io.kotest:kotest-extensions-testcontainers:${kotestVersion}")
    api("io.mockk:mockk:${mockkVersion}")
    api("org.testcontainers:testcontainers:${testContainersVersion}")
    api("org.testcontainers:testcontainers-mongodb:${testContainersVersion}")
    api("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
}
