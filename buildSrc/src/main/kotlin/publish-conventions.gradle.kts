plugins {
    id("signing")
    `maven-publish`
}

publishing {
    val publishGit = "resoluteworks/invirt"

    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components[project.extra.properties["publishComponent"]?.toString() ?: "java"])
            pom {
                name = project.name
                description = "${project.properties["publishDescription"]}"
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
