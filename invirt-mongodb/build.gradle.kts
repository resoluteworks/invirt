plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

description = "Provides a thin Kotlin layer over the MongoDB Kotlin driver, with a managed client wrapper, versioned and timestamped document interfaces, and typed filters, sorts and indexes."

dependencies {
    val mongoDriverVersion = providers.gradleProperty("mongoDriverVersion").get()
    val awaitilityVersion = providers.gradleProperty("awaitilityVersion").get()
    val jacksonVersion = providers.gradleProperty("jacksonVersion").get()

    implementation(project(":invirt-utils"))
    implementation(project(":invirt-data"))

    api("org.mongodb:mongodb-driver-kotlin-sync:${mongoDriverVersion}")

    implementation("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

    testImplementation(project(":invirt-mongodb-test"))
}
