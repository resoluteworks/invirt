plugins {
    id("common-conventions")
    id("publish-conventions")
    id("test-conventions")
    id("test-library-conventions")
}

description = "Provides a MongoDB Testcontainer along with helpers for spying on collections and asserting documents."

dependencies {
    implementation(project(":invirt-data"))
    implementation(project(":invirt-utils"))
    implementation(project(":invirt-core"))
    implementation(project(":invirt-mongodb"))
}
