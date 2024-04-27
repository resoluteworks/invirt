plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    val mongoDriverVersion: String by project

    implementation(project(":invirt-data"))
    api("org.mongodb:mongodb-driver-kotlin-sync:${mongoDriverVersion}")

    testImplementation(project(":invirt-test"))
}
