plugins {
    base
    id("jacoco-report-aggregation")
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.19"
    id("com.gradleup.nmcp")
}

group = "dev.invirt"

repositories {
    mavenCentral()
}

tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(
        subprojects
            .filter { it.hasCoverage }
            .map { it.tasks.named<Test>("test").get() }
    )

    sourceDirectories.from(
        subprojects
            .filter { it.hasCoverage }
            .flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    classDirectories.from(
        subprojects
            .filter { it.hasCoverage }
            .map { it.the<SourceSetContainer>()["main"].output }
    )

    executionData.from(
        subprojects
            .filter { it.hasCoverage }
            .map { it.tasks.named<JacocoReport>("jacocoTestReport").get().executionData }
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}

coverallsJacoco {
    reportSourceSets = subprojects
        .filter { it.hasCoverage }
        .map {
            File(it.projectDir, "src/main/kotlin")
        }

    reportPath = project.layout.buildDirectory.file("reports/jacoco/jacocoRootReport/jacocoRootReport.xml").get().asFile.absolutePath
}

val Project.hasCoverage
    get() = name != "invirt-bom" &&
        name != "invirt-test" &&
        name != "invirt-mongodb-test" &&
        name != "invirt-kafka-test"
