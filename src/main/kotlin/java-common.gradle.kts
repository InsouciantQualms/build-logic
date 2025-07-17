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
        importOrder("")
        removeUnusedImports()
        formatAnnotations()
        target("src/**/*.java")
    }
}

checkstyle {
    toolVersion = "10.12.7"
    maxWarnings = 0
    maxErrors = 0
}

// Load checkstyle config from classpath immediately
val checkstyleConfigText = Thread.currentThread().contextClassLoader.getResourceAsStream("checkstyle.xml")?.bufferedReader()?.readText()
val suppressionsText = Thread.currentThread().contextClassLoader.getResourceAsStream("checkstyle-suppressions.xml")?.bufferedReader()?.readText()

if (checkstyleConfigText != null) {
    // Configure checkstyle tasks to use files from classpath
    tasks.withType<Checkstyle>().configureEach {
        doFirst {
            // Create config directory that matches Gradle's expected location
            val configDir = file("${project.rootDir}/config/checkstyle")
            configDir.mkdirs()
            
            // Write checkstyle.xml
            val configFile = File(configDir, "checkstyle.xml")
            configFile.writeText(checkstyleConfigText)
            
            // Also write suppressions file if it exists
            if (suppressionsText != null) {
                val suppressionsFile = File(configDir, "checkstyle-suppressions.xml")
                suppressionsFile.writeText(suppressionsText)
            }
            
            // Set the config file
            (this as Checkstyle).configFile = configFile
        }
    }
} else {
    logger.warn("checkstyle.xml not found in classpath - checkstyle will not be configured")
}

// Handle checkstyle dependencies
dependencies {
    checkstyle("com.puppycrawl.tools:checkstyle:10.12.7") {
        exclude(group = "com.google.collections", module = "google-collections")
    }
}

// Force resolution of checkstyle configuration conflicts
configurations.configureEach {
    if (name == "checkstyle") {
        resolutionStrategy {
            force("org.codehaus.plexus:plexus-utils:3.3.0")
            force("org.apache.commons:commons-lang3:3.8.1")
            force("org.apache.httpcomponents:httpcore:4.4.14")
            force("commons-codec:commons-codec:1.15")
        }
    }
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
}

dependencies {
    compileOnly(resolve("libs.jetbrains.annotations"))
    testImplementation(resolve("libs.junit.jupiter.engine"))
    testRuntimeOnly(resolve("libs.junit.platform.launcher"))
}
