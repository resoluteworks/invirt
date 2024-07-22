plugins {
    id("example-app-conventions")
}

application {
    mainClass.set("examples.authentication.ApplicationKt")
}

dependencies {
    implementation("com.auth0:java-jwt:4.4.0")
}
