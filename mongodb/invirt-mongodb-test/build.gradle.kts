plugins {
    id("common-conventions")
    id("publish-conventions")
    id("test-conventions")
    id("test-library-conventions")
}

dependencies {
    val http4kVersion: String by project
    val kotestVersion: String by project
    val mockkVersion: String by project
    val testContainersVersion: String by project

    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))
    implementation(project(":invirt-core"))
    implementation(project(":invirt-mongodb"))
}
