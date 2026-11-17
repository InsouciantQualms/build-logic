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

// open-java-format is the sole Java formatter (it also sorts and removes unused imports). It runs in a forked
// JVM because it uses javac internals the JDK only exposes via --add-exports; forking keeps those flags off the
// Gradle daemon. Long-string reflowing is skipped so text blocks are left as written.
val openJavaFormat: Configuration by configurations.creating {
    isCanBeConsumed = false
}

val javaSources = fileTree("src") {
    include("**/*.java")
}

val openJavaFormatApply by tasks.registering(JavaExec::class) {
    group = "formatting"
    description = "Formats Java sources in place with open-java-format (forked JVM)."
    classpath = openJavaFormat
    mainClass = "com.palantir.javaformat.java.Main"
    jvmArgs(listOf("api", "file", "parser", "tree", "util").flatMap {
        listOf("--add-exports", "jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED")
    })
    argumentProviders.add(CommandLineArgumentProvider {
        listOf("--replace", "--skip-reflowing-long-strings") + javaSources.files.map { it.absolutePath }
    })
    inputs.files(javaSources).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file(layout.buildDirectory.file("open-java-format/stamp"))
    onlyIf { !javaSources.isEmpty }
    doLast {
        outputs.files.singleFile.writeText("formatted")
    }
}

spotless {
    java {
        formatAnnotations()
        target("src/**/*.java")
        targetExclude("**/build/**")
    }
}

tasks.named("spotlessJava") {
    dependsOn(openJavaFormatApply)
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
    openJavaFormat(libs.open.java.format)
}
