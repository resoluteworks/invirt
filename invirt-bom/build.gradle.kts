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

dependencies {
    constraints {
        rootProject.subprojects
            .filter { it.name != project.name }
            .sortedBy { it.name }
            .forEach { api(it) }
    }
}
