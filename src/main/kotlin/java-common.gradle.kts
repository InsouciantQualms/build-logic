@file:Suppress("UnstableApiUsage")

plugins {
    id("base-common")
    java
    `java-test-fixtures`
    checkstyle
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

configurations.configureEach {
    checkstyle {
        resolutionStrategy {
            force("org.codehaus.plexus:plexus-utils:3.3.0")
            force("org.apache.commons:commons-lang3:3.8.1")
            force("org.apache.httpcomponents:httpcore:4.4.14")
            force("commons-codec:commons-codec:1.15")
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
        palantirJavaFormat("2.50.0")
        importOrder("")
        removeUnusedImports()
        formatAnnotations()
        target("src/**/*.java")
        targetExclude("**/build/**")
    }
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply")
}

checkstyle {
    val resourceUrl = Thread.currentThread().contextClassLoader.getResource("checkstyle.xml")
        ?: throw GradleException("Failed to load checkstyle configuration file!")
    config = resources.text.fromUri(resourceUrl)
    toolVersion = resolve("libs.versions.checkstyle")
    maxWarnings = 999
    maxErrors = 999
}

tasks.withType<Checkstyle> {
    dependsOn("spotlessCheck")
}

dependencies {
    compileOnly(resolve("libs.jetbrains.annotations"))
    testImplementation(resolve("libs.junit.jupiter.engine"))
    testRuntimeOnly(resolve("libs.junit.platform.launcher"))
    checkstyle(resolve("libs.checkstyle"))
}
