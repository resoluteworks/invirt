plugins {
    id("common-conventions")
    id("publish-conventions")
    id("test-conventions")
    id("test-library-conventions")
}

dependencies {
    val http4kVersion: String by project
    val validkVersion: String by project

    implementation(project(":invirt-core"))

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-client-okhttp")
    implementation("org.http4k:http4k-config")
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-multipart")
    implementation("org.http4k:http4k-server-netty")
    implementation("org.http4k:http4k-template-pebble")

    implementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
    implementation("io.resoluteworks:validk:${validkVersion}")
}
