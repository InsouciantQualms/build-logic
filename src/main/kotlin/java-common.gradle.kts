@file:Suppress("UnstableApiUsage")

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("base-common")
    java
    `java-test-fixtures`
    checkstyle
}

val libs = the<LibrariesForLibs>()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
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
    toolVersion = libs.versions.checkstyle.get()
    maxWarnings = 0
    maxErrors = 0
}

tasks.withType<Checkstyle> {
    dependsOn("spotlessCheck")
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    checkstyle(libs.checkstyle)
}
