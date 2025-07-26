plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    val kafkaVersion: String by project
    val http4kVersion: String by project
    val jacksonVersion: String by project

    implementation(project(":invirt-utils"))

    api("org.apache.kafka:kafka-clients:${kafkaVersion}")
    api("org.apache.kafka:kafka-streams:${kafkaVersion}")

    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-config")

    testImplementation(project(":invirt-kafka-test"))
}
