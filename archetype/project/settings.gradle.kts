rootProject.name = "{{projectName}}"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("../build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
    
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            from(files("../build-logic/libs.versions.toml"))
        }
    }
}