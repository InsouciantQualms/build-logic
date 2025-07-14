plugins {
    id("base-common")
    kotlin("jvm")
}

// FIXME Need to align kotlinx dependencies better to use Kotlin 2.2.0 (collections and datetime)
configurations.all {
    resolutionStrategy {
        force(resolve("libs.kotlin.stdlib"))
    }
}

kotlin {
    jvmToolchain(resolve("libs.versions.jdk").toInt())
}

dependencies {

    implementation(platform(resolve("libs.kotlin.bom")))
    implementation(resolve("libs.kotlin.stdlib"))
    implementation(resolve("libs.kotlinx.collections.immutable"))
    implementation(resolve("libs.kotlinx.serialization.json"))
    implementation(resolve("libs.kotlinx.datetime"))

    testImplementation(resolve("kotest.runner.junit5"))
    testImplementation(resolve("kotest.assertions.core"))
    testImplementation(resolve("kotest.property"))
}
