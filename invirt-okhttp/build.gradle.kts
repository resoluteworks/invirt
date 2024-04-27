plugins {
    id("common-conventions")
    id("test-conventions")
    id("publish-conventions")
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
