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

val editorConfigProvider = layout.buildDirectory.file("resources/.editorconfig")

// Materialize the shared .editorconfig as a declared task output so it survives `clean`
// (a configuration-time copy is wiped by clean before spotless runs) and is regenerated
// whenever the file goes missing. No Project receiver inside the action keeps it config-cache safe.
val materializeEditorConfig = tasks.register("materializeEditorConfig") {
    val dest = editorConfigProvider
    outputs.file(dest)
    doLast {
        val out = dest.get().asFile
        out.parentFile?.mkdirs()
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(".editorconfig")
            ?: throw GradleException("Resource not found on classpath: .editorconfig")
        stream.use { input -> out.outputStream().use { it.write(input.readBytes()) } }
    }
}

spotless {
    kotlin {
        ktlint("1.7.0").setEditorConfigPath(editorConfigProvider.get().asFile)
        target("src/**/*.kt")
        targetExclude("**/build/**")
    }
}

tasks.matching { it.name.startsWith("spotless") }.configureEach {
    dependsOn(materializeEditorConfig)
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
