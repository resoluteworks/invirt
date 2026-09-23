buildscript {
    extra.apply{
        set("publishComponent", "javaPlatform")
    }
}

plugins {
    `java-platform`
    id("artifact-conventions")
    id("publish-conventions")
}

description = "A Maven BOM that aligns the published versions of the Invirt modules for consumers that import it as a platform dependency."

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        rootProject.subprojects
            .filter { it.name != project.name }
            .sortedBy { it.name }
            .forEach { api(project(it.path)) }
    }
}
