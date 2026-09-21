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
    testImplementation("org.testcontainers:testcontainers-mongodb:${testContainersVersion}")
    testImplementation("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
    finalizedBy("jacocoTestReport")
}

jacoco {
    // The org.jacoco.core jar on the buildSrc classpath is the single JaCoCo pin; its VERSION carries a
    // build timestamp (0.8.15.2026...) that the published agent and ant artifacts do not.
    toolVersion = org.jacoco.core.JaCoCo.VERSION.substringBeforeLast(".")
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}
