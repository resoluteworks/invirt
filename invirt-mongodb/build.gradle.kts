plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

description = "Provides a thin Kotlin layer over the MongoDB Kotlin driver, with a managed client wrapper, versioned and timestamped document interfaces, typed filters, sorts and indexes, and Mongock migration helpers."

dependencies {
    val mongoDriverVersion = providers.gradleProperty("mongoDriverVersion").get()
    val awaitilityVersion = providers.gradleProperty("awaitilityVersion").get()
    val jacksonVersion = providers.gradleProperty("jacksonVersion").get()

    implementation(project(":invirt-utils"))
    implementation(project(":invirt-data"))

    api("org.mongodb:mongodb-driver-kotlin-sync:${mongoDriverVersion}")

    api(platform("io.mongock:mongock-bom:5.5.1"))
    api("io.mongock:mongock-api")
    implementation("io.mongock:mongock-standalone")
    implementation("io.mongock:mongodb-sync-v4-driver")
    constraints {
        // mongock-runner-core depends on maven-artifact 3.6.1, whose plexus-utils and commons-lang3 carry known CVEs
        implementation("org.codehaus.plexus:plexus-utils:3.6.1")
        implementation("org.apache.commons:commons-lang3:3.18.0")
    }
    implementation("org.awaitility:awaitility-kotlin:${awaitilityVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

    testImplementation(project(":invirt-mongodb-test"))
}
