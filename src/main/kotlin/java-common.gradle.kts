@file:Suppress("UnstableApiUsage")

plugins {
    id("base-common")
    java
    `java-test-fixtures`
    checkstyle
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
    maxWarnings = 0
    maxErrors = 0
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
