plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    val mongoDriverVersion: String by project

    implementation(project(":invirt-utils"))
    implementation(project(":invirt-data"))

    api("org.mongodb:mongodb-driver-kotlin-sync:${mongoDriverVersion}")

    api(platform("io.mongock:mongock-bom:5.5.1"))
    api("io.mongock:mongock-api")
    implementation("io.mongock:mongock-standalone")
    implementation("io.mongock:mongodb-sync-v4-driver")
    implementation("org.awaitility:awaitility-kotlin:4.2.2")

    testImplementation(project(":invirt-mongodb-test"))
}
