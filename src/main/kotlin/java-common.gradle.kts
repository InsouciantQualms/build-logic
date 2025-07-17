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
            force(resolve("libs.jetbrains.annotations"))
            force(resolve("libs.jackson.annotations"))
            force(resolve("libs.jackson.core"))
            force(resolve("libs.jackson.databind"))
            force(resolve("libs.jackson.guava"))
            force(resolve("libs.jackson.jdk8"))
            force(resolve("libs.jackson.parameters"))
            force("com.google.guava:guava:32.1.2-jre")
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

spotless {
    java {
        palantirJavaFormat("2.38.0")
        importOrder("java", "javax", "org", "com", "")
        removeUnusedImports()
        formatAnnotations()
        target("src/**/*.java")
    }
}

dependencies {

    compileOnly(resolve("libs.jetbrains.annotations"))
    testImplementation(resolve("libs.junit.jupiter.engine"))
    testRuntimeOnly(resolve("libs.junit.platform.launcher"))
}
