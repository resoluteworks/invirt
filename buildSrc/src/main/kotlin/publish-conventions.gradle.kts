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
