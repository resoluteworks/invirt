plugins {
    id("signing")
    `maven-publish`
    id("com.gradleup.nmcp")
}

publishing {
    val publishGit = "resoluteworks/invirt"

    repositories {
        mavenLocal()

        // The registry downstream consumers resolve every invirt version from, Maven Central carrying only the
        // releases cut with `make release`. The publish-github-packages workflow is the only writer: it runs
        // with the GITHUB_TOKEN of a GitHub Actions run, which is where both variables come from.
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/resoluteworks/invirt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components[project.extra.properties["publishComponent"]?.toString() ?: "java"])
            pom {
                name = project.name
                description = provider { project.description ?: error("${project.path} must set description in its build.gradle.kts; it becomes the POM <description>") }
                url = "https://github.com/${publishGit}"
                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "https://github.com/${publishGit}/blob/main/LICENSE"
                        distribution = "repo"
                    }
                }
                scm {
                    url = "https://github.com/${publishGit}"
                    connection = "scm:git:git://github.com/${publishGit}.git"
                    developerConnection = "scm:git:ssh://git@github.com:${publishGit}.git"
                }
                developers {
                    developer {
                        name = "Cosmin Marginean"
                    }
                }
            }
        }
    }
}

signing {
    // A developer machine holds the signing key and signs every publication, which Maven Central requires.
    // The publish workflow has no key, and GitHub Packages does not ask for signatures, so there the
    // signing tasks are skipped.
    isRequired = providers.gradleProperty("signing.keyId").isPresent
    sign(publishing.publications["mavenJava"])
}

// The Kotlin Gradle plugin resolves Bouncy Castle on its own kotlinBouncyCastleConfiguration (created once the
// signing plugin is applied) for its PGP helper tasks, at a version baked into the plugin. That version lags
// the security releases (GHSA-9pwp-9qqc-pr26 is fixed in 1.85), and Dependabot reads the resolved graph, so
// the whole org.bouncycastle group is held at a current release here regardless of what the plugin ships.
configurations.matching { it.name == "kotlinBouncyCastleConfiguration" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.bouncycastle") {
            useVersion("1.86")
            because("the Kotlin Gradle plugin's own Bouncy Castle pin lags the fix for GHSA-9pwp-9qqc-pr26 (1.85)")
        }
    }
}
