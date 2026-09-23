plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

description = "Defines the DataFilter, Sort and Page abstractions used to derive filtering, sorting and pagination logic from request query parameters."

dependencies {
    val http4kVersion = providers.gradleProperty("http4kVersion").get()

    implementation(platform("org.http4k:http4k-bom:${http4kVersion}"))
    implementation("org.http4k:http4k-core")
}
