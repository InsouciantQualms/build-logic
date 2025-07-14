@file:Suppress("UnstableApiUsage")

plugins {
    id("base-common")
    java
    `java-test-fixtures`
}

configurations {
    all {
        resolutionStrategy {
            force(resolve("libs.slf4j.api"))
            force(resolve("libs.jackson.annotations"))
            force(resolve("libs.jetbrains.annotations"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(resolve("libs.versions.jdk")))
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
}

dependencies {

    compileOnly(resolve("libs.jetbrains.annotations"))
    testImplementation(resolve("libs.junit.jupiter.engine"))
    testRuntimeOnly(resolve("libs.junit.platform.launcher"))
}
