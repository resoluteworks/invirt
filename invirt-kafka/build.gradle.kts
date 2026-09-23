plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

description = "Provides Kafka producer, consumer and topic management helpers built on the Kafka clients and streams APIs."

dependencies {
    val kafkaVersion = providers.gradleProperty("kafkaVersion").get()
    val http4kVersion = providers.gradleProperty("http4kVersion").get()
    val jacksonVersion = providers.gradleProperty("jacksonVersion").get()

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
