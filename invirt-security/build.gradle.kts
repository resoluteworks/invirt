plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    val http4kVersion: String by project

    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))
    implementation(project(":invirt-core"))

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")

    testImplementation(project(":invirt-test"))
    testImplementation("org.http4k:http4k-testing-kotest:${http4kVersion}")
}
