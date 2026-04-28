import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java-common")
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

val editorConfigFile = layout.buildDirectory.file("resources/.editorconfig").get().asFile
copyFromClasspath(".editorconfig", editorConfigFile)

spotless {
    kotlin {
        ktlint("1.7.0").setEditorConfigPath(editorConfigFile)
        target("src/**/*.kt")
        targetExclude("**/build/**")
    }
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply")
}

detekt {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(files("src/main/kotlin", "src/test/kotlin", "src/testFixtures/kotlin"))
    rootProject.file("detekt.yml").takeIf { it.exists() }?.let { config.setFrom(files(it)) }
}

tasks.check {
    dependsOn("detekt")
}

dependencies {

    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.jackson.bom))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.konsist)
}
