plugins {
    id("common-conventions")
    id("jacoco")
}

dependencies {
    val kotestVersion: String by project
    val mockkVersion: String by project
    val testContainersVersion: String by project
    val awaitilityVersion: String by project

    testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
    testImplementation("io.kotest:kotest-property:${kotestVersion}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${kotestVersion}")
    testImplementation("io.kotest:kotest-extensions-testcontainers:${kotestVersion}")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("org.testcontainers:testcontainers:${testContainersVersion}")
    testImplementation("org.testcontainers:mongodb:${testContainersVersion}")
    testImplementation("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}
