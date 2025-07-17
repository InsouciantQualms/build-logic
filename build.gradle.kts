plugins {
    `kotlin-dsl`
}

dependencies {

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plantuml.gradle.plugin)
    implementation(libs.spotless.plugin)
}
