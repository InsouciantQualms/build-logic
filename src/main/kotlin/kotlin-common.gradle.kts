plugins {
    id("base-common")
    kotlin("jvm")
}

configurations.all {
    resolutionStrategy {
        force(resolve("libs.kotlin.stdlib"))
        force(resolve("libs.kotlin.reflect"))
        force(resolve("libs.jetbrains.annotations"))
        force(resolve("libs.opentest4j"))
    }
}

kotlin {
    jvmToolchain(resolve("libs.versions.jdk").toInt())
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
