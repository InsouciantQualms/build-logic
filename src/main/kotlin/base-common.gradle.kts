plugins {
    base
    id("com.diffplug.spotless")
}

group = "dev.iq.${rootProject.name}"

configurations.all {
    resolutionStrategy {
        preferProjectModules()
        failOnVersionConflict()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}
