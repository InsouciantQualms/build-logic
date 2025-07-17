plugins {
    id("base-common")
    kotlin("jvm")
}

configurations.all {
    resolutionStrategy {
        force(resolve("libs.kotlin.stdlib"))
        force(resolve("libs.kotlin.stdlib.common"))
        force(resolve("libs.kotlin.stdlib.jdk8"))
        force(resolve("libs.kotlin.stdlib.jdk7"))
        force(resolve("libs.kotlin.reflect"))
        force(resolve("libs.jetbrains.annotations"))
        force(resolve("libs.opentest4j"))
    }
}

kotlin {
    jvmToolchain(resolve("libs.versions.jdk").toInt())
}

spotless {
    kotlin {
        ktlint("1.3.1") // .setEditorConfigPath("$rootDir/.editorconfig")
        target("src/**/*.kt")
        targetExclude("**/build/**")
    }
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
