plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    val http4kVersion: String by project

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
}
