plugins {
    id("java-common")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(resolve("libs.versions.jdk").toInt())
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
}
