plugins {
    id("java-common")
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

kotlin {
    jvmToolchain(resolve("libs.versions.jdk").toInt())
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

val editorConfigFile = layout.buildDirectory.file("resources/.editorconfig").get().asFile
copyFromClasspath(".editorconfig", editorConfigFile)

spotless {
    kotlin {
        ktlint("1.3.1").setEditorConfigPath(editorConfigFile)
        target("src/**/*.kt")
        targetExclude("**/build/**")
    }
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply")
}

detekt {
    toolVersion = resolve("libs.versions.detekt")
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(files("src/main/kotlin", "src/test/kotlin", "src/testFixtures/kotlin"))
    rootProject.file("detekt.yml").takeIf { it.exists() }?.let { config.setFrom(files(it)) }
}

tasks.check {
    dependsOn("detekt")
}

dependencies {

    implementation(platform(resolve("libs.kotlin.bom")))
    implementation(platform(resolve("libs.jackson.bom")))

    implementation(resolve("libs.kotlin.stdlib"))
    implementation(resolve("libs.kotlinx.collections.immutable"))
    implementation(resolve("libs.kotlinx.serialization.json"))
    implementation(resolve("libs.kotlinx.datetime"))

    testImplementation(resolve("libs.kotest.runner.junit5"))
    testImplementation(resolve("libs.kotest.assertions.core"))
    testImplementation(resolve("libs.kotest.property"))
    testImplementation(resolve("libs.konsist"))
}
