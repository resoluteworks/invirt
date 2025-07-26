plugins {
    id("common-conventions")
    id("publish-conventions")
    id("test-conventions")
    id("test-library-conventions")
}

dependencies {
    val http4kVersion: String by project

    implementation(project(":invirt-core"))
    implementation(project(":invirt-security"))
    implementation(project(":invirt-utils"))

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
}
