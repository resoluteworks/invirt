plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))

    val http4kVersion: String by project
    val jacksonVersion: String by project
    val validkVersion: String by project

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-config")
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-multipart")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-template-pebble")

    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
    implementation("works.resolute:validk:${validkVersion}")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    testImplementation(project(":invirt-test"))
    testImplementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
}
