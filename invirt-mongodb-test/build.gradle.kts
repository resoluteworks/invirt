plugins {
    id("common-conventions")
    id("publish-conventions")
    id("test-conventions")
    id("test-library-conventions")
}

dependencies {
    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))
    implementation(project(":invirt-core"))
    implementation(project(":invirt-mongodb"))
}
