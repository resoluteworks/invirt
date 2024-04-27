plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-2"
    id("jacoco-report-aggregation")
}

group = "io.resoluteworks"

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("NEXUS_USERNAME"))
            password.set(System.getenv("NEXUS_PASSWORD"))
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects
        .filter { it.hasCoverage }
        .map { it.tasks.named<Test>("test").get() })

    sourceDirectories.from(subprojects
        .filter { it.hasCoverage }
        .flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    classDirectories.from(subprojects
        .filter { it.hasCoverage }
        .map { it.the<SourceSetContainer>()["main"].output })

    executionData.from(subprojects
        .filter { it.hasCoverage }
        .map { it.tasks.named<JacocoReport>("jacocoTestReport").get().executionData }
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}

val Project.hasCoverage get() = name != "invirt-bom" && name != "invirt-test"
